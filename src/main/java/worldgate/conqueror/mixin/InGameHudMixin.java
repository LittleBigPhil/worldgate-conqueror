package worldgate.conqueror.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.*;
import worldgate.conqueror.mechanic.ModEntityAttributes;
import worldgate.conqueror.mechanic.ModStatusEffects;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow private int ticks;
    @Shadow @Final private Random random;
    @Shadow @Final private MinecraftClient client;
    @Shadow private long heartJumpEndTick;
    @Shadow private int lastHealthValue;
    @Shadow private long lastHealthCheckTime;
    @Shadow private int renderHealthValue;

    @Shadow protected abstract PlayerEntity getCameraPlayer();

    @Overwrite
    private void renderStatusBars(DrawContext context) {
        PlayerEntity playerEntity = this.getCameraPlayer();
        if (playerEntity != null) {
            int playerHealth = MathHelper.ceil(playerEntity.getHealth());
            boolean bl = this.heartJumpEndTick > (long)this.ticks && (this.heartJumpEndTick - (long)this.ticks) / 3L % 2L == 1L;
            long l = Util.getMeasuringTimeMs();
            if (playerHealth < this.lastHealthValue && playerEntity.timeUntilRegen > 0) {
                this.lastHealthCheckTime = l;
                this.heartJumpEndTick = (long)(this.ticks + 20);
            } else if (playerHealth > this.lastHealthValue && playerEntity.timeUntilRegen > 0) {
                this.lastHealthCheckTime = l;
                this.heartJumpEndTick = (long)(this.ticks + 10);
            }

            if (l - this.lastHealthCheckTime > 1000L) {
                this.lastHealthValue = playerHealth;
                this.renderHealthValue = playerHealth;
                this.lastHealthCheckTime = l;
            }

            this.lastHealthValue = playerHealth;
            int playerRenderHealth = this.renderHealthValue;
            this.random.setSeed((long)(this.ticks * 312871));
            int leftOfLeftRows = context.getScaledWindowWidth() / 2 - 91;
            int rightOfRightRows = context.getScaledWindowWidth() / 2 + 91;
            int topOfFirstRow = context.getScaledWindowHeight() - 39;
            float playerMaxHealth = Math.max((float)playerEntity.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH), (float)Math.max(playerRenderHealth, playerHealth));
            int absorptionAmount = MathHelper.ceil(playerEntity.getAbsorptionAmount());
            int playerHealthRows = MathHelper.ceil((playerMaxHealth + (float) absorptionAmount) / 2.0F / 10.0F);
            int regeneratingHeartIndex = Math.max(10 - (playerHealthRows - 2), 3);
            int topOfSecondRow = topOfFirstRow - 10;
            int s = -1;
            if (playerEntity.hasStatusEffect(StatusEffects.REGENERATION)) {
                s = this.ticks % MathHelper.ceil(playerMaxHealth + 5.0F);
            }

            this.client.getProfiler().push("armor");
            renderArmor(context, playerEntity, topOfFirstRow, playerHealthRows, regeneratingHeartIndex, leftOfLeftRows);
            this.client.getProfiler().swap("health");
            this.renderHealthBar(context, playerEntity, leftOfLeftRows, topOfFirstRow, regeneratingHeartIndex, s, playerMaxHealth, playerHealth, playerRenderHealth, absorptionAmount, bl);
            LivingEntity livingEntity = this.getRiddenEntity();
            int mountHearts = this.getHeartCount(livingEntity);
            if (mountHearts == 0) {
                this.client.getProfiler().swap("food");
                int maxFood = (int)playerEntity.getAttributeValue(ModEntityAttributes.MAX_FOOD_ATTRIBUTE);
                int numberOfFoodRows = MathHelper.ceil(((float)maxFood) / 2.0F / 10.0F);
                this.renderFood(context, playerEntity, topOfFirstRow, rightOfRightRows, maxFood, numberOfFoodRows);
                topOfSecondRow -= 10 * numberOfFoodRows;
            }

            this.client.getProfiler().swap("air");
            int maxAir = playerEntity.getMaxAir();
            int currentAir = Math.min(playerEntity.getAir(), maxAir);
            if (playerEntity.isSubmergedIn(FluidTags.WATER) || currentAir < maxAir) {
                int mountHeartExtraRows = this.getHeartRows(mountHearts) - 1;
                topOfSecondRow -= mountHeartExtraRows * 10;
                int x = MathHelper.ceil((double)(currentAir - 2) * 10.0 / (double) maxAir);
                int y = MathHelper.ceil((double) currentAir * 10.0 / (double) maxAir) - x;
                RenderSystem.enableBlend();


                final Identifier AIR_TEXTURE = Identifier.ofVanilla("hud/air");
                final Identifier AIR_BURSTING_TEXTURE = Identifier.ofVanilla("hud/air_bursting");
                for(int airBubbleIndex = 0; airBubbleIndex < x + y; ++airBubbleIndex) {
                    if (airBubbleIndex < x) {
                        context.drawGuiTexture(AIR_TEXTURE, rightOfRightRows - airBubbleIndex * 8 - 9, topOfSecondRow, 9, 9);
                    } else {
                        context.drawGuiTexture(AIR_BURSTING_TEXTURE, rightOfRightRows - airBubbleIndex * 8 - 9, topOfSecondRow, 9, 9);
                    }
                }

                RenderSystem.disableBlend();
            }

            this.client.getProfiler().pop();
        }
    }

    @Shadow protected abstract int getHeartRows(int t);
    @Shadow protected abstract int getHeartCount(LivingEntity livingEntity);
    @Shadow protected abstract LivingEntity getRiddenEntity();

    @Shadow protected abstract  void renderHealthBar(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking);
    @Shadow private static void renderArmor(DrawContext context, PlayerEntity player, int i, int j, int k, int x) {
        // This method body will be ignored by the mixin processor
        throw new AssertionError();
    }

    @Unique
    private void renderFood(DrawContext context, PlayerEntity player, int top, int right, int maxFood, int foodRows) {
        final Identifier FOOD_EMPTY_HUNGER_TEXTURE = Identifier.ofVanilla("hud/food_empty_hunger");
        final Identifier FOOD_HALF_HUNGER_TEXTURE = Identifier.ofVanilla("hud/food_half_hunger");
        final Identifier FOOD_FULL_HUNGER_TEXTURE = Identifier.ofVanilla("hud/food_full_hunger");
        final Identifier FOOD_EMPTY_TEXTURE = Identifier.ofVanilla("hud/food_empty");
        final Identifier FOOD_HALF_TEXTURE = Identifier.ofVanilla("hud/food_half");
        final Identifier FOOD_FULL_TEXTURE = Identifier.ofVanilla("hud/food_full");

        HungerManager hungerManager = player.getHungerManager();
        int foodLevel = hungerManager.getFoodLevel();
        RenderSystem.enableBlend();
        int maxFoodInTextures = MathHelper.ceil(((float) maxFood) / 2.0f);

        for(int foodTextureIndex = 0; foodTextureIndex < maxFoodInTextures; ++foodTextureIndex) {
            int topOfTexture = top;
            Identifier identifier;
            Identifier identifier2;
            Identifier identifier3;
            if (player.hasStatusEffect(ModStatusEffects.HUNGER)) {
                identifier = FOOD_EMPTY_HUNGER_TEXTURE;
                identifier2 = FOOD_HALF_HUNGER_TEXTURE;
                identifier3 = FOOD_FULL_HUNGER_TEXTURE;
            } else {
                identifier = FOOD_EMPTY_TEXTURE;
                identifier2 = FOOD_HALF_TEXTURE;
                identifier3 = FOOD_FULL_TEXTURE;
            }

            int horizontalIndex = foodTextureIndex % 10;
            int verticalIndex = foodTextureIndex / 10;
            topOfTexture -= verticalIndex * 10;

            int leftOfTexture = right - horizontalIndex * 8 - 9;
            context.drawGuiTexture(identifier, leftOfTexture, topOfTexture, 9, 9);
            if (foodTextureIndex * 2 + 1 < foodLevel) {
                context.drawGuiTexture(identifier3, leftOfTexture, topOfTexture, 9, 9);
            }

            if (foodTextureIndex * 2 + 1 == foodLevel) {
                context.drawGuiTexture(identifier2, leftOfTexture, topOfTexture, 9, 9);
            }
        }

        RenderSystem.disableBlend();
    }

    @Overwrite
    private void renderFood(DrawContext context, PlayerEntity player, int top, int right) {
        final Identifier FOOD_EMPTY_HUNGER_TEXTURE = Identifier.ofVanilla("hud/food_empty_hunger");
        final Identifier FOOD_HALF_HUNGER_TEXTURE = Identifier.ofVanilla("hud/food_half_hunger");
        final Identifier FOOD_FULL_HUNGER_TEXTURE = Identifier.ofVanilla("hud/food_full_hunger");
        final Identifier FOOD_EMPTY_TEXTURE = Identifier.ofVanilla("hud/food_empty");
        final Identifier FOOD_HALF_TEXTURE = Identifier.ofVanilla("hud/food_half");
        final Identifier FOOD_FULL_TEXTURE = Identifier.ofVanilla("hud/food_full");

        HungerManager hungerManager = player.getHungerManager();
        int foodLevel = hungerManager.getFoodLevel();
        RenderSystem.enableBlend();

        for(int foodTextureIndex = 0; foodTextureIndex < 10; ++foodTextureIndex) {
            int topOfTexture = top;
            Identifier identifier;
            Identifier identifier2;
            Identifier identifier3;
            if (player.hasStatusEffect(ModStatusEffects.HUNGER)) {
                identifier = FOOD_EMPTY_HUNGER_TEXTURE;
                identifier2 = FOOD_HALF_HUNGER_TEXTURE;
                identifier3 = FOOD_FULL_HUNGER_TEXTURE;
            } else {
                identifier = FOOD_EMPTY_TEXTURE;
                identifier2 = FOOD_HALF_TEXTURE;
                identifier3 = FOOD_FULL_TEXTURE;
            }

            if (player.getHungerManager().getSaturationLevel() <= 0.0F && this.ticks % (foodLevel * 3 + 1) == 0) {
                topOfTexture += this.random.nextInt(3) - 1;
            }

            int leftOfTexture = right - foodTextureIndex * 8 - 9;
            context.drawGuiTexture(identifier, leftOfTexture, topOfTexture, 9, 9);
            if (foodTextureIndex * 2 + 1 < foodLevel) {
                context.drawGuiTexture(identifier3, leftOfTexture, topOfTexture, 9, 9);
            }

            if (foodTextureIndex * 2 + 1 == foodLevel) {
                context.drawGuiTexture(identifier2, leftOfTexture, topOfTexture, 9, 9);
            }
        }

        RenderSystem.disableBlend();
    }
}
