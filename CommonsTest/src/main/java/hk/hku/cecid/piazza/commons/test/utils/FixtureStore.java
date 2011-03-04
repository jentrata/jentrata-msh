/**
 * 
 */
package hk.hku.cecid.piazza.commons.test.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;


/**
 * @author aaronwalker
 *
 */
public class FixtureStore {

    public static ClassLoader createFixtureLoader(boolean delegate, Class<?> class1) {
        return new FixtureClassLoader(delegate,class1);
    }
    
    public static class FixtureClassLoader extends ClassLoader {

        private boolean delegate;
        private Class<?> parentCL;
        
        public FixtureClassLoader(boolean delegate, Class<?> class1) {
            this.delegate = delegate;
            this.parentCL = class1;
        }

        @Override
        public URL getResource(String resource) {
            
            URL url = super.getResource(parentCL.getName() + "/" + resource);
            if(url == null && delegate) {
                return super.getResource(resource);
            }
            return url;
        }

        @Override
        public InputStream getResourceAsStream(String resource) {
            InputStream is =  super.getResourceAsStream(parentCL.getName() + "/" + resource);
            if(is == null && delegate) {
                return super.getResourceAsStream(resource);
            }
            return is;
        }

        @Override
        public Enumeration<URL> getResources(String resource) throws IOException {
            
            Enumeration<URL> urls = super.getResources(parentCL.getName() + "/" + resource);
            if(urls == null && delegate) {
                return super.getResources(resource);
            }
            return urls;
        }

        
        
    }

}
