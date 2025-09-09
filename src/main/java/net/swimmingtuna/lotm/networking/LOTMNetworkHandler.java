package net.swimmingtuna.lotm.networking;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.blocks.DimensionalSight.DimensionalSightPacketS2C;
import net.swimmingtuna.lotm.blocks.DimensionalSight.ScryingEntityPacketS2C;
import net.swimmingtuna.lotm.networking.packet.*;
import net.swimmingtuna.lotm.util.AllyInformation.SyncAlliesPacket;
import net.swimmingtuna.lotm.util.CapabilitySyncer.network.SimpleEntityCapabilityStatusPacket;
import net.swimmingtuna.lotm.util.ClientData.ClientShouldntMoveData;

import java.util.List;
import java.util.function.BiConsumer;

public class LOTMNetworkHandler {
    private static final String PROTOCOL_VERSION = "1.0";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(LOTM.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static int nextId = 0;

    private static int id() {
        return nextId++;
    }

    public static void register() {
        List<BiConsumer<SimpleChannel, Integer>> packets = ImmutableList.<BiConsumer<SimpleChannel, Integer>>builder()
                .add(SimpleEntityCapabilityStatusPacket::register)
                .add(SpiritualityC2S::register)
                .build();
        packets.forEach(consumer -> consumer.accept(INSTANCE, id()));


        INSTANCE.messageBuilder(LuckManipulationLeftClickC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(LuckManipulationLeftClickC2S::new)
                .encoder(LuckManipulationLeftClickC2S::toByte)
                .consumerMainThread(LuckManipulationLeftClickC2S::handle)
                .add();
        INSTANCE.messageBuilder(DimensionalSightCompleteDataPacketS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(DimensionalSightCompleteDataPacketS2C::new)
                .encoder(DimensionalSightCompleteDataPacketS2C::encode)
                .consumerMainThread(DimensionalSightCompleteDataPacketS2C::handle)
                .add();
        INSTANCE.messageBuilder(CleanupDimensionalSightPacketS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(CleanupDimensionalSightPacketS2C::decode)
                .encoder(CleanupDimensionalSightPacketS2C::encode)
                .consumerMainThread(CleanupDimensionalSightPacketS2C::handle)
                .add();


        INSTANCE.messageBuilder(AbilityUsePacketC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(AbilityUsePacketC2S::new)
                .encoder(AbilityUsePacketC2S::toByte)
                .consumerMainThread(AbilityUsePacketC2S::handle)
                .add();
        INSTANCE.messageBuilder(ProphesizeLeftClickC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ProphesizeLeftClickC2S::new)
                .encoder(ProphesizeLeftClickC2S::toByte)
                .consumerMainThread(ProphesizeLeftClickC2S::handle)
                .add();
        INSTANCE.messageBuilder(MercuryLiqueficationC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(MercuryLiqueficationC2S::new)
                .encoder(MercuryLiqueficationC2S::toByte)
                .consumerMainThread(MercuryLiqueficationC2S::handle)
                .add();
        INSTANCE.messageBuilder(MonsterDomainLeftClickC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(MonsterDomainLeftClickC2S::new)
                .encoder(MonsterDomainLeftClickC2S::toByte)
                .consumerMainThread(MonsterDomainLeftClickC2S::handle)
                .add();
        INSTANCE.messageBuilder(SwordOfTwilightC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SwordOfTwilightC2S::new)
                .encoder(SwordOfTwilightC2S::toByte)
                .consumerMainThread(SwordOfTwilightC2S::handle)
                .add();
        INSTANCE.messageBuilder(DeathKnellLeftClickC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(DeathKnellLeftClickC2S::new)
                .encoder(DeathKnellLeftClickC2S::toByte)
                .consumerMainThread(DeathKnellLeftClickC2S::handle)
                .add();


        INSTANCE.messageBuilder(ForceLookPacketS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ForceLookPacketS2C::decode)
                .encoder(ForceLookPacketS2C::encode)
                .consumerMainThread(ForceLookPacketS2C::handle)
                .add();
        INSTANCE.messageBuilder(StopForceLookPacketS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(StopForceLookPacketS2C::decode)
                .encoder(StopForceLookPacketS2C::encode)
                .consumerMainThread(StopForceLookPacketS2C::handle)
                .add();
        INSTANCE.messageBuilder(SwordOfSilverC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SwordOfSilverC2S::new)
                .encoder(SwordOfSilverC2S::toByte)
                .consumerMainThread(SwordOfSilverC2S::handle)
                .add();
        INSTANCE.messageBuilder(SyncAlliesPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncAlliesPacket::decode)
                .encoder(SyncAlliesPacket::encode)
                .consumerMainThread(SyncAlliesPacket::handle)
                .add();
        INSTANCE.messageBuilder(LightningEntityPacketS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(LightningEntityPacketS2C::new)
                .encoder(LightningEntityPacketS2C::encode)
                .consumerMainThread(LightningEntityPacketS2C::handle)
                .add();
        INSTANCE.messageBuilder(SyncShouldntRenderInvisibilityPacketS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncShouldntRenderInvisibilityPacketS2C::new)
                .encoder(SyncShouldntRenderInvisibilityPacketS2C::encode)
                .consumerMainThread(SyncShouldntRenderInvisibilityPacketS2C::handle)
                .add();
        INSTANCE.messageBuilder(SyncShouldntRenderHandPacketS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncShouldntRenderHandPacketS2C::new)
                .encoder(SyncShouldntRenderHandPacketS2C::encode)
                .consumerMainThread(SyncShouldntRenderHandPacketS2C::handle)
                .add();
        INSTANCE.messageBuilder(SendPlayerRenderDataS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SendPlayerRenderDataS2C::new)
                .encoder(SendPlayerRenderDataS2C::toBytes)
                .consumerMainThread(SendPlayerRenderDataS2C::handle)
                .add();
        INSTANCE.messageBuilder(SyncAntiConcealmentPacketS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncAntiConcealmentPacketS2C::new)
                .encoder(SyncAntiConcealmentPacketS2C::encode)
                .consumerMainThread(SyncAntiConcealmentPacketS2C::handle)
                .add();
        INSTANCE.messageBuilder(ScribeCopyAbilityC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ScribeCopyAbilityC2S::new)
                .encoder(ScribeCopyAbilityC2S::toByte)
                .consumerMainThread(ScribeCopyAbilityC2S::handle)
                .add();
        INSTANCE.messageBuilder(MonsterLeftClickC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(MonsterLeftClickC2S::new)
                .encoder(MonsterLeftClickC2S::toByte)
                .consumerMainThread(MonsterLeftClickC2S::handle)
                .add();
        INSTANCE.messageBuilder(GigantificationC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(GigantificationC2S::new)
                .encoder(GigantificationC2S::toByte)
                .consumerMainThread(GigantificationC2S::handle)
                .add();
        INSTANCE.messageBuilder(AddItemInInventoryC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(AddItemInInventoryC2S::new)
                .encoder(AddItemInInventoryC2S::toByte)
                .consumerMainThread(AddItemInInventoryC2S::handle)
                .add();
        INSTANCE.messageBuilder(LeftClickC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(LeftClickC2S::new)
                .encoder(LeftClickC2S::toByte)
                .consumerMainThread(LeftClickC2S::handle)
                .add();
        INSTANCE.messageBuilder(UpdateItemInHandC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(UpdateItemInHandC2S::new)
                .encoder(UpdateItemInHandC2S::toByte)
                .consumerMainThread(UpdateItemInHandC2S::handle)
                .add();
        INSTANCE.messageBuilder(MatterAccelerationBlockC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(MatterAccelerationBlockC2S::new)
                .encoder(MatterAccelerationBlockC2S::toByte)
                .consumerMainThread(MatterAccelerationBlockC2S::handle)
                .add();
        INSTANCE.messageBuilder(SpiritVisionC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SpiritVisionC2S::new)
                .encoder(SpiritVisionC2S::toByte)
                .consumerMainThread(SpiritVisionC2S::handle)
                .add();
        INSTANCE.messageBuilder(SpiritWorldTraversalC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SpiritWorldTraversalC2S::new)
                .encoder(SpiritWorldTraversalC2S::toByte)
                .consumerMainThread(SpiritWorldTraversalC2S::handle)
                .add();
        INSTANCE.messageBuilder(MisfortuneManipulationLeftClickC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(MisfortuneManipulationLeftClickC2S::new)
                .encoder(MisfortuneManipulationLeftClickC2S::toByte)
                .consumerMainThread(MisfortuneManipulationLeftClickC2S::handle)
                .add();
        INSTANCE.messageBuilder(DawnWeaponryLeftClickC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(DawnWeaponryLeftClickC2S::new)
                .encoder(DawnWeaponryLeftClickC2S::toByte)
                .consumerMainThread(DawnWeaponryLeftClickC2S::handle)
                .add();
        INSTANCE.messageBuilder(ConsciousnessStrollC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ConsciousnessStrollC2S::new)
                .encoder(ConsciousnessStrollC2S::toByte)
                .consumerMainThread(ConsciousnessStrollC2S::handle)
                .add();
        INSTANCE.messageBuilder(SealingLeftClickC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SealingLeftClickC2S::new)
                .encoder(SealingLeftClickC2S::toByte)
                .consumerMainThread(SealingLeftClickC2S::handle)
                .add();
        INSTANCE.messageBuilder(MonsterCalamityIncarnationLeftClickC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(MonsterCalamityIncarnationLeftClickC2S::new)
                .encoder(MonsterCalamityIncarnationLeftClickC2S::toByte)
                .consumerMainThread(MonsterCalamityIncarnationLeftClickC2S::handle)
                .add();
        INSTANCE.messageBuilder(FalseProphecyLeftClickC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(FalseProphecyLeftClickC2S::new)
                .encoder(FalseProphecyLeftClickC2S::toByte)
                .consumerMainThread(FalseProphecyLeftClickC2S::handle)
                .add();
        INSTANCE.messageBuilder(CalamityEnhancementLeftClickC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(CalamityEnhancementLeftClickC2S::new)
                .encoder(CalamityEnhancementLeftClickC2S::toByte)
                .consumerMainThread(CalamityEnhancementLeftClickC2S::handle)
                .add();
        INSTANCE.messageBuilder(NonVisibleS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(NonVisibleS2C::decode)
                .encoder(NonVisibleS2C::encode)
                .consumerMainThread(NonVisibleS2C::handle)
                .add();
        INSTANCE.messageBuilder(UpdateEntityLocationS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(UpdateEntityLocationS2C::decode)
                .encoder(UpdateEntityLocationS2C::encode)
                .consumerMainThread(UpdateEntityLocationS2C::handle)
                .add();
        INSTANCE.messageBuilder(UpdateDragonBreathS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(UpdateDragonBreathS2C::decode)
                .encoder(UpdateDragonBreathS2C::encode)
                .consumerMainThread(UpdateDragonBreathS2C::handle)
                .add();
        INSTANCE.messageBuilder(SyncSequencePacketS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncSequencePacketS2C::new)
                .encoder(SyncSequencePacketS2C::encode)
                .consumerMainThread(SyncSequencePacketS2C::handle)
                .add();
        INSTANCE.messageBuilder(SyncLeftClickCooldownS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncLeftClickCooldownS2C::new)
                .encoder(SyncLeftClickCooldownS2C::encode)
                .consumerMainThread(SyncLeftClickCooldownS2C::handle)
                .add();
        INSTANCE.messageBuilder(SpiritWorldSyncPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SpiritWorldSyncPacket::new)
                .encoder(SpiritWorldSyncPacket::toBytes)
                .consumerMainThread(SpiritWorldSyncPacket::handle)
                .add();
        INSTANCE.messageBuilder(RequestCooldownSetC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(RequestCooldownSetC2S::new)
                .encoder(RequestCooldownSetC2S::toByte)
                .consumerMainThread(RequestCooldownSetC2S::handle)
                .add();
        INSTANCE.messageBuilder(SendParticleS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SendParticleS2C::new)
                .encoder(SendParticleS2C::encode)
                .consumerMainThread(SendParticleS2C::handle)
                .add();
        INSTANCE.messageBuilder(SendDustParticleS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SendDustParticleS2C::new)
                .encoder(SendDustParticleS2C::encode)
                .consumerMainThread(SendDustParticleS2C::handle)
                .add();
        INSTANCE.messageBuilder(SyncAbilityCooldownsS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncAbilityCooldownsS2C::decode)
                .encoder(SyncAbilityCooldownsS2C::encode)
                .consumerMainThread(SyncAbilityCooldownsS2C::handle)
                .add();
        INSTANCE.messageBuilder(SyncAbilitiesS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncAbilitiesS2C::new)
                .encoder(SyncAbilitiesS2C::encode)
                .consumerMainThread(SyncAbilitiesS2C::handle)
                .add();
        INSTANCE.messageBuilder(ClientFogDataS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ClientFogDataS2C::new)
                .encoder(ClientFogDataS2C::toByte)
                .consumerMainThread(ClientFogDataS2C::handle)
                .add();
        INSTANCE.messageBuilder(ClientGrayscaleS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ClientGrayscaleS2C::new)
                .encoder(ClientGrayscaleS2C::toByte)
                .consumerMainThread(ClientGrayscaleS2C::handle)
                .add();
        INSTANCE.messageBuilder(ClientShouldntMovePacketS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ClientShouldntMovePacketS2C::new)
                .encoder(ClientShouldntMovePacketS2C::toByte)
                .consumerMainThread(ClientShouldntMovePacketS2C::handle)
                .add();
        INSTANCE.messageBuilder(ClientWormOfStarDataS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ClientWormOfStarDataS2C::new)
                .encoder(ClientWormOfStarDataS2C::toByte)
                .consumerMainThread(ClientWormOfStarDataS2C::handle)
                .add();
        INSTANCE.messageBuilder(ClearAbilitiesS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ClearAbilitiesS2C::new)
                .encoder(ClearAbilitiesS2C::toByte)
                .consumerMainThread(ClearAbilitiesS2C::handle)
                .add();
        INSTANCE.messageBuilder(ToggleDistanceC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ToggleDistanceC2S::new)
                .encoder(ToggleDistanceC2S::toByte)
                .consumerMainThread(ToggleDistanceC2S::handle)
                .add();
        INSTANCE.messageBuilder(TravelerWaypointC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(TravelerWaypointC2S::new)
                .encoder(TravelerWaypointC2S::toByte)
                .consumerMainThread(TravelerWaypointC2S::handle)
                .add();
        INSTANCE.messageBuilder(DimensionalSightPacketS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(DimensionalSightPacketS2C::decode)
                .encoder(DimensionalSightPacketS2C::encode)
                .consumerMainThread(DimensionalSightPacketS2C::handle)
                .add();
        INSTANCE.messageBuilder(SyncPlayerMobTrackerPacketS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncPlayerMobTrackerPacketS2C::new)
                .encoder(SyncPlayerMobTrackerPacketS2C::toBytes)
                .consumerMainThread(SyncPlayerMobTrackerPacketS2C::handle)
                .add();
        INSTANCE.messageBuilder(ScryingEntityPacketS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ScryingEntityPacketS2C::decode)
                .encoder(ScryingEntityPacketS2C::encode)
                .consumerMainThread(ScryingEntityPacketS2C::handle)
                .add();
        INSTANCE.messageBuilder(ClientShouldntRenderS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ClientShouldntRenderS2C::decode)
                .encoder(ClientShouldntRenderS2C::encode)
                .consumerMainThread(ClientShouldntRenderS2C::handle)
                .add();

    }


    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToAllPlayers(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void sendTrackingBlock(BlockPos pos, Level level, Object msg) {
        if (level.getServer() != null) {
            PlayerList playerList = level.getServer().getPlayerList();
            for (int i = 0; i < playerList.getPlayers().size(); ++i) {
                ServerPlayer serverplayer = playerList.getPlayers().get(i);
                if (serverplayer.level().dimension() == level.dimension()) {
                    double d0 = (double) pos.getX() - serverplayer.getX();
                    double d2 = (double) pos.getZ() - serverplayer.getZ();
                    if (d0 * d0 + d2 * d2 < 16384.0) {
                        INSTANCE.sendTo(msg, serverplayer.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
                    }
                }
            }
        }
    }
}

