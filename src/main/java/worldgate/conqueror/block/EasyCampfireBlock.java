package worldgate.conqueror.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import worldgate.conqueror.entity.EasyCampfireEntity;
import worldgate.conqueror.entity.ModEntities;

// See CampfireBlock and FurnaceBlock
public class EasyCampfireBlock extends Block implements BlockEntityProvider {

    protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 7.0, 16.0);
    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    public EasyCampfireBlock(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EasyCampfireEntity(ModEntities.CAMPFIRE_ENTITY, pos, state);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else {
            this.openScreen(world, pos, player);
            return ActionResult.CONSUME;
        }
    }
    protected void openScreen(World world, BlockPos pos, PlayerEntity player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof EasyCampfireEntity) {
            player.openHandledScreen((NamedScreenHandlerFactory)blockEntity);
            player.incrementStat(Stats.INTERACT_WITH_CAMPFIRE);
        }
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (type != ModEntities.CAMPFIRE_ENTITY) {
            return null;
        } else {
            return (world_inner, pos, state_inner, entity) -> EasyCampfireEntity.tick(world_inner, pos, state_inner, (EasyCampfireEntity) entity) ;
        }
    }

    private static final float FIRE_DAMAGE = 1f;
    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity) {
            entity.damage(world.getDamageSources().campfire(), FIRE_DAMAGE);
        }

        super.onEntityCollision(state, world, pos, entity);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        // fire crackling sound
        if (random.nextInt(10) == 0) {
            world.playSound(
                    (double) pos.getX() + 0.5,
                    (double) pos.getY() + 0.5,
                    (double) pos.getZ() + 0.5,
                    SoundEvents.BLOCK_CAMPFIRE_CRACKLE,
                    SoundCategory.BLOCKS,
                    0.5F + random.nextFloat(),
                    random.nextFloat() * 0.7F + 0.6F,
                    false
            );
        }

        // particles
        if (random.nextInt(5) == 0) {
            for (int i = 0; i < random.nextInt(1) + 1; i++) {
                world.addParticle(
                        ParticleTypes.LAVA,
                        (double)pos.getX() + 0.5,
                        (double)pos.getY() + 0.5,
                        (double)pos.getZ() + 0.5,
                        (double)(random.nextFloat() / 2.0F),
                        5.0E-5,
                        (double)(random.nextFloat() / 2.0F)
                );
            }
        }
    }
}
