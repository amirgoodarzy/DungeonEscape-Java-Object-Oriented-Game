package dungeonescape.managers;

import java.io.Serializable;

/**
 * State machine enumeration modeling overall game modes.
 */
public enum GameState implements Serializable {
    MAIN_MENU,
    PLAYING,
    PAUSED,
    GAME_OVER,
    VICTORY
}
