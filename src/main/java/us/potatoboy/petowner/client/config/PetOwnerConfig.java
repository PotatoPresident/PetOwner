package us.potatoboy.petowner.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import us.potatoboy.petowner.client.PetOwnerClient;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class PetOwnerConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public PetOwnerClient.KeybindMode keybindMode = PetOwnerClient.KeybindMode.TOGGLE;
	public boolean alwaysShow = false;
	public boolean showKeybindMessage = false;

	public static PetOwnerConfig loadConfig(File file) {
		PetOwnerConfig config;

		if (file.exists() && file.isFile()) {
			try (
					FileInputStream fileInputStream = new FileInputStream(file);
					InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
					BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
			) {
				config = GSON.fromJson(bufferedReader, PetOwnerConfig.class);
			} catch (IOException e) {
				throw new RuntimeException("[HTM] Failed to load config", e);
			}
		} else {
			config = new PetOwnerConfig();
		}

		config.saveConfig(file);

		return config;
	}

	public void saveConfig(File config) {
		try (
				FileOutputStream stream = new FileOutputStream(config);
				Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)
		) {
			GSON.toJson(this, writer);
		} catch (IOException e) {
			PetOwnerClient.LOGGER.error("Failed to save config");
		}
	}
}
