package worldgate.conqueror.mixin;

import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.client.recipebook.RecipeBookGroup;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.book.RecipeBookOptions;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(RecipeBookCategory.class)
public class RecipeBookCategoryMixin {
    @Shadow @Final @Mutable private static RecipeBookCategory[] field_25767;

    @Unique private static final RecipeBookCategory CAMPFIRE = addVariant("CAMPFIRE");

    @Invoker("<init>")
    private static RecipeBookCategory invokeInit(String internalName, int internalId) {
        throw new AssertionError();
    }

    @Unique
    private static RecipeBookCategory addVariant(String name) {
        ArrayList<RecipeBookCategory> variants = new ArrayList<>(Arrays.asList(RecipeBookCategoryMixin.field_25767));
        RecipeBookCategory category = invokeInit(name, variants.getLast().ordinal() + 1);
        variants.add(category);
        RecipeBookCategoryMixin.field_25767 = variants.toArray(new RecipeBookCategory[0]);
        return category;
    }

    // Use the following code to find what the obfuscated field name that contains the array of values is.
    /*@Inject(method = "<clinit>", at = @At("TAIL"))
    private static void findObfuscatedField(CallbackInfo info) {
        try {
            Class<RecipeBookCategory> enumClass = RecipeBookCategory.class;
            Field[] declaredFields = enumClass.getDeclaredFields();
            for (Field field : declaredFields) {
                if (Modifier.isStatic(field.getModifiers())) {
                    System.out.println(field.getName() + ": " + field.getType());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to find obfuscated field.", e);
        }
    }*/
}
