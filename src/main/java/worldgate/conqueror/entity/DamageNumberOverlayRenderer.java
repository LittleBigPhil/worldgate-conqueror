package worldgate.conqueror.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import worldgate.conqueror.entity.TextDisplayEntity;

public class DamageNumberOverlayRenderer {
    private final MinecraftClient client;

    public DamageNumberOverlayRenderer(MinecraftClient client) {
        this.client = client;
    }

    public void render(MatrixStack matrices, float tickDelta) {
        if (client.world == null || client.player == null) return;

        for (Entity entity : client.world.getEntities()) {
            if (entity instanceof TextDisplayEntity) {
                renderDamageNumber((TextDisplayEntity) entity, matrices, tickDelta);
            }
        }
    }

    private void renderDamageNumber(TextDisplayEntity entity, MatrixStack matrices, float tickDelta) {
        EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
        Vec3d cameraPos = dispatcher.camera.getPos();

        double x = entity.prevX + (entity.getX() - entity.prevX) * tickDelta - cameraPos.x;
        double y = entity.prevY + (entity.getY() - entity.prevY) * tickDelta - cameraPos.y;
        double z = entity.prevZ + (entity.getZ() - entity.prevZ) * tickDelta - cameraPos.z;

        matrices.push();
        matrices.translate(x, y + entity.getHeight() + 0.5f, z);
        //matrices.multiply(dispatcher.getRotation());
        matrices.multiply(dispatcher.getRotation().rotateY((float) Math.PI));
        matrices.scale(-0.025f, -0.025f, 0.025f);

        Text text = entity.getDisplayText();
        float opacity = Math.min(1.0f, entity.getLifetime() / 10.0f);
        int color = 0xFF0000 | (((int) (opacity * 255.0f)) << 24);

        TextRenderer textRenderer = client.textRenderer;
        float textWidth = textRenderer.getWidth(text);


        VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();

        // Render outline
        //textRenderer.draw(text, -textWidth / 2f, 0, 0x000000, false, matrices.peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
        //textRenderer.draw(text, -textWidth / 2f + 1, 0, 0x000000, false, matrices.peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
        //textRenderer.draw(text, -textWidth / 2f, -1, 0x000000, false, matrices.peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
        //textRenderer.draw(text, -textWidth / 2f, 1, 0x000000, false, matrices.peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);

        // Render main text
        textRenderer.draw(text, -textWidth / 2f, 0, color, false, matrices.peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);

        immediate.draw();

        matrices.pop();
    }
}