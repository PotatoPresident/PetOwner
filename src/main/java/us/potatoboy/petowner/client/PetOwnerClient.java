package us.potatoboy.petowner.client;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import us.potatoboy.petowner.mixin.FoxTrustedAccessor;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Environment(EnvType.CLIENT)
public class PetOwnerClient implements ClientModInitializer {
    private static final LoadingCache<UUID, Optional<String>> usernameCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(6, TimeUnit.HOURS)
            .build(new CacheLoader<UUID, Optional<String>>() {
                @Override
                public Optional<String> load(UUID key) throws Exception {
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
        AutoConfig.register(PetOwnerConfig.class, JanksonConfigSerializer::new);
        PetOwnerConfig config = AutoConfig.getConfigHolder(PetOwnerConfig.class).getConfig();

        UseEntityCallback.EVENT.register(((playerEntity, world, hand, entity, entityHitResult) -> {
            if (!config.click) return ActionResult.PASS;

            if (!hand.equals(Hand.MAIN_HAND)) return ActionResult.PASS;
            if (config.requireEmptyHand) {
                if (!playerEntity.getMainHandStack().isEmpty()) return ActionResult.PASS;
            }

            for (UUID ownerId : getOwnerIds(entity))
            {
                if (ownerId == null) continue;
                if (playerEntity.getUuid().equals(ownerId)) continue;

                Optional<String> username = PetOwnerClient.getNameFromId(ownerId);

                if (username.isPresent()) {
                    playerEntity.sendMessage(new TranslatableText("text.petowner.message.owner", username.get()), false);
                } else {
                    playerEntity.sendMessage(new TranslatableText("text.petowner.message.error"), false);
                }
            }

            return ActionResult.PASS;
        }));
    }

    public static Optional<String> getNameFromId(UUID uuid) {
        return usernameCache.getUnchecked(uuid);
    }

    public static List<UUID> getOwnerIds(Entity entity) {
        if (entity instanceof TameableEntity) {
            TameableEntity tameableEntity = (TameableEntity) entity;

            if (tameableEntity.isTamed()) {
                return Collections.singletonList(tameableEntity.getOwnerUuid());
            }
        }

        if (entity instanceof HorseBaseEntity) {
            HorseBaseEntity horseBaseEntity = (HorseBaseEntity) entity;

            if (horseBaseEntity.isTame()) {
                return Collections.singletonList(horseBaseEntity.getOwnerUuid());
            }
        }

        if (entity instanceof FoxEntity) {
            FoxEntity foxEntity = (FoxEntity) entity;
            return ((FoxTrustedAccessor)foxEntity).getTrusedIds();
        }

        return new ArrayList<>();
    }
}
