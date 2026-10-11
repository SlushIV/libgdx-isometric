package game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

import java.util.ArrayList;

public class Main extends ApplicationAdapter {

    private OrthographicCamera camera;
    private ModelBatch modelBatch;
    private Environment environment;

    private ArrayList<GameObject> gameObjects;
    private Player player;


    @Override
    public void create() {
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        modelBatch = new ModelBatch();

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        float zoom = 15f;

        camera = new OrthographicCamera(zoom * (w / h), zoom);
        camera.position.set(20f, 20f, 20f);
        camera.lookAt(0f, 0f, 0f);
        camera.near = 1f;
        camera.far = 300f;
        camera.update();

        // Initialize objects
        player = new Player();

        gameObjects = new ArrayList<>();

        gameObjects.add(new Floor(0f, -0.5f, 0f, 12f, 12f));
        gameObjects.add(new Floor(12f, -0.5f, 0f, 12f, 12f));
        gameObjects.add(new Floor(0f, -0.5f, 12f, 12f, 12f));

        gameObjects.add(new Crate(-3f, 0.6f, -3f));
        gameObjects.add(new Crate(2f, 0.6f, 4f));
    }

    // GAME LOOP
    @Override
    public void render() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.15f, 0.18f, 0.25f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Update player logic
        player.update(Gdx.graphics.getDeltaTime(), camera);

        modelBatch.begin(camera);

        // render objs
        for (GameObject obj : gameObjects) {
            obj.render(modelBatch, environment);
        }

        // render player
        player.render(modelBatch, environment);

        modelBatch.end();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        player.dispose();

        // Clean up memory for all floors and crates!
        for (GameObject obj : gameObjects) {
            obj.dispose();
        }
    }
}
