package worldgate.conqueror.mechanic;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import worldgate.conqueror.WorldgateConqueror;
import worldgate.conqueror.mixin.player.LivingEntityAccessor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

public class BlockBreakingHandler {

    private static final int TICK_TIMEOUT = 400; // Pulled from WorldRenderer. Don't change unless you want desync (or mixin WorldRenderer).
    private static final int SWEEP_RANGE = 2;

    // these should be per player, and only on the server
    private final Map<BlockPos, Float> progressMap = new HashMap<>();
    private final Map<BlockPos, Integer> blockIDMap = new HashMap<>();
    private final Map<BlockPos, Long> lastUpdatedTickMap = new HashMap<>();

    private void removeFromAll(BlockPos pos) {
        progressMap.remove(pos);
        blockIDMap.remove(pos);
        lastUpdatedTickMap.remove(pos);
    }

    private int getID(BlockPos pos) {
        if (blockIDMap.containsKey(pos)) {
            return blockIDMap.get(pos);
        } else {
            return getNewIDAndSave(pos);
        }
    }
    private int getNewIDAndSave(BlockPos pos) {
        int id = getNewID();
        blockIDMap.put(pos, id);
        return id;
    }
    private int getNewID() {
        int i = 0;
        while(idInUse(i)) {
            i++;
        }
        return i;
    }
    private boolean idInUse(Integer id) {
        for(Integer usedID : blockIDMap.values()) {
            if (usedID.equals(id)) {
                return true;
            }
        }
        return false;
    }

    public void registerEventCallback() {
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (player.isSpectator() || player.isCreative()) {
                return ActionResult.PASS;
            }
            if (player.getAttackCooldownProgress(0) < 1.0) {
                return ActionResult.FAIL;
            } else {
                if (!world.isClient) {
                    ((LivingEntityAccessor) player).setLastAttackedTicks(0);

                    removeOldUpdates(world);
                    boolean isSweepAttack = player.getAttributeValue(EntityAttributes.PLAYER_SWEEPING_DAMAGE_RATIO) > 0
                            && player.isOnGround()
                            && !player.isSprinting()
                            && !player.isSneaking();
                    if (isSweepAttack) {
                        getSweepBlocks(world, pos).forEach(innerPos -> {
                            updateProgressAndTryBreak(player, world, innerPos, true);
                        });
                    }
                    updateProgressAndTryBreak(player, world, pos, false);
                }
                return ActionResult.SUCCESS;
            }
        });

        //UseItemCallback
        //UseEntityCallback
        //AttackEntityCallback

        // These should be moved elsewhere
        UseItemCallback.EVENT.register(((player, world, hand) -> {
            var itemStack = player.getStackInHand(hand);
            if (player.isSpectator() || player.isCreative()) {
                return TypedActionResult.pass(itemStack);
            }
            if (player.getAttackCooldownProgress(0) < 1.0) {
                return TypedActionResult.fail(itemStack);
            } else {
                return TypedActionResult.pass(itemStack);
            }
        }));
        UseBlockCallback.EVENT.register(((player, world, hand, hitResult) -> {
            if (player.isSpectator() || player.isCreative()) {
                return ActionResult.PASS;
            }
            if (player.getAttackCooldownProgress(0) < 1.0) {
                return ActionResult.FAIL;
            } else {
                return ActionResult.PASS;
            }
        }));
    }

    private void updateProgressAndTryBreak(PlayerEntity player, World world, BlockPos pos, boolean isSweep) {
        if (isSweep) {
            if (!player.getMainHandStack().isSuitableFor(world.getBlockState(pos))) {
                return;
            }
        }
        updateProgress(player, world, pos, isSweep);
        syncWithClient(player, pos);
        tryBreak(player, world, pos);
    }
    private List<BlockPos> getSweepBlocks(World world, BlockPos pos) {
        return IntStream.range(-SWEEP_RANGE, SWEEP_RANGE + 1).mapToObj(x -> {
            return IntStream.range(-SWEEP_RANGE, SWEEP_RANGE + 1).mapToObj(z -> {
                return new BlockPos(x + pos.getX(), pos.getY(), z + pos.getZ());
            });
        }).flatMap(Function.identity())
                .filter(posInner -> {
                    var xDiff = Math.abs(posInner.getX() - pos.getX());
                    var zDiff = Math.abs(posInner.getZ() - pos.getZ());
                    return xDiff + zDiff > 0 && xDiff + zDiff <= SWEEP_RANGE;
                }).filter(posInner -> world.getBlockState(pos).getBlock().equals(world.getBlockState(posInner).getBlock()))
                .toList();
    }

    private void removeOldUpdates(World world) {
        ArrayList<BlockPos> toRemove = new ArrayList<>();
        for (var entry : lastUpdatedTickMap.entrySet()) {
            if (world.getTime() - entry.getValue() > TICK_TIMEOUT) {
                toRemove.add(entry.getKey());
            }
        }
        for (var pos : toRemove) {
            removeFromAll(pos);
        }
    }
    private void updateProgress(PlayerEntity player, World world, BlockPos pos, boolean isSweep) {
        BlockState state = world.getBlockState(pos);
        float damage  = calculateBlockDamage(player, state, isSweep);

        boolean canHarvest = player.canHarvest(state);
        float blockHP = calculateBlockHP(canHarvest, world, pos);

        if (progressMap.containsKey(pos)) {
            damage += progressMap.get(pos) * blockHP;
        }
        progressMap.put(pos, damage / blockHP);
        lastUpdatedTickMap.put(pos, world.getTime());

    }
    private void tryBreak(PlayerEntity player, World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        boolean canHarvest = player.canHarvest(state);

        if (progressMap.containsKey(pos) && progressMap.get(pos) > 1f) {
            world.breakBlock(pos, canHarvest, player);
            removeFromAll(pos);
        }
    }

    private static float calculateBlockDamage(PlayerEntity player, BlockState state, boolean isSweep) {
        float miningSpeed = player.getBlockBreakingSpeed(state);
        double attackSpeed = player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED);
        if (isSweep) {
            double sweepRatio = player.getAttributeValue(EntityAttributes.PLAYER_SWEEPING_DAMAGE_RATIO);
            miningSpeed *= sweepRatio;
        }

        return miningSpeed / (float)attackSpeed;
    }
    private static float calculateBlockHP(boolean canHarvest, World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        float blockHardness = state.getHardness(world, pos);
        float hardnessFactor = 1.5f; // Look at state.calcBlockBreakingDelta for why
        if (!canHarvest) {
            hardnessFactor = 5f;
        }
        return blockHardness * hardnessFactor;
    }

    public record BlockBreakingPayload(int blockID, BlockPos pos, float progress) implements CustomPayload {
        // There's a vanilla payload for this already, but it doesn't work for me for some reason.
        public static final Id<BlockBreakingPayload> ID = new Id<>(Identifier.of(WorldgateConqueror.MOD_ID, "sync_block_break"));

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }


        public static final PacketCodec<PacketByteBuf, BlockBreakingPayload> CODEC = CustomPayload.codecOf(
                BlockBreakingPayload::write, BlockBreakingPayload::new
        );
        private BlockBreakingPayload(PacketByteBuf buf) {
            this(buf.readInt(), buf.readBlockPos(), buf.readFloat()); // delegates to the canonical record constructor
        }
        public void write(PacketByteBuf buf) {
            buf.writeInt(blockID).writeBlockPos(pos).writeFloat(progress);
        }
    }
    private void syncWithClient(PlayerEntity player, BlockPos pos) {
        ServerPlayNetworking.send(
                (ServerPlayerEntity) player,
                new BlockBreakingPayload(getID(pos), pos, progressMap.get(pos))
        );
    }
    public void registerNetworkingReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(
                BlockBreakingPayload.ID,
                (payload, context) -> {
                    context.client().execute( () -> {
                        context.client().world.setBlockBreakingInfo(payload.blockID, payload.pos, (int) (payload.progress * 10f - 1));
                        context.player().resetLastAttackedTicks();
                    });
                }
        );
    }
}
