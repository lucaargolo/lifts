package io.github.lucaargolo.lifts.common.blockentity.lift

enum class LiftActionResult {
    TOO_MANY_PLATFORMS,
    INVALID_PLATFORM,
    NO_PLATFORM,
    NO_RANGE,
    NO_ENERGY,
    NO_FUEL,
    SUCCESSFUL;

    fun isAccepted() = this == SUCCESSFUL
}