package dev.ronse.afkfarmutils.api.notification;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class MinecraftToastNotificationHandler implements NotificationHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void notify(String title, String message) {
        LOGGER.debug("Showing Minecraft toast notification: {} - {}", title, message);
        MinecraftClient.getInstance().execute(() -> SystemToast.add(
                MinecraftClient.getInstance().getToastManager(),
                SystemToast.Type.PERIODIC_NOTIFICATION,
                Text.of(title),
                Text.of(message)
        ));
    }
}
