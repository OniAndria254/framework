package mg.itu.prom16.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FrontControllerServlet extends HttpServlet {
    public boolean checked;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String url = request.getRequestURL().toString();

        // Récupérer le nom du package des contrôleurs à partir du context-param
        String controllerPackage = getServletContext().getInitParameter("controllerPackage");
        List<String> controllerNames = loadControllerNames(controllerPackage);

        // Envoyer les noms des contrôleurs au client
        PrintWriter out = response.getWriter();
        out.println("Liste des contrôleurs : " + controllerNames.toString());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    private List<String> loadControllerNames(String packageName) {
        List<String> controllerNames = new ArrayList<>();
        try {
            // Charger les classes du package spécifié
            Class<?>[] classes = Class.forName(packageName + ".*").getClasses();
            for (Class<?> clazz : classes) {
                // Vérifier si la classe est annotée avec MyControllerAnnotation
                if (clazz.isAnnotationPresent(MyControllerAnnotation.class)) {
                    // Ajouter le nom de la classe à la liste
                    controllerNames.add(clazz.getName());
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return controllerNames;
    }
}
