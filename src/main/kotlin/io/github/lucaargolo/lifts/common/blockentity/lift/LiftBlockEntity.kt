package io.github.lucaargolo.lifts.common.blockentity.lift

import io.github.lucaargolo.lifts.common.block.lift.Lift
import io.github.lucaargolo.lifts.utils.SynchronizeableBlockEntity
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.property.Properties
import net.minecraft.util.ItemScatterer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

abstract class LiftBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState): SynchronizeableBlockEntity(type, pos, state) {

    private var prevReachableLifts = 0

    var lift: Lift? = null
    var liftName: String? = null
    var liftShaft: LiftShaft? = null

    private val facing: Direction
        get() = cachedState[Properties.HORIZONTAL_FACING]

    private val frontPos: BlockPos
        get() = pos.add(facing.vector)

    private var isPlatformHereCache: Boolean? = null

    val isPlatformHere: Boolean
        get() = isPlatformHereCache ?: let {
            val result = world?.getBlockState(frontPos)?.isAir?.not() ?: false
            isPlatformHereCache = result
            result
        }


    var ready = false

    fun resetPlatformCache() {
        isPlatformHereCache = null
        if(world?.isClient == false) sync()
    }

    override fun markRemoved() {
        super.markRemoved()
        liftShaft?.removeLift(this)
    }

    override fun markDirty() {
        super.markDirty()
        val reachableLifts = getReachableLifts()
        if(reachableLifts != prevReachableLifts) {
            liftShaft?.updateLift(this)
        }
        prevReachableLifts = reachableLifts
    }

    abstract fun getReachableLifts(): Int
    abstract fun preSendRequirements(distance: Int): LiftActionResult
    abstract fun postSendRequirements(distance: Int)

    override fun readNbt(tag: NbtCompound) {
        super.readNbt(tag)
        if(world?.isClient == true) {
            resetPlatformCache()
            liftShaft?.updateLift(this)
        }
        liftName = if(tag.contains("liftName")) tag.getString("liftName") else null
    }

    override fun writeNbt(tag: NbtCompound) {
        liftName?.let { tag.putString("liftName", it) }
        super.writeNbt(tag)
    }

    companion object {
        fun commonTick(world: World, pos: BlockPos, state: BlockState, entity: LiftBlockEntity) {
            if(entity.lift == null) {
                entity.lift = world.getBlockState(pos)?.block as? Lift
            }
            if(entity.liftShaft == null) {
                entity.liftShaft = world.let { LiftShaft.getOrCreate(it, pos) }
                if(entity.liftShaft?.facing != null && entity.facing != entity.liftShaft?.facing)  {
                    (world as? ServerWorld)?.let { serverWorld ->
                        val stacks = Block.getDroppedStacks(state, serverWorld, pos, entity)
                        stacks.forEach {
                            ItemScatterer.spawn(serverWorld, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), it)
                        }
                    }
                    world.setBlockState(pos, Blocks.AIR.defaultState)
                    return
                }
                entity.liftShaft?.addLift(entity)
            }
            val serverWorld = world as? ServerWorld ?: return
            if(serverWorld.isReceivingRedstonePower(pos)) {
                if(entity.ready && !entity.isPlatformHere) {
                    val actionResult = entity.liftShaft?.sendPlatformTo(serverWorld, entity, false)
                    entity.ready = actionResult?.isAccepted() ?: false
                }
            }else{
                entity.ready = true
            }
        }
    }

}