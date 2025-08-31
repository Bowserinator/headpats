package net.hellomouse;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = HeadpatMod.MOD_ID)
public class HeadpatConfig implements ConfigData {
    @ConfigEntry.BoundedDiscrete(min = 0, max = 1)
    public float firstPersonSwayStrength = 1f;
    public boolean pettedPlayersPurr = false;
}
