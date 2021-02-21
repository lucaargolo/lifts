package io.github.lucaargolo.lifts.utils

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.nbt.CompoundTag

open class SynchronizeableBlockEntity(type: BlockEntityType<*>): BlockEntity(type), BlockEntityClientSerializable {

    override fun fromClientTag(tag: CompoundTag) = fromTag(cachedState.block.defaultState, tag)

    override fun toClientTag(tag: CompoundTag) = toTag(tag)

}