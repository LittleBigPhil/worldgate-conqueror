package worldgate.conqueror.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import org.spongepowered.include.com.google.common.collect.ImmutableList;
import worldgate.conqueror.block.ModBlocks;
import worldgate.conqueror.util.VectorHelper;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

//see LakeFeature (deprecated)
public class PondFeature extends Feature<PondFeature.Config> {
    private static final BlockState AIR = Blocks.AIR.getDefaultState();
    private static final Vec3i BOUNDS = new Vec3i(16,8,16);
    private static final int HALF_HEIGHT = 4;
    private static final int MIN_BLOBS = 4;
    private static final int EXTRA_BLOBS = 4;
    private static final int MAX_HEIGHT_DIFFERENCE = 4;

    public PondFeature(Codec<PondFeature.Config> codec) {
        super(codec);
    }

    private static int offsetToIndex(Vec3i v) {
        return (v.getX() * BOUNDS.getZ() + v.getZ()) * BOUNDS.getY() + v.getY();
    }
    private static Stream<Vec3i> offsets() {
        return IntStream.range(0, BOUNDS.getX()).mapToObj(x -> {
            return IntStream.range(0, BOUNDS.getY()).mapToObj(y -> {
                return IntStream.range(0, BOUNDS.getZ()).mapToObj(z -> new Vec3i(x, BOUNDS.getY() - (y + 1),z));
            });
        }).flatMap(Function.identity()).flatMap(Function.identity());
    }
    private static Stream<Vec3i> innerOffsets() {
        return offsets().filter(v ->
                (v.getX() != 0) && (v.getX() != BOUNDS.getX() - 1)
                && (v.getY() != 0) && (v.getY() != BOUNDS.getY() - 1)
                && (v.getZ() != 0) && (v.getZ() != BOUNDS.getZ() - 1)
        );
    }
    private static Stream<Vec3i> adjacentOffsets(Vec3i centerOffset) {
        var adjacentIntegers = ImmutableList.of(-1,1);
        return adjacentIntegers.stream().flatMap(x -> {
            return adjacentIntegers.stream().flatMap(y -> {
                return adjacentIntegers.stream().map(z -> new Vec3i(x,y,z));
            });
        }).map(v -> v.add(centerOffset))
            .filter(PondFeature::withinBounds);
    }
    private static boolean withinBounds(Vec3i offset) {
        return (offset.getX() > 0) && (offset.getX() < BOUNDS.getX())
        && (offset.getY() > 0) && (offset.getY() < BOUNDS.getY())
        && (offset.getZ() > 0) && (offset.getZ() < BOUNDS.getZ());
    }

    private static BlockPos moveToSurface(StructureWorldAccess world, BlockPos pos) {
        int i = 0;
        if (world.getBlockState(pos).isAir() || !world.getFluidState(pos).isEmpty()) {
            BlockPos under = pos.down();
            while (world.getBlockState(under).isAir() || !world.getFluidState(under).isEmpty()) {
                pos = under;
                i++;
                if (i > MAX_HEIGHT_DIFFERENCE) {
                    break;
                }
                under = under.down();
            }
        } else {
            do {
                pos = pos.up();
                i++;
                if (i > MAX_HEIGHT_DIFFERENCE) {
                    break;
                }
            } while (!world.getBlockState(pos).isAir() && world.getFluidState(pos).isEmpty());
        }
        return pos;
    }

    @Override
    public boolean generate(FeatureContext<PondFeature.Config> context) {
        BlockPos origin = context.getOrigin();
        StructureWorldAccess world = context.getWorld();
        Random random = context.getRandom();
        PondFeature.Config config = context.getConfig();

        var offsetOrigin = offsetOrigin(world, origin);
        if (offsetOrigin.isEmpty()) {
            return false;
        } else {
            origin = offsetOrigin.get();
        }

        // Boolean array to track the fluid blocks within the feature volume
        boolean[] blocks = new boolean[BOUNDS.getX() * BOUNDS.getY() * BOUNDS.getZ()];

        // Generate random blobs of fluid within the feature volume.
        int blobCount = random.nextInt(EXTRA_BLOBS) + MIN_BLOBS;
        for (int i = 0; i < blobCount; i++) {
            generateBlob(blocks, random);
        }

        BlockState fluidState = config.fluid().get(random, origin);
        fillFluidBlocks(world, origin, blocks, fluidState);

        BlockState barrierState = config.barrier().get(random, origin);
        fillBarrierBlocks(world, origin, blocks, barrierState);

        return true;
    }

    private Optional<BlockPos> offsetOrigin(StructureWorldAccess world, BlockPos startingOrigin) {
        var surfaceCorners = new ImmutableList.Builder<BlockPos>().add(
                startingOrigin.add(BOUNDS.getX() - 1, 0, BOUNDS.getZ() - 1),
                startingOrigin.add(BOUNDS.getX() - 1, 0, 0),
                startingOrigin.add(0, 0, BOUNDS.getZ() - 1)
        ).add(startingOrigin).build().stream().map(corner -> moveToSurface(world, corner)).toList();

        var anyUnderwater = surfaceCorners.stream().anyMatch(cornerPos -> !world.getFluidState(cornerPos).isEmpty());
        if (anyUnderwater) {
            return Optional.empty();
        }

        var surfaceCornerYs = surfaceCorners.stream().map(BlockPos::getY).toList();
        var maxSurfaceCornerY = surfaceCornerYs.stream().max(Integer::compareTo).orElse(startingOrigin.getY());
        var minSurfaceCornerY = surfaceCornerYs.stream().min(Integer::compareTo).orElse(startingOrigin.getY());
        if (maxSurfaceCornerY - minSurfaceCornerY > MAX_HEIGHT_DIFFERENCE) {
            return Optional.empty();
        }

        // Move the origin down because we carve the lake out of the terrain.
        var originYOffset = HALF_HEIGHT + (startingOrigin.getY() - minSurfaceCornerY);
        if (startingOrigin.getY() <= world.getBottomY() + originYOffset) {
            return Optional.empty();
        }
        return Optional.of(startingOrigin.down(originYOffset));
    }

    private void generateBlob(boolean[] blocks, Random random) {
        var sizeRandomness = VectorHelper.nextVec3d(random);
        var sizeVariation = new Vec3d(BOUNDS.getX() * 6.0/16.0, BOUNDS.getY() / 2.0, BOUNDS.getZ() * 6.0/16.0);
        var sizeMinimum = sizeVariation.multiply(.5);
        var size = VectorHelper.hadamard(sizeRandomness, sizeVariation).add(sizeMinimum);
        var radius = size.multiply(.5);

        var positionRandomness = VectorHelper.nextVec3d(random);
        var basePositionVariation = Vec3d.of(BOUNDS);
        var positionMargins = new Vec3d(1,1,1); // Leaving the edges out of this, so that a barrier can always be placed later.
        var finalPositionVariation = basePositionVariation.subtract(size).subtract(positionMargins.multiply(2));
        var positionMinimum = positionMargins.add(radius);
        var center = VectorHelper.hadamard(positionRandomness, finalPositionVariation).add(positionMinimum);

        innerOffsets().forEach(offset -> {
            var offsetFromCenter = Vec3d.of(offset).subtract(center);
            var offsetFromCenterScaled = VectorHelper.hadamardDivide(offsetFromCenter, radius);
            var distanceFromCenterScaledSquared = VectorHelper.dot(offsetFromCenterScaled, offsetFromCenterScaled);
            if (distanceFromCenterScaledSquared < 1.0) {
                blocks[offsetToIndex(offset)] = true;
            }
        });
    }

    private void fillFluidBlocks(StructureWorldAccess world, BlockPos blockPos, boolean[] blocks, BlockState fluidState) {
        offsets().forEach(offset -> {
            if (blocks[offsetToIndex(offset)]) {
                BlockPos blockPosOffset = blockPos.add(offset);
                if (this.canReplace(world.getBlockState(blockPosOffset)) && world.getFluidState(blockPosOffset).isEmpty()) {
                    // If it's in the upper half, then turn it into air, otherwise turn it into fluid
                    boolean isLowerHalf = offset.getY() < HALF_HEIGHT;
                    if (isLowerHalf) {
                        world.setBlockState(blockPosOffset, fluidState, Block.NOTIFY_LISTENERS);
                    } else {
                        // If you turned it into air, then schedule ticks to make gravel and sand fall appropriately
                        world.setBlockState(blockPosOffset, AIR, Block.NOTIFY_LISTENERS);
                        world.scheduleBlockTick(blockPosOffset, AIR.getBlock(), 0);
                        this.markBlocksAboveForPostProcessing(world, blockPosOffset);
                    }
                }
            }
        });
    }

    private void fillBarrierBlocks(StructureWorldAccess world, BlockPos blockPos, boolean[] blocks, BlockState barrierState) {
        if (!barrierState.isAir()) {
            offsets().forEach(offset -> {
                var blockPosOffset = blockPos.add(offset);
                if (!world.getFluidState(blockPosOffset).isEmpty()) {
                    return;
                }
                boolean fluidAtPosition = blocks[offsetToIndex(offset)];
                boolean fluidNextToPosition = adjacentOffsets(offset).anyMatch(adjacentOffset -> blocks[offsetToIndex(adjacentOffset)]);
                boolean isOnEdge = !fluidAtPosition && fluidNextToPosition;
                boolean isLowerHalf = offset.getY() < HALF_HEIGHT;
                BlockState currentState = world.getBlockState(blockPosOffset);
                var isExposedToAir = !world.getBlockState(blockPosOffset.up()).isSolid() && world.getFluidState(blockPosOffset.up()).isEmpty();
                // We only need barrier blocks for the lower half, because that's where we placed the fluid blocks.
                if (isOnEdge && isLowerHalf) {
                    //if (!currentState.isSolid() && !currentState.isIn(BlockTags.LAVA_POOL_STONE_CANNOT_REPLACE)) {
                    if (!currentState.isIn(BlockTags.LAVA_POOL_STONE_CANNOT_REPLACE)) {
                        if (barrierState.getBlock() == ModBlocks.SOIL && isExposedToAir) {
                            world.setBlockState(blockPosOffset, ModBlocks.GRASS_BLOCK.getDefaultState(), Block.NOTIFY_LISTENERS);
                        } else {
                            world.setBlockState(blockPosOffset, barrierState, Block.NOTIFY_LISTENERS);
                        }
                    }
                } else if (isOnEdge) {
                    world.setBlockState(blockPosOffset, AIR, Block.NOTIFY_LISTENERS);
                    world.scheduleBlockTick(blockPosOffset, AIR.getBlock(), 0);
                    this.markBlocksAboveForPostProcessing(world, blockPosOffset);
                } else if (currentState.getBlock() == ModBlocks.SOIL && isExposedToAir) {
                    world.setBlockState(blockPosOffset, ModBlocks.GRASS_BLOCK.getDefaultState(), Block.NOTIFY_LISTENERS);
                } else if (currentState.getBlock() == ModBlocks.GRASS_BLOCK && !isExposedToAir) {
                    world.setBlockState(blockPosOffset, ModBlocks.SOIL.getDefaultState(), Block.NOTIFY_LISTENERS);
                }
            });
        }
    }

    private boolean canReplace(BlockState state) {
        return !state.isIn(BlockTags.FEATURES_CANNOT_REPLACE);
    }

    public record Config(BlockStateProvider fluid, BlockStateProvider barrier) implements FeatureConfig {
        public static final Codec<PondFeature.Config> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                BlockStateProvider.TYPE_CODEC.fieldOf("fluid").forGetter(PondFeature.Config::fluid),
                                BlockStateProvider.TYPE_CODEC.fieldOf("barrier").forGetter(PondFeature.Config::barrier)
                        )
                        .apply(instance, PondFeature.Config::new)
        );
    }
}
