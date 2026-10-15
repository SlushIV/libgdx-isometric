package game;

import java.util.ArrayList;

public interface Updatable {
    void update(float delta, ObjectManager objectManager);
    boolean isAlive();
}
