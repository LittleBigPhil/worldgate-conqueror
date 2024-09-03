package worldgate.conqueror.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TextDisplayEntity extends Entity {
    private static final TrackedData<Text> DISPLAY_TEXT = DataTracker.registerData(TextDisplayEntity.class, TrackedDataHandlerRegistry.TEXT_COMPONENT);
    private static final TrackedData<Integer> LIFETIME = DataTracker.registerData(TextDisplayEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public static class Factory implements EntityType.EntityFactory<TextDisplayEntity> {

        @Override
        public TextDisplayEntity create(EntityType<TextDisplayEntity> type, World world) {
            return new TextDisplayEntity(type, world);
        }
    }
    public static void spawnDamageNumber(Entity damagedEntity, DamageSource source, float amount) {
        if (source.getAttacker() != null && source.getAttacker().isPlayer()) {
            Vec3d spawnPos = damagedEntity.getPos().add(0, damagedEntity.getHeight(), 0);
            World world = damagedEntity.getWorld();

            // Create a temporary entity to display the damage number
            Entity damageNumber = new TextDisplayEntity(world, spawnPos, Text.of(String.format("%.1f", amount)));
            world.spawnEntity(damageNumber);
        }
    }

    public TextDisplayEntity(EntityType<? extends TextDisplayEntity> entityType, World world) {
        super(entityType, world);
        this.noClip = true;
    }

    public TextDisplayEntity(World world, Vec3d pos, Text text) {
        this(ModEntities.TEXT_DISPLAY, world);
        this.setPosition(pos.x, pos.y, pos.z);
        this.setDisplayText(text);
        this.setLifetime(20); // 1 second at 20 ticks per second
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(DISPLAY_TEXT, Text.empty());
        builder.add(LIFETIME, 0);
    }

    @Override
    public void tick() {
        super.tick();
        int lifetime = this.getLifetime();
        if (lifetime <= 1) {
            this.discard();
        } else {
            this.setLifetime(lifetime - 1);
            // Optional: Add some upward motion to the text
            this.setVelocity(0, 0.01, 0);
            this.move(MovementType.SELF, this.getVelocity());
        }
    }

    public void setDisplayText(Text text) {
        this.dataTracker.set(DISPLAY_TEXT, text);
    }
    public Text getDisplayText() {
        return this.dataTracker.get(DISPLAY_TEXT);
    }
    public void setLifetime(int ticks) {
        this.dataTracker.set(LIFETIME, ticks);
    }
    public int getLifetime() {
        return this.dataTracker.get(LIFETIME);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.contains("Text")) {
            this.setDisplayText(Text.of((nbt.getString("Text"))));
            //this.setDisplayText(Text.Serialization.fromJson(nbt.getString("Text"), registries));
        }
        this.setLifetime(nbt.getInt("Lifetime"));
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putString("Text", getDisplayText().getString());
        nbt.putInt("Lifetime", this.getLifetime());
    }
}