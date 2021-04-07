package io.github.lucaargolo.lifts.common.blockentity.lift

import io.github.lucaargolo.lifts.common.block.BlockCompendium
import io.github.lucaargolo.lifts.common.block.lift.Lift
import io.github.lucaargolo.lifts.common.entity.platform.PlatformEntity
import net.minecraft.block.BlockState
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3i
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.World
import java.util.*
import kotlin.Comparator
import kotlin.collections.LinkedHashMap
import kotlin.collections.LinkedHashSet

class LiftShaft private constructor(val key: RegistryKey<World>, val x: Int, val z: Int) {

    private val blockEntitySet: SortedSet<LiftBlockEntity> = sortedSetOf(Comparator { a, b -> b.pos.y - a.pos.y})
    private val platformedLifts: LinkedHashSet<LiftBlockEntity> = linkedSetOf()
    private val simulationCache: LinkedHashMap<LiftBlockEntity, LiftActionResult> = linkedMapOf()

    var facing: Direction? = null
    val lifts: SortedSet<LiftBlockEntity>
        get() = blockEntitySet
    var size = 0

    fun addLift(lift: LiftBlockEntity): Boolean {
        simulationCache.clear()
        return if(blockEntitySet.add(lift)) {
            if(facing == null) facing = lift.cachedState[Properties.HORIZONTAL_FACING]
            if(lift.isPlatformHere) platformedLifts.add(lift)
            blockEntitySet.forEach {
                if(it.world?.isClient == false) it.sync()
            }
            size++
            true
        }else{
            false
        }
    }
    fun removeLift(lift: LiftBlockEntity): Boolean {
        simulationCache.clear()
        return if (blockEntitySet.remove(lift)) {
            if(lift.isPlatformHere) platformedLifts.remove(lift)
            blockEntitySet.forEach {
                if(it.world?.isClient == false) it.sync()
            }
            size--
            true
        } else {
            false
        }
    }

    fun updateLift(lift: LiftBlockEntity) {
        simulationCache.clear()
        if(lift.isPlatformHere) {
            platformedLifts.add(lift)
        }else{
            platformedLifts.remove(lift)
        }
        blockEntitySet.forEach {
            if(it.world?.isClient == false) it.sync()
        }
    }

    fun sendPlatformTo(world: World, destination: LiftBlockEntity, simulation: Boolean): LiftActionResult {
        val cachedResult = simulationCache[destination]
        if(platformedLifts.size > 1) {
            return LiftActionResult.TOO_MANY_PLATFORMS
        } else if(platformedLifts.size < 1) {
            return LiftActionResult.NO_PLATFORM
        }else if(simulation && cachedResult != null) {
            return cachedResult
        }

        val platformedEntity = platformedLifts.first()
        val lift = platformedEntity.lift ?: (BlockCompendium.STIRLING_LIFT as Lift)
        val frontPos = platformedEntity.pos.add(facing?.vector ?: Vec3i.ZERO)
        val state = world.getBlockState(frontPos)
        val distance = MathHelper.abs(destination.pos.y - frontPos.y)

        if(!state.isFullCube(world, frontPos) || state.block.hasBlockEntity()) {
            return cacheAndReturn(LiftActionResult.INVALID_PLATFORM, destination, simulation)
        }else if(distance > lift.liftConfig.platformRange) {
            return cacheAndReturn(LiftActionResult.NO_RANGE, destination, simulation)
        }

        val triple = floodfillPlatformBlocks(world, state, frontPos, linkedSetOf(), frontPos, frontPos)
        val platformBlocks = triple.first

        if(platformBlocks.count() > 25) {
            return cacheAndReturn(LiftActionResult.INVALID_PLATFORM, destination, simulation)
        }

        val preRequirements = platformedEntity.preSendRequirements(distance)
        if(!preRequirements.isAccepted()) {
            return cacheAndReturn(preRequirements, destination, simulation)
        }

        if(!simulation) {
            val platform = PlatformEntity(triple.second, triple.third, world)
            val spawnPos = triple.third
            platform.updatePosition(spawnPos.x + 0.5, spawnPos.y + 0.0, spawnPos.z + 0.5)
            platform.speed = lift.liftConfig.platformSpeed
            platform.initialElevation = spawnPos.y + 0.0
            platform.finalElevation = destination.pos.y + 0.0
            if(world.spawnEntity(platform)) {
                simulationCache.clear()
                platformedEntity.postSendRequirements(distance)
            }
        }

        return LiftActionResult.SUCCESSFUL
    }

    private fun cacheAndReturn(result: LiftActionResult, destination: LiftBlockEntity, simulation: Boolean): LiftActionResult {
        if(simulation) {
            simulationCache[destination] = result
        }
        return result
    }

    private fun floodfillPlatformBlocks(world: World, state: BlockState, pos: BlockPos, set: LinkedHashSet<BlockPos>, corner1: BlockPos, corner2: BlockPos): Triple<LinkedHashSet<BlockPos>, BlockPos, BlockPos> {
        var newCorner1 = corner1
        var newCorner2 = corner2
        if(!set.contains(pos) && world.getBlockState(pos) == state && set.count() <= 25) {
            set.add(pos)
            if(pos.x > newCorner1.x || pos.z > newCorner1.z) {
                newCorner1 = pos
            }
            if(pos.x < newCorner2.x || pos.z < newCorner2.z) {
                newCorner2 = pos
            }
            Direction.values().iterator().forEach {
                if(it.axis != Direction.Axis.Y) {
                    val triple = floodfillPlatformBlocks(world, state, pos.add(it.vector), set, newCorner1, newCorner2)
                    if(triple.second.x > newCorner1.x || triple.second.z > newCorner1.z) {
                        newCorner1 = triple.second
                    }
                    if(triple.third.x < newCorner2.x || triple.third.z < newCorner2.z) {
                        newCorner2 = triple.third
                    }
                }
            }
        }
        return Triple(set, newCorner1, newCorner2)
    }

    override fun hashCode(): Int {
        return key.value.toString().hashCode() xor x xor z
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is LiftShaft) {
            return (key == other.key && x == other.x && z == other.z)
        }
        return false
    }

    companion object {
        private val serverLiftShaftSet: LinkedHashSet<LiftShaft> = linkedSetOf()
        private val clientLiftShaftSet: LinkedHashSet<LiftShaft> = linkedSetOf()

        private fun getEnvironmentLiftSet(world: World): LinkedHashSet<LiftShaft> {
            return if(world.isClient) {
                clientLiftShaftSet
            }else {
                serverLiftShaftSet
            }
        }

        fun getOrCreate(world: World, pos: BlockPos): LiftShaft = getEnvironmentLiftSet(world).find { it.key == world.registryKey && it.x == pos.x && it.z == pos.z } ?: let {
            val newLiftShaft = LiftShaft(world.registryKey, pos.x, pos.z)
            getEnvironmentLiftSet(world).add(newLiftShaft)
            newLiftShaft
        }

        fun tickClient() {
            val iterator = clientLiftShaftSet.iterator()
            while(iterator.hasNext()) {
                val shaft = iterator.next()
                if(shaft.blockEntitySet.isEmpty()) {
                    iterator.remove()
                }
            }
        }

        fun tickServer() {
            val iterator = serverLiftShaftSet.iterator()
            while(iterator.hasNext()) {
                val shaft = iterator.next()
                if(shaft.blockEntitySet.isEmpty()) {
                    iterator.remove()
                }
            }
        }

        fun clearClient() {
            clientLiftShaftSet.clear()
        }

        fun clearServer() {
            serverLiftShaftSet.clear()
        }
    }

}