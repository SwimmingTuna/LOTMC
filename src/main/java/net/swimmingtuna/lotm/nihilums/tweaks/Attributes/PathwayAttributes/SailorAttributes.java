package net.swimmingtuna.lotm.nihilums.tweaks.Attributes.PathwayAttributes;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.swimmingtuna.lotm.nihilums.tweaks.Attributes.BaseAttributes;
import net.swimmingtuna.lotm.attributes.ModAttributes;

import java.util.List;

public class SailorAttributes extends BaseAttributes {
    public static final List<Double> healthList
            = List.of(80.0, 65.0, 60.0, 50.0, 45.0, 35.0, 30.0, 30.0, 25.0, 23.0);
    public static final List<Double> speedList
            = List.of(0.1, 0.09, 0.08, 0.07, 0.07, 0.06, 0.05, 0.04, 0.02, 0.01);
    public static final List<Double> attackList
            = List.of(18.0, 16.0, 14.0, 12.0, 10.0, 8.0, 5.0, 4.0, 3.0, 2.0);
    public static final List<Double> nightVisionList
            = List.of(3.0, 3.0, 3.0, 3.0, 2.0, 2.0, 1.5, 1.0, 1.0, 0.5);
    public static final List<Double> fireResistanceList =
            List.of(3.0, 3.0, 3.0, 2.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    public static final List<Double> jumpList
            = List.of(0.125, 0.125, 0.125, 0.125, 0.125, 0.125, 0.125, 0.08, 0.08, 0.055);
    public static final List<Double> armorList
            = List.of(20.0, 18.0, 16.0, 14.0, 12.0, 10.0, 8.0, 6.0, 4.0, 2.0);
    public static final List<Double> armorToughnessList
            = List.of(15.0, 13.0, 11.0, 9.0, 8.0, 6.0, 5.0, 4.0, 3.0, 2.0);
    public static final List<Double> digSpeedList
            = List.of(10.0, 9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0);
    public static final List<Double> waterBreathingList
            = List.of(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0);

    public static void applyAll(LivingEntity entity, int seq) {
        apply(entity.getAttribute(Attributes.MAX_HEALTH),
                healthBoostID, healthList.get(seq) - 20.0, "HealthBoost");
        apply(entity.getAttribute(Attributes.MOVEMENT_SPEED),
                speedID, speedList.get(seq), "SpeedBoost");
        apply(entity.getAttribute(Attributes.ATTACK_DAMAGE),
                attackID, attackList.get(seq), "AttackBoost");
        apply(entity.getAttribute(Attributes.ARMOR),
                armorID, armorList.get(seq), "ArmorBoost");
        apply(entity.getAttribute(Attributes.ARMOR_TOUGHNESS),
                armorToughnessID, armorToughnessList.get(seq), "ArmorToughnessBoost");


        apply(entity.getAttribute(ModAttributes.NIGHT_VISION.get()),
                nightVisionID, nightVisionList.get(seq), "NightVision");
        apply(entity.getAttribute(ModAttributes.FIRE_RESISTANCE.get()),
                fireResistanceID, fireResistanceList.get(seq), "FireResistance");
        apply(entity.getAttribute(ModAttributes.JUMP_BOOST.get()),
                jumpID, jumpList.get(seq), "JumpBoost");
        apply(entity.getAttribute(ModAttributes.DIG_SPEED.get()),
                digSpeedID, digSpeedList.get(seq), "DigSpeed");
        apply(entity.getAttribute(ModAttributes.WATER_BREATHING.get()),
                waterBreathingID, waterBreathingList.get(seq), "WaterBreathingBoost");
    }
}