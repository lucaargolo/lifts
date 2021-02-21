package io.github.lucaargolo.lifts.network

import io.github.lucaargolo.lifts.common.blockentity.lift.LiftBlockEntity
import io.github.lucaargolo.lifts.common.entity.platform.PlatformEntity
import io.github.lucaargolo.lifts.utils.ModIdentifier
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

object PacketCompendium {

    val SPAWN_PLATFORM_ENTITY = ModIdentifier("spawn_platform_entity")
    val SEND_PLATFORM_ENTITY = ModIdentifier("send_platform_entity")
    val RENAME_LIFT_ENTITY = ModIdentifier("rename_lift_entity")

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
            val finalElevation = buf.readDouble()

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

                entity.initialElevation = y
                entity.finalElevation = finalElevation

                world.addEntity(id, entity)
            }
        }
    }

    fun onInitialize() {
        ServerPlayNetworking.registerGlobalReceiver(SEND_PLATFORM_ENTITY) {server, player, handler, buf, sender ->
            val destination = buf.readBlockPos()
            server.execute {
                val destinyEntity = (player.world.getBlockEntity(destination) as? LiftBlockEntity) ?: return@execute
                if(destinyEntity.ready && destinyEntity.isShaftValid && !destinyEntity.isPlatformHere) {
                    destinyEntity.liftShaft?.firstOrNull{ it.isPlatformHere }?.sendPlatformTo(player.world as ServerWorld, destinyEntity)
                }
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(RENAME_LIFT_ENTITY) { server, player, handler, buf, sender ->
            val entityPos = buf.readBlockPos()
            val newEntityName = buf.readString(32767)
            server.execute {
                (player.world.getBlockEntity(entityPos) as? LiftBlockEntity)?.let {
                    if(newEntityName.isEmpty()) {
                        it.liftName = null
                    }else{
                        it.liftName = newEntityName
                    }
                    it.sync()
                }
            }
        }
    }



}