package us.potatoboy.petowner.client;

import java.util.List;
import java.util.UUID;

public interface PetRenderState {
    List<UUID> petOwner$getOwnerIds();
    boolean petOwner$getHasPassenger();
    boolean petOwner$getIsTargeted();

    void petOwner$setOwnerIds(List<UUID> ownerIds);
    void petOwner$setHasPassenger(boolean hasPassenger);
    void petOwner$setIsTargeted(boolean isTargeted);
}
