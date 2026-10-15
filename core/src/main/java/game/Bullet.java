package game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;

public class Bullet implements Renderable, Updatable {

    public Model model;
    public ModelInstance instance;

    // bounding box caching
    private final BoundingBox baseBounds = new BoundingBox();
    public final BoundingBox bounds = new BoundingBox();

    private float width = 0.2f;
    private float height = 0.2f;
    private float depth = 0.2f;

    // movement tuning
    private float maxSpeed = 24f;
    private float acceleration = 18f;

    public final Vector3 position = new Vector3();
    public final Vector3 direction = new Vector3();
    public final Vector3 velocity = new Vector3();
    private final Vector3 targetVelocity = new Vector3();

    // lifetime
    private float lifetime = 5.0f;
    private boolean alive = true;

    public Bullet(Vector3 playerPosition, Vector3 mousePosition) {
        position.set(playerPosition);

        // then subtract mouse target - player position and do .nor to have a vector for direction
        direction.set(mousePosition).sub(position);
        direction.nor();

        // change position so bullet does NOT spawn inside player
        position.set(position.add(direction.scl(1.5f)));

        velocity.setZero();
        targetVelocity.set(direction).scl(maxSpeed);

        ModelBuilder modelBuilder = new ModelBuilder();
        model = modelBuilder.createBox(
            width, height, depth,
            new Material(ColorAttribute.createDiffuse(Color.ROYAL)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );

        instance = new ModelInstance(model, position);
        model.calculateBoundingBox(baseBounds);
        updateBounds();
    }

    public void update(float delta, ObjectManager objectManager) {

        lifetime -= delta;

        if (lifetime <= 0) {
            alive = false;
            return;
        }

        for (Collidable obj : objectManager.getCollidables()) {
            if (bounds.intersects(obj.getBounds())) {
                alive = false;
                return;
            }
        }

        float alpha = 1f - (float) Math.exp(-acceleration * delta);
        velocity.x = MathUtils.lerp(velocity.x, targetVelocity.x, alpha);
        velocity.z = MathUtils.lerp(velocity.z, targetVelocity.z, alpha);
        position.x += velocity.x * delta;
        position.z += velocity.z * delta;
        updateBounds();

    }

    public void turnStatic(ObjectManager objectManager) {
        if (alive) {
            alive = false;

            // reset orientation to world grid and scale XZ to platform size
            velocity.setZero();
            instance.transform.setToTranslation(position);
            updateBounds();

            // add BulletPlatform to collidables and delete the "real" bullet
            objectManager.add(new BulletPlatform(position));
            System.out.println(position.x + " " + position.y + " " + position.z);
        }
    }

    public BoundingBox getBounds() {
        return bounds;
    }

    public boolean isAlive() {
        return alive;
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
}
