package dev.ronse.afkfarmutils;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.ronse.afkfarmutils.api.config.AFKFarmUtilsConfig;
import dev.ronse.afkfarmutils.api.events.AFKFarmingStateEvent;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class AFKModMenuImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.of("AFK Farm Utils Config"))
                    .setSavingRunnable(AFKFarmUtilsConfig.INSTANCE::save);

            ConfigCategory general = builder.getOrCreateCategory(Text.of("General"));
            ConfigCategory food = builder.getOrCreateCategory(Text.of("Food"));
            ConfigCategory panicHealth = builder.getOrCreateCategory(Text.of("Panic - Health"));
            ConfigCategory panicFood = builder.getOrCreateCategory(Text.of("Panic - Food"));
            ConfigCategory attack = builder.getOrCreateCategory(Text.of("Attack"));
            ConfigCategory permaSneak = builder.getOrCreateCategory(Text.of("Perma Sneak"));

            general.addEntry(builder.entryBuilder().startBooleanToggle(Text.of("Status"), AFKStates.isFarmingEnabled)
                    .setDefaultValue(false)
                    .setTooltip(Text.of("Enable or disable AFK farming"))
                    .setSaveConsumer(value -> {
                        AFKStates.isFarmingEnabled = value;
                        AFKFarmingStateEvent.EVENT.invoker().onAFKFarmingStateChange(value);
                    })
                    .build());


            food.addEntry(builder.entryBuilder().startBooleanToggle(Text.of("Eat Food"), AFKFarmUtilsConfig.INSTANCE.isEatFood())
                    .setDefaultValue(true)
                    .setTooltip(Text.of("Eat food when hungry"))
                    .setSaveConsumer(AFKFarmUtilsConfig.INSTANCE::setEatFood)
                    .build());

            food.addEntry(builder.entryBuilder().startBooleanToggle(Text.of("Eat Special Food"), AFKFarmUtilsConfig.INSTANCE.isEatSpecialFood())
                    .setDefaultValue(false)
                    .setTooltip(Text.of("Eat special food when hungry"))
                    .setSaveConsumer(AFKFarmUtilsConfig.INSTANCE::setEatSpecialFood)
                    .build());


            panicHealth.addEntry(builder.entryBuilder().startIntField(Text.of("Health Panic Level"), AFKFarmUtilsConfig.INSTANCE.getHealthPanicLevel())
                    .setDefaultValue(4)
                    .setTooltip(Text.of("Health level to panic at"))
                    .setSaveConsumer(AFKFarmUtilsConfig.INSTANCE::setHealthPanicLevel)
                    .build());

            panicHealth.addEntry(builder.entryBuilder().startEnumSelector(Text.of("Health Panic Action"), AFKFarmUtilsConfig.PanicAction.class, AFKFarmUtilsConfig.INSTANCE.getHealthPanicAction())
                    .setDefaultValue(AFKFarmUtilsConfig.PanicAction.LEAVE)
                    .setTooltip(Text.of("Action to take when health is low"))
                    .setSaveConsumer(AFKFarmUtilsConfig.INSTANCE::setHealthPanicAction)
                    .build());

            panicHealth.addEntry(builder.entryBuilder().startStrField(Text.of("Health Panic Message/Command"), AFKFarmUtilsConfig.INSTANCE.getHealthPanicMsgOrCmd())
                    .setDefaultValue("AFKFarmUtils panicked and left the game!")
                    .setTooltip(Text.of("Message or command to send when health is low"))
                    .setSaveConsumer(AFKFarmUtilsConfig.INSTANCE::setHealthPanicMsgOrCmd)
                    .build());


            panicFood.addEntry(builder.entryBuilder().startIntField(Text.of("Food Panic Level"), AFKFarmUtilsConfig.INSTANCE.getFoodPanicLevel())
                    .setDefaultValue(8)
                    .setTooltip(Text.of("Food level to panic at"))
                    .setSaveConsumer(AFKFarmUtilsConfig.INSTANCE::setFoodPanicLevel)
                    .build());

            panicFood.addEntry(builder.entryBuilder().startEnumSelector(Text.of("Food Panic Action"), AFKFarmUtilsConfig.PanicAction.class, AFKFarmUtilsConfig.INSTANCE.getFoodPanicAction())
                    .setDefaultValue(AFKFarmUtilsConfig.PanicAction.CHAT)
                    .setTooltip(Text.of("Action to take when food is low"))
                    .setSaveConsumer(AFKFarmUtilsConfig.INSTANCE::setFoodPanicAction)
                    .build());

            panicFood.addEntry(builder.entryBuilder().startStrField(Text.of("Food Panic Message/Command"), AFKFarmUtilsConfig.INSTANCE.getFoodPanicMsgOrCmd())
                    .setDefaultValue("I'm hungry and need to eat!")
                    .setTooltip(Text.of("Message or command to send when food is low"))
                    .setSaveConsumer(AFKFarmUtilsConfig.INSTANCE::setFoodPanicMsgOrCmd)
                    .build());


            attack.addEntry(builder.entryBuilder().startIntField(Text.of("Attack Delay (Ticks)"), AFKFarmUtilsConfig.INSTANCE.getAttackDelayTicks())
                    .setDefaultValue(20 * 5)
                    .setTooltip(Text.of("Delay between attacks in ticks"))
                    .setSaveConsumer(AFKFarmUtilsConfig.INSTANCE::setAttackDelayTicks)
                    .build());

            permaSneak.addEntry(builder.entryBuilder().startBooleanToggle(Text.of("Perma Sneak"), AFKFarmUtilsConfig.INSTANCE.isPermasneak())
                    .setDefaultValue(false)
                    .setTooltip(Text.of("Enable or disable perma sneak"))
                    .setSaveConsumer(AFKFarmUtilsConfig.INSTANCE::setPermasneak)
                    .build());

            return builder.build();
        };
    }
}
