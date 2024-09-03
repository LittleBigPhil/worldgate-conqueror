package worldgate.conqueror.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import worldgate.conqueror.WorldgateConqueror;
import worldgate.conqueror.block.ModBlocks;
import worldgate.conqueror.item.component.ModComponents;
import worldgate.conqueror.item.component.ModularToolComponent;
import worldgate.conqueror.mechanic.DamageTypeDistribution;
import worldgate.conqueror.mechanic.ModEquipmentSlots;

import java.util.List;


public class ModItems {

    public static final Item BANDAGE = registerItem("bandage", new BandageItem( new Item.Settings() ) );
    public static final Item DOWSING_ROD = registerItem("dowsing_rod", new Item(new Item.Settings() ) );

    public static final Item TOOL_HEAD = registerItem("tool_head", new ModularToolHead(new Item.Settings()));
    public static final Item TOOL_HANDLE = registerItem("tool_handle", new ModularToolHandle(new Item.Settings()));
    public static final Item MODULAR_TOOL = registerItem("modular_tool", new ModularTool(new Item.Settings()
            .component(ModComponents.TOOL_COMPONENT, ModularToolComponent.defaultValue())
    ));

    public static final Item EDIBLE_SUGAR = registerItem("sugar", new FoodItem( new Item.Settings()
            .food(new FoodComponent.Builder().nutrition(1).saturationModifier(0f).snack().build())
    ));
    public static final Item FLAX = registerItem("flax", new Item( new Item.Settings() ) );

    public static final Item BUCKET = registerItem("bucket", new ChargedBucketItem(Fluids.EMPTY, new Item.Settings().maxCount(16)));
    public static final Item WATER_BUCKET = registerItem("water_bucket", new ChargedBucketItem(Fluids.WATER, new Item.Settings().recipeRemainder(BUCKET).maxCount(1)));

    public static final Item FLUFF = registerItem("fluff", new Item(new Item.Settings()) );
    public static final Item CATTAIL_SEED = registerItem("cattail_seed", new Item(new Item.Settings()) );

    public static final Item AGAVE_SEEDS = registerItem("agave_seeds", new AliasedBlockItem(ModBlocks.AGAVE_PLANT, new Item.Settings()));
    public static final Item AGAVE_LEAF = registerItem("agave_leaf", new Item(new Item.Settings()));
    public static final Item AGAVE_FRUIT = registerItem("agave_fruit", new FoodItem(new Item.Settings()
            .food(new FoodComponent.Builder().nutrition(4).saturationModifier(0f).build())
    ));
    public static final Item NEEDLE_AND_THREAD = registerItem("needle_and_thread", new Item(new Item.Settings()) );
    public static final Item QUILTED_LINEN = registerItem("quilted_linen", new BlockItem(ModBlocks.QUILTED_LINEN, new Item.Settings()) );

    public static final Item HUSK_HIDE = registerItem("husk_hide", new Item(new Item.Settings()) );
    public static final Item BOILED_RAWHIDE = registerItem("boiled_rawhide", new Item(new Item.Settings()) );

    public static final Item GAMBESON_JACKET = registerItem("gambeson_jacket",
            new CustomArmor.Builder(new Item.Settings().maxCount(1), EquipmentSlot.CHEST)
                    .withResist(DamageTypeDistribution.Armor.GAMBESON)
                    .withDamageImmunityTime(0.20)
                    .build()
    );
    public static final Item GAMBESON_TROUSERS = registerItem("gambeson_trousers",
            new CustomArmor.Builder(new Item.Settings().maxCount(1), EquipmentSlot.LEGS)
                    .withResist(DamageTypeDistribution.Armor.GAMBESON)
                    .withDamageImmunityTime(0.20)
                    .build()
    );
    public static final Item RAWHIDE_CUIRASS = registerItem("rawhide_cuirass",
            new CustomArmor.Builder(new Item.Settings().maxCount(1), EquipmentSlot.CHEST)
                    .withResist(DamageTypeDistribution.Armor.RAWHIDE)
                    .withHealth(2)
                    .build()
    );
    public static final Item RAWHIDE_GREAVES = registerItem("rawhide_greaves",
            new CustomArmor.Builder(new Item.Settings().maxCount(1), EquipmentSlot.LEGS)
                    .withResist(DamageTypeDistribution.Armor.RAWHIDE)
                    .withHealth(2)
                    .build()
    );
    public static final Item PADDED_RAWHIDE_CUIRASS = registerItem("padded_rawhide_cuirass",
            new CustomArmor.Builder(new Item.Settings().maxCount(1), EquipmentSlot.CHEST)
                    .withResist(DamageTypeDistribution.Armor.RAWHIDE.add(DamageTypeDistribution.Armor.GAMBESON))
                    .withHealth(2)
                    .withDamageImmunityTime(0.20)
                    .build()
    );
    public static final Item PADDED_RAWHIDE_GREAVES = registerItem("padded_rawhide_greaves",
            new CustomArmor.Builder(new Item.Settings().maxCount(1), EquipmentSlot.LEGS)
                    .withResist(DamageTypeDistribution.Armor.RAWHIDE.add(DamageTypeDistribution.Armor.GAMBESON))
                    .withHealth(2)
                    .withDamageImmunityTime(0.20)
                    .build()
    );


    public static final Item BOOTS_OF_SPEED = registerItem("boots_of_speed",
            new CustomArmor.Builder(new Item.Settings().maxCount(1), EquipmentSlot.FEET)
                    .withMoveSpeed(.33)
                    .withFood(4)
                    .build()
    );
    public static final Item BOOTS_OF_LEAPING = registerItem("boots_of_leaping",
            new CustomArmor.Builder(new Item.Settings().maxCount(1), EquipmentSlot.FEET)
                    .withJumpHeight(.75)
                    .withFood(4)
                    .build()
    );
    public static final Item FLIPPERS = registerItem("flippers",
            new CustomArmor.Builder(new Item.Settings().maxCount(1), EquipmentSlot.FEET)
                    .withWaterSpeed(2.50)
                    .withMoveSpeed(-.33)
                    .withFood(4)
                    .build()
    );

    public static final Item RING_OF_STRENGTH = registerItem("ring_of_strength",
            new CustomArmor.Builder(new Item.Settings().maxCount(1), ModEquipmentSlots.ACCESSORY)
                    .withName("ring_of_strength")
                    .withStrength(25)
                    .build()
    );public static final Item RING_OF_MIND = registerItem("ring_of_mind",
            new CustomArmor.Builder(new Item.Settings().maxCount(1), ModEquipmentSlots.ACCESSORY)
                    .withName("ring_of_mind")
                    .withMind(25)
                    .build()
    );
    public static final Item RING_OF_HARDINESS = registerItem("ring_of_hardiness",
            new CustomArmor.Builder(new Item.Settings().maxCount(1), ModEquipmentSlots.ACCESSORY)
                    .withName("ring_of_hardiness")
                    .withHardiness(25)
                    .build()
    );
    public static final Item RING_OF_NIGHT_VISION = registerItem("ring_of_night_vision",
            new CustomArmor.Builder(new Item.Settings().maxCount(1), ModEquipmentSlots.ACCESSORY)
                    .withName("ring_of_night_vision")
                    .withStatusEffect(StatusEffects.NIGHT_VISION)
                    .build()
    );

    public static final Item RAWHIDE_GLOVE = registerItem("rawhide_glove",
            new CustomArmor.Builder(new Item.Settings().maxCount(1), EquipmentSlot.OFFHAND)
                    .withName("rawhide_glove")
                    .withStrength(25)
                    .build()
    );

    public static final Item CHITIN = registerItem("chitin", new Item(new Item.Settings()));
    public static final Item DRIED_CHITIN = registerItem("dried_chitin", new Item(new Item.Settings()));
    public static final Item CHITIN_CUIRASS = registerItem("chitin_cuirass",
            new CustomArmor.Builder(new Item.Settings().maxCount(1), EquipmentSlot.CHEST)
                    .withResist(DamageTypeDistribution.Armor.CHITIN)
                    .withHealth(2)
                    .build()
    );
    public static final Item CHITIN_GREAVES = registerItem("chitin_greaves",
            new CustomArmor.Builder(new Item.Settings().maxCount(1), EquipmentSlot.CHEST)
                    .withResist(DamageTypeDistribution.Armor.CHITIN)
                    .withHealth(2)
                    .build()
    );
    public static final Item PADDED_CHITIN_CUIRASS = registerItem("padded_chitin_cuirass",
            new CustomArmor.Builder(new Item.Settings().maxCount(1), EquipmentSlot.CHEST)
                    .withResist(DamageTypeDistribution.Armor.CHITIN.add(DamageTypeDistribution.Armor.GAMBESON))
                    .withHealth(2)
                    .withDamageImmunityTime(0.20)
                    .build()
    );
    public static final Item PADDED_CHITIN_GREAVES = registerItem("padded_chitin_greaves",
            new CustomArmor.Builder(new Item.Settings().maxCount(1), EquipmentSlot.CHEST)
                    .withResist(DamageTypeDistribution.Armor.CHITIN.add(DamageTypeDistribution.Armor.GAMBESON))
                    .withHealth(2)
                    .withDamageImmunityTime(0.20)
                    .build()
    );

    public static final Item BREATH_WEAPON_ITEM = registerItem("flamethrower",
            new BreathWeaponItem(new Item.Settings().maxCount(1))
    );

    public static final Item SUGAR_CANE_ITEM = registerItem("sugar_cane", new BlockItem(ModBlocks.SUGAR_CANE, new Item.Settings()));
    public static final Item SHORT_GRASS_ITEM = registerItem("short_grass", new BlockItem(ModBlocks.SHORT_GRASS, new Item.Settings()));
    public static final Item TALL_GRASS_ITEM = registerItem("tall_grass", new BlockItem(ModBlocks.TALL_GRASS, new Item.Settings()));

    public static final Item GLOWING_MUSHROOM_ITEM = registerItem("glowing_mushroom", new BlockItem(ModBlocks.GLOWING_MUSHROOM, new Item.Settings()));

    public static final Item CAMPFIRE_ITEM = registerItem("campfire", new BlockItem(ModBlocks.CAMPFIRE, new Item.Settings().component(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT)));

    public static final Item LOG_ITEM = registerItem("log", new BlockItem(ModBlocks.LOG, new Item.Settings()));
    public static final Item SAPlING_ITEM = registerItem("sapling", new BlockItem(ModBlocks.SAPLING, new Item.Settings()));
    public static final Item LEAVES_ITEM = registerItem("leaves", new BlockItem(ModBlocks.LEAVES, new Item.Settings()));

    public static final Item GRAVEL_ITEM = registerItem("gravel", new BlockItem(ModBlocks.GRAVEL, new Item.Settings()));
    public static final Item SAND_ITEM = registerItem("sand", new BlockItem(ModBlocks.SAND, new Item.Settings()));
    public static final Item RED_SAND_ITEM = registerItem("red_sand", new BlockItem(ModBlocks.RED_SAND, new Item.Settings()));

    public static final Item SOIL_ITEM = registerItem("soil", new BlockItem(ModBlocks.SOIL, new Item.Settings()));
    public static final Item GRASS_BLOCK_ITEM = registerItem("grass_block", new BlockItem(ModBlocks.GRASS_BLOCK, new Item.Settings()));

    public static final Item LIMESTONE_ITEM = registerItem("limestone", new BlockItem(ModBlocks.LIMESTONE, new Item.Settings()));
    public static final Item GRANITE_ITEM = registerItem("granite", new BlockItem(ModBlocks.GRANITE, new Item.Settings()));
    public static final Item SANDSTONE_ITEM = registerItem("sandstone", new BlockItem(ModBlocks.SANDSTONE, new Item.Settings()));
    public static final Item RED_SANDSTONE_ITEM = registerItem("red_sandstone", new BlockItem(ModBlocks.RED_SANDSTONE, new Item.Settings()));
    public static final Item BASALT_ITEM = registerItem("basalt", new BlockItem(ModBlocks.BASALT, new Item.Settings()));

    private static Item registerItem(String name, Item item) {
        Identifier itemID = Identifier.of(WorldgateConqueror.MOD_ID, name);
        return Registry.register(Registries.ITEM, itemID, item);
    }

    public static void registerModItems() {
        WorldgateConqueror.LOGGER.info("Registering mod items for " + WorldgateConqueror.MOD_ID);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register((itemGroup) -> itemGroup.add(ModItems.BANDAGE));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
                .register((itemGroup) -> itemGroup.add(ModItems.DOWSING_ROD));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK)
                .register((itemGroup) -> itemGroup.add(ModItems.EDIBLE_SUGAR));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register((itemGroup) -> itemGroup.add(ModItems.FLAX));


        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
                .register((itemGroup) -> itemGroup.add(ModItems.BUCKET));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
                .register((itemGroup) -> itemGroup.add(ModItems.WATER_BUCKET));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register((itemGroup) -> itemGroup.add(ModItems.TOOL_HEAD));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register((itemGroup) -> itemGroup.add(ModItems.TOOL_HANDLE));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
                .register((itemGroup) -> itemGroup.add(ModItems.MODULAR_TOOL));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register((itemGroup) -> itemGroup.add(ModItems.FLUFF));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL)
                .register((itemGroup) -> itemGroup.add(ModItems.CATTAIL_SEED));


        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL)
                .register((itemGroup) -> itemGroup.add(ModItems.AGAVE_SEEDS));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL)
                .register((itemGroup) -> itemGroup.add(ModItems.AGAVE_LEAF));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL)
                .register((itemGroup) -> itemGroup.add(ModItems.AGAVE_FRUIT));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register((itemGroup) -> itemGroup.add(ModItems.NEEDLE_AND_THREAD));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register((itemGroup) -> itemGroup.add(ModItems.QUILTED_LINEN));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register((itemGroup) -> itemGroup.add(ModItems.HUSK_HIDE));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register((itemGroup) -> itemGroup.add(ModItems.BOILED_RAWHIDE));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register((itemGroup) -> itemGroup.add(ModItems.GAMBESON_JACKET));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register((itemGroup) -> itemGroup.add(ModItems.GAMBESON_TROUSERS));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register((itemGroup) -> itemGroup.add(ModItems.RAWHIDE_CUIRASS));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register((itemGroup) -> itemGroup.add(ModItems.RAWHIDE_GREAVES));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register((itemGroup) -> itemGroup.add(ModItems.PADDED_RAWHIDE_CUIRASS));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register((itemGroup) -> itemGroup.add(ModItems.PADDED_RAWHIDE_GREAVES));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register((itemGroup) -> itemGroup.add(ModItems.BREATH_WEAPON_ITEM));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register((itemGroup) -> itemGroup.add(ModItems.BOOTS_OF_SPEED));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register((itemGroup) -> itemGroup.add(ModItems.BOOTS_OF_LEAPING));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register((itemGroup) -> itemGroup.add(ModItems.FLIPPERS));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register((itemGroup) -> itemGroup.add(ModItems.RING_OF_STRENGTH));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register((itemGroup) -> itemGroup.add(ModItems.RING_OF_MIND));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register((itemGroup) -> itemGroup.add(ModItems.RING_OF_HARDINESS));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register((itemGroup) -> itemGroup.add(ModItems.RING_OF_NIGHT_VISION));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register((itemGroup) -> itemGroup.add(ModItems.RAWHIDE_GLOVE));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register((itemGroup) -> itemGroup.add(ModItems.CHITIN));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register((itemGroup) -> itemGroup.add(ModItems.DRIED_CHITIN));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register((itemGroup) -> itemGroup.add(ModItems.CHITIN_CUIRASS));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register((itemGroup) -> itemGroup.add(ModItems.CHITIN_GREAVES));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register((itemGroup) -> itemGroup.add(ModItems.PADDED_CHITIN_CUIRASS));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register((itemGroup) -> itemGroup.add(ModItems.PADDED_CHITIN_GREAVES));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL)
                .register((itemGroup) -> itemGroup.add(ModItems.SUGAR_CANE_ITEM));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL)
                .register((itemGroup) -> itemGroup.add(ModItems.TALL_GRASS_ITEM));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL)
                .register((itemGroup) -> itemGroup.add(ModItems.SHORT_GRASS_ITEM));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL)
                .register((itemGroup) -> itemGroup.add(ModItems.GLOWING_MUSHROOM_ITEM));


        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL)
                .register((itemGroup) -> itemGroup.add(ModItems.CAMPFIRE_ITEM));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL)
                .register((itemGroup) -> itemGroup.add(ModItems.LOG_ITEM));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL)
                .register((itemGroup) -> itemGroup.add(ModItems.SAPlING_ITEM));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL)
                .register((itemGroup) -> itemGroup.add(ModItems.LEAVES_ITEM));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL)
                .register((itemGroup) -> itemGroup.add(ModItems.SAND_ITEM));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL)
                .register((itemGroup) -> itemGroup.add(ModItems.RED_SAND_ITEM));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL)
                .register((itemGroup) -> itemGroup.add(ModItems.GRAVEL_ITEM));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL)
                .register((itemGroup) -> itemGroup.add(ModItems.SOIL_ITEM));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL)
                .register((itemGroup) -> itemGroup.add(ModItems.GRASS_BLOCK_ITEM));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL)
                .register((itemGroup) -> itemGroup.add(ModItems.LIMESTONE_ITEM));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL)
                .register((itemGroup) -> itemGroup.add(ModItems.GRANITE_ITEM));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL)
                .register((itemGroup) -> itemGroup.add(ModItems.SANDSTONE_ITEM));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL)
                .register((itemGroup) -> itemGroup.add(ModItems.RED_SANDSTONE_ITEM));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL)
                .register((itemGroup) -> itemGroup.add(ModItems.BASALT_ITEM));

        for (String str : List.of(
                "flint_axe_attached", "flint_axe_centered",
                "flint_sword_attached", "flint_sword_centered",
                "flint_spear_attached", "flint_spear_centered",
                "flint_hammer_attached", "flint_hammer_centered",
                "flint_pick_attached", "flint_pick_centered",
                "flint_shovel_attached", "flint_shovel_centered",
                "flint_scythe_attached", "flint_scythe_centered",
                "grip", "pole", "handle"
        )) {
            registerItem(str, new Item(new Item.Settings()));
        }

        FuelRegistry.INSTANCE.remove(Items.CRAFTING_TABLE);
        FuelRegistry.INSTANCE.remove(Items.STICK);
        FuelRegistry.INSTANCE.remove(Items.LADDER);
        FuelRegistry.INSTANCE.remove(Items.WHITE_WOOL);
        FuelRegistry.INSTANCE.remove(Items.CHEST);
        FuelRegistry.INSTANCE.remove(Items.COMPOSTER);
        FuelRegistry.INSTANCE.remove(Items.OAK_PLANKS);
        FuelRegistry.INSTANCE.remove(Items.OAK_STAIRS);
        FuelRegistry.INSTANCE.remove(Items.OAK_SLAB);
        FuelRegistry.INSTANCE.remove(Items.OAK_DOOR);
        FuelRegistry.INSTANCE.remove(Items.OAK_TRAPDOOR);
        FuelRegistry.INSTANCE.remove(Items.OAK_FENCE);
        FuelRegistry.INSTANCE.remove(Items.OAK_FENCE_GATE);
        FuelRegistry.INSTANCE.remove(Items.OAK_SIGN);

        //ComposterBlock.registerDefaultCompostableItems();

    }
}
