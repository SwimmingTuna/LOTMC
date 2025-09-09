package net.swimmingtuna.lotm;

import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PotionItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.client.ClientConfigs;
import net.swimmingtuna.lotm.client.Configs;
import net.swimmingtuna.lotm.entity.Renderers.*;
import net.swimmingtuna.lotm.events.ClientEvents;
import net.swimmingtuna.lotm.init.*;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.screen.ConcealedBundleScreen;
import net.swimmingtuna.lotm.screen.PotionCauldronScreen;
import net.swimmingtuna.lotm.attributes.ModAttributes;
import net.swimmingtuna.lotm.util.CustomEntityDataSerializers;
import net.swimmingtuna.lotm.util.PlayerMobs.NameManager;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import net.swimmingtuna.lotm.world.worldgen.biome.BiomeModifierRegistry;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Mod(LOTM.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class LOTM {

    public static final int NEW_STRUCTURE_SIZE = 512;
    public static Supplier<Boolean> fadeOut;
    public static Supplier<Integer> fadeTicks;
    public static Supplier<Double> maxBrightness;
    public static Supplier<Double> fadeRate = () -> maxBrightness.get() / fadeTicks.get();

    public static final String MOD_ID = "lotm";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static int getMaxStackCount() {
        return 1028;
    }



    public LOTM() {
        Set<String> classNames = new HashSet<>();
        List<ModFileScanData> modFileScanData = ModList.get().getAllScanData();
        BeyonderHolderAttacher.register();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        BeyonderClassInit.BEYONDER_CLASS.register(modEventBus);
        DamageTypeInit.DAMAGE_TYPES.register(modEventBus);
        MinecraftForge.EVENT_BUS.addListener(this::serverAboutToStart);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Configs.commonSpec);
        EnchantmentInit.register(modEventBus);
        CreativeTabInit.register(modEventBus);
        BiomeInit.register(modEventBus);
        DamageTypeInit.DAMAGE_TYPES.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(new GameRuleInit());
        ChunkGeneratorInit.register(modEventBus);
        ItemInit.register(modEventBus);
        BlockInit.register(modEventBus);
        BlockEntityInit.register(modEventBus);
        ModEffects.register(modEventBus);
        ModAttributes.register(modEventBus);
        EntityInit.register(modEventBus);
        modEventBus.addListener(EntityInit::registerEntityAttributes);
        CommandInit.ARGUMENT_TYPES.register(modEventBus);
        ParticleInit.register(modEventBus);
        SoundInit.register(modEventBus);
        MenuInit.register(modEventBus);
        CustomEntityDataSerializers.register();
        GeckoLib.initialize();

        modEventBus.addListener(ClientEvents::onRegisterOverlays);
        BiomeModifierRegistry.BIOME_MODIFIER_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfigs.SPEC, String.format("%s-client.toml", LOTM.MOD_ID));
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);
        MinecraftForge.EVENT_BUS.addListener(CommandInit::onCommandRegistration);

    }

    private void serverAboutToStart(ServerAboutToStartEvent event) {
        NameManager.INSTANCE.init();
    }


    @SubscribeEvent
    public static void commonSetup(final FMLCommonSetupEvent event) {
        {
            event.enqueueWork(() -> {
                for (Item item : ForgeRegistries.ITEMS) {
                    if (item instanceof PotionItem) {
                        try {
                            Field stackSizeField = Item.class.getDeclaredField("f_41370_");
                            stackSizeField.setAccessible(true);
                            stackSizeField.set(item, 64);
                            LOGGER.info("stacking_potions: stack size for potion " + item.getDescriptionId() + " changed to 64");
                        } catch (Exception ex) {
                            LOGGER.error("stacking_potions: " + ex.toString());
                        }
                    }
                }
            });
        }
        LOTMNetworkHandler.register();
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityInit.AQUEOUS_LIGHT_ENTITY_DROWN.get(), AqueousLightEntityRenderer::new);
        event.registerEntityRenderer(EntityInit.SPEAR_OF_DAWN_ENTITY.get(), SpearOfDawnRenderer::new);
        event.registerEntityRenderer(EntityInit.AQUEOUS_LIGHT_ENTITY_PUSH.get(), AqueousLightEntityPushRenderer::new);
        event.registerEntityRenderer(EntityInit.AQUEOUS_LIGHT_ENTITY_PULL.get(), AqueousLightEntityPullRenderer::new);
        event.registerEntityRenderer(EntityInit.LIGHTNING_ENTITY.get(), LightningEntityRenderer::new);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ItemInit.FREEZERUNE);
            event.accept(ItemInit.FLAMERUNE);
            event.accept(ItemInit.LIGHTNINGRUNE);
            event.accept(ItemInit.WITHERRUNE);
            event.accept(ItemInit.CONFUSIONRUNE);
            event.accept(ItemInit.ALLY_MAKER);
            event.accept(ItemInit.LIGHTNING_STORM);
            event.accept(ItemInit.ROAR);
            event.accept(ItemInit.CALAMITY_INCARNATION_TSUNAMI);
            event.accept(ItemInit.CALAMITY_INCARNATION_TORNADO);
            event.accept(ItemInit.LIGHTNING_BALL);
            event.accept(ItemInit.LIGHTNING_BALL_ABSORB);
            event.accept(ItemInit.MATTER_ACCELERATION_BLOCKS);
            event.accept(ItemInit.MATTER_ACCELERATION_ENTITIES);
            event.accept(ItemInit.MATTER_ACCELERATION_SELF);
            event.accept(ItemInit.STORM_SEAL);
            event.accept(ItemInit.SAILOR_LIGHTNING_TRAVEL);
            event.accept(ItemInit.STORM_SEAL);
            event.accept(ItemInit.VOLCANIC_ERUPTION);
            event.accept(ItemInit.EXTREME_COLDNESS);
            event.accept(ItemInit.LIGHTNING_BRANCH);
            event.accept(ItemInit.EARTHQUAKE);
            event.accept(ItemInit.SAILORPROJECTILECTONROL);
            event.accept(ItemInit.STAR_OF_LIGHTNING);
            event.accept(ItemInit.RAIN_EYES);
            event.accept(ItemInit.SONIC_BOOM);
            event.accept(ItemInit.SAILOR_LIGHTNING);
            event.accept(ItemInit.WATER_SPHERE);
            event.accept(ItemInit.THUNDER_CLAP);
            event.accept(ItemInit.TYRANNY);
            event.accept(ItemInit.WATER_COLUMN);
            event.accept(ItemInit.HURRICANE);
            event.accept(ItemInit.TORNADO);
            event.accept(ItemInit.MIND_READING);
            event.accept(ItemInit.AWE);
            event.accept(ItemInit.FRENZY);
            event.accept(ItemInit.PLACATE);
            event.accept(ItemInit.BATTLE_HYPNOTISM);
            event.accept(ItemInit.PSYCHOLOGICAL_INVISIBILITY);
            event.accept(ItemInit.GUIDANCE);
            event.accept(ItemInit.ALTERATION);
            event.accept(ItemInit.DREAM_WALKING);
            event.accept(ItemInit.NIGHTMARE);
            event.accept(ItemInit.MANIPULATE_MOVEMENT);
            event.accept(ItemInit.MANIPULATE_EMOTION);
            event.accept(ItemInit.APPLY_MANIPULATION);
            event.accept(ItemInit.MENTAL_PLAGUE);
            event.accept(ItemInit.MIND_STORM);
            event.accept(ItemInit.MANIPULATE_FONDNESS);
            event.accept(ItemInit.CONSCIOUSNESS_STROLL);
            event.accept(ItemInit.DRAGON_BREATH);
            event.accept(ItemInit.PLAGUE_STORM);
            event.accept(ItemInit.DREAM_WEAVING);
            event.accept(ItemInit.DISCERN);
            event.accept(ItemInit.TSUNAMI);
            event.accept(ItemInit.DREAM_INTO_REALITY);
            event.accept(ItemInit.PROPHECY);
            event.accept(ItemInit.ENVISION_LIFE);
            event.accept(ItemInit.ENVISION_WEATHER);
            event.accept(ItemInit.ENVISION_BARRIER);
            event.accept(ItemInit.ENVISION_DEATH);
            event.accept(ItemInit.ENVISION_KINGDOM);
            event.accept(ItemInit.ENVISION_LOCATION);
            event.accept(ItemInit.ENVISION_HEALTH);
            event.accept(ItemInit.HORNBEAM_ESSENTIALS_OIL);
            event.accept(ItemInit.DEEP_SEA_MARLINS_BLOOD);
            event.accept(ItemInit.STRING_GRASS_POWDER);
            event.accept(ItemInit.SPIRIT_EATER_STOMACH_POUCH);
            event.accept(ItemInit.RED_CHESTNUT_FLOWER);
            event.accept(ItemInit.LUCK_MANIPULATION);
            event.accept(ItemInit.LUCKGIFTING);
            event.accept(ItemInit.LUCKDEPRIVATION);
            event.accept(ItemInit.LUCKFUTURETELLING);
            event.accept(ItemInit.MISFORTUNEIMPLOSION);
            event.accept(ItemInit.MISFORTUNEBESTOWAL);
            event.accept(ItemInit.MONSTERDANGERSENSE);
            event.accept(ItemInit.LUCKCHANNELING);
            event.accept(ItemInit.MISFORTUNEMANIPULATION);
            event.accept(ItemInit.MONSTERDOMAINTELEPORATION);
            event.accept(ItemInit.MONSTERPROJECTILECONTROL);
            event.accept(ItemInit.LUCKPERCEPTION);
            event.accept(ItemInit.PSYCHESTORM);
            event.accept(ItemInit.SPIRITVISION);
            event.accept(ItemInit.MONSTERCALAMITYATTRACTION);
            event.accept(ItemInit.PROVIDENCEDOMAIN);
            event.accept(ItemInit.DECAYDOMAIN);
            event.accept(ItemInit.CALAMITYINCARNATION);
            event.accept(ItemInit.ENABLEDISABLERIPPLE);
            event.accept(ItemInit.AURAOFCHAOS);
            event.accept(ItemInit.CHAOSWALKERCOMBAT);
            event.accept(ItemInit.MISFORTUNEREDIRECTION);
            event.accept(ItemInit.FORTUNEAPPROPIATION);
            event.accept(ItemInit.FALSEPROPHECY);
            event.accept(ItemInit.MONSTERREBOOT);
            event.accept(ItemInit.FATEREINCARNATION);
            event.accept(ItemInit.CYCLEOFFATE);
            event.accept(ItemInit.CHAOSAMPLIFICATION);
            event.accept(ItemInit.FATEDCONNECTION);
            event.accept(ItemInit.REBOOTSELF);
            event.accept(ItemInit.WHISPEROFCORRUPTION);
            event.accept(ItemInit.LUCKDENIAL);
            event.accept(ItemInit.PROBABILITYMISFORTUNEINCREASE);
            event.accept(ItemInit.PROBABILITYFORTUNEINCREASE);
            event.accept(ItemInit.PROBABILITYWIPE);
            event.accept(ItemInit.PROBABILITYEFFECT);
            event.accept(ItemInit.PROBABILITYMISFORTUNE);
            event.accept(ItemInit.PROBABILITYFORTUNE);
            event.accept(ItemInit.PROBABILITYINFINITEFORTUNE);
            event.accept(ItemInit.PROBABILITYINFINITEMISFORTUNE);
            event.accept(ItemInit.SPECTATOR_9_POTION);
            event.accept(ItemInit.SPECTATOR_8_POTION);
            event.accept(ItemInit.SPECTATOR_7_POTION);
            event.accept(ItemInit.SPECTATOR_6_POTION);
            event.accept(ItemInit.SPECTATOR_5_POTION);
            event.accept(ItemInit.SPECTATOR_4_POTION);
            event.accept(ItemInit.SPECTATOR_3_POTION);
            event.accept(ItemInit.SPECTATOR_2_POTION);
            event.accept(ItemInit.SPECTATOR_1_POTION);
            event.accept(ItemInit.SPECTATOR_0_POTION);
            event.accept(ItemInit.BEYONDER_RESET_POTION);
            event.accept(ItemInit.SAILOR_9_POTION);
            event.accept(ItemInit.SAILOR_8_POTION);
            event.accept(ItemInit.SAILOR_7_POTION);
            event.accept(ItemInit.SAILOR_6_POTION);
            event.accept(ItemInit.SAILOR_5_POTION);
            event.accept(ItemInit.SAILOR_4_POTION);
            event.accept(ItemInit.SAILOR_3_POTION);
            event.accept(ItemInit.SAILOR_2_POTION);
            event.accept(ItemInit.SAILOR_1_POTION);
            event.accept(ItemInit.SAILOR_0_POTION);
            event.accept(ItemInit.MONSTER_9_POTION);
            event.accept(ItemInit.MONSTER_8_POTION);
            event.accept(ItemInit.MONSTER_7_POTION);
            event.accept(ItemInit.MONSTER_6_POTION);
            event.accept(ItemInit.MONSTER_5_POTION);
            event.accept(ItemInit.MONSTER_4_POTION);
            event.accept(ItemInit.MONSTER_3_POTION);
            event.accept(ItemInit.MONSTER_2_POTION);
            event.accept(ItemInit.MONSTER_1_POTION);
            event.accept(ItemInit.MONSTER_0_POTION);
            event.accept(ItemInit.WARRIOR_9_POTION);
            event.accept(ItemInit.WARRIOR_8_POTION);
            event.accept(ItemInit.WARRIOR_7_POTION);
            event.accept(ItemInit.WARRIOR_6_POTION);
            event.accept(ItemInit.WARRIOR_5_POTION);
            event.accept(ItemInit.WARRIOR_4_POTION);
            event.accept(ItemInit.WARRIOR_3_POTION);
            event.accept(ItemInit.WARRIOR_2_POTION);
            event.accept(ItemInit.WARRIOR_1_POTION);
            event.accept(ItemInit.WARRIOR_0_POTION);
            event.accept(ItemInit.LUCKBOTTLEITEM);
            event.accept(ItemInit.RAGING_BLOWS);
            event.accept(ItemInit.AQUEOUS_LIGHT_DROWN);
            event.accept(ItemInit.ENABLE_OR_DISABLE_LIGHTNING);
            event.accept(ItemInit.AQUEOUS_LIGHT_PULL);
            event.accept(ItemInit.AQUEOUS_LIGHT_PUSH);
            event.accept(ItemInit.WIND_MANIPULATION_FLIGHT);
            event.accept(ItemInit.WIND_MANIPULATION_BLADE);
            event.accept(ItemInit.WIND_MANIPULATION_SENSE);
            event.accept(ItemInit.ACIDIC_RAIN);
            event.accept(ItemInit.AQUATIC_LIFE_MANIPULATION);
            event.accept(ItemInit.TSUNAMI_SEAL);
            event.accept(ItemInit.SIREN_SONG_HARM);
            event.accept(ItemInit.SIREN_SONG_WEAKEN);
            event.accept(ItemInit.SIREN_SONG_STUN);
            event.accept(ItemInit.SIREN_SONG_STRENGTHEN);
            event.accept(ItemInit.DEATHKNELL);
            event.accept(ItemInit.CONCEALED_DOOR);
            event.accept(ItemInit.SYMPHONYOFHATRED);
            event.accept(ItemInit.WINTRYBLADE);
            event.accept(ItemInit.SWORDOFDAWN);
            event.accept(ItemInit.SWORDOFTWILIGHT);
            event.accept(ItemInit.SPEAROFDAWN);
            event.accept(ItemInit.SWORDOFSILVER);
            event.accept(ItemInit.PICKAXEOFDAWN);
            event.accept(ItemInit.GIGANTIFICATION);
            event.accept(ItemInit.LIGHTOFDAWN);
            event.accept(ItemInit.ENABLEDISABLEPROTECTION);
            event.accept(ItemInit.DAWNARMORY);
            event.accept(ItemInit.DAWNWEAPONRY);
            event.accept(ItemInit.EYEOFDEMONHUNTING);
            event.accept(ItemInit.WARRIORDANGERSENSE);
            event.accept(ItemInit.MERCURYLIQUEFICATION);
            event.accept(ItemInit.SILVERSWORDMANIFESTATION);
            event.accept(ItemInit.SILVERRAPIER);
            event.accept(ItemInit.SILVERARMORY);
            event.accept(ItemInit.LIGHTCONCEALMENT);
            event.accept(ItemInit.BEAMOFGLORY);
            event.accept(ItemInit.AURAOFGLORY);
            event.accept(ItemInit.TWILIGHTSWORD);
            event.accept(ItemInit.MERCURYCAGE);
            event.accept(ItemInit.DIVINEHANDRIGHT);
            event.accept(ItemInit.DIVINEHANDLEFT);
            event.accept(ItemInit.TWILIGHTMANIFESTATION);
            event.accept(ItemInit.AURAOFTWILIGHT);
            event.accept(ItemInit.TWILIGHTFREEZE);
            event.accept(ItemInit.TWILIGHTACCELERATE);
            event.accept(ItemInit.TWILIGHTLIGHT);
            event.accept(ItemInit.GLOBEOFTWILIGHT);
            event.accept(ItemInit.DAWN_HELMET);
            event.accept(ItemInit.DAWN_CHESTPLATE);
            event.accept(ItemInit.DAWN_LEGGINGS);
            event.accept(ItemInit.DAWN_BOOTS);
            event.accept(ItemInit.SILVER_HELMET);
            event.accept(ItemInit.SILVER_CHESTPLATE);
            event.accept(ItemInit.SILVER_LEGGINGS);
            event.accept(ItemInit.SILVER_BOOTS);
            event.accept(ItemInit.CREATEDOOR);
            event.accept(ItemInit.TRICKBURNING);
            event.accept(ItemInit.TRICKFREEZING);
            event.accept(ItemInit.TRICKTUMBLE);
            event.accept(ItemInit.TRICKTELEKENISIS);
            event.accept(ItemInit.TRICKELECTRICSHOCK);
            event.accept(ItemInit.TRICKESCAPETRICK);
            event.accept(ItemInit.TRICKFLASH);
            event.accept(ItemInit.TRICKFOG);
            event.accept(ItemInit.TRICKLOUDNOISE);
            event.accept(ItemInit.TRICKBLACKCURTAIN);
            event.accept(ItemInit.TRICKWIND);
            event.accept(ItemInit.ASTROLOGER_SPIRIT_VISION);
            event.accept(ItemInit.RECORDSCRIBE);
            event.accept(ItemInit.TRAVELERSDOOR);
            event.accept(ItemInit.TRAVELERSDOORHOME);
            event.accept(ItemInit.INVISIBLEHAND);
            event.accept(ItemInit.BLINK);
            event.accept(ItemInit.BLINKAFTERIMAGE);
            event.accept(ItemInit.SCRIBEABILITIES);
            event.accept(ItemInit.BLINK_STATE);
            event.accept(ItemInit.EXILE);
            event.accept(ItemInit.DOOR_MIRAGE);
            event.accept(ItemInit.SEPARATE_WORM_OF_STAR);
            event.accept(ItemInit.CREATE_CONCEALED_BUNDLE);
            event.accept(ItemInit.CREATE_CONCEALED_SPACE);
            event.accept(ItemInit.SPATIAL_CAGE);
            event.accept(ItemInit.SPATIAL_TEARING);
            event.accept(ItemInit.SYMBOLIZATION);
            event.accept(ItemInit.DIMENSIONAL_SIGHT);
            event.accept(ItemInit.REPLICATE);
            event.accept(ItemInit.SEALING);
            event.accept(ItemInit.TELEPORTATION);
            event.accept(ItemInit.SPACE_FRAGMENTATION);
            event.accept(ItemInit.GRAVITY_MANIPULATION);
            event.accept(ItemInit.SPATIAL_MAZE);
            event.accept(ItemInit.DOOR_SPATIAL_LOCK_ON);
            event.accept(ItemInit.DOOR_DIMENSION_CLOSING);
            event.accept(ItemInit.DOOR_SEALED_SPACE);
            event.accept(ItemInit.DOOR_LAYERING);
            event.accept(ItemInit.DOOR_GAMMA_RAY_BURST);
            event.accept(ItemInit.CONCEPTUALIZATION);
            event.accept(ItemInit.REPLICATION);
            event.accept(ItemInit.ASTROLABE);
            event.accept(ItemInit.WORM_OF_STAR);
        }
        if (event.getTabKey() == CreativeModeTabs.OP_BLOCKS) {
            event.accept(BlockInit.VISIONARY_BARRIER_BLOCK);
            event.accept(BlockInit.VISIONARY_GLASS_PANE);
            event.accept(BlockInit.LOTM_LIGHT_BLUE_STAINED_GLASS);
            event.accept(BlockInit.LOTM_RED_NETHER_BRICKS);
            event.accept(BlockInit.LOTM_WHITE_STAINED_GLASS);
            event.accept(BlockInit.LOTM_BLUE_STAINED_GLASS);
            event.accept(BlockInit.CATHEDRAL_BLOCK);
            event.accept(BlockInit.MONSTER_DOMAIN_BLOCK);
            event.accept(BlockInit.MINDSCAPE_BLOCK);
            event.accept(BlockInit.MINDSCAPE_OUTSIDE);
            event.accept(BlockInit.LOTM_BOOKSHELF);
            event.accept(BlockInit.LOTM_DEEPSLATE_BRICKS);
            event.accept(BlockInit.LOTM_REDSTONE_BLOCK);
            event.accept(BlockInit.LOTM_SANDSTONE);
            event.accept(BlockInit.MINDSCAPE_OUTSIDE);
            event.accept(BlockInit.LOTM_POLISHED_DIORITE);
            event.accept(BlockInit.LOTM_DARK_OAK_PLANKS);
            event.accept(BlockInit.LOTM_QUARTZ);
            event.accept(BlockInit.LOTM_CHISELED_STONE_BRICKS);
            event.accept(BlockInit.LOTM_MANGROVE_PLANKS);
            event.accept(BlockInit.LOTM_SPRUCE_PLANKS);
            event.accept(BlockInit.LOTM_SPRUCE_LOG);
            event.accept(BlockInit.LOTM_OAK_PLANKS);
            event.accept(BlockInit.LOTM_BIRCH_PLANKS);
            event.accept(BlockInit.LOTM_BLACK_CONCRETE);
            event.accept(BlockInit.LOTM_STONE);
            event.accept(BlockInit.LOTM_STONE_BRICKS);
            event.accept(BlockInit.LOTM_CRACKED_STONE_BRICKS);
            event.accept(BlockInit.LOTM_LIGHT_BLUE_CONCRETE);
            event.accept(BlockInit.LOTM_BLUE_CONCRETE);
            event.accept(BlockInit.LOTM_BLACKSTONE);
            event.accept(BlockInit.LOTM_WHITE_CONCRETE);
            event.accept(BlockInit.LOTM_POLISHED_ANDESITE);
            event.accept(BlockInit.LOTM_POLISHED_BLACKSTONE);
            event.accept(BlockInit.LOTM_SEA_LANTERN);
            event.accept(BlockInit.LOTM_OAK_LOG);
            event.accept(ItemInit.LUCKBOTTLEITEM);
            event.accept(ItemInit.LUCKYGOLDCOIN);

            event.accept(BlockInit.VISIONARY_BLACK_STAINED_GLASS_PANE);
            event.accept(BlockInit.VISIONARY_WHITE_STAINED_GLASS_PANE);
            event.accept(BlockInit.VISIONARY_GRAY_STAINED_GLASS_PANE);
            event.accept(BlockInit.VISIONARY_LIGHT_GRAY_STAINED_GLASS_PANE);
            event.accept(BlockInit.VISIONARY_BROWN_STAINED_GLASS_PANE);
            event.accept(BlockInit.VISIONARY_PURPLE_STAINED_GLASS_PANE);
            event.accept(BlockInit.VISIONARY_CYAN_STAINED_GLASS_PANE);
            event.accept(BlockInit.VISIONARY_BLUE_STAINED_GLASS_PANE);
            event.accept(BlockInit.VISIONARY_LIGHT_BLUE_STAINED_GLASS_PANE);
            event.accept(BlockInit.VISIONARY_LIME_STAINED_GLASS_PANE);
            event.accept(BlockInit.VISIONARY_GREEN_STAINED_GLASS_PANE);
            event.accept(BlockInit.VISIONARY_YELLOW_STAINED_GLASS_PANE);
            event.accept(BlockInit.VISIONARY_RED_STAINED_GLASS_PANE);
            event.accept(BlockInit.VISIONARY_ORANGE_STAINED_GLASS_PANE);
            event.accept(BlockInit.VISIONARY_PINK_STAINED_GLASS_PANE);
            event.accept(BlockInit.VISIONARY_MAGENTA_STAINED_GLASS_PANE);
            event.accept(BlockInit.LOTM_LIGHT_BLUE_CARPET);
            event.accept(BlockInit.LOTM_CHAIN);
            event.accept(BlockInit.LOTM_LANTERN);
            event.accept(BlockInit.POTION_CAULDRON);

            event.accept(BlockInit.LOTM_DARKOAK_SLAB);
            event.accept(BlockInit.LOTM_QUARTZ_SLAB);
            event.accept(BlockInit.LOTM_DARKOAK_STAIRS);
            event.accept(BlockInit.LOTM_OAK_STAIRS);
            event.accept(BlockInit.LOTM_QUARTZ_STAIRS);
            event.accept(BlockInit.LOTM_DEEPSLATEBRICK_STAIRS);
        }
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(EntityInit.LUCK_BOTTLE_ENTITY.get(), ThrownItemRenderer::new);
            ItemBlockRenderTypes.setRenderLayer(BlockInit.VISIONARY_BARRIER_BLOCK.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(BlockInit.VOID_GLASS.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(BlockInit.LOTM_BLUE_STAINED_GLASS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(BlockInit.LOTM_WHITE_STAINED_GLASS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(BlockInit.LOTM_LIGHT_BLUE_STAINED_GLASS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(BlockInit.VISIONARY_BLACK_STAINED_GLASS_PANE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(BlockInit.VISIONARY_CYAN_STAINED_GLASS_PANE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(BlockInit.VISIONARY_LIME_STAINED_GLASS_PANE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(BlockInit.VISIONARY_GREEN_STAINED_GLASS_PANE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(BlockInit.VISIONARY_LIGHT_BLUE_STAINED_GLASS_PANE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(BlockInit.VISIONARY_BLUE_STAINED_GLASS_PANE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(BlockInit.VISIONARY_WHITE_STAINED_GLASS_PANE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(BlockInit.VISIONARY_GRAY_STAINED_GLASS_PANE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(BlockInit.VISIONARY_LIGHT_GRAY_STAINED_GLASS_PANE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(BlockInit.VISIONARY_BROWN_STAINED_GLASS_PANE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(BlockInit.VISIONARY_PURPLE_STAINED_GLASS_PANE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(BlockInit.VISIONARY_MAGENTA_STAINED_GLASS_PANE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(BlockInit.VISIONARY_YELLOW_STAINED_GLASS_PANE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(BlockInit.VISIONARY_RED_STAINED_GLASS_PANE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(BlockInit.VISIONARY_PINK_STAINED_GLASS_PANE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(BlockInit.VISIONARY_ORANGE_STAINED_GLASS_PANE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(BlockInit.VISIONARY_GLASS_PANE.get(), RenderType.translucent());
            MenuScreens.register(MenuInit.POTION_CAULDRON_MENU.get(), PotionCauldronScreen::new);
            MenuScreens.register(MenuInit.CONCEALED_BUNDLE_MENU.get(), ConcealedBundleScreen::new);
        }
    }

    public static void sendMessageToAllPlayers(String message) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            Component messageComponent = Component.literal(message);
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                player.sendSystemMessage(messageComponent);
            }
        }
    }

    public static void sendMessageToAllPlayers(String message, ChatFormatting formatting) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            Component messageComponent = Component.literal(message).withStyle(formatting);
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                player.sendSystemMessage(messageComponent);
            }
        }
    }
}
