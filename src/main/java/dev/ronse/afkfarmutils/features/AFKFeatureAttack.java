package dev.ronse.afkfarmutils.features;

import dev.ronse.afkfarmutils.AFKStates;
import dev.ronse.afkfarmutils.api.config.AFKFarmUtilsConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class AFKFeatureAttack extends AFKFeature {
    private long lastAttackTick = 0;

    public AFKFeatureAttack(boolean enabled) {
        super("Attack", "Automatically attack", enabled);
    }

    @Override
    public void doTick() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if(!AFKStates.isFarmingEnabled || player == null || client.interactionManager == null || client.world == null) return;

        HitResult hit = client.crosshairTarget;
        if(hit instanceof EntityHitResult entityHitResult) {
            Entity target = entityHitResult.getEntity();
            client.interactionManager.attackEntity(player, target);
            player.swingHand(player.getActiveHand());
            lastAttackTick = client.world.getTime();
        }
    }

    @Override
    public boolean canTick(long now) {
        AFKFeatureEat eatFeature = (AFKFeatureEat) AFKFeatureRegistry.getInstance().getFeature("Eat");

        if(eatFeature != null && eatFeature.isEating()) return false;
        return now - lastAttackTick >= AFKFarmUtilsConfig.INSTANCE.getAttackDelayTicks();
    }
}
