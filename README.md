java version = 20
tomcat version = 10.1

Déclarer le servlet suivant dans votre web.xml: mg.itu.prom16.controller.FrontControllerServlet (servlet-class)

Ajouter un context-param avec controllerPackage comme param-name et le nom du package comme param-value 

copier myServlet.jar du framework au lib de votre projet 

Annoter votre Controller avec @MyControllerAnnotation

Annoter vos méthodes avec @Get("list") où list est l'url passé après le nom du projet
