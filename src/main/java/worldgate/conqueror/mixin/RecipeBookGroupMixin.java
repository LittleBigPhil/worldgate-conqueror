package worldgate.conqueror.mixin;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.recipebook.RecipeBookGroup;
import net.minecraft.recipe.book.RecipeBookCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(RecipeBookGroup.class)
public class RecipeBookGroupMixin {
    @Inject(method = "getGroups", at = @At("HEAD"), cancellable = true)
    private static void getGroups(RecipeBookCategory category, CallbackInfoReturnable<List<RecipeBookGroup>> cir) {
        if (category == RecipeBookCategory.valueOf("CAMPFIRE")) {
            cir.setReturnValue(ImmutableList.of(RecipeBookGroup.CAMPFIRE));
        }
    }
}
