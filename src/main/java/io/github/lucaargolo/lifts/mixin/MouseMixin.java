package io.github.lucaargolo.lifts.mixin;

import io.github.lucaargolo.lifts.common.block.screen.ScreenBlockHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {

    @Shadow @Final private MinecraftClient client;

    @Inject(at = @At("HEAD"), method = "onMouseScroll", cancellable = true)
    public void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo info) {
        boolean shouldCancel = ScreenBlockHandler.INSTANCE.mouseScrollHook(client.crosshairTarget, client.world, vertical);
        if(shouldCancel) info.cancel();
    }

}
