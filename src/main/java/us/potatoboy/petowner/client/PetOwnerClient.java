package us.potatoboy.petowner.client;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import us.potatoboy.petowner.client.config.PetOwnerConfig;
import us.potatoboy.petowner.mixin.FoxTrustedAccessor;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PetOwnerClient implements ClientModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("PetOwner");

	public static boolean enabled = true;
	public static KeyBinding keyBinding;

	private static final LoadingCache<UUID, Optional<String>> usernameCache = CacheBuilder
			.newBuilder()
			.expireAfterWrite(6, TimeUnit.HOURS)
			.build(new CacheLoader<>() {
				@Override
				public @NotNull Optional<String> load(@NotNull UUID key) {
					CompletableFuture.runAsync(() -> {
						GameProfile playerProfile;
						try {
							playerProfile = Objects.requireNonNull(MinecraftClient.getInstance().getSessionService().fetchProfile(key, false)).profile();
							usernameCache.put(key, Optional.ofNullable(playerProfile.getName()));
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

		keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.petowner.hide",
				InputUtil.UNKNOWN_KEY.getCode(),
				"category.petowner.title"
		));

		ClientTickEvents.END_CLIENT_TICK.register(minecraftClient -> {
			if (keyBinding.isUnbound()) return;

			if (PetOwnerConfig.keybindMode == PetOwnerConfig.KeybindMode.HOLD) {
				enabled = keyBinding.isPressed();

				if (keyBinding.isPressed() || keyBinding.wasPressed()) {
					if (minecraftClient.player != null && PetOwnerConfig.showKeybindMessage) {
						minecraftClient.player.sendMessage(Text.translatable(enabled ? "text.petowner.message.enabled" : "text.petowner.message.disabled"), true);
					}
				}
			} else {
				//Toggle mode
				while (keyBinding.wasPressed()) {
					enabled = !enabled;
					if (minecraftClient.player != null && PetOwnerConfig.showKeybindMessage) {
						minecraftClient.player.sendMessage(Text.translatable(enabled ? "text.petowner.message.enabled" : "text.petowner.message.disabled"), true);
					}
				}
			}
		});
	}

	public static Optional<String> getNameFromId(UUID uuid) {
		return usernameCache.getUnchecked(uuid);
	}

	public static List<UUID> getOwnerIds(Entity entity) {
		if (entity instanceof TameableEntity tameableEntity) {

			if (tameableEntity.isTamed()) {
				return Collections.singletonList(tameableEntity.getOwnerUuid());
			}
		}

		if (entity instanceof AbstractHorseEntity horseBaseEntity) {

			if (horseBaseEntity.isTame()) {
				return Collections.singletonList(horseBaseEntity.getOwnerUuid());
			}
		}

		if (entity instanceof FoxEntity foxEntity) {
			return ((FoxTrustedAccessor) foxEntity).getTrustedIds();
		}

		return new ArrayList<>();
	}
}
