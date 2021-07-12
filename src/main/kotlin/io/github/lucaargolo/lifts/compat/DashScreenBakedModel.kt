package io.github.lucaargolo.lifts.compat

import io.activej.serializer.annotations.Deserialize
import io.activej.serializer.annotations.Serialize
import io.github.lucaargolo.lifts.client.render.bakedmodel.screen.ScreenBakedModel
import net.minecraft.client.render.model.BakedModel
import net.oskarstrom.dashloader.DashRegistry
import net.oskarstrom.dashloader.api.annotation.DashObject
import net.oskarstrom.dashloader.model.DashModel

@DashObject(ScreenBakedModel::class)
class DashScreenBakedModel : DashModel{

    var modelList: IntArray @Serialize(order = 0) get
    var spriteList: IntArray @Serialize(order = 1) get

    constructor(screenBakedModel: ScreenBakedModel, registry: DashRegistry) {
        this.modelList = screenBakedModel.modelList.map(registry::createModelPointer).toIntArray()
        this.spriteList = screenBakedModel.spriteList.map(registry::createSpritePointer).toIntArray()
    }

    constructor(@Deserialize("modelList") modelList: IntArray, @Deserialize("spriteList") spriteList: IntArray) {
        this.modelList = modelList
        this.spriteList = spriteList
    }

    override fun toUndash(registry: DashRegistry): BakedModel {
        val screenBakedModel = ScreenBakedModel()
        this.modelList.forEach { screenBakedModel.modelList.add(registry.getModel(it)) }
        this.spriteList.forEach { screenBakedModel.spriteList.add(registry.getSprite(it)) }
        return screenBakedModel
    }

    override fun getStage(): Int = 3
}