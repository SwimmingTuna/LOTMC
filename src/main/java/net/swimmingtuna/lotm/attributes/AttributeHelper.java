package net.swimmingtuna.lotm.attributes;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;
import net.swimmingtuna.lotm.nihilums.tweaks.Attributes.PathwayAttributes.*;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.util.BeyonderUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttributeHelper {

    private enum AttributeType {
        HEALTH, SPEED, ATTACK, NIGHT_VISION, FIRE_RESISTANCE,
        JUMP, ARMOR, ARMOR_TOUGHNESS, WATER_BREATHING, DIG_SPEED
    }

    // Pathway attribute lookup map - initialized once for performance
    private static final Map<BeyonderClass, Map<AttributeType, List<Double>>> PATHWAY_ATTRIBUTES = new HashMap<>();

    static {
        // Initialize pathway attribute mappings
        initializePathwayAttributes();
    }

    private static void initializePathwayAttributes() {
        // Sailor attributes
        Map<AttributeType, List<Double>> sailorAttributes = new HashMap<>();
        sailorAttributes.put(AttributeType.HEALTH, SailorAttributes.healthList);
        sailorAttributes.put(AttributeType.SPEED, SailorAttributes.speedList);
        sailorAttributes.put(AttributeType.ATTACK, SailorAttributes.attackList);
        sailorAttributes.put(AttributeType.NIGHT_VISION, SailorAttributes.nightVisionList);
        sailorAttributes.put(AttributeType.FIRE_RESISTANCE, SailorAttributes.fireResistanceList);
        sailorAttributes.put(AttributeType.JUMP, SailorAttributes.jumpList);
        sailorAttributes.put(AttributeType.ARMOR, SailorAttributes.armorList);
        sailorAttributes.put(AttributeType.ARMOR_TOUGHNESS, SailorAttributes.armorToughnessList);
        sailorAttributes.put(AttributeType.WATER_BREATHING, SailorAttributes.waterBreathingList);
        sailorAttributes.put(AttributeType.DIG_SPEED, SailorAttributes.digSpeedList);
        PATHWAY_ATTRIBUTES.put(BeyonderClassInit.SAILOR.get(), sailorAttributes);

        // Apprentice attributes
        Map<AttributeType, List<Double>> apprenticeAttributes = new HashMap<>();
        apprenticeAttributes.put(AttributeType.HEALTH, ApprenticeAttributes.healthList);
        apprenticeAttributes.put(AttributeType.SPEED, ApprenticeAttributes.speedList);
        apprenticeAttributes.put(AttributeType.ATTACK, ApprenticeAttributes.attackList);
        apprenticeAttributes.put(AttributeType.JUMP, ApprenticeAttributes.jumpList);
        apprenticeAttributes.put(AttributeType.DIG_SPEED, ApprenticeAttributes.digSpeedList);
        PATHWAY_ATTRIBUTES.put(BeyonderClassInit.APPRENTICE.get(), apprenticeAttributes);

        // Monster attributes
        Map<AttributeType, List<Double>> monsterAttributes = new HashMap<>();
        monsterAttributes.put(AttributeType.HEALTH, MonsterAttributes.healthList);
        monsterAttributes.put(AttributeType.SPEED, MonsterAttributes.speedList);
        monsterAttributes.put(AttributeType.ATTACK, MonsterAttributes.attackList);
        monsterAttributes.put(AttributeType.NIGHT_VISION, MonsterAttributes.nightVisionList);
        monsterAttributes.put(AttributeType.FIRE_RESISTANCE, MonsterAttributes.fireResistanceList);
        monsterAttributes.put(AttributeType.JUMP, MonsterAttributes.jumpList);
        monsterAttributes.put(AttributeType.ARMOR, MonsterAttributes.armorList);
        monsterAttributes.put(AttributeType.ARMOR_TOUGHNESS, MonsterAttributes.armorToughnessList);
        monsterAttributes.put(AttributeType.WATER_BREATHING, MonsterAttributes.waterBreathingList);
        monsterAttributes.put(AttributeType.DIG_SPEED, MonsterAttributes.digSpeedList);
        PATHWAY_ATTRIBUTES.put(BeyonderClassInit.MONSTER.get(), monsterAttributes);


        // Spectator attributes
        Map<AttributeType, List<Double>> spectatorAttributes = new HashMap<>();
        spectatorAttributes.put(AttributeType.HEALTH, SpectatorAttributes.healthList);
        spectatorAttributes.put(AttributeType.SPEED, SpectatorAttributes.speedList);
        spectatorAttributes.put(AttributeType.ATTACK, SpectatorAttributes.attackList);
        spectatorAttributes.put(AttributeType.NIGHT_VISION, SpectatorAttributes.nightVisionList);
        spectatorAttributes.put(AttributeType.FIRE_RESISTANCE, SpectatorAttributes.fireResistanceList);
        spectatorAttributes.put(AttributeType.JUMP, SpectatorAttributes.jumpList);
        spectatorAttributes.put(AttributeType.ARMOR, SpectatorAttributes.armorList);
        spectatorAttributes.put(AttributeType.ARMOR_TOUGHNESS, SpectatorAttributes.armorToughnessList);
        spectatorAttributes.put(AttributeType.DIG_SPEED, SpectatorAttributes.digSpeedList);
        PATHWAY_ATTRIBUTES.put(BeyonderClassInit.SPECTATOR.get(), spectatorAttributes);

        // Warrior Attributes
        Map<AttributeType, List<Double>> warriorAttributes = new HashMap<>();
        warriorAttributes.put(AttributeType.HEALTH, WarriorAttributes.healthList);
        warriorAttributes.put(AttributeType.SPEED, WarriorAttributes.speedList);
        warriorAttributes.put(AttributeType.ATTACK, WarriorAttributes.attackList);
        warriorAttributes.put(AttributeType.JUMP, WarriorAttributes.jumpList);
        warriorAttributes.put(AttributeType.ARMOR, WarriorAttributes.armorList);
        warriorAttributes.put(AttributeType.ARMOR_TOUGHNESS, WarriorAttributes.armorToughnessList);
        warriorAttributes.put(AttributeType.DIG_SPEED, WarriorAttributes.digSpeedList);
        PATHWAY_ATTRIBUTES.put(BeyonderClassInit.WARRIOR.get(), warriorAttributes);
    }

    private static double getAttributeWithPathway(LivingEntity entity, Attribute attribute, AttributeType attributeType) {
        if (!(entity instanceof Player)) {
            BeyonderClass pathway = BeyonderUtil.getPathway(entity);
            if (pathway != null) {
                int sequence = BeyonderUtil.getSequence(entity);
                if (sequence != -1 && sequence < 10) {
                    Map<AttributeType, List<Double>> pathwayAttrs = PATHWAY_ATTRIBUTES.get(pathway);
                    if (pathwayAttrs != null) {
                        List<Double> attributeList = pathwayAttrs.get(attributeType);
                        if (attributeList != null && sequence < attributeList.size()) {
                            return attributeList.get(sequence);
                        }
                    }
                }
            }
        }
        return get(entity, attribute);
    }

    public static double getHealth(LivingEntity entity) {
        return getAttributeWithPathway(entity, Attributes.MAX_HEALTH, AttributeType.HEALTH);
    }

    public static double getMovementSpeed(LivingEntity entity) {
        return getAttributeWithPathway(entity, Attributes.MOVEMENT_SPEED, AttributeType.SPEED);
    }

    public static double getSwimSpeed(LivingEntity entity) {
        return get(entity, ForgeMod.SWIM_SPEED.get());
    }

    public static double getAttackDamage(LivingEntity entity) {
        return getAttributeWithPathway(entity, Attributes.ATTACK_DAMAGE, AttributeType.ATTACK);
    }

    public static double getArmor(LivingEntity entity) {
        return getAttributeWithPathway(entity, Attributes.ARMOR, AttributeType.ARMOR);
    }

    public static double getArmorToughness(LivingEntity entity) {
        return getAttributeWithPathway(entity, Attributes.ARMOR_TOUGHNESS, AttributeType.ARMOR_TOUGHNESS);
    }

    public static double getAttackSpeed(LivingEntity entity) {
        return get(entity, Attributes.ATTACK_SPEED);
    }

    public static double getKnockbackResistance(LivingEntity entity) {
        return get(entity, Attributes.KNOCKBACK_RESISTANCE);
    }

    // Custom Attributes - Get Methods
    public static double getNightVision(LivingEntity entity) {
        return getAttributeWithPathway(entity, ModAttributes.NIGHT_VISION.get(), AttributeType.NIGHT_VISION);
    }

    public static double getFireResistance(LivingEntity entity) {
        return getAttributeWithPathway(entity, ModAttributes.FIRE_RESISTANCE.get(), AttributeType.FIRE_RESISTANCE);
    }

    public static double getJumpBoost(LivingEntity entity) {
        return getAttributeWithPathway(entity, ModAttributes.JUMP_BOOST.get(), AttributeType.JUMP);
    }

    public static double getWaterBreathing(LivingEntity entity) {
        return getAttributeWithPathway(entity, ModAttributes.WATER_BREATHING.get(), AttributeType.WATER_BREATHING);
    }

    public static double getDigSpeed(LivingEntity entity) {
        return getAttributeWithPathway(entity, ModAttributes.DIG_SPEED.get(), AttributeType.DIG_SPEED);
    }

    public static void setHealth(LivingEntity entity, double value) {
        set(entity, Attributes.MAX_HEALTH, value);
    }


    public static void setMovementSpeed(LivingEntity entity, double value) {
        set(entity, Attributes.MOVEMENT_SPEED, value);
    }

    public static void setSwimSpeed(LivingEntity entity, double value) {
        set(entity, ForgeMod.SWIM_SPEED.get(), value);
    }

    public static void setAttackDamage(LivingEntity entity, double value) {
        set(entity, Attributes.ATTACK_DAMAGE, value);
    }

    public static void setArmor(LivingEntity entity, double value) {
        set(entity, Attributes.ARMOR, value);
    }

    public static void setArmorToughness(LivingEntity entity, double value) {
        set(entity, Attributes.ARMOR_TOUGHNESS, value);
    }

    public static void setAttackSpeed(LivingEntity entity, double value) {
        set(entity, Attributes.ATTACK_SPEED, value);
    }

    public static void setKnockbackResistance(LivingEntity entity, double value) {
        set(entity, Attributes.KNOCKBACK_RESISTANCE, value);
    }

    // Custom Attributes - Set Methods
    public static void setNightVision(LivingEntity entity, double value) {
        set(entity, ModAttributes.NIGHT_VISION.get(), value);
    }

    public static void setFireResistance(LivingEntity entity, double value) {
        set(entity, ModAttributes.FIRE_RESISTANCE.get(), value);
    }

    public static void setJumpBoost(LivingEntity entity, double value) {
        set(entity, ModAttributes.JUMP_BOOST.get(), value);
    }

    public static void setWaterBreathing(LivingEntity entity, double value) {
        set(entity, ModAttributes.WATER_BREATHING.get(), value);
    }

    public static void setDigSpeed(LivingEntity entity, double value) {
        set(entity, ModAttributes.DIG_SPEED.get(), value);
    }

    // Private helper methods
    private static double get(LivingEntity entity, net.minecraft.world.entity.ai.attributes.Attribute attribute) {
        if (entity == null || attribute == null) {
            return 0.0;
        }

        var instance = entity.getAttribute(attribute);
        if (instance == null) {
            return 0.0;
        }

        return instance.getValue();
    }

    private static void set(LivingEntity entity, net.minecraft.world.entity.ai.attributes.Attribute attribute, double value) {
        if (entity == null || attribute == null) {
            return;
        }

        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }
}