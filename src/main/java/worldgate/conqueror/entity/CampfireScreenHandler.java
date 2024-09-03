package worldgate.conqueror.entity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.slot.FurnaceOutputSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

// See FurnaceScreenHandler and its parent
public class CampfireScreenHandler extends AbstractRecipeScreenHandler<SingleStackRecipeInput, AbstractCookingRecipe> {

    private static final int START_OF_HOT_BAR_SLOTS = EasyCampfireEntity.OUTPUT_SLOT_INDEX + 9*3;
    private static final int TOTAL_SLOTS = START_OF_HOT_BAR_SLOTS + 9;

    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;
    protected final World world;

    public CampfireScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(2), new ArrayPropertyDelegate(2));
    }
    public CampfireScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate) {
        super(ModEntities.CAMPFIRE_SCREEN_HANDLER, syncId);
        checkSize(inventory, 2);
        checkDataCount(propertyDelegate, 2);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        this.world = playerInventory.player.getWorld();
        this.addSlot(new Slot(inventory, EasyCampfireEntity.INPUT_SLOT_INDEX, 56, 17+17));
        this.addSlot(new CampfireOutputSlot(playerInventory.player, inventory, EasyCampfireEntity.OUTPUT_SLOT_INDEX, 116, 35));

        // player inventory
        for (int i = 0; i < 3; i++) { // for each row above the hot bar
            for (int j = 0; j < 9; j++) { // for each column
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) { // for each column in the hot bar
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }

        this.addProperties(propertyDelegate);
    }


    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public int getCraftingResultSlotIndex() {
        return EasyCampfireEntity.OUTPUT_SLOT_INDEX;
    }
    @Override
    public int getCraftingWidth() {
        return 1;
    }
    @Override
    public int getCraftingHeight() {
        return 1;
    }
    @Override
    public int getCraftingSlotCount() {
        return 2;
    }
    @Override
    public boolean canInsertIntoSlot(int index) {
        return true;
    }
    @Override
    public void clearCraftingSlots() {
        this.getSlot(EasyCampfireEntity.INPUT_SLOT_INDEX).setStackNoCallbacks(ItemStack.EMPTY);
        this.getSlot(EasyCampfireEntity.OUTPUT_SLOT_INDEX).setStackNoCallbacks(ItemStack.EMPTY);
    }



    @Override
    public RecipeBookCategory getCategory() {
        return RecipeBookCategory.valueOf("CAMPFIRE");
    } //RecipeBookCategory.SMOKER
    @Override
    public void populateRecipeFinder(RecipeMatcher finder) {
        if (this.inventory instanceof RecipeInputProvider) {
            ((RecipeInputProvider)this.inventory).provideRecipeInputs(finder);
        }
    }
    @Override
    public boolean matches(RecipeEntry<AbstractCookingRecipe> recipe) {
        return recipe.value().matches(new SingleStackRecipeInput(this.inventory.getStack(EasyCampfireEntity.INPUT_SLOT_INDEX)), this.world);
    }


    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack copyOfOriginalStack = ItemStack.EMPTY; // default to failure indicator
        Slot slotBeingMoved = this.slots.get(slot);
        if (slotBeingMoved != null && slotBeingMoved.hasStack()) {
            ItemStack stackAfterMoving = slotBeingMoved.getStack();
            copyOfOriginalStack = stackAfterMoving.copy();
            if (slot == EasyCampfireEntity.OUTPUT_SLOT_INDEX) {
                if (!this.insertItem(stackAfterMoving, EasyCampfireEntity.OUTPUT_SLOT_INDEX + 1, TOTAL_SLOTS, true)) {
                    return ItemStack.EMPTY;
                }

                slotBeingMoved.onQuickTransfer(stackAfterMoving, copyOfOriginalStack);
            } else if (slot == EasyCampfireEntity.INPUT_SLOT_INDEX) {
                if (!this.insertItem(stackAfterMoving, EasyCampfireEntity.OUTPUT_SLOT_INDEX + 1, TOTAL_SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slot > EasyCampfireEntity.OUTPUT_SLOT_INDEX && slot < START_OF_HOT_BAR_SLOTS) {
                if (this.isCookable(stackAfterMoving)) { // smelt it if possible
                    if (!this.insertItem(stackAfterMoving, EasyCampfireEntity.INPUT_SLOT_INDEX, EasyCampfireEntity.INPUT_SLOT_INDEX+1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else { // otherwise put it in hot bar
                    if (!this.insertItem(stackAfterMoving, START_OF_HOT_BAR_SLOTS, TOTAL_SLOTS, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else { // in the hotbar
                if (this.isCookable(stackAfterMoving)) { // smelt it if possible
                    if (!this.insertItem(stackAfterMoving, EasyCampfireEntity.INPUT_SLOT_INDEX, EasyCampfireEntity.INPUT_SLOT_INDEX+1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else { // otherwise put it in the rest of the inventory
                    if (!this.insertItem(stackAfterMoving, EasyCampfireEntity.OUTPUT_SLOT_INDEX + 1, START_OF_HOT_BAR_SLOTS, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (stackAfterMoving.isEmpty()) {
                slotBeingMoved.setStack(ItemStack.EMPTY);
            } else {
                slotBeingMoved.markDirty();
            }

            if (stackAfterMoving.getCount() == copyOfOriginalStack.getCount()) {
                return ItemStack.EMPTY; // fail safe
            }

            slotBeingMoved.onTakeItem(player, stackAfterMoving);
        }

        return copyOfOriginalStack;
    }

    protected boolean isCookable(ItemStack itemStack) {
        return this.world.getRecipeManager().getFirstMatch(RecipeType.CAMPFIRE_COOKING, new SingleStackRecipeInput(itemStack), this.world).isPresent();
    }

    public boolean isBurning() {
        //return this.propertyDelegate.get(0) > 0;
        return true;
    }

    public float getCookProgress() {
        int cookTime = this.propertyDelegate.get(EasyCampfireEntity.COOK_TIME_PROPERTY_INDEX);
        int cookTimeTotal = this.propertyDelegate.get(EasyCampfireEntity.COOK_TIME_TOTAL_PROPERTY_INDEX);
        return cookTimeTotal != 0 && cookTime != 0 ? MathHelper.clamp((float) cookTime / (float) cookTimeTotal, 0.0F, 1.0F) : 0.0F;
    }
}
