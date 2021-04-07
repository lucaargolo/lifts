package io.github.lucaargolo.lifts

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import io.github.lucaargolo.lifts.common.block.BlockCompendium
import io.github.lucaargolo.lifts.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.lifts.common.blockentity.lift.LiftShaft
import io.github.lucaargolo.lifts.common.entity.EntityCompendium
import io.github.lucaargolo.lifts.common.item.ItemCompendium
import io.github.lucaargolo.lifts.common.containers.ScreenHandlerCompendium
import io.github.lucaargolo.lifts.network.PacketCompendium
import io.github.lucaargolo.lifts.utils.ModConfig
import io.github.lucaargolo.lifts.utils.ModIdentifier
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.PrintWriter
import java.nio.file.Files

class Lifts: ModInitializer {

    override fun onInitialize() {
        PacketCompendium.onInitialize()
        BlockCompendium.initialize()
        BlockEntityCompendium.initialize()
        ItemCompendium.initialize()
        EntityCompendium.initialize()
        ScreenHandlerCompendium.initialize()

        ServerLifecycleEvents.SERVER_STARTED.register {
            LiftShaft.clearServer()
        }
        ServerTickEvents.END_SERVER_TICK.register {
            LiftShaft.tickServer()
        }
    }

    companion object {
        const val MOD_ID = "lifts"

        private val creativeTab = FabricItemGroupBuilder.create(ModIdentifier("creative_tab")).icon{ ItemStack(BlockCompendium.ELECTRIC_LIFT_MK5) }.build()
        private val parser = JsonParser()
        private val gson = GsonBuilder().setPrettyPrinting().create()
        private val logger: Logger = LogManager.getLogger("Terrarian Slimes")

        val CONFIG: ModConfig by lazy {
            val configFile = File("${FabricLoader.getInstance().configDir}${File.separator}$MOD_ID.json")
            var finalConfig: ModConfig
            logger.info("Trying to read config file...")
            try {
                if (configFile.createNewFile()) {
                    logger.info("No config file found, creating a new one...")
                    val json: String = gson.toJson(parser.parse(gson.toJson(ModConfig())))
                    PrintWriter(configFile).use { out -> out.println(json) }
                    finalConfig = ModConfig()
                    logger.info("Successfully created default config file.")
                } else {
                    logger.info("A config file was found, loading it..")
                    finalConfig = gson.fromJson(String(Files.readAllBytes(configFile.toPath())), ModConfig::class.java)
                    if (finalConfig == null) {
                        throw NullPointerException("The config file was empty.")
                    } else {
                        logger.info("Successfully loaded config file.")
                    }
                }
            } catch (exception: Exception) {
                logger.error("There was an error creating/loading the config file!", exception)
                finalConfig = ModConfig()
                logger.warn("Defaulting to original config.")
            }
            finalConfig
        }

        fun creativeGroupSettings(): Item.Settings = Item.Settings().group(creativeTab)
    }

}