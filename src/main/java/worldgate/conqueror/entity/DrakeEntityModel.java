package worldgate.conqueror.entity;

import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;

// Made with Blockbench 4.10.4
// Exported for Minecraft version 1.17+ for Yarn
public class DrakeEntityModel<T extends DrakeEntity> extends SinglePartEntityModel<T> {
    private final ModelPart Root;
    private final ModelPart Leg;
    private final ModelPart FrontLeftLeg;
    private final ModelPart BackRightLeg;
    private final ModelPart FrontRightLeg;
    private final ModelPart BackLeftLeg;
    private final ModelPart Tail;
    private final ModelPart Head;
    private final ModelPart Mouth;
    private final ModelPart BottomJaw;
    private final ModelPart TopJaw;
    public DrakeEntityModel(ModelPart root) {
        this.Root = root.getChild("Root");
        this.Leg = Root.getChild("Leg");
        this.FrontLeftLeg = Leg.getChild("FrontLeftLeg");
        this.BackRightLeg = Leg.getChild("BackRightLeg");
        this.FrontRightLeg = Leg.getChild("FrontRightLeg");
        this.BackLeftLeg = Leg.getChild("BackLeftLeg");
        this.Tail = Root.getChild("Tail");
        this.Head = Root.getChild("Head");
        this.Mouth = Head.getChild("Mouth");
        this.BottomJaw = Mouth.getChild("BottomJaw");
        this.TopJaw = Mouth.getChild("TopJaw");
    }
    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData Root = modelPartData.addChild("Root", ModelPartBuilder.create().uv(0, 0).cuboid(-2.0F, -5.0F, -3.0F, 5.0F, 5.0F, 9.0F, new Dilation(0.0F)), ModelTransform.pivot(-1.0F, 21.0F, 0.0F));

        ModelPartData Leg = Root.addChild("Leg", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData FrontLeftLeg = Leg.addChild("FrontLeftLeg", ModelPartBuilder.create().uv(0, 0).cuboid(-1.0F, -1.0F, -1.0F, 2.0F, 6.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(-3.0F, -2.0F, 4.0F));

        ModelPartData BackRightLeg = Leg.addChild("BackRightLeg", ModelPartBuilder.create().uv(0, 0).cuboid(-1.0F, -1.0F, -1.0F, 2.0F, 6.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(4.0F, -2.0F, -1.0F));

        ModelPartData FrontRightLeg = Leg.addChild("FrontRightLeg", ModelPartBuilder.create().uv(0, 0).cuboid(-1.0F, -1.0F, -1.0F, 2.0F, 6.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(4.0F, -2.0F, 4.0F));

        ModelPartData BackLeftLeg = Leg.addChild("BackLeftLeg", ModelPartBuilder.create().uv(0, 0).cuboid(-1.0F, -1.0F, -1.0F, 2.0F, 6.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(-3.0F, -2.0F, -1.0F));

        ModelPartData Tail = Root.addChild("Tail", ModelPartBuilder.create().uv(0, 14).cuboid(-1.5F, -1.0F, -9.0F, 3.0F, 2.0F, 9.0F, new Dilation(0.0F)), ModelTransform.pivot(0.5F, -4.0F, -3.0F));

        ModelPartData Head = Root.addChild("Head", ModelPartBuilder.create().uv(15, 14).cuboid(-2.5F, -2.5F, 0.0F, 5.0F, 5.0F, 3.0F, new Dilation(0.0F)), ModelTransform.pivot(0.5F, -2.5F, 6.0F));

        ModelPartData Mouth = Head.addChild("Mouth", ModelPartBuilder.create(), ModelTransform.pivot(-0.5F, 1.5F, 4.0F));

        ModelPartData BottomJaw = Mouth.addChild("BottomJaw", ModelPartBuilder.create().uv(20, 22).cuboid(-2.5F, -0.5F, -1.0F, 5.0F, 1.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(0.5F, -0.5F, -1.0F));

        ModelPartData TopJaw = Mouth.addChild("TopJaw", ModelPartBuilder.create().uv(19, 0).cuboid(-2.5F, -0.5F, -1.0F, 5.0F, 1.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(0.5F, -1.5F, -1.0F));
        return TexturedModelData.of(modelData, 64, 64);
    }
    @Override
    public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, int color) {
        Root.render(matrices, vertexConsumer, light, overlay, color);

    }

    @Override
    public ModelPart getPart() {
        return null;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {

    }
}