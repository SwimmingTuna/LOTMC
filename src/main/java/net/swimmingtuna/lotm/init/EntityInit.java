package net.swimmingtuna.lotm.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.*;

public class EntityInit {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, LOTM.MOD_ID);

    public static final RegistryObject<EntityType<ColoredBoxEntity>> COLORED_BOX_ENTITY =
            ENTITIES.register("colored_box_entity", () -> EntityType.Builder.<ColoredBoxEntity>of(ColoredBoxEntity::new, MobCategory.MISC)
                    .sized(5.0f,5.0f).clientTrackingRange(400).build(new ResourceLocation(LOTM.MOD_ID, "colored_box_entity").toString()));
    public static final RegistryObject<EntityType<AqueousLightEntity>> AQUEOUS_LIGHT_ENTITY_DROWN =
            ENTITIES.register("aqueous_light", () -> EntityType.Builder.<AqueousLightEntity>of(AqueousLightEntity::new, MobCategory.MISC)
                    .sized(0.5f,0.5f).clientTrackingRange(100).build(new ResourceLocation(LOTM.MOD_ID, "aqueous_light").toString()));
    public static final RegistryObject<EntityType<MercuryEntity>> MERCURY_ENTITY =
            ENTITIES.register("mercury_entity", () -> EntityType.Builder.<MercuryEntity>of(MercuryEntity::new, MobCategory.MISC)
                    .sized(0.5f,0.5f).clientTrackingRange(100).build(new ResourceLocation(LOTM.MOD_ID, "mercury_entity").toString()));
    public static final RegistryObject<EntityType<DawnRayEntity>> DAWN_RAY_ENTITY =
            ENTITIES.register("dawn_ray_entity", () -> EntityType.Builder.<DawnRayEntity>of(DawnRayEntity::new, MobCategory.MISC)
                    .sized(0.5f,0.5f).clientTrackingRange(400).build(new ResourceLocation(LOTM.MOD_ID, "dawn_ray_entity").toString()));
    public static final RegistryObject<EntityType<TwilightLightEntity>> TWILIGHT_LIGHT =
            ENTITIES.register("twilight_light_entity", () -> EntityType.Builder.<TwilightLightEntity>of(TwilightLightEntity::new, MobCategory.MISC)
                    .sized(0.5f,0.5f).clientTrackingRange(400).build(new ResourceLocation(LOTM.MOD_ID, "twilight_light_entity").toString()));
    public static final RegistryObject<EntityType<GuardianBoxEntity>> GUARDIAN_BOX_ENTITY =
            ENTITIES.register("guardian_box_entity", () -> EntityType.Builder.<GuardianBoxEntity>of(GuardianBoxEntity::new, MobCategory.MISC)
                    .sized(5.0f,5.0f).clientTrackingRange(400).build(new ResourceLocation(LOTM.MOD_ID, "guardian_box_entity").toString()));
    public static final RegistryObject<EntityType<DeathKnellBulletEntity>> DEATH_KNELL_BULLET_ENTITY =
            ENTITIES.register("death_knell_bullet", () -> EntityType.Builder.<DeathKnellBulletEntity>of(DeathKnellBulletEntity::new, MobCategory.MISC)
                    .sized(0.5f,0.5f).clientTrackingRange(200).build(new ResourceLocation(LOTM.MOD_ID, "death_knell_bullet").toString()));
    public static final RegistryObject<EntityType<MCLightningBoltEntity>> MC_LIGHTNING_BOLT =
            ENTITIES.register("mc_lightning_bolt", () -> EntityType.Builder.<MCLightningBoltEntity>of(MCLightningBoltEntity::new, MobCategory.MISC)
                    .sized(0.5f,0.5f).build(new ResourceLocation(LOTM.MOD_ID, "mc_lightning_bolt").toString()));
    public static final RegistryObject<EntityType<WaterColumnEntity>> WATER_COLUMN_ENTITY =
            ENTITIES.register("water_column", () -> EntityType.Builder.<WaterColumnEntity>of(WaterColumnEntity::new, MobCategory.MISC)
                    .sized(0.5f,0.5f).build(new ResourceLocation(LOTM.MOD_ID, "water_column").toString()));
    public static final RegistryObject<EntityType<RoarEntity>> ROAR_ENTITY =
            ENTITIES.register("roar_entity", () -> EntityType.Builder.<RoarEntity>of(RoarEntity::new, MobCategory.MISC)
                    .sized(2.0f,2.0f).clientTrackingRange(200).build(new ResourceLocation(LOTM.MOD_ID, "roar_entity").toString()));
    public static final RegistryObject<EntityType<SpaceFragmentationEntity>> SPACE_FRAGMENTATION_ENTITY =
            ENTITIES.register("space_fragmentation_entity", () -> EntityType.Builder.<SpaceFragmentationEntity>of(SpaceFragmentationEntity::new, MobCategory.MISC)
                    .sized(2.0f, 10.10029375f).clientTrackingRange(300).build(new ResourceLocation(LOTM.MOD_ID,"space_fragmentation_entity").toString()));
    public static final RegistryObject<EntityType<StormSealEntity>> STORM_SEAL_ENTITY =
            ENTITIES.register("storm_seal_entity", () -> EntityType.Builder.<StormSealEntity>of(StormSealEntity::new, MobCategory.MISC)
                    .sized(2.0f,2.0f).build(new ResourceLocation(LOTM.MOD_ID, "storm_seal_entity").toString()));
    public static final RegistryObject<EntityType<AqueousLightEntityPull>> AQUEOUS_LIGHT_ENTITY_PULL =
            ENTITIES.register("aqueous_light_pull", () -> EntityType.Builder.<AqueousLightEntityPull>of(AqueousLightEntityPull::new, MobCategory.MISC)
                    .sized(0.5f,0.5f).clientTrackingRange(100).build("aqueous_light_pull"));
    public static final RegistryObject<EntityType<AqueousLightEntityPush>> AQUEOUS_LIGHT_ENTITY_PUSH =
            ENTITIES.register("aqueous_light_push", () -> EntityType.Builder.<AqueousLightEntityPush>of(AqueousLightEntityPush::new, MobCategory.MISC)
                    .sized(0.5f,0.5f).clientTrackingRange(100).build("aqueous_light_push"));
    public static final RegistryObject<EntityType<DragonBreathEntity>> DRAGON_BREATH_ENTITY =
            ENTITIES.register("dragon_breath", () -> EntityType.Builder.<DragonBreathEntity>of(DragonBreathEntity::new, MobCategory.MISC)
                    .sized(2.0f,2.0f).clientTrackingRange(300).build("dragon_breath"));
    public static final RegistryObject<EntityType<MeteorEntity>> METEOR_ENTITY =
            ENTITIES.register("meteor", () -> EntityType.Builder.<MeteorEntity>of(MeteorEntity::new, MobCategory.MISC)
                    .sized(2.0f,2.0f).clientTrackingRange(300).build("meteor"));
    public static final RegistryObject<EntityType<SwordOfTwilightEntity>> SWORD_OF_TWILIGHT_ENTITY =
            ENTITIES.register("sword_of_twilight_entity", () -> EntityType.Builder.<SwordOfTwilightEntity>of(SwordOfTwilightEntity::new, MobCategory.MISC)
                    .sized(2.0f, 2.0f).clientTrackingRange(1000).updateInterval(1).setShouldReceiveVelocityUpdates(true).build("sword_of_twilight_entity"));
    public static final RegistryObject<EntityType<SpaceRiftEntity>> SPACE_RIFT_ENTITY =
            ENTITIES.register("space_rift_entity", () -> EntityType.Builder.<SpaceRiftEntity>of(SpaceRiftEntity::new, MobCategory.MISC)
                    .sized(1f,1f).clientTrackingRange(100).build("space_rift_entity"));
    public static final RegistryObject<EntityType<SpatialCageEntity>> SPATIAL_CAGE_ENTITY =
            ENTITIES.register("spatial_cage_entity", () -> EntityType.Builder.<SpatialCageEntity>of(SpatialCageEntity::new, MobCategory.MISC)
                    .sized(1f,1f).clientTrackingRange(100).build("spatial_cage_entity"));
    public static final RegistryObject<EntityType<LightningBallEntity>> LIGHTNING_BALL =
            ENTITIES.register("lightningball", () -> EntityType.Builder.<LightningBallEntity>of(LightningBallEntity::new, MobCategory.MISC)
                    .sized(1.0f,1.0f).clientTrackingRange(300).build("lightningball"));
    public static final RegistryObject<EntityType<CircleEntity>> CIRCLE_ENTITY =
            ENTITIES.register("circle", () -> EntityType.Builder.<CircleEntity>of(CircleEntity::new, MobCategory.MISC)
                    .sized(1.0f,1.0f).build("circle"));
    public static final RegistryObject<EntityType<DivineHandRightEntity>> DIVINE_HAND_RIGHT_ENTITY =
            ENTITIES.register("divine_hand_right_entity", () -> EntityType.Builder.<DivineHandRightEntity>of(DivineHandRightEntity::new, MobCategory.MISC)
                    .sized(1.0f,1.0f).build("divine_hand_right_entity"));
    public static final RegistryObject<EntityType<DivineHandLeftEntity>> DIVINE_HAND_LEFT_ENTITY =
            ENTITIES.register("divine_hand_left_entity", () -> EntityType.Builder.<DivineHandLeftEntity>of(DivineHandLeftEntity::new, MobCategory.MISC)
                    .sized(1.0f,1.0f).build("divine_hand_left_entity"));
    public static final RegistryObject<EntityType<MeteorTrailEntity>> METEOR_TRAIL_ENTITY =
            ENTITIES.register("meteortrailentity", () -> EntityType.Builder.<MeteorTrailEntity>of(MeteorTrailEntity::new, MobCategory.MISC)
                    .sized(1.0f,1.0f).clientTrackingRange(200).build("meteortrailentity"));
    public static final RegistryObject<EntityType<StoneEntity>> STONE_ENTITY =
            ENTITIES.register("stone", () -> EntityType.Builder.<StoneEntity>of(StoneEntity::new, MobCategory.MISC)
                    .sized(1.0f,1.0f).clientTrackingRange(200).clientTrackingRange(64).updateInterval(1).build("stone"));
    public static final RegistryObject<EntityType<NetherrackEntity>> NETHERRACK_ENTITY =
            ENTITIES.register("netherrack", () -> EntityType.Builder.<NetherrackEntity>of(NetherrackEntity::new, MobCategory.MISC)
                    .sized(1.0f,1.0f).clientTrackingRange(200).clientTrackingRange(4).updateInterval(20).build("netherrack"));
    public static final RegistryObject<EntityType<EndStoneEntity>> ENDSTONE_ENTITY =
            ENTITIES.register("endstone", () -> EntityType.Builder.<EndStoneEntity>of(EndStoneEntity::new, MobCategory.MISC)
                    .sized(1.0f,1.0f).clientTrackingRange(200).clientTrackingRange(4).updateInterval(20).build("endstone"));
    public static final RegistryObject<EntityType<LavaEntity>> LAVA_ENTITY =
            ENTITIES.register("lava", () -> EntityType.Builder.<LavaEntity>of(LavaEntity::new, MobCategory.MISC)
                    .sized(1.0f,1.0f).clientTrackingRange(200).clientTrackingRange(4).updateInterval(20).build("lava"));
    public static final RegistryObject<EntityType<TornadoEntity>> TORNADO_ENTITY =
            ENTITIES.register("tornado", () -> EntityType.Builder.<TornadoEntity>of(TornadoEntity::new, MobCategory.MISC)
                    .sized(1.0f,1.0f).clientTrackingRange(100).updateInterval(20).build("tornado"));
    public static final RegistryObject<EntityType<HurricaneOfLightEntity>> HURRICANE_OF_LIGHT_ENTITY =
            ENTITIES.register("hurricane_of_light", () -> EntityType.Builder.<HurricaneOfLightEntity>of(HurricaneOfLightEntity::new, MobCategory.MISC)
                    .sized(1.0f,1.0f).clientTrackingRange(200).updateInterval(20).build("hurricane_of_light"));
    public static final RegistryObject<EntityType<MeteorNoLevelEntity>> METEOR_NO_LEVEL_ENTITY =
            ENTITIES.register("meteor_no_hurt", () -> EntityType.Builder.<MeteorNoLevelEntity>of(MeteorNoLevelEntity::new, MobCategory.MISC)
                    .sized(2.0f,2.0f).clientTrackingRange(300).build("meteor_no_hurt"));
    public static final RegistryObject<EntityType<ApprenticeDoorEntity>> APPRENTICE_DOOR_ENTITY =
            ENTITIES.register("apprentice_door_entity", () -> EntityType.Builder.<ApprenticeDoorEntity>of(ApprenticeDoorEntity::new, MobCategory.MISC)
                    .sized(0.5f,2f).clientTrackingRange(100).build("apprentice_door_entity"));
    public static final RegistryObject<EntityType<FlashEntity>> FLASH_ENTITY =
            ENTITIES.register("flash", () -> EntityType.Builder.<FlashEntity>of(FlashEntity::new, MobCategory.MISC)
                    .sized(0.5f,0.5f).clientTrackingRange(200).build(new ResourceLocation(LOTM.MOD_ID, "flash").toString()));
    public static final RegistryObject<EntityType<SilverLightEntity>> SILVER_LIGHT_ENTITY =
            ENTITIES.register("silver_beam_entity", () -> EntityType.Builder.<SilverLightEntity>of(SilverLightEntity::new, MobCategory.MISC)
                    .sized(2.5f,2.5f).clientTrackingRange(400).build(new ResourceLocation(LOTM.MOD_ID, "silver_beam_entity").toString()));
    public static final RegistryObject<EntityType<SpearOfDawnEntity>> SPEAR_OF_DAWN_ENTITY =
            ENTITIES.register("spear_of_dawn_entity", () -> EntityType.Builder.<SpearOfDawnEntity>of(SpearOfDawnEntity::new, MobCategory.MISC)
                    .sized(1.0f,1.0f).clientTrackingRange(400).build(new ResourceLocation(LOTM.MOD_ID, "spear_of_dawn_entity").toString()));
    public static final RegistryObject<EntityType<GlobeOfTwilightEntity>> GLOBE_OF_TWILIGHT_ENTITY =
            ENTITIES.register("globe_of_twilight_entity", () -> EntityType.Builder.<GlobeOfTwilightEntity>of(GlobeOfTwilightEntity::new, MobCategory.MISC)
                    .sized(1.0f,1.0f).clientTrackingRange(200).build(new ResourceLocation(LOTM.MOD_ID, "globe_of_twilight_entity").toString()));
    public static final RegistryObject<EntityType<WindBladeEntity>> WIND_BLADE_ENTITY =
            ENTITIES.register("wind_blade", () -> EntityType.Builder.<WindBladeEntity>of(WindBladeEntity::new, MobCategory.MISC)
                    .sized(0.5f,0.5f).clientTrackingRange(100).clientTrackingRange(4).updateInterval(20).build("wind_blade"));
    public static final RegistryObject<EntityType<LowSequenceDoorEntity>> LOW_SEQUENCE_DOOR_ENTITY =
            ENTITIES.register("low_sequence_door_entity", () -> EntityType.Builder.<LowSequenceDoorEntity>of(LowSequenceDoorEntity::new, MobCategory.MISC)
                    .sized(0.5f,2f).clientTrackingRange(100).build("low_sequence_door_entity"));
    public static final RegistryObject<EntityType<MercuryCageEntity>> MERCURY_CAGE_ENTITY =
            ENTITIES.register("mercury_cage_entity", () -> EntityType.Builder.<MercuryCageEntity>of(MercuryCageEntity::new, MobCategory.MISC)
                    .sized(1.0f,1.0f).clientTrackingRange(400).build("mercury_cage_entity"));
    public static final RegistryObject<EntityType<MercuryPortalEntity>> MERCURY_PORTAL_ENTITY =
            ENTITIES.register("mercury_portal_entity", () -> EntityType.Builder.<MercuryPortalEntity>of(MercuryPortalEntity::new, MobCategory.MISC)
                    .sized(0.5f,0.5f).clientTrackingRange(100).build("mercury_portal_entity"));
    public static final RegistryObject<EntityType<WindCushionEntity>> WIND_CUSHION_ENTITY =
            ENTITIES.register("wind_cushion", () -> EntityType.Builder.<WindCushionEntity>of(WindCushionEntity::new, MobCategory.MISC)
                    .sized(0.5f,0.5f).build("wind_cushion"));
    public static final RegistryObject<EntityType<LightningEntity>> LIGHTNING_ENTITY =
            ENTITIES.register("lightning_entity", () -> EntityType.Builder.<LightningEntity>of(LightningEntity::new, MobCategory.MISC)
                    .sized(0.5f,0.5f).clientTrackingRange(400).build("lightning_entity"));
    public static final RegistryObject<EntityType<CustomFallingBlockEntity>> CUSTOM_FALLING_BLOCK_ENTITY =
            ENTITIES.register("custom_falling_block_entity", () -> EntityType.Builder.<CustomFallingBlockEntity>of(CustomFallingBlockEntity::new, MobCategory.MISC)
                    .sized(0.98f,0.98f).clientTrackingRange(10).updateInterval(20).build("custom_falling_block_entity"));
    public static final RegistryObject<EntityType<WhisperOfCorruptionEntity>> WHISPERS_OF_CORRUPTION_ENTITY =
            ENTITIES.register("whisperofcorruption", () -> EntityType.Builder.<WhisperOfCorruptionEntity>of(WhisperOfCorruptionEntity::new, MobCategory.MISC)
                    .sized(1.0f,1.0f).build("whisperofcorruption"));
    public static final RegistryObject<EntityType<DimensionalSightSealEntity>> DIMENSIONAL_SIGHT_SEAL_ENTITY =
            ENTITIES.register("dimensionalsightseal", () -> EntityType.Builder.<DimensionalSightSealEntity>of(DimensionalSightSealEntity::new, MobCategory.MISC)
                    .sized(5.0f,5.0f).build("dimensionalsightseal"));
    public static final RegistryObject<EntityType<LuckBottleEntity>> LUCK_BOTTLE_ENTITY =
            ENTITIES.register("luck_bottle_entity", () -> EntityType.Builder.<LuckBottleEntity>of(LuckBottleEntity::new, MobCategory.MISC)
                    .sized(0.5f,0.5f).clientTrackingRange(100).build("luck_bottle_entity"));
    public static final RegistryObject<EntityType<PlayerMobEntity>> PLAYER_MOB_ENTITY = ENTITIES.register("player_mob", () ->
            EntityType.Builder.<PlayerMobEntity>of(PlayerMobEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(300)
                    .build(new ResourceLocation(LOTM.MOD_ID, "player_mob").toString())
    );
    public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(PLAYER_MOB_ENTITY.get(), PlayerMobEntity.registerAttributes().build());
    }


    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }
}
