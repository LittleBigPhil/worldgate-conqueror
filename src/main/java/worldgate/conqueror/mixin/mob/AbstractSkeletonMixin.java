package worldgate.conqueror.mixin.mob;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import worldgate.conqueror.mechanic.DamageTypeDistribution;

@Mixin(AbstractSkeletonEntity.class)
public abstract class AbstractSkeletonMixin {

    @Inject(method = "createAbstractSkeletonAttributes", at = @At("RETURN"), cancellable = true)
    private static void createAbstractSkeletonAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        DefaultAttributeContainer.Builder builder = cir.getReturnValue();
        DamageTypeDistribution.Mob.SKELETON.setBaseAttributesOf(builder);
        cir.setReturnValue(builder);
    }

    @Overwrite
    public void initEquipment(Random random, LocalDifficulty localDifficulty) {
        // Disable spawning with equipment.
        //super.initEquipment(random, localDifficulty);
        this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
    }

    @Shadow
    public abstract void equipStack(EquipmentSlot equipmentSlot, ItemStack itemStack);
}
