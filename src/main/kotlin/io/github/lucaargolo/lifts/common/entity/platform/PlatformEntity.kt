package io.github.lucaargolo.lifts.common.entity.platform

import io.github.lucaargolo.lifts.common.entity.EntityCompendium
import io.github.lucaargolo.lifts.network.PacketCompendium
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtHelper
import net.minecraft.network.Packet
import net.minecraft.util.math.*
import net.minecraft.world.World
import kotlin.math.*

class PlatformEntity: Entity {

    private var collidingEntities: List<Entity>? = null
    var blockMatrix: Array<Array<BlockState?>?>? = null
    var initialElevation = 0.0
    var finalElevation = 0.0

    constructor(entityType: EntityType<PlatformEntity>, world: World): super(entityType, world)

    constructor(world: World): super(EntityCompendium.PLATFORM_TYPE, world)

    constructor(pos1: BlockPos, pos2: BlockPos, world: World): this(world) {
        createBlockMatrix(pos1, pos2)
    }

    override fun updatePosition(x: Double, y: Double, z: Double) {
        setPos(x, y, z)
        this.boundingBox = Box(x - 0.5, y, z - 0.5, x + boundingBox.xLength - 0.5, y + boundingBox.yLength, z + boundingBox.zLength - 0.5)
    }

    fun easeInOutSine(x: Double): Double {
        return -(cos(PI * x) - 1) / 2
    }

    override fun tick() {
        val newCollidingEntities = this.world.getEntitiesByType<Entity>(null, this.boundingBox.expand(0.0, 0.3, 0.0)) {it !is PlatformEntity}
        collidingEntities?.forEach {
            if(!newCollidingEntities.contains(it)) {
                it.fallDistance = 0f
                it.velocity = this.velocity
            }
        }
        collidingEntities = newCollidingEntities
        var progress = (pos.y-initialElevation)/(finalElevation - initialElevation)
        if(progress < 1.0) {
            val d = if(pos.y > finalElevation) -1 else 1
            val p = if(progress <= 0.5) progress else 1-progress
            val vel = d*easeInOutSine(p)
            val oldElevation = pos.y
            move(MovementType.SELF, Vec3d(0.0, vel+(d*0.1), 0.0))
            val elevationOffset = pos.y - oldElevation
            collidingEntities?.forEach {
                it.addVelocity(0.0, elevationOffset-it.velocity.y, 0.0)
            }
        }
        progress = (pos.y-initialElevation)/(finalElevation - initialElevation)
        if(progress >= 1.0) {
            val yPos = removeBlockMatrix()
            collidingEntities?.forEach {
                it.fallDistance = 0f
                it.teleport(it.pos.x, yPos+1.0, it.pos.z)
            }
        }
        super.tick()
    }

    private fun removeBlockMatrix(): Int {
        val yPos = round(pos.y).toInt()
        blockMatrix?.forEachIndexed { x, row ->
            row?.forEachIndexed { z, state ->
                world.setBlockState(BlockPos(blockPos.x+x, yPos, blockPos.z+z), state)
            }
        }
        remove()
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
                matrixRow[z-minZ] = world.getBlockState(pos)
                world.setBlockState(pos, Blocks.AIR.defaultState)
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

    override fun initDataTracker() { }

    override fun moveToBoundingBoxCenter() {
        setPos(x, boundingBox.minY, z)
    }

    fun writeBlockMatrixToTag(tag: CompoundTag) {
        tag.putInt("xSize", blockMatrix?.size ?: 0)
        tag.putInt("ySize", blockMatrix?.getOrNull(0)?.size ?: 0)
        blockMatrix?.forEachIndexed { x, row ->
            row?.forEachIndexed { z, state ->
                tag.put("block-$x$z", NbtHelper.fromBlockState(state))
            }
        }
    }

    override fun writeCustomDataToTag(tag: CompoundTag) {
        writeBlockMatrixToTag(tag)
    }

    fun readBlockMatrixFromTag(tag: CompoundTag) {
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

    override fun readCustomDataFromTag(tag: CompoundTag) {
        readBlockMatrixFromTag(tag)
    }

    override fun createSpawnPacket(): Packet<*> {
        val buf = PacketByteBufs.create()
        buf.writeVarInt(entityId)
        buf.writeUuid(uuid)
        buf.writeDouble(x)
        buf.writeDouble(y)
        buf.writeDouble(z)
        buf.writeByte(MathHelper.floor(pitch * 256.0f / 360.0f))
        buf.writeByte(MathHelper.floor(yaw * 256.0f / 360.0f))

        val tag = CompoundTag()
        writeBlockMatrixToTag(tag)
        buf.writeCompoundTag(tag)

        buf.writeDouble(finalElevation)

        return ServerPlayNetworking.createS2CPacket(PacketCompendium.SPAWN_PLATFORM_ENTITY, buf)
    }

}