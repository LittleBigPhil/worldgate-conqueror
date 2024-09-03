package worldgate.conqueror.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import worldgate.conqueror.mechanic.ModStatusEffects;

public class FoodItem extends Item {
    public FoodItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (user.hasStatusEffect(ModStatusEffects.NAUSEA)) {
            return TypedActionResult.fail(itemStack);
        } else {
            return super.use(world, user, hand);
        }
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (user.hasStatusEffect(ModStatusEffects.NAUSEA)) {
            user.stopUsingItem();
        }
    }
}
