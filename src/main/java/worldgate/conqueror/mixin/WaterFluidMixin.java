package worldgate.conqueror.mixin;

import net.minecraft.fluid.WaterFluid;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import worldgate.conqueror.item.ModItems;

@Mixin(WaterFluid.class)
public class WaterFluidMixin {
    /**
     * @author Decayed
     * @reason Replaces the default water bucket item with the custom one.
     */
    @Overwrite
    public Item getBucketItem() {
        return ModItems.WATER_BUCKET;
    }
}
