package io.github.lucaargolo.lifts.common.item.linking

import io.github.lucaargolo.lifts.utils.Linkable
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.text.TranslatableText
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos

class LinkingTool(settings: Settings): Item(settings) {

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val player = context.player ?: return super.useOnBlock(context)
        val stack = context.stack
        val world = context.world
        val blockPos = context.blockPos
        val tag = stack.orCreateNbt
        if(player.isSneaking) {
            tag.putLong("pos", blockPos.asLong())
            if(!world.isClient) {
                player.sendMessage(TranslatableText("message.lifts.link.saved_pos").formatted(Formatting.GREEN), true)
            }
            return ActionResult.SUCCESS
        }else if(tag.contains("pos")) {
            val posToLink = BlockPos.fromLong(tag.getLong("pos"))
            (world.getBlockEntity(blockPos) as? Linkable)?.let{ linkable ->
                val linkActionResult = linkable.link(posToLink)
                if(!world.isClient) {
                    player.sendMessage(TranslatableText("message.lifts.link.${linkActionResult.name.lowercase()}").formatted(if(linkActionResult.isAccepted()) Formatting.GREEN else Formatting.RED), true)
                }
                return if(linkActionResult.isAccepted()) ActionResult.SUCCESS else ActionResult.FAIL
            }
            if(!world.isClient) {
                player.sendMessage(TranslatableText("message.lifts.link.not_linkable").formatted(Formatting.YELLOW), true)
            }
            return ActionResult.FAIL
        }
        return super.useOnBlock(context)
    }

}