package nika_nika_fruit_v7tnmscy;

import net.fabricmc.api.ClientModInitializer;
import nika_nika_fruit_v7tnmscy.client.HudRenderer;
import nika_nika_fruit_v7tnmscy.client.KeyBindings;
import nika_nika_fruit_v7tnmscy.client.KeyInputHandler;

public class NikaNikaFruitClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NikaNikaFruit.LOGGER.info("The Devil fruits Client Initializer loaded.");
        
        // Register key bindings
        KeyBindings.registerKeyBindings();
        
        // Register key input handler
        KeyInputHandler.register();
        
        // Register HUD renderer
        HudRenderer.register();
    }
}