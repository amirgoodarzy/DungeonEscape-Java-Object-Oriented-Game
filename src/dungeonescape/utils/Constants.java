package dungeonescape.utils;

import java.awt.Color;

/**
 * Global game configuration parameters, dimensions, and styling constants.
 * Demonstrates public static final constants for maintainability and centralized tuning.
 */
public final class Constants {
    // Private constructor to prevent instantiation (pure utility class pattern)
    private Constants() {}

    // Display & Grid Geometry
    public static final int TILE_SIZE = 48;
    public static final int ROOM_WIDTH_TILES = 16;
    public static final int ROOM_HEIGHT_TILES = 12;
    public static final int GAME_WIDTH = ROOM_WIDTH_TILES * TILE_SIZE;   // 768 px
    public static final int GAME_HEIGHT = ROOM_HEIGHT_TILES * TILE_SIZE; // 576 px
    public static final int HUD_HEIGHT = 150;
    public static final int WINDOW_WIDTH = GAME_WIDTH;
    public static final int WINDOW_HEIGHT = GAME_HEIGHT + HUD_HEIGHT;

    // Game Loop Timing
    public static final int FPS = 60;
    public static final int TIMER_DELAY_MS = 1000 / FPS; // ~16ms per frame

    // Player Default Attributes
    public static final int PLAYER_MAX_HEALTH = 100;
    public static final int PLAYER_BASE_ATTACK = 15;
    public static final int PLAYER_BASE_DEFENSE = 5;
    public static final int PLAYER_INVENTORY_CAPACITY = 8;

    // Enemy Attributes
    public static final int ZOMBIE_HEALTH = 40;
    public static final int ZOMBIE_ATTACK = 10;
    public static final int ZOMBIE_DEFENSE = 2;
    public static final int ZOMBIE_STEP_DELAY = 35; // moves every 35 ticks (~0.6s)

    public static final int GHOST_HEALTH = 25;
    public static final int GHOST_ATTACK = 14;
    public static final int GHOST_DEFENSE = 0;
    public static final int GHOST_STEP_DELAY = 22; // moves every 22 ticks (~0.35s)

    public static final int BOSS_HEALTH = 120;
    public static final int BOSS_ATTACK = 22;
    public static final int BOSS_DEFENSE = 8;
    public static final int BOSS_STEP_DELAY = 30;

    // UI Palette
    public static final Color COLOR_BACKGROUND = new Color(20, 20, 26);
    public static final Color COLOR_FLOOR = new Color(38, 38, 48);
    public static final Color COLOR_FLOOR_GRID = new Color(46, 46, 58);
    public static final Color COLOR_WALL = new Color(70, 70, 85);
    public static final Color COLOR_DOOR_LOCKED = new Color(180, 50, 50);
    public static final Color COLOR_DOOR_UNLOCKED = new Color(50, 180, 80);
    public static final Color COLOR_EXIT = new Color(255, 215, 0);
    public static final Color COLOR_HUD_BG = new Color(12, 12, 16);
    public static final Color COLOR_TEXT = new Color(230, 230, 240);
    public static final Color COLOR_ACCENT = new Color(80, 150, 240);

    // Save File Names
    public static final String DEFAULT_SAVE_FILE = "dungeon_save.dat";
    public static final String DEFAULT_RULES_FILE = "custom_rules.txt";
}
