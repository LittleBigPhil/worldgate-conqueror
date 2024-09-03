package worldgate.conqueror.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import worldgate.conqueror.WorldgateConqueror;
import worldgate.conqueror.block.ModBlocks;

public class ModMaterialRules {

    public static final MapCodec<MaterialRules.MaterialRule> BEDROCK_CEILING = register("bedrock_ceiling", MapCodec.unit(() -> {
        return MaterialRules.condition(
                MaterialRules.not(MaterialRules.verticalGradient(
                        "minecraft:bedrock_roof",
                        YOffset.belowTop(5),
                        YOffset.belowTop(0)
                )),
                MaterialRules.block(Blocks.BEDROCK.getDefaultState())
        );
    }));
    public static final MapCodec<MaterialRules.MaterialRule> BEDROCK_FLOOR = register("bedrock_floor", MapCodec.unit(() -> {
        return MaterialRules.condition(
                MaterialRules.verticalGradient(
                        "minecraft:bedrock_floor",
                        YOffset.aboveBottom(0),
                        YOffset.aboveBottom(5)
                ),
                MaterialRules.block(Blocks.BEDROCK.getDefaultState())
        );
    }));

    public static final MapCodec<MaterialRules.MaterialRule> GRASS_SURFACE = register("grass_surface", MapCodec.unit(() -> {
        return MaterialRules.condition(
                MaterialRules.stoneDepth(0, false, 0, net.minecraft.util.math.VerticalSurfaceType.FLOOR),
                MaterialRules.condition(
                        MaterialRules.water(0, 0),
                        MaterialRules.block(ModBlocks.GRASS_BLOCK.getDefaultState())
                )
        );
    }));
    public static final MapCodec<MaterialRules.MaterialRule> DIRT_SURFACE = register("dirt_surface", MapCodec.unit(() -> {
        return MaterialRules.condition(
                MaterialRules.stoneDepth(2, false, 2, net.minecraft.util.math.VerticalSurfaceType.FLOOR),
                MaterialRules.block(ModBlocks.SOIL.getDefaultState())
        );
    }));
    public static final MapCodec<MaterialRules.MaterialRule> SAND_SURFACE = register("sand_surface", MapCodec.unit(() -> {
        return MaterialRules.condition(
                MaterialRules.stoneDepth(2, false, 2, net.minecraft.util.math.VerticalSurfaceType.FLOOR),
                MaterialRules.block(ModBlocks.SAND.getDefaultState())
        );
    }));
    public static final MapCodec<MaterialRules.MaterialRule> RED_SAND_SURFACE = register("red_sand_surface", MapCodec.unit(() -> {
        return MaterialRules.condition(
                MaterialRules.stoneDepth(2, false, 2, net.minecraft.util.math.VerticalSurfaceType.FLOOR),
                MaterialRules.block(ModBlocks.RED_SAND.getDefaultState())
        );
    }));
    public static final MapCodec<MaterialRules.MaterialRule> SANDSTONE_SURFACE = register("sandstone_surface", MapCodec.unit(() -> {
        return MaterialRules.condition(
                MaterialRules.stoneDepth(6, false, 2, net.minecraft.util.math.VerticalSurfaceType.FLOOR),
                MaterialRules.block(ModBlocks.SANDSTONE.getDefaultState())
        );
    }));
    public static final MapCodec<MaterialRules.MaterialRule> RED_SANDSTONE_SURFACE = register("red_sandstone_surface", MapCodec.unit(() -> {
        return MaterialRules.condition(
                MaterialRules.stoneDepth(6, false, 2, net.minecraft.util.math.VerticalSurfaceType.FLOOR),
                MaterialRules.block(ModBlocks.RED_SANDSTONE.getDefaultState())
        );
    }));
    public static final MapCodec<MaterialRules.MaterialRule> BASALT_STONE = register("basalt_stone", MapCodec.unit(() -> {
        return MaterialRules.block(ModBlocks.BASALT.getDefaultState());
    }));


    public static final MapCodec<MaterialRules.MaterialRule> GRAVEL_SURFACE = register("gravel_surface", MapCodec.unit(() -> {
        return MaterialRules.condition(
                MaterialRules.stoneDepth(2, false, 2, net.minecraft.util.math.VerticalSurfaceType.FLOOR),
                MaterialRules.block(ModBlocks.GRAVEL.getDefaultState())
        );
    }));
    public static final MapCodec<MaterialRules.MaterialRule> GRAVEL_UNDERWATER = register("gravel_underwater", MapCodec.unit(() -> {
        return MaterialRules.condition(
                MaterialRules.stoneDepth(2, false, 2, net.minecraft.util.math.VerticalSurfaceType.FLOOR),
                MaterialRules.condition(
                        MaterialRules.not(MaterialRules.water(0, 0)),
                        MaterialRules.block(ModBlocks.GRAVEL.getDefaultState())
                )
        );
    }));

    public static final MapCodec<MaterialRules.MaterialRule> GRASS_AND_DIRT_SURFACE = register("grass_and_dirt_surface", MapCodec.unit(() -> {
        return MaterialRules.sequence(decode(GRASS_SURFACE), decode(DIRT_SURFACE));
    }));
    public static final MapCodec<MaterialRules.MaterialRule> GRAVEL_OR_DIRT_SURFACE = register("gravel_or_dirt_surface", MapCodec.unit(() -> {
        return MaterialRules.sequence(decode(GRAVEL_UNDERWATER), decode(GRASS_AND_DIRT_SURFACE));
    }));
    public static final MapCodec<MaterialRules.MaterialRule> SAND_AND_STONE_SURFACE = register("sand_and_stone_surface", MapCodec.unit(() -> {
        return MaterialRules.sequence(decode(SAND_SURFACE), decode(SANDSTONE_SURFACE));
    }));
    public static final MapCodec<MaterialRules.MaterialRule> RED_SAND_AND_STONE_SURFACE = register("red_sand_and_stone_surface", MapCodec.unit(() -> {
        return MaterialRules.sequence(decode(RED_SAND_SURFACE), decode(RED_SANDSTONE_SURFACE));
    }));
    public static final MapCodec<MaterialRules.MaterialRule> SAND_AND_BASALT = register("sand_and_basalt", MapCodec.unit(() -> {
        return MaterialRules.sequence(decode(SAND_AND_STONE_SURFACE), decode(BASALT_STONE));
    }));

    private static MapCodec<MaterialRules.MaterialRule> register(String id, MapCodec<MaterialRules.MaterialRule> codec) {
        return Registry.register(Registries.MATERIAL_RULE, Identifier.of("worldgate-conqueror", id), codec);
    }
    private static MaterialRules.MaterialRule decode(MapCodec<MaterialRules.MaterialRule> codec) {
        return codec.decode(null, null).result().orElseThrow();
    }

    public static void registerRules() {
        WorldgateConqueror.LOGGER.info("Registering material rules for {}", WorldgateConqueror.MOD_ID);
    }
}
