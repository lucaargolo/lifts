package io.github.lucaargolo.lifts.network

import io.github.lucaargolo.lifts.common.entity.platform.PlatformEntity
import io.github.lucaargolo.lifts.utils.ModIdentifier
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.util.math.BlockPos

object PacketCompendium {

    val SPAWN_PLATFORM_ENTITY = ModIdentifier("spawn_platform_entity")

    fun onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(SPAWN_PLATFORM_ENTITY) {client, handler, buf, sender ->
            val id = buf.readVarInt()
            val uuid = buf.readUuid()
            val x = buf.readDouble()
            val y = buf.readDouble()
            val z = buf.readDouble()
            val pitch = buf.readByte()
            val yaw = buf.readByte()

            val tag = buf.readCompoundTag()

            client.execute {
                val world = handler.world
                val entity = PlatformEntity(world)

                entity.updateTrackedPosition(x, y, z)
                entity.refreshPositionAfterTeleport(x, y, z)

                entity.pitch = pitch * 360 / 256.0f
                entity.yaw = yaw * 360 / 256.0f
                entity.entityId = id
                entity.uuid = uuid

                tag?.let { entity.readBlockMatrixFromTag(tag) }

                world.addEntity(id, entity)
            }
        }
    }

    fun onInitialize() {

    }



}