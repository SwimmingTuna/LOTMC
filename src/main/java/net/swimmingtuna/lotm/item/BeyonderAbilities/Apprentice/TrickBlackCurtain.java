package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.blocks.DimensionalSight.DimensionalSightTileEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.init.ParticleInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.networking.packet.UpdateItemInHandC2S;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickHandlerSkillP;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class TrickBlackCurtain extends LeftClickHandlerSkillP {
    public TrickBlackCurtain(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 8, 25, 140);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity livingEntity, InteractionHand hand) {
        if (!checkAll(livingEntity)) {
            return InteractionResult.FAIL;
        }
        spawnParticles(livingEntity);
        addCooldown(livingEntity);
        useSpirituality(livingEntity);
        return InteractionResult.SUCCESS;
    }

    public static void spawnParticles(LivingEntity entity) {
        int height = 3;
        int width = 5;
        ServerLevel level = (ServerLevel) entity.level();

        Vec3 look = entity.getLookAngle();
        look = new Vec3(look.x, 0, look.z).normalize().scale(3);
        Vec3 right = look.cross(new Vec3(0, 1, 0)).normalize();

        BlockPos basePos = entity.blockPosition().offset((int) Math.round(look.x), 0, (int) Math.round(look.z));
        int counter = 0;
        for (int y = 0; y < height; y++) {
            for (int x = (int) -(Math.floor((double) width / 2)); x <= (int) (Math.floor((double) width / 2)); x++) {
                Vec3 offset = right.scale(x).add(0, y, 0);
                Vec3 spawnPos;
                DimensionalSightTileEntity dimensionalSightTileEntity = BeyonderUtil.findNearbyDimensionalSight(entity);
                if (dimensionalSightTileEntity != null && dimensionalSightTileEntity.getScryTarget() != null) {
                    if (counter == 0) {
                        entity.sendSystemMessage(Component.literal("You created a curtain in front of your Dimensional Sight Target").withStyle(ChatFormatting.AQUA));
                        counter++;
                    }
                    basePos = dimensionalSightTileEntity.getScryTarget().blockPosition().offset((int) Math.round(look.x), 0, (int) Math.round(look.z));
                }
                spawnPos = Vec3.atCenterOf(basePos).add(offset);
                level.sendParticles(ParticleInit.BLACK_CURTAIN.get(), spawnPos.x, spawnPos.y, spawnPos.z, 100, 0.3, 0.3, 0.3, 0.01);
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, generates a curtain of black fog in the direction you are looking."));
        tooltipComponents.add(Component.literal("Left click for Trick: Burning"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("25").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("7 Seconds").withStyle(ChatFormatting.YELLOW)));
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
        if (target instanceof Player) {
            return 20;
        }
        return 0;
    }
    @Override
    public <T> LeftClickType getleftClickEmpty(T item) {
        return new UpdateItemInHandC2S((Integer) item, new ItemStack(ItemInit.TRICKBURNING.get()));
    }
}