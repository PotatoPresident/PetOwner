package us.potatoboy.petowner.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfiglite.api.ConfigScreen;
import net.minecraft.text.TranslatableText;
import us.potatoboy.petowner.client.PetOwnerClient;

public class ModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> {
			ConfigScreen screen = ConfigScreen.create(new TranslatableText("text.petowner.config.title"), parent);
			screen.add(
					new TranslatableText("text.petowner.config.always_show"),
					PetOwnerClient.config.alwaysShow,
					() -> false,
					o -> {
						PetOwnerClient.config.alwaysShow = (boolean) o;
						PetOwnerClient.saveConfig();
					}
			);
			screen.add(
					new TranslatableText("text.petowner.config.mode"),
					PetOwnerClient.config.keybindMode,
					() ->  PetOwnerClient.KeybindMode.TOGGLE,
					o -> {
						PetOwnerClient.config.keybindMode = (PetOwnerClient.KeybindMode) o;
						PetOwnerClient.saveConfig();
					}
			);
			screen.add(
					new TranslatableText("text.petowner.config.show_keybind_message"),
					PetOwnerClient.config.showKeybindMessage,
					() -> true,
					o -> {
						PetOwnerClient.config.showKeybindMessage = (boolean) o;
						PetOwnerClient.saveConfig();
					}
			);

			return screen.get();
		};
	}
}
