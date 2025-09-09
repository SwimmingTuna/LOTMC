package net.swimmingtuna.lotm.beyonder;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.nihilums.tweaks.Attributes.PathwayAttributes.SpectatorAttributes;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.entity.MeteorEntity;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.entity.StoneEntity;
import net.swimmingtuna.lotm.entity.TornadoEntity;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.Earthquake;
import net.swimmingtuna.lotm.nihilums.tweaks.DamageMap.DamageMapBase;
import net.swimmingtuna.lotm.nihilums.tweaks.PathwaysPassiveEvents.SpectatorPassiveEvents;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.*;
import java.util.regex.Pattern;

import static net.swimmingtuna.lotm.util.BeyonderUtil.isLivingEntityMoving;
import static net.swimmingtuna.lotm.util.BeyonderUtil.stopFlying;

public class SpectatorClass implements BeyonderClass {
    @Override
    public List<String> sequenceNames() {
        return List.of(
                "Visionary",
                "Author",
                "Discerner",
                "Dream Weaver",
                "Manipulator",
                "Dreamwalker",
                "Hypnotist",
                "Psychiatrist",
                "Telepathist",
                "Spectator"
        );
    }

    @Override
    public List<Integer> antiDivination() {
        return List.of(20, 15, 13, 9, 5, 2, 1, 1, 1, 1);
    }

    @Override
    public List<Integer> divination() {
        return List.of(20, 15, 13, 9, 5, 2, 1, 1, 1, 1);
    }

    @Override
    public List<Integer> spiritualityLevels() {
        return List.of(25000, 10000, 6000, 3000, 1800, 800, 500, 350, 175, 125);
    }

    @Override
    public List<Integer> mentalStrength() {
        return List.of(630, 420, 320, 270, 220, 145, 110, 95, 70, 45);
    }

    @Override
    public List<Integer> spiritualityRegen() {
        return List.of(45, 30, 20, 15, 12, 9, 6, 5, 3, 2);
    }

    @Override
    public void applyAllModifiers(LivingEntity entity, int seq) {
        SpectatorAttributes.applyAll(entity, seq);
    }

    @Override
    public SimpleContainer getAbilityItemsContainer(int sequenceLevel) {
        SimpleContainer container = new SimpleContainer(45);
        Map<Integer, List<ItemStack>> orderedItems = new LinkedHashMap<>();
        for (int i = 9; i >= sequenceLevel; i--) {
            orderedItems.put(i, new ArrayList<>());
        }

        Multimap<Integer, Item> items = getItems();
        for (Map.Entry<Integer, Item> entry : items.entries()) {
            int level = entry.getKey();
            Item item = entry.getValue();

            if (level >= sequenceLevel) {
                if (item == ItemInit.FRENZY.get() && sequenceLevel < 5) {
                    continue;
                }
                orderedItems.get(level).add(item.getDefaultInstance());
            }
        }

        int slotIndex = 0;
        for (int i = 9; i >= sequenceLevel; i--) {
            List<ItemStack> levelItems = orderedItems.get(i);
            for (ItemStack stack : levelItems) {
                container.setItem(slotIndex++, stack);
            }
        }

        return container;
    }


    @Override
    public Multimap<Integer, Item> getItems() {
        HashMultimap<Integer, Item> items = HashMultimap.create();
        items.put(9, ItemInit.ALLY_MAKER.get());
        items.put(8, ItemInit.MIND_READING.get());
        items.put(7, ItemInit.AWE.get());
        items.put(7, ItemInit.FRENZY.get());
        items.put(7, ItemInit.PLACATE.get());
        items.put(6, ItemInit.BATTLE_HYPNOTISM.get());
        items.put(6, ItemInit.PSYCHOLOGICAL_INVISIBILITY.get());
        items.put(5, ItemInit.DREAM_WALKING.get());
        items.put(5, ItemInit.NIGHTMARE.get());
        items.put(4, ItemInit.APPLY_MANIPULATION.get());
        items.put(4, ItemInit.MANIPULATE_EMOTION.get());
        items.put(4, ItemInit.MANIPULATE_FONDNESS.get());
        items.put(4, ItemInit.MANIPULATE_MOVEMENT.get());
        items.put(4, ItemInit.DRAGON_BREATH.get());
        items.put(4, ItemInit.MENTAL_PLAGUE.get());
        items.remove(4, ItemInit.FRENZY.get());
        items.put(4, ItemInit.MIND_STORM.get());
        items.put(3, ItemInit.PLAGUE_STORM.get());
        items.put(3, ItemInit.CONSCIOUSNESS_STROLL.get());
        items.put(3, ItemInit.DREAM_WEAVING.get());
        items.put(2, ItemInit.DISCERN.get());
        items.put(2, ItemInit.DREAM_INTO_REALITY.get());
        items.put(1, ItemInit.PROPHECY.get());
        //items.put(1, ItemInit.METEOR_SHOWER.get());
        //items.put(1, ItemInit.METEOR_NO_LEVEL_SHOWER.get());
        items.put(0, ItemInit.ENVISION_BARRIER.get());
        items.put(0, ItemInit.ENVISION_LIFE.get());
        items.put(0, ItemInit.ENVISION_DEATH.get());
        items.put(0, ItemInit.ENVISION_HEALTH.get());
        items.put(0, ItemInit.ENVISION_LOCATION.get());
        items.put(0, ItemInit.ENVISION_WEATHER.get());
        items.put(0, ItemInit.ENVISION_KINGDOM.get());

        return items;
    }

    @Override
    public ChatFormatting getColorFormatting() {
        return ChatFormatting.AQUA;
    }


    public void applyMobEffect(LivingEntity pPlayer, MobEffect mobEffect, int duration, int amplifier, boolean ambient, boolean visible) {
        MobEffectInstance currentEffect = pPlayer.getEffect(mobEffect);
        MobEffectInstance newEffect = new MobEffectInstance(mobEffect, duration, amplifier, ambient, visible);
        if (currentEffect == null) {
            pPlayer.addEffect(newEffect);
        } else if (currentEffect.getAmplifier() < amplifier) {
            pPlayer.addEffect(newEffect);
        } else if (currentEffect.getAmplifier() == amplifier && duration >= currentEffect.getDuration()) {
            pPlayer.addEffect(newEffect);
        }
    }

    public static final Pattern PROPHECY_PATTERN = Pattern.compile("(\\w+) will (.*?) in (\\d+) (second|seconds|minute|minutes)");
    public static final Map<String, String> EVENT_TO_TAG = new HashMap<>();

    static {
        EVENT_TO_TAG.put("encounter a meteor", "spectatorProphesizedMeteor");
        EVENT_TO_TAG.put("encounter a tornado", "spectatorProphesizedTornado");
        EVENT_TO_TAG.put("encounter an earthquake", "spectatorProphesizedEarthquake");
        EVENT_TO_TAG.put("encounter a plague", "spectatorProphesizedPlague");
        EVENT_TO_TAG.put("have potion success", "spectatorProphesizedGuaranteedPotion");
        EVENT_TO_TAG.put("encounter weakness", "spectatorProphesizedWeakness");
        EVENT_TO_TAG.put("encounter a sinkhole", "spectatorProphesizedSinkhole");
        EVENT_TO_TAG.put("be healed", "spectatorProphesizedHealed");
        EVENT_TO_TAG.put("be lucky", "spectatorProphesizedLuck");
        EVENT_TO_TAG.put("be unlucky", "spectatorProphesizedMisfortune");
    }

    public static void prophecyTickEvent(LivingEvent.LivingTickEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            LivingEntity livingEntity = event.getEntity();
            CompoundTag tag = livingEntity.getPersistentData();
            int meteor = tag.getInt("spectatorProphesizedMeteor");
            int tornado = tag.getInt("spectatorProphesizedTornado");
            int earthquake = tag.getInt("spectatorProphesizedEarthquake");
            int plague = tag.getInt("spectatorProphesizedPlague");
            int potion = tag.getInt("spectatorProphesizedGuaranteedPotion");
            int weakness = tag.getInt("spectatorProphesizedWeakness");
            int healed = tag.getInt("spectatorProphesizedHealed");
            int luck = tag.getInt("spectatorProphesizedLuck");
            int sinkhole = tag.getInt("spectatorProphesizedSinkhole");
            int misfortune = tag.getInt("spectatorProphesizedMisfortune");
            if (sinkhole == 1) {
                tag.putInt("spectatorProphecySinkholeOccurence", 80);
            }
            if (tag.getInt("spectatorProphecySinkholeOccurence") >= 1) {
                int sinkholeOccurence = tag.getInt("spectatorProphecySinkholeOccurence");
                tag.putInt("spectatorProphecySinkholeOccurence", sinkholeOccurence - 1);
                int x = tag.getInt("spectatorProphesizedSinkholeX");
                int y = tag.getInt("spectatorProphesizedSinkholeY");
                int z = tag.getInt("spectatorProphesizedSinkholeZ");

                // Initialize sinkhole position if not set
                if (x == 0 && y == 0 && z == 0) {
                    tag.putInt("spectatorProphesizedSinkholeX", (int) livingEntity.getX());
                    tag.putInt("spectatorProphesizedSinkholeY", (int) livingEntity.getY());
                    tag.putInt("spectatorProphesizedSinkholeZ", (int) livingEntity.getZ());
                }

                // Reset position when sinkhole ends
                if (sinkholeOccurence == 1) {
                    tag.putInt("spectatorProphesizedSinkholeX", 0);
                    tag.putInt("spectatorProphesizedSinkholeY", 0);
                    tag.putInt("spectatorProphesizedSinkholeZ", 0);
                }

                if (sinkholeOccurence != 1) {
                    int livingX = tag.getInt("spectatorProphesizedSinkholeX");
                    int livingY = tag.getInt("spectatorProphesizedSinkholeY");
                    int livingZ = tag.getInt("spectatorProphesizedSinkholeZ");
                    if (livingEntity.tickCount % 3 == 0) {
                        tag.putInt("spectatorProphecySinkholeOccurence", sinkholeOccurence - 1);
                        int currentDepth = tag.getInt("sinkholeProphecyCurrentDepth");

                        if (sinkholeOccurence == 80) {
                            currentDepth = 0;
                            tag.putInt("sinkholeProphecyCurrentDepth", 0);
                        }

                        int sinkholeRadius = (int) (BeyonderUtil.getDamage(livingEntity).get(ItemInit.PROPHECY.get()) * 1.5);
                        sinkholeRadius = Math.max(5, sinkholeRadius);
                        BlockPos center = new BlockPos(livingX, livingY, livingZ);

                        // Destroy one layer per tick (removed z % 2 == 0 condition)
                        if (currentDepth < 40) {
                            currentDepth++;
                            tag.putInt("sinkholeProphecyCurrentDepth", currentDepth);
                            for (int i = -sinkholeRadius; i <= sinkholeRadius; i++) {
                                for (int j = -sinkholeRadius; j <= sinkholeRadius; j++) {
                                    if (i * i + j * j <= sinkholeRadius * sinkholeRadius) {
                                        BlockPos pos = center.offset(i, 0, j);
                                        BlockPos targetPos = pos.offset(0, -currentDepth, 0);
                                        if (targetPos.getY() <= -50) {
                                            continue;
                                        }
                                        BlockState state = livingEntity.level().getBlockState(targetPos);
                                        if (!state.isAir() && state.getBlock() != Blocks.BEDROCK && !state.is(BlockTags.WITHER_IMMUNE)) {
                                            if (livingEntity.getRandom().nextInt(5) == 0 && livingEntity.level() instanceof ServerLevel serverLevel) {
                                                serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5, 3, 0.3, 0.3, 0.3, 0.05);
                                            }
                                            livingEntity.level().destroyBlock(targetPos, false);
                                        }
                                    }
                                }
                            }
                        }

                        // Clean up existing blocks every 10 ticks
                        if (sinkholeOccurence % 10 == 0) {
                            for (int depth = 1; depth <= currentDepth; depth++) {
                                for (int i = -sinkholeRadius; i <= sinkholeRadius; i++) {
                                    for (int j = -sinkholeRadius; j <= sinkholeRadius; j++) {
                                        if (i * i + j * j <= sinkholeRadius * sinkholeRadius) {
                                            BlockPos pos = center.offset(i, 0, j);
                                            BlockPos targetPos = pos.offset(0, -depth, 0);
                                            if (targetPos.getY() <= -50) {
                                                continue;
                                            }
                                            BlockState state = livingEntity.level().getBlockState(targetPos);
                                            if (!state.isAir() && state.getBlock() != Blocks.BEDROCK && !state.is(BlockTags.WITHER_IMMUNE)) {
                                                livingEntity.level().destroyBlock(targetPos, false);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Entity physics
                        List<LivingEntity> entities = livingEntity.level().getEntitiesOfClass(LivingEntity.class, new AABB(center.getX() - sinkholeRadius - 5, center.getY() - currentDepth - 5, center.getZ() - sinkholeRadius - 5, center.getX() + sinkholeRadius + 5, center.getY() + 10, center.getZ() + sinkholeRadius + 5));
                        for (LivingEntity entity : entities) {
                            double dx = center.getX() + 0.5 - entity.getX();
                            double dz = center.getZ() + 0.5 - entity.getZ();
                            double distance = Math.sqrt(dx * dx + dz * dz);
                            if (distance <= sinkholeRadius * 0.8) {
                                if (entity.getY() > center.getY() - currentDepth - 1) {
                                    entity.setDeltaMovement(entity.getDeltaMovement().add(0, -0.4, 0));
                                    entity.hurtMarked = true;
                                }
                            } else if (distance <= sinkholeRadius * 2) {
                                dx = dx / distance;
                                dz = dz / distance;
                                double pullStrength = 0.1 * (1 - distance / (sinkholeRadius * 2));
                                entity.setDeltaMovement(entity.getDeltaMovement().add(dx * (pullStrength * 3), -0.1, dz * (pullStrength * 3)));
                                entity.hurtMarked = true;
                                if (entity instanceof Player player && !player.isCreative()) {
                                    player.setSprinting(false);
                                    if (player.isCrouching()) {
                                        player.setDeltaMovement(player.getDeltaMovement().scale(0.8));
                                        player.hurtMarked = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (meteor >= 1) {
                tag.putInt("spectatorProphesizedMeteor", meteor - 1);
            }
            if (sinkhole >= 1) {
                tag.putInt("spectatorProphesizedSinkhole", sinkhole - 1);
            }
            if (tornado >= 1) {
                tag.putInt("spectatorProphesizedTornado", tornado - 1);
            }
            if (earthquake >= 1) {
                tag.putInt("spectatorProphesizedEarthquake", earthquake - 1);
            }
            if (plague >= 1) {
                tag.putInt("spectatorProphesizedPlague", plague - 1);
            }
            if (potion >= 1) {
                tag.putInt("spectatorProphesizedGuaranteedPotion", potion - 1);
            }
            if (weakness >= 1) {
                tag.putInt("spectatorProphesizedWeakness", weakness - 1);
            }
            if (healed >= 1) {
                tag.putInt("spectatorProphesizedHealed", healed - 1);
            }
            if (luck >= 1) {
                tag.putInt("spectatorProphesizedLuck", luck - 1);
            }
            if (misfortune >= 1) {
                tag.putInt("spectatorProphesizedMisfortune", misfortune - 1);
            }

            if (luck == 1) {
                tag.putDouble("luck", tag.getDouble("luck") + 200);
            }
            if (misfortune == 1) {
                tag.putDouble("misfortune", tag.getDouble("misfortune") + 200);
            }
            if (meteor == 1) {
                MeteorEntity.summonMeteorAtPositionWithScale(livingEntity, livingEntity.getX(), livingEntity.getY() + 200, livingEntity.getZ(), livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), 9);
                for (int i = 0; i < 6; i++) {
                    float random = BeyonderUtil.getRandomInRange(150);
                    float random2 = BeyonderUtil.getRandomInRange(150);
                    MeteorEntity.summonMeteorAtPositionWithScale(livingEntity, livingEntity.getX() + random, livingEntity.getY() + 200, livingEntity.getZ() + random2, livingEntity.getX() + random2, livingEntity.getY(), livingEntity.getZ() + random2, 6 + (int) (Math.random() * 3));
                }
            }
            if (tornado == 1) {
                TornadoEntity tornadoEntity = new TornadoEntity(EntityInit.TORNADO_ENTITY.get(), livingEntity.level());
                tornadoEntity.teleportTo(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
                tornadoEntity.setTornadoHeight(100);
                tornadoEntity.setTornadoRadius(70);
                if (BeyonderUtil.getSequence(livingEntity) == 0) {
                    tornadoEntity.setTornadoLifecount(300);
                } else {
                    tornadoEntity.setTornadoLifecount(150);
                }
                tornadoEntity.setTornadoMov(livingEntity.getLookAngle().scale(0.5f).toVector3f());
                tornadoEntity.setTornadoRandom(true);
                tornadoEntity.setTornadoPickup(false);
                livingEntity.level().addFreshEntity(tornadoEntity);
            }
            if (earthquake == 1) {
                tag.putInt("prophesizedEarthquake", 300);
            }
            if (tag.getInt("prophesizedEarthquake") >= 1) {
                int prophesizedEarthquake = tag.getInt("prophesizedEarthquake");
                tag.putInt("prophesizedEarthquake", prophesizedEarthquake - 1);
                int radius = 80;
                if (prophesizedEarthquake % 20 == 0) {
                    for (LivingEntity entity : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate((radius)))) {
                        if (entity.onGround()) {
                            entity.hurt(BeyonderUtil.fallSource(livingEntity, entity), 20);
                        }
                    }
                }
                if (prophesizedEarthquake % 2 == 0) {
                    AABB checkArea = livingEntity.getBoundingBox().inflate(radius);
                    Random random = new Random();
                    for (BlockPos blockPos : BlockPos.betweenClosed(new BlockPos((int) checkArea.minX, (int) checkArea.minY, (int) checkArea.minZ), new BlockPos((int) checkArea.maxX, (int) checkArea.maxY, (int) checkArea.maxZ))) {
                        if (!livingEntity.level().getBlockState(blockPos).isAir() && Earthquake.isOnSurface(livingEntity.level(), blockPos)) {
                            if (random.nextInt(20) == 1) {
                                BlockState blockState = livingEntity.level().getBlockState(blockPos);
                                if (livingEntity.level() instanceof ServerLevel serverLevel) {
                                    serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, blockState),
                                            blockPos.getX(),
                                            blockPos.getY() + 1,
                                            blockPos.getZ(),
                                            0, 0.0, 0.0, 0, 0);
                                }
                            }
                            if (random.nextInt(4000) == 1) {
                                livingEntity.level().destroyBlock(blockPos, false);
                            } else if (random.nextInt(10000) == 2) {
                                StoneEntity stoneEntity = new StoneEntity(livingEntity.level(), livingEntity);
                                ScaleData scaleData = ScaleTypes.BASE.getScaleData(stoneEntity);
                                stoneEntity.teleportTo(blockPos.getX(), blockPos.getY() + 3, blockPos.getZ());
                                stoneEntity.setDeltaMovement(0, (3 + (Math.random() * (6 - 3))), 0);
                                stoneEntity.setStoneYRot((int) (Math.random() * 18));
                                stoneEntity.setStoneXRot((int) (Math.random() * 18));
                                scaleData.setScale((float) (1 + (Math.random()) * 2.0f));
                                livingEntity.level().addFreshEntity(stoneEntity);
                            }
                        }
                    }
                }
            }
            if (plague == 1) {
                for (LivingEntity living : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate(80))) {
                    BeyonderUtil.applyMobEffect(living, MobEffects.WITHER, 500, 5, true, true);
                    BeyonderUtil.applyNoRegeneration(living, 300);
                    BeyonderUtil.applyMobEffect(living, MobEffects.WEAKNESS, 500, 3, true, true);
                    BeyonderUtil.applyMobEffect(living, MobEffects.CONFUSION, 300, 1, true, true);
                }
            }
            if (weakness == 1) {
                BeyonderUtil.applyMobEffect(livingEntity, ModEffects.ABILITY_WEAKNESS.get(), 1200, 1, true, true);
            }
            if (healed == 1) {
                livingEntity.setHealth(livingEntity.getMaxHealth());
                for (MobEffectInstance effect : new ArrayList<>(livingEntity.getActiveEffects())) {
                    if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                        livingEntity.removeEffect(effect.getEffect());
                    }
                }
            }
            if (luck == 1) {
                livingEntity.getPersistentData().putDouble("luck", livingEntity.getPersistentData().getDouble("luck") + 100);
            }
            if (misfortune == 1) {
                livingEntity.getPersistentData().putDouble("misfortune", livingEntity.getPersistentData().getDouble("misfortune") + 100);
            }
        }
    }

    public static void demiseTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        CompoundTag tag = entity.getPersistentData();
        if (!entity.level().isClientSide()) {
            LivingEntity livingEntity = event.getEntity();
            if (!livingEntity.level().isClientSide()) {
                int flyTime = tag.getInt("LOTMFlying");
                float flySpeed = tag.getFloat("LOTMFlySpeed");
                if (flyTime >= 1) {
                    tag.putInt("LOTMFlying", flyTime - 1);
                    if (livingEntity instanceof Player pPlayer ) {
                        Abilities playerAbilities = pPlayer.getAbilities();
                        if (!pPlayer.isCreative()) {
                            playerAbilities.mayfly = true;
                            playerAbilities.setFlyingSpeed(flySpeed);
                        }
                        pPlayer.onUpdateAbilities();
                        if (livingEntity instanceof ServerPlayer serverPlayer) {
                            serverPlayer.connection.send(new ClientboundPlayerAbilitiesPacket(playerAbilities));
                        }
                    } else if (livingEntity instanceof PlayerMobEntity playerMobEntity) {
                        playerMobEntity.setIsFlying(true);
                        playerMobEntity.setFlySpeed(flySpeed);
                    }
                } else {
                    boolean x = livingEntity instanceof Player player && (player.isCreative() || player.isSpectator());
                    if (!x) {
                        stopFlying(livingEntity);
                    }
                }
            }
            boolean hasSpectatorDemise = entity.hasEffect(ModEffects.SPECTATORDEMISE.get());
            if (!hasSpectatorDemise) {
                tag.putInt("EntityDemise", 0);
                tag.putInt("NonDemise", 0);
            }

            if (hasSpectatorDemise) {
                MobEffectInstance demiseEffect = entity.getEffect(ModEffects.SPECTATORDEMISE.get());
                if (demiseEffect != null) {
                    int effectDuration = demiseEffect.getDuration();
                    int effectDurationSeconds;

                    if (effectDuration < 20) {
                        effectDurationSeconds = 1;
                    } else {
                        effectDurationSeconds = (effectDuration + 19) / 20;
                    }

                    int demise = tag.getInt("EntityDemise");
                    int nonDemise = tag.getInt("NonDemise");
                    if (isLivingEntityMoving(entity)) {
                        tag.putInt("EntityDemise", demise + 1);
                    } else {
                        tag.putInt("NonDemise", nonDemise + 1);
                    }

                    if (demise == 400) {
                        entity.kill();
                        tag.putInt("NonDemise", 0);
                    }
                    if (nonDemise > 200) {
                        tag.putInt("EntityDemise", 0);
                        tag.putInt("NonDemise", 0);
                        entity.removeEffect(ModEffects.SPECTATORDEMISE.get());
                        tag.putInt("NonDemise", 0);
                    }
                    if (nonDemise == 200) {
                        tag.putInt("EntityDemise", 0);
                        tag.putInt("NonDemise", 0);
                        entity.removeEffect(ModEffects.SPECTATORDEMISE.get());
                        entity.sendSystemMessage(Component.literal("You survived your fate").withStyle(ChatFormatting.GREEN).withStyle(ChatFormatting.BOLD));
                    }

                    if (demise == 20) {
                        tag.putInt("EntityDemise", 21);
                        entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 19 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                    }
                    if (demise == 40) {
                        tag.putInt("EntityDemise", 41);
                        entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 18 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                    }
                    if (demise == 60) {
                        tag.putInt("EntityDemise", 61);
                        entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 17 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                    }
                    if (demise == 80) {
                        tag.putInt("EntityDemise", 81);
                        entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 16 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                    }
                    if (demise == 100) {
                        tag.putInt("EntityDemise", 101);
                        entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 15 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                    }
                    if (demise == 120) {
                        tag.putInt("EntityDemise", 121);
                        entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 14 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                    }
                    if (demise == 140) {
                        tag.putInt("EntityDemise", 141);
                        entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 13 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                    }
                    if (demise == 160) {
                        tag.putInt("EntityDemise", 161);
                        entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 12 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                    }
                    if (demise == 180) {
                        tag.putInt("EntityDemise", 181);
                        entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 11 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                    }
                    if (demise == 200) {
                        tag.putInt("EntityDemise", 201);
                        entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 10 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                    }
                    if (demise == 220) {
                        tag.putInt("EntityDemise", 221);
                        entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 9 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                    }
                    if (demise == 240) {
                        tag.putInt("EntityDemise", 241);
                        entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 8 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                    }
                    if (demise == 260) {
                        tag.putInt("EntityDemise", 261);
                        entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 7 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                    }
                    if (demise == 280) {
                        tag.putInt("EntityDemise", 281);
                        entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 6 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                    }
                    if (demise == 300) {
                        tag.putInt("EntityDemise", 301);
                        entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 5 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                    }
                    if (demise == 320) {
                        tag.putInt("EntityDemise", 321);
                        entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 4 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                    }
                    if (demise == 340) {
                        tag.putInt("EntityDemise", 341);
                        entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 3 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                    }
                    if (demise == 360) {
                        tag.putInt("EntityDemise", 361);
                        entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 2 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                    }
                    if (demise == 380) {
                        tag.putInt("EntityDemise", 381);
                        entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 1 second, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                    }


                    if (nonDemise >= 20 && nonDemise <= 180 && nonDemise % 20 == 0) {
                        tag.putInt("NonDemise", nonDemise + 1);
                        int standStillSecondsLeft = (200 - nonDemise) / 20;
                        entity.sendSystemMessage(Component.literal("You need to stand still for " + standStillSecondsLeft + " more seconds").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
                    }
                }
            } else if (entity.tickCount % 100 == 0) {
                tag.putInt("NonDemise", 0);
                tag.putInt("EntityDemise", 0);
            }
        }
    }

    @Override
    public void removeAllEvents(LivingEntity entity) {
        SpectatorPassiveEvents.removeAllEvents(entity);
    }

    @Override
    public void addAllEvents(LivingEntity entity, int sequence) {
        SpectatorPassiveEvents.addAllEvents(entity, sequence);
    }

//    @Override
//    public void recalculateMap(int sequence){
//        damageMap.put(ItemInit.APPLY_MANIPULATION.get(), 0.0f);
//        damageMap.put(ItemInit.AWE.get(), 285.0f - (sequence * 22.5f));
//        damageMap.put(ItemInit.BATTLE_HYPNOTISM.get(), 600.0f - (sequence * 30));
//        damageMap.put(ItemInit.CONSCIOUSNESS_STROLL.get(), 0.0f);
//        damageMap.put(ItemInit.DISCERN.get(), 0.0f);
//        damageMap.put(ItemInit.DRAGON_BREATH.get(), applyAbilityStrengthened((float) ((90.0f * dreamIntoReality) - (sequence * 6)) / abilityWeakness, abilityStrengthened));
//        damageMap.put(ItemInit.DREAM_INTO_REALITY.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
//        damageMap.put(ItemInit.DREAM_WALKING.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));
//        damageMap.put(ItemInit.DREAM_WEAVING.get(), applyAbilityStrengthened((30.0f - (sequence * 4.5f)) / abilityWeakness, abilityStrengthened));
//        damageMap.put(ItemInit.ENVISION_BARRIER.get(), applyAbilityStrengthened((151.5f - (sequence * 30)) / abilityWeakness, abilityStrengthened));
//        damageMap.put(ItemInit.ENVISION_DEATH.get(), applyAbilityStrengthened((float) ((20.0f + (dreamIntoReality * 5f)) - (sequence * 15)) / abilityWeakness, abilityStrengthened));
//        damageMap.put(ItemInit.ENVISION_HEALTH.get(), applyAbilityStrengthened((float) (0.99f - (sequence * 0.075f) + (dreamIntoReality * 0.075f)) / abilityWeakness, abilityStrengthened));
//        damageMap.put(ItemInit.ENVISION_KINGDOM.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));
//        damageMap.put(ItemInit.ENVISION_LIFE.get(), applyAbilityStrengthened((4.5f + (sequence * 1.5f)) * abilityWeakness, abilityStrengthened));
//        damageMap.put(ItemInit.ENVISION_LOCATION.get(), applyAbilityStrengthened((float) (750.0f / dreamIntoReality) / abilityWeakness, abilityStrengthened));
//        damageMap.put(ItemInit.ENVISION_WEATHER.get(), applyAbilityStrengthened((float) (750.0f / dreamIntoReality) / abilityWeakness, abilityStrengthened));
//        damageMap.put(ItemInit.FRENZY.get(), applyAbilityStrengthened((float) ((22.5f - sequence * 1.5f) * dreamIntoReality) / abilityWeakness, abilityStrengthened));
//        damageMap.put(ItemInit.MANIPULATE_MOVEMENT.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
//        damageMap.put(ItemInit.MANIPULATE_EMOTION.get(), applyAbilityStrengthened((225.0f - (sequence * 30)) / abilityWeakness, abilityStrengthened));
//        damageMap.put(ItemInit.MANIPULATE_FONDNESS.get(), applyAbilityStrengthened((float) (900.0f * dreamIntoReality) / abilityWeakness, abilityStrengthened));
//        damageMap.put(ItemInit.MENTAL_PLAGUE.get(), applyAbilityStrengthened((float) (300.0f / dreamIntoReality) * abilityWeakness, abilityStrengthened));
//        damageMap.put(ItemInit.MIND_READING.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));
//        damageMap.put(ItemInit.MIND_STORM.get(), applyAbilityStrengthened((45.0f - (sequence * 3)) / abilityWeakness, abilityStrengthened));
//        damageMap.put(ItemInit.NIGHTMARE.get(), applyAbilityStrengthened((60.0f - (sequence * 3)) / abilityWeakness, abilityStrengthened));
//        damageMap.put(ItemInit.PLACATE.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));
//        damageMap.put(ItemInit.PLAGUE_STORM.get(), applyAbilityStrengthened((float) ((18.0f * dreamIntoReality) - (sequence * 2.25f)) / abilityWeakness, abilityStrengthened));
//        damageMap.put(ItemInit.PROPHECY.get(), applyAbilityStrengthened((float) (12.0f + (dreamIntoReality * 3) - (sequence * 6)) / abilityWeakness, abilityStrengthened));
//        damageMap.put(ItemInit.PSYCHOLOGICAL_INVISIBILITY.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));
//    }
}
