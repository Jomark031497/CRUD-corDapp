package com.template.states

import com.template.contracts.TemplateContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.Party


// *********
// * State *
// *********
@BelongsToContract(TemplateContract::class)
class IOUState(val name: Party,
               val age: Int,
               val address: String,
               val status: StatusEnums ) : ContractState {
    override val participants get() = listOf(name);

}