package worldgate.conqueror.mixin;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import worldgate.conqueror.mechanic.ModEquipmentSlots;

@Mixin(AttributeModifierSlot.class)
public class AttributeModifierSlotMixin {

    @Inject(method="forEquipmentSlot", at=@At("HEAD"), cancellable = true)
    private static void forEquipmentSlot(EquipmentSlot slot, CallbackInfoReturnable<AttributeModifierSlot> cir) {
        if (ModEquipmentSlots.isAccessory(slot)) {
            cir.setReturnValue(ModEquipmentSlots.ACCESSORY_ATTRIBUTE_SLOT);
            cir.cancel();
        }
    }
}
