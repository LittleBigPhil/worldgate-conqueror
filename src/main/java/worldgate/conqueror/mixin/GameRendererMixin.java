package worldgate.conqueror.mixin;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import worldgate.conqueror.mechanic.ModStatusEffects;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "getNightVisionStrength", at = @At("HEAD"), cancellable = true)
    private static void getNightVisionStrength(LivingEntity entity, float tickDelta, CallbackInfoReturnable<Float> cir) {
        if (entity.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            cir.setReturnValue(1.0f);
            cir.cancel();
            return;
        }
        cir.cancel();
    }
}
