/* 
 * Copyright(c) 2005 Center for E-Commerce Infrastructure Development, The
 * University of Hong Kong (HKU). All Rights Reserved.
 *
 * This software is licensed under the GNU GENERAL PUBLIC LICENSE Version 2.0 [1]
 * 
 * [1] http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt
 */

package hk.hku.cecid.ebms.spa.dao;

import hk.hku.cecid.piazza.commons.dao.ds.DataSourceDVO;

/**
 * @author Hans Sinnige
 */
public class ClusterDataSourceDVO extends DataSourceDVO implements
        ClusterDVO {

    public ClusterDataSourceDVO() {
        super();
    }

    /**
     * @return The hostname
     */
    public String getHostname() {
        return super.getString("hostname");
    }

    /**
     * @param hostname The hostname to set.
     */
    public void setHostname(String hostname) {
        super.setString("hostname", hostname);
    }

    /**
     * @return The status
     */
    public String getStatus() {
        return super.getString("status");
    }

    /**
     * @param status The status to set.
     */
    public void setStatus(String status) {
        super.setString("status", status);
    }

    /**
     * @return The timestamp
     */
    public long getTimestamp() {
        return super.getLong("timestamp");
    }

    /**
     * @param timestamp The timestamp to set.
     */
    public void setTimestamp(long timestamp) {
        super.setLong("timestamp", timestamp);
    }


}
