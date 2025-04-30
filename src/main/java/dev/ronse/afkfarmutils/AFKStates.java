package dev.ronse.afkfarmutils;

import dev.ronse.afkfarmutils.api.events.AFKFarmingStateEvent;

public class AFKStates {
    public static boolean isFarmingEnabled = false;

    private static boolean lastIsFarmingEnabled = false;

    public static void tick() {
        if(isFarmingEnabled != lastIsFarmingEnabled) {
            AFKFarmingStateEvent.EVENT.invoker().onAFKFarmingStateChange(isFarmingEnabled);
            lastIsFarmingEnabled = isFarmingEnabled;
        }
    }
}
