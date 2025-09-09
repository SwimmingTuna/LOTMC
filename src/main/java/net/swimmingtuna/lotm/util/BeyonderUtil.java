package net.swimmingtuna.lotm.util;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.beyonder.*;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.blocks.DimensionalSight.DimensionalSightTileEntity;
import net.swimmingtuna.lotm.capabilities.scribed_abilities.ScribedUtils;
import net.swimmingtuna.lotm.capabilities.sealed_data.SealedUtils;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.client.Configs;
import net.swimmingtuna.lotm.commands.AbilityRegisterCommand;
import net.swimmingtuna.lotm.entity.ApprenticeDoorEntity;
import net.swimmingtuna.lotm.entity.CustomFallingBlockEntity;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.GameRuleInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Ability;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice.*;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.*;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.*;
import net.swimmingtuna.lotm.item.SealedArtifacts.DeathKnell;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.*;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EFunctions;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventManager;
import net.swimmingtuna.lotm.util.AllyInformation.PlayerAllyData;
import net.swimmingtuna.lotm.util.ClientData.ClientLeftclickCooldownData;
import net.swimmingtuna.lotm.util.ClientData.ClientShouldntRenderHandData;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickHandlerSkill;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickHandlerSkillP;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickHandlerSword;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import net.swimmingtuna.lotm.world.worlddata.BeyonderEntityData;
import net.swimmingtuna.lotm.world.worlddata.CalamityEnhancementData;
import net.swimmingtuna.lotm.world.worlddata.PlayerMobTracker;
import net.swimmingtuna.lotm.world.worldgen.dimension.DimensionInit;
import org.jetbrains.annotations.Nullable;
import virtuoel.pehkui.api.ScaleTypes;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;

import static net.swimmingtuna.lotm.commands.BeyonderRecipeCommand.executeRecipeCommand;
import static net.swimmingtuna.lotm.init.DamageTypeInit.MENTAL_DAMAGE;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.CycleOfFate.removeCycleEffect;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.MentalPlague.applyEffectsAndDamage;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.PsychologicalInvisibility.removePsychologicalInvisibilityEffect;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems.TwilightFreeze.removeTwilightFreezeEffect;

public class BeyonderUtil {

    public static final Map<UUID, SimpleAbilityItem> pendingAbilityCopies = new HashMap<>();

    public static Projectile getProjectiles(LivingEntity livingEntity, int radius) {
        if (livingEntity.level().isClientSide()) {
            return null;
        }
        Level level = livingEntity.level();
        AABB boundingBox = livingEntity.getBoundingBox().inflate(radius);
        Predicate<Projectile> projectilePredicate = projectile -> {
            if (projectile.tickCount <= 6 || projectile.tickCount >= 100) {
                return false;
            }
            if (projectile.getOwner() != livingEntity) {
                return false;
            }

            CompoundTag tag = projectile.getPersistentData();
            return tag.getInt("windDodgeProjectilesCounter") == 0;
        };
        return level.getEntitiesOfClass(Projectile.class, boundingBox, projectilePredicate).stream()
                .findFirst()
                .orElse(null);
    }

    public static boolean isLookingAt(LivingEntity looker, Entity target, double maxAngleDegrees) {
        Vec3 lookVec = looker.getViewVector(1.0F);
        Vec3 targetVec = target.position().subtract(looker.position()).normalize();

        double dot = lookVec.dot(targetVec);
        double angle = Math.acos(dot) * 180.0 / Math.PI;

        return angle <= maxAngleDegrees;
    }

    public static boolean isLookingTowards2D(LivingEntity looker, Entity target, double maxAngleDegrees) {
        Vec3 lookVec = looker.getViewVector(1.0F);
        Vec3 targetVec = target.position().subtract(looker.position());
        Vec3 lookVec2D = new Vec3(lookVec.x, 0, lookVec.z).normalize();
        Vec3 targetVec2D = new Vec3(targetVec.x, 0, targetVec.z).normalize();

        double dot = lookVec2D.dot(targetVec2D);
        double angle = Math.acos(Math.max(-1.0, Math.min(1.0, dot))) * 180.0 / Math.PI;

        return angle <= maxAngleDegrees;
    }

    public static void projectileEvent(LivingEntity living) {
        //PROJECTILE EVENT
        if (living.level().isClientSide) {
            return;
        }
        if (BeyonderUtil.getPathway(living) == null) {
            return;
        }
        Projectile projectile = BeyonderUtil.getProjectiles(living, 50);
        if (projectile == null) return;
        //MATTER ACCELERATION ENTITIES
        LivingEntity target = BeyonderUtil.getTarget(projectile, 75, 0);
        if (target != null) {
            if (BeyonderUtil.currentPathwayAndSequenceMatches(living, BeyonderClassInit.SAILOR.get(), 8) && living.getPersistentData().getBoolean("sailorProjectileMovement")) {
                double dx = target.getX() - projectile.getX();
                double dy = target.getY() - projectile.getY();
                double dz = target.getZ() - projectile.getZ();
                double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
                double speed = 1.2;
                projectile.setDeltaMovement((dx / length) * speed, (dy / length) * speed, (dz / length) * speed);
                projectile.hurtMarked = true;
            }
        }


        //SAILOR PASSIVE CHECK FROM HERE
        if (target != null) {
            if (BeyonderUtil.currentPathwayAndSequenceMatches(living, BeyonderClassInit.SAILOR.get(), 8) && living.getPersistentData().getBoolean("sailorProjectileMovement")) {
                double dx = target.getX() - projectile.getX();
                double dy = target.getY() - projectile.getY();
                double dz = target.getZ() - projectile.getZ();
                double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
                double speed = 1.2;
                projectile.setDeltaMovement((dx / length) * speed, (dy / length) * speed, (dz / length) * speed);
                projectile.hurtMarked = true;
            }
        }

        //MONSTER CALCULATION PASSIVE
        if (target != null) {
            if (BeyonderUtil.currentPathwayAndSequenceMatches(living, BeyonderClassInit.MONSTER.get(), 8) && living.getPersistentData().getBoolean("monsterProjectileControl")) {
                double dx = target.getX() - projectile.getX();
                double dy = target.getY() - projectile.getY();
                double dz = target.getZ() - projectile.getZ();
                double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
                double speed = 1.2;
                projectile.setDeltaMovement((dx / length) * speed, (dy / length) * speed, (dz / length) * speed);
                projectile.hurtMarked = true;
            }
        }
    }


    public static LivingEntity getTarget(Projectile projectile, double maxValue, double minValue) {
        Entity owner = null;
        if (projectile.getOwner() != null) {
            if (projectile.getOwner() instanceof LivingEntity) {
                owner = projectile.getOwner();
            }
        }
        LivingEntity closestEntity = null;
        double closestDistance = Double.MAX_VALUE;
        Vec3 projectilePos = projectile.position();
        List<LivingEntity> nearbyEntities = projectile.level().getEntitiesOfClass(LivingEntity.class, projectile.getBoundingBox().inflate(maxValue));
        for (LivingEntity entity : nearbyEntities) {
            if (entity != owner && !entity.level().isClientSide() && (owner instanceof LivingEntity living && !BeyonderUtil.areAllies(living, entity))) {
                double distance = entity.distanceToSqr(projectilePos);
                if (distance < maxValue && distance > minValue && distance < closestDistance) {
                    closestDistance = distance;
                    closestEntity = entity;
                }
            }
        }
        return closestEntity;
    }

    public static Projectile getLivingEntitiesProjectile(LivingEntity player) {
        if (player.level().isClientSide()) {
            return null;
        }
        List<Projectile> projectiles = player.level().getEntitiesOfClass(Projectile.class, player.getBoundingBox().inflate(30));
        for (Projectile projectile : projectiles) {
            if (projectile.getOwner() == player && projectile.tickCount > 8 && projectile.tickCount < 50) {
                return projectile;
            }
        }
        return null;
    }

    public static DamageSource magicSource(Entity attacker, Entity target) {
        Level level = target.level();
        Holder<DamageType> damageTypeHolder = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC);
        return new DamageSource(damageTypeHolder, attacker, attacker, null) {
            @Override
            public boolean is(TagKey<DamageType> damageTypeKey) {
                if (damageTypeKey == DamageTypeTags.BYPASSES_INVULNERABILITY) {
                    return true;
                }
                return super.is(damageTypeKey);
            }
        };
    }

    public static DamageSource explosionSource(Entity attacker, Entity target) {
        Level level = target.level();
        Holder<DamageType> damageTypeHolder = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.EXPLOSION);
        return new DamageSource(damageTypeHolder, attacker, attacker, null) {
            @Override
            public boolean is(TagKey<DamageType> damageTypeKey) {
                if (damageTypeKey == DamageTypeTags.BYPASSES_INVULNERABILITY) {
                    return true;
                }
                return super.is(damageTypeKey);
            }
        };
    }

    public static DamageSource fallSource(Entity attacker, Entity target) {
        Level level = target.level();
        Holder<DamageType> damageTypeHolder = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.FALL);
        return new DamageSource(damageTypeHolder, attacker, attacker, null) {
            @Override
            public boolean is(TagKey<DamageType> damageTypeKey) {
                if (damageTypeKey == DamageTypeTags.BYPASSES_INVULNERABILITY) {
                    return true;
                }
                return super.is(damageTypeKey);
            }
        };
    }

    public static DamageSource freezeSource(Entity attacker, Entity target) {
        Level level = target.level();
        Holder<DamageType> damageTypeHolder = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.FREEZE);
        return new DamageSource(damageTypeHolder, attacker, attacker, null) {
            @Override
            public boolean is(TagKey<DamageType> damageTypeKey) {
                if (damageTypeKey == DamageTypeTags.BYPASSES_INVULNERABILITY) {
                    return true;
                }
                return super.is(damageTypeKey);
            }
        };
    }

    public static DamageSource lavaSource(Entity attacker, Entity target) {
        Level level = target.level();
        Holder<DamageType> damageTypeHolder = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.LAVA);
        return new DamageSource(damageTypeHolder, attacker, attacker, null) {
            @Override
            public boolean is(TagKey<DamageType> damageTypeKey) {
                if (damageTypeKey == DamageTypeTags.BYPASSES_INVULNERABILITY) {
                    return true;
                }
                return super.is(damageTypeKey);
            }
        };
    }

    public static DamageSource fallingBlockSource(Entity attacker, Entity target) {
        Level level = target.level();
        Holder<DamageType> damageTypeHolder = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.FALLING_BLOCK);
        return new DamageSource(damageTypeHolder, attacker, attacker, null) {
            @Override
            public boolean is(TagKey<DamageType> damageTypeKey) {
                if (damageTypeKey == DamageTypeTags.BYPASSES_INVULNERABILITY) {
                    return true;
                }
                return super.is(damageTypeKey);
            }
        };
    }

    public static DamageSource lightningSource(Entity attacker, Entity target) {
        Level level = target.level();
        Holder<DamageType> damageTypeHolder = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.LIGHTNING_BOLT);
        return new DamageSource(damageTypeHolder, attacker, attacker, null) {
            @Override
            public boolean is(TagKey<DamageType> damageTypeKey) {
                if (damageTypeKey == DamageTypeTags.BYPASSES_INVULNERABILITY) {
                    return true;
                }
                return super.is(damageTypeKey);
            }
        };
    }

    public static DamageSource genericSource(Entity attacker, Entity target) {
        Level level = target.level();
        Holder<DamageType> damageTypeHolder = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.GENERIC);
        return new DamageSource(damageTypeHolder, attacker, attacker, null) {
            @Override
            public boolean is(TagKey<DamageType> damageTypeKey) {
                if (damageTypeKey == DamageTypeTags.BYPASSES_INVULNERABILITY) {
                    return true;
                }
                return super.is(damageTypeKey);
            }
        };
    }

    public static DamageSource mentalSource(Level level, LivingEntity attacker, LivingEntity target) {
        final Registry<DamageType> registry = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        final Holder.Reference<DamageType> damage = registry.getHolderOrThrow(MENTAL_DAMAGE);
        return new MentalDamageSource(damage, attacker, target);
    }

    public static boolean applyMentalDamage(LivingEntity attacker, LivingEntity target, float baseAmount) {
        Level level = attacker.level();
        Holder<DamageType> damageTypeHolder = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(MENTAL_DAMAGE);
        MentalDamageSource damageSource = new MentalDamageSource(damageTypeHolder, attacker, target);
        float calculatedDamage = damageSource.calculateDamage(baseAmount);
        return target.hurt(damageSource, calculatedDamage);
    }


    public static StructurePlaceSettings getStructurePlaceSettings(BlockPos pos) {
        BoundingBox boundingBox = new BoundingBox(
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                pos.getX() + 160,
                pos.getY() + 97,
                pos.getZ() + 265
        );
        StructurePlaceSettings settings = new StructurePlaceSettings();
        settings.setRotation(Rotation.NONE);
        settings.setMirror(Mirror.NONE);
        settings.setRotationPivot(pos);
        settings.setBoundingBox(boundingBox);
        return settings;
    }

    public static List<Item> getAbilities(LivingEntity livingEntity) {
        List<Item> abilityNames = new ArrayList<>();
        if (livingEntity.level().isClientSide()) {
            return abilityNames;
        }
        int sequence = getSequence(livingEntity);
        if (currentPathwayMatchesNoException(livingEntity, BeyonderClassInit.SPECTATOR.get())) {
            if (sequence <= 8) {
                abilityNames.add(ItemInit.MIND_READING.get());
            }
            if (sequence <= 7) {
                abilityNames.add(ItemInit.AWE.get());
                abilityNames.add(ItemInit.PLACATE.get());
            }
            if (sequence == 7 || sequence == 6 || sequence == 5) {
                abilityNames.add(ItemInit.FRENZY.get());
            }
            if (sequence <= 6) {
                abilityNames.add(ItemInit.PSYCHOLOGICAL_INVISIBILITY.get());
                abilityNames.add(ItemInit.BATTLE_HYPNOTISM.get());
            }
            if (sequence <= 5) {
                abilityNames.add(ItemInit.GUIDANCE.get());
                abilityNames.add(ItemInit.ALTERATION.get());
                abilityNames.add(ItemInit.NIGHTMARE.get());
                abilityNames.add(ItemInit.DREAM_WALKING.get());
            }
            if (sequence <= 4) {
                abilityNames.add(ItemInit.APPLY_MANIPULATION.get());
                abilityNames.add(ItemInit.MANIPULATE_MOVEMENT.get());
                abilityNames.add(ItemInit.MANIPULATE_FONDNESS.get());
                abilityNames.add(ItemInit.MANIPULATE_EMOTION.get());
                abilityNames.add(ItemInit.MENTAL_PLAGUE.get());
                abilityNames.add(ItemInit.MIND_STORM.get());
                abilityNames.add(ItemInit.DRAGON_BREATH.get());
            }
            if (sequence <= 3) {
                abilityNames.add(ItemInit.CONSCIOUSNESS_STROLL.get());
                abilityNames.add(ItemInit.PLAGUE_STORM.get());
                abilityNames.add(ItemInit.DREAM_WEAVING.get());
            }
            if (sequence <= 2) {
                abilityNames.add(ItemInit.DISCERN.get());
                abilityNames.add(ItemInit.DREAM_INTO_REALITY.get());
            }
            if (sequence <= 1) {
                abilityNames.add(ItemInit.PROPHECY.get());
            }
            if (sequence <= 0) {
                abilityNames.add(ItemInit.ENVISION_BARRIER.get());
                abilityNames.add(ItemInit.ENVISION_DEATH.get());
                abilityNames.add(ItemInit.ENVISION_HEALTH.get());
                abilityNames.add(ItemInit.ENVISION_KINGDOM.get());
                abilityNames.add(ItemInit.ENVISION_LIFE.get());
                abilityNames.add(ItemInit.ENVISION_LOCATION.get());
                abilityNames.add(ItemInit.ENVISION_WEATHER.get());
            }
        }
        if (currentPathwayMatchesNoException(livingEntity, BeyonderClassInit.SAILOR.get())) {
            if (sequence <= 8) {
                abilityNames.add(ItemInit.RAGING_BLOWS.get());
            }
            if (sequence <= 7) {
                abilityNames.add(ItemInit.ENABLE_OR_DISABLE_LIGHTNING.get());
                abilityNames.add(ItemInit.AQUEOUS_LIGHT_PUSH.get());
                abilityNames.add(ItemInit.AQUEOUS_LIGHT_PULL.get());
                abilityNames.add(ItemInit.AQUEOUS_LIGHT_DROWN.get());
                abilityNames.add(ItemInit.SAILORPROJECTILECTONROL.get());
            }
            if (sequence <= 6) {
                abilityNames.add(ItemInit.WIND_MANIPULATION_BLADE.get());
                abilityNames.add(ItemInit.WIND_MANIPULATION_FLIGHT.get());
                abilityNames.add(ItemInit.WIND_MANIPULATION_SENSE.get());
            }
            if (sequence <= 5) {
                abilityNames.add(ItemInit.SAILOR_LIGHTNING.get());
                abilityNames.add(ItemInit.SIREN_SONG_HARM.get());
                abilityNames.add(ItemInit.SIREN_SONG_STRENGTHEN.get());
                abilityNames.add(ItemInit.SIREN_SONG_WEAKEN.get());
                abilityNames.add(ItemInit.SIREN_SONG_STUN.get());
                abilityNames.add(ItemInit.ACIDIC_RAIN.get());
                abilityNames.add(ItemInit.WATER_SPHERE.get());
            }
            if (sequence <= 4) {
                abilityNames.add(ItemInit.TSUNAMI.get());
                abilityNames.add(ItemInit.TSUNAMI_SEAL.get());
                abilityNames.add(ItemInit.HURRICANE.get());
                abilityNames.add(ItemInit.TORNADO.get());
                abilityNames.add(ItemInit.EARTHQUAKE.get());
                abilityNames.add(ItemInit.ROAR.get());
            }
            if (sequence <= 3) {
                abilityNames.add(ItemInit.LIGHTNING_STORM.get());
                abilityNames.add(ItemInit.LIGHTNING_BRANCH.get());
                abilityNames.add(ItemInit.SONIC_BOOM.get());
                abilityNames.add(ItemInit.THUNDER_CLAP.get());
            }
            if (sequence == 3) {
                abilityNames.add(ItemInit.AQUATIC_LIFE_MANIPULATION.get());
            }
            if (sequence <= 2) {
                abilityNames.add(ItemInit.RAIN_EYES.get());
                abilityNames.add(ItemInit.VOLCANIC_ERUPTION.get());
                abilityNames.add(ItemInit.EXTREME_COLDNESS.get());
                abilityNames.add(ItemInit.LIGHTNING_BALL.get());
            }
            if (sequence <= 1) {
                abilityNames.add(ItemInit.LIGHTNING_BALL_ABSORB.get());
                abilityNames.add(ItemInit.SAILOR_LIGHTNING_TRAVEL.get());
                abilityNames.add(ItemInit.STAR_OF_LIGHTNING.get());
                abilityNames.add(ItemInit.LIGHTNING_REDIRECTION.get());
            }
            if (sequence <= 0) {
                abilityNames.add(ItemInit.STORM_SEAL.get());
                abilityNames.add(ItemInit.WATER_COLUMN.get());
                abilityNames.add(ItemInit.MATTER_ACCELERATION_SELF.get());
                abilityNames.add(ItemInit.MATTER_ACCELERATION_BLOCKS.get());
                abilityNames.add(ItemInit.MATTER_ACCELERATION_ENTITIES.get());
                abilityNames.add(ItemInit.TYRANNY.get());
            }
        }
        if (currentPathwayMatchesNoException(livingEntity, BeyonderClassInit.MONSTER.get())) {
            if (sequence <= 9) {
                abilityNames.add(ItemInit.SPIRITVISION.get());
                abilityNames.add(ItemInit.MONSTERDANGERSENSE.get());
            }
            if (sequence <= 8) {
                abilityNames.add(ItemInit.MONSTERPROJECTILECONTROL.get());

            }
            if (sequence <= 7) {
                abilityNames.add(ItemInit.LUCKPERCEPTION.get());

            }
            if (sequence <= 6) {
                abilityNames.add(ItemInit.PSYCHESTORM.get());
            }
            if (sequence <= 5) {
                abilityNames.add(ItemInit.LUCK_MANIPULATION.get());
                abilityNames.add(ItemInit.LUCKDEPRIVATION.get());
                abilityNames.add(ItemInit.LUCKGIFTING.get());
                abilityNames.add(ItemInit.MISFORTUNEBESTOWAL.get());
                abilityNames.add(ItemInit.LUCKFUTURETELLING.get());
            }
            if (sequence <= 4) {
                abilityNames.add(ItemInit.DECAYDOMAIN.get());
                abilityNames.add(ItemInit.PROVIDENCEDOMAIN.get());
                abilityNames.add(ItemInit.LUCKCHANNELING.get());
                abilityNames.add(ItemInit.LUCKDENIAL.get());
                abilityNames.add(ItemInit.MISFORTUNEMANIPULATION.get());
                abilityNames.add(ItemInit.MONSTERCALAMITYATTRACTION.get());
            }
            if (sequence <= 3) {
                abilityNames.add(ItemInit.CALAMITYINCARNATION.get());
                abilityNames.add(ItemInit.ENABLEDISABLERIPPLE.get());
                abilityNames.add(ItemInit.AURAOFCHAOS.get());
                abilityNames.add(ItemInit.CHAOSWALKERCOMBAT.get());
                abilityNames.add(ItemInit.MISFORTUNEREDIRECTION.get());
                abilityNames.add(ItemInit.MONSTERDOMAINTELEPORATION.get());
            }
            if (sequence <= 2) {
                abilityNames.add(ItemInit.WHISPEROFCORRUPTION.get());
                abilityNames.add(ItemInit.FORTUNEAPPROPIATION.get());
                abilityNames.add(ItemInit.FALSEPROPHECY.get());
                abilityNames.add(ItemInit.MISFORTUNEIMPLOSION.get());
            }
            if (sequence <= 1) {
                abilityNames.add(ItemInit.MONSTERREBOOT.get());
                abilityNames.add(ItemInit.FATEREINCARNATION.get());
                abilityNames.add(ItemInit.CYCLEOFFATE.get());
                abilityNames.add(ItemInit.CHAOSAMPLIFICATION.get());
                abilityNames.add(ItemInit.FATEDCONNECTION.get());
                abilityNames.add(ItemInit.REBOOTSELF.get());
            }
            if (sequence <= 0) {
                abilityNames.add(ItemInit.PROBABILITYMISFORTUNEINCREASE.get());
                abilityNames.add(ItemInit.PROBABILITYFORTUNEINCREASE.get());
                abilityNames.add(ItemInit.PROBABILITYFORTUNE.get());
                abilityNames.add(ItemInit.PROBABILITYMISFORTUNE.get());
                abilityNames.add(ItemInit.PROBABILITYWIPE.get());
                abilityNames.add(ItemInit.PROBABILITYEFFECT.get());
                abilityNames.add(ItemInit.PROBABILITYINFINITEFORTUNE.get());
                abilityNames.add(ItemInit.PROBABILITYINFINITEMISFORTUNE.get());
            }
        }
        if (currentPathwayMatchesNoException(livingEntity, BeyonderClassInit.WARRIOR.get())) {
            if (sequence <= 6) {
                abilityNames.add(ItemInit.GIGANTIFICATION.get());
                abilityNames.add(ItemInit.LIGHTOFDAWN.get());
                abilityNames.add(ItemInit.DAWNARMORY.get());
                abilityNames.add(ItemInit.DAWNWEAPONRY.get());
            }
            if (sequence <= 5) {
                abilityNames.add(ItemInit.ENABLEDISABLEPROTECTION.get());
            }
            if (sequence <= 4) {
                abilityNames.add(ItemInit.EYEOFDEMONHUNTING.get());
                abilityNames.add(ItemInit.WARRIORDANGERSENSE.get());
            }
            if (sequence <= 3) {
                abilityNames.add(ItemInit.MERCURYLIQUEFICATION.get());
                abilityNames.add(ItemInit.SILVERSWORDMANIFESTATION.get());
                abilityNames.add(ItemInit.SILVERRAPIER.get());
                abilityNames.add(ItemInit.SILVERARMORY.get());
                abilityNames.add(ItemInit.LIGHTCONCEALMENT.get());
            }
            if (sequence <= 2) {
                abilityNames.add(ItemInit.BEAMOFGLORY.get());
                abilityNames.add(ItemInit.AURAOFGLORY.get());
                abilityNames.add(ItemInit.TWILIGHTSWORD.get());
                abilityNames.add(ItemInit.MERCURYCAGE.get());
            }
            if (sequence <= 1) {
                abilityNames.add(ItemInit.DIVINEHANDLEFT.get());
                abilityNames.add(ItemInit.DIVINEHANDRIGHT.get());
                abilityNames.add(ItemInit.TWILIGHTMANIFESTATION.get());
            }
            if (sequence <= 0) {
                abilityNames.add(ItemInit.AURAOFTWILIGHT.get());
                abilityNames.add(ItemInit.TWILIGHTFREEZE.get());
                abilityNames.add(ItemInit.TWILIGHTACCELERATE.get());
                abilityNames.add(ItemInit.GLOBEOFTWILIGHT.get());
                abilityNames.add(ItemInit.BEAMOFTWILIGHT.get());
                abilityNames.add(ItemInit.TWILIGHTLIGHT.get());
            }
        }
        if (currentPathwayMatchesNoException(livingEntity, BeyonderClassInit.APPRENTICE.get())) {
            if (sequence <= 9) {
                abilityNames.add(ItemInit.CREATEDOOR.get());
            }
            if (sequence <= 8) {
                abilityNames.add(ItemInit.TRICKBURNING.get());
                abilityNames.add(ItemInit.TRICKFREEZING.get());
                abilityNames.add(ItemInit.TRICKTUMBLE.get());
                abilityNames.add(ItemInit.TRICKWIND.get());
                abilityNames.add(ItemInit.TRICKFOG.get());
                abilityNames.add(ItemInit.TRICKELECTRICSHOCK.get());
                abilityNames.add(ItemInit.TRICKTELEKENISIS.get());
                abilityNames.add(ItemInit.TRICKESCAPETRICK.get());
                abilityNames.add(ItemInit.TRICKFLASH.get());
                abilityNames.add(ItemInit.TRICKLOUDNOISE.get());
                abilityNames.add(ItemInit.TRICKBLACKCURTAIN.get());
            }
            if (sequence <= 7) {
                abilityNames.add(ItemInit.ASTROLOGER_SPIRIT_VISION.get());
            }
            if (sequence <= 6) {
                abilityNames.add(ItemInit.RECORDSCRIBE.get());
                abilityNames.add(ItemInit.SCRIBEABILITIES.get());
            }
            if (sequence <= 5) {
                abilityNames.add(ItemInit.TRAVELERSDOOR.get());
                abilityNames.add(ItemInit.TRAVELERSDOORHOME.get());
                abilityNames.add(ItemInit.INVISIBLEHAND.get());
                abilityNames.add(ItemInit.BLINK.get());
                abilityNames.add(ItemInit.BLINKAFTERIMAGE.get());
            }
            if (sequence <= 4) {
                abilityNames.add(ItemInit.BLINK_STATE.get());
                abilityNames.add(ItemInit.EXILE.get());
                abilityNames.add(ItemInit.DOOR_MIRAGE.get());
                abilityNames.add(ItemInit.CREATE_CONCEALED_BUNDLE.get());
                abilityNames.add(ItemInit.CREATE_CONCEALED_SPACE.get());
                abilityNames.add(ItemInit.SEPARATE_WORM_OF_STAR.get());
            }
            if (sequence <= 3) {
                abilityNames.add(ItemInit.SPATIAL_CAGE.get());
                abilityNames.add(ItemInit.SPATIAL_TEARING.get());
            }
            if (sequence <= 2) {
                abilityNames.add(ItemInit.SYMBOLIZATION.get());
                abilityNames.add(ItemInit.DIMENSIONAL_SIGHT.get());
                abilityNames.add(ItemInit.REPLICATE.get());
                abilityNames.add(ItemInit.SEALING.get());
                abilityNames.add(ItemInit.TELEPORTATION.get());
            }
            if (sequence <= 1) {
                abilityNames.add(ItemInit.SPACE_FRAGMENTATION.get());
                abilityNames.add(ItemInit.GRAVITY_MANIPULATION.get());
                abilityNames.add(ItemInit.SPATIAL_MAZE.get());
            }
            if (sequence <= 0) {
                abilityNames.add(ItemInit.DOOR_SPATIAL_LOCK_ON.get());
                abilityNames.add(ItemInit.DOOR_DIMENSION_CLOSING.get());
                abilityNames.add(ItemInit.DOOR_SEALED_SPACE.get());
                abilityNames.add(ItemInit.DOOR_LAYERING.get());
                abilityNames.add(ItemInit.DOOR_GAMMA_RAY_BURST.get());
                abilityNames.add(ItemInit.CONCEPTUALIZATION.get());
                abilityNames.add(ItemInit.REPLICATION.get());
            }
        }
        return abilityNames;
    }

    private static final Map<ChatFormatting, Integer> COLOR_MAP = new HashMap<ChatFormatting, Integer>();
    private static final Map<String, BeyonderClass> NAME_TO_BEYONDER = new HashMap<>();

    static {
        COLOR_MAP.put(ChatFormatting.BLACK, 0x000000);
        COLOR_MAP.put(ChatFormatting.DARK_BLUE, 0x0000AA);
        COLOR_MAP.put(ChatFormatting.DARK_GREEN, 0x00AA00);
        COLOR_MAP.put(ChatFormatting.DARK_AQUA, 0x00AAAA);
        COLOR_MAP.put(ChatFormatting.DARK_RED, 0xAA0000);
        COLOR_MAP.put(ChatFormatting.DARK_PURPLE, 0xAA00AA);
        COLOR_MAP.put(ChatFormatting.GOLD, 0xFFAA00);
        COLOR_MAP.put(ChatFormatting.GRAY, 0xAAAAAA);
        COLOR_MAP.put(ChatFormatting.DARK_GRAY, 0x555555);
        COLOR_MAP.put(ChatFormatting.BLUE, 0x5555FF);
        COLOR_MAP.put(ChatFormatting.GREEN, 0x55FF55);
        COLOR_MAP.put(ChatFormatting.AQUA, 0x55FFFF);
        COLOR_MAP.put(ChatFormatting.RED, 0xFF5555);
        COLOR_MAP.put(ChatFormatting.LIGHT_PURPLE, 0xFF55FF);
        COLOR_MAP.put(ChatFormatting.YELLOW, 0xFFFF55);
        COLOR_MAP.put(ChatFormatting.WHITE, 0xFFFFFF);
        NAME_TO_BEYONDER.put("Apothecary", new ApothecaryClass());
        NAME_TO_BEYONDER.put("Apprentice", new ApprenticeClass());
        NAME_TO_BEYONDER.put("Arbiter", new ArbiterClass());
        NAME_TO_BEYONDER.put("Assassin", new AssassinClass());
        NAME_TO_BEYONDER.put("Bard", new BardClass());
        NAME_TO_BEYONDER.put("Corpse Collector", new CorpseCollectorClass());
        NAME_TO_BEYONDER.put("Criminal", new CriminalClass());
        NAME_TO_BEYONDER.put("Hunter", new HunterClass());
        NAME_TO_BEYONDER.put("Lawyer", new LawyerClass());
        NAME_TO_BEYONDER.put("Marauder", new MarauderClass());
        NAME_TO_BEYONDER.put("Monster", new MonsterClass());
        NAME_TO_BEYONDER.put("Mystery Pryer", new MysteryPryerClass());
        NAME_TO_BEYONDER.put("Planter", new PlanterClass());
        NAME_TO_BEYONDER.put("Prisoner", new PrisonerClass());
        NAME_TO_BEYONDER.put("Reader", new ReaderClass());
        NAME_TO_BEYONDER.put("Sailor", new SailorClass());
        NAME_TO_BEYONDER.put("Savant", new SavantClass());
        NAME_TO_BEYONDER.put("Secret Supplicant", new SecretsSupplicantClass());
        NAME_TO_BEYONDER.put("Seer", new SeerClass());
        NAME_TO_BEYONDER.put("Sleepless", new SleeplessClass());
        NAME_TO_BEYONDER.put("Spectator", new SpectatorClass());
        NAME_TO_BEYONDER.put("Warrior", new WarriorClass());
    }

    private static String getItemName(Item item) {
        return I18n.get(item.getDescriptionId()).toLowerCase();
    }

    private static final String REGISTERED_ABILITIES_KEY = "RegisteredAbilities";

    public static void useAbilityByNumber(Player player, int abilityNumber, InteractionHand hand) {
        if (player.level().isClientSide()) {
            return;
        }
        if (player.isSpectator()) {
            return;
        }

        if (BeyonderUtil.hasStun(player)) {
            player.sendSystemMessage(Component.literal("You are stunned and unable to use abilities for another " +
                            (int) (player.getPersistentData().getInt("LOTMStun")) / 20 + " seconds.")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.contains(REGISTERED_ABILITIES_KEY, Tag.TAG_COMPOUND)) {
            player.sendSystemMessage(Component.literal("No registered abilities found."));
            return;
        }

        CompoundTag registeredAbilities = persistentData.getCompound(REGISTERED_ABILITIES_KEY);
        if (!registeredAbilities.contains(String.valueOf(abilityNumber), Tag.TAG_STRING)) {
            player.sendSystemMessage(Component.literal("Ability " + abilityNumber + " not found."));
            return;
        }

        ResourceLocation resourceLocation = new ResourceLocation(registeredAbilities.getString(String.valueOf(abilityNumber)));
        Item item = ForgeRegistries.ITEMS.getValue(resourceLocation);
        if (item == null) {
            player.sendSystemMessage(Component.literal("Item not found in registry for ability " + abilityNumber +
                    " with resource location: " + resourceLocation));
            return;
        }

        String itemName = item.getDescription().getString();
        if (!(item instanceof Ability ability)) {
            player.sendSystemMessage(Component.literal("Registered ability ").append(itemName)
                    .append(" for ability number " + abilityNumber + " is not an ability.").withStyle(ChatFormatting.RED));
            return;
        }

        if (player.getCooldowns().isOnCooldown(item)) {
            player.sendSystemMessage(Component.literal("Ability ").append(itemName).append(" is on cooldown!").withStyle(ChatFormatting.RED));
            return;
        }

        // Create a dummy ItemStack for the ability to use in the ability methods
        ItemStack abilityItemStack = new ItemStack(item);

        // Check if we can use the ability - using our modified version that doesn't require holding the item
        if (!checkIfCanUseAbility(player, item)) {
            return;
        }

        // Additional check for SimpleAbilityItem - key difference is that we're not requiring the item to be in hand
        if (ability instanceof SimpleAbilityItem simpleAbility) {
            if (!simpleAbility.checkAll(player)) {
                return;
            }
        }

        double entityReach = ability.getEntityReach();
        double blockReach = ability.getBlockReach();
        boolean successfulUse = false;
        boolean hasEntityInteraction = false;
        boolean hasBlockInteraction = false;
        boolean hasGeneralAbility = false;

        // Rest of the method remains the same...
        try {
            Method entityMethod = ability.getClass().getMethod("useAbilityOnEntity", ItemStack.class, LivingEntity.class, LivingEntity.class, InteractionHand.class);
            hasEntityInteraction = !entityMethod.getDeclaringClass().equals(Ability.class);
        } catch (NoSuchMethodException ignored) {
        }

        try {
            Method blockMethod = ability.getClass().getMethod("useAbilityOnBlock", UseOnContext.class);
            hasBlockInteraction = !blockMethod.equals(Ability.class.getDeclaredMethod("useAbilityOnBlock", UseOnContext.class));
        } catch (NoSuchMethodException ignored) {
        }

        try {
            Method generalMethod = ability.getClass().getDeclaredMethod("useAbility", Level.class, LivingEntity.class, InteractionHand.class);
            hasGeneralAbility = !generalMethod.equals(Ability.class.getDeclaredMethod("useAbility", Level.class, LivingEntity.class, InteractionHand.class));
        } catch (NoSuchMethodException ignored) {
        }

        if (hasEntityInteraction) {
            Vec3 eyePosition = player.getEyePosition();
            Vec3 lookVector = player.getLookAngle();
            Vec3 reachVector = eyePosition.add(lookVector.scale(entityReach));
            AABB searchBox = player.getBoundingBox().inflate(entityReach);
            EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(player.level(), player, eyePosition, reachVector, searchBox,
                    entity -> !entity.isSpectator() && entity.isPickable(), 0.1f);

            if (entityHit != null && entityHit.getEntity() instanceof LivingEntity livingEntity) {
                if (player.level().isEmptyBlock(livingEntity.blockPosition().above())) {
                    InteractionResult result = ability.useAbilityOnEntity(abilityItemStack, player, livingEntity, hand);
                    if (result != InteractionResult.PASS) {
                        successfulUse = true;
                    }
                }
            } else {
                List<LivingEntity> possibleTargets = player.level().getEntitiesOfClass(LivingEntity.class, searchBox,
                        entity -> !entity.isSpectator() && entity.isPickable() && entity != player);

                LivingEntity bestTarget = null;
                double bestDotProduct = 0.9915;
                for (LivingEntity target : possibleTargets) {
                    Vec3 toEntity = target.getEyePosition().subtract(eyePosition).normalize();
                    double dotProduct = toEntity.dot(lookVector);
                    if (dotProduct > bestDotProduct) {
                        BlockHitResult hitResult = player.level().clip(new ClipContext(
                                eyePosition, target.getEyePosition(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

                        if (hitResult.getType() == HitResult.Type.MISS && player.level().isEmptyBlock(target.blockPosition().above())) {
                            bestTarget = target;
                            bestDotProduct = dotProduct;
                        }
                    }
                }
                if (bestTarget != null) {
                    InteractionResult result = ability.useAbilityOnEntity(abilityItemStack, player, bestTarget, hand);
                    if (result != InteractionResult.PASS) {
                        successfulUse = true;
                    }
                }
            }
        }

        // Check for block interaction
        if (!successfulUse && hasBlockInteraction) {
            Vec3 eyePosition = player.getEyePosition();
            Vec3 lookVector = player.getLookAngle();
            Vec3 reachVector = eyePosition.add(lookVector.x * blockReach, lookVector.y * blockReach, lookVector.z * blockReach);

            BlockHitResult blockHit = player.level().clip(new ClipContext(
                    eyePosition,
                    reachVector,
                    ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE,
                    player
            ));

            if (blockHit.getType() != HitResult.Type.MISS) {
                UseOnContext context = new UseOnContext(player.level(), player, hand, abilityItemStack, blockHit);
                InteractionResult result = ability.useAbilityOnBlock(context);
                if (result != InteractionResult.PASS) {
                    successfulUse = true;
                }
            }
        }

        if ((hasEntityInteraction || hasBlockInteraction) && !hasGeneralAbility) {
            if (successfulUse) {
                if (ability instanceof SimpleAbilityItem simpleAbility) {
                    simpleAbility.useSpirituality(player);
                    simpleAbility.addCooldown(player);
                }

                player.displayClientMessage(Component.literal("Used: " + itemName).withStyle(getStyle(player)), true);
            } else {
                player.displayClientMessage(Component.literal("Missed: " + itemName).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD), true);
            }
        } else if (!hasEntityInteraction && !hasBlockInteraction) {
            InteractionResult result = ability.useAbility(player.level(), player, hand);
            if (result != InteractionResult.PASS && ability instanceof SimpleAbilityItem simpleAbility) {
                //simpleAbility.useSpirituality(player);
                simpleAbility.addCooldown(player);
            }

            player.displayClientMessage(Component.literal("Used: " + itemName).withStyle(getStyle(player)), true);
        } else if (successfulUse) {
            if (ability instanceof SimpleAbilityItem simpleAbility) {
                //simpleAbility.useSpirituality(player);
                simpleAbility.addCooldown(player);
            }

            player.displayClientMessage(Component.literal("Used: " + itemName).withStyle(getStyle(player)), true);
        } else {
            InteractionResult result = ability.useAbility(player.level(), player, hand);
            if (result != InteractionResult.PASS && ability instanceof SimpleAbilityItem simpleAbility) {
                //simpleAbility.useSpirituality(player);
                simpleAbility.addCooldown(player);
            }

            player.displayClientMessage(Component.literal("Used: " + itemName).withStyle(getStyle(player)), true);
        }
    }

    public static boolean checkAll(LivingEntity living, Item item) {
        boolean itemCheckPassed = !(living instanceof Player);
        if (living instanceof Player player && player.getCooldowns().isOnCooldown(item)) {
            return false;
        } else {
            if (living.getPersistentData().getInt("abilityCooldownFor" + item.getDescription().getString()) >= 1) {
                return false;
            }
        }
        if (item instanceof SimpleAbilityItem simpleAbilityItem) {
            if (living instanceof Player) {
                itemCheckPassed = living.getItemInHand(InteractionHand.MAIN_HAND).is(item);
            }

            if (itemCheckPassed) {
                boolean checkAllResult = SimpleAbilityItem.checkAll(living, simpleAbilityItem.getRequiredPathway(), simpleAbilityItem.getRequiredSequence(), simpleAbilityItem.getRequiredSpirituality(), false);
                if (!checkAllResult) {
                    boolean sequenceAble = BeyonderUtil.sequenceAbleCopy(living);
                    if (sequenceAble) {
                        boolean abilityCopied = BeyonderUtil.checkAbilityIsCopied(living, simpleAbilityItem);
                        if (abilityCopied) {
                            BeyonderUtil.useCopiedAbility(living, simpleAbilityItem);
                            return SimpleAbilityItem.checkSpirituality(living, simpleAbilityItem.getSpirituality(), true);
                        }
                    }
                }
                boolean finalCheck = SimpleAbilityItem.checkAll(living, simpleAbilityItem.getRequiredPathway(), simpleAbilityItem.getRequiredSequence(), simpleAbilityItem.getRequiredSpirituality(), true);
                if (finalCheck) {
                    BeyonderUtil.copyAbilities(living.level(), living, simpleAbilityItem);
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean checkIfCanUseAbility(LivingEntity livingEntity, Item simpleAbilityItem) {
        if (!livingEntity.level().isClientSide()) {
            if (livingEntity instanceof Player player && player.getCooldowns().isOnCooldown(simpleAbilityItem)) {
                return false;
            } else {
                if (livingEntity.getPersistentData().getInt("abilityCooldownFor" + simpleAbilityItem.getDescription().getString()) >= 1) {
                    return false;
                }
            }
            boolean shouldntActiveCalamity = true;
            boolean allowBeyonderAbilitiesNearSpawn = livingEntity.level().getGameRules().getBoolean(GameRuleInit.SHOULD_BEYONDER_ABILITY_NEAR_SPAWN);
            if (!allowBeyonderAbilitiesNearSpawn) {
                BlockPos entityPos = livingEntity.getOnPos();
                BlockPos worldSpawnPos = livingEntity.level().getSharedSpawnPos();
                if (entityPos.closerThan(worldSpawnPos, 300)) {
                    shouldntActiveCalamity = false;
                }
            }
            if (!shouldntActiveCalamity) {
                if (livingEntity instanceof Player player) {
                    player.displayClientMessage(Component.literal("You are unable to use abilities too close to spawn").withStyle(ChatFormatting.RED), true);
                }
                return false;
            }
            MisfortuneManipulation.livingUseAbilityMisfortuneManipulation(livingEntity);
            CompoundTag tag = livingEntity.getPersistentData();
            if (livingEntity.getMainHandItem().getItem() instanceof SimpleAbilityItem) {
                if (hasStun(livingEntity)) {
                    if (livingEntity instanceof Player) {
                        livingEntity.sendSystemMessage(Component.literal("You are stunned and unable to use abilities for another " + (int) livingEntity.getPersistentData().getInt("LOTMStun") / 20 + " seconds.").withStyle(ChatFormatting.RED));
                    }
                    return false;
                } else if (tag.getInt("cantUseAbility") >= 1) {
                    tag.putInt("cantUseAbility", tag.getInt("cantUseAbility") - 1);
                    if (livingEntity instanceof Player) {
                        livingEntity.sendSystemMessage(Component.literal("How unlucky! You messed up and couldn't use your ability!").withStyle(ChatFormatting.RED));
                    }
                    return false;
                } else if (tag.getInt("unableToUseAbility") >= 1) {
                    tag.putInt("unableToUseAbility", tag.getInt("unableToUseAbility") - 1);
                    if (livingEntity instanceof Player player) {
                        player.displayClientMessage(Component.literal("You are unable to use your ability").withStyle(ChatFormatting.RED), true);
                    }
                }
            }
        }
        return true;
    }

    private static boolean tryTargetedAbility(Player player, Ability ability, InteractionHand hand, String itemName) {
        double entityReach = ability.getEntityReach();
        double blockReach = ability.getBlockReach();

        // Try entity targeting first
        Vec3 eyePosition = player.getEyePosition();
        Vec3 lookVector = player.getLookAngle();
        Vec3 entityReachVector = eyePosition.add(lookVector.x * entityReach, lookVector.y * entityReach, lookVector.z * entityReach);
        AABB searchBox = player.getBoundingBox().inflate(entityReach);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(player.level(), player, eyePosition, entityReachVector, searchBox,
                entity -> !entity.isSpectator() && entity.isPickable(), 0.0f);

        if (entityHit != null && entityHit.getEntity() instanceof LivingEntity livingEntity) {
            InteractionResult result = ability.useAbilityOnEntity(player.getItemInHand(hand), player, livingEntity, hand);
            if (result == InteractionResult.SUCCESS) {
                player.displayClientMessage(Component.literal("Used: " + itemName).withStyle(getStyle(player)), true);
                return true;
            }
        }

        // If entity targeting failed, try block targeting
        Vec3 blockReachVector = eyePosition.add(lookVector.x * blockReach, lookVector.y * blockReach, lookVector.z * blockReach);
        BlockHitResult blockHit = player.level().clip(new ClipContext(
                eyePosition,
                blockReachVector,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        ));

        if (blockHit.getType() != HitResult.Type.MISS) {
            UseOnContext context = new UseOnContext(player.level(), player, hand, player.getItemInHand(hand), blockHit);
            InteractionResult result = ability.useAbilityOnBlock(context);
            if (result == InteractionResult.SUCCESS) {
                player.displayClientMessage(Component.literal("Used: " + itemName).withStyle(getStyle(player)), true);
                return true;
            }
        }

        return false;
    }

    public static Style getStyle(LivingEntity livingEntity) {
        BeyonderClass pathway = getPathway(livingEntity);
        if (pathway != null) {
            return Style.EMPTY.withBold(true).withColor(pathway.getColorFormatting());
        }
        return Style.EMPTY;
    }

    public static void mentalDamage(Player source, Player hurtEntity, int damage) { //can make it so that with useOn, sets shiftKeyDown to true for player
        BeyonderHolder sourceHolder = BeyonderHolderAttacher.getHolderUnwrap(source);
        BeyonderHolder hurtHolder = BeyonderHolderAttacher.getHolderUnwrap(hurtEntity);
        float x = Math.min(damage, damage * (hurtHolder.getMentalStrength() / sourceHolder.getMentalStrength()));
        hurtEntity.hurt(hurtEntity.damageSources().magic(), x);
    }

    public static float mentalInt(Player source, Player hurtEntity, int mentalInt) {
        BeyonderHolder sourceHolder = BeyonderHolderAttacher.getHolderUnwrap(source);
        BeyonderHolder hurtHolder = BeyonderHolderAttacher.getHolderUnwrap(hurtEntity);
        float x = Math.min(mentalInt, mentalInt * (hurtHolder.getMentalStrength() / sourceHolder.getMentalStrength()));
        return x;
    }

    //TICK EVENT
    public static void abilityCooldownsServerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (!player.level().isClientSide()) {
            if (player instanceof ServerPlayer serverPlayer) {
                Map<String, Integer> cooldowns = new HashMap<>();
                CompoundTag tag = serverPlayer.getPersistentData();
                if (tag.contains(AbilityRegisterCommand.REGISTERED_ABILITIES_KEY, Tag.TAG_COMPOUND)) {
                    CompoundTag registeredAbilities = tag.getCompound(AbilityRegisterCommand.REGISTERED_ABILITIES_KEY);
                    for (String combinationNumber : registeredAbilities.getAllKeys()) {
                        String abilityResourceLocationString = registeredAbilities.getString(combinationNumber);
                        ResourceLocation resourceLocation = new ResourceLocation(abilityResourceLocationString);
                        Item item = ForgeRegistries.ITEMS.getValue(resourceLocation);
                        if (item instanceof SimpleAbilityItem simpleAbilityItem) {
                            String combination = AbilityRegisterCommand.findCombinationForNumber(Integer.parseInt(combinationNumber));
                            if (!combination.isEmpty()) {
                                if (player.getCooldowns().isOnCooldown(item)) {
                                    float cooldownPercent = player.getCooldowns().getCooldownPercent(item, 0.0F);
                                    if (cooldownPercent > 0) {
                                        int totalCooldown = simpleAbilityItem.getCooldown();
                                        int remainingCooldown = (int) (totalCooldown * cooldownPercent);
                                        cooldowns.put(combination, remainingCooldown);
                                    } else {
                                        cooldowns.put(combination, 0);
                                    }
                                } else {
                                    cooldowns.put(combination, 0);
                                }
                            }
                        }
                    }
                }
                SyncAbilityCooldownsS2C syncPacket = new SyncAbilityCooldownsS2C(cooldowns);
                LOTMNetworkHandler.sendToPlayer(syncPacket, serverPlayer);
            }
        }
    }


    public static int getCoordinateAtLeastAway(int centerCoord, int minDistance, int maxDistance) {
        Random random = new Random();
        int offset = random.nextInt(maxDistance - minDistance + 1) + minDistance;
        return random.nextBoolean() ? centerCoord + offset : centerCoord - offset;
    }

    public static void setCooldown(ServerPlayer player, int cooldown) {
        player.getPersistentData().putInt("leftClickCooldown", cooldown);
        LOTMNetworkHandler.sendToPlayer(new SyncLeftClickCooldownS2C(cooldown), player);
    }

    public static int getCooldown(ServerPlayer player) {
        return player.getPersistentData().getInt("leftClickCooldown");
    }

    private static void handleEmptyLeftClickSkill(LeftClickHandlerSkill item) {
        LOTMNetworkHandler.sendToServer(item.getleftClickEmpty());
    }

    private static void handleEmptyLeftClickItem(LeftClickHandlerSword item) {
        LOTMNetworkHandler.sendToServer(item.getleftClickEmpty());
    }

    private static void handleEmptyLeftClickSkill(LeftClickHandlerSkillP item, Integer slot) {
        LOTMNetworkHandler.sendToServer(item.getleftClickEmpty(slot));
    }

    public static void leftClick(Player pPlayer) {
        ItemStack heldItem = pPlayer.getMainHandItem();
        Item item = heldItem.getItem();
        int activeSlot = pPlayer.getInventory().selected;

        if (ClientLeftclickCooldownData.getCooldown() > 0)
            return;

        LOTMNetworkHandler.sendToServer(new RequestCooldownSetC2S());
        if (!heldItem.isEmpty()) {
            if (item instanceof LeftClickHandlerSkill neededItem) {
                handleEmptyLeftClickSkill(neededItem);
            } else if(item instanceof LeftClickHandlerSkillP neededItem) {
                handleEmptyLeftClickSkill(neededItem, activeSlot);
            } else if (item instanceof LeftClickHandlerSword neededItem) {
                handleEmptyLeftClickItem(neededItem);
            } if (heldItem.getItem() instanceof DeathKnell) {
                LOTMNetworkHandler.sendToServer(new DeathKnellLeftClickC2S());
            }
        }
    }


    public static void requestCooldown() {
        LOTMNetworkHandler.sendToServer(new RequestCooldownSetC2S());
    }

    public static void spawnParticlesInSphere(ServerLevel level, double x, double y, double z, int maxRadius, int maxParticles, float xSpeed, float ySpeed, float zSpeed, ParticleOptions particle) {
        for (int i = 0; i < maxParticles; i++) {
            double dx = level.random.nextGaussian() * maxRadius;
            double dy = level.random.nextGaussian() * 2;
            double dz = level.random.nextGaussian() * maxRadius;
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance < maxRadius) {
                double density = 1.0 - (distance / maxRadius);
                if (level.random.nextDouble() < density) {
                    level.sendParticles(particle, x + dx, y + dy, z + dz, 0, xSpeed, ySpeed, zSpeed, 1);
                }
            }
        }
    }

    public static void applyMobEffect(LivingEntity pPlayer, MobEffect mobEffect, int duration, int amplifier, boolean ambient, boolean visible) {
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

    public static boolean isBeyonderCapable(LivingEntity living) { //marked
        return getPathway(living) != null;
    }

    public static @Nullable BeyonderClass getPathway(LivingEntity living) { //marked
        if (!living.level().isClientSide()) {
            if (living instanceof Player player) {
                BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
                return holder.getCurrentClass();
            } else if (living instanceof PlayerMobEntity playerMobEntity) {
                return playerMobEntity.getCurrentPathway();
            } else {
                if (living.level() instanceof ServerLevel serverLevel) {
                    BeyonderEntityData mappingData = BeyonderEntityData.getInstance(serverLevel);
                    String pathwayString = mappingData.getStringForEntity(living.getType());
                    if (pathwayString != null) {
                        String lowerPathway = pathwayString.toLowerCase();
                        for (BeyonderClass beyonderClass : BeyonderClassInit.getRegistry()) {
                            for (String sequenceName : beyonderClass.sequenceNames()) {
                                if (lowerPathway.equalsIgnoreCase(sequenceName.toLowerCase())) {
                                    return beyonderClass;
                                }
                            }
                        }
                    }
                }
                //String pathwayName = living.getPersistentData().getString("separateEntityPathway");
                //return getPathwayByName(pathwayName);
            }
        }
        return null;
    }

    public static void setPathway(LivingEntity living, BeyonderClass pathway) {
        if (living instanceof Player player) {
            BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
            holder.setPathway(pathway);
        } else if (living instanceof PlayerMobEntity playerMobEntity) {
            playerMobEntity.setPathway(pathway);
        } else {
            //living.getPersistentData().putString("separateEntityPathway", getPathwayName(pathway));
        }
    }


    public static int getSequence(LivingEntity living) { //marked
        if (living != null) {
            if (living instanceof PlayerMobEntity playerMobEntity) {
                return playerMobEntity.getCurrentSequence();
            } else if (living.level() instanceof ServerLevel serverLevel && !(living instanceof Player)) {
                BeyonderEntityData mappingData = BeyonderEntityData.getInstance(serverLevel);
                String pathwayString = mappingData.getStringForEntity(living.getType());
                if (pathwayString != null) {
                    String lowerPathway = pathwayString.toLowerCase();
                    BeyonderClass beyonderClass = getPathway(living);
                    if (beyonderClass != null) {
                        List<String> sequenceNames = beyonderClass.sequenceNames();
                        for (int i = 0; i < sequenceNames.size(); i++) {
                            if (lowerPathway.equalsIgnoreCase(sequenceNames.get(i).toLowerCase())) {
                                return i;
                            }
                        }
                    }
                }
            } else if (living instanceof Player player) {
                BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
                return holder.getSequence();
            }
            if (living.getPersistentData().contains("separateEntitySequence")) {
                //return living.getPersistentData().getInt("separateEntitySequence");
            }
            float maxHp = living.getMaxHealth();
            if (maxHp <= 20) {
                return 9;
            } else if (maxHp <= 35) {
                return 8;
            } else if (maxHp <= 70) {
                return 7;
            } else if (maxHp <= 120) {
                return 6;
            } else if (maxHp <= 190) {
                return 5;
            } else if (maxHp <= 300) {
                return 4;
            } else if (maxHp <= 450) {
                return 3;
            } else if (maxHp <= 700) {
                return 2;
            } else if (maxHp <= 999) {
                return 1;
            } else if (maxHp >= 1000) {
                return 0;
            } else {
                return -1;
            }
        } else {
            return 10;
        }
    }

    public static int getCooldownsForAbility(LivingEntity livingEntity, Item ability) {
        int cooldown = 0;
        for (Item item : getAbilities(livingEntity)) {
            if (item == ability && item instanceof SimpleAbilityItem simpleAbilityItem) {
                if (livingEntity instanceof Player player) {
                    int currentCooldownPercent = (int) (player.getCooldowns().getCooldownPercent(item, 0) * 100);
                    cooldown = simpleAbilityItem.getCooldown() * currentCooldownPercent / 100;
                }
            }
        }
        return cooldown;
    }

    public static Map<Item, Float> getDamage(LivingEntity livingEntity) {
        Map<Item, Float> damageMap = new HashMap<>(1000);
        Level level = livingEntity.level();

        int enhancement = 1;
        if (level instanceof ServerLevel serverLevel) {
            enhancement = CalamityEnhancementData.getInstance(serverLevel).getCalamityEnhancement();
        }

        int dreamIntoReality = getDreamIntoReality(livingEntity);

        float abilityStrengthened = 1;
        if (livingEntity.getPersistentData().getInt("abilityStrengthened") >= 1) {
            abilityStrengthened = 2;
        }

        int abilityWeakness = 1;
        if (livingEntity.hasEffect(ModEffects.ABILITY_WEAKNESS.get())) {
            abilityWeakness = Math.max(1, (Objects.requireNonNull(livingEntity.
                    getEffect(ModEffects.ABILITY_WEAKNESS.get()))).getAmplifier());
        }

        if (livingEntity.getPersistentData().getBoolean("planeswalkerSymbolization")) {
            abilityWeakness *= 2;
        }

        int sequence = BeyonderUtil.getSequence(livingEntity);

        //SPECTATOR
        damageMap.put(ItemInit.APPLY_MANIPULATION.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.AWE.get(), applyAbilityStrengthened((285.0f - (sequence * 22.5f)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.BATTLE_HYPNOTISM.get(), applyAbilityStrengthened((600.0f - (sequence * 30)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.CONSCIOUSNESS_STROLL.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));
        damageMap.put(ItemInit.DISCERN.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));
        damageMap.put(ItemInit.DRAGON_BREATH.get(), applyAbilityStrengthened((float) ((90.0f * dreamIntoReality) - (sequence * 6)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.DREAM_INTO_REALITY.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.DREAM_WALKING.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));
        damageMap.put(ItemInit.DREAM_WEAVING.get(), applyAbilityStrengthened((30.0f - (sequence * 4.5f)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.ENVISION_BARRIER.get(), applyAbilityStrengthened((151.5f - (sequence * 30)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.ENVISION_DEATH.get(), applyAbilityStrengthened((float) ((20.0f + (dreamIntoReality * 5f)) - (sequence * 15)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.ENVISION_HEALTH.get(), applyAbilityStrengthened((float) (0.99f - (sequence * 0.075f) + (dreamIntoReality * 0.075f)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.ENVISION_KINGDOM.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));
        damageMap.put(ItemInit.ENVISION_LIFE.get(), applyAbilityStrengthened((4.5f + (sequence * 1.5f)) * abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.ENVISION_LOCATION.get(), applyAbilityStrengthened((float) (750.0f / dreamIntoReality) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.ENVISION_WEATHER.get(), applyAbilityStrengthened((float) (750.0f / dreamIntoReality) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.FRENZY.get(), applyAbilityStrengthened((float) ((22.5f - sequence * 1.5f) * dreamIntoReality) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MANIPULATE_MOVEMENT.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MANIPULATE_EMOTION.get(), applyAbilityStrengthened((225.0f - (sequence * 30)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MANIPULATE_FONDNESS.get(), applyAbilityStrengthened((float) (900.0f * dreamIntoReality) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MENTAL_PLAGUE.get(), applyAbilityStrengthened((float) (300.0f / dreamIntoReality) * abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MIND_READING.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));
        damageMap.put(ItemInit.MIND_STORM.get(), applyAbilityStrengthened((45.0f - (sequence * 3)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.NIGHTMARE.get(), applyAbilityStrengthened((60.0f - (sequence * 3)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.PLACATE.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));
        damageMap.put(ItemInit.PLAGUE_STORM.get(), applyAbilityStrengthened((float) ((18.0f * dreamIntoReality) - (sequence * 2.25f)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.PROPHECY.get(), applyAbilityStrengthened((float) (12.0f + (dreamIntoReality * 3) - (sequence * 6)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.PSYCHOLOGICAL_INVISIBILITY.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));

        //SAILOR
        damageMap.put(ItemInit.ACIDIC_RAIN.get(), applyAbilityStrengthened((75.0f - (sequence * 10.5f)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.AQUATIC_LIFE_MANIPULATION.get(), applyAbilityStrengthened((75.0f - (sequence * 7.5f)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.AQUEOUS_LIGHT_PUSH.get(), applyAbilityStrengthened((12.0f - sequence * 1.5f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.AQUEOUS_LIGHT_PULL.get(), applyAbilityStrengthened((12.0f - sequence * 1.5f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.AQUEOUS_LIGHT_DROWN.get(), applyAbilityStrengthened((12.0f - sequence * 1.5f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.CALAMITY_INCARNATION_TORNADO.get(), applyAbilityStrengthened((450.0f - (75 * sequence)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.CALAMITY_INCARNATION_TSUNAMI.get(), applyAbilityStrengthened((300.0f - (45 * sequence)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.EARTHQUAKE.get(), applyAbilityStrengthened((112.5f - (sequence * 9)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.ENABLE_OR_DISABLE_LIGHTNING.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.EXTREME_COLDNESS.get(), applyAbilityStrengthened((225.0f - (sequence * 30)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.HURRICANE.get(), applyAbilityStrengthened((900.0f - (sequence * 150)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.LIGHTNING_BALL.get(), applyAbilityStrengthened((15.0f + (15 - sequence * 4.5f)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.LIGHTNING_BALL_ABSORB.get(), applyAbilityStrengthened((15.0f + (15 - sequence * 4.5f)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.LIGHTNING_BRANCH.get(), applyAbilityStrengthened((100.0f - (sequence * 10.0f)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.LIGHTNING_REDIRECTION.get(), applyAbilityStrengthened((187.5f - (sequence * 90)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.LIGHTNING_STORM.get(), applyAbilityStrengthened((750.0f - (sequence * 120)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MATTER_ACCELERATION_BLOCKS.get(), applyAbilityStrengthened((15.0f - sequence * 1.5f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MATTER_ACCELERATION_ENTITIES.get(), applyAbilityStrengthened((200.0f - (sequence * 60)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MATTER_ACCELERATION_SELF.get(), applyAbilityStrengthened((180.0f - (sequence * 45)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.RAGING_BLOWS.get(), applyAbilityStrengthened((15.0f - (sequence * 1.5f)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.RAIN_EYES.get(), applyAbilityStrengthened((750.0f - (sequence * 75)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.ROAR.get(), applyAbilityStrengthened((15.0f - sequence * 1.5f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.SAILOR_LIGHTNING.get(), applyAbilityStrengthened((170.0f - (27 * sequence)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.SAILOR_LIGHTNING_TRAVEL.get(), applyAbilityStrengthened((600.0f - (sequence * 225)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.SAILORPROJECTILECTONROL.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.SIREN_SONG_HARM.get(), applyAbilityStrengthened((150.0f - (sequence * 18)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.SIREN_SONG_WEAKEN.get(), applyAbilityStrengthened((150.0f - (sequence * 18)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.SIREN_SONG_STRENGTHEN.get(), applyAbilityStrengthened((31.5f - sequence * 1.5f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.SIREN_SONG_STUN.get(), applyAbilityStrengthened((150.0f - (sequence * 18)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.SONIC_BOOM.get(), applyAbilityStrengthened((60.0f - (sequence * 7.5f)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.STAR_OF_LIGHTNING.get(), applyAbilityStrengthened((187.5f - sequence * 30) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.STORM_SEAL.get(), applyAbilityStrengthened((4.5f - sequence * 1.5f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.THUNDER_CLAP.get(), applyAbilityStrengthened((450.0f - (sequence * 75)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TORNADO.get(), applyAbilityStrengthened((225.0f - (sequence * 45)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TSUNAMI.get(), applyAbilityStrengthened((900.0f - (sequence * 120)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TSUNAMI_SEAL.get(), applyAbilityStrengthened((900.0f - (sequence * 120)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TYRANNY.get(), applyAbilityStrengthened((375.0f - (sequence * 120)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.VOLCANIC_ERUPTION.get(), applyAbilityStrengthened((180.0f - (sequence * 15)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.WATER_COLUMN.get(), applyAbilityStrengthened((300.0f - (sequence * 90)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.WATER_SPHERE.get(), applyAbilityStrengthened((300.0f - (sequence * 30)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.WIND_MANIPULATION_BLADE.get(), applyAbilityStrengthened((10.5f - sequence * 1.5f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.WIND_MANIPULATION_FLIGHT.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));
        damageMap.put(ItemInit.WIND_MANIPULATION_SENSE.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));

        // MONSTER
        damageMap.put(ItemInit.AURAOFCHAOS.get(), applyAbilityStrengthened((375.0f - (sequence * 75) + (enhancement * 75)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.CHAOSAMPLIFICATION.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));
        damageMap.put(ItemInit.CHAOSWALKERCOMBAT.get(), applyAbilityStrengthened(((float) Math.max(75, 300 - (sequence * 52.5f))) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.CYCLEOFFATE.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));
        damageMap.put(ItemInit.DECAYDOMAIN.get(), applyAbilityStrengthened((375.0f - (sequence * 67.5f)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.PROVIDENCEDOMAIN.get(), applyAbilityStrengthened((375.0f - (sequence * 67.5f)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.ENABLEDISABLERIPPLE.get(), applyAbilityStrengthened((225.0f - (sequence * 30)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.FALSEPROPHECY.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));
        damageMap.put(ItemInit.FATEDCONNECTION.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));
        damageMap.put(ItemInit.FATEREINCARNATION.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));
        damageMap.put(ItemInit.FORTUNEAPPROPIATION.get(), applyAbilityStrengthened((300.0f - (sequence * 60)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.LUCKCHANNELING.get(), applyAbilityStrengthened((150.0f - (sequence * 37.5f)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.LUCKDENIAL.get(), applyAbilityStrengthened((2700.0f - (sequence * 225)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.LUCKDEPRIVATION.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.LUCKFUTURETELLING.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.LUCKGIFTING.get(), applyAbilityStrengthened((151.5f - (sequence * 7.5f)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.LUCK_MANIPULATION.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.LUCKPERCEPTION.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MISFORTUNEBESTOWAL.get(), applyAbilityStrengthened((90.0f - (sequence * 10.5f) + (enhancement * 15)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MISFORTUNEIMPLOSION.get(), applyAbilityStrengthened((375.0f - (sequence * 150) + (enhancement * 75)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MISFORTUNEMANIPULATION.get(), applyAbilityStrengthened((22.5f - sequence * 3) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MISFORTUNEREDIRECTION.get(), applyAbilityStrengthened((450.0f - (sequence * 75)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.CALAMITYINCARNATION.get(), applyAbilityStrengthened((12.0f - sequence * 1.5f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MONSTERDANGERSENSE.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MONSTERCALAMITYATTRACTION.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MONSTERDOMAINTELEPORATION.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MONSTERPROJECTILECONTROL.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MONSTERREBOOT.get(), applyAbilityStrengthened((100.0f - (sequence * 10)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.PROBABILITYFORTUNE.get(), applyAbilityStrengthened((300.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.PROBABILITYEFFECT.get(), applyAbilityStrengthened((300.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.PROBABILITYINFINITEFORTUNE.get(), applyAbilityStrengthened((3000.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.PROBABILITYINFINITEMISFORTUNE.get(), applyAbilityStrengthened((3000.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.PROBABILITYMISFORTUNE.get(), applyAbilityStrengthened((300.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.PROBABILITYWIPE.get(), applyAbilityStrengthened((300.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.PROBABILITYFORTUNEINCREASE.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.PROBABILITYMISFORTUNEINCREASE.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.PSYCHESTORM.get(), applyAbilityStrengthened((60.0f - (sequence * 5f)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.REBOOTSELF.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.SPIRITVISION.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.WHISPEROFCORRUPTION.get(), applyAbilityStrengthened(((float) sequence * 1.5f) / abilityWeakness, abilityStrengthened));

        // WARRIOR
        damageMap.put(ItemInit.GIGANTIFICATION.get(), applyAbilityStrengthened((10.0f - sequence * 1.5f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.SWORDOFDAWN.get(), applyAbilityStrengthened((225.0f - (sequence * 22.5f)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.SWORDOFSILVER.get(), applyAbilityStrengthened((300.0f - (sequence * 30)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TWILIGHTSWORD.get(), applyAbilityStrengthened((600.0f - (sequence * 150)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.DAWNARMORY.get(), applyAbilityStrengthened((225.0f - (sequence * 22.5f)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.SPEAROFDAWN.get(), applyAbilityStrengthened((15.0f - (sequence * 1.5f)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.DAWNWEAPONRY.get(), applyAbilityStrengthened((225.0f - (sequence * 22.5f)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.SILVERARMORY.get(), applyAbilityStrengthened((225.0f - (sequence * 22.5f)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.LIGHTOFDAWN.get(), applyAbilityStrengthened((150.0f - (sequence * 15)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.AURAOFGLORY.get(), applyAbilityStrengthened((90.0f - (sequence * 15)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TWILIGHTMANIFESTATION.get(), applyAbilityStrengthened((300.0f - (sequence * 150)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TWILIGHTFREEZE.get(), applyAbilityStrengthened((900.0f - (sequence * 150)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TWILIGHTACCELERATE.get(), applyAbilityStrengthened((2700.0f - (sequence * 450)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.AURAOFTWILIGHT.get(), applyAbilityStrengthened((150.0f - (sequence * 30)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MERCURYLIQUEFICATION.get(), applyAbilityStrengthened((22.5f - (sequence * 3)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.DIVINEHANDRIGHT.get(), applyAbilityStrengthened((15.0f - (sequence * 6)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.DIVINEHANDLEFT.get(), applyAbilityStrengthened((15.0f - (sequence * 6)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.SILVERRAPIER.get(), applyAbilityStrengthened((60.0f - (sequence * 9)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MERCURYCAGE.get(), applyAbilityStrengthened((750.0f - (sequence * 120)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.GLOBEOFTWILIGHT.get(), applyAbilityStrengthened((30.0f - (sequence * 12)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TWILIGHTLIGHT.get(), applyAbilityStrengthened((450.0f - (sequence * 90)) / abilityWeakness, abilityStrengthened));

        // APPRENTICE
        damageMap.put(ItemInit.BLINK.get(), applyAbilityStrengthened(1.0f * abilityWeakness + (sequence * 0.125f), -abilityStrengthened));
        damageMap.put(ItemInit.BLINKAFTERIMAGE.get(), applyAbilityStrengthened(2.5f - (sequence * 0.15f) - abilityWeakness, -abilityStrengthened));
        damageMap.put(ItemInit.BLINK_STATE.get(), applyAbilityStrengthened(0.1f + (sequence * 0.2f) + abilityWeakness, -abilityStrengthened));
        damageMap.put(ItemInit.CREATE_CONCEALED_BUNDLE.get(), applyAbilityStrengthened(1.0f * abilityWeakness + (sequence * 0.125f), -abilityStrengthened));
        damageMap.put(ItemInit.CREATE_CONCEALED_SPACE.get(), applyAbilityStrengthened(1.0f * abilityWeakness + (sequence * 0.125f), -abilityStrengthened));
        damageMap.put(ItemInit.CREATEDOOR.get(), applyAbilityStrengthened(1.0f * abilityWeakness + (sequence * 0.05f), -abilityStrengthened));
        damageMap.put(ItemInit.DIMENSIONAL_SIGHT.get(), applyAbilityStrengthened((1000.0f - sequence * 200) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.DOOR_MIRAGE.get(), applyAbilityStrengthened((50.0f + (sequence * 10)) * abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.EXILE.get(), applyAbilityStrengthened((80.0f - ((sequence * 15) * abilityWeakness)), abilityStrengthened));
        damageMap.put(ItemInit.INVISIBLEHAND.get(), applyAbilityStrengthened((float) (75 - (sequence * 12)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MINIATURIZE.get(), applyAbilityStrengthened(1.0f * abilityWeakness + (sequence * 0.125f), abilityStrengthened));
        damageMap.put(ItemInit.RECORDSCRIBE.get(), applyAbilityStrengthened(1.0f * abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.SPATIAL_CAGE.get(), applyAbilityStrengthened((400.0f - sequence * 100) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.SEPARATE_WORM_OF_STAR.get(), applyAbilityStrengthened(1.0f * abilityWeakness, -abilityStrengthened));
        damageMap.put(ItemInit.SEALING.get(), applyAbilityStrengthened((1200.0f - sequence * 200) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.SPATIAL_TEARING.get(), applyAbilityStrengthened((600 - sequence * 100.0f) / abilityWeakness, -abilityStrengthened));
        damageMap.put(ItemInit.SPACE_FRAGMENTATION.get(), applyAbilityStrengthened((75 - sequence * 15.0f) / abilityWeakness, -abilityStrengthened));
        damageMap.put(ItemInit.SYMBOLIZATION.get(), applyAbilityStrengthened((160.0f - sequence * 30) / abilityWeakness, -abilityStrengthened));
        damageMap.put(ItemInit.TELEPORTATION.get(), applyAbilityStrengthened((1.0f - (sequence * 0.015f)) * abilityWeakness, -abilityStrengthened));
        damageMap.put(ItemInit.TRAVELERSDOOR.get(), applyAbilityStrengthened(1.0f + (sequence * 0.1f) * abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TRAVELERSDOORHOME.get(), applyAbilityStrengthened(1.0f * abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TRICKBURNING.get(), applyAbilityStrengthened((200.0f - sequence * 20) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TRICKELECTRICSHOCK.get(), applyAbilityStrengthened((15.0f - sequence) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TRICKESCAPETRICK.get(), applyAbilityStrengthened(15.0f - (sequence + abilityWeakness), abilityStrengthened));
        damageMap.put(ItemInit.TRICKFLASH.get(), applyAbilityStrengthened(200.0f - (sequence * 20.0f * abilityWeakness), abilityStrengthened));
        damageMap.put(ItemInit.TRICKFOG.get(), applyAbilityStrengthened((30.0f - sequence * 3) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TRICKFREEZING.get(), applyAbilityStrengthened((70.0f - (sequence * 10f)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TRICKLOUDNOISE.get(), applyAbilityStrengthened((300.0f - (270.0f * (8.0f / sequence))) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TRICKTELEKENISIS.get(), applyAbilityStrengthened((75.0f - (sequence * 9.0f)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TRICKTUMBLE.get(), applyAbilityStrengthened((120.0f - (sequence * 13.5f)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TRICKWIND.get(), applyAbilityStrengthened((150 - (sequence * 15.0f)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.GRAVITY_MANIPULATION.get(), applyAbilityStrengthened((500.0f - sequence * 150) / abilityWeakness, abilityStrengthened));
        return damageMap;
    }

    public static List<Item> getAbilities() {
        List<Item> abilityNames = new ArrayList<>();
        //Spectator
        abilityNames.add(ItemInit.MIND_READING.get());
        abilityNames.add(ItemInit.AWE.get());
        abilityNames.add(ItemInit.FRENZY.get());
        abilityNames.add(ItemInit.PLACATE.get());
        abilityNames.add(ItemInit.PSYCHOLOGICAL_INVISIBILITY.get());
        abilityNames.add(ItemInit.BATTLE_HYPNOTISM.get());
        abilityNames.add(ItemInit.GUIDANCE.get());
        abilityNames.add(ItemInit.ALTERATION.get());
        abilityNames.add(ItemInit.NIGHTMARE.get());
        abilityNames.add(ItemInit.DREAM_WALKING.get());
        abilityNames.add(ItemInit.FRENZY.get());
        abilityNames.add(ItemInit.APPLY_MANIPULATION.get());
        abilityNames.add(ItemInit.MANIPULATE_MOVEMENT.get());
        abilityNames.add(ItemInit.MANIPULATE_FONDNESS.get());
        abilityNames.add(ItemInit.MANIPULATE_EMOTION.get());
        abilityNames.add(ItemInit.MENTAL_PLAGUE.get());
        abilityNames.add(ItemInit.MIND_STORM.get());
        abilityNames.add(ItemInit.DRAGON_BREATH.get());
        abilityNames.add(ItemInit.CONSCIOUSNESS_STROLL.get());
        abilityNames.add(ItemInit.PLAGUE_STORM.get());
        abilityNames.add(ItemInit.DREAM_WEAVING.get());
        abilityNames.add(ItemInit.DISCERN.get());
        abilityNames.add(ItemInit.DREAM_INTO_REALITY.get());
        abilityNames.add(ItemInit.PROPHECY.get());
        abilityNames.add(ItemInit.ENVISION_BARRIER.get());
        abilityNames.add(ItemInit.ENVISION_DEATH.get());
        abilityNames.add(ItemInit.ENVISION_HEALTH.get());
        abilityNames.add(ItemInit.ENVISION_KINGDOM.get());
        abilityNames.add(ItemInit.ENVISION_LIFE.get());
        abilityNames.add(ItemInit.ENVISION_LOCATION.get());
        abilityNames.add(ItemInit.ENVISION_WEATHER.get());

        //Sailor
        abilityNames.add(ItemInit.RAGING_BLOWS.get());
        abilityNames.add(ItemInit.ENABLE_OR_DISABLE_LIGHTNING.get());
        abilityNames.add(ItemInit.AQUEOUS_LIGHT_PUSH.get());
        abilityNames.add(ItemInit.AQUEOUS_LIGHT_PULL.get());
        abilityNames.add(ItemInit.AQUEOUS_LIGHT_DROWN.get());
        abilityNames.add(ItemInit.SAILORPROJECTILECTONROL.get());
        abilityNames.add(ItemInit.WIND_MANIPULATION_BLADE.get());
        abilityNames.add(ItemInit.WIND_MANIPULATION_FLIGHT.get());
        abilityNames.add(ItemInit.WIND_MANIPULATION_SENSE.get());
        abilityNames.add(ItemInit.SAILOR_LIGHTNING.get());
        abilityNames.add(ItemInit.SIREN_SONG_HARM.get());
        abilityNames.add(ItemInit.SIREN_SONG_STRENGTHEN.get());
        abilityNames.add(ItemInit.SIREN_SONG_WEAKEN.get());
        abilityNames.add(ItemInit.SIREN_SONG_STUN.get());
        abilityNames.add(ItemInit.ACIDIC_RAIN.get());
        abilityNames.add(ItemInit.WATER_SPHERE.get());
        abilityNames.add(ItemInit.TSUNAMI.get());
        abilityNames.add(ItemInit.TSUNAMI_SEAL.get());
        abilityNames.add(ItemInit.HURRICANE.get());
        abilityNames.add(ItemInit.TORNADO.get());
        abilityNames.add(ItemInit.EARTHQUAKE.get());
        abilityNames.add(ItemInit.ROAR.get());
        abilityNames.add(ItemInit.AQUATIC_LIFE_MANIPULATION.get());
        abilityNames.add(ItemInit.LIGHTNING_STORM.get());
        abilityNames.add(ItemInit.LIGHTNING_BRANCH.get());
        abilityNames.add(ItemInit.SONIC_BOOM.get());
        abilityNames.add(ItemInit.THUNDER_CLAP.get());
        abilityNames.add(ItemInit.RAIN_EYES.get());
        abilityNames.add(ItemInit.VOLCANIC_ERUPTION.get());
        abilityNames.add(ItemInit.EXTREME_COLDNESS.get());
        abilityNames.add(ItemInit.LIGHTNING_BALL.get());
        abilityNames.add(ItemInit.LIGHTNING_BALL_ABSORB.get());
        abilityNames.add(ItemInit.SAILOR_LIGHTNING_TRAVEL.get());
        abilityNames.add(ItemInit.STAR_OF_LIGHTNING.get());
        abilityNames.add(ItemInit.LIGHTNING_REDIRECTION.get());
        abilityNames.add(ItemInit.STORM_SEAL.get());
        abilityNames.add(ItemInit.WATER_COLUMN.get());
        abilityNames.add(ItemInit.MATTER_ACCELERATION_SELF.get());
        abilityNames.add(ItemInit.MATTER_ACCELERATION_BLOCKS.get());
        abilityNames.add(ItemInit.MATTER_ACCELERATION_ENTITIES.get());
        abilityNames.add(ItemInit.TYRANNY.get());

        //Monster
        abilityNames.add(ItemInit.SPIRITVISION.get());
        abilityNames.add(ItemInit.MONSTERDANGERSENSE.get());
        abilityNames.add(ItemInit.MONSTERPROJECTILECONTROL.get());
        abilityNames.add(ItemInit.LUCKPERCEPTION.get());
        abilityNames.add(ItemInit.PSYCHESTORM.get());
        abilityNames.add(ItemInit.LUCK_MANIPULATION.get());
        abilityNames.add(ItemInit.LUCKDEPRIVATION.get());
        abilityNames.add(ItemInit.LUCKGIFTING.get());
        abilityNames.add(ItemInit.MISFORTUNEBESTOWAL.get());
        abilityNames.add(ItemInit.LUCKFUTURETELLING.get());
        abilityNames.add(ItemInit.DECAYDOMAIN.get());
        abilityNames.add(ItemInit.PROVIDENCEDOMAIN.get());
        abilityNames.add(ItemInit.LUCKCHANNELING.get());
        abilityNames.add(ItemInit.LUCKDENIAL.get());
        abilityNames.add(ItemInit.MISFORTUNEMANIPULATION.get());
        abilityNames.add(ItemInit.MONSTERCALAMITYATTRACTION.get());
        abilityNames.add(ItemInit.CALAMITYINCARNATION.get());
        abilityNames.add(ItemInit.ENABLEDISABLERIPPLE.get());
        abilityNames.add(ItemInit.AURAOFCHAOS.get());
        abilityNames.add(ItemInit.CHAOSWALKERCOMBAT.get());
        abilityNames.add(ItemInit.MISFORTUNEREDIRECTION.get());
        abilityNames.add(ItemInit.MONSTERDOMAINTELEPORATION.get());
        abilityNames.add(ItemInit.WHISPEROFCORRUPTION.get());
        abilityNames.add(ItemInit.FORTUNEAPPROPIATION.get());
        abilityNames.add(ItemInit.FALSEPROPHECY.get());
        abilityNames.add(ItemInit.MISFORTUNEIMPLOSION.get());
        abilityNames.add(ItemInit.MONSTERREBOOT.get());
        abilityNames.add(ItemInit.FATEREINCARNATION.get());
        abilityNames.add(ItemInit.CYCLEOFFATE.get());
        abilityNames.add(ItemInit.CHAOSAMPLIFICATION.get());
        abilityNames.add(ItemInit.FATEDCONNECTION.get());
        abilityNames.add(ItemInit.REBOOTSELF.get());
        abilityNames.add(ItemInit.PROBABILITYMISFORTUNEINCREASE.get());
        abilityNames.add(ItemInit.PROBABILITYFORTUNEINCREASE.get());
        abilityNames.add(ItemInit.PROBABILITYFORTUNE.get());
        abilityNames.add(ItemInit.PROBABILITYMISFORTUNE.get());
        abilityNames.add(ItemInit.PROBABILITYWIPE.get());
        abilityNames.add(ItemInit.PROBABILITYEFFECT.get());
        abilityNames.add(ItemInit.PROBABILITYINFINITEFORTUNE.get());
        abilityNames.add(ItemInit.PROBABILITYINFINITEMISFORTUNE.get());

        //Warrior
        abilityNames.add(ItemInit.GIGANTIFICATION.get());
        abilityNames.add(ItemInit.LIGHTOFDAWN.get());
        abilityNames.add(ItemInit.DAWNARMORY.get());
        abilityNames.add(ItemInit.DAWNWEAPONRY.get());
        abilityNames.add(ItemInit.ENABLEDISABLEPROTECTION.get());
        abilityNames.add(ItemInit.EYEOFDEMONHUNTING.get());
        abilityNames.add(ItemInit.WARRIORDANGERSENSE.get());
        abilityNames.add(ItemInit.MERCURYLIQUEFICATION.get());
        abilityNames.add(ItemInit.SILVERSWORDMANIFESTATION.get());
        abilityNames.add(ItemInit.SILVERRAPIER.get());
        abilityNames.add(ItemInit.SILVERARMORY.get());
        abilityNames.add(ItemInit.LIGHTCONCEALMENT.get());
        abilityNames.add(ItemInit.BEAMOFGLORY.get());
        abilityNames.add(ItemInit.AURAOFGLORY.get());
        abilityNames.add(ItemInit.TWILIGHTSWORD.get());
        abilityNames.add(ItemInit.MERCURYCAGE.get());
        abilityNames.add(ItemInit.DIVINEHANDLEFT.get());
        abilityNames.add(ItemInit.DIVINEHANDRIGHT.get());
        abilityNames.add(ItemInit.TWILIGHTMANIFESTATION.get());
        abilityNames.add(ItemInit.AURAOFTWILIGHT.get());
        abilityNames.add(ItemInit.TWILIGHTFREEZE.get());
        abilityNames.add(ItemInit.TWILIGHTACCELERATE.get());
        abilityNames.add(ItemInit.GLOBEOFTWILIGHT.get());
        abilityNames.add(ItemInit.BEAMOFTWILIGHT.get());
        abilityNames.add(ItemInit.TWILIGHTLIGHT.get());

        //Apprentice
        abilityNames.add(ItemInit.CREATEDOOR.get());
        abilityNames.add(ItemInit.TRICKBURNING.get());
        abilityNames.add(ItemInit.TRICKFREEZING.get());
        abilityNames.add(ItemInit.TRICKTUMBLE.get());
        abilityNames.add(ItemInit.TRICKWIND.get());
        abilityNames.add(ItemInit.TRICKFOG.get());
        abilityNames.add(ItemInit.TRICKELECTRICSHOCK.get());
        abilityNames.add(ItemInit.TRICKTELEKENISIS.get());
        abilityNames.add(ItemInit.TRICKESCAPETRICK.get());
        abilityNames.add(ItemInit.TRICKFLASH.get());
        abilityNames.add(ItemInit.TRICKLOUDNOISE.get());
        abilityNames.add(ItemInit.TRICKBLACKCURTAIN.get());
        abilityNames.add(ItemInit.ASTROLOGER_SPIRIT_VISION.get());
        abilityNames.add(ItemInit.RECORDSCRIBE.get());
        abilityNames.add(ItemInit.SCRIBEABILITIES.get());
        abilityNames.add(ItemInit.TRAVELERSDOOR.get());
        abilityNames.add(ItemInit.TRAVELERSDOORHOME.get());
        abilityNames.add(ItemInit.INVISIBLEHAND.get());
        abilityNames.add(ItemInit.BLINK.get());
        abilityNames.add(ItemInit.BLINKAFTERIMAGE.get());
        abilityNames.add(ItemInit.BLINK_STATE.get());
        abilityNames.add(ItemInit.EXILE.get());
        abilityNames.add(ItemInit.DOOR_MIRAGE.get());
        abilityNames.add(ItemInit.CREATE_CONCEALED_BUNDLE.get());
        abilityNames.add(ItemInit.CREATE_CONCEALED_SPACE.get());
        abilityNames.add(ItemInit.SEPARATE_WORM_OF_STAR.get());
        abilityNames.add(ItemInit.SPATIAL_CAGE.get());
        abilityNames.add(ItemInit.SPATIAL_TEARING.get());
        abilityNames.add(ItemInit.SYMBOLIZATION.get());
        abilityNames.add(ItemInit.DIMENSIONAL_SIGHT.get());
        abilityNames.add(ItemInit.REPLICATE.get());
        abilityNames.add(ItemInit.SEALING.get());
        abilityNames.add(ItemInit.TELEPORTATION.get());
        abilityNames.add(ItemInit.SPACE_FRAGMENTATION.get());
        abilityNames.add(ItemInit.GRAVITY_MANIPULATION.get());
        abilityNames.add(ItemInit.SPATIAL_MAZE.get());
        abilityNames.add(ItemInit.DOOR_SPATIAL_LOCK_ON.get());
        abilityNames.add(ItemInit.DOOR_DIMENSION_CLOSING.get());
        abilityNames.add(ItemInit.DOOR_SEALED_SPACE.get());
        abilityNames.add(ItemInit.DOOR_LAYERING.get());
        abilityNames.add(ItemInit.DOOR_GAMMA_RAY_BURST.get());
        abilityNames.add(ItemInit.CONCEPTUALIZATION.get());
        abilityNames.add(ItemInit.REPLICATION.get());
        return abilityNames;
    }


    public static float applyAbilityStrengthened(float damage, float abilityStrengthened) {
        if (abilityStrengthened > 1) {
            damage *= (1.5f * Configs.COMMON.damageMultiplier.get());
        }
        return damage;
    }


    public static boolean isLivingEntityMoving(LivingEntity entity) {
        CompoundTag tag = entity.getPersistentData();
        updatePositions(entity, tag);
        double MOVEMENT_THRESHOLD = 0.0018;
        double prevX = tag.getDouble("prevX");
        double prevY = tag.getDouble("prevY");
        double prevZ = tag.getDouble("prevZ");
        double currentX = tag.getDouble("currentX");
        double currentY = tag.getDouble("currentY");
        double currentZ = tag.getDouble("currentZ");

        // Check if movement exceeds threshold in any direction
        return Math.abs(prevX - currentX) > MOVEMENT_THRESHOLD ||
                Math.abs(prevY - currentY) > MOVEMENT_THRESHOLD ||
                Math.abs(prevZ - currentZ) > MOVEMENT_THRESHOLD;
    }

    public static LivingEntity checkLivingEntityCollision(Entity entity, Level level, Double radius) {
        AABB entityBoundingBox = entity.getBoundingBox();
        AABB searchArea = entityBoundingBox.inflate(radius);
        for (Entity otherEntity : level.getEntities(entity, searchArea, otherEntity -> true)) {
            if (entityBoundingBox.intersects(otherEntity.getBoundingBox())) {
                if (otherEntity instanceof LivingEntity living) {
                    return living;
                }
            }
        }
        return null;
    }

    public static void updatePositions(Entity entity, CompoundTag tag) {
        int tickCounter = tag.getInt("tickCounter");

        if (tickCounter == 0) {
            // Store previous position
            tag.putDouble("prevX", entity.getX());
            tag.putDouble("prevY", entity.getY());
            tag.putDouble("prevZ", entity.getZ());
            tag.putInt("tickCounter", 1);
        } else {
            // Store current position
            tag.putDouble("currentX", entity.getX());
            tag.putDouble("currentY", entity.getY());
            tag.putDouble("currentZ", entity.getZ());
            tag.putInt("tickCounter", 0);
        }
    }

    public static void setTargetToHighestHP(Mob mob, int searchRange) {
        List<LivingEntity> nearbyEntities = mob.level().getEntitiesOfClass(LivingEntity.class, mob.getBoundingBox().inflate(searchRange), entity -> entity != mob && entity.isAlive() && mob.canAttack(entity));
        if (nearbyEntities.isEmpty()) {
            return;
        }
        nearbyEntities.stream().max(Comparator.comparingDouble(LivingEntity::getMaxHealth)).ifPresent(mob::setTarget);
    }

    public static void saveWorld() {
        // Get the Minecraft server instance
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            for (ServerLevel level : server.getAllLevels()) {
                level.save(null, true, false);
            }
            PlayerList playerList = server.getPlayerList();
            playerList.saveAll();
        }
    }

    public static void executeCommand(ServerLevel world, BlockPos pos, String command) {
        CommandSourceStack source = new CommandSourceStack(
                CommandSource.NULL,
                Vec3.atCenterOf(pos),
                Vec2.ZERO,
                world,
                4, // Permission level
                "", // Name
                Component.literal(""), // Display name
                world.getServer(),
                null // Entity
        );

        world.getServer().getCommands().performPrefixedCommand(source, command);
    }

    public static void executeCommand(MinecraftServer server, String command) {
        CommandSourceStack source = server.createCommandSourceStack();
        server.getCommands().performPrefixedCommand(source, command);
    }

    public static void registerAllRecipes(CommandContext<CommandSourceStack> server) {
        // Monster Potions
        executeRecipeCommand(server, "/beyonderrecipe add lotm:monster_9_potion ingredients 2 iceandfire:cyclops_eye legendary_monsters:nature_crystal minecraft:gunpowder alexscaves:charred_remnant bosses_of_mass_destruction:soul_star");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:monster_8_potion ingredients 2 legendary_monsters:crystal_of_sandstorm legendary_monsters:frozen_rune alexscaves:sweet_tooth minecraft:netherite_scrap mutantmonsters:mutant_skeleton_skull");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:monster_7_potion ingredients 2 alexscaves:pure_darkness legendary_monsters:anchor_handle arphex:giant_spinneret macabre:mortis_essence faded_conquest_2:key_of_death");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:monster_6_potion ingredients 2 cataclysm:monstrous_horn illageandspillage:spellbound_book minecraft:nether_star bosses_of_mass_destruction:void_thorn illageandspillage:bag_of_horrors");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:monster_5_potion ingredients 2 soulsweapons:chaos_crown cataclysm:witherite_ingot arphex:crusher_claw arphex:void_geode_shard soulsweapons:darkin_blade");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:monster_4_potion ingredients 2 macabre:gargamaw_heart cataclysm:ignitium_ingot legendary_monsters:air_rune alexscaves:immortal_embryo cataclysm:remnant_skull");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:monster_3_potion ingredients 1 terramity:pocket_universe cataclysm:cursium_ingot arphex:abyssal_crystal");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:monster_2_potion ingredients 1 terramity:music_sheet_of_the_legendary_super_sniffer soulsweapons:lord_soul_day_stalker minecraft:iron_ingot");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:monster_1_potion ingredients 1 terramity:music_sheet_of_the_omnipotent_ultra_sniffer minecraft:netherite_block");

        // Sailor Potions
        executeRecipeCommand(server, "/beyonderrecipe add lotm:sailor_9_potion ingredients 2 cataclysm:kobolediator_skull mowziesmobs:sol_visage aquamirae:fin arphex:roach_nymph arphex:fly_appendage");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:sailor_8_potion ingredients 2 faded_conquest_2:summon_blocknight alexsmobs:warped_muscle iceandfire:sea_serpent_fang minecraft:prismarine_shard mutantmonsters:endersoul_hand");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:sailor_7_potion ingredients 2 eeeabsmobs:heart_of_pagan mowziesmobs:ice_crystal aquamirae:abyssal_amethyst arphex:abyssal_shard faded_conquest_2:keyof_pestilence");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:sailor_6_potion ingredients 2 cataclysm:monstrous_horn illageandspillage:spellbound_book arphex:oversized_stinger illageandspillage:totem_of_banishment bosses_of_mass_destruction:void_thorn");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:sailor_5_potion ingredients 2 soulsweapons:essence_of_eventide aquamirae:frozen_key soulsweapons:darkin_blade arphex:void_geode_shard cataclysm:gauntlet_of_guard");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:sailor_4_potion ingredients 2 macabre:baal_heart alexscaves:tectonic_shard cataclysm:abyssal_egg alexscaves:immortal_embryo terramity:music_sheet_of_the_gnome_king");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:sailor_3_potion ingredients 1 soulsweapons:essence_of_luminescence arphex:void_geode cataclysm:essence_of_the_storm");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:sailor_2_potion ingredients 1 terramity:music_sheet_of_the_legendary_super_sniffer soulsweapons:lord_soul_night_prowler minecraft:lightning_rod");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:sailor_1_potion ingredients 1 terramity:music_sheet_of_the_omnipotent_ultra_sniffer minecraft:diamond_block");

        // Spectator Potions
        executeRecipeCommand(server, "/beyonderrecipe add lotm:spectator_9_potion ingredients 2 iceandfire:cyclops_eye legendary_monsters:dinosaur_bone born_in_chaos_v1:nightmare_claw macabre:eye arphex:venomous_appendage");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:spectator_8_potion ingredients 2 faded_conquest_2:stormclasher_katana alexscaves:heavy_bone born_in_chaos_v1:seedof_chaos born_in_chaos_v1:spider_mandible mutantmonsters:endersoul_hand");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:spectator_7_potion ingredients 2 deeperdarker:soul_crystal animatedmobsmod:ender_spectre bosses_of_mass_destruction:ancient_anima arphex:mangled_spider_flesh legendary_monsters:withered_bone");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:spectator_6_potion ingredients 2 awakened_bosses:herobrine_nugget faded_conquest_2:war_claymore born_in_chaos_v1:lifestealer_bone minecraft:nether_star legendary_monsters:lava_eaters_skin");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:spectator_5_potion ingredients 2 soulsweapons:essence_of_eventide soulsweapons:lord_soul_rose aquamirae:frozen_key cataclysm:witherite_ingot arphex:void_geode_shard");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:spectator_4_potion ingredients 2 macabre:gomoria_heart cataclysm:ignitium_ingot arphex:fire_opal_shard sleepy_hollows:lootbag cataclysm:remnant_skull");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:spectator_3_potion ingredients 1 born_in_chaos_v1:soulbane arphex:fire_opal cataclysm:cursium_ingot");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:spectator_2_potion ingredients 1 terramity:fortunes_favor soulsweapons:lord_soul_day_stalker minecraft:spyglass");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:spectator_1_potion ingredients 1 terramity:music_sheet_of_the_omnipotent_ultra_sniffer minecraft:emerald_block");

        // Warrior Potions
        executeRecipeCommand(server, "/beyonderrecipe add lotm:warrior_9_potion ingredients 2 mowziesmobs:sol_visage zoniex:deathly_onyx mowziesmobs:wrought_axe macabre:rattails born_in_chaos_v1:fangofthe_hound_leader");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:warrior_8_potion ingredients 2 aether:silver_dungeon_key iceandfire:hydra_fang terramity:spiteful_soul mutantmonsters:hulk_hammer macabre:blindbaloon_item");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:warrior_7_potion ingredients 2 aether:gold_dungeon_key bosses_of_mass_destruction:blazing_eye macabre:mortis_essence arphex:scarab_seal bosses_of_mass_destruction:obsidian_heart");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:warrior_6_potion ingredients 2 awakened_bosses:herobrine_nugget macabre:rootofinfestation legendary_monsters:lava_eaters_skin born_in_chaos_v1:soul_cutlass illageandspillage:totem_of_banishment");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:warrior_5_potion ingredients 2 soulsweapons:chaos_crown soulsweapons:lord_soul_rose arphex:void_geode_shard cataclysm:witherite_ingot cataclysm:gauntlet_of_guard");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:warrior_4_potion ingredients 2 alexscaves:tectonic_shard macabre:valamon_heart eeeabsmobs:guardian_core sleepy_hollows:lootbag terramity:music_sheet_of_the_gnome_king");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:warrior_3_potion ingredients 1 terramity:perish_staff arphex:abyssal_crystal cataclysm:essence_of_the_storm");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:warrior_2_potion ingredients 1 terramity:fortunes_favor soulsweapons:lord_soul_day_stalker minecraft:clock");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:warrior_1_potion ingredients 1 terramity:music_sheet_of_the_omnipotent_ultra_sniffer minecraft:gold_block");
    }

//    public static void registerAbilities(Player player, MinecraftServer server) {
//        int sequence = BeyonderUtil.getSequence(player);
//        BeyonderClass beyonderClass = getPathway(player);
//        boolean isSpectator = currentPathwayMatchesNoException(player, BeyonderClassInit.SPECTATOR.get());
//        boolean isSailor = currentPathwayMatchesNoException(player, BeyonderClassInit.SAILOR.get());
//        boolean isMonster = currentPathwayMatchesNoException(player, BeyonderClassInit.MONSTER.get());
//        boolean isApprentice = currentPathwayMatchesNoException(player, BeyonderClassInit.APPRENTICE.get());
//        boolean isWarrior = currentPathwayMatchesNoException(player, BeyonderClassInit.WARRIOR.get());
//        if (isWarrior) {
//            if (sequence == 9) {
//                player.sendSystemMessage(Component.literal("No abilities to register"));
//            } else if (sequence == 8) {
//                player.sendSystemMessage(Component.literal("No abilities to register"));
//            } else if (sequence == 7) {
//                player.sendSystemMessage(Component.literal("No abilities to register"));
//            } else if (sequence == 6) {
//                executeCommand(server, "/abilityput LLLLL lotm:gignatification");
//                executeCommand(server, "/abilityput LLLLR lotm:lightofdawn");
//                executeCommand(server, "/abilityput RRRRR lotm:dawnarmory");
//            } else if (sequence == 5) {
//                executeCommand(server, "/abilityput LLLLL lotm:gignatification");
//                executeCommand(server, "/abilityput LLLLR lotm:lightofdawn");
//                executeCommand(server, "/abilityput RRRRL lotm:dawnarmory");
//                executeCommand(server, "/abilityput RRLLL lotm:enabledisableprotection");
//            } else if (sequence == 4) {
//                executeCommand(server, "/abilityput LLLLL lotm:gignatification");
//                executeCommand(server, "/abilityput LLLLR lotm:lightofdawn");
//                executeCommand(server, "/abilityput RRRRL lotm:dawnarmory");
//                executeCommand(server, "/abilityput RRLLL lotm:enabledisableprotection");
//                executeCommand(server, "/abilityput LRLRL lotm:eyeofdemonhunting");
//                executeCommand(server, "/abilityput LRRLR lotm:warriordangersense");
//            } else if (sequence == 3) {
//                executeCommand(server, "/abilityput LLLLL lotm:gignatification");
//                executeCommand(server, "/abilityput LLLLR lotm:lightofdawn");
//                executeCommand(server, "/abilityput RRRRL lotm:dawnarmory");
//                executeCommand(server, "/abilityput RRLLL lotm:enabledisableprotection");
//                executeCommand(server, "/abilityput LRLRL lotm:eyeofdemonhunting");
//                executeCommand(server, "/abilityput LRRLR lotm:warriordangersense");
//                executeCommand(server, "/abilityput RRRRR lotm:mercuryliquefication");
//                executeCommand(server, "/abilityput LRLLR lotm:silverswordmanifestation");
//                executeCommand(server, "/abilityput RRRLL lotm:silverrapier");
//                executeCommand(server, "/abilityput RLRRL lotm:silverarmory");
//                executeCommand(server, "/abilityput LLRRR lotm:lightconcealment");
//            } else if (sequence == 2) {
//                executeCommand(server, "/abilityput LLLLL lotm:gignatification");
//                executeCommand(server, "/abilityput LLLLR lotm:lightofdawn");
//                executeCommand(server, "/abilityput RRRRL lotm:dawnarmory");
//                executeCommand(server, "/abilityput RRLLL lotm:enabledisableprotection");
//                executeCommand(server, "/abilityput LRLRL lotm:eyeofdemonhunting");
//                executeCommand(server, "/abilityput LRRLR lotm:warriordangersense");
//                executeCommand(server, "/abilityput RRRRR lotm:mercuryliquefication");
//                executeCommand(server, "/abilityput LRLLR lotm:silverswordmanifestation");
//                executeCommand(server, "/abilityput RRRLL lotm:silverrapier");
//                executeCommand(server, "/abilityput RLRRL lotm:silverarmory");
//                executeCommand(server, "/abilityput LLRRR lotm:lightconcealment");
//                executeCommand(server, "/abilityput RLLLL lotm:beamofglory");
//                executeCommand(server, "/abilityput LRLLL lotm:auraofglory");
//                executeCommand(server, "/abilityput RLRLL lotm:twilightsword");
//                executeCommand(server, "/abilityput RLRLR lotm:mercurycage");
//            } else if (sequence == 1) {
//                executeCommand(server, "/abilityput LLLLL lotm:gignatification");
//                executeCommand(server, "/abilityput LLLLR lotm:lightofdawn");
//                executeCommand(server, "/abilityput RRRRL lotm:dawnarmory");
//                executeCommand(server, "/abilityput RRLLL lotm:enabledisableprotection");
//                executeCommand(server, "/abilityput LRLRL lotm:eyeofdemonhunting");
//                executeCommand(server, "/abilityput LRRLR lotm:warriordangersense");
//                executeCommand(server, "/abilityput RRRRR lotm:mercuryliquefication");
//                executeCommand(server, "/abilityput LRLLR lotm:silverswordmanifestation");
//                executeCommand(server, "/abilityput RRRLL lotm:silverrapier");
//                executeCommand(server, "/abilityput RLRRL lotm:silverarmory");
//                executeCommand(server, "/abilityput LLRRR lotm:lightconcealment");
//                executeCommand(server, "/abilityput RLLLL lotm:beamofglory");
//                executeCommand(server, "/abilityput LRLLL lotm:auraofglory");
//                executeCommand(server, "/abilityput RLRLL lotm:twilightsword");
//                executeCommand(server, "/abilityput RLRLR lotm:mercurycage");
//                executeCommand(server, "/abilityput LRRLL lotm:divinehandright");
//                executeCommand(server, "/abilityput RLLRR lotm:divinehandleft");
//                executeCommand(server, "/abilityput LLRLR lotm:twilightmanifestation");
//            } else if (sequence == 0) {
//                executeCommand(server, "/abilityput LLLLL lotm:gignatification");
//                executeCommand(server, "/abilityput LLLLR lotm:lightofdawn");
//                executeCommand(server, "/abilityput RRRRL lotm:dawnarmory");
//                executeCommand(server, "/abilityput RRLLL lotm:enabledisableprotection");
//                executeCommand(server, "/abilityput LRLRL lotm:eyeofdemonhunting");
//                executeCommand(server, "/abilityput LRRLR lotm:warriordangersense");
//                executeCommand(server, "/abilityput RRRRR lotm:mercuryliquefication");
//                executeCommand(server, "/abilityput LRLLR lotm:silverswordmanifestation");
//                executeCommand(server, "/abilityput RRRLL lotm:silverrapier");
//                executeCommand(server, "/abilityput RLRRL lotm:silverarmory");
//                executeCommand(server, "/abilityput LLRRR lotm:lightconcealment");
//                executeCommand(server, "/abilityput RLLLL lotm:beamoftwilight");
//                executeCommand(server, "/abilityput LRLLL lotm:auraoftwilight");
//                executeCommand(server, "/abilityput RLRLL lotm:twilightsword");
//                executeCommand(server, "/abilityput RLRLR lotm:mercurycage");
//                executeCommand(server, "/abilityput LRRLL lotm:divinehandright");
//                executeCommand(server, "/abilityput RLLRR lotm:divinehandleft");
//                executeCommand(server, "/abilityput LLRLR lotm:twilightmanifestation");
//
//                executeCommand(server, "/abilityput RLRRR lotm:twilightfreeze");
//                executeCommand(server, "/abilityput RLLLL lotm:twilightlight");
//                executeCommand(server, "/abilityput RRLRR lotm:twilightaccelerate");
//                executeCommand(server, "/abilityput LRRRR lotm:globeoftwilight");
//            }
//        } else if (isSpectator) {
//            if (sequence == 9) {
//                player.sendSystemMessage(Component.literal("No abilities to register"));
//            } else if (sequence >= 8) {
//                executeCommand(server, "/abilityput LRRLL lotm:mindreading");
//            } else if (sequence == 7) {
//                executeCommand(server, "/abilityput LRRLL lotm:mindreading");
//                executeCommand(server, "/abilityput LLLLL lotm:awe");
//                executeCommand(server, "/abilityput LLLRL lotm:frenzy");
//                executeCommand(server, "/abilityput RRRLR lotm:placate");
//            } else if (sequence == 6) {
//                executeCommand(server, "/abilityput LRRLL lotm:mindreading");
//                executeCommand(server, "/abilityput LLLLL lotm:awe");
//                executeCommand(server, "/abilityput LLLRL lotm:frenzy");
//                executeCommand(server, "/abilityput RRRLR lotm:placate");
//                executeCommand(server, "/abilityput RRRLL lotm:psychologicalinvisibility");
//            } else if (sequence == 5) {
//                executeCommand(server, "/abilityput LRRLL lotm:mindreading");
//                executeCommand(server, "/abilityput LLLLL lotm:awe");
//                executeCommand(server, "/abilityput LLLRL lotm:frenzy");
//                executeCommand(server, "/abilityput RRRLR lotm:placate");
//                executeCommand(server, "/abilityput RRRLL lotm:psychologicalinvisibility");
//                executeCommand(server, "/abilityput RRRRR lotm:dreamwalking");
//            } else if (sequence == 4) {
//                executeCommand(server, "/abilityput LRRLL lotm:mindreading");
//                executeCommand(server, "/abilityput LLLLL lotm:awe");
//                executeCommand(server, "/abilityput LLLRL lotm:frenzy");
//                executeCommand(server, "/abilityput RRRLR lotm:placate");
//                executeCommand(server, "/abilityput RRRLL lotm:psychologicalinvisibility");
//                executeCommand(server, "/abilityput RRRRR lotm:dreamwalking");
//                executeCommand(server, "/abilityput RRRRL lotm:dragonbreath");
//                executeCommand(server, "/abilityput RLLLL lotm:mindstorm");
//            } else if (sequence == 3) {
//                executeCommand(server, "/abilityput LRRLL lotm:mindreading");
//                executeCommand(server, "/abilityput LLLLL lotm:awe");
//                executeCommand(server, "/abilityput LLLRL lotm:frenzy");
//                executeCommand(server, "/abilityput RRRLR lotm:placate");
//                executeCommand(server, "/abilityput RRRLL lotm:psychologicalinvisibility");
//                executeCommand(server, "/abilityput RRRRR lotm:dreamwalking");
//                executeCommand(server, "/abilityput RRRRL lotm:dragonbreath");
//                executeCommand(server, "/abilityput RLLLL lotm:plaguestorm");
//            } else if (sequence == 2) {
//                executeCommand(server, "/abilityput LRRLL lotm:mindreading");
//                executeCommand(server, "/abilityput LLLLL lotm:awe");
//                executeCommand(server, "/abilityput LLLRL lotm:frenzy");
//                executeCommand(server, "/abilityput RRRLR lotm:placate");
//                executeCommand(server, "/abilityput RRRLL lotm:psychologicalinvisibility");
//                executeCommand(server, "/abilityput RRRRR lotm:dreamwalking");
//                executeCommand(server, "/abilityput RRRRL lotm:dragonbreath");
//                executeCommand(server, "/abilityput RLLLL lotm:plaguestorm");
//                executeCommand(server, "/abilityput RRLRR lotm:dreamintoreality");
//                executeCommand(server, "/abilityput LLLLR lotm:discern");
//            } else if (sequence == 1) {
//                executeCommand(server, "/abilityput LRRLL lotm:mindreading");
//                executeCommand(server, "/abilityput LLLLL lotm:awe");
//                executeCommand(server, "/abilityput LLLRL lotm:frenzy");
//                executeCommand(server, "/abilityput RRRLR lotm:placate");
//                executeCommand(server, "/abilityput RRRLL lotm:psychologicalinvisibility");
//                executeCommand(server, "/abilityput RRRRR lotm:dreamwalking");
//                executeCommand(server, "/abilityput RRRRL lotm:dragonbreath");
//                executeCommand(server, "/abilityput RLLLL lotm:plaguestorm");
//                executeCommand(server, "/abilityput RRLRR lotm:dreamintoreality");
//                executeCommand(server, "/abilityput LLLLR lotm:discern");
//                executeCommand(server, "/abilityput LLRRR lotm:prophesizeblock");
//                executeCommand(server, "/abilityput LLRLR lotm:prophesizeplayer");
//                executeCommand(server, "/abilityput LLRLL lotm:prophesizedemise");
//                executeCommand(server, "/abilityput RRLLL lotm:meteorshower");
//            } else if (sequence == 0) {
//                executeCommand(server, "/abilityput LRRLL lotm:mindreading");
//                executeCommand(server, "/abilityput LLLLL lotm:awe");
//                executeCommand(server, "/abilityput LLLRL lotm:frenzy");
//                executeCommand(server, "/abilityput RRRLR lotm:placate");
//                executeCommand(server, "/abilityput RRRLL lotm:psychologicalinvisibility");
//                executeCommand(server, "/abilityput RRRRR lotm:dreamwalking");
//                executeCommand(server, "/abilityput RRRRL lotm:dragonbreath");
//                executeCommand(server, "/abilityput RLLLL lotm:plaguestorm");
//                executeCommand(server, "/abilityput RRLRR lotm:dreamintoreality");
//                executeCommand(server, "/abilityput LLLLR lotm:discern");
//                executeCommand(server, "/abilityput LLRRR lotm:prophesizeblock");
//                executeCommand(server, "/abilityput LLRLR lotm:prophesizeplayer");
//                executeCommand(server, "/abilityput LLRLL lotm:prophesizedemise");
//                executeCommand(server, "/abilityput RRLLL lotm:meteorshower");
//                executeCommand(server, "/abilityput RLRRR lotm:envisionhealth");
//                executeCommand(server, "/abilityput RLLRR lotm:envisionbarrier");
//            }
//        } else if (isMonster) {
//            if (sequence == 9) {
//                executeCommand(server, "/abilityput RLRRL lotm:monsterdangersense");
//            } else if (sequence >= 8) {
//                executeCommand(server, "/abilityput RLRRL lotm:monsterdangersense");
//            } else if (sequence == 7) {
//                executeCommand(server, "/abilityput RLRRL lotm:monsterdangersense");
//                executeCommand(server, "/abilityput RRLLR lotm:luckperception");
//            } else if (sequence == 6) {
//                executeCommand(server, "/abilityput RLRRL lotm:monsterdangersense");
//                executeCommand(server, "/abilityput RRLLR lotm:luckperception");
//                executeCommand(server, "/abilityput LLLRR lotm:psychestorm");
//            } else if (sequence == 5) {
//                executeCommand(server, "/abilityput RLRRL lotm:monsterdangersense");
//                executeCommand(server, "/abilityput RRLLR lotm:luckperception");
//                executeCommand(server, "/abilityput LLLRR lotm:psychestorm");
//                executeCommand(server, "/abilityput RRLLL lotm:luckfuturetelling");
//                executeCommand(server, "/abilityput LLLLL lotm:misfortunebestowal");
//            } else if (sequence == 4) {
//                executeCommand(server, "/abilityput RLRRL lotm:monsterdangersense");
//                executeCommand(server, "/abilityput RRLLR lotm:luckperception");
//                executeCommand(server, "/abilityput LLLRR lotm:psychestorm");
//                executeCommand(server, "/abilityput RRLLL lotm:luckfuturetelling");
//                executeCommand(server, "/abilityput LLLLL lotm:misfortunebestowal");
//                executeCommand(server, "/abilityput LRRLL lotm:providencedomain");
//                executeCommand(server, "/abilityput LRLLL lotm:misfortunedomain");
//            } else if (sequence == 3) {
//                executeCommand(server, "/abilityput RLRRL lotm:monsterdangersense");
//                executeCommand(server, "/abilityput RRLLR lotm:luckperception");
//                executeCommand(server, "/abilityput LLLRR lotm:psychestorm");
//                executeCommand(server, "/abilityput RRLLL lotm:luckfuturetelling");
//                executeCommand(server, "/abilityput LLLLL lotm:misfortunebestowal");
//                executeCommand(server, "/abilityput LRRLL lotm:providencedomain");
//                executeCommand(server, "/abilityput LRLLL lotm:misfortunedomain");
//                executeCommand(server, "/abilityput RRRRR lotm:auraofchaos");
//                executeCommand(server, "/abilityput RRRRL lotm:chaoswalkercombat");
//                executeCommand(server, "/abilityput LRRRR lotm:enabledisableripple");
//            } else if (sequence == 2) {
//                executeCommand(server, "/abilityput RLRRL lotm:monsterdangersense");
//                executeCommand(server, "/abilityput RRLLR lotm:luckperception");
//                executeCommand(server, "/abilityput LLLRR lotm:psychestorm");
//                executeCommand(server, "/abilityput RRLLL lotm:luckfuturetelling");
//                executeCommand(server, "/abilityput LLLLL lotm:misfortunebestowal");
//                executeCommand(server, "/abilityput LRRLL lotm:providencedomain");
//                executeCommand(server, "/abilityput LRLLL lotm:misfortunedomain");
//                executeCommand(server, "/abilityput RRRRR lotm:auraofchaos");
//                executeCommand(server, "/abilityput RRRLL lotm:chaoswalkercombat");
//                executeCommand(server, "/abilityput LRRRR lotm:enabledisableripple");
//                executeCommand(server, "/abilityput RRRRL lotm:whispersofcorruption");
//                executeCommand(server, "/abilityput LRRRR lotm:misfortuneimplosion");
//            } else if (sequence == 1) {
//                executeCommand(server, "/abilityput RLRRL lotm:monsterdangersense");
//                executeCommand(server, "/abilityput RRLLR lotm:luckperception");
//                executeCommand(server, "/abilityput LLLRR lotm:psychestorm");
//                executeCommand(server, "/abilityput RRLLL lotm:luckfuturetelling");
//                executeCommand(server, "/abilityput LLLLL lotm:misfortunebestowal");
//                executeCommand(server, "/abilityput LRRLL lotm:providencedomain");
//                executeCommand(server, "/abilityput LRLLL lotm:misfortunedomain");
//                executeCommand(server, "/abilityput RRRRR lotm:auraofchaos");
//                executeCommand(server, "/abilityput RRRLL lotm:chaoswalkercombat");
//                executeCommand(server, "/abilityput LRRRR lotm:enabledisableripple");
//                executeCommand(server, "/abilityput RRRRL lotm:whispersofcorruption");
//                executeCommand(server, "/abilityput LRRRR lotm:misfortuneimplosion");
//                executeCommand(server, "/abilityput RLLLL lotm:rebootself");
//                executeCommand(server, "/abilityput RRRLR lotm:cycleoffate");
//                executeCommand(server, "/abilityput RRLRR lotm:fatereincarnation");
//            } else if (sequence == 0) {
//                executeCommand(server, "/abilityput RLRRL lotm:monsterdangersense");
//                executeCommand(server, "/abilityput RRLLR lotm:luckperception");
//                executeCommand(server, "/abilityput LLLRR lotm:psychestorm");
//                executeCommand(server, "/abilityput RRLLL lotm:luckfuturetelling");
//                executeCommand(server, "/abilityput LLLLL lotm:misfortunebestowal");
//                executeCommand(server, "/abilityput LRRLL lotm:providencedomain");
//                executeCommand(server, "/abilityput LRLLL lotm:misfortunedomain");
//                executeCommand(server, "/abilityput RRRRR lotm:auraofchaos");
//                executeCommand(server, "/abilityput RRRLL lotm:chaoswalkercombat");
//                executeCommand(server, "/abilityput LRRRR lotm:enabledisableripple");
//                executeCommand(server, "/abilityput RRRRL lotm:whispersofcorruption");
//                executeCommand(server, "/abilityput LRRRR lotm:misfortuneimplosion");
//                executeCommand(server, "/abilityput RLLLL lotm:rebootself");
//                executeCommand(server, "/abilityput RRRLR lotm:cycleoffate");
//                executeCommand(server, "/abilityput RRLRR lotm:fatereincarnation");
//                executeCommand(server, "/abilityput LLLRL lotm:probabilityinfinitefortune");
//                executeCommand(server, "/abilityput RLRRR lotm:probabilityinfinitemisfortune");
//                executeCommand(server, "/abilityput LRLRL lotm:probabilityfortune");
//                executeCommand(server, "/abilityput RLRLR lotm:probabilitymisfortune");
//            }
//        } else if (isSailor) {
//            if (sequence == 9) {
//                player.sendSystemMessage(Component.literal("No abilities to register"));
//            } else if (sequence >= 8) {
//                executeCommand(server, "/abilityput LLLLL lotm:ragingblows");
//            } else if (sequence == 7) {
//                executeCommand(server, "/abilityput LLLLL lotm:ragingblows");
//            } else if (sequence == 6) {
//                executeCommand(server, "/abilityput LLLLL lotm:ragingblows");
//            } else if (sequence == 5) {
//                executeCommand(server, "/abilityput LLLLL lotm:ragingblows");
//                executeCommand(server, "/abilityput RRRRR lotm:sailorlightning");
//                executeCommand(server, "/abilityput LLRRL lotm:acidicrain");
//                executeCommand(server, "/abilityput LRRLL lotm:watersphere");
//            } else if (sequence == 4) {
//                executeCommand(server, "/abilityput LLLLL lotm:ragingblows");
//                executeCommand(server, "/abilityput RRRRR lotm:sailorlightning");
//                executeCommand(server, "/abilityput LLRRL lotm:acidicrain");
//                executeCommand(server, "/abilityput LRRLL lotm:watersphere");
//                executeCommand(server, "/abilityput LLLRR lotm:tornado");
//                executeCommand(server, "/abilityput LLLLR lotm:roar");
//                executeCommand(server, "/abilityput RRRLL lotm:earthquake");
//            } else if (sequence == 3) {
//                executeCommand(server, "/abilityput LLLLL lotm:ragingblows");
//                executeCommand(server, "/abilityput RRRRR lotm:sailorlightning");
//                executeCommand(server, "/abilityput LLRRL lotm:acidicrain");
//                executeCommand(server, "/abilityput LRRLL lotm:watersphere");
//                executeCommand(server, "/abilityput LLLRR lotm:tornado");
//                executeCommand(server, "/abilityput LLLLR lotm:roar");
//                executeCommand(server, "/abilityput RRRLL lotm:earthquake");
//                executeCommand(server, "/abilityput LRRRR lotm:sonicboom");
//                executeCommand(server, "/abilityput RRRRL lotm:lightningbranch");
//                executeCommand(server, "/abilityput RRLLR lotm:thunderclap");
//            } else if (sequence == 2) {
//                executeCommand(server, "/abilityput LLLLL lotm:ragingblows");
//                executeCommand(server, "/abilityput RRRRR lotm:sailorlightning");
//                executeCommand(server, "/abilityput LLRRL lotm:acidicrain");
//                executeCommand(server, "/abilityput LRRLL lotm:watersphere");
//                executeCommand(server, "/abilityput LLLRR lotm:tornado");
//                executeCommand(server, "/abilityput LLLLR lotm:roar");
//                executeCommand(server, "/abilityput RRRLL lotm:earthquake");
//                executeCommand(server, "/abilityput LRRRR lotm:sonicboom");
//                executeCommand(server, "/abilityput RRRRL lotm:lightningbranch");
//                executeCommand(server, "/abilityput RRLLR lotm:thunderclap");
//                executeCommand(server, "/abilityput LRLRL lotm:lightningball");
//                executeCommand(server, "/abilityput RRLLL lotm:extremecoldness");
//                executeCommand(server, "/abilityput LRRLR lotm:raineyes");
//                executeCommand(server, "/abilityput RLRRL lotm:volcaniceruption");
//            } else if (sequence == 1) {
//
//                executeCommand(server, "/abilityput LLLLL lotm:ragingblows");
//                executeCommand(server, "/abilityput RRRRR lotm:sailorlightning");
//                executeCommand(server, "/abilityput LLRRL lotm:acidicrain");
//                executeCommand(server, "/abilityput LRRLL lotm:watersphere");
//                executeCommand(server, "/abilityput LLLRR lotm:tornado");
//                executeCommand(server, "/abilityput LLLLR lotm:roar");
//                executeCommand(server, "/abilityput RRRLL lotm:earthquake");
//                executeCommand(server, "/abilityput LRRRR lotm:sonicboom");
//                executeCommand(server, "/abilityput RRRRL lotm:lightningbranch");
//                executeCommand(server, "/abilityput RRLLR lotm:thunderclap");
//                executeCommand(server, "/abilityput LRLRL lotm:lightningball");
//                executeCommand(server, "/abilityput RRLLL lotm:extremecoldness");
//                executeCommand(server, "/abilityput LRRLR lotm:raineyes");
//                executeCommand(server, "/abilityput RLRRL lotm:volcaniceruption");
//                executeCommand(server, "/abilityput LRLLL lotm:lightningballabsorb");
//                executeCommand(server, "/abilityput RRRLR lotm:sailorlightningtravel");
//                executeCommand(server, "/abilityput LLRRR lotm:staroflightning");
//                executeCommand(server, "/abilityput LRLLR lotm:lightningredirection");
//            } else if (sequence == 0) {
//                executeCommand(server, "/abilityput LLLLL lotm:ragingblows");
//                executeCommand(server, "/abilityput RRRRR lotm:sailorlightning");
//                executeCommand(server, "/abilityput LLRRL lotm:acidicrain");
//                executeCommand(server, "/abilityput LRRLL lotm:watersphere");
//                executeCommand(server, "/abilityput LLLRR lotm:tornado");
//                executeCommand(server, "/abilityput LLLLR lotm:roar");
//                executeCommand(server, "/abilityput RRRLL lotm:earthquake");
//                executeCommand(server, "/abilityput LRRRR lotm:sonicboom");
//                executeCommand(server, "/abilityput RRRRL lotm:lightningbranch");
//                executeCommand(server, "/abilityput RRLLR lotm:thunderclap");
//                executeCommand(server, "/abilityput LRLRL lotm:lightningball");
//                executeCommand(server, "/abilityput RRLLL lotm:extremecoldness");
//                executeCommand(server, "/abilityput LRRLR lotm:raineyes");
//                executeCommand(server, "/abilityput RLRRL lotm:volcaniceruption");
//                executeCommand(server, "/abilityput LRLLL lotm:lightningballabsorb");
//                executeCommand(server, "/abilityput RRRLR lotm:sailorlightningtravel");
//                executeCommand(server, "/abilityput LLRRR lotm:staroflightning");
//                executeCommand(server, "/abilityput LRLLR lotm:lightningredirection");
//                executeCommand(server, "/abilityput RLLLL lotm:tyranny");
//                executeCommand(server, "/abilityput RLLLR lotm:stormseal");
//            }
//        }
//    }


    public static boolean isPhysicalDamage(DamageSource source) {
        return source.is(DamageTypes.FALL) || source.is(DamageTypes.CACTUS) || source.is(DamageTypes.FLY_INTO_WALL) || source.is(DamageTypes.GENERIC) || source.is(DamageTypes.FALLING_BLOCK) || source.is(DamageTypes.FALLING_ANVIL) || source.is(DamageTypes.FALLING_STALACTITE) || source.is(DamageTypes.STING) ||
                source.is(DamageTypes.MOB_ATTACK) || source.is(DamageTypes.MOB_ATTACK_NO_AGGRO) || source.is(DamageTypes.PLAYER_ATTACK) || source.is(DamageTypes.ARROW) || source.is(DamageTypes.TRIDENT) || source.is(DamageTypes.MOB_PROJECTILE) || source.is(DamageTypes.FIREBALL) || source.is(DamageTypes.UNATTRIBUTED_FIREBALL) ||
                source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.WITHER_SKULL) || source.is(DamageTypes.PLAYER_EXPLOSION) || source.is(DamageTypes.PLAYER_ATTACK) || source.is(DamageTypes.FIREWORKS) || source.is(DamageTypes.FREEZE) || source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.LAVA) || source.is(DamageTypes.HOT_FLOOR) || source.is(DamageTypes.HOT_FLOOR);
    }

    public static boolean isSupernaturalDamage(DamageSource source) {
        return source.is(DamageTypes.MAGIC) || source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.LIGHTNING_BOLT) || source.is(DamageTypes.DRAGON_BREATH) || source.is(DamageTypes.WITHER) || source.is(MENTAL_DAMAGE);
    }

    public static float getMentalStrength(LivingEntity livingEntity) {
        int mentalStrength = 10;
        float mobReducer = 1;
        if (livingEntity instanceof Mob) {
            mobReducer = 0.75f;
        }
        if (!livingEntity.level().isClientSide()) {
            BeyonderClass pathway = getPathway(livingEntity);
            if (pathway != null) {
                int sequence = BeyonderUtil.getSequence(livingEntity);
                if (sequence != -1) {
                    mentalStrength = pathway.mentalStrength().get(sequence);
                }
            }
        }
        if (Float.isNaN(mentalStrength) || Float.isInfinite(mentalStrength) || mentalStrength < 0) {
            return 10.0f;
        }

        return Math.max(1, mentalStrength * mobReducer);
    }

    public static float getDivination(LivingEntity livingEntity) {
        int divination = 1;
        if (!livingEntity.level().isClientSide()) {
            BeyonderClass pathway = getPathway(livingEntity);
            if (pathway != null) {
                int sequence = BeyonderUtil.getSequence(livingEntity);
                divination = pathway.divination().get(sequence);
                if (currentPathwayAndSequenceMatchesNoException(livingEntity, BeyonderClassInit.APPRENTICE.get(), 7) && isExposedToMoonlight(livingEntity)) {
                    divination = Math.round(divination * 1.5f);
                }
            }
        }
        return divination;
    }

    public static boolean isExposedToMoonlight(LivingEntity entity) {
        Level level = entity.level();
        if (!level.isDay()) {
            BlockPos entityPos = entity.blockPosition();
            return level.canSeeSky(entityPos);
        }

        return false;
    }

    public static double findSurfaceY(Entity entity, double x, double z, ResourceKey<Level> dimensionKey) {
        MinecraftServer server = entity.level().getServer();
        if (server == null) return -1;
        ServerLevel targetWorld = server.getLevel(dimensionKey);
        if (targetWorld == null) return -1;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int blockX = Mth.floor(x);
        int blockZ = Mth.floor(z);
        for (int y = targetWorld.getMaxBuildHeight() - 1; y >= targetWorld.getMinBuildHeight(); y--) {
            pos.set(blockX, y, blockZ);
            if (targetWorld.canSeeSky(pos)) {
                BlockPos belowPos = pos.below();
                BlockState belowState = targetWorld.getBlockState(belowPos);
                if (!belowState.isAir() && belowState.isSolidRender(targetWorld, belowPos)) {
                    return y + 1.0;
                }
            }
        }

        // Fallback: return world spawn Y if no suitable surface found
        return targetWorld.getSharedSpawnPos().getY();
    }

    public static float getAntiDivination(LivingEntity livingEntity) {
        int mentalStrength = 1;
        if (!livingEntity.level().isClientSide()) {
            BeyonderClass pathway = getPathway(livingEntity);
            if (pathway != null) {
                int sequence = BeyonderUtil.getSequence(livingEntity);
                mentalStrength = pathway.antiDivination().get(sequence);
            }
        }
        return mentalStrength;
    }

    public static boolean isPurifiable(LivingEntity livingEntity) {
        return livingEntity.getName().getString().toLowerCase().contains("skeleton") || livingEntity.getName().getString().toLowerCase().contains("demon") || livingEntity.getName().getString().toLowerCase().contains("ghost") || livingEntity.getName().getString().toLowerCase().contains("wraith") || livingEntity.getName().getString().toLowerCase().contains("zombie") || livingEntity.getName().getString().toLowerCase().contains("undead") || livingEntity.getPersistentData().getBoolean("isWraith");

    }

    public static boolean areAllies(LivingEntity livingEntity, LivingEntity ally) {
        if (livingEntity.level() instanceof ServerLevel serverLevel) {
            PlayerAllyData allyData = serverLevel.getDataStorage().computeIfAbsent(PlayerAllyData::load, PlayerAllyData::create, "player_allies");
            if (PlayerMobEntity.isCopyOf(livingEntity, ally)) {
                return true;
            }
            return allyData.areAllies(livingEntity.getUUID(), ally.getUUID());
        }
        return false;
    }



    public static List<LivingEntity> getAllies(LivingEntity livingEntity) {
        List<LivingEntity> allyEntities = new ArrayList<>();
        if (livingEntity.level() instanceof ServerLevel serverLevel) {
            PlayerAllyData allyData = serverLevel.getDataStorage().computeIfAbsent(PlayerAllyData::load, PlayerAllyData::create, "player_allies");
            Set<UUID> allyUUIDs = allyData.getAllies(livingEntity.getUUID());
            for (UUID allyUUID : allyUUIDs) {
                LivingEntity ally = getLivingEntityFromUUID(serverLevel, allyUUID);
                if (ally != null) {
                    allyEntities.add(ally);
                }
            }
        }
        return allyEntities;
    }


    public static void forceAlly(LivingEntity user, LivingEntity allyToBe) {
        if (user.level() instanceof ServerLevel serverLevel) {
            PlayerAllyData allyData = serverLevel.getDataStorage().computeIfAbsent(PlayerAllyData::load, PlayerAllyData::create, "player_allies");
            allyData.addAlly(user.getUUID(), allyToBe.getUUID());
            allyData.addAlly(allyToBe.getUUID(), user.getUUID());
        }
    }

    public static void forceRemoveAlly(LivingEntity user, LivingEntity allyToRemove) {
        if (user.level() instanceof ServerLevel serverLevel) {
            PlayerAllyData allyData = serverLevel.getDataStorage().computeIfAbsent(PlayerAllyData::load, PlayerAllyData::create, "player_allies");
            allyData.removeAlly(user.getUUID(), allyToRemove.getUUID());
            allyData.removeAlly(allyToRemove.getUUID(), user.getUUID());
        }
    }

    public static void useSpirituality(LivingEntity living, int spirituality) { //marked
        if (!living.level().isClientSide()) {
            if (living instanceof Player player) {
                BeyonderHolderAttacher.getHolderUnwrap(player).useSpirituality(spirituality);
            } else if (living instanceof PlayerMobEntity playerMobEntity) {
                playerMobEntity.useSpirituality(spirituality);
            } else {
                int spiritualityLevel = living.getPersistentData().getInt("lotmSpirituality");
                living.getPersistentData().putInt("lotmSpirituality", Math.max(1, spiritualityLevel - spirituality));
            }
        }
    }


    public static void addSpirituality(LivingEntity living, int spirituality) { //marked
        if (!living.level().isClientSide()) {
            if (living instanceof Player player) {
                BeyonderHolderAttacher.getHolderUnwrap(player).setSpirituality(getSpirituality(living) + spirituality);
            } else if (living instanceof PlayerMobEntity playerMobEntity) {
                playerMobEntity.setSpirituality(getSpirituality(playerMobEntity) + spirituality);
            } else {
                if (living.level() instanceof ServerLevel serverLevel) {
                    BeyonderEntityData mappingData = BeyonderEntityData.getInstance(serverLevel);
                    String pathwayString = mappingData.getStringForEntity(living.getType());
                    if (pathwayString != null) {
                        int spiritualityLevel = living.getPersistentData().getInt("lotmSpirituality");
                        living.getPersistentData().putInt("lotmSpirituality", Math.min(getMaxSpirituality(living), spiritualityLevel + spirituality));
                    }
                }
            }
        }
    }

    public static int getSpirituality(LivingEntity living) {
        if (!living.level().isClientSide()) {
            if (living instanceof Player player) {
                return (int) BeyonderHolderAttacher.getHolderUnwrap(player).getSpirituality();
            } else if (living instanceof PlayerMobEntity playerMobEntity) {
                return playerMobEntity.getSpirituality();
            } else {
                if (living.level() instanceof ServerLevel serverLevel) {
                    BeyonderEntityData mappingData = BeyonderEntityData.getInstance(serverLevel);
                    String pathwayString = mappingData.getStringForEntity(living.getType());
                    if (pathwayString != null) {
                        return living.getPersistentData().getInt("lotmSpirituality");
                    }
                }
            }
        }
        return 0;
    }

    public static int getMaxSpirituality(LivingEntity living) { //marked
        if (!living.level().isClientSide()) {
            BeyonderClass pathway = getPathway(living);
            if (pathway != null) {
                return pathway.spiritualityLevels().get(getSequence(living));
            }
        }
        return 0;
    }

    public static final Map<Item, Potion> EFFECT_INGREDIENTS = new HashMap<>() {{
        put(Items.SUGAR, Potions.SWIFTNESS);
        put(Items.RABBIT_FOOT, Potions.LEAPING);
        put(Items.GLISTERING_MELON_SLICE, Potions.HEALING);
        put(Items.SPIDER_EYE, Potions.POISON);
        put(Items.PUFFERFISH, Potions.WATER_BREATHING);
        put(Items.MAGMA_CREAM, Potions.FIRE_RESISTANCE);
        put(Items.GOLDEN_CARROT, Potions.NIGHT_VISION);
        put(Items.BLAZE_POWDER, Potions.STRENGTH);
        put(Items.GHAST_TEAR, Potions.REGENERATION);
        put(Items.TURTLE_HELMET, Potions.TURTLE_MASTER);
        put(Items.PHANTOM_MEMBRANE, Potions.SLOW_FALLING);
    }};

    public static Potion getPotionForIngredient(Item ingredient) {
        return EFFECT_INGREDIENTS.get(ingredient);
    }

    public static boolean isValidPotionIngredient(Item ingredient) {
        return EFFECT_INGREDIENTS.containsKey(ingredient);
    }

    public static boolean inCombat(LivingEntity living) {
        return living.getPersistentData().getBoolean("LOTMinCombat");
    }

    public static void createSphereOfParticlesFromCenter(ServerLevel level, Vec3 center, ParticleOptions particleOptions, int particleCount, double travelDistance, float speed) {
        if (!(level instanceof ServerLevel)) {
            return;
        }

        double goldenRatio = (1 + Math.sqrt(5)) / 2;
        double angleIncrement = Math.PI * 2 * goldenRatio;

        double velocityScale = (travelDistance / 2.2) * speed;

        for (int i = 0; i < particleCount; i++) {
            double t = (double) i / particleCount;
            double inclination = Math.acos(1 - 2 * t);
            double azimuth = angleIncrement * i;
            double x = Math.sin(inclination) * Math.cos(azimuth);
            double y = Math.sin(inclination) * Math.sin(azimuth);
            double z = Math.cos(inclination);
            double velocityX = -x * velocityScale;
            double velocityY = -y * velocityScale;
            double velocityZ = -z * velocityScale;
            level.sendParticles(particleOptions, center.x, center.y, center.z, 0, velocityX, velocityY, velocityZ, 1.0);
        }
    }

    public static void createSphereOfParticlesToCenter(Level level, Vec3 center, ParticleOptions particleOptions, int particleCount, double travelDistance, float speed) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        double goldenRatio = (1 + Math.sqrt(5)) / 2;
        double angleIncrement = Math.PI * 2 * goldenRatio;

        double velocityScale = (travelDistance / 2.2) * speed;

        for (int i = 0; i < particleCount; i++) {
            double t = (double) i / particleCount;
            double inclination = Math.acos(1 - 2 * t);
            double azimuth = angleIncrement * i;
            double x = Math.sin(inclination) * Math.cos(azimuth);
            double y = Math.sin(inclination) * Math.sin(azimuth);
            double z = Math.cos(inclination);
            double velocityX = x * velocityScale;
            double velocityY = y * velocityScale;
            double velocityZ = z * velocityScale;
            serverLevel.sendParticles(particleOptions, center.x, center.y, center.z, 0, velocityX, velocityY, velocityZ, 1.0);
        }
    }

    public static List<LivingEntity> checkEntitiesInLocation(LivingEntity livingEntity, float inflation, float X, float Y, float Z) {
        AABB box = new AABB(X - inflation, Y - inflation, Z - inflation, X + inflation, Y + inflation, Z + inflation);
        return livingEntity.level().getEntitiesOfClass(LivingEntity.class, box);
    }

    public static ChatFormatting ageStyle(LivingEntity livingEntity) {
        ChatFormatting style = ChatFormatting.YELLOW;
        if (!livingEntity.level().isClientSide()) {
            int age = livingEntity.getPersistentData().getInt("age");
            int sequence = getSequence(livingEntity);
            int maxAge = 20;
            if (sequence == 9) {
                maxAge = 40;
            } else if (sequence == 8) {
                maxAge = 80;
            } else if (sequence == 7) {
                maxAge = 150;
            } else if (sequence == 6) {
                maxAge = 230;
            } else if (sequence == 5) {
                maxAge = 330;
            } else if (sequence == 4) {
                maxAge = 550;
            } else if (sequence == 3) {
                maxAge = 700;
            } else if (sequence == 2) {
                maxAge = 1000;
            } else if (sequence == 1) {
                maxAge = 1600;
            } else if (sequence == 0) {
                maxAge = 3000;
            }
            boolean tenPercent = age >= maxAge * 0.1;
            boolean twentyPercent = age >= maxAge * 0.2;
            boolean thirtyPercent = age >= maxAge * 0.3;
            boolean fortyPercent = age >= maxAge * 0.4;
            boolean fiftyPercent = age >= maxAge * 0.5;
            boolean sixtyPercent = age >= maxAge * 0.6;
            boolean seventyPercent = age >= maxAge * 0.7;
            boolean eightyPercent = age >= maxAge * 0.8;
            boolean ninetyPercent = age >= maxAge * 0.9;
            boolean oneHundredPercent = age >= maxAge;
            if (tenPercent) {
                style = ChatFormatting.YELLOW;
            }
            if (twentyPercent) {
                style = ChatFormatting.YELLOW;
            }
            if (thirtyPercent) {
                style = ChatFormatting.YELLOW;
            }
            if (fortyPercent) {
                style = ChatFormatting.RED;
            }
            if (fiftyPercent) {
                style = ChatFormatting.RED;
            }
            if (sixtyPercent) {
                style = ChatFormatting.RED;
            }
            if (seventyPercent) {
                style = ChatFormatting.DARK_RED;
            }
            if (eightyPercent) {
                style = ChatFormatting.DARK_RED;
            }
            if (ninetyPercent) {
                style = ChatFormatting.DARK_RED;
            }
            if (oneHundredPercent) {
                style = ChatFormatting.DARK_RED;
            }
        }
        return style;
    }


    public static ChatFormatting corruptionStyle(LivingEntity livingEntity) {
        ChatFormatting style = ChatFormatting.YELLOW;
        if (!livingEntity.level().isClientSide()) {
            int corruption = livingEntity.getPersistentData().getInt("corruption");
            int maxCorruption = 100;
            boolean tenPercent = corruption >= maxCorruption * 0.1;
            boolean twentyPercent = corruption >= maxCorruption * 0.2;
            boolean thirtyPercent = corruption >= maxCorruption * 0.3;
            boolean fortyPercent = corruption >= maxCorruption * 0.4;
            boolean fiftyPercent = corruption >= maxCorruption * 0.5;
            boolean sixtyPercent = corruption >= maxCorruption * 0.6;
            boolean seventyPercent = corruption >= maxCorruption * 0.7;
            boolean eightyPercent = corruption >= maxCorruption * 0.8;
            boolean ninetyPercent = corruption >= maxCorruption * 0.9;
            boolean oneHundredPercent = corruption >= maxCorruption;
            if (tenPercent) {
                style = ChatFormatting.YELLOW;
            }
            if (twentyPercent) {
                style = ChatFormatting.YELLOW;
            }
            if (thirtyPercent) {
                style = ChatFormatting.YELLOW;
            }
            if (fortyPercent) {
                style = ChatFormatting.RED;
            }
            if (fiftyPercent) {
                style = ChatFormatting.RED;
            }
            if (sixtyPercent) {
                style = ChatFormatting.RED;
            }
            if (seventyPercent) {
                style = ChatFormatting.DARK_RED;
            }
            if (eightyPercent) {
                style = ChatFormatting.DARK_RED;
            }
            if (ninetyPercent) {
                style = ChatFormatting.DARK_RED;
            }
            if (oneHundredPercent) {
                style = ChatFormatting.DARK_RED;
            }
        }
        return style;
    }

    public static void ageHandlerTick(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        int age = tag.getInt("age");
        int sequence = getSequence(livingEntity);
        int maxAge = 20;
        if (sequence == 9) {
            maxAge = 40;
        } else if (sequence == 8) {
            maxAge = 80;
        } else if (sequence == 7) {
            maxAge = 150;
        } else if (sequence == 6) {
            maxAge = 230;
        } else if (sequence == 5) {
            maxAge = 330;
        } else if (sequence == 4) {
            maxAge = 550;
        } else if (sequence == 3) {
            maxAge = 700;
        } else if (sequence == 2) {
            maxAge = 1000;
        } else if (sequence == 1) {
            maxAge = 1600;
        } else if (sequence == 0) {
            maxAge = 3000;
        }
        boolean tenPercent = age >= maxAge * 0.1;
        boolean twentyPercent = age >= maxAge * 0.2;
        boolean thirtyPercent = age >= maxAge * 0.3;
        boolean fortyPercent = age >= maxAge * 0.4;
        boolean fiftyPercent = age >= maxAge * 0.5;
        boolean sixtyPercent = age >= maxAge * 0.6;
        boolean seventyPercent = age >= maxAge * 0.7;
        boolean eightyPercent = age >= maxAge * 0.8;
        boolean ninetyPercent = age >= maxAge * 0.9;
        boolean oneHundredPercent = age >= maxAge;
        if (!livingEntity.level().isClientSide()) {
            int ageDecay = tag.getInt("ageDecay");
            if (ageDecay >= 1) {
                tag.putInt("ageDecay", ageDecay - 1);
                for (int i = 0; i <= 20; i++) {
                    double random = (Math.random() * 4) - 2;
                    double scaleAmount = Math.max(2, ScaleTypes.BASE.getScaleData(livingEntity).getScale() * Math.random() * 2);
                    double x = livingEntity.getX() + ((scaleAmount * Math.random()) - (scaleAmount * Math.random()));
                    double y = livingEntity.getY() + ((scaleAmount * Math.random()) - (scaleAmount * Math.random()));
                    double z = livingEntity.getZ() + ((scaleAmount * Math.random()) - (scaleAmount * Math.random()));
                    LOTMNetworkHandler.sendToAllPlayers(new SendParticleS2C(ParticleTypes.WHITE_ASH, x, y, z, random, random, random));
                }
                if (ageDecay == 1) {
                    for (int i = 0; i <= 150; i++) {
                        double random = (Math.random() * 4) - 2;
                        double scaleAmount = Math.max(2, ScaleTypes.BASE.getScaleData(livingEntity).getScale() * Math.random() * 2);
                        double x = livingEntity.getX() + ((scaleAmount * Math.random()) - (scaleAmount * Math.random()));
                        double y = livingEntity.getY() + ((scaleAmount * Math.random()) - (scaleAmount * Math.random()));
                        double z = livingEntity.getZ() + ((scaleAmount * Math.random()) - (scaleAmount * Math.random()));
                        LOTMNetworkHandler.sendToAllPlayers(new SendParticleS2C(ParticleTypes.WHITE_ASH, x, y, z, random, random, random));
                    }
                    tag.putInt("age", 0);
                    tag.putInt("ageDecay", 0);
                    LivingEntity living = null;
                    if (tag.contains("ageUUID")) {
                        LivingEntity living1 = getLivingEntityFromUUID(livingEntity.level(), tag.getUUID("ageUUID"));
                        if (living1 != null) {
                            living = living1;
                        }
                    }
                    if (living == null) {
                        livingEntity.kill();
                    } else {
                        livingEntity.die(livingEntity.damageSources().mobAttack(living));
                        if (livingEntity != null) {
                            livingEntity.kill();
                        }
                    }
                    livingEntity.sendSystemMessage(Component.literal("You died due to aging."));
                }
            }
            if (age >= 1) {
                if (livingEntity.tickCount % 20 == 0) {
                    tag.putInt("age", Math.max(0, age - (10 - sequence)));
                }
                if (oneHundredPercent) {
                    tag.putInt("age", 0);
                    tag.putInt("ageDecay", 100);
                }

                if (ninetyPercent) {
                    BeyonderUtil.applyMobEffect(livingEntity, ModEffects.ABILITY_WEAKNESS.get(), 20, 2, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SLOWDOWN, 20, 4, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.WEAKNESS, 20, 4, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.DIG_SLOWDOWN, 20, 3, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.WITHER, 20, 5, true, true);
                    BeyonderUtil.applyNoRegeneration(livingEntity, 20);
                } else if (eightyPercent) {
                    BeyonderUtil.applyMobEffect(livingEntity, ModEffects.ABILITY_WEAKNESS.get(), 20, 2, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SLOWDOWN, 20, 3, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.WEAKNESS, 20, 4, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.DIG_SLOWDOWN, 20, 3, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.WITHER, 20, 4, true, true);
                    BeyonderUtil.applyNoRegeneration(livingEntity, 20);
                } else if (seventyPercent) {
                    BeyonderUtil.applyMobEffect(livingEntity, ModEffects.ABILITY_WEAKNESS.get(), 20, 1, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SLOWDOWN, 20, 3, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.WEAKNESS, 20, 3, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.DIG_SLOWDOWN, 20, 2, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.WITHER, 20, 3, true, true);
                    BeyonderUtil.applyNoRegeneration(livingEntity, 20);
                } else if (sixtyPercent) {
                    BeyonderUtil.applyNoRegeneration(livingEntity, 20);
                    BeyonderUtil.applyMobEffect(livingEntity, ModEffects.ABILITY_WEAKNESS.get(), 20, 1, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SLOWDOWN, 20, 3, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.WEAKNESS, 20, 3, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.DIG_SLOWDOWN, 20, 1, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.WITHER, 20, 2, true, true);
                } else if (fiftyPercent) {
                    BeyonderUtil.applyMobEffect(livingEntity, ModEffects.ABILITY_WEAKNESS.get(), 20, 1, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SLOWDOWN, 20, 2, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.WEAKNESS, 20, 2, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.DIG_SLOWDOWN, 20, 1, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.WITHER, 20, 1, true, true);

                } else if (fortyPercent) {
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.WITHER, 20, 0, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, ModEffects.ABILITY_WEAKNESS.get(), 20, 1, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SLOWDOWN, 20, 1, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.WEAKNESS, 20, 2, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.DIG_SLOWDOWN, 20, 0, true, true);
                } else if (thirtyPercent) {
                    BeyonderUtil.applyMobEffect(livingEntity, ModEffects.ABILITY_WEAKNESS.get(), 20, 1, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SLOWDOWN, 20, 1, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.WEAKNESS, 20, 1, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.DIG_SLOWDOWN, 20, 0, true, true);
                } else if (twentyPercent) {
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SLOWDOWN, 20, 0, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.WEAKNESS, 20, 0, true, true);
                } else if (tenPercent) {
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SLOWDOWN, 20, 0, true, true);
                }
            }
            if (sequence <= 2 && sequence != -1 && livingEntity instanceof Player player) {
                if (player.getFoodData().getFoodLevel() <= 8) {
                    player.getFoodData().setFoodLevel(9);
                }
            }
        }
    }

    public static void ageHandlerHurt(LivingHurtEvent event) {
        if (!event.getEntity().level().isClientSide() && event.getEntity().getPersistentData().getInt("age") >= event.getEntity().getMaxHealth() * 5) {
            if (isPhysicalDamage(event.getSource())) {
                event.setAmount(event.getAmount() * 1.5f);
            }
        }
    }

    public static boolean scribeLookingAtYou(LivingEntity target, LivingEntity scribe) {
        double radius = 30.0;
        double angleThreshold = 45.0;
        if (currentPathwayAndSequenceMatchesNoException(scribe, BeyonderClassInit.APPRENTICE.get(), 6)) {
            Vec3 scribePos = scribe.position();
            Vec3 targetPos = target.position();
            double distanceQr = scribePos.distanceToSqr(targetPos);
            if (distanceQr > radius * radius) {
                return false;
            }
            Vec3 lookVec = scribe.getLookAngle().normalize();
            Vec3 toTargetVec = targetPos.subtract(scribePos).normalize();
            double dotProduct = lookVec.dot(toTargetVec);
            double angle = Math.toDegrees(Math.acos(dotProduct));

            return angle < angleThreshold;
        }
        return false;
    }

    public static boolean isBeyonder(LivingEntity livingEntity) {
        return getSequence(livingEntity) != -1 && getPathway(livingEntity) != null;
    }

    public static void resetPathway(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            if (livingEntity instanceof Player player) {
                BeyonderHolderAttacher.getHolderUnwrap(player).removePathway();
            } else if (livingEntity instanceof PlayerMobEntity playerMobEntity) {
                playerMobEntity.setPathway(null);
                playerMobEntity.setSequence(-1);
            } else {
                livingEntity.getPersistentData().putInt("separateEntitySequence", 0);
                livingEntity.getPersistentData().putString("separateEntityPathway", "");
            }
        }
    }

    public static boolean isSmeltable(ItemStack itemStack, Level world) {
        SimpleContainer container = new SimpleContainer(itemStack);
        return world.getRecipeManager().getRecipeFor(RecipeType.SMELTING, container, world).isPresent();
    }

    public static ItemStack getSmeltingResult(ItemStack itemStack, Level world) {
        SimpleContainer container = new SimpleContainer(itemStack);
        return world.getRecipeManager().getRecipeFor(RecipeType.SMELTING, container, world).map(recipe -> recipe.getResultItem(world.registryAccess())).orElse(ItemStack.EMPTY);
    }

    public static Boolean isEntityColliding(Entity entity, Level level, Double radius) {
        AABB entityBoundingBox = entity.getBoundingBox();
        for (Entity otherEntity : level.getEntitiesOfClass(Entity.class, entity.getBoundingBox().inflate(radius))) {
            if (entityBoundingBox.intersects(otherEntity.getBoundingBox())) {
                return true;
            }
        }
        return false;
    }

    public static Entity checkEntityCollision(Entity entity, Level level, Double radius) {
        AABB entityBoundingBox = entity.getBoundingBox();
        AABB searchArea = entityBoundingBox.inflate(radius);
        for (Entity otherEntity : level.getEntities(entity, searchArea, otherEntity -> true)) {
            if (entityBoundingBox.intersects(otherEntity.getBoundingBox())) {
                return otherEntity;
            }
        }
        return null;
    }


    public static void teleportEntityThroughDimensions(Entity entity, ResourceLocation dimensionKey, double x, double y, double z) {
        if (entity == null || entity.level().isClientSide()) {
            return;
        }

        ServerLevel currentWorld = (ServerLevel) entity.level();
        MinecraftServer server = currentWorld.getServer();
        ResourceKey<Level> dimResourceKey = ResourceKey.create(Registries.DIMENSION, dimensionKey);
        ServerLevel destinationWorld = server.getLevel(dimResourceKey);

        if (destinationWorld == null) {
            return;
        }

        if (entity instanceof ServerPlayer player) {
            player.teleportTo(destinationWorld, x, y, z, player.getYRot(), player.getXRot());
        } else {
            Entity newEntity = entity.getType().create(destinationWorld);
            if (newEntity != null) {
                newEntity.restoreFrom(entity);
                newEntity.moveTo(x, y, z, entity.getYRot(), entity.getXRot());
                entity.discard();
                destinationWorld.addFreshEntity(newEntity);
            }
        }
    }

    public static void teleportEntityTroughDimensionsChat(Entity entity, ResourceLocation dimensionKey, double x, double y, double z) {
        if (entity == null || entity.level().isClientSide()) {
            return;
        }

        ServerLevel currentWorld = (ServerLevel) entity.level();
        MinecraftServer server = currentWorld.getServer();
        ResourceKey<Level> dimResourceKey = ResourceKey.create(Registries.DIMENSION, dimensionKey);
        ServerLevel destinationWorld = server.getLevel(dimResourceKey);

        if (destinationWorld == null) {
            return;
        }

        if (entity instanceof ServerPlayer player) {
            player.teleportTo(destinationWorld, x, y, z, player.getYRot(), player.getXRot());
        } else {
            Entity newEntity = entity.getType().create(destinationWorld);
            if (newEntity != null) {
                newEntity.restoreFrom(entity);
                newEntity.moveTo(x, y, z, entity.getYRot(), entity.getXRot());
                entity.discard();
                destinationWorld.addFreshEntity(newEntity);
            }
        }
    }

    public static LivingEntity getEntityFromUUID(Level level, UUID uuid) {
        if (level instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(uuid);
            if (entity instanceof LivingEntity livingEntity) {
                return livingEntity;
            }
        }
        return null;
    }

    public static boolean currentPathwayMatches(LivingEntity livingEntity, BeyonderClass matchingPathway) {
        if (getPathway(livingEntity) == matchingPathway || (getPathway(livingEntity) == BeyonderClassInit.APPRENTICE.get() && getSequence(livingEntity) <= 6)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean currentPathwayMatchesNoException(LivingEntity livingEntity, BeyonderClass matchingPathway) {
        if (getPathway(livingEntity) == matchingPathway) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean currentPathwayAndSequenceMatches(LivingEntity livingEntity, BeyonderClass matchingPathway, int sequence) {
        if ((getPathway(livingEntity) == matchingPathway && getSequence(livingEntity) <= sequence) || (getPathway(livingEntity) == BeyonderClassInit.APPRENTICE.get() && getSequence(livingEntity) <= 6)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean currentPathwayAndSequenceMatchesNoException(LivingEntity livingEntity, BeyonderClass matchingPathway, int sequence) {
        if (getPathway(livingEntity) == matchingPathway && getSequence(livingEntity) <= sequence) {
            return true;
        } else {
            return false;
        }
    }

    public static void setSpirituality(LivingEntity livingEntity, int spirituality) {
        if (!livingEntity.level().isClientSide()) {
            if (livingEntity instanceof Player player) {
                BeyonderHolderAttacher.getHolderUnwrap(player).setSpirituality(spirituality);
            } else if (livingEntity instanceof PlayerMobEntity playerMobEntity) {
                playerMobEntity.setSpirituality(spirituality);
            } else {
                if (livingEntity.level() instanceof ServerLevel serverLevel) {
                    BeyonderEntityData mappingData = BeyonderEntityData.getInstance(serverLevel);
                    String pathwayString = mappingData.getStringForEntity(livingEntity.getType());
                    if (pathwayString != null) {
                        livingEntity.getPersistentData().putInt("lotmSpirituality", spirituality);
                    }
                }
            }
        }
    }

    public static boolean sequenceAbleCopy(LivingEntity entity) {
        int sequence = getSequence(entity);
        return getPathway(entity) == BeyonderClassInit.APPRENTICE.get() && sequence <= 6;
    }

    public static boolean sequenceAbleCopy(BeyonderHolder holder) {
        int sequence = holder.getSequence();
        if (holder.currentClassMatches(BeyonderClassInit.APPRENTICE.get()) && sequence <= 6) {
            return true;
        }
        return false;
    }

    public static void copyAbilities(Level level, LivingEntity living, SimpleAbilityItem ability) { //marked
        int abilitySequence = ability.getRequiredSequence();
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, living.getBoundingBox().inflate(50))) {
            if (entity == living) {
                continue;
            }
            if (BeyonderUtil.isBeyonderCapable(entity)) {
                if (currentPathwayAndSequenceMatchesNoException(entity, BeyonderClassInit.APPRENTICE.get(), 6)) {
                    if (BeyonderUtil.scribeLookingAtYou(living, entity)) {
                        if (checkValidAbilityCopy(new ItemStack(ability))) {
                            if (ScribedUtils.getAbilitiesCount(living) < entity.getPersistentData().getInt("maxScribedAbilities")) {
                                if (copyAbilityTest(entity, getSequence(entity), abilitySequence)) {
                                    if (!pendingAbilityCopies.containsKey(entity.getUUID())) {
                                        entity.getPersistentData().putInt("timerCopiedAbility", 200);
                                        pendingAbilityCopies.put(entity.getUUID(), ability);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void confirmCopyAbility(Player player) {
        if (pendingAbilityCopies.containsKey(player.getUUID())) {
            if (!player.isShiftKeyDown()) {
                player.getPersistentData().putBoolean("acceptCopiedAbility", true);
            } else {
                player.getPersistentData().putBoolean("deleteCopiedAbility", true);
            }
        }
    }


    public static void copyAbilityTick(Player player) {
        Iterator<Map.Entry<UUID, SimpleAbilityItem>> iterator = pendingAbilityCopies.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, SimpleAbilityItem> entry = iterator.next();
            SimpleAbilityItem ability = entry.getValue();
            UUID uuid = entry.getKey();
            if (player.getUUID().equals(uuid)) {
                if (player.getPersistentData().getInt("timerCopiedAbility") > 0) {
                    player.getPersistentData().putInt("timerCopiedAbility", player.getPersistentData().getInt("timerCopiedAbility") - 1);
                    player.displayClientMessage(Component.literal("Trying to copy: ").withStyle(ChatFormatting.GREEN).withStyle(ChatFormatting.BOLD).append(Component.literal(ability.getDefaultInstance().getHoverName().getString()).withStyle(ChatFormatting.WHITE).withStyle(ChatFormatting.BOLD)), true);
                    if (player.getPersistentData().getBoolean("acceptCopiedAbility")) {
                        player.getPersistentData().putBoolean("acceptCopiedAbility", false);
                        player.displayClientMessage(Component.literal("You have copied: ").withStyle(ChatFormatting.GREEN).withStyle(ChatFormatting.BOLD).append(Component.literal(ability.getDefaultInstance().getHoverName().getString()).withStyle(ChatFormatting.WHITE).withStyle(ChatFormatting.BOLD)), true);
                        ScribedUtils.copyAbility(player, ability);
                        iterator.remove();
                    } else if (player.getPersistentData().getBoolean("deleteCopiedAbility")) {
                        player.getPersistentData().putBoolean("deleteCopiedAbility", false);
                        player.displayClientMessage(Component.literal("You have given up on copying: ").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD).append(Component.literal(ability.getDefaultInstance().getHoverName().getString()).withStyle(ChatFormatting.WHITE).withStyle(ChatFormatting.BOLD)), true);
                        iterator.remove();
                    }
                } else {
                    player.getPersistentData().remove("timerCopiedAbility");
                    player.displayClientMessage(Component.literal("You have run out of time to copy: ").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD).append(Component.literal(ability.getDefaultInstance().getHoverName().getString()).withStyle(ChatFormatting.WHITE).withStyle(ChatFormatting.BOLD)), true);
                    iterator.remove();
                }
            }
        }
    }

    public static void useCopiedAbility(LivingEntity living, Item ability) { //marked
        if (living instanceof Player player) {
            if (currentPathwayAndSequenceMatchesNoException(player, BeyonderClassInit.APPRENTICE.get(), 6)) {
                ScribedUtils.useScribedAbility(player, ability);
                if (ScribedUtils.getRemainingUses(player, ability) == 0) {
                    if (player.getMainHandItem().getItem() == ability) {
                        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    }
                }
            }
        }
    }

    public static boolean checkAbilityIsCopied(LivingEntity living, Item ability) { //marked
        return ScribedUtils.hasAbility(living, ability);
    }


    public static boolean copyAbilityTest(LivingEntity living, int copierSequence, int targetAbilitySequence) {
        float damage = BeyonderUtil.getDamage(living).get(ItemInit.RECORDSCRIBE.get());
        double chance = (0.3 + (0.7 / 9) * (targetAbilitySequence - copierSequence)) * damage;
        chance = Math.max(0.05, Math.min(chance, 1));
        if (copierSequence < targetAbilitySequence - 2) {
            chance = 1.01;
        } else if (targetAbilitySequence <= 4 && copierSequence > 4) {
            chance = chance / 2;
        }
        double random = Math.random();
        return random < chance;
    }


    public static boolean checkValidAbilityCopy(ItemStack ability) {
        List<ItemStack> invalidAbilities = new ArrayList<>();
        invalidAbilities.add(new ItemStack(ItemInit.GIGANTIFICATION.get()));
        invalidAbilities.add(new ItemStack(ItemInit.FATEDCONNECTION.get()));

        for (ItemStack invalidAbility : invalidAbilities) {
            if (ItemStack.isSameItem(ability, invalidAbility)) {
                return false;
            }
        }
        return true;
    }

    public static void setSequence(LivingEntity livingEntity, int sequence) {
        if (!livingEntity.level().isClientSide()) {
            if (livingEntity instanceof Player player) {
                BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
                holder.setSequence(sequence);
            } else if (livingEntity instanceof PlayerMobEntity playerMobEntity) {
                playerMobEntity.setSequence(sequence);
            } else {
                livingEntity.getPersistentData().putInt("separateEntitySequence", sequence);
            }
        }
    }

    public static void setPathwayAndSequence(LivingEntity livingEntity, BeyonderClass pathway, int sequence) {
        if (!livingEntity.level().isClientSide()) {
            if (livingEntity instanceof Player player) {
                BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
                holder.setSequence(sequence);
                holder.setPathway(pathway);
            } else if (livingEntity instanceof PlayerMobEntity playerMobEntity) {
                playerMobEntity.setSequence(sequence);
                playerMobEntity.setPathway(pathway);
            } else {
                return;
            }
        }
    }

    public static void removePathway(LivingEntity living) {
        if (living instanceof Player player) {
            BeyonderHolderAttacher.getHolderUnwrap(player).removePathway();
        } else {
            setPathwayAndSequence(living, null, -1);
        }
        ScaleTypes.BASE.getScaleData(living).setScale(1);
        if (living instanceof Player player) {
            Abilities playerAbilities = player.getAbilities();
            playerAbilities.setFlyingSpeed(0.05F);
            playerAbilities.setWalkingSpeed(0.1F);
            player.onUpdateAbilities();
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundPlayerAbilitiesPacket(playerAbilities));
            }
        }
        BeyonderUtil.removeTags(living);
    }


    public static void setScale(Entity entity, float scale) {
        if (!entity.level().isClientSide()) {
            ScaleTypes.BASE.getScaleData(entity).setScale(scale);
        }
    }

    public static float getScale(Entity entity) {
        float scale = 1;
        if (!entity.level().isClientSide()) {
            scale = ScaleTypes.BASE.getScaleData(entity).getScale();
        }
        return scale;
    }

    public static void setTargetScale(Entity entity, float scale) {
        if (!entity.level().isClientSide()) {
            ScaleTypes.BASE.getScaleData(entity).setTargetScale(scale);
        }
    }

    public static float getRandomInRange(float range) { //If I put 4, it can return -4,-3,-2,-1,0,1,2,3,3.9999
        float random = (float) Math.random();
        return (random * 2 * range) - range;
    }

    public static float getPositiveRandomInRange(float range) { //If I put 4, it can return 0,1,2,3,3.9999
        float random = (float) Math.random();
        return (range * random);
    }

    public static void destroyBlocksInSphere(Entity entity, BlockPos hitPos, double radius, float damage) {
        for (BlockPos pos : BlockPos.betweenClosed(
                hitPos.offset((int) -radius, (int) -radius, (int) -radius),
                hitPos.offset((int) radius, (int) radius, (int) radius))) {
            if (pos.distSqr(hitPos) <= radius * radius) {
                if (entity.level().getBlockState(pos).getDestroySpeed(entity.level(), pos) >= 0 && entity.level().getBlockState(pos).getDestroySpeed(entity.level(), pos) <= 51) {
                    entity.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        List<Entity> entities = entity.level().getEntities(entity, new AABB(hitPos.offset((int) -radius, (int) -radius, (int) -radius), hitPos.offset((int) radius, (int) radius, (int) radius)));
        for (Entity pEntity : entities) {
            if (pEntity instanceof LivingEntity livingEntity) {
                double distance = Math.sqrt(pEntity.blockPosition().distSqr(hitPos));
                double normalizedDistance = Math.min(distance / radius, 1.0);
                float damageMultiplier = (float) (1.0 - (0.6 * normalizedDistance));
                float finalDamage = damage * damageMultiplier;
                if (entity instanceof Projectile projectile) {
                    if (projectile.getOwner() == null) {
                        livingEntity.hurt(BeyonderUtil.genericSource(projectile, livingEntity), finalDamage);
                    } else {
                        livingEntity.hurt(BeyonderUtil.genericSource(projectile.getOwner(), livingEntity), finalDamage);
                    }
                } else {
                    livingEntity.hurt(BeyonderUtil.genericSource(entity, livingEntity), finalDamage);
                }
            }
        }
    }

    public static void destroyBlocksInSphereNotHittingOwner(Entity entity, BlockPos hitPos, double radius, float damage) {
        for (BlockPos pos : BlockPos.betweenClosed(
                hitPos.offset((int) -radius, (int) -radius, (int) -radius),
                hitPos.offset((int) radius, (int) radius, (int) radius))) {
            if (pos.distSqr(hitPos) <= radius * radius) {
                if (entity.level().getBlockState(pos).getDestroySpeed(entity.level(), pos) >= 0 && entity.level().getBlockState(pos).getDestroySpeed(entity.level(), pos) <= 51) {
                    entity.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        List<Entity> entities = entity.level().getEntities(entity, new AABB(hitPos.offset((int) -radius, (int) -radius, (int) -radius), hitPos.offset((int) radius, (int) radius, (int) radius)));
        for (Entity pEntity : entities) {
            if (pEntity == entity) {
                continue;
            }
            if (pEntity instanceof LivingEntity livingEntity) {
                double distance = Math.sqrt(pEntity.blockPosition().distSqr(hitPos));
                double normalizedDistance = Math.min(distance / radius, 1.0);
                float damageMultiplier = (float) (1.0 - (0.6 * normalizedDistance));
                float finalDamage = damage * damageMultiplier;
                if (entity instanceof Projectile projectile) {
                    if (projectile.getOwner() == null) {
                        livingEntity.hurt(BeyonderUtil.genericSource(projectile, livingEntity), finalDamage);
                    } else {
                        livingEntity.hurt(BeyonderUtil.genericSource(projectile.getOwner(), livingEntity), finalDamage);
                    }
                } else {
                    livingEntity.hurt(BeyonderUtil.genericSource(entity, livingEntity), finalDamage);
                }
            }
        }
    }

    public static int chatFormatingToInt(ChatFormatting color) {
        return COLOR_MAP.getOrDefault(color, 0xFFFFFF);
    }

    public static String getPathwayName(BeyonderClass pathway) {
        return pathway.sequenceNames().get(9);
    }

    public static BeyonderClass getPathwayByName(String name) {
        return NAME_TO_BEYONDER.getOrDefault(name, new SeerClass());
    }

    public static void useAvailableAbilityAsMob(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide() && getPathway(livingEntity) != null && livingEntity instanceof Mob mob) {
            boolean shouldntActiveCalamity = true;
            boolean allowBeyonderAbilitiesNearSpawn = livingEntity.level().getGameRules().getBoolean(GameRuleInit.SHOULD_BEYONDER_ABILITY_NEAR_SPAWN);
            if (!allowBeyonderAbilitiesNearSpawn) {
                BlockPos entityPos = livingEntity.getOnPos();
                BlockPos worldSpawnPos = livingEntity.level().getSharedSpawnPos();
                if (entityPos.closerThan(worldSpawnPos, 300)) {
                    shouldntActiveCalamity = false;
                }
            }
            if (!shouldntActiveCalamity) {
                LOTM.LOGGER.info(mob.getName().getString() + " couldn't use ability as it's too close to spawn.");
                return;
            }
            ItemStack heldItem = livingEntity.getItemInHand(InteractionHand.MAIN_HAND);
            if (!(heldItem.getItem() instanceof SimpleAbilityItem simpleAbilityItem)) {
                return;
            }
            if (hasStun(livingEntity)) {
                return;
            }
            if (!SimpleAbilityItem.checkAll(livingEntity, simpleAbilityItem.getRequiredPathway(),
                    simpleAbilityItem.getRequiredSequence(), simpleAbilityItem.getRequiredSpirituality(), false)) {
                return;
            }
            double entityReach = simpleAbilityItem.getEntityReach();
            double blockReach = simpleAbilityItem.getBlockReach();
            boolean successfulUse = false;
            boolean hasEntityInteraction = false;
            boolean hasBlockInteraction = false;
            boolean hasGeneralAbility = false;

            try {
                Method entityMethod = simpleAbilityItem.getClass().getDeclaredMethod("useAbilityOnEntity", ItemStack.class, LivingEntity.class, LivingEntity.class, InteractionHand.class);
                hasEntityInteraction = !entityMethod.equals(Ability.class.getDeclaredMethod("useAbilityOnEntity", ItemStack.class, LivingEntity.class, LivingEntity.class, InteractionHand.class));
            } catch (NoSuchMethodException ignored) {
            }
            try {
                Method blockMethod = simpleAbilityItem.getClass().getDeclaredMethod("useAbilityOnBlock", UseOnContext.class);
                hasBlockInteraction = !blockMethod.equals(Ability.class.getDeclaredMethod("useAbilityOnBlock", UseOnContext.class));
            } catch (NoSuchMethodException ignored) {
            }

            try {
                Method generalMethod = simpleAbilityItem.getClass().getDeclaredMethod("useAbility", Level.class, LivingEntity.class, InteractionHand.class);
                hasGeneralAbility = !generalMethod.equals(Ability.class.getDeclaredMethod("useAbility", Level.class, LivingEntity.class, InteractionHand.class));
            } catch (NoSuchMethodException ignored) {
            }

            LivingEntity target = mob.getTarget();
            if (hasEntityInteraction) {
                if (target != null && target.distanceTo(mob) <= entityReach) {
                    InteractionResult result = simpleAbilityItem.useAbilityOnEntity(heldItem, mob, target, InteractionHand.MAIN_HAND);
                    if (result != InteractionResult.PASS) {
                        successfulUse = true;
                    }
                }
            }

            if (!successfulUse && hasBlockInteraction) {
                Vec3 eyePosition = livingEntity.getEyePosition();
                Vec3 lookVector = livingEntity.getLookAngle();
                Vec3 reachVector = eyePosition.add(lookVector.x * blockReach, lookVector.y * blockReach, lookVector.z * blockReach);
                BlockHitResult blockHit = livingEntity.level().clip(new ClipContext(eyePosition, reachVector,
                        ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, livingEntity));
                if (blockHit.getType() != HitResult.Type.MISS) {
                    UseOnContext context = new UseOnContext(
                            livingEntity.level(),
                            null,
                            InteractionHand.MAIN_HAND,
                            heldItem,
                            blockHit
                    );
                    InteractionResult result = simpleAbilityItem.useOn(context);
                    if (result != InteractionResult.PASS) {
                        successfulUse = true;
                    }
                }
            }

            String itemName = simpleAbilityItem.getDescription().getString();
            String mobName = mob.getName().getString();
            String message = mobName + " used " + itemName;
            if ((hasEntityInteraction || hasBlockInteraction) && !hasGeneralAbility) {
                if (successfulUse) {
                    LOTM.LOGGER.info(message);
                }
            } else if (!hasEntityInteraction && !hasBlockInteraction) {
                LOTM.LOGGER.info(message);
                simpleAbilityItem.useAbility(mob.level(), mob, InteractionHand.MAIN_HAND);
            } else if (successfulUse) {
                LOTM.LOGGER.info(message);
            } else {
                LOTM.LOGGER.info(message);
                simpleAbilityItem.useAbility(mob.level(), mob, InteractionHand.MAIN_HAND);
            }
        }
    }

    public static ApprenticeDoorEntity getDoorFromUUID(Level level, UUID uuid) {
        if (level instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(uuid);
            if (entity instanceof ApprenticeDoorEntity door) {
                return door;
            }
        }
        return null;
    }

    public static boolean isConcealed(LivingEntity entity) {
        return entity.level().dimension() == DimensionInit.CONCEALED_SPACE_LEVEL_KEY;
    }

    public static void removeTags(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            CompoundTag tag = livingEntity.getPersistentData();
            for (LivingEntity living : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate(500))) {
                if (living.getPersistentData().contains("divineHandUUID")) {
                    if (living.getPersistentData().getUUID("divineHandUUID").equals(livingEntity.getUUID())) {
                        living.getPersistentData().putInt("divineHandGuarding", 0);
                    }
                }
            }
            tag.putInt("spaceFragmentationCopies", 0);
            tag.putInt("invisibleHandCounter", 0);
            tag.putDouble("invisibleHandDistance", 0);
            tag.putInt("travelBlinkDistance", 0);
            TravelersDoorWaypoint.clearAllWaypoints(livingEntity);
            tag.putBoolean("monsterAuraOfChaos", false);
            tag.putBoolean("monsterChaosWalkerCombat", false);
            tag.putInt("monsterCyclePotionEffectsCount", 0);
            tag.putInt("monsterCycleOfFateUser", 0);
            removeCycleEffect(livingEntity);
            removeTwilightFreezeEffect(livingEntity);
            tag.putBoolean("monsterRipple", false);
            tag.putInt("monsterReincarnationCounter", 0);
            tag.putInt("age", 0);
            ProbabilityManipulationWipe.wipeProbablility(tag);
            tag.putInt("calamityIncarnationInMeteor", 0);
            tag.putInt("calamityIncarnationInTornado", 0);
            tag.putInt("calamityIncarnationInLightning", 0);
            tag.putInt("calamityIncarnationInPlague", 0);
            tag.putInt("monsterCalamityImmunity", 0);
            tag.putBoolean("monsterDangerSense", false);
            tag.putBoolean("monsterCalamityAttraction", false);
            tag.putInt("probabilityManipulationInfiniteFortune", 0);
            tag.putInt("probabilityManipulationInfiniteMisfortune", 0);
            tag.putInt("monsterRebootPotionEffectsCount", 0);
            tag.putInt("monsterRebootAge", 0);
            tag.putInt("monsterRebootLuck", 0);
            tag.putInt("monsterRebootMisfortune", 0);
            tag.putInt("monsterRebootSanity", 0);
            tag.putInt("monsterRebootCorruption", 0);
            tag.putInt("monsterRebootHealth", 20);
            tag.putInt("monsterRebootSpirituality", 0);
            tag.putBoolean("auraOfGlory", false);
            tag.putBoolean("auraOfTwilight", false);
            tag.remove("dawnStoredArmorData");
            tag.putBoolean("warriorProtection", false);
            tag.putInt("monsterMisfortuneManipulationGravity", 0);
            tag.putBoolean("warriorProtection", false);
            tag.putBoolean("demonHuntingEye", false);
            tag.putBoolean("warriorShouldDestroyBlock", false);
            tag.putInt("globeOfTwilight", 0);
            tag.putInt("warriorLightConcealment", 0);
            tag.putInt("lightOfDawnCounter", 0);
            tag.putInt("corruption", 0);
            tag.putBoolean("mercuryLiquefication", false);
            tag.putInt("silverRapierSummoning", 0);
            tag.putInt("twilightAgeAccelerate", 0);
            tag.putInt("twilightAgeAccelerateEnemy", 0);
            tag.putInt("twilightLight", 0);
            tag.putInt("twilightManifestation", 0);
            tag.putBoolean("warriorDangerSense", false);
            DreamIntoReality.stopFlying(livingEntity);
            tag.putInt("BarrierRadius", 0);
            tag.putInt("waitMakeLifeTimer", 0);
            tag.putInt("BlinkDistance", 0);
            tag.remove("manipulateMovementX");
            tag.remove("manipulateMovementY");
            tag.remove("manipulateMovementZ");
            tag.putBoolean("manipulateMovementBoolean", false);
            removePsychologicalInvisibilityEffect(livingEntity);
            tag.putInt("sailorAcidicRain", 0);
            tag.putInt("calamityIncarnationTornado", 0);
            tag.putInt("calamityIncarnationTsunami", 0);
            tag.putInt("sailorEarthquake", 0);
            tag.putBoolean("SailorLightning", false);
            tag.putInt("sailorExtremeColdness", 0);
            tag.putInt("sailorHurricane", 0);
            tag.putInt("tyrantMentionedInChat", 0);
            tag.putInt("sailorLightningStorm1", 0);
            tag.putInt("sailorLightningStorm", 0);
            tag.putInt("matterAccelerationBlockTimer", 0);
            tag.putInt("tyrantSelfAcceleration", 0);
            tag.putInt("ragingBlows", 0);
            tag.putBoolean("rainEyes", false);
            tag.putBoolean("sailorProjectileMovement", false);
            tag.putInt("sirenSongHarm", 0);
            tag.putInt("sirenSongWeaken", 0);
            tag.putInt("sirenSongStun", 0);
            tag.putInt("sirenSongStrengthen", 0);
            tag.putInt("sailorLightningStar", 0);
            tag.putInt("inStormSeal", 0);
            tag.putInt("sailorTsunami", 0);
            tag.putInt("sailorTsunamiSeal", 0);
            tag.putInt("sailorSphere", 0);
            tag.putInt("mercuryLiqueficationTrapped", 0);
            tag.putInt("sailorSeal", 0);
            tag.putBoolean("sailorFlight1", false);
            tag.putInt("sailorFlight", 0);
            tag.putInt("sailorFlightDamageCancel", 0);
            tag.putInt("luckStoneDamageImmunity", 0);
            tag.putInt("luckStoneDamage", 0);
            tag.putInt("luckMeteorDamage", 0);
            tag.putInt("calamityMeteorImmunity", 0);
            tag.putInt("luckLightningMCDamage", 0);
            tag.putInt("luckMCLightningImmunity", 0);
            tag.putInt("calamityExplosionOccurrence", 0);
            tag.putInt("luckLightningLOTMDamage", 0);
            tag.putInt("calamityLightningBoltMonsterResistance", 0);
            tag.putInt("calamityLightningStormResistance", 0);
            tag.putInt("luckTornadoResistance", 0);
            tag.putInt("luckTornadoImmunity", 0);
            tag.putInt("calamityLOTMLightningImmunity", 0);
            tag.putInt("calamityLightningStormImmunity", 0);
            tag.putInt("abilityStrengthened", 0);
            tag.putBoolean("lightningRedirection", false);
            tag.putBoolean("trickmasterTelekenisis", false);
            tag.putInt("escapeTrickCount", 0);
            tag.putInt("wormOfStar", 0);
            tag.remove("separateEntitySequence");
            tag.remove("separateEntityPathway");
            tag.putBoolean("windManipulationSense", false);
            if (livingEntity instanceof ServerPlayer serverPlayer) {
                LOTMNetworkHandler.sendToPlayer(new ClientWormOfStarDataS2C(0), serverPlayer);
            }
            tag.putBoolean("shouldntRenderSecretsSorcererHand", false);
            tag.putBoolean("wormOfStarChoice", false);
            tag.putBoolean("doorBlinkState", false);
            tag.putInt("doorBlinkStateDistance", 0);
            tag.putBoolean("planeswalkerSymbolization", false);
        }
    }

    public static int getDreamIntoReality(LivingEntity living) {
        return Math.max(1, living.getPersistentData().getInt("dreamIntoReality"));
    }

    public static List<LivingEntity> getNonAlliesNearby(LivingEntity living, float inflation) {
        List<LivingEntity> nonAllies = new ArrayList<>();
        for (LivingEntity entity : living.level().getEntitiesOfClass(LivingEntity.class, living.getBoundingBox().inflate(inflation))) {
            if (!isEntityAlly(living, entity) && living != entity)
                nonAllies.add(entity);
        }
        return nonAllies;
    }

    public static List<Entity> getNonAllyEntitiesNearby(LivingEntity living, float inflation) {
        List<Entity> nonAllies = new ArrayList<>();
        for (Entity entity : living.level().getEntitiesOfClass(Entity.class, living.getBoundingBox().inflate(inflation))) {
            if (!isEntityAlly(living, entity) && entity != living) {
                nonAllies.add(entity);
            }
        }
        return nonAllies;
    }


    public static boolean isEntityAlly(LivingEntity living, Entity possibleAlly) {
        if (possibleAlly instanceof LivingEntity livingAlly) {
            return BeyonderUtil.areAllies(living, livingAlly);
        } else if (possibleAlly instanceof Projectile projectile) {
            if (projectile.getOwner() != null && projectile.getOwner() instanceof LivingEntity owner) {
                return BeyonderUtil.areAllies(living, owner);
            }
        }
        return false;
    }

    public static void sendParticles(LivingEntity living, ParticleOptions particle, double spawnX, double spawnY, double spawnZ) {
        if (living.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(particle, spawnX, spawnY, spawnZ, 0, 0, 0, 0, 0);
        }
    }

    public static void sendParticles(LivingEntity living, ParticleOptions particle, double spawnX, double spawnY, double spawnZ, double velocityX, double velocityY, double velocityZ) {
        if (living.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(particle, spawnX, spawnY, spawnZ, 0, velocityX, velocityY, velocityZ, 1.0);
        }
    }

    public static LivingEntity getLivingEntityFromUUID(Level level, UUID uuid) {
        if (level instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(uuid);
            if (entity instanceof LivingEntity livingEntity) {
                return livingEntity;
            }
            for (Entity loadedEntity : serverLevel.getAllEntities()) {
                if (loadedEntity.getUUID().equals(uuid) && loadedEntity instanceof LivingEntity livingEntity) {
                    return livingEntity;
                }
            }
            for (Player player : serverLevel.players()) {
                if (player.getUUID().equals(uuid)) {
                    return player;
                }
            }
        }
        return null;
    }

    public static Player getPlayerFromUUID(MinecraftServer server, UUID uuid) {
        return server.getPlayerList().getPlayer(uuid);
    }


    public static CustomFallingBlockEntity getCustomFallingBlockFromUUID(Level level, UUID uuid) {
        if (level instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(uuid);
            if (entity instanceof CustomFallingBlockEntity fallingBlock) {
                return fallingBlock;
            }
        }
        return null;
    }

    public static BeyonderClass chooseRandomPathway() {
        Random random = new Random();
        int choice = random.nextInt(5);

        return switch (choice) {
            case 0 -> BeyonderClassInit.SPECTATOR.get();
            case 1 -> BeyonderClassInit.WARRIOR.get();
            case 2 -> BeyonderClassInit.MONSTER.get();
            case 3 -> BeyonderClassInit.APPRENTICE.get();
            case 4 -> BeyonderClassInit.SAILOR.get();
            default -> BeyonderClassInit.SPECTATOR.get(); // Fallback (shouldn't happen)
        };
    }

    public static int chooseRandomSequence(int lowestSequence) {
        Random random = new Random();
        if (lowestSequence > 9) {
            lowestSequence = 9;
        }
        int range = (9 - lowestSequence) + 1;
        return switch (random.nextInt(range)) {
            case 0 -> lowestSequence;
            case 1 -> lowestSequence + 1;
            case 2 -> lowestSequence + 2;
            case 3 -> lowestSequence + 3;
            case 4 -> lowestSequence + 4;
            case 5 -> lowestSequence + 5;
            case 6 -> lowestSequence + 6;
            case 7 -> lowestSequence + 7;
            case 8 -> lowestSequence + 8;
            case 9 -> 9;
            default -> lowestSequence;
        };
    }

    public static int maxWormAmount(LivingEntity living) {
        int max = 0;
        if (!living.level().isClientSide()) {
            int sequence = getSequence(living);
            if (sequence == 4) {
                return 200;
            } else if (sequence == 3) {
                return 800;
            } else if (sequence == 2) {
                return 4000;
            } else if (sequence == 1) {
                return 16000;
            } else if (sequence == 0) {
                return 80000;
            }
        }
        return max;
    }

    public static void startFlying(LivingEntity livingEntity, float flySpeed) {
        if (livingEntity instanceof Player pPlayer) {
            Abilities playerAbilities = pPlayer.getAbilities();
            if (!pPlayer.isCreative() || !pPlayer.isSpectator()) {
                playerAbilities.mayfly = true;
                playerAbilities.flying = true;
                playerAbilities.setFlyingSpeed(flySpeed);
            }
            pPlayer.onUpdateAbilities();
            if (livingEntity instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundPlayerAbilitiesPacket(playerAbilities));
            }
        } else if (livingEntity instanceof PlayerMobEntity playerMobEntity) {
            playerMobEntity.setIsFlying(true);
            playerMobEntity.setFlySpeed(flySpeed);
        } else if(!(livingEntity instanceof Player player) || !player.isSpectator()){
            stopFlying(livingEntity);
        }
    }

    public static void stopFlying(LivingEntity livingEntity) {
        if (livingEntity instanceof Player player) {
            Abilities playerAbilities = player.getAbilities();
            if (!playerAbilities.instabuild) {
                playerAbilities.mayfly = false;
                playerAbilities.flying = false;
            }
            playerAbilities.setFlyingSpeed(0.05F);
            player.onUpdateAbilities();
            if (livingEntity instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundPlayerAbilitiesPacket(playerAbilities));
            }
        } else if (livingEntity instanceof PlayerMobEntity playerMobEntity) {
            playerMobEntity.setIsFlying(false);
            playerMobEntity.setFlySpeed(1.0f);
        }
        livingEntity.getPersistentData().putInt("LOTMFlying", 0);
    }


    public static void startFlying(LivingEntity livingEntity, float flySpeed, int flyTime) {
        if (livingEntity instanceof Player pPlayer) {
            Abilities playerAbilities = pPlayer.getAbilities();
            if (!pPlayer.isCreative()) {
                playerAbilities.mayfly = true;
                playerAbilities.flying = true;
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
        livingEntity.getPersistentData().putInt("LOTMFlying", flyTime);
        livingEntity.getPersistentData().putFloat("LOTMFlySpeed", flySpeed);
    }

    public static boolean canFly(LivingEntity livingEntity) {
        if (livingEntity instanceof Player player) {
            return player.getAbilities().mayfly;
        } else return livingEntity instanceof PlayerMobEntity playerMobEntity && playerMobEntity.getIsFlying();
    }

    public static boolean canSeal(LivingEntity owner, LivingEntity target) {
        if (!owner.level().isClientSide() && !target.level().isClientSide()) {
            int sequence = BeyonderUtil.getSequence(owner);
            int targetSequence = BeyonderUtil.getSequence(target);
            if (BeyonderUtil.currentPathwayAndSequenceMatchesNoException(target, BeyonderClassInit.APPRENTICE.get(), 3)) {
                return targetSequence >= sequence;
            }
        }

        return true;
    }

    public static void teleportEntity(LivingEntity entity, Level destination, double x, double y, double z) {
        if (!SealedUtils.isSealed(entity)) {
            teleportEntityThroughDimensions(entity, destination.dimension().location(), x, y, z);
        } else if (entity instanceof Player player) {
            player.displayClientMessage(Component.literal("You cant teleport over your existing seal").withStyle(ChatFormatting.RED), false);
        }
    }

    public static void teleportEntity(LivingEntity entity, ResourceLocation destination, double x, double y, double z) {
        if (!SealedUtils.isSealed(entity)) {
            teleportEntityThroughDimensions(entity, destination, x, y, z);
        } else if (entity instanceof Player player) {
            player.displayClientMessage(Component.literal("You cant teleport over your existing seal").withStyle(ChatFormatting.RED), false);
        }
    }

    private static final Set<String> EXCLUDED_METHODS = Set.of(
            "matterAccelerationSelfAbility",
            "envisionLocationBlink",
            "exileTickEvent",
            "cycleOfFateTickEvent",
            "cycleOfFateDeath",
            "stormSealTick",
            "envisionLocationTeleport",
            "dreamWalk"
    );

    public static boolean shouldBypassSeal() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        for (int i = 2; i < Math.min(stackTrace.length, 10); i++) {
            String methodName = stackTrace[i].getMethodName();
            if (EXCLUDED_METHODS.contains(methodName)) {
                return true;
            }
        }
        return false;
    }

    public static DimensionalSightTileEntity findNearbyDimensionalSight(LivingEntity entity) {
        Level level = entity.level();
        if (!level.isClientSide()) {
            BlockPos entityPos = entity.blockPosition();
            int searchRadius = 15;
            Vec3 entityLookVec = entity.getLookAngle();
            Vec3 entityEyePos = entity.getEyePosition();
            for (int x = -searchRadius; x <= searchRadius; x++) {
                for (int y = -searchRadius; y <= searchRadius; y++) {
                    for (int z = -searchRadius; z <= searchRadius; z++) {
                        BlockPos checkPos = entityPos.offset(x, y, z);
                        BlockEntity blockEntity = level.getBlockEntity(checkPos);
                        if (blockEntity instanceof DimensionalSightTileEntity dimensionalSight) {
                            if (dimensionalSight.getCasterUUID() != null && dimensionalSight.getCasterUUID().equals(entity.getUUID()) && dimensionalSight.getScryUniqueID() != null) {
                                if (isBlockVisibleToEntity(entity, checkPos, entityEyePos, entityLookVec)) {
                                    return dimensionalSight;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private static boolean isBlockVisibleToEntity(LivingEntity entity, BlockPos blockPos, Vec3 entityEyePos, Vec3 entityLookVec) {
        Vec3 blockCenter = Vec3.atCenterOf(blockPos);
        Vec3 toBlock = blockCenter.subtract(entityEyePos).normalize();
        double dotProduct = entityLookVec.dot(toBlock);
        double fovThreshold = 0.0;
        if (dotProduct < fovThreshold) {
            return false;
        }
        Level level = entity.level();
        ClipContext clipContext = new ClipContext(entityEyePos, blockCenter, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity);
        BlockHitResult hitResult = level.clip(clipContext);
        return hitResult.getBlockPos().equals(blockPos) || hitResult.getType() == HitResult.Type.MISS;
    }

    public static List<PlayerMobEntity> getAllPlayerMobEntities(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) return Collections.emptyList();

        PlayerMobTracker tracker = PlayerMobTracker.get(serverLevel);
        Map<String, List<PlayerMobEntity>> mobsAcrossDimensions = tracker.getAllPlayerMobsAcrossDimensions(serverLevel);

        List<PlayerMobEntity> allMobs = new ArrayList<>();
        for (List<PlayerMobEntity> mobs : mobsAcrossDimensions.values()) {
            allMobs.addAll(mobs);
        }

        return allMobs;
    }

    public static void setInvisible(LivingEntity living, boolean choice, int time) {
        LOTMNetworkHandler.sendToAllPlayers(new SyncShouldntRenderInvisibilityPacketS2C(choice, living.getUUID(), time));
    }

    public static boolean isInvisible(LivingEntity living) {
        return ClientShouldntRenderHandData.getShouldntRender(living.getUUID());
    }

    public static boolean isStunned(LivingEntity living) {
        if (BeyonderUtil.hasParalysis(living)) {
            return true;
        } else if (BeyonderUtil.hasStun(living)) {
            return true;
        } else if (BeyonderUtil.hasAwe(living)) {
            return true;
        }
        return false;
    }

    public static void forceLookAtPosition(ServerPlayer player, Vec3 targetPos, boolean smooth) {
        Vec3 playerPos = player.position().add(0, player.getEyeHeight(), 0);
        ForceLookPacketS2C packet = new ForceLookPacketS2C(playerPos, targetPos, smooth);
        sendPacketToPlayer(player, packet);
    }

    public static void forceLookAtEntity(ServerPlayer player, Entity target, boolean smooth) {
        Vec3 targetPos = target.position().add(0, target.getEyeHeight() * 0.5, 0);
        forceLookAtPosition(player, targetPos, smooth);
    }

    // Force player to look in a specific direction (yaw/pitch)
    public static void forceLookDirection(ServerPlayer player, float yaw, float pitch, boolean smooth) {
        ForceLookPacketS2C packet = new ForceLookPacketS2C(yaw, pitch, smooth);
        sendPacketToPlayer(player, packet);
    }

    // Placeholder for your networking system
    private static void sendPacketToPlayer(ServerPlayer player, ForceLookPacketS2C packet) {
        LOTMNetworkHandler.sendToPlayer(packet, player);
    }

    public static void stopForceLook(ServerPlayer player) {
        StopForceLookPacketS2C stopPacket = new StopForceLookPacketS2C();
        sendStopPacketToPlayer(player, stopPacket);
    }

    private static void sendStopPacketToPlayer(ServerPlayer player, StopForceLookPacketS2C packet) {
        LOTMNetworkHandler.sendToPlayer(packet, player);
    }

    public static boolean isCreative(LivingEntity living) {
        return (living instanceof Player player && (player.isCreative() || player.isSpectator()));
    }

    public static void applyAwe(LivingEntity living, int duration) {
        CompoundTag tag = living.getPersistentData();
        int currentDuration = tag.getInt("LOTMAwe");
        if (currentDuration == 0 || duration >= currentDuration) {
            tag.putInt("LOTMAwe", duration);
        }
        if (living instanceof ServerPlayer serverPlayer) {
            LOTMNetworkHandler.sendToPlayer(new ClientShouldntMovePacketS2C(duration), serverPlayer);
        }
    }

    public static boolean hasAwe(LivingEntity living) {
        return living.getPersistentData().getInt("LOTMAwe") >= 1;
    }

    public static void applyFrenzy(LivingEntity living, int duration) {
        CompoundTag tag = living.getPersistentData();
        int currentDuration = tag.getInt("LOTMFrenzy");
        if (currentDuration == 0 || duration >= currentDuration) {
            tag.putInt("LOTMFrenzy", duration);
        }
    }

    public static boolean hasFrenzy(LivingEntity living) {
        return living.getPersistentData().getInt("LOTMFrenzy") >= 1;
    }

    public static void applyStun(LivingEntity living, int duration) {
        CompoundTag tag = living.getPersistentData();
        int currentDuration = tag.getInt("LOTMStun");
        if (currentDuration == 0 || duration >= currentDuration) {
            tag.putInt("LOTMStun", duration);
        }
        if (living instanceof ServerPlayer serverPlayer) {
            LOTMNetworkHandler.sendToPlayer(new ClientShouldntMovePacketS2C(duration), serverPlayer);
        }
    }

    public static boolean hasStun(LivingEntity living) {
        return living.getPersistentData().getInt("LOTMStun") >= 1;
    }

    public static void applyBleeding(LivingEntity living, int duration, int amplifier) {
        CompoundTag tag = living.getPersistentData();
        int currentDuration = tag.getInt("LOTMBleeding");
        int currentAmplifier = tag.getInt("LOTMBleedingAmplifier");

        if (currentDuration == 0) {
            tag.putInt("LOTMBleeding", duration);
            tag.putInt("LOTMBleedingAmplifier", amplifier);
        } else if (currentAmplifier < amplifier) {
            tag.putInt("LOTMBleeding", duration);
            tag.putInt("LOTMBleedingAmplifier", amplifier);
        } else if (currentAmplifier == amplifier && duration >= currentDuration) {
            tag.putInt("LOTMBleeding", duration);
            tag.putInt("LOTMBleedingAmplifier", amplifier);
        }
    }

    public static boolean hasBleeding(LivingEntity living) {
        return living.getPersistentData().getInt("LOTMBleeding") >= 1;
    }

    public static void applyBattleHypnotism(LivingEntity living, int duration) {
        CompoundTag tag = living.getPersistentData();
        int currentDuration = tag.getInt("LOTMBattleHypnotism");
        if (currentDuration == 0 || duration >= currentDuration) {
            tag.putInt("LOTMBattleHypnotism", duration);
        }
    }

    public static boolean hasBattleHypnotism(LivingEntity living) {
        return living.getPersistentData().getInt("LOTMBattleHypnotism") >= 1;
    }

    public static void applyManipulation(LivingEntity living, int duration) {
        CompoundTag tag = living.getPersistentData();
        int currentDuration = tag.getInt("LOTMManipulation");
        if (currentDuration == 0 || duration >= currentDuration) {
            tag.putInt("LOTMManipulation", duration);
        }
    }

    public static boolean hasManipulation(LivingEntity living) {
        return living.getPersistentData().getInt("LOTMManipulation") >= 1;
    }

    public static void applyMentalPlague(LivingEntity living, int duration) {
        CompoundTag tag = living.getPersistentData();
        int currentDuration = tag.getInt("LOTMMentalPlague");
        if (currentDuration == 0 || duration >= currentDuration) {
            tag.putInt("LOTMMentalPlague", duration);
        }
    }

    public static boolean hasMentalPlague(LivingEntity living) {
        return living.getPersistentData().getInt("LOTMMentalPlague") >= 1;
    }

    public static void applyParalysis(LivingEntity living, int duration) {
        CompoundTag tag = living.getPersistentData();
        int currentDuration = tag.getInt("LOTMParalysis");
        if (currentDuration == 0 || duration >= currentDuration) {
            tag.putInt("LOTMParalysis", duration);
        }
        if (living instanceof ServerPlayer serverPlayer) {
            LOTMNetworkHandler.sendToPlayer(new ClientShouldntMovePacketS2C(duration), serverPlayer);
        }
    }

    public static boolean hasParalysis(LivingEntity living) {
        return living.getPersistentData().getInt("LOTMParalysis") >= 1;
    }

    public static void applyNoRegeneration(LivingEntity living, int duration) {
        CompoundTag tag = living.getPersistentData();
        int currentDuration = tag.getInt("LOTMNoRegeneration");
        if (currentDuration == 0 || duration >= currentDuration) {
            tag.putInt("LOTMNoRegeneration", duration);
        }
        tag.putInt("noRegenerationEffectHealth", (int) living.getHealth());
    }

    public static boolean hasNoRegeneration(LivingEntity living) {
        return living.getPersistentData().getInt("LOTMNoRegeneration") >= 1;
    }

    public static void applyBeneficialEffectBlocker(LivingEntity living, int duration) {
        CompoundTag tag = living.getPersistentData();
        int currentDuration = tag.getInt("LOTMBeneficialEffectBlocker");
        if (currentDuration == 0 || duration >= currentDuration) {
            tag.putInt("LOTMBeneficialEffectBlocker", duration);
        }
    }

    public static boolean hasBeneficialEffectBlocker(LivingEntity living) {
        return living.getPersistentData().getInt("LOTMBeneficialEffectBlocker") >= 1;
    }

    public static void removeAwe(LivingEntity living) {
        living.getPersistentData().putInt("LOTMAwe", 0);
    }

    public static void removeFrenzy(LivingEntity living) {
        living.getPersistentData().putInt("LOTMFrenzy", 0);
    }

    public static void removeStun(LivingEntity living) {
        living.getPersistentData().putInt("LOTMStun", 0);
    }

    public static void removeBleeding(LivingEntity living) {
        CompoundTag tag = living.getPersistentData();
        tag.putInt("LOTMBleeding", 0);
        tag.putInt("LOTMBleedingAmplifier", 0);
    }

    public static void removeBattleHypnotism(LivingEntity living) {
        living.getPersistentData().putInt("LOTMBattleHypnotism", 0);
    }

    public static void removeManipulation(LivingEntity living) {
        living.getPersistentData().putInt("LOTMManipulation", 0);
    }

    public static void removeMentalPlague(LivingEntity living) {
        living.getPersistentData().putInt("LOTMMentalPlague", 0);
    }

    public static void removeParalysis(LivingEntity living) {
        living.getPersistentData().putInt("LOTMParalysis", 0);
    }

    public static void removeNoRegeneration(LivingEntity living) {
        living.getPersistentData().putInt("LOTMNoRegeneration", 0);
    }

    public static void removeBeneficialEffectBlocker(LivingEntity living) {
        living.getPersistentData().putInt("LOTMBeneficialEffectBlocker", 0);
    }


    public static void effectTick(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        if (!livingEntity.level().isClientSide()) {
            int awe = tag.getInt("LOTMAwe");
            int frenzy = tag.getInt("LOTMFrenzy");
            int stun = tag.getInt("LOTMStun");
            int bleeding = tag.getInt("LOTMBleeding");
            int bleedingAmp = tag.getInt("LOTMBleedingAmplifier");
            int battleHypnotism = tag.getInt("LOTMBattleHypnotism");
            int manipulation = tag.getInt("LOTMManipulation");
            int mentalPlague = tag.getInt("LOTMMentalPlague");
            int paralysis = tag.getInt("LOTMParalysis");
            int noRegeneration = tag.getInt("LOTMNoRegeneration");
            int beneficialEffectBlocker = tag.getInt("LOTMBeneficialEffectBlocker");
            if (awe > 1) {
                tag.putInt("LOTMAwe", awe - 1);
                double x = livingEntity.getX();
                double y = livingEntity.getY();
                double z = livingEntity.getZ();
                livingEntity.teleportTo(x, y, z);
                livingEntity.setDeltaMovement(0, 0, 0);
                livingEntity.hurtMarked = true;
            }

            if (frenzy >= 1) {
                tag.putInt("LOTMFrenzy", frenzy - 1);
                double x = livingEntity.getX() + Math.random() * 0.2 - 0.1;
                double y = livingEntity.getY();
                double z = livingEntity.getZ() + Math.random() * 0.2 - 0.1;

                double deltaX = Math.random() * 0.25 + (Math.random() - 1.072) * 0.25;
                double deltaY = 0;
                double deltaZ = Math.random() * 0.25 + (Math.random() - 1.055) * 0.25;

                AABB targetBoundingBox = new AABB(x, y, z, x + livingEntity.getBbWidth(), y + livingEntity.getBbHeight(), z + livingEntity.getBbWidth());
                if (!livingEntity.level().noCollision(livingEntity, targetBoundingBox)) {
                    Vec3 newPos = livingEntity.position().add(deltaX, deltaY, deltaZ);
                    if (livingEntity.level().noCollision(livingEntity, livingEntity.getBoundingBox().move(newPos))) {
                        x = newPos.x;
                        y = newPos.y;
                        z = newPos.z;
                    }
                }
                livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().add(Math.random() * 0.25 + (Math.random() - 1.072) * 0.25, 0, Math.random() * 0.25 + (Math.random() - 1.055) * 0.25));
                livingEntity.setSprinting(true);
                livingEntity.hurtMarked = true;
                if (livingEntity instanceof Player player && livingEntity.getRandom().nextFloat() > 0.5f && livingEntity.onGround()) {
                    player.jumpFromGround();
                }
            }

            if (stun >= 1) {
                tag.putInt("LOTMStun", stun - 1);
                double x = livingEntity.getX();
                double y = livingEntity.getY();
                double z = livingEntity.getZ();
                livingEntity.teleportTo(x, y, z);
                livingEntity.setDeltaMovement(0, 0, 0);
                livingEntity.hurtMarked = true;
            }

            if (bleeding >= 1) {
                tag.putInt("LOTMBleeding", bleeding - 1);
                if (livingEntity.tickCount % 5 == 0) {
                    livingEntity.hurt(livingEntity.damageSources().cramming(), bleedingAmp);
                }
            }

            if (battleHypnotism >= 1) {
                if (livingEntity.tickCount % 3 == 0) {
                    for (Mob mob : livingEntity.level().getEntitiesOfClass(Mob.class, livingEntity.getBoundingBox().inflate(20 * 2))) {
                        if (mob != livingEntity) {
                            mob.setTarget(livingEntity);
                        }
                    }
                    if (!(livingEntity instanceof Player)) {
                        livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1, 2));
                    }
                }
                if (battleHypnotism == 1) {
                    if (livingEntity instanceof Mob mob && mob.getTarget() != null && !(mob.getTarget() instanceof Player)) {
                        int playersFound = 0;
                        for (Player player : mob.level().getEntitiesOfClass(Player.class, mob.getBoundingBox().inflate(12))) {
                            playersFound++;
                            mob.setTarget(player);
                        }
                        if (playersFound == 0) {
                            mob.setTarget(null);
                        }
                    }
                    tag.putInt("LOTMBattleHypnotism", 0);
                }
                if (battleHypnotism <= 5) {
                    if (livingEntity instanceof Mob mob) {
                        if (hasBattleHypnotism(mob)) {
                            if (mob.getTarget() instanceof Mob) {
                                mob.setTarget(null);
                            }
                        }
                    }
                }
                tag.putInt("LOTMBattleHypnotism", battleHypnotism - 1);
            }

            if (manipulation >= 1) {
                tag.putInt("LOTMManipulation", manipulation - 1);
            }

            if (mentalPlague >= 1) {
                if (mentalPlague == 1) {
                    for (LivingEntity entity1 : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate(50))) {
                        applyEffectsAndDamage(entity1);
                    }
                    applyEffectsAndDamage(livingEntity);
                }
                tag.putInt("LOTMMentalPlague", mentalPlague - 1);
            }

            if (paralysis >= 1) {
                tag.putInt("LOTMParalysis", paralysis - 1);
                double x = livingEntity.getX();
                double y = livingEntity.getY();
                double z = livingEntity.getZ();
                livingEntity.teleportTo(x, y, z);
                livingEntity.setDeltaMovement(0, 0, 0);
                livingEntity.hurtMarked = true;
            }

            if (noRegeneration >= 1) {
                tag.putInt("LOTMNoRegeneration", noRegeneration - 1);
            }

            if (beneficialEffectBlocker >= 1) {
                tag.putInt("LOTMBeneficialEffectBlocker", beneficialEffectBlocker - 1);
            }


            if (hasStun(livingEntity) || hasParalysis(livingEntity) || hasAwe(livingEntity)) {
                if (livingEntity instanceof Player) {
                    return;
                }
                livingEntity.setDeltaMovement(0, livingEntity.getDeltaMovement().y, 0);
                if (livingEntity instanceof Mob mob) {
                    mob.getNavigation().stop();

                }
            }
            if (livingEntity instanceof Mob mob && mob.getTarget() != null && mob.getTarget().is(livingEntity)) {
                for (LivingEntity living : mob.level().getEntitiesOfClass(LivingEntity.class, mob.getBoundingBox().inflate(10))) {
                    if (living instanceof Player && !BeyonderUtil.areAllies(mob, living)) {
                        mob.setTarget(living);
                    }
                }
            }
            if (hasNoRegeneration(livingEntity)) {
                int x = livingEntity.getPersistentData().getInt("noRegenerationEffectHealth");
                if (livingEntity.getHealth() < x) {
                    livingEntity.getPersistentData().putInt("noRegenerationEffectHealth", (int) livingEntity.getHealth());
                }
                if (livingEntity.getHealth() > x) {
                    livingEntity.setHealth(x);
                }
            }
        }
    }

    public static void setGray(LivingEntity player, int duration) {
        if (player instanceof ServerPlayer serverPlayer) {
            LOTMNetworkHandler.sendToPlayer(new ClientGrayscaleS2C(duration * 2, 1.8f), serverPlayer);
        }
    }
}