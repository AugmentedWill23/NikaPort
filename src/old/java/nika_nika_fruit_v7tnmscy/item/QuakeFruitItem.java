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

public class QuakeFruitItem extends Item {
    public QuakeFruitItem(Settings settings) { super(settings); }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient) {
            // Standard duplicate protection (fusion ritual removed)
            String fruitIdCheck = DevilFruitRegistry.FRUIT_QUAKE;
            if (DevilFruitRegistry.hasAnyFruit(user) && !DevilFruitRegistry.playerOwnsFruit(user, fruitIdCheck)) {
                if (user instanceof ServerPlayerEntity sp) {
                    sp.sendMessage(Text.literal("§cYou cannot consume Dark and Quake together."), true);
                    sp.damage(sp.getServerWorld(), sp.getDamageSources().outOfWorld(), Float.MAX_VALUE);
                }
                if (!user.isCreative()) stack.decrement(1);
                return ActionResult.SUCCESS;
            }

            // Normal Quake acquisition
            String fruitId = DevilFruitRegistry.FRUIT_QUAKE;
            if (!DevilFruitRegistry.canAcquireFruit(user, fruitId)) {
                DevilFruitRegistry.warnAndVanishFruit(user, fruitId);
                if (!user.isCreative()) stack.decrement(1);
                return ActionResult.SUCCESS;
            }
            DevilFruitRegistry.assignFruit(user, fruitId);
            if (user instanceof ServerPlayerEntity sp) {
                sp.sendMessage(Text.literal("§fYou have consumed the Quake-Quake Fruit!"), false);
                sp.sendMessage(Text.literal("§7R cycle | Z use | X quick | O ult | RShift awaken"), false);
            }
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 1.0F, 0.6F);
            if (!user.isCreative()) stack.decrement(1);


        }
        return ActionResult.SUCCESS;
    }
}
