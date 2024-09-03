package worldgate.conqueror.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldgate.conqueror.entity.AccessorySlot;
import worldgate.conqueror.mechanic.ModEquipmentSlots;

import java.util.Map;

@Mixin(PlayerScreenHandler.class)
public abstract class PlayerScreenHandlerMixin extends AbstractRecipeScreenHandler<CraftingRecipeInput, CraftingRecipe> {


    @Final @Shadow private RecipeInputInventory craftingInput;
    @Final @Shadow private CraftingResultInventory craftingResult;
    @Final @Shadow private static Map<EquipmentSlot, Identifier> EMPTY_ARMOR_SLOT_TEXTURES;
    @Final @Shadow private static EquipmentSlot[] EQUIPMENT_SLOT_ORDER;

    public PlayerScreenHandlerMixin(ScreenHandlerType<?> screenHandlerType, int i) {
        super(screenHandlerType, i);
    }

    @Inject(method="<init>", at=@At(
            value = "TAIL"
    ))
    private void constructor(PlayerInventory inventory, boolean onServer, PlayerEntity owner, CallbackInfo ci) {

        // Remove the offhand slot
        this.slots.removeLast();
        ((ScreenHandlerAccessor)(Object) this).getTrackedStacks().removeLast();
        ((ScreenHandlerAccessor)(Object) this).getPreviousTrackedStacks().removeLast();

        // Add the offhand slot back in with the correct index
        this.addSlot(new Slot(inventory, 40 + ModEquipmentSlots.NUMBER_OF_ACCESSORY_SLOTS, 77, 62) {
            @Override
            public void setStack(ItemStack stack, ItemStack previousStack) {
                owner.onEquipStack(EquipmentSlot.OFFHAND, previousStack, stack);
                super.setStack(stack, previousStack);
            }

            @Override
            public Pair<Identifier, Identifier> getBackgroundSprite() {
                return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, PlayerScreenHandler.EMPTY_OFFHAND_ARMOR_SLOT);
            }
        });

        // Add the new slots in
        for (int i = 0; i < ModEquipmentSlots.NUMBER_OF_ACCESSORY_SLOTS; i++) {
            var equipmentSlot = ModEquipmentSlots.ACCESSORY;
            var identifier = Identifier.ofVanilla("item/empty_armor_slot_shield");
            this.addSlot(new AccessorySlot(inventory, owner, equipmentSlot, (stack) -> !alreadyHaveAccessoryEquipped(stack), 40 + i, 77, 8 + i * 18, identifier));
        }
    }
    private boolean accessorySlotOpen() {
        for (int i = 0; i < ModEquipmentSlots.NUMBER_OF_ACCESSORY_SLOTS; i++) {
            var slot = this.getSlot(46 + i);
            if (!slot.hasStack()) {
                return true;
            }
        }
        return false;
    }
    private boolean alreadyHaveAccessoryEquipped(ItemStack accessoryStack) {
        for (int i = 0; i < ModEquipmentSlots.NUMBER_OF_ACCESSORY_SLOTS; i++) {
            var slot = this.getSlot(46 + i);
            var itemInSlot = slot.getStack().getItem();
            if (itemInSlot.equals(accessoryStack.getItem())) {
                return true;
            }
        }
        return false;
    }

    @Overwrite @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot2 = this.slots.get(slot);
        if (slot2.hasStack()) {
            ItemStack itemStack2 = slot2.getStack();
            itemStack = itemStack2.copy();
            EquipmentSlot equipmentSlot = player.getPreferredEquipmentSlot(itemStack);
            if (slot == 0) {
                if (!this.insertItem(itemStack2, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }

                slot2.onQuickTransfer(itemStack2, itemStack);
            } else if (slot >= 1 && slot < 5) {
                if (!this.insertItem(itemStack2, 9, 45, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slot >= 5 && slot < 9) {
                if (!this.insertItem(itemStack2, 9, 45, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (ModEquipmentSlots.isAccessory(equipmentSlot) && slot < 46 && accessorySlotOpen() && !alreadyHaveAccessoryEquipped(itemStack2)) {
                if (!this.insertItem(itemStack2, 46, 46 + ModEquipmentSlots.NUMBER_OF_ACCESSORY_SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!ModEquipmentSlots.isAccessory(equipmentSlot) && equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && !((Slot)this.slots.get(8 - equipmentSlot.getEntitySlotId())).hasStack()) {
                int i = 8 - equipmentSlot.getEntitySlotId();
                if (!this.insertItem(itemStack2, i, i + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (equipmentSlot == EquipmentSlot.OFFHAND && !((Slot)this.slots.get(45)).hasStack()) {
                if (!this.insertItem(itemStack2, 45, 46, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slot >= 9 && slot < 36) {
                if (!this.insertItem(itemStack2, 36, 45, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slot >= 36 && slot < 45) {
                if (!this.insertItem(itemStack2, 9, 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(itemStack2, 9, 45, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty()) {
                slot2.setStack(ItemStack.EMPTY, itemStack);
            } else {
                slot2.markDirty();
            }

            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot2.onTakeItem(player, itemStack2);
            if (slot == 0) {
                player.dropItem(itemStack2, false);
            }
        }

        return itemStack;
    }
}
