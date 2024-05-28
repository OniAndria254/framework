package mg.itu.prom16.model;


public class Mapping {
    String classe;
    String methode;
    
    public Mapping() {
    }
    
    public String getClasse() {
        return classe;
    }
    public void setClasse(String classe) {
        this.classe = classe;
    }
    public String getMethode() {
        return methode; 
    }
    public void setMethode(String methode) {
        this.methode = methode;
    }
    public Mapping(String classe, String methode) {
        this.classe = classe;
        this.methode = methode;
    }
    
}
