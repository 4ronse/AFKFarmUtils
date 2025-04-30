package dev.ronse.afkfarmutils.features;

import com.mojang.logging.LogUtils;
import dev.ronse.afkfarmutils.api.config.AFKFarmUtilsConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.slf4j.Logger;

public class AFKFeaturePermaSneak extends AFKFeature {
    private static final Logger LOGGER = LogUtils.getLogger();

    public AFKFeaturePermaSneak(boolean enabled) {
        super("PermaSneak", "Permanently sneak when AFK", enabled);
    }

    @Override
    public void doTick() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) return;
        if(!AFKFarmUtilsConfig.INSTANCE.isPermasneak()) return;
        if(!player.isSneaking()) client.options.sneakKey.setPressed(true);
    }

    @Override
    public boolean canTick(long now) {
        return AFKFarmUtilsConfig.INSTANCE.isPermasneak();
    }

    @Override
    public void onAFKFarmingDisable() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) return;
        client.options.sneakKey.setPressed(false);
    }
}
