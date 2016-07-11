package hk.hku.cecid.ebms.spa.listener;

import hk.hku.cecid.piazza.commons.Sys;
import hk.hku.cecid.piazza.commons.servlet.RequestListenerException;
import hk.hku.cecid.piazza.commons.servlet.http.HttpRequestAdaptor;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by aaronwalker on 8/07/2016.
 */
public class EbmsCamelListener extends HttpRequestAdaptor {


    private FileSystemXmlApplicationContext context;

    @Override
    public void listenerCreated() throws RequestListenerException {
        super.listenerCreated();
        File deployDir = new File(System.getProperty("corvus.home") + "/deploy");
        context = new FileSystemXmlApplicationContext(deployDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        }));
        context.setClassLoader(Sys.main.getClassLoader());
        context.start();
    }

    @Override
    public void listenerDestroyed() throws RequestListenerException {
        super.listenerDestroyed();
        context.stop();
    }

    @Override
    public String processRequest(HttpServletRequest request, HttpServletResponse response) throws RequestListenerException {
        return "OK";
    }
}
