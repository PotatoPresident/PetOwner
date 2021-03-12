package us.potatoboy.petowner.client.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

public class ConfigGUI extends Screen {
	private final Screen parent;

	private KeybindModeButtonWidget keybindModeButtonWidget;

	public ConfigGUI(Screen parent) {
		super(new TranslatableText("text.petowner.config.title"));

		this.parent = parent;
	}

	@Override
	public void init(MinecraftClient client, int width, int height) {
		super.init(client, width, height);

		keybindModeButtonWidget = addButton(new KeybindModeButtonWidget(10, 30, 175, 20));

		addButton(new ButtonWidget(width - 220, height - 30, 100, 20, new TranslatableText("text.petowner.config.button.confirm"), (onPress) -> {
			keybindModeButtonWidget.saveValue();
			client.openScreen(parent);
		}));
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);

		drawStringWithShadow(matrices, textRenderer, new TranslatableText("text.petowner.config.title").getString(), 20, 10, 16777215);
		if (keybindModeButtonWidget.isHovered()) {
			renderTooltip(matrices, keybindModeButtonWidget.getTooltip(), mouseX, mouseY);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return true;
	}
}
