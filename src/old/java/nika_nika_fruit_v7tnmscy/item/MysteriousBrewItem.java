package nika_nika_fruit_v7tnmscy.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import nika_nika_fruit_v7tnmscy.DevilFruitRegistry;

public class MysteriousBrewItem extends Item {
    public MysteriousBrewItem(Settings settings) { super(settings); }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient) {
            // Remove the current devil fruit ownership and powers, but preserve mastery
            DevilFruitRegistry.clearFruit(user);
            // Deactivate any active transformations/buffs
            NikaNikaFruitItem.deactivateTransformation(user);
            user.sendMessage(Text.literal("§dYou drink the Mysterious Brew. Your Devil Fruit powers fade away, but your mastery remains."), false);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.8F, 1.2F);
            if (!user.isCreative()) {
                stack.decrement(1);
            }
        }
        return ActionResult.SUCCESS;
    }
}
