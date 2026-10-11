package game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

public class Player {
    public Vector3 position;
    public Vector3 velocity;
    public Model model;
    public ModelInstance instance;

    // Movement tuning
    private float maxSpeed = 6f;
    private float acceleration = 6f;
    private float deceleration = 12f;

    // Jump tuning
    private float gravity = -25f;
    private float jumpForce = 10f;
    private float groundLevel = 1f; // The Y coordinate of our floor

    // Dash tuning
    private int dashCount = 2;
    private float dashCooldown = 2f;

    private boolean isDashing = false;
    private float dashTimer = 0f;
    private float dashDuration = 0.05f; // How long the dash lasts
    private float dashSpeed = 50f;
    private Vector3 dashDir = new Vector3();

    public Player() {
        position = new Vector3(0f, groundLevel, 0f);
        velocity = new Vector3(0f, 0f, 0f);

        ModelBuilder modelBuilder = new ModelBuilder();
        model = modelBuilder.createBox(
            1.5f, 2f, 1.5f,
            new Material(ColorAttribute.createDiffuse(Color.ROYAL)),
            Usage.Position | Usage.Normal
        );

        instance = new ModelInstance(model, position);
    }

    public void update(float delta, OrthographicCamera camera) {

        if (dashCount < 2) {
            dashCooldown -= delta;
            if (dashCooldown <= 0) {
                dashCount++;
                dashCooldown = 2f;
                System.out.println("dashes: " + dashCount);
            }
        }

        // --- HANDLE DASHING STATE ---
        if (isDashing) {
            dashTimer -= delta;
            // Move fast in the locked dash direction, keeping current Y velocity
            velocity.set(dashDir.x * dashSpeed, velocity.y, dashDir.z * dashSpeed);

            if (dashTimer <= 0) {
                isDashing = false; // Dash finished
            }
        } else {
            // --- STANDARD MOVEMENT & INPUT ---
            Vector3 inputDir = new Vector3();

            Vector3 screenUp = new Vector3(-1f, 0f, -1f).nor();
            Vector3 screenDown = new Vector3(1f, 0f, 1f).nor();
            Vector3 screenLeft = new Vector3(-1f, 0f, 1f).nor();
            Vector3 screenRight = new Vector3(1f, 0f, -1f).nor();

            if (Gdx.input.isKeyPressed(Input.Keys.W)) inputDir.add(screenUp);
            if (Gdx.input.isKeyPressed(Input.Keys.S)) inputDir.add(screenDown);
            if (Gdx.input.isKeyPressed(Input.Keys.A)) inputDir.add(screenLeft);
            if (Gdx.input.isKeyPressed(Input.Keys.D)) inputDir.add(screenRight);

            // Trigger Dash on Shift key (if moving and grounded AND has dash)
            if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT) && !inputDir.isZero() && position.y <= groundLevel && dashCount > 0) {
                isDashing = true;
                dashCount--;
                dashTimer = dashDuration;
                dashDir.set(inputDir.nor());
                System.out.println("dashes: " + dashCount);
            }

            // Trigger Jump on Space bar (if grounded)
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && position.y <= groundLevel) {
                velocity.y = jumpForce;
            }

            // Horizontal acceleration & friction
            if (!inputDir.isZero()) {
                inputDir.nor();
                Vector3 targetVelocity = new Vector3(inputDir).scl(maxSpeed);
                velocity.x = lerp(velocity.x, targetVelocity.x, acceleration * delta);
                velocity.z = lerp(velocity.z, targetVelocity.z, acceleration * delta);
            } else {
                velocity.x = lerp(velocity.x, 0, deceleration * delta);
                velocity.z = lerp(velocity.z, 0, deceleration * delta);
            }
        }

        // --- APPLY GRAVITY ---
        if (position.y > groundLevel || velocity.y > 0) {
            velocity.y += gravity * delta;
        }

        // --- UPDATE POSITION ---
        position.mulAdd(velocity, delta);

        // Ground collision check
        if (position.y < groundLevel) {
            position.y = groundLevel;
            velocity.y = 0; // Stop falling when hitting the floor
        }

        // Sync visual transform
        instance.transform.setTranslation(position);

        // --- CAMERA FOLLOW ---
        float offsetX = 20f;
        float offsetY = 20f;
        float offsetZ = 20f;

        camera.position.set(
            position.x + offsetX,
            position.y + offsetY,
            position.z + offsetZ
        );
        camera.lookAt(position);
        camera.update();
    }

    // Helper linear interpolation function for floats
    private float lerp(float start, float end, float alpha) {
        return start + (end - start) * Math.min(1f, Math.max(0f, alpha));
    }

    public void render(ModelBatch batch, Environment env) {
        batch.render(instance, env);
    }

    public void dispose() {
        model.dispose();
    }
}
