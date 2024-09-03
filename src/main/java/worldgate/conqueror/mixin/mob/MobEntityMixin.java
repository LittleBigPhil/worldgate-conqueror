package worldgate.conqueror.mixin.mob;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.MobVisibilityCache;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import worldgate.conqueror.mechanic.GrappleTarget;
import worldgate.conqueror.mechanic.KnockbackTarget;
import worldgate.conqueror.mechanic.ModEquipmentSlots;
import worldgate.conqueror.mechanic.ModStatusEffects;

@Mixin(MobEntity.class)
public class MobEntityMixin {
    @Shadow private final DefaultedList<ItemStack> armorItems = DefaultedList.ofSize(4 + ModEquipmentSlots.NUMBER_OF_ACCESSORY_SLOTS, ItemStack.EMPTY);
    @Shadow protected final float[] armorDropChances = new float[4 + ModEquipmentSlots.NUMBER_OF_ACCESSORY_SLOTS];

    @Overwrite
    public boolean isAffectedByDaylight() {
        return false;
    }

    //@Shadow protected int despawnCounter;
    @Shadow @Final private MobVisibilityCache visibilityCache;
    @Shadow @Final protected GoalSelector targetSelector;
    @Shadow @Final protected GoalSelector goalSelector;

    @Inject(method = "tickNewAi", at = @At("HEAD"), cancellable = true)
    private void tickNewAi(CallbackInfo ci) {
        MobEntity mob = (MobEntity)(Object)this;
        GrappleTarget asGrapple = (GrappleTarget) mob;

        if (asGrapple.isGrappled()) {
            //mob.despawnCounter++;
            Profiler profiler = mob.getWorld().getProfiler();
            profiler.push("sensing");
            this.visibilityCache.clear();
            profiler.pop();
            int i = mob.age + mob.getId();
            if (i % 2 != 0 && mob.age > 1) {
                profiler.push("targetSelector");
                this.targetSelector.tickGoals(false);
                profiler.pop();
                profiler.push("goalSelector");
                this.goalSelector.tickGoals(false);
                profiler.pop();
            } else {
                profiler.push("targetSelector");
                this.targetSelector.tick();
                profiler.pop();
                profiler.push("goalSelector");
                this.goalSelector.tick();
                profiler.pop();
            }

            if (!asGrapple.isGrappled()) {
                // if something happened to break the grapple
                ci.cancel();
                return;
            }

            var player = asGrapple.getGrappledBy().entity();
            var playerPos = player.getPos();
            var mobOffset = mob.getPos().subtract(playerPos);
            var distance = mobOffset.length();
            var maxDistance = 1.4f;
            var minDistance = 1.35f;
            var dirOfMob = mobOffset.normalize();
            if (distance > maxDistance || distance < minDistance) {
                var preferredDistance = (maxDistance + minDistance) / 2.0;
                var target = playerPos.add(dirOfMob.multiply(preferredDistance));
                mob.getMoveControl().moveTo(target.x, target.y, target.z, 1);
            }
            mob.getLookControl().lookAt(player);
            mob.getMoveControl().tick();
            mob.getLookControl().tick();

            // Disable normal AI
            ci.cancel();
        }
    }

    @Inject(method = "tryAttack", at = @At("HEAD"), cancellable = true)
    protected void tryAttack(Entity target, CallbackInfoReturnable<Boolean> cir) {
        var entity = (LivingEntity)(Object) this;
        if (ModStatusEffects.isDisarmed(entity)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Redirect(method = "tryAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;takeKnockback(DDD)V"))
    private void attackKnockbackResistable(LivingEntity instance, double strength, double x, double z) {
        ((KnockbackTarget) instance).maybeTakeKnockback((LivingEntity) (Object) this, strength, x, z);
    }
}
