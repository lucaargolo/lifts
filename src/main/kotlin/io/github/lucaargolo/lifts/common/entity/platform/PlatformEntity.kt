package io.github.lucaargolo.lifts.common.entity.platform

import io.github.lucaargolo.lifts.common.entity.EntityCompendium
import io.github.lucaargolo.lifts.network.PacketCompendium
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.MovementType
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtHelper
import net.minecraft.network.Packet
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ItemScatterer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.*

class PlatformEntity: Entity {

    private var collidingEntities: List<Entity>? = null
    var blockMatrix: Array<Array<BlockState?>?>? = null
    var platformSpeed = 1.0
    var initialElevation = 0.0
    var finalElevation = 0.0
    var lastProgress = -9999.0
    var sameLastProgress = 0.0

    constructor(entityType: EntityType<PlatformEntity>, world: World): super(entityType, world)

    constructor(world: World): super(EntityCompendium.PLATFORM_TYPE, world)

    constructor(pos1: BlockPos, pos2: BlockPos, world: World): this(world) {
        createBlockMatrix(pos1, pos2)
    }

    override fun updateTrackedPositionAndAngles(x: Double, y: Double, z: Double, yaw: Float, pitch: Float, interpolationSteps: Int, interpolate: Boolean) {
    }

    fun easeInOutSine(x: Double): Double {
        return -(cos(PI * x) - 1) / 2
    }

    override fun tick() {
        collidingEntities = this.world.getOtherEntities(this, this.boundingBox.expand(0.0, 0.3, 0.0))
        var progress = (pos.y-initialElevation)/(finalElevation - initialElevation)
        if(progress < 1.0) {
            val d = if(pos.y > finalElevation) -1 else 1
            val h = abs(finalElevation-initialElevation)
            val p = if(progress <= 0.5) progress else 1-progress
            val e = min(p*(1/(5/h)), 1.0)
            val vel = d*easeInOutSine(e)*(platformSpeed/2)
            val oldElevation = pos.y
            move(MovementType.SELF, Vec3d(0.0, (vel+(d*0.1))*0.5, 0.0))
            val elevationOffset = pos.y - oldElevation
            collidingEntities?.forEach {
                it.addVelocity(0.0, elevationOffset-it.velocity.y, 0.0)
            }
        }
        progress = (pos.y-initialElevation)/(finalElevation - initialElevation)
        if(progress == lastProgress) {
            if(sameLastProgress++ >= 3) {
                val yPos = removeBlockMatrix()
                collidingEntities?.forEach {
                    it.teleport(it.pos.x, yPos+1.0, it.pos.z)
                }
            }
        }else{
            lastProgress = progress
        }
        if(progress >= 1.0) {
            val yPos = removeBlockMatrix()
            collidingEntities?.forEach {
                it.teleport(it.pos.x, yPos+1.0, it.pos.z)
            }
        }
        super.tick()
    }

    private fun removeBlockMatrix(): Int {
        val yPos = round(pos.y).toInt()
        if(!world.isClient) {
            blockMatrix?.forEachIndexed { x, row ->
                row?.forEachIndexed { z, state ->
                    val pos = BlockPos(blockPos.x + x, yPos, blockPos.z + z)
                    if (world.getBlockState(pos).isAir) {
                        world.setBlockState(pos, state)
                    } else {
                        (world as? ServerWorld)?.let { serverWorld ->
                            val stacks = Block.getDroppedStacks(state, serverWorld, pos, null)
                            stacks.forEach {
                                ItemScatterer.spawn(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), it)
                            }
                        }
                    }
                }
            }
        }
        remove(RemovalReason.DISCARDED)
        return yPos
    }

    private fun createBlockMatrix(pos1: BlockPos, pos2: BlockPos) {
        val y = pos1.y

        val minX = min(pos1.x, pos2.x)
        val maxX = max(pos1.x, pos2.x)
        val minZ = min(pos1.z, pos2.z)
        val maxZ = max(pos1.z, pos2.z)

        val blockMatrix = arrayOfNulls<Array<BlockState?>>((maxX-minX)+1)
        for(x in (minX..maxX)) {
            val matrixRow = arrayOfNulls<BlockState>((maxZ-minZ)+1)
            for(z in (minZ..maxZ)) {
                val pos = BlockPos(x, y, z)
                val state = world.getBlockState(pos)
                matrixRow[z - minZ] = if(state.isFullCube(world, pos) && !state.hasBlockEntity()) {
                    world.setBlockState(pos, Blocks.AIR.defaultState)
                    state
                }else{
                    Blocks.AIR.defaultState
                }
            }
            blockMatrix[x-minX] = matrixRow
        }

        this.blockMatrix = blockMatrix
        createBoundingBox()
    }

    private fun createBoundingBox() {
        blockMatrix?.let { matrix ->
            val xSize = matrix.size
            val zSize = if(xSize > 0) matrix[0]?.size ?: 0 else 0
            this.boundingBox = Box(x - 0.5, y, z - 0.5, x + xSize - 0.5, y + 1.0, z + zSize - 0.5)
        }
    }

    override fun isCollidable() = true

    override fun calculateDimensions() { }

    override fun calculateBoundingBox(): Box {
        return Box(x - 0.5, y, z - 0.5, x + boundingBox.xLength - 0.5, y + boundingBox.yLength, z + boundingBox.zLength - 0.5)
    }

    override fun initDataTracker() { }

    fun writeBlockMatrixToTag(tag: NbtCompound) {
        tag.putInt("xSize", blockMatrix?.size ?: 0)
        tag.putInt("ySize", blockMatrix?.getOrNull(0)?.size ?: 0)
        blockMatrix?.forEachIndexed { x, row ->
            row?.forEachIndexed { z, state ->
                tag.put("block-$x$z", NbtHelper.fromBlockState(state))
            }
        }
    }

    override fun writeCustomDataToNbt(tag: NbtCompound) {
        writeBlockMatrixToTag(tag)
    }

    fun readBlockMatrixFromTag(tag: NbtCompound) {
        var x = 0
        var z = 0
        val blockMatrix = arrayOfNulls<Array<BlockState?>>(tag.getInt("xSize"))
        while(tag.contains("block-$x$z")) {
            val matrixRow = blockMatrix[x] ?: arrayOfNulls(tag.getInt("ySize"))
            matrixRow[z] = NbtHelper.toBlockState(tag.getCompound("block-$x$z"))
            if(blockMatrix[x] == null) {
                blockMatrix[x] = matrixRow
            }
            z++
            if(!tag.contains("block-$x$z")) {
                z = 0
                x++
            }
        }
        this.blockMatrix = blockMatrix
        createBoundingBox()
    }

    override fun readCustomDataFromNbt(tag: NbtCompound) {
        readBlockMatrixFromTag(tag)
    }

    override fun createSpawnPacket(): Packet<*> {
        val buf = PacketByteBufs.create()
        buf.writeVarInt(id)
        buf.writeUuid(uuid)
        buf.writeDouble(x)
        buf.writeDouble(y)
        buf.writeDouble(z)
        buf.writeByte(MathHelper.floor(pitch * 256.0f / 360.0f))
        buf.writeByte(MathHelper.floor(yaw * 256.0f / 360.0f))

        val tag = NbtCompound()
        writeBlockMatrixToTag(tag)
        buf.writeNbt(tag)

        buf.writeDouble(finalElevation)
        buf.writeDouble(platformSpeed)

        return ServerPlayNetworking.createS2CPacket(PacketCompendium.SPAWN_PLATFORM_ENTITY, buf)
    }

}