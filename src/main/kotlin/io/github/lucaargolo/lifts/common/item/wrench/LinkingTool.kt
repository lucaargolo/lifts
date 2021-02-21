package io.github.lucaargolo.lifts.common.item.wrench

import io.github.lucaargolo.lifts.utils.Linkable
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.text.LiteralText
import net.minecraft.util.ActionResult
import net.minecraft.util.math.BlockPos

class LinkingTool(settings: Settings): Item(settings) {

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val player = context.player ?: return super.useOnBlock(context)
        val stack = context.stack
        val world = context.world
        val blockPos = context.blockPos
        val tag = stack.orCreateTag
        if(player.isSneaking) {
            tag.putLong("pos", blockPos.asLong())
            if(!world.isClient) {
                player.sendMessage(LiteralText("Successfully saved pos!"), true)
            }
            return ActionResult.SUCCESS
        }else if(tag.contains("pos")) {
            val posToLink = BlockPos.fromLong(tag.getLong("pos"))
            (world.getBlockEntity(blockPos) as? Linkable)?.let{ linkable ->
                if(linkable.link(posToLink)) {
                    if(!world.isClient) {
                        player.sendMessage(LiteralText("Successfully linked pos!"), true)
                    }
                    return ActionResult.SUCCESS
                }
            }
            if(!world.isClient) {
                player.sendMessage(LiteralText("Failed to link pos!"), true)
            }
            return ActionResult.FAIL
        }
        return super.useOnBlock(context)
    }

}