package dev.ronse.afkfarmutils.features;

import com.mojang.logging.LogUtils;
import dev.ronse.afkfarmutils.api.config.AFKFarmUtilsConfig;
import dev.ronse.afkfarmutils.api.notification.DesktopNotificationHandler;
import dev.ronse.afkfarmutils.api.notification.MinecraftToastNotificationHandler;
import dev.ronse.afkfarmutils.api.notification.NotificationHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.slf4j.Logger;

public class AFKFeaturePanic extends AFKFeature {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AFKFarmUtilsConfig CONFIG = AFKFarmUtilsConfig.INSTANCE;
    private static final long PANIC_COOLDOWN_TICKS = 20 * 10;
    private static NotificationHandler notificationHandler;
    private long lastPanicTimeTick = 0;

    enum PanicState {
        NONE,
        PANIC_HEALTH,
        PANIC_HUNGER
    }

    public AFKFeaturePanic(boolean enabled) {
        super("Panic", "Automatically respond to dangerous situations", enabled);
        initNotificationHandler();
    }

    private void initNotificationHandler() {
        if (notificationHandler != null) return;

        try {
            DesktopNotificationHandler desktopHandler = new DesktopNotificationHandler();

            if (desktopHandler.isNotificationAvailable()) {
                notificationHandler = desktopHandler;
                LOGGER.info("Using desktop notifications for panic alerts");
            } else {
                throw new Exception("Desktop notifications not available: " + desktopHandler.getDiagnosticSummary());
            }
        } catch (Exception e) {
            LOGGER.warn("Falling back to toast notifications: {}", e.getMessage());
            notificationHandler = new MinecraftToastNotificationHandler();
            LOGGER.info("Using toast notifications for panic alerts");
        }
    }

    @Override
    public void doTick() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        PanicState panicState = getCurrentPanicState(player);
        if (panicState == PanicState.NONE) return;

        boolean isHealthPanic = (panicState == PanicState.PANIC_HEALTH);
        AFKFarmUtilsConfig.PanicAction action = isHealthPanic
                ? CONFIG.getHealthPanicAction()
                : CONFIG.getFoodPanicAction();

        String actionValue = isHealthPanic
                ? CONFIG.getHealthPanicMsgOrCmd()
                : CONFIG.getFoodPanicMsgOrCmd();

        LOGGER.warn("Panic triggered! State: {}, Action: {}", panicState, action);

        executePanicAction(player, client, action, actionValue);
    }

    private void executePanicAction(ClientPlayerEntity player, MinecraftClient client, AFKFarmUtilsConfig.PanicAction action, String actionValue) {
        switch (action) {
            case NONE -> { /* Do nothing */ }
            case CHAT -> player.networkHandler.sendChatMessage(actionValue);
            case COMMAND -> executeCommands(player, actionValue);
            case NOTIFY_PLAYER -> notificationHandler.notify("AFKFarmUtils Panic", actionValue);
            case LEAVE -> {
                // Send a message before leaving
                if (!actionValue.isEmpty()) player.networkHandler.sendChatMessage(actionValue);
                client.world.disconnect();
            }
        }
    }

    private void executeCommands(ClientPlayerEntity player, String commandString) {
        String[] commands = commandString.split(";");
        for (String command : commands) {
            command = command.strip();
            if (!command.isEmpty()) {
                player.networkHandler.sendCommand(command);
            }
        }
    }

    @Override
    public boolean canTick(long currentTick) {
        // Only tick if there's an active panic state
        PanicState state = getCurrentPanicState(MinecraftClient.getInstance().player);
        if (state == PanicState.NONE) return false;

        // Check if cooldown has elapsed
        if (currentTick - lastPanicTimeTick > PANIC_COOLDOWN_TICKS) {
            lastPanicTimeTick = currentTick;
            return true;
        }

        return false;
    }

    private PanicState getCurrentPanicState(ClientPlayerEntity player) {
        if (player == null) return PanicState.NONE;

        // Check health panic
        if (player.getHealth() <= CONFIG.getHealthPanicLevel())
            return PanicState.PANIC_HEALTH;

        // Check hunger panic
        if (player.getHungerManager().getFoodLevel() <= CONFIG.getFoodPanicLevel())
            return PanicState.PANIC_HUNGER;

        return PanicState.NONE;
    }
}