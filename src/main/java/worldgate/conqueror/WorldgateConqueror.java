package worldgate.conqueror;

import fuzs.extensibleenums.api.v2.core.EnumAppender;
import fuzs.extensibleenums.api.v2.core.UnsafeExtensibleEnum;
import fuzs.extensibleenums.impl.BuiltInEnumFactoriesImpl;
import fuzs.extensibleenums.impl.ExtensibleEnums;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import worldgate.conqueror.block.ModBlocks;
import worldgate.conqueror.item.component.ModComponents;
import worldgate.conqueror.entity.ModEntities;
import worldgate.conqueror.item.ModItems;
import worldgate.conqueror.mechanic.ModEntityAttributes;
import worldgate.conqueror.mechanic.ModEquipmentSlots;
import worldgate.conqueror.mechanic.ModStatusEffects;
import worldgate.conqueror.network.ModNetworking;
import worldgate.conqueror.particle.ModParticles;
import worldgate.conqueror.recipe.ModRecipes;
import worldgate.conqueror.worldgen.ModDensityFunctions;
import worldgate.conqueror.worldgen.ModFeatures;
import worldgate.conqueror.worldgen.ModMaterialRules;

public class WorldgateConqueror implements ModInitializer {
	public static final String MOD_ID = "worldgate-conqueror";
    public static final Logger LOGGER = LoggerFactory.getLogger("worldgate-conqueror");


	@Override
	public void onInitialize() {
		ModComponents.registerModComponents();
		ModBlocks.registerModBlocks();
		ModItems.registerModItems();
		WorldgatePortals.registerPortals();
		ModRecipes.registerRecipes();
		ModMaterialRules.registerRules();

		ModDensityFunctions.registerDensityFunctions();
		ModFeatures.registerFeatures();

		ModNetworking.registerNetworking();

		ModEntityAttributes.registerAttributes();
		ModStatusEffects.registerEffects();
		ModEntities.registerEntities();

		ModParticles.registerParticles();

		EntitySleepEvents.ALLOW_SLEEP_TIME.register((player, sleepingPosition, vanillaResult) -> ActionResult.SUCCESS);
	}
}