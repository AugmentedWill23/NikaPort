package nika_nika_fruit_v7tnmscy.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static final String CATEGORY = "key.categories.nika_nika_fruit";
    
    public static KeyBinding CYCLE_MOVE;
    public static KeyBinding USE_MOVE;
    public static KeyBinding ULTIMATE_TRANSFORM;
    public static KeyBinding SHOW_MASTERY;
    public static KeyBinding SHOW_INFO;
    public static KeyBinding QUICK_USE;

    // Haki keys
    public static KeyBinding HAKI_CYCLE;
    public static KeyBinding HAKI_SELECT_TYPE;
    public static KeyBinding HAKI_CYCLE_REVERSE;
    public static KeyBinding HAKI_USE;
    public static KeyBinding HAKI_SHOW_MASTERY;
    public static KeyBinding HAKI_SHOW_INFO;
    
    public static void registerKeyBindings() {
        CYCLE_MOVE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.nika_nika_fruit.cycle_move",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            CATEGORY
        ));
        
        USE_MOVE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.nika_nika_fruit.use_move",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            CATEGORY
        ));
        
        ULTIMATE_TRANSFORM = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.nika_nika_fruit.ultimate_transform",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            CATEGORY
        ));
        
        SHOW_MASTERY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.nika_nika_fruit.show_mastery",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            CATEGORY
        ));
        
        SHOW_INFO = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.nika_nika_fruit.show_info",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            CATEGORY
        ));
        
        QUICK_USE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.nika_nika_fruit.quick_use",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            CATEGORY
        ));

        // Haki: T/G/Y/I
        HAKI_CYCLE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.nika_nika_fruit.haki_cycle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            CATEGORY
        ));
        // Haki: select type with Up/Down
        HAKI_SELECT_TYPE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.nika_nika_fruit.haki_select_type",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UP,
            CATEGORY
        ));
        HAKI_CYCLE_REVERSE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.nika_nika_fruit.haki_cycle_reverse",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_DOWN,
            CATEGORY
        ));
        HAKI_USE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.nika_nika_fruit.haki_use",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            CATEGORY
        ));
        HAKI_SHOW_MASTERY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.nika_nika_fruit.haki_show_mastery",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_Y,
            CATEGORY
        ));
        HAKI_SHOW_INFO = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.nika_nika_fruit.haki_show_info",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_I,
            CATEGORY
        ));
    }
}