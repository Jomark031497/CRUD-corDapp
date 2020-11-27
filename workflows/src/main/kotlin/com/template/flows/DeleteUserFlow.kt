package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.UserContract
import com.template.states.UserState
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

@InitiatingFlow
@StartableByRPC
class DeleteUserFlow(private val linearId: UniqueIdentifier): BaseFlow() {

    private fun userStates(dataState : StateAndRef<UserState>): UserState {
        val data = dataState.state.data
        return UserState(
                name = data.name,
                age = data.age,
                address = data.address,
                gender = data.gender,
                status = data.status,
                isDeleted =  true,
                node = ourIdentity,
                linearId = linearId,
                participants = data.participants
        )
    }

    @Suspendable
    override fun call(): SignedTransaction {

        val dataState = getVaultData(linearId)
        val transaction: TransactionBuilder = transaction(userStates(dataState), dataState)
        val signedTransaction: SignedTransaction = verifyAndSign(transaction)
        val sessions: List<FlowSession> = (userStates(dataState).participants - ourIdentity).map { initiateFlow(it) }.toSet().toList()
        val transactionSignedByAllParties: SignedTransaction = collectSignature(signedTransaction, sessions)
        return recordTransaction(transactionSignedByAllParties, sessions)
    }
}

@InitiatedBy(DeleteUserFlow::class)
class DebuggingFlowResponder(val flowSession: FlowSession) : FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        val signTransactionFlow = object : SignTransactionFlow(flowSession)
        {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
            }
        }
        val signedTransaction = subFlow(signTransactionFlow)
        return subFlow(ReceiveFinalityFlow(otherSideSession = flowSession, expectedTxId = signedTransaction.id))
    }
}