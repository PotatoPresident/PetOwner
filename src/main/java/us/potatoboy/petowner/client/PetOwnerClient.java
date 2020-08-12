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
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.apache.commons.io.IOUtils;

import java.net.HttpURLConnection;
import java.net.URL;
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

            if (entityHitResult.getEntity() instanceof TameableEntity) {
                TameableEntity tameableEntity = (TameableEntity) entityHitResult.getEntity();

                if (!tameableEntity.isTamed()) return ActionResult.PASS;

                if (tameableEntity.isOwner(playerEntity)) return ActionResult.PASS;

                CompletableFuture.runAsync(() -> {
                    try {
                        String name = getNameFromId(tameableEntity.getOwnerUuid());

                        playerEntity.sendMessage(Text.method_30163("Owner: " + name), false);
                    } catch (Exception e) {
                        playerEntity.sendMessage(Text.method_30163("Something went wrong"), false);
                        e.printStackTrace();
                    }
                });
            }
            return ActionResult.PASS;
        }));
    }

    private static String getNameFromId(UUID uuid) throws Exception {
        String url_ = "https://api.mojang.com/user/profiles/%s/names";
        URL url = new URL(String.format(url_, uuid.toString().replace("-", "")));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        String s = IOUtils.toString(connection.getInputStream());
        JsonArray array = new JsonParser().parse(s).getAsJsonArray();
        JsonObject object = (JsonObject) array.get(array.size() - 1);

        return object.get("name").getAsString();
    }
}
