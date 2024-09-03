package worldgate.conqueror.mechanic;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;

public interface Grappler {
    GrappleTarget getGrappleTarget();
    void setGrappleTarget(GrappleTarget target);
    LivingEntity entity();

    default boolean isGrappling() {
        if (getGrappleTarget() != null) {
            if (getGrappleTarget().entity().isDead()) {
                breakGrappleAsGrappler();
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
    default void tickGrappler() {
        if (this.isGrappling()) {
            if (getGrappleTarget().entity().getPos().subtract(entity().getPos()).length() > GrappleHandler.BREAK_DISTANCE) {
                breakGrappleAsGrappler();
            } else if (entity() instanceof PlayerEntity player && !GrappleHandler.canContinueGrapple(player, getGrappleTarget().entity())) {
                breakGrappleAsGrappler();
            }
        }
    }
    default void breakGrappleAsGrappler() {
        getGrappleTarget().endGrappleAsTarget();

        getGrappleTarget().setGrappledBy(null);
        setGrappleTarget(null);
    }
    default void tryFlinch(float damageAmount) {
        // this only ever runs on the server
        if (isGrappling()) {
            var percentOfHealth = damageAmount / entity().getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH);
            if (entity().getRandom().nextFloat() <= percentOfHealth) {
                //WorldgateConqueror.LOGGER.info("Flinched on server?={} world", !entity().getWorld().isClient());
                GrappleHandler.endGrappleAsServer(entity(), getGrappleTarget().entity());
            }
        }
    }
}
