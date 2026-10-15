package game;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;

public interface Renderable {
    void render(ModelBatch batch, Environment env);
    void dispose();
}
