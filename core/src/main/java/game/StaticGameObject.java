package game;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.collision.BoundingBox;

public interface StaticGameObject {

    void render(ModelBatch batch, Environment env);
    BoundingBox getBounds();
    void dispose();
}
