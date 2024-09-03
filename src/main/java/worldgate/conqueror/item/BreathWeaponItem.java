package worldgate.conqueror.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import worldgate.conqueror.entity.BreathWeaponProjectileEntity;

public class BreathWeaponItem extends Item {
    public BreathWeaponItem(Settings settings) {
        super(settings);
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!world.isClient) {
            shootFlameProjectile(world, user);
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.setCurrentHand(hand);
        return TypedActionResult.consume(user.getStackInHand(hand));
    }

    private void shootFlameProjectile(World world, LivingEntity user) {
        //var flame = new SmallFireballEntity(world, user, Vec3d.ZERO);
        var flame = new BreathWeaponProjectileEntity(world);
        flame.setBreathTypeName("Item");
        flame.setOwner(user);
        flame.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, .5F, 8.0F);
        var horizontalOffset = -.2f;
        if (user.getActiveHand() == Hand.MAIN_HAND) {
            horizontalOffset *= -1;
        }
        var angle = user.getYaw() * (Math.PI / 180);
        var xRatio = -MathHelper.cos((float) angle);
        var zRatio = -MathHelper.sin((float) angle);
        flame.setPosition(user.getEyePos().add(horizontalOffset * xRatio, -0.1F, horizontalOffset * zRatio)); // Offset copied from how bows work
        world.spawnEntity(flame);
    }

}
