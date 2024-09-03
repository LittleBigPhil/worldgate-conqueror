package worldgate.conqueror.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.*;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidDrainable;
import net.minecraft.block.FluidFillable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

//see BucketItem
public class ChargedBucketItem extends Item implements FluidModificationItem {
    private static final int USE_DURATION = 16;
    private final Fluid fluid;

    public ChargedBucketItem(Fluid fluid, Settings settings) {
        super(settings);
        this.fluid = fluid;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (user.isOnGround()) {
            user.setCurrentHand(hand);
            return TypedActionResult.consume(itemStack);
        } else {
            return TypedActionResult.fail(itemStack);
        }
    }
    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!user.isOnGround()) {
            user.stopUsingItem();
        }
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }



    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72000;
        //return 16; // Half default food (in ticks)
    }

    public static ItemStack getEmptiedStack(ItemStack stack, PlayerEntity player) {
        return !player.isInCreativeMode() ? new ItemStack(ModItems.BUCKET) : stack;
    }

    @Override
    public boolean placeFluid(@Nullable PlayerEntity player, World world, BlockPos pos, @Nullable BlockHitResult hitResult) {
        if (!(this.fluid instanceof FlowableFluid flowableFluid)) {
            return false;
        } else {
            Block block;
            boolean bl;
            BlockState blockState;
            boolean var10000;
            label82: {
                blockState = world.getBlockState(pos);
                block = blockState.getBlock();
                bl = blockState.canBucketPlace(this.fluid);
                label70:
                if (!blockState.isAir() && !bl) {
                    if (block instanceof FluidFillable fluidFillable && fluidFillable.canFillWithFluid(player, world, pos, blockState, this.fluid)) {
                        break label70;
                    }

                    var10000 = false;
                    break label82;
                }

                var10000 = true;
            }

            boolean bl2 = var10000;
            if (!bl2) {
                return hitResult != null && this.placeFluid(player, world, hitResult.getBlockPos().offset(hitResult.getSide()), null);
            } else if (world.getDimension().ultrawarm() && this.fluid.isIn(FluidTags.WATER)) {
                int i = pos.getX();
                int j = pos.getY();
                int k = pos.getZ();
                world.playSound(
                        player, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F
                );

                for (int l = 0; l < 8; l++) {
                    world.addParticle(ParticleTypes.LARGE_SMOKE, (double)i + Math.random(), (double)j + Math.random(), (double)k + Math.random(), 0.0, 0.0, 0.0);
                }

                return true;
            } else {
                if (block instanceof FluidFillable fluidFillable && this.fluid == Fluids.WATER) {
                    fluidFillable.tryFillWithFluid(world, pos, blockState, flowableFluid.getStill(false));
                    this.playEmptyingSound(player, world, pos);
                    return true;
                }

                if (!world.isClient && bl && !blockState.isLiquid()) {
                    world.breakBlock(pos, true);
                }

                if (!world.setBlockState(pos, this.fluid.getDefaultState().getBlockState(), Block.NOTIFY_ALL_AND_REDRAW) && !blockState.getFluidState().isStill()) {
                    return false;
                } else {
                    this.playEmptyingSound(player, world, pos);
                    return true;
                }
            }
        }
    }

    protected void playEmptyingSound(@Nullable PlayerEntity player, WorldAccess world, BlockPos pos) {
        SoundEvent soundEvent = this.fluid.isIn(FluidTags.LAVA) ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA : SoundEvents.ITEM_BUCKET_EMPTY;
        world.playSound(player, pos, soundEvent, SoundCategory.BLOCKS, 1.0F, 1.0F);
        world.emitGameEvent(player, GameEvent.FLUID_PLACE, pos);
    }


    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity)) {
            return;
        }

        int useTime = this.getMaxUseTime(stack, user) - remainingUseTicks;

        if (useTime < USE_DURATION) {
            return; // Not wound up enough, do nothing
        }

        BlockHitResult blockHitResult = raycast(
                world, (PlayerEntity) user, this.fluid == Fluids.EMPTY ? RaycastContext.FluidHandling.SOURCE_ONLY : RaycastContext.FluidHandling.NONE
        );
        if (blockHitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = blockHitResult.getBlockPos();
            Direction direction = blockHitResult.getSide();
            BlockPos blockPos2 = blockPos.offset(direction);
            if (!world.canPlayerModifyAt((PlayerEntity) user, blockPos) || !((PlayerEntity) user).canPlaceOn(blockPos2, direction, stack)) {
                return;// stack;// TypedActionResult.fail(stack);
            } else if (this.fluid == Fluids.EMPTY) {
                BlockState blockState = world.getBlockState(blockPos);
                if (blockState.getBlock() instanceof FluidDrainable fluidDrainable) {
                    ItemStack itemStack2 = fluidDrainable.tryDrainFluid((PlayerEntity) user, world, blockPos, blockState);
                    if (!itemStack2.isEmpty()) {
                        ((PlayerEntity) user).incrementStat(Stats.USED.getOrCreateStat(this));
                        fluidDrainable.getBucketFillSound().ifPresent(sound -> user.playSound(sound, 1.0F, 1.0F));
                        world.emitGameEvent(user, GameEvent.FLUID_PICKUP, blockPos);
                        ItemStack itemStack3 = ItemUsage.exchangeStack(stack, (PlayerEntity) user, itemStack2);
                        if (!world.isClient) {
                            Criteria.FILLED_BUCKET.trigger((ServerPlayerEntity)user, itemStack2);
                        }

                        user.setStackInHand(user.getActiveHand(), itemStack3);
                        return;// itemStack3;//TypedActionResult.success(itemStack3, world.isClient());
                    }
                }

                return;// stack;//TypedActionResult.fail(stack);
            } else {
                BlockState blockState = world.getBlockState(blockPos);
                BlockPos blockPos3 = blockState.getBlock() instanceof FluidFillable && this.fluid == Fluids.WATER ? blockPos : blockPos2;
                if (this.placeFluid((PlayerEntity) user, world, blockPos3, blockHitResult)) {
                    this.onEmptied((PlayerEntity) user, world, stack, blockPos3);
                    if (user instanceof ServerPlayerEntity) {
                        Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity)user, blockPos3, stack);
                    }

                    ((PlayerEntity) user).incrementStat(Stats.USED.getOrCreateStat(this));
                    ItemStack itemStack2 = ItemUsage.exchangeStack(stack, (PlayerEntity) user, getEmptiedStack(stack, (PlayerEntity) user));
                    user.setStackInHand(user.getActiveHand(), itemStack2);
                    return;// itemStack2;//TypedActionResult.success(itemStack2, world.isClient());
                } else {
                    return;// stack;//TypedActionResult.fail(stack);
                }
            }
        }
    }
    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {

        return stack;
    }
}
