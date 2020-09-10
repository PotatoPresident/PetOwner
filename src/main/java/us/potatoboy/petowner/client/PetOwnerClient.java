package us.potatoboy.petowner.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.apache.commons.io.IOUtils;
import us.potatoboy.petowner.mixin.FoxTrustedAccessor;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Environment(EnvType.CLIENT)
public class PetOwnerClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AutoConfig.register(PetOwnerConfig.class, JanksonConfigSerializer::new);
        PetOwnerConfig config = AutoConfig.getConfigHolder(PetOwnerConfig.class).getConfig();

        UseEntityCallback.EVENT.register(((playerEntity, world, hand, entity, entityHitResult) -> {
            if (!hand.equals(Hand.MAIN_HAND)) return ActionResult.PASS;
            if (config.requireEmptyHand) {
                if (!playerEntity.getMainHandStack().isEmpty()) return ActionResult.PASS;
            }

            for (UUID ownerId : getOwnerIds(entity))
            {
                if (ownerId == null) continue;
                if (playerEntity.getUuid().equals(ownerId)) continue;

                CompletableFuture.runAsync(() -> {
                    try {
                        String name = getNameFromId(ownerId);

                        playerEntity.sendMessage(new TranslatableText("text.petowner.message.owner", name), false);
                    } catch (Exception e) {
                        playerEntity.sendMessage(new TranslatableText("text.petowner.message.error"), false);
                        e.printStackTrace();
                    }
                });
            }

            return ActionResult.PASS;
        }));
    }

    private static String getNameFromId(UUID uuid) throws Exception {
        System.out.println("Getting name of uuid: " + uuid);
        String url_ = "https://api.mojang.com/user/profiles/%s/names";
        URL url = new URL(String.format(url_, uuid.toString().replace("-", "")));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        String s = IOUtils.toString(connection.getInputStream());
        JsonArray array = new JsonParser().parse(s).getAsJsonArray();
        JsonObject object = (JsonObject) array.get(array.size() - 1);

        return object.get("name").getAsString();
    }

    private static List<UUID> getOwnerIds(Entity entity) {
        if (entity instanceof TameableEntity) {
            TameableEntity tameableEntity = (TameableEntity) entity;

            if (tameableEntity.isTamed()) {
                return Arrays.asList(tameableEntity.getOwnerUuid());
            }
        }

        if (entity instanceof HorseBaseEntity) {
            HorseBaseEntity horseBaseEntity = (HorseBaseEntity) entity;

            if (horseBaseEntity.isTame()) {
                return Arrays.asList(horseBaseEntity.getOwnerUuid());
            }
        }

        if (entity instanceof FoxEntity) {
            FoxEntity foxEntity = (FoxEntity) entity;
            return ((FoxTrustedAccessor)foxEntity).getTrusedIds();
        }

        return new ArrayList<>();
    }
}
