package dev.ronse.afkfarmutils.features;

public abstract class AFKFeature implements IAFKFeature {
    public final String name;
    public final String description;
    public final boolean enabled;

    public AFKFeature(String name, String description, boolean enabled) {
        this.name = name;
        this.description = description;
        this.enabled = enabled;
    }
}
