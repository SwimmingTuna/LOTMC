package net.swimmingtuna.lotm.init;


import net.minecraft.world.level.GameRules;

public class GameRuleInit {

    public static final GameRules.Key<GameRules.BooleanValue> NPC_SHOULD_SPAWN = GameRules.register("shouldSpawnNpc", GameRules.Category.MOBS, GameRules.BooleanValue.create(false));
    public static final GameRules.Key<GameRules.BooleanValue> SHOULD_DROP_CHARACTERISTIC = GameRules.register("shouldDropCharacteristic", GameRules.Category.MOBS, GameRules.BooleanValue.create(false));
    public static final GameRules.Key<GameRules.BooleanValue> RESET_SEQUENCE = GameRules.register("shouldResetSequence", GameRules.Category.MOBS, GameRules.BooleanValue.create(false));
    public static final GameRules.Key<GameRules.BooleanValue> PATHWAY_SAFETY_NET = GameRules.register("shouldHaveSequenceSafetyNet", GameRules.Category.MOBS, GameRules.BooleanValue.create(false));
    public static final GameRules.Key<GameRules.BooleanValue> MOBS_SHOULD_ACTIVATE_CALAMITIES = GameRules.register("shouldMobsActivateCalamities", GameRules.Category.MOBS, GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.BooleanValue> MOBS_SHOULD_ONLY_USE_ABILITIES_ON_PLAYERS = GameRules.register("shouldMobsOnlyUseAbilitiesOnPlayers", GameRules.Category.MOBS, GameRules.BooleanValue.create(false));
    public static final GameRules.Key<GameRules.BooleanValue> SHOULD_BEYONDER_ABILITY_NEAR_SPAWN = GameRules.register("shouldBeyonderAbilityNearSpawn", GameRules.Category.MOBS, GameRules.BooleanValue.create(true));



}

