package worldgate.conqueror.mixin.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.Portal;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.MathHelper;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import worldgate.conqueror.mechanic.ModEntityAttributes;
import worldgate.conqueror.mechanic.SubmersionAccessor;

// Probably broke the fabric onElytraStart event, setting priority to 999 makes it not crash
@Mixin(value = ClientPlayerEntity.class, priority = 999)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile, MinecraftClient client) {
        super(world, profile);
        this.client = client;
    }

    @Shadow private boolean isWalking() { return true; }
    @Shadow private boolean canStartSprinting() { return true; }
    @Shadow private boolean canSprint() { return true; }
    @Shadow protected boolean isCamera() { return true; }
    @Shadow private void tickNausea(boolean fromPortalEffect) {}
    @Shadow private void pushOutOfBlocks(double x, double z) {}
    @Shadow protected void startRidingJump() {}
    @Shadow @Mutable @Final protected final MinecraftClient client;
    @Shadow private boolean inSneakingPose;
    @Shadow private boolean falling;
    @Shadow private int underwaterVisibilityTicks;
    @Shadow private int field_3938;
    @Shadow private float mountJumpStrength;
    @Shadow private int ticksToNextAutojump;

    @Shadow public abstract boolean isSubmergedInWater();

    // Removed double tap to sprint
    @Override
    public void tickMovement() {
        var player = (ClientPlayerEntity)(Object) this;

        if (!(this.client.currentScreen instanceof DownloadingTerrainScreen)) {
            this.tickNausea(player.getCurrentPortalEffect() == Portal.Effect.CONFUSION);
            this.tickPortalCooldown();
        }

        boolean jumpInput = player.input.jumping;
        PlayerAbilities playerAbilities = this.getAbilities();
        this.inSneakingPose = !playerAbilities.flying
                && !this.isSwimming()
                && !this.hasVehicle()
                && this.canChangeIntoPose(EntityPose.CROUCHING)
                && (this.isSneaking() || !this.isSleeping() && !this.canChangeIntoPose(EntityPose.STANDING));
        float f = (float)this.getAttributeValue(EntityAttributes.PLAYER_SNEAKING_SPEED);
        player.input.tick(player.shouldSlowDown(), f);
        this.client.getTutorialManager().onMovement(player.input);
        if (this.isUsingItem() && !this.hasVehicle()) {
            player.input.movementSideways *= 0.2F;
            player.input.movementForward *= 0.2F;
        }

        boolean tooSoonToAutoJump = false;
        if (this.ticksToNextAutojump > 0) {
            this.ticksToNextAutojump--;
            tooSoonToAutoJump = true;
            player.input.jumping = true;
        }

        if (!this.noClip) {
            this.pushOutOfBlocks(this.getX() - (double)this.getWidth() * 0.35, this.getZ() + (double)this.getWidth() * 0.35);
            this.pushOutOfBlocks(this.getX() - (double)this.getWidth() * 0.35, this.getZ() - (double)this.getWidth() * 0.35);
            this.pushOutOfBlocks(this.getX() + (double)this.getWidth() * 0.35, this.getZ() - (double)this.getWidth() * 0.35);
            this.pushOutOfBlocks(this.getX() + (double)this.getWidth() * 0.35, this.getZ() + (double)this.getWidth() * 0.35);
        }

        boolean canStartSprinting = this.canStartSprinting();

        boolean isTouchingFluid = ((SubmersionAccessor)(Object) this).isTouchingFluid();
        boolean isSubmergedInFluid = ((SubmersionAccessor)(Object) this).isSubmergedInFluid();

        if ((!isTouchingFluid || isSubmergedInFluid) && canStartSprinting && this.client.options.sprintKey.isPressed()) {
            this.setSprinting(true);
        }

        if (this.isSprinting()) {
            boolean bl8 = !player.input.hasForwardMovement() || !this.canSprint();
            boolean bl9 = bl8 || this.horizontalCollision && !this.collidedSoftly || isTouchingFluid && !isSubmergedInFluid;
            if (this.isSwimming()) {
                if (!this.isOnGround() && !player.input.sneaking && bl8 || !isTouchingFluid) {
                    this.setSprinting(false);
                }
            } else if (bl9) {
                this.setSprinting(false);
            }
        }

        boolean bl8 = false;
        if (playerAbilities.allowFlying) {
            if (this.client.interactionManager.isFlyingLocked()) {
                if (!playerAbilities.flying) {
                    playerAbilities.flying = true;
                    bl8 = true;
                    this.sendAbilitiesUpdate();
                }
            } else if (!jumpInput && player.input.jumping && !tooSoonToAutoJump) {
                if (this.abilityResyncCountdown == 0) {
                    this.abilityResyncCountdown = 7;
                } else if (!this.isSwimming()) {
                    playerAbilities.flying = !playerAbilities.flying;
                    if (playerAbilities.flying && this.isOnGround()) {
                        this.jump();
                    }

                    bl8 = true;
                    this.sendAbilitiesUpdate();
                    this.abilityResyncCountdown = 0;
                }
            }
        }

        if (player.input.jumping && !bl8 && !jumpInput && !playerAbilities.flying && !this.hasVehicle() && !this.isClimbing()) {
            ItemStack itemStack = this.getEquippedStack(EquipmentSlot.CHEST);
            if (itemStack.isOf(Items.ELYTRA) && ElytraItem.isUsable(itemStack) && this.checkFallFlying()) {
                player.networkHandler.sendPacket(new ClientCommandC2SPacket(this, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            }
        }

        this.falling = this.isFallFlying();
        if (this.isTouchingWater() && player.input.sneaking && this.shouldSwimInFluids()) {
            this.knockDownwards();
        }

        if (this.isSubmergedIn(FluidTags.WATER)) {
            int i = this.isSpectator() ? 10 : 1;
            this.underwaterVisibilityTicks = MathHelper.clamp(this.underwaterVisibilityTicks + i, 0, 600);
        } else if (this.underwaterVisibilityTicks > 0) {
            this.isSubmergedIn(FluidTags.WATER);
            this.underwaterVisibilityTicks = MathHelper.clamp(this.underwaterVisibilityTicks - 10, 0, 600);
        }

        if (playerAbilities.flying && this.isCamera()) {
            int i = 0;
            if (player.input.sneaking) {
                i--;
            }

            if (player.input.jumping) {
                i++;
            }

            if (i != 0) {
                this.setVelocity(this.getVelocity().add(0.0, (double)((float)i * playerAbilities.getFlySpeed() * 3.0F), 0.0));
            }
        }

        JumpingMount jumpingMount = player.getJumpingMount();
        if (jumpingMount != null && jumpingMount.getJumpCooldown() == 0) {
            if (this.field_3938 < 0) {
                this.field_3938++;
                if (this.field_3938 == 0) {
                    this.mountJumpStrength = 0.0F;
                }
            }

            if (jumpInput && !player.input.jumping) {
                this.field_3938 = -10;
                jumpingMount.setJumpStrength(MathHelper.floor(player.getMountJumpStrength() * 100.0F));
                this.startRidingJump();
            } else if (!jumpInput && player.input.jumping) {
                this.field_3938 = 0;
                this.mountJumpStrength = 0.0F;
            } else if (jumpInput) {
                this.field_3938++;
                if (this.field_3938 < 10) {
                    this.mountJumpStrength = (float)this.field_3938 * 0.1F;
                } else {
                    this.mountJumpStrength = 0.8F + 2.0F / (float)(this.field_3938 - 9) * 0.1F;
                }
            }
        } else {
            this.mountJumpStrength = 0.0F;
        }

        super.tickMovement();
        if (this.isOnGround() && playerAbilities.flying && !this.client.interactionManager.isFlyingLocked()) {
            playerAbilities.flying = false;
            this.sendAbilitiesUpdate();
        }
    }

    @Redirect(method = "updateHealth", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerEntity;maxHurtTime:I", opcode = Opcodes.PUTFIELD))
    private void setMaxHurtTime(ClientPlayerEntity entity, int defaultMaxHurtTime) {
        entity.maxHurtTime = (int) entity.getAttributeValue(ModEntityAttributes.DAMAGE_IMMUNITY_TIME);
    }
    @Redirect(method = "updateHealth", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerEntity;timeUntilRegen:I", opcode = Opcodes.PUTFIELD))
    private void setTimeUntilRegen(ClientPlayerEntity entity, int defaultTimeUntilRegen) {
        entity.timeUntilRegen = 5 + (int) entity.getAttributeValue(ModEntityAttributes.DAMAGE_IMMUNITY_TIME);
    }
}
