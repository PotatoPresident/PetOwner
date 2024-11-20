package us.potatoboy.petowner.client.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class PetOwnerConfig extends MidnightConfig {
	@Entry public static KeybindMode keybindMode = KeybindMode.TOGGLE;
	@Entry public static boolean alwaysShow = false;
	@Entry public static boolean showKeybindMessage = true;

	public enum KeybindMode {
		TOGGLE,
		HOLD
	}
}
