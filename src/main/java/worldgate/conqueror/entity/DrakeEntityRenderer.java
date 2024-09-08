package worldgate.conqueror.entity;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import worldgate.conqueror.WorldgateConqueror;

public class DrakeEntityRenderer extends MobEntityRenderer<DrakeEntity, DrakeEntityModel<DrakeEntity>> {
    public static final Identifier TEXTURE = Identifier.of(WorldgateConqueror.MOD_ID, "textures/entity/drake.png");

    public DrakeEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new DrakeEntityModel<>(context.getPart(ModModelLayers.DRAKE)), .6f); // .6f is the shadow size of the entity
    }

    @Override
    public Identifier getTexture(DrakeEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(DrakeEntity livingEntity, float f, float g, MatrixStack matrixStack,
                       VertexConsumerProvider vertexConsumerProvider, int i) {
        // could scale babies here to be half as big
        super.render(livingEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }
}
