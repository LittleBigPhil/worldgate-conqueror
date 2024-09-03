package worldgate.conqueror.mixin.player;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Unique
    private boolean justSpawned = false;
    @Inject(method = "onSpawn", at=@At("TAIL"))
    public void onSpawn(CallbackInfo ci) {
        justSpawned = true;
    }
    // This is used to propagate the health value of the player to the clients.
    // If you have armor on that increases max health, it takes a tiny amount of time for that to register in the attributes
    // So it would otherwise cap your hp to the default max hp
    // See also LivingEntityMixin.readCustomDataFromNbt
    @Inject(method = "tick", at=@At("HEAD"), cancellable = false)
    public void tick(CallbackInfo ci){
        var player = (ServerPlayerEntity)(Object)this;
        if (justSpawned && player.getHealth() <= player.getMaxHealth()) {
            // setHealth marks the data tracker entry as dirty, so it'll propagate to the clients when possible
            // But, it only does that when you set it to a different value.
            player.setHealth(player.getHealth() - .01f);
            player.setHealth(player.getHealth());
            justSpawned = false;
        }
    }

    @Redirect(method = "trySleep", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;isBedWithinRange(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z"))
    private boolean isBedWithinRange(ServerPlayerEntity instance, BlockPos pos, Direction direction) {
        return true;
    }
}
