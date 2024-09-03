package worldgate.conqueror.entity;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import worldgate.conqueror.WorldgateConqueror;
import worldgate.conqueror.block.ModBlocks;

public class ModEntities {

    public static final EntityType<TextDisplayEntity> TEXT_DISPLAY = registerEntityType(
            "text_display",
            EntityType.Builder.create(new TextDisplayEntity.Factory(), SpawnGroup.MISC)
                    .dimensions(.75f, .75f)
                    .trackingTickInterval(2)
                    .maxTrackingRange(32)
                    .build()
    );
    public static final EntityType<BreathWeaponProjectileEntity> FLAME_PROJECTILE_ENTITY_TYPE = registerEntityType(
            "flame_projectile",
            EntityType.Builder.<BreathWeaponProjectileEntity>create(BreathWeaponProjectileEntity::new, SpawnGroup.MISC)
                    .dimensions(0.25F, 0.25F) // Small flame projectile size
                    .trackingTickInterval(10)
                    .maxTrackingRange(64)
                    .build()
    );
    public static final BlockEntityType<EasyCampfireEntity> CAMPFIRE_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier.of(WorldgateConqueror.MOD_ID, "campfire"),
            BlockEntityType.Builder.create(EasyCampfireEntity::new, ModBlocks.CAMPFIRE).build()
    );
    public static final ScreenHandlerType<CampfireScreenHandler> CAMPFIRE_SCREEN_HANDLER = Registry.register(
            Registries.SCREEN_HANDLER,
            Identifier.of(WorldgateConqueror.MOD_ID, "campfire"),
            new ScreenHandlerType<>(CampfireScreenHandler::new, FeatureFlags.VANILLA_FEATURES)
    );



    public static final EntityType<SpiderVariantEntity> PLAIN_SPIDER = registerEntityType(
            "plain_spider",
            SpiderVariantEntity.entityType(SpiderVariantEntity.PoisonType.NONE)
    );public static final EntityType<SpiderVariantEntity> NEUROTOXIC_SPIDER = registerEntityType(
            "neurotoxic_spider",
            SpiderVariantEntity.entityType(SpiderVariantEntity.PoisonType.NEUROTOXIC)
    );public static final EntityType<SpiderVariantEntity> HEMORRHAGIC_SPIDER = registerEntityType(
            "hemorrhagic_spider",
            SpiderVariantEntity.entityType(SpiderVariantEntity.PoisonType.HEMORRHAGIC)
    );public static final EntityType<SpiderVariantEntity> DEBILITATING_SPIDER = registerEntityType(
            "debilitating_spider",
            SpiderVariantEntity.entityType(SpiderVariantEntity.PoisonType.DEBILITATING)
    );

    static {
        FabricDefaultAttributeRegistry.register(PLAIN_SPIDER, SpiderEntity.createSpiderAttributes());
        FabricDefaultAttributeRegistry.register(NEUROTOXIC_SPIDER, SpiderEntity.createSpiderAttributes());
        FabricDefaultAttributeRegistry.register(HEMORRHAGIC_SPIDER, SpiderEntity.createSpiderAttributes());
        FabricDefaultAttributeRegistry.register(DEBILITATING_SPIDER, SpiderEntity.createSpiderAttributes());
    }
    //public static final EntityModelLayer SPIDER_LAYER = new EntityModelLayer(Identifier.of(WorldgateConqueror.MOD_ID, "spider"), "main");



    private static <T extends Entity> EntityType<T> registerEntityType(String identifier, EntityType<T> type) {
        return Registry.register(
                Registries.ENTITY_TYPE,
                Identifier.of(WorldgateConqueror.MOD_ID, identifier),
                type
        );
    }


    public static void registerEntities() {
        WorldgateConqueror.LOGGER.info("Registering entities for {}", WorldgateConqueror.MOD_ID);
    }
    public static void registerClient() {
        EntityRendererRegistry.register(ModEntities.TEXT_DISPLAY, TextDisplayEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.FLAME_PROJECTILE_ENTITY_TYPE, FlyingItemEntityRenderer::new);

        HandledScreens.register(ModEntities.CAMPFIRE_SCREEN_HANDLER, CampfireScreen::new);

        EntityRendererRegistry.register(ModEntities.PLAIN_SPIDER, SpiderVariantEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.NEUROTOXIC_SPIDER, SpiderVariantEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.HEMORRHAGIC_SPIDER, SpiderVariantEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.DEBILITATING_SPIDER, SpiderVariantEntityRenderer::new);
        //SpiderEntityRenderer
        //EntityModelLayerRegistry.registerModelLayer(ModEntities.SPIDER_LAYER, SpiderEntityModel::getTexturedModelData);
    }
}
