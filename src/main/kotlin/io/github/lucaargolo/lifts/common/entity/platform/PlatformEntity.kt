package io.github.lucaargolo.lifts.common.entity.platform

import io.github.lucaargolo.lifts.common.entity.EntityCompendium
import io.github.lucaargolo.lifts.network.PacketCompendium
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.impl.screenhandler.client.ClientNetworking
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.*
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtHelper
import net.minecraft.network.Packet
import net.minecraft.util.math.*
import net.minecraft.world.World
import kotlin.math.max
import kotlin.math.min

class PlatformEntity: Entity {

    var blockMatrix: Array<Array<BlockState?>?>? = null

    constructor(entityType: EntityType<PlatformEntity>, world: World): super(entityType, world)

    constructor(world: World): super(EntityCompendium.PLATFORM_TYPE, world)

    constructor(pos1: BlockPos, pos2: BlockPos, world: World): super(EntityCompendium.PLATFORM_TYPE, world) {
        createBlockMatrix(pos1, pos2, true)
    }

    override fun updatePosition(x: Double, y: Double, z: Double) {
        setPos(x, y, z)
        this.boundingBox = Box(x - 0.5, y, z - 0.5, x + boundingBox.xLength - 0.5, y + boundingBox.yLength, z + boundingBox.zLength - 0.5)
    }

    override fun tick() {
        val validEntities = world.getEntitiesByType<Entity>(null, this.boundingBox.expand(0.5)) { it.isCollidable && it != this}
        validEntities.forEach {
            //it.move(MovementType.SELF, Vec3d(0.0, 1.0, 0.0))
            //it.updatePosition(it.pos.x, it.pos.y+0.005, it.pos.z)
        }
        //move(MovementType.SELF, Vec3d(0.0, 1.0, 0.0))
        //updatePosition(pos.x, pos.y+0.005, pos.z)
        super.tick()
    }

    override fun setBoundingBox(boundingBox: Box?) {
        super.setBoundingBox(boundingBox)
    }

    override fun calculateDimensions() {

    }

    private fun createBlockMatrix(pos1: BlockPos, pos2: BlockPos, destroy: Boolean) {
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
                if(destroy) world.setBlockState(pos, Blocks.AIR.defaultState)
            }
            blockMatrix[x-minX] = matrixRow
        }

        this.blockMatrix = blockMatrix
        this.boundingBox = Box(minX+0.0, y+0.0, minZ+0.0, maxX+1.0, y+1.0, maxZ+1.0)
    }

    override fun isCollidable() = true

    override fun initDataTracker() {

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

        return ServerPlayNetworking.createS2CPacket(PacketCompendium.SPAWN_PLATFORM_ENTITY, buf)
    }

}