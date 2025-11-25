package nika_nika_fruit_v7tnmscy.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import nika_nika_fruit_v7tnmscy.NikaNikaFruit;

import java.util.Random;

public class DevilsChestItem extends Item {
    public DevilsChestItem(Settings settings) { super(settings); }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient) return ActionResult.SUCCESS;
        ItemStack stack = user.getStackInHand(hand);

        // Require Devil's Key in inventory
        int keySlot = findDevilsKey(user);
        if (keySlot < 0) {
            user.sendMessage(Text.literal("§8The Devil's Chest remains sealed. You need a §cDevil's Key§8."), false);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.PLAYERS, 0.7F, 0.8F);
            return ActionResult.SUCCESS;
        }

        // Consume one Devil's Key
        ItemStack key = user.getInventory().getStack(keySlot);
        key.decrement(1);

        // Open and give reward
        giveReward(world, user);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1.0F, 1.0F);
        if (!user.isCreative()) stack.decrement(1);
        return ActionResult.SUCCESS;
    }

    private int findDevilsKey(PlayerEntity user) {
        for (int i = 0; i < user.getInventory().size(); i++) {
            ItemStack s = user.getInventory().getStack(i);
            if (!s.isEmpty() && s.isOf(NikaNikaFruit.DEVILS_KEY_ITEM)) return i;
        }
        return -1;
    }

    private static void insertOrDrop(PlayerEntity user, ItemStack reward) {
        if (!user.getInventory().insertStack(reward)) {
            user.dropItem(reward, false);
        }
    }

    private void giveReward(World world, PlayerEntity user) {
        Random rand = new Random();
        // Weights based on requested percentages (scaled by 100):
        // God-like 0.01% -> 1, Mythical 0.1% -> 10, Legendary 0.5% -> 50, Epic 1% -> 100,
        // Rare 5% -> 500, Uncommon 45% -> 4500, Common 49.39% -> 4939 (sum 10100)
        int roll = rand.nextInt(10100);
        if (roll < 1) { // God-like
            int which = rand.nextInt(3);
            ItemStack reward = switch (which) {
                case 0 -> new ItemStack(NikaNikaFruit.SOUL_FRUIT_ITEM);
                case 1 -> new ItemStack(NikaNikaFruit.NikaNikaFruit_ITEM);
                default -> new ItemStack(NikaNikaFruit.DARKXQUAKE_FRUIT_ITEM);
            };
            insertOrDrop(user, reward);
            user.sendMessage(Text.literal("§d§lGOD‑LIKE! §fYou obtained a legendary Devil Fruit."), false);
            return;
        }
        roll -= 1;
        if (roll < 10) { // Mythical
            int which = rand.nextInt(5);
            ItemStack reward = switch (which) {
                case 0 -> new ItemStack(NikaNikaFruit.BLACKBEARD_DNA_ITEM);
                case 1 -> new ItemStack(NikaNikaFruit.DARK_FRUIT_ITEM);
                case 2 -> new ItemStack(NikaNikaFruit.QUAKE_FRUIT_ITEM);
                case 3 -> new ItemStack(NikaNikaFruit.DRAGON_FRUIT_ITEM);
                default -> new ItemStack(NikaNikaFruit.OPERATION_FRUIT_ITEM);
            };
            insertOrDrop(user, reward);
            user.sendMessage(Text.literal("§5§lMYTHICAL! §fA rare power awakens."), false);
            return;
        }
        roll -= 10;
        if (roll < 50) { // Legendary
            int which = rand.nextInt(5);
            ItemStack reward = switch (which) {
                case 0 -> new ItemStack(NikaNikaFruit.MYSTERIOUS_BREW_ITEM, 1 + rand.nextInt(3));
                case 1 -> new ItemStack(NikaNikaFruit.HAKI_BOOK_ARMAMENT);
                case 2 -> new ItemStack(NikaNikaFruit.HAKI_BOOK_CONQUERORS);
                case 3 -> new ItemStack(NikaNikaFruit.HAKI_BOOK_OBSERVATION);
                default -> new ItemStack(NikaNikaFruit.AWAKENING_CRYSTAL_ITEM);
            };
            insertOrDrop(user, reward);
            user.sendMessage(Text.literal("§6§lLEGENDARY! §fTreasures of power."), false);
            return;
        }
        roll -= 50;
        if (roll < 100) { // Epic
            int which = rand.nextInt(3);
            ItemStack reward = switch (which) {
                case 0 -> new ItemStack(net.minecraft.item.Items.NETHERITE_INGOT);
                case 1 -> new ItemStack(NikaNikaFruit.MAGIC_CORE_ITEM);
                default -> new ItemStack(NikaNikaFruit.KEY_ITEM);
            };
            insertOrDrop(user, reward);
            user.sendMessage(Text.literal("§d§lEPIC!"), false);
            return;
        }
        roll -= 100;
        if (roll < 500) { // Rare
            insertOrDrop(user, new ItemStack(net.minecraft.item.Items.DIAMOND, 1 + rand.nextInt(2)));
            user.sendMessage(Text.literal("§9§lRARE! §fShiny diamonds."), false);
            return;
        }
        roll -= 500;
        if (roll < 4500) { // Uncommon (iron items)
            net.minecraft.item.Item[] iron = new net.minecraft.item.Item[] {
                net.minecraft.item.Items.IRON_INGOT,
                net.minecraft.item.Items.IRON_SWORD,
                net.minecraft.item.Items.IRON_PICKAXE,
                net.minecraft.item.Items.IRON_AXE,
                net.minecraft.item.Items.IRON_SHOVEL,
                net.minecraft.item.Items.IRON_CHESTPLATE,
                net.minecraft.item.Items.IRON_LEGGINGS,
                net.minecraft.item.Items.IRON_BOOTS,
                net.minecraft.item.Items.IRON_HELMET
            };
            net.minecraft.item.Item choice = iron[rand.nextInt(iron.length)];
            insertOrDrop(user, new ItemStack(choice, choice == net.minecraft.item.Items.IRON_INGOT ? (2 + rand.nextInt(3)) : 1));
            user.sendMessage(Text.literal("§a§lUNCOMMON"), false);
            return;
        }
        // Common (wooden items)
        net.minecraft.item.Item[] wood = new net.minecraft.item.Item[] {
            net.minecraft.item.Items.OAK_PLANKS,
            net.minecraft.item.Items.SPRUCE_PLANKS,
            net.minecraft.item.Items.BIRCH_PLANKS,
            net.minecraft.item.Items.JUNGLE_PLANKS,
            net.minecraft.item.Items.ACACIA_PLANKS,
            net.minecraft.item.Items.DARK_OAK_PLANKS,
            net.minecraft.item.Items.STICK,
            net.minecraft.item.Items.WOODEN_SWORD,
            net.minecraft.item.Items.WOODEN_PICKAXE,
            net.minecraft.item.Items.WOODEN_AXE,
            net.minecraft.item.Items.WOODEN_SHOVEL,
            net.minecraft.item.Items.CHEST
        };
        net.minecraft.item.Item w = wood[rand.nextInt(wood.length)];
        int count = (w == net.minecraft.item.Items.STICK) ? (4 + rand.nextInt(5)) : (w.toString().contains("PLANKS") ? (8 + rand.nextInt(9)) : 1);
        insertOrDrop(user, new ItemStack(w, count));
        user.sendMessage(Text.literal("§7Common loot."), false);
    }
}
