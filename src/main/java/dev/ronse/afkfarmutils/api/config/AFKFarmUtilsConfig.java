package dev.ronse.afkfarmutils.api.config;

import dev.ronse.afkfarmutils.AFKFarmUtils;

import java.nio.file.Path;

@SuppressWarnings("unused")
public class AFKFarmUtilsConfig extends JsonFile {
    public static final AFKFarmUtilsConfig INSTANCE = new AFKFarmUtilsConfig();

    public enum PanicAction {
        NONE,
        CHAT,
        NOTIFY_PLAYER,
        LEAVE,
        COMMAND
    }

    // Config file version
    private final String configVersion = AFKFarmUtils.MOD_VERSION;

    // Settings
    private boolean eatFood = true;
    private boolean eatSpecialFood = false;
    private boolean permasneak = false;
    private int attackDelayTicks = 20 * 5;

    // Panic - Health
    private int healthPanicLevel = 4;
    private PanicAction healthPanicAction = PanicAction.LEAVE;
    private String healthPanicMsgOrCmd = "AFKFarmUtils panicked and left the game!";

    // Panic - Food
    private int foodPanicLevel = 8;
    private PanicAction foodPanicAction = PanicAction.CHAT;
    private String foodPanicMsgOrCmd = "I'm hungry and need to eat!";

    // Constructor
    private AFKFarmUtilsConfig() {
        super(Path.of("config", "afkfarmutils.json"));
    }

    // Getters and Setters (Auto-save on change)

    public boolean isEatFood() {
        return eatFood;
    }

    public void setEatFood(boolean eatFood) {
        this.eatFood = eatFood;
        save();
    }

    public boolean isEatSpecialFood() {
        return eatSpecialFood;
    }

    public void setEatSpecialFood(boolean eatSpecialFood) {
        this.eatSpecialFood = eatSpecialFood;
        save();
    }

    public boolean isPermasneak() {
        return permasneak;
    }

    public void setPermasneak(boolean permasneak) {
        this.permasneak = permasneak;
        save();
    }

    public int getAttackDelayTicks() {
        return attackDelayTicks;
    }

    public void setAttackDelayTicks(int attackDelayTicks) {
        this.attackDelayTicks = attackDelayTicks;
        save();
    }

    public int getHealthPanicLevel() {
        return healthPanicLevel;
    }

    public void setHealthPanicLevel(int healthPanicLevel) {
        this.healthPanicLevel = healthPanicLevel;
        save();
    }

    public PanicAction getHealthPanicAction() {
        return healthPanicAction;
    }

    public void setHealthPanicAction(PanicAction healthPanicAction) {
        this.healthPanicAction = healthPanicAction;
        save();
    }

    public String getHealthPanicMsgOrCmd() {
        return healthPanicMsgOrCmd;
    }

    public void setHealthPanicMsgOrCmd(String healthPanicMsgOrCmd) {
        this.healthPanicMsgOrCmd = healthPanicMsgOrCmd;
        save();
    }

    public int getFoodPanicLevel() {
        return foodPanicLevel;
    }

    public void setFoodPanicLevel(int foodPanicLevel) {
        this.foodPanicLevel = foodPanicLevel;
        save();
    }

    public PanicAction getFoodPanicAction() {
        return foodPanicAction;
    }

    public void setFoodPanicAction(PanicAction foodPanicAction) {
        this.foodPanicAction = foodPanicAction;
        save();
    }

    public String getFoodPanicMsgOrCmd() {
        return foodPanicMsgOrCmd;
    }

    public void setFoodPanicMsgOrCmd(String foodPanicMsgOrCmd) {
        this.foodPanicMsgOrCmd = foodPanicMsgOrCmd;
        save();
    }

    public String getConfigVersion() {
        return configVersion;
    }
}
