package dev.ronse.afkfarmutils.features;

import com.mojang.logging.LogUtils;
import dev.ronse.afkfarmutils.api.config.AFKFarmUtilsConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Rarity;
import org.slf4j.Logger;

import java.util.Set;

public class AFKFeatureEat extends AFKFeature {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AFKFarmUtilsConfig CONFIG = AFKFarmUtilsConfig.INSTANCE;

    private static final Set<Item> BANNED_FOODS = Set.of(
            Items.OMINOUS_BOTTLE,
            Items.POISONOUS_POTATO,
            Items.PUFFERFISH,
            Items.SPIDER_EYE
    );

    public AFKFeatureEat(boolean enabled) {
        super("Eat", "Automatically eat food when hungry", enabled);
    }

    private boolean isFoodFound = false;
    private boolean isEating = false;
    private int foodSlot = -1;

    @Override
    public void doTick() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if(isEating) {
            if(player.getHungerManager().getFoodLevel() > CONFIG.getFoodPanicLevel() + 1) {
                stopEating();
                return;
            }

            ItemStack mainHandStack = player.getMainHandStack();
            ItemStack offHandStack = player.getOffHandStack();

            if(!isAllowedFood(mainHandStack) && !isAllowedFood(offHandStack)) stopEating();
        }

        if(!isFoodFound) {
            if(findFood()) {
                isFoodFound = true;
                LOGGER.info("Found food in inventory: {}", foodSlot);
                if(foodSlot > -1) swapToHotbar(foodSlot, player.getInventory().selectedSlot);
            } else {
                LOGGER.info("No food found in inventory");
                return;
            }
        }

        if(isFoodFound && !isEating) startEating();
    }

    private boolean isFood(ItemStack stack) {
        if(stack == null || stack.isEmpty() || BANNED_FOODS.contains(stack.getItem())) return false;
        return stack.get(DataComponentTypes.FOOD) != null;
    }

    private boolean isAllowedFood(ItemStack stack) {
        if(!isFood(stack)) return false;
        return CONFIG.isEatSpecialFood() || stack.getRarity() == Rarity.COMMON;
    }

    private boolean findFood() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        PlayerInventory inventory = player.getInventory();

        ItemStack mainHandStack = player.getMainHandStack();
        ItemStack offHandStack = player.getOffHandStack();

        if(isAllowedFood(mainHandStack) || isAllowedFood(offHandStack)) return true;
        for(int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if(isAllowedFood(stack)) {
                foodSlot = i;
                if(foodSlot < 9) foodSlot += 36;
                return true;
            }
        }
        return false;
    }

    private void swapToHotbar(int invSlot, int hotbarSlot) {
        LOGGER.info("Swapping slots: [InvSlot: {} -> HotbarSlot: {}]", invSlot, hotbarSlot);

        MinecraftClient client = MinecraftClient.getInstance();

        client.interactionManager.clickSlot(
                client.player.currentScreenHandler.syncId,
                invSlot,
                hotbarSlot,
                SlotActionType.SWAP,
                client.player
        );
    }

    private void startEating() {
        LOGGER.info("Starting eating");

        isEating = true;
        MinecraftClient.getInstance().options.useKey.setPressed(true);
    }

    private void stopEating() {
        LOGGER.info("Stopping eating");

        MinecraftClient.getInstance().options.useKey.setPressed(false);
        if(foodSlot > -1) swapToHotbar(foodSlot, MinecraftClient.getInstance().player.getInventory().selectedSlot);

        isFoodFound = false;
        isEating = false;
        foodSlot = -1;
    }

    @Override
    public boolean canTick(long now) {
        if(isEating) return true;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        return player.getHungerManager().getFoodLevel() <= CONFIG.getFoodPanicLevel() + 1;
    }

    @Override
    public void onAFKFarmingDisable() {
        stopEating();
    }

    public boolean isEating() {
        return isEating;
    }
}
