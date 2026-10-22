package game;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public interface Collidable {
    BoundingBox getBounds();
    Vector3 getPosition();
}
