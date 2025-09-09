package net.swimmingtuna.lotm.item.OtherItems;

import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.swimmingtuna.lotm.entity.GuardianBoxEntity;
import net.swimmingtuna.lotm.entity.HurricaneOfLightEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.item.Renderer.SwordOfDawnRenderer;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class SwordOfDawn extends SwordItem implements GeoItem {

    public SwordOfDawn(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected) {
        if (entity instanceof LivingEntity livingEntity && !level.isClientSide()) {
            if (livingEntity.tickCount % 20 == 0 && !livingEntity.level().isClientSide()) {
                if (!BeyonderUtil.currentPathwayAndSequenceMatches(livingEntity, BeyonderClassInit.WARRIOR.get(), 6)) {
                    removeItemFromSlot(livingEntity, stack);
                } else {
                    if (BeyonderUtil.getSpirituality(livingEntity) >= 25) {
                        BeyonderUtil.useSpirituality(livingEntity, 25);
                    } else {
                        removeItemFromSlot(livingEntity, stack);
                    }
                    if (livingEntity instanceof Mob mob && mob.getTarget() == null) {
                        removeItemFromSlot(livingEntity, stack);
                    }
                }
            }
        }
        super.inventoryTick(stack, level, entity, itemSlot, isSelected);
    }

    private void removeItemFromSlot(LivingEntity entity, ItemStack stack) {
        if (entity.getItemBySlot(EquipmentSlot.MAINHAND) == stack) {
            entity.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        } else if (entity.getItemBySlot(EquipmentSlot.OFFHAND) == stack) {
            entity.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        if (BeyonderUtil.isPurifiable(pTarget)) {
            pTarget.hurt(BeyonderUtil.magicSource(pAttacker, pTarget), this.getDamage());
            pTarget.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 2, true, true));
            pTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2, true, true));
        }
        return super.hurtEnemy(pStack, pTarget, pAttacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (!pPlayer.level().isClientSide()) {
            if (!pPlayer.isShiftKeyDown()) {
                Vec3 eyePosition = pPlayer.getEyePosition();
                Vec3 lookVector = pPlayer.getLookAngle();
                Vec3 reachVector = eyePosition.add(lookVector.x * 5, lookVector.y * 5, lookVector.z * 5);
                BlockHitResult blockHit = pPlayer.level().clip(new ClipContext(eyePosition, reachVector, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, pPlayer));
                if (blockHit.getType() == HitResult.Type.MISS && BeyonderUtil.getSpirituality(pPlayer) >= 350) {
                    HurricaneOfLightEntity.summonHurricaneOfLightDawn(pPlayer);
                    BeyonderUtil.useSpirituality(pPlayer, 1000 - (BeyonderUtil.getSequence(pPlayer) * 115));
                    pPlayer.getCooldowns().addCooldown(this, 400);
                }
            } else if (pPlayer.isShiftKeyDown() && BeyonderUtil.getSequence(pPlayer) <= 5) {
                int boxCount = 0;
                for (GuardianBoxEntity guardianBox : pPlayer.level().getEntitiesOfClass(GuardianBoxEntity.class, pPlayer.getBoundingBox().inflate(200))) {
                    Optional<UUID> ownerUUID = Optional.of(pPlayer.getUUID());
                    if (Objects.equals(guardianBox.getOwnerUUID(), ownerUUID)) {
                        boxCount++;
                    }
                }
                if (boxCount == 0) {
                    GuardianBoxEntity guardianBoxEntity = new GuardianBoxEntity(EntityInit.GUARDIAN_BOX_ENTITY.get(), pPlayer.level());
                    int sequence = BeyonderUtil.getSequence(pPlayer);
                    ScaleData scaleData = ScaleTypes.BASE.getScaleData(guardianBoxEntity);
                    scaleData.setTargetScale(18 - (sequence));
                    guardianBoxEntity.setOwnerUUID(pPlayer.getUUID());
                    guardianBoxEntity.teleportTo(pPlayer.getX(), pPlayer.getY(), pPlayer.getZ());
                    guardianBoxEntity.setMaxHealth(300 - (sequence * 20));
                    pPlayer.level().addFreshEntity(guardianBoxEntity);
                    pPlayer.getCooldowns().addCooldown(this, 400);
                    BeyonderUtil.useSpirituality(pPlayer, 200);
                }
            }
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, create a hurricane of dawn, destroying everything in it's path. At sequence 2, this will fragment armor, causing it to rapidly break. If you're shifting, you can also create a box of light to protect you and your allies from enemies.").withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(Component.literal("On hit, if the creature is evil, it will deal additional damage, slow, and weaken them.").withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("25 per second, 300 with each use of ability.").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("20 Seconds.").withStyle(ChatFormatting.YELLOW)));
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("DAWN_ITEM", ChatFormatting.YELLOW);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "Activation", 0, state -> PlayState.STOP));
    }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private SwordOfDawnRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new SwordOfDawnRenderer();

                return this.renderer;
            }
        });
    }
}