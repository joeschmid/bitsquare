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

package io.bitsquare.app.gui;

import io.bitsquare.app.BitsquareEnvironment;
import io.bitsquare.app.BitsquareExecutable;
import io.bitsquare.btc.BitcoinNetwork;
import io.bitsquare.network.BootstrapNodes;
import io.bitsquare.network.Node;
import io.bitsquare.util.joptsimple.EnumValueConverter;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import static io.bitsquare.app.BitsquareEnvironment.*;
import static io.bitsquare.msg.tomp2p.TomP2PMessageModule.*;
import static io.bitsquare.network.Node.*;
import static java.util.Arrays.asList;

public class BitsquareAppMain extends BitsquareExecutable {

    public static void main(String[] args) throws Exception {
        new BitsquareAppMain().execute(args);
    }

    @Override
    protected void customizeOptionParsing(OptionParser parser) {
        parser.accepts(USER_DATA_DIR_KEY, description("User data directory", DEFAULT_USER_DATA_DIR))
                .withRequiredArg();
        parser.accepts(APP_NAME_KEY, description("Application name", DEFAULT_APP_NAME))
                .withRequiredArg();
        parser.accepts(APP_DATA_DIR_KEY, description("Application data directory", DEFAULT_APP_DATA_DIR))
                .withRequiredArg();
        parser.acceptsAll(asList(APP_DATA_DIR_CLEAN_KEY, "clean"),
                description("Clean application data directory", DEFAULT_APP_DATA_DIR_CLEAN))
                .withRequiredArg()
                .ofType(boolean.class);
        parser.accepts(NAME_KEY, description("Name of this node", null))
                .withRequiredArg();
        parser.accepts(PORT_KEY, description("Port to listen on", Node.DEFAULT_PORT))
                .withRequiredArg()
                .ofType(int.class);
        parser.accepts(USE_MANUAL_PORT_FORWARDING_KEY, description("Use manual port forwarding", false))
                .withRequiredArg()
                .ofType(boolean.class);
        parser.accepts(BitcoinNetwork.KEY, description("", BitcoinNetwork.DEFAULT))
                .withRequiredArg()
                .ofType(BitcoinNetwork.class)
                .withValuesConvertedBy(new EnumValueConverter(BitcoinNetwork.class));
        parser.accepts(BOOTSTRAP_NODE_NAME_KEY, description("", BootstrapNodes.DEFAULT.getName()))
                .withRequiredArg();
        parser.accepts(BOOTSTRAP_NODE_IP_KEY, description("", BootstrapNodes.DEFAULT.getIp()))
                .withRequiredArg();
        parser.accepts(BOOTSTRAP_NODE_PORT_KEY, description("", BootstrapNodes.DEFAULT.getPort()))
                .withRequiredArg()
                .ofType(int.class);
        parser.accepts(NETWORK_INTERFACE_KEY, description("Network interface", null))
                .withRequiredArg();
    }

    @Override
    protected void doExecute(OptionSet options) {
        BitsquareApp.setEnvironment(new BitsquareEnvironment(options));
        javafx.application.Application.launch(BitsquareApp.class);
    }
}
