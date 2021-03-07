package us.potatoboy.petowner.client.config;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import us.potatoboy.petowner.client.PetOwnerClient;

public class KeybindModeButtonWidget extends ButtonWidget {
	private boolean value;

	public KeybindModeButtonWidget(int x, int y, int width, int height) {
		super(x, y, width, height, new LiteralText(""), press -> {});

		value = PetOwnerClient.config.keybindMode;
		updateMessage();
	}

	@Override
	public void onPress() {
		value = !value;
		updateMessage();
	}

	@Override
	public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.renderButton(matrices, mouseX, mouseY, delta);
	}

	private void updateMessage() {
		setMessage(new TranslatableText("text.petowner.config.mode." + (value ? "toggle": "hold")));
	}

	public Text getTooltip() {
		return new TranslatableText("text.petowner.config.mode.tooltip." + (value ? "toggle": "hold"));
	}

	public void saveValue() {
		PetOwnerClient.config.keybindMode = value;
		PetOwnerClient.saveConfig();
	}
}
