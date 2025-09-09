package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;


import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.init.ParticleInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EFunctions;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventManager;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class Symbolization extends SimpleAbilityItem {

    public Symbolization(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 2, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player, this, 10);
        useSpirituality(player);
        symbolization(player);
        return InteractionResult.SUCCESS;
    }

    public void symbolization(LivingEntity player) {
        if (!player.level().isClientSide()) {

            CompoundTag tag = player.getPersistentData();
            boolean wasSymbolized = tag.getBoolean("planeswalkerSymbolization");
            boolean isNowSymbolized = !wasSymbolized;
            tag.putBoolean("planeswalkerSymbolization", isNowSymbolized);

            if (player instanceof Player pPlayer) {
                pPlayer.displayClientMessage(Component.literal("You are currently " + (isNowSymbolized ? "" : "NOT ") + "symbolized").withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.BOLD), true);
            }

            if (isNowSymbolized) {
                BeyonderUtil.startFlying(player, 0.15f, 20);
                BeyonderUtil.setInvisible(player, true, 10);
                EventManager.addToRegularLoop(player, EFunctions.SYMBOLIZATION.get());
            } else {
                BeyonderUtil.stopFlying(player);
                BeyonderUtil.setInvisible(player, false, 0);
                EventManager.removeFromRegularLoop(player, EFunctions.SYMBOLIZATION.get());
            }
        }
    }


    public static void symbolizationTick(LivingEvent.LivingTickEvent event) {
        LivingEntity living = event.getEntity();

        if (!living.level().isClientSide() && living.getPersistentData().getBoolean("planeswalkerSymbolization")) {
            CompoundTag tag = living.getPersistentData();
            BeyonderUtil.startFlying(living, 0.15f, 20);

            if (BeyonderUtil.getSpirituality(living) < 10) {
                BeyonderUtil.setInvisible(living, false, 0);
                tag.putBoolean("planeswalkerSymbolization", false);
                EventManager.removeFromRegularLoop(living, EFunctions.SYMBOLIZATION.get());
            } else {
                BeyonderUtil.useSpirituality(living, 10);
                float scale = BeyonderUtil.getScale(living);
                float random = BeyonderUtil.getRandomInRange(scale) * 2;

                if (living.level() instanceof ServerLevel serverLevel) {
                    for (int i = 0; i <= 1; i++) {
                        serverLevel.sendParticles(ParticleInit.SYMBOLIZATION_PARTICLE.get(), living.getX() + random, living.getY() + random, living.getZ() + random, 0, 0, 0, 0, 0);
                    }
                }

                if (living.tickCount % 20 == 0) {
                    BeyonderUtil.setInvisible(living, true, 30);
                }
            }
        }
    }

    public static void symbolizationAttack(LivingAttackEvent event) {
        LivingEntity attacked = event.getEntity();
        if (!attacked.level().isClientSide()) {
            if (attacked.getPersistentData().getBoolean("planeswalkerSymbolization")) {
                if (event.getAmount() <= BeyonderUtil.getDamage(attacked).get(ItemInit.SYMBOLIZATION.get())) {
                    event.setCanceled(true);
                }
            }
        }
    }

    public static void cancelSymbolization(LivingEntity player) {
        CompoundTag tag = player.getPersistentData();
        if (tag.contains("planeswalkerSymbolization") && tag.getBoolean("planeswalkerSymbolization")) {
            tag.remove("planeswalkerSymbolization");
            BeyonderUtil.stopFlying(player);
            BeyonderUtil.setInvisible(player, false, 0);
        }
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Transform into a conceptual entity, immune to nearly every form of damage, bar any immense amounts. In exchange, the power of all your moves will be halved."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("Blink Distance").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("None").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("APPRENTICE_ABILITY", ChatFormatting.AQUA);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (PlayerMobEntity.isCopy(livingEntity)) {
            return 0;
        }
        if (target != null && !livingEntity.getPersistentData().getBoolean("planeswalkerSymbolization")) {
            return 70;
        } else if (target == null && livingEntity.getPersistentData().getBoolean("planeswalkerSymbolization")) {
            return 100;
        }
        return 0;
    }
}
