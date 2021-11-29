package io.github.lucaargolo.lifts.network

import io.github.lucaargolo.lifts.client.screen.ElectricLiftScreen
import io.github.lucaargolo.lifts.common.blockentity.lift.LiftBlockEntity
import io.github.lucaargolo.lifts.common.entity.platform.PlatformEntity
import io.github.lucaargolo.lifts.utils.ModIdentifier
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

object PacketCompendium {

    val SPAWN_PLATFORM_ENTITY = ModIdentifier("spawn_platform_entity")
    val SEND_PLATFORM_ENTITY = ModIdentifier("send_platform_entity")
    val RENAME_LIFT_ENTITY = ModIdentifier("rename_lift_entity")
    val UPDATE_ELECTRIC_LIFT_SCREEN_HANDLER = ModIdentifier("update_electric_lift_screen_handler")

    fun onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(UPDATE_ELECTRIC_LIFT_SCREEN_HANDLER) { client, _, buf, _ ->
            val long = buf.readLong()
            client.execute {
                (client.currentScreen as? ElectricLiftScreen)?.screenHandler?.energyStored = long
            }
        }
        ClientPlayNetworking.registerGlobalReceiver(SPAWN_PLATFORM_ENTITY) {client, handler, buf, _ ->
            val id = buf.readVarInt()
            val uuid = buf.readUuid()
            val x = buf.readDouble()
            val y = buf.readDouble()
            val z = buf.readDouble()
            val pitch = buf.readByte()
            val yaw = buf.readByte()

            val tag = buf.readNbt()
            val finalElevation = buf.readDouble()
            val speed = buf.readDouble()

            client.execute {
                val world = handler.world
                val entity = PlatformEntity(world)

                entity.updateTrackedPosition(x, y, z)
                entity.refreshPositionAfterTeleport(x, y, z)

                entity.pitch = pitch * 360 / 256.0f
                entity.yaw = yaw * 360 / 256.0f
                entity.id = id
                entity.uuid = uuid

                tag?.let { entity.readBlockMatrixFromTag(tag) }

                entity.initialElevation = y
                entity.finalElevation = finalElevation
                entity.platformSpeed = speed

                world.addEntity(id, entity)
            }
        }
    }

    fun onInitialize() {
        ServerPlayNetworking.registerGlobalReceiver(SEND_PLATFORM_ENTITY) {server, player, _, buf, _ ->
            val destination = buf.readBlockPos()
            server.execute {
                val destinyEntity = (player.world.getBlockEntity(destination) as? LiftBlockEntity) ?: return@execute
                if(destinyEntity.ready && !destinyEntity.isPlatformHere) {
                    destinyEntity.liftShaft?.sendPlatformTo(player.world, destinyEntity, false)
                }
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(RENAME_LIFT_ENTITY) { server, player, _, buf, _ ->
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