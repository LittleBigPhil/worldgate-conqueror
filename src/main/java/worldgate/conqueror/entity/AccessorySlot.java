package worldgate.conqueror.entity;

import com.mojang.datafixers.util.Pair;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

// See vanilla ArmorSlot
public class AccessorySlot extends Slot {
    private final LivingEntity entity;
    private final EquipmentSlot equipmentSlot;
    @Nullable
    private final Identifier backgroundSprite;
    private final Predicate<ItemStack> canInsertPredicate;

    public AccessorySlot(Inventory inventory, LivingEntity entity, EquipmentSlot equipmentSlot, Predicate<ItemStack> canInsertPredicate, int index, int x, int y, @Nullable Identifier backgroundSprite) {
        super(inventory, index, x, y);
        this.entity = entity;
        this.equipmentSlot = equipmentSlot;
        this.backgroundSprite = backgroundSprite;
        this.canInsertPredicate = canInsertPredicate;
    }

    public void setStack(ItemStack stack, ItemStack previousStack) {
        this.entity.onEquipStack(this.equipmentSlot, previousStack, stack);
        super.setStack(stack, previousStack);
    }

    public int getMaxItemCount() {
        return 1;
    }

    public boolean canInsert(ItemStack stack) {
        return (this.equipmentSlot == this.entity.getPreferredEquipmentSlot(stack)) && canInsertPredicate.test(stack);
    }

    public boolean canTakeItems(PlayerEntity playerEntity) {
        ItemStack itemStack = this.getStack();
        return !itemStack.isEmpty() && !playerEntity.isCreative() && EnchantmentHelper.hasAnyEnchantmentsWith(itemStack, EnchantmentEffectComponentTypes.PREVENT_ARMOR_CHANGE) ? false : super.canTakeItems(playerEntity);
    }

    public Pair<Identifier, Identifier> getBackgroundSprite() {
        return super.getBackgroundSprite();
        //return this.backgroundSprite != null ? Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, this.backgroundSprite) : super.getBackgroundSprite();
    }
}
