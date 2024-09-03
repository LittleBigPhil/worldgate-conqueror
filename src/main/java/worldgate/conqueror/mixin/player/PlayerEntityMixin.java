package worldgate.conqueror.mixin.player;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ProjectileDeflection;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import worldgate.conqueror.WorldgateConqueror;
import worldgate.conqueror.item.ModularTool;
import worldgate.conqueror.mechanic.*;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements ItemSwitchResetter {


    /*private static final float EXHAUSTION_FROM_SPRINT_JUMP = 1f;
    private static final float EXHAUSTION_FROM_GENERIC_JUMP = .25f;
    @Overwrite
    public void jump() {
        if (isGrappling()) {
            return;
        }
        super.jump();
        this.incrementStat(Stats.JUMP);
        if (this.isSprinting()) {
            this.addExhaustion(0.2F); //default .2
        } else {
            this.addExhaustion(0.05F); //default .05
        }
    }*/

    @Inject(method = "tick", at=@At("HEAD"), cancellable = false)
    private void tick(CallbackInfo ci) {
        if (ModularTool.isTwoHanded(getMainHandStack()) || ModularTool.isTwoHanded(getOffHandStack())) {
            if (!getMainHandStack().isEmpty() && !getOffHandStack().isEmpty()) {
                resetLastSwitchedTicks();
            }
        }
        if (ModStatusEffects.isDisarmed(this)) {
            resetLastSwitchedTicks();
        }
    }


    // This is duplicated from LivingEntityMixin
    @Inject(method = "getMovementSpeed()F", at=@At("RETURN"), cancellable = true)
    private void getMovementSpeed(CallbackInfoReturnable<Float> cir) {
        if (((Grappler)(Object)this).isGrappling()) {
            cir.setReturnValue(.4f * cir.getReturnValue());
        }
    }

    @Final @Shadow private PlayerAbilities abilities;
    @Overwrite @Override
    // Made it properly scale jumping movement speed with regular movement speed
    public float getOffGroundSpeed() {
        if (this.abilities.flying && !this.hasVehicle()) {
            return this.isSprinting() ? this.abilities.getFlySpeed() * 2.0F : this.abilities.getFlySpeed();
        } else {
            return this.isSprinting() ? 0.25999999F * getMovementSpeed() : 0.2F * getMovementSpeed();
        }
    }



    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }
    @Shadow protected abstract void addExhaustion(float amount);
    @Shadow protected abstract void incrementStat(Identifier id);


    @Overwrite
    public static DefaultAttributeContainer.Builder createPlayerAttributes() {
        var builder = DefaultAttributeContainer.builder()
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE)
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
                .add(EntityAttributes.GENERIC_MOVEMENT_EFFICIENCY)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.1)
                .add(EntityAttributes.GENERIC_LUCK)
                .add(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED)
                .add(EntityAttributes.PLAYER_SUBMERGED_MINING_SPEED)
                .add(EntityAttributes.PLAYER_SNEAKING_SPEED)
                .add(EntityAttributes.PLAYER_MINING_EFFICIENCY)
                .add(EntityAttributes.PLAYER_SWEEPING_DAMAGE_RATIO)
                .add(EntityAttributes.GENERIC_WATER_MOVEMENT_EFFICIENCY)

                .add(ModEntityAttributes.WATER_MOVEMENT_SPEED, 1)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10)
                .add(EntityAttributes.GENERIC_STEP_HEIGHT, 1)
                .add(ModEntityAttributes.DAMAGE_IMMUNITY_TIME, 5)
                .add(ModEntityAttributes.DODGE, 10)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 0)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED, 2)
                .add(ModEntityAttributes.ITEM_SWITCH_SPEED, 1.25)
                .add(EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE, 3)
                .add(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE, 2)
                .add(ModEntityAttributes.MAX_FOOD_ATTRIBUTE, 10)

                .add(ModEntityAttributes.STRENGTH, 0)
                .add(ModEntityAttributes.MIND, 0)
                .add(ModEntityAttributes.HARDINESS, 0)
                .add(ModEntityAttributes.POISON_STRENGTH, 0)

                .add(ModEntityAttributes.ARMOR_EFFICIENCY, 1);
        DamageTypeDistribution.Mob.BASE.setBaseAttributesOf(builder);
        return builder;
    }


    @Shadow protected abstract float getDamageAgainst(Entity target, float f, DamageSource damageSource);

    // See also LivingEntityMixin.eatFood
    @Inject(method = "eatFood", at = @At("HEAD"), cancellable = true)
    private void eatFood(World world, ItemStack stack, FoodComponent foodComponent, CallbackInfoReturnable<ItemStack> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.hasStatusEffect(ModStatusEffects.NAUSEA)) {
            cir.setReturnValue(stack);
            cir.cancel();
        }
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void preAttack(Entity target, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        if (player.getAttackCooldownProgress(0) < 1.0) {
            ci.cancel(); // Cancel the attack if the cooldown is not complete
        }
    }
    @Overwrite
    public void attack(Entity target) {
        if (target.isAttackable()) {
            if (!target.handleAttack(this)) {
                PlayerEntity player = (PlayerEntity)(Object) this;
                float baseDamage = this.isUsingRiptide() ? this.riptideAttackDamage : (float)this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                ItemStack itemStack = this.getWeaponStack();
                DamageSource damageSource = this.getDamageSources().playerAttack(player);
                float damageGainedFromEnchantments = this.getDamageAgainst(target, baseDamage, damageSource) - baseDamage;
                //this.resetLastAttackedTicks();
                this.swingHand(Hand.MAIN_HAND);
                if (target.getType().isIn(EntityTypeTags.REDIRECTABLE_PROJECTILE)
                        && target instanceof ProjectileEntity projectileEntity
                        && projectileEntity.deflect(ProjectileDeflection.REDIRECTED, this, this, true)) {
                    this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, this.getSoundCategory());
                    return;
                }

                if (baseDamage > 0.0F || damageGainedFromEnchantments > 0.0F) {
                    boolean isSprinting;
                    if (this.isSprinting()) {
                        this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, this.getSoundCategory(), 1.0F, 1.0F);
                        isSprinting = true;
                    } else {
                        isSprinting = false;
                    }

                    baseDamage += itemStack.getItem().getBonusAttackDamage(target, baseDamage, damageSource);
                    boolean isCriticalHit = this.fallDistance > 0.0F
                            && !this.isOnGround()
                            && !this.isClimbing()
                            && !this.isTouchingWater()
                            && !this.hasStatusEffect(StatusEffects.BLINDNESS)
                            && !this.hasVehicle()
                            && target instanceof LivingEntity
                            && !this.isSprinting();
                    if (isCriticalHit) {
                        baseDamage *= 1.5F;
                    }

                    float totalDamage = baseDamage + damageGainedFromEnchantments;

                    boolean isSweepingAttack = false;
                    double speedDelta = (double)(this.horizontalSpeed - this.prevHorizontalSpeed);
                    if (!isCriticalHit && !isSprinting && this.isOnGround()) {// && speedDelta < (double)this.getMovementSpeed()) {
                        ItemStack itemStack2 = this.getStackInHand(Hand.MAIN_HAND);
                        if (this.getAttributeValue(EntityAttributes.PLAYER_SWEEPING_DAMAGE_RATIO) > 0) {
                            isSweepingAttack = true;
                        } else if (itemStack2.getItem() instanceof SwordItem) {
                            isSweepingAttack = true;
                        }
                    }

                    float j = 0.0F;
                    if (target instanceof LivingEntity livingEntity) {
                        j = livingEntity.getHealth();
                    }

                    Vec3d targetVelocity = target.getVelocity();
                    boolean didDamage = target.damage(damageSource, totalDamage);
                    if (didDamage) {
                        doAttackKnockback(target, damageSource, isSprinting);

                        if (isSweepingAttack) {
                            doSweepingAttack(target, baseDamage, damageSource);
                        }

                        if (target instanceof ServerPlayerEntity && target.velocityModified) {
                            ((ServerPlayerEntity)target).networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(target));
                            target.velocityModified = false;
                            target.setVelocity(targetVelocity);
                        }

                        var isGrappleHit = target instanceof GrappleTarget && ((GrappleTarget) target).isGrappled();
                        if (isCriticalHit || isGrappleHit) {
                            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, this.getSoundCategory(), 1.0F, 1.0F);
                            this.addCritParticles(target);
                        }

                        if (!isCriticalHit && !isSweepingAttack) {
                            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, this.getSoundCategory(), 1.0F, 1.0F);
                            //this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, this.getSoundCategory(), 1.0F, 1.0F);
                        }

                        if (damageGainedFromEnchantments > 0.0F) {
                            this.addEnchantedHitParticles(target);
                        }

                        this.onAttacking(target);
                        Entity entity = target;
                        if (target instanceof EnderDragonPart) {
                            entity = ((EnderDragonPart)target).owner;
                        }

                        boolean usedItem = false;
                        if (this.getWorld() instanceof ServerWorld serverWorld2) {
                            if (entity instanceof LivingEntity livingEntity3x) {
                                usedItem = itemStack.postHit(livingEntity3x, player);
                            }

                            EnchantmentHelper.onTargetDamaged(serverWorld2, target, damageSource);
                        }

                        if (!this.getWorld().isClient && !itemStack.isEmpty() && entity instanceof LivingEntity) {
                            if (usedItem) {
                                itemStack.postDamageEntity((LivingEntity)entity, player);
                            }

                            if (itemStack.isEmpty()) {
                                if (itemStack == this.getMainHandStack()) {
                                    this.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                                } else {
                                    this.setStackInHand(Hand.OFF_HAND, ItemStack.EMPTY);
                                }
                            }
                        }

                        if (target instanceof LivingEntity) {
                            float n = j - ((LivingEntity)target).getHealth();
                            this.increaseStat(Stats.DAMAGE_DEALT, Math.round(n * 10.0F));
                            if (this.getWorld() instanceof ServerWorld && n > 2.0F) {
                                int o = (int)((double)n * 0.5);
                                ((ServerWorld)this.getWorld())
                                        .spawnParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getBodyY(0.5), target.getZ(), o, 0.1, 0.0, 0.1, 0.2);
                            }
                        }

                        this.addExhaustion(0.1F);
                    } else {
                        this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, this.getSoundCategory(), 1.0F, 1.0F);
                    }
                }
            }
        }
    }
    @Unique private void doSweepingAttack(Entity target, float baseDamage, DamageSource damageSource) {
        float sweepingBaseDamage = Math.max(1.0F, (float)this.getAttributeValue(EntityAttributes.PLAYER_SWEEPING_DAMAGE_RATIO) * baseDamage);

        for (LivingEntity sweepingDamageTarget : this.getWorld().getNonSpectatingEntities(LivingEntity.class, target.getBoundingBox().expand(1.0, 0.25, 1.0))) {
            if (sweepingDamageTarget != this
                    && sweepingDamageTarget != target
                    && !this.isTeammate(sweepingDamageTarget)
                    && (!(sweepingDamageTarget instanceof ArmorStandEntity) || !((ArmorStandEntity) sweepingDamageTarget).isMarker())
                    && this.squaredDistanceTo(sweepingDamageTarget) < 9.0) {
                float sweepingDamageDealt = this.getDamageAgainst(sweepingDamageTarget, sweepingBaseDamage, damageSource);
                ((KnockbackTarget)sweepingDamageTarget).maybeTakeKnockback(
                        this,
                        0.4F,
                        (double)MathHelper.sin(this.getYaw() * (float) (Math.PI / 180.0)),
                        (double)(-MathHelper.cos(this.getYaw() * (float) (Math.PI / 180.0)))
                );
                sweepingDamageTarget.damage(damageSource, sweepingDamageDealt);
                if (this.getWorld() instanceof ServerWorld serverWorld) {
                    EnchantmentHelper.onTargetDamaged(serverWorld, sweepingDamageTarget, damageSource);
                }
            }
        }

        this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, this.getSoundCategory(), 1.0F, 1.0F);
        this.spawnSweepAttackParticles();
    }
    @Unique private void doAttackKnockback(Entity target, DamageSource damageSource, boolean isSprinting) {
        float knockbackAmount = this.getKnockbackAgainst(target, damageSource) + (isSprinting ? 1.0F : 0.0F);
        //WorldgateConqueror.LOGGER.info("{} knockbackAmount", knockbackAmount);
        if (knockbackAmount > 0.0F) {
            if (target instanceof KnockbackTarget knockbackTarget) {
                knockbackTarget.maybeTakeKnockback(
                        this,
                        (double)(knockbackAmount * 0.5F),
                        (double) MathHelper.sin(this.getYaw() * (float) (Math.PI / 180.0)),
                        (double)(-MathHelper.cos(this.getYaw() * (float) (Math.PI / 180.0)))
                );
            } else {
                target.addVelocity(
                        (double)(-MathHelper.sin(this.getYaw() * (float) (Math.PI / 180.0)) * knockbackAmount * 0.5F),
                        0.1,
                        (double)(MathHelper.cos(this.getYaw() * (float) (Math.PI / 180.0)) * knockbackAmount * 0.5F)
                );
            }

            this.setVelocity(this.getVelocity().multiply(0.6, 1.0, 0.6));
            this.setSprinting(false);
        }
    }

    @Shadow public abstract void resetLastAttackedTicks();
    @Shadow public abstract float getAttackCooldownProgress(float v);
    @Shadow public abstract void spawnSweepAttackParticles();
    @Shadow public abstract void addCritParticles(Entity target);
    @Shadow public abstract void addEnchantedHitParticles(Entity target);
    @Shadow public abstract void increaseStat(Identifier damageDealt, int round);

    @Shadow public abstract boolean isSpectator();

    @Shadow public abstract @NotNull ItemStack getWeaponStack();

    @Unique private boolean isItemSwitchCooldown = false;
    @Inject(method = "resetLastAttackedTicks", at = @At("HEAD"), cancellable = true)
    private void onResetLastAttackedTicks(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        if (player.getAttackCooldownProgress(0) < 1.0) {
            ci.cancel();
        } else {
            isItemSwitchCooldown = false;
        }
    }
    @Unique public void resetLastSwitchedTicks() {
        var entity = (LivingEntityAccessor)(Object) this;
        entity.setLastAttackedTicks(0);
        isItemSwitchCooldown = true;
    }
    @Inject(method = "getAttackCooldownProgressPerTick", at = @At("RETURN"), cancellable = true)
    public void getAttackCooldownProgressPerTick(CallbackInfoReturnable<Float> cir) {
        if (isItemSwitchCooldown) {
            cir.setReturnValue((float)(20.0 / this.getAttributeValue(ModEntityAttributes.ITEM_SWITCH_SPEED)));
        }
    }


    @Inject(method = "applyDamage", at = @At("HEAD"), cancellable = false)
    public void applyDamage(DamageSource source, float amount, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!this.isInvulnerableTo(source)) {
            float postMitigation = this.applyArmorToDamage(source, amount);
            postMitigation = this.modifyAppliedDamage(source, postMitigation);

            ((Grappler) self).tryFlinch(postMitigation);
        }
    }
}
