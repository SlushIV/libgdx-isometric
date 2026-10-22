package game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.collision.Ray;

import java.util.ArrayList;

public class Player {
    public final Vector3 position = new Vector3();
    public final Vector3 velocity = new Vector3();
    public Model model;
    public ModelInstance instance;

    // bounding box caching
    private final BoundingBox baseBounds = new BoundingBox();
    public final BoundingBox bounds = new BoundingBox();

    private float width = 1.5f;
    private float height = 2f;
    private float depth = 1.5f;
    private float skin = 0.001f; // Padding to prevent floating-point wall sticking

    // hp tuning
    private int maxHp = 100;
    private int currentHp = 67;

    // ammo tuning
    private int currentAmmo;
    private int maxAmmo;

    // movement tuning
    private float maxSpeed = 6f;
    private float acceleration = 12f;
    private float deceleration = 24f;

    // jump tuning
    private float gravity = -25f;
    private float jumpForce = 10f;

    // dash tuning
    private int maxDashes = 2;
    private int dashCount = maxDashes;
    private float currentDashCooldown = 0f;
    private float maxDashCooldown = 2f;

    private boolean isDashing = false;
    private float dashTimer = 0f;
    private float dashDuration = 0.05f;
    private float dashSpeed = 50f;
    private final Vector3 dashDir = new Vector3();

    // bullet tuning
    private ArrayList<Bullet> bullets = new ArrayList<>();

    // this is for calculating the mouse coords in the isometric world
    private final Ray mouseRay = new Ray();
    private final Plane groundPlane = new Plane(new Vector3(0, 1, 0), 0);
    private final Vector3 mouseWorldPos = new Vector3();
    private final Vector3 planeNormal = new Vector3(0, 1, 0);

    // directional vectors
    // INTUITION ALIGNED
    private final Vector3 vectorUp = new Vector3(-1f, 0f, -1f).nor();
    private final Vector3 vectorDown = new Vector3(1f, 0f, 1f).nor();
    private final Vector3 vectorLeft = new Vector3(-1f, 0f, 1f).nor();
    private final Vector3 vectorRight = new Vector3(1f, 0f, -1f).nor();

    // GRID ALIGNED
//    private final Vector3 vectorUp = new Vector3(0, 0f, -1f).nor();
//    private final Vector3 vectorDown = new Vector3(0, 0f, 1f).nor();
//    private final Vector3 vectorLeft = new Vector3(-1f, 0f, 0).nor();
//    private final Vector3 vectorRight = new Vector3(1f, 0f, 0).nor();

    private final Vector3 inputDir = new Vector3();
    private final Vector3 targetVelocity = new Vector3();

    private final Vector3 tmpObjectCenter = new Vector3(); // object center calculations

    // camera offset relative to player
    private final Vector3 cameraOffset = new Vector3(20f, 20f, 20f);

    public Player() {
        position.set(0f, 1f, 0f);
        velocity.setZero();

        ModelBuilder modelBuilder = new ModelBuilder();
        model = modelBuilder.createBox(
            width, height, depth,
            new Material(ColorAttribute.createDiffuse(Color.ROYAL)),
            Usage.Position | Usage.Normal
        );

        instance = new ModelInstance(model, position);
        model.calculateBoundingBox(baseBounds);
        updateBounds();
    }

    public void update(float delta, OrthographicCamera camera, ObjectManager objectManager) {

        // ground check
        boolean grounded = checkGrounded(objectManager.getCollidables());

        // something if player falls too low (rn just spawn again)
        if (position.y < -50f) {
            position.set(0f, 1f, 0f);
            velocity.setZero();
        }

        // prolly temporary but buller queue handling
        for (int i = bullets.size() - 1; i >= 0; i--) {

            if (!bullets.get(i).isAlive()) {
                bullets.remove(i);
            }
        }

        // dash cooldown recovery
        if (dashCount < maxDashes) {
            currentDashCooldown -= delta;
            if (currentDashCooldown <= 0) {
                dashCount++;
                currentDashCooldown = maxDashCooldown;
            }
        }

        // input states
        if (isDashing) {
            dashTimer -= delta;
            velocity.x = dashDir.x * dashSpeed;
            velocity.z = dashDir.z * dashSpeed;

            if (dashTimer <= 0) {
                isDashing = false; // dash finished
            }
        } else {
            inputDir.setZero();

            if (Gdx.input.isKeyPressed(Input.Keys.W)) inputDir.add(vectorUp);
            if (Gdx.input.isKeyPressed(Input.Keys.S)) inputDir.add(vectorDown);
            if (Gdx.input.isKeyPressed(Input.Keys.A)) inputDir.add(vectorLeft);
            if (Gdx.input.isKeyPressed(Input.Keys.D)) inputDir.add(vectorRight);

            if (!inputDir.isZero()) {
                inputDir.nor();
            }

            // dash
            if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT) && !inputDir.isZero() && dashCount > 0) {
                isDashing = true;

                // only reset cooldown if its the first dash otherwise it fucks up the cooldown of the first to recover dash
                if (dashCount >= maxDashes) {
                    currentDashCooldown = maxDashCooldown;
                    dashTimer = dashDuration;
                }

                dashCount--;
                dashDir.set(inputDir);
            }

            // jump
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && grounded) {
                velocity.y = jumpForce;
                grounded = false;
            }

            // shoot
            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                // calculate 3D mouse location (bullet dest)
                Vector3 targetPoint = getMouseWorldPosition(camera);
                Bullet bullet = new Bullet(position, targetPoint);
                objectManager.add(bullet);
                bullets.add(bullet);

            }

            // turn bullet into platform (please)
            if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
                if (!bullets.isEmpty()) {
                    bullets.getLast().turnStatic(objectManager);
                }
            }

            // exponential velocity smoothing
            if (!inputDir.isZero()) {
                targetVelocity.set(inputDir).scl(maxSpeed);
                float alpha = 1f - (float) Math.exp(-acceleration * delta);
                velocity.x = MathUtils.lerp(velocity.x, targetVelocity.x, alpha);
                velocity.z = MathUtils.lerp(velocity.z, targetVelocity.z, alpha);
            } else {
                float alpha = 1f - (float) Math.exp(-deceleration * delta);
                velocity.x = MathUtils.lerp(velocity.x, 0f, alpha);
                velocity.z = MathUtils.lerp(velocity.z, 0f, alpha);
            }
        }

        // gravity
        if (!grounded) {
            velocity.y += gravity * delta;
        }

        // y axis
        position.y += velocity.y * delta;
        updateBounds();

        for (Collidable obj : objectManager.getCollidables()) {
            if (bounds.intersects(obj.getBounds())) {
                obj.getBounds().getCenter(tmpObjectCenter);
                if (position.y > tmpObjectCenter.y) {
                    // Landed on floor / top of object
                    position.y = obj.getBounds().max.y + (height / 2f) + skin;
                    velocity.y = 0f;
                } else {
                    // Hit ceiling / bottom of object
                    position.y = obj.getBounds().min.y - (height / 2f) - skin;
                    velocity.y = 0f;
                }
                updateBounds();
                break;
            }
        }

        // x axis
        position.x += velocity.x * delta;
        updateBounds();

        for (Collidable obj : objectManager.getCollidables()) {
            if (bounds.intersects(obj.getBounds())) {
                obj.getBounds().getCenter(tmpObjectCenter);
                if (position.x < tmpObjectCenter.x) {
                    // Collided with left side
                    position.x = obj.getBounds().min.x - (width / 2f) - skin;
                } else {
                    // Collided with right side
                    position.x = obj.getBounds().max.x + (width / 2f) + skin;
                }
                velocity.x = 0f;
                updateBounds();
                break;
            }
        }

        // z axis
        position.z += velocity.z * delta;
        updateBounds();

        for (Collidable obj : objectManager.getCollidables()) {
            if (bounds.intersects(obj.getBounds())) {
                obj.getBounds().getCenter(tmpObjectCenter);
                if (position.z < tmpObjectCenter.z) {
                    // collided with front side
                    position.z = obj.getBounds().min.z - (depth / 2f) - skin;
                } else {
                    // collided with back side
                    position.z = obj.getBounds().max.z + (depth / 2f) + skin;
                }
                velocity.z = 0f;
                updateBounds();
                break;
            }
        }

        // camera
        camera.position.set(position).add(cameraOffset);
        camera.lookAt(position);
        camera.update();
    }

    /**
     * Converts 2D mouse screen coordinates into a 3D world position (X, Y, Z)
     * on a horizontal plane matching the player's current height.
     */
    public Vector3 getMouseWorldPosition(OrthographicCamera camera) {
        // cast ray from camera through mouse cursor position
        mouseRay.set(camera.getPickRay(Gdx.input.getX(), Gdx.input.getY()));

        // set horizontal plane height equal to player's current Y height
        groundPlane.set(position, planeNormal);

        // find 3D intersection coordinate
        if (Intersector.intersectRayPlane(mouseRay, groundPlane, mouseWorldPos)) {
            return mouseWorldPos;
        }

        return position; // fallback
    }

    private boolean checkGrounded(ArrayList<Collidable> collidables) {
        float probeDistance = 0.02f;
        position.y -= probeDistance;
        updateBounds();

        boolean onGround = false;
        for (Collidable obj : collidables) {
            if (bounds.intersects(obj.getBounds())) {
                onGround = true;
                break;
            }
        }

        position.y += probeDistance;
        updateBounds();

        return onGround;
    }

    public BoundingBox getBounds() {
        return bounds;
    }

    private void updateBounds() {
        instance.transform.setTranslation(position);
        bounds.set(baseBounds).mul(instance.transform);
    }

    public void render(ModelBatch batch, Environment env) {
        batch.render(instance, env);
    }

    public void dispose() {
        model.dispose();
    }

    public int getDashCount() {
        return dashCount;
    }

    public float getDashCooldownRatio() {
        return currentDashCooldown / maxDashCooldown;
    } // 0.0f to 1.0f

    public int getCurrentHp() {
        return currentHp;
    }

    public int getCurrentAmmo() {
        return currentAmmo;
    }
}
