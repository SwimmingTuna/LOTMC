package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;


import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.blocks.DimensionalSight.DimensionalSightTileEntity;
import net.swimmingtuna.lotm.entity.*;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.BiomeInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EFunctions;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventManager;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.world.worldgen.dimension.DimensionInit;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class Exile extends SimpleAbilityItem {

    public Exile(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 4, 700, 800);
    }
    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        exile(player);
        useSpirituality(player);
        return InteractionResult.SUCCESS;
    }

    private void exile(LivingEntity player) {
        if (!player.level().isClientSide()) {
            Vec3 lookVec = player.getLookAngle().scale(20);
            Vec3 spawnPos = player.position().add(lookVec);
            float yaw = -player.getYRot() + 180;

            ApprenticeDoorEntity apprenticeDoor = new ApprenticeDoorEntity(player.level(), player, yaw, 100);
            DimensionalSightTileEntity dimensionalSightTileEntity = BeyonderUtil.findNearbyDimensionalSight(player);

            if (dimensionalSightTileEntity != null && dimensionalSightTileEntity.getScryTarget() != null) {
                player.sendSystemMessage(Component.literal("You created a door to exile at your Dimensional Sight Target's location")
                        .withStyle(ChatFormatting.AQUA));
                apprenticeDoor.teleportTo(dimensionalSightTileEntity.getScryTarget().getX(),
                        dimensionalSightTileEntity.getScryTarget().getY(),
                        dimensionalSightTileEntity.getScryTarget().getZ());
            }
            else {
                apprenticeDoor.teleportTo(spawnPos.x(), spawnPos.y(), spawnPos.z());
            }

            player.level().addFreshEntity(apprenticeDoor);

            EventManager.addToRegularLoop(player, EFunctions.EXILE.get());
        }
    }

    public static void exileTickEvent(LivingEvent.LivingTickEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            LivingEntity livingEntity = event.getEntity();
            CompoundTag tag = livingEntity.getPersistentData();
            int x = tag.getInt("exileDoorX");
            int y = tag.getInt("exileDoorY");
            int z = tag.getInt("exileDoorZ");
            int timer = tag.getInt("exileDoorTimer");
            String dimensionString = tag.getString("exileDoorDimension");
            ServerLevel destinationWorld = null;

            if (livingEntity.level() instanceof ServerLevel serverLevel) {
                MinecraftServer server = serverLevel.getServer();

                if (!dimensionString.isEmpty()) {
                    ResourceLocation dimensionLocation = new ResourceLocation(dimensionString);
                    ResourceKey<Level> dimResourceKey = ResourceKey.create(Registries.DIMENSION, dimensionLocation);
                    destinationWorld = server.getLevel(dimResourceKey);
                }
            }

            if (timer == 1) {
                tag.putInt("shouldntExileWithDoor", 80);

                if (livingEntity instanceof Player) {
                    if (destinationWorld != null) {
                        BeyonderUtil.teleportEntity(livingEntity, destinationWorld, x, y, z);
                    }
                    else {
                        BeyonderUtil.teleportEntity(livingEntity, Level.OVERWORLD.location(), x, y, z);
                    }
                }
                else {
                    livingEntity.teleportTo(x,y,z);

                    if (livingEntity.getHealth() < 15) {
                        livingEntity.hurt(livingEntity.damageSources().generic(), 15);
                    }
                }

                EventManager.removeFromRegularLoop(livingEntity, EFunctions.EXILE.get());
            }

            if (timer >= 1) {
                tag.putInt("exileDoorTimer", timer - 1);

                if (livingEntity instanceof Mob mob && timer % 20 == 0) {
                    mob.fallDistance = 0;

                    if (mob.level() instanceof ServerLevel serverLevel) {
                        ChunkPos centerChunk = new ChunkPos(mob.blockPosition());

                        for (int dx = -3; dx <= 3; dx++) {
                            for (int dz = -3; dz <= 3; dz++) {
                                ChunkPos chunkPos = new ChunkPos(centerChunk.x + dx, centerChunk.z + dz);
                                serverLevel.getChunkSource().addRegionTicket(TicketType.PLAYER, chunkPos, 3, chunkPos);
                            }
                        }
                    }

                    mob.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20, 1, false, false));
                    int randomChoice = mob.getPersistentData().getInt("exileDoorMob");

                    if (randomChoice == 1) {
                        if (mob.getHealth() > 17) {
                            mob.hurt(mob.damageSources().lava(), 15);
                            mob.setSecondsOnFire(5);
                        }
                    }
                    else if (randomChoice == 2) {
                        if (mob.getHealth() > 15) {
                            mob.hurt(mob.damageSources().freeze(), 15);
                        }
                        BeyonderUtil.applyMobEffect(mob, MobEffects.MOVEMENT_SLOWDOWN, 200, 1, false, false);
                    }
                    else if (randomChoice == 0) {
                        if (mob.getHealth() > 10) {
                            mob.hurt(mob.damageSources().magic(), 10);
                        }
                    }
                }
            }

            int shouldntExileTimer = tag.getInt("shouldntExileWithDoor");
            if (shouldntExileTimer >= 1) {
                tag.putInt("shouldntExileWithDoor", shouldntExileTimer - 1);
            }

            if (livingEntity.tickCount % 40 == 0) {
                if (livingEntity.level().dimension() == DimensionInit.EXILED_DIMENSION_LEVEL_KEY) {
                    BlockPos entityPos = livingEntity.blockPosition();
                    Holder<Biome> currentBiome = livingEntity.level().getBiome(entityPos);

                    if (currentBiome.is(BiomeInit.BEYONDER_MOUNTAINS)) {
                        int livingX = (int) livingEntity.getX();
                        int livingZ = (int) livingEntity.getZ();
                        int spawnX = (int) (livingX + BeyonderUtil.getRandomInRange(20));
                        int spawnZ = (int) (livingZ + BeyonderUtil.getRandomInRange(20));
                        double surfaceY = BeyonderUtil.findSurfaceY(livingEntity, x, z, DimensionInit.EXILED_DIMENSION_LEVEL_KEY);

                        PlayerMobEntity playerMob = new PlayerMobEntity(EntityInit.PLAYER_MOB_ENTITY.get(), livingEntity.level());
                        playerMob.setTarget(livingEntity);
                        playerMob.setLastHurtByMob(livingEntity);

                        if (livingEntity instanceof Player player) {
                            playerMob.setLastHurtByPlayer(player);
                        }

                        playerMob.teleportTo(spawnX, surfaceY, spawnZ);
                        BeyonderClass pathway = BeyonderUtil.chooseRandomPathway();
                        playerMob.setPathway(pathway);
                        playerMob.setSequence(BeyonderUtil.chooseRandomSequence(5));
                        playerMob.setMaxLife(400);
                        livingEntity.level().addFreshEntity(playerMob);
                    }
                    else if (currentBiome.is(BiomeInit.CALAMITY_PEAKS)) {
                        Random random = new Random();
                        int nextInt = random.nextInt(100);

                        if (nextInt > 95) {
                            MeteorEntity.summonMeteorAtPositionWithScale(livingEntity, livingEntity.getX() + BeyonderUtil.getRandomInRange(50), livingEntity.getY() + 150, livingEntity.getZ() + BeyonderUtil.getRandomInRange(50), livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), Math.max(5, 8 - BeyonderUtil.getSequence(livingEntity)));
                        }
                        else if (nextInt > 90) {
                            TornadoEntity.summonTornado(livingEntity);
                        }
                        else if (nextInt > 75) {
                            LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, livingEntity.level());
                            lightningBolt.teleportTo(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
                            lightningBolt.setDamage(20);
                            livingEntity.level().addFreshEntity(lightningBolt);
                        }
                        else if (nextInt > 60) {
                            LightningEntity lightningEntity = new LightningEntity(EntityInit.LIGHTNING_ENTITY.get(), livingEntity.level());
                            lightningEntity.teleportTo(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
                            lightningEntity.setSpeed(10.0f);
                            lightningEntity.setDeltaMovement((Math.random() * 0.4) - 0.2, -4, (Math.random() * 0.4) - 0.2);
                            lightningEntity.setMaxLength(30);
                            lightningEntity.setTargetEntity(livingEntity);
                            lightningEntity.setDamage(25);
                        }
                        else if (nextInt > 50) {
                            StoneEntity stoneEntity = new StoneEntity(EntityInit.STONE_ENTITY.get(), livingEntity.level());
                            stoneEntity.teleportTo(livingEntity.getX(), livingEntity.getY() + 30, livingEntity.getZ());
                            stoneEntity.setDeltaMovement(0, -5, 0);
                            livingEntity.level().addFreshEntity(stoneEntity);
                        }
                        else if (nextInt > 35) {
                            livingEntity.getDeltaMovement().add(0, 5,0);
                        }
                        else if (nextInt > 20) {
                            BeyonderUtil.applyStun(livingEntity, 20);
                        }
                        else {
                            tag.putInt("luckDoubleDamage", tag.getInt("luckDoubleDamage") + 1);

                            if (livingEntity instanceof Player player) {
                                player.displayClientMessage(Component.literal("Your next damage instance will be doubled").withStyle(ChatFormatting.RED), true);
                            }
                        }
                    }
                    else if (currentBiome.is(BiomeInit.GLACIAL_LANDSCAPE)) {
                        livingEntity.hurt(livingEntity.damageSources().freeze(), 20);
                    }
                    else if (currentBiome.is(BiomeInit.HELLISH_CLIFFS)) {
                        livingEntity.hurt(livingEntity.damageSources().lava(), 20);
                    }
                }
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, summon a door that will pull in and exile all those to a dimension where they will encounter either freezing temperature, a burning hell, an area with constant calamities, or rogue beyonders. They will be in this dimension for 20 seconds before coming back"));
        tooltipComponents.add(Component.literal("Mobs exiled will not have their health drop below 20."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("700").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("40 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("APPRENTICE_ABILITY", ChatFormatting.AQUA);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target != null) {
            return 70;
        }
        return 0;
    }
}
