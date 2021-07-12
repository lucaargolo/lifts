package io.github.lucaargolo.lifts.client.render.bakedmodel.screen

import com.mojang.datafixers.util.Pair
import io.github.lucaargolo.lifts.common.block.BlockCompendium
import io.github.lucaargolo.lifts.utils.ModIdentifier
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.model.*
import net.minecraft.client.render.model.json.ModelOverrideList
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.item.ItemStack
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.state.property.Properties
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockRenderView
import java.awt.Color
import java.util.*
import java.util.function.Function
import java.util.function.Supplier

@Suppress("unused", "UNUSED_PARAMETER")
class ScreenBakedModel: UnbakedModel, BakedModel, FabricBakedModel {

    private val modelIdList = mutableListOf(
        ModIdentifier("block/screen")
    )

    val modelList = mutableListOf<BakedModel>()

    private val spriteIdList = mutableListOf(
        SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, ModIdentifier("block/screen_border")),
        SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, ModIdentifier("block/screen_front")),
        SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, ModIdentifier("block/machine_base")),
        SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, ModIdentifier("block/screen_side"))
    )
    val spriteList = mutableListOf<Sprite>()

    override fun getModelDependencies(): Collection<Identifier> = listOf()

    override fun getTextureDependencies(unbakedModelGetter: Function<Identifier, UnbakedModel>, unresolvedTextureReferences: MutableSet<Pair<String, String>>) = spriteIdList

    override fun bake(loader: ModelLoader, textureGetter: Function<SpriteIdentifier, Sprite>, rotationContainer: ModelBakeSettings, modelId: Identifier): BakedModel {
        modelList.clear()
        spriteList.clear()
        modelIdList.forEach { modelIdentifier ->
            loader.getOrLoadModel(modelIdentifier).bake(loader, textureGetter, ModelRotation.X0_Y0, modelId)?.let { modelList.add(it) } // NORTH
            loader.getOrLoadModel(modelIdentifier).bake(loader, textureGetter, ModelRotation.X0_Y180, modelId)?.let { modelList.add(it) } //SOUTH
            loader.getOrLoadModel(modelIdentifier).bake(loader, textureGetter, ModelRotation.X0_Y270, modelId)?.let { modelList.add(it) } //WEST
            loader.getOrLoadModel(modelIdentifier).bake(loader, textureGetter, ModelRotation.X0_Y90, modelId)?.let { modelList.add(it) } //EAST
        }
        spriteIdList.forEach { spriteIdentifier ->
            spriteList.add(textureGetter.apply(spriteIdentifier))
        }
        return this
    }

    override fun getSprite() = spriteList[0]

    override fun isVanillaAdapter() = false

    override fun emitItemQuads(stack: ItemStack, randomSupplier: Supplier<Random>, context: RenderContext) {
        val fakeState = BlockCompendium.SCREEN.defaultState.with(Properties.HORIZONTAL_FACING, Direction.NORTH)
        emitBlockQuads(null, fakeState, BlockPos.ORIGIN, randomSupplier, context)
    }

    override fun emitBlockQuads(world: BlockRenderView?, state: BlockState, pos: BlockPos, randomSupplier: Supplier<Random>, context: RenderContext) {
        val color = Color(255, 255, 255, 255).rgb

        context.pushTransform { quad ->
            quad.spriteColor(0, color, color, color, color)
            true
        }

        val facing = state[Properties.HORIZONTAL_FACING]
        modelList.getOrNull(facing.id-2)?.emitFromVanilla(state, context, randomSupplier) {
            it.face.axis == facing.axis || let { _ ->
                val neighborState = world?.getBlockState(pos.add(it.face.vector)) ?: return@let true
                neighborState.block != BlockCompendium.SCREEN || neighborState[Properties.HORIZONTAL_FACING] != facing
            }
        }
        context.emitter.drawSide(facing, world, pos)

        context.popTransform()
    }

    private fun Direction.getLeft(): Direction {
        return when(this) {
            Direction.NORTH -> Direction.WEST
            Direction.SOUTH -> Direction.EAST
            Direction.EAST -> Direction.NORTH
            Direction.WEST -> Direction.SOUTH
            Direction.UP, Direction.DOWN -> Direction.EAST
        }
    }

    private fun Direction.getRight(): Direction {
        return when(this) {
            Direction.NORTH -> Direction.EAST
            Direction.SOUTH -> Direction.WEST
            Direction.EAST -> Direction.SOUTH
            Direction.WEST -> Direction.NORTH
            Direction.UP, Direction.DOWN -> Direction.WEST
        }
    }

    private fun Direction.getUp(): Direction {
        return when(this) {
            Direction.UP -> Direction.NORTH
            Direction.DOWN -> Direction.SOUTH
            else -> Direction.UP
        }
    }

    private fun Direction.getDown(): Direction {
        return when(this) {
            Direction.UP -> Direction.SOUTH
            Direction.DOWN -> Direction.NORTH
            else -> Direction.DOWN
        }
    }

    private fun QuadEmitter.drawSide(side: Direction, world: BlockRenderView?, pos: BlockPos) {
        val bl1 = true //world?.getBlockState(pos.add(side.getUp().vector))?.block != BlockCompendium.SCREEN
        val bl2 = true //world?.getBlockState(pos.add(side.getDown().vector))?.block != BlockCompendium.SCREEN
        val bl3 = true //world?.getBlockState(pos.add(side.getLeft().vector))?.block != BlockCompendium.SCREEN
        val bl4 = true //world?.getBlockState(pos.add(side.getRight().vector))?.block != BlockCompendium.SCREEN

        val bl5 = true //world?.getBlockState(pos.add(side.getUp().vector).add(side.getLeft().vector))?.block != BlockCompendium.SCREEN
        val bl6 = true //world?.getBlockState(pos.add(side.getUp().vector).add(side.getRight().vector))?.block != BlockCompendium.SCREEN
        val bl7 = true //world?.getBlockState(pos.add(side.getDown().vector).add(side.getLeft().vector))?.block != BlockCompendium.SCREEN
        val bl8 = true //world?.getBlockState(pos.add(side.getDown().vector).add(side.getRight().vector))?.block != BlockCompendium.SCREEN

        if(bl1) draw(side, 15/16f, 1f, 1/16f, 15/16f, 12.95f/16f) //UP
        if(bl2) draw(side, 15/16f, 1/16f, 1/16f, 0f, 12.95f/16f) //DOWN
        if(bl3) draw(side, 1f, 15/16f, 15/16f, 1/16f, 12.95f/16f) //LEFT
        if(bl4) draw(side, 1/16f, 15/16f, 0f, 1/16f, 12.95f/16f) //RIGHT

        if(bl1 || bl3 || bl5) draw(side, 1f, 1f, 15/16f, 15/16f, 12.95f/16f) //UP_LEFT
        if(bl1 || bl4 || bl6) draw(side, 1/16f, 1f, 0f, 15/16f, 12.95f/16f) //UP_RIGHT

        if(bl2 || bl3 || bl7) draw(side, 1f, 1/16f, 15/16f, 0f, 12.95f/16f) //DOWN_LEFT
        if(bl2 || bl4 || bl8) draw(side, 1/16f, 1/16f, 0f, 0f, 12.95f/16f) //DOWN_RIGHT

    }

    private fun QuadEmitter.draw(side: Direction, left: Float, bottom: Float, right: Float, top: Float, depth: Float) {
        square(side, left, bottom, right, top, depth)
        spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV)
        spriteColor(0, -1, -1, -1, -1)
        emit()
    }

    @Suppress("DEPRECATION")
    private fun BakedModel.emitFromVanilla(state: BlockState, context: RenderContext, randSupplier: Supplier<Random>, shouldEmit: (BakedQuad) -> Boolean) {
        val emitter = context.emitter
        Direction.values().forEach { dir ->
            getQuads(state, dir, randSupplier.get()).forEach { quad ->
                if (shouldEmit(quad)) {
                    emitter.fromVanilla(quad.vertexData, 0, false)
                    emitter.emit()
                }
            }
        }
        getQuads(state, null, randSupplier.get()).forEach { quad ->
            if (shouldEmit(quad)) {
                emitter.fromVanilla(quad.vertexData, 0, false)
                emitter.emit()
            }
        }
    }

    override fun getQuads(state: BlockState?, face: Direction?, random: Random?): MutableList<BakedQuad> = mutableListOf()

    override fun useAmbientOcclusion() = true
    override fun hasDepth() = false
    override fun isSideLit() = true
    override fun isBuiltin() = false

    override fun getOverrides(): ModelOverrideList = ModelOverrideList.EMPTY
    override fun getTransformation(): ModelTransformation = MinecraftClient.getInstance().bakedModelManager.getModel(ModelIdentifier("minecraft:stone#")).transformation

}