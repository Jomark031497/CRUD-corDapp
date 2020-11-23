package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.UserContract
import com.template.states.GenderEnums
import com.template.states.StatusEnums
import com.template.states.UserState
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

// *********
// Flows
// *********

/*
@InitiatingFlow     required by any flow that requests communication with a counterparty.
@StartableByRPC     This allows the flow to be called from an RPC connection which is the
                    interface between the outside of a Corda node and it’s internals.
@Suspendable        This annotation is needed on all functions that communicate with a counterparty.
                    the annotation allows the function to be suspended while the counterparty is dealing
                    with their side of the transaction.
FlowLogic           contains one abstract function call that needs to be implemented by the flow.
call                When the flow is triggered, call is executed and any logic that put inside the
                    function runs.
 */
@InitiatingFlow
@StartableByRPC
class UserFlows (private val name :String,
                 private val age : Int,
                 private val address : String,
                 private val gender: GenderEnums,
                 private val status : StatusEnums,
                 private val counterParty: Party): FlowLogic<SignedTransaction>() {

    private fun userStates(): UserState {
        return UserState(
                name = name,
                age = age,
                address = address,
                gender = gender,
                status = status,
                node = ourIdentity,
                linearId = UniqueIdentifier(),
                participants = listOf(ourIdentity, counterParty)
        )
    }

    @Suspendable
    override fun call(): SignedTransaction {
        val transaction: TransactionBuilder = transaction()
        val signedTransaction: SignedTransaction = verifyAndSign(transaction)
        val sessions: List<FlowSession> = (userStates().participants - ourIdentity).map { initiateFlow(it) }.toSet().toList()
        val transactionSignedByAllParties: SignedTransaction = collectSignature(signedTransaction, sessions)
        return recordTransaction(transactionSignedByAllParties, sessions)
    }

    /*
    notary:
    * serviceHub          :provided since we extended FlowLogic
    * networkMapCache     :provide the identities of the parties on the network
    * notaryIdentities    :narrows it down even more

    issueCommand: creates a command that represents the intent of the transaction.
    *
     */
    private fun transaction(): TransactionBuilder {
        val notary: Party = serviceHub.networkMapCache.notaryIdentities.first()
        val issueCommand = Command(UserContract.Commands.Issue(), userStates().participants.map { it.owningKey })
        val builder = TransactionBuilder(notary = notary)

//contract sa userID
        builder.addOutputState(userStates(), UserContract.ID)
        builder.addCommand(issueCommand)
        return builder
    }

    private fun verifyAndSign(transaction: TransactionBuilder): SignedTransaction {
        transaction.verify(serviceHub)
        return serviceHub.signInitialTransaction(transaction)
    }

    @Suspendable
    private fun collectSignature(
            transaction: SignedTransaction,
            sessions: List<FlowSession>
    ): SignedTransaction = subFlow(CollectSignaturesFlow(transaction, sessions))

    @Suspendable
    private fun recordTransaction(transaction: SignedTransaction, sessions: List<FlowSession>): SignedTransaction =
            subFlow(FinalityFlow(transaction, sessions))
}


@InitiatedBy(UserFlows::class)
class IOUIssueFlowResponder(val flowSession: FlowSession) : FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        val signTransactionFlow = object : SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
            }
        }
        val signedTransaction = subFlow(signTransactionFlow)
        return subFlow(ReceiveFinalityFlow(otherSideSession = flowSession, expectedTxId = signedTransaction.id))
    }
}