package us.potatoboy.petowner.client;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.InputConstants;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.fox.Fox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import us.potatoboy.petowner.client.config.PetOwnerConfig;
import us.potatoboy.petowner.mixin.FoxTrustedInvoker;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PetOwnerClient implements ClientModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("PetOwner");

	public static boolean enabled = true;
	public static KeyMapping keyBinding;

	private static final LoadingCache<UUID, Optional<String>> usernameCache = CacheBuilder
			.newBuilder()
			.expireAfterWrite(6, TimeUnit.HOURS)
			.build(new CacheLoader<>() {
				@Override
				public @NotNull Optional<String> load(@NotNull UUID key) {
					CompletableFuture.runAsync(() -> {
						GameProfile playerProfile;
						try {
							playerProfile = Objects.requireNonNull(Minecraft.getInstance().services().sessionService().fetchProfile(key, false)).profile();
							usernameCache.put(key, Optional.ofNullable(playerProfile.name()));
						} catch (NullPointerException e) {
							usernameCache.put(key, Optional.empty());
						}
					});

					return Optional.of("Waiting...");
				}
			});

	@Override
	public void onInitializeClient() {
		MidnightConfig.init("petowner", PetOwnerConfig.class);

        var category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("petowner", "keys"));
		keyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.petowner.hide",
				InputConstants.UNKNOWN.getValue(),
				category
		));

		var enabledText = Component.translatable("text.petowner.message.enabled").withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN);
		var disabledText = Component.translatable("text.petowner.message.disabled").withStyle(ChatFormatting.BOLD, ChatFormatting.RED);

		ClientTickEvents.END_CLIENT_TICK.register(minecraftClient -> {
			if (keyBinding.isUnbound()) return;

			if (PetOwnerConfig.keybindMode == PetOwnerConfig.KeybindMode.HOLD) {
				enabled = keyBinding.isDown();

				if (keyBinding.isDown() || keyBinding.consumeClick()) {
					if (minecraftClient.player != null && PetOwnerConfig.showKeybindMessage) {
						minecraftClient.player.sendSystemMessage((enabled ? enabledText : disabledText));
					}
				}
			} else {
				//Toggle mode
				while (keyBinding.consumeClick()) {
					enabled = !enabled;
					if (minecraftClient.player != null && PetOwnerConfig.showKeybindMessage) {
						minecraftClient.player.sendSystemMessage((enabled ? enabledText : disabledText));
					}
				}
			}
		});
	}

	public static Optional<String> getNameFromId(UUID uuid) {
		return usernameCache.getUnchecked(uuid);
	}

	public static List<UUID> getOwnerIds(Entity entity) {
		if (entity instanceof TamableAnimal tameableEntity) {

			if (tameableEntity.isTame() && tameableEntity.getOwnerReference() != null) {
				return Collections.singletonList(tameableEntity.getOwnerReference().getUUID());
			}
		}

		if (entity instanceof AbstractHorse horseBaseEntity) {

			if (horseBaseEntity.isTamed() && horseBaseEntity.getOwnerReference() != null) {
				return Collections.singletonList(horseBaseEntity.getOwnerReference().getUUID());
			}
		}

		if (entity instanceof Fox foxEntity) {
			return ((FoxTrustedInvoker) foxEntity).invokeGetTrustedEntities().map(EntityReference::getUUID).toList();
		}

		return new ArrayList<>();
	}
}
