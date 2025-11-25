package nika_nika_fruit_v7tnmscy.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.player.PlayerEntity;

public class ObservationHakiBookItem extends Item {
    public ObservationHakiBookItem(Settings settings) { super(settings); }

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
            float prog = nika_nika_fruit_v7tnmscy.haki.HakiSystem.getObservationProgress(user);
            boolean unlocked = nika_nika_fruit_v7tnmscy.haki.HakiSystem.isObservationUnlocked(user);
            if (unlocked) {
                user.sendMessage(Text.literal("§bObservation already unlocked."), true);
            } else {
                long noHitTicks = nika_nika_fruit_v7tnmscy.haki.HakiSystem.getObservationNoHitTicks(user);
                boolean noHitDone = noHitTicks >= (15L * 60L * 20L);
                if (prog >= 10000f && noHitDone) {
                    if (user instanceof net.minecraft.server.network.ServerPlayerEntity sp) {
                        nika_nika_fruit_v7tnmscy.haki.HakiSystem.cmdUnlock(sp, "observation");
                        user.getStackInHand(hand).decrement(1); // consume the book
                    }
                } else {
                    int needSecs = (int)((15L*60L*20L - Math.min(noHitTicks, 15L*60L*20L)) / 20L);
                    String mmss = String.format("%02d:%02d", needSecs/60, needSecs%60);
                    user.sendMessage(Text.literal("§7Objective A: Take §e10000§7 total damage. Progress: §e" + (int)prog + "/10000"), false);
                    user.sendMessage(Text.literal("§7Objective B: Survive §e15:00§7 without taking damage. Remaining: §e" + mmss), false);
                    user.sendMessage(Text.literal("§8Both objectives must be complete, then right-click this book again."), false);
                }
            }
        }
        return net.minecraft.util.ActionResult.SUCCESS;
    }
}