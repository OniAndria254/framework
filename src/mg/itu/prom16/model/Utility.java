package mg.itu.prom16.model;

import java.util.Arrays;
import java.util.List;

public class Utility {
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
}
