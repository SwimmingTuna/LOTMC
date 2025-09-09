package net.swimmingtuna.lotm.events;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.attributes.AttributeHelper;
import net.swimmingtuna.lotm.beyonder.*;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.capabilities.doll_data.DollUtils;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.client.Configs;
import net.swimmingtuna.lotm.commands.AbilityRegisterCommand;
import net.swimmingtuna.lotm.entity.*;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.GameRuleInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.AllyMaker;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice.*;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.*;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.*;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.DreamWalking;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.Prophecy;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.PsychologicalInvisibility;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems.*;
import net.swimmingtuna.lotm.item.BeyonderPotions.BeyonderCharacteristic;
import net.swimmingtuna.lotm.item.OtherItems.SwordOfTwilight;
import net.swimmingtuna.lotm.item.SealedArtifacts.DeathKnell;
import net.swimmingtuna.lotm.item.SealedArtifacts.WintryBlade;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.SyncSequencePacketS2C;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EFunctions;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventManager;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventsProvider;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;
import net.swimmingtuna.lotm.util.AllyInformation.PlayerAllyData;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.ClientData.*;
import net.swimmingtuna.lotm.util.CorruptionAndLuckHandler;
import net.swimmingtuna.lotm.util.PlayerMobs.PlayerMobSequenceData;
import net.swimmingtuna.lotm.world.worlddata.BeyonderEntityData;
import net.swimmingtuna.lotm.world.worlddata.CalamityEnhancementData;
import net.swimmingtuna.lotm.world.worldgen.MirrorWorldChunkGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.swimmingtuna.lotm.beyonder.WarriorClass.newWarriorDamageNegation;
import static net.swimmingtuna.lotm.beyonder.WarriorClass.twilightTick;
import static net.swimmingtuna.lotm.blocks.MonsterDomainBlockEntity.domainDrops;
import static net.swimmingtuna.lotm.entity.PlayerMobEntity.getDrop;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.ChaosWalkerDisableEnable.onChaosWalkerCombat;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.DomainOfProvidence.domainDropsExperience;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.EnableDisableRipple.rippleOfMisfortune;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.FalseProphecy.doubleProphecyDamageHelper;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.MisfortuneManipulation.livingLightningStorm;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.MonsterCalamityIncarnation.calamityIncarnationTornado;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.MonsterCalamityIncarnation.calamityLightningStorm;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.MonsterDangerSense.monsterDangerSense;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.ProbabilityManipulationWorldFortune.probabilityManipulationWorld;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.AcidicRain.acidicRainTick;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.CalamityIncarnationTsunami.calamityIncarnationTsunamiTick;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.Earthquake.earthquake;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.ExtremeColdness.extremeColdness;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.Hurricane.hurricane;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.LightningStorm.lightningStorm;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.MatterAccelerationEntities.matterAccelerationEntitiesAndRainEyes;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.MatterAccelerationSelf.matterAccelerationSelf;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.RagingBlows.ragingBlowsTick;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.SailorLightningTravel.sailorLightningTravel;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.SirenSongHarm.sirenSongsTick;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.StarOfLightning.starOfLightning;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.Tsunami.tsunami;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.WaterSphere.waterSphereCheck;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.WindManipulationFlight.windManipulationGuide;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.WindManipulationSense.windManipulationSense;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.ConsciousnessStroll.consciousnessStroll;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.DreamIntoReality.dreamIntoReality;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.DreamWeaving.dreamWeaving;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.EnvisionBarrier.envisionBarrier;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.EnvisionKingdom.envisionKingdom;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.ManipulateMovement.manipulateMovement;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.Nightmare.nightmareTick;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems.Gigantification.warriorGiant;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems.WarriorDangerSense.warriorDangerSense;
import static net.swimmingtuna.lotm.world.worldgen.dimension.DimensionInit.SPIRIT_WORLD_LEVEL_KEY;

@Mod.EventBusSubscriber(modid = LOTM.MOD_ID)
public class ModEvents {
    private static final Map<Item, Integer> abilityCooldowns = new HashMap<>();

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level != null) {
            level.getDataStorage().computeIfAbsent(
                    PlayerAllyData::load,
                    PlayerAllyData::create,
                    "player_allies"
            );
        }
    }


    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel().dimensionType().equals(SPIRIT_WORLD_LEVEL_KEY)) {
            if (event.getLevel() instanceof ServerLevel spiritWorld) {
                ServerLevel overworld = spiritWorld.getServer().getLevel(Level.OVERWORLD);
                if (overworld != null && spiritWorld.getChunkSource().getGenerator() instanceof MirrorWorldChunkGenerator) {
                    ChunkGenerator newGenerator = new MirrorWorldChunkGenerator(
                            spiritWorld.getChunkSource().getGenerator().getBiomeSource(),
                            overworld.dimension()
                    );
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerJoinWorld(PlayerEvent.PlayerLoggedInEvent event) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientAbilityCombinationData.clientSideLoginHandling();
        }
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
            LOTMNetworkHandler.sendToPlayer(new SyncSequencePacketS2C(holder.getSequence()), (ServerPlayer) player);
            CompoundTag persistentData = player.getPersistentData();

            if (persistentData.contains("DemiseCounter")) {
                int demiseCounter = persistentData.getInt("DemiseCounter");

                if (!persistentData.contains("EntityDemise") || persistentData.getInt("EntityDemise") == 0) {
                    player.getPersistentData().putInt("EntityDemise", demiseCounter);
                }
            } else {
                if (!persistentData.contains("EntityDemise") || persistentData.getInt("EntityDemise") == 0) {

                    player.getPersistentData().putInt("EntityDemise", 0);
                }
            }
        }
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void leftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        BeyonderUtil.leftClick(event.getEntity());
    }

    @SubscribeEvent
    public static void craftEvent(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            if (!(BeyonderUtil.currentPathwayAndSequenceMatchesNoException(player, BeyonderClassInit.WARRIOR.get(), 4))) {
                ItemStack craftedItem = event.getCrafting();
                if (craftedItem.getItem() == ItemInit.LIGHTNINGRUNE.get() || craftedItem.getItem() == ItemInit.CONFUSIONRUNE.get() || craftedItem.getItem() == ItemInit.FLAMERUNE.get() || craftedItem.getItem() == ItemInit.WITHERRUNE.get() || craftedItem.getItem() == ItemInit.FREEZERUNE.get()) {
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack slot = player.getInventory().getItem(i);
                        if (slot.getItem() == craftedItem.getItem()) {
                            player.getInventory().removeItem(i, 1);
                            break;
                        }
                    }
                    player.sendSystemMessage(Component.literal("You aren't the correct pathway and/or sequence to be able to craft this.").withStyle(ChatFormatting.RED));
                }
            }
        }
    }

    @SubscribeEvent
    public static void mobEffectEvent(MobEffectEvent.Added event) {
        LivingEntity entity = event.getEntity();
        if (entity.level() instanceof ServerLevel serverLevel) {
            //
            //if (BeyonderUtil.currentPathwayAndSequenceMatchesNoException(entity, BeyonderClassInit.APPRENTICE.get(), 3) && event.getEffectInstance().getEffect().getCategory() == MobEffectCategory.HARMFUL) {
            //    MobEffectInstance currentEffect = event.getEffectInstance();
            //    int originalDuration = currentEffect.getDuration();
            //    int newDuration = (int) (originalDuration * 0.7);
            //    MobEffectInstance reducedEffect = new MobEffectInstance(currentEffect.getEffect(), newDuration, currentEffect.getAmplifier(), currentEffect.isAmbient(), currentEffect.isVisible(), currentEffect.showIcon());
            //    entity.removeEffect(currentEffect.getEffect());
            //    entity.addEffect(reducedEffect);
            //}
            //CalamityEnhancementData data = CalamityEnhancementData.getInstance(serverLevel);
            //int chaosLevel = data.getCalamityEnhancement();
            //if (chaosLevel != 1) {
            //    MobEffectInstance mobEffectInstance = event.getEffectInstance();
            //    if (mobEffectInstance.getAmplifier() <= 5) {
            //BeyonderUtil.applyMobEffect(entity, mobEffectInstance.getEffect(), mobEffectInstance.getDuration(), mobEffectInstance.getAmplifier() * chaosLevel, mobEffectInstance.isAmbient(), mobEffectInstance.isVisible()));
            //    }
            //}
            if (!event.getEntity().level().isClientSide() && BeyonderUtil.hasBeneficialEffectBlocker(event.getEntity())) {
                MobEffect addedEffect = event.getEffectInstance().getEffect();
                if (addedEffect.getCategory() == MobEffectCategory.BENEFICIAL) {
                    entity.removeEffect(addedEffect);
                }
            }
        }
    }

    @SubscribeEvent
    public static void mobEffectEvent(MobEffectEvent.Remove event) {
        if (!event.getEntity().level().isClientSide()) {

        }
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void leftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (player.getMainHandItem().getItem() instanceof SimpleAbilityItem) {
            BeyonderUtil.leftClick(player);
            event.setCanceled(true);
            return;
        }
    }

    @SubscribeEvent
    public static void onPlayerTickClient(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        Style style = BeyonderUtil.getStyle(player);
        CompoundTag playerPersistentData = player.getPersistentData();
        BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
        int sequence = holder.getSequence();
        if (player.level().isClientSide()) {
            if (ClientFogData.getFogTimer() >= 1) {
                ClientFogData.decrementFog();
            }
            if (ClientShouldntMoveData.getDontMoveTimer() >= 1) {
                ClientShouldntMoveData.decrementDontMoveTimer();
            }
            ClientGrayscaleData.decrementDuration();
            if (player.tickCount % 100 == 0) {
                //DimensionalSightEntity.debugLoadedChunks();
            }
            if (ClientAbilityKeyResetData.getAbilityResetTimer() >= 1) {
                ClientAbilityKeyResetData.decrementAbilityResetTimer();
                if (ClientAbilityKeyResetData.getAbilityResetTimer() == 1) {
                    ClientAbilityCombinationData.resetKeysClicked();
                    player.displayClientMessage(Component.literal("_ _ _ _ _").withStyle(ChatFormatting.BOLD), true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTickServer(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        CompoundTag tag = player.getPersistentData();
        BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        int sequence = holder.getSequence();
        if (player.level().isClientSide() || event.phase != TickEvent.Phase.START) {
            return;
        }
        if (!player.level().isClientSide() && holder.currentClassMatches(BeyonderClassInit.MONSTER) && sequence <= 9 && player.tickCount % 5 == 0) {
            MonsterClass.checkForProjectiles(player);
        }
        if (!player.level().isClientSide() && player.tickCount % 20 == 0) {
            //boolean x = ClientAntiConcealmentData.getAntiConceal();
            //player.sendSystemMessage(Component.literal("value is " + x));
        }

        if (player instanceof ServerPlayer serverPlayer) {
            if (player.tickCount % 20 == 0) {
                AbilityRegisterCommand.tickEvent(serverPlayer);
                if (holder.getSequence() != 0 && ClientSequenceData.getCurrentSequence() == 0) {
                    ClientSequenceData.setCurrentSequence(-1);
                }

            }
            int currentCooldown = BeyonderUtil.getCooldown(serverPlayer);
            if (currentCooldown >= 1) {
                currentCooldown--;
                BeyonderUtil.setCooldown(serverPlayer, currentCooldown);
            }
        }
        BeyonderUtil.copyAbilityTick(player);
        BeyonderUtil.abilityCooldownsServerTick(event);
        DollUtils.dollPlayerTick(player);


    }

    @SubscribeEvent
    public static void sealItemCanceler(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            EyeOfDemonHunting.demonHunterAlchemy(event);
            CompoundTag tag = player.getPersistentData();
            int sealCounter = tag.getInt("sailorSeal");
            if (sealCounter >= 1) {
                event.setCanceled(true);
            }
        }
    }


    @SubscribeEvent
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        Player player = event.getEntity();
        ItemStack itemStack = player.getItemInHand(event.getHand());
        if (itemStack.getItem() instanceof SimpleAbilityItem) {
            if (event.getTarget() instanceof LivingEntity livingEntity) {
                InteractionResult result = ((SimpleAbilityItem) itemStack.getItem()).useAbilityOnEntity(itemStack, player, livingEntity, event.getHand());
                if (result == InteractionResult.SUCCESS) {
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                }
            }
        }
    }

    @SubscribeEvent
    public static void entityInteractEvent(PlayerInteractEvent.EntityInteract event) {
        MercuryLiquefication.mercuryArmorRightClick(event);
    }

    @SubscribeEvent
    public static void rightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        MercuryLiquefication.mercuryRightClick(event);
    }


    @SubscribeEvent
    public static void handleLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        Level level = livingEntity.level();

        if (level instanceof ServerLevel serverLevel) {
            if (tag.getInt("gotHitByMentalAttack") >= 1) {
                float health = livingEntity.getHealth();
                if (Float.isNaN(health) || health < 0.0F) {
                    livingEntity.kill();
                }
                tag.putInt("gotHitByMentalAttack", tag.getInt("gotHitByMentalAttack") - 1);
            }
            if (tag.getInt("abilityCooldown") >= 1 ) {
                tag.putInt("abilityCooldown", tag.getInt("abilityCooldown") - 1);
            }
            if (tag.getInt("LOTMinCombat") >= 1) {
                tag.putInt("LOTMinCombat", tag.getInt("LOTMinCombat") - 1);
            }
            if (livingEntity.isUnderWater()) {
                if (AttributeHelper.getWaterBreathing(livingEntity) == 1.0) {
                    livingEntity.setAirSupply(300);
                }
            }
            twilightTick(event);
            //envisionKingdom(livingEntity, level);
            CorruptionAndLuckHandler.corruptionAndLuckManagers(serverLevel, livingEntity);
            SwordOfTwilight.twilightSwordTick(event);

            event.getEntity().getCapability(EventsProvider.EVENTS_DATA).ifPresent(cap -> {
                List<IFunction> listW = cap.getWorldEvents();
                for (var obj : listW) {
                    LOTM.LOGGER.info("Calling world event: {} - {}", obj.getClass().getSimpleName(), obj.toString());
                    obj.use(event);
                }

                if (tag.getInt("inTwilight") == 0 && tag.getInt("cancelTick") == 0) {
                    List<IFunction> listR = cap.getRegularEvents();
                    for (var obj : listR) {
                        LOTM.LOGGER.info("Calling regular event: {} - {}", obj.getClass().getSimpleName(), obj.toString());
                        obj.use(event);
                    }
                }
                cap.deleteAllMarked();
            });
            if (tag.getInt("inTwilight") == 0 && tag.getInt("cancelTick") == 0) {
                //mob ticks
                MatterAccelerationBlocks.matterAccelerationBlocksMobTick(event);
                //BeyonderEntityData.regenerateSpirituality(event);

                //regular ticks
                BeyonderUtil.projectileEvent(livingEntity);
                BeyonderUtil.effectTick(event);
                DimensionalSightSealEntity.dimensionalSightSealTick(livingEntity);
                matterAccelerationSelf(livingEntity);
                DawnArmory.dawnArmorTickEvent(event);
                MonsterClass.calamityExplosion(livingEntity);
                //SealedUtils.timerTick(livingEntity);
                //SailorClass.rainEyesTickEvent(event);
                //GravityManipulation.gravityManipulationTickEvent(event);
                //PsychologicalInvisibility.psychologicalInvisibilityTick(event);
                //Symbolization.symbolizationTick(event);
                //ApprenticeClass.enableWaterWalking(event);
                //MisfortuneImplosion.misfortuneImplosionLightning(event);
                //SpatialCageEntity.cageTick(livingEntity);
                //BlinkState.secretsSorcererBlinkState(event);
                //Exile.exileTickEvent(event);
                //DoorMirage.mirageTick(livingEntity);
                //ApprenticeClass.apprenticeTick(event);
                //VolcanicEruption.volcanicEruptionTick(event);
                //LightningRedirection.lightningRedirectionTick(event);
                //TrickTelekenisis.trickMasterTelekenisisPassive(event);
                //Prophecy.prophecyTick(event);
                //SpectatorClass.prophecyTickEvent(event);
                //dreamIntoReality(livingEntity);
                //acidicRainTick(livingEntity);
                //lightningStorm(livingEntity);
                //tsunami(livingEntity);
                //ragingBlowsTick(livingEntity);
                //waterSphereCheck(livingEntity);
                //windManipulationFlight(livingEntity);
                //sirenSongs(livingEntity);
                //starOfLightning(livingEntity);
                //sirenSongsTick(livingEntity);
                //DivineHandRightEntity.divineHandCooldownDecrease(livingEntity);
                //earthquake(livingEntity);
                //hurricane(livingEntity);
                //extremeColdness(livingEntity);
                //calamityIncarnationTsunamiTick(livingEntity);
                //envisionBarrier(livingEntity);
                //manipulateMovement(livingEntity);
                //consciousnessStroll(livingEntity);
                //calamityIncarnationTornado(livingEntity);
                //windManipulationGuide(livingEntity);
                //RagingBlows.ragingCombo(event);
                //windManipulationSense(livingEntity);
                //sailorLightningTravel(livingEntity);
                //monsterDomainIntHandler(livingEntity);
                //nightmareTick(livingEntity);
                //MonsterClass.calamityUndeadArmy(livingEntity);
                //calamityLightningStorm(livingEntity);
                //warriorDangerSense(livingEntity);
                //MonsterClass.decrementMonsterAttackEvent(livingEntity);
                //onChaosWalkerCombat(livingEntity);
                //MonsterClass.monsterLuckPoisonAttacker(livingEntity);
                //MonsterClass.monsterLuckIgnoreMobs(livingEntity);
                //monsterDangerSense(event);
                //GlobeOfTwilight.globeOfTwilightTick(event);
                //SilverRapier.mercuryTick(event);
                //FateReincarnation.monsterReincarnationChecker(event);
                //TwilightAccelerate.twilightAccelerateTick(event);
                //TwilightFreeze.twilightFreezeTick(event);
                //TwilightLight.twilightLightTick(event);



                TwilightManifestation.twilightManifestationTick(event);
                SwordOfTwilight.decrementTwilightSword(event);
                MercuryLiquefication.mercuryArmorTick(event);
                MercuryLiquefication.mercuryLiqueficationTick(event);
                BeyonderUtil.ageHandlerTick(event);
                InvisibleHand.invisibleHandTick(event);
                TrickBurning.smeltItem(event);
                Gigantification.gigantificationScale(event);
                EnableOrDisableProtection.warriorProtectionTick(event);
                GuardianBoxEntity.decrementGuardianTimer(livingEntity);
                EyeOfDemonHunting.eyeTick(event);
                EyeOfDemonHunting.demonHunterAntiConcealment(event);
                WintryBlade.wintryBladeTick(event);
                warriorGiant(livingEntity);
                DeathKnell.deathKnellNegativeTick(livingEntity);
                ProbabilityManipulationInfiniteMisfortune.testEvent(event);
                probabilityManipulationWorld(livingEntity);
                CycleOfFate.cycleOfFateTickEvent(event);
                DreamWalking.dreamWalkingTick(event);
                MonsterClass.dodgeProjectiles(livingEntity);
                MisfortuneManipulation.livingTickMisfortuneManipulation(event);
                FalseProphecy.falseProphecyTick(livingEntity);
                AuraOfChaos.auraOfChaos(event);
                PsycheStorm.psycheStormTick(event);
                AuraOfGlory.auraOfGloryAndTwilightTick(event);
                livingLightningStorm(livingEntity);
                Gigantification.gigantificationDestroyBlocks(event);
                LightOfDawn.sunriseGleamTick(event);
                doubleProphecyDamageHelper(event);
                MonsterClass.showMonsterParticles(livingEntity);
                MonsterClass.luckDenial(livingEntity);
                MonsterCalamityIncarnation.calamityTickEvent(event);
                dreamWeaving(livingEntity);
                LightConcealment.lightConcealmentTick(event);
                SpectatorClass.demiseTick(event);
                AqueousLightDrown.aqueousLightDrownTick(event);
                matterAccelerationEntitiesAndRainEyes(livingEntity);
                ExtremeColdness.extremeColdnessTick(event);
                StormSeal.stormSealTick(event);
                SpatialMaze.mazeTick(livingEntity);
                AqueousLightDrown.lightTickEvent(livingEntity);
                TsunamiSeal.sealTick(event);
            }
        }
    }

    @SubscribeEvent
    public static void blockRightClickEvent(PlayerInteractEvent.RightClickBlock event) {
        ApprenticeClass.doorRightClick(event);
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        CompoundTag tag = player.getPersistentData();
        BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
        if (player.level().isClientSide()) return;
        SailorClass.sailorLightningPassive(event);
        TrickElectricShock.trickMasterShockPassive(event);
    }

    @SubscribeEvent
    public static void projectileImpactEvent(ProjectileImpactEvent event) {
        Entity projectile = event.getProjectile();
        if (!projectile.level().isClientSide()) {
            SailorClass.sailorProjectileLightning(event);
        }
    }


    @SubscribeEvent
    public static void attackEvent(LivingAttackEvent event) {
        LivingEntity attacked = event.getEntity();
        Entity attacker = event.getSource().getEntity();
        if (!attacked.level().isClientSide()) {
            WarriorClass.warriorAttackEvent(event);
            Symbolization.symbolizationAttack(event);
            ApprenticeClass.apprenticeAttackEvent(event);
            if (BeyonderUtil.isCreative(attacked)) {
                event.setCanceled(true);
            }
            DamageSource source = event.getSource();
            if (AttributeHelper.getFireResistance(attacked) == 3 && (source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.HOT_FLOOR) || source.is(DamageTypes.LAVA))) {
                event.setCanceled(true);
                return;
            }
        }
        if (attacker != null) {
            if (!attacked.level().isClientSide() && !attacker.level().isClientSide()) {
                if (attacker.getPersistentData().getInt("dreamWeavingDeathTimer") >= 1 && attacker instanceof LivingEntity livingAttacker) {
                    attacker.hurt(event.getSource(), event.getAmount() * 3);
                    BeyonderUtil.applyMentalDamage(livingAttacker, attacked, 2);
                }
                if (attacker instanceof LivingEntity) {
                    SailorClass.sailorAttackEvent(event);
                }
                attacked.getPersistentData().putInt("LOTMinCombat", 300);
                attacker.getPersistentData().putInt("LOTMinCombat", 300);
                DoorMirage.doorMirageAttackEvent(event);
                BlinkAfterimage.travelerBlinkPassive(event);
                CompoundTag tag = attacked.getPersistentData();
                TrickEscapeTrick.escapeTrickAttackEvent(event);
                String entityName = event.getSource().getMsgId().toLowerCase();
                ApprenticeClass.apprenticeAttackEvent(event);
                if (BeyonderUtil.currentPathwayAndSequenceMatchesNoException(attacked, BeyonderClassInit.SAILOR.get(), 1) &&
                        (event.getSource().getMsgId().toLowerCase().contains("lightning") ||
                                event.getSource().getMsgId().toLowerCase().contains("thunder"))) {
                    event.setCanceled(true);
                }
                CompoundTag sourceTag = attacker.getPersistentData();

                //SAILOR FLIGHT
                if (tag.getInt("sailorFlightDamageCancel") != 0 && event.getSource().is(DamageTypes.FALL)) {
                    event.setCanceled(true);
                    tag.putInt("sailorFlightDamageCancel", 0);
                }
                int ignoreDamage = tag.getInt("luckIgnoreDamage");
                if (ignoreDamage >= 1) {
                    event.setCanceled(true);
                    attacked.getPersistentData().putInt("luckIgnoreDamage", attacked.getPersistentData().getInt("luckIgnoreDamage") - 1);

                }
                if (tag.getInt("inStormSeal") >= 1) {
                    event.setCanceled(true);
                }

                MonsterClass.monsterDodgeAttack(event);
                if (attacker instanceof LivingEntity livingEntity) {
                    if (livingEntity.getMainHandItem().getItem() instanceof AllyMaker) {
                        event.setCanceled(true);
                    }
                }
                if (BeyonderUtil.currentPathwayAndSequenceMatchesNoException(attacked, BeyonderClassInit.MONSTER.get(), 5)) {
                    if (attacker instanceof LivingEntity living) {
                        EventManager.addToRegularLoop(living, EFunctions.DECREMENT_MONSTER_ATTACK_EVENT.get());
                        attacker.getPersistentData().putInt("attackedMonster", 100);
                    }
                }
                if (attacker.getPersistentData().getInt("beneficialFalseProphecyAttack") >= 1) {
                    attacker.getPersistentData().putInt("beneficialDamageDoubled", 5);
                    attacker.getPersistentData().putBoolean("shouldDoubleProphecyDamage", true);
                    attacker.getPersistentData().putInt("beneficialFalseProphecyAttack", 0);
                }
                if (attacker.getPersistentData().getInt("beneficialDamageDoubled") >= 1 && attacker.getPersistentData().getBoolean("shouldDoubleProphecyDamage")) {
                    attacker.getPersistentData().putInt("beneficialDamageDoubled", attacker.getPersistentData().getInt("beneficialDamageDoubled") - 1);
                    event.setCanceled(true);
                    attacker.getPersistentData().putBoolean("shouldDoubleProphecyDamage", false);
                    attacked.hurt(BeyonderUtil.magicSource(attacker, attacked), event.getAmount() * 2);
                }
                if (attacker.getPersistentData().getInt("harmfulFalseProphecyAttack") >= 1) {
                    attacker.getPersistentData().putInt("luckDoubleDamage", attacker.getPersistentData().getInt("luckDoubleDamage") + 5);
                    attacker.getPersistentData().putInt("harmfulFalseProphecyAttack", 0);
                }

                int stoneImmunity = tag.getInt("luckStoneDamageImmunity");
                int meteorImmunity = tag.getInt("calamityMeteorImmunity");
                int mcLightningImmunity = tag.getInt("luckMCLightningImmunity");
                int lotmLightningDamage = tag.getInt("luckLightningLOTMDamage");
                int lotmLightningImmunity = tag.getInt("calamityLOTMLightningImmunity");
                int lightningStormImmunity = tag.getInt("calamityLightningStormImmunity");
                if (attacker instanceof StoneEntity) {
                    if (stoneImmunity >= 1) {
                        event.setCanceled(true);
                    }
                }
                if (attacker instanceof MeteorEntity || attacker instanceof MeteorNoLevelEntity) {
                    if (meteorImmunity >= 1) {
                        event.setCanceled(true);
                    }
                }
                if (event.getSource().is(DamageTypes.LIGHTNING_BOLT)) {
                    if (mcLightningImmunity >= 1) {
                        event.setCanceled(true);
                    }
                }
                if (attacker instanceof LightningEntity) {
                    if (lotmLightningImmunity >= 1 || lightningStormImmunity >= 1) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void livingJumpEvent(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide()) {
            CompoundTag tag = entity.getPersistentData();
            int falseProphecyBeneficial = tag.getInt("beneficialFalseProphecyJump");
            int falseProphecyHarmful = tag.getInt("harmfulFalseProphecyJump");
            if (falseProphecyBeneficial >= 1) {
                tag.putInt("falseProphecyJumpBeneficial", tag.getInt("falseProphecyJumpBeneficial") + 1);
            }
            if (falseProphecyHarmful >= 1) {
                tag.putInt("falseProphecyJumpHarmful", tag.getInt("falseProphecyJumpHarmful") + 1);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity living = event.getEntity();
        double boost = AttributeHelper.getJumpBoost(living);

        if (boost == 0) {
            return;
        }

        living.setDeltaMovement(living.getDeltaMovement().
                add(0, boost, 0));
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        float breakSpeed = (float) AttributeHelper.getDigSpeed(player);
        if (breakSpeed != 0.0F) {
            event.setNewSpeed(event.getOriginalSpeed() * breakSpeed);
        }
    }

    @SubscribeEvent
    public static void hurtEvent(LivingHurtEvent event) {
        Entity entity = event.getEntity();
        CompoundTag tag = entity.getPersistentData();
        DamageSource source = event.getSource();
        Entity entitySource = source.getEntity();
        if (!event.getEntity().level().isClientSide()) {
            PsychologicalInvisibility.psychologicalInvisibilityAttack(event);
            Teleportation.teleportationHurtEvent(event);
            BeyonderUtil.ageHandlerHurt(event);
            GuardianBoxEntity.guardianHurtEvent(event);
            newWarriorDamageNegation(event);
            MercuryLiquefication.mercuryArmorHurt(event);
            Entity entitySourceOwner = source.getEntity();
            if (entitySource != null) {
                if (entitySourceOwner instanceof Projectile projectile && projectile.getOwner() != null) {
                    entitySourceOwner = projectile.getOwner();
                }
                if (entitySourceOwner.getPersistentData().getInt("dreamWeavingDeathTimer") >= 1) {
                    event.setAmount(event.getAmount() * 4.0f);
                }
                CompoundTag sourceTag = entitySource.getPersistentData();
                if (entity instanceof LivingEntity living) {
                    int resLevel = (int) AttributeHelper.getFireResistance(living);
                    if (resLevel > 0) {
                        switch (resLevel) {
                            case 3:
                                if (source.is(DamageTypes.LAVA)) {
                                    event.setAmount(0);
                                }
                            case 2:
                                if (source.is(DamageTypes.HOT_FLOOR))
                                    event.setAmount(0);
                            case 1:
                                if (source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.IN_FIRE))
                                    event.setAmount(0);
                                break;
                        }
                        return;
                    }
                    TrickEscapeTrick.escapeTrickHurtEvent(event);
                    if (BeyonderUtil.currentPathwayAndSequenceMatchesNoException(living, BeyonderClassInit.SAILOR.get(), 1) && (entitySource.getName().getString().toLowerCase().contains("lightning") || entitySource.getName().getString().toLowerCase().contains("thunder") || entitySource.toString().toLowerCase().contains("lightning") || entitySource.toString().toLowerCase().contains("thunder"))) {
                        event.setCanceled(true);
                        event.setAmount(0);
                    }

                    if (entitySourceOwner instanceof LivingEntity livingEntity) {
                        if (BeyonderUtil.areAllies(livingEntity, living)) {
                            event.setAmount(event.getAmount() * 0.6f);
                        }
                    }
                    MonsterClass.monsterDodgeAttack(event);
                    int stoneImmunity = tag.getInt("luckStoneDamageImmunity");
                    int stoneDamage = tag.getInt("luckStoneDamage");
                    int meteorDamage = tag.getInt("luckMeteorDamage");
                    int meteorImmunity = tag.getInt("calamityMeteorImmunity");
                    int MCLightingDamage = tag.getInt("luckLightningMCDamage");
                    int mcLightningImmunity = tag.getInt("luckMCLightningImmunity");
                    int calamityExplosionOccurrenceDamage = tag.getInt("calamityExplosionOccurrence");
                    int lotmLightningDamage = tag.getInt("luckLightningLOTMDamage");
                    int lightningBoltResistance = tag.getInt("calamityLightningBoltMonsterResistance");
                    int lotmLightningDamageCalamity = tag.getInt("calamityLightningStormResistance");
                    int tornadoResistance = tag.getInt("luckTornadoResistance");
                    int lotmLightningImmunity = tag.getInt("calamityLOTMLightningImmunity");
                    int lightningStormImmunity = tag.getInt("calamityLightningStormImmunity");
                    Level level = entity.level();
                    int enhancement = 1;
                    if (level instanceof ServerLevel serverLevel) {
                        enhancement = CalamityEnhancementData.getInstance(serverLevel).getCalamityEnhancement();
                    }
                    if (enhancement >= 2) {
                        event.setAmount((float) (event.getAmount() + (enhancement * 0.25)));
                    }
                    if (entitySource instanceof StoneEntity) {
                        if (stoneImmunity >= 1) {
                            event.setCanceled(true);
                        } else if (stoneDamage >= 1) {
                            event.setAmount(event.getAmount() / 2);
                        }
                    }
                    if (entitySource instanceof MeteorEntity || entitySource instanceof MeteorNoLevelEntity) {
                        if (meteorImmunity >= 1) {
                            event.setCanceled(true);
                        } else if (meteorDamage >= 1) {
                            event.setAmount(event.getAmount() / 2);
                        }
                    }
                    if (source.is(DamageTypes.LIGHTNING_BOLT)) {
                        if (mcLightningImmunity >= 1) {
                            event.setCanceled(true);
                        } else if (MCLightingDamage >= 1) {
                            event.setAmount(event.getAmount() / 2);
                        }
                    }
                    if (source.is(DamageTypes.EXPLOSION)) {
                        if (calamityExplosionOccurrenceDamage >= 1) {
                            event.setAmount(event.getAmount() / 2);
                        }
                    }
                    if (entitySource instanceof LightningEntity) {
                        if (lotmLightningImmunity >= 1 || lightningStormImmunity >= 1) {
                            event.setCanceled(true);
                        } else if (lotmLightningDamage >= 1 || lightningBoltResistance >= 1 || lotmLightningDamageCalamity >= 1) {
                            event.setAmount(event.getAmount() / 2);
                        }
                    }
                }
                //SAILOR FLIGHT
                if (entity instanceof Player player) {
                    int flightCancel = tag.getInt("sailorFlightDamageCancel");
                    if (!player.level().isClientSide()) {

                        //SAILOR FLIGHT
                        if (flightCancel != 0 && event.getSource() == player.damageSources().fall()) {
                            event.setCanceled(true);
                            tag.putInt("sailorFlightDamageCancel", 0);
                        }
                    }
                    rippleOfMisfortune(player);

                    //MONSTER LUCK
                    int doubleDamage = tag.getInt("luckDoubleDamage");
                    int ignoreDamage = tag.getInt("luckIgnoreDamage");
                    int halveDamage = tag.getInt("luckHalveDamage");
                    if (halveDamage >= 1) {
                        event.setAmount(event.getAmount() / 2);
                        tag.putInt("luckHalveDamage", halveDamage - 1);
                    }
                    if (ignoreDamage >= 1) {
                        event.setCanceled(true);
                        entity.getPersistentData().putInt("luckIgnoreDamage", entity.getPersistentData().getInt("luckIgnoreDamage") - 1);
                    } else if (doubleDamage >= 1) {
                        event.setAmount(event.getAmount() * 2);
                        entity.getPersistentData().putInt("luckDoubleDamage", entity.getPersistentData().getInt("luckDoubleDamage") - 1);

                    }
                }


                //STORM SEAL
                if (entity.getPersistentData().getInt("inStormSeal") >= 1) {
                    event.setCanceled(true);
                }
            }
            if (entity instanceof PlayerMobEntity playerMob && playerMob.getMaxlife() == 4) {
                event.setCanceled(true);
                event.setAmount(0);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath() && event.getOriginal().getPersistentData().contains(AbilityRegisterCommand.REGISTERED_ABILITIES_KEY)) {
            CompoundTag originalAbilities = event.getOriginal().getPersistentData().getCompound(AbilityRegisterCommand.REGISTERED_ABILITIES_KEY);
            event.getEntity().getPersistentData().put(AbilityRegisterCommand.REGISTERED_ABILITIES_KEY, originalAbilities.copy());
        }
    }


    @SubscribeEvent
    public static void deathEvent(LivingDeathEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        Entity entityAttacker = event.getSource().getEntity();
        Level level = livingEntity.level();
        if (entityAttacker instanceof Projectile projectile && projectile.getOwner() != null) {
            if (projectile.getOwner() instanceof Player) {
                entityAttacker = projectile.getOwner();
            }
        }
        if (!level.isClientSide()) {
            if (livingEntity instanceof Player pPlayer) {
                BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(pPlayer);
                ProbabilityManipulationWipe.wipeProbablility(tag);
            }
            boolean isValidPlayerAttack = false;
            boolean areAllies = false;
            if (entityAttacker instanceof Player || (entityAttacker instanceof Projectile projectile && projectile.getOwner() != null && projectile.getOwner() instanceof Player)) {
                isValidPlayerAttack = true;
                if (entityAttacker instanceof LivingEntity livingAttacker) {
                    areAllies = BeyonderUtil.areAllies(livingAttacker, livingEntity);
                } else if (entityAttacker instanceof Projectile projectile &&
                        projectile.getOwner() instanceof LivingEntity livingOwner) {
                    areAllies = BeyonderUtil.areAllies(livingOwner, livingEntity);
                }
            }
            if (BeyonderUtil.isBeyonder(livingEntity) && !event.isCanceled() && entityAttacker != livingEntity && livingEntity instanceof Player && isValidPlayerAttack && !areAllies) {
                int sequence = BeyonderUtil.getSequence(livingEntity);
                BeyonderClass pathway = BeyonderUtil.getPathway(livingEntity);
                boolean resetSequence = level.getLevelData().getGameRules().getBoolean(GameRuleInit.RESET_SEQUENCE);
                boolean safetyNet = level.getLevelData().getGameRules().getBoolean(GameRuleInit.PATHWAY_SAFETY_NET);
                boolean dropCharacteristic = level.getLevelData().getGameRules().getBoolean(GameRuleInit.SHOULD_DROP_CHARACTERISTIC);
                boolean fateReincarnation = livingEntity.getPersistentData().getInt("monsterReincarnationCounter") >= 1;
                if (dropCharacteristic) {
                    if (!safetyNet) {
                        ItemStack stack = new ItemStack(ItemInit.BEYONDER_CHARACTERISTIC.get());
                        if (fateReincarnation) {
                            BeyonderCharacteristic.setData(stack, BeyonderClassInit.MONSTER.get(), 1, false, 1);
                        } else {
                            BeyonderCharacteristic.setData(stack, pathway, sequence, false, 1);
                        }
                        ItemEntity itemEntity = new ItemEntity(level, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), stack);
                        livingEntity.level().addFreshEntity(itemEntity);
                        if (!resetSequence) {
                            if (sequence == 9) {
                                BeyonderUtil.resetPathway(livingEntity);
                            } else {
                                BeyonderUtil.setSequence(livingEntity, sequence + 1);
                            }
                        } else {
                            BeyonderUtil.removePathway(livingEntity);
                        }
                    } else if (sequence != 8 && sequence != 4) {
                        ItemStack stack = new ItemStack(ItemInit.BEYONDER_CHARACTERISTIC.get());
                        if (fateReincarnation) {
                            BeyonderCharacteristic.setData(stack, BeyonderClassInit.MONSTER.get(), 1, false, 1);
                        } else {
                            BeyonderCharacteristic.setData(stack, pathway, sequence, false, 1);
                        }
                        ItemEntity itemEntity = new ItemEntity(level, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), stack);
                        livingEntity.level().addFreshEntity(itemEntity);
                        if (!resetSequence) {
                            if (sequence == 9) {
                                BeyonderUtil.resetPathway(livingEntity);
                            } else {
                                BeyonderUtil.setSequence(livingEntity, sequence + 1);
                            }
                        } else {
                            BeyonderUtil.removePathway(livingEntity);
                        }
                    }
                }

            }
            CycleOfFate.cycleOfFateDeath(event);


            AqueousLightDrown.lightDeathEvent(event);
            CorruptionAndLuckHandler.onPlayerDeath(event);

            if (tag.getInt("inStormSeal") >= 1) {
                event.setCanceled(true);
                livingEntity.setHealth(5.0f);
            }
            tag.putDouble("corruption", 0);
            tag.putInt("age", 0);
            if (livingEntity instanceof Player player) {
                byte[] keysClicked = new byte[5];
                player.getPersistentData().putByteArray("keysClicked", keysClicked);

            }
            if (livingEntity instanceof Player && livingEntity.getCommandSenderWorld().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
                DamageSource source = event.getSource();
                Entity trueSource = source.getEntity();
                if (trueSource instanceof Player player) {
                    ItemStack stack = player.getUseItem();
                    int looting = stack.getEnchantmentLevel(Enchantments.MOB_LOOTING);
                    ItemStack drop = getDrop(livingEntity, source, looting);
                    if (!drop.isEmpty()) {
                        player.drop(drop, true);
                    }
                }
            }
        }
    }

    @Mod.EventBusSubscriber(modid = LOTM.MOD_ID)
    public static class SpawnHandler {

        @SubscribeEvent
        public static void onCheckSpawn(MobSpawnEvent.FinalizeSpawn event) {
            if (event.getEntity() instanceof PlayerMobEntity) {
                ResourceKey<Level> worldKey = event.getLevel().getLevel().dimension();
                if (Configs.COMMON.isDimensionBlocked(worldKey)) {
                    event.setSpawnCancelled(true);
                }
            }
        }
    }


    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            domainDrops(event);
        }
    }

    @SubscribeEvent
    public static void onLivingDropExperience(LivingExperienceDropEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            domainDropsExperience(event);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        CompoundTag persistentData = player.getPersistentData();
        BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
        int sequence = holder.getSequence();
        if (!player.level().isClientSide()) {
            LOTMNetworkHandler.sendToPlayer(new SyncSequencePacketS2C(holder.getSequence()), (ServerPlayer) player);
            if (persistentData.contains("DemiseCounter")) {
                int demiseCounter = persistentData.getInt("DemiseCounter");
                if (!persistentData.contains("EntityDemise") || persistentData.getInt("EntityDemise") == 0) {
                    player.getPersistentData().putInt("EntityDemise", demiseCounter);
                }
            } else {
                if (!persistentData.contains("EntityDemise") || persistentData.getInt("EntityDemise") == 0) {
                    player.getPersistentData().putInt("EntityDemise", 0);
                }
            }
            if (holder.getCurrentClass() != null && holder.getSequence() != -1) {
                holder.getCurrentClass().applyAllModifiers(player, holder.getSequence());
                player.setHealth(player.getMaxHealth());
            }
            if (!persistentData.contains("keysClicked")) {
                byte[] keysClicked = new byte[5];
                persistentData.putByteArray("keysClicked", keysClicked);
            }
            if (BeyonderUtil.currentPathwayAndSequenceMatches(player, BeyonderClassInit.APPRENTICE.get(), 4)) {
                persistentData.putInt("wormOfStar", BeyonderUtil.maxWormAmount(player));
            }
        }
    }

    @SubscribeEvent
    public static void onLivingJoinWorld(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (!entity.level().isClientSide()) {
            PlayerMobSequenceData.onEntityJoinLevel(event);
            if (!(entity instanceof Player && !(entity instanceof PlayerMobEntity)) && entity instanceof LivingEntity living) {
                if (entity.level() instanceof ServerLevel serverLevel) {
                    BeyonderEntityData mappingData = BeyonderEntityData.getInstance(serverLevel);
                    String pathwayString = mappingData.getStringForEntity(entity.getType());
                    if (pathwayString != null) {
                        BeyonderClass pathway = BeyonderUtil.getPathway(living);
                        if (pathway != null) {
                            BeyonderUtil.setSpirituality(living, BeyonderUtil.getMaxSpirituality(living));
                            pathway.applyAllModifiers(living, BeyonderUtil.getSequence(living));
                        }
                    }
                }
                LightningRedirection.onLightningJoinWorld(event);
            }
            if (entity instanceof LivingEntity livingEntity) {
                if (livingEntity instanceof PlayerMobEntity playerMobEntity) {
                    if (!playerMobEntity.level().getLevelData().getGameRules().getBoolean(GameRuleInit.NPC_SHOULD_SPAWN) && !playerMobEntity.shouldIgnoreGamerule()) {
                        event.setCanceled(true);
                    } else {
                        playerMobEntity.setSpirituality(playerMobEntity.getMaxSpirituality());
                    }
                }
            } else if (entity instanceof Projectile projectile) {
                Entity owner = projectile.getOwner();
                //if (owner != null) {
                //    projectile.getPersistentData().putBoolean("inSpiritWorld", owner.getPersistentData().getBoolean("inSpiritWorld"));
                // }
            }
        }
    }

    @SubscribeEvent
    public static void addAttributes(EntityAttributeCreationEvent event) {

    }


    @SubscribeEvent
    public static void onEntityChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity originalEntity = event.getEntity();
        if (!originalEntity.level().isClientSide()) {

        }
    }

    @SubscribeEvent
    public static void onEntityRemoved(EntityLeaveLevelEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            PlayerMobSequenceData.onEntityLeaveLevel(event); //add it to do the sequence and pathway stuff
        }
    }
}