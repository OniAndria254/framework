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

Possibilité de recevoir un ou des objets en paramètre:
    - Utiliser nomObjet.nomAttribut en name du input
    - Annoter le parametre objet avec @Param(paramName = "exampleName") où exampleName.nomAttribut est le name du input

L'utilisation de l'annotation @Restapi à une méthode de controller permettra de retourner un json au lieu de vue. 

Utiliser les annotations @Get ou @Post sur les méthodes
Si aucun des 2 n'est présent, on utilisera @Get par défaut

Utiliser @Url("/exemple") pour l'url