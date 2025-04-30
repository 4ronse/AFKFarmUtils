package dev.ronse.afkfarmutils.api.notification;

public interface NotificationHandler {
    void notify(String title, String message);

    default void notify(String title, String message, String imagePath) { notify(title, message); }
    default void cleanup() {}
}
