package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.entity.LightningEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.networking.packet.UpdateItemInHandC2S;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.EntityUtil.BeamEntity;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickHandlerSkillP;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickType;
import net.swimmingtuna.lotm.util.ReachChangeUUIDs;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class MatterAccelerationEntities extends LeftClickHandlerSkillP {

    public MatterAccelerationEntities(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 0, 800, 900);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        matterAccelerationEntitiesSelf(player);
        addCooldown(player);
        useSpirituality(player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useAbilityOnBlock(UseOnContext pContext) {
        if (pContext.getPlayer() == null) {
            Entity entity = pContext.getItemInHand().getEntityRepresentation();
            if (entity instanceof LivingEntity user) {
                BlockPos targetPos = pContext.getClickedPos();

                if (!checkAll(user)) {
                    return InteractionResult.FAIL;
                }
                matterAccelerationEntitiesTarget(user, targetPos);
                return InteractionResult.SUCCESS;
            }
        } else {
            Player player = pContext.getPlayer();
            BlockPos targetPos = pContext.getClickedPos();

            if (!checkAll(player)) {
                return InteractionResult.FAIL;
            }
            matterAccelerationEntitiesTarget(player, targetPos);
            addCooldown(player);
            useSpirituality(player);

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useAbilityOnEntity(ItemStack stack, LivingEntity player, LivingEntity interactionTarget, InteractionHand hand) {
        if (!player.level().isClientSide()) {
            if (!checkAll(player)) {
                return InteractionResult.FAIL;
            }
            addCooldown(player);
            useSpirituality(player);
            matterAccelerationEntitiesTarget(player, BlockPos.containing(interactionTarget.position()));
        }
        return InteractionResult.SUCCESS;
    }

    public static void matterAccelerationEntitiesSelf(LivingEntity player) {
        if (!player.level().isClientSide()) {
            for (Entity entity : BeyonderUtil.getNonAllyEntitiesNearby(player, 250)) {
                if (BeyonderUtil.isBeyonder(player) || entity instanceof Monster) {
                    entity.getPersistentData().putInt("matterAccelerationEntitiesX", (int) player.getX());
                    entity.getPersistentData().putInt("matterAccelerationEntitiesY", (int) player.getY());
                    entity.getPersistentData().putInt("matterAccelerationEntitiesZ", (int) player.getZ());
                    entity.getPersistentData().putInt("matterAccelerationEntitiesTimer", (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.MATTER_ACCELERATION_ENTITIES.get()));
                }
            }
        }
    }

    public static void matterAccelerationEntitiesTarget(LivingEntity player, BlockPos pos) {
        if (!player.level().isClientSide()) {
            for (Entity entity : BeyonderUtil.getNonAllyEntitiesNearby(player, 250)) {
                if (BeyonderUtil.isBeyonder(player) || entity instanceof Monster) {
                    entity.getPersistentData().putInt("matterAccelerationEntitiesX", pos.getX());
                    entity.getPersistentData().putInt("matterAccelerationEntitiesY", pos.getY());
                    entity.getPersistentData().putInt("matterAccelerationEntitiesZ", pos.getZ());
                    entity.getPersistentData().putInt("matterAccelerationEntitiesTimer", (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.MATTER_ACCELERATION_ENTITIES.get()));
                }
                if (entity instanceof Projectile projectile) {
                    boolean x = projectile.getOwner() != null && projectile.getOwner() instanceof LivingEntity owner && !BeyonderUtil.areAllies(owner, player);
                    if (projectile.getOwner() == null) {
                        x = false;
                    }
                    if (x) {
                        entity.getPersistentData().putInt("matterAccelerationEntitiesX", (int) player.getX());
                        entity.getPersistentData().putInt("matterAccelerationEntitiesY", (int) player.getY());
                        entity.getPersistentData().putInt("matterAccelerationEntitiesZ", (int) player.getZ());
                        entity.getPersistentData().putInt("matterAccelerationEntitiesTimer", (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.MATTER_ACCELERATION_ENTITIES.get()));
                        double projX = player.getX() - entity.getX();
                        double projY = player.getY() - entity.getY();
                        double projZ = player.getZ() - entity.getZ();
                        entity.setDeltaMovement(projX, projY, projZ);
                    }
                }
            }
        }
    }

    private final Lazy<Multimap<Attribute, AttributeModifier>> lazyAttributeMap = Lazy.of(this::createAttributeMap);

    @SuppressWarnings("deprecation")
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
        attributeBuilder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_ENTITY_REACH, "Reach modifier", 150, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with entities
        attributeBuilder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_BLOCK_REACH, "Reach modifier", 150, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with blocks, p much useless for this item
        return attributeBuilder.build();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, accelerates the speed of all entities to the clicked block or entity, or if none are selected, your own position. ONLY if a block or entity is clicked, projectiles are included. Entities that collide together here hurt each other. In addition, projectiles are unable to approach you, as you accelerate them in another direction"));
        tooltipComponents.add(Component.literal("Left Click for Matter Acceleration (Self)"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("800").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("45 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    public static void matterAccelerationEntitiesAndRainEyes(LivingEntity entity) {
        //MATTER ACCELERATION: ENTITIES
        int matterTimer = entity.getPersistentData().getInt("matterAccelerationEntitiesTimer");
        if (entity.tickCount % 2 == 0) {
            if (BeyonderUtil.currentPathwayAndSequenceMatchesNoException(entity, BeyonderClassInit.SAILOR.get(), 0) || (entity.getPersistentData().getBoolean("rainEyes"))) {
                for (Entity pEntity : entity.level().getEntitiesOfClass(Entity.class, entity.getBoundingBox().inflate(100))) {
                    if (!BeyonderUtil.isEntityAlly(entity, pEntity)) {
                        if (BeyonderUtil.getSequence(entity) == 0) {
                            if (pEntity instanceof Projectile pProjectile) {
                                float projectileHeight = pEntity.getBbHeight();
                                float projectileWidth = pEntity.getBbWidth();
                                float projectileSize = Math.min(50, Math.max(10, projectileHeight * projectileWidth * 100));
                                double distance = pEntity.distanceTo(entity);
                                double pushThreshold = Math.max(8.0, projectileSize * 0.25);
                                if (distance <= pushThreshold) {
                                    Vec3 playerPos = entity.position();
                                    Vec3 projectilePos = pEntity.position();
                                    Vec3 pushDirection = projectilePos.subtract(playerPos).normalize();
                                    double pushForce = (projectileSize / 15.0) * (pushThreshold / Math.max(0.3, distance)) * 1.5;
                                    pushForce = Math.min(pushForce, 5.0);
                                    Vec3 pushVelocity = pushDirection.scale(pushForce);
                                    pEntity.setDeltaMovement(pEntity.getDeltaMovement().add(pushVelocity));
                                    pEntity.hurtMarked = true;
                                }
                            } else if (pEntity instanceof LightningEntity lightningEntity) {
                                if (lightningEntity.getLastPos().distanceTo(entity.getOnPos().getCenter()) <= 30) {
                                    float randomX = Math.max(50, BeyonderUtil.getRandomInRange(100));
                                    float randomZ = Math.max(50, BeyonderUtil.getRandomInRange(100));
                                    if (lightningEntity.getTargetEntity() == entity) {
                                        lightningEntity.setTargetEntity(null);
                                        lightningEntity.setTargetPos(new Vec3(randomX, entity.getY() - 100, randomZ));
                                    }
                                    if (lightningEntity.getTargetPos().distanceTo(Vec3.atLowerCornerOf(entity.getOnPos())) <= 20) {
                                        lightningEntity.setTargetPos(new Vec3(randomX, entity.getY() - 100, randomZ));
                                    }
                                }
                            } else if (pEntity instanceof BeamEntity beamEntity) {
                                boolean x = beamEntity.getPersistentData().getBoolean("matterBeam");
                                if (beamEntity.getOwner() == null || beamEntity.getOwner() instanceof Mob) {
                                    x = true;
                                } else if (beamEntity.getOwner() instanceof Player player && BeyonderUtil.isLookingTowards2D(player, entity, 30)) {
                                    x = true;
                                }
                                if (x) {
                                    if (beamEntity.getPersistentData().getInt("beamRange") == 0) {
                                        beamEntity.getPersistentData().putInt("beamRange", (int) beamEntity.getRange());
                                    }
                                    beamEntity.getPersistentData().putBoolean("matterBeam", true);
                                    beamEntity.setRange((int) beamEntity.distanceTo(entity) - (5 * beamEntity.getSize()));
                                }
                            }
                        } if (BeyonderUtil.getSequence(entity) <= 2) {
                            if (entity.getPersistentData().getBoolean("rainEyes")) {
                                if (pEntity instanceof LivingEntity living && living.tickCount % 200 == 0 && !BeyonderUtil.isInvisible(living) && pEntity != entity) {
                                    if (BeyonderUtil.getPathway(living) != null || living instanceof Player) {
                                        int x = (int) living.getX();
                                        int y = (int) living.getY();
                                        int z = (int) living.getZ();
                                        String pathway = "non-existent";
                                        BeyonderClass pathwayResult = BeyonderUtil.getPathway(living);
                                        if (pathwayResult != null) {
                                            pathway = BeyonderUtil.getPathwayName(pathwayResult);
                                        }
                                        Style chatFormatting = BeyonderUtil.getStyle(living);
                                        int sequence = BeyonderUtil.getSequence(living);
                                        String name = living.getName().getString();
                                        int distance = (int) living.distanceTo(entity);
                                        ChatFormatting sequenceColor;
                                        if (sequence >= 7 && sequence <= 9) {
                                            sequenceColor = ChatFormatting.GREEN;
                                        } else if (sequence >= 5 && sequence <= 6) {
                                            sequenceColor = ChatFormatting.YELLOW;
                                        } else if (sequence >= 3 && sequence <= 4) {
                                            sequenceColor = ChatFormatting.RED;
                                        } else if (sequence >= 0 && sequence <= 2) {
                                            sequenceColor = ChatFormatting.DARK_RED;
                                        } else {
                                            sequenceColor = ChatFormatting.WHITE;
                                        }
                                        Component message = Component.literal("")
                                                .append(Component.literal(name).withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN))
                                                .append(Component.literal(" is a ").withStyle(ChatFormatting.BOLD))
                                                .append(Component.literal("Sequence ").withStyle(ChatFormatting.BOLD))
                                                .append(Component.literal(String.valueOf(sequence)).withStyle(ChatFormatting.BOLD, sequenceColor))
                                                .append(Component.literal(" ").withStyle(ChatFormatting.BOLD))
                                                .append(Component.literal(pathway).withStyle(chatFormatting)
                                                        .append(Component.literal(", and located ").withStyle(ChatFormatting.BOLD))
                                                        .append(Component.literal(String.valueOf(distance)).withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE))
                                                        .append(Component.literal(" blocks away from you at ").withStyle(ChatFormatting.BOLD))
                                                        .append(Component.literal(x + "," + y + "," + z).withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA)));

                                        entity.sendSystemMessage(message);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (matterTimer < 1) {
            return;
        }
        entity.getPersistentData().putInt("matterAccelerationEntitiesTimer", matterTimer - 1);
        if (!BeyonderUtil.isCreative(entity)) {
            if (entity.getPersistentData().getInt("matterAccelerationEntitiesX") != 0) {
                int x = entity.getPersistentData().getInt("matterAccelerationEntitiesX");
                int y = entity.getPersistentData().getInt("matterAccelerationEntitiesY");
                int z = entity.getPersistentData().getInt("matterAccelerationEntitiesZ");
                Vec3 matterPos = new Vec3(x, y, z);
                int damage = 0;
                if (entity.tickCount % 2 == 0) {
                    for (LivingEntity living : entity.level().getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(15))) {
                        if (living.getPersistentData().getInt("matterAccelerationEntitiesTimer") >= 1 && living != entity && entity.isAlive()) {
                            damage++;
                        }
                    }
                    if (damage != 0) {
                        entity.hurt(entity.damageSources().generic(), damage * 0.2f);
                        entity.invulnerableTime = 1;
                        entity.hurtTime = 1;
                        entity.hurtDuration = 1;
                    }
                }
                Vec3 entityVec3 = entity.getOnPos().getCenter();
                Vec3 movement = new Vec3(matterPos.x() - entityVec3.x(), matterPos.y() - entityVec3.y(), matterPos.z() - entityVec3.z()).scale(1);
                entity.setDeltaMovement(movement.x(), movement.y(), movement.z());
                entity.hurtMarked = true;
                for (Projectile projectile : entity.level().getEntitiesOfClass(Projectile.class, entity.getBoundingBox().inflate(25))) {
                    if (projectile.getPersistentData().getInt("matterAccelerationEntitiesTimer") >= 1) {
                        Vec3 projectileVec3 = projectile.getOnPos().getCenter();
                        Vec3 projectileMovement = new Vec3(matterPos.x() - projectileVec3.x(), matterPos.y() - projectileVec3.y(), matterPos.z() - projectileVec3.z()).scale(1);
                        projectile.setDeltaMovement(projectileMovement.x(), projectileMovement.y(), projectileMovement.z());
                        projectile.hurtMarked = true;
                    }
                }
            }
        }
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SAILOR_ABILITY", ChatFormatting.BLUE);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target != null) {
            return 10;
        }
        return 0;
    }
    @Override
    public <T> LeftClickType getleftClickEmpty(T item) {
        return new UpdateItemInHandC2S((Integer) item, new ItemStack(ItemInit.MATTER_ACCELERATION_SELF.get()));
    }
}
