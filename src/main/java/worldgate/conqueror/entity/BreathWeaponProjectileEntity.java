package worldgate.conqueror.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.EntityEffectParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.World;
import worldgate.conqueror.mechanic.ModStatusEffects;
import worldgate.conqueror.mechanic.StatusEffectTarget;
import worldgate.conqueror.particle.ModParticles;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BreathWeaponProjectileEntity extends ExplosiveProjectileEntity implements FlyingItemEntity {
    private static final TrackedData<String> BREATH_TYPE = DataTracker.registerData(BreathWeaponProjectileEntity.class, TrackedDataHandlerRegistry.STRING);
    public interface BreathType {
        String name();
        Set<ModStatusEffects.Instance> statusEffects();
        int color();
        float damage();
        int lifetime();
        int skipRenderFrames();
    }
    private static final Map<String, BreathType> breathTypes = new HashMap<String, BreathType>();
    public static BreathType registerBreathType(BreathType breathType) {
        breathTypes.put(breathType.name(), breathType);
        return breathType;
    }
    public static BreathType getBreathTypeByName(String name) {
        return breathTypes.get(name);
    }
    static {
        registerBreathType(new BreathType() {
            @Override
            public String name() {
                return "Item";
            }

            @Override
            public Set<ModStatusEffects.Instance> statusEffects() {
                return ModStatusEffects.hemorrhagicPoison(0.0f);
            }

            @Override
            public int color() {
                return ModStatusEffects.BLEEDING.value().getColor();
            }

            @Override
            public float damage() {
                return 0;
            }

            @Override
            public int lifetime() {
                return 15;
            }

            @Override
            public int skipRenderFrames() {
                return 3;
            }
        });
    }

    public BreathWeaponProjectileEntity(EntityType<? extends BreathWeaponProjectileEntity> entityType, World world) {
        super(entityType, world);
        this.accelerationPower = 0.0;
    }
    public BreathWeaponProjectileEntity(World world) {
        this(ModEntities.FLAME_PROJECTILE_ENTITY_TYPE, world);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        if (this.getWorld() instanceof ServerWorld serverWorld && entityHitResult.getEntity() instanceof LivingEntity livingEntity) {
            //livingEntity.setOnFireFor(5); // Set entity on fire for 5 seconds
            for (var effect : getBreathType().statusEffects()) {
                ((StatusEffectTarget) livingEntity).addStatusEffectResistable(effect.effectInstance(), this.getOwner(), effect.strength());
            }
        }
    }
    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!this.getWorld().isClient) {
            this.discard();
        }
    }

    public void setBreathTypeName(String type) {
        this.getDataTracker().set(BREATH_TYPE, type);
    }
    public String getBreathTypeName() {
        return this.getDataTracker().get(BREATH_TYPE);
    }
    public BreathType getBreathType() {
        return getBreathTypeByName(getBreathTypeName());
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(BREATH_TYPE, "");
    }
    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putString("BreathType", this.getBreathTypeName());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("BreathType", NbtElement.COMPOUND_TYPE)) {
            this.setBreathTypeName(nbt.getString("BreathType"));
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return false;
    }


    @Override
    protected boolean isBurning() {
        return false;
    }

    @Override
    public ItemStack getStack() {
        return ItemStack.EMPTY;
        //return new ItemStack(Items.FIRE_CHARGE); // Useful for debugging
    }

    @Override
    protected ParticleEffect getParticleType() {
        return null;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.age > getBreathType().lifetime()) {
            this.discard();
        }

        Entity owner = this.getOwner();
        var isValidOwner = (owner == null || !owner.isRemoved());
        if (this.getWorld().isClient || isValidOwner && this.getWorld().isChunkLoaded(this.getBlockPos())) {
            var particleSpawnPos = this.getVelocity().multiply(.75).add(this.getPos());

            var color = getBreathType().color();
            var colorWithAlpha = ColorHelper.Argb.withAlpha(255, color); // Taken from the StatusEffect constructor
            this.getWorld().addParticle(EntityEffectParticleEffect.create(ModParticles.BreathWeaponParticle, colorWithAlpha), particleSpawnPos.getX(), particleSpawnPos.getY(), particleSpawnPos.getZ(), 0.0, 0.0, 0.0);
        }
    }
    @Override
    public boolean shouldRender(double distance) {
        if (this.age < getBreathType().skipRenderFrames()) {
            return false;
        }

        double d = this.getBoundingBox().getAverageSideLength() * 4.0;
        if (Double.isNaN(d)) {
            d = 4.0;
        }

        d *= 64.0;
        return distance < d * d;
    }
}