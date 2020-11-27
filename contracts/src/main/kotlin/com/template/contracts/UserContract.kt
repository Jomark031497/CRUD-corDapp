package com.template.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

/**
The Contract
*   Govern the ways in which states can evolve over time
*   Contract execution is deterministic, and transaction acceptance is
    based on the transaction’s contents alone.
 */

/**
 * UserContract     Implements Contract requiring verify function that gets called
                    to verify (hence the name) a transaction.
 */

class UserContract : Contract {

    /** This is used in other parts of Corda when reflection is required. */
    companion object {
        const val ID = "com.template.contracts.UserContract"
    }

    override fun verify(tx: LedgerTransaction) {
    }

    interface Commands : CommandData {
        class Issue : Commands
        class Update : Commands
        class Delete : Commands
    }
}