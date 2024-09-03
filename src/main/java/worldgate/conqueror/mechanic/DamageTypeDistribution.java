package worldgate.conqueror.mechanic;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;

public record DamageTypeDistribution(float blunt, float pierce, float slash) {
    public static DamageTypeDistribution BLUNT = new DamageTypeDistribution(1f, 0f, 0f);
    public static DamageTypeDistribution PIERCE = new DamageTypeDistribution(0f, 1f, 0f);
    public static DamageTypeDistribution SLASH = new DamageTypeDistribution(0f, 0f, 1f);
    public static DamageTypeDistribution MIXED = new DamageTypeDistribution(.333f, .334f, .333f);
    public static DamageTypeDistribution AXE = new DamageTypeDistribution(.5f, 0f, .5f);

    public static class Mob {
        public static DamageTypeDistribution BASE = new DamageTypeDistribution(25,25,25);
        public static DamageTypeDistribution SKELETON = new DamageTypeDistribution(25, 100, 100);
        public static DamageTypeDistribution ZOMBIE = new DamageTypeDistribution(50, 30, 50);
        public static DamageTypeDistribution SPIDER = new DamageTypeDistribution(30, 30, 75);
    }

    public static class Armor {
        public static DamageTypeDistribution GAMBESON = new DamageTypeDistribution(20,5,10).halve();
        public static DamageTypeDistribution RAWHIDE = Mob.ZOMBIE.subtract(Mob.BASE).halve();
        public static DamageTypeDistribution CHITIN = Mob.SPIDER.subtract(Mob.BASE).halve();
    }

    public DamageTypeDistribution scaleBy(float scaling) {
        return new DamageTypeDistribution(blunt * scaling, pierce * scaling, slash * scaling);
    }
    public DamageTypeDistribution halve() {
        return scaleBy(.5f);
    }
    public float attackThrough(DamageTypeDistribution resist) {
        return attackThrough(blunt, resist.blunt) + attackThrough(pierce, resist.pierce) + attackThrough(slash, resist.slash);
    }
    public static float attackThrough(float damage, float resist) {
        // The simplified League formula (Warframe also uses that)
        // It starts out like percentage points,
        //     but at high values an X% increase in resist gives an X% increase in effective health.
        // More generally, effective health = health * (resist + 100) / 100
        // Negative resist doesn't do anything, but all entities are expected to have a base resist of 25
        if (resist > 0) {
            return damage / (1 + resist / 100);
        } else {
            return damage;
        }
    }
    public static DamageTypeDistribution resistsOf(LivingEntity entity) {
        float blunt = (float) entity.getAttributeValue(ModEntityAttributes.BLUNT_RESISTANCE);
        float pierce = (float) entity.getAttributeValue(ModEntityAttributes.PIERCE_RESISTANCE);
        float slash = (float) entity.getAttributeValue(ModEntityAttributes.SLASH_RESISTANCE);
        float efficiency = (float) entity.getAttributeValue(ModEntityAttributes.ARMOR_EFFICIENCY);
        return new DamageTypeDistribution(blunt, pierce, slash).scaleBy(efficiency);
    }
    public DamageTypeDistribution add(DamageTypeDistribution that) {
        return new DamageTypeDistribution(this.blunt + that.blunt, this.pierce + that.pierce, this.slash + that.slash);
    }
    public DamageTypeDistribution subtract(DamageTypeDistribution that) {
        return add(that.scaleBy(-1));
    }

    public DefaultAttributeContainer.Builder setBaseAttributesOf(DefaultAttributeContainer.Builder builder) {
        return builder
                .add(ModEntityAttributes.BLUNT_RESISTANCE, blunt)
                .add(ModEntityAttributes.PIERCE_RESISTANCE, pierce)
                .add(ModEntityAttributes.SLASH_RESISTANCE, slash);
    }
}
