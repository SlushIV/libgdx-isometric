package game;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import java.util.List;

public class Grid {

    public final float gridSize = 1.0f; // 1 meter per grid cell
    public final int width, height;
    public final float offsetX;
    public final float offsetZ;

    // obstacle count per tile: 0 = walkable, >0 = blocked
    private int[][] grid;

    public Grid(int width, int height) {
        grid = new int[width][height];
        this.width = width;
        this.height = height;

        // world center -> array center grid[width/2][height/2]
        offsetX = width / 2.0f;
        offsetZ = height / 2.0f;
    }

    // this has no use case yet since objectmanager calls addObstacle when adding objects to the scene
    // potentially useful if you want to draw a new scene/level? idk
    public void createGrid(List<Collidable> collidables) {

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < height; z++) {
                grid[x][z] = 0;
            }
        }

        // Add each world obstacle
        for (Collidable obj : collidables) {
            addObstacle(obj);
        }
    }

    // increments by 1
    public void addObstacle(Collidable obj) {
        // very terrible exclude floor check
        if (obj.getPosition().y < 0) return;

        BoundingBox box = obj.getBounds();

        Vector3 min = box.min;
        Vector3 max = box.max;

        // math.floor handles correct rounding for neg values (only relevant for bullets tbh)
        int rawMinX = (int) Math.floor((min.x + offsetX) / gridSize);
        int rawMaxX = (int) Math.floor((max.x + offsetX) / gridSize);
        int rawMinZ = (int) Math.floor((min.z + offsetZ) / gridSize);
        int rawMaxZ = (int) Math.floor((max.z + offsetZ) / gridSize);

        // skip objects COMPLETELY outside the grid boundary
        if (rawMaxX < 0 || rawMinX >= width || rawMaxZ < 0 || rawMinZ >= height) {
            return;
        }

        // relevant for objects outside but not fully
        int minX = Math.max(0, rawMinX);
        int maxX = Math.min(width - 1, rawMaxX);
        int minZ = Math.max(0, rawMinZ);
        int maxZ = Math.min(height - 1, rawMaxZ);

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                grid[x][z]++;
            }
        }
    }

    // decrements by 1
    public void removeObstacle(Collidable removingObj) {
        // very terrible exclude floor check
        if (removingObj.getPosition().y < 0) return;

        BoundingBox box = removingObj.getBounds();
        Vector3 min = box.min;
        Vector3 max = box.max;

        // math.floor handles correct rounding for neg values (only relevant for bullets tbh)
        int rawMinX = (int) Math.floor((min.x + offsetX) / gridSize);
        int rawMaxX = (int) Math.floor((max.x + offsetX) / gridSize);
        int rawMinZ = (int) Math.floor((min.z + offsetZ) / gridSize);
        int rawMaxZ = (int) Math.floor((max.z + offsetZ) / gridSize);

        // skip objects COMPLETELY outside the grid boundary
        if (rawMaxX < 0 || rawMinX >= width || rawMaxZ < 0 || rawMinZ >= height) {
            return;
        }

        // relevant for objects outside but not fully
        int minX = Math.max(0, rawMinX);
        int maxX = Math.min(width - 1, rawMaxX);
        int minZ = Math.max(0, rawMinZ);
        int maxZ = Math.min(height - 1, rawMaxZ);

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (grid[x][z] > 0) {
                    grid[x][z]--;
                }
            }
        }
    }

    // array lookup for walkable (not needed yet)
    public boolean isWalkable(float x, float z) { // coordinates
        // offset to account for negative coords
        int gridX = (int) Math.floor((x + offsetX) / gridSize); // math.floor handles correct rounding for neg values (only relevant for bullets tbh)
        int gridZ = (int) Math.floor((z + offsetZ) / gridSize); // honestly ts doesnt even need math floor its JUST IN CASE

        if (gridX < 0 || gridX >= width || gridZ < 0 || gridZ >= height) {
            return false;
        }

        return grid[gridX][gridZ] == 0;
    }

    // array lookup for walkable (stupid)
    public boolean isWalkable(int x, int z) { // indices
        if (x < 0 || x >= width || z < 0 || z >= height) {
            return false;
        }

        return grid[x][z] == 0;
    }

    // array lookup for pathfinding THIS ALWAYS RETURNS A int[2] (or null), int[0] = gridX, int[1] = gridZ
    public int[] getGridIndices(float x, float z) { // coordinates
        // offset to account for negative coords
        int gridX = (int) Math.floor((x + offsetX) / gridSize); // math.floor handles correct rounding for neg values (only relevant for bullets tbh)
        int gridZ = (int) Math.floor((z + offsetZ) / gridSize); // honestly ts doesnt even need math floor its JUST IN CASE

        // ts should not happen as its only called on objects like in the grid?
        if (gridX < 0 || gridX >= width || gridZ < 0 || gridZ >= height) {
            return null;
        }

        return new int[]{gridX, gridZ};
    }

    public Vector3 getWorldCoordinates(int x, int z) { // grid indices
        if (x < 0 || x >= width || z < 0 || z >= height) {
            return Vector3.Zero; // cant go there but shoudnt really happen if the method is used in a sanitized context
        }

        Vector3 result = new Vector3();

        // since index = (coordinate + offset) / gridsize
        // -> coordinate = index * gridsize - offset
        result.x = (x * gridSize) - offsetX;
        result.z = (z * gridSize) - offsetZ;
        result.y = 0f; // dont rlly know how to handle y coords on a plane grid yet idk
        return result;
    }

    public int[][] getGrid() {
        return grid;
    }
}
