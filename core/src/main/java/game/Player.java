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
import com.badlogic.gdx.math.collision.BoundingBox;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;

public class Player {
    public Vector3 position;
    public Vector3 velocity;
    public Model model;
    public ModelInstance instance;

    public BoundingBox bounds;

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

        instance = new ModelInstance(model, position.x, position.y, position.z);

        bounds = new BoundingBox();
        instance.calculateBoundingBox(bounds);
        bounds.mul(instance.transform);
    }

    public void update(float delta, OrthographicCamera camera, ArrayList<GameObject> gameObjects, ArrayList<Floor> floors) {

        if (dashCount < 2) {
            dashCooldown -= delta;
            if (dashCooldown <= 0) {
                dashCount++;
                dashCooldown = 2f;
                System.out.println("dashes: " + dashCount);
            }
        }

        boolean grounded = false;

        // check if grounded
        for (Floor floor : floors) {
            if (bounds.intersects(floor.getBounds())) {
                grounded = true;
                break;
            }
        }

        // HANDLE DASHING STATE
        if (isDashing) {
            dashTimer -= delta;
            // set dash velocity in locked direction
            velocity.set(dashDir.x * dashSpeed, velocity.y, dashDir.z * dashSpeed);

            if (dashTimer <= 0) {
                isDashing = false; // dash finished
            }
        } else {
            // input xz direction
            Vector3 inputDir = new Vector3();

            Vector3 vectorUp = new Vector3(-1f, 0f, -1f).nor();
            Vector3 vectorDown = new Vector3(1f, 0f, 1f).nor();
            Vector3 vectorLeft = new Vector3(-1f, 0f, 1f).nor();
            Vector3 vectorRight = new Vector3(1f, 0f, -1f).nor();

            if (Gdx.input.isKeyPressed(Input.Keys.W)) inputDir.add(vectorUp);
            if (Gdx.input.isKeyPressed(Input.Keys.S)) inputDir.add(vectorDown);
            if (Gdx.input.isKeyPressed(Input.Keys.A)) inputDir.add(vectorLeft);
            if (Gdx.input.isKeyPressed(Input.Keys.D)) inputDir.add(vectorRight);

            // dash (if moving and grounded AND has dash)
            if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT) && !inputDir.isZero() && grounded && dashCount > 0) {
                isDashing = true;
                dashCount--;
                dashTimer = dashDuration;
                dashDir.set(inputDir.nor());
                System.out.println("dashes: " + dashCount);
            }

            // jump if grounded
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && grounded) {
                velocity.y = jumpForce;
            }

            // xz movement
            if (!inputDir.isZero()) {
                // accelerate
                inputDir.nor();
                Vector3 targetVelocity = new Vector3(inputDir).scl(maxSpeed);
                velocity.x = lerp(velocity.x, targetVelocity.x, acceleration * delta);
                velocity.z = lerp(velocity.z, targetVelocity.z, acceleration * delta);
            } else {
                // decelerate
                velocity.x = lerp(velocity.x, 0, deceleration * delta);
                velocity.z = lerp(velocity.z, 0, deceleration * delta);
            }
        }

        // apply gravity or not)
        if (!grounded || velocity.y > 0) {
            velocity.y += gravity * delta;
        } else {
            velocity.y = 0;
        }

        // position update
        position.mulAdd(velocity, delta);

        // hitbox update
        instance.transform.setTranslation(position);
        instance.calculateBoundingBox(bounds);
        bounds.mul(instance.transform);

//        // --- COLLISIONS ---
//        for (GameObject obj : gameObjects) {
//            // Check if player's bounding box intersects any world object (like crates)
//            if (bounds.intersects(obj.getBounds())) {
//                // Simple collision response: push player back out based on velocity or stop movement
//                // For now, let's keep it simple: revert the movement if we hit something solid
//                position.mulAdd(velocity, -delta);
//                velocity.set(0, velocity.y, 0); // Stop horizontal movement on hit
//                instance.transform.setTranslation(position);
//                bounds.mul(instance.transform);
//            }
//        }

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

    public BoundingBox getBounds() {
        return bounds;
    }

    public void render(ModelBatch batch, Environment env) {
        batch.render(instance, env);
    }

    public void dispose() {
        model.dispose();
    }
}
