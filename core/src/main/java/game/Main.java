package game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import java.util.ArrayList;

public class Main extends ApplicationAdapter {

    private OrthographicCamera camera;
    private ModelBatch modelBatch;
    private Environment environment;

    private ArrayList<StaticGameObject> staticGameObjects;
    private ArrayList<Bullet> playerBullets;
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

        staticGameObjects = new ArrayList<>();
        playerBullets = new ArrayList<>();

        staticGameObjects.add(new Floor(0f, -0.5f, 0f, 12f, 12f));
        staticGameObjects.add(new Floor(12f, -0.5f, 0f, 12f, 12f));
        staticGameObjects.add(new Floor(0f, -0.5f, 12f, 12f, 12f));

        staticGameObjects.add(new Crate(-3f, 0.6f, -3f));
        staticGameObjects.add(new Crate(2f, 0.6f, 4f));
    }

    // GAME LOOP
    @Override
    public void render() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.15f, 0.18f, 0.25f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // game time
        float delta = Gdx.graphics.getDeltaTime();

        // update player logic
        player.update(delta, camera, staticGameObjects, playerBullets);

        modelBatch.begin(camera);

        // updates all active bullets and removes dead ones, count down to avoid any index shift after delete skips
        for (int i = playerBullets.size() - 1; i >= 0; i--) {
            Bullet bullet = playerBullets.get(i);
            bullet.update(delta, staticGameObjects);

            if (!bullet.isAlive()) {
                bullet.dispose();
                playerBullets.remove(i);
            }
        }

        // render objs
        for (StaticGameObject obj : staticGameObjects) {
            obj.render(modelBatch, environment);
        }

        for (Bullet bullet : playerBullets) {
            bullet.render(modelBatch, environment);
        }

        // render player
        player.render(modelBatch, environment);

        modelBatch.end();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        player.dispose();

        // clean up memory for all floors and crates!
        for (StaticGameObject obj : staticGameObjects) {
            obj.dispose();
        }

        for (Bullet bullet : playerBullets) {
            bullet.dispose();
        }
    }
}
