package us.potatoboy.petowner.mixin;

import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import us.potatoboy.petowner.client.PetOwnerClient;
import us.potatoboy.petowner.client.config.PetOwnerConfig;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mixin(EntityRenderer.class)
public abstract class OwnerNameTagRendering {
	@Final
	@Shadow
	protected EntityRenderDispatcher dispatcher;

	@Shadow public abstract TextRenderer getTextRenderer();

	@Inject(method = "render", at = @At("HEAD"))
	private void render(Entity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		//If HUD is hidden
		if (MinecraftClient.getInstance().options.hudHidden) return;
		//If the player is riding the entity
		if (entity.hasPassenger(MinecraftClient.getInstance().player)) return;
		//If the key is bound and owner is disabled
		if (!PetOwnerClient.keyBinding.isUnbound() && !PetOwnerClient.enabled) return;
		//If the entity is not targeted
		if (dispatcher.targetedEntity != entity && !PetOwnerConfig.alwaysShow) return;

		List<UUID> ownerIds = PetOwnerClient.getOwnerIds(entity);
		if (ownerIds.isEmpty()) return;

		for (int i = 0; i < ownerIds.size(); i++) {
			UUID ownerId = ownerIds.get(i);
			if (ownerId == null) return;

			Optional<String> usernameString = PetOwnerClient.getNameFromId(ownerId);

			Text text = Text.translatable("text.petowner.message.owner", usernameString.isPresent() ?
					usernameString.get() : Text.translatable("text.petowner.message.error"));
			if (FabricLoader.getInstance().isDevelopmentEnvironment() && usernameString.isEmpty()) {
					LogUtils.getLogger().error("If you're trying to figure out why the mod doesn't work, it's cause you're in a dev env");
			}

			double d = this.dispatcher.getSquaredDistanceToCamera(entity);
			if (d <= 4096.0D) {
				float height = entity.getHeight() + 0.5F;
				int y = 10 + (10 * i);
				matrices.push();
				matrices.translate(0.0D, height, 0.0D);
				matrices.multiply(this.dispatcher.getRotation());
				matrices.scale(-0.025F, -0.025F, 0.025F);
				Matrix4f matrix4f = matrices.peek().getPositionMatrix();
				TextRenderer textRenderer = this.getTextRenderer();
				float x = (float) (-textRenderer.getWidth(text) / 2);

				float backgroundOpacity = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F);
				int backgroundColor = (int) (backgroundOpacity * 255.0F) << 24;

				textRenderer.draw(text, x, (float) y, 553648127, false, matrix4f, vertexConsumers, true, backgroundColor, light);
				textRenderer.draw(text, x, (float) y, -1, false, matrix4f, vertexConsumers, false, 0, light);

				matrices.pop();
			}
		}
	}
}
