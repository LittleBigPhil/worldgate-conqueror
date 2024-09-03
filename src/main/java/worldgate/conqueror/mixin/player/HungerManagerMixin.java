package worldgate.conqueror.mixin.player;

import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldgate.conqueror.mechanic.ModEntityAttributes;


@Mixin(HungerManager.class)
public abstract class HungerManagerMixin {
    private static final float EXHAUSTION_UNIT = 1.0F;

    @Shadow private int foodLevel;
    @Shadow private float saturationLevel;
    @Unique private PlayerEntity player = null;

    @Inject(method = "update", at = @At("HEAD"))
    public void update(PlayerEntity player, CallbackInfo ci) {
        this.player = player;
        clampDynamic();
    }
    @Unique
    private int getMaxFood() {
        if (player != null) {
            return (int) player.getAttributeValue(ModEntityAttributes.MAX_FOOD_ATTRIBUTE);
        } else {
            return 10; // 2 = 1 leg
        }
    }
    @Unique private void clampDynamic() {
        this.foodLevel = MathHelper.clamp(this.foodLevel, 0, getMaxFood());
        this.saturationLevel = 0; // Disable saturation mechanic
    }

    @Overwrite
    private void addInternal(int nutrition, float saturation) {
        this.foodLevel += nutrition;
        this.saturationLevel += saturation;
        clampDynamic();
    }
    @Overwrite public boolean isNotFull() { return this.foodLevel < getMaxFood(); }


}
