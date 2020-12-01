package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contractsandstates.UserState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder



@InitiatingFlow
@StartableByRPC
class UpdateNameFlow(private val name: String, private val linearId: UniqueIdentifier): BaseFlow() {


    private fun userStates(dataState : StateAndRef<UserState>): UserState {
        return UserState(
                name = name,
                age = dataState.state.data.age,
                address = dataState.state.data.address,
                gender = dataState.state.data.gender,
                status = dataState.state.data.status,
                node = ourIdentity,
                linearId = linearId,
                participants = dataState.state.data.participants
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

@InitiatedBy(UpdateNameFlow::class)
class UpdateNameFlowResponder(val flowSession: FlowSession) : FlowLogic<SignedTransaction>() {

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