package worldgate.conqueror.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import worldgate.conqueror.WorldgateConqueror;
import worldgate.conqueror.entity.TextDisplayEntity;
import worldgate.conqueror.mechanic.ModEquipmentSlots;

@Mixin(ArmorStandEntity.class)
public class ArmorStandEntityMixin {
    @Shadow private final DefaultedList<ItemStack> armorItems = DefaultedList.ofSize(4 + ModEquipmentSlots.NUMBER_OF_ACCESSORY_SLOTS, ItemStack.EMPTY);

    @Inject(method = "damage", at = @At("HEAD"), cancellable = false)
    public void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!((ArmorStandEntity)(Object)this).isRemoved()) {
            TextDisplayEntity.spawnDamageNumber(((Entity)(Object)this), source, amount);
        }
    }
}
