/**
 * 
 */
package hk.hku.cecid.piazza.commons.test.utils;


/**
 * @author aaronwalker
 *
 */
public class FixtureStore {

    public static ClassLoader createFixtureLoader(boolean b, Class<?> class1) {
        return class1.getClassLoader();
    }

}
