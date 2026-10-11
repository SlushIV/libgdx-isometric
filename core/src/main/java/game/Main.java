package game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

public class Main extends ApplicationAdapter {

    private OrthographicCamera camera;
    private ModelBatch modelBatch;
    private Environment environment;

    private Model floorModel;
    private Model crateModel;
    private ModelInstance floorInstance;
    private ModelInstance crateInstance;

    // Bring in our new Player object!
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

        ModelBuilder modelBuilder = new ModelBuilder();

        // Floor Platform
        floorModel = modelBuilder.createBox(
            12f, 1f, 12f,
            new Material(ColorAttribute.createDiffuse(Color.LIGHT_GRAY)),
            Usage.Position | Usage.Normal
        );
        floorInstance = new ModelInstance(floorModel, 0f, -0.5f, 0f);

        // Crate Block
        crateModel = modelBuilder.createBox(
            1.2f, 1.2f, 1.2f,
            new Material(ColorAttribute.createDiffuse(Color.CORAL)),
            Usage.Position | Usage.Normal
        );
        crateInstance = new ModelInstance(crateModel, -3f, 0.6f, -3f);

        // Initialize our player object
        player = new Player();
    }

    // GAME LOOP
    @Override
    public void render() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.15f, 0.18f, 0.25f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Pass the camera here!
        player.update(Gdx.graphics.getDeltaTime(), camera);

        modelBatch.begin(camera);
        modelBatch.render(floorInstance, environment);
        modelBatch.render(crateInstance, environment);
        modelBatch.render(player.instance, environment);
        modelBatch.end();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        floorModel.dispose();
        crateModel.dispose();
        player.dispose(); // Clean up player memory too
    }
}
