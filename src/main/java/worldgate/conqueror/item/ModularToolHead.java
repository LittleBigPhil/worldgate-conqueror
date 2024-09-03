package worldgate.conqueror.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import worldgate.conqueror.item.component.ModComponents;
import worldgate.conqueror.item.component.ModularToolComponent;

public class ModularToolHead extends Item {
    public ModularToolHead(Settings settings) {
        super(settings);
    }

    public Text getName(ItemStack stack) {
        ModularToolComponent toolComponent = stack.getOrDefault(ModComponents.TOOL_COMPONENT, ModularToolComponent.defaultValue());
        return Text.translatable(toolComponent.head().orElse("Tool") + " Head");
    }
}
