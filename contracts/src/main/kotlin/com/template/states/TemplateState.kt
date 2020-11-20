package com.template.states

import com.template.contracts.TemplateContract
import com.template.contracts.UserContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party


// *********
// * State *
// *********
@BelongsToContract(UserContract::class)
class UserState(val name: String,
                val age: Int,
                val address: String,
                val gender: String,
                val node: Party,
                val status: StatusEnums,
                override val linearId: UniqueIdentifier,
                override val participants : List<Party>
) : LinearState