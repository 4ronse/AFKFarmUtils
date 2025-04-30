package dev.ronse.afkfarmutils.api.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface AFKFarmingStateEvent {
    Event<AFKFarmingStateEvent> EVENT = EventFactory.createArrayBacked(
            AFKFarmingStateEvent.class,
            callbacks -> isFarmingEnabled -> {
                for(AFKFarmingStateEvent callback : callbacks) {
                    callback.onAFKFarmingStateChange(isFarmingEnabled);
                }
            }
    );

    void onAFKFarmingStateChange(boolean isFarmingEnabled);
}
