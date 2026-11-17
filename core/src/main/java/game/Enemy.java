package game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import java.util.ArrayDeque;

public class Enemy implements Renderable, Updatable {

    public final Vector3 position = new Vector3();
    public final Vector3 velocity = new Vector3();
    public Model model;
    public ModelInstance instance;

    private final Player target;

    // bounding box caching
    private final BoundingBox baseBounds = new BoundingBox();
    public final BoundingBox bounds = new BoundingBox();

    private float width = 1f;
    private float height = 2f;
    private float depth = 1f;
    private float skin = 0.001f; // Padding to prevent floating-point wall sticking

    // hp tuning
    private int maxHp = 100;
    private int currentHp = 67;

    // ammo tuning
    private int currentAmmo;
    private int maxAmmo;

    // movement tuning
    private float maxSpeed = 5f;
    private float acceleration = 5f;
    private float deceleration = 25f;
    private Vector3 targetVelocity = new Vector3();

    // path
    private ArrayDeque<Vector3> path = new ArrayDeque<>();

    public Enemy(float x, float y, float z, Player p) {
        target = p;
        position.set(x, y, z);
        velocity.setZero();

        ModelBuilder modelBuilder = new ModelBuilder();
        model = modelBuilder.createBox(
            width, height, depth,
            new Material(ColorAttribute.createDiffuse(Color.RED)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );

        instance = new ModelInstance(model, position);
        model.calculateBoundingBox(baseBounds);
        updateBounds();
    }


    @Override
    public void update(float delta, ObjectManager objectManager) {
        // do pathfind in a q idk
        Vector3 destination = findShortestPath(objectManager);

        if (bounds.intersects(target.getBounds())) {
            velocity.x = 0;
            velocity.z = 0;
        } else {

            // go some steps
            targetVelocity.set(destination).sub(position); // sub position to get movement vector relative to self position (not world origin!)

            targetVelocity.y = 0f; // Keep movement strictly horizontal

            if (!targetVelocity.isZero()) {
                targetVelocity.nor().scl(maxSpeed);
            }

            float alpha = 1f - (float) Math.exp(-acceleration * delta);
            velocity.x = MathUtils.lerp(velocity.x, targetVelocity.x, alpha);
            velocity.z = MathUtils.lerp(velocity.z, targetVelocity.z, alpha);
        }

        position.x += velocity.x * delta;
        position.z += velocity.z * delta;
        updateBounds();

        // update hp n shi
    }

    // this is NOT A* YET THIS IS VERY PRIMITIVE AND NOT SMART
    public Vector3 findShortestPath(ObjectManager objectManager) {

        int[] coords = objectManager.getGrid().getGridIndices(position.x, position.z);
        int gridX = coords[0];
        int gridZ = coords[1];

        Vector3 destination = position; // hopefully theres a spot thats closer than the current destination lmao

        for (int i = gridX - 1; i <= gridX + 1; i++) {
            for (int j = gridZ - 1; j <= gridZ + 1; j++) {

                if (i == gridX && j == gridZ) {
                    continue;
                }

                if (objectManager.getGrid().isWalkable(i, j)) {
                    // compares current dest vector with possible new dest vector (the coordinate of the grid indices)
                    Vector3 candidate = objectManager.getGrid().getWorldCoordinates(i, j);
                    if (target.getPosition().dst(candidate) < target.getPosition().dst(destination)) {
                        destination = candidate;
                    }
                }
            }
        }

        return destination;
    }

    @Override
    public boolean isAlive() {
        return currentHp > 0;
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

    public int getCurrentHp() {
        return currentHp;
    }


    public BoundingBox getBounds() {
        return bounds;
    }

    public Vector3 getPosition() {
        return position;
    }
}
