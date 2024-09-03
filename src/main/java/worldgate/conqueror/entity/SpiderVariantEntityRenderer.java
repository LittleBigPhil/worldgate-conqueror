package worldgate.conqueror.entity;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.SpiderEntityRenderer;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.util.Identifier;
import worldgate.conqueror.WorldgateConqueror;

public class SpiderVariantEntityRenderer<T extends SpiderVariantEntity> extends SpiderEntityRenderer<T> {
    public SpiderVariantEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    private static final Identifier PLAIN_TEXTURE = Identifier.of(WorldgateConqueror.MOD_ID, "textures/entity/spider/plain_spider.png");
    private static final Identifier NEUROTOXIC_TEXTURE = Identifier.of(WorldgateConqueror.MOD_ID, "textures/entity/spider/neurotoxic_spider.png");
    private static final Identifier HEMORRHAGIC_TEXTURE = Identifier.of(WorldgateConqueror.MOD_ID, "textures/entity/spider/hemorrhagic_spider.png");
    private static final Identifier DEBILITATING_TEXTURE = Identifier.of(WorldgateConqueror.MOD_ID, "textures/entity/spider/debilitating_spider.png");
    public Identifier getTexture(T spiderEntity) {
        return switch (spiderEntity.getPoisonType()) {
            case NEUROTOXIC -> NEUROTOXIC_TEXTURE;
            case HEMORRHAGIC -> HEMORRHAGIC_TEXTURE;
            case DEBILITATING -> DEBILITATING_TEXTURE;
            default -> PLAIN_TEXTURE;
        };
    }
}
