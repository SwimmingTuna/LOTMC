package net.swimmingtuna.lotm.beyonder;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.swimmingtuna.lotm.nihilums.tweaks.Attributes.PathwayAttributes.MonsterAttributes;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.entity.LightningEntity;
import net.swimmingtuna.lotm.entity.StoneEntity;
import net.swimmingtuna.lotm.entity.TornadoEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.init.ParticleInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.MisfortuneManipulation;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.SendParticleS2C;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EFunctions;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventManager;
import net.swimmingtuna.lotm.nihilums.tweaks.PathwaysPassiveEvents.MonsterPassiveEvents;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.world.worlddata.CalamityEnhancementData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MonsterClass implements BeyonderClass {
    @Override
    public List<String> sequenceNames() {
        return List.of(
                "Wheel of Fortune",
                "Snake of Mercury",
                "Soothsayer",
                "Chaoswalker",
                "Misfortune Mage",
                "Winner",
                "Calamity Priest",
                "Lucky One",
                "Robot",
                "Monster"
        );
    }

    @Override
    public List<Integer> antiDivination() {
        return List.of(70, 60, 45, 40, 30, 10, 7, 5, 1, 1);
    }

    @Override
    public List<Integer> divination() {
        return List.of(45, 37, 30, 22, 27, 9, 7, 6, 1, 1);
    }

    @Override
    public List<Integer> spiritualityLevels() {
        return List.of(29000, 11500, 6800, 3400, 2200, 1100, 600, 440, 240, 175);
    }

    @Override
    public List<Integer> mentalStrength() {
        return List.of(450, 320, 210, 175, 150, 110, 80, 70, 50, 33);
    }

    @Override
    public List<Integer> spiritualityRegen() {
        return List.of(50, 33, 25, 15, 12, 9, 7, 6, 4, 3);
    }


    @Override
    public void applyAllModifiers(LivingEntity entity, int seq) {
        MonsterAttributes.applyAll(entity, seq);
    }

    @Override
    public Multimap<Integer, Item> getItems() {
        HashMultimap<Integer, Item> items = HashMultimap.create();
        items.put(9, ItemInit.ALLY_MAKER.get());
        items.put(9, ItemInit.SPIRITVISION.get());
        items.put(9, ItemInit.MONSTERDANGERSENSE.get());

        items.put(8, ItemInit.MONSTERPROJECTILECONTROL.get());

        items.put(7, ItemInit.LUCKPERCEPTION.get());

        items.put(6, ItemInit.PSYCHESTORM.get());

        items.put(5, ItemInit.LUCK_MANIPULATION.get());
        items.put(5, ItemInit.LUCKDEPRIVATION.get());
        items.put(5, ItemInit.LUCKGIFTING.get());
        items.put(5, ItemInit.MISFORTUNEBESTOWAL.get());
        items.put(5, ItemInit.LUCKFUTURETELLING.get());

        items.put(4, ItemInit.DECAYDOMAIN.get());
        items.put(4, ItemInit.PROVIDENCEDOMAIN.get());
        items.put(4, ItemInit.LUCKCHANNELING.get());
        items.put(4, ItemInit.LUCKDENIAL.get());
        items.put(4, ItemInit.MISFORTUNEMANIPULATION.get());
        items.put(4, ItemInit.MONSTERCALAMITYATTRACTION.get());

        items.put(3, ItemInit.CALAMITYINCARNATION.get());
        items.put(3, ItemInit.ENABLEDISABLERIPPLE.get());
        items.put(3, ItemInit.AURAOFCHAOS.get());
        items.put(3, ItemInit.CHAOSWALKERCOMBAT.get());
        items.put(3, ItemInit.MISFORTUNEREDIRECTION.get());
        items.put(3, ItemInit.MONSTERDOMAINTELEPORATION.get());

        items.put(2, ItemInit.WHISPEROFCORRUPTION.get());
        items.put(2, ItemInit.FORTUNEAPPROPIATION.get());
        items.put(2, ItemInit.FALSEPROPHECY.get());
        items.put(2, ItemInit.MISFORTUNEIMPLOSION.get());

        items.put(1, ItemInit.MONSTERREBOOT.get());
        items.put(1, ItemInit.FATEREINCARNATION.get());
        items.put(1, ItemInit.CYCLEOFFATE.get());
        items.put(1, ItemInit.CHAOSAMPLIFICATION.get());
        items.put(1, ItemInit.FATEDCONNECTION.get());
        items.put(1, ItemInit.REBOOTSELF.get());

        items.put(0, ItemInit.PROBABILITYMISFORTUNE.get());
        items.put(0, ItemInit.PROBABILITYFORTUNE.get());
        items.put(0, ItemInit.PROBABILITYFORTUNEINCREASE.get());
        items.put(0, ItemInit.PROBABILITYMISFORTUNEINCREASE.get());
        items.put(0, ItemInit.PROBABILITYWIPE.get());
        items.put(0, ItemInit.PROBABILITYEFFECT.get());
        items.put(0, ItemInit.PROBABILITYINFINITEFORTUNE.get());
        items.put(0, ItemInit.PROBABILITYINFINITEMISFORTUNE.get());

        return items;
    }

    @Override
    public ChatFormatting getColorFormatting() {
        return ChatFormatting.GRAY;
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

    public static void monsterLuckPoisonAttacker(LivingEntity pPlayer) {
        if (pPlayer.tickCount % 100 == 0) {
            if (pPlayer.getPersistentData().getInt("luckAttackerPoisoned") >= 1) {
                for (LivingEntity livingEntity : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().inflate(50))) {
                    if (livingEntity != pPlayer) {
                        if (livingEntity.getPersistentData().getInt("attackedMonster") >= 1) {
                            BeyonderUtil.applyParalysis(livingEntity, 60);
                            livingEntity.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 2, true, true));
                            livingEntity.getPersistentData().putInt("attackedMonster", 0);
                            pPlayer.getPersistentData().putInt("luckAttackerPoisoned", pPlayer.getPersistentData().getInt("luckAttackerPoisoned") - 1);
                        }
                    }
                }
            }
        }
    }

    public static void monsterLuckIgnoreMobs(LivingEntity pPlayer) {
        if (pPlayer.tickCount % 40 == 0) {
            if (pPlayer.getPersistentData().getInt("luckIgnoreMobs") >= 1) {
                for (Mob mob : pPlayer.level().getEntitiesOfClass(Mob.class, pPlayer.getBoundingBox().inflate(20))) {
                    if (mob.getTarget() == pPlayer) {
                        for (LivingEntity living : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().inflate(50))) {
                            if (living != null) {
                                mob.setTarget(living);
                            } else {
                                BeyonderUtil.applyParalysis(mob, 60);
                            }
                        }
                        pPlayer.getPersistentData().putInt("luckIgnoreMobs", pPlayer.getPersistentData().getInt("luckIgnoreMobs") - 1);
                    }
                }
            }
        }
    }

    public static void decrementMonsterAttackEvent(LivingEntity livingEntity) {
        if (livingEntity.getPersistentData().getInt("attackedMonster") >= 1) {
            livingEntity.getPersistentData().putInt("attackedMonster", livingEntity.getPersistentData().getInt("attackedMonster") - 1);
        } else {
            EventManager.removeFromRegularLoop(livingEntity, EFunctions.DECREMENT_MONSTER_ATTACK_EVENT.get());
        }
    }

    public static void monsterDodgeAttack(LivingHurtEvent event) {
        LivingEntity livingEntity = event.getEntity();
        DamageSource source = event.getSource();
        Entity entitySource = source.getEntity();
        if (!livingEntity.level().isClientSide() && BeyonderUtil.isBeyonderCapable(livingEntity)) {
            if ((entitySource != null && entitySource != livingEntity) && !source.is(DamageTypes.CRAMMING) && !source.is(DamageTypes.STARVE) && !source.is(DamageTypes.FALL) && !source.is(DamageTypes.DROWN) && !source.is(DamageTypes.FELL_OUT_OF_WORLD) && !source.is(DamageTypes.ON_FIRE)) {
                if (BeyonderUtil.currentPathwayMatchesNoException(livingEntity, BeyonderClassInit.MONSTER.get())) {
                    int randomChance = (int) ((Math.random() * 20) - BeyonderUtil.getSequence(livingEntity));
                    if (randomChance >= 13) {
                        double amount = event.getAmount();
                        double x = 0;
                        double z = 0;
                        Random random = new Random();
                        if (random.nextInt(2) == 0) {
                            x = Math.min(3, amount * -0.15);
                            z = Math.min(3, amount * -0.15);
                        } else {
                            x = Math.min(3, amount * 0.15);
                            z = Math.min(3, amount * 0.15);
                        }
                        livingEntity.setDeltaMovement(x, 1, z);
                        livingEntity.hurtMarked = true;
                        event.setAmount(0);
                        livingEntity.sendSystemMessage(Component.literal("A breeze of wind moved you out of the way of damage").withStyle(ChatFormatting.GREEN));
                    }
                }
            }
        }
    }

    public static void monsterDodgeAttack(LivingAttackEvent event) {
        LivingEntity livingEntity = event.getEntity();
        DamageSource source = event.getSource();
        Entity entitySource = source.getEntity();
        if (!livingEntity.level().isClientSide() && BeyonderUtil.isBeyonderCapable(livingEntity)) {
            if ((entitySource != null && entitySource != livingEntity) && !source.is(DamageTypes.CRAMMING) && !source.is(DamageTypes.STARVE) && !source.is(DamageTypes.FALL) && !source.is(DamageTypes.DROWN) && !source.is(DamageTypes.FELL_OUT_OF_WORLD) && !source.is(DamageTypes.ON_FIRE)) {
                if (BeyonderUtil.currentPathwayMatchesNoException(livingEntity, BeyonderClassInit.MONSTER.get())) {
                    int randomChance = (int) ((Math.random() * 20) - BeyonderUtil.getSequence(livingEntity));
                    if (randomChance >= 13) {
                        double amount = event.getAmount();
                        double x = 0;
                        double z = 0;
                        Random random = new Random();
                        if (random.nextInt(2) == 0) {
                            x = Math.min(3, amount * -0.15);
                            z = Math.min(3, amount * -0.15);
                        } else {
                            x = Math.min(3, amount * 0.15);
                            z = Math.min(3, amount * 0.15);
                        }
                        livingEntity.setDeltaMovement(x, 0.5, z);
                        livingEntity.hurtMarked = true;
                        event.setCanceled(true);
                        livingEntity.sendSystemMessage(Component.literal("A breeze of wind moved you out of the way of damage").withStyle(ChatFormatting.GREEN));
                    }
                }
            }
        }
    }

    public static void monsterPassive(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        if (!livingEntity.level().isClientSide() && livingEntity.tickCount % 10 == 0) {
            if (BeyonderUtil.currentPathwayAndSequenceMatchesNoException(livingEntity, BeyonderClassInit.MONSTER.get(), 7)) {
                for (LivingEntity target : BeyonderUtil.getNonAlliesNearby(livingEntity, 90)) {
                    if (BeyonderUtil.isLookingAt(livingEntity, target, 5)) {
                        int beneficialEffectCount = 0;
                        for (MobEffectInstance effectInstance : target.getActiveEffects()) {
                            MobEffect effect = effectInstance.getEffect();
                            if (effect.isBeneficial()) {
                                beneficialEffectCount++;
                            }
                        }
                        boolean canResist = BeyonderUtil.currentPathwayMatchesNoException(livingEntity, BeyonderClassInit.MONSTER.get()) && BeyonderUtil.getSequence(target) <= BeyonderUtil.getSequence(livingEntity);
                        if (!canResist) {
                            int damage = (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.MONSTERREBOOT.get());
                            if (livingEntity.getPersistentData().getInt("luckDeprivation") == 0 && (livingEntity.getPersistentData().getDouble("luck") >= 1 || beneficialEffectCount >= 1)) {
                                depriveLuck(target, livingEntity);
                                tag.putInt("luckDeprivation", 100);
                                if (livingEntity instanceof Player player) {
                                    player.sendSystemMessage(Component.literal("You deprived " + target.getName().getString() + " off all their luck and beneficial effects."));
                                }
                            } if (livingEntity.getPersistentData().getInt("luckDenial") == 0) {
                                luckDenial(target);
                                tag.putInt("luckDenial", 200);
                                if (livingEntity instanceof Player player) {
                                    player.sendSystemMessage(Component.literal("You denied " + target.getName().getString() + " from receiving luck for" + + (int) ((damage * 27) / 20) + " seconds " + "and receiving beneficial effects for " + + (int) ((damage * 27) / 4) + " seconds").withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.BOLD));
                                }
                            } if (livingEntity.getPersistentData().getInt("misfortuneRedirection") == 0) {
                                misfortuneRedirection(target, livingEntity);
                                tag.putInt("misfortuneRedirection", 200);
                                if (livingEntity instanceof Player player) {
                                    player.sendSystemMessage(Component.literal(""));
                                }
                            }
                        }
                    }
                }
            }
        }
        if (tag.getInt("luckDeprivation") >= 1) {
            tag.putInt("luckDeprivation", tag.getInt("luckDeprivation") - 1);
        }
        if (tag.getInt("luckDenial") >= 1) {
            tag.putInt("luckDenial", tag.getInt("luckDenial") - 1);
        }
        if (tag.getInt("misfortuneRedirection") >= 1) {
            tag.putInt("misfortuneRedirection", tag.getInt("misfortuneRedirection") - 1);
        }
        if (!livingEntity.level().isClientSide() && livingEntity.tickCount % 20 == 0) {
            Level level = livingEntity.level();
            int enhancement = 1;
            if (level instanceof ServerLevel serverLevel) {
                enhancement = CalamityEnhancementData.getInstance(serverLevel).getCalamityEnhancement();
            }
            if (livingEntity.getPersistentData().getInt("misfortuneRedirectionLightning") >= 1) {
                LightningEntity lightning = new LightningEntity(EntityInit.LIGHTNING_ENTITY.get(), livingEntity.level());
                lightning.setSpeed(5.0f);
                lightning.setTargetEntity(livingEntity);
                lightning.setMaxLength(120);
                lightning.setDamage(10 + (enhancement * 2));
                lightning.setNewStartPos(new Vec3(livingEntity.getX(), livingEntity.getY() + 80, livingEntity.getZ()));
                lightning.setDeltaMovement(0, -3, 0);
                lightning.setNoUp(true);
                for (int i = 0; i < enhancement; i++) {
                    livingEntity.level().addFreshEntity(lightning);
                }
                livingEntity.getPersistentData().putInt("misfortuneRedirectionLightning", livingEntity.getPersistentData().getInt("misfortuneRedirectionLightning") - 1);
            }
            if (livingEntity.getPersistentData().getInt("misfortuneRedirectionMCLightning") >= 1) {
                livingEntity.getPersistentData().putInt("misfortuneRedirectionMCLightning", livingEntity.getPersistentData().getInt("misfortuneRedirectionMCLightning") - 1);
                LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, livingEntity.level());
                lightningBolt.teleportTo(lightningBolt.getX(), lightningBolt.getY(), lightningBolt.getZ());
                lightningBolt.setDamage(12);
                livingEntity.invulnerableTime = 2;
                livingEntity.hurtTime = 2;
                livingEntity.hurtDuration = 2;
                livingEntity.level().addFreshEntity(lightningBolt);
            }
        }
    }

    private static void misfortuneRedirection(LivingEntity interactionTarget, LivingEntity player) {
        if (!player.level().isClientSide() && !interactionTarget.level().isClientSide()) {
            for (LivingEntity livingEntity : interactionTarget.level().getEntitiesOfClass(LivingEntity.class, interactionTarget.getBoundingBox().inflate(BeyonderUtil.getDamage(player).get(ItemInit.MISFORTUNEREDIRECTION.get())))) {
                CompoundTag tag = livingEntity.getPersistentData();
                int enhancement = 1;
                Level level = player.level();
                if (level instanceof ServerLevel serverLevel) {
                    enhancement = CalamityEnhancementData.getInstance(serverLevel).getCalamityEnhancement();
                }
                for (MobEffectInstance effectInstance : livingEntity.getActiveEffects()) {
                    MobEffect effect = effectInstance.getEffect();
                    if (!effect.isBeneficial()) {
                        BeyonderUtil.applyMobEffect(interactionTarget, effect, effectInstance.getDuration(), effectInstance.getAmplifier(), effectInstance.isAmbient(), effectInstance.isVisible());
                        livingEntity.removeEffect(effect);
                    }
                }
                int paralysisDuration = 0;
                int lotmLightningCount = 0;
                int mcLightningCount = 0;
                int poisonDuration = 0;
                int calamityGroundTremorCounter = 0;
                int calamityGazeCounter = 0;
                int calamityBreezeCounter = 0;
                int calamityWaveCounter = 0;
                int calamityExplosionCounter = 0;
                int cantUseAbilityCount = 0;
                int doubleDamageCount = 0;
                int meteor = tag.getInt("luckMeteor");
                int lotmLightning = tag.getInt("luckLightningLOTM");
                int paralysis = tag.getInt("luckParalysis");
                int unequipArmor = tag.getInt("luckUnequipArmor");
                int wardenSpawn = tag.getInt("luckWarden");
                int mcLightning = tag.getInt("luckLightningMC");
                int poison = tag.getInt("luckPoison");
                int tornadoInt = tag.getInt("luckTornado");
                int stone = tag.getInt("luckStone");
                int doubleDamage = tag.getInt("luckDoubleDamage");
                int cantUseAbility = tag.getInt("cantUseAbility");
                int calamityMeteor = tag.getInt("calamityMeteor");
                int calamityLightningStorm = tag.getInt("calamityLightningStorm");
                int calamityLightningBolt = tag.getInt("calamityLightningBolt");
                int calamityGroundTremor = tag.getInt("calamityGroundTremor");
                int calamityGaze = tag.getInt("calamityGaze");
                int calamityUndeadArmy = tag.getInt("calamityUndeadArmy");
                int calamityBabyZombie = tag.getInt("calamityBabyZombie");
                int calamityWindArmorRemoval = tag.getInt("calamityWindArmorRemoval");
                int calamityBreeze = tag.getInt("calamityBreeze");
                int calamityWave = tag.getInt("calamityWave");
                int calamityExplosion = tag.getInt("calamityExplosion");
                int calamityTornado = tag.getInt("calamityTornado");
                if (meteor >= 1) {
                    MisfortuneManipulation.summonMeteor(interactionTarget,player);
                }
                if (lotmLightning >= 1) {
                    lotmLightningCount = lotmLightningCount + enhancement;
                    interactionTarget.getPersistentData().putInt("misfortuneRedirectionLightning", interactionTarget.getPersistentData().getInt("misfortuneRedirectionLightning") + lotmLightningCount);
                }
                if (paralysis >= 1) {
                    paralysisDuration = paralysisDuration + enhancement;
                    BeyonderUtil.applyParalysis(interactionTarget, paralysisDuration * 10);
                }
                if (unequipArmor >= 1) {
                    if (interactionTarget instanceof Player pPlayer) {
                        Random random = new Random();
                        List<EquipmentSlot> armorSlots = Arrays.asList(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
                        List<EquipmentSlot> equippedArmor = armorSlots.stream()
                                .filter(slot -> !pPlayer.getItemBySlot(slot).isEmpty())
                                .toList();
                        if (!equippedArmor.isEmpty()) {
                            EquipmentSlot randomArmorSlot = equippedArmor.get(random.nextInt(equippedArmor.size()));
                            ItemStack armorPiece = pPlayer.getItemBySlot(randomArmorSlot);
                            pPlayer.spawnAtLocation(armorPiece);
                            pPlayer.setItemSlot(randomArmorSlot, ItemStack.EMPTY);
                        }
                    }
                }
                if (wardenSpawn >= 1) {
                    for (int i = 0; i < enhancement; i++) {
                        WitherBoss witherBoss = new WitherBoss(EntityType.WITHER, interactionTarget.level());
                        witherBoss.setTarget(interactionTarget);
                        witherBoss.teleportTo(interactionTarget.getX(), interactionTarget.getY(), interactionTarget.getZ());
                        AttributeInstance maxHp = witherBoss.getAttribute(Attributes.MAX_HEALTH);
                        maxHp.setBaseValue(551);
                        witherBoss.getPersistentData().putInt("DeathTimer", 0);
                    }
                }
                if (mcLightning >= 1) {
                    mcLightningCount = mcLightningCount + enhancement;
                    interactionTarget.getPersistentData().putInt("misfortuneRedirectionMCLightning", mcLightningCount);
                }
                if (poison >= 1) {
                    poisonDuration = poisonDuration + enhancement;
                    interactionTarget.addEffect(new MobEffectInstance(MobEffects.POISON, poisonDuration * 15, 1, false, false));
                }
                if (tornadoInt >= 1) {
                    for (int i = 0; i < enhancement; i++) {
                        TornadoEntity.summonTornadoRandom(interactionTarget);
                    }
                }
                if (stone >= 1) {
                    for (int i = 0; i < enhancement; i++) {
                        StoneEntity.summonStoneRandom(interactionTarget);
                    }
                }
                if (doubleDamage >= 1) {
                    doubleDamageCount = doubleDamageCount + enhancement;
                    interactionTarget.getPersistentData().putInt("luckDoubleDamage", doubleDamage + doubleDamageCount);
                }
                if (cantUseAbility >= 1) {
                    cantUseAbilityCount = cantUseAbilityCount + enhancement;
                    interactionTarget.getPersistentData().putInt("cantUseAbility", cantUseAbility + cantUseAbilityCount);
                }
                if (calamityMeteor >= 1) {
                    for (int i = 0; i < enhancement; i++) {
                        MisfortuneManipulation.summonMeteor(interactionTarget, player);
                    }
                }
                if (calamityLightningStorm >= 1) {
                    interactionTarget.getPersistentData().putInt("sailorLightningStorm1", interactionTarget.getPersistentData().getInt("sailorLightningStorm1") + (10 * enhancement));
                    interactionTarget.getPersistentData().putInt("sailorStormVecX1", (int) interactionTarget.getX());
                    interactionTarget.getPersistentData().putInt("sailorStormVecY1", (int) interactionTarget.getY());
                    interactionTarget.getPersistentData().putInt("sailorStormVecZ1", (int) interactionTarget.getZ());
                }
                if (calamityLightningBolt >= 1) {
                    interactionTarget.getPersistentData().putInt("misfortuneRedirectionLightning", interactionTarget.getPersistentData().getInt("misfortuneRedirectionLightning") + lotmLightningCount);
                }
                if (calamityGroundTremor >= 1) {
                    calamityGroundTremorCounter++;
                    for (LivingEntity living : interactionTarget.level().getEntitiesOfClass(LivingEntity.class, interactionTarget.getBoundingBox().inflate(calamityGroundTremorCounter * 5))) {
                        if (livingEntity != player) {
                            living.hurt(BeyonderUtil.genericSource(interactionTarget, living), 5 * enhancement);
                        }
                    }
                }
                if (calamityGaze >= 1) {
                    calamityGazeCounter++;
                    for (LivingEntity living : interactionTarget.level().getEntitiesOfClass(LivingEntity.class, interactionTarget.getBoundingBox().inflate(calamityGazeCounter * 5))) {
                        if (living != player) {
                            double corruption = living.getPersistentData().getDouble("corruption");
                            living.getPersistentData().putDouble("corruption", living.getPersistentData().getDouble("corruption") + (4 * enhancement));
                        }
                    }
                }
                if (calamityUndeadArmy >= 1) {
                    undeadArmy(interactionTarget);
                }
                if (calamityBabyZombie >= 1) {
                    Zombie zombie = new Zombie(EntityType.ZOMBIE, player.level());
                    ItemStack netheriteHelmet = new ItemStack(Items.NETHERITE_HELMET);
                    ItemStack netheriteChestplate = new ItemStack(Items.NETHERITE_BOOTS);
                    ItemStack netheriteLeggings = new ItemStack(Items.NETHERITE_LEGGINGS);
                    ItemStack netheriteBoots = new ItemStack(Items.NETHERITE_BOOTS);
                    ItemStack netheriteSword = new ItemStack(Items.NETHERITE_SWORD);
                    zombie.setDropChance(EquipmentSlot.MAINHAND, 0);
                    zombie.setDropChance(EquipmentSlot.CHEST, 0);
                    zombie.setDropChance(EquipmentSlot.LEGS, 0);
                    zombie.setDropChance(EquipmentSlot.FEET, 0);
                    zombie.setDropChance(EquipmentSlot.HEAD, 0);
                    netheriteHelmet.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 5);
                    netheriteChestplate.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 5);
                    netheriteLeggings.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 5);
                    netheriteBoots.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 5);
                    netheriteSword.enchant(Enchantments.FIRE_ASPECT, 2);
                    netheriteSword.enchant(Enchantments.SHARPNESS, 2);
                    zombie.setItemSlot(EquipmentSlot.HEAD, netheriteHelmet);
                    zombie.setItemSlot(EquipmentSlot.CHEST, netheriteChestplate);
                    zombie.setItemSlot(EquipmentSlot.LEGS, netheriteLeggings);
                    zombie.setItemSlot(EquipmentSlot.FEET, netheriteBoots);
                    zombie.setItemSlot(EquipmentSlot.MAINHAND, netheriteSword);
                    zombie.setBaby(true);
                    zombie.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100000, 3, true, true));
                    zombie.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100000, 3, false, false));
                    zombie.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100000, 2, false, false));
                    zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
                    zombie.teleportTo(interactionTarget.getX(), interactionTarget.getY(), interactionTarget.getZ());
                    zombie.setTarget(interactionTarget);
                    for (int i = 0; i < enhancement; i++) {
                        interactionTarget.level().addFreshEntity(zombie);
                    }
                }
                if (calamityWindArmorRemoval >= 1) {
                    if (interactionTarget instanceof Player pPlayer) {
                        Random random = new Random();
                        List<EquipmentSlot> armorSlots = Arrays.asList(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
                        List<EquipmentSlot> equippedArmor = armorSlots.stream()
                                .filter(slot -> !pPlayer.getItemBySlot(slot).isEmpty())
                                .toList();
                        if (!equippedArmor.isEmpty()) {
                            EquipmentSlot randomArmorSlot = equippedArmor.get(random.nextInt(equippedArmor.size()));
                            ItemStack armorPiece = pPlayer.getItemBySlot(randomArmorSlot);
                            pPlayer.spawnAtLocation(armorPiece);
                            pPlayer.setItemSlot(randomArmorSlot, ItemStack.EMPTY);
                        }
                    }
                }
                if (calamityBreeze >= 1) {
                    calamityBreezeCounter++;
                    for (LivingEntity living : interactionTarget.level().getEntitiesOfClass(LivingEntity.class, interactionTarget.getBoundingBox().inflate((calamityBreezeCounter * 5) + (enhancement * 5)))) {
                        if (livingEntity != player) {
                            BeyonderUtil.applyStun(living, calamityBreezeCounter * 10);
                            living.hurt(BeyonderUtil.genericSource(interactionTarget, living), 4);
                            living.setTicksFrozen(calamityBreezeCounter * 10);
                        }
                    }
                }
                if (calamityWave >= 1) {
                    calamityWaveCounter++;
                    for (LivingEntity living : interactionTarget.level().getEntitiesOfClass(LivingEntity.class, interactionTarget.getBoundingBox().inflate((calamityBreezeCounter * 5) + (enhancement * 5)))) {
                        if (livingEntity != player) {
                            living.setSecondsOnFire(calamityWaveCounter * 2);
                            living.hurt(BeyonderUtil.lavaSource(player, living), 5 * enhancement);
                        }
                    }
                }
                if (calamityExplosion >= 1) {
                    calamityExplosionCounter++;
                    BlockPos hitPos = interactionTarget.getOnPos();
                    float radius = calamityExplosionCounter * 4 + (enhancement);
                    interactionTarget.level().playSound(null, interactionTarget.getOnPos(), SoundEvents.GENERIC_EXPLODE, SoundSource.AMBIENT, 30.0f, 1.0f);
                    for (BlockPos pos : BlockPos.betweenClosed(
                            hitPos.offset((int) -radius, (int) -radius, (int) -radius),
                            hitPos.offset((int) radius, (int) radius, (int) radius))) {
                        if (pos.distSqr(hitPos) <= radius * radius) {
                            if (interactionTarget.level().getBlockState(pos).getDestroySpeed(interactionTarget.level(), pos) >= 0) {
                                interactionTarget.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                            }
                        }
                    }
                    List<Entity> entities = interactionTarget.level().getEntities(interactionTarget,
                            new AABB(hitPos.offset((int) -radius, (int) -radius, (int) -radius),
                                    hitPos.offset((int) radius, (int) radius, (int) radius)));
                    for (Entity entity : entities) {
                        if (entity instanceof LivingEntity livingEntity1) {
                            livingEntity1.hurt(BeyonderUtil.genericSource(interactionTarget, livingEntity1), 4 * radius); // problem w/ damage sources
                        }
                    }
                }

                if (calamityTornado >= 1) {
                    for (int i = 0; i < enhancement; i++) {
                        TornadoEntity.summonTornadoRandom(interactionTarget);
                    }
                }
            }
        }
    }

    public static void undeadArmy(LivingEntity livingEntity) {
        int x = (int) (livingEntity.getX() + (Math.random() * 40) - 20);
        int z = (int) (livingEntity.getX() + (Math.random() * 40) - 20);
        int surfaceY = livingEntity.level().getHeight(Heightmap.Types.WORLD_SURFACE, x, z) + 1;
        Random random = new Random();
        Level level = livingEntity.level();
        int enhancement = 1;
        if (level instanceof ServerLevel serverLevel) {
            enhancement = CalamityEnhancementData.getInstance(serverLevel).getCalamityEnhancement();
        }        ItemStack leatherHelmet = new ItemStack(Items.LEATHER_HELMET);
        ItemStack leatherChestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
        ItemStack leatherLeggings = new ItemStack(Items.LEATHER_LEGGINGS);
        ItemStack leatherBoots = new ItemStack(Items.LEATHER_BOOTS);
        ItemStack ironHelmet = new ItemStack(Items.IRON_HELMET);
        ItemStack ironChestplate = new ItemStack(Items.IRON_CHESTPLATE);
        ItemStack ironLeggings = new ItemStack(Items.IRON_LEGGINGS);
        ItemStack ironBoots = new ItemStack(Items.IRON_BOOTS);
        ItemStack diamondHelmet = new ItemStack(Items.DIAMOND_HELMET);
        ItemStack diamondChestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
        ItemStack diamondLeggings = new ItemStack(Items.DIAMOND_LEGGINGS);
        ItemStack diamondBoots = new ItemStack(Items.DIAMOND_BOOTS);
        ItemStack netheriteHelmet = new ItemStack(Items.NETHERITE_HELMET);
        ItemStack netheriteChestplate = new ItemStack(Items.NETHERITE_CHESTPLATE);
        ItemStack netheriteLeggings = new ItemStack(Items.NETHERITE_LEGGINGS);
        ItemStack netheriteBoots = new ItemStack(Items.NETHERITE_BOOTS);
        ItemStack enchantedBow = new ItemStack(Items.BOW);
        ItemStack woodSword = new ItemStack(Items.WOODEN_SWORD);
        ItemStack ironSword = new ItemStack(Items.IRON_SWORD);
        ItemStack diamondSword = new ItemStack(Items.DIAMOND_SWORD);
        ItemStack netheriteSword = new ItemStack(Items.NETHERITE_SWORD);
        Zombie zombie = new Zombie(EntityType.ZOMBIE, livingEntity.level());
        Skeleton skeleton = new Skeleton(EntityType.SKELETON, livingEntity.level());
        for (int i = 0; i < enhancement; i++) {
            int randomPos = (int) ((Math.random() * 24) - 12);
            if (random.nextInt(10) == 10) {
                zombie.setTarget(livingEntity);
                livingEntity.level().addFreshEntity(zombie);
            }
            if (random.nextInt(10) == 9) {
                zombie.setPos(x + randomPos, surfaceY, z + randomPos);
                zombie.setItemSlot(EquipmentSlot.HEAD, leatherHelmet);
                zombie.setItemSlot(EquipmentSlot.CHEST, leatherChestplate);
                zombie.setItemSlot(EquipmentSlot.LEGS, leatherLeggings);
                zombie.setItemSlot(EquipmentSlot.FEET, leatherBoots);
                zombie.setItemSlot(EquipmentSlot.MAINHAND, woodSword);
                zombie.setTarget(livingEntity);
                livingEntity.level().addFreshEntity(zombie);
            }
            if (random.nextInt(10) == 8) {
                zombie.setPos(x + randomPos, surfaceY, z + randomPos);
                zombie.setItemSlot(EquipmentSlot.HEAD, ironHelmet);
                zombie.setItemSlot(EquipmentSlot.CHEST, ironChestplate);
                zombie.setItemSlot(EquipmentSlot.LEGS, ironLeggings);
                zombie.setItemSlot(EquipmentSlot.FEET, ironBoots);
                zombie.setItemSlot(EquipmentSlot.MAINHAND, ironSword);
                zombie.setTarget(livingEntity);
                livingEntity.level().addFreshEntity(zombie);
            }
            if (random.nextInt(10) == 7) {
                zombie.setPos(x + randomPos, surfaceY, z + randomPos);
                zombie.setItemSlot(EquipmentSlot.HEAD, diamondHelmet);
                zombie.setItemSlot(EquipmentSlot.CHEST, diamondChestplate);
                zombie.setItemSlot(EquipmentSlot.LEGS, diamondLeggings);
                zombie.setItemSlot(EquipmentSlot.FEET, diamondBoots);
                zombie.setItemSlot(EquipmentSlot.MAINHAND, diamondSword);
                zombie.setTarget(livingEntity);
                livingEntity.level().addFreshEntity(zombie);
            }
            if (random.nextInt(10) == 6) {
                zombie.setPos(x + randomPos, surfaceY, z + randomPos);
                zombie.setItemSlot(EquipmentSlot.HEAD, netheriteHelmet);
                zombie.setItemSlot(EquipmentSlot.CHEST, netheriteChestplate);
                zombie.setItemSlot(EquipmentSlot.LEGS, netheriteLeggings);
                zombie.setItemSlot(EquipmentSlot.FEET, netheriteBoots);
                zombie.setItemSlot(EquipmentSlot.MAINHAND, netheriteSword);
                zombie.setTarget(livingEntity);
                livingEntity.level().addFreshEntity(zombie);
            }
            if (random.nextInt(20) == 5) {
                skeleton.setPos(x + randomPos, surfaceY, z + randomPos);
                skeleton.setItemSlot(EquipmentSlot.MAINHAND, enchantedBow);
                zombie.setTarget(livingEntity);
                livingEntity.level().addFreshEntity(skeleton);
            }
            if (random.nextInt(20) == 4) {
                skeleton.setPos(x + randomPos, surfaceY, z + randomPos);
                skeleton.setItemSlot(EquipmentSlot.HEAD, leatherHelmet);
                skeleton.setItemSlot(EquipmentSlot.CHEST, leatherChestplate);
                skeleton.setItemSlot(EquipmentSlot.LEGS, leatherLeggings);
                skeleton.setItemSlot(EquipmentSlot.FEET, leatherBoots);
                enchantedBow.enchant(Enchantments.POWER_ARROWS, 1);
                skeleton.setItemSlot(EquipmentSlot.MAINHAND, enchantedBow);
                zombie.setTarget(livingEntity);
                livingEntity.level().addFreshEntity(skeleton);
            }
            if (random.nextInt(20) == 3) {
                skeleton.setPos(x + randomPos, surfaceY, z + randomPos);
                skeleton.setItemSlot(EquipmentSlot.HEAD, ironHelmet);
                skeleton.setItemSlot(EquipmentSlot.CHEST, ironChestplate);
                skeleton.setItemSlot(EquipmentSlot.LEGS, ironLeggings);
                skeleton.setItemSlot(EquipmentSlot.FEET, ironBoots);
                enchantedBow.enchant(Enchantments.POWER_ARROWS, 2);
                skeleton.setItemSlot(EquipmentSlot.MAINHAND, enchantedBow);
                zombie.setTarget(livingEntity);
                livingEntity.level().addFreshEntity(skeleton);
            }
            if (random.nextInt(20) == 2) {
                skeleton.setPos(x + randomPos, surfaceY, z + randomPos);
                skeleton.setItemSlot(EquipmentSlot.HEAD, diamondHelmet);
                skeleton.setItemSlot(EquipmentSlot.CHEST, diamondChestplate);
                skeleton.setItemSlot(EquipmentSlot.LEGS, diamondLeggings);
                skeleton.setItemSlot(EquipmentSlot.FEET, diamondBoots);
                enchantedBow.enchant(Enchantments.POWER_ARROWS, 3);
                skeleton.setItemSlot(EquipmentSlot.MAINHAND, enchantedBow);
                zombie.setTarget(livingEntity);
                livingEntity.level().addFreshEntity(skeleton);
            }
            if (random.nextInt(20) == 1) {
                skeleton.setPos(x + randomPos, surfaceY, z + randomPos);
                skeleton.setItemSlot(EquipmentSlot.HEAD, netheriteHelmet);
                skeleton.setItemSlot(EquipmentSlot.CHEST, netheriteChestplate);
                skeleton.setItemSlot(EquipmentSlot.LEGS, netheriteLeggings);
                skeleton.setItemSlot(EquipmentSlot.FEET, netheriteBoots);
                enchantedBow.enchant(Enchantments.POWER_ARROWS, 4);
                skeleton.setItemSlot(EquipmentSlot.MAINHAND, enchantedBow);
                zombie.setTarget(livingEntity);
                livingEntity.level().addFreshEntity(skeleton);
            }
            zombie.setDropChance(EquipmentSlot.HEAD, 0.0F);
            zombie.setDropChance(EquipmentSlot.CHEST, 0.0F);
            zombie.setDropChance(EquipmentSlot.LEGS, 0.0F);
            zombie.setDropChance(EquipmentSlot.FEET, 0.0F);
            skeleton.setDropChance(EquipmentSlot.HEAD, 0.0F);
            skeleton.setDropChance(EquipmentSlot.CHEST, 0.0F);
            skeleton.setDropChance(EquipmentSlot.LEGS, 0.0F);
            skeleton.setDropChance(EquipmentSlot.FEET, 0.0F);
        }
    }

    public static void depriveLuck(LivingEntity interactionTarget, LivingEntity player) {
        if (!player.level().isClientSide() && !interactionTarget.level().isClientSide()) {
            CompoundTag tag = player.getPersistentData();
            CompoundTag pTag = interactionTarget.getPersistentData();
            double luck = tag.getDouble("luck");
            double pLuck = pTag.getDouble("luck");
            tag.putDouble("luck", luck + pLuck);
            pTag.putDouble("luck", 0);
            for (MobEffectInstance effectInstance : interactionTarget.getActiveEffects()) {
                MobEffect effect = effectInstance.getEffect();
                if (effect.isBeneficial()) {
                    BeyonderUtil.applyMobEffect(player, effect, effectInstance.getDuration(), effectInstance.getAmplifier(), effectInstance.isAmbient(), effectInstance.isVisible());
                    interactionTarget.removeEffect(effect);
                }
            }
        }
    }

    public static void luckDenial(LivingEntity livingEntity) {
        CompoundTag tag = livingEntity.getPersistentData();
        double luck = tag.getDouble("luck");
        double misfortune = tag.getDouble("misfortune");
        double luckDenialTimer = tag.getDouble("luckDenialTimer");
        double luckDenialLuck = tag.getDouble("luckDenialLuck");
        double luckDenialMisfortune = tag.getDouble("luckDenialMisfortune");
        if (luckDenialTimer >= 1) {
            tag.putDouble("luckDenialTimer", luckDenialTimer - 1);
            if (luck >= luckDenialLuck) {
                tag.putDouble("luck", luckDenialLuck);
            } else if (luck < luckDenialLuck) {
                tag.putDouble("luckDenialLuck", luck);
            }
            if (misfortune <= luckDenialMisfortune) {
                tag.putDouble("misfortune", luckDenialMisfortune);
            } else if (misfortune > luckDenialMisfortune) {
                tag.putDouble("luckDenialMisfortune", misfortune);
            }
        }
    }

    private static void denyLuck(LivingEntity interactionTarget, LivingEntity player) {
        if (!player.level().isClientSide() && !interactionTarget.level().isClientSide()) {
            CompoundTag tag = interactionTarget.getPersistentData();
            double luck = tag.getDouble("luck");
            double misfortune = tag.getDouble("misfortune");
            double damage = BeyonderUtil.getDamage(player).get(ItemInit.MONSTERREBOOT.get());
            if (BeyonderUtil.getSequence(player) <= 2) {
                tag.putDouble("luckDenialTimer", damage * 27);
                tag.putDouble("luckDenialLuck", luck);
                tag.putDouble("luckDenialMisfortune", misfortune);
            } else {
                tag.putDouble("luckDenialTimer", damage * 27);
                tag.putDouble("luckDenialLuck", luck);
            }
            BeyonderUtil.applyBeneficialEffectBlocker(interactionTarget, (int) damage / 5);
        }
    }


    public static void dodgeProjectiles(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            if (livingEntity.tickCount % 3 == 0) {
                if (livingEntity.getPersistentData().getInt("windMovingProjectilesCounter") >= 1) {
                    for (Projectile projectile : livingEntity.level().getEntitiesOfClass(Projectile.class, livingEntity.getBoundingBox().inflate(100))) {
                        if (projectile.getPersistentData().getInt("windDodgeProjectilesCounter") == 0) {
                            if (projectile instanceof Arrow arrow && arrow.tickCount >= 100) {
                                return;
                            }
                            float scale = ScaleTypes.BASE.getScaleData(projectile).getScale();
                            double maxDistance = 6 * scale;
                            double deltaX = Math.abs(projectile.getX() - livingEntity.getX());
                            double deltaY = Math.abs(projectile.getY() - livingEntity.getY());
                            double deltaZ = Math.abs(projectile.getZ() - livingEntity.getZ());
                            if ((deltaX <= maxDistance && deltaY <= maxDistance && deltaZ <= maxDistance) && projectile.getOwner() != livingEntity) {
                                double mathRandom = (Math.random() + .4) - 0.2;
                                double x = projectile.getDeltaMovement().x() + (mathRandom * scale);
                                double y = projectile.getDeltaMovement().y() + (mathRandom * scale);
                                double z = projectile.getDeltaMovement().z() + (mathRandom * scale);
                                projectile.setDeltaMovement(x, y, z);
                                projectile.hurtMarked = true;
                                projectile.getPersistentData().putInt("windDodgeProjectilesCounter", 100);
                                livingEntity.getPersistentData().putInt("windMovingProjectilesCounter", livingEntity.getPersistentData().getInt("windMovingProjectilesCounter") - 1);
                                if (livingEntity instanceof Player player) {
                                    player.displayClientMessage(Component.literal("A gust of wind moved a projectile headed towards you").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GREEN), true);
                                }
                            }
                        } else {
                            projectile.getPersistentData().putInt("windDodgeProjectilesCounter", projectile.getPersistentData().getInt("windDodgeProjectilesCounter") - 1);
                        }
                    }
                } else {
                    if (BeyonderUtil.isBeyonderCapable(livingEntity)) {
                        if (livingEntity instanceof Player pPlayer) {
                            BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(pPlayer);
                            int sequence = holder.getSequence();
                            if (BeyonderUtil.currentPathwayMatchesNoException(livingEntity, BeyonderClassInit.MONSTER.get()) && holder.getSequence() <= 7) {
                                int reverseChance = (int) (Math.random() * 20 - sequence);
                                for (Projectile projectile : livingEntity.level().getEntitiesOfClass(Projectile.class, livingEntity.getBoundingBox().inflate(100))) {
                                    if (projectile.getPersistentData().getInt("monsterReverseProjectiles") == 0) {
                                        if (projectile instanceof Arrow arrow && arrow.tickCount >= 80) {
                                            return;
                                        }
                                        if (reverseChance >= 10) {
                                            float scale = ScaleTypes.BASE.getScaleData(projectile).getScale();
                                            double maxDistance = 6 * scale;
                                            double deltaX = Math.abs(projectile.getX() - livingEntity.getX());
                                            double deltaY = Math.abs(projectile.getY() - livingEntity.getY());
                                            double deltaZ = Math.abs(projectile.getZ() - livingEntity.getZ());
                                            if ((deltaX <= maxDistance && deltaY <= maxDistance && deltaZ <= maxDistance) && projectile.getOwner() != livingEntity) {
                                                double x = projectile.getDeltaMovement().x() * -1;
                                                double y = projectile.getDeltaMovement().y() * -1;
                                                double z = projectile.getDeltaMovement().z() * -1;
                                                projectile.setDeltaMovement(x, y, z);
                                                projectile.hurtMarked = true;
                                                if (livingEntity instanceof Player player) {
                                                    player.displayClientMessage(Component.literal("A strong breeze luckily reversed a projectile headed towards you").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GREEN), true);
                                                }
                                            }
                                        }
                                        projectile.getPersistentData().putInt("monsterReverseProjectiles", 60);
                                    } else {
                                        projectile.getPersistentData().putInt("monsterReverseProjectiles", projectile.getPersistentData().getInt("windDodgeProjectilesCounter") - 1);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void showMonsterParticles(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide() && livingEntity.tickCount % 100 == 0) {
            if (livingEntity instanceof ServerPlayer serverPlayer) {
                BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(serverPlayer);
                if (holder.getSequence() <= 2 && BeyonderUtil.currentPathwayMatches(livingEntity, BeyonderClassInit.MONSTER.get())) {
                    for (LivingEntity entities : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate(50))) {
                        if (entities != serverPlayer) {
                            CompoundTag tag = entities.getPersistentData();
                            int cantUseAbility = tag.getInt("cantUseAbility");
                            int meteor = tag.getInt("luckMeteor");
                            int lotmLightning = tag.getInt("luckLightningLOTM");
                            int paralysis = tag.getInt("luckParalysis");
                            int unequipArmor = tag.getInt("luckUnequipArmor");
                            int wardenSpawn = tag.getInt("luckWarden");
                            int mcLightning = tag.getInt("luckLightningMC");
                            int poison = tag.getInt("luckPoison");
                            int tornadoInt = tag.getInt("luckTornado");
                            int stone = tag.getInt("luckStone");
                            int doubleDamage = tag.getInt("luckDoubleDamage");
                            int calamityMeteor = tag.getInt("calamityMeteor");
                            int calamityLightningStorm = tag.getInt("calamityLightningStorm");
                            int calamityLightningBolt = tag.getInt("calamityLightningBolt");
                            int calamityGroundTremor = tag.getInt("calamityGroundTremor");
                            int calamityGaze = tag.getInt("calamityGaze");
                            int calamityUndeadArmy = tag.getInt("calamityUndeadArmy");
                            int calamityBabyZombie = tag.getInt("calamityBabyZombie");
                            int calamityWindArmorRemoval = tag.getInt("calamityWindArmorRemoval");
                            int calamityBreeze = tag.getInt("calamityBreeze");
                            int calamityWave = tag.getInt("calamityWave");
                            int calamityExplosion = tag.getInt("calamityExplosion");
                            int calamityTornado = tag.getInt("calamityTornado");
                            int ignoreDamage = tag.getInt("luckIgnoreDamage");
                            int diamonds = tag.getInt("luckDiamonds");
                            int regeneration = tag.getInt("luckRegeneration");
                            int moveProjectiles = tag.getInt("windMovingProjectilesCounter");
                            int halveDamage = tag.getInt("luckHalveDamage");
                            int ignoreMobs = tag.getInt("luckIgnoreMobs");
                            int luckAttackerPoisoned = tag.getInt("luckAttackerPoisoned");
                            if (cantUseAbility >= 1) {
                                for (int i = 0; i < cantUseAbility; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.CANT_USE_ABILITY_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (meteor >= 1) {
                                int particleCount = Math.max(1, (int) 20 - (meteor / 2));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.METEOR_CALAMITY_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (lotmLightning >= 1) {
                                int particleCount = Math.max(1, (int) 15 - (lotmLightning));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.LOTM_LIGHTNING_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (paralysis >= 1) {
                                int particleCount = Math.max(1, (int) 15 - (paralysis));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.TRIP_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (unequipArmor >= 1) {
                                int particleCount = (int) Math.max(1, (int) 20 - (unequipArmor));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.WIND_UNEQUIP_ARMOR_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (wardenSpawn >= 1) {
                                int particleCount = (int) Math.max(1, (int) 20 - (wardenSpawn / 1.5));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.WARDEN_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (mcLightning >= 1) {
                                for (int i = 0; i < mcLightning; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.MC_LIGHTNING_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (poison >= 1) {
                                int particleCount = (int) Math.max(1, (int) 20 - (poison / 0.75));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.POISON_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (tornadoInt >= 1) {
                                int particleCount = (int) Math.max(1, (int) 20 - (tornadoInt * 0.75));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.TORNADO_CALAMITY_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (stone >= 1) {
                                int particleCount = (int) Math.max(1, (int) 20 - (tornadoInt / 0.5));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.FALLING_STONE_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (doubleDamage >= 1) {
                                for (int i = 0; i < doubleDamage; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.DOUBLE_DAMAGE_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (calamityMeteor >= 1) {
                                int particleCount = (int) Math.max(1, (int) 20 - (calamityMeteor / 3.5));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.METEOR_CALAMITY_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (calamityLightningStorm >= 1) {
                                int particleCount = (int) Math.max(1, (int) 20 - (calamityLightningStorm / 2.5));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.LIGHTNING_STORM_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (calamityLightningBolt >= 1) {
                                int particleCount = Math.max(1, (int) 20 - (calamityLightningBolt * 2));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.LOTM_LIGHTNING_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (calamityGroundTremor >= 1) {
                                int particleCount = Math.max(1, (int) 20 - (calamityGroundTremor / 2));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.GROUND_TREMOR_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (calamityGaze >= 1) {
                                int particleCount = (int) Math.max(1, (int) 20 - (calamityGaze / 2.5));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.GOO_GAZE_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (calamityUndeadArmy >= 1) {
                                int particleCount = Math.max(1, (int) 20 - (calamityUndeadArmy));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.UNDEAD_ARMY_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (calamityBabyZombie >= 1) {
                                int particleCount = Math.max(1, (int) 20 - (calamityBabyZombie));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.BABY_ZOMBIE_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (calamityWindArmorRemoval >= 1) {
                                int particleCount = Math.max(1, (int) 20 - (calamityWindArmorRemoval / 2));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.WIND_UNEQUIP_ARMOR_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (calamityBreeze >= 1) {
                                int particleCount = (int) Math.max(1, (int) 20 - (calamityBreeze / 1.25));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.BREEZE_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (calamityWave >= 1) {
                                int particleCount = (int) Math.max(1, (int) 20 - (calamityWave / 1.25));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.HEAT_WAVE_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (calamityExplosion >= 1) {
                                int particleCount = Math.max(1, (int) 20 - (calamityExplosion / 3));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.EXPLOSION_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (calamityTornado >= 1) {
                                int particleCount = (int) Math.max(1, (int) 20 - (calamityTornado / 3.5));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.TORNADO_CALAMITY_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (ignoreDamage >= 1) {
                                for (int i = 0; i < ignoreDamage; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.IGNORE_DAMAGE_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (diamonds >= 1) {
                                for (int i = 0; i < diamonds; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.DIAMOND_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (regeneration >= 1) {
                                for (int i = 0; i < regeneration; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.REGENERATION_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (moveProjectiles >= 1) {
                                for (int i = 0; i < moveProjectiles; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.WIND_MOVE_PROJECTILES_PARTICLES.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (halveDamage >= 1) {
                                for (int i = 0; i < halveDamage; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.HALF_DAMAGE_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (ignoreMobs >= 1) {
                                for (int i = 0; i < ignoreMobs; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.IGNORE_MOBS_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (luckAttackerPoisoned >= 1) {
                                for (int i = 0; i < luckAttackerPoisoned; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.ATTACKER_POISONED_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void checkForProjectiles(Player player) {
        Level level = player.level();
        for (Projectile projectile : level.getEntitiesOfClass(Projectile.class, player.getBoundingBox().inflate(100))) {
            boolean bothInSameDimension = projectile.getPersistentData().getBoolean("inSpiritWorld") == player.getPersistentData().getBoolean("inSpiritWorld");
            //if (bothInSameDimension) {
            List<Vec3> trajectory = predictProjectileTrajectory(projectile, player);
            float scale = ScaleTypes.BASE.getScaleData(projectile).getScale();
            double maxDistance = 20 * scale;
            double deltaX = Math.abs(projectile.getX() - player.getX());
            double deltaY = Math.abs(projectile.getY() - player.getY());
            double deltaZ = Math.abs(projectile.getZ() - player.getZ());
            if (deltaX <= maxDistance || deltaY <= maxDistance || deltaZ <= maxDistance) {
                if (player.level() instanceof ServerLevel serverLevel) {
                    drawParticleLine(serverLevel, (ServerPlayer) player, trajectory);
                }
            }
        }
        //}
    }

    public static void drawParticleLine(ServerLevel level, ServerPlayer player, List<Vec3> points) {
        int particleInterval = 2; // Only spawn a particle every 5 points
        for (int i = 0; i < points.size() - 1; i += particleInterval) {
            Vec3 start = points.get(i);
            Vec3 end = i + particleInterval < points.size() ? points.get(i + particleInterval) : points.get(points.size() - 1);
            Vec3 direction = end.subtract(start).normalize();
            double distance = start.distanceTo(end);
            Vec3 particlePosition = start.add(direction.scale(distance / 2));
            LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(DustParticleOptions.REDSTONE, particlePosition.x, particlePosition.y, particlePosition.z, 0, 0, 0), player);
        }
    }

    public static List<Vec3> predictProjectileTrajectory(Projectile projectile, LivingEntity player) {
        List<Vec3> trajectory = new ArrayList<>();
        Vec3 projectilePos = projectile.position();
        Vec3 projectileDelta = projectile.getDeltaMovement();
        Level level = projectile.level();
        boolean isArrow = projectile instanceof AbstractArrow;
        trajectory.add(projectilePos);
        int maxIterations = 1000;
        double maxDistance = 100.0;

        for (int i = 0; i < maxIterations; i++) {
            projectilePos = projectilePos.add(projectileDelta);
            trajectory.add(projectilePos);
            if (projectilePos.distanceTo(projectile.position()) > maxDistance) {
                break;
            }
            if (isArrow && projectile instanceof AbstractArrow arrow && arrow.tickCount <= 100) {
                projectileDelta = projectileDelta.scale(0.99F);
                projectileDelta = projectileDelta.add(0, -0.05, 0);
            } else {
                projectileDelta = projectileDelta.scale(0.99F);
                projectileDelta = projectileDelta.add(0, -0.03, 0);
            }
        }

        return trajectory;
    }

    public static void calamityUndeadArmy(LivingEntity pPlayer) {
        CompoundTag tag = pPlayer.getPersistentData();
        if (pPlayer.level() instanceof ServerLevel serverLevel) {
            int enhancement = CalamityEnhancementData.getInstance(serverLevel).getCalamityEnhancement();
            int x = tag.getInt("calamityUndeadArmyX");
            int y = tag.getInt("calamityUndeadArmyY");
            int z = tag.getInt("calamityUndeadArmyZ");
            int subtractX = (int) (x - pPlayer.getX());
            int subtractY = (int) (y - pPlayer.getY());
            int subtractZ = (int) (z - pPlayer.getZ());
            int surfaceY = pPlayer.level().getHeight(Heightmap.Types.WORLD_SURFACE, x, z) + 1;
            int undeadArmyCounter = tag.getInt("calamityUndeadArmyCounter");
            if (undeadArmyCounter >= 1) {
                for (int i = 0; i < enhancement; i++) {
                    Random random = new Random();
                    ItemStack leatherHelmet = new ItemStack(Items.LEATHER_HELMET);
                    ItemStack leatherChestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
                    ItemStack leatherLeggings = new ItemStack(Items.LEATHER_LEGGINGS);
                    ItemStack leatherBoots = new ItemStack(Items.LEATHER_BOOTS);
                    ItemStack ironHelmet = new ItemStack(Items.IRON_HELMET);
                    ItemStack ironChestplate = new ItemStack(Items.IRON_CHESTPLATE);
                    ItemStack ironLeggings = new ItemStack(Items.IRON_LEGGINGS);
                    ItemStack ironBoots = new ItemStack(Items.IRON_BOOTS);
                    ItemStack diamondHelmet = new ItemStack(Items.DIAMOND_HELMET);
                    ItemStack diamondChestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
                    ItemStack diamondLeggings = new ItemStack(Items.DIAMOND_LEGGINGS);
                    ItemStack diamondBoots = new ItemStack(Items.DIAMOND_BOOTS);
                    ItemStack netheriteHelmet = new ItemStack(Items.NETHERITE_HELMET);
                    ItemStack netheriteChestplate = new ItemStack(Items.NETHERITE_CHESTPLATE);
                    ItemStack netheriteLeggings = new ItemStack(Items.NETHERITE_LEGGINGS);
                    ItemStack netheriteBoots = new ItemStack(Items.NETHERITE_BOOTS);
                    ItemStack enchantedBow = new ItemStack(Items.BOW);
                    ItemStack woodSword = new ItemStack(Items.WOODEN_SWORD);
                    ItemStack ironSword = new ItemStack(Items.IRON_SWORD);
                    ItemStack diamondSword = new ItemStack(Items.DIAMOND_SWORD);
                    ItemStack netheriteSword = new ItemStack(Items.NETHERITE_SWORD);
                    Zombie zombie = new Zombie(EntityType.ZOMBIE, pPlayer.level());
                    Skeleton skeleton = new Skeleton(EntityType.SKELETON, pPlayer.level());
                    int randomPos = (int) ((Math.random() * 24) - 12);
                    if (random.nextInt(10) == 10) {
                        for (LivingEntity entity : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().move(subtractX, subtractY, subtractZ).inflate(20))) {
                            if (BeyonderUtil.currentPathwayMatchesNoException(pPlayer, BeyonderClassInit.MONSTER.get()) && BeyonderUtil.getSequence(pPlayer) <= 6) {
                                if (entity != null) {
                                    zombie.setTarget(entity);
                                }
                            }
                        }
                        pPlayer.level().addFreshEntity(zombie);
                    }
                    if (random.nextInt(10) == 9) {
                        zombie.setPos(x + randomPos, surfaceY, z + randomPos);
                        zombie.setItemSlot(EquipmentSlot.HEAD, leatherHelmet);
                        zombie.setItemSlot(EquipmentSlot.CHEST, leatherChestplate);
                        zombie.setItemSlot(EquipmentSlot.LEGS, leatherLeggings);
                        zombie.setItemSlot(EquipmentSlot.FEET, leatherBoots);
                        zombie.setItemSlot(EquipmentSlot.MAINHAND, woodSword);
                        for (LivingEntity entity : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().move(subtractX, subtractY, subtractZ).inflate(20))) {
                            if (BeyonderUtil.currentPathwayMatchesNoException(pPlayer, BeyonderClassInit.MONSTER.get()) && BeyonderUtil.getSequence(pPlayer) <= 6) {
                                if (entity != null) {
                                    zombie.setTarget(entity);
                                }
                            }
                        }
                        pPlayer.level().addFreshEntity(zombie);
                    }
                    if (random.nextInt(10) == 8) {
                        zombie.setPos(x + randomPos, surfaceY, z + randomPos);
                        zombie.setItemSlot(EquipmentSlot.HEAD, ironHelmet);
                        zombie.setItemSlot(EquipmentSlot.CHEST, ironChestplate);
                        zombie.setItemSlot(EquipmentSlot.LEGS, ironLeggings);
                        zombie.setItemSlot(EquipmentSlot.FEET, ironBoots);
                        zombie.setItemSlot(EquipmentSlot.MAINHAND, ironSword);
                        for (LivingEntity entity : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().move(subtractX, subtractY, subtractZ).inflate(20))) {
                            if (BeyonderUtil.currentPathwayMatchesNoException(pPlayer, BeyonderClassInit.MONSTER.get()) && BeyonderUtil.getSequence(pPlayer) <= 6) {
                                if (entity != null) {
                                    zombie.setTarget(entity);
                                }
                            }
                        }
                        pPlayer.level().addFreshEntity(zombie);
                    }
                    if (random.nextInt(10) == 7) {
                        zombie.setPos(x + randomPos, surfaceY, z + randomPos);
                        zombie.setItemSlot(EquipmentSlot.HEAD, diamondHelmet);
                        zombie.setItemSlot(EquipmentSlot.CHEST, diamondChestplate);
                        zombie.setItemSlot(EquipmentSlot.LEGS, diamondLeggings);
                        zombie.setItemSlot(EquipmentSlot.FEET, diamondBoots);
                        zombie.setItemSlot(EquipmentSlot.MAINHAND, diamondSword);
                        for (LivingEntity entity : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().move(subtractX, subtractY, subtractZ).inflate(20))) {
                            if (BeyonderUtil.currentPathwayMatchesNoException(pPlayer, BeyonderClassInit.MONSTER.get()) && BeyonderUtil.getSequence(pPlayer) <= 6) {
                                if (entity != null) {
                                    zombie.setTarget(entity);
                                }
                            }
                        }
                        pPlayer.level().addFreshEntity(zombie);
                    }
                    if (random.nextInt(10) == 6) {
                        zombie.setPos(x + randomPos, surfaceY, z + randomPos);
                        zombie.setItemSlot(EquipmentSlot.HEAD, netheriteHelmet);
                        zombie.setItemSlot(EquipmentSlot.CHEST, netheriteChestplate);
                        zombie.setItemSlot(EquipmentSlot.LEGS, netheriteLeggings);
                        zombie.setItemSlot(EquipmentSlot.FEET, netheriteBoots);
                        zombie.setItemSlot(EquipmentSlot.MAINHAND, netheriteSword);
                        for (LivingEntity entity : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().move(subtractX, subtractY, subtractZ).inflate(20))) {
                            if (BeyonderUtil.currentPathwayMatchesNoException(pPlayer, BeyonderClassInit.MONSTER.get()) && BeyonderUtil.getSequence(pPlayer) <= 6) {
                                if (entity != null) {
                                    zombie.setTarget(entity);
                                }
                            }
                        }
                        pPlayer.level().addFreshEntity(zombie);
                    }
                    if (random.nextInt(20) == 5) {
                        skeleton.setPos(x + randomPos, surfaceY, z + randomPos);
                        skeleton.setItemSlot(EquipmentSlot.MAINHAND, enchantedBow);
                        for (LivingEntity entity : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().move(subtractX, subtractY, subtractZ).inflate(20))) {
                            if (BeyonderUtil.currentPathwayMatchesNoException(pPlayer, BeyonderClassInit.MONSTER.get()) && BeyonderUtil.getSequence(pPlayer) <= 6) {
                                if (entity != null) {
                                    zombie.setTarget(entity);
                                }
                            }
                        }
                        pPlayer.level().addFreshEntity(skeleton);
                    }
                    if (random.nextInt(20) == 4) {
                        skeleton.setPos(x + randomPos, surfaceY, z + randomPos);
                        skeleton.setItemSlot(EquipmentSlot.HEAD, leatherHelmet);
                        skeleton.setItemSlot(EquipmentSlot.CHEST, leatherChestplate);
                        skeleton.setItemSlot(EquipmentSlot.LEGS, leatherLeggings);
                        skeleton.setItemSlot(EquipmentSlot.FEET, leatherBoots);
                        enchantedBow.enchant(Enchantments.POWER_ARROWS, 1);
                        skeleton.setItemSlot(EquipmentSlot.MAINHAND, enchantedBow);
                        for (LivingEntity entity : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().move(subtractX, subtractY, subtractZ).inflate(20))) {
                            if (BeyonderUtil.currentPathwayMatchesNoException(pPlayer, BeyonderClassInit.MONSTER.get()) && BeyonderUtil.getSequence(pPlayer) <= 6) {
                                if (entity != null) {
                                    zombie.setTarget(entity);
                                }
                            }
                        }
                        pPlayer.level().addFreshEntity(skeleton);
                    }
                    if (random.nextInt(20) == 3) {
                        skeleton.setPos(x + randomPos, surfaceY, z + randomPos);
                        skeleton.setItemSlot(EquipmentSlot.HEAD, ironHelmet);
                        skeleton.setItemSlot(EquipmentSlot.CHEST, ironChestplate);
                        skeleton.setItemSlot(EquipmentSlot.LEGS, ironLeggings);
                        skeleton.setItemSlot(EquipmentSlot.FEET, ironBoots);
                        enchantedBow.enchant(Enchantments.POWER_ARROWS, 2);
                        skeleton.setItemSlot(EquipmentSlot.MAINHAND, enchantedBow);
                        for (LivingEntity entity : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().move(subtractX, subtractY, subtractZ).inflate(20))) {
                            if (BeyonderUtil.currentPathwayMatchesNoException(pPlayer, BeyonderClassInit.MONSTER.get()) && BeyonderUtil.getSequence(pPlayer) <= 6) {
                                if (entity != null) {
                                    zombie.setTarget(entity);
                                }
                            }
                        }
                        pPlayer.level().addFreshEntity(skeleton);
                    }
                    if (random.nextInt(20) == 2) {
                        skeleton.setPos(x + randomPos, surfaceY, z + randomPos);
                        skeleton.setItemSlot(EquipmentSlot.HEAD, diamondHelmet);
                        skeleton.setItemSlot(EquipmentSlot.CHEST, diamondChestplate);
                        skeleton.setItemSlot(EquipmentSlot.LEGS, diamondLeggings);
                        skeleton.setItemSlot(EquipmentSlot.FEET, diamondBoots);
                        enchantedBow.enchant(Enchantments.POWER_ARROWS, 3);
                        skeleton.setItemSlot(EquipmentSlot.MAINHAND, enchantedBow);
                        for (LivingEntity entity : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().move(subtractX, subtractY, subtractZ).inflate(20))) {
                            if (BeyonderUtil.currentPathwayMatchesNoException(pPlayer, BeyonderClassInit.MONSTER.get()) && BeyonderUtil.getSequence(pPlayer) <= 6) {
                                if (entity != null) {
                                    zombie.setTarget(entity);
                                }
                            }
                        }
                        pPlayer.level().addFreshEntity(skeleton);
                    }
                    if (random.nextInt(20) == 1) {
                        skeleton.setPos(x + randomPos, surfaceY, z + randomPos);
                        skeleton.setItemSlot(EquipmentSlot.HEAD, netheriteHelmet);
                        skeleton.setItemSlot(EquipmentSlot.CHEST, netheriteChestplate);
                        skeleton.setItemSlot(EquipmentSlot.LEGS, netheriteLeggings);
                        skeleton.setItemSlot(EquipmentSlot.FEET, netheriteBoots);
                        enchantedBow.enchant(Enchantments.POWER_ARROWS, 4);
                        skeleton.setItemSlot(EquipmentSlot.MAINHAND, enchantedBow);
                        for (LivingEntity entity : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().move(subtractX, subtractY, subtractZ).inflate(20))) {
                            if (BeyonderUtil.currentPathwayMatchesNoException(pPlayer, BeyonderClassInit.MONSTER.get()) && BeyonderUtil.getSequence(pPlayer) <= 6) {
                                if (entity != null) {
                                    zombie.setTarget(entity);
                                }
                            }
                        }
                        pPlayer.level().addFreshEntity(skeleton);
                    }
                    zombie.setDropChance(EquipmentSlot.HEAD, 0.0F);
                    zombie.setDropChance(EquipmentSlot.CHEST, 0.0F);
                    zombie.setDropChance(EquipmentSlot.LEGS, 0.0F);
                    zombie.setDropChance(EquipmentSlot.FEET, 0.0F);
                    skeleton.setDropChance(EquipmentSlot.HEAD, 0.0F);
                    skeleton.setDropChance(EquipmentSlot.CHEST, 0.0F);
                    skeleton.setDropChance(EquipmentSlot.LEGS, 0.0F);
                    skeleton.setDropChance(EquipmentSlot.FEET, 0.0F);
                    tag.putInt("calamityUndeadArmyCounter", tag.getInt("calamityUndeadArmyCounter") - 1);
                }
            } else {
                EventManager.removeFromRegularLoop(pPlayer, EFunctions.CALAMITY_UNDEAD_ARMY.get());
            }
        }
    }

    public static void calamityExplosion(LivingEntity livingEntity) {
        CompoundTag tag = livingEntity.getPersistentData();
        if (livingEntity.level() instanceof ServerLevel serverLevel) {
            int x = tag.getInt("calamityExplosionOccurrence");
            if (x >= 1 && livingEntity.tickCount % 20 == 0 && !livingEntity.level().isClientSide()) {
                int explosionX = tag.getInt("calamityExplosionX");
                int explosionY = tag.getInt("calamityExplosionY");
                int explosionZ = tag.getInt("calamityExplosionZ");
                int subtractX = explosionX - (int) livingEntity.getX();
                int subtractY = explosionY - (int) livingEntity.getY();
                int subtractZ = explosionZ - (int) livingEntity.getZ();
                tag.putInt("calamityExplosionOccurrence", x - 1);
                for (LivingEntity entity : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().move(subtractX, subtractY, subtractZ).inflate(15))) {
                    if (BeyonderUtil.currentPathwayMatchesNoException(livingEntity, BeyonderClassInit.MONSTER.get()) && BeyonderUtil.getSequence(livingEntity) <= 3) {
                        entity.getPersistentData().putInt("calamityExplosionImmunity", 2);
                    }
                }
            }
            if (x == 1) {
                int explosionX = tag.getInt("calamityExplosionX");
                int explosionY = tag.getInt("calamityExplosionY");
                int explosionZ = tag.getInt("calamityExplosionZ");
                int data = CalamityEnhancementData.getInstance(serverLevel).getCalamityEnhancement();
                livingEntity.level().playSound(null, explosionX, explosionY, explosionZ, SoundEvents.GENERIC_EXPLODE, SoundSource.AMBIENT, 5.0F, 5.0F);
                Explosion explosion = new Explosion(livingEntity.level(), null, explosionX, explosionY, explosionZ, 10.0F + (data * 3), true, Explosion.BlockInteraction.DESTROY);
                explosion.explode();
                explosion.finalizeExplosion(true);
                tag.putInt("calamityExplosionOccurrence", 0);
            }
        }
    }

//    private void applyRandomWeaponEffects(LivingEntity entity, int sequenceLevel) {
//        // Get or create a persistent random weapon type for this entity
//        String weaponType = getOrSetRandomWeaponType(entity);
//
//        // Apply effects based on the selected weapon type and sequence level
//        switch (weaponType) {
//            case "sword":
//                applySwordEffects(entity, sequenceLevel);
//                break;
//            case "axe":
//                applyAxeEffects(entity, sequenceLevel);
//                break;
//            case "pickaxe":
//                applyPickaxeEffects(entity, sequenceLevel);
//                break;
//            case "bow":
//                applyBowEffects(entity, sequenceLevel);
//                break;
//            case "shield":
//                applyShieldEffects(entity, sequenceLevel);
//                break;
//        }
//    }

//    private String getOrSetRandomWeaponType(LivingEntity entity) {
//        CompoundTag persistentData = entity.getPersistentData();
//        String weaponType = persistentData.getString("randomWeaponType");
//
//        if (weaponType.isEmpty()) {
//            // Select a random weapon type and store it persistently
//            String[] weaponTypes = {"sword", "axe", "pickaxe", "bow", "shield"};
//            weaponType = weaponTypes[entity.getRandom().nextInt(weaponTypes.length)];
//            persistentData.putString("randomWeaponType", weaponType);
//        }
//
//        return weaponType;
//    }
//
//    private void applySwordEffects(LivingEntity entity, int sequenceLevel) {
//        if (sequenceLevel == 8 || sequenceLevel == 7) {
//            applyMobEffect(entity, MobEffects.MOVEMENT_SPEED, 60, speed + 1, true, true);
//        } else if (sequenceLevel == 6 || sequenceLevel == 5) {
//            applyMobEffect(entity, MobEffects.MOVEMENT_SPEED, 60, speed + 1, true, true);
//            applyMobEffect(entity, MobEffects.DIG_SPEED, 60, 0, true, true);
//        } else if (sequenceLevel <= 4) {
//            applyMobEffect(entity, MobEffects.MOVEMENT_SPEED, 60, speed + 2, true, true);
//            applyMobEffect(entity, MobEffects.DIG_SPEED, 60, 0, true, true);
//        }
//    }
//
//    private void applyAxeEffects(LivingEntity entity, int sequenceLevel) {
//        if (sequenceLevel == 8 || sequenceLevel == 7) {
//            applyMobEffect(entity, MobEffects.DAMAGE_BOOST, 60, strength + 1, true, true);
//        } else if (sequenceLevel == 6 || sequenceLevel == 5) {
//            applyMobEffect(entity, MobEffects.DAMAGE_BOOST, 60, strength + 1, true, true);
//            applyMobEffect(entity, MobEffects.DAMAGE_RESISTANCE, 60, resistance + 1, true, true);
//        } else if (sequenceLevel <= 4) {
//            applyMobEffect(entity, MobEffects.DAMAGE_BOOST, 60, strength + 1, true, true);
//            applyMobEffect(entity, MobEffects.DAMAGE_RESISTANCE, 60, resistance + 1, true, true);
//        }
//    }
//
//    private void applyPickaxeEffects(LivingEntity entity, int sequenceLevel) {
//        if (sequenceLevel == 8 || sequenceLevel == 7) {
//            applyMobEffect(entity, MobEffects.MOVEMENT_SPEED, 60, speed + 1, true, true);
//
//        } else if (sequenceLevel == 6 || sequenceLevel == 5) {
//            applyMobEffect(entity, MobEffects.MOVEMENT_SPEED, 60, speed + 2, true, true);
//
//        } else if (sequenceLevel <= 4) {
//            applyMobEffect(entity, MobEffects.MOVEMENT_SPEED, 60, speed + 3, true, true);
//        }
//    }
//
//    private void applyBowEffects(LivingEntity entity, int sequenceLevel) {
//        if (sequenceLevel == 8 || sequenceLevel == 7) {
//            applyMobEffect(entity, MobEffects.MOVEMENT_SPEED, 60, speed + 1, true, true);
//        } else if (sequenceLevel == 6 || sequenceLevel == 5) {
//            applyMobEffect(entity, MobEffects.MOVEMENT_SPEED, 60, speed + 1, true, true);
//        } else if (sequenceLevel <= 4) {
//            applyMobEffect(entity, MobEffects.MOVEMENT_SPEED, 60, speed + 2, true, true);
//            applyMobEffect(entity, MobEffects.REGENERATION, 60, regen + 1, true, true);
//        }
//    }

//    private void applyShieldEffects(LivingEntity entity, int sequenceLevel) {
//        if (sequenceLevel == 8 || sequenceLevel == 7) {
//            applyMobEffect(entity, MobEffects.DAMAGE_RESISTANCE, 60, resistance + 1, true, true);
//        } else if (sequenceLevel == 6 || sequenceLevel == 5) {
//            applyMobEffect(entity, MobEffects.DAMAGE_RESISTANCE, 60, resistance + 1, true, true);
//        } else if (sequenceLevel <= 4) {
//            applyMobEffect(entity, MobEffects.DAMAGE_RESISTANCE, 60, resistance + 1, true, true);
//        }
//    }

    @Override
    public void removeAllEvents(LivingEntity entity) {
        MonsterPassiveEvents.removeAllEvents(entity);
    }

    @Override
    public void addAllEvents(LivingEntity entity, int sequence) {
        MonsterPassiveEvents.addAllEvents(entity, sequence);
    }
}
