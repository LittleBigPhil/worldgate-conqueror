package worldgate.conqueror.mechanic;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import worldgate.conqueror.WorldgateConqueror;
import worldgate.conqueror.item.ModItems;
import worldgate.conqueror.mixin.player.LivingEntityAccessor;
import worldgate.conqueror.util.RandomHelper;

public class GrappleHandler {
    public static float BREAK_DISTANCE = 2.75f;

    public static void init() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (canStartGrapple(player, hand, entity)) {
                ((LivingEntityAccessor) player).setLastAttackedTicks(0);
                if (!player.getWorld().isClient() && grappleResistOvercame(player, (LivingEntity) entity)) {
                    startGrappleAsPlayerAction(player, (LivingEntity) entity);
                    syncWithClient(player, player, (LivingEntity) entity, true);
                }
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (canEndGrapple(player, hand, entity)) {
                ((LivingEntityAccessor) player).setLastAttackedTicks(0);
                if (!player.getWorld().isClient() && grappleResistOvercame(player, (LivingEntity) entity)) {
                    endGrappleAsPlayerAction(player, (LivingEntity) entity);
                } else {
                    ((GrappleTarget)entity).breakGrappleAsTarget();
                }
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });
    }

    private static boolean canStartGrapple(PlayerEntity player, Hand hand, Entity entity) {
        boolean validHand = validHandsForGrappling(player, hand);

        boolean notInCooldown = player.getAttackCooldownProgress(0) >= 1.0;

        boolean validTarget = entity instanceof LivingEntity;
        if (validTarget) {
            validTarget = !((GrappleTarget) entity).isGrappled();
        }

        boolean currentlyGrappling = ((Grappler)player).isGrappling();

        return validHand && notInCooldown && validTarget && !currentlyGrappling;
    }
    public static boolean canContinueGrapple(PlayerEntity player, Entity entity) {
        return validHandsForGrappling(player, Hand.MAIN_HAND);
    }
    private static boolean canEndGrapple(PlayerEntity player, Hand hand, Entity entity) {
        boolean validHand = validHandsForGrappling(player, hand);

        boolean notInCooldown = player.getAttackCooldownProgress(0) >= 1.0;

        boolean validTarget = entity instanceof LivingEntity;
        if (validTarget) {
            validTarget = ((GrappleTarget) entity).isGrappled();
            if (validTarget) {
                validTarget = ((GrappleTarget) entity).getGrappledBy() == (Grappler) player;
            }
        }

        boolean currentlyGrappling = ((Grappler)player).isGrappling();

        return validHand && notInCooldown && validTarget && currentlyGrappling;
    }

    private static void startGrappleAsPlayerAction(PlayerEntity player, LivingEntity target) {
        if (((Grappler)player).isGrappling()) {
            ((Grappler) player).breakGrappleAsGrappler();
        }

        ((Grappler) player).setGrappleTarget((GrappleTarget) target);
        ((GrappleTarget) target).startGrappledBy((Grappler) player);

        //WorldgateConqueror.LOGGER.info("startGrapple: {},{}", ((Grappler)player).getGrappleTarget(), ((GrappleTarget)target).getGrappledBy());
    }
    private static void endGrappleAsPlayerAction(PlayerEntity player, LivingEntity target) {
        ((GrappleTarget)target).breakGrappleAsTarget();

        var playerPos = player.getPos();
        var targetPos = target.getPos();
        var targetDir = (targetPos.subtract(playerPos)).normalize();
        var knockbackHorizontalSpeed = .7;
        var knockbackHorizontal = targetDir.multiply(knockbackHorizontalSpeed);
        var knockbackVerticalSpeed = .3;
        knockbackHorizontal = new Vec3d(knockbackHorizontal.getX(), Math.min(knockbackVerticalSpeed, knockbackHorizontal.getY()), knockbackHorizontal.getZ());
        target.setVelocity(knockbackHorizontal.add(0, knockbackVerticalSpeed, 0));

        //WorldgateConqueror.LOGGER.info("endGrapple: {},{},{}", player instanceof ServerPlayerEntity, ((Grappler)player).getGrappleTarget(), ((GrappleTarget)target).getGrappledBy());
    }
    public static void startGrappleAsMob(LivingEntity grapplerMob, LivingEntity grappledEntity){
        // should only run on the server
        if (grappledEntity.getWorld().isClient()) { // just to make sure
            throw new RuntimeException("Tried to start grapple as client.");
        }

        if (!grappleResistOvercame(grapplerMob, grappledEntity)) {
            return;
        }

        //WorldgateConqueror.LOGGER.info("mob:{} grappled grappledEntity", grapplerMob);
        ((Grappler) grapplerMob).setGrappleTarget((GrappleTarget) grappledEntity);
        ((GrappleTarget) grappledEntity).startGrappledBy((Grappler) grapplerMob);
        syncWithClient((PlayerEntity) grappledEntity, grapplerMob, grappledEntity, true);
    }
    public static void endGrappleAsServer(LivingEntity grappler, LivingEntity grappled) {
        ((GrappleTarget) grappled).breakGrappleAsTarget();
        if (grappler instanceof PlayerEntity player) {
            syncWithClient(player, grappler, grappled, false);
        }
        if (grappled instanceof PlayerEntity player) {
            syncWithClient(player, grappler, grappled, false);
        }
    }
    public static void interpretGrapplePayload(LivingEntity grappler, LivingEntity grappleTarget, boolean isStart) {
        var asGrappler = (Grappler) grappler;
        var asTarget = (GrappleTarget) grappleTarget;
        if (isStart) {
            asGrappler.setGrappleTarget(asTarget);
            asTarget.startGrappledBy(asGrappler);
        } else {
            asTarget.breakGrappleAsTarget();
        }
    }

    private static boolean grappleResistOvercame(LivingEntity grappler, LivingEntity target) {
        var strengthResistOvercame = !((StatusEffectTarget) target).doesResistEffect(ModStatusEffects.GRAPPLED, grappler, 0);
        var hitChance = DamageTypeDistribution.attackThrough(1.0f, (float) target.getAttributeValue(ModEntityAttributes.DODGE));
        var dodgeChanceOvercame = RandomHelper.chance(target.getRandom(), hitChance);
        return strengthResistOvercame && dodgeChanceOvercame;
    }

    private static boolean validHandsForGrappling(PlayerEntity player, Hand hand) {
        //return player.getMainHandStack().isEmpty() || player.getOffHandStack().isEmpty();
        boolean offHandFailSafe = false;
        if (hand.equals(Hand.MAIN_HAND)) {
            offHandFailSafe = validHandsForGrappling(player, Hand.OFF_HAND);
        }
        return offHandFailSafe || player.getStackInHand(hand).isEmpty() || isWearingGlove(player, hand);
    }
    private static boolean isWearingGlove(PlayerEntity player, Hand hand) {
        var itemInHand = player.getStackInHand(hand).getItem();
        return itemInHand.equals(ModItems.RAWHIDE_GLOVE);
    }

    public record GrapplePayload(int grapplerId, int grappleTargetId, boolean isStart) implements CustomPayload {
        public static final Id<GrappleHandler.GrapplePayload> ID = new Id<>(Identifier.of(WorldgateConqueror.MOD_ID, "sync_grapple"));

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }

        public static final PacketCodec<PacketByteBuf, GrappleHandler.GrapplePayload> CODEC = CustomPayload.codecOf(
                GrappleHandler.GrapplePayload::write, GrappleHandler.GrapplePayload::new
        );
        private GrapplePayload(PacketByteBuf buf) {
            this(buf.readInt(), buf.readInt(), buf.readBoolean()); // delegates to the canonical record constructor
        }
        public void write(PacketByteBuf buf) {
            buf.writeInt(grapplerId);
            buf.writeInt(grappleTargetId);
            buf.writeBoolean(isStart);
        }
    }
    private static void syncWithClient(PlayerEntity player, LivingEntity grappler, LivingEntity grappleTarget, boolean isStart) {
        ServerPlayNetworking.send(
                (ServerPlayerEntity) player,
                new GrappleHandler.GrapplePayload(grappler.getId(), grappleTarget.getId(), isStart)
        );
    }
    public static void registerNetworkingReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(
                GrappleHandler.GrapplePayload.ID,
                (payload, context) -> {
                    context.client().execute( () -> {
                        //WorldgateConqueror.LOGGER.info("Received payload={}", payload);
                        var grappler = context.client().world.getEntityById(payload.grapplerId);
                        var grappleTarget = context.client().world.getEntityById(payload.grappleTargetId);
                        GrappleHandler.interpretGrapplePayload((LivingEntity) grappler, (LivingEntity) grappleTarget, payload.isStart);
                    });
                }
        );
    }
}