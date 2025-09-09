package net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses.BeyonderTicks.Apprentice;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.capabilities.replicated_entity.ReplicatedEntityUtils;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.ClientWormOfStarDataS2C;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IFunction;
import net.swimmingtuna.lotm.util.BeyonderUtil;

public class ApprenticeTickLayer implements IFunction {
    @Override
    public void use(LivingEvent.LivingTickEvent event) {
        LivingEntity player = event.getEntity();
        int sequenceLevel = BeyonderUtil.getSequence(player);

        if (player.level().getGameTime() % 50 == 0) {
            CompoundTag tag = player.getPersistentData();
            int maxWormCount = 0;
            int wormRegenAmount = 0;

            switch (sequenceLevel) {
                case 6:
                    tag.putInt("maxScribedAbilities", 20);
                    break;
                case 5:
                    tag.putInt("maxScribedAbilities", 25);
                    break;
                case 4:
                    maxWormCount = 200;
                    wormRegenAmount = 1;
                    tag.putInt("wormOfStar", Math.min(maxWormCount, tag.getInt("wormOfStar") + wormRegenAmount));

                    if (player instanceof ServerPlayer serverPlayer) {
                        LOTMNetworkHandler.sendToPlayer(new ClientWormOfStarDataS2C(tag.getInt("wormOfStar")), serverPlayer);
                    }
                    tag.putInt("maxScribedAbilities", 30);

                    break;
                case 3:
                    maxWormCount = 800;
                    wormRegenAmount = 3;
                    tag.putInt("wormOfStar", Math.min(maxWormCount, tag.getInt("wormOfStar") + wormRegenAmount));

                    if (player instanceof ServerPlayer serverPlayer) {
                        LOTMNetworkHandler.sendToPlayer(new ClientWormOfStarDataS2C(tag.getInt("wormOfStar")), serverPlayer);
                    }

                    tag.putInt("maxScribedAbilities", 35);
                    break;
                case 2:
                    maxWormCount = 4000;
                    wormRegenAmount = 10;
                    tag.putInt("wormOfStar", Math.min(maxWormCount, tag.getInt("wormOfStar") + wormRegenAmount));

                    if (player instanceof ServerPlayer serverPlayer) {
                        LOTMNetworkHandler.sendToPlayer(new ClientWormOfStarDataS2C(tag.getInt("wormOfStar")), serverPlayer);
                    }

                    tag.putInt("maxScribedAbilities", 40);
                    if (player instanceof Player pPlayer) {
                        ReplicatedEntityUtils.setMaxEntities(pPlayer, 5);
                        ReplicatedEntityUtils.setMaxAbilitiesUse(pPlayer, 1);
                    }
                    break;
                case 1:
                    maxWormCount = 16000;
                    wormRegenAmount = 25;
                    tag.putInt("wormOfStar", Math.min(maxWormCount, tag.getInt("wormOfStar") + wormRegenAmount));

                    if (player instanceof ServerPlayer serverPlayer) {
                        LOTMNetworkHandler.sendToPlayer(new ClientWormOfStarDataS2C(tag.getInt("wormOfStar")), serverPlayer);
                    }

                    tag.putInt("maxScribedAbilities", 45);
                    if (player instanceof Player pPlayer) {
                        ReplicatedEntityUtils.setMaxEntities(pPlayer, 10);
                        ReplicatedEntityUtils.setMaxAbilitiesUse(pPlayer, 4);
                    }
                    break;
                case 0:
                    maxWormCount = 80000;
                    wormRegenAmount = 100;
                    tag.putInt("wormOfStar", Math.min(maxWormCount, tag.getInt("wormOfStar") + wormRegenAmount));

                    if (player instanceof ServerPlayer serverPlayer) {
                        LOTMNetworkHandler.sendToPlayer(new ClientWormOfStarDataS2C(tag.getInt("wormOfStar")), serverPlayer);
                    }

                    tag.putInt("maxScribedAbilities", 50);
                    if (player instanceof Player pPlayer) {
                        ReplicatedEntityUtils.setMaxEntities(pPlayer, 20);
                        ReplicatedEntityUtils.setMaxAbilitiesUse(pPlayer, 10);
                    }
                    break;
            }
            if (sequenceLevel <= 4) {
                if (tag.getInt("wormOfStar") < maxWormCount * 0.1) {
                    player.sendSystemMessage(Component.literal("Died due to a lack of Worms of Star").withStyle(ChatFormatting.DARK_RED).withStyle(ChatFormatting.BOLD));
                    player.kill();
                } else if (tag.getInt("wormOfStar") < maxWormCount * 0.25) {
                    player.sendSystemMessage(Component.literal("You can't handle the low amount of Worms of Star and will soon die").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                    player.hurt(player.damageSources().magic(), player.getMaxHealth() / 7);
                    BeyonderUtil.applyMobEffect(player, MobEffects.BLINDNESS, 100, 1, true, true);
                } else if (tag.getInt("wormOfStar") < maxWormCount * 0.5) {
                    player.sendSystemMessage(Component.literal("You are dangerously low on Worms of Star and are taking damage because of it").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                    player.hurt(player.damageSources().magic(), player.getMaxHealth() / 10);
                    BeyonderUtil.applyMobEffect(player, MobEffects.DARKNESS, 100, 1, true, true);
                } else if (tag.getInt("wormOfStar") < maxWormCount * 0.75) {
                    player.hurt(player.damageSources().magic(), player.getMaxHealth() / 20);
                    player.sendSystemMessage(Component.literal("You are over exerting yourself, and shouldn't separate any more Worms of Stars").withStyle(ChatFormatting.YELLOW).withStyle(ChatFormatting.BOLD));
                }
            }
        }
    }

    @Override
    public String getID() {
        return "ApprenticeTickEventID";
    }
}
