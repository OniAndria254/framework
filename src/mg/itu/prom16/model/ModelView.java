package mg.itu.prom16.model;

import java.io.Serializable;
import java.util.HashMap;

public class ModelView {
    private String url;
    private HashMap<String, Object> data = new HashMap<>();  
    private String errorUrl;
    
    public String getErrorUrl() {
        return errorUrl;
    }

    public void setErrorUrl(String errorUrl) {
        this.errorUrl = errorUrl;
    }

    public ModelView(String url, HashMap<String, Object> data) {
        this.url = url;
        this.data = data;
    }

    public ModelView() {
    }

    public ModelView(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HashMap<String, Object> getData() {
        return data;
    }

    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }
    
    // Fonction addObject pour ajouter des données au HashMap
    public void addObject(String key, Object value) {
        data.put(key, value);
    }
}
