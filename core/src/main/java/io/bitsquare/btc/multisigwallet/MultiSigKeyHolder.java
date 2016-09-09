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

import io.bitsquare.p2p.NodeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class MultiSigKeyHolder {
    private static final Logger log = LoggerFactory.getLogger(MultiSigKeyHolder.class);

    public final String name;
    public final NodeAddress nodeAddress;
    public final boolean isCoordinator;
    public final String addressString;
    public final byte[] pubKey;

    public MultiSigKeyHolder(String name, NodeAddress nodeAddress, boolean isCoordinator, String addressString, byte[] pubKey) {
        this.name = name;
        this.nodeAddress = nodeAddress;
        this.isCoordinator = isCoordinator;
        this.addressString = addressString;
        this.pubKey = pubKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MultiSigKeyHolder that = (MultiSigKeyHolder) o;

        if (isCoordinator != that.isCoordinator) return false;
        if (addressString != null ? !addressString.equals(that.addressString) : that.addressString != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (nodeAddress != null ? !nodeAddress.equals(that.nodeAddress) : that.nodeAddress != null) return false;
        if (!Arrays.equals(pubKey, that.pubKey)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (nodeAddress != null ? nodeAddress.hashCode() : 0);
        result = 31 * result + (isCoordinator ? 1 : 0);
        result = 31 * result + (addressString != null ? addressString.hashCode() : 0);
        result = 31 * result + (pubKey != null ? Arrays.hashCode(pubKey) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MultiSigKeyHolder{" +
                "name='" + name + '\'' +
                ", nodeAddress=" + nodeAddress +
                ", isCoordinator=" + isCoordinator +
                ", addressString='" + addressString + '\'' +
                ", pubKey=" + Arrays.toString(pubKey) +
                '}';
    }
}
