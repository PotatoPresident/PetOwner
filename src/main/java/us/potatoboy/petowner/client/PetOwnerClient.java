package us.potatoboy.petowner.client;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
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
import us.potatoboy.petowner.client.config.PetOwnerConfig;
import us.potatoboy.petowner.mixin.FoxTrustedAccessor;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PetOwnerClient implements ClientModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("PetOwner");

	public static boolean enabled = true;
	public static KeyBinding keyBinding;
	public static PetOwnerConfig config;

	private static final LoadingCache<UUID, Optional<String>> usernameCache = CacheBuilder
			.newBuilder()
			.expireAfterWrite(6, TimeUnit.HOURS)
			.build(new CacheLoader<>() {
				@Override
				public Optional<String> load(UUID key) {
					CompletableFuture.runAsync(() -> {
						GameProfile playerProfile = new GameProfile(key, null);
						playerProfile = MinecraftClient.getInstance().getSessionService().fillProfileProperties(playerProfile, false);
						usernameCache.put(key, Optional.ofNullable(playerProfile.getName()));
					});

					return Optional.of("Waiting...");
				}
			});

	@Override
	public void onInitializeClient() {
		config = PetOwnerConfig.loadConfig(new File(FabricLoader.getInstance().getConfigDir().toFile(), "petowner.json"));

		keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.petowner.hide",
				InputUtil.UNKNOWN_KEY.getCode(),
				"category.petowner.title"
		));

		ClientTickEvents.END_CLIENT_TICK.register(minecraftClient -> {
			if (keyBinding.isUnbound()) return;

			if (config.keybindMode == KeybindMode.HOLD) {
				enabled = keyBinding.isPressed();

				if (keyBinding.isPressed() || keyBinding.wasPressed()) {
					if (minecraftClient.player != null && config.showKeybindMessage) {
						minecraftClient.player.sendMessage(Text.translatable(enabled ? "text.petowner.message.enabled" : "text.petowner.message.disabled"), true);
					}
				}
			} else {
				//Toggle mode
				while (keyBinding.wasPressed()) {
					enabled = !enabled;
					if (minecraftClient.player != null && config.showKeybindMessage) {
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

	public static void saveConfig () {
		config.saveConfig(new File(FabricLoader.getInstance().getConfigDir().toFile(), "petowner.json"));
	}

	public enum KeybindMode {
		TOGGLE,
		HOLD
	}
}
