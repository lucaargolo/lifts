package io.github.lucaargolo.lifts.common.blockentity.lift

import io.github.lucaargolo.lifts.utils.SynchronizeableBlockEntity
import io.github.lucaargolo.lifts.common.block.lift.Lift
import io.github.lucaargolo.lifts.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.lifts.common.entity.platform.PlatformEntity
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.property.Properties
import net.minecraft.util.ItemScatterer
import net.minecraft.util.Tickable
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World

abstract class LiftBlockEntity(type: BlockEntityType<*>): SynchronizeableBlockEntity(type), Tickable {

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
    }

    override fun markDirty() {
        liftShaft?.markEntityDirty(this)
    }

    override fun markRemoved() {
        liftShaft?.removeLift(this)
    }

    abstract fun preSendRequirements(distance: Int): LiftActionResult
    abstract fun postSendRequirements(distance: Int)

    override fun tick() {
        if(lift == null) {
            lift = world?.getBlockState(pos)?.block as? Lift
        }
        if(liftShaft == null) {
            liftShaft = LiftShaft.getOrCreate(pos)
            if(liftShaft?.facing != null && facing != liftShaft?.facing)  {
                (world as? ServerWorld)?.let { serverWorld ->
                    val stacks = Block.getDroppedStacks(cachedState, serverWorld, pos, this)
                    stacks.forEach {
                        ItemScatterer.spawn(serverWorld, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), it)
                    }
                }
                world?.setBlockState(pos, Blocks.AIR.defaultState)
                return
            }
            liftShaft?.addLift(this)
        }
        val world = world as? ServerWorld ?: return
        if(world.isReceivingRedstonePower(pos)) {
            if(ready && !isPlatformHere) {
                val actionResult = liftShaft?.sendPlatformTo(world, this, false)
                ready = actionResult?.isAccepted() ?: false
            }
        }else{
            ready = true
        }
    }

    override fun fromTag(state: BlockState?, tag: CompoundTag) {
        super.fromTag(state, tag)
        liftName = if(tag.contains("liftName")) tag.getString("liftName") else null
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        liftName?.let { tag.putString("liftName", it) }
        return super.toTag(tag)
    }

}