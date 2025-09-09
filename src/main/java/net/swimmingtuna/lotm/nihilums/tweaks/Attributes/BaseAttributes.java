package net.swimmingtuna.lotm.nihilums.tweaks.Attributes;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.swimmingtuna.lotm.attributes.ModAttributes;

import java.util.Objects;
import java.util.UUID;

public class BaseAttributes {
    protected static final UUID healthBoostID = UUID.fromString("a3a90fac-39d0-4b75-9990-8211f70e0a0f");
    protected static final UUID speedID = UUID.fromString("1cd27c58-f3e8-46d9-8990-44a9f14dfc28");
    protected static final UUID attackID = UUID.fromString("b29d4a1d-20c5-42aa-a984-1c4bc77ccdad");
    protected static final UUID nightVisionID = UUID.fromString("12cd2ed8-a4f6-4e78-945d-f10e4559aa2e");
    protected static final UUID fireResistanceID = UUID.fromString("9dbc9d39-a22b-445d-85c9-265ca4bd26b6");
    protected static final UUID jumpID = UUID.fromString("d745714c-cdc4-4e4e-bb24-50d07c46f853");
    protected static final UUID armorID = UUID.fromString("66c23c87-9e2b-4823-8b04-58fe34f9ad9d");
    protected static final UUID armorToughnessID = UUID.fromString("d41c5348-216e-4c94-a0fd-8bc5eb4da8ca");
    protected static final UUID waterBreathingID = UUID.fromString("52900b28-d96b-489b-b5dd-83e3d7f15443");
    protected static final UUID dolphinsGraceID = UUID.fromString("bb491edc-944b-4758-839d-793538a4b09a");
    protected static final UUID digSpeedID = UUID.fromString("b23dc144-8c77-4d09-94b2-2982573812f3");


    public static void clean(AttributeInstance attr, UUID id){
        if(attr.getModifier(id) != null)
            attr.removeModifier(id);
    }

    public static void apply(AttributeInstance attr, UUID id, Double value, String name){
        if(attr != null) {
            clean(attr, id);

            attr.addPermanentModifier(new AttributeModifier(
                    id, name, value, AttributeModifier.Operation.ADDITION));
        }
    }

    public static void cleanAll(LivingEntity entity){
        clean(Objects.requireNonNull(entity.getAttribute(Attributes.MAX_HEALTH)), healthBoostID);

        clean(Objects.requireNonNull(entity.getAttribute(Attributes.MOVEMENT_SPEED)), speedID);
        clean(Objects.requireNonNull(entity.getAttribute(Attributes.ATTACK_DAMAGE)), attackID);
        clean(Objects.requireNonNull(entity.getAttribute(Attributes.ARMOR)), armorID);
        clean(Objects.requireNonNull(entity.getAttribute(Attributes.ARMOR_TOUGHNESS)), armorToughnessID);

        clean(Objects.requireNonNull(entity.getAttribute(ModAttributes.NIGHT_VISION.get())), nightVisionID);
        clean(Objects.requireNonNull(entity.getAttribute(ModAttributes.FIRE_RESISTANCE.get())), fireResistanceID);
        clean(Objects.requireNonNull(entity.getAttribute(ModAttributes.JUMP_BOOST.get())), jumpID);
        clean(Objects.requireNonNull(entity.getAttribute(ModAttributes.WATER_BREATHING.get())), waterBreathingID);
        clean(Objects.requireNonNull(entity.getAttribute(ModAttributes.DIG_SPEED.get())), digSpeedID);
    }
}
