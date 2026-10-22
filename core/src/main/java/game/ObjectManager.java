package game;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;

public class ObjectManager {

    private final ArrayList<Updatable> updatables = new ArrayList<>();
    private final ArrayList<Renderable> renderables = new ArrayList<>();
    private final ArrayList<Collidable> collidables = new ArrayList<>();
    private final Grid grid;

    public ObjectManager(int width, int height) {
        grid = new Grid(width, height);
    }

    public ObjectManager() {
        grid = new Grid(50, 50);
    }

    public void add(Object entity) {
        if (entity instanceof Updatable u) updatables.add(u);
        if (entity instanceof Renderable r) renderables.add(r);
        if (entity instanceof Collidable c) {
            collidables.add(c);
            grid.addObstacle(c);
        }
    }

    public void update(float delta, ObjectManager objectManager) {
        // ONLY objects with movement/timers get updated (bullets, dynamic platforms, enemies)
        for (int i = updatables.size() - 1; i >= 0; i--) {
            Updatable u = updatables.get(i);
            u.update(delta, objectManager);

            if (!u.isAlive()) {
                updatables.remove(i);
                renderables.remove(u);

                if (u instanceof Collidable c) {
                    collidables.remove(c);
                    grid.removeObstacle(c);
                }

                if (u instanceof Disposable d) d.dispose();
            }
        }
    }

    public void render(ModelBatch batch, Environment env) {
        for (Renderable r : renderables) {
            r.render(batch, env);
        }
    }

    public void dispose() {
        for (Renderable r : renderables) {
            r.dispose();
        }
    }

    public ArrayList<Collidable> getCollidables() {
        return collidables;
    }

    public ArrayList<Updatable> getUpdatables() {
        return updatables;
    }

    public ArrayList<Renderable> getRenderables() {
        return renderables;
    }

    public Grid getGrid() {
        return grid;
    }

    public int[][] getGridArray() {
        return grid.getGrid();
    }
}
