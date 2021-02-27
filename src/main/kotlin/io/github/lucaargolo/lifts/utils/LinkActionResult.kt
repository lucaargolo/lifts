package io.github.lucaargolo.lifts.utils

enum class LinkActionResult {
    TOO_FAR_AWAY,
    NOT_SCREEN,
    NOT_LIFT,
    SUCCESSFUL;

    fun isAccepted() = this == SUCCESSFUL
}
