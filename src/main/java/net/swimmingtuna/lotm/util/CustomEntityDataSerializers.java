package net.swimmingtuna.lotm.util;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.swimmingtuna.lotm.entity.ApprenticeDoorEntity;

public class CustomEntityDataSerializers {

    public static final EntityDataSerializer<ApprenticeDoorEntity.DoorMode> DOOR_MODE = new EntityDataSerializer<>() {
        @Override
        public void write(FriendlyByteBuf buf, ApprenticeDoorEntity.DoorMode doorMode) {
            buf.writeEnum(doorMode);
        }

        @Override
        public ApprenticeDoorEntity.DoorMode read(FriendlyByteBuf buf) {
            return buf.readEnum(ApprenticeDoorEntity.DoorMode.class);
        }

        @Override
        public ApprenticeDoorEntity.DoorMode copy(ApprenticeDoorEntity.DoorMode doorMode) {
            return doorMode;
        }
    };

    public static final EntityDataSerializer<ApprenticeDoorEntity.DoorAnimationKind> DOOR_ANIMATION_KIND = new EntityDataSerializer<>() {
        @Override
        public void write(FriendlyByteBuf buf, ApprenticeDoorEntity.DoorAnimationKind doorAnimationKind) {
            buf.writeEnum(doorAnimationKind);
        }

        @Override
        public ApprenticeDoorEntity.DoorAnimationKind read(FriendlyByteBuf buf) {
            return buf.readEnum(ApprenticeDoorEntity.DoorAnimationKind.class);
        }

        @Override
        public ApprenticeDoorEntity.DoorAnimationKind copy(ApprenticeDoorEntity.DoorAnimationKind doorAnimationKind) {
            return doorAnimationKind;
        }
    };

    public static void register() {
        EntityDataSerializers.registerSerializer(DOOR_MODE);
        EntityDataSerializers.registerSerializer(DOOR_ANIMATION_KIND);
    }

}