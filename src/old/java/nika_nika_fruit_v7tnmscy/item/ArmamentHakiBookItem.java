package nika_nika_fruit_v7tnmscy.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.player.PlayerEntity;

public class ArmamentHakiBookItem extends Item {
    public ArmamentHakiBookItem(Settings settings) { super(settings); }

    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, net.minecraft.entity.Entity entity, net.minecraft.entity.EquipmentSlot slot) {
        if (entity instanceof PlayerEntity p) {
            if (ItemStack.areItemsAndComponentsEqual(p.getOffHandStack(), stack) && world.getTime() % 200 == 0) {
                p.sendMessage(Text.literal("§7[Up/Down] select Haki type, §7[H] select move, §7[G] use"), true);
            }
        }
    }

    @Override
    public net.minecraft.util.ActionResult use(net.minecraft.world.World world, net.minecraft.entity.player.PlayerEntity user, net.minecraft.util.Hand hand) {
        if (!world.isClient) {
            float prog = nika_nika_fruit_v7tnmscy.haki.HakiSystem.getArmamentProgress(user);
            boolean unlocked = nika_nika_fruit_v7tnmscy.haki.HakiSystem.isArmamentUnlocked(user);
            if (unlocked) {
                user.sendMessage(Text.literal("§8Armament already unlocked."), true);
            } else {
                if (prog >= 10000f) {
                    if (user instanceof net.minecraft.server.network.ServerPlayerEntity sp) {
                        nika_nika_fruit_v7tnmscy.haki.HakiSystem.cmdUnlock(sp, "armament");
                        user.getStackInHand(hand).decrement(1); // consume the book
                    }
                } else {
                    user.sendMessage(Text.literal("§7Objective: Deal §e10000§7 damage with §lfists only§r. Progress: §e" + (int)prog + "/10000"), false);
                }
            }
        }
        return net.minecraft.util.ActionResult.SUCCESS;
    }
}