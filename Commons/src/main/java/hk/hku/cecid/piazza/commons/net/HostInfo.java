/*
 * Copyright(c) 2005 Center for E-Commerce Infrastructure Development, The
 * University of Hong Kong (HKU). All Rights Reserved.
 *
 * This software is licensed under the GNU GENERAL PUBLIC LICENSE Version 2.0 [1]
 *
 * [1] http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt
 */

package hk.hku.cecid.piazza.commons.net;

import java.net.InetAddress;

/**
 */
public final class HostInfo {

    /**
     * Get the ip address of this host
     */
    public static String GetLocalhostAddress() {
	String localHostAddress = "";
	try {
	    InetAddress localAddr = InetAddress.getLocalHost();
	    localHostAddress = localAddr.getHostAddress();
	} catch (Exception e) {
	    localHostAddress = "no_host";
	}
	return localHostAddress;
    }
}
