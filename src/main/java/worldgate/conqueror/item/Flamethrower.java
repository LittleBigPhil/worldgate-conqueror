package worldgate.conqueror.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class Flamethrower extends Item {
    public Flamethrower(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            shootFlames(world, user);
        }
        user.getItemCooldownManager().set(this, 20); // Cooldown of 1 second (20 ticks)
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    private void shootFlames(World world, PlayerEntity user) {
        Vec3d startPos = user.getCameraPosVec(1.0F);
        Vec3d direction = user.getRotationVec(1.0F);
        Vec3d endPos = startPos.add(direction.multiply(10)); // 10 block range

        Box flameBox = new Box(startPos, endPos).expand(1.0); // Flame area width
        List<LivingEntity> entities = world.getEntitiesByClass(LivingEntity.class, flameBox, e -> e != user);

        for (LivingEntity entity : entities) {
            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.setOnFireFor(5); // Set entity on fire for 5 seconds
                livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 100, 1)); // Apply wither effect for 5 seconds
            }
        }
    }
}
