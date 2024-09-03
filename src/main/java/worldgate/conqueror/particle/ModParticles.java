package worldgate.conqueror.particle;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.particle.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import worldgate.conqueror.WorldgateConqueror;

import java.util.function.Function;

public class ModParticles {

    public static final ParticleType<EntityEffectParticleEffect> BreathWeaponParticle = register(
            "breath_weapon", false, EntityEffectParticleEffect::createCodec, EntityEffectParticleEffect::createPacketCodec
    );

    // Based on ParticleTypes.register, but with the correct identifier.
    private static <T extends ParticleEffect> ParticleType<T> register(
            String name,
            boolean alwaysShow,
            Function<ParticleType<T>, MapCodec<T>> codecGetter,
            Function<ParticleType<T>, PacketCodec<? super RegistryByteBuf, T>> packetCodecGetter
    ) {
        return Registry.register(Registries.PARTICLE_TYPE, Identifier.of(WorldgateConqueror.MOD_ID, name), new ParticleType<T>(alwaysShow) {
            @Override
            public MapCodec<T> getCodec() {
                return (MapCodec<T>)codecGetter.apply(this);
            }

            @Override
            public PacketCodec<? super RegistryByteBuf, T> getPacketCodec() {
                return (PacketCodec<? super RegistryByteBuf, T>)packetCodecGetter.apply(this);
            }
        });
    }

    public static void registerParticles() {
    }
    public static void registerParticlesClient() {
        ParticleFactoryRegistry.getInstance().register(BreathWeaponParticle, StationaryEffectParticle.ColoredFactory::new);
    }
}
