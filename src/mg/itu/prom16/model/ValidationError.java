package mg.itu.prom16.model;
import java.util.HashMap;
import java.util.Map;
public class ValidationError {
    private Map<String, String> errors = new HashMap<>();
    private Map<String, String> previousValues = new HashMap<>();

    // Méthode pour ajouter une erreur et la valeur précédente
    public void addError(String fieldName, String errorMessage, String previousValue) {
        errors.put(fieldName, errorMessage);
        previousValues.put(fieldName, previousValue); // Ajout de la valeur précédente
    }
    
    public void addError(String fieldName, String errorMessage, Object previousValue) {
        errors.put(fieldName, errorMessage);
        previousValues.put(fieldName, previousValue != null ? previousValue.toString() : null); // Sauvegarder la valeur précédente sous forme de chaîne
    }
    

    // Vérifie s'il y a des erreurs
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    // Retourne la map des erreurs
    public Map<String, String> getErrors() {
        return errors;
    }

    // Retourne la map des valeurs précédentes
    public Map<String, String> getPreviousValues() {
        return previousValues;
    }
}

