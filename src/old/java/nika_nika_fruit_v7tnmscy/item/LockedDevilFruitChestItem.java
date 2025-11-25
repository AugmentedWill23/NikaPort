package nika_nika_fruit_v7tnmscy.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import nika_nika_fruit_v7tnmscy.NikaNikaFruit;

import java.util.Random;

public class LockedDevilFruitChestItem extends Item {
    public LockedDevilFruitChestItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient) return ActionResult.SUCCESS;
        ServerPlayerEntity sp = (ServerPlayerEntity) user;
        ItemStack stack = user.getStackInHand(hand);

        int keyCount = countKeys(sp);
        boolean hasKey = keyCount > 0;

        if (!hasKey) {
            // 90% fail rate without a key
            if (world.getRandom().nextFloat() < 0.90f) {
                world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_ANVIL_BREAK, SoundCategory.PLAYERS, 0.7F, 0.9F);
                sp.sendMessage(Text.literal("§8The lock resists and the chest crumbles to dust..."), false);
                if (!user.isCreative()) stack.decrement(1);
                return ActionResult.SUCCESS;
            } else {
                sp.sendMessage(Text.literal("§6You pried the lock open!"), false);
                giveLockedRewards(world, user);
                if (!user.isCreative()) stack.decrement(1);
                return ActionResult.SUCCESS;
            }
        }

        // Has a key
        if (keyCount > 5) {
            // No confirmation when bulk-opening
            consumeOneKey(sp);
            giveLockedRewards(world, user);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.UI_LOOM_TAKE_RESULT, SoundCategory.PLAYERS, 1.0F, 0.7F);
            if (!user.isCreative()) stack.decrement(1);
            return ActionResult.SUCCESS;
        }

        // Ask for confirmation via clickable chat (menu substitute)
        sp.addCommandTag("locked_confirm_pending");
        sp.sendMessage(Text.literal("§7Open Locked Chest using a key? Type §a/lockedchest confirm §7to proceed."), false);
        sp.getWorld().playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.UI_BUTTON_CLICK, SoundCategory.PLAYERS, 0.6F, 1.0F);
        return ActionResult.SUCCESS;
    }

    public static void commandConfirm(ServerPlayerEntity player) {
        if (!player.getCommandTags().contains("locked_confirm_pending")) {
            player.sendMessage(Text.literal("§cNo locked chest action pending."), false);
            return;
        }
        player.removeCommandTag("locked_confirm_pending");
        if (countKeys(player) <= 0) {
            player.sendMessage(Text.literal("§cYou don't have a key."), false);
            return;
        }
        if (!consumeOneKey(player)) {
            player.sendMessage(Text.literal("§cFailed to consume a key."), false);
            return;
        }
        if (!consumeOneLockedChest(player)) {
            player.sendMessage(Text.literal("§cNo locked chest in your inventory."), false);
            return;
        }
        giveLockedRewards(player.getWorld(), player);
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_LOOM_TAKE_RESULT, SoundCategory.PLAYERS, 1.0F, 0.7F);
    }

    private static int countKeys(ServerPlayerEntity p) {
        int total = 0;
        for (int i = 0; i < p.getInventory().size(); i++) {
            ItemStack s = p.getInventory().getStack(i);
            if (!s.isEmpty() && s.isOf(NikaNikaFruit.KEY_ITEM)) total += s.getCount();
        }
        return total;
    }

    private static boolean consumeOneKey(ServerPlayerEntity p) {
        for (int i = 0; i < p.getInventory().size(); i++) {
            ItemStack s = p.getInventory().getStack(i);
            if (!s.isEmpty() && s.isOf(NikaNikaFruit.KEY_ITEM)) {
                s.decrement(1);
                return true;
            }
        }
        return false;
    }

    private static boolean consumeOneLockedChest(ServerPlayerEntity p) {
        for (int i = 0; i < p.getInventory().size(); i++) {
            ItemStack s = p.getInventory().getStack(i);
            if (!s.isEmpty() && s.isOf(NikaNikaFruit.LOCKED_DEVIL_FRUIT_CHEST_ITEM)) {
                s.decrement(1);
                return true;
            }
        }
        return false;
    }

    private static void insertOrDrop(PlayerEntity user, ItemStack reward) {
        if (!user.getInventory().insertStack(reward)) {
            user.dropItem(reward, false);
        }
    }

    private static ItemStack randomHakiBook(Random rand) {
        int r = rand.nextInt(2);
        return switch (r) {
            case 0 -> new ItemStack(NikaNikaFruit.HAKI_BOOK_ARMAMENT);
            default -> new ItemStack(NikaNikaFruit.HAKI_BOOK_OBSERVATION);
        };
    }

    public static void giveLockedRewards(World world, PlayerEntity user) {
        Random rand = new Random();
        // Better but still low rates
        double fruitChance = 0.10; // slightly better than normal (DXQ handled separately at 0.5%)
        double brewChance = 0.60;
        double keyChance = 0.03; // keys can drop from locked chests too
        // Fruit
        boolean gaveFruit = false;
        if (rand.nextDouble() < fruitChance) {
            // Rarity order (rarest -> most common): nika, soul, darkxquake, dragon, operation, dark, quake
            int roll = rand.nextInt(1000);
            ItemStack fruit;
            if (roll < 5) fruit = new ItemStack(NikaNikaFruit.NikaNikaFruit_ITEM); // 0.5%
            else if (roll < 20) fruit = new ItemStack(NikaNikaFruit.SOUL_FRUIT_ITEM); // 1.5%
            else if (roll < 60) fruit = new ItemStack(NikaNikaFruit.DARKXQUAKE_FRUIT_ITEM); // 4%
            else if (roll < 140) fruit = new ItemStack(NikaNikaFruit.DRAGON_FRUIT_ITEM); // 8%
            else if (roll < 240) fruit = new ItemStack(NikaNikaFruit.OPERATION_FRUIT_ITEM); // 10%
            else if (roll < 500) fruit = new ItemStack(NikaNikaFruit.DARK_FRUIT_ITEM); // 26%
            else fruit = new ItemStack(NikaNikaFruit.QUAKE_FRUIT_ITEM); // remaining 49%
            insertOrDrop(user, fruit);
            gaveFruit = true;
            user.sendMessage(Text.literal("§6You found a Devil Fruit!"), false);
        }
        
        // Haki books (small chance)
        if (rand.nextDouble() < 0.15) {
            insertOrDrop(user, randomHakiBook(rand));
        }
        // Brews
        if (!gaveFruit && rand.nextDouble() < brewChance) {
            int count = 1 + rand.nextInt(3);
            insertOrDrop(user, new ItemStack(NikaNikaFruit.MYSTERIOUS_BREW_ITEM, count));
        }
        // Netheirite gear chances
        if (rand.nextDouble() < 0.10) insertOrDrop(user, new ItemStack(net.minecraft.item.Items.NETHERITE_SWORD));
        if (rand.nextDouble() < 0.08) insertOrDrop(user, new ItemStack(net.minecraft.item.Items.NETHERITE_PICKAXE));
        if (rand.nextDouble() < 0.12) insertOrDrop(user, new ItemStack(net.minecraft.item.Items.NETHERITE_INGOT));

        // Other valuable items pool (2-4 items)
        net.minecraft.item.Item[] pool = new net.minecraft.item.Item[] {
            net.minecraft.item.Items.DIAMOND_SWORD,
            net.minecraft.item.Items.DIAMOND_PICKAXE,
            net.minecraft.item.Items.DIAMOND_AXE,
            net.minecraft.item.Items.DIAMOND_SHOVEL,
            net.minecraft.item.Items.ENCHANTED_GOLDEN_APPLE,
            net.minecraft.item.Items.GOLDEN_APPLE,
            net.minecraft.item.Items.BOW,
            net.minecraft.item.Items.CROSSBOW,
            net.minecraft.item.Items.ARROW,
            net.minecraft.item.Items.POTION
        };
        int items = 2 + rand.nextInt(3);
        for (int i = 0; i < items; i++) {
            net.minecraft.item.Item it = pool[rand.nextInt(pool.length)];
            insertOrDrop(user, new ItemStack(it));
        }

        // Key rare drop
        if (rand.nextDouble() < keyChance) insertOrDrop(user, new ItemStack(NikaNikaFruit.KEY_ITEM));

        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.UI_LOOM_TAKE_RESULT, SoundCategory.PLAYERS, 1.0F, 0.7F);
    }
}
