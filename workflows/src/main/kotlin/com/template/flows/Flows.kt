package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.*

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class Initiator : FlowLogic<Unit>() {

    @Suspendable
    override fun call() {
        // Initiator flow logic goes here.
    }
}

@InitiatedBy(Initiator::class)
class Responder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        // Responder flow logic goes here.
    }
}
