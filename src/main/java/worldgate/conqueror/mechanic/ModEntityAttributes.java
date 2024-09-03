package worldgate.conqueror.mechanic;

import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import worldgate.conqueror.WorldgateConqueror;

public class ModEntityAttributes {
    public static final RegistryEntry.Reference<EntityAttribute> MAX_FOOD_ATTRIBUTE = register("max_food", 0.0, 0.0, 100.0);

    public static final RegistryEntry.Reference<EntityAttribute> BLUNT_RESISTANCE = register("blunt_resistance", 0.0, 0.0, 1024.0);
    public static final RegistryEntry.Reference<EntityAttribute> PIERCE_RESISTANCE = register("pierce_resistance", 0.0, 0.0, 1024.0);
    public static final RegistryEntry.Reference<EntityAttribute> SLASH_RESISTANCE = register("slash_resistance", 0.0, 0.0, 1024.0);
    public static final RegistryEntry.Reference<EntityAttribute> ARMOR_EFFICIENCY = register("armor_efficiency", 1.0, 0.0, 1024.0);

    public static final RegistryEntry.Reference<EntityAttribute> ITEM_SWITCH_SPEED = register("item_switch_speed", 4.0, 0.0, 1024.0);
    public static final RegistryEntry.Reference<EntityAttribute> WATER_MOVEMENT_SPEED = register("water_movement_speed", 1.0, 0.0, 1024.0);

    public static final RegistryEntry.Reference<EntityAttribute> DAMAGE_IMMUNITY_TIME = register("damage_immunity_time", 5.0, 0.0, 1024.0);
    public static final RegistryEntry.Reference<EntityAttribute> DODGE = register("dodge", 5.0, 0.0, 1024.0);

    // for saving throws and opposed rolls
    // loosely: str, wis, con from dnd 5e
    // see https://www.reddit.com/r/dndnext/comments/2yv4bj/5th_edition_saving_throws_and_skills_list/
    public static final RegistryEntry.Reference<EntityAttribute> STRENGTH = register("strength", 0.0, -1024.0, 1024.0);
    public static final RegistryEntry.Reference<EntityAttribute> MIND = register("mind", 0.0, -1024.0, 1024.0);
    public static final RegistryEntry.Reference<EntityAttribute> HARDINESS = register("hardiness", 0.0, -1024.0, 1024.0);
    public static final RegistryEntry.Reference<EntityAttribute> POISON_STRENGTH = register("poison_strength", 0.0, -1024.0, 1024.0);

    private static RegistryEntry.Reference<EntityAttribute> register(String id, double fallback, double min, double max) {
        return Registry.registerReference(Registries.ATTRIBUTE, Identifier.of(WorldgateConqueror.MOD_ID, id),
                new ClampedEntityAttribute("attribute.worldgate-conqueror." + id, fallback, min, max)
                        .setTracked(true)
        );
    }

    public static void registerAttributes() {
        WorldgateConqueror.LOGGER.info("Registering entity attributes for " + WorldgateConqueror.MOD_ID);
    }
}
