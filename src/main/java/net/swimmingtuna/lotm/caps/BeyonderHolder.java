package net.swimmingtuna.lotm.caps;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.simple.SimpleChannel;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.beyonder.ApprenticeClass;
import net.swimmingtuna.lotm.nihilums.tweaks.Attributes.BaseAttributes;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.ClearAbilitiesS2C;
import net.swimmingtuna.lotm.networking.packet.SyncSequencePacketS2C;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.CapabilitySyncer.core.PlayerCapability;
import net.swimmingtuna.lotm.util.CapabilitySyncer.network.EntityCapabilityStatusPacket;
import net.swimmingtuna.lotm.util.CapabilitySyncer.network.SimpleEntityCapabilityStatusPacket;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = LOTM.MOD_ID)
public class BeyonderHolder extends PlayerCapability {
    public static final int SEQUENCE_MIN = 0;
    public static final int SEQUENCE_MAX = 9;
    private static final String REGISTERED_ABILITIES_KEY = "RegisteredAbilities";
    private static final UUID HEALTH_MODIFIER_UUID = UUID.fromString("a3a90fac-39d0-4b75-9990-8211f70e0a0f");
    private final RandomSource random;
    private int currentSequence = -1;
    @Nullable private BeyonderClass currentClass = null;
    private int mentalStrength = 0;
    private int divination = 0;
    private int antiDivination = 0;
    private double spirituality = 100;
    private double maxSpirituality = 100;
    private double spiritualityRegen = 1;

    protected BeyonderHolder(Player entity) {
        super(entity);
        this.random = RandomSource.create();
    }

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent event) {
        BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(event.player);
        if (!event.player.level().isClientSide && event.phase == TickEvent.Phase.END && event.player.isAlive() && holder.getSequence() != -1) {
            holder.regenSpirituality(event.player);
//            if (holder.getCurrentClass() != null) {
//                holder.getCurrentClass().tick(event.player, holder.getSequence());
//            }
        }

    }

    public void removePathway() {
        if(this.currentClass != null)
            this.currentClass.removeAllEvents(this.player);

        this.currentClass = null;
        this.currentSequence = -1;
        this.mentalStrength = 0;
        this.spirituality = 100;
        this.maxSpirituality = 100;
        this.spiritualityRegen = 1;
        this.divination = 0;
        this.antiDivination = 0;
        @Nullable AttributeInstance healthAttribute = this.player.getAttribute(Attributes.MAX_HEALTH);

        if (healthAttribute != null) {
            if (healthAttribute.getModifier(HEALTH_MODIFIER_UUID) != null) {
                healthAttribute.removeModifier(HEALTH_MODIFIER_UUID);
            }
        }

        this.player.setHealth(this.player.getMaxHealth());
        CompoundTag persistentData = this.player.getPersistentData();

        if (persistentData.contains(REGISTERED_ABILITIES_KEY)) {
            persistentData.remove(REGISTERED_ABILITIES_KEY);
        }

        LOTMNetworkHandler.sendToPlayer(new ClearAbilitiesS2C(), (ServerPlayer) player);
        updateTracking();

        BaseAttributes.cleanAll(player);

        LOTMNetworkHandler.sendToPlayer(new SyncSequencePacketS2C(this.currentSequence), (ServerPlayer) player);
    }

    public void setPathwayAndSequence(BeyonderClass newClass, int sequence) {
        BaseAttributes.cleanAll(player);

        if(currentClass != null)
        currentClass.removeAllEvents(player);

        this.currentClass = newClass;
        this.currentSequence = sequence;
        this.maxSpirituality = this.currentClass.spiritualityLevels().get(this.currentSequence);
        this.spirituality = this.maxSpirituality;
        this.mentalStrength = this.currentClass.mentalStrength().get(this.currentSequence);
        this.divination = this.currentClass.divination().get(this.currentSequence);
        this.antiDivination = this.currentClass.antiDivination().get(this.currentSequence);
        this.spiritualityRegen = this.currentClass.spiritualityRegen().get(this.currentSequence);
        this.player.setHealth(this.player.getMaxHealth());

        if (newClass == BeyonderClassInit.APPRENTICE.get() && sequence <= 4) {
            this.player.getPersistentData().putInt("wormOfStar", BeyonderUtil.maxWormAmount(this.player));
        } else {
            this.player.getPersistentData().putInt("wormOfStar", 0);
        }

        updateTracking();

        newClass.addAllEvents(player, sequence);
        newClass.applyAllModifiers(player, sequence);

        LOTMNetworkHandler.sendToPlayer(new SyncSequencePacketS2C(this.currentSequence), (ServerPlayer) player);
    }

    public double getMaxSpirituality() {
        return maxSpirituality;
    }

    public void setMaxSpirituality(int maxSpirituality) {
        this.maxSpirituality = maxSpirituality;
        updateTracking();
    }

    public void setMentalStrength(int mentalStrength) {
        this.mentalStrength = mentalStrength;
        updateTracking();
    }

    public int getMentalStrength() {
        return this.mentalStrength;
    }


    public void setDivination(int divination) {
        this.divination = divination;
        updateTracking();
    }

    public int getDivination() {
        return this.divination;
    }

    public void setAntiDivination(int antiDivination) {
        this.antiDivination = antiDivination;
        updateTracking();
    }

    public int getAntiDivination() {
        return this.antiDivination;
    }

    public double getSpiritualityRegen() {
        return this.spiritualityRegen;
    }

    public void setSpiritualityRegen(int spiritualityRegen) {
        this.spiritualityRegen = spiritualityRegen;
        updateTracking();
    }

    public double getSpirituality() {
        return this.spirituality;
    }

    public void setSpirituality(double spirituality) {
        this.spirituality = Mth.clamp(spirituality, 0, this.maxSpirituality);
        updateTracking();
    }

    public @Nullable BeyonderClass getCurrentClass() {
        return this.currentClass;
    }

    public void setPathway(BeyonderClass newClass) {
        this.currentClass = newClass;
        updateTracking();
    }

    public void removeCurrentClass() {
        this.currentClass = null;
        updateTracking();
    }

    public int getSequence() {
        return this.currentSequence;
    }

    public void setSequence(int currentSequence) {
        if (this.currentClass != null) {
            this.currentSequence = currentSequence;
            this.maxSpirituality = this.currentClass.spiritualityLevels().get(currentSequence);
            this.spiritualityRegen = this.currentClass.spiritualityRegen().get(currentSequence);
            this.spirituality = this.maxSpirituality;
            updateTracking();

            currentClass.applyAllModifiers(player, currentSequence);
            currentClass.addAllEvents(player, currentSequence);

            LOTMNetworkHandler.sendToPlayer(new SyncSequencePacketS2C(this.currentSequence), (ServerPlayer) player);
        }
    }

    public void incrementSequence() {
        if (this.currentSequence > SEQUENCE_MIN) {
            this.currentSequence--;
            this.maxSpirituality = this.currentClass.spiritualityLevels().get(this.currentSequence);
            this.spirituality = this.maxSpirituality;
            this.spiritualityRegen = this.currentClass.spiritualityRegen().get(this.currentSequence);
            updateTracking();
        }
    }

    public void decrementSequence() {
        if (this.currentSequence < SEQUENCE_MAX) {
            this.currentSequence++;
            this.maxSpirituality = this.currentClass.spiritualityLevels().get(this.currentSequence);
            this.spirituality = this.maxSpirituality;
            this.spiritualityRegen = this.currentClass.spiritualityRegen().get(this.currentSequence);
            updateTracking();
        }
    }

    public boolean useSpirituality(int amount) {
        if (this.player.isCreative()) {
            return true;
        }
        if (this.spirituality - amount < 0) {
            return false;
        }
        this.spirituality = Mth.clamp(this.spirituality - amount, 0, this.maxSpirituality);
        updateTracking();
        return true;
    }

    public void increaseSpirituality(int amount) {
        this.spirituality = Mth.clamp(this.spirituality + amount, 0, this.maxSpirituality);
        updateTracking();
    }

    @Override
    public CompoundTag serializeNBT(boolean savingToDisk) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("currentSequence", this.currentSequence);
        tag.putInt("mentalStrength", this.mentalStrength);
        tag.putInt("divination", this.divination);
        tag.putInt("antiDivination", this.antiDivination);
        tag.putString("currentClass", this.currentClass == null ? "" : BeyonderClassInit.getRegistry().getKey(this.currentClass).toString());
        tag.putDouble("spirituality", this.spirituality);
        tag.putDouble("maxSpirituality", this.maxSpirituality);
        tag.putDouble("spiritualityRegen", this.spiritualityRegen);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt, boolean readingFromDisk) {
        this.currentSequence = nbt.getInt("currentSequence");
        this.mentalStrength = nbt.getInt("mentalStrength");
        this.divination = nbt.getInt("divination");
        this.antiDivination = nbt.getInt("antiDivination");
        String className = nbt.getString("currentClass");
        if (!className.isEmpty()) {
            this.currentClass = BeyonderClassInit.getRegistry().getValue(new ResourceLocation(className));
        }
        this.spirituality = nbt.getDouble("spirituality");
        this.maxSpirituality = nbt.getDouble("maxSpirituality");
        this.spiritualityRegen = nbt.getDouble("spiritualityRegen");
    }

    @Override
    public EntityCapabilityStatusPacket createUpdatePacket() {
        return new SimpleEntityCapabilityStatusPacket(this.entity.getId(), BeyonderHolderAttacher.RESOURCE_LOCATION, this);
    }

    @Override
    public SimpleChannel getNetworkChannel() {
        return LOTMNetworkHandler.INSTANCE;
    }

    public void regenSpirituality(Entity entity) {
        if (entity instanceof Player && this.spirituality < this.maxSpirituality) {
            double increase = (Mth.nextDouble(this.random, 0.1, 1.0) * (this.spiritualityRegen * 1.5f)) / 5;
            this.spirituality = Mth.clamp(this.spirituality + increase, 0, this.maxSpirituality);
            updateTracking();
        }
    }

    public boolean currentClassMatches(Supplier<? extends BeyonderClass> beyonderClassSupplier) {
        return currentClassMatches(beyonderClassSupplier.get());
    }

    public boolean currentClassMatches(BeyonderClass beyonderClass) {
        return this.currentClass == beyonderClass;
    }

    public static void resetMaxHealthModifier(@Nullable LivingEntity player) {
        if (player == null) return;
        @Nullable AttributeInstance healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute == null) return;
        if (healthAttribute.getModifier(HEALTH_MODIFIER_UUID) != null) {
            healthAttribute.removeModifier(HEALTH_MODIFIER_UUID);
        }
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

}
