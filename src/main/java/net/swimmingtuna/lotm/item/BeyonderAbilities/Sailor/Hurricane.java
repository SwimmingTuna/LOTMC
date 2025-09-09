package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.entity.TornadoEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.networking.packet.LeftClickC2S;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EFunctions;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventManager;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickHandlerSkill;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

import static net.swimmingtuna.lotm.util.BeyonderUtil.findSurfaceY;

public class Hurricane extends LeftClickHandlerSkill {

    public Hurricane(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 4, 1000, 1800);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        hurricaneAbility(player);
        addCooldown(player);
        useSpirituality(player);
        return InteractionResult.SUCCESS;
    }

    private void hurricaneAbility(LivingEntity pPlayer) {
        if (!pPlayer.level().isClientSide()) {
            EventManager.addToRegularLoop(pPlayer, EFunctions.HURRICANE.get());
            pPlayer.getPersistentData().putInt("sailorHurricane", (int) (float) BeyonderUtil.getDamage(pPlayer).get(ItemInit.HURRICANE.get()));
        }
    }

    public static void hurricane(LivingEntity livingEntity) {
        //HURRICANE
        CompoundTag tag = livingEntity.getPersistentData();
        boolean sailorHurricaneRain = tag.getBoolean("sailorHurricaneRain");
        BlockPos pos = new BlockPos((int) (livingEntity.getX() + (Math.random() * 100 - 100)), (int) (livingEntity.getY() - 100), (int) (livingEntity.getZ() + (Math.random() * 300 - 300)));
        int hurricane = tag.getInt("sailorHurricane");
        if (hurricane < 1) {
            EventManager.removeFromRegularLoop(livingEntity, EFunctions.HURRICANE.get());
            return;
        }
        if (sailorHurricaneRain) {
            tag.putInt("sailorHurricane", hurricane - 1);
            if (hurricane == 600 && livingEntity.level() instanceof ServerLevel serverLevel) {
                serverLevel.setWeatherParameters(0, 700, true, true);
            }
            if (hurricane % 5 == 0) {
                SailorLightning.lightningHigh(livingEntity, livingEntity.level());
            }
            if (hurricane == 600 || hurricane == 300) {
                List<LivingEntity> nearbyEntities = BeyonderUtil.getNonAlliesNearby(livingEntity, 80);
                List<LivingEntity> validTargets = new ArrayList<>();
                int lowestSequence = Integer.MAX_VALUE;
                for (LivingEntity living : nearbyEntities) {
                    int sequence = BeyonderUtil.getSequence(living);
                    if (sequence == -1 || sequence == 10) {
                        continue;
                    }
                    if (sequence < lowestSequence) {
                        lowestSequence = sequence;
                        validTargets.clear();
                        validTargets.add(living);
                    } else if (sequence == lowestSequence) {
                        validTargets.add(living);
                    }
                }
                Set<BlockPos> usedPositions = new HashSet<>();
                int tornadoCount = 0;
                Collections.shuffle(validTargets);

                for (LivingEntity target : validTargets) {
                    if (tornadoCount >= 5) break;
                    double surfaceY = findSurfaceY(target, target.getX(), target.getZ(), target.level().dimension());
                    if (surfaceY == -1) continue;
                    BlockPos targetPos = new BlockPos((int) target.getX(), (int) surfaceY - 10, (int) target.getZ());
                    if (usedPositions.contains(targetPos)) {
                        continue;
                    }
                    usedPositions.add(targetPos);
                    TornadoEntity tornado = new TornadoEntity(livingEntity.level(), livingEntity, 0, 0, 0);
                    tornado.teleportTo(targetPos.getX(), targetPos.getY() + 100, targetPos.getZ());
                    tornado.setTornadoRandom(true);
                    tornado.setOwner(livingEntity);
                    tornado.setTornadoHeight(300);
                    tornado.setTornadoRadius(30);
                    tornado.setTornadoPickup(false);
                    livingEntity.level().addFreshEntity(tornado);

                    tornadoCount++;
                }
                while (tornadoCount < 5) {
                    double surfaceY = findSurfaceY(livingEntity, pos.getX(), pos.getZ(), livingEntity.level().dimension());
                    if (surfaceY == -1) break;
                    BlockPos randomPos = new BlockPos(pos.getX(), (int) surfaceY, pos.getZ());
                    if (!usedPositions.contains(randomPos)) {
                        usedPositions.add(randomPos);
                        TornadoEntity tornado = new TornadoEntity(livingEntity.level(), livingEntity, 0, 0, 0);
                        tornado.teleportTo(randomPos.getX(), randomPos.getY() + 100, randomPos.getZ());
                        tornado.setTornadoRandom(true);
                        tornado.setOwner(livingEntity);
                        tornado.setTornadoHeight(300);
                        tornado.setTornadoRadius(30);
                        tornado.setTornadoPickup(false);
                        livingEntity.level().addFreshEntity(tornado);

                        tornadoCount++;
                    }
                    pos = new BlockPos((int) (livingEntity.getX() + (Math.random() * 100 - 100)), (int) (livingEntity.getY() - 100), (int) (livingEntity.getZ() + (Math.random() * 300 - 300)));
                }
            }
        }
        if (!sailorHurricaneRain && livingEntity.level() instanceof ServerLevel serverLevel && hurricane == 600) {
            tag.putInt("sailorHurricane", hurricane - 1);
            serverLevel.setWeatherParameters(0, 700, true, false);
        }
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, summon a hurricane which will rain down lightning and summon tornados in a large area around you for at least 30 seconds, depending on sequence. The tornados and lightning bolts will target the highest sequence non-allies around you."));
        tooltipComponents.add(Component.literal("Left click to choose whether to make it only rain or have the full hurricane effect."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("1000").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("1.5 Minutes").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SAILOR_ABILITY", ChatFormatting.BLUE);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target == null && BeyonderUtil.getSpirituality(livingEntity) == BeyonderUtil.getMaxSpirituality(livingEntity)) {
            return 2;
        }
        return 0;
    }

    @Override
    public LeftClickType getleftClickEmpty() {
        return new LeftClickC2S();
    }
}
