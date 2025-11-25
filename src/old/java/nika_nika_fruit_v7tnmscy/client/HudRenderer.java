package nika_nika_fruit_v7tnmscy.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import nika_nika_fruit_v7tnmscy.item.NikaNikaFruitItem;
import nika_nika_fruit_v7tnmscy.DevilFruitRegistry;

public class HudRenderer implements HudRenderCallback {
    // Move names
    private static final String[] BASE_MOVES = {
        "Gomu Gomu no Pistol",
        "Gomu Gomu no Balloon", 
        "Gomu Gomu no Rocket",
        "Gomu Gomu no Whip",
        "Gomu Gomu no Bazooka",
        "Gomu Gomu no Bell",
        "Gomu Gomu no Barjan Gun"
    };
    
    private static final String[] AWAKENED_MOVES = {
        "Gear Five - Gomu Gomu no Pistol",
        "Gear Five - Gomu Gomu no Balloon", 
        "Gear Five - Gomu Gomu no Rocket",
        "Gear Five - Gomu Gomu no Whip",
        "Gear Five - Gomu Gomu no Bazooka",
        "Gear Five - Gomu Gomu no Bell",
        "Gear Five - Gomu Gomu no Barjan Gun",
        "Gear Five - Gomu Gomu no Gigantii",
        "Gear Five - Gomu Gomu no Lightning",
        "Gear Five - Toon Force",
        "Gear 5 Supreme Haki Infused Barjan Gun"
    };
    
    // Overlay state for Ult Requirements
    private static long ultOverlayHideTime = 0L;
    private static java.util.List<net.minecraft.text.Text> ultOverlayLines = java.util.Collections.emptyList();

    // Call this from the ult key press to show the requirements overlay
    public static void showUltRequirements(net.minecraft.entity.player.PlayerEntity player) {
        if (player == null) return;
        java.util.List<net.minecraft.text.Text> lines = new java.util.ArrayList<>();

        String fruit = nika_nika_fruit_v7tnmscy.DevilFruitRegistry.getPlayerFruit(player);
        if (fruit == null) fruit = nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_NIKA;
        int lvl = nika_nika_fruit_v7tnmscy.item.NikaNikaFruitItem.getMasteryLevel(player);

        if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_QUAKE.equals(fruit)) {
            int drowned = nika_nika_fruit_v7tnmscy.item.NikaNikaFruitItem.getQuakeDrownedTridentDeaths(player);
            int dolphin = nika_nika_fruit_v7tnmscy.item.NikaNikaFruitItem.getQuakeDolphinDeaths(player);
            lines.add(net.minecraft.text.Text.literal("Tremor Ult"));
            lines.add(net.minecraft.text.Text.literal("- Drowned (Trident): " + drowned + "/3"));
            lines.add(net.minecraft.text.Text.literal("- Dolphin's Grace: " + dolphin + "/5"));
            lines.add(net.minecraft.text.Text.literal("- Or Lv60 (cur " + lvl + ")"));
        } else if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DARK.equals(fruit)) {
            lines.add(net.minecraft.text.Text.literal("No ult."));
        } else if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DARKXQUAKE.equals(fruit)) {
            boolean special = nika_nika_fruit_v7tnmscy.DevilFruitRegistry.isDarkxQuakeAwakeningUnlocked(player);
            lines.add(net.minecraft.text.Text.literal("DarkxQuake Ult"));
            lines.add(net.minecraft.text.Text.literal("- Lv220 (cur " + lvl + ") or Warden: " + (special ? "YES" : "NO")));
        } else if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_SOUL.equals(fruit)) {
            int souls = nika_nika_fruit_v7tnmscy.item.NikaNikaFruitItem.getSoulStolenPlayers(player);
            lines.add(net.minecraft.text.Text.literal("Soul Ult"));
            lines.add(net.minecraft.text.Text.literal("- Souls: " + souls + "/3 or Lv200 (cur " + lvl + ")"));
        } else if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DRAGON.equals(fruit)) {
            lines.add(net.minecraft.text.Text.literal("Dragon Fury"));
            lines.add(net.minecraft.text.Text.literal("- Reach Mastery 250 (cur " + lvl + ")"));
            lines.add(net.minecraft.text.Text.literal("- No damage/other requirements"));
        } else if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_OPERATION.equals(fruit)) {
            lines.add(net.minecraft.text.Text.literal("Operation"));
            lines.add(net.minecraft.text.Text.literal("- No true transformation. Use Room/Gamma Knife, etc."));
        } else { // Nika
            lines.add(net.minecraft.text.Text.literal("Gear 5"));
            lines.add(net.minecraft.text.Text.literal("- Joyboy or Lv200 (cur " + lvl + ")"));
        }

        ultOverlayLines = lines;
        net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
        long now = mc.world != null ? mc.world.getTime() : 0L;
        ultOverlayHideTime = now + 120; // show for ~6 seconds
    }

    private void renderUltRequirementsOverlay(DrawContext drawContext, TextRenderer textRenderer, int screenWidth, int screenHeight, PlayerEntity player) {
        net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
        if (client.world == null) return;
        long now = client.world.getTime();
        if (now >= ultOverlayHideTime) return;
        if (ultOverlayLines == null || ultOverlayLines.isEmpty()) return;

        int maxWidth = (int)(screenWidth * 0.6);
        java.util.List<String> wrapped = new java.util.ArrayList<>();

        // Simple word-wrap for each line to fit maxWidth
        for (net.minecraft.text.Text t : ultOverlayLines) {
            String s = t.getString();
            int start = 0;
            while (start < s.length()) {
                int end = s.length();
                // shrink until it fits
                while (end > start && textRenderer.getWidth(s.substring(start, end)) > maxWidth) {
                    int lastSpace = s.lastIndexOf(' ', end - 1);
                    if (lastSpace <= start) {
                        end--; // no space found, hard cut
                    } else {
                        end = lastSpace + 1;
                    }
                }
                wrapped.add(s.substring(start, end).trim());
                start = end;
            }
        }

        int lineHeight = textRenderer.fontHeight + 2;
        int padding = 6;
        int boxWidth = 0;
        for (String w : wrapped) boxWidth = Math.max(boxWidth, textRenderer.getWidth(w));
        boxWidth = Math.min(boxWidth, maxWidth);
        int boxHeight = wrapped.size() * lineHeight + padding * 2;
        int x = (screenWidth - boxWidth) / 2 - padding;
        int y = screenHeight - boxHeight - 30;

        // Background and border
        drawContext.fill(x, y, x + boxWidth + padding * 2, y + boxHeight, 0xAA000000);
        drawContext.drawBorder(x, y, boxWidth + padding * 2, boxHeight, 0xFFFFFFFF);

        // Draw lines
        int dy = y + padding;
        for (int i = 0; i < wrapped.size(); i++) {
            int tx = x + padding;
            drawContext.drawText(textRenderer, net.minecraft.text.Text.literal(wrapped.get(i)), tx, dy, 0xFFFFFF, false);
            dy += lineHeight;
        }

        // Armament arms overlay (simple visual) - black coating when Armament is ON
        if (nika_nika_fruit_v7tnmscy.haki.HakiSystem.isArmamentActive(player)) {
            int w = 30; int h = 60; int yArm = screenHeight - h - 20;
            int leftX = 10; int rightX = screenWidth - w - 10;
            int color = 0xAA000000;
            drawContext.fill(leftX, yArm, leftX + w, yArm + h, color);
            drawContext.fill(rightX, yArm, rightX + w, yArm + h, color);
        }
    }
    
    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        
        if (player == null || client.options.hudHidden) {
            return;
        }
        
        // Render HUD when the player has any Devil Fruit or any Haki unlocked
        if (!NikaNikaFruitItem.isPlayerDevilFruitUser(player) && !DevilFruitRegistry.hasAnyFruit(player) && !nika_nika_fruit_v7tnmscy.haki.HakiSystem.hasAnyUnlocked(player)) {
            return;
        }
        
        TextRenderer textRenderer = client.textRenderer;
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        
        // Render current move display for fruits
        if (DevilFruitRegistry.hasAnyFruit(player)) {
            renderCurrentMove(drawContext, textRenderer, screenWidth, screenHeight, player);
        }
        // Render Haki current move display (top center, just below fruit if both)
        if (nika_nika_fruit_v7tnmscy.haki.HakiSystem.shouldRender(player)) {
            renderHakiCurrentMove(drawContext, textRenderer, screenWidth, screenHeight, player);
        }
        
        // Removed ult progress bars per request
        
        // Render transformation timer if active (Nika only)
        if (DevilFruitRegistry.hasAnyFruit(player) && NikaNikaFruitItem.isPlayerTransformed(player)) {
            renderTransformationTimer(drawContext, textRenderer, screenWidth, screenHeight, player);
        }
        
        // Render mastery and right move bars
        if (DevilFruitRegistry.hasAnyFruit(player)) {
            renderMasteryDisplay(drawContext, textRenderer, screenWidth, screenHeight, player);
            renderRightMoveBar(drawContext, textRenderer, screenWidth, screenHeight, player);
            renderUltRequirementsOverlay(drawContext, textRenderer, screenWidth, screenHeight, player);
            if (NikaNikaFruitItem.isPlayerTransformed(player) && DevilFruitRegistry.playerOwnsFruit(player, DevilFruitRegistry.FRUIT_DARKXQUAKE)) {
                int edge = 6;
                int alpha = 0x88000000;
                drawContext.fill(0, 0, screenWidth, edge, alpha);
                drawContext.fill(0, screenHeight - edge, screenWidth, screenHeight, alpha);
                drawContext.fill(0, 0, edge, screenHeight, alpha);
                drawContext.fill(screenWidth - edge, 0, screenWidth, screenHeight, alpha);
            }
        }
        // Haki mastery + right move bar
        if (nika_nika_fruit_v7tnmscy.haki.HakiSystem.shouldRender(player)) {
            renderHakiMastery(drawContext, textRenderer, screenWidth, screenHeight, player);
            renderHakiRightMoveBar(drawContext, textRenderer, screenWidth, screenHeight, player);
        }

        // Armament arms overlay (simple visual) - black coating when Armament is ON
        if (nika_nika_fruit_v7tnmscy.haki.HakiSystem.isArmamentActive(player)) {
            int w = 30; int h = 60; int yArm = screenHeight - h - 20;
            int leftX = 10; int rightX = screenWidth - w - 10;
            int color = 0xAA000000;
            drawContext.fill(leftX, yArm, leftX + w, yArm + h, color);
            drawContext.fill(rightX, yArm, rightX + w, yArm + h, color);
        }
    }

    private void renderHakiCurrentMove(DrawContext drawContext, TextRenderer textRenderer, int screenWidth, int screenHeight, PlayerEntity player) {
        String currentMove = nika_nika_fruit_v7tnmscy.haki.HakiSystem.getSelectedMoveName(player);
        Text moveText = Text.literal("Haki Move: §b" + currentMove);
        int textWidth = textRenderer.getWidth(moveText);
        int x = (screenWidth - textWidth) / 2;
        int y = 10 + textRenderer.fontHeight + 12; // below fruit
        drawContext.fill(x - 5, y - 2, x + textWidth + 5, y + textRenderer.fontHeight + 2, 0x80000000);
        drawContext.drawText(textRenderer, moveText, x, y, 0xFFFFFF, true);
    }
    
    private void renderCurrentMove(DrawContext drawContext, TextRenderer textRenderer, int screenWidth, int screenHeight, PlayerEntity player) {
        boolean isTransformed = NikaNikaFruitItem.isPlayerTransformed(player);
        String[] availableMoves = NikaNikaFruitItem.getMoveNames(player, isTransformed);
        int currentMoveIndex = NikaNikaFruitItem.getCurrentMoveIndex(player);
        
        if (currentMoveIndex >= availableMoves.length) {
            currentMoveIndex = 0;
        }
        
        String currentMove = availableMoves[currentMoveIndex];
        Text moveText = Text.literal("Current Move: §e" + currentMove);
        
        // Position: Top center of screen
        int textWidth = textRenderer.getWidth(moveText);
        int x = (screenWidth - textWidth) / 2;
        int y = 10;
        
        // Background
        drawContext.fill(x - 5, y - 2, x + textWidth + 5, y + textRenderer.fontHeight + 2, 0x80000000);
        
        // Text
        drawContext.drawText(textRenderer, moveText, x, y, 0xFFFFFF, true);
        
        // Show move count
        String moveCount = "§7(" + (currentMoveIndex + 1) + "/" + availableMoves.length + ")";
        int countWidth = textRenderer.getWidth(moveCount);
        drawContext.drawText(textRenderer, Text.literal(moveCount), x + textWidth - countWidth, y + textRenderer.fontHeight + 5, 0xAAAAAA, true);
    }
    
    private void renderDamageProgressBar(DrawContext drawContext, TextRenderer textRenderer, int screenWidth, int screenHeight, PlayerEntity player) {
        boolean isDxq = nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DARKXQUAKE);
        
        // Position: Above hotbar
        int barWidth = 182;
        int barHeight = 5;
        int x = (screenWidth - barWidth) / 2;
        int y = screenHeight - 64; // Above hotbar
        
        // Background (skip for Nika)
        boolean isQuake = nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_QUAKE);
        boolean isDark = nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DARK);
        boolean isNika = !isDxq && !isQuake && !isDark;
        if (!isNika) {
            drawContext.fill(x, y, x + barWidth, y + barHeight, 0xFF000000);
        }
        
        if (isDxq) {
            // DarkxQuake Awakening: The True Menace
            // Progress shows Mastery towards 50, or "Warden Unlock" if achieved
            int level = NikaNikaFruitItem.getMasteryLevel(player);
            boolean wardenUnlock = nika_nika_fruit_v7tnmscy.DevilFruitRegistry.isDarkxQuakeAwakeningUnlocked(player);
            float progress = Math.min(level / 220.0f, 1.0f);
            int progressWidth = (int)(barWidth * progress);
            int color = progress >= 1.0f ? 0xFF00FF00 : (progress >= 0.6f ? 0xFFFFFF00 : 0xFFAA00FF);
            if (!isNika && !isDark) {
                drawContext.fill(x, y, x + progressWidth, y + barHeight, color);
            }

            // Border (skip for Nika)
            if (!isNika && !isDark) {
                drawContext.drawHorizontalLine(x - 1, x + barWidth, y - 1, 0xFFFFFFFF);
                drawContext.drawHorizontalLine(x - 1, x + barWidth, y + barHeight, 0xFFFFFFFF);
                drawContext.drawVerticalLine(x - 1, y - 1, y + barHeight, 0xFFFFFFFF);
                drawContext.drawVerticalLine(x + barWidth, y - 1, y + barHeight, 0xFFFFFFFF);
            }

            // Title and details
            Text title = Text.literal("§8Ult: The True Menace");
            int titleW = textRenderer.getWidth(title);
            int titleX = (screenWidth - titleW) / 2;
            int titleY = y - textRenderer.fontHeight - 12;
            drawContext.fill(titleX - 3, titleY - 2, titleX + titleW + 3, titleY + textRenderer.fontHeight + 2, 0x80000000);
            drawContext.drawText(textRenderer, title, titleX, titleY, 0xDDDDDD, true);

            String details = wardenUnlock ? "Warden Unlock: §aYES" : String.format("Mastery: %d/220", level);
            Text detailText = Text.literal(details);
            int detailW = textRenderer.getWidth(detailText);
            int detailX = (screenWidth - detailW) / 2;
            int detailY = y - textRenderer.fontHeight - 2;
            drawContext.fill(detailX - 2, detailY - 1, detailX + detailW + 2, detailY + textRenderer.fontHeight + 1, 0x80000000);
            drawContext.drawText(textRenderer, detailText, detailX, detailY, progress >= 1.0f || wardenUnlock ? 0x00FF00 : 0xFFFFFF, true);
        } else {
            // Nika/Tremor/Dark: Rage bar = damage taken; show damage taken/dealt and ult name
            float taken = NikaNikaFruitItem.getPlayerDamageTaken(player);
            float dealt = NikaNikaFruitItem.getPlayerDamageDealt(player);
            float requiredDamage = 200.0f;
            float progress = Math.min(taken / requiredDamage, 1.0f);
            int progressWidth = (int) (barWidth * progress);
            int color;
            if (progress >= 1.0f) color = 0xFF00FF00; else if (progress >= 0.75f) color = 0xFFFFFF00; else color = 0xFFFF6600;
            if (!isNika && !isDark) {
                drawContext.fill(x, y, x + progressWidth, y + barHeight, color);
            }
            
            // Border (skip for Nika)
            if (!isNika && !isDark) {
                drawContext.drawHorizontalLine(x - 1, x + barWidth, y - 1, 0xFFFFFFFF);
                drawContext.drawHorizontalLine(x - 1, x + barWidth, y + barHeight, 0xFFFFFFFF);
                drawContext.drawVerticalLine(x - 1, y - 1, y + barHeight, 0xFFFFFFFF);
                drawContext.drawVerticalLine(x + barWidth, y - 1, y + barHeight, 0xFFFFFFFF);
            }
            
            // Title and details
            String ultName = "§fUlt: Gear 5";
            if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_QUAKE)) ultName = "§fUlt: Tremor";
            if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DARK)) ultName = "§8Dark: no ultimate";
            Text title = Text.literal(ultName);
            int titleW = textRenderer.getWidth(title);
            int titleX = (screenWidth - titleW) / 2;
            int titleY = y - textRenderer.fontHeight - 12;
            drawContext.fill(titleX - 3, titleY - 2, titleX + titleW + 3, titleY + textRenderer.fontHeight + 2, 0x80000000);
            drawContext.drawText(textRenderer, title, titleX, titleY, 0xFFFFFF, true);
            
            if (!isNika && !isDark) {
                String progressText = String.format("Taken: %.0f/200  |  Dealt: %.0f/100", taken, dealt);
                Text text = Text.literal(progressText);
                int textWidth = textRenderer.getWidth(text);
                int textX = (screenWidth - textWidth) / 2;
                int textY = y - textRenderer.fontHeight - 2;
                drawContext.fill(textX - 2, textY - 1, textX + textWidth + 2, textY + textRenderer.fontHeight + 1, 0x80000000);
                drawContext.drawText(textRenderer, text, textX, textY, progress >= 1.0f && dealt >= 100.0f ? 0x00FF00 : 0xFFFFFF, true);
            }
        }

        // Armament arms overlay (simple visual) - black coating when Armament is ON
        if (nika_nika_fruit_v7tnmscy.haki.HakiSystem.isArmamentActive(player)) {
            int w = 30; int h = 60; int yArm = screenHeight - h - 20;
            int leftX = 10; int rightX = screenWidth - w - 10;
            int color = 0xAA000000;
            drawContext.fill(leftX, yArm, leftX + w, yArm + h, color);
            drawContext.fill(rightX, yArm, rightX + w, yArm + h, color);
        }
    }
    
    private void renderTransformationTimer(DrawContext drawContext, TextRenderer textRenderer, int screenWidth, int screenHeight, PlayerEntity player) {
        long timeRemaining = NikaNikaFruitItem.getTransformationTimeRemaining(player);
        if (timeRemaining <= 0) return;
        
        // Convert ticks to minutes and seconds
        long totalSeconds = timeRemaining / 20; // 20 ticks per second
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        
        String label = "§f§lGEAR 5 ACTIVE";
        if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DARKXQUAKE)) label = "§8§lTHE TRUE MENACE";
        else if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_QUAKE)) label = "§f§lTREMOR AWAKENED";
        else if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.playerOwnsFruit(player, nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DARK)) label = "§8§lDARKNESS AWAKENED";
        String timeText = String.format(label + ": %02d:%02d", minutes, seconds);
        Text timerText = Text.literal(timeText);
        
        // Position: Top right corner
        int textWidth = textRenderer.getWidth(timerText);
        int x = screenWidth - textWidth - 10;
        int y = 10;
        
        // Background with special effect for Gear 5
        drawContext.fill(x - 5, y - 2, x + textWidth + 5, y + textRenderer.fontHeight + 2, 0xAA000000);
        
        // Border with white glow effect
        drawContext.drawHorizontalLine(x - 6, x + textWidth + 5, y - 3, 0xFFFFFFFF);
        drawContext.drawHorizontalLine(x - 6, x + textWidth + 5, y + textRenderer.fontHeight + 2, 0xFFFFFFFF);
        drawContext.drawVerticalLine(x - 6, y - 3, y + textRenderer.fontHeight + 2, 0xFFFFFFFF);
        drawContext.drawVerticalLine(x + textWidth + 5, y - 3, y + textRenderer.fontHeight + 2, 0xFFFFFFFF);
        
        // Text with pulsing effect based on time remaining
        int color = 0xFFFFFF;
        if (timeRemaining < 1200) { // Less than 1 minute remaining
            color = (timeRemaining % 10 < 5) ? 0xFFFFFF : 0xFF4444; // Flash red/white
        } else if (timeRemaining < 6000) { // Less than 5 minutes remaining
            color = 0xFFFF88; // Yellow warning
        }
        
        drawContext.drawText(textRenderer, timerText, x, y, color, true);
    }
    
    private void renderHakiMastery(DrawContext drawContext, TextRenderer textRenderer, int screenWidth, int screenHeight, PlayerEntity player) {
        // Single bar: currently selected Haki type, on the left side under fruit mastery
        int x = 10; int y = 34; int barWidth = 120; int barHeight = 3;
        int lvl = nika_nika_fruit_v7tnmscy.haki.HakiSystem.getDisplayedLevel(player);
        float exp = nika_nika_fruit_v7tnmscy.haki.HakiSystem.getDisplayedExp(player);
        float req = Math.max(1.0f, nika_nika_fruit_v7tnmscy.haki.HakiSystem.getDisplayedReq(player));
        nika_nika_fruit_v7tnmscy.haki.HakiSystem.Type sel = nika_nika_fruit_v7tnmscy.haki.HakiSystem.getSelectedType(player);
        int color = switch (sel) {
            case ARMAMENT -> 0xFF888888; case OBSERVATION -> 0xFF88CCFF; case CONQUERORS -> 0xFFDD88FF; };
        String title = switch (sel) {
            case ARMAMENT -> "Armament"; case OBSERVATION -> "Observation"; case CONQUERORS -> "Conqueror's"; };
        Text label = Text.literal(title + " Lv." + lvl);
        int panelWidth = barWidth + 65;
        drawContext.fill(x - 5, y - 2, x + panelWidth, y + textRenderer.fontHeight + 2, 0x80000000);
        drawContext.drawText(textRenderer, label, x, y, 0xDDDDDD, true);
        int barY = y + textRenderer.fontHeight + 5;
        drawContext.fill(x, barY, x + barWidth, barY + barHeight, 0xFF000000);
        drawContext.fill(x, barY, x + (int)(barWidth * (exp / req)), barY + barHeight, color);
        // Hint line
        drawContext.drawText(textRenderer, Text.literal("§7Up/Down: type  H: move  G: use"), x, barY + barHeight + 4, 0xAAAAAA, false);
    }

    private void renderMasteryDisplay(DrawContext drawContext, TextRenderer textRenderer, int screenWidth, int screenHeight, PlayerEntity player) {
        int level = NikaNikaFruitItem.getMasteryLevel(player);
        float currentExp = NikaNikaFruitItem.getMasteryExp(player);
        float requiredExp = NikaNikaFruitItem.getExpRequiredForNextLevel(player);
        
        String masteryText = "§6Mastery Lv." + level;
        Text levelText = Text.literal(masteryText);
        
        // Position: Top left corner
        int x = 10;
        int y = 10;
        int textWidth = textRenderer.getWidth(levelText);
        
        // Background
        drawContext.fill(x - 5, y - 2, x + textWidth + 5, y + textRenderer.fontHeight + 2, 0x80000000);
        
        // Text
        drawContext.drawText(textRenderer, levelText, x, y, 0xFFD700, true);
        
        // EXP bar below mastery level
        int barWidth = 100;
        int barHeight = 3;
        int barY = y + textRenderer.fontHeight + 5;
        
        // Background
        drawContext.fill(x, barY, x + barWidth, barY + barHeight, 0xFF000000);
        
        // Progress
        float progress = currentExp / requiredExp;
        int progressWidth = (int) (barWidth * progress);
        drawContext.fill(x, barY, x + progressWidth, barY + barHeight, 0xFFFFD700);
        
        // Border
        drawContext.drawHorizontalLine(x - 1, x + barWidth, barY - 1, 0xFFFFFFFF);
        drawContext.drawHorizontalLine(x - 1, x + barWidth, barY + barHeight, 0xFFFFFFFF);
        drawContext.drawVerticalLine(x - 1, barY - 1, barY + barHeight, 0xFFFFFFFF);
        drawContext.drawVerticalLine(x + barWidth, barY - 1, barY + barHeight, 0xFFFFFFFF);
        
        // Show hint for mastery screen
        String hintText = "§7Press M for details";
        Text hint = Text.literal(hintText);
        int hintWidth = textRenderer.getWidth(hint);
        drawContext.drawText(textRenderer, hint, x, barY + barHeight + 3, 0xAAAAAA, false);
    }
    
    private void renderHakiRightMoveBar(DrawContext drawContext, TextRenderer textRenderer, int screenWidth, int screenHeight, PlayerEntity player) {
        String name = nika_nika_fruit_v7tnmscy.haki.HakiSystem.getSelectedMoveName(player);
        long remainTicks = nika_nika_fruit_v7tnmscy.haki.HakiSystem.getSelectedMoveCooldown(player);
        int seconds = (int)(remainTicks / 20);
        int barWidth = 90;
        int barHeight = 100;
        int x = 8; // left side for Haki
        int y = (screenHeight - barHeight) / 2;
        drawContext.fill(x, y, x + barWidth, y + barHeight, 0x88000000);
        drawContext.drawBorder(x, y, barWidth, barHeight, 0xFFFFFFFF);
        Text title = Text.literal("§fHaki");
        drawContext.drawText(textRenderer, title, x + 6, y + 6, 0xFFFFFF, true);
        drawContext.drawText(textRenderer, Text.literal(name), x + 6, y + 20, 0x88CCFF, true);
        int meterX = x + barWidth - 20;
        int meterTop = y + 6;
        int meterBottom = y + barHeight - 6;
        int meterHeight = meterBottom - meterTop;
        int filled = seconds <= 0 ? 0 : Math.min(meterHeight, (int)(meterHeight * Math.min(1.0f, seconds / 30.0f)));
        int filledTop = meterBottom - filled;
        drawContext.fill(meterX, filledTop, meterX + 8, meterBottom, seconds > 0 ? 0xFFAA4444 : 0xFF44AA44);
        drawContext.drawBorder(meterX, meterTop, 8, meterHeight, 0xFFFFFFFF);
        String cdText = seconds > 0 ? (seconds + "s") : "ready";
        int cdY = y + barHeight - textRenderer.fontHeight - 6;
        drawContext.drawText(textRenderer, Text.literal("§7CD: " + cdText), x + 6, cdY, 0xAAAAAA, false);
        drawContext.drawText(textRenderer, Text.literal("§7Up/Down: type  H: move  G: use"), x + 6, cdY - textRenderer.fontHeight - 2, 0xAAAAAA, false);
    }

    private void renderRightMoveBar(DrawContext drawContext, TextRenderer textRenderer, int screenWidth, int screenHeight, PlayerEntity player) {
        boolean isTransformed = NikaNikaFruitItem.isPlayerTransformed(player);
        String[] moves = NikaNikaFruitItem.getMoveNames(player, isTransformed);
        int index = NikaNikaFruitItem.getCurrentMoveIndex(player);
        if (index >= moves.length) index = 0;
        String name = moves[index];
        long remainTicks = NikaNikaFruitItem.getSelectedMoveCooldownRemaining(player);
        int seconds = (int)(remainTicks / 20);
        
        int barWidth = 90;
        int barHeight = 100;
        int x = screenWidth - barWidth - 8;
        int y = (screenHeight - barHeight) / 2;
        
        // Panel background
        drawContext.fill(x, y, x + barWidth, y + barHeight, 0x88000000);
        drawContext.drawBorder(x, y, barWidth, barHeight, 0xFFFFFFFF);
        
        // Title
        Text title = Text.literal("§fMove");
        drawContext.drawText(textRenderer, title, x + 6, y + 6, 0xFFFFFF, true);
        
        // Name
        int nameY = y + 20;
        drawContext.drawText(textRenderer, Text.literal(name), x + 6, nameY, 0xFFDD88, true);
        
        // Cooldown meter
        int meterX = x + barWidth - 20;
        int meterTop = y + 6;
        int meterBottom = y + barHeight - 6;
        int meterHeight = meterBottom - meterTop;
        int filled = seconds <= 0 ? 0 : Math.min(meterHeight, (int)(meterHeight * Math.min(1.0f, seconds / 30.0f))); // visualize up to 30s
        int filledTop = meterBottom - filled;
        drawContext.fill(meterX, filledTop, meterX + 8, meterBottom, seconds > 0 ? 0xFFAA4444 : 0xFF44AA44);
        drawContext.drawBorder(meterX, meterTop, 8, meterHeight, 0xFFFFFFFF);
        
        // Cooldown text
        String cdText = seconds > 0 ? (seconds + "s") : "ready";
        int cdY = y + barHeight - textRenderer.fontHeight - 6;
        drawContext.drawText(textRenderer, Text.literal("§7CD: " + cdText), x + 6, cdY, 0xAAAAAA, false);
        
        // Hint
        drawContext.drawText(textRenderer, Text.literal("§7R: select  Z: use"), x + 6, cdY - textRenderer.fontHeight - 2, 0xAAAAAA, false);
    }
    
    public static void register() {
        HudRenderCallback.EVENT.register(new HudRenderer());
    }
}