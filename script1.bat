#!/bin/bash

# Chemin vers le répertoire contenant le servlet
SERVLET_DIR="C:/e-bossy/S4/Web Dyn/framework/framework/src"

# Chemin vers le répertoire de sortie pour le fichier .jar
OUTPUT_DIR="C:/e-bossy/S4/Web Dyn/framework/framework/lib"

# Nom du fichier .jar
JAR_NAME="MyServlet.jar"

# Compilation du servlet
echo "Compiling servlet..."
javac -d $OUTPUT_DIR $SERVLET_DIR/*.java

# Création du fichier .jar
echo "Creating .jar file..."
cd $OUTPUT_DIR
jar cvf $JAR_NAME *

echo "Servlet compiled and .jar file created successfully."
