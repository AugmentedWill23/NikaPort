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

public class DarkFruitItem extends Item {
    public DarkFruitItem(Settings settings) { super(settings); }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient) {
            String fruitId = DevilFruitRegistry.FRUIT_DARK;
            // Standard duplicate protection (fusion ritual removed)
            if (DevilFruitRegistry.hasAnyFruit(user) && !DevilFruitRegistry.playerOwnsFruit(user, fruitId)) {
                if (user instanceof ServerPlayerEntity sp) {
                    sp.sendMessage(Text.literal("§cYou cannot consume Dark and Quake together."), true);
                    sp.damage(sp.getServerWorld(), sp.getDamageSources().outOfWorld(), Float.MAX_VALUE);
                }
                if (!user.isCreative()) stack.decrement(1);
                return ActionResult.SUCCESS;
            }
            if (!DevilFruitRegistry.canAcquireFruit(user, fruitId)) {
                // Already owned by someone else or player already has a fruit
                DevilFruitRegistry.warnAndVanishFruit(user, fruitId);
                if (!user.isCreative()) stack.decrement(1);
                return ActionResult.SUCCESS;
            }
            DevilFruitRegistry.assignFruit(user, fruitId);
            // Basic confirmation (moves/UI for Dark to be added in future phase)
            if (user instanceof ServerPlayerEntity sp) {
                sp.sendMessage(Text.literal("§5You have consumed the Dark-Dark Fruit!"), false);
                sp.sendMessage(Text.literal("§7R cycle | Z use | X quick | O ult info | §8Dark: no ult"), false);
            }
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 1.0F, 0.6F);
            if (!user.isCreative()) stack.decrement(1);


        }
        return ActionResult.SUCCESS;
    }
}
