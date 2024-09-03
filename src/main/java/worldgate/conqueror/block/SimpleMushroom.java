package worldgate.conqueror.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.*;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;

import java.util.Iterator;

public class SimpleMushroom extends PlantBlock {
    public static final MapCodec<SimpleMushroom> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(
                createSettingsCodec()
        ).apply(instance, SimpleMushroom::new);
    });
    protected static final VoxelShape SHAPE = Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 6.0, 11.0);

    public MapCodec<SimpleMushroom> getCodec() {
        return CODEC;
    }

    public SimpleMushroom(AbstractBlock.Settings settings) {
        super(settings);
    }

    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (random.nextInt(25) == 0) {
            int allowedSurroundingMushrooms = 5;

            for (BlockPos blockPos : BlockPos.iterate(pos.add(-4, -1, -4), pos.add(4, 1, 4))) {
                if (world.getBlockState(blockPos).isOf(this)) {
                    --allowedSurroundingMushrooms;
                    if (allowedSurroundingMushrooms <= 0) {
                        return;
                    }
                }
            }

            BlockPos potentialSpreadPos = pos.add(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);

            for(int k = 0; k < 4; ++k) {
                if (world.isAir(potentialSpreadPos) && state.canPlaceAt(world, potentialSpreadPos)) {
                    pos = potentialSpreadPos;
                }

                potentialSpreadPos = pos.add(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
            }

            if (world.isAir(potentialSpreadPos) && state.canPlaceAt(world, potentialSpreadPos)) {
                world.setBlockState(potentialSpreadPos, state, 2);
            }
        }

    }

    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return floor.isOpaqueFullCube(world, pos);
    }

    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos blockPos = pos.down();
        BlockState blockState = world.getBlockState(blockPos);
        if (blockState.isIn(BlockTags.MUSHROOM_GROW_BLOCK)) {
            return true;
        } else {
            return world.getBaseLightLevel(pos, 0) < 13 && this.canPlantOnTop(blockState, world, blockPos);
        }
    }
}
