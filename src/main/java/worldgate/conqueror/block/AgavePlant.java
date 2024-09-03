package worldgate.conqueror.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.*;

import java.util.List;
import java.util.stream.Stream;

// copied and modified from CropBlock
public class AgavePlant extends PlantBlock implements Fertilizable {
    public static final IntProperty AGE = Properties.AGE_2;
    public static final MapCodec<AgavePlant> CODEC = createCodec(AgavePlant::new);

    public AgavePlant(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends PlantBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    public int getMaxAge() {
        return 2;
    }
    public int getAge(BlockState state) {
        return (Integer)state.get(this.getAgeProperty());
    }
    protected IntProperty getAgeProperty() {
        return AGE;
    }
    public BlockState withAge(int age) {
        return (BlockState)this.getDefaultState().with(this.getAgeProperty(), age);
    }
    public final boolean isMature(BlockState state) {
        return this.getAge(state) >= this.getMaxAge();
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return AGE_TO_SHAPE[this.getAge(state)];
    }
    private static final VoxelShape[] AGE_TO_SHAPE = new VoxelShape[]{
            Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
            Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 12.0, 16.0),
            Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)
    };//, Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 8.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 10.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 12.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 14.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)};

    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return floor.isOf(ModBlocks.SAND);
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    private static Stream<Runnable> getLeafOpportunities(WorldView world, BlockPos pos, BlockState state) {
        return Stream.of(pos.north(), pos.east(), pos.south(), pos.west())
                .filter(world::isAir)
                .map(offsetPos -> () -> {
                    // Replace block at offsetPos with a leaf block (oriented correctly)
                    Direction growthDirection = Direction.fromVector(offsetPos.getX() - pos.getX(), 0, offsetPos.getZ() - pos.getZ());
                    BlockState leafState = ModBlocks.AGAVE_LEAF.getDefaultState().with(AgaveLeafBlock.FACING, growthDirection);
                    ((ModifiableWorld)world).setBlockState(offsetPos, leafState, Block.NOTIFY_ALL);
                });
    }
    private static Stream<Runnable> getFruitOpportunities(WorldView world, BlockPos pos, BlockState state) {
        return Stream.of(pos.up())
                .filter(world::isAir)
                .map(offsetPos -> () -> {
                    // Replace block at offsetPos with a fruit block
                    ((ModifiableWorld)world).setBlockState(offsetPos, ModBlocks.AGAVE_FRUIT.getDefaultState(), Block.NOTIFY_ALL);
                });
    }


    @Override
    // Random growth
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.getBaseLightLevel(pos, 0) >= 9) {
            int i = this.getAge(state);
            if (i < this.getMaxAge()) {
                if (random.nextInt(26) == 0) {
                    world.setBlockState(pos, this.withAge(i + 1), 2);
                }
            } else {
                if (random.nextInt(26) == 0) {
                    tryGrow(world, pos, state, 1);
                }
            }
        }

    }

    // Fertilizer growth
    public void applyGrowth(ServerWorld world, BlockPos pos, BlockState state) {
        int hypotheticalAge = this.getAge(state) + this.getGrowthAmount(world);
        int actualAge = this.getMaxAge();
        if (hypotheticalAge > actualAge) {
            tryGrow(world, pos, state, hypotheticalAge - actualAge);
            hypotheticalAge = actualAge;
        }

        world.setBlockState(pos, this.withAge(hypotheticalAge), 2);
    }
    protected int getGrowthAmount(ServerWorld world) {
        return MathHelper.nextInt(world.random, 2, 3);
    } //bonemeal

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        return (!this.isMature(state)) || roomToGrow(world, pos, state);
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        applyGrowth(world, pos, state);
    }

    private boolean roomToGrow(WorldView world, BlockPos pos, BlockState state) {
        return Stream.concat(getLeafOpportunities(world, pos, state), getFruitOpportunities(world, pos, state))
                .findAny().isPresent();
    }

    public static void tryGrow(WorldView world, BlockPos pos, BlockState state, int amount) {
        if (amount > 0) {
            List<Runnable> leafOpportunities = getLeafOpportunities(world, pos, state).toList();
            if (!leafOpportunities.isEmpty()) {
                leafOpportunities.forEach(Runnable::run);
                amount -= 1;
            }
        }
        if (amount > 0) {
            List<Runnable> fruitOpportunities = getFruitOpportunities(world, pos, state).toList();
            fruitOpportunities.forEach(Runnable::run);
        }
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        AgaveLeafBlock.applyThornsEffect(world, entity);
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            if (world.getBlockState(pos.up()).getBlock() == ModBlocks.AGAVE_FRUIT) {
                world.breakBlock(pos.up(), true);
            }
            Stream.of(pos.north(), pos.east(), pos.south(), pos.west()).forEach(offsetPos -> {
                BlockState offsetState = world.getBlockState(offsetPos);
                Direction growthDirection = Direction.fromVector(offsetPos.getX() - pos.getX(), 0, offsetPos.getZ() - pos.getZ());
                BlockState stateOfGrownLeaf = ModBlocks.AGAVE_LEAF.getDefaultState().with(AgaveLeafBlock.FACING, growthDirection);
                if (offsetState == stateOfGrownLeaf) {
                    world.breakBlock(offsetPos, true);
                }
            });
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

}
