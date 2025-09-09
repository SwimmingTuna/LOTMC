package net.swimmingtuna.lotm.nihilums.tweaks.EventManager;

import net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses.*;
import net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses.BeyonderTicks.Apprentice.ApprenticeSPTickLayer;
import net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses.BeyonderTicks.Apprentice.ApprenticeTickLayer;
import net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses.BeyonderTicks.Apprentice.WaterWalkingLayer;
import net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses.BeyonderTicks.Monster.MonsterTickLayer;
import net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses.BeyonderTicks.Sailor.SailorTickLayer;
import net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses.BeyonderTicks.Spectator.SpectatorTickLayer;
import net.swimmingtuna.lotm.nihilums.tweaks.LayerClasses.BeyonderTicks.Warrior.WarriorTickLayer;

public enum EFunctions {
    // Original functions
    SEAL(new SealLayer()),
    LIGHTNINGREDIRECTION(new LightningRedirectionLayer()),
    RAIN_EYES(new RainEyesLayer()),
    GRAVITY_MANIPULATION(new GravityManipulationLayer()),
    PSYCHOLOGICAL_INVISIBILITY(new PsychologicalInvisibilityLayer()),
    SYMBOLIZATION(new SymbolizationLayer()),
    SPATIAL_CAGE(new SpatialCageLayer()),
    BLINK_STATE(new BlinkStateLayer()),
    EXILE(new ExileLayer()),
    DOOR_MIRAGE(new DoorMirageLayer()),
    WATER_WALKING(new WaterWalkingLayer()),
    APPRENTICE_TICK(new ApprenticeTickLayer()),
    APPRENTICE_SP_TICK(new ApprenticeSPTickLayer()),
    MONSTER_TICK(new MonsterTickLayer()),
    SAILOR_TICK(new SailorTickLayer()),
    SPECTATOR_TICK(new SpectatorTickLayer()),
    WARRIOR_TICK(new WarriorTickLayer()),
    REGENERATE_SPIRITUALITY(new RegenerateSpiritualityLayer()),
    ENVISION_KINGDOM(new EnvisionKingdomLayer()),
    VOLCANIC_ERUPTION(new VolcanicEruptionLayer()),
    TRICKMASTERTELEKENESIS(new TrickmasterTelekenisisPassiveLayer()),
    PROPHECY(new ProphecyTickLayer()),
    SPECTATORPROPHECY(new SpectatorClassProphecyTickLayer()),
    DREAM_INTO_REALITY(new DreamIntoRealityLayer()),
    ACIDICRAIN(new AcidicRainTickLayer()),
    LIGHTNING_STORM(new LightningStormLayer()),
    TSUNAMI(new TsunamiLayer()),
    RAGINGBLOWS(new RagingBlowsTickLayer()),
    WATERSPHERE(new WaterSphereCheckLayer()),
    WIND_MANIPULATION_FLIGHT(new WindManipulationFlightLayer()),
    SIRENSONG(new SirenSongsLayer()),
    STAR_OF_LIGHTNING(new StarOfLightningLayer()),
    DIVINE_HAND_COOLDOWN_DECREASE(new DivineHandCooldownDecreaseLayer()),
    EARTHQUAKE(new EarthquakeLayer()),
    HURRICANE(new HurricaneLayer()),
    AFFECTEDBYEXTREMECOLDNESS(new ExtremeColdnessLayer()),
    CALAMITYINCARNATIONTSUNAMI(new CalamityIncarnationTsunamiTickLayer()),
    MANIPULATE_MOVEMENT(new ManipulateMovementLayer()),
    CONSCIOUSNESS_STROLL(new ConsciousnessStrollLayer()),
    CALAMITY_INCARNATION_TORNADO(new CalamityIncarnationTornadoLayer()),
    RAGINGCOMBO(new RagingComboLayer()),
    WIND_MANIPULATION_SENSE(new WindManipulationSenseLayer()),
    LIGHTNINGTRAVEL(new SailorLightningTravelLayer()),
    NIGHTMARE_TICK(new NightmareTickLayer()),
    CALAMITY_UNDEAD_ARMY(new CalamityUndeadArmyLayer()),
    CALAMITY_LIGHTNING_STORM(new CalamityLightningStormLayer()),
    WARRIOR_DANGER_SENSE(new WarriorDangerSenseLayer()),
    DECREMENT_MONSTER_ATTACK_EVENT(new DecrementMonsterAttackEventLayer()),
    CHAOSWALKERCOMBAT(new OnChaosWalkerCombatLayer()),
    MONSTER_DANGER_SENSE(new MonsterDangerSenseLayer()),
    GLOBE_OF_TWILIGHT_TICK(new GlobeOfTwilightTickLayer()),
    MERCURYLIQUEFICATIONTICK(new MercuryTickLayer()),
    FATEREINCARNATION(new MonsterReincarnationCheckerLayer()),
    TWILIGHTACCELERATE(new TwilightAccelerateTickLayer()),
    TWILIGHT_FREEZE_TICK(new TwilightFreezeTickLayer()),
    MISFORTUNEIMPLOSIONLIGHTNING(new MisfortuneImplosionLightningLayer()),
    TWILIGHT_LIGHT_TICK(new TwilightLightTickLayer());

    // Additional enum values to add to your existing EFunctions enum


    private final IFunction func;

    EFunctions(IFunction obj) {
        func = obj;
    }

    public IFunction get() {
        return func;
    }
}