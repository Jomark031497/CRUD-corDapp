package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contractsandstates.Enums.GenderEnums
import com.template.contractsandstates.Enums.StatusEnums
import com.template.contractsandstates.UserState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.util.*

// *********
// Flows
// *********
@InitiatingFlow
@StartableByRPC
class CreateUserOwnNodesWithoutOtherParty (private val name :String,
                                           private val age : Int,
                                           private val address : String,
                                           private val gender : GenderEnums,
                                           private val status : StatusEnums): BaseFlow() {

    private fun userStates(): UserState {
        return UserState(
                name = name,
                age = age,
                address = address,
                gender = gender,
                status = status,
                node = ourIdentity,
                linearId = UniqueIdentifier(),
                participants = listOf(ourIdentity)
        )
    }

    @Suspendable
    override fun call(): SignedTransaction {
        val transaction: TransactionBuilder = transaction(userStates())
        val transactionSignedByAllParties: SignedTransaction = verifyAndSign(transaction)
        return recordTransaction(transactionSignedByAllParties, Collections.emptyList())
    }

}

