package io.github.lucaargolo.lifts.compat

import io.activej.serializer.annotations.Deserialize
import io.activej.serializer.annotations.Serialize
import io.github.lucaargolo.lifts.client.render.bakedmodel.screen.ElectricLiftBakedModel
import net.minecraft.client.render.model.BakedModel
import net.oskarstrom.dashloader.DashRegistry
import net.oskarstrom.dashloader.api.annotation.DashObject
import net.oskarstrom.dashloader.model.DashModel

@DashObject(ElectricLiftBakedModel::class)
class DashElectricLiftBakedModel : DashModel{

    var modelList: IntArray @Serialize(order = 0) get
    var spriteList: IntArray @Serialize(order = 1) get

    constructor(electricLiftBakedModel: ElectricLiftBakedModel, registry: DashRegistry) {
        this.modelList = electricLiftBakedModel.modelList.map(registry::createModelPointer).toIntArray()
        this.spriteList = electricLiftBakedModel.spriteList.map(registry::createSpritePointer).toIntArray()
    }

    constructor(@Deserialize("modelList") modelList: IntArray, @Deserialize("spriteList") spriteList: IntArray) {
        this.modelList = modelList
        this.spriteList = spriteList
    }

    override fun toUndash(registry: DashRegistry): BakedModel {
        val electricLiftBakedModel = ElectricLiftBakedModel()
        this.modelList.forEach { electricLiftBakedModel.modelList.add(registry.getModel(it)) }
        this.spriteList.forEach { electricLiftBakedModel.spriteList.add(registry.getSprite(it)) }
        return electricLiftBakedModel
    }

    override fun getStage(): Int = 3
}