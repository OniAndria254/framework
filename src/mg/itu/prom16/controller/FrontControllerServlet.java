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
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import mg.itu.prom16.annotation.*;
import mg.itu.prom16.model.*;

public class FrontControllerServlet extends HttpServlet {
    private HashMap<String, Mapping> hashMap = new HashMap<>();

    @Override
    public void init() throws ServletException {
        super.init(); // Appeler la méthode init de la superclasse HttpServlet
        try {
            initialisation();
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

    private boolean hasAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {
        return clazz.isAnnotationPresent(annotation);
    }

    private void initialisation() throws Exception {
        // Récupération des classes et méthodes annotées
        String packageName = getServletContext().getInitParameter("controllerPackage");
        List<Class<?>> classes = FrontControllerServlet.getClasses(packageName);

        if (classes.size()==0) {
            throw new ServletException("Package vide ou inexistant");
        }

        for (int j = 0; j < classes.size(); j++) {
            if (this.hasAnnotation(classes.get(j), MyControllerAnnotation.class)) {
                Method[] methods = classes.get(j).getMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(Url.class)) {
                        String url = method.getAnnotation(Url.class).chemin();
                        String verb = "";
                        if (method.isAnnotationPresent(Post.class)) {
                            verb = "Post";
                        }
                        else {
                            verb = "Get";
                        }
                        String className = classes.get(j).getName();
                        String methodName = method.getName();

                        if (url.isEmpty() == false) {
                            VerbMethod vm = new VerbMethod(verb , methodName);
                            Mapping mapping=new Mapping(className,vm);
                            if (hashMap.containsKey(url)) {
                                Mapping temp =hashMap.get(url);
                                if (temp.hasVerbMethod(vm) == false) {
                                    temp.addVerbMethod(vm);
                                }else{
                                    throw new Exception("L'URL \""+ url +"\" est deja utilise par le verb '"+verb+"'");
                                }
                            }else{
                                hashMap.put(url, mapping);
                            }
                        }
                    
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

            // Récupération de l'instance de la classe du contrôleur
            try {
                VerbMethod single =  mapping.getSingleVerbMethod(request.getMethod());

                Method method=Utility.findMethod(mapping.getClasse(), single);

                Class<?> controllerClass = Class.forName(mapping.getClasse());
                // Object control = Class.forName(mapping.getClasse()).getDeclaredConstructor().newInstance();
                
                Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();

                Field[] fieldss = controllerInstance.getClass().getDeclaredFields();
                for (Field field : fieldss) {
                    if (field.getType().equals(AttributeSession.class)) {
                        field.setAccessible(true);
                        field.set(controllerInstance, new AttributeSession(request));
                    }
                }
                // Récupération de toutes les méthodes déclarées dans la classe
                Method[] declaredMethods = controllerClass.getDeclaredMethods();

                Method vraiMethod = method;
                

                Parameter[] parameters = vraiMethod.getParameters();
                Object[] args = new Object[parameters.length];

                ArrayList<String> parameterNames = new ArrayList<>();
                String paramValue = null;

                for (int i = 0; i < parameters.length; i++) {
                    Parameter parameter = parameters[i];
                    parameterNames.add(parameter.getName());

                    if (Utility.isPrimitive(parameter.getType())) {
                        // paramValue = request.getParameter(parameter.getName());
                        // System.out.print("String: " + parameter.getName());
                        // args[i] = paramValue;
                        // Vérifier si l'annotation Param est présente
                        if (parameter.isAnnotationPresent(Param.class)) {
                            Param annotation = parameter.getAnnotation(Param.class);
                            // Utiliser le nom spécifié dans l'annotation pour récupérer la valeur de la requête
                            paramValue = request.getParameter(annotation.paramName());
                            args[i] = paramValue;
                        }
                        else {
                            // System.out.println("atoo");
                            throw new ServletException("ETU002382: Méthode avec des parametres non-annotés");
                        }
                    }
                    else {
                        if (parameter.isAnnotationPresent(Param.class)) {
                            // Vérifier si le paramètre est un objet
                            Class<?> parameterType = parameter.getType();
                                                                    
                            Object obj = parameterType.getDeclaredConstructor().newInstance();
                            Field[] fields = obj.getClass().getDeclaredFields();
                            Method[] methods = obj.getClass().getDeclaredMethods();

                            for (Field field : fields) {
                                // System.out.println(parameter.getAnnotation(Param.class).paramName() + "." + field.getName());
                                Object value = Utility.parseValue(request.getParameter(parameter.getAnnotation(Param.class).paramName() + "." + field.getName()), field.getType());
                                setObjectField(obj, methods, field, value);
                            }
                            args[i] = obj;
                        }
                        else {
                            Class<?> parameterType = parameter.getType();
                            if (parameterType == CustomSession.class) {
                                CustomSession customSession = Utility.HttpSessionToCustomSession(request.getSession(false));
                                args[i] = customSession;
                            }
                            // throw new ServletException("ETU002382: Méthode avec des parametres non-annotés");
                        }
                        
                    }
                }

                // Ajouter la vérification de l'annotation Restapi ici
                if (vraiMethod.isAnnotationPresent(Restapi.class)) {
                    // Récupérer la valeur de retour de la méthode
                    Object result = vraiMethod.invoke(controllerInstance, args);
                    
                    if (result instanceof ModelView) {
                        ModelView modelView = (ModelView) result;
                        
                        // Transformer les données en JSON
                        String jsonData = Utility.modelViewToJson(modelView);
                        
                        // Configurer le type de réponse comme text/json
                        response.setContentType("text/json");
                        
                        // Écrire la réponse
                        PrintWriter pw = response.getWriter();
                        pw.print(jsonData);
                    } else {
                        // Transformer directement en JSON si ce n'est pas un ModelView
                        String jsonData = Utility.objectToJson(result);
                        
                        // Configurer le type de réponse comme text/json
                        response.setContentType("text/json");
                        
                        // Écrire la réponse
                        PrintWriter pw = response.getWriter();
                        pw.print(jsonData);
                    }
                    
                    // Arrêter l'exécution après avoir envoyé la réponse JSON
                    return;
                }

                // Invocation de la méthode
                Object result = vraiMethod.invoke(controllerInstance, args);

                for (Object arg : args) {
                    if (arg instanceof CustomSession) {
                        Utility.CustomSessionToHttpSession((CustomSession)arg, request);
                    }
                }


                // Traitement spécifique selon le type de données retourné par la méthode @Get
                if (result instanceof String) {
                    printWriter.println("Controller: " + mapping.getClasse() + ", Methode: " + vraiMethod.getName());
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
                    throw new ServletException("Type de retour non-géré");
                }
            } catch (Exception e) {
                throw new ServletException(e.getCause() + " ;" + e.getMessage());
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    
            // Write a custom message to the response body
            response.setContentType("text/plain");
            response.getWriter().println("Erreur 404 : Lien inexistant");
            response.getWriter().flush();
        }
    }

    public static Object setObjectField(Object obj, Method[] methods, Field field, Object value) throws Exception {
        String setterMethod = "set" + Utility.capitalize(field.getName());

        for (Method method : methods) {
            if (!method.getName().equals(setterMethod)) {
                continue;
            }   
            return method.invoke(obj, value);
            
        }
        throw new Exception("Aucun setter trouvé pour l'attribut: " + field.getName());
    }

    

    // private void printClasses(PrintWriter printWriter, List<String> list) {
    //     for (int i = 0; i < list.size(); i++) {
    //         printWriter.println(list.get(i));
    //     }
    // }

    public static List<Class<?>> getClasses(String packageName) throws Exception{
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
            throw new ServletException("Package inexistant");
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
                System.out.println("");
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
