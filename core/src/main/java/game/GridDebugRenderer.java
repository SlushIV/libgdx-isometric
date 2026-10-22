package game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class GridDebugRenderer {

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    public void render(Grid grid, OrthographicCamera camera) {
        // depth testing so grid lines hide behind 3D walls/models
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        int[][] data = grid.getGrid();
        float size = grid.gridSize;
        float offX = grid.offsetX;
        float offZ = grid.offsetZ;
        float y = 0.02f; // Y offset to float lines right above the ground

        for (int x = 0; x < grid.width; x++) {
            for (int z = 0; z < grid.height; z++) {
                // convert grid indices back into 3D world corners
                float minX = (x * size) - offX;
                float minZ = (z * size) - offZ;
                float maxX = minX + size;
                float maxZ = minZ + size;

                // red for blocked white for walkable
                if (data[x][z] > 0) {
                    shapeRenderer.setColor(Color.RED);
                } else {
                    shapeRenderer.setColor(Color.WHITE);
                }

                // Draw tile boundary box on the XZ floor plane
                shapeRenderer.line(minX, y, minZ, maxX, y, minZ); // Bottom
                shapeRenderer.line(minX, y, maxZ, maxX, y, maxZ); // Top
                shapeRenderer.line(minX, y, minZ, minX, y, maxZ); // Left
                shapeRenderer.line(maxX, y, minZ, maxX, y, maxZ); // Right
            }
        }

        shapeRenderer.end();
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}
