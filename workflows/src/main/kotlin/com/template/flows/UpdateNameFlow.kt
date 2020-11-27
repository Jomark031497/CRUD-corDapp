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
class UpdateNameFlow(   private val name: String,
                        private val linearId: UniqueIdentifier): FlowLogic<SignedTransaction>() {


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

    private fun getVaultData() : StateAndRef<UserState> {
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))
        return serviceHub.vaultService.queryBy<UserState>(queryCriteria).states.single()
    }

    @Suspendable
    override fun call(): SignedTransaction {

        val transaction: TransactionBuilder = transaction()
        val signedTransaction: SignedTransaction = verifyAndSign(transaction)
        val sessions: List<FlowSession> = (userStates(getVaultData()).participants - ourIdentity).map { initiateFlow(it) }.toSet().toList()
        val transactionSignedByAllParties: SignedTransaction = collectSignature(signedTransaction, sessions)
        return recordTransaction(transactionSignedByAllParties, sessions)
    }

    private fun transaction(): TransactionBuilder {

        val notary: Party = serviceHub.networkMapCache.notaryIdentities.first()
        val updateCommand = Command(UserContract.Commands.Update(), userStates(getVaultData()).participants.map { it.owningKey })
        val builder = TransactionBuilder(notary = notary)

        builder.addInputState(getVaultData())
        builder.addOutputState(userStates(getVaultData()), UserContract.ID)
        builder.addCommand(updateCommand)
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