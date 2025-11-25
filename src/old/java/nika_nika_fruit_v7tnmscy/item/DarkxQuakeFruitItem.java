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

public class DarkxQuakeFruitItem extends Item {
    public DarkxQuakeFruitItem(Settings settings) { super(settings); }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient) {
            // There are two versions: survival-locked fusion, and creative-only admin variant
            if (user.isCreative()) {
                // Creative use grants the power directly
                nika_nika_fruit_v7tnmscy.DevilFruitRegistry.clearFruit(user);
                if (nika_nika_fruit_v7tnmscy.DevilFruitRegistry.canAcquireFruit(user, DevilFruitRegistry.FRUIT_DARKXQUAKE)) {
                    nika_nika_fruit_v7tnmscy.DevilFruitRegistry.assignFruit(user, DevilFruitRegistry.FRUIT_DARKXQUAKE);
                    if (user instanceof ServerPlayerEntity sp) sp.sendMessage(Text.literal("§8§lAdmin: DarkxQuake granted."), false);
                }
                if (!user.isCreative()) stack.decrement(1); // In creative we still don't consume the item
                return ActionResult.SUCCESS;
            } else {
                // Survival: normally requires mastery 50 on both, but Blackbeard DNA bypasses requirements
                boolean hasReq = nika_nika_fruit_v7tnmscy.item.BlackbeardDNAItem.hasDNA(user);
                if (hasReq && DevilFruitRegistry.canAcquireFruit(user, DevilFruitRegistry.FRUIT_DARKXQUAKE)) {
                    nika_nika_fruit_v7tnmscy.DevilFruitRegistry.clearFruit(user);
                    nika_nika_fruit_v7tnmscy.DevilFruitRegistry.assignFruit(user, DevilFruitRegistry.FRUIT_DARKXQUAKE);
                    if (user instanceof ServerPlayerEntity sp) {
                        sp.sendMessage(Text.literal("§8§lYou devour the fusion fruit. DarkxQuake awakens within you!"), false);
                        // Grant achievement "THE MENACE BEGINS"
                        nika_nika_fruit_v7tnmscy.item.NikaNikaFruitItem.grantAdvancement(sp, "the_menace_begins");
                    }
                } else {
                    if (user instanceof ServerPlayerEntity sp) {
                        sp.sendMessage(Text.literal("§cOnly one who possesses Blackbeard's DNA may consume this."), true);
                    }
                }
                if (!user.isCreative()) stack.decrement(1); // In creative we still don't consume the item
            }
        }
        return ActionResult.SUCCESS;
    }
}
