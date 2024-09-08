package worldgate.conqueror.entity;

import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;
import worldgate.conqueror.WorldgateConqueror;

public class ModModelLayers {
    public static final EntityModelLayer DRAKE =
            new EntityModelLayer(Identifier.of(WorldgateConqueror.MOD_ID, "drake"), "main");
}
