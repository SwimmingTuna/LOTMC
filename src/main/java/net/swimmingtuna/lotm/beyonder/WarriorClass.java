package net.swimmingtuna.lotm.beyonder;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.swimmingtuna.lotm.nihilums.tweaks.Attributes.PathwayAttributes.WarriorAttributes;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.nihilums.tweaks.PathwaysPassiveEvents.WarriorPassiveEvents;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.List;

import static net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems.DawnWeaponry.hasFullDawnArmor;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems.DawnWeaponry.hasFullSilverArmor;
import static net.swimmingtuna.lotm.util.BeyonderUtil.applyMobEffect;

public class WarriorClass implements BeyonderClass {

    @Override
    public List<String> sequenceNames() {
        return List.of(
                "Twilight Giant",
                "Hand of God",
                "Glory",
                "Silver Knight",
                "Demon Hunter",
                "Guardian",
                "Dawn Paladin",
                "Weapon Master",
                "Pugilist",
                "Warrior"
        );
    }

    @Override
    public List<Integer> antiDivination() {
        return List.of(60, 45, 35, 30, 20, 2, 1, 1, 1, 1);
    }

    @Override
    public List<Integer> divination() {
        return List.of(20, 15, 13, 9, 5, 2, 1, 1, 1, 1);
    }

    @Override
    public List<Integer> spiritualityLevels() {
        return List.of(30000, 12000, 7000, 3500, 2300, 900, 550, 400, 225, 150);
    }

    @Override
    public List<Integer> mentalStrength() {
        return List.of(450, 320, 210, 175, 150, 110, 80, 70, 50, 33);
    }

    @Override
    public List<Integer> spiritualityRegen() {
        return List.of(52, 34, 26, 17, 12, 8, 7, 6, 4, 3);
    }

    @Override
    public void applyAllModifiers(LivingEntity entity, int seq) {
        WarriorAttributes.applyAll(entity, seq);
    }

    @Override
    public Multimap<Integer, Item> getItems() {
        HashMultimap<Integer, Item> items = HashMultimap.create();
        items.put(9, ItemInit.ALLY_MAKER.get());
        items.put(6, ItemInit.GIGANTIFICATION.get());
        items.put(6, ItemInit.LIGHTOFDAWN.get());
        items.put(6, ItemInit.DAWNARMORY.get());
        items.put(6, ItemInit.DAWNWEAPONRY.get());
        items.put(5, ItemInit.ENABLEDISABLEPROTECTION.get());
        items.put(4, ItemInit.EYEOFDEMONHUNTING.get());
        items.put(4, ItemInit.WARRIORDANGERSENSE.get());
        items.put(3, ItemInit.MERCURYLIQUEFICATION.get());
        items.put(3, ItemInit.SILVERSWORDMANIFESTATION.get());
        items.put(3, ItemInit.SILVERRAPIER.get());
        items.put(3, ItemInit.SILVERARMORY.get());
        items.put(3, ItemInit.LIGHTCONCEALMENT.get());
        items.put(2, ItemInit.BEAMOFGLORY.get());
        items.put(2, ItemInit.AURAOFGLORY.get());
        items.put(2, ItemInit.TWILIGHTSWORD.get());
        items.put(2, ItemInit.MERCURYCAGE.get());
        items.put(1, ItemInit.DIVINEHANDLEFT.get());
        items.put(1, ItemInit.DIVINEHANDRIGHT.get());
        items.put(1, ItemInit.TWILIGHTMANIFESTATION.get());
        items.remove(0, ItemInit.AURAOFGLORY.get());
        items.remove(0, ItemInit.BEAMOFGLORY.get());
        items.put(0, ItemInit.AURAOFTWILIGHT.get());
        items.put(0, ItemInit.TWILIGHTFREEZE.get());
        items.put(0, ItemInit.TWILIGHTACCELERATE.get());
        items.put(0, ItemInit.TWILIGHTLIGHT.get());
        items.put(0, ItemInit.GLOBEOFTWILIGHT.get());
        items.put(0, ItemInit.BEAMOFTWILIGHT.get());
        return items;
    }

    @Override
    public ChatFormatting getColorFormatting() {
        return ChatFormatting.YELLOW;
    }

    public static void warriorAttackEvent(LivingAttackEvent event){
        LivingEntity livingEntity = event.getEntity();
        DamageSource source = event.getSource();
        Entity entitySource = source.getEntity();
        boolean isBeyonder = entitySource instanceof LivingEntity living && BeyonderUtil.isBeyonder(living);
        if (!livingEntity.level().isClientSide()) {
            if (entitySource instanceof Projectile projectile && projectile.getOwner() != null) {
                entitySource = projectile.getOwner();
            }
            boolean isGiant = livingEntity.getPersistentData().getBoolean("warriorGiant");
            boolean isHoGGiant = livingEntity.getPersistentData().getBoolean("handOfGodGiant");
            boolean isTwilightGiant = livingEntity.getPersistentData().getBoolean("twilightGiant");
            boolean isPhysical = BeyonderUtil.isPhysicalDamage(source);
            boolean isSupernatural = BeyonderUtil.isSupernaturalDamage(source);
            float originalAmount = event.getAmount();
            int sequence = BeyonderUtil.getSequence(livingEntity);
            boolean isWarrior = BeyonderUtil.currentPathwayMatchesNoException(livingEntity, BeyonderClassInit.WARRIOR.get());
            if (hasFullSilverArmor(livingEntity) && originalAmount <= 25) {
                event.setCanceled(true);
            }
            if (isWarrior && isPhysical) {
                if (sequence == 6 && isGiant && originalAmount <= 5) {
                    event.setCanceled(true);
                } else if (sequence == 5 && isGiant && originalAmount <= 7) {
                    event.setCanceled(true);
                } else if (sequence == 4 && isGiant && originalAmount <= 10) {
                    event.setCanceled(true);
                } else if ((sequence == 3 || sequence == 2) && isGiant && originalAmount <= 15) {
                    event.setCanceled(true);
                } else if (sequence == 1 && isHoGGiant && originalAmount <= 20) {
                    event.setCanceled(true);
                } else if (sequence == 0 && isHoGGiant && originalAmount <= 25) {
                    event.setCanceled(true);
                }
            }
            if (isWarrior && isSupernatural) {
                if (sequence == 1 && isHoGGiant && originalAmount <= 20) {
                    event.setCanceled(true);
                } else if (sequence == 0 && isTwilightGiant && originalAmount <= 25) {
                    event.setCanceled(true);
                }
            }
        }
    }


    public static void newWarriorDamageNegation(LivingHurtEvent event) {
        LivingEntity livingEntity = event.getEntity();
        DamageSource source = event.getSource();
        Entity entitySource = source.getEntity();
        boolean isBeyonder = entitySource instanceof LivingEntity living && BeyonderUtil.isBeyonder(living);
        if (!livingEntity.level().isClientSide()) {
            if (entitySource instanceof Projectile projectile && projectile.getOwner() != null) {
                entitySource = projectile.getOwner();
            }
            boolean isGiant = livingEntity.getPersistentData().getBoolean("warriorGiant");
            boolean isHoGGiant = livingEntity.getPersistentData().getBoolean("handOfGodGiant");
            boolean isTwilightGiant = livingEntity.getPersistentData().getBoolean("twilightGiant");
            boolean isPhysical = BeyonderUtil.isPhysicalDamage(source);
            boolean isSupernatural = BeyonderUtil.isSupernaturalDamage(source);
            float originalAmount = event.getAmount();
            float amount = originalAmount;
            int sequence = -BeyonderUtil.getSequence(livingEntity);

            // Track damage reduction multipliers
            float physicalReduction = 0.0f;
            float supernaturalReduction = 0.0f;
            boolean isWarrior = BeyonderUtil.currentPathwayMatchesNoException(livingEntity, BeyonderClassInit.WARRIOR.get());
            //if (hasFullSilverArmor(livingEntity) && originalAmount <= 25) {
            //    event.setAmount(0);
            //    return;
            //}

            if (hasFullDawnArmor(livingEntity)) {
                float maxDamageAmount = isBeyonder ? 10 - (sequence) : 5 - ((float) sequence / 2);
                if (originalAmount <= maxDamageAmount) {
                    physicalReduction += 0.75f;
                    supernaturalReduction += 0.75f;
                }
            }
            /*
            if (isWarrior && isPhysical) {
                if (sequence == 6 && isGiant && originalAmount <= 5) {
                    event.setAmount(0);
                    return;
                } else if (sequence == 5 && isGiant && originalAmount <= 7) {
                    event.setAmount(0);
                    return;
                } else if (sequence == 4 && isGiant && originalAmount <= 10) {
                    event.setAmount(0);
                    return;
                } else if ((sequence == 3 || sequence == 2) && isGiant && originalAmount <= 15) {
                    event.setCanceled(true);
                    return;
                } else if (sequence == 1 && isHoGGiant && originalAmount <= 20) {
                    event.setAmount(0);
                    return;
                } else if (sequence == 0 && isHoGGiant && originalAmount <= 25) {
                    event.setAmount(0);
                    return;
                }
            }
            if (isWarrior && isSupernatural) {
                if (sequence == 1 && isHoGGiant && originalAmount <= 20) {
                    event.setAmount(0);
                    return;
                } else if (sequence == 0 && isTwilightGiant && originalAmount <= 25) {
                    event.setAmount(0);
                    return;
                }
            }

             */
            if (hasFullSilverArmor(livingEntity)) {
                physicalReduction += 0.5f;
                supernaturalReduction += 0.2f;
            } else if (hasFullDawnArmor(livingEntity) && isSupernatural) {

                supernaturalReduction += (0.4f) - (sequence * 0.05f);
            }
            if (isWarrior) {
                if (sequence == 8) {
                    if (isSupernatural) {
                        supernaturalReduction += 0.225f;
                    }
                } else if (sequence == 7) {
                    if (isSupernatural) {
                        supernaturalReduction += 0.225f;
                    } else if (isPhysical) {
                        physicalReduction += 0.225f;
                    }
                } else if (sequence == 6) {
                    if (isSupernatural) {
                        supernaturalReduction += 0.27f;
                    } else if (isPhysical) {
                        if (!isGiant) {
                            physicalReduction += 0.3f;
                        } else {
                            physicalReduction += 0.41f;
                        }
                    }
                } else if (sequence == 5) {
                    if (isSupernatural) {
                        supernaturalReduction += 0.27f;
                    } else if (isPhysical) {
                        if (!isGiant) {
                            physicalReduction += 0.375f;
                        } else {
                            physicalReduction += 0.49f;
                        }
                    }
                } else if (sequence == 4) {
                    if (isSupernatural) {
                        supernaturalReduction += 0.36f;
                    } else if (isPhysical) {
                        if (!isGiant) {
                            physicalReduction += 0.41f;
                        } else {
                            physicalReduction += 0.49f;
                        }
                    }
                } else if (sequence == 3 || sequence == 2) {
                    if (isSupernatural) {
                        supernaturalReduction += 0.36f;
                    } else if (isPhysical) {
                        if (!isGiant) {
                            physicalReduction += 0.45f;
                        } else {
                            physicalReduction += 0.49f;
                        }
                    }
                } else if (sequence == 1) {
                    if (isSupernatural) {
                        if (isHoGGiant) {
                            supernaturalReduction += 0.63f;
                        } else {
                            supernaturalReduction += 0.42f;
                        }
                    } else if (isPhysical) {
                        if (isHoGGiant) {
                            physicalReduction += 0.544f;
                        } else {
                            physicalReduction += 0.49f;
                        }
                    }
                } else if (sequence == 0) {
                    if (isSupernatural) {
                        if (isTwilightGiant) {
                            supernaturalReduction += 0.72f;
                        } else {
                            supernaturalReduction += 0.45f;
                        }
                    } else if (isPhysical) {
                        if (isTwilightGiant) {
                            physicalReduction += 0.6f;
                        } else {
                            physicalReduction += 0.5f;
                        }
                    }
                }
            }
            if (!isBeyonder && (!(entitySource instanceof Projectile projectile && projectile.getOwner() != null && projectile.getOwner() instanceof Player))) {
                physicalReduction *= 0.5f;
                supernaturalReduction *= 0.5f;
            }
            float maxReduction = Math.max(0.25f, Math.min(0.7f, (10 - sequence) * 0.075f));
            if (livingEntity instanceof Mob) {
                maxReduction = Math.max(0.13f, Math.min(0.3f, (10 - sequence) * 0.07f));
            }
            if (entitySource instanceof Mob) {
                maxReduction *= 0.5f;
            }
            if (isPhysical) {
                float finalReduction = Math.min(physicalReduction, maxReduction);
                event.setAmount(amount * (1.0f - finalReduction));
            } else if (isSupernatural) {
                float finalReduction = Math.min(supernaturalReduction, maxReduction);
                event.setAmount(amount * (1.0f - finalReduction));
            }
        }
    }

    public static void twilightTick(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        if (!livingEntity.level().isClientSide() && tag.getInt("inTwilight") >= 1) {
            tag.putInt("inTwilight", tag.getInt("inTwilight") - 1);
            double x = livingEntity.getX();
            double y = livingEntity.getY();
            double z = livingEntity.getZ();

            livingEntity.teleportTo(x, y, z);
            livingEntity.setDeltaMovement(0,0,0);
            livingEntity.hurtMarked = true;
        }
    }

//    private void applyRandomWeaponEffects(LivingEntity entity, int sequenceLevel) {
//        String weaponType = getOrSetRandomWeaponType(entity);
//        switch (weaponType) {
//            case "sword":
//                applySwordEffects(entity, sequenceLevel);
//                break;
//            case "axe":
//                applyAxeEffects(entity, sequenceLevel);
//                break;
//            case "pickaxe":
//                applyPickaxeEffects(entity, sequenceLevel);
//                break;
//            case "bow":
//                applyBowEffects(entity, sequenceLevel);
//                break;
//            case "shield":
//                applyShieldEffects(entity, sequenceLevel);
//                break;
//        }
//    }
//    private String getOrSetRandomWeaponType(LivingEntity entity) {
//        CompoundTag persistentData = entity.getPersistentData();
//        String weaponType = persistentData.getString("randomWeaponType");
//
//        if (weaponType.isEmpty()) {
//            String[] weaponTypes = {"sword", "axe", "pickaxe", "bow", "shield"};
//            weaponType = weaponTypes[entity.getRandom().nextInt(weaponTypes.length)];
//            persistentData.putString("randomWeaponType", weaponType);
//        }
//
//        return weaponType;
//    }
//    private void applySwordEffects(LivingEntity entity, int sequenceLevel) {
//        if (sequenceLevel == 8) {
//            applyMobEffect(entity, MobEffects.MOVEMENT_SPEED, 20, speed + 1, true, true);
//        } else if (sequenceLevel <= 7 && sequenceLevel >= 6) {
//            applyMobEffect(entity, MobEffects.MOVEMENT_SPEED, 20, speed + 1, true, true);
//        } else if (sequenceLevel <= 5) {
//            applyMobEffect(entity, MobEffects.MOVEMENT_SPEED, 20, speed + 2, true, true);
//            applyMobEffect(entity, MobEffects.DIG_SPEED, 20, 0, true, true);
//        }
//    }
//
//    private void applyAxeEffects(LivingEntity entity, int sequenceLevel) {
//        if (sequenceLevel <= 7 && sequenceLevel >= 6) {
//            applyMobEffect(entity, MobEffects.DAMAGE_BOOST, 20, strength + 1, true, true);
//        } else if (sequenceLevel <= 5) {
//            applyMobEffect(entity, MobEffects.DAMAGE_BOOST, 20, strength + 1, true, true);
//            applyMobEffect(entity, MobEffects.DAMAGE_RESISTANCE, 20, resistance + 1, true, true);
//        }
//    }
//
//    private void applyPickaxeEffects(LivingEntity entity, int sequenceLevel) {
//        if (sequenceLevel <= 7 && sequenceLevel >= 6) {
//            applyMobEffect(entity, MobEffects.MOVEMENT_SPEED, 20, speed + 1, true, true);
//        } else if (sequenceLevel <= 5) {
//            applyMobEffect(entity, MobEffects.MOVEMENT_SPEED, 20, speed + 2, true, true);
//        }
//    }
//
//    private void applyBowEffects(LivingEntity entity, int sequenceLevel) {
//        if (sequenceLevel <= 7 && sequenceLevel >= 6) {
//            applyMobEffect(entity, MobEffects.MOVEMENT_SPEED, 20, speed + 1, true, true);
//        } else if (sequenceLevel <= 5) {
//            applyMobEffect(entity, MobEffects.MOVEMENT_SPEED, 20, speed + 2, true, true);
//            applyMobEffect(entity, MobEffects.REGENERATION, 20, regen + 1, true, true);
//        }
//    }
//
//    private void applyShieldEffects(LivingEntity entity, int sequenceLevel) {
//        if (sequenceLevel == 8) {
//            applyMobEffect(entity, MobEffects.DAMAGE_RESISTANCE, 20, resistance + 1, true, true);
//        } else if (sequenceLevel <= 7 && sequenceLevel >= 6) {
//            applyMobEffect(entity, MobEffects.DAMAGE_RESISTANCE, 20, resistance + 1, true, true);
//        } else if (sequenceLevel <= 5) {
//            applyMobEffect(entity, MobEffects.DAMAGE_RESISTANCE, 20, resistance + 1, true, true);
//        }
//    }

    @Override
    public void removeAllEvents(LivingEntity entity) {
        WarriorPassiveEvents.removeAllEvents(entity);
    }

    @Override
    public void addAllEvents(LivingEntity entity, int sequence) {
        WarriorPassiveEvents.addAllEvents(entity, sequence);
    }
}
