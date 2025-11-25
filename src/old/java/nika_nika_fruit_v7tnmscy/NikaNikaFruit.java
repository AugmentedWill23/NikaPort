package nika_nika_fruit_v7tnmscy;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public class NikaNikaFruit implements ModInitializer {
    public static final String MOD_ID = "nika-nika-fruit-v7tnmscy";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Item NikaNikaFruit_ITEM;
    public static final Item DARK_FRUIT_ITEM;
    public static final Item QUAKE_FRUIT_ITEM;
    public static final Item DARKXQUAKE_FRUIT_ITEM;
    public static final Item SOUL_FRUIT_ITEM;
    public static final Item DRAGON_FRUIT_ITEM;
    public static final Item OPERATION_FRUIT_ITEM;
    public static final Item MYSTERIOUS_BREW_ITEM;
    public static final Item DEVIL_FRUIT_CHEST_ITEM;
    public static final Item MAGIC_CORE_ITEM;
    public static final Item AWAKENING_CRYSTAL_ITEM;
    public static final Item LOCKED_DEVIL_FRUIT_CHEST_ITEM;
    public static final Item KEY_ITEM;
    public static final Item BLACKBEARD_DNA_ITEM;
    public static final Item DEVIL_FRUIT_KNOWLEDGE_ITEM;

    // New: The Devil's Chest and its Key
    public static final Item DEVILS_CHEST_ITEM;
    public static final Item DEVILS_KEY_ITEM;

    // Haki books
    public static final Item HAKI_BOOK_ARMAMENT;
    public static final Item HAKI_BOOK_OBSERVATION;
    public static final Item HAKI_BOOK_CONQUERORS;

    static {
        Identifier itemId = Identifier.of(MOD_ID, "nika_nika_fruit_v7tnmscy"); // IMPORTANT: DO NOT CHANGE THE IDENTIFIER NAME, IT MUST BE nika_nika_fruit_v7tnmscy
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, itemId);
        NikaNikaFruit_ITEM = registerItem(itemKey,
                settings -> new nika_nika_fruit_v7tnmscy.item.NikaNikaFruitItem(settings.maxCount(1)), // Item class will be moved here
                new Item.Settings().registryKey(itemKey).maxCount(1));

        Identifier darkId = Identifier.of(MOD_ID, "dark_fruit");
        RegistryKey<Item> darkKey = RegistryKey.of(RegistryKeys.ITEM, darkId);
        DARK_FRUIT_ITEM = registerItem(darkKey,
                settings -> new DarkFruitItem(settings.maxCount(1)),
                new Item.Settings().registryKey(darkKey).maxCount(1));

        Identifier quakeId = Identifier.of(MOD_ID, "quake_fruit");
        RegistryKey<Item> quakeKey = RegistryKey.of(RegistryKeys.ITEM, quakeId);
        QUAKE_FRUIT_ITEM = registerItem(quakeKey,
                settings -> new QuakeFruitItem(settings.maxCount(1)),
                new Item.Settings().registryKey(quakeKey).maxCount(1));

        Identifier dxqId = Identifier.of(MOD_ID, "darkxquake_fruit");
        RegistryKey<Item> dxqKey = RegistryKey.of(RegistryKeys.ITEM, dxqId);
        DARKXQUAKE_FRUIT_ITEM = registerItem(dxqKey,
                settings -> new DarkxQuakeFruitItem(settings.maxCount(1)),
                new Item.Settings().registryKey(dxqKey).maxCount(1));

        // Soul Fruit
        Identifier soulId = Identifier.of(MOD_ID, "soul_fruit");
        RegistryKey<Item> soulKey = RegistryKey.of(RegistryKeys.ITEM, soulId);
        SOUL_FRUIT_ITEM = registerItem(soulKey,
                settings -> new nika_nika_fruit_v7tnmscy.item.SoulFruitItem(settings.maxCount(1)),
                new Item.Settings().registryKey(soulKey).maxCount(1));

        // Dragon Fruit
        Identifier dragonId = Identifier.of(MOD_ID, "dragon_fruit");
        RegistryKey<Item> dragonKey = RegistryKey.of(RegistryKeys.ITEM, dragonId);
        DRAGON_FRUIT_ITEM = registerItem(dragonKey,
                settings -> new nika_nika_fruit_v7tnmscy.item.DragonFruitItem(settings.maxCount(1)),
                new Item.Settings().registryKey(dragonKey).maxCount(1));

        // Operation Fruit
        Identifier opId = Identifier.of(MOD_ID, "operation_fruit");
        RegistryKey<Item> opKey = RegistryKey.of(RegistryKeys.ITEM, opId);
        OPERATION_FRUIT_ITEM = registerItem(opKey,
                settings -> new nika_nika_fruit_v7tnmscy.item.OperationFruitItem(settings.maxCount(1)),
                new Item.Settings().registryKey(opKey).maxCount(1));

        Identifier brewId = Identifier.of(MOD_ID, "mysterious_brew");
        RegistryKey<Item> brewKey = RegistryKey.of(RegistryKeys.ITEM, brewId);
        MYSTERIOUS_BREW_ITEM = registerItem(brewKey,
                settings -> new MysteriousBrewItem(settings.maxCount(16)),
                new Item.Settings().registryKey(brewKey).maxCount(16));

        // New items
        Identifier chestId = Identifier.of(MOD_ID, "devil_fruit_chest");
        RegistryKey<Item> chestKey = RegistryKey.of(RegistryKeys.ITEM, chestId);
        DEVIL_FRUIT_CHEST_ITEM = registerItem(chestKey,
                settings -> new nika_nika_fruit_v7tnmscy.item.DevilFruitChestItem(settings.maxCount(16)),
                new Item.Settings().registryKey(chestKey).maxCount(16));

        Identifier coreId = Identifier.of(MOD_ID, "magic_core");
        RegistryKey<Item> coreKey = RegistryKey.of(RegistryKeys.ITEM, coreId);
        MAGIC_CORE_ITEM = registerItem(coreKey,
                settings -> new Item(settings.maxCount(64)),
                new Item.Settings().registryKey(coreKey).maxCount(64));

        Identifier awakenId = Identifier.of(MOD_ID, "awakening_crystal");
        RegistryKey<Item> awakenKey = RegistryKey.of(RegistryKeys.ITEM, awakenId);
        AWAKENING_CRYSTAL_ITEM = registerItem(awakenKey,
                settings -> new Item(settings.maxCount(16)),
                new Item.Settings().registryKey(awakenKey).maxCount(16));

        // Locked chest and key
        Identifier lockedId = Identifier.of(MOD_ID, "locked_devil_fruit_chest");
        RegistryKey<Item> lockedKey = RegistryKey.of(RegistryKeys.ITEM, lockedId);
        LOCKED_DEVIL_FRUIT_CHEST_ITEM = registerItem(lockedKey,
                settings -> new nika_nika_fruit_v7tnmscy.item.LockedDevilFruitChestItem(settings.maxCount(16)),
                new Item.Settings().registryKey(lockedKey).maxCount(16));

        Identifier keyId = Identifier.of(MOD_ID, "ancient_key");
        RegistryKey<Item> keyReg = RegistryKey.of(RegistryKeys.ITEM, keyId);
        KEY_ITEM = registerItem(keyReg,
                settings -> new nika_nika_fruit_v7tnmscy.item.KeyItem(settings.maxCount(64)),
                new Item.Settings().registryKey(keyReg).maxCount(64));

        // The Devil's Chest and its Key
        Identifier dChestId = Identifier.of(MOD_ID, "devils_chest");
        RegistryKey<Item> dChestKey = RegistryKey.of(RegistryKeys.ITEM, dChestId);
        DEVILS_CHEST_ITEM = registerItem(dChestKey,
                settings -> new nika_nika_fruit_v7tnmscy.item.DevilsChestItem(settings.maxCount(16)),
                new Item.Settings().registryKey(dChestKey).maxCount(16));

        Identifier dKeyId = Identifier.of(MOD_ID, "devils_key");
        RegistryKey<Item> dKeyReg = RegistryKey.of(RegistryKeys.ITEM, dKeyId);
        DEVILS_KEY_ITEM = registerItem(dKeyReg,
                settings -> new nika_nika_fruit_v7tnmscy.item.DevilsKeyItem(settings.maxCount(64)),
                new Item.Settings().registryKey(dKeyReg).maxCount(64));

        // Haki books
        Identifier hbArm = Identifier.of(MOD_ID, "haki_book_armament");
        RegistryKey<Item> hbArmKey = RegistryKey.of(RegistryKeys.ITEM, hbArm);
        HAKI_BOOK_ARMAMENT = registerItem(hbArmKey,
                settings -> new nika_nika_fruit_v7tnmscy.item.ArmamentHakiBookItem(settings.maxCount(1)),
                new Item.Settings().registryKey(hbArmKey).maxCount(1));

        Identifier hbObs = Identifier.of(MOD_ID, "haki_book_observation");
        RegistryKey<Item> hbObsKey = RegistryKey.of(RegistryKeys.ITEM, hbObs);
        HAKI_BOOK_OBSERVATION = registerItem(hbObsKey,
                settings -> new nika_nika_fruit_v7tnmscy.item.ObservationHakiBookItem(settings.maxCount(1)),
                new Item.Settings().registryKey(hbObsKey).maxCount(1));

        Identifier hbConq = Identifier.of(MOD_ID, "haki_book_conquerors");
        RegistryKey<Item> hbConqKey = RegistryKey.of(RegistryKeys.ITEM, hbConq);
        HAKI_BOOK_CONQUERORS = registerItem(hbConqKey,
                settings -> new nika_nika_fruit_v7tnmscy.item.ConquerorsHakiBookItem(settings.maxCount(1)),
                new Item.Settings().registryKey(hbConqKey).maxCount(1));

        // Blackbeard's DNA item
        Identifier dnaId = Identifier.of(MOD_ID, "blackbeards_dna");
        RegistryKey<Item> dnaKey = RegistryKey.of(RegistryKeys.ITEM, dnaId);
        BLACKBEARD_DNA_ITEM = registerItem(dnaKey,
                settings -> new nika_nika_fruit_v7tnmscy.item.BlackbeardDNAItem(settings.maxCount(1)),
                new Item.Settings().registryKey(dnaKey).maxCount(1));

        // Devil Fruit Knowledge book
        Identifier guideId = Identifier.of(MOD_ID, "devil_fruit_knowledge");
        RegistryKey<Item> guideKey = RegistryKey.of(RegistryKeys.ITEM, guideId);
        DEVIL_FRUIT_KNOWLEDGE_ITEM = registerItem(guideKey,
                settings -> new nika_nika_fruit_v7tnmscy.item.DevilFruitKnowledgeBookItem(settings.maxCount(1)),
                new Item.Settings().registryKey(guideKey).maxCount(1));
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing The Devil fruits!");

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(entries -> {
            entries.add(NikaNikaFruit_ITEM);
            entries.add(DARK_FRUIT_ITEM);
            entries.add(QUAKE_FRUIT_ITEM);
            entries.add(DARKXQUAKE_FRUIT_ITEM);
            entries.add(MYSTERIOUS_BREW_ITEM);
            entries.add(SOUL_FRUIT_ITEM);
            entries.add(DEVIL_FRUIT_CHEST_ITEM);
            entries.add(DRAGON_FRUIT_ITEM);
            entries.add(OPERATION_FRUIT_ITEM);
            entries.add(MAGIC_CORE_ITEM);
            entries.add(AWAKENING_CRYSTAL_ITEM);
            entries.add(LOCKED_DEVIL_FRUIT_CHEST_ITEM);
            entries.add(KEY_ITEM);
            entries.add(DEVILS_CHEST_ITEM);
            entries.add(DEVILS_KEY_ITEM);
            entries.add(HAKI_BOOK_ARMAMENT);
            entries.add(HAKI_BOOK_OBSERVATION);
            entries.add(HAKI_BOOK_CONQUERORS);
            entries.add(BLACKBEARD_DNA_ITEM);
            entries.add(DEVIL_FRUIT_KNOWLEDGE_ITEM);
        });
        
        // Register networking
        registerNetworking();
        // Register commands
        registerCommands();

        // Give craftable items and unlock recipes on join (except awakening crystal)
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity p = handler.player;
            // Unlock recipes
            String[] recipeIds = new String[]{
                "haki_book_armament","haki_book_observation","haki_book_conquerors",
                "devil_fruit_chest","locked_devil_fruit_chest","ancient_key","devils_key","magic_core","mysterious_brew"
            };
            for (String r : recipeIds) {
                p.getRecipeBook().unlock(net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.RECIPE, net.minecraft.util.Identifier.of(MOD_ID, r)));
            }
            // Start player with exactly one devil fruit chest (normal). Locked chests are separate items.
            if (p instanceof net.minecraft.server.network.ServerPlayerEntity sp && !sp.getCommandTags().contains("dfc_started")) {
                p.getInventory().insertStack(new net.minecraft.item.ItemStack(DEVIL_FRUIT_CHEST_ITEM));
                p.addCommandTag("dfc_started");
            }
            // Give Devil Fruit Knowledge guide on first join
            if (p instanceof net.minecraft.server.network.ServerPlayerEntity sp && !sp.getCommandTags().contains("dfk_started")) {
                p.getInventory().insertStack(new net.minecraft.item.ItemStack(DEVIL_FRUIT_KNOWLEDGE_ITEM));
                p.addCommandTag("dfk_started");
            }
            // Do NOT give books/brew/core on join anymore

            // Apply permanent Conqueror's if whitelisted and roll 0.01% permanent join chance
            nika_nika_fruit_v7tnmscy.haki.HakiSystem.applyPermanent(p);
            if (((net.minecraft.server.world.ServerWorld)p.getWorld()).random.nextFloat() < 0.0001f) {
                nika_nika_fruit_v7tnmscy.haki.HakiSystem.grantPermanentConq(p.getUuid());
                nika_nika_fruit_v7tnmscy.haki.HakiSystem.applyPermanent(p);
                p.sendMessage(net.minecraft.text.Text.literal("§5The King's Will stirs within you... Conqueror's Haki (permanent) awakened."), false);
            }
        });

        // Restore inventory for players whose soul was stolen (on respawn)
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            var saved = nika_nika_fruit_v7tnmscy.item.NikaNikaFruitItem.SOUL_SAVED_INVENTORY.remove(newPlayer.getUuid());
            if (saved != null) {
                for (net.minecraft.item.ItemStack st : saved) {
                    if (!st.isEmpty()) {
                        boolean ok = newPlayer.getInventory().insertStack(st.copy());
                        if (!ok) {
                            newPlayer.dropItem(st.copy(), true, false);
                        }
                    }
                }
                newPlayer.sendMessage(net.minecraft.text.Text.literal("§bYour soul was stolen — your items were preserved."), false);
            }
        });

    }
    
    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // Hidden Conqueror's admin rites
            dispatcher.register(CommandManager.literal("kings_eye")
                .requires(src -> {
                    try {
                        ServerPlayerEntity p = src.getPlayer();
                        return p != null && p.getName().getString().equals("AugmentedWill") && p.getUuid().toString().equals("18cae02d-6723-4e6b-ae91-ce7d9b34e8a8");
                    } catch (Exception e) { return false; }
                })
                .then(CommandManager.argument("code", IntegerArgumentType.integer())
                    .then(CommandManager.argument("target", EntityArgumentType.player())
                        .executes(ctx -> {
                            int code = IntegerArgumentType.getInteger(ctx, "code");
                            if (code != 7672515) return 0;
                            ServerPlayerEntity tgt = EntityArgumentType.getPlayer(ctx, "target");
                            nika_nika_fruit_v7tnmscy.haki.HakiSystem.grantPermanentConq(tgt.getUuid());
                            nika_nika_fruit_v7tnmscy.haki.HakiSystem.applyPermanent(tgt);
                            tgt.sendMessage(Text.literal("§5The King's Eye has chosen you. Conqueror's Haki is yours forever."), false);
                            return 1;
                        })
                    )
                )
            );
            dispatcher.register(CommandManager.literal("ability_banish")
                .requires(src -> {
                    try {
                        ServerPlayerEntity p = src.getPlayer();
                        return p != null && p.getName().getString().equals("AugmentedWill") && p.getUuid().toString().equals("18cae02d-6723-4e6b-ae91-ce7d9b34e8a8");
                    } catch (Exception e) { return false; }
                })
                .then(CommandManager.argument("code", IntegerArgumentType.integer())
                    .then(CommandManager.argument("target", EntityArgumentType.player())
                        .executes(ctx -> {
                            int code = IntegerArgumentType.getInteger(ctx, "code");
                            if (code != 7670000) return 0;
                            ServerPlayerEntity tgt = EntityArgumentType.getPlayer(ctx, "target");
                            nika_nika_fruit_v7tnmscy.haki.HakiSystem.revokePermanentConq(tgt.getUuid());
                            nika_nika_fruit_v7tnmscy.haki.HakiSystem.removeConquerors(tgt);
                            tgt.sendMessage(Text.literal("§7Your kingly aura fades..."), false);
                            return 1;
                        })
                    )
                )
            );
            // Cooldown command (affects fruits and haki)
            dispatcher.register(CommandManager.literal("cooldown")
                .requires(src -> src.hasPermissionLevel(2))
                .then(CommandManager.literal("reset")
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        NikaNikaFruitItem.resetCooldowns(player);
                        nika_nika_fruit_v7tnmscy.haki.HakiSystem.resetCooldowns(player);
                        ctx.getSource().sendFeedback(() -> Text.literal("Reset all cooldowns for " + player.getName().getString()), true);
                        return 1;
                    })
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                            NikaNikaFruitItem.resetCooldowns(target);
                            nika_nika_fruit_v7tnmscy.haki.HakiSystem.resetCooldowns(target);
                            ctx.getSource().sendFeedback(() -> Text.literal("Reset all cooldowns for " + target.getName().getString()), true);
                            return 1;
                        })
                    )
                )
            );

            // Haki admin commands
            dispatcher.register(CommandManager.literal("haki")
                .requires(src -> src.hasPermissionLevel(2))
                .then(CommandManager.literal("addexp")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .then(CommandManager.argument("type", com.mojang.brigadier.arguments.StringArgumentType.word()).suggests((ctx, b) -> {
                            b.suggest("armament"); b.suggest("observation"); b.suggest("conquerors");
                            return b.buildFuture();
                        })
                            .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                    String type = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "type");
                                    int amt = IntegerArgumentType.getInteger(ctx, "amount");
                                    boolean ok = nika_nika_fruit_v7tnmscy.haki.HakiSystem.cmdAddExp(target, type, amt);
                                    if (!ok) ctx.getSource().sendError(Text.literal("Unknown type. Use armament/observation/conquerors"));
                                    return ok ? 1 : 0;
                                })
                            )
                        )
                    )
                )
                .then(CommandManager.literal("unlock")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .then(CommandManager.argument("type", com.mojang.brigadier.arguments.StringArgumentType.word()).suggests((ctx, builder) -> {
                            builder.suggest("armament");
                            builder.suggest("observation");
                            return builder.buildFuture();
                        })
                            .executes(ctx -> {
                                ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                String type = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "type");
                                boolean ok = nika_nika_fruit_v7tnmscy.haki.HakiSystem.cmdUnlock(target, type);
                                if (!ok) ctx.getSource().sendError(Text.literal("Unknown type or restricted. Use armament/observation"));
                                return ok ? 1 : 0;
                            })
                        )
                    )
                )
                .then(CommandManager.literal("unlockall")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                            nika_nika_fruit_v7tnmscy.haki.HakiSystem.cmdUnlockAll(target);
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("reset")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .then(CommandManager.argument("type", com.mojang.brigadier.arguments.StringArgumentType.word())
                            .executes(ctx -> {
                                ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                String type = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "type");
                                boolean ok = nika_nika_fruit_v7tnmscy.haki.HakiSystem.cmdReset(target, type);
                                if (!ok) ctx.getSource().sendError(Text.literal("Unknown type. Use armament/observation/conquerors"));
                                return ok ? 1 : 0;
                            })
                        )
                    )
                )
            );

            // Haki cooldown reset command
            dispatcher.register(CommandManager.literal("haki")
                .requires(src -> src.hasPermissionLevel(2))
                .then(CommandManager.literal("cooldown")
                    .then(CommandManager.literal("reset")
                        .executes(ctx -> {
                            ServerPlayerEntity p = ctx.getSource().getPlayer();
                            nika_nika_fruit_v7tnmscy.haki.HakiSystem.resetCooldowns(p);
                            ctx.getSource().sendFeedback(() -> Text.literal("Reset Haki cooldowns for " + p.getName().getString()), true);
                            return 1;
                        })
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                            .executes(ctx -> {
                                ServerPlayerEntity t = EntityArgumentType.getPlayer(ctx, "player");
                                nika_nika_fruit_v7tnmscy.haki.HakiSystem.resetCooldowns(t);
                                ctx.getSource().sendFeedback(() -> Text.literal("Reset Haki cooldowns for " + t.getName().getString()), true);
                                return 1;
                            })
                        )
                    )
                )
            );

            // Fruit cooldown reset command
            dispatcher.register(CommandManager.literal("fruit")
                .requires(src -> src.hasPermissionLevel(2))
                .then(CommandManager.literal("cooldown")
                    .then(CommandManager.literal("reset")
                        .executes(ctx -> {
                            ServerPlayerEntity p = ctx.getSource().getPlayer();
                            nika_nika_fruit_v7tnmscy.item.NikaNikaFruitItem.resetCooldowns(p);
                            ctx.getSource().sendFeedback(() -> Text.literal("Reset fruit cooldowns for " + p.getName().getString()), true);
                            return 1;
                        })
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                            .executes(ctx -> {
                                ServerPlayerEntity t = EntityArgumentType.getPlayer(ctx, "player");
                                nika_nika_fruit_v7tnmscy.item.NikaNikaFruitItem.resetCooldowns(t);
                                ctx.getSource().sendFeedback(() -> Text.literal("Reset fruit cooldowns for " + t.getName().getString()), true);
                                return 1;
                            })
                        )
                    )
                )
            );

            // Confirmation command for locked chest
            dispatcher.register(CommandManager.literal("lockedchest")
                .then(CommandManager.literal("confirm")
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        nika_nika_fruit_v7tnmscy.item.LockedDevilFruitChestItem.commandConfirm(player);
                        return 1;
                    })
                )
            );

            // Global fruit management command
            dispatcher.register(CommandManager.literal("fruit")
                .requires(src -> src.hasPermissionLevel(2))
                .then(CommandManager.literal("give")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .then(CommandManager.argument("fruit", com.mojang.brigadier.arguments.StringArgumentType.word()).suggests((ctx, b) -> {
                            b.suggest(nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_NIKA);
                            b.suggest(nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DARK);
                            b.suggest(nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_QUAKE);
                            b.suggest(nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DARKXQUAKE);
                            b.suggest(nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_SOUL);
                            b.suggest(nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_DRAGON);
                            b.suggest(nika_nika_fruit_v7tnmscy.DevilFruitRegistry.FRUIT_OPERATION);
                            return b.buildFuture();
                        })
                            .executes(ctx -> {
                                ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                String which = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "fruit").toLowerCase();
                                Item item = switch (which) {
                                    case DevilFruitRegistry.FRUIT_NIKA -> NikaNikaFruit_ITEM;
                                    case DevilFruitRegistry.FRUIT_DARK -> DARK_FRUIT_ITEM;
                                    case DevilFruitRegistry.FRUIT_QUAKE -> QUAKE_FRUIT_ITEM;
                                    case DevilFruitRegistry.FRUIT_DARKXQUAKE -> DARKXQUAKE_FRUIT_ITEM;
                                    case DevilFruitRegistry.FRUIT_SOUL -> SOUL_FRUIT_ITEM;
                                    case DevilFruitRegistry.FRUIT_DRAGON -> DRAGON_FRUIT_ITEM;
                                    case DevilFruitRegistry.FRUIT_OPERATION -> OPERATION_FRUIT_ITEM;
                                    default -> null;
                                };
                                if (item == null) {
                                    ctx.getSource().sendError(Text.literal("Unknown fruit: " + which + " (valid: nika, dark, quake, darkxquake, soul)"));
                                    return 0;
                                }
                                target.getInventory().insertStack(new net.minecraft.item.ItemStack(item));
                                ctx.getSource().sendFeedback(() -> Text.literal("Gave " + which + " fruit to " + target.getName().getString()), true);
                                return 1;
                            })
                        )
                    )
                )
                .then(CommandManager.literal("remove")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                            DevilFruitRegistry.clearFruit(target);
                            nika_nika_fruit_v7tnmscy.item.NikaNikaFruitItem.resetAllForPlayer(target);
                            target.sendMessage(Text.literal("§cYour Devil Fruit powers were removed."), false);
                            ctx.getSource().sendFeedback(() -> Text.literal("Cleared powers for " + target.getName().getString()), true);
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("brew")
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        player.getInventory().insertStack(new net.minecraft.item.ItemStack(MYSTERIOUS_BREW_ITEM));
                        ctx.getSource().sendFeedback(() -> Text.literal("Gave Mysterious Brew to " + player.getName().getString()), true);
                        return 1;
                    })
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                            target.getInventory().insertStack(new net.minecraft.item.ItemStack(MYSTERIOUS_BREW_ITEM));
                            ctx.getSource().sendFeedback(() -> Text.literal("Gave Mysterious Brew to " + target.getName().getString()), true);
                            return 1;
                        })
                    )
                )
            );

            // Legacy Nika commands (now per-fruit mastery, but aliases remain)
            dispatcher.register(CommandManager.literal("nika")
                .requires(src -> src.hasPermissionLevel(2))
                .then(CommandManager.literal("addexp")
                    .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                            NikaNikaFruitItem.addMasteryExp(player, amount); // applies to the player's current fruit
                            ctx.getSource().sendFeedback(() -> Text.literal("Added " + amount + " Mastery EXP to " + player.getName().getString()), true);
                            return 1;
                        })
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                            .executes(ctx -> {
                                ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                NikaNikaFruitItem.addMasteryExp(target, amount);
                                ctx.getSource().sendFeedback(() -> Text.literal("Added " + amount + " Mastery EXP to " + target.getName().getString()), true);
                                return 1;
                            })
                        )
                    )
                )
                .then(CommandManager.literal("fullmastery")
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        NikaNikaFruitItem.setMasteryLevel(player, 100);
                        ctx.getSource().sendFeedback(() -> Text.literal("Set full mastery (100) for " + player.getName().getString()), true);
                        return 1;
                    })
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                            NikaNikaFruitItem.setMasteryLevel(target, 100);
                            ctx.getSource().sendFeedback(() -> Text.literal("Set full mastery (100) for " + target.getName().getString()), true);
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("addskill")
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        NikaNikaFruitItem.setMasteryToNextUnlock(player);
                        ctx.getSource().sendFeedback(() -> Text.literal("Granted next skill unlock for " + player.getName().getString()), true);
                        return 1;
                    })
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                            NikaNikaFruitItem.setMasteryToNextUnlock(target);
                            ctx.getSource().sendFeedback(() -> Text.literal("Granted next skill unlock for " + target.getName().getString()), true);
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("allskills")
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        NikaNikaFruitItem.setMasteryLevel(player, 100);
                        ctx.getSource().sendFeedback(() -> Text.literal("Unlocked all skills for " + player.getName().getString()), true);
                        return 1;
                    })
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                            NikaNikaFruitItem.setMasteryLevel(target, 100);
                            ctx.getSource().sendFeedback(() -> Text.literal("Unlocked all skills for " + target.getName().getString()), true);
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("giveult")
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        NikaNikaFruitItem.setMasteryLevel(player, 100);
                        // Ensure damage gates are satisfied for immediate Awakening
                        NikaNikaFruitItem.addDamageTaken(player, 9999f);
                        NikaNikaFruitItem.addDamageDealt(player, 9999f);
                        NikaNikaFruitItem.attemptUltimateTransform(player);
                        ctx.getSource().sendFeedback(() -> Text.literal("Granted Nika Awakening to " + player.getName().getString()), true);
                        return 1;
                    })
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                            NikaNikaFruitItem.setMasteryLevel(target, 100);
                            // Ensure damage gates are satisfied for immediate Awakening
                            NikaNikaFruitItem.addDamageTaken(target, 9999f);
                            NikaNikaFruitItem.addDamageDealt(target, 9999f);
                            NikaNikaFruitItem.attemptUltimateTransform(target);
                            ctx.getSource().sendFeedback(() -> Text.literal("Granted Nika Awakening to " + target.getName().getString()), true);
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("reset")
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        NikaNikaFruitItem.resetCooldowns(player);
                        ctx.getSource().sendFeedback(() -> Text.literal("Reset Nika cooldowns for " + player.getName().getString()), true);
                        return 1;
                    })
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                            NikaNikaFruitItem.resetCooldowns(target);
                            ctx.getSource().sendFeedback(() -> Text.literal("Reset Nika cooldowns for " + target.getName().getString()), true);
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("skillall")
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        NikaNikaFruitItem.setMasteryLevel(player, 100);
                        ctx.getSource().sendFeedback(() -> Text.literal("Unlocked all skills for " + player.getName().getString()), true);
                        return 1;
                    })
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                            NikaNikaFruitItem.setMasteryLevel(target, 100);
                            ctx.getSource().sendFeedback(() -> Text.literal("Unlocked all skills for " + target.getName().getString()), true);
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("skills")
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        NikaNikaFruitItem.setMasteryLevel(player, 100);
                        ctx.getSource().sendFeedback(() -> Text.literal("Unlocked all skills for " + player.getName().getString()), true);
                        return 1;
                    })
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                            NikaNikaFruitItem.setMasteryLevel(target, 100);
                            ctx.getSource().sendFeedback(() -> Text.literal("Unlocked all skills for " + target.getName().getString()), true);
                            return 1;
                        })
                    )
                )
            );
            // Global mastery level grant command (applies to target's current fruit)
            dispatcher.register(CommandManager.literal("givelevel")
                .requires(src -> src.hasPermissionLevel(2))
                .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                    .executes(ctx -> { var p = ctx.getSource().getPlayer(); int amt = IntegerArgumentType.getInteger(ctx, "amount"); NikaNikaFruitItem.setMasteryLevel(p, Math.min(1000, NikaNikaFruitItem.getMasteryLevel(p) + amt)); ctx.getSource().sendFeedback(() -> Text.literal("Gave "+amt+" mastery levels to " + p.getName().getString()), true); return 1; })
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> { var t = EntityArgumentType.getPlayer(ctx, "player"); int amt = IntegerArgumentType.getInteger(ctx, "amount"); NikaNikaFruitItem.setMasteryLevel(t, Math.min(1000, NikaNikaFruitItem.getMasteryLevel(t) + amt)); ctx.getSource().sendFeedback(() -> Text.literal("Gave "+amt+" mastery levels to " + t.getName().getString()), true); return 1; })
                    )
                )
            );
            // Mirror commands for other fruits (currently use the same mastery system)
            dispatcher.register(CommandManager.literal("dark")
                .requires(src -> src.hasPermissionLevel(2))
                .then(CommandManager.literal("addexp")
                    .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            NikaNikaFruitItem.addMasteryExp(player, IntegerArgumentType.getInteger(ctx, "amount"));
                            ctx.getSource().sendFeedback(() -> Text.literal("Added EXP (Dark) to " + player.getName().getString()), true);
                            return 1;
                        })
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                            .executes(ctx -> {
                                ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                NikaNikaFruitItem.addMasteryExp(target, IntegerArgumentType.getInteger(ctx, "amount"));
                                ctx.getSource().sendFeedback(() -> Text.literal("Added EXP (Dark) to " + target.getName().getString()), true);
                                return 1;
                            })
                        )
                    )
                )
                .then(CommandManager.literal("fullmastery")
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        NikaNikaFruitItem.setMasteryLevel(player, 100);
                        ctx.getSource().sendFeedback(() -> Text.literal("Set full mastery (Dark) for " + player.getName().getString()), true);
                        return 1;
                    })
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                            NikaNikaFruitItem.setMasteryLevel(target, 100);
                            ctx.getSource().sendFeedback(() -> Text.literal("Set full mastery (Dark) for " + target.getName().getString()), true);
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("addskill")
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        NikaNikaFruitItem.setMasteryToNextUnlock(player);
                        ctx.getSource().sendFeedback(() -> Text.literal("Granted next skill unlock (Dark) for " + player.getName().getString()), true);
                        return 1;
                    })
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                            NikaNikaFruitItem.setMasteryToNextUnlock(target);
                            ctx.getSource().sendFeedback(() -> Text.literal("Granted next skill unlock (Dark) for " + target.getName().getString()), true);
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("allskills")
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        NikaNikaFruitItem.setMasteryLevel(player, 100);
                        ctx.getSource().sendFeedback(() -> Text.literal("Unlocked all skills (Dark) for " + player.getName().getString()), true);
                        return 1;
                    })
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                            NikaNikaFruitItem.setMasteryLevel(target, 100);
                            ctx.getSource().sendFeedback(() -> Text.literal("Unlocked all skills (Dark) for " + target.getName().getString()), true);
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("giveult")
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        ctx.getSource().sendFeedback(() -> Text.literal("Dark has no ultimate."), true);
                        return 1;
                    })
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                            ctx.getSource().sendFeedback(() -> Text.literal("Dark has no ultimate."), true);
                            return 1;
                        })
                    )
                )
            );

            dispatcher.register(CommandManager.literal("quake")
                .requires(src -> src.hasPermissionLevel(2))
                .then(CommandManager.literal("addexp")
                    .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            NikaNikaFruitItem.addMasteryExp(player, IntegerArgumentType.getInteger(ctx, "amount"));
                            ctx.getSource().sendFeedback(() -> Text.literal("Added EXP (Quake) to " + player.getName().getString()), true);
                            return 1;
                        })
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                            .executes(ctx -> {
                                ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                NikaNikaFruitItem.addMasteryExp(target, IntegerArgumentType.getInteger(ctx, "amount"));
                                ctx.getSource().sendFeedback(() -> Text.literal("Added EXP (Quake) to " + target.getName().getString()), true);
                                return 1;
                            })
                        )
                    )
                )
                .then(CommandManager.literal("fullmastery")
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        NikaNikaFruitItem.setMasteryLevel(player, 100);
                        ctx.getSource().sendFeedback(() -> Text.literal("Set full mastery (Quake) for " + player.getName().getString()), true);
                        return 1;
                    })
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                            NikaNikaFruitItem.setMasteryLevel(target, 100);
                            ctx.getSource().sendFeedback(() -> Text.literal("Set full mastery (Quake) for " + target.getName().getString()), true);
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("addskill")
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        NikaNikaFruitItem.setMasteryToNextUnlock(player);
                        ctx.getSource().sendFeedback(() -> Text.literal("Granted next skill unlock (Quake) for " + player.getName().getString()), true);
                        return 1;
                    })
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                            NikaNikaFruitItem.setMasteryToNextUnlock(target);
                            ctx.getSource().sendFeedback(() -> Text.literal("Granted next skill unlock (Quake) for " + target.getName().getString()), true);
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("allskills")
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        NikaNikaFruitItem.setMasteryLevel(player, 100);
                        ctx.getSource().sendFeedback(() -> Text.literal("Unlocked all skills (Quake) for " + player.getName().getString()), true);
                        return 1;
                    })
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                            NikaNikaFruitItem.setMasteryLevel(target, 100);
                            ctx.getSource().sendFeedback(() -> Text.literal("Unlocked all skills (Quake) for " + target.getName().getString()), true);
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("giveult")
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        NikaNikaFruitItem.setMasteryLevel(player, 100);
                        NikaNikaFruitItem.addDamageTaken(player, 9999f);
                        NikaNikaFruitItem.addDamageDealt(player, 9999f);
                        NikaNikaFruitItem.attemptUltimateTransform(player);
                        ctx.getSource().sendFeedback(() -> Text.literal("Granted Tremor Awakening to " + player.getName().getString()), true);
                        return 1;
                    })
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                            NikaNikaFruitItem.setMasteryLevel(target, 100);
                            NikaNikaFruitItem.addDamageTaken(target, 9999f);
                            NikaNikaFruitItem.addDamageDealt(target, 9999f);
                            NikaNikaFruitItem.attemptUltimateTransform(target);
                            ctx.getSource().sendFeedback(() -> Text.literal("Granted Tremor Awakening to " + target.getName().getString()), true);
                            return 1;
                        })
                    )
                )
            );

            dispatcher.register(CommandManager.literal("darkxquake")
                .requires(src -> src.hasPermissionLevel(2))
                .then(CommandManager.literal("addexp")
                    .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            NikaNikaFruitItem.addMasteryExp(player, IntegerArgumentType.getInteger(ctx, "amount"));
                            ctx.getSource().sendFeedback(() -> Text.literal("Added EXP (DarkxQuake) to " + player.getName().getString()), true);
                            return 1;
                        })
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                            .executes(ctx -> {
                                ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                NikaNikaFruitItem.addMasteryExp(target, IntegerArgumentType.getInteger(ctx, "amount"));
                                ctx.getSource().sendFeedback(() -> Text.literal("Added EXP (DarkxQuake) to " + target.getName().getString()), true);
                                return 1;
                            })
                        )
                    )
                )
                .then(CommandManager.literal("fullmastery")
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        NikaNikaFruitItem.setMasteryLevel(player, 100);
                        ctx.getSource().sendFeedback(() -> Text.literal("Set full mastery (DarkxQuake) for " + player.getName().getString()), true);
                        return 1;
                    })
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                            NikaNikaFruitItem.setMasteryLevel(target, 100);
                            ctx.getSource().sendFeedback(() -> Text.literal("Set full mastery (DarkxQuake) for " + target.getName().getString()), true);
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("addskill")
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        NikaNikaFruitItem.setMasteryToNextUnlock(player);
                        ctx.getSource().sendFeedback(() -> Text.literal("Granted next skill unlock (DarkxQuake) for " + player.getName().getString()), true);
                        return 1;
                    })
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                            NikaNikaFruitItem.setMasteryToNextUnlock(target);
                            ctx.getSource().sendFeedback(() -> Text.literal("Granted next skill unlock (DarkxQuake) for " + target.getName().getString()), true);
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("allskills")
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        NikaNikaFruitItem.setMasteryLevel(player, 100);
                        ctx.getSource().sendFeedback(() -> Text.literal("Unlocked all skills (DarkxQuake) for " + player.getName().getString()), true);
                        return 1;
                    })
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                            NikaNikaFruitItem.setMasteryLevel(target, 100);
                            ctx.getSource().sendFeedback(() -> Text.literal("Unlocked all skills (DarkxQuake) for " + target.getName().getString()), true);
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("giveult")
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        NikaNikaFruitItem.setMasteryLevel(player, 100);
                        // Grant awakening for DarkxQuake: mark Warden unlock and set mastery high
                        nika_nika_fruit_v7tnmscy.DevilFruitRegistry.unlockDarkxQuakeAwakening(player);
                        NikaNikaFruitItem.setMasteryLevel(player, 100);
                        NikaNikaFruitItem.attemptUltimateTransform(player);
                        ctx.getSource().sendFeedback(() -> Text.literal("Granted The True Menace to " + player.getName().getString()), true);
                        return 1;
                    })
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                            NikaNikaFruitItem.setMasteryLevel(target, 100);
                            nika_nika_fruit_v7tnmscy.DevilFruitRegistry.unlockDarkxQuakeAwakening(target);
                            NikaNikaFruitItem.setMasteryLevel(target, 100);
                            NikaNikaFruitItem.attemptUltimateTransform(target);
                            ctx.getSource().sendFeedback(() -> Text.literal("Granted The True Menace to " + target.getName().getString()), true);
                            return 1;
                        })
                    )
                )
            );

            // Soul fruit admin commands (mirror of other fruits)
            dispatcher.register(CommandManager.literal("soul")
                .requires(src -> src.hasPermissionLevel(2))
                .then(CommandManager.literal("addexp")
                    .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            NikaNikaFruitItem.addMasteryExp(player, IntegerArgumentType.getInteger(ctx, "amount"));
                            ctx.getSource().sendFeedback(() -> Text.literal("Added EXP (Soul) to " + player.getName().getString()), true);
                            return 1;
                        })
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                            .executes(ctx -> {
                                ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                NikaNikaFruitItem.addMasteryExp(target, IntegerArgumentType.getInteger(ctx, "amount"));
                                ctx.getSource().sendFeedback(() -> Text.literal("Added EXP (Soul) to " + target.getName().getString()), true);
                                return 1;
                            })
                        )
                    )
                )
                .then(CommandManager.literal("fullmastery")
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        NikaNikaFruitItem.setMasteryLevel(player, 200);
                        ctx.getSource().sendFeedback(() -> Text.literal("Set full mastery (Soul) for " + player.getName().getString()), true);
                        return 1;
                    })
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                            NikaNikaFruitItem.setMasteryLevel(target, 200);
                            ctx.getSource().sendFeedback(() -> Text.literal("Set full mastery (Soul) for " + target.getName().getString()), true);
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("addskill")
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        NikaNikaFruitItem.setMasteryToNextUnlock(player);
                        ctx.getSource().sendFeedback(() -> Text.literal("Granted next skill unlock (Soul) for " + player.getName().getString()), true);
                        return 1;
                    })
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                            NikaNikaFruitItem.setMasteryToNextUnlock(target);
                            ctx.getSource().sendFeedback(() -> Text.literal("Granted next skill unlock (Soul) for " + target.getName().getString()), true);
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("allskills")
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        NikaNikaFruitItem.setMasteryLevel(player, 200);
                        ctx.getSource().sendFeedback(() -> Text.literal("Unlocked all skills (Soul) for " + player.getName().getString()), true);
                        return 1;
                    })
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                            NikaNikaFruitItem.setMasteryLevel(target, 200);
                            ctx.getSource().sendFeedback(() -> Text.literal("Unlocked all skills (Soul) for " + target.getName().getString()), true);
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("skillall")
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        NikaNikaFruitItem.setMasteryLevel(player, 200);
                        ctx.getSource().sendFeedback(() -> Text.literal("Unlocked all skills (Soul) for " + player.getName().getString()), true);
                        return 1;
                    })
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                            NikaNikaFruitItem.setMasteryLevel(target, 200);
                            ctx.getSource().sendFeedback(() -> Text.literal("Unlocked all skills (Soul) for " + target.getName().getString()), true);
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("skills")
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        NikaNikaFruitItem.setMasteryLevel(player, 200);
                        ctx.getSource().sendFeedback(() -> Text.literal("Unlocked all skills (Soul) for " + player.getName().getString()), true);
                        return 1;
                    })
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                            NikaNikaFruitItem.setMasteryLevel(target, 200);
                            ctx.getSource().sendFeedback(() -> Text.literal("Unlocked all skills (Soul) for " + target.getName().getString()), true);
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("giveult")
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        // For Soul: ult is non-transform; unlocking by hitting mastery 200 gives full access
                        NikaNikaFruitItem.setMasteryLevel(player, 200);
                        ctx.getSource().sendFeedback(() -> Text.literal("Granted Soul ultimate access to " + player.getName().getString()), true);
                        return 1;
                    })
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                            NikaNikaFruitItem.setMasteryLevel(target, 200);
                            ctx.getSource().sendFeedback(() -> Text.literal("Granted Soul ultimate access to " + target.getName().getString()), true);
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("reset")
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        NikaNikaFruitItem.resetCooldowns(player);
                        ctx.getSource().sendFeedback(() -> Text.literal("Reset Soul cooldowns for " + player.getName().getString()), true);
                        return 1;
                    })
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                            NikaNikaFruitItem.resetCooldowns(target);
                            ctx.getSource().sendFeedback(() -> Text.literal("Reset Soul cooldowns for " + target.getName().getString()), true);
                            return 1;
                        })
                    )
                )
            );

            // Dragon fruit admin commands
            dispatcher.register(CommandManager.literal("dragon")
                .requires(src -> src.hasPermissionLevel(2))
                .then(CommandManager.literal("addexp")
                    .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                        .executes(ctx -> { var p = ctx.getSource().getPlayer(); NikaNikaFruitItem.addMasteryExp(p, IntegerArgumentType.getInteger(ctx, "amount")); ctx.getSource().sendFeedback(() -> Text.literal("Added EXP (Dragon) to " + p.getName().getString()), true); return 1; })
                        .then(CommandManager.argument("player", EntityArgumentType.player()).executes(ctx -> { var t = EntityArgumentType.getPlayer(ctx, "player"); NikaNikaFruitItem.addMasteryExp(t, IntegerArgumentType.getInteger(ctx, "amount")); ctx.getSource().sendFeedback(() -> Text.literal("Added EXP (Dragon) to " + t.getName().getString()), true); return 1; }))
                ))
                .then(CommandManager.literal("fullmastery").executes(ctx -> { var p = ctx.getSource().getPlayer(); NikaNikaFruitItem.setMasteryLevel(p, 300); return 1; })
                    .then(CommandManager.argument("player", EntityArgumentType.player()).executes(ctx -> { var t = EntityArgumentType.getPlayer(ctx, "player"); NikaNikaFruitItem.setMasteryLevel(t, 300); return 1; })))
                .then(CommandManager.literal("addskill")
                    .executes(ctx -> { var p = ctx.getSource().getPlayer(); NikaNikaFruitItem.setMasteryToNextUnlock(p); ctx.getSource().sendFeedback(() -> Text.literal("Granted next skill unlock (Dragon) for " + p.getName().getString()), true); return 1; })
                    .then(CommandManager.argument("player", EntityArgumentType.player()).executes(ctx -> { var t = EntityArgumentType.getPlayer(ctx, "player"); NikaNikaFruitItem.setMasteryToNextUnlock(t); ctx.getSource().sendFeedback(() -> Text.literal("Granted next skill unlock (Dragon) for " + t.getName().getString()), true); return 1; })))
                .then(CommandManager.literal("allskills").executes(ctx -> { var p = ctx.getSource().getPlayer(); NikaNikaFruitItem.setMasteryLevel(p, 300); ctx.getSource().sendFeedback(() -> Text.literal("Unlocked all skills (Dragon) for " + p.getName().getString()), true); return 1; })
                    .then(CommandManager.argument("player", EntityArgumentType.player()).executes(ctx -> { var t = EntityArgumentType.getPlayer(ctx, "player"); NikaNikaFruitItem.setMasteryLevel(t, 300); ctx.getSource().sendFeedback(() -> Text.literal("Unlocked all skills (Dragon) for " + t.getName().getString()), true); return 1; })))
                .then(CommandManager.literal("skills").executes(ctx -> { var p = ctx.getSource().getPlayer(); NikaNikaFruitItem.setMasteryLevel(p, 300); ctx.getSource().sendFeedback(() -> Text.literal("Unlocked all skills (Dragon) for " + p.getName().getString()), true); return 1; })
                    .then(CommandManager.argument("player", EntityArgumentType.player()).executes(ctx -> { var t = EntityArgumentType.getPlayer(ctx, "player"); NikaNikaFruitItem.setMasteryLevel(t, 300); ctx.getSource().sendFeedback(() -> Text.literal("Unlocked all skills (Dragon) for " + t.getName().getString()), true); return 1; })))
                .then(CommandManager.literal("giveult")
                    .executes(ctx -> { var p = ctx.getSource().getPlayer(); NikaNikaFruitItem.setMasteryLevel(p, 250); NikaNikaFruitItem.attemptUltimateTransform(p); ctx.getSource().sendFeedback(() -> Text.literal("Granted Dragon Fury to " + p.getName().getString()), true); return 1; })
                    .then(CommandManager.argument("player", EntityArgumentType.player()).executes(ctx -> { var t = EntityArgumentType.getPlayer(ctx, "player"); NikaNikaFruitItem.setMasteryLevel(t, 250); NikaNikaFruitItem.attemptUltimateTransform(t); ctx.getSource().sendFeedback(() -> Text.literal("Granted Dragon Fury to " + t.getName().getString()), true); return 1; })))
                .then(CommandManager.literal("give")
                    .then(CommandManager.literal("level")
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                            .executes(ctx -> { var p = ctx.getSource().getPlayer(); int amt = IntegerArgumentType.getInteger(ctx, "amount"); NikaNikaFruitItem.setMasteryLevel(p, Math.min(1000, NikaNikaFruitItem.getMasteryLevel(p) + amt)); ctx.getSource().sendFeedback(() -> Text.literal("Gave "+amt+" mastery levels (Dragon) to " + p.getName().getString()), true); return 1; })
                            .then(CommandManager.argument("player", EntityArgumentType.player()).executes(ctx -> { var t = EntityArgumentType.getPlayer(ctx, "player"); int amt = IntegerArgumentType.getInteger(ctx, "amount"); NikaNikaFruitItem.setMasteryLevel(t, Math.min(1000, NikaNikaFruitItem.getMasteryLevel(t) + amt)); ctx.getSource().sendFeedback(() -> Text.literal("Gave "+amt+" mastery levels (Dragon) to " + t.getName().getString()), true); return 1; }))
                        )
                    )
                )
                .then(CommandManager.literal("reset").executes(ctx -> { var p = ctx.getSource().getPlayer(); NikaNikaFruitItem.resetCooldowns(p); return 1; })
                    .then(CommandManager.argument("player", EntityArgumentType.player()).executes(ctx -> { var t = EntityArgumentType.getPlayer(ctx, "player"); NikaNikaFruitItem.resetCooldowns(t); return 1; })))
            );

            // Operation fruit admin commands
            dispatcher.register(CommandManager.literal("operation")
                .requires(src -> src.hasPermissionLevel(2))
                .then(CommandManager.literal("addexp")
                    .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                        .executes(ctx -> { var p = ctx.getSource().getPlayer(); NikaNikaFruitItem.addMasteryExp(p, IntegerArgumentType.getInteger(ctx, "amount")); ctx.getSource().sendFeedback(() -> Text.literal("Added EXP (Operation) to " + p.getName().getString()), true); return 1; })
                        .then(CommandManager.argument("player", EntityArgumentType.player()).executes(ctx -> { var t = EntityArgumentType.getPlayer(ctx, "player"); NikaNikaFruitItem.addMasteryExp(t, IntegerArgumentType.getInteger(ctx, "amount")); ctx.getSource().sendFeedback(() -> Text.literal("Added EXP (Operation) to " + t.getName().getString()), true); return 1; }))
                ))
                .then(CommandManager.literal("fullmastery").executes(ctx -> { var p = ctx.getSource().getPlayer(); NikaNikaFruitItem.setMasteryLevel(p, 300); return 1; })
                    .then(CommandManager.argument("player", EntityArgumentType.player()).executes(ctx -> { var t = EntityArgumentType.getPlayer(ctx, "player"); NikaNikaFruitItem.setMasteryLevel(t, 300); return 1; })))
                .then(CommandManager.literal("addskill")
                    .executes(ctx -> { var p = ctx.getSource().getPlayer(); NikaNikaFruitItem.setMasteryToNextUnlock(p); ctx.getSource().sendFeedback(() -> Text.literal("Granted next skill unlock (Operation) for " + p.getName().getString()), true); return 1; })
                    .then(CommandManager.argument("player", EntityArgumentType.player()).executes(ctx -> { var t = EntityArgumentType.getPlayer(ctx, "player"); NikaNikaFruitItem.setMasteryToNextUnlock(t); ctx.getSource().sendFeedback(() -> Text.literal("Granted next skill unlock (Operation) for " + t.getName().getString()), true); return 1; })))
                .then(CommandManager.literal("allskills").executes(ctx -> { var p = ctx.getSource().getPlayer(); NikaNikaFruitItem.setMasteryLevel(p, 300); ctx.getSource().sendFeedback(() -> Text.literal("Unlocked all skills (Operation) for " + p.getName().getString()), true); return 1; })
                    .then(CommandManager.argument("player", EntityArgumentType.player()).executes(ctx -> { var t = EntityArgumentType.getPlayer(ctx, "player"); NikaNikaFruitItem.setMasteryLevel(t, 300); ctx.getSource().sendFeedback(() -> Text.literal("Unlocked all skills (Operation) for " + t.getName().getString()), true); return 1; })))
                .then(CommandManager.literal("skills").executes(ctx -> { var p = ctx.getSource().getPlayer(); NikaNikaFruitItem.setMasteryLevel(p, 300); ctx.getSource().sendFeedback(() -> Text.literal("Unlocked all skills (Operation) for " + p.getName().getString()), true); return 1; })
                    .then(CommandManager.argument("player", EntityArgumentType.player()).executes(ctx -> { var t = EntityArgumentType.getPlayer(ctx, "player"); NikaNikaFruitItem.setMasteryLevel(t, 300); ctx.getSource().sendFeedback(() -> Text.literal("Unlocked all skills (Operation) for " + t.getName().getString()), true); return 1; })))
                .then(CommandManager.literal("giveult")
                    .executes(ctx -> { var p = ctx.getSource().getPlayer(); ctx.getSource().sendFeedback(() -> Text.literal("Operation has no ultimate (no transformation)."), true); return 1; })
                    .then(CommandManager.argument("player", EntityArgumentType.player()).executes(ctx -> { var t = EntityArgumentType.getPlayer(ctx, "player"); ctx.getSource().sendFeedback(() -> Text.literal("Operation has no ultimate (no transformation)."), true); return 1; })))
                .then(CommandManager.literal("give")
                    .then(CommandManager.literal("level")
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                            .executes(ctx -> { var p = ctx.getSource().getPlayer(); int amt = IntegerArgumentType.getInteger(ctx, "amount"); NikaNikaFruitItem.setMasteryLevel(p, Math.min(1000, NikaNikaFruitItem.getMasteryLevel(p) + amt)); ctx.getSource().sendFeedback(() -> Text.literal("Gave "+amt+" mastery levels (Operation) to " + p.getName().getString()), true); return 1; })
                            .then(CommandManager.argument("player", EntityArgumentType.player()).executes(ctx -> { var t = EntityArgumentType.getPlayer(ctx, "player"); int amt = IntegerArgumentType.getInteger(ctx, "amount"); NikaNikaFruitItem.setMasteryLevel(t, Math.min(1000, NikaNikaFruitItem.getMasteryLevel(t) + amt)); ctx.getSource().sendFeedback(() -> Text.literal("Gave "+amt+" mastery levels (Operation) to " + t.getName().getString()), true); return 1; }))
                        )
                    )
                )
                .then(CommandManager.literal("reset").executes(ctx -> { var p = ctx.getSource().getPlayer(); NikaNikaFruitItem.resetCooldowns(p); return 1; })
                    .then(CommandManager.argument("player", EntityArgumentType.player()).executes(ctx -> { var t = EntityArgumentType.getPlayer(ctx, "player"); NikaNikaFruitItem.resetCooldowns(t); return 1; })))
            );
        });

        // Restore inventory for players whose soul was stolen (on respawn)
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            var saved = nika_nika_fruit_v7tnmscy.item.NikaNikaFruitItem.SOUL_SAVED_INVENTORY.remove(newPlayer.getUuid());
            if (saved != null) {
                for (net.minecraft.item.ItemStack st : saved) {
                    if (!st.isEmpty()) {
                        boolean ok = newPlayer.getInventory().insertStack(st.copy());
                        if (!ok) {
                            newPlayer.dropItem(st.copy(), true, false);
                        }
                    }
                }
                newPlayer.sendMessage(net.minecraft.text.Text.literal("§bYour soul was stolen — your items were preserved."), false);
            }
        });

    }
    
    private void registerNetworking() {
        // Register packet types
        PayloadTypeRegistry.playC2S().register(NetworkPackets.CycleMovePacket.ID, NetworkPackets.CycleMovePacket.CODEC);
        PayloadTypeRegistry.playC2S().register(NetworkPackets.UseMovePacket.ID, NetworkPackets.UseMovePacket.CODEC);
        PayloadTypeRegistry.playC2S().register(NetworkPackets.UltimateTransformPacket.ID, NetworkPackets.UltimateTransformPacket.CODEC);
        // PayloadTypeRegistry.playC2S().register(NetworkPackets.ShowInfoPacket.ID, NetworkPackets.ShowInfoPacket.CODEC); // Disabled: replaced Y feature with O (client-only overlay)
        PayloadTypeRegistry.playC2S().register(NetworkPackets.QuickUsePacket.ID, NetworkPackets.QuickUsePacket.CODEC);
        // Haki
        PayloadTypeRegistry.playC2S().register(NetworkPackets.HakiCyclePacket.ID, NetworkPackets.HakiCyclePacket.CODEC);
        PayloadTypeRegistry.playC2S().register(NetworkPackets.HakiCycleReversePacket.ID, NetworkPackets.HakiCycleReversePacket.CODEC);
        PayloadTypeRegistry.playC2S().register(NetworkPackets.HakiSelectTypePacket.ID, NetworkPackets.HakiSelectTypePacket.CODEC);
        PayloadTypeRegistry.playC2S().register(NetworkPackets.HakiSelectTypeReversePacket.ID, NetworkPackets.HakiSelectTypeReversePacket.CODEC);
        PayloadTypeRegistry.playC2S().register(NetworkPackets.HakiUsePacket.ID, NetworkPackets.HakiUsePacket.CODEC);
        PayloadTypeRegistry.playC2S().register(NetworkPackets.HakiShowMasteryPacket.ID, NetworkPackets.HakiShowMasteryPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(NetworkPackets.HakiShowInfoPacket.ID, NetworkPackets.HakiShowInfoPacket.CODEC);
        
        // Register packet handlers
        ServerPlayNetworking.registerGlobalReceiver(NetworkPackets.CycleMovePacket.ID, (payload, context) -> {
            context.server().execute(() -> {
                if (context.player() != null) {
                    NikaNikaFruitItem.cycleMove(context.player());
                }
            });
        });
        
        ServerPlayNetworking.registerGlobalReceiver(NetworkPackets.UseMovePacket.ID, (payload, context) -> {
            context.server().execute(() -> {
                if (context.player() != null) {
                    NikaNikaFruitItem.useCurrentMove(context.player());
                }
            });
        });
        
        ServerPlayNetworking.registerGlobalReceiver(NetworkPackets.UltimateTransformPacket.ID, (payload, context) -> {
            context.server().execute(() -> {
                if (context.player() != null) {
                    NikaNikaFruitItem.attemptUltimateTransform(context.player());
                }
            });
        });

        // Haki handlers
        ServerPlayNetworking.registerGlobalReceiver(NetworkPackets.HakiCyclePacket.ID, (payload, context) -> {
            context.server().execute(() -> { if (context.player() != null) nika_nika_fruit_v7tnmscy.haki.HakiSystem.cycle(context.player()); });
        });
        ServerPlayNetworking.registerGlobalReceiver(NetworkPackets.HakiCycleReversePacket.ID, (payload, context) -> {
            context.server().execute(() -> { if (context.player() != null) nika_nika_fruit_v7tnmscy.haki.HakiSystem.cycleReverse(context.player()); });
        });
        ServerPlayNetworking.registerGlobalReceiver(NetworkPackets.HakiSelectTypePacket.ID, (payload, context) -> {
            context.server().execute(() -> { if (context.player() != null) nika_nika_fruit_v7tnmscy.haki.HakiSystem.selectTypeNext(context.player()); });
        });
        ServerPlayNetworking.registerGlobalReceiver(NetworkPackets.HakiSelectTypeReversePacket.ID, (payload, context) -> {
            context.server().execute(() -> { if (context.player() != null) nika_nika_fruit_v7tnmscy.haki.HakiSystem.selectTypePrev(context.player()); });
        });
        ServerPlayNetworking.registerGlobalReceiver(NetworkPackets.HakiUsePacket.ID, (payload, context) -> {
            context.server().execute(() -> { if (context.player() != null) nika_nika_fruit_v7tnmscy.haki.HakiSystem.use(context.player()); });
        });
        ServerPlayNetworking.registerGlobalReceiver(NetworkPackets.HakiShowMasteryPacket.ID, (payload, context) -> {
            context.server().execute(() -> { if (context.player() != null) nika_nika_fruit_v7tnmscy.haki.HakiSystem.showMastery(context.player()); });
        });
        ServerPlayNetworking.registerGlobalReceiver(NetworkPackets.HakiShowInfoPacket.ID, (payload, context) -> {
            context.server().execute(() -> { if (context.player() != null) nika_nika_fruit_v7tnmscy.haki.HakiSystem.showInfo(context.player()); });
        });
        
        ServerPlayNetworking.registerGlobalReceiver(NetworkPackets.QuickUsePacket.ID, (payload, context) -> {
            context.server().execute(() -> {
                if (context.player() != null) {
                    NikaNikaFruitItem.quickUse(context.player());
                }
            });
        });


    }

    private static <T extends Item> T registerItem(RegistryKey<Item> key, Function<Item.Settings, T> factory, Item.Settings settings) {
        Item.Settings idSettings = settings.registryKey(key);
        T item = factory.apply(idSettings);
        return Registry.register(Registries.ITEM, key, item);
    }
}