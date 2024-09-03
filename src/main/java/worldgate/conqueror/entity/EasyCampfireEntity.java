package worldgate.conqueror.entity;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.*;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// See FurnaceBlockEntity and its parents
public class EasyCampfireEntity extends LockableContainerBlockEntity implements SidedInventory, RecipeUnlocker, RecipeInputProvider {

    protected static final int INPUT_SLOT_INDEX = 0;
    protected static final int OUTPUT_SLOT_INDEX = 1;
    protected DefaultedList<ItemStack> inventory = DefaultedList.ofSize(2, ItemStack.EMPTY);

    public static final int COOK_TIME_PROPERTY_INDEX = 0;
    public static final int COOK_TIME_TOTAL_PROPERTY_INDEX = 1;
    public static final int PROPERTY_COUNT = 2;
    public static final int DEFAULT_COOK_TIME = 600;
    int cookTime;
    int cookTimeTotal;
    protected final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case COOK_TIME_PROPERTY_INDEX -> cookTime;
                case COOK_TIME_TOTAL_PROPERTY_INDEX -> cookTimeTotal;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case COOK_TIME_PROPERTY_INDEX:
                    cookTime = value;
                    break;
                case COOK_TIME_TOTAL_PROPERTY_INDEX:
                    cookTimeTotal = value;
            }
        }

        @Override
        public int size() {
            return PROPERTY_COUNT;
        }
    };

    private final Object2IntOpenHashMap<Identifier> recipesUsed = new Object2IntOpenHashMap<>();
    private final RecipeManager.MatchGetter<SingleStackRecipeInput, ? extends AbstractCookingRecipe> matchGetter;
    public static final RecipeType<CampfireCookingRecipe> RECIPE_TYPE = RecipeType.CAMPFIRE_COOKING;
    //public static final RecipeType<SmokingRecipe> RECIPE_TYPE = RecipeType.SMOKING;

    public EasyCampfireEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.matchGetter = RecipeManager.createCachedMatchGetter(RECIPE_TYPE);
    }
    public EasyCampfireEntity(BlockPos pos, BlockState state) {
        this(ModEntities.CAMPFIRE_ENTITY, pos, state);
    }

    @Override
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapper) {
        super.writeNbt(nbt, wrapper);

        Inventories.writeNbt(nbt, this.inventory, wrapper);

        nbt.putShort("CookTime", (short)this.cookTime);
        nbt.putShort("CookTimeTotal", (short)this.cookTimeTotal);

        NbtCompound nbtCompound = new NbtCompound();
        this.recipesUsed.forEach((identifier, count) -> nbtCompound.putInt(identifier.toString(), count));
        nbt.put("RecipesUsed", nbtCompound);
    }
    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapper) {
        super.readNbt(nbt, wrapper);

        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        Inventories.readNbt(nbt, this.inventory, wrapper);

        this.cookTime = nbt.getShort("CookTime");
        this.cookTimeTotal = nbt.getShort("CookTimeTotal");

        NbtCompound nbtCompound = nbt.getCompound("RecipesUsed");
        for (String string : nbtCompound.getKeys()) {
            this.recipesUsed.put(Identifier.of(string), nbtCompound.getInt(string));
        }
    }

    @Override
    protected Text getContainerName() {
        return Text.translatable("container.worldgate-conqueror.campfire");
    }
    @Override
    protected DefaultedList<ItemStack> getHeldStacks() {
        return this.inventory;
    }
    @Override
    protected void setHeldStacks(DefaultedList<ItemStack> inventory) {
        this.inventory = inventory;
    }
    @Override
    public int size() {
        return this.inventory.size();
    }


    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new CampfireScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }


    public static void tick(World world, BlockPos pos, BlockState state, EasyCampfireEntity blockEntity) {
        boolean wasBurning = blockEntity.isBurning();
        boolean bl2 = false;

        //ItemStack fuelStack = blockEntity.inventory.get(1);
        ItemStack inputStack = blockEntity.inventory.get(INPUT_SLOT_INDEX);
        boolean bl3 = !inputStack.isEmpty();
        //boolean bl4 = !fuelStack.isEmpty();
        if (blockEntity.isBurning() || bl3) {
            RecipeEntry<?> recipeEntry;
            if (bl3) {
                recipeEntry = (RecipeEntry<?>)blockEntity.matchGetter.getFirstMatch(new SingleStackRecipeInput(inputStack), world).orElse(null);
            } else {
                recipeEntry = null;
            }

            // start burning if possible
            int maxCountPerStack = blockEntity.getMaxCountPerStack();
            if (!blockEntity.isBurning() && canAcceptRecipeOutput(world.getRegistryManager(), recipeEntry, blockEntity.inventory, maxCountPerStack)) {
                if (blockEntity.isBurning()) {
                    bl2 = true;
                }
            }

            // Update cooking progress, and succeed if finished
            if (blockEntity.isBurning() && canAcceptRecipeOutput(world.getRegistryManager(), recipeEntry, blockEntity.inventory, maxCountPerStack)) {
                blockEntity.cookTime++;
                if (blockEntity.cookTime == blockEntity.cookTimeTotal) {
                    blockEntity.cookTime = 0;
                    blockEntity.cookTimeTotal = getCookTime(world, blockEntity);
                    if (craftRecipe(world.getRegistryManager(), recipeEntry, blockEntity.inventory, maxCountPerStack)) {
                        blockEntity.setLastRecipe(recipeEntry);
                    }

                    bl2 = true;
                }
            } else {
                blockEntity.cookTime = 0;
            }
        } else if (!blockEntity.isBurning() && blockEntity.cookTime > 0) {
            // Keep cookTime within the appropriate bounds.
            blockEntity.cookTime = MathHelper.clamp(blockEntity.cookTime - 2, 0, blockEntity.cookTimeTotal);
        }

        // Update the block state if necessary
        if (wasBurning != blockEntity.isBurning()) {
        }
        // Update the entity state if necessary
        if (bl2) {
            markDirty(world, pos, state);
        }
    }

    private boolean isBurning() {
        return true;
    }
    private static boolean canAcceptRecipeOutput(
            DynamicRegistryManager registryManager, @Nullable RecipeEntry<?> recipe, DefaultedList<ItemStack> slots, int count
    ) {
        if (!slots.get(INPUT_SLOT_INDEX).isEmpty() && recipe != null) {
            ItemStack recipeResult = recipe.value().getResult(registryManager);
            if (recipeResult.isEmpty()) {
                return false;
            } else {
                ItemStack outputStack = slots.get(OUTPUT_SLOT_INDEX);
                if (outputStack.isEmpty()) {
                    return true;
                } else if (!ItemStack.areItemsAndComponentsEqual(outputStack, recipeResult)) {
                    return false;
                } else {
                    return (outputStack.getCount() < count && outputStack.getCount() < outputStack.getMaxCount()) || outputStack.getCount() < recipeResult.getMaxCount();
                }
            }
        } else {
            return false;
        }
    }

    private static boolean craftRecipe(DynamicRegistryManager registryManager, @Nullable RecipeEntry<?> recipe, DefaultedList<ItemStack> slots, int count) {
        if (recipe != null && canAcceptRecipeOutput(registryManager, recipe, slots, count)) {
            ItemStack inputStack = slots.get(INPUT_SLOT_INDEX);
            ItemStack recipeResult = recipe.value().getResult(registryManager);
            ItemStack outputStack = slots.get(OUTPUT_SLOT_INDEX);
            if (outputStack.isEmpty()) {
                slots.set(OUTPUT_SLOT_INDEX, recipeResult.copy());
            } else if (ItemStack.areItemsAndComponentsEqual(outputStack, recipeResult)) {
                outputStack.increment(1);
            }

            inputStack.decrement(1);
            return true;
        } else {
            return false;
        }
    }
    private static int getCookTime(World world, EasyCampfireEntity campfire) {
        SingleStackRecipeInput singleStackRecipeInput = new SingleStackRecipeInput(campfire.getStack(0));
        return (Integer) campfire.matchGetter
                .getFirstMatch(singleStackRecipeInput, world)
                .map(recipe -> ((AbstractCookingRecipe)recipe.value()).getCookingTime())
                .orElse(200);
    }

    @Override
    public void provideRecipeInputs(RecipeMatcher finder) {
        for (ItemStack itemStack : this.inventory) {
            finder.addInput(itemStack);
        }
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        ItemStack itemStack = this.inventory.get(slot);
        boolean cookingShouldReset = !stack.isEmpty() && ItemStack.areItemsAndComponentsEqual(itemStack, stack);
        this.inventory.set(slot, stack);
        stack.capCount(this.getMaxCount(stack));
        if (slot == INPUT_SLOT_INDEX && !cookingShouldReset) {
            this.cookTimeTotal = getCookTime(this.world, this);
            this.cookTime = 0;
            this.markDirty();
        }
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        if (side == Direction.DOWN) {
            return new int[]{OUTPUT_SLOT_INDEX};
        } else {
            return new int[]{INPUT_SLOT_INDEX};
        }
    }
    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return true;
    }
    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }



    @Override
    public void setLastRecipe(@Nullable RecipeEntry<?> recipe) {
        if (recipe != null) {
            Identifier identifier = recipe.id();
            this.recipesUsed.addTo(identifier, 1);
        }
    }
    @Nullable
    @Override
    public RecipeEntry<?> getLastRecipe() {
        return null;
    }
    @Override
    public void unlockLastRecipe(PlayerEntity player, List<ItemStack> ingredients) {
    }
    public void dropExperienceForRecipesUsed(ServerPlayerEntity player) {
        List<RecipeEntry<?>> list = this.getRecipesUsedAndDropExperience(player.getServerWorld(), player.getPos());
        player.unlockRecipes(list);

        for (RecipeEntry<?> recipeEntry : list) {
            if (recipeEntry != null) {
                player.onRecipeCrafted(recipeEntry, this.inventory);
            }
        }

        this.recipesUsed.clear();
    }
    public List<RecipeEntry<?>> getRecipesUsedAndDropExperience(ServerWorld world, Vec3d pos) {
        List<RecipeEntry<?>> list = Lists.<RecipeEntry<?>>newArrayList();

        for (Object2IntMap.Entry<Identifier> entry : this.recipesUsed.object2IntEntrySet()) {
            world.getRecipeManager().get((Identifier)entry.getKey()).ifPresent(recipe -> {
                list.add(recipe);
                dropExperience(world, pos, entry.getIntValue(), ((AbstractCookingRecipe)recipe.value()).getExperience());
            });
        }

        return list;
    }
    private static void dropExperience(ServerWorld world, Vec3d pos, int multiplier, float experience) {
        int i = MathHelper.floor((float)multiplier * experience);
        float f = MathHelper.fractionalPart((float)multiplier * experience);
        if (f != 0.0F && Math.random() < (double)f) {
            i++;
        }

        ExperienceOrbEntity.spawn(world, pos, i);
    }
}
