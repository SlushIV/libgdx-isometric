package game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import java.util.ArrayList;

public class Bullet {

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

    public void update(float delta, ArrayList<StaticGameObject> staticGameObjects) {
        lifetime -= delta;

        if (lifetime <= 0) {
            alive = false;
            return;
        }

        for (StaticGameObject obj : staticGameObjects) {
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
