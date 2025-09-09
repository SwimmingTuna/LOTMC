package net.swimmingtuna.lotm.item.BeyonderAbilities.Monster;


import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.blocks.MonsterDomainBlockEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.BlockInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.networking.packet.MonsterDomainLeftClickC2S;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickHandlerSkill;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickType;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DomainOfDecay extends LeftClickHandlerSkill {
    public static final BooleanProperty LIT = BooleanProperty.create("lit");

    public DomainOfDecay(Properties properties) {
        super(properties, BeyonderClassInit.MONSTER, 4, 400, 600);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        makeDomainOfProvidence(player);
        useSpirituality(player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected) {
        if (entity instanceof Player livingEntity) {
            CompoundTag tag = livingEntity.getPersistentData();
            if (livingEntity.tickCount % 2 == 0 && !level.isClientSide()) {
                int radius = tag.getInt("monsterDomainRadius");
                if (livingEntity.isShiftKeyDown() && (livingEntity.getMainHandItem().getItem() instanceof DomainOfDecay)) {
                    tag.putInt("monsterDomainRadius", radius + 5);
                    int maxRadius = (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.DECAYDOMAIN.get());
                    livingEntity.displayClientMessage(Component.literal("Current Domain Radius is " + radius).withStyle(BeyonderUtil.getStyle(livingEntity)), true);
                    if (radius >= maxRadius + 1) {
                        livingEntity.displayClientMessage(Component.literal("Current Domain Radius is 0").withStyle(BeyonderUtil.getStyle(livingEntity)), true);
                        tag.putInt("monsterDomainRadius", 0);
                    }
                }
            }
        }
        super.inventoryTick(stack, level, entity, itemSlot, isSelected);
    }


    private void makeDomainOfProvidence(LivingEntity player) {
        if (!player.level().isClientSide()) {
            Level level = player.level();
            BlockPos playerPos = player.getOnPos();
            AtomicBoolean foundOwnedDomain = new AtomicBoolean(false);

            BlockPos.betweenClosedStream(playerPos.offset(-5, -5, -5), playerPos.offset(5, 5, 5)).forEach(pos -> {
                if (level.getBlockEntity(pos) instanceof MonsterDomainBlockEntity domainEntity) {
                    if (domainEntity.getOwner() != null && domainEntity.getOwner() == player) {
                        level.removeBlock(pos, false);
                        foundOwnedDomain.set(true);
                        player.sendSystemMessage(Component.literal("Removed your domain at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()));
                    }
                }
            });

            if (!foundOwnedDomain.get()) {
                Vec3 eyePosition = player.getEyePosition();
                Vec3 lookVector = player.getLookAngle();
                Vec3 reachVector = eyePosition.add(lookVector.x * blockReach, lookVector.y * blockReach, lookVector.z * blockReach);
                ClipContext clipContext = new ClipContext(eyePosition, reachVector, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
                BlockHitResult blockHit = level.clip(clipContext);
                if (blockHit.getType() != HitResult.Type.BLOCK) {
                    level.setBlock(playerPos, BlockInit.MONSTER_DOMAIN_BLOCK.get().defaultBlockState().setValue(LIT, false), 3);
                    if (level.getBlockEntity(playerPos) instanceof MonsterDomainBlockEntity domainEntity) {
                        domainEntity.setOwner(player);
                        int radius = player.getPersistentData().getInt("monsterDomainRadius");
                        domainEntity.setRadius(radius);
                        domainEntity.setBad(true);
                        domainEntity.setChanged();
                    }
                }
            }
        }
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, put down a domain of decay, which will cause everything in the radius of it to encounter severe negative effects, the strength of them being stronger the smaller area. Examples include entities getting withered, ores turning to stone, crops dying, tools getting damaged, and more. Use near a domain in order to remove it"));
        tooltipComponents.add(Component.literal("Shift to increase radius"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("400").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("30 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("MONSTER_ABILITY", ChatFormatting.GRAY);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        return 0;
    }

    @Override
    public LeftClickType getleftClickEmpty() {
        return new MonsterDomainLeftClickC2S();
    }
}
