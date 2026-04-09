package us.potatoboy.petowner.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import us.potatoboy.petowner.client.PetOwnerClient;
import us.potatoboy.petowner.client.PetRenderState;
import us.potatoboy.petowner.client.config.PetOwnerConfig;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mixin(EntityRenderer.class)
public abstract class OwnerNameTagRendering<T extends Entity, S extends EntityRenderState> {

    @Inject(method = "render", at = @At("HEAD"))
	private void render(S state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState, CallbackInfo ci) {
		PetRenderState petRenderState = (PetRenderState) state;

		//If HUD is hidden
		if (MinecraftClient.getInstance().options.hudHidden) return;
		//If the player is riding the entity
		if (petRenderState.petOwner$getHasPassenger()) return;
		//If the key is bound and owner is disabled
		if (!PetOwnerClient.keyBinding.isUnbound() && !PetOwnerClient.enabled) return;
		//If the entity is not targeted
		if (!petRenderState.petOwner$getIsTargeted() && !PetOwnerConfig.alwaysShow) return;

		List<UUID> ownerIds = petRenderState.petOwner$getOwnerIds();
		if (ownerIds == null || ownerIds.isEmpty()) return;

		for (int i = 0; i < ownerIds.size(); i++) {
			UUID ownerId = ownerIds.get(i);
			if (ownerId == null) return;

			Optional<String> usernameString = PetOwnerClient.getNameFromId(ownerId);

			Text text = Text.translatable("text.petowner.message.owner", usernameString.isPresent() ?
					Text.literal(usernameString.get()).formatted(Formatting.WHITE) : Text.translatable("text.petowner.message.error").formatted(Formatting.RED)).formatted(Formatting.DARK_AQUA);
			if (FabricLoader.getInstance().isDevelopmentEnvironment() && usernameString.isEmpty()) {
					PetOwnerClient.LOGGER.error("If you're trying to figure out why the mod doesn't work, it's cause you're in a dev env");
			}

            queue.submitLabel(matrices, state.nameLabelPos, 10 + (10*i), text, !state.sneaking, state.light, state.squaredDistanceToCamera, cameraState);
		}
	}

	@Inject(method = "updateRenderState", at = @At("HEAD"))
	private void updateRenderState(T entity, S state, float tickDelta, CallbackInfo ci) {
		if (state instanceof PetRenderState petRenderState) {
			petRenderState.petOwner$setOwnerIds(PetOwnerClient.getOwnerIds(entity));
			petRenderState.petOwner$setHasPassenger(entity.hasPassenger(MinecraftClient.getInstance().player));
			petRenderState.petOwner$setIsTargeted(entity == MinecraftClient.getInstance().targetedEntity);

			if (!PetOwnerClient.getOwnerIds(entity).isEmpty()) {
				state.nameLabelPos = entity.getAttachments().getPointNullable(EntityAttachmentType.NAME_TAG, 0, entity.getLerpedYaw(tickDelta));
			}
		}
	}

}
