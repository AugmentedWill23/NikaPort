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

public class BlackbeardDNAItem extends Item {
    public BlackbeardDNAItem(Settings settings) { super(settings); }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient) {
            // Mark the player as allowed to consume DarkxQuake regardless of other requirements
            user.addCommandTag("bb_dna");
            if (user instanceof ServerPlayerEntity sp) {
                sp.sendMessage(Text.literal("§8You feel a forbidden power coursing through your veins..."), false);
                // Grant achievement THE NEW MENACE immediately
                nika_nika_fruit_v7tnmscy.item.NikaNikaFruitItem.grantAdvancement(sp, "the_menace_begins");
            }
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_WARDEN_HEARTBEAT, SoundCategory.PLAYERS, 1.0F, 0.6F);
            if (!user.isCreative()) stack.decrement(1);
        }
        return ActionResult.SUCCESS;
    }

    public static boolean hasDNA(PlayerEntity player) {
        return player.getCommandTags().contains("bb_dna");
    }
}
