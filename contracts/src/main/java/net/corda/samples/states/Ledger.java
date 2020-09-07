package net.corda.samples.states;

import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.samples.contracts.AssetContract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@BelongsToContract(AssetContract.class)
public class Ledger implements ContractState {

    private Party owner;
    private Map<Party, Long> balanceMap;

    public Ledger(Party owner, Map<Party, Long> balanceMap) {
        this.owner = owner;
        this.balanceMap = balanceMap;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(owner);
    }

    public Party getOwner() {
        return owner;
    }

    public void setOwner(Party owner) {
        this.owner = owner;
    }

    public Map<Party, Long> getBalanceMap() {
        return balanceMap;
    }

    public void setBalanceMap(Map<Party, Long> balanceMap) {
        this.balanceMap = balanceMap;
    }
}
