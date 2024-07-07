package mg.itu.prom16.model;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class AttributeSession {
    public HttpSession httpSession;

    public AttributeSession(HttpServletRequest request) {
        this.httpSession = request.getSession();
    }
    
    public Object get(String key) {
        return httpSession.getAttribute(key);
    }

    public void add(String key, Object value) {
        httpSession.setAttribute(key, value);
    }

    // Méthode pour supprimer un attribut de la session
    public void remove(String key) {
        httpSession.removeAttribute(key);
    }

    // Méthode pour mettre à jour la valeur d'un attribut existant
    public void update(String key, Object newValue) {
        httpSession.setAttribute(key, newValue);
    }

    // Méthode pour terminer la session HTTP, supprimant ainsi tous les attributs de la session
    public void destroy() {
        httpSession.invalidate();
    }
}
