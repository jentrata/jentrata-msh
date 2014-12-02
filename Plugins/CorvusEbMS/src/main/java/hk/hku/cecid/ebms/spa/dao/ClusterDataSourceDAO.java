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
import hk.hku.cecid.piazza.commons.dao.DAOException;
import hk.hku.cecid.piazza.commons.dao.ds.DataSourceDAO;

import java.util.List;

/**
 * @author Hans Sinnige
 * 
 */
public class ClusterDataSourceDAO extends DataSourceDAO implements ClusterDAO {

    public DVO createDVO() {
        return new ClusterDataSourceDVO();
    }

    public boolean findCluster(ClusterDVO data) throws DAOException {
        return super.retrieve((ClusterDataSourceDVO) data);
    }

    public void addCluster(ClusterDVO data) throws DAOException {
        super.create((ClusterDataSourceDVO) data);
    }

    public boolean updateCluster(ClusterDVO data) throws DAOException {
        return super.persist((ClusterDataSourceDVO) data);
    }

    public void deleteCluster(ClusterDVO data) throws DAOException {
        super.remove((ClusterDataSourceDVO) data);
    }
    
    public List selectCluster() throws DAOException {
        return super.find("select_cluster", new Object[]{});
    }


    /**
     * findClusterEntry() - get the entry with value hostname from the
     * table cluster
     *
     * @param hostname The hostname for which the search is done
     *
     * @return List of Cluster entries (1)
     */
    public List findClusterEntry(String hostname) throws DAOException {
	if (hostname == null)
	    throw new DAOException("The required param 'hostname' is missing.");
        return super.find( "find_cluster_entry", new Object[] { hostname });
    }

    /**
     * findClusterAllEntries() - get all entries inside the table cluster
     *
     * @return List of Cluster entries
     */
    public List findClusterAllEntries() throws DAOException {
        return super.find( "find_cluster_all_entries", new Object[] {});
    }

    /**
     * findClusterStatusEntries() - get all entries from the table cluster
     * with attribute status set to given values
     *
     * @param status The status for which the search is done
     *
     * @return List of Cluster entries
     */
    public List findClusterStatusEntries(String status) throws DAOException {
	if (status == null)
	    throw new DAOException("The required param 'status' is missing.");
        return super.find( "find_cluster_status_entries", new Object[] { status });
    }

    /**
     * findClusterLatestTimestamp() - Find the entry with the highest / latest
     * timestamp
     *
     * @return List of Cluster entries (1)
     */
   public List findClusterLatestTimestamp() throws DAOException {
        return super.find( "find_cluster_latest_timestamp", new Object[] {});
    }
}
