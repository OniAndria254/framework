package mg.itu.prom16.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

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
import jakarta.servlet.annotation.MultipartConfig;

@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2, // 2 MB
    maxFileSize = 1024 * 1024 * 10,      // 10 MB
    maxRequestSize = 1024 * 1024 * 50    // 50 MB
)

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
        String packageName = getServletContext().getInitParameter("controllerPackage");
        List<Class<?>> classes = FrontControllerServlet.getClasses(packageName);
    
        if (classes.isEmpty()) {
            throw new ServletException("Package vide ou inexistant");
        }
    
        for (Class<?> controllerClass : classes) {
            if (hasAnnotation(controllerClass, MyControllerAnnotation.class)) {
                processControllerClass(controllerClass);
            }
        }
    }
    
    private void processControllerClass(Class<?> controllerClass) throws Exception {
        Method[] methods = controllerClass.getMethods();
    
        for (Method method : methods) {
            if (method.isAnnotationPresent(Url.class)) {
                processControllerMethod(controllerClass, method);
            }
        }
    }
    
    private void processControllerMethod(Class<?> controllerClass, Method method) throws Exception {
        String url = method.getAnnotation(Url.class).chemin();
        
        if (!url.isEmpty()) {
            String verb = getHttpVerbFromMethod(method);
            String className = controllerClass.getName();
            String methodName = method.getName();
    
            VerbMethod vm = new VerbMethod(verb, methodName);
            Mapping mapping = new Mapping(className, vm);
    
            registerMapping(url, vm, mapping);
        }
    }
    
    private String getHttpVerbFromMethod(Method method) {
        if (method.isAnnotationPresent(Post.class)) {
            return "Post";
        } else {
            return "Get";
        }
    }
    
    private void registerMapping(String url, VerbMethod vm, Mapping mapping) throws Exception {
        if (hashMap.containsKey(url)) {
            Mapping existingMapping = hashMap.get(url);
    
            if (!existingMapping.hasVerbMethod(vm)) {
                existingMapping.addVerbMethod(vm);
            } else {
                throw new Exception("L'URL \"" + url + "\" est déjà utilisée par le verbe '" + vm.getVerb() + "'");
            }
        } else {
            hashMap.put(url, mapping);
        }
    }
    

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        PrintWriter printWriter = response.getWriter();
        String url = getUrlFromRequest(request);
        boolean isMultipart = isMultipartRequest(request);

        if (hashMap.containsKey(url)) {
            Mapping mapping = hashMap.get(url);
            try {
                handleRequestMapping(mapping, request, response, isMultipart);
            } catch (Exception e) {
                throw new ServletException(e.getCause() + " ;" + e.getMessage());
            }
        } else {
            handleNotFound(response);
        }
    }

    private String getUrlFromRequest(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        return requestURI.substring(contextPath.length());
    }

    private boolean isMultipartRequest(HttpServletRequest request) {
        return request.getContentType() != null && request.getContentType().startsWith("multipart/");
    }

    private void handleRequestMapping(Mapping mapping, HttpServletRequest request, HttpServletResponse response, boolean isMultipart) 
        throws Exception {
        VerbMethod single = mapping.getSingleVerbMethod(request.getMethod());
        Method method = Utility.findMethod(mapping.getClasse(), single);
        Object controllerInstance = createControllerInstance(mapping.getClasse(), request);
        
        Object[] args = resolveMethodArguments(method, request, isMultipart);
        
        if (method.isAnnotationPresent(Restapi.class)) {
            handleRestApiMethod(method, controllerInstance, args, response);
        } else {
            handleRegularMethod(method, controllerInstance, args, request, response);
        }
    }

    private Object createControllerInstance(String className, HttpServletRequest request) 
        throws Exception {
        Class<?> controllerClass = Class.forName(className);
        Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();

        for (Field field : controllerClass.getDeclaredFields()) {
            if (field.getType().equals(AttributeSession.class)) {
                field.setAccessible(true);
                field.set(controllerInstance, new AttributeSession(request));
            }
        }

        return controllerInstance;
    }

    private Object[] resolveMethodArguments(Method method, HttpServletRequest request, boolean isMultipart) 
        throws Exception {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];

            if (Utility.isPrimitive(parameter.getType())) {
                args[i] = resolvePrimitiveParameter(parameter, request);
            } else {
                args[i] = resolveComplexParameter(parameter, request, isMultipart);
            }
        }

        return args;
    }

    private Object resolvePrimitiveParameter(Parameter parameter, HttpServletRequest request) 
            throws ServletException {
        if (parameter.isAnnotationPresent(Param.class)) {
            Param annotation = parameter.getAnnotation(Param.class);
            return request.getParameter(annotation.paramName());
        } else {
            throw new ServletException("ETU002382: Méthode avec des parametres non-annotés");
        }
    }

    private Object resolveComplexParameter(Parameter parameter, HttpServletRequest request, boolean isMultipart) 
            throws Exception {
        if (parameter.isAnnotationPresent(Param.class)) {
            Param annotation = parameter.getAnnotation(Param.class);

            if (parameter.getType().equals(Part.class) && isMultipart) {
                return request.getPart(annotation.paramName());
            } else {
                return populateComplexObject(parameter, request);
            }
        } else if (parameter.getType().equals(CustomSession.class)) {
            return Utility.HttpSessionToCustomSession(request.getSession(false));
        }
        
        return null;
    }

    private Object populateComplexObject(Parameter parameter, HttpServletRequest request) 
            throws Exception {
        Class<?> parameterType = parameter.getType();
        Object obj = parameterType.getDeclaredConstructor().newInstance();

        for (Field field : obj.getClass().getDeclaredFields()) {
            Object value = Utility.parseValue(request.getParameter(parameter.getAnnotation(Param.class).paramName() + "." + field.getName()), field.getType());
            setObjectField(obj, obj.getClass().getDeclaredMethods(), field, value);
        }

        return obj;
    }

    private void handleRestApiMethod(Method method, Object controllerInstance, Object[] args, HttpServletResponse response) 
        throws Exception {
        Object result = method.invoke(controllerInstance, args);
        String jsonData = result instanceof ModelView
                ? Utility.modelViewToJson((ModelView) result)
                : Utility.objectToJson(result);
        
        response.setContentType("text/json");
        response.getWriter().print(jsonData);
    }

    private void handleRegularMethod(Method method, Object controllerInstance, Object[] args, HttpServletRequest request, HttpServletResponse response) 
        throws Exception {
        Object result = method.invoke(controllerInstance, args);

        if (result instanceof String) {
            response.getWriter().println(result);
        } else if (result instanceof ModelView) {
            handleModelView((ModelView) result, request, response);
        } else {
            throw new ServletException("Type de retour non-géré");
        }
    }

    private void handleModelView(ModelView modelView, HttpServletRequest request, HttpServletResponse response) 
            throws Exception {
        RequestDispatcher dispatcher = request.getRequestDispatcher(modelView.getUrl());
        for (Map.Entry<String, Object> entry : modelView.getData().entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
        }
        dispatcher.forward(request, response);
    }

    private void handleNotFound(HttpServletResponse response) 
        throws IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.setContentType("text/plain");
        response.getWriter().println("Erreur 404 : Lien inexistant");
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
