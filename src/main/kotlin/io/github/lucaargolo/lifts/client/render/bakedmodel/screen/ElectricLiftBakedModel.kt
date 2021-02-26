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
import net.minecraft.item.BlockItem
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
class ElectricLiftBakedModel: UnbakedModel, BakedModel, FabricBakedModel {

    private val modelIdList = mutableListOf(
        ModIdentifier("block/electric_lift")
    )

    private val modelList = mutableListOf<BakedModel>()

    private val spriteIdList = mutableListOf(
        SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, ModIdentifier("block/electric_lift_base")),
        SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, ModIdentifier("block/electric_lift_front")),
        SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, ModIdentifier("block/mk1_overlay")),
        SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, ModIdentifier("block/mk2_overlay")),
        SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, ModIdentifier("block/mk3_overlay")),
        SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, ModIdentifier("block/mk4_overlay")),
        SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, ModIdentifier("block/mk5_overlay"))
    )
    private val spriteList = mutableListOf<Sprite>()

    override fun getModelDependencies(): Collection<Identifier> = listOf()

    override fun getTextureDependencies(unbakedModelGetter: Function<Identifier, UnbakedModel>, unresolvedTextureReferences: MutableSet<Pair<String, String>>) = spriteIdList

    override fun bake(loader: ModelLoader, textureGetter: Function<SpriteIdentifier, Sprite>, rotationContainer: ModelBakeSettings, modelId: Identifier): BakedModel {
        if(modelList.isEmpty() && spriteList.isEmpty()) {
            modelIdList.forEach { modelIdentifier ->
                loader.getOrLoadModel(modelIdentifier).bake(loader, textureGetter, ModelRotation.X0_Y0, modelId)?.let { modelList.add(it) } // NORTH
                loader.getOrLoadModel(modelIdentifier).bake(loader, textureGetter, ModelRotation.X0_Y180, modelId)?.let { modelList.add(it) } //SOUTH
                loader.getOrLoadModel(modelIdentifier).bake(loader, textureGetter, ModelRotation.X0_Y270, modelId)?.let { modelList.add(it) } //WEST
                loader.getOrLoadModel(modelIdentifier).bake(loader, textureGetter, ModelRotation.X0_Y90, modelId)?.let { modelList.add(it) } //EAST
            }
            spriteIdList.forEach { spriteIdentifier ->
                spriteList.add(textureGetter.apply(spriteIdentifier))
            }
        }
        return this
    }

    override fun getSprite() = spriteList[0]

    override fun isVanillaAdapter() = false

    override fun emitItemQuads(stack: ItemStack, randomSupplier: Supplier<Random>, context: RenderContext) {
        val fakeState = (stack.item as? BlockItem)?.block?.defaultState?.with(Properties.HORIZONTAL_FACING, Direction.NORTH)
        emitBlockQuads(null, fakeState, BlockPos.ORIGIN, randomSupplier, context)
    }

    override fun emitBlockQuads(world: BlockRenderView?, state: BlockState?, pos: BlockPos, randomSupplier: Supplier<Random>, context: RenderContext) {
        val color = Color(255, 255, 255, 255).rgb

        context.pushTransform { quad ->
            quad.spriteColor(0, color, color, color, color)
            true
        }

        val facing = state?.get(Properties.HORIZONTAL_FACING) ?: Direction.NORTH
        modelList.getOrNull(facing.id-2)?.emitFromVanilla(state, context, randomSupplier)

        val emitter = context.emitter
        val overlaySprite = when(state?.block) {
            BlockCompendium.ELECTRIC_LIFT_MK1 -> spriteList[2]
            BlockCompendium.ELECTRIC_LIFT_MK2 -> spriteList[3]
            BlockCompendium.ELECTRIC_LIFT_MK3 -> spriteList[4]
            BlockCompendium.ELECTRIC_LIFT_MK4 -> spriteList[5]
            BlockCompendium.ELECTRIC_LIFT_MK5 -> spriteList[6]
            else -> null
        }

        overlaySprite?.let {
            emitter.draw(Direction.UP, it, 0f, 0f, 1f, 1f, -0.0005f)
            emitter.draw(Direction.DOWN, it, 0f, 0f, 1f, 1f, -0.0005f)
            emitter.draw(Direction.NORTH, it, 0f, 0f, 1f, 1f, -0.0005f)
            emitter.draw(Direction.SOUTH, it, 0f, 0f, 1f, 1f, -0.0005f)
            emitter.draw(Direction.EAST, it, 0f, 0f, 1f, 1f, -0.0005f)
            emitter.draw(Direction.WEST, it, 0f, 0f, 1f, 1f, -0.0005f)
        }

        context.popTransform()
    }

    private fun QuadEmitter.draw(side: Direction, sprite: Sprite, left: Float, bottom: Float, right: Float, top: Float, depth: Float) {
        square(side, left, bottom, right, top, depth)
        spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV)
        spriteColor(0, -1, -1, -1, -1)
        emit()
    }

    @Suppress("DEPRECATION")
    private fun BakedModel.emitFromVanilla(state: BlockState?, context: RenderContext, randSupplier: Supplier<Random>) {
        val emitter = context.emitter
        Direction.values().forEach { dir ->
            getQuads(state, dir, randSupplier.get()).forEach { quad ->
                emitter.fromVanilla(quad.vertexData, 0, false)
                emitter.emit()
            }
        }
        getQuads(state, null, randSupplier.get()).forEach { quad ->
            emitter.fromVanilla(quad.vertexData, 0, false)
            emitter.emit()
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