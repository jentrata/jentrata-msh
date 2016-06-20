/* 
 * Copyright(c) 2005 Center for E-Commerce Infrastructure Development, The
 * University of Hong Kong (HKU). All Rights Reserved.
 *
 * This software is licensed under the GNU GENERAL PUBLIC LICENSE Version 2.0 [1]
 * 
 * [1] http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt
 */

package hk.hku.cecid.ebms.spa.dao;

import hk.hku.cecid.piazza.commons.dao.DAO;
import hk.hku.cecid.piazza.commons.dao.DAOException;

import java.util.List;

/**
 * @author Hans Sinnige
 * 
 */
public interface ClusterDAO extends DAO {
    public boolean findCluster(ClusterDVO data) throws DAOException;

    public void addCluster(ClusterDVO data) throws DAOException;

    public boolean updateCluster(ClusterDVO data) throws DAOException;

    public void deleteCluster(ClusterDVO data) throws DAOException;
    
    public List selectCluster() throws DAOException;

    public List findClusterEntry(String hostname) throws DAOException;

    public List findClusterAllEntries() throws DAOException;

    public List findClusterStatusEntries(String status) throws DAOException;

    public List findClusterLatestTimestamp() throws DAOException;
}