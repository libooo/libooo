package herschel.ia.pal;

public class ContextRuleException extends IllegalArgumentException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ContextRuleException() {
	super();
    }

    public ContextRuleException(String s) {
	super(s);
    }

    public ContextRuleException(String message, Throwable cause) {
	super(message, cause);
    }

    public ContextRuleException(Throwable cause) {
	super(cause);
    }

}
