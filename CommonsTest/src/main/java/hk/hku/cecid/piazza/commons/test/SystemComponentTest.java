/**
 * 
 */
package hk.hku.cecid.piazza.commons.test;

import hk.hku.cecid.edi.as2.pkg.AS2Message;
import hk.hku.cecid.piazza.commons.activation.InputStreamDataSource;
import hk.hku.cecid.piazza.commons.dao.DAO;
import hk.hku.cecid.piazza.commons.dao.DAOException;
import hk.hku.cecid.piazza.commons.dao.DAOFactory;
import hk.hku.cecid.piazza.commons.dao.DVO;
import hk.hku.cecid.piazza.commons.module.SystemComponent;
import hk.hku.cecid.piazza.commons.security.KeyStoreManager;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author aaronwalker
 *
 */
public class SystemComponentTest<T> {
    
    protected Logger LOG = LoggerFactory.getLogger(getClass());
    
    protected DAOBuilder TARGET;
    
    protected ClassLoader FIXTURE_LOADER = getClass().getClassLoader();
    
    protected Random RANDOM = new Random(System.currentTimeMillis());

    public String getSystemComponentId() {
        return null;
    }

    public void setUp() throws Exception {
        
    }

    public void tearDown() throws Exception {
        
    }

    public void commitSQL(Class<? extends DAO> daoClass, String createTableSql) {
        
    }
    
    public static class DAOBuilder {
        
        public DAOFactory getDAOFactory() {
            return new DAOFactory() {

                @Override
                protected void initFactory() throws DAOException {

                }

                @Override
                protected void initDAO(DAO dao) throws DAOException {
                    
                }
            };
        }

        public KeyStoreManager getComponent(String componentKeystoreManager) {
            return null;
        }

        public AS2Message storeOutgoingMessage(String mid, String string, DVO partnershipDVO,
                InputStreamDataSource inputStreamDataSource) {
            return storeOutgoingMessage(mid,string,partnershipDVO, inputStreamDataSource,null);
        }
        
        public AS2Message storeOutgoingMessage(String mid, String string, DVO partnershipDVO,
                InputStreamDataSource inputStreamDataSource, Object object) {
            return null;
        }

        public SystemComponent getSystemModule() {
            return null;
        }
    }


}
