package net.swimmingtuna.lotm.attributes;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.swimmingtuna.lotm.LOTM;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = LOTM.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AttributeAdder {

    @SubscribeEvent
    public static void modifyAttributes(EntityAttributeModificationEvent event) {
        // Add custom attributes to ALL living entities
        addToAllLivingEntities(event, ModAttributes.NIGHT_VISION);
        addToAllLivingEntities(event, ModAttributes.FIRE_RESISTANCE);
        addToAllLivingEntities(event, ModAttributes.JUMP_BOOST);
        addToAllLivingEntities(event, ModAttributes.WATER_BREATHING);
        addToAllLivingEntities(event, ModAttributes.DIG_SPEED);

        event.add(EntityType.PLAYER, ModAttributes.NIGHT_VISION.get());
        event.add(EntityType.PLAYER, ModAttributes.FIRE_RESISTANCE.get());
        event.add(EntityType.PLAYER, ModAttributes.JUMP_BOOST.get());
        event.add(EntityType.PLAYER, ModAttributes.WATER_BREATHING.get());
        event.add(EntityType.PLAYER, ModAttributes.DIG_SPEED.get());
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    private static void addToAllLivingEntities(EntityAttributeModificationEvent event, Supplier<Attribute>... attributes) {
        ForgeRegistries.ENTITY_TYPES.getValues().stream()
                .filter(entityType -> LivingEntity.class.isAssignableFrom(entityType.getBaseClass()))
                .forEach(entityType -> {
                    for (Supplier<Attribute> attribute : attributes) {
                        event.add((EntityType<? extends LivingEntity>) entityType, attribute.get());
                    }
                });
    }
}