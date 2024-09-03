package worldgate.conqueror.mechanic;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.Hand;

public class ZombieGrappleOrAttackGoal extends ZombieAttackGoal {
    private static final float GRAPPLE_CHANCE = .30f;

    public ZombieGrappleOrAttackGoal(ZombieEntity zombie, double speed, boolean pauseWhenMobIdle) {
        super(zombie, speed, pauseWhenMobIdle);
    }


    private boolean targetIsGrappleable() {
        return this.mob.getTarget() != null && !((GrappleTarget) this.mob.getTarget()).isGrappled();
    }

    @Override
    protected void attack(LivingEntity target) {
        if (this.canAttack(target)) {
            this.resetCooldown();
            this.mob.swingHand(Hand.MAIN_HAND);
            if (targetIsGrappleable() && this.mob.getRandom().nextFloat() <= GRAPPLE_CHANCE) {
                GrappleHandler.startGrappleAsMob(this.mob, this.mob.getTarget());
            } else {
                this.mob.tryAttack(target);
            }
        }
    }
}
