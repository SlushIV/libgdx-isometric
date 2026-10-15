package game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;

import java.util.ArrayList;

public class Main extends ApplicationAdapter {

    private OrthographicCamera camera;
    private ModelBatch modelBatch;
    private Environment environment;

    private final ObjectManager objectManager = new ObjectManager();

    private Player player;

    // zoom manipulation later perhaps? (the larger the zoom the further away)
    public float zoom = 25f;

    @Override
    public void create() {
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        modelBatch = new ModelBatch();

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        camera = new OrthographicCamera(zoom * (w / h), zoom);
        camera.position.set(20f, 20f, 20f);
        camera.lookAt(0f, 0f, 0f);
        camera.near = 1f;
        camera.far = 300f;
        camera.update();

        // initialize objects
        player = new Player();

        objectManager.add(new Floor(0f, -0.5f, 0f, 12f, 12f));
        objectManager.add(new Floor(12f, -0.5f, 0f, 12f, 12f));
        objectManager.add(new Floor(0f, -0.5f, 12f, 12f, 12f));

        objectManager.add(new Crate(-3f, 0.6f, -3f));
        objectManager.add(new Crate(2f, 0.6f, 4f));
    }

    // GAME LOOP
    @Override
    public void render() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.15f, 0.18f, 0.25f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(camera);

        // game time
        float delta = Gdx.graphics.getDeltaTime();

        // update player and rest
        player.update(delta, camera, objectManager);
        objectManager.update(delta, objectManager);

        // render
        player.render(modelBatch, environment);
        objectManager.render(modelBatch, environment);

        modelBatch.end();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        player.dispose();
        objectManager.dispose();
    }
}
