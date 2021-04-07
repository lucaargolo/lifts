package io.github.lucaargolo.lifts.utils

class ModConfig {

    open class LiftConfig(
        val platformSpeed: Double,
        val platformRange: Int
    )

    class ElectricLiftConfig(
        platformSpeed: Double,
        platformRange: Int,
        val energyCapacity: Double
    ): LiftConfig(platformSpeed, platformRange)

    class LiftConfigs(
        val stirlingLift: LiftConfig = LiftConfig(1.0, 16),
        val electricLiftMk1: ElectricLiftConfig = ElectricLiftConfig(1.2, 32, 32000.0),
        val electricLiftMk2: ElectricLiftConfig = ElectricLiftConfig(1.4, 64, 64000.0),
        val electricLiftMk3: ElectricLiftConfig = ElectricLiftConfig(1.6, 128, 128000.0),
        val electricLiftMk4: ElectricLiftConfig = ElectricLiftConfig(1.8, 128, 256000.0),
        val electricLiftMk5: ElectricLiftConfig = ElectricLiftConfig(2.0, 256, 512000.0)
    )

    val maxFuelTicksStored = 32000
    val fuelTicksNeededPerBlock = 100

    val energyUnitsNeededPerBlock = 100

    val liftConfigs = LiftConfigs()

}