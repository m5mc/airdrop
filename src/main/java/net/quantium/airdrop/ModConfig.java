package net.quantium.airdrop;

import net.minecraftforge.common.config.Configuration;

public final class ModConfig {
    public float radius = 150;
    public float timeMin = 60;
    public float timeMax = 120;
    public float timeDrop = 5;

    public static ModConfig config(Configuration c) {
        ModConfig x = new ModConfig();

        x.timeMin = c.getFloat("delayTimeMin", "Airdrop", x.timeMin, 0, Float.POSITIVE_INFINITY, "Minimum delay time between airdrops");
        x.timeMax = c.getFloat("delayTimeMax", "Airdrop", x.timeMax, 0, Float.POSITIVE_INFINITY, "Maximum delay time between airdrops");
        x.timeDrop = c.getFloat("dropTime", "Airdrop", x.timeDrop, 0, Float.POSITIVE_INFINITY, "Airdrop drop time");
        x.radius = c.getFloat("dropRadius", "Airdrop", x.radius, 0, Float.POSITIVE_INFINITY, "Airdrop drop 1 sigma radius from spawn");

        c.save();
        return x;
    }
}
