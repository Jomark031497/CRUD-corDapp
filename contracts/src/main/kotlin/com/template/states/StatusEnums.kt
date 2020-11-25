package com.template.states

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
enum class StatusEnums {
    SINGLE, MARRIED, UNDISCLOSED
}