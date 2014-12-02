/* 
 * Copyright(c) 2005 Center for E-Commerce Infrastructure Development, The
 * University of Hong Kong (HKU). All Rights Reserved.
 *
 * This software is licensed under the GNU GENERAL PUBLIC LICENSE Version 2.0 [1]
 * 
 * [1] http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt
 */

package hk.hku.cecid.ebms.spa.dao;

import hk.hku.cecid.piazza.commons.dao.DVO;

/**
 * @author Hans Sinnige
 * 
 */
public interface ClusterDVO extends DVO {

    /**
     * @param Returns the hostname.
     */
    public String getHostname();

    /**
     * @param hostname The hostname to set.
     */
    public void setHostname(String hostname);

    /**
     * @return Returns the status.
     */
    public String getStatus();

    /**
     * @param status The status to set.
     */
    public void setStatus(String status);

    /**
     * @return Returns the timestamp.
     */
    public long getTimestamp();

    /**
     * @param timestamp The timestamp to set.
     */
    public void setTimestamp(long timestamp);

}