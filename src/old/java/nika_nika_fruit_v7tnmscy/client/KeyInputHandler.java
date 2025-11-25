package nika_nika_fruit_v7tnmscy.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import nika_nika_fruit_v7tnmscy.item.NikaNikaFruitItem;
import nika_nika_fruit_v7tnmscy.network.NetworkPackets;

public class KeyInputHandler {
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            PlayerEntity player = client.player;
            if (player == null) return;
            
            // Process inputs always; validation happens server-side for whether the player has a fruit
            
            // Cycle Move (R key)
            if (KeyBindings.CYCLE_MOVE.wasPressed()) {
                ClientPlayNetworking.send(new NetworkPackets.CycleMovePacket());
            }
            
            // Use Move (Z key)
            if (KeyBindings.USE_MOVE.wasPressed()) {
                ClientPlayNetworking.send(new NetworkPackets.UseMovePacket());
            }
            
            // Ultimate Transform (Right Shift)
            if (KeyBindings.ULTIMATE_TRANSFORM.wasPressed()) {
                ClientPlayNetworking.send(new NetworkPackets.UltimateTransformPacket());
            }
            
            // Show Mastery Screen (M key)
            if (KeyBindings.SHOW_MASTERY.wasPressed()) {
                showMasteryScreen(client, player);
            }
            
            // Show Ult Requirements (O key)
            if (KeyBindings.SHOW_INFO.wasPressed()) {
                // Send to server to bind quick-use and show info from authoritative side
                // Show the ult requirements overlay locally only; no server-side quick-bind anymore
                try {
                    nika_nika_fruit_v7tnmscy.client.HudRenderer.showUltRequirements(player);
                } catch (Throwable t) {
                    // Silently ignore overlay errors to prevent crashes
                }
            }
            
            // Quick Use (X key)
            if (KeyBindings.QUICK_USE.wasPressed()) {
                ClientPlayNetworking.send(new NetworkPackets.QuickUsePacket());
            }

            // Haki keys
            if (KeyBindings.HAKI_CYCLE.wasPressed()) {
                // H selects current Haki move within selected type
                ClientPlayNetworking.send(new NetworkPackets.HakiCyclePacket());
            }
            if (KeyBindings.HAKI_SELECT_TYPE.wasPressed()) {
                // Up arrow cycles Haki type
                ClientPlayNetworking.send(new NetworkPackets.HakiSelectTypePacket());
            }
            if (KeyBindings.HAKI_CYCLE_REVERSE.wasPressed()) {
                // Down arrow cycles Haki type backwards
                ClientPlayNetworking.send(new NetworkPackets.HakiSelectTypeReversePacket());
            }
            if (KeyBindings.HAKI_USE.wasPressed()) {
                ClientPlayNetworking.send(new NetworkPackets.HakiUsePacket());
            }
            if (KeyBindings.HAKI_SHOW_MASTERY.wasPressed()) {
                ClientPlayNetworking.send(new NetworkPackets.HakiShowMasteryPacket());
            }
            if (KeyBindings.HAKI_SHOW_INFO.wasPressed()) {
                ClientPlayNetworking.send(new NetworkPackets.HakiShowInfoPacket());
            }

            // Fallback: if player has no fruit, let R/Z control Haki so abilities work without book
            if (!nika_nika_fruit_v7tnmscy.DevilFruitRegistry.hasAnyFruit(player)) {
                if (KeyBindings.CYCLE_MOVE.wasPressed()) {
                    ClientPlayNetworking.send(new NetworkPackets.HakiCyclePacket());
                }
                if (KeyBindings.USE_MOVE.wasPressed()) {
                    ClientPlayNetworking.send(new NetworkPackets.HakiUsePacket());
                }
            }
        });
    }
    
    private static void showMasteryScreen(MinecraftClient client, PlayerEntity player) {
        // Display mastery information in chat temporarily (simpler than creating GUI)
        int level = NikaNikaFruitItem.getMasteryLevel(player);
        float currentExp = NikaNikaFruitItem.getMasteryExp(player);
        float requiredExp = NikaNikaFruitItem.getExpRequiredForNextLevel(player);
        float progressPercent = (currentExp / requiredExp) * 100.0f;
        
        String fruit = nika_nika_fruit_v7tnmscy.DevilFruitRegistry.getPlayerFruit(player);
        if (fruit == null) fruit = "nika";
        String fruitName = switch (fruit) {
            case nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DARK -> "Dark-Dark";
            case nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_QUAKE -> "Quake-Quake";
            case nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DARKXQUAKE -> "DarkxQuake";
            case nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_SOUL -> "Soul-Soul";
            case nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DRAGON -> "Dragon-Dragon";
            case nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_OPERATION -> "Operation (Ope Ope)";
            default -> "Nika";
        };
        player.sendMessage(net.minecraft.text.Text.literal("§6§l===== " + fruitName + " Mastery ====="), false);
        player.sendMessage(net.minecraft.text.Text.literal("§eLevel: " + level), false);
        player.sendMessage(net.minecraft.text.Text.literal("§eEXP: " + String.format("%.0f", currentExp) + "/" + String.format("%.0f", requiredExp) + " (" + String.format("%.1f", progressPercent) + "%)"), false);
        
        // Show progress bar
        int barLength = 20;
        int filledBars = Math.round(progressPercent / 100.0f * barLength);
        StringBuilder progressBar = new StringBuilder("§a");
        for (int i = 0; i < filledBars; i++) {
            progressBar.append("█");
        }
        progressBar.append("§7");
        for (int i = filledBars; i < barLength; i++) {
            progressBar.append("█");
        }
        player.sendMessage(net.minecraft.text.Text.literal(progressBar.toString()), false);
        
        player.sendMessage(net.minecraft.text.Text.literal("§6§l===== Move Unlocks ====="), false);
        
        // Build moves/milestones dynamically for the current fruit
        boolean pretendTransformed = true; // show full list including awakened entries
        String[] moves = NikaNikaFruitItem.getMoveNames(player, pretendTransformed);
        for (int i = 0; i < moves.length; i++) {
            int req = NikaNikaFruitItem.getRequiredLevelFor(player, i);
            String status = level >= req ? "§a✓ " : "§c✗ ";
            player.sendMessage(net.minecraft.text.Text.literal(status + moves[i] + " (Level " + req + ")"), false);
        }
        
        player.sendMessage(net.minecraft.text.Text.literal("§6§l========================"), false);
    }
}