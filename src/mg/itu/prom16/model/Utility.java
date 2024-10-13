package mg.itu.prom16.model;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.HashMap;

public class Utility {
    private static final Gson gson = new Gson();
    public static boolean isPrimitive(Class<?> clazz) {
        // Liste des types primitifs en Java
        List<Class<?>> primitiveTypes = Arrays.asList(
                boolean.class,
                byte.class,
                short.class,
                char.class,
                int.class,
                long.class,
                float.class,
                double.class,
                String.class
        );

        return primitiveTypes.contains(clazz);
    }

    public static String objectToJson(Object obj) {
        MyJson myJson = new MyJson();
        return myJson.getGson().toJson(obj);
    }

    // Méthode pour convertir une chaîne JSON en objet Java
    public static <T> T jsonToObject(String jsonString, Class<T> clazz) {
        MyJson myJson = new MyJson();
        return myJson.getGson().fromJson(jsonString, new TypeToken<T>(){}.getType());
    }

    public static String modelViewToJson(ModelView modelView) {
        MyJson myJson = new MyJson();
        return myJson.getGson().toJson(modelView.getData());
    }

    // Méthode pour transformer les données du ModelView en JSON séparé
    public static String modelViewDataToJson(ModelView modelView) {
        HashMap<String, Object> data = modelView.getData();
        MyJson myJson = new MyJson();
        return myJson.getGson().toJson(data);
    }
    
    public static String capitalize(String str) {
        char firstChar = str.charAt(0);
        return Character.toUpperCase(firstChar) + str.substring(1);
    }

    // Méthode pour parser la valeur de la requête au type approprié
    public static Object parseValue(String value, Class<?> type) throws Exception {
        if (type == int.class) {
            return Integer.parseInt(value);
        } else if (type == double.class) {
            return Double.parseDouble(value);
        } else if (type == boolean.class) {
            return Boolean.parseBoolean(value);
        } else {
            // Gérer d'autres types si nécessaire
            return value;
        }
    }

    public static CustomSession HttpSessionToCustomSession(HttpSession httpSession) {
        CustomSession customSession = new CustomSession();

        // Copier tous les attributs de la session HTTP vers la session personnalisée
        Enumeration<?> attrNames = httpSession.getAttributeNames();
        while (attrNames.hasMoreElements()) {
            String attributeName = (String) attrNames.nextElement();
            Object attributeValue = httpSession.getAttribute(attributeName);
            customSession.add(attributeName, attributeValue);
        }

        return customSession;
    }

    public static void CustomSessionToHttpSession(CustomSession customSession, HttpServletRequest request) {
        // Créer une nouvelle session HTTP
        HttpSession httpSession = request.getSession(true);

        // Ajouter tous les attributs de la session personnalisée à la session HTTP
        for (Map.Entry<String, Object> entry : customSession.hashMap.entrySet()) {
            httpSession.setAttribute(entry.getKey(), entry.getValue());
        }
        // return httpSession;
    }

    public static Method findMethod(String className, VerbMethod vm) {
        try {
            Class<?> clazz = Class.forName(className);
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (method.getName().equals(vm.getMethod())) {
                    return method;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
