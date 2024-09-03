package worldgate.conqueror.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import worldgate.conqueror.mechanic.BlockBreakingHandler;
import worldgate.conqueror.mechanic.GrappleHandler;

public class ModNetworking {

    public static <T extends net.minecraft.network.packet.CustomPayload> void registerPayload(CustomPayload.Id<T> id, PacketCodec<PacketByteBuf,T> codec) {
        PayloadTypeRegistry.configurationC2S().register(id, codec);
        PayloadTypeRegistry.configurationS2C().register(id, codec);
        PayloadTypeRegistry.playC2S().register(id, codec);
        PayloadTypeRegistry.playS2C().register(id, codec);
    }

    public static void registerNetworking() {
        registerPayload(BlockBreakingHandler.BlockBreakingPayload.ID, BlockBreakingHandler.BlockBreakingPayload.CODEC);
        registerPayload(GrappleHandler.GrapplePayload.ID, GrappleHandler.GrapplePayload.CODEC);
    }
}
