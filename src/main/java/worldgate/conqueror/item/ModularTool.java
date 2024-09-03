package worldgate.conqueror.item;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import worldgate.conqueror.item.component.ModComponents;
import worldgate.conqueror.item.component.ModularToolComponent;
import worldgate.conqueror.mechanic.DamageTypeDistribution;

import java.util.List;

public class ModularTool extends Item {
    public ModularTool(Settings settings) {
        super(settings);
    }

    @Override
    public Text getName(ItemStack stack) {
        ModularToolComponent toolComponent = stack.getOrDefault(ModComponents.TOOL_COMPONENT, ModularToolComponent.defaultValue());
        String prefix = toolComponent.handle().map(handle ->
                switch (handle) {
                    case ModularToolComponent.Handles.GRIP -> "Short ";
                    case ModularToolComponent.Handles.POLE -> "Pole ";
                    default -> "";
                }).orElse("");
        return Text.translatable(prefix + toolComponent.head().orElse("Tool"));
    }

    private boolean matchesBlock(ItemStack stack, BlockState state) {
        ModularToolComponent toolComponent = stack.getOrDefault(ModComponents.TOOL_COMPONENT, ModularToolComponent.defaultValue());
        return toolComponent.head()
                .flatMap( head -> ModularToolComponent.Heads.blockTagOf(head)
                        .map(state::isIn)
                ).orElse(false);
    }

    @Override
    public boolean isCorrectForDrops(ItemStack stack, BlockState state) {
        return matchesBlock(stack, state);
    }

    @Override
    public float getMiningSpeed(ItemStack stack, BlockState state) {
        if (matchesBlock(stack, state)) {
            return 3f;
        } else {
            return 1f;
        }
    }

    public static boolean isTwoHanded(ItemStack stack) {
        ModularToolComponent toolComponent = stack.getOrDefault(ModComponents.TOOL_COMPONENT, ModularToolComponent.defaultValue());
        return toolComponent.handle().map(handle ->
                switch(handle) {
                    case ModularToolComponent.Handles.POLE -> true;
                    default -> false;
                }
        ).orElse(false);
    }
    public static DamageTypeDistribution getDamageTypeDistribution(ItemStack stack) {
        ModularToolComponent toolComponent = stack.getOrDefault(ModComponents.TOOL_COMPONENT, ModularToolComponent.defaultValue());
        return toolComponent.getDamageTypeDistribution();
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);

        if (isTwoHanded(stack)) {
            tooltip.add(Text.literal("Two-Handed").formatted(Formatting.GRAY));
        }

        DamageTypeDistribution damageDistribution = getDamageTypeDistribution(stack);
        tooltip.add(Text.literal("Damage Type:").formatted(Formatting.GRAY));
        if (damageDistribution.blunt() > 0) {
            tooltip.add(Text.literal(String.format(" %.0f%% Blunt", damageDistribution.blunt() * 100)).formatted(Formatting.DARK_GRAY));
        }
        if (damageDistribution.pierce() > 0) {
            tooltip.add(Text.literal(String.format(" %.0f%% Pierce", damageDistribution.pierce() * 100)).formatted(Formatting.DARK_GRAY));
        }
        if (damageDistribution.slash() > 0) {
            tooltip.add(Text.literal(String.format(" %.0f%% Slash", damageDistribution.slash() * 100)).formatted(Formatting.DARK_GRAY));
        }
    }

}
