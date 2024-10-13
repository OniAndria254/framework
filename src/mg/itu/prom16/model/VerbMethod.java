package mg.itu.prom16.model;

public class VerbMethod {
    private String verb;
    private String method;
    
    public VerbMethod(String verb, String method) {
        this.verb = verb;
        this.method = method;
    }

    public VerbMethod() {
    }

    public String getVerb() {
        return verb;
    }
    public void setVerb(String verb) {
        this.verb = verb;
    }
    public String getMethod() {
        return method;
    }
    public void setMethod(String method) {
        this.method = method;
    }


}
