package net.swimmingtuna.lotm.attributes;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.swimmingtuna.lotm.LOTM;

public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, LOTM.MOD_ID);

    public static final RegistryObject<Attribute> NIGHT_VISION = ATTRIBUTES.register("night_vision",
            ()-> new RangedAttribute("attribute.lotm.night_vision",1.0D,1.0D,4).setSyncable(true));
    public static final RegistryObject<Attribute> FIRE_RESISTANCE = ATTRIBUTES.register("fire_resistance",
            ()-> new RangedAttribute("attribute.lotm.fire_resistance",0.0D,0.0D,3).setSyncable(true));
    public static final RegistryObject<Attribute> JUMP_BOOST = ATTRIBUTES.register("jump_boost",
            ()-> new RangedAttribute("attribute.lotm.jump_boost",0.0D,0.0D,10).setSyncable(true));
    public static final RegistryObject<Attribute> WATER_BREATHING = ATTRIBUTES.register("water_breathing",
            ()-> new RangedAttribute("attribute.lotm.water_breathing",0.0D,0.0D,1).setSyncable(true));
    public static final RegistryObject<Attribute> DIG_SPEED = ATTRIBUTES.register("dig_speed",
            ()-> new RangedAttribute("attribute.lotm.dig_speed",1.0D,1.0D,100).setSyncable(true));

    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }

}