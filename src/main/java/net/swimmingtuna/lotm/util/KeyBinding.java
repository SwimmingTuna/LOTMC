package net.swimmingtuna.lotm.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBinding {
    public static final String KEY_CATEGORY_LOTM = "key.category.lotm.mystery";
    public static final String ABILITY_KEY = "key.lotm.ability_check";
    public static final String SPIRIT_TRAVERSAL_KEY = "key.lotm.spirit_traversal_key";
    public static final String ABILITY_KEY_1 = "key.lotm.ability_key_1";
    public static final String ABILITY_KEY_0 = "key.lotm.ability_key_0";
    public static final String ABILITY_KEY_2 = "key.lotm.ability_key_clear";


    public static final KeyMapping SPIRIT_VISION = new KeyMapping(ABILITY_KEY, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, KEY_CATEGORY_LOTM);
    public static final KeyMapping SPIRIT_WORLD_TRAVERSAL = new KeyMapping(SPIRIT_TRAVERSAL_KEY, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, KEY_CATEGORY_LOTM);

    public static final KeyMapping ABILITY_KEY_O = new KeyMapping(ABILITY_KEY_0, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, KEY_CATEGORY_LOTM);
    public static final KeyMapping ABILITY_KEY_X = new KeyMapping(ABILITY_KEY_1, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, KEY_CATEGORY_LOTM);
    public static final KeyMapping ABILITY_KEY_CLEAR = new KeyMapping(ABILITY_KEY_2, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, KEY_CATEGORY_LOTM);
}

