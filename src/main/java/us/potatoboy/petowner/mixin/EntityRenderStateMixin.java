package us.potatoboy.petowner.mixin;

import net.minecraft.client.render.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import us.potatoboy.petowner.client.PetRenderState;

import java.util.List;
import java.util.UUID;

@Mixin(EntityRenderState.class)
public abstract class EntityRenderStateMixin implements PetRenderState {
    @Unique
    public List<UUID> ownerIds;
    @Unique
    public boolean hasPassenger;
    @Unique
    public boolean isTargeted;

    @Override
    public List<UUID> petOwner$getOwnerIds() {
        return ownerIds;
    }

    @Override
    public boolean petOwner$getHasPassenger() {
        return hasPassenger;
    }

    @Override
    public boolean petOwner$getIsTargeted() {
        return isTargeted;
    }

    @Override
    public void petOwner$setOwnerIds(List<UUID> ownerIds) {
        this.ownerIds = ownerIds;
    }

    @Override
    public void petOwner$setHasPassenger(boolean hasPassenger) {
        this.hasPassenger = hasPassenger;
    }

    @Override
    public void petOwner$setIsTargeted(boolean isTargeted) {
        this.isTargeted = isTargeted;
    }
}
