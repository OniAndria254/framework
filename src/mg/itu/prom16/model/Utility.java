package mg.itu.prom16.model;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import mg.itu.prom16.annotation.Daty;
import mg.itu.prom16.annotation.Length;
import mg.itu.prom16.annotation.Numeric;
import mg.itu.prom16.annotation.Range;
import mg.itu.prom16.annotation.Required;

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

    public static void validate(Object obj) throws IllegalArgumentException, IllegalAccessException {
        Class<?> clazz = obj.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            Object value = field.get(obj);

            // Validation @Numeric
            if (field.isAnnotationPresent(Numeric.class)) {
                if (value != null && value instanceof String) {
                    String stringValue = (String) value;
                    try {
                        // Vérifier si la chaîne contient uniquement des chiffres
                        if (!stringValue.matches("\\d+")) {
                            throw new IllegalArgumentException("Le champ " + field.getName() + " doit contenir uniquement des chiffres.");
                        }
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Le champ " + field.getName() + " doit être numérique.");
                    }
                } else if (value != null) {
                    throw new IllegalArgumentException("Le champ " + field.getName() + " doit être une chaîne contenant uniquement des chiffres.");
                }
            }


            // Validation @Date
            if (field.isAnnotationPresent(Daty.class)) {
                Daty dateAnnotation = field.getAnnotation(Daty.class);
                String format = dateAnnotation.format();
                if (value != null && value instanceof String) {
                    SimpleDateFormat sdf = new SimpleDateFormat(format);
                    sdf.setLenient(false);
                    try {
                        sdf.parse((String) value);
                    } catch (ParseException e) {
                        throw new IllegalArgumentException("Le champ " + field.getName() + " doit être une date au format " + format);
                    }
                }
            }

            // Validation @Range
            if (field.isAnnotationPresent(Range.class)) {
                Range rangeAnnotation = field.getAnnotation(Range.class);
                int min = rangeAnnotation.min();
                int max = rangeAnnotation.max();

                if (value != null && value instanceof Integer) {
                    int intValue = (Integer) value;
                    if (intValue < min || intValue > max) {
                        throw new IllegalArgumentException("Le champ " + field.getName() + " doit être compris entre " + min + " et " + max + ".");
                    }
                } else if (value != null) {
                    throw new IllegalArgumentException("Le champ " + field.getName() + " doit être un entier pour utiliser @Range.");
                }
            }

            // Validation @Required
            if (field.isAnnotationPresent(Required.class)) {
                if (value == null || (value instanceof String && ((String) value).trim().isEmpty())) {
                    throw new IllegalArgumentException("Le champ " + field.getName() + " est obligatoire.");
                }
            }

            // Validation @Length
            if (field.isAnnotationPresent(Length.class)) {
                Length lengthAnnotation = field.getAnnotation(Length.class);
                int min = lengthAnnotation.min();
                int max = lengthAnnotation.max();

                if (value != null && value instanceof String) {
                    String stringValue = (String) value;
                    int length = stringValue.length();

                    if (length < min || length > max) {
                        throw new IllegalArgumentException("Le champ " + field.getName() + " doit avoir une longueur entre " + min + " et " + max + " caractères.");
                    }
                } else if (value != null) {
                    throw new IllegalArgumentException("Le champ " + field.getName() + " n'est pas de type String, mais il est annoté avec @Length.");
                }
            }


        }
    }
}
