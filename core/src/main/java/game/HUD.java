package game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class HUD implements Disposable {

    private final Stage stage;
    private final BitmapFont font;

    // UI elements
    private final ProgressBar hpBar;
    private final Label hpLabel;

    private final ProgressBar dashBar;
    private final Label dashLabel;

    private final Label ammoLabel;
    private final Label scoreLabel;

    // textures (saved for disposal)
    private final Texture bgTex;
    private final Texture hpTex;
    private final Texture dashTex;

    public HUD() {
        this.stage = new Stage(new ScreenViewport());

        // font shi (internal treats assets folder as root! so its actually /assets/fonts/font-name.ttf)
        if (Gdx.files.internal("fonts/JetBrainsMono-Regular.ttf").exists()) {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/JetBrainsMono-Regular.ttf"));
            try {
                FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
                parameter.size = 28;
                font = generator.generateFont(parameter);
            } finally {
                generator.dispose();
            }
        } else {
            System.out.println("no font uh oh");
            font = new BitmapFont(); // libgdx default font
        }

        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);

        // solid colored textures for progress bars using Pixmap
        bgTex = createSolidTexture(Color.DARK_GRAY);
        hpTex = createSolidTexture(Color.RED);
        dashTex = createSolidTexture(Color.CYAN);

        // hp bar setup
        ProgressBar.ProgressBarStyle hpStyle = new ProgressBar.ProgressBarStyle();
        hpStyle.background = new TextureRegionDrawable(new TextureRegion(bgTex));
        hpStyle.knobBefore = new TextureRegionDrawable(new TextureRegion(hpTex));

        hpBar = new ProgressBar(0, 100, 1, false, hpStyle);
        hpBar.getStyle().background.setMinHeight(32);
        hpBar.getStyle().knobBefore.setMinHeight(32);

        // dash bar setup
        ProgressBar.ProgressBarStyle dashStyle = new ProgressBar.ProgressBarStyle();
        dashStyle.background = new TextureRegionDrawable(new TextureRegion(bgTex));
        dashStyle.knobBefore = new TextureRegionDrawable(new TextureRegion(dashTex));

        dashBar = new ProgressBar(0, 100, 1, false, dashStyle);
        dashBar.getStyle().background.setMinHeight(32);
        dashBar.getStyle().knobBefore.setMinHeight(32);

        // label setup
        hpLabel = new Label("", labelStyle);
        dashLabel = new Label("", labelStyle);
        ammoLabel = new Label("", labelStyle);
        scoreLabel = new Label("", labelStyle);

        // table to fill screen from top
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        // top left subtable
        Table topLeftTable = new Table();
        topLeftTable.add(hpLabel).left().row();
        topLeftTable.add(hpBar).width(400).height(16).padTop(16).left().row(); // .row() is basically a line break

        topLeftTable.add(dashLabel).padTop(32).left().row();
        topLeftTable.add(dashBar).width(400).height(16).padTop(16).left().row();

        // top right subtable
        Table topRightTable = new Table();
        topRightTable.add(scoreLabel).right();

        // bottom left subtable (empty atm maybe add stuff later?)
        Table bottomLeftTable = new Table();

        // bottom right subtable
        Table bottomRightTable = new Table();
        bottomRightTable.add(ammoLabel).right();

        // add tables to table (lol)
        rootTable.top();
        rootTable.add(topLeftTable).left().pad(20).expandX(); // top tables
        rootTable.add(topRightTable).right().pad(20).top().row();

        rootTable.add().expandY().colspan(2).row(); // middle table to fill 2 columns (colspan(2)) vertically SINCE WE HAVE 2 COLUMNS WOW

        rootTable.add(bottomLeftTable).expandX(); // bottom tables
        rootTable.add(bottomRightTable).right().pad(20);
    }

    /**
     * Call this every frame in render() to update HUD readings from game state.
     * @param dashProgress Float value from 0.0 (empty) to 1.0 (fully charged)
     */
    public void update(int currentHp, int dashCount, float dashProgress, int ammo, int score) {
        // REMEMBER TO CHANGE THE MAX HP TO BE DYNAMICALLY ADJUSTABLE LATER
        hpBar.setRange(0, 100);
        hpBar.setValue(currentHp);
        hpLabel.setText("HP: " + currentHp + " / " + 100);

        // REMEMBER TO CHANGE THE MAX DASHES TO BE DYNAMICALLY ADJUSTABLE LATER
        dashLabel.setText("DASHES: " + dashCount + " / " + 2);
        dashBar.setValue(dashProgress * 100f);

        ammoLabel.setText("AMMO: " + ammo);
        scoreLabel.setText("SCORE: " + score);

        stage.act(Gdx.graphics.getDeltaTime());
    }

    public void render() {
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    /** Helper to create 1x1 colored textures without needing PNG files */
    private Texture createSolidTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    @Override
    public void dispose() {
        stage.dispose();
        font.dispose();
        bgTex.dispose();
        hpTex.dispose();
        dashTex.dispose();
    }

    /** AI GENERATED INTUITIVE LAYOUT OF THE UI
    +===================================================================================+
    | rootTable (setFillParent: TRUE, top-aligned)                                      |
    |                                                                                   |
    |  +---------------------------------------+     +-------------------------------+  |
    |  | topLeftTable                          |     | topRightTable                 |  |
    |  | (.left().pad(20).expandX())           |     | (.right().pad(20).top())      |  |
    |  |                                       |     |                               |  |
    |  |  [hpLabel]                            |     |                   [scoreLabel]|  |
    |  |  (padTop: 16px)                       |     +-------------------------------+  |
    |  |  [=============== hpBar ==============]                                        |
    |  |  (width: 400px, height: 16px)                                                  |
    |  |                                                                                |
    |  |  (padTop: 32px)                                                                |
    |  |  [dashLabel]                                                                   |
    |  |  (padTop: 16px)                                                                |
    |  |  [============== dashBar =============]                                        |
    |  |  (width: 400px, height: 16px)                                                  |
    |  +---------------------------------------+                                        |
    |                                                                                   |
    |                                                                                   |
    |===================================================================================|
    |                                                                                   |
    |                   EMPTY CELL WITH .expandY().colspan(2)                           |
    |                   (Absorbs all vertical space to push                             |
    |                    Row 1 to top and Row 3 to bottom)                              |
    |                                                                                   |
    |===================================================================================|
    |                                                                                   |
    |  EMPTY CELL (.expandX())                       +-------------------------------+  |
    |  (Pushes bottomRightTable to edge)             | bottomRightTable              |  |
    |                                                | (.right().pad(20))            |  |
    |                                                |                               |  |
    |                                                |                    [ammoLabel]|  |
    |                                                +-------------------------------+  |
    +===================================================================================+
    */
}
