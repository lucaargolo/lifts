package io.github.lucaargolo.lifts.common.blockentity.lift

import io.github.lucaargolo.lifts.common.block.lift.Lift
import io.github.lucaargolo.lifts.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.lifts.common.entity.platform.PlatformEntity
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.property.Properties
import net.minecraft.util.Tickable
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

class LiftBlockEntity(lift: Lift?): BlockEntity(BlockEntityCompendium.LIFT_TYPE), Tickable {

    private var liftShaft: LinkedHashSet<LiftBlockEntity>? = null

    private val facing: Direction
        get() = cachedState[Properties.HORIZONTAL_FACING]

    private val frontPos: BlockPos
        get() = pos.add(facing.vector)

    private val isPlatformHere: Boolean
        get() {
            val world = world ?: return false
            return !world.getBlockState(frontPos).isAir
        }

    private val isShaftValid: Boolean
        get() {
            var activePlatforms = 0
            liftShaft?.forEach { elevator ->
                if(elevator.isPlatformHere) {
                    activePlatforms++
                }
            }
            return activePlatforms == 1
        }

    override fun markRemoved() {
        liftShaft?.remove(this)
    }

    override fun tick() {
        val world: ServerWorld = world as? ServerWorld ?: return
        if(liftShaft == null) {
            val set = LiftHelper.getOrCreateLiftShaft(pos)
            set.add(this)
            liftShaft = set
        }
        if(world.isReceivingRedstonePower(pos) && isShaftValid && !isPlatformHere) {
            val success = liftShaft?.firstOrNull{ it.isPlatformHere }?.sendPlatformTo(world, this) ?: false
        }
    }

    private fun sendPlatformTo(world: ServerWorld, destination: LiftBlockEntity): Boolean {
        val block = world.getBlockState(frontPos).block
        val triple = floodfillPlatformBlocks(world, block, frontPos, linkedSetOf(), frontPos, frontPos)
        val platformBlocks = triple.first
        return if(platformBlocks.count() > 25) {
            false
        }else{
            val platform = PlatformEntity(triple.second, triple.third, world)
            platform.updatePosition(pos.x+0.5, pos.y+0.5, pos.z+0.5)
            world.spawnEntity(platform)
        }
    }

    private fun floodfillPlatformBlocks(world: ServerWorld, block: Block, pos: BlockPos, set: LinkedHashSet<BlockPos>, corner1: BlockPos, corner2: BlockPos): Triple<LinkedHashSet<BlockPos>, BlockPos, BlockPos> {
        var newCorner1 = corner1
        var newCorner2 = corner2
        if(world.getBlockState(pos).block == block && set.count() <= 25) {
            if(pos.x > newCorner1.x || pos.y > newCorner1.y) {
                newCorner1 = pos
            }
            if(pos.x < newCorner2.x || pos.y < newCorner2.y) {
                newCorner2 = pos
            }
            set.add(pos)
        } else {
            return Triple(set, newCorner1, newCorner2)
        }
        if(!set.contains(pos.north())) {
            val triple = floodfillPlatformBlocks(world, block, pos.north(), set, newCorner1, newCorner2)
            if(triple.second.x > newCorner1.x || triple.second.y > newCorner1.y) {
                newCorner1 = triple.second
            }
            if(triple.third.x < newCorner2.x || triple.third.y < newCorner2.y) {
                newCorner2 = triple.third
            }
        }
        if(!set.contains(pos.south())) {
            val triple = floodfillPlatformBlocks(world, block, pos.south(), set, newCorner1, newCorner2)
            if(triple.second.x > newCorner1.x || triple.second.y > newCorner1.y) {
                newCorner1 = triple.second
            }
            if(triple.third.x < newCorner2.x || triple.third.y < newCorner2.y) {
                newCorner2 = triple.third
            }
        }
        if(!set.contains(pos.east())) {
            val triple = floodfillPlatformBlocks(world, block, pos.east(), set, newCorner1, newCorner2)
            if(triple.second.x > newCorner1.x || triple.second.y > newCorner1.y) {
                newCorner1 = triple.second
            }
            if(triple.third.x < newCorner2.x || triple.third.y < newCorner2.y) {
                newCorner2 = triple.third
            }
        }
        if(!set.contains(pos.west())) {
            val triple = floodfillPlatformBlocks(world, block, pos.west(), set, newCorner1, newCorner2)
            if(triple.second.x > newCorner1.x || triple.second.y > newCorner1.y) {
                newCorner1 = triple.second
            }
            if(triple.third.x < newCorner2.x || triple.third.y < newCorner2.y) {
                newCorner2 = triple.third
            }
        }
        return Triple(set, newCorner1, newCorner2)
    }
}