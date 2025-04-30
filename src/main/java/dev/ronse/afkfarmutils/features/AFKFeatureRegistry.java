package dev.ronse.afkfarmutils.features;

import com.mojang.logging.LogUtils;
import dev.ronse.afkfarmutils.api.events.AFKFarmingStateEvent;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.Collections;

/**
 * Registry for managing AFK features in the mod.
 * Handles registration, enabling/disabling, and ticking of features.
 */
public class AFKFeatureRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static AFKFeatureRegistry instance;

    private final Map<String, AFKFeature> features;
    private final Map<String, Boolean> featureStates;

    /**
     * Private constructor for singleton pattern
     */
    private AFKFeatureRegistry() {
        features = new HashMap<>();
        featureStates = new HashMap<>();

        AFKFarmingStateEvent.EVENT.register((isFarmingEnabled -> {
            if(isFarmingEnabled) {
                for (AFKFeature feature : features.values()) {
                    if (feature.enabled) {
                        feature.onAFKFarmingEnable();
                    }
                }
            } else {
                for (AFKFeature feature : features.values()) {
                    if (feature.enabled) {
                        feature.onAFKFarmingDisable();
                    }
                }
            }
        }));
    }

    /**
     * Get the singleton instance of the registry
     * @return The FeatureRegistry instance
     */
    public static AFKFeatureRegistry getInstance() {
        if (instance == null) {
            instance = new AFKFeatureRegistry();
        }
        return instance;
    }

    /**
     * Register a new AFK feature
     * @param feature The feature to register
     * @return true if registration was successful, false if a feature with the same name already exists
     */
    public boolean registerFeature(AFKFeature feature) {
        if (features.containsKey(feature.name)) {
            LOGGER.warn("Attempted to register duplicate feature: {}", feature.name);
            return false;
        }

        features.put(feature.name, feature);
        featureStates.put(feature.name, feature.enabled);
        LOGGER.info("Registered feature: {}, {}", feature.name, feature.enabled ? "ENABLED" : "DISABLED");
        return true;
    }

    /**
     * Get a feature by its name
     * @param name The name of the feature
     * @return The feature, or null if not found
     */
    public AFKFeature getFeature(String name) {
        return features.get(name);
    }

    /**
     * Get all registered features
     * @return An unmodifiable collection of all features
     */
    public Collection<AFKFeature> getAllFeatures() {
        return Collections.unmodifiableCollection(features.values());
    }

    /**
     * Enable or disable a feature
     * @param name The name of the feature
     * @param enabled The new state
     * @return true if the feature exists and state was changed, false otherwise
     */
    public boolean setFeatureEnabled(String name, boolean enabled) {
        if (!features.containsKey(name)) {
            LOGGER.warn("Attempted to change state of unknown feature: {}", name);
            return false;
        }

        featureStates.put(name, enabled);
        LOGGER.info("Feature {} is now {}", name, enabled ? "enabled" : "disabled");
        return true;
    }

    /**
     * Check if a feature is enabled
     * @param name The name of the feature
     * @return true if the feature exists and is enabled, false otherwise
     */
    public boolean isFeatureEnabled(String name) {
        return featureStates.getOrDefault(name, false);
    }

    /**
     * Tick all enabled features
     * @param currentTime The current time for tick calculations
     */
    public void tickFeatures(long currentTime) {
        for (Map.Entry<String, AFKFeature> entry : features.entrySet()) {
            String name = entry.getKey();
            AFKFeature feature = entry.getValue();

            if (isFeatureEnabled(name) && feature.canTick(currentTime)) {
                try {
                    feature.doTick();
                } catch (Exception e) {
                    LOGGER.error("Error during tick of feature {}", name, e);
                }
            }
        }
    }

    /**
     * Unregister all features (useful for reloading)
     */
    public void unregisterAll() {
        features.clear();
        featureStates.clear();
        LOGGER.info("Unregistered all features");
    }
}
