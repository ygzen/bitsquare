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

package io.bitsquare.btc.multisigwallet;

import io.bitsquare.btc.AddressEntry;
import io.bitsquare.common.persistance.Persistable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;

public class MultiSigWallet implements Persistable {
    private static final Logger log = LoggerFactory.getLogger(MultiSigWallet.class);

    public final String title;
    public final int mumOfRequiredSigners;
    public int mumOfKeyHolders;
    public final HashSet<MultiSigKeyHolder> multiSigKeyHolders = new HashSet<>();
    public final AddressEntry addressEntry;
    public final String uid;

    public MultiSigWallet(String title, int mumOfRequiredSigners, int mumOfKeyHolders, AddressEntry addressEntry) {
        this.uid = UUID.randomUUID().toString();
        this.title = title;
        this.mumOfRequiredSigners = mumOfRequiredSigners;
        this.mumOfKeyHolders = mumOfKeyHolders;
        this.addressEntry = addressEntry;
    }

    public void addMultiSigKeyHolder(MultiSigKeyHolder multiSigKeyHolder) {
        checkArgument(multiSigKeyHolders.size() < mumOfKeyHolders, "You cannot add more multiSigKeyHolders as defined in mumOfKeyHolders.");
        multiSigKeyHolders.add(multiSigKeyHolder);
    }
}
