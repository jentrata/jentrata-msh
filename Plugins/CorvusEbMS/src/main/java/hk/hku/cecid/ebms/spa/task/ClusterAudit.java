package hk.hku.cecid.ebms.spa.task;

import java.net.*;
import java.io.*;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.sql.Timestamp;

import hk.hku.cecid.piazza.commons.dao.DAOException;
import hk.hku.cecid.piazza.commons.module.ActiveModule;
import hk.hku.cecid.piazza.commons.module.ModuleException;
import hk.hku.cecid.piazza.commons.net.HostInfo;

import hk.hku.cecid.ebms.spa.EbmsProcessor;
import hk.hku.cecid.ebms.spa.dao.MessageDAO;
import hk.hku.cecid.ebms.spa.dao.MessageDVO;
import hk.hku.cecid.ebms.spa.dao.ClusterDAO;
import hk.hku.cecid.ebms.spa.dao.ClusterDVO;
import hk.hku.cecid.ebms.spa.handler.MessageClassifier;

/**
 * The <code>ClusterAudit</code> audits messages in the cluster and redistributes them if needed
 *
 * It uses the table cluster inside the database to determine which hosts are part of the cluster
 * and to find out if it should wait for or start to audit the hosts and when needed their related
 * messages.
 * 
 * Creation Date: 25/11/2014
 * 
 * @author 	Hans Sinnige
 * @version	1.0.1
 * 
 */
public class ClusterAudit extends ActiveModule {
	
    // Internal Message DAO object. 
    private MessageDAO msgDAO;
    private ClusterDAO clusterDAO;
 
    // The flag for initializing monitor related objects.
    private boolean initialized = false;
 
    // Our current hostname
    private static String Hostname;

    // Static properties, values are set inside ebms.properties.xml under <cluster>
    private static int Interval = 180000;
    private static String Port = "8080";
    private static int Timeout = 10000;

    /**
     * Creates a new instance of MessageMonitor.
     * 
     * @param descriptorLocation the module descriptor.
     * @param loader the class loader for this module.
     * @param shouldInitialize true if the module should be initialized.
     */
    public ClusterAudit(String descriptorLocation, ClassLoader loader, boolean shouldInitialize) {
	super(descriptorLocation, loader, shouldInitialize);
    }
    
    /**
     * Invoke for initialization.
     */
    public void init() {
	super.init();
    }
    
    /**
     * Post/Lazy initialization. This method is invoked at the firs time only
     * this module execute.<br/><br/>
     * 
     * Initialize all class data and register this host in the cluster list.
     */
    public void initialize(){
	try{
	    msgDAO = (MessageDAO) EbmsProcessor.core.dao.createDAO(MessageDAO.class);
	    clusterDAO = (ClusterDAO) EbmsProcessor.core.dao.createDAO(ClusterDAO.class);
	    
	    this.Port = EbmsProcessor.core.properties.getProperty("/ebms/cluster/port");
	    this.Interval = Integer.parseInt(EbmsProcessor.core.properties.getProperty("/ebms/cluster/interval"));
	    this.Timeout = Integer.parseInt(EbmsProcessor.core.properties.getProperty("/ebms/cluster/connectiontimeout"));
	    this.Hostname = HostInfo.GetLocalhostAddress();
	    
	    registerCurrentHost();
	    this.initialized = true;
	}catch(DAOException daoe){
	    EbmsProcessor.core.log.fatal("ClusterAudit: Unable to intialize." + daoe);
	}
    }
    
    /**
     * The method is invoked constantly with interval defined in the configuration
     * descriptor or 60 second by default.
     * 
     * @return true if this method should be invoked again after a defined interval.
     */
    public boolean execute() {
	// Lazy initialization. 
	if (!this.initialized)
	    this.initialize();
	
	int counter = 0;
	
	try {
	    long timeStamp = getHostTimestamp(Hostname);
	    String oldHostname = "";
	    Date date = new Date();
	    long currentTimestamp = date.getTime();
	    
	    // Check if audit starttime is passed or not
	    if (timeStamp > currentTimestamp) {
		date.setTime( timeStamp );
		EbmsProcessor.core.log.debug ( "ClusterAudit: wait (" + date.toString() + ")");
	    } else {
		EbmsProcessor.core.log.debug ( "ClusterAudit: start (" + date.toString() + ")");
		// To prevent more hosts auditing the cluster at the same time the
		// status of our host is set to auditor. Then the cluster is checked
		// if this is the only auditor in the cluster. In case this is the
		// only auditor the audit will start. In case it is not the only
		// auditor the audit will be skipped and this host will be added
		// at the end of the cluster list.
		updateStatusHostname( Hostname, "auditor" );
		if (uniqueAuditor(Hostname)) {
		    // Walk through the list of hosts inside this cluster
		    List clusterDVOList = clusterDAO.findClusterAllEntries();
		    Iterator i = clusterDVOList.iterator();
		    while (i.hasNext()) {
			ClusterDVO clusterEntry = (ClusterDVO) i.next();
			oldHostname = clusterEntry.getHostname();
			EbmsProcessor.core.log.debug ( "ClusterAudit: check host: " + oldHostname );
			counter = 0;
			// Skip this host and check if the host is available
			if (!oldHostname.equals(Hostname) && !hostIsAvailable(oldHostname)) {
			    EbmsProcessor.core.log.debug ( "ClusterAudit: host no longer available: " + oldHostname );
			    // Replace hostname for those messages that are still related to the other host
			    counter = replaceHostname( Hostname, oldHostname );
			    if (counter == 0) {
				// If there are no messages left in the other host remove it from the cluster list
				EbmsProcessor.core.log.debug ( "ClusterAudit: remove host: " + oldHostname );
				removeHostname( oldHostname );
			    } else {
				// Update the status of the host to inactive
				EbmsProcessor.core.log.debug ( "ClusterAudit: inactive host: " + oldHostname + " message(s) moved" );
				updateStatusHostname( oldHostname, "inactive" );
			    }
			}
		    }
		}
		// Put us at the end of the cluster list
		registerCurrentHost();
		EbmsProcessor.core.log.debug ( "ClusterAudit: end (" + date.toString() + ")");
	    }
	}catch(DAOException daoe){
	    EbmsProcessor.core.log.fatal("ClusterAudit: Unable to complete cluster audit." + daoe);
	}
	return true;
    }

    /**
     * getHostTimestamp() - retrieves the value of attribute timestamp from
     * the database table 'cluster' for the provided host
     *
     * @param hostname the host for which information is requested
     *
     * @return timeStamp value of the provided host, or 0 in case the host isn't found
     */
    private long getHostTimestamp(String hostname) throws DAOException {
	long timeStamp = 0;
	List clusterDVOList = clusterDAO.findClusterEntry(hostname);
	Iterator i = clusterDVOList.iterator();
	while (i.hasNext()) {
	    ClusterDVO clusterEntry = (ClusterDVO) i.next();
	    timeStamp = clusterEntry.getTimestamp();
	}
	return timeStamp;
    }
    
    /**
     * getLatestClusterTimestamp() - retrieves the 'highest' value of attribute
     * timestamp from database table cluster.
     *
     * @return timeStamp value of the provided host, or 0 in case there is no
     * value found.
     */
    private long getLatestClusterTimestamp() throws DAOException {
	long timeStamp = 0;
	List clusterDVOList = clusterDAO.findClusterLatestTimestamp();
	Iterator i = clusterDVOList.iterator();
	while (i.hasNext()) {
	    ClusterDVO clusterEntry = (ClusterDVO) i.next();
	    timeStamp = clusterEntry.getTimestamp();
	}
	return timeStamp;
    }

    /**
     * getLatestTimestamp() - retrieves the 'highest' value of timestamp. The
     * value is based upon the highest value from database table 'cluster' but
     * in case that value is lower than the current time or in case the cluster
     * is empty the current time is returned as timestamp.
     *
     * @return timeStamp value 
     */
    private long getLatestTimestamp() throws DAOException {
	long timeStamp = getLatestClusterTimestamp();
	Date currentDate = new Date();
	long currentTimeStamp = currentDate.getTime();
	if (currentTimeStamp > timeStamp) {
	    timeStamp = currentTimeStamp;
	}
	return timeStamp;
    }

    /**
     * removeHostname() - removes given host from database table cluster
     *
     * @param hostname the host to be removed
     */
    private void removeHostname( String hostname ) throws DAOException {
	// Check if an entry for our host exist and preset all values
	ClusterDVO clusterDVO = (ClusterDVO) clusterDAO.createDVO();
	clusterDVO.setHostname(hostname);
	clusterDAO.deleteCluster(clusterDVO);
    }

    /**
     * updateStatusHostname() - update the status of given host in
     * database table cluster
     *
     * @param hostname the host to be updated
     * @param status the new status value
     *
     */
    private void updateStatusHostname( String hostname, String status ) throws DAOException {
	ClusterDVO clusterDVO = (ClusterDVO) clusterDAO.createDVO();
	clusterDVO.setHostname(hostname);
	clusterDVO.setStatus(status);
	clusterDAO.updateCluster(clusterDVO);
    }

    /**
     * updateClusterData() - update the data of given host in
     * database table cluster. In case the host is unknown in the
     * table cluster a new entry will be added with given values
     *
     * @param hostname the host to be updated
     * @param status the status value
     * @param timestamp the timestamp value
     *
     */
    private void updateClusterData(String hostname, String status, long timestamp) throws DAOException {
	// Check if an entry for our host exist and preset all values
	ClusterDVO clusterDVO = (ClusterDVO) clusterDAO.createDVO();
	clusterDVO.setHostname(hostname);
	if (clusterDAO.findCluster(clusterDVO)) {
	    // Update host entry
	    clusterDVO.setHostname(hostname);
	    clusterDVO.setStatus(status);
	    clusterDVO.setTimestamp(timestamp);
	    clusterDAO.updateCluster(clusterDVO);
	} else {
	    // Insert host entry
	    clusterDVO.setHostname(hostname);
	    clusterDVO.setStatus(status);
	    clusterDVO.setTimestamp(timestamp);
	    clusterDAO.addCluster(clusterDVO);
	}
    }

    /**
     * registerCurrentHost() - Add the current host to the database
     * table cluster with an active status and up to date timestamp.
     * In case the host already exist inside cluster its data will
     * be updated.
     *
     */
    private void registerCurrentHost() throws DAOException {
	// Retrieve the latest registrated timestamp from the cluster
	long timeStamp = getLatestTimestamp();
	timeStamp += Interval;
	
	// Update or add entry to the cluster table
	updateClusterData(Hostname, "active", timeStamp);
    }
    
    /**
     * hostIsAvailable() - Checks if a given host is still available for receiving
     * messages. This is done by scanning the listening port of the given host.
     *
     * @param hostname the host which is tried
     *
     * @return true in case host is available
     */
    private boolean hostIsAvailable(String hostname) {
	boolean available = true;

	try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(hostname, Integer.parseInt(Port)), 1000);
            socket.close();
	} catch (Exception ex) {
            available = false;
	}
        return available;
    }
    
    /**
     * uniqueAuditor() - Is this host the only auditor
     *
     * @param hostname the host which is tried
     *
     * @return true in case the host is the unique auditor
     */
    private boolean uniqueAuditor(String hostname) throws DAOException {
	boolean unique = true;
	String localHostname = "";
	List clusterDVOList = clusterDAO.findClusterStatusEntries("auditor");

	Iterator i = clusterDVOList.iterator();
	while (i.hasNext()) {
	    ClusterDVO clusterEntry = (ClusterDVO) i.next();
	    localHostname = clusterEntry.getHostname();
	    if (!localHostname.equals(hostname) && hostIsAvailable(localHostname)) {
		unique = false;
	    }
	}
	return unique;
    }
	
    /**
     * replaceHostname() - replace the hostname in case messages are 'stuck'
     *
     * @param newhostname the host which will replace the old 
     * @param oldhostname the host which will be replace by the new 
     *
     * @return number of messages by which the hostname is replaced
     */
    private int replaceHostname(String newhostname, String oldhostname) throws DAOException {
	int totcounter = 0;
	int counter = 0;

	counter = msgDAO.updateOldIncomingMessagesPendingbyTimestamp(newhostname, oldhostname);
	if (counter > 0) {
	    EbmsProcessor.core.log.debug ( "ClusterAudit: inbox message(s) recovered.");
	}
	totcounter += counter;
	counter = 0;

	counter = msgDAO.updateOldOutboxPendingMessagesbyTimestamp(newhostname, oldhostname);
	if (counter > 0) {
	    EbmsProcessor.core.log.debug ( "ClusterAudit: pending outbox message(s) recovered.");
	}
	totcounter += counter;
	counter = 0;

	counter = msgDAO.updateOldOutboxProcessingMessagesbyTimestamp(newhostname, oldhostname);
	if (counter > 0) {
	    EbmsProcessor.core.log.debug ( "ClusterAudit: processing outbox message(s) recovered.");
	}
	totcounter += counter;

	return totcounter;
    }
}
