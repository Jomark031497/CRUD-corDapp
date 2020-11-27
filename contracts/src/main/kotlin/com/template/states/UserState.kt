package com.template.states


import com.template.contracts.UserContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party

/**
 * **********
    The State
 * **********

* Represents on ledger facts
* States are evolved by marking the current state as historic and creating an updated state
* Each node has a vault where it stores any relevant states to itself
* IMMUTABLE
* can contain any kind of information
 */

/**
 * ContractState       requires a property that returns the participants of the state.
 * linearId            UniqueIdentifier // a UUID. ID is generated from the UUID class.
 * LinearState         evolves by superseding itself. As such, when the state is updated it
                    should be included as an input of a transaction with a newer version being output.
 * Participants        The parties defined within this list determine who gets to have the state saved to
                    their own vault/database.
 * @BelongsToContract  Annotation which binds a state to a contract.
*/
@BelongsToContract(UserContract::class)
class UserState(val name: String,
                val age: Int,
                val address: String,
                val gender: GenderEnums,
                val node: Party,
                val status: StatusEnums,
                val isDeleted: Boolean = false,
                override val linearId: UniqueIdentifier,
                override val participants : List<Party>
) : LinearState
 /**
        linearState means the state may evolve overtime
 */