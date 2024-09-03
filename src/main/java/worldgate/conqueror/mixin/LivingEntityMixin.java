package worldgate.conqueror.mixin;

import net.minecraft.block.Blocks;
import net.minecraft.block.PowderSnowBlock;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import worldgate.conqueror.entity.*;
import worldgate.conqueror.item.CustomArmor;
import worldgate.conqueror.mechanic.*;
import worldgate.conqueror.util.Mat3d;
import worldgate.conqueror.util.RandomHelper;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements GrappleTarget, Grappler, StatusEffectTarget, KnockbackTarget {

    @Shadow private final DefaultedList<ItemStack> syncedArmorStacks = DefaultedList.ofSize(4 + ModEquipmentSlots.NUMBER_OF_ACCESSORY_SLOTS, ItemStack.EMPTY);

    @Unique
    private Grappler grappledBy;
    @Unique
    private GrappleTarget grappleTarget;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Unique @Override
    public LivingEntity entity() {
        return (LivingEntity)(Object)this;
    }
    @Unique @Override
    public Grappler getGrappledBy() {
        return this.grappledBy;
    }
    @Unique @Override
    public void setGrappledBy(Grappler grappledBy) {
        this.grappledBy = grappledBy;
    }
    @Unique @Override
    public GrappleTarget getGrappleTarget() {
        return this.grappleTarget;
    }
    @Unique @Override
    public void setGrappleTarget(GrappleTarget grappleTarget) {
        this.grappleTarget = grappleTarget;
    }
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        tickGrappleTarget();
        tickGrappler();
        tickEquipment();
    }

    private void tickEquipment() {
        for (var slot : EquipmentSlot.values()) {
            var item = entity().getEquippedStack(slot).getItem();
            if (item instanceof CustomArmor armorItem && (
                    armorItem.getSlotType() == slot || (ModEquipmentSlots.isAccessory(armorItem.getSlotType()) && ModEquipmentSlots.isAccessory(slot))
                    )) {
                for (var armorEffect : armorItem.getStatusEffects()) {
                    entity().addStatusEffect(armorEffect);
                }
            }
        }
    }

    @ModifyVariable(method = "travel", at = @At(value = "HEAD"), ordinal = 0, argsOnly = true)
    private Vec3d onTravel(Vec3d movementInput) {
        LivingEntity entity = (LivingEntity) (Object) this;
        var asGrappleTarget = (GrappleTarget)entity;
        if (asGrappleTarget.isGrappled()) {
            var offset = asGrappleTarget.getGrappledBy().entity().getPos().subtract(entity.getPos());
            offset = offset.add(0,-offset.y,0);
            var distance = offset.length();
            var offsetDir = offset.normalize();
            // negative x in minecraft is y on a graph
            // z in minecraft is x on a graph
            // therefore yaw should be MathHelper.atan2(-x, z);
            // but we need to convert it to degrees out of radians
            entity.setYaw((float) (MathHelper.atan2(-offsetDir.x, offsetDir.z) * (180 / Math.PI))); // towards negative x

            var idealDistance = 1.375f;
            var speed = 1f;
            if (!asGrappleTarget.getGrappledBy().entity().isOnGround()) {
                // helps with breaking grapples when you use a weapon with knockback
                speed *= 0;
            }
            return new Vec3d(0, 0, speed * (distance - idealDistance));
        } else if (ModStatusEffects.isImmobilized(entity)) {
            return Vec3d.ZERO;
        } else {
            return movementInput;
        }
    }
    @Shadow protected abstract boolean shouldSwimInFluids();
    @Shadow protected abstract float getBaseMovementSpeedMultiplier();
    @Shadow private SoundEvent getFallSound(int distance) { return null; }

    @Unique private static float DEFAULT_FRICTION = .91f;
    @Unique private static FluidSettings WATER_SETTINGS = new FluidSettings(.8f, .4f, .75f);
    @Unique private static FluidSettings LAVA_SETTINGS = new FluidSettings(.6f, .25f, .75f);
    @Overwrite // This copies what's already there, and then separates it into methods, so that I can modify those methods individually.
    public void travel(Vec3d movementInput) {
        var entity = (LivingEntity)(Object) this;
        if (this.isLogicalSideForUpdatingMovement()) {
            double gravityStrength = this.getFinalGravity();
            boolean isFalling = this.getVelocity().y <= 0.0;
            if (isFalling && entity.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
                gravityStrength = Math.min(gravityStrength, 0.01);
            }

            FluidState fluidState = this.getWorld().getFluidState(this.getBlockPos());
            if (this.isTouchingWater() && this.shouldSwimInFluids() && !entity.canWalkOnFluid(fluidState)) {
                travelFluid(movementInput, gravityStrength, isFalling, WATER_SETTINGS);
            } else if (this.isInLava() && this.shouldSwimInFluids() && !entity.canWalkOnFluid(fluidState)) {
                travelFluid(movementInput, gravityStrength, isFalling, LAVA_SETTINGS);
            } else if (entity.isFallFlying()) {
                travelFallFlying(gravityStrength);
            } else {
                travelNormal(movementInput, gravityStrength);
            }
        }

        entity.updateLimbs(this instanceof Flutterer);
    }
    // Complete rework to move speed and drag, also unifies the lava behavior and the water behavior
    @Unique void travelFluid(Vec3d movementInput, double gravityStrength, boolean isFalling, FluidSettings fluidSettings) {
        var entity = (LivingEntity)(Object) this;
        double startingYLevel = this.getY();
        float moveSpeed = entity.getMovementSpeed();
        float slipperiness = fluidSettings.slipperiness();
        if (this.isSprinting()) {
            moveSpeed *= fluidSettings.swimmingSpeed();
        } else {
            moveSpeed *= fluidSettings.wadingSpeed();
        }
        float waterMovementEfficiency = (float)entity.getAttributeValue(ModEntityAttributes.WATER_MOVEMENT_SPEED);
        moveSpeed *= waterMovementEfficiency;

        this.setVelocity(this.getVelocityWithTraction3D(movementInput, this.getVelocity(), moveSpeed, slipperiness));
        this.move(MovementType.SELF, this.getVelocity());
        Vec3d velocityAfterMovement = this.getVelocity();
        if (this.horizontalCollision && entity.isClimbing()) {
            velocityAfterMovement = new Vec3d(velocityAfterMovement.x, 0.2, velocityAfterMovement.z);
            this.setVelocity(velocityAfterMovement);
        }

        Vec3d velocityAfterGravity = entity.applyFluidMovingSpeed(gravityStrength, isFalling, this.getVelocity());
        this.setVelocity(velocityAfterGravity);
        if (this.horizontalCollision && this.doesNotCollide(velocityAfterGravity.x, velocityAfterGravity.y + 0.6F - this.getY() + startingYLevel, velocityAfterGravity.z)) {
            this.setVelocity(velocityAfterGravity.x, 0.3F, velocityAfterGravity.z);
        }

    }
    // This just copies what's already there (elytra)
    @Unique void travelFallFlying(double gravityStrength) {
        this.limitFallDistance();
        Vec3d vec3d4 = this.getVelocity();
        Vec3d vec3d5 = this.getRotationVector();
        float fx = this.getPitch() * (float) (Math.PI / 180.0);
        double i = Math.sqrt(vec3d5.x * vec3d5.x + vec3d5.z * vec3d5.z);
        double j = vec3d4.horizontalLength();
        double k = vec3d5.length();
        double l = Math.cos((double)fx);
        l = l * l * Math.min(1.0, k / 0.4);
        vec3d4 = this.getVelocity().add(0.0, gravityStrength * (-1.0 + l * 0.75), 0.0);
        if (vec3d4.y < 0.0 && i > 0.0) {
            double m = vec3d4.y * -0.1 * l;
            vec3d4 = vec3d4.add(vec3d5.x * m / i, m, vec3d5.z * m / i);
        }

        if (fx < 0.0F && i > 0.0) {
            double m = j * (double)(-MathHelper.sin(fx)) * 0.04;
            vec3d4 = vec3d4.add(-vec3d5.x * m / i, m * 3.2, -vec3d5.z * m / i);
        }

        if (i > 0.0) {
            vec3d4 = vec3d4.add((vec3d5.x / i * j - vec3d4.x) * 0.1, 0.0, (vec3d5.z / i * j - vec3d4.z) * 0.1);
        }

        this.setVelocity(vec3d4.multiply(0.99F, 0.98F, 0.99F));
        this.move(MovementType.SELF, this.getVelocity());
        if (this.horizontalCollision && !this.getWorld().isClient) {
            double m = this.getVelocity().horizontalLength();
            double n = j - m;
            float o = (float)(n * 10.0 - 3.0);
            if (o > 0.0F) {
                this.playSound(this.getFallSound((int)o), 1.0F, 1.0F);
                this.damage(this.getDamageSources().flyIntoWall(), o);
            }
        }

        if (this.isOnGround() && !this.getWorld().isClient) {
            this.setFlag(Entity.FALL_FLYING_FLAG_INDEX, false);
        }
    }
    // Complete rework to move speed and drag
    @Unique void travelNormal(Vec3d movementInput, double gravityStrength) {
        var entity = (LivingEntity)(Object) this;
        BlockPos blockPos = this.getVelocityAffectingPos();
        float slipperiness = this.getWorld().getBlockState(blockPos).getBlock().getSlipperiness();
        if (!this.isOnGround()) {
            slipperiness = .989f; // Equivalent to the most slippery block: blue ice.
        }

        updateVelocityBeforeMovementNormal(movementInput, this.getVelocity(), this.getMovementSpeed(), slipperiness);
        Vec3d appliedMovementSpeed = moveNormal();

        double finalYVelocity = appliedMovementSpeed.y;
        if (entity.hasStatusEffect(StatusEffects.LEVITATION)) {
            finalYVelocity += (0.05 * (double)(entity.getStatusEffect(StatusEffects.LEVITATION).getAmplifier() + 1) - appliedMovementSpeed.y) * 0.2;
        } else if (!this.getWorld().isClient || this.getWorld().isChunkLoaded(blockPos)) {
            finalYVelocity -= gravityStrength;
        } else if (this.getY() > (double)this.getWorld().getBottomY()) {
            finalYVelocity = -0.1;
        } else {
            finalYVelocity = 0.0;
        }

        this.setVelocity(appliedMovementSpeed.x, finalYVelocity, appliedMovementSpeed.z);
    }

    @Unique private void updateVelocityBeforeMovementNormal(Vec3d movementInput, Vec3d currentVelocity, double moveSpeed, double slipperiness) {
        var afterTraction = getVelocityWithTraction3D(movementInput, currentVelocity, moveSpeed, slipperiness);
        if (isOnGround() && currentVelocity.y > 0) {
            afterTraction = new Vec3d(afterTraction.x, currentVelocity.y, afterTraction.z);
        }
        var afterClimbing = applyClimbingSpeed(afterTraction);
        setVelocity(afterClimbing);
    }
    @Unique private Vec3d movementInputToVelocitySpace(Vec3d movementInput, float yaw) {
        float f = MathHelper.sin(yaw * (float) (Math.PI / 180.0));
        float g = MathHelper.cos(yaw * (float) (Math.PI / 180.0));
        return new Vec3d(movementInput.x * (double)g - movementInput.z * (double)f, movementInput.y, movementInput.z * (double)g + movementInput.x * (double)f);
    }
    @Unique private Vec3d getVelocityWithTraction3D(Vec3d movementInput, Vec3d currentVelocity, double moveSpeed, double slipperiness) {
        movementInput = movementInputToVelocitySpace(movementInput, this.getYaw());
        Vec3d localSpaceZ = movementInput;

        // For some reason the inverse of this is always applied every frame in LivingEntity, so this fixes that.
        var inputFactor = .98;
        if (entity().isSneaking()) {
            // For some reason sneaking modifies the movementInput instead of the movementSpeed, so this fixes that.
            inputFactor = 0.294;
        }
        movementInput = movementInput.multiply(1.0 / inputFactor);
        moveSpeed *= inputFactor;
        // Movement speeds were completely changed by the revamped calculations, so this compensates for that.
        moveSpeed *= 2;

        if (movementInput.lengthSquared() == 0) {
            if (currentVelocity.lengthSquared() != 0) {
                localSpaceZ = currentVelocity;
            } else {
                return currentVelocity;
            }
        } else if (movementInput.lengthSquared() > 1) {
            movementInput = movementInput.normalize();
        }

        var velocityInLocal = Mat3d.transformToLocalSpace(currentVelocity, localSpaceZ);
        var movementInput1D = movementInput.length();
        var intentionality = movementInput1D;
        if (entity().isSneaking()) {
            intentionality = 1;
        }
        var velocityAfterInLocal = new Vec3d(
                getVelocityWithTraction1D(0, velocityInLocal.x, moveSpeed, slipperiness, intentionality),
                getVelocityWithTraction1D(0, velocityInLocal.y, moveSpeed, slipperiness, intentionality),
                getVelocityWithTraction1D(movementInput1D, velocityInLocal.z, moveSpeed, slipperiness, intentionality)
        );
        return Mat3d.transformToWorldSpace(velocityAfterInLocal, localSpaceZ);
    }

    /**
     *
     * @param movementInput >= 0
     * @param currentVelocity
     * @param moveSpeed >= 0
     * @param slipperiness (0, 1)
     * @param intentionality (0, 1)
     * @return
     */
    @Unique private double getVelocityWithTraction1D(double movementInput, double currentVelocity, double moveSpeed, double slipperiness, double intentionality) {
        var targetVelocity = movementInput * moveSpeed;
        if (targetVelocity == currentVelocity) {
            // This is both optimization and removal of jitter.
            // Didn't test if it actually does either of those things.
            return currentVelocity;
        }

        // Chose this formula because it only uses the existing parameters, and it works with movement speed buffs out of the box.
        var tractionCoefficient = (1.0 - Math.min(slipperiness, .98)); // Capped at the equivalent of ice and packed ice (notably less than the .989 of blue ice).
        var frictionCoefficient = (1.0 - slipperiness);
        if (entity().hasNoDrag()) {
            frictionCoefficient = 0;
        }
        if (movementInput > 0 && targetVelocity < currentVelocity) {
            // Makes having a higher traction coefficient always better for control.
            tractionCoefficient = frictionCoefficient;
        }
        var traction = tractionCoefficient * (targetVelocity - currentVelocity);
        var friction = frictionCoefficient * (0.0 - currentVelocity);
        var acceleration = MathHelper.lerp(intentionality, friction, traction);
        return currentVelocity + acceleration;
    }
    @Unique private Vec3d moveNormal() {
        this.move(MovementType.SELF, this.getVelocity());
        Vec3d vec3d = this.getVelocity();
        if ((this.horizontalCollision || this.jumping)
                && (entity().isClimbing() || this.getBlockStateAtPos().isOf(Blocks.POWDER_SNOW) && PowderSnowBlock.canWalkOnPowderSnow(this))) {
            vec3d = new Vec3d(vec3d.x, 0.2, vec3d.z);
        }

        return vec3d;
    }
    @Shadow protected boolean jumping;

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void jump(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (ModStatusEffects.isImmobilized(entity) || isGrappled() || isGrappling()) {
            ci.cancel();
        }
    }
    @Redirect(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSprinting()Z"))
    private boolean doJumpSprintBoost(LivingEntity instance) {
        return false;
    }

    @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
    private void heal(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.hasStatusEffect(ModStatusEffects.HEMOPHILIA)) {
            ci.cancel();
        }
    }
    // See also PlayerEntityMixin.eatFood
    @Inject(method = "eatFood", at = @At("HEAD"), cancellable = true)
    private void eatFood(World world, ItemStack stack, FoodComponent foodComponent, CallbackInfoReturnable<ItemStack> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.hasStatusEffect(ModStatusEffects.NAUSEA)) {
            cir.setReturnValue(stack);
            cir.cancel();
        }
    }
    // This is duplicated in PlayerEntityMixin
    @Inject(method = "getMovementSpeed()F", at=@At("RETURN"), cancellable = true)
    private void getMovementSpeed(CallbackInfoReturnable<Float> cir) {
        if (isGrappling()) {
            cir.setReturnValue(.4f * cir.getReturnValue());
        }
    }


    @Inject(method = "swingHand(Lnet/minecraft/util/Hand;)V", at = @At("HEAD"), cancellable = true)
    private void swingHand(Hand hand, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity player) {
            if (player.getAttackCooldownProgress(0) < 1.0) {
                ci.cancel(); // Prevent swinging animation
            }
        }
    }

    @Overwrite
    public static DefaultAttributeContainer.Builder createLivingAttributes() {
        var builder = DefaultAttributeContainer.builder()
                .add(EntityAttributes.GENERIC_MAX_HEALTH)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                .add(EntityAttributes.GENERIC_ARMOR)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS)
                .add(EntityAttributes.GENERIC_MAX_ABSORPTION)
                .add(EntityAttributes.GENERIC_SCALE)
                .add(EntityAttributes.GENERIC_GRAVITY)
                .add(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE)
                .add(EntityAttributes.GENERIC_FALL_DAMAGE_MULTIPLIER)
                .add(EntityAttributes.GENERIC_JUMP_STRENGTH)
                .add(EntityAttributes.GENERIC_OXYGEN_BONUS)
                .add(EntityAttributes.GENERIC_BURNING_TIME)
                .add(EntityAttributes.GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE)
                .add(EntityAttributes.GENERIC_WATER_MOVEMENT_EFFICIENCY, 1)
                .add(EntityAttributes.GENERIC_MOVEMENT_EFFICIENCY)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK)

                .add(ModEntityAttributes.WATER_MOVEMENT_SPEED, 1)
                .add(EntityAttributes.GENERIC_STEP_HEIGHT, 1)
                .add(ModEntityAttributes.DAMAGE_IMMUNITY_TIME, 5)
                .add(ModEntityAttributes.DODGE, 10)

                .add(ModEntityAttributes.STRENGTH, 0)
                .add(ModEntityAttributes.MIND, 0)
                .add(ModEntityAttributes.HARDINESS, 0)
                .add(ModEntityAttributes.POISON_STRENGTH, 0)

                .add(ModEntityAttributes.ARMOR_EFFICIENCY, 1);
        DamageTypeDistribution.Mob.BASE.setBaseAttributesOf(builder);
        return builder;
    }

    @Inject(method = "applyArmorToDamage", at = @At("HEAD"), cancellable = true)
    private void onApplyArmorToDamage(DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
        LivingEntity self = (LivingEntity) (Object) this;

        DamageSourceHelper.getDamageDistribution(source).map(damageDistribution -> {
            DamageTypeDistribution resistances = DamageTypeDistribution.resistsOf(self);
            float reducedDamage = damageDistribution.scaleBy(amount).attackThrough(resistances);
            cir.setReturnValue(reducedDamage);
            return 0;
        });
    }

    @Shadow public abstract boolean isInvulnerableTo(DamageSource source);

    @Shadow protected abstract float applyArmorToDamage(DamageSource source, float amount);
    @Shadow protected abstract float modifyAppliedDamage(DamageSource source, float amount);

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    public void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        var dodge = (float) entity().getAttributeValue(ModEntityAttributes.DODGE);
        var hitRate = DamageTypeDistribution.attackThrough(1.0f, dodge);
        if (!RandomHelper.chance(entity().getRandom(), hitRate)) {
            cir.setReturnValue(false);
            cir.cancel();
            return;
        }
    }

    @Inject(method = "applyDamage", at = @At("HEAD"), cancellable = false)
    public void applyDamage(DamageSource source, float amount, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!this.isInvulnerableTo(source)) {
            float postMitigation = this.applyArmorToDamage(source, amount);
            postMitigation = this.modifyAppliedDamage(source, postMitigation);
            TextDisplayEntity.spawnDamageNumber(((Entity)(Object)this), source, postMitigation);

            ((Grappler) self).tryFlinch(postMitigation);
        }
    }

    @Redirect(method = "damage", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;maxHurtTime:I", opcode = Opcodes.PUTFIELD))
    private void setMaxHurtTime(LivingEntity entity, int defaultMaxHurtTime) {
        entity.maxHurtTime = (int) entity.getAttributeValue(ModEntityAttributes.DAMAGE_IMMUNITY_TIME);
    }
    @Redirect(method = "onDamaged", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;maxHurtTime:I", opcode = Opcodes.PUTFIELD))
    private void setMaxHurtTime2(LivingEntity entity, int defaultMaxHurtTime) {
        entity.maxHurtTime = (int) entity.getAttributeValue(ModEntityAttributes.DAMAGE_IMMUNITY_TIME);
    }
    @Redirect(method = "animateDamage", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;maxHurtTime:I", opcode = Opcodes.PUTFIELD))
    private void setMaxHurtTime3(LivingEntity entity, int defaultMaxHurtTime) {
        entity.maxHurtTime = (int) entity.getAttributeValue(ModEntityAttributes.DAMAGE_IMMUNITY_TIME);
    }
    @Redirect(method = "damage", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;timeUntilRegen:I", opcode = Opcodes.PUTFIELD))
    private void setTimeUntilRegen(LivingEntity entity, int defaultTimeUntilRegen) {
        entity.timeUntilRegen = 5 + (int) entity.getAttributeValue(ModEntityAttributes.DAMAGE_IMMUNITY_TIME);
    }
    @Redirect(method = "onDamaged", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;timeUntilRegen:I", opcode = Opcodes.PUTFIELD))
    private void setTimeUntilRegen2(LivingEntity entity, int defaultTimeUntilRegen) {
        entity.timeUntilRegen = 5 + (int) entity.getAttributeValue(ModEntityAttributes.DAMAGE_IMMUNITY_TIME);
    }

    // This affects projectiles, thorns, potions of harming, ender dragon breath attack, etc.
    // Could be achieved by adding the damage type tag corresponding to no knockback, but this is easier.
    @Redirect(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;takeKnockback(DDD)V"))
    private void dontTakeKnockback(LivingEntity instance, double strength, double x, double z) {}

    @Shadow @Final private static TrackedData<Float> HEALTH;// = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.FLOAT);

    @Shadow protected abstract Vec3d applyClimbingSpeed(Vec3d motion);

    @Shadow public abstract float getMovementSpeed();

    @Shadow private float movementSpeed;

    @Inject(method = "readCustomDataFromNbt", at=@At("TAIL"), cancellable = false)
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("Health", NbtElement.NUMBER_TYPE)) {
            // not using setHealth, so it doesn't get clamped, so max health changes don't break on loading.
            this.dataTracker.set(HEALTH, nbt.getFloat("Health"));
        }
    }
}