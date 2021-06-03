package io.github.lucaargolo.lifts.mixin;

import io.github.lucaargolo.lifts.common.entity.platform.PlatformEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(at = @At("HEAD"), method = "fall", cancellable = true)
    public void interceptFall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition, CallbackInfo ci){
        world.getOtherEntities(this, this.getBoundingBox().expand(0.0, 5.0, 0.0)).forEach(entity -> {
            if(entity instanceof PlatformEntity) {
                entity.fallDistance = 0f;
                ci.cancel();
            }
        });
    }

}
