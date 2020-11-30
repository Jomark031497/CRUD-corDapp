package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.UserContract
import com.template.states.UserState
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.CollectSignaturesFlow
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.util.*

abstract class BaseFlow : FlowLogic<SignedTransaction>(){


    /**
     * returnType   :       StateAndRef<T>
     * parameters   :       linearId
     * DESC         :       Pass a linearId to this function and it will fetch the first data
     */
    fun getVaultData(linearId: UniqueIdentifier) : StateAndRef<UserState> {
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))
        return serviceHub.vaultService.queryBy<UserState>(queryCriteria).states.single()
    }

    fun transaction(state : UserState): TransactionBuilder {
        val notary: Party = serviceHub.networkMapCache.notaryIdentities.first()
        val issueCommand = Command(UserContract.Commands.Issue(), state.participants.map { it.owningKey })
        val builder = TransactionBuilder(notary = notary)

        builder.addOutputState(state, UserContract.ID)
        builder.addCommand(issueCommand)
        return builder
    }

    fun transaction(state: UserState,
                    dataState: StateAndRef<UserState>): TransactionBuilder {

        val notary: Party = serviceHub.networkMapCache.notaryIdentities.first()
        val updateCommand = Command(UserContract.Commands.Update(), state.participants.map { it.owningKey })
        val builder = TransactionBuilder(notary = notary)

        builder
                .addInputState(dataState)
                .addOutputState(state, UserContract.ID)
                .addCommand(updateCommand)
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
    fun recordTransaction(transaction: SignedTransaction, sessions: List<FlowSession>): SignedTransaction =
            subFlow(FinalityFlow(transaction, sessions))
}