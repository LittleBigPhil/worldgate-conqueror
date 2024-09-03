package worldgate.conqueror.item.component;

import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import worldgate.conqueror.WorldgateConqueror;

public class ModComponents {

    public static final ComponentType<ModularToolComponent> TOOL_COMPONENT = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(WorldgateConqueror.MOD_ID, "tool_component"),
            ComponentType.<ModularToolComponent>builder().codec(ModularToolComponent.CODEC).build()
    );

    public static void registerModComponents() {
        WorldgateConqueror.LOGGER.info("Registering {} components", WorldgateConqueror.MOD_ID);
    }
}
