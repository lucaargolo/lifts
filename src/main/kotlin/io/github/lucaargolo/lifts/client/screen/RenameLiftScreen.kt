package io.github.lucaargolo.lifts.client.screen

import io.github.lucaargolo.lifts.common.blockentity.lift.LiftBlockEntity
import io.github.lucaargolo.lifts.network.PacketCompendium
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText

class RenameLiftScreen(val blockEntity: LiftBlockEntity): Screen(TranslatableText("screen.title.rename_lift")) {

    private var nameField: TextFieldWidget? = null
    private var setButton: ButtonWidget? = null

    override fun init() {
        super.init()
        nameField = TextFieldWidget(textRenderer, (width/2)-60, (height/2)-5, 120, 16, LiteralText(""))
        nameField?.text = blockEntity.liftName ?: ""
        nameField?.setMaxLength(16)
        nameField?.setEditableColor(16777215)
        this.addChild(nameField)
        setButton = ButtonWidget((width/2)-61, (height/2)+16, 122, 20, TranslatableText("screen.common.set_name")) {
            val passedData = PacketByteBufs.create()
            passedData.writeBlockPos(blockEntity.pos)
            passedData.writeString(nameField?.text)
            ClientPlayNetworking.send(PacketCompendium.RENAME_LIFT_ENTITY, passedData)
            onClose()
        }
        this.addButton(setButton)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val nameField = nameField ?: return super.keyPressed(keyCode, scanCode, modifiers)
        return if (nameField.keyPressed(keyCode, scanCode, modifiers)) true
        else if (nameField.isFocused && nameField.isVisible && keyCode != 256) true
        else super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(matrices)
        super.render(matrices, mouseX, mouseY, delta)
        val text = TranslatableText("screen.message.write_name").append(": ")
        textRenderer.draw(matrices, text, (width/2f)-(textRenderer.getWidth(text)/2f), (height/2f)-20, 0xFFFFFF)
        nameField?.render(matrices, mouseX, mouseY, delta)
    }

    override fun tick() {
        nameField?.text?.isEmpty()?.let {
            if(it) {
                setButton?.message = TranslatableText("screen.common.reset_name")
            }else{
                setButton?.message = TranslatableText("screen.common.set_name")
            }
        }
    }

    override fun isPauseScreen() = false

}