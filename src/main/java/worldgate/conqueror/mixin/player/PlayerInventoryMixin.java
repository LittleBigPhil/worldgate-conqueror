package worldgate.conqueror.mixin.player;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldgate.conqueror.mechanic.ItemSwitchResetter;
import worldgate.conqueror.mechanic.ModEquipmentSlots;

import java.util.ArrayList;
import java.util.List;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {

    @Shadow public final DefaultedList<ItemStack> armor = DefaultedList.ofSize(4 + ModEquipmentSlots.NUMBER_OF_ACCESSORY_SLOTS, ItemStack.EMPTY);

    @Shadow public int selectedSlot;
    @Unique private int lastSelectedSlot = 0;
    @Unique private ItemStack lastSelectedStack = null;
    @Inject(method = "updateItems", at = @At("HEAD"))
    private void onUpdateItems(CallbackInfo ci) {
        // Runs on both the server and the client.
        PlayerInventory inventory = (PlayerInventory)(Object)this;
        var selectedStack = ((PlayerInventory)(Object) this).getStack(selectedSlot);
        if (selectedSlot != lastSelectedSlot) {
            //((LivingEntityAccessor)inventory.player).setLastAttackedTicks(0);
            ((ItemSwitchResetter)inventory.player).resetLastSwitchedTicks();
        } else if(lastSelectedStack != null && !lastSelectedStack.equals(selectedStack)) {
            ((ItemSwitchResetter)inventory.player).resetLastSwitchedTicks();
        }
        lastSelectedSlot = selectedSlot;
        lastSelectedStack = selectedStack;
    }
}