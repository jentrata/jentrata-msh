/**
 * 
 */
package org.jentrata.validation;

/**
 * @author aaronwalker
 *
 */
public class ValidationException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -4666453346191712907L;

    public ValidationException() {
        super();
    }

    public ValidationException(String msg, Throwable t) {
        super(msg, t);
    }

    public ValidationException(String msg) {
        super(msg);
    }

    public ValidationException(Throwable t) {
        super(t);
    }
}
