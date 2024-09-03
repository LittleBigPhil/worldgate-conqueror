package worldgate.conqueror.mixin;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.FogShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import worldgate.conqueror.mechanic.ModStatusEffects;

import java.util.List;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {
    @Shadow private static final List<BackgroundRenderer.StatusEffectFogModifier> FOG_MODIFIERS =
            Lists.newArrayList(new BackgroundRenderer.StatusEffectFogModifier[]{
                    new ModStatusEffects.DarknessFogModifier()
            });


    @Shadow private static BackgroundRenderer.StatusEffectFogModifier getFogModifier(Entity entity, float tickDelta) {return null;};

    @Overwrite
    public static void applyFog(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float tickDelta) {
        CameraSubmersionType cameraSubmersionType = camera.getSubmersionType();
        Entity entity = camera.getFocusedEntity();
        BackgroundRenderer.FogData fogData = new BackgroundRenderer.FogData(fogType);
        BackgroundRenderer.StatusEffectFogModifier statusEffectFogModifier = getFogModifier(entity, tickDelta);
        if (cameraSubmersionType == CameraSubmersionType.LAVA) {
            if (entity.isSpectator()) {
                fogData.fogStart = -8.0F;
                fogData.fogEnd = viewDistance * 0.5F;
            } else if (entity instanceof LivingEntity && ((LivingEntity)entity).hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
                fogData.fogStart = 0.0F;
                fogData.fogEnd = 15.0F; // changed from 5
            } else {
                fogData.fogStart = 0.25F;
                fogData.fogEnd = 5.0F; // changed from 1
            }
        } else if (cameraSubmersionType == CameraSubmersionType.POWDER_SNOW) {
            if (entity.isSpectator()) {
                fogData.fogStart = -8.0F;
                fogData.fogEnd = viewDistance * 0.5F;
            } else {
                fogData.fogStart = 0.0F;
                fogData.fogEnd = 2.0F;
            }
        } else if (statusEffectFogModifier != null) {
            LivingEntity livingEntity = (LivingEntity)entity;
            StatusEffectInstance statusEffectInstance = livingEntity.getStatusEffect(statusEffectFogModifier.getStatusEffect());
            //if (statusEffectInstance != null) {
                statusEffectFogModifier.applyStartEndModifier(fogData, livingEntity, statusEffectInstance, viewDistance, tickDelta);
            //}
        } else if (cameraSubmersionType == CameraSubmersionType.WATER) {
            fogData.fogStart = -8.0F;
            fogData.fogEnd = 96.0F;
            if (entity instanceof ClientPlayerEntity) {
                ClientPlayerEntity clientPlayerEntity = (ClientPlayerEntity)entity;
                fogData.fogEnd *= Math.max(0.25F, clientPlayerEntity.getUnderwaterVisibility());
                RegistryEntry<Biome> registryEntry = clientPlayerEntity.getWorld().getBiome(clientPlayerEntity.getBlockPos());
                if (registryEntry.isIn(BiomeTags.HAS_CLOSER_WATER_FOG)) {
                    fogData.fogEnd *= 0.85F;
                }
            }

            if (fogData.fogEnd > viewDistance) {
                fogData.fogEnd = viewDistance;
                fogData.fogShape = FogShape.CYLINDER;
            }
        } else if (thickFog) {
            fogData.fogStart = viewDistance * 0.05F;
            fogData.fogEnd = Math.min(viewDistance, 192.0F) * 0.5F;
        } else if (fogType == BackgroundRenderer.FogType.FOG_SKY) {
            fogData.fogStart = 0.0F;
            fogData.fogEnd = viewDistance;
            fogData.fogShape = FogShape.CYLINDER;
        } else {
            float f = MathHelper.clamp(viewDistance / 10.0F, 4.0F, 64.0F);
            fogData.fogStart = viewDistance - f;
            fogData.fogEnd = viewDistance;
            fogData.fogShape = FogShape.CYLINDER;
        }

        RenderSystem.setShaderFogStart(fogData.fogStart);
        RenderSystem.setShaderFogEnd(fogData.fogEnd);
        RenderSystem.setShaderFogShape(fogData.fogShape);
    }

}
