package worldgate.conqueror.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public class TextDisplayEntityRenderer extends EntityRenderer<TextDisplayEntity> {
    /*private static class CustomRenderLayer extends RenderLayer {
        public static final RenderLayer ALWAYS_ON_TOP_LAYER = RenderLayer.of("always_on_top",
                VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
                VertexFormat.DrawMode.QUADS, 256, false, true,
                RenderLayer.MultiPhaseParameters.builder()
                        .texture(new RenderPhase.Texture(Identifier.ofVanilla("minecraft:textures/font/ascii.png"), false, false))
                        .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                        .depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
                        .cull(RenderPhase.DISABLE_CULLING)
                        .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                        .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                        .build(false));

        public CustomRenderLayer(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
            super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
        }
    }
    private static final RenderLayer ALWAYS_ON_TOP_LAYER = CustomRenderLayer.ALWAYS_ON_TOP_LAYER;*/

    public TextDisplayEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(TextDisplayEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        //matrices.translate(0, 0 + entity.getHeight(), 0);
        matrices.multiply(this.dispatcher.getRotation().rotateY((float) Math.PI));
        matrices.scale(-0.025f, -0.025f, 0.025f);

        Text text = entity.getDisplayText();
        float opacity = Math.min(1.0f, entity.getLifetime() / 10.0f);
        int color = 0xFF0000 | (((int) (opacity * 255.0f)) << 24);


        //VertexConsumer vertexConsumer = vertexConsumers.getBuffer(ALWAYS_ON_TOP_LAYER);
        float width = -this.getTextRenderer().getWidth(text) / 2f;
        float height = -this.getTextRenderer().fontHeight / 2f;
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        //renderQuad(vertexConsumer, matrix, width - 1, height - 1, width + this.getTextRenderer().getWidth(text) + 1, height + this.getTextRenderer().fontHeight + 1, 0, 0, 0, 0.25f);
        //renderText(text, width, height, color, matrix, vertexConsumer);

        RenderSystem.disableDepthTest();
        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        this.getTextRenderer().draw(
                text,
                -this.getTextRenderer().getWidth(text) / 2f,
                0,
                color,
                false,
                matrices.peek().getPositionMatrix(),
                //vertexConsumer,
                immediate,
                TextRenderer.TextLayerType.SEE_THROUGH,
                0,
                light
        );
        immediate.draw();
        RenderSystem.enableDepthTest();

        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    private void renderQuad(VertexConsumer vertexConsumer, Matrix4f matrix, float x1, float y1, float x2, float y2, float r, float g, float b, float a) {
        vertexConsumer.vertex(matrix, x1, y1, 0).color(r, g, b, a).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(0, 0, 1);
        vertexConsumer.vertex(matrix, x1, y2, 0).color(r, g, b, a).texture(0, 1).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(0, 0, 1);
        vertexConsumer.vertex(matrix, x2, y2, 0).color(r, g, b, a).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(0, 0, 1);
        vertexConsumer.vertex(matrix, x2, y1, 0).color(r, g, b, a).texture(1, 0).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(0, 0, 1);
    }

    private void renderText(Text text, float x, float y, int color, Matrix4f matrix, VertexConsumer vertexConsumer) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = (color >> 24) & 0xFF;

        for (int i = 0; i < text.getString().length(); i++) {
            char c = text.getString().charAt(i);
            float charWidth = this.getTextRenderer().getWidth(String.valueOf(c));

            int textureX = c % 16 * 8;
            int textureY = c / 16 * 8;

            float u1 = textureX / 128f;
            float v1 = textureY / 128f;
            float u2 = (textureX + 8) / 128f;
            float v2 = (textureY + 8) / 128f;

            renderQuad(vertexConsumer, matrix, x, y, x + charWidth, y + 8, r / 255f, g / 255f, b / 255f, a / 255f);
            x += charWidth;
        }
    }

    @Override
    public Identifier getTexture(TextDisplayEntity entity) {
        return Identifier.ofVanilla("textures/font/ascii.png");
        //return Identifier.ofVanilla("textures/entity/experience_orb.png");
    }
}