package worldgate.conqueror.entity;

import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public class CampfireRecipeBookScreen extends RecipeBookWidget {
    private static final Text TOGGLE_CAMPFIREABLE_RECIPES_TEXT = Text.translatable("gui.worlgate-conqueror.recipebook.toggleRecipes.campfireable");
    private static final ButtonTextures TEXTURES = new ButtonTextures(
            Identifier.ofVanilla("recipe_book/furnace_filter_enabled"),
            Identifier.ofVanilla("recipe_book/furnace_filter_disabled"),
            Identifier.ofVanilla("recipe_book/furnace_filter_enabled_highlighted"),
            Identifier.ofVanilla("recipe_book/furnace_filter_disabled_highlighted")
    );

    @Override
    protected Text getToggleCraftableButtonText() {
        return TOGGLE_CAMPFIREABLE_RECIPES_TEXT;
    }
    @Override
    protected void setBookButtonTexture() {
        this.toggleCraftableButton.setTextures(TEXTURES);
    }

    @Override
    public void slotClicked(@Nullable Slot slot) {
        super.slotClicked(slot);
        if (slot != null && slot.id < this.craftingScreenHandler.getCraftingSlotCount()) {
            this.ghostSlots.reset();
        }
    }
    @Override
    public void showGhostRecipe(RecipeEntry<?> recipe, List<Slot> slots) {
        ItemStack resultStack = recipe.value().getResult(this.client.world.getRegistryManager());
        this.ghostSlots.setRecipe(recipe);
        this.ghostSlots.addSlot(Ingredient.ofStacks(resultStack), ((Slot)slots.get(EasyCampfireEntity.OUTPUT_SLOT_INDEX)).x, ((Slot)slots.get(EasyCampfireEntity.OUTPUT_SLOT_INDEX)).y);

        DefaultedList<Ingredient> defaultedList = recipe.value().getIngredients();
        Iterator<Ingredient> iterator = defaultedList.iterator();
        for (int i = 0; i < 1; i++) {
            if (!iterator.hasNext()) {
                return;
            }

            Ingredient ingredient = (Ingredient)iterator.next();
            if (!ingredient.isEmpty()) {
                Slot slot2 = (Slot)slots.get(i);
                this.ghostSlots.addSlot(ingredient, slot2.x, slot2.y);
            }
        }
    }
}
