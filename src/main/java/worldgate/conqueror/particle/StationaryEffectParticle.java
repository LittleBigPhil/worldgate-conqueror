package worldgate.conqueror.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.EntityEffectParticleEffect;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

// Based on SpellParticle, but without the ascending and gravity
public class StationaryEffectParticle extends SpriteBillboardParticle {
    private static final Random RANDOM = Random.create();
    private final SpriteProvider spriteProvider;
    private float defaultAlpha = 1.0F;

    StationaryEffectParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
        super(world, x, y, z, 0.5 - RANDOM.nextDouble(), velocityY, 0.5 - RANDOM.nextDouble());
        this.velocityMultiplier = 0.96F;
        this.gravityStrength = 0F;
        this.ascending = false;
        this.spriteProvider = spriteProvider;
        this.velocityY *= 0.2F;
        if (velocityX == 0.0 && velocityZ == 0.0) {
            this.velocityX *= 0.1F;
            this.velocityZ *= 0.1F;
        }

        this.scale *= 0.75F;
        this.maxAge = (int)(8.0 / (Math.random() * 0.8 + 0.2));
        this.collidesWithWorld = false;
        this.setSpriteForAge(spriteProvider);
        if (this.isInvisible()) {
            this.setAlpha(0.0F);
        }
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteForAge(this.spriteProvider);
        if (this.isInvisible()) {
            this.alpha = 0.0F;
        } else {
            this.alpha = MathHelper.lerp(0.05F, this.alpha, this.defaultAlpha);
        }
    }

    @Override
    protected void setAlpha(float alpha) {
        super.setAlpha(alpha);
        this.defaultAlpha = alpha;
    }

    private boolean isInvisible() {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        ClientPlayerEntity clientPlayerEntity = minecraftClient.player;
        return clientPlayerEntity != null
                && clientPlayerEntity.getEyePos().squaredDistanceTo(this.x, this.y, this.z) <= 9.0
                && minecraftClient.options.getPerspective().isFirstPerson()
                && clientPlayerEntity.isUsingSpyglass();
    }

    // Maybe I'll use this later?
    @Environment(EnvType.CLIENT)
    public static class DefaultFactory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public DefaultFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(SimpleParticleType simpleParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
            return new StationaryEffectParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
        }
    }

    @Environment(EnvType.CLIENT)
    public static class ColoredFactory implements ParticleFactory<EntityEffectParticleEffect> {
        private final SpriteProvider spriteProvider;

        public ColoredFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(
                EntityEffectParticleEffect entityEffectParticleEffect, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i
        ) {
            Particle particle = new StationaryEffectParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider){};
            particle.setColor(entityEffectParticleEffect.getRed(), entityEffectParticleEffect.getGreen(), entityEffectParticleEffect.getBlue());
            //particle.setAlpha(entityEffectParticleEffect.getAlpha());
            return particle;
        }
    }
}
