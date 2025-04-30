package dev.ronse.afkfarmutils.features;

public interface IAFKFeature {
    void doTick();
    boolean canTick(long now);

    default void onAFKFarmingEnable() {}
    default void onAFKFarmingDisable() {}
}
