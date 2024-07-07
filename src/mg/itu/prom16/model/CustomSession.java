package mg.itu.prom16.model;

import java.util.HashMap;

public class CustomSession {
    public HashMap<String, Object> hashMap = new HashMap<>();

    // Méthode pour récupérer la valeur d'un élément par sa clé
    public Object get(String key) {
        return hashMap.get(key);
    }

    // Méthode pour ajouter un nouvel élémentkkkkk au HashMap
    public void add(String key, Object value) {
        hashMap.put(key, value);
    }

    // Méthode pour supprimer un élément du HashMap en utilisant sa clé
    public void remove(String key) {
        hashMap.remove(key);
    }

    // Méthode pour mettre à jour la valeur d'un élément existant ou ajouter un nouvel élément si la clé n'existe pas
    public void update(String key, Object value) {
        hashMap.put(key, value);
    }

    // Méthode pour effacer tous les éléments du HashMap
    public void destroy() {
        hashMap.clear();
    }
}
