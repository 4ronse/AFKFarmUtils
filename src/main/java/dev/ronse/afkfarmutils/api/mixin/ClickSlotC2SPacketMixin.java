package dev.ronse.afkfarmutils.api.mixin;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClickSlotC2SPacket.class)
public abstract class ClickSlotC2SPacketMixin {
    @Unique
    private static final Logger LOGGER = LogUtils.getLogger();

    @Inject(method = "<init>(IIIILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/item/ItemStack;Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;)V", at = @At("RETURN"))
    void onInit(int syncId, int revision, int slot, int button, SlotActionType actionType, ItemStack stack, Int2ObjectMap<ItemStack> modifiedStacks, CallbackInfo ci) {
        LOGGER.info("ClickSlotC2SPacket initialized with syncId: {}, revision: {}, slot: {}, button: {}, actionType: {}, stack: {}, modifiedStacks: {}",
            syncId, revision, slot, button, actionType, stack, modifiedStacks
        );
    }
}
