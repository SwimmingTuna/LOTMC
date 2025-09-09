package net.swimmingtuna.lotm.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.capabilities.concealed_data.ConcealedDataCapability;
import net.swimmingtuna.lotm.capabilities.concealed_data.ConcealedDataProvider;
import net.swimmingtuna.lotm.capabilities.concealed_data.IConcealedDataCapability;
import net.swimmingtuna.lotm.capabilities.doll_data.DollDataCapability;
import net.swimmingtuna.lotm.capabilities.doll_data.DollDataProvider;
import net.swimmingtuna.lotm.capabilities.doll_data.IDollDataCapability;
import net.swimmingtuna.lotm.capabilities.is_concealed_data.IIsConcealedCapability;
import net.swimmingtuna.lotm.capabilities.is_concealed_data.IsConcealedCapability;
import net.swimmingtuna.lotm.capabilities.is_concealed_data.IsConcealedProvider;
import net.swimmingtuna.lotm.capabilities.replicated_entity.IReplicatedEntityCapability;
import net.swimmingtuna.lotm.capabilities.replicated_entity.ReplicatedEntityCapability;
import net.swimmingtuna.lotm.capabilities.replicated_entity.ReplicatedEntityProvider;
import net.swimmingtuna.lotm.capabilities.scribed_abilities.IScribedAbilitiesCapability;
import net.swimmingtuna.lotm.capabilities.scribed_abilities.ScribedAbilitiesCapability;
import net.swimmingtuna.lotm.capabilities.scribed_abilities.ScribedAbilitiesProvider;
import net.swimmingtuna.lotm.capabilities.sealed_data.ISealedDataCapability;
import net.swimmingtuna.lotm.capabilities.sealed_data.SealedDataCapability;
import net.swimmingtuna.lotm.capabilities.sealed_data.SealedDataProvider;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventsProvider;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.IEventsCapabilityData;

@Mod.EventBusSubscriber(modid = LOTM.MOD_ID)
public class CapabilityInit {
    private static final String MOD_ID = LOTM.MOD_ID;

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IConcealedDataCapability.class);
        event.register(IIsConcealedCapability.class);
        event.register(IsConcealedCapability.class);
        event.register(IScribedAbilitiesCapability.class);
        event.register(IDollDataCapability.class);
        event.register(IReplicatedEntityCapability.class);
        event.register(ISealedDataCapability.class);
        event.register(IEventsCapabilityData.class);
    }

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        // All entities
        if (event.getObject() instanceof LivingEntity entity) {
            if (!entity.getCapability(ConcealedDataProvider.CONCEALED_DATA).isPresent()) {
                event.addCapability(
                        new ResourceLocation(MOD_ID, "concealed_data"),
                        new ConcealedDataProvider()
                );
            }
            if (!entity.getCapability(IsConcealedProvider.IS_CONCEALED).isPresent()) {
                event.addCapability(
                        new ResourceLocation(MOD_ID, "is_concealed"),
                        new IsConcealedProvider()
                );
            }
            if (!entity.getCapability(SealedDataProvider.SEALED_DATA).isPresent()) {
                event.addCapability(
                        new ResourceLocation(MOD_ID, "sealed_data"),
                        new SealedDataProvider()
                );
            }
            if (!entity.getCapability(ScribedAbilitiesProvider.SCRIBED_ABILITIES).isPresent()) {
                event.addCapability(
                        new ResourceLocation(MOD_ID, "scribed_abilities"),
                        new ScribedAbilitiesProvider()
                );
            }
            if (!entity.getCapability(EventsProvider.EVENTS_DATA).isPresent()) {
                event.addCapability(
                        new ResourceLocation(MOD_ID, "events_data"),
                        new EventsProvider()
                );
            }
        }

        // Player only
        if (event.getObject() instanceof Player player) {
            if (!player.getCapability(DollDataProvider.DOLL_DATA).isPresent()) {
                event.addCapability(
                        new ResourceLocation(MOD_ID, "doll_data"),
                        new DollDataProvider()
                );
            }
            if (!player.getCapability(ReplicatedEntityProvider.REPLICATED_ENTITY).isPresent()) {
                event.addCapability(
                        new ResourceLocation(MOD_ID, "replicated_entity"),
                        new ReplicatedEntityProvider()
                );
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player clone = event.getEntity();

        original.getCapability(ConcealedDataProvider.CONCEALED_DATA).ifPresent(oldData -> {
            clone.getCapability(ConcealedDataProvider.CONCEALED_DATA).ifPresent(newData -> {
                ((ConcealedDataCapability) newData).copyFrom((ConcealedDataCapability) oldData);
            });
        });
        original.getCapability(IsConcealedProvider.IS_CONCEALED).ifPresent(oldData -> {
            clone.getCapability(IsConcealedProvider.IS_CONCEALED).ifPresent(newData -> {
                ((IsConcealedCapability) newData).copyFrom((IsConcealedCapability) oldData);
            });
        });
        original.getCapability(SealedDataProvider.SEALED_DATA).ifPresent(oldData -> {
            clone.getCapability(SealedDataProvider.SEALED_DATA).ifPresent(newData -> {
                ((SealedDataCapability) newData).copyFrom((SealedDataCapability) oldData);
            });
        });
        original.getCapability(ScribedAbilitiesProvider.SCRIBED_ABILITIES).ifPresent(oldData -> {
            clone.getCapability(ScribedAbilitiesProvider.SCRIBED_ABILITIES).ifPresent(newData -> {
                ((ScribedAbilitiesCapability) newData).copyFrom((ScribedAbilitiesCapability) oldData);
            });
        });
        original.getCapability(DollDataProvider.DOLL_DATA).ifPresent(oldData -> {
            clone.getCapability(DollDataProvider.DOLL_DATA).ifPresent(newData -> {
                ((DollDataCapability) newData).copyFrom((DollDataCapability) oldData);
            });
        });
        original.getCapability(ReplicatedEntityProvider.REPLICATED_ENTITY).ifPresent(oldData -> {
            clone.getCapability(ReplicatedEntityProvider.REPLICATED_ENTITY).ifPresent(newData -> {
                ((ReplicatedEntityCapability) newData).copyFrom((ReplicatedEntityCapability) oldData);
            });
        });
    }
}