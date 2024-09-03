package worldgate.conqueror.recipe;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import worldgate.conqueror.item.component.ModComponents;
import worldgate.conqueror.item.component.ModularToolComponent;

public class ModularRecipe extends ShapedRecipe {
    public ModularRecipe(String group, CraftingRecipeCategory category, RawShapedRecipe raw, ItemStack result, boolean showNotification) {
        super(group, category, raw, result, showNotification);
        this.group = group;
        this.category = category;
        this.raw = raw;
        this.result = result;
        this.showNotification = showNotification;
    }

    final RawShapedRecipe raw;
    final ItemStack result;
    final String group;
    final CraftingRecipeCategory category;
    final boolean showNotification;

    public ModularRecipe(String group, CraftingRecipeCategory category, RawShapedRecipe raw, ItemStack result) {
        this(group, category, raw, result, true);
    }

    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.MODULAR_RECIPE_SERIALIZER;
    }

    public String getGroup() {
        return this.group;
    }

    public CraftingRecipeCategory getCategory() {
        return this.category;
    }

    public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
        return this.result;
    }

    public DefaultedList<Ingredient> getIngredients() {
        return this.raw.getIngredients();
    }

    public boolean showNotification() {
        return this.showNotification;
    }

    public boolean fits(int width, int height) {
        return width >= this.raw.getWidth() && height >= this.raw.getHeight();
    }

    public boolean matches(CraftingRecipeInput craftingRecipeInput, World world) {
        return this.raw.matches(craftingRecipeInput);
    }

    public ItemStack craft(CraftingRecipeInput craftingRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup) {
        ItemStack craftedResult = this.getResult(wrapperLookup).copy();
        ModularToolComponent startingComponent = craftedResult.getOrDefault(ModComponents.TOOL_COMPONENT, ModularToolComponent.defaultValue());
        for (ItemStack stack : craftingRecipeInput.getStacks()) {
            ModularToolComponent ingredientComponent = stack.get(ModComponents.TOOL_COMPONENT);
            startingComponent = startingComponent.add(ingredientComponent);
        }
        craftedResult.set(ModComponents.TOOL_COMPONENT, startingComponent);
        craftedResult.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, startingComponent.calculateAttributes());
        return craftedResult;
    }

    public int getWidth() {
        return this.raw.getWidth();
    }

    public int getHeight() {
        return this.raw.getHeight();
    }

    public boolean isEmpty() {
        DefaultedList<Ingredient> defaultedList = this.getIngredients();
        return defaultedList.isEmpty() || defaultedList.stream().filter((ingredient) -> {
            return !ingredient.isEmpty();
        }).anyMatch((ingredient) -> {
            return ingredient.getMatchingStacks().length == 0;
        });
    }

    public static class Serializer implements RecipeSerializer<ModularRecipe> {
        public static final MapCodec<ModularRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(Codec.STRING.optionalFieldOf("group", "").forGetter((recipe) -> {
                return recipe.group;
            }), CraftingRecipeCategory.CODEC.fieldOf("category").orElse(CraftingRecipeCategory.MISC).forGetter((recipe) -> {
                return recipe.category;
            }), RawShapedRecipe.CODEC.forGetter((recipe) -> {
                return recipe.raw;
            }), ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter((recipe) -> {
                return recipe.result;
            }), Codec.BOOL.optionalFieldOf("show_notification", true).forGetter((recipe) -> {
                return recipe.showNotification;
            })).apply(instance, ModularRecipe::new);
        });
        public static final PacketCodec<RegistryByteBuf, ModularRecipe> PACKET_CODEC = PacketCodec.ofStatic(ModularRecipe.Serializer::write, ModularRecipe.Serializer::read);

        public Serializer() {
        }

        public MapCodec<ModularRecipe> codec() {
            return CODEC;
        }

        public PacketCodec<RegistryByteBuf, ModularRecipe> packetCodec() {
            return PACKET_CODEC;
        }

        private static ModularRecipe read(RegistryByteBuf buf) {
            String string = buf.readString();
            CraftingRecipeCategory craftingRecipeCategory = (CraftingRecipeCategory)buf.readEnumConstant(CraftingRecipeCategory.class);
            RawShapedRecipe rawShapedRecipe = (RawShapedRecipe)RawShapedRecipe.PACKET_CODEC.decode(buf);
            ItemStack itemStack = (ItemStack)ItemStack.PACKET_CODEC.decode(buf);
            boolean bl = buf.readBoolean();
            return new ModularRecipe(string, craftingRecipeCategory, rawShapedRecipe, itemStack, bl);
        }

        private static void write(RegistryByteBuf buf, ModularRecipe recipe) {
            buf.writeString(recipe.group);
            buf.writeEnumConstant(recipe.category);
            RawShapedRecipe.PACKET_CODEC.encode(buf, recipe.raw);
            ItemStack.PACKET_CODEC.encode(buf, recipe.result);
            buf.writeBoolean(recipe.showNotification);
        }
    }

}
