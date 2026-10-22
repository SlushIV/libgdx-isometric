package game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class Floor implements Collidable, Renderable {
    public Model model;
    public ModelInstance instance;
    public BoundingBox bounds;

    public final Vector3 position = new Vector3();

    public Floor(float x, float y, float z, float width, float depth) {
        position.set(x, y, z);

        ModelBuilder modelBuilder = new ModelBuilder();
        model = modelBuilder.createBox(
            width, 1f, depth,
            new Material(ColorAttribute.createDiffuse(Color.LIGHT_GRAY)),
            Usage.Position | Usage.Normal
        );

        instance = new ModelInstance(model, position);

        bounds = new BoundingBox();
        instance.calculateBoundingBox(bounds);
        bounds.mul(instance.transform);
    }

    @Override
    public void render(ModelBatch batch, Environment env) {
        batch.render(instance, env);
    }

    @Override
    public BoundingBox getBounds() {
        return bounds;
    }

    @Override
    public Vector3 getPosition() {
        return position;
    }

    @Override
    public void dispose() {
        model.dispose();
    }
}
