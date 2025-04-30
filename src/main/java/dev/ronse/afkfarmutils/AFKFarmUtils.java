package dev.ronse.afkfarmutils;

import com.mojang.logging.LogUtils;
import dev.ronse.afkfarmutils.api.config.AFKFarmUtilsConfig;
import dev.ronse.afkfarmutils.api.events.AFKFarmingStateEvent;
import dev.ronse.afkfarmutils.api.notification.DesktopNotificationHandler;
import dev.ronse.afkfarmutils.features.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.event.Level;

// TODO: Fix panic feature
// TODO: Panic feature - Implement System Notifications. Perchance even a mobile notification? Discord webhook? Pigeon messenger?
// TODO: Disable farming mode when not AFK

public class AFKFarmUtils implements ClientModInitializer {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String MOD_VERSION = "0.0.1";
    public static final String MOD_ID = "afkfarmutils";

    private static KeyBinding FARMING_TOGGLE_KEYBINDING;

    @Override
    public void onInitializeClient() {
        LogUtils.configureRootLoggingLevel(Level.DEBUG);

        // Load Config
        AFKFarmUtilsConfig.INSTANCE.load();

        // Register Features
        AFKFeatureRegistry registry = AFKFeatureRegistry.getInstance();
        registry.registerFeature(new AFKFeaturePanic(true));
        registry.registerFeature(new AFKFeatureAttack(true));
        registry.registerFeature(new AFKFeatureEat(true));
        registry.registerFeature(new AFKFeaturePermaSneak(true));
        LOGGER.info("Registered {} features", registry.getAllFeatures().size());


        // Register Keybinding
        FARMING_TOGGLE_KEYBINDING = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.%s.farming_toggle".formatted(MOD_ID),
                        GLFW.GLFW_KEY_EQUAL,
                        "key.categories.%s".formatted(MOD_ID)
                )
        );
        LOGGER.debug("Registered keybinding: {}", FARMING_TOGGLE_KEYBINDING.getTranslationKey());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(client.player == null) return;

            if(FARMING_TOGGLE_KEYBINDING.wasPressed()) {
                AFKStates.isFarmingEnabled = !AFKStates.isFarmingEnabled;
                Text status = Text.literal(AFKStates.isFarmingEnabled ? "Enabled" : "Disabled")
                        .setStyle(Style.EMPTY.withColor(AFKStates.isFarmingEnabled ? 0x00FF00 : 0xFF0000).withBold(true));
                client.player.sendMessage(
                        Text.literal("Farming is now ").setStyle(Style.EMPTY.withColor(Formatting.YELLOW)).append(status),
                        false
                );
            }

            AFKStates.tick();

            if(!AFKStates.isFarmingEnabled) return;
            long now = client.world.getTime();
            registry.tickFeatures(now);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> AFKStates.isFarmingEnabled = false);

        // Register reload command
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> dispatcher.register(CommandManager.literal("afkfarmutils")
                    .then(CommandManager.literal("reload")
                            .executes(context -> {
                                AFKFarmUtilsConfig.INSTANCE.load();
                                context.getSource().sendFeedback((
                                        () -> Text.of("Reloaded config!")
                                ), true);
                                return 1;
                            })
                    )
        ));
    }
}
