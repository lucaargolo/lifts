package io.github.lucaargolo.lifts.mixin;

import io.github.lucaargolo.lifts.utils.BackgroundRendererCache;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {

    @Inject(at = @At("HEAD"), method = "applyFog")
    private static void applyFogHook(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, CallbackInfo ci) {
        BackgroundRendererCache.INSTANCE.setCamera(camera);
        BackgroundRendererCache.INSTANCE.setFogType(fogType);
        BackgroundRendererCache.INSTANCE.setViewDistance(viewDistance);
        BackgroundRendererCache.INSTANCE.setThickFog(thickFog);
    }
}
