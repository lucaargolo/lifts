package io.github.lucaargolo.lifts.client

import io.github.lucaargolo.lifts.client.render.bakedmodel.BakedModelCompendium
import io.github.lucaargolo.lifts.client.render.blockentity.BlockEntityRendererCompendium
import io.github.lucaargolo.lifts.client.render.entity.EntityRendererCompendium
import io.github.lucaargolo.lifts.common.block.BlockCompendium
import io.github.lucaargolo.lifts.common.blockentity.lift.LiftBlockEntity
import io.github.lucaargolo.lifts.common.blockentity.lift.LiftShaft
import io.github.lucaargolo.lifts.common.containers.ScreenHandlerCompendium
import io.github.lucaargolo.lifts.compat.OptifineShadersCompat
import io.github.lucaargolo.lifts.network.PacketCompendium
import io.github.lucaargolo.lifts.utils.LateTooltipHolder
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.render.RenderLayer

class LiftsClient: ClientModInitializer {
    override fun onInitializeClient() {
        PacketCompendium.onInitializeClient()
        ScreenHandlerCompendium.onInitializeClient()
        BlockEntityRendererCompendium.initialize()
        EntityRendererCompendium.initialize()
        BakedModelCompendium.initialize()
        LateTooltipHolder.onInitializeClient()
        OptifineShadersCompat.initialize()
        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            LiftShaft.clearClient()
        }
        ClientTickEvents.END_CLIENT_TICK.register {
            LiftShaft.tickClient()
        }
        ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register { blockEntity, _ ->
            (blockEntity as? LiftBlockEntity)?.let { it.liftShaft?.removeLift(it) }
        }
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutoutMipped(),
            BlockCompendium.ELECTRIC_LIFT_MK1,
            BlockCompendium.ELECTRIC_LIFT_MK2,
            BlockCompendium.ELECTRIC_LIFT_MK3,
            BlockCompendium.ELECTRIC_LIFT_MK4,
            BlockCompendium.ELECTRIC_LIFT_MK5,
            BlockCompendium.COAL_STRUCTURE,
            BlockCompendium.IRON_STRUCTURE,
            BlockCompendium.GOLD_STRUCTURE,
            BlockCompendium.DIAMOND_STRUCTURE,
            BlockCompendium.EMERALD_STRUCTURE,
            BlockCompendium.NETHERITE_STRUCTURE
        )
    }
}