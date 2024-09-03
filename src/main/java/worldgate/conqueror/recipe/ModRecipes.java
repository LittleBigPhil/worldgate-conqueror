package worldgate.conqueror.recipe;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.util.Identifier;
import worldgate.conqueror.WorldgateConqueror;

public class ModRecipes {
    public static RecipeSerializer<?> MODULAR_RECIPE_SERIALIZER = Registry.register(Registries.RECIPE_SERIALIZER, Identifier.of(WorldgateConqueror.MOD_ID, "modular_recipe"), new ModularRecipe.Serializer());

    public static void registerRecipes() {
        WorldgateConqueror.LOGGER.info("Registering mod recipe types for " + WorldgateConqueror.MOD_ID);
    }
}
