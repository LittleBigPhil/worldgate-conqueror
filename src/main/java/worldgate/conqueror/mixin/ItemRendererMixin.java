package worldgate.conqueror.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import worldgate.conqueror.item.component.ModComponents;
import worldgate.conqueror.item.component.ModularToolComponent;
import worldgate.conqueror.item.ModItems;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Inject(
            method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;III)V",
            at = @At("HEAD"),
            cancellable = true)
    public void renderItem(@Nullable LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, @Nullable World world, int light, int overlay, int seed, CallbackInfo ci) {
        if (!stack.isEmpty()) {
            if (stack.getItem() == ModItems.MODULAR_TOOL || stack.getItem() == ModItems.TOOL_HEAD) {
                ModularToolComponent toolComponent = stack.getOrDefault(ModComponents.TOOL_COMPONENT, ModularToolComponent.defaultValue());
                List<Item> items = getItemParts(toolComponent, stack.getItem() == ModItems.TOOL_HEAD);
                List<ItemStack> itemStacks = items.stream().map(item -> copyStackIntoItem(stack, item)).toList();
                for (ItemStack partStack : itemStacks) {
                    MinecraftClient.getInstance().getItemRenderer().renderItem(entity, partStack, renderMode, leftHanded, matrices, vertexConsumers, world, light, overlay, seed);
                }
                ci.cancel();
            }
        }
    }

    @Inject(
            method= "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    public void renderItem(ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
        List<Item> validItems = List.of(ModItems.MODULAR_TOOL, ModItems.TOOL_HEAD, ModItems.TOOL_HANDLE);
        if (validItems.contains(stack.getItem())) {

            ModularToolComponent toolComponent = stack.getOrDefault(ModComponents.TOOL_COMPONENT, ModularToolComponent.defaultValue());
            List<Item> items = getItemParts(toolComponent, stack.getItem() == ModItems.TOOL_HEAD);
            List<ItemStack> itemStacks = items.stream().map(item -> copyStackIntoItem(stack, item)).toList();

            // Get the world and entity from the context
            World world = MinecraftClient.getInstance().world;
            LivingEntity entity = MinecraftClient.getInstance().player;
            int seed = 0;

            for (ItemStack partStack : itemStacks) {
                BakedModel partModel = MinecraftClient.getInstance().getItemRenderer().getModel(partStack, world, entity, seed);
                MinecraftClient.getInstance().getItemRenderer().renderItem(partStack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, partModel);
            }

            ci.cancel();
        }
    }

    @Unique
    private List<Item> getItemParts(ModularToolComponent toolComponent, boolean isJustHead) {
        if (isJustHead) {
            return Stream.of(toolComponent.getHeadItemCentered())
                    .flatMap(Optional::stream).toList();
        } else {
            return Stream.of(
                    toolComponent.getHandleItem(),
                    toolComponent.getHeadItemAttached()
            ).flatMap(Optional::stream).toList();
        }
    }
    @Unique
    private static ItemStack copyStackIntoItem(ItemStack baseStack, Item item) {
        ItemStack newStack = new ItemStack(item);
        newStack.setCount(baseStack.getCount());
        newStack.applyComponentsFrom(baseStack.getComponents());
        return newStack;
    }
}