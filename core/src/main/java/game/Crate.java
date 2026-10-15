package game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.collision.BoundingBox;

public class Crate implements Collidable, Renderable {

    public Model model;
    public ModelInstance instance;
    public BoundingBox bounds;

    private static Model sharedCrateModel = null; // Optional, share model memory if you have many crates

    public Crate(float x, float y, float z) {
        // box model (load a Blender asset here later)
        ModelBuilder modelBuilder = new ModelBuilder();
        model = modelBuilder.createBox(
            1.2f, 1.2f, 1.2f,
            new Material(ColorAttribute.createDiffuse(Color.CORAL)),
            Usage.Position | Usage.Normal
        );

        instance = new ModelInstance(model, x, y, z);

        // calculate world-space bounding box for collision
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
    public void dispose() {
        model.dispose();
    }
}
