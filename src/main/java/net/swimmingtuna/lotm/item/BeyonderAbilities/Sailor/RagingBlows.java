package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EFunctions;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventManager;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.ReachChangeUUIDs;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class RagingBlows extends SimpleAbilityItem {

    public RagingBlows(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 8, 45, 300);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        ragingBlows(player);
        addCooldown(player);
        useSpirituality(player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useAbilityOnEntity(ItemStack stack, LivingEntity player, LivingEntity interactionTarget, InteractionHand hand) {
        if (!player.level().isClientSide()) {
            if (!checkAll(player)) {
                return InteractionResult.FAIL;
            }
            addCooldown(player, this, (int) (this.cooldown * 2.0f));
            useSpirituality(player);
            if (player instanceof Mob) {
                if (Math.random() > 0.5) {
                    ragingBlows(player);
                } else {
                    ragingCombo(player, interactionTarget);
                }
            } else {
                ragingCombo(player, interactionTarget);
            }
        }
        return InteractionResult.SUCCESS;
    }

    public static void ragingCombo(LivingEntity player, LivingEntity interactionTarget) {
        CompoundTag tag = player.getPersistentData();
        EventManager.addToRegularLoop(player, EFunctions.RAGINGCOMBO.get());
        tag.putUUID("ragingComboUUID", interactionTarget.getUUID());
        tag.putInt("ragingCombo", 1);
    }

    @SuppressWarnings("deprecation")
    private final Lazy<Multimap<Attribute, AttributeModifier>> lazyAttributeMap = Lazy.of(this::createAttributeMap);

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            return this.lazyAttributeMap.get();
        }
        return super.getDefaultAttributeModifiers(slot);
    }

    private Multimap<Attribute, AttributeModifier> createAttributeMap() {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> attributeBuilder = ImmutableMultimap.builder();
        attributeBuilder.putAll(super.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND));
        attributeBuilder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_ENTITY_REACH, "Reach modifier", 15, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with entities
        attributeBuilder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_BLOCK_REACH, "Reach modifier", 15, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with blocks, p much useless for this item
        return attributeBuilder.build();
    }

    public static void ragingCombo(LivingEvent.LivingTickEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            LivingEntity living = event.getEntity();
            CompoundTag tag = living.getPersistentData();
            if (tag.contains("ragingComboUUID") && tag.getInt("ragingCombo") >= 1) {
                if (!BeyonderUtil.isStunned(living)) {
                    tag.putInt("ragingCombo", tag.getInt("ragingCombo") + 1);
                    int counter = tag.getInt("ragingCombo");
                    LivingEntity target = BeyonderUtil.getLivingEntityFromUUID(living.level(), tag.getUUID("ragingComboUUID"));
                    if (target != null && target.isAlive()) {
                        float damage = BeyonderUtil.getDamage(living).get(ItemInit.RAGING_BLOWS.get());
                        if (target.distanceTo(living) <= Math.max(20, damage * 7)) {
                            if (living instanceof ServerPlayer player) {
                                BeyonderUtil.forceLookAtEntity(player, target, false);
                            }
                            Vec3 targetLookVec = target.getLookAngle();
                            Vec3 lookVec = living.getLookAngle();
                            Vec3 targetRightVec = targetLookVec.cross(new Vec3(0, 1, 0)).normalize();
                            float targetWidth = target.getBbWidth();
                            float targetHeight = target.getBbHeight();
                            float safeDistance = Math.max(2.0f, targetWidth + 1.0f);
                            float safeHeightOffset = Math.max(3.0f, targetHeight + 2.0f);

                            if (counter == 2) {
                                BeyonderUtil.applyMobEffect(living, MobEffects.MOVEMENT_SLOWDOWN, 10, 4, false, false);
                            } else if (counter == 12) {
                                Vec3 behindPos = targetLookVec.scale(-safeDistance);
                                safeTeleport(living, target, behindPos);
                                target.setDeltaMovement(0, 0, 0);
                                target.hurtMarked = true;
                            } else if (counter == 13 || counter == 14 || counter == 15 || counter == 16 || counter == 17 || counter == 18 || counter == 19 || counter == 20 || counter == 21) {
                                if (target.distanceTo(living) <= damage) {
                                    punch(living, target);
                                }
                                if (counter % 2 == 0) {
                                    punchParticles(target);
                                }
                            } else if (counter == 22) {
                                punch(living, target);
                                target.setDeltaMovement(lookVec.scale(8));
                                punchParticles(target);
                            } else if (counter == 24) {
                                Vec3 leftPos = targetRightVec.scale(-safeDistance);
                                safeTeleport(living, target, leftPos);
                                target.setDeltaMovement(0, 0, 0);
                                target.hurtMarked = true;
                            } else if (counter == 25 || counter == 26 || counter == 27) {
                                if (target.distanceTo(living) <= damage) {
                                    punch(living, target);
                                }
                                if (counter % 2 == 0) {
                                    punchParticles(target);
                                }
                            } else if (counter == 28) {
                                punch(living, target);
                                punchParticles(target);
                                target.setDeltaMovement(lookVec.scale(2));
                            } else if (counter == 30) {
                                Vec3 rightPos = targetRightVec.scale(safeDistance);
                                safeTeleport(living, target, rightPos);
                                target.setDeltaMovement(0, 0, 0);
                                target.hurtMarked = true;
                            } else if (counter == 31 || counter == 32 || counter == 33) {
                                if (target.distanceTo(living) <= damage) {
                                    punch(living, target);
                                    if (counter % 2 == 0) {
                                        punchParticles(target);
                                    }
                                }
                            } else if (counter == 34) {
                                punch(living, target);
                                punchParticles(target);
                                target.setDeltaMovement(lookVec.scale(2));
                            } else if (counter == 36) {
                                Vec3 frontPos = targetLookVec.scale(safeDistance);
                                safeTeleport(living, target, frontPos);
                                target.setDeltaMovement(0, damage, 0);
                                target.hurtMarked = true;
                            } else if (counter == 42) {
                                punch(living, target);
                                punchParticles(target);
                                Vec3 frontPos = targetLookVec.scale(safeDistance);
                                safeTeleport(living, target, frontPos);
                                target.setDeltaMovement(0, damage * 0.5, 0);
                                target.hurtMarked = true;
                            } else if (counter >= 50 && counter <= 65) {
                                if (counter % 3 == 0) {
                                    punchParticles(target);
                                }
                                living.teleportTo(target.getX(), target.getY() + safeHeightOffset + 2, target.getZ());
                                target.setDeltaMovement(0, Math.min(0, target.getDeltaMovement().y()), 0);
                                target.hurtMarked = true;
                                punch(living, target);
                            } else if (counter > 65 && counter < 75) {
                                living.teleportTo(target.getX(), target.getY() + safeHeightOffset + 2, target.getZ());
                                target.setDeltaMovement(0, Math.min(0, target.getDeltaMovement().y()), 0);
                                target.hurtMarked = true;

                            } else if (counter == 76) {
                                strongPunch(living, target);
                                if (living.level() instanceof ServerLevel serverLevel) {
                                    serverLevel.playSound(null, living.getOnPos(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 30.0f, 5.0f);
                                }
                                punchParticles(target);
                                punchParticles(target);
                                punchParticles(target);
                                punchParticles(target);
                                punchParticles(target);
                                punchParticles(target);
                                BeyonderUtil.destroyBlocksInSphereNotHittingOwner(living, target.getOnPos(), 12 - (BeyonderUtil.getSequence(living)), 0);
                                target.setDeltaMovement(0, -damage, 0);
                                target.hurtMarked = true;
                                tag.putInt("ragingCombo", 0);
                                if (living instanceof ServerPlayer player) {
                                    BeyonderUtil.stopForceLook(player);
                                }
                                EventManager.removeFromRegularLoop(living, EFunctions.RAGINGCOMBO.get());
                            }
                        } else {
                            tag.putInt("ragingCombo", 0);
                            if (living instanceof ServerPlayer player) {
                                BeyonderUtil.stopForceLook(player);
                            }
                            EventManager.removeFromRegularLoop(living, EFunctions.RAGINGCOMBO.get());
                        }
                    } else {
                        tag.putInt("ragingCombo", 0);
                        if (living instanceof ServerPlayer player) {
                            BeyonderUtil.stopForceLook(player);
                        }
                        EventManager.removeFromRegularLoop(living, EFunctions.RAGINGCOMBO.get());
                    }
                } else {
                    tag.putInt("ragingCombo", 0);
                    if (living instanceof ServerPlayer player) {
                        BeyonderUtil.stopForceLook(player);
                    }
                    EventManager.removeFromRegularLoop(living, EFunctions.RAGINGCOMBO.get());
                }
            }
            if (tag.getInt("ragingCombo") >= 77) {
                EventManager.removeFromRegularLoop(living, EFunctions.RAGINGCOMBO.get());
                if (living instanceof ServerPlayer player) {
                    BeyonderUtil.stopForceLook(player);
                }
                tag.putInt("ragingCombo", 0);
            }
        }
    }

    private static void safeTeleport(LivingEntity living, LivingEntity target, Vec3 offset) {
        Level level = living.level();
        Vec3 desiredPos = new Vec3(target.getX() + offset.x, target.getY() + offset.y, target.getZ() + offset.z);
        if (isPositionSafe(level, desiredPos, living)) {
            living.teleportTo(desiredPos.x, desiredPos.y, desiredPos.z);
            return;
        }
        if (living.level() instanceof ServerLevel serverLevel) {
            Random random = new Random();

            for (int i = 0; i < 5; i++) {
                double offsetX = (random.nextDouble() - 0.5) * 4.0; // -2 to +2 blocks
                double offsetY = (random.nextDouble() - 0.5) * 4.0; // -2 to +2 blocks
                double offsetZ = (random.nextDouble() - 0.5) * 4.0; // -2 to +2 blocks

                double particleX = living.getX() + offsetX;
                double particleY = living.getY() + offsetY;
                double particleZ = living.getZ() + offsetZ;

                serverLevel.sendParticles(ParticleTypes.POOF, particleX, particleY, particleZ, 1, 0, 0, 0, 0);
            }
        }
        Vec3 frontPos = target.getLookAngle().scale(1.5f);
        Vec3 inFrontPos = new Vec3(target.getX() + frontPos.x, target.getY() + frontPos.y, target.getZ() + frontPos.z);
        if (isPositionSafe(level, inFrontPos, living)) {
            living.teleportTo(inFrontPos.x, inFrontPos.y, inFrontPos.z);
            return;
        }
        Vec3 targetPos = new Vec3(target.getX(), target.getY(), target.getZ());
        if (isPositionSafe(level, targetPos, living)) {
            living.teleportTo(targetPos.x, targetPos.y, targetPos.z);
            return;
        }
        living.teleportTo(desiredPos.x, desiredPos.y, desiredPos.z);
    }

    private static boolean isPositionSafe(Level level, Vec3 pos, LivingEntity entity) {
        BlockPos blockPos = new BlockPos((int) pos.x, (int) pos.y, (int) pos.z);
        float entityHeight = entity.getBbHeight();
        int heightToCheck = Math.max(2, (int) Math.ceil(entityHeight));
        for (int i = 0; i < heightToCheck; i++) {
            BlockPos checkPos = blockPos.above(i);
            if (!level.getBlockState(checkPos).isAir()) {
                return false;
            }
        }
        return true;
    }

    public static void punch(LivingEntity living, LivingEntity target) {
        float damage = BeyonderUtil.getDamage(living).get(ItemInit.RAGING_BLOWS.get());
        boolean sailorLightning = living.getPersistentData().getBoolean("SailorLightning");
        if (BeyonderUtil.currentPathwayMatchesNoException(living, BeyonderClassInit.SAILOR.get()) && BeyonderUtil.getSequence(living) <= 7 && sailorLightning) {
            int sequence = BeyonderUtil.getSequence(living);
            double chanceOfDamage = (100.0 - (sequence * 12.5));
            if (Math.random() * 500 < chanceOfDamage) {
                LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, target.level());
                lightningBolt.moveTo(target.getX(), target.getY(), target.getZ());
                lightningBolt.setVisualOnly(false);
                lightningBolt.setDamage(Math.max(3, 15 - (sequence * 2)));
                if (BeyonderUtil.getSequence(living) <= 1) {
                    float amount = 3;
                    if (BeyonderUtil.getSequence(living) == 1) {
                        amount = 2;
                    }
                    BeyonderUtil.applyMentalDamage(living, target, amount);
                }
                target.level().addFreshEntity(lightningBolt);
            }

        }
        for (LivingEntity livingEntity : target.level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(Math.max(3, damage * 0.66f)))) {
            if (livingEntity != living && !BeyonderUtil.areAllies(livingEntity, living)) {
                livingEntity.hurt(BeyonderUtil.genericSource(living, livingEntity), damage * 0.6f);
                livingEntity.invulnerableTime = 0;
                livingEntity.hurtTime = 0;
                livingEntity.hurtDuration = 0;
            }
        }
        living.level().playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 0.5F, 0.5F);
    }

    public static void strongPunch(LivingEntity living, LivingEntity target) {
        float damage = BeyonderUtil.getDamage(living).get(ItemInit.RAGING_BLOWS.get()) * 3.0f;
        boolean sailorLightning = living.getPersistentData().getBoolean("SailorLightning");
        if (sailorLightning) {
            LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, target.level());
            lightningBolt.moveTo(target.getX(), target.getY(), target.getZ());
            lightningBolt.setVisualOnly(false);
            target.hurt(BeyonderUtil.lightningSource(living, target), Math.max(6, damage * 3));
            if (BeyonderUtil.getSequence(living) <= 1) {
                float amount = 3;
                if (BeyonderUtil.getSequence(living) == 1) {
                    amount = 2;
                }
                BeyonderUtil.applyMentalDamage(living, target, amount);
            }
            target.level().addFreshEntity(lightningBolt);
        }
        for (LivingEntity livingEntity : target.level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(Math.max(3, damage * 0.66f)))) {
            if (livingEntity != living && !BeyonderUtil.areAllies(livingEntity, living)) {
                livingEntity.hurt(BeyonderUtil.genericSource(living, livingEntity), damage * 1.75f);
                livingEntity.invulnerableTime = 0;
                livingEntity.hurtTime = 0;
                livingEntity.hurtDuration = 0;
            }
        }
        living.level().playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 0.5F, 0.5F);
    }

    public static void punchParticles(LivingEntity living) {
        if (living.level() instanceof ServerLevel serverLevel) {
            float targetWidth = living.getBbWidth();
            float targetHeight = living.getBbHeight();
            float particleRadius = Math.max(4.0f, (targetWidth + 1.0f) * 2.0f);
            float particleHeight = Math.max(4.0f, targetHeight * 1.5f);
            double angle = Math.random() * 2 * Math.PI;
            double distance = Math.random() * particleRadius * 1.2;
            double height = (Math.random() * particleHeight) - (particleHeight * 0.2);
            double particleX = living.getX() + Math.cos(angle) * distance;
            double particleY = living.getY() + height;
            double particleZ = living.getZ() + Math.sin(angle) * distance;
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, particleX, particleY, particleZ, 0, 0, 0, 0, 0.0);
        }
    }

    public static void ragingBlows(LivingEntity player) {
        if (!player.level().isClientSide()) {
            EventManager.addToRegularLoop(player, EFunctions.RAGINGBLOWS.get());
            CompoundTag persistentData = player.getPersistentData();
            persistentData.putInt("ragingBlows", 1);
        }
    }


    public static void ragingBlowsTick(LivingEntity livingEntity) {
        CompoundTag tag = livingEntity.getPersistentData();
        int sequence = BeyonderUtil.getSequence(livingEntity);
        boolean sailorLightning = tag.getBoolean("SailorLightning");
        int ragingBlows = tag.getInt("ragingBlows");
        int ragingBlowsRadius = (27 - (sequence * 3));
        if (ragingBlows >= 1) {
            RagingBlows.spawnRagingBlowsParticles(livingEntity);
            tag.putInt("ragingBlows", ragingBlows + 1);
        }
        if (ragingBlows >= 6 && ragingBlows <= 96 && ragingBlows % 6 == 0) {
            livingEntity.level().playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 0.5F, 0.5F);
            Vec3 playerLookVector = livingEntity.getViewVector(1.0F);
            Vec3 playerPos = livingEntity.position();
            for (LivingEntity entity : livingEntity.level().getEntitiesOfClass(LivingEntity.class, new AABB(playerPos.x - ragingBlowsRadius, playerPos.y - ragingBlowsRadius, playerPos.z - ragingBlowsRadius, playerPos.x + ragingBlowsRadius, playerPos.y + ragingBlowsRadius, playerPos.z + ragingBlowsRadius))) {
                if (entity != livingEntity && playerLookVector.dot(entity.position().subtract(playerPos)) > 0 && !BeyonderUtil.areAllies(livingEntity, entity)) {
                    float damage = BeyonderUtil.getDamage(livingEntity).get(ItemInit.RAGING_BLOWS.get());
                    entity.hurt(entity.damageSources().generic(), damage);
                    double ragingBlowsX = livingEntity.getX() - entity.getX();
                    double ragingBlowsZ = livingEntity.getZ() - entity.getZ();
                    entity.knockback(0.25, ragingBlowsX, ragingBlowsZ);
                    if (sequence <= 7) {
                        double chanceOfDamage = (100.0 - (sequence * 12.5));
                        if (Math.random() * 100 < chanceOfDamage && sailorLightning) {
                            LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, entity.level());
                            lightningBolt.moveTo(entity.getX(), entity.getY(), entity.getZ());
                            entity.level().addFreshEntity(lightningBolt);
                        }
                    }
                }
            }
        }
        if (ragingBlows >= 100) {
            EventManager.removeFromRegularLoop(livingEntity, EFunctions.RAGINGBLOWS.get());
            tag.putInt("ragingBlows", 0);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, unleash a fury of blows in the direction you're looking for a period of time. Use on an entity to throw them into a rapid combo."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("45").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("15 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    public static void spawnRagingBlowsParticles(LivingEntity player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            Vec3 playerPos = player.position();
            Vec3 playerLookVector = player.getViewVector(1.0F);
            int radius = (27 - (BeyonderUtil.getSequence(player) * 3));
            CompoundTag persistentData = player.getPersistentData();
            int particleCounter = persistentData.getInt("ragingBlowsParticleCounter");

            if (particleCounter < 7) {
                double randomDistance = Math.random() * radius;
                Vec3 randomOffset = playerLookVector.scale(randomDistance);

                // Add random horizontal offset
                double randomHorizontalOffset = Math.random() * Math.PI * 2; // Random angle between 0 and 2π
                randomOffset = randomOffset.add(new Vec3(Math.cos(randomHorizontalOffset) * radius / 4, 0, Math.sin(randomHorizontalOffset) * radius / 4));

                // Add random vertical offset
                double randomVerticalOffset = Math.random() * Math.PI / 2 - Math.PI / 4; // Random angle between -π/4 and π/4
                randomOffset = randomOffset.add(new Vec3(0, Math.sin(randomVerticalOffset) * radius / 4, 0));

                double randomX = playerPos.x + randomOffset.x;
                double randomY = playerPos.y + randomOffset.y;
                double randomZ = playerPos.z + randomOffset.z;

                // Check if the random offset vector is in front of the player
                if (playerLookVector.dot(randomOffset) > 0) {
                    serverLevel.sendParticles(ParticleTypes.EXPLOSION, randomX, randomY, randomZ, 0, 0, 0, 0, 0);
                }

                particleCounter++;
                persistentData.putInt("ragingBlowsParticleCounter", particleCounter);
            } else {
                persistentData.putInt("ragingBlowsParticleCounter", 0);
            }
        }
    }

    public static void spawnRagingBlowsParticlesPM(PlayerMobEntity player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            Vec3 playerPos = player.position();
            Vec3 playerLookVector = player.getViewVector(1.0F);
            int radius = (25 - (player.getCurrentSequence() * 3));
            CompoundTag persistentData = player.getPersistentData();
            int particleCounter = persistentData.getInt("ragingBlowsParticleCounter");

            if (particleCounter < 7) {
                double randomDistance = Math.random() * radius;
                Vec3 randomOffset = playerLookVector.scale(randomDistance);

                // Add random horizontal offset
                double randomHorizontalOffset = Math.random() * Math.PI * 2; // Random angle between 0 and 2π
                randomOffset = randomOffset.add(new Vec3(Math.cos(randomHorizontalOffset) * radius / 4, 0, Math.sin(randomHorizontalOffset) * radius / 4));

                // Add random vertical offset
                double randomVerticalOffset = Math.random() * Math.PI / 2 - Math.PI / 4; // Random angle between -π/4 and π/4
                randomOffset = randomOffset.add(new Vec3(0, Math.sin(randomVerticalOffset) * radius / 4, 0));

                double randomX = playerPos.x + randomOffset.x;
                double randomY = playerPos.y + randomOffset.y;
                double randomZ = playerPos.z + randomOffset.z;

                // Check if the random offset vector is in front of the player
                if (playerLookVector.dot(randomOffset) > 0) {
                    serverLevel.sendParticles(ParticleTypes.EXPLOSION, randomX, randomY, randomZ, 0, 0, 0, 0, 0);
                }

                particleCounter++;
                persistentData.putInt("ragingBlowsParticleCounter", particleCounter);
            } else {
                persistentData.putInt("ragingBlowsParticleCounter", 0);
            }
        }
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SAILOR_ABILITY", ChatFormatting.BLUE);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target != null && target.distanceTo(livingEntity) <= (27 - (BeyonderUtil.getSequence(livingEntity) * 3)) + 5) {
            return 80;
        }
        return 0;
    }
}