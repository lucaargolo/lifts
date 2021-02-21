package io.github.lucaargolo.lifts.common.blockentity.lift

import io.github.lucaargolo.lifts.utils.SynchronizeableBlockEntity
import io.github.lucaargolo.lifts.common.block.lift.Lift
import io.github.lucaargolo.lifts.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.lifts.common.entity.platform.PlatformEntity
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.property.Properties
import net.minecraft.util.Tickable
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class LiftBlockEntity(val lift: Lift?): SynchronizeableBlockEntity(BlockEntityCompendium.LIFT_TYPE), Tickable {

    var liftName: String? = null
    var liftShaft: LinkedHashSet<LiftBlockEntity>? = null

    private val facing: Direction
        get() = cachedState[Properties.HORIZONTAL_FACING]

    private val frontPos: BlockPos
        get() = pos.add(facing.vector)

    val isPlatformHere: Boolean
        get() {
            val world = world ?: return false
            return !world.getBlockState(frontPos).isAir
        }

    val isShaftValid: Boolean
        get() {
            var activePlatforms = 0
            liftShaft?.forEach { elevator ->
                if(elevator.isPlatformHere) {
                    activePlatforms++
                }
            }
            return activePlatforms == 1
        }

    var ready = false

    override fun markRemoved() {
        liftShaft?.remove(this)
        if(world?.isClient != true) {
            liftShaft?.forEach {
                it.sync()
            }
        }
    }

    override fun tick() {
        val world: ServerWorld = world as? ServerWorld ?: return
        if(liftShaft == null) {
            val set = LiftHelper.getOrCreateLiftShaft(pos)
            set.add(this)
            liftShaft = set
            liftShaft?.forEach {
                it.sync()
            }
        }
        if(world.isReceivingRedstonePower(pos)) {
            if(ready && isShaftValid && !isPlatformHere) {
                ready = liftShaft?.firstOrNull{ it.isPlatformHere }?.sendPlatformTo(world, this) ?: false
            }
        }else{
            ready = true
        }
    }

    fun sendPlatformTo(world: ServerWorld, destination: LiftBlockEntity): Boolean {
        val block = world.getBlockState(frontPos).block
        val triple = floodfillPlatformBlocks(world, block, frontPos, linkedSetOf(), frontPos, frontPos)
        val platformBlocks = triple.first
        return if(platformBlocks.count() > 25) {
            false
        }else{
            val platform = PlatformEntity(triple.second, triple.third, world)
            val spawnPos = triple.third
            platform.updatePosition(spawnPos.x+0.5, spawnPos.y+0.0, spawnPos.z+0.5)
            platform.initialElevation = spawnPos.y+0.0
            platform.finalElevation = destination.pos.y+0.0
            world.spawnEntity(platform)
        }
    }

    private fun floodfillPlatformBlocks(world: ServerWorld, block: Block, pos: BlockPos, set: LinkedHashSet<BlockPos>, corner1: BlockPos, corner2: BlockPos): Triple<LinkedHashSet<BlockPos>, BlockPos, BlockPos> {
        var newCorner1 = corner1
        var newCorner2 = corner2
        if(!set.contains(pos) && world.getBlockState(pos).block == block && set.count() <= 25) {
            set.add(pos)
            if(pos.x > newCorner1.x || pos.z > newCorner1.z) {
                newCorner1 = pos
            }
            if(pos.x < newCorner2.x || pos.z < newCorner2.z) {
                newCorner2 = pos
            }
            Direction.values().iterator().forEach {
                if(it.axis != Direction.Axis.Y) {
                    val triple = floodfillPlatformBlocks(world, block, pos.add(it.vector), set, newCorner1, newCorner2)
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

    override fun fromTag(state: BlockState?, tag: CompoundTag) {
        super.fromTag(state, tag)
        liftName = if(tag.contains("liftName")) tag.getString("liftName") else null
    }

    override fun fromClientTag(tag: CompoundTag) {
        liftShaft = linkedSetOf()
        val shaftLongArray = tag.getLongArray("shaft")
        shaftLongArray.forEach { long ->
            val pos = BlockPos.fromLong(long)
            val entity = world?.getBlockEntity(pos) as? LiftBlockEntity
            entity?.let { liftShaft?.add(it) }
        }
        fromTag(lift?.defaultState, tag)
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        liftName?.let { tag.putString("liftName", it) }
        return super.toTag(tag)
    }

    override fun toClientTag(tag: CompoundTag): CompoundTag {
        val shaftLongArray = liftShaft?.map{it.pos.asLong()}?.toLongArray() ?: longArrayOf()
        tag.putLongArray("shaft", shaftLongArray)
        return toTag(tag)
    }
}