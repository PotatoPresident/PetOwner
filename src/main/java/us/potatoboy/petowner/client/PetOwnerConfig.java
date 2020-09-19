package us.potatoboy.petowner.client;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;

@Config(name = "petowner")
public class PetOwnerConfig implements ConfigData {
    @ConfigEntry.Category("nametag")
    public Boolean nametag = true;

    @ConfigEntry.Category("click")
    public Boolean click = false;

    @ConfigEntry.Category("click")
    public Boolean requireEmptyHand = true;
}
