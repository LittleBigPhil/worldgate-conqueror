package worldgate.conqueror.mixin.mob;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import worldgate.conqueror.mechanic.DamageTypeDistribution;
import worldgate.conqueror.mechanic.ZombieGrappleOrAttackGoal;

@Mixin(ZombieEntity.class)
public class ZombieEntityMixin extends HostileEntity {

    protected ZombieEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "createZombieAttributes", at=@At("RETURN"), cancellable = true)
    private static void createZombieAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        DefaultAttributeContainer.Builder builder = cir.getReturnValue();
        double defaultMoveSpeed = 0.23000000417232513;
        builder.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, defaultMoveSpeed * 1.5);
        DamageTypeDistribution.Mob.ZOMBIE.setBaseAttributesOf(builder);
        cir.setReturnValue(builder);
    }

    @Overwrite
    public static boolean shouldBeBaby(Random random) {
        return false;
    }

    @Overwrite
    public void initEquipment(Random random, LocalDifficulty localDifficulty) {
        return;
    }

    @Inject(method = "initCustomGoals", at=@At("HEAD"), cancellable = false)
    protected void initCustomGoals(CallbackInfo ci) {
        this.goalSelector.add(2, new ZombieGrappleOrAttackGoal((ZombieEntity)(Object)this, 1.0, false));
        //this.goalSelector.add(2, new ZombieGrappleGoal((ZombieEntity)(Object)this, 2.0, 1F));
    }

    @Shadow
    public boolean canBreakDoors() {
        return false;
    }
    @Overwrite
    public void initCustomGoals() {
        var zombie = (ZombieEntity)(Object)this;
        //this.goalSelector.add(2, new ZombieAttackGoal(zombie, 1.0, false));
        this.goalSelector.add(6, new MoveThroughVillageGoal(zombie, 1.0, true, 4, zombie::canBreakDoors));
        this.goalSelector.add(7, new WanderAroundFarGoal(zombie, 1.0));
        this.targetSelector.add(1, new RevengeGoal(zombie).setGroupRevenge(ZombifiedPiglinEntity.class));
        this.targetSelector.add(2, new ActiveTargetGoal<>(zombie, PlayerEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(zombie, MerchantEntity.class, false));
        this.targetSelector.add(3, new ActiveTargetGoal<>(zombie, IronGolemEntity.class, true));
        this.targetSelector.add(5, new ActiveTargetGoal<>(zombie, TurtleEntity.class, 10, true, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER));
    }

}
