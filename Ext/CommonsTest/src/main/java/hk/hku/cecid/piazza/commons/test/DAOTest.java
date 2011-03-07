/**
 * 
 */
package hk.hku.cecid.piazza.commons.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hk.hku.cecid.piazza.commons.dao.ds.DataSourceDAO;


/**
 * @author aaronwalker
 *
 */
public abstract class DAOTest<T extends DataSourceDAO> {
    
    protected Logger logger = LoggerFactory.getLogger(getClass());
    
    public T getTestingTarget() {
        return null;
    }
    
    public void setUp() throws Exception {
        
    }
    
    public abstract String getTableName();

}
