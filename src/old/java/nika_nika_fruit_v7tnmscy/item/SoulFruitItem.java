package nika_nika_fruit_v7tnmscy.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import nika_nika_fruit_v7tnmscy.DevilFruitRegistry;

public class SoulFruitItem extends Item {
    public SoulFruitItem(Settings settings) { super(settings); }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient) {
            String fruitId = DevilFruitRegistry.FRUIT_SOUL;
            // Duplicate protection: only one fruit per player
            if (DevilFruitRegistry.hasAnyFruit(user) && !DevilFruitRegistry.playerOwnsFruit(user, fruitId)) {
                if (user instanceof ServerPlayerEntity sp) {
                    sp.sendMessage(Text.literal("§cYou attempted to possess a duplicate Devil Fruit!"), true);
                    sp.damage(sp.getServerWorld(), sp.getDamageSources().outOfWorld(), Float.MAX_VALUE);
                }
                if (!user.isCreative()) stack.decrement(1);
                return ActionResult.SUCCESS;
            }
            if (!DevilFruitRegistry.canAcquireFruit(user, fruitId)) {
                DevilFruitRegistry.warnAndVanishFruit(user, fruitId);
                if (!user.isCreative()) stack.decrement(1);
                return ActionResult.SUCCESS;
            }
            DevilFruitRegistry.assignFruit(user, fruitId);
            if (user instanceof ServerPlayerEntity sp) {
                sp.sendMessage(Text.literal("§bYou have consumed the Soul-Soul Fruit!"), false);
                sp.sendMessage(Text.literal("§7Controls: R to cycle, Z to use, O to view ult requirements, Right Shift for Transformation (when ready), X to Quick-Use bound move."), false);
            }
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 1.0F, 0.8F);
            if (!user.isCreative()) stack.decrement(1);
        }
        return ActionResult.SUCCESS;
    }
}
