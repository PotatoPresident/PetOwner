package us.potatoboy.petowner.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
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

    @Inject(method = "submit", at = @At("HEAD"))
	private void render(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
		PetRenderState petRenderState = (PetRenderState) state;

		//If HUD is hidden
		if (Minecraft.getInstance().options.hideGui) return;
		//If the player is riding the entity
		if (petRenderState.petOwner$getHasPassenger()) return;
		//If the key is bound and owner is disabled
		if (!PetOwnerClient.keyBinding.isUnbound() && !PetOwnerClient.enabled) return;
		//If the entity is not targeted
		if (!petRenderState.petOwner$getIsTargeted() && !PetOwnerConfig.alwaysShow) return;

		List<UUID> ownerIds = petRenderState.petOwner$getOwnerIds();
		if (ownerIds.isEmpty()) return;

		for (int i = 0; i < ownerIds.size(); i++) {
			UUID ownerId = ownerIds.get(i);
			if (ownerId == null) return;

			Optional<String> usernameString = PetOwnerClient.getNameFromId(ownerId);

			Component text = Component.translatable("text.petowner.message.owner", usernameString.isPresent() ?
					Component.literal(usernameString.get()).withStyle(ChatFormatting.WHITE) : Component.translatable("text.petowner.message.error").withStyle(ChatFormatting.RED)).withStyle(ChatFormatting.DARK_AQUA);
			if (FabricLoader.getInstance().isDevelopmentEnvironment() && usernameString.isEmpty()) {
					PetOwnerClient.LOGGER.error("If you're trying to figure out why the mod doesn't work, it's cause you're in a dev env");
			}

            submitNodeCollector.submitNameTag(poseStack, state.nameTagAttachment, 10 + (10*i), text, !state.isDiscrete, state.lightCoords, state.distanceToCameraSq, camera);
		}
	}

	@Inject(method = "extractRenderState", at = @At("HEAD"))
	private void updateRenderState(T entity, S state, float tickDelta, CallbackInfo ci) {
		if (state instanceof PetRenderState petRenderState) {
			petRenderState.petOwner$setOwnerIds(PetOwnerClient.getOwnerIds(entity));
			petRenderState.petOwner$setHasPassenger(entity.hasPassenger(Minecraft.getInstance().player));
			petRenderState.petOwner$setIsTargeted(entity == Minecraft.getInstance().crosshairPickEntity);

			if (!PetOwnerClient.getOwnerIds(entity).isEmpty()) {
				state.nameTagAttachment = entity.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, entity.getYRot(tickDelta));
			}
		}
	}

}
