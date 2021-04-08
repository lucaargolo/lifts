package io.github.lucaargolo.lifts.compat

import io.github.lucaargolo.lifts.Lifts
import java.lang.reflect.Field
import java.lang.reflect.Method

object OptifineShadersCompat {

    private var shadersClass: Class<*>? = null
    private var shadersFramebufferClass: Class<*>? = null
    private var programsClass: Class<*>? = null
    private var programClass: Class<*>? = null

    private var isRenderingWorldField: Field? = null
    private var isShadowPassField: Field? = null
    private var dfbField: Field? = null
    private var sfbField: Field? = null
    private var programsField: Field? = null
    private var activeProgramField: Field? = null

    private var bindShadersFramebufferMethod: Method? = null
    private var useProgramMethod: Method? = null
    private var getProgramNoneMethod: Method? = null

    fun initialize() {
        Lifts.LOGGER.info("Trying to load optifine compat!")
        try {
            shadersClass = Class.forName("net.optifine.shaders.Shaders")
            shadersFramebufferClass = Class.forName("net.optifine.shaders.ShadersFramebuffer")
            programsClass = Class.forName("net.optifine.shaders.Programs")
            programClass = Class.forName("net.optifine.shaders.Program")
            isRenderingWorldField = shadersClass?.getField("isRenderingWorld")
            isShadowPassField = shadersClass?.getField("isShadowPass")
            dfbField = shadersClass?.getDeclaredField("dfb")
            dfbField?.isAccessible = true
            sfbField = shadersClass?.getDeclaredField("sfb")
            sfbField?.isAccessible = true
            programsField = shadersClass?.getDeclaredField("programs")
            programsField?.isAccessible = true
            activeProgramField = shadersClass?.getField("activeProgram")
            bindShadersFramebufferMethod = shadersFramebufferClass?.getMethod("bindFramebuffer")
            useProgramMethod = shadersClass?.getMethod("useProgram", programClass)
            getProgramNoneMethod = programsClass?.getMethod("getProgramNone")
            Lifts.LOGGER.info("Successfully loaded optifine compatibility!")
        }catch (e: Exception) {
            Lifts.LOGGER.info("Optifine not found, not loading compatibility!")
        }
    }

    private var lastActiveProgram: Any? = null

    fun startDrawingScreen() {
        if(isRenderingWorldField?.getBoolean(null) == true && isShadowPassField?.getBoolean(null) == false) {
            val programs = programsField?.get(null)
            lastActiveProgram = activeProgramField?.get(null)
            useProgramMethod?.invoke(null, getProgramNoneMethod?.invoke(programs))
        }
    }

    fun endDrawingScreen() {
        if(isRenderingWorldField?.getBoolean(null) == true) {
            isShadowPassField?.getBoolean(null)?.let { isRenderingShadow ->
                if(isRenderingShadow) {
                    val shadowFramebuffer = sfbField?.get(null)
                    bindShadersFramebufferMethod?.invoke(shadowFramebuffer)
                }else{
                    val drawFramebuffer = dfbField?.get(null)
                    bindShadersFramebufferMethod?.invoke(drawFramebuffer)
                    useProgramMethod?.invoke(null, lastActiveProgram)
                }
            }
        }
    }

}