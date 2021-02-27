package io.github.lucaargolo.lifts.mixin;

import io.github.lucaargolo.lifts.common.block.screen.ScreenBlockHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Shadow @Nullable public HitResult crosshairTarget;
    @Shadow @Nullable public ClientWorld world;

//    @Inject(at = @At("HEAD"), method = "openScreen", cancellable = true)
//    public void openScreen(Screen screen, CallbackInfo info) {
//        boolean shouldCancel = ScreenBlockHandler.INSTANCE.openScreenHook(crosshairTarget, world, screen);
//        if(shouldCancel) info.cancel();
//    }

    @Inject(at = @At("TAIL"), method = "<init>")
    public void init(RunArgs args, CallbackInfo info) {
        ScreenBlockHandler.INSTANCE.setupFramebuffer(128, 128);
    }

}
