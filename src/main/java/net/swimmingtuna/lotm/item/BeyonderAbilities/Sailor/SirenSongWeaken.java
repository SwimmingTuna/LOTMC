package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.networking.packet.UpdateItemInHandC2S;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EFunctions;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventManager;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickHandlerSkillP;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class SirenSongWeaken extends LeftClickHandlerSkillP {

    public SirenSongWeaken(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 5, 300, 600);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        sirenSongWeaken(player, level);
        return InteractionResult.SUCCESS;
    }

    private static void sirenSongWeaken(LivingEntity player, Level level) {
        if (!player.level().isClientSide()) {
            EventManager.addToRegularLoop(player, EFunctions.SIRENSONG.get());
            CompoundTag tag = player.getPersistentData();
            if (tag.getInt("sirenSongWeaken") == 0) {
                tag.putInt("sirenSongWeaken", 400);
            }
            if (tag.getInt("sirenSongWeaken") > 1 && tag.getInt("sirenSongWeaken") < 400) {
                tag.putInt("sirenSongWeaken", 0);
            }
            if (tag.getInt("sirenSongHarm") > 1) {
                tag.putInt("sirenSongHarm", 0);
                tag.putInt("sirenSongWeaken", 400);

            }
            if (tag.getInt("sirenSongStun") > 1) {
                tag.putInt("sirenSongStun", 0);
                tag.putInt("sirenSongWeaken", 400);
            }
            if (tag.getInt("sirenSongStrengthen") > 1) {
                tag.putInt("sirenSongStrengthen", 0);
                tag.putInt("sirenSongWeaken", 400);
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, sings out a song which weakens everyone around you"));
        tooltipComponents.add(Component.literal("Left Click for Siren Song Harm"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("300").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("30 Seconds").withStyle(ChatFormatting.YELLOW)));
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
        if (target != null) {
            return 30;
        }
        return 0;
    }
    @Override
    public <T> LeftClickType getleftClickEmpty(T item) {
        return new UpdateItemInHandC2S((Integer) item, new ItemStack(ItemInit.SIREN_SONG_HARM.get()));
    }
}