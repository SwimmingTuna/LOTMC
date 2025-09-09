package net.swimmingtuna.lotm.nihilums.tweaks.Attributes.PathwayAttributes;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.swimmingtuna.lotm.nihilums.tweaks.Attributes.BaseAttributes;
import net.swimmingtuna.lotm.attributes.ModAttributes;

import java.util.List;

public class SpectatorAttributes extends BaseAttributes {
    public static final List<Double> healthList
            = List.of(45.0, 40.0, 40.0, 35.0, 32.0, 28.0, 28.0, 23.0, 20.0, 20.0);
    public static final List<Double> speedList
            = List.of(0.08, 0.07, 0.06, 0.06, 0.06, 0.04, 0.04, 0.02, 0.0, 0.0);
    public static final List<Double> attackList
            = List.of(16.0, 12.0, 12.0, 9.0, 9.0, 6.0, 6.0, 3.0, 0.0, 0.0);
    public static final List<Double> nightVisionList
            = List.of(3.0, 3.0, 3.0, 3.0, 3.0, 2.5, 2.0, 1.5, 1.5, 1.0);
    public static final List<Double> fireResistanceList =
            List.of(3.0, 3.0, 3.0, 3.0, 3.0, 2.0, 1.0, 0.0, 0.0, 0.0);
    public static final List<Double> jumpList
            = List.of(0.125, 0.125, 0.125, 0.125, 0.125, 0.125, 0.125, 0.055, 0.0, 0.0);
    public static final List<Double> armorList
            = List.of(20.0, 18.0, 15.0, 12.0, 10.0, 8.0, 6.0, 0.0, 0.0, 0.0);
    public static final List<Double> armorToughnessList
            = List.of(14.0, 12.0, 10.0, 8.0, 8.0, 6.0, 4.0, 0.0, 0.0, 0.0);
    public static final List<Double> digSpeedList
            = List.of(4.0, 3.0, 3.0, 2.0, 2.0, 1.0, 0.0, 0.0, 0.0, 0.0);

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
    }
}