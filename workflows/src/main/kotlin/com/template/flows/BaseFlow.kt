package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.UserContract
import com.template.states.UserState
import net.corda.core.contracts.Command
import net.corda.core.flows.CollectSignaturesFlow
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

abstract class BaseFlow : FlowLogic<SignedTransaction>(){

    fun transaction(state : UserState): TransactionBuilder {
        val notary: Party = serviceHub.networkMapCache.notaryIdentities.first()
        val issueCommand = Command(UserContract.Commands.Issue(), state.participants.map { it.owningKey })
        val builder = TransactionBuilder(notary = notary)

        builder.addOutputState(state, UserContract.ID)
        builder.addCommand(issueCommand)
        return builder
    }

    fun verifyAndSign(transaction: TransactionBuilder): SignedTransaction {
        transaction.verify(serviceHub)
        return serviceHub.signInitialTransaction(transaction)
    }

    @Suspendable
    fun collectSignature(
            transaction: SignedTransaction,
            sessions: List<FlowSession>
    ): SignedTransaction = subFlow(CollectSignaturesFlow(transaction, sessions))

    @Suspendable
    fun recordTransaction(transaction: SignedTransaction): SignedTransaction =
            subFlow(FinalityFlow(transaction))

    @Suspendable
    fun recordTransaction(transaction: SignedTransaction, sessions: List<FlowSession>): SignedTransaction =
            subFlow(FinalityFlow(transaction, sessions))
}