package worldgate.conqueror;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.entity.SpiderEntityRenderer;
import net.minecraft.client.render.entity.model.SpiderEntityModel;
import worldgate.conqueror.entity.*;
import worldgate.conqueror.mechanic.BlockBreakingHandler;
import worldgate.conqueror.mechanic.GrappleHandler;
import worldgate.conqueror.mechanic.ModStatusEffects;
import worldgate.conqueror.particle.ModParticles;

public class ModClient implements ClientModInitializer {


    @Override
    public void onInitializeClient() {
        BlockBreakingHandler blockBreakingHandler = new BlockBreakingHandler();
        blockBreakingHandler.registerEventCallback();
        blockBreakingHandler.registerNetworkingReceiver();
        GrappleHandler.registerNetworkingReceiver();

        GrappleHandler.init();

        ModEntities.registerClient();
        ModStatusEffects.registerClient();

        ModParticles.registerParticlesClient();
    }
}
