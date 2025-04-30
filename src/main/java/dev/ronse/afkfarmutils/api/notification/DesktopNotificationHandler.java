package dev.ronse.afkfarmutils.api.notification;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class DesktopNotificationHandler implements NotificationHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean isInitialized = false;
    private static boolean systemTraySupported = false;
    private static boolean headlessMode = false;
    private static boolean initializationSuccessful = false;
    private static SystemTray tray = null;
    private static TrayIcon trayIcon = null;
    private static final Map<String, String> diagnosticInfo = new HashMap<>();

    public DesktopNotificationHandler() {
        init();
    }

    /**
     * Initializes the desktop notification system with detailed diagnostic information
     */
    private void init() {
        // Avoid double initialization
        if (isInitialized) return;
        isInitialized = true;

        // Collect system information for diagnostics
        gatherSystemInfo();

        // Only run on client-side
        if (!FabricLoader.getInstance().getEnvironmentType().equals(EnvType.CLIENT)) {
            LOGGER.info("Desktop notifications only supported on client side. Current environment: {}",
                    FabricLoader.getInstance().getEnvironmentType());
            diagnosticInfo.put("error", "Not running on client side");
            return;
        }

        // Check for headless mode
        headlessMode = GraphicsEnvironment.isHeadless();
        if (headlessMode) {
            LOGGER.warn("Running in headless mode. System tray notifications will not be available.");
            diagnosticInfo.put("error", "Running in headless mode");
            return;
        }

        // Check if SystemTray is supported
        systemTraySupported = SystemTray.isSupported();
        if (!systemTraySupported) {
            LOGGER.warn("System tray is not supported on this platform");
            LOGGER.info("System tray diagnostic info: {}", diagnosticInfo);
            diagnosticInfo.put("error", "SystemTray.isSupported() returned false");
            return;
        }

        // Try to initialize AWT components on the Event Dispatch Thread
        try {
            if (!EventQueue.isDispatchThread()) {
                LOGGER.info("Initializing notification system on Event Dispatch Thread");
                final Runnable initRunnable = this::initSystemTray;
                EventQueue.invokeAndWait(initRunnable);
            } else {
                initSystemTray();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to initialize system tray on Event Dispatch Thread", e);
            diagnosticInfo.put("error", "EDT initialization failed: " + e.getMessage());
        }

        // Log complete diagnostic information
        LOGGER.info("Desktop notification system diagnostic info: {}", diagnosticInfo);
    }

    /**
     * Initialize the actual system tray components
     */
    private void initSystemTray() {
        try {
            // Get system tray instance
            tray = SystemTray.getSystemTray();
            diagnosticInfo.put("tray_size", tray.getTrayIconSize().width + "x" + tray.getTrayIconSize().height);

            // Try loading icon with proper error handling
            Image image = null;
            String iconPath = null;
            String[] possiblePaths = {
                    "/assets/afkfarmutils/icon.png",
                    "/assets/afkfarmutils/textures/icon.png",
                    "/icon.png",
                    "/assets/afkfarmutils/textures/gui/icon.png"
            };

            for (String path : possiblePaths) {
                try {
                    InputStream is = getClass().getResourceAsStream(path);
                    if (is != null) {
                        image = Toolkit.getDefaultToolkit().createImage(getClass().getResource(path));
                        iconPath = path;
                        LOGGER.info("Found icon at: {}", path);
                        break;
                    }
                } catch (Exception e) {
                    LOGGER.debug("Could not load icon from {}: {}", path, e.getMessage());
                }
            }

            // Log icon loading result
            if (iconPath != null) {
                diagnosticInfo.put("icon_loaded_from", iconPath);
            } else {
                diagnosticInfo.put("icon_error", "Could not load icon from any path");
            }

            // Fallback to generated image if needed
            if (image == null) {
                LOGGER.warn("Using generated fallback icon - could not load icon from resources");
                int size = tray.getTrayIconSize().width;
                image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = ((BufferedImage) image).createGraphics();
                g.setColor(Color.GREEN);
                g.fillRect(0, 0, size, size);
                g.setColor(Color.BLACK);
                g.drawRect(0, 0, size-1, size-1);
                g.dispose();
                diagnosticInfo.put("icon", "using generated fallback icon " + size + "x" + size);
            }

            // Create and configure tray icon
            trayIcon = new TrayIcon(image, "AFKFarmUtils");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("AFKFarmUtils Notifications");

            // Add popup menu to icon
            PopupMenu popupMenu = new PopupMenu();
            MenuItem aboutItem = new MenuItem("AFKFarmUtils");
            aboutItem.setEnabled(false);
            MenuItem testItem = new MenuItem("Test Notification");
            testItem.addActionListener(e -> notify("Test Notification", "This is a test notification"));
            MenuItem exitItem = new MenuItem("Hide Notifications");
            exitItem.addActionListener(e -> cleanup());

            popupMenu.add(aboutItem);
            popupMenu.add(testItem);
            popupMenu.add(exitItem);
            trayIcon.setPopupMenu(popupMenu);

            // Add icon to system tray
            tray.add(trayIcon);
            initializationSuccessful = true;

            // Send test notification to confirm it's working
            trayIcon.displayMessage("AFKFarmUtils", "Notification system initialized", TrayIcon.MessageType.INFO);
            LOGGER.info("System tray icon created successfully");
            diagnosticInfo.put("status", "initialized successfully");
        } catch (AWTException e) {
            LOGGER.error("Failed to create system tray icon: {}", e.getMessage());
            diagnosticInfo.put("error", "AWTException: " + e.getMessage());
            initializationSuccessful = false;
        } catch (Exception e) {
            LOGGER.error("Unexpected error initializing notification system", e);
            diagnosticInfo.put("error", "Exception: " + e.getMessage());
            initializationSuccessful = false;
        }
    }

    /**
     * Gather system information for diagnostics
     */
    private void gatherSystemInfo() {
        try {
            diagnosticInfo.put("os.name", System.getProperty("os.name"));
            diagnosticInfo.put("os.version", System.getProperty("os.version"));
            diagnosticInfo.put("java.version", System.getProperty("java.version"));
            diagnosticInfo.put("java.awt.headless", String.valueOf(System.getProperty("java.awt.headless")));
            diagnosticInfo.put("user.language", System.getProperty("user.language"));
            diagnosticInfo.put("user.country", System.getProperty("user.country"));
            diagnosticInfo.put("awt.toolkit", Toolkit.getDefaultToolkit().getClass().getName());
            diagnosticInfo.put("systemtray.supported", String.valueOf(SystemTray.isSupported()));
            diagnosticInfo.put("headless", String.valueOf(GraphicsEnvironment.isHeadless()));
            diagnosticInfo.put("minecraft.side", FabricLoader.getInstance().getEnvironmentType().toString());
        } catch (Exception e) {
            LOGGER.warn("Error gathering system info", e);
            diagnosticInfo.put("diagnostic_error", e.getMessage());
        }
    }

    @Override
    public void notify(String title, String message) {
        // First try to initialize if not already done
        if (!isInitialized) {
            init();
        }

        // Check if notifications are supported and properly initialized
        if (!initializationSuccessful || trayIcon == null) {
            LOGGER.warn("Desktop notifications not available. Would have shown: {} - {}", title, message);
            LOGGER.info("Notification system status: {}", getDiagnosticSummary());
            return;
        }

        // Run notification on AWT Event Thread to avoid threading issues
        try {
            EventQueue.invokeLater(() -> {
                try {
                    trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
                    LOGGER.debug("Notification displayed: {} - {}", title, message);
                } catch (Exception e) {
                    LOGGER.error("Failed to display notification: {}", e.getMessage());
                }
            });
        } catch (Exception e) {
            LOGGER.error("Failed to queue notification: {}", e.getMessage());
        }
    }

    @Override
    public void notify(String title, String message, String imagePath) {
        notify(title, message);
    }

    @Override
    public void cleanup() {
        if (initializationSuccessful && tray != null && trayIcon != null) {
            try {
                tray.remove(trayIcon);
                trayIcon = null;
                initializationSuccessful = false;
                LOGGER.info("System tray icon removed successfully");
            } catch (Exception e) {
                LOGGER.error("Failed to remove system tray icon: {}", e.getMessage());
            }
        }
    }

    /**
     * Check if notifications are currently supported and working
     * @return true if notifications can be displayed
     */
    public boolean isNotificationAvailable() {
        return initializationSuccessful && trayIcon != null;
    }

    /**
     * Get diagnostic summary for troubleshooting
     * @return A string containing diagnostic information
     */
    public String getDiagnosticSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("System tray supported: ").append(systemTraySupported);
        sb.append(", Headless mode: ").append(headlessMode);
        sb.append(", Initialization successful: ").append(initializationSuccessful);

        if (diagnosticInfo.containsKey("error")) {
            sb.append(", Error: ").append(diagnosticInfo.get("error"));
        }

        sb.append(", OS: ").append(diagnosticInfo.getOrDefault("os.name", "unknown"));
        sb.append(", Java: ").append(diagnosticInfo.getOrDefault("java.version", "unknown"));

        return sb.toString();
    }

    /**
     * Get full diagnostic information
     * @return Map containing all diagnostic data
     */
    public Map<String, String> getDiagnosticInfo() {
        return new HashMap<>(diagnosticInfo);
    }
}