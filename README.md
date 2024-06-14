java version = 20
tomcat version = 10.1

Déclarer le servlet suivant dans votre web.xml: mg.itu.prom16.controller.FrontControllerServlet (servlet-class)

Ajouter un context-param avec controllerPackage comme param-name et le nom du package comme param-value 

copier myServlet.jar du framework au lib de votre projet 

Annoter votre Controller avec @MyControllerAnnotation

Annoter vos méthodes avec @Get("/emp/list") où "/emp/list" est l'url passé après le nom du projet
Ces méthodes auront pour type de retour String ou ModelView(qui contient l'url de redirection et les données à afficher)

Pour les méthodes de votre controller utilisant ayant des parametres(Cas de formulaire):
Soit:
    - Utiliser le meme nom que le input
    - Annoter le parametre avec @Param(paramName = "exampleName") où exampleName est le name du input