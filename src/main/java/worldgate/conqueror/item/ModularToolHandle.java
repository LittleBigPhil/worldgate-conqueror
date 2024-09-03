package worldgate.conqueror.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import worldgate.conqueror.item.component.ModComponents;
import worldgate.conqueror.item.component.ModularToolComponent;

public class ModularToolHandle extends Item {
    public ModularToolHandle(Item.Settings settings) {
        super(settings);
    }

    public Text getName(ItemStack stack) {
        ModularToolComponent toolComponent = stack.getOrDefault(ModComponents.TOOL_COMPONENT, ModularToolComponent.defaultValue());
        return Text.translatable(toolComponent.handle().orElse("Tool Handle"));
    }
}
