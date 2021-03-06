/*
 * This file is part of Bitsquare.
 *
 * Bitsquare is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bitsquare is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bitsquare. If not, see <http://www.gnu.org/licenses/>.
 */

package io.bitsquare.gui.main.funds.withdrawal;

import io.bitsquare.btc.AddressEntry;
import io.bitsquare.btc.WalletService;
import io.bitsquare.btc.listeners.AddressConfidenceListener;
import io.bitsquare.btc.listeners.BalanceListener;
import io.bitsquare.gui.components.confidence.ConfidenceProgressIndicator;
import io.bitsquare.gui.util.BSFormatter;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.TransactionConfidence;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.*;

import static com.google.common.base.Preconditions.checkNotNull;

public class WithdrawalListItem {
    private final StringProperty addressString = new SimpleStringProperty();
    private final BalanceListener balanceListener;

    private final Label balanceLabel;

    private final AddressEntry addressEntry;

    private final WalletService walletService;
    private final BSFormatter formatter;
    private final AddressConfidenceListener confidenceListener;

    private final ConfidenceProgressIndicator progressIndicator;

    private final Tooltip tooltip;

    private Coin balance;

    public WithdrawalListItem(AddressEntry addressEntry, WalletService walletService, BSFormatter formatter) {
        this.addressEntry = addressEntry;
        this.walletService = walletService;
        this.formatter = formatter;
        this.addressString.set(getAddress().toString());

        // confidence
        progressIndicator = new ConfidenceProgressIndicator();
        progressIndicator.setId("funds-confidence");
        tooltip = new Tooltip("Not used yet");
        progressIndicator.setProgress(0);
        progressIndicator.setPrefSize(24, 24);
        Tooltip.install(progressIndicator, tooltip);

        confidenceListener = walletService.addAddressConfidenceListener(new AddressConfidenceListener(getAddress()) {
            @Override
            public void onTransactionConfidenceChanged(TransactionConfidence confidence) {
                updateConfidence(confidence);
            }
        });

        updateConfidence(walletService.getConfidenceForAddress(getAddress()));


        // balance
        balanceLabel = new Label();
        balanceListener = walletService.addBalanceListener(new BalanceListener(getAddress()) {
            @Override
            public void onBalanceChanged(Coin balance) {
                updateBalance(balance);
            }
        });

        updateBalance(walletService.getBalanceForAddress(getAddress()));
    }

    public void cleanup() {
        walletService.removeAddressConfidenceListener(confidenceListener);
        walletService.removeBalanceListener(balanceListener);
    }

    private void updateBalance(Coin balance) {
        this.balance = balance;
        if (balance != null) {
            balanceLabel.setText(formatter.formatCoin(balance));
        }
    }

    private void updateConfidence(TransactionConfidence confidence) {
        if (confidence != null) {
            //log.debug("Type numBroadcastPeers getDepthInBlocks " + confidence.getConfidenceType() + " / " +
            // confidence.numBroadcastPeers() + " / " + confidence.getDepthInBlocks());
            switch (confidence.getConfidenceType()) {
                case UNKNOWN:
                    tooltip.setText("Unknown transaction status");
                    progressIndicator.setProgress(0);
                    break;
                case PENDING:
                    tooltip.setText("Seen by " + confidence.numBroadcastPeers() + " peer(s) / 0 confirmations");
                    progressIndicator.setProgress(-1.0);
                    break;
                case BUILDING:
                    tooltip.setText("Confirmed in " + confidence.getDepthInBlocks() + " block(s)");
                    progressIndicator.setProgress(Math.min(1, (double) confidence.getDepthInBlocks() / 6.0));
                    break;
                case DEAD:
                    tooltip.setText("Transaction is invalid.");
                    progressIndicator.setProgress(0);
                    break;
            }
        }
    }


    public final String getLabel() {
        switch (addressEntry.getAddressContext()) {
            case REGISTRATION_FEE:
                return "Registration fee";
            case TRADE:
                checkNotNull(addressEntry.getOfferId());
                return "Offer ID: " + addressEntry.getOfferId();
            case ARBITRATOR_DEPOSIT:
                return "Arbitration deposit";
        }
        return "";
    }


    public final StringProperty addressStringProperty() {
        return this.addressString;
    }

    Address getAddress() {
        return addressEntry.getAddress();
    }


    public AddressEntry getAddressEntry() {
        return addressEntry;
    }


    public ConfidenceProgressIndicator getProgressIndicator() {
        return progressIndicator;
    }


    public Label getBalanceLabel() {
        return balanceLabel;
    }


    public Coin getBalance() {
        return balance;
    }
}
