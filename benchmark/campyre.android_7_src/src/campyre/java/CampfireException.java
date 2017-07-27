package campyre.java;

public class CampfireException extends Exception {
    private static final long serialVersionUID = -2623309261327198087L;
    private String msg;
    
    public CampfireException(String msg) {
    	super(msg);
    	this.msg = msg;
    }
    
    public CampfireException(Exception e, String msg) {
    	super(e);
    	this.msg = msg;
    }
    
    public String getMessage() {
    	return this.msg;
    }
}
