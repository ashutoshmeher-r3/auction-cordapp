package net.corda.samples.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.FinalityFlow;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.samples.contracts.AssetContract;
import net.corda.samples.states.Ledger;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public class UpdateLedgerFlow extends FlowLogic<SignedTransaction> {

    private long amount;
    private Party bidder;

    public UpdateLedgerFlow(long amount, Party bidder) {
        this.amount = amount;
        this.bidder = bidder;
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        List<StateAndRef<Ledger>> ledgerStateAndRefs = getServiceHub().getVaultService()
                .queryBy(Ledger.class).getStates();

        if(ledgerStateAndRefs.size() ==0){
            throw new FlowException("Ledger not available");
        }

        Ledger ledger = new Ledger(getOurIdentity(), new LinkedHashMap<>(ledgerStateAndRefs.get(0).getState().getData().getBalanceMap()));
        long availableAmount = ledger.getBalanceMap().get(bidder) - amount;
        ledger.getBalanceMap().put(bidder, availableAmount);

        TransactionBuilder transactionBuilder = new TransactionBuilder(ledgerStateAndRefs.get(0).getState().getNotary())
                .addOutputState(ledger)
                .addCommand(new AssetContract.Commands.LegerOp(), Collections.singletonList(getOurIdentity().getOwningKey()));

        if(ledgerStateAndRefs.size() > 0) {
            transactionBuilder.addInputState(ledgerStateAndRefs.get(0));
        }

        // Verify the transaction
        transactionBuilder.verify(getServiceHub());

        // Sign the transaction
        SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);

        // Notarise the transaction and record the state in the ledger.
        return subFlow(new FinalityFlow(signedTransaction, Collections.emptyList()));
    }
}
