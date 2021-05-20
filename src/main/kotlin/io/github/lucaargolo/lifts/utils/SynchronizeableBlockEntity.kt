package io.github.lucaargolo.lifts.utils

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos

open class SynchronizeableBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState): BlockEntity(type, pos, state), BlockEntityClientSerializable {

    override fun fromClientTag(tag: NbtCompound) = readNbt(tag)

    override fun toClientTag(tag: NbtCompound) = writeNbt(tag)

}