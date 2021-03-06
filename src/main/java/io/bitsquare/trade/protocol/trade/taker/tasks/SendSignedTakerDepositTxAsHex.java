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

package io.bitsquare.trade.protocol.trade.taker.tasks;

import io.bitsquare.bank.BankAccount;
import io.bitsquare.btc.WalletService;
import io.bitsquare.msg.MessageService;
import io.bitsquare.msg.listeners.OutgoingMessageListener;
import io.bitsquare.network.Peer;
import io.bitsquare.trade.protocol.trade.taker.messages.RequestOffererPublishDepositTxMessage;
import io.bitsquare.util.task.ExceptionHandler;
import io.bitsquare.util.task.ResultHandler;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Utils;

import java.security.PublicKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendSignedTakerDepositTxAsHex {
    private static final Logger log = LoggerFactory.getLogger(SendSignedTakerDepositTxAsHex.class);

    public static void run(ResultHandler resultHandler,
                           ExceptionHandler exceptionHandler,
                           Peer peer,
                           MessageService messageService,
                           WalletService walletService,
                           BankAccount bankAccount,
                           String accountId,
                           PublicKey messagePublicKey,
                           String tradeId,
                           String contractAsJson,
                           String takerSignature,
                           Transaction signedTakerDepositTx,
                           long offererTxOutIndex) {
        log.trace("Run task");
        long takerTxOutIndex = signedTakerDepositTx.getInput(1).getOutpoint().getIndex();

        RequestOffererPublishDepositTxMessage tradeMessage = new RequestOffererPublishDepositTxMessage(tradeId,
                bankAccount,
                accountId,
                messagePublicKey,
                Utils.HEX.encode(signedTakerDepositTx.bitcoinSerialize()),
                Utils.HEX.encode(signedTakerDepositTx.getInput(1).getScriptBytes()),
                Utils.HEX.encode(signedTakerDepositTx.getInput(1)
                        .getConnectedOutput()
                        .getParentTransaction()
                        .bitcoinSerialize()),
                contractAsJson,
                takerSignature,
                walletService.getAddressInfoByTradeID(tradeId).getAddressString(),
                takerTxOutIndex,
                offererTxOutIndex);
        messageService.sendMessage(peer, tradeMessage, new OutgoingMessageListener() {
            @Override
            public void onResult() {
                log.trace("RequestOffererDepositPublicationMessage successfully arrived at peer");
                resultHandler.handleResult();
            }

            @Override
            public void onFailed() {
                log.error("RequestOffererDepositPublicationMessage  did not arrive at peer");
                exceptionHandler.handleException(
                        new Exception("RequestOffererDepositPublicationMessage did not arrive at peer"));
            }
        });
    }
}
