package mg.itu.prom16.model;
import java.util.*;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;


public class Mapping {
    String classe;
    ArrayList<VerbMethod> verbMethods = new ArrayList<>();
    
    
    public Mapping(String classe, ArrayList<VerbMethod> verbMethods) {
        this.classe = classe;
        this.verbMethods = verbMethods;
    }
    public Mapping() {
    }  
    
    public String getClasse() {
        return classe;
    }
    public void setClasse(String classe) {
        this.classe = classe;
    }
    public ArrayList<VerbMethod> getVerbMethods() {
        return verbMethods;
    }
    public void setVerbMethods(ArrayList<VerbMethod> verbMethods) {
        this.verbMethods = verbMethods;
    }

    public void addVerbMethod (VerbMethod vm){
        getVerbMethods().add(vm);
    }

    public boolean hasVerbMethod (VerbMethod vm){
        for (VerbMethod verbMethod : getVerbMethods()) {
            if (vm.getMethod().compareTo(verbMethod.getMethod()) == 0 && vm.getVerb().compareTo(verbMethod.getVerb())==0) {
                return true;
            }
        }

        return false;
    }

    public Mapping (String classname , VerbMethod vm){
        setClasse(classname);
        getVerbMethods().add(vm);
    }

    public VerbMethod getSingleVerbMethod (String verb){
        for (VerbMethod verbMethod : verbMethods) {
            if (verbMethod.getVerb().compareToIgnoreCase(verb) == 0) {
                return verbMethod;
            }
        }
        verb = "Get";
        for (VerbMethod verbMethod : verbMethods) {
            if (verbMethod.getVerb().compareToIgnoreCase(verb) == 0) {
                return verbMethod;
            }
        }
        verb = "Post";
        for (VerbMethod verbMethod : verbMethods) {
            if (verbMethod.getVerb().compareToIgnoreCase(verb) == 0) {
                return verbMethod;
            }
        }
        return null;
    }
    
}
