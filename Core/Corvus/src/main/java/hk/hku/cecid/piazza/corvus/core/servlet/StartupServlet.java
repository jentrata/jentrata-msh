/* 
 * Copyright(c) 2005 Center for E-Commerce Infrastructure Development, The
 * University of Hong Kong (HKU). All Rights Reserved.
 *
 * This software is licensed under the GNU GENERAL PUBLIC LICENSE Version 2.0 [1]
 * 
 * [1] http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt
 */

package hk.hku.cecid.piazza.corvus.core.servlet;

import hk.hku.cecid.piazza.commons.Sys;
import hk.hku.cecid.piazza.corvus.core.Kernel;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * StartupServlet is the main startup servlet of Corvus.
 * It will initialize the Corvus kernel once the servlet is being initialized.
 * 
 * @author Hugo Y. K. Lam
 *  
 */
public class StartupServlet extends HttpServlet {

    /**
     * Initializes the servlet.
     * 
     * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        String jentrataHome = System.getProperty("corvus.home",System.getProperty("catalina.home"));
        Properties jentrataProps = new Properties();
        try {
            File jentrataPropsFile = new File(jentrataHome + "/conf/jentrata.conf");
            if(jentrataPropsFile.exists()) {
                InputStream fis = new FileInputStream(jentrataPropsFile);
                jentrataProps.load(fis);
                fis.close();
            }
        } catch (Exception ex) {
            //ignore
        }
        System.getProperties().putAll(jentrataProps);

        Kernel.getInstance();
        
        Sys.main.log.info("Corvus Startup Servlet initialized successfully");
    }
    
    
    /**
     * Invoked when the servlet is out of service.
     * 
     * @see javax.servlet.Servlet#destroy()
     */
    public void destroy() {
        super.destroy();
        
        Kernel.getInstance().shutdown(); 

        Sys.main.log.info("Corvus Startup Servlet destroyed successfully");
    }
}