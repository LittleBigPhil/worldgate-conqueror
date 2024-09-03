package worldgate.conqueror.mechanic;

import fuzs.extensibleenums.api.v2.core.EnumAppender;
import fuzs.extensibleenums.api.v2.core.UnsafeExtensibleEnum;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.EquipmentSlot;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.function.Predicate;

public class ModEquipmentSlots {

    public static final int NUMBER_OF_ACCESSORY_SLOTS = 2;
    public static final EquipmentSlot ACCESSORY;
    private static final ArrayList<EquipmentSlot> ACCESSORY_EQUIPMENT_SLOTS = new ArrayList<>();
    public static boolean isAccessory(EquipmentSlot equipmentSlot) {
        return ACCESSORY_EQUIPMENT_SLOTS.contains(equipmentSlot);
    }
    public static final AttributeModifierSlot ACCESSORY_ATTRIBUTE_SLOT;

    static {
        var equipmentMixin = EnumAppender.create(EquipmentSlot.class,
                        0, EquipmentSlot.Type.class,
                        1, int.class,
                        2, int.class,
                        3, int.class,
                        0, String.class
                );
        for (int i = 0; i < NUMBER_OF_ACCESSORY_SLOTS; i++) {
            equipmentMixin.addEnumConstant(MessageFormat.format("ACCESSORY{0}", i),
                    EquipmentSlot.Type.HUMANOID_ARMOR, 4 + i, 1, 7 + i, MessageFormat.format("accessory{0}", i)
            );
        }
        equipmentMixin.applyTo();
        //BuiltInEnumFactoriesImpl.testEnumValueAddition(EquipmentSlot.class, "ACCESSORY");

        for (int i = 0; i < NUMBER_OF_ACCESSORY_SLOTS; i++) {
            ACCESSORY_EQUIPMENT_SLOTS.add(EquipmentSlot.valueOf(MessageFormat.format("ACCESSORY{0}",i)));
        }
        ACCESSORY = ACCESSORY_EQUIPMENT_SLOTS.getFirst();

        Predicate<EquipmentSlot> isAccessoryPredicate = ModEquipmentSlots::isAccessory;

        EnumAppender.create(AttributeModifierSlot.class,
                0, int.class,
                0, String.class
                //0, Predicate.class
        ).addEnumConstant("ACCESSORY",10, "accessory").applyTo();
        var field = findField(AttributeModifierSlot.class, 0, Predicate.class);
        ACCESSORY_ATTRIBUTE_SLOT = AttributeModifierSlot.valueOf("ACCESSORY");
        UnsafeExtensibleEnum.setObjectField(field, ACCESSORY_ATTRIBUTE_SLOT, isAccessoryPredicate);
    }

    public static void register() {

    }

    // Copied from extensibleenums
    private static Field findField(Class<? extends Enum<?>> enumClazz, int ordinal, Class<?> clazz) {
        for (Field field : enumClazz.getDeclaredFields()) {
            if (field.getType() == clazz && ordinal-- == 0) return field;
        }
        throw new IllegalStateException("No field of type %s found at ordinal %s in enum class %s".formatted(clazz, ordinal, enumClazz));
    }
}
