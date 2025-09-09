package net.swimmingtuna.lotm.capabilities.is_concealed_data;

import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;
import java.util.UUID;

public class IsConcealedUtils {
    public static Optional<IIsConcealedCapability> getConcealedData(LivingEntity entity) {
        return entity.getCapability(IsConcealedProvider.IS_CONCEALED).resolve();
    }

    public static boolean getIsConcealed(LivingEntity entity){
        return getConcealedData(entity).map(IIsConcealedCapability::isConcealed).orElse(false);
    }

    public static void setIsConcealed(LivingEntity entity, Boolean isConcealed){
        entity.getCapability(IsConcealedProvider.IS_CONCEALED).ifPresent(data -> {
            data.setConcealed(isConcealed);
        });
    }

    public static UUID getConcealmentOwner(LivingEntity entity){
        return getConcealedData(entity).map(IIsConcealedCapability::concealmentOwner).orElse(null);
    }

    public static void setConcealmentOwner(LivingEntity entity, UUID concealmentOwner){
        entity.getCapability(IsConcealedProvider.IS_CONCEALED).ifPresent(data -> {
            data.setConcealmentOwner(concealmentOwner);
        });
    }

    public static int getConcealmentSequence(LivingEntity entity){
        return getConcealedData(entity).map(IIsConcealedCapability::concealmentSequence).orElse(9);
    }

    public static void setConcealmentSequence(LivingEntity entity, int sequence){
        entity.getCapability(IsConcealedProvider.IS_CONCEALED).ifPresent(data -> {
            data.setConcealmentSequence(sequence);
        });
    }
}
