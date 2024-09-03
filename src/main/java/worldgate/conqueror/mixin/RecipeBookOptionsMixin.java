package worldgate.conqueror.mixin;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.book.RecipeBookOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(RecipeBookOptions.class)
public class RecipeBookOptionsMixin {
    @Shadow
    private static final Map<RecipeBookCategory, Pair<String, String>> CATEGORY_OPTION_NAMES = ImmutableMap.of(
            RecipeBookCategory.CRAFTING,
            Pair.of("isGuiOpen", "isFilteringCraftable"),
            RecipeBookCategory.FURNACE,
            Pair.of("isFurnaceGuiOpen", "isFurnaceFilteringCraftable"),
            RecipeBookCategory.BLAST_FURNACE,
            Pair.of("isBlastingFurnaceGuiOpen", "isBlastingFurnaceFilteringCraftable"),
            RecipeBookCategory.SMOKER,
            Pair.of("isSmokerGuiOpen", "isSmokerFilteringCraftable"),
            RecipeBookCategory.valueOf("CAMPFIRE"),
            Pair.of("isCampfireGuiOpen", "isCampfireFilteringCraftable")
    );
}
