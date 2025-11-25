package nika_nika_fruit_v7tnmscy.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.player.PlayerEntity;

public class ConquerorsHakiBookItem extends Item {
    public ConquerorsHakiBookItem(Settings settings) { super(settings); }

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
            user.sendMessage(Text.literal("§5Conqueror's unlock chances:"), false);
            user.sendMessage(Text.literal("§7- §e0.01%§7 permanent on world join"), false);
            user.sendMessage(Text.literal("§7- §e0.001%§7 on death to Wither or Warden (world-only)"), false);
            user.sendMessage(Text.literal("§7- §e0.0001%§7 on death to Ender Dragon (permanent)"), false);
            user.sendMessage(Text.literal("§8Admins may bless with a hidden rite."), false);
        }
        return net.minecraft.util.ActionResult.SUCCESS;
    }
}