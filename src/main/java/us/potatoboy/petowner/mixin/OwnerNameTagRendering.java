package us.potatoboy.petowner.mixin;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import us.potatoboy.petowner.client.PetOwnerClient;
import us.potatoboy.petowner.client.PetOwnerConfig;

import java.util.List;
import java.util.UUID;

@Mixin(EntityRenderer.class)
public abstract class OwnerNameTagRendering {
    @Final
    @Shadow
    protected EntityRenderDispatcher dispatcher;

    @Inject(method = "render", at = @At("HEAD"))
    private void render(Entity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (MinecraftClient.getInstance().options.hudHidden) return;

        PetOwnerConfig config = AutoConfig.getConfigHolder(PetOwnerConfig.class).getConfig();
        if (!config.nametag) return;

        if (dispatcher.targetedEntity != entity) return;

        List<UUID> ownerIds = PetOwnerClient.getOwnerIds(entity);
        if (ownerIds.isEmpty()) return;

        int index = 0;
        for (UUID ownerId : ownerIds) {
            if (ownerId == null) return;

            Text text;

            String usernameString = PetOwnerClient.getNameFromId(ownerId);

            if (usernameString == null) {
                text = new TranslatableText("text.petowner.message.owner", new TranslatableText("text.petowner.message.error"));
            } else {
                text = new TranslatableText("text.petowner.message.owner", usernameString);
            }

            double d = this.dispatcher.getSquaredDistanceToCamera(entity);
            @SuppressWarnings("rawtypes") EntityRenderer entityRenderer = (EntityRenderer)(Object)this;
            if (d <= 4096.0D) {
                float f = entity.getHeight() + 0.5F;
                int i = 10 + (10 * index);
                matrices.push();
                matrices.translate(0.0D, f, 0.0D);
                matrices.multiply(this.dispatcher.getRotation());
                matrices.scale(-0.025F, -0.025F, 0.025F);
                Matrix4f matrix4f = matrices.peek().getModel();
                TextRenderer textRenderer = entityRenderer.getFontRenderer();
                float h = (float)(-textRenderer.getWidth(text) / 2);

                textRenderer.draw(text, h, (float)i, -1, false, matrix4f, vertexConsumers, false, 0, light);

                matrices.pop();
                index++;
            }
        }
    }
}
