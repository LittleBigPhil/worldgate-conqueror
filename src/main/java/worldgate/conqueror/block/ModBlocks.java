package worldgate.conqueror.block;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.*;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.ColorCode;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.FoliageColors;
import net.minecraft.world.biome.GrassColors;
import worldgate.conqueror.WorldgateConqueror;
import worldgate.conqueror.item.ModItems;

import java.util.Optional;

public class ModBlocks {
    // Remember to register the block's item

    public static class Strengths {
        public static float BY_HAND = .5f;
        public static float BY_TOOL = 2.5f;
        public static float OBNOXIOUS = 4.5f;
    }

    public static final Block LOG = registerBlock("log", new PillarBlock(AbstractBlock.Settings.copy(Blocks.OAK_LOG).requiresTool().strength(Strengths.BY_TOOL)));
    public static final Block LEAVES = registerBlock("leaves", new CustomLeavesBlock(AbstractBlock.Settings.copy(Blocks.OAK_LEAVES).strength(Strengths.BY_HAND).allowsSpawning((state, world, pos, type) -> true)));

    public static final Block SOIL = registerBlock("soil", new ColoredFallingBlock(new ColorCode(0x85621EFF), AbstractBlock.Settings.copy(Blocks.DIRT).requiresTool().strength(Strengths.BY_TOOL)));
    public static final Block GRASS_BLOCK = registerBlock("grass_block", new GrassBlock(AbstractBlock.Settings.copy(Blocks.GRASS_BLOCK).requiresTool().strength(Strengths.BY_TOOL)));

    public static final Block SAND = registerBlock("sand", new ColoredFallingBlock(new ColorCode(14406560), AbstractBlock.Settings.copy(Blocks.SAND).strength(Strengths.BY_HAND)));
    public static final Block RED_SAND = registerBlock("red_sand", new ColoredFallingBlock(new ColorCode(11098145), AbstractBlock.Settings.copy(Blocks.RED_SAND).strength(Strengths.BY_HAND)));
    public static final Block GRAVEL = registerBlock("gravel",  new ColoredFallingBlock(new ColorCode(-8356741), AbstractBlock.Settings.copy(Blocks.GRAVEL).strength(Strengths.BY_HAND)));

    public static final Block LIMESTONE = registerBlock("limestone", new Block(AbstractBlock.Settings.copy(Blocks.STONE).requiresTool().strength(Strengths.BY_TOOL)));
    public static final Block GRANITE = registerBlock("granite", new Block(AbstractBlock.Settings.copy(Blocks.GRANITE).requiresTool().strength(Strengths.OBNOXIOUS)));
    public static final Block RED_SANDSTONE = registerBlock("red_sandstone", new Block(AbstractBlock.Settings.copy(Blocks.RED_SANDSTONE).requiresTool().strength(Strengths.BY_TOOL)));
    public static final Block SANDSTONE = registerBlock("sandstone", new Block(AbstractBlock.Settings.copy(Blocks.SANDSTONE).requiresTool().strength(Strengths.BY_TOOL)));
    public static final Block BASALT = registerBlock("basalt", new Block(AbstractBlock.Settings.copy(Blocks.BASALT).requiresTool().strength(Strengths.BY_TOOL)));

    public static final Block QUILTED_LINEN = registerBlock("quilted_linen", new Block(AbstractBlock.Settings.copy(Blocks.WHITE_WOOL)));

    public static final SaplingGenerator TREE_GENERATOR = new SaplingGenerator(
            "tree",
            0.1F,
            Optional.empty(),
            Optional.empty(),
            Optional.of(RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE,Identifier.of(WorldgateConqueror.MOD_ID, "tree_short"))),
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
    );
    public static final Block SAPLING = registerBlock("sapling", new SaplingBlock(TREE_GENERATOR, AbstractBlock.Settings.copy(Blocks.OAK_SAPLING)));

    public static Block SUGAR_CANE = registerBlock("sugar_cane", new SugarCaneBlock(AbstractBlock.Settings.copy(Blocks.SUGAR_CANE).strength(Strengths.BY_HAND)));
    public static Block SHORT_GRASS = registerBlock("short_grass", new ShortPlantBlock(AbstractBlock.Settings.copy(Blocks.SHORT_GRASS).strength(Strengths.BY_HAND)));
    public static Block TALL_GRASS = registerBlock("tall_grass", new TallPlantBlock(AbstractBlock.Settings.copy(Blocks.TALL_GRASS).strength(Strengths.BY_HAND)));

    public static Block GLOWING_MUSHROOM = registerBlock("glowing_mushroom", new SimpleMushroom(AbstractBlock.Settings.copy(Blocks.BROWN_MUSHROOM).luminance(state -> 10)));

    public static final Block AGAVE_LEAF = registerBlock("agave_leaf", new AgaveLeafBlock(AbstractBlock.Settings.copy(Blocks.CACTUS).strength(Strengths.BY_TOOL).requiresTool().noCollision().nonOpaque()));
    public static final Block AGAVE_PLANT = registerBlock("agave_plant", new AgavePlant(AbstractBlock.Settings.copy(Blocks.WHEAT).strength(Strengths.BY_TOOL).requiresTool()));
    public static final Block AGAVE_FRUIT = registerBlock("agave_fruit", new AgaveFruitBlock(AbstractBlock.Settings.copy(Blocks.MELON).strength(Strengths.BY_HAND).noCollision().nonOpaque()));

    public static final Block CAMPFIRE = registerBlock("campfire", new EasyCampfireBlock(AbstractBlock.Settings.create()
            .mapColor(MapColor.SPRUCE_BROWN)
            .instrument(NoteBlockInstrument.BASS)
            .strength(Strengths.BY_HAND)
            .sounds(BlockSoundGroup.WOOD)
            //.luminance(createLightLevelFromLitBlockState(15))
            .luminance(state -> 15)
            .nonOpaque()
            .burnable()
            )
    );

    private static Block registerBlock(String name, Block block) {
        Identifier blockID = Identifier.of(WorldgateConqueror.MOD_ID, name);
        return Registry.register(Registries.BLOCK, blockID, block);
    }

    public static void registerModBlocks() {
        WorldgateConqueror.LOGGER.info("Registering mod blocks for " + WorldgateConqueror.MOD_ID);

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> BiomeColors.getFoliageColor(world, pos), ModBlocks.LEAVES);
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> FoliageColors.getDefaultColor(), ModItems.LEAVES_ITEM);

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> BiomeColors.getGrassColor(world, pos), ModBlocks.GRASS_BLOCK);
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> GrassColors.getDefaultColor(), ModItems.GRASS_BLOCK_ITEM);

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> BiomeColors.getGrassColor(world, pos), ModBlocks.SHORT_GRASS);
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> GrassColors.getDefaultColor(), ModItems.SHORT_GRASS_ITEM);
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> BiomeColors.getGrassColor(world, pos), ModBlocks.TALL_GRASS);
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> GrassColors.getDefaultColor(), ModItems.TALL_GRASS_ITEM);

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.SAPLING, RenderLayer.getCutoutMipped());//getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.LEAVES, RenderLayer.getCutoutMipped());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.GRASS_BLOCK, RenderLayer.getCutoutMipped());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.SUGAR_CANE, RenderLayer.getCutoutMipped());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.SHORT_GRASS, RenderLayer.getCutoutMipped());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.TALL_GRASS, RenderLayer.getCutoutMipped());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.GLOWING_MUSHROOM, RenderLayer.getCutoutMipped());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.CAMPFIRE, RenderLayer.getCutoutMipped());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.AGAVE_PLANT, RenderLayer.getCutoutMipped());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.AGAVE_LEAF, RenderLayer.getCutoutMipped());
        //BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.AGAVE_FRUIT, RenderLayer.getCutoutMipped());
    }
}
