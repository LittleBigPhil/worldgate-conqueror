package worldgate.conqueror.mechanic;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;

public interface GrappleTarget {
    Grappler getGrappledBy();
    void setGrappledBy(Grappler grappledBy);
    LivingEntity entity();

    default void startGrappledBy(Grappler grappledBy) {
        setGrappledBy(grappledBy);
        entity().addStatusEffect(new StatusEffectInstance(ModStatusEffects.GRAPPLED, -1));
    }
    default void tickGrappleTarget() {
        if (this.isGrappled()) {
            if (getGrappledBy().entity().getPos().subtract(entity().getPos()).length() > GrappleHandler.BREAK_DISTANCE) {
                breakGrappleAsTarget();
            }
        }
    }
    default boolean isGrappled() {
        if (getGrappledBy() != null) {
            if (getGrappledBy().entity().isDead()) {
                breakGrappleAsTarget();
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
    default void endGrappleAsTarget() {
        entity().removeStatusEffect(ModStatusEffects.GRAPPLED);
    }
    default void breakGrappleAsTarget() {
        endGrappleAsTarget();

        getGrappledBy().setGrappleTarget(null);
        setGrappledBy(null);
    }
}
