package com.template.flows

import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction

@InitiatingFlow
@StartableByRPC
class DeleteFlow (private val linearId: UniqueIdentifier) : FlowLogic<Unit>() {


    override fun call() : Unit {

        return
    }

}