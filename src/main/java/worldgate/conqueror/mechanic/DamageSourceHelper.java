package worldgate.conqueror.mechanic;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import worldgate.conqueror.WorldgateConqueror;
import worldgate.conqueror.item.ModItems;
import worldgate.conqueror.item.ModularTool;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DamageSourceHelper {
    private static final Map<RegistryKey<DamageType>, DamageTypeDistribution> damageTypeMap = new HashMap<>();

    static {
        registerDamageType(Identifier.ofVanilla("player_attack"), DamageTypeDistribution.BLUNT); // punch (rest should be caught earlier)
        registerDamageType(Identifier.ofVanilla("mob_attack"), DamageTypeDistribution.SLASH); // most mobs' melee (notable weirdness is ender dragon's push is included)

        registerDamageType(Identifier.ofVanilla("arrow"), DamageTypeDistribution.PIERCE); // skeleton
        registerDamageType(Identifier.ofVanilla("trident"), DamageTypeDistribution.PIERCE); // duh
        registerDamageType(Identifier.ofVanilla("explosion"), DamageTypeDistribution.BLUNT); // creeper

        registerDamageType(Identifier.of(WorldgateConqueror.MOD_ID, "blunt"), DamageTypeDistribution.BLUNT);
        registerDamageType(Identifier.of(WorldgateConqueror.MOD_ID, "pierce"), DamageTypeDistribution.PIERCE);
        registerDamageType(Identifier.of(WorldgateConqueror.MOD_ID, "slash"), DamageTypeDistribution.SLASH);
    }

    private static void registerDamageType(Identifier id, DamageTypeDistribution distribution) {
        damageTypeMap.put(RegistryKey.of(RegistryKeys.DAMAGE_TYPE, id), distribution);
    }

    public static Optional<DamageTypeDistribution> getDamageDistribution(DamageSource source) {
        if (source.getSource() != null && source.getSource() instanceof LivingEntity livingSource) {
            ItemStack mainHand = livingSource.getMainHandStack();
            if (mainHand.getItem() == ModItems.MODULAR_TOOL) {
                return Optional.of(ModularTool.getDamageTypeDistribution(mainHand));
            }
        }

        Optional<RegistryKey<DamageType>> optKey = source.getTypeRegistryEntry().getKey();
        return optKey.flatMap(key -> {
            if (damageTypeMap.containsKey(key)) {
                return Optional.of(damageTypeMap.get(key));
            } else {
                return Optional.empty();
            }
        });
    }
}