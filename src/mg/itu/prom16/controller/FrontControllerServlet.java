package mg.itu.prom16.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mg.itu.prom16.annotation.*;
import mg.itu.prom16.model.*;

public class FrontControllerServlet extends HttpServlet {
    private HashMap<String, Mapping> hashMap = new HashMap<>();
    private ArrayList<String> exceptions = new ArrayList<>();

    @Override
    public void init() throws ServletException {
        super.init(); // Appeler la méthode init de la superclasse HttpServlet
        // Initialisation du HashMap
        initialisation();
    }

    private boolean hasAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {
        return clazz.isAnnotationPresent(annotation);
    }

    private void initialisation() {
        // Récupération des classes et méthodes annotées
        String packageName = getServletContext().getInitParameter("controllerPackage");
        List<Class<?>> classes = FrontControllerServlet.getClasses(packageName);
        System.out.println(classes.size());

        for (int j = 0; j < classes.size(); j++) {
            if (this.hasAnnotation(classes.get(j), MyControllerAnnotation.class)) {
                Method[] methods = classes.get(j).getMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(Get.class)) {
                        String url = method.getAnnotation(Get.class).value();
                        String className = classes.get(j).getName();
                        String methodName = method.getName();

                        // Création d'une instance de Mapping et ajout au HashMap
                        Mapping mapping = new Mapping(className, methodName);
                        if(hashMap.containsKey(url)) {
                            exceptions.add("Duplication d'url");
                        }
                        hashMap.put(url, mapping);
                    }
                }
            }
        }
    }
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        PrintWriter printWriter = response.getWriter();
        String requestURI = request.getRequestURI();
        String contextPath= request.getContextPath();
        String url = requestURI.substring(contextPath.length());
        // String lastPart = getURLSplit(url); // Assurez-vous que cette méthode est définie ailleurs

        // Vérification si l'URL existe dans le HashMap
        if (hashMap.containsKey(url)) {
            Mapping mapping = hashMap.get(url);
            printWriter.println("Controller: " + mapping.getClasse() + ", Methode: " + mapping.getMethode());

            // Récupération de l'instance de la classe du contrôleur
            try {
                Class<?> controllerClass = Class.forName(mapping.getClasse());
                Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();

                // Récupération de la méthode à invoquer
                Method method = controllerClass.getMethod(mapping.getMethode());
                // Invocation de la méthode
                Object result = method.invoke(controllerInstance);

                // Traitement spécifique selon le type de données retourné par la méthode @Get
                if (result instanceof String) {
                    printWriter.println(result);
                } else if (result instanceof ModelView) {
                    ModelView modelView = (ModelView) result;
                    // Extrait de l'URL du ModelView
                    String targetUrl = modelView.getUrl();
                    // Redirection vers l'URL cible avec les données comme attributs de requête
                    RequestDispatcher dispatcher = request.getRequestDispatcher(targetUrl);
                    for (String key : modelView.getData().keySet()) {
                        // Convertir la clé en chaîne de caractères pour l'utilisation avec request.setAttribute
                        Object attributeValue =modelView.getData().get(key); // La valeur reste l'objet

                        // Utiliser setAttribute pour chaque entrée du HashMap
                        request.setAttribute(key, attributeValue);
                    }
                    dispatcher.forward(request, response);
                } else {
                    exceptions.add("Type de retour non-gere");
                }
            } catch (Exception e) {
                printWriter.println("Erreur lors de l'invoquation de la méthode: " + e.getCause());
                e.printStackTrace();
            }
        } else {
            exceptions.add("URL non existante");
        }
        for (String string : exceptions) {
            printWriter.println(string);
        }
    }


    // private void printClasses(PrintWriter printWriter, List<String> list) {
    //     for (int i = 0; i < list.size(); i++) {
    //         printWriter.println(list.get(i));
    //     }
    // }

    public static List<Class<?>> getClasses(String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        URL path = Thread.currentThread().getContextClassLoader().getResource(packageName.replace('.', File.separatorChar));
        if (path == null) {
            System.err.println("Ressource not found for package: "+packageName);
            return classes;
        }
        File directory;
        try {
            directory = new File(URLDecoder.decode(path.getFile(), "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            return classes;
        }

        if (!directory.exists()) {
            System.err.println("Directory not found: " + directory.getAbsolutePath());
            return classes;
        }

        collectClasses(packageName, directory, classes);
        return classes;
    }

    private static void collectClasses(String packageName, File directory, List<Class<?>> classes) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for(File file : files) {
            if(file.isDirectory()) {
                collectClasses(packageName + '.' + file.getName(), file, classes);
            }
            else if(file.getName().endsWith(".class"))
            {
                System.out.println("Error");
                try {
                    String className = packageName + '.' + file.getName().substring(0, file.getName().length()-6);
                    classes.add(Class.forName(className));
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
            }
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    
}
