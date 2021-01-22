package io.github.lucaargolo.lifts.common.block.screen

import io.github.lucaargolo.lifts.common.blockentity.screen.ScreenBlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.state.property.Properties
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.sign

object ScreenBlockHandler {

    fun openScreenHook(hit: HitResult?, world: World?, screen: Screen?): Boolean {
        if (world != null && hit is BlockHitResult) {
            val pos = hit.blockPos
            (world.getBlockEntity(pos) as? ScreenBlockEntity)?.let{ blockEntity ->
                if (blockEntity.clickDelay == 5) {
                    screen?.let { blockEntity.setupScreen(it) }
                    return true
                }
            }
        }
        return false
    }

    fun mouseScrollHook(hit: HitResult?, world: World?, vertical: Double): Boolean {
        if (world != null && hit is BlockHitResult) {
            val pos = hit.blockPos
            (world.getBlockEntity(pos) as? ScreenBlockEntity)?.let { blockEntity ->
                val facing = blockEntity.cachedState.get(Properties.HORIZONTAL_FACING)
                blockEntity.screen?.let{ screen ->
                    val client = MinecraftClient.getInstance()
                    val d: Double = (if (client.options.discreteMouseScroll) sign(vertical) else vertical) * client.options.mouseWheelSensitivity
                    val mousePos = getMousePosition(hit, facing, pos)
                    return screen.mouseScrolled(mousePos.first, mousePos.second, d)
                }
            }
        }
        return false
    }

    fun getMousePosition(hitResult: HitResult, facing: Direction, pos: BlockPos): Pair<Double, Double> {
        val rayPos = hitResult.pos
        val ctxPos = Vec3d(rayPos.x - pos.x, rayPos.y - pos.y, rayPos.z - pos.z)
        var mouseX = 0.0
        var mouseY = 0.0
        if(hitResult is BlockHitResult && hitResult.blockPos == pos && hitResult.side == facing) {
            mouseX = when(facing) {
                Direction.NORTH -> (1-ctxPos.x)*256
                Direction.SOUTH -> (ctxPos.x)*256
                Direction.EAST -> (1-ctxPos.z)*256
                Direction.WEST -> (ctxPos.z)*256
                else -> 0.0
            }
            mouseY = (1-ctxPos.y)*256
        }
        return Pair(mouseX, mouseY)
    }

}