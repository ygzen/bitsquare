#!/bin/bash

buildDir=$(pwd)
deployDir="$buildDir/../../gui/deploy"
customJDK="$deployDir/jdk1.8.92_minimized.jdk"
customJavaHome="$customJDK/Contents/Home"


mkdir -p $deployDir
sudo rm -r $customJDK
sudo mkdir -p $customJDK

echo "---- Add hot fix classed to JDK ----"
echo "Copy system JDK to $customJDK"
sudo  cp -rf "$JAVA_HOME/../../" "$customJDK"

echo "Build jdkfix module"
mvn -f ../../ clean package -pl jdkfix -am -DskipTests -Dmaven.javadoc.skip=true

cd $customJavaHome/jre/lib/ext/

echo "Copy jdkfix classes to jfxrt.jar"
sudo mkdir -p $customJavaHome/jre/lib/ext/javafx/collections
sudo cp -r $buildDir/../../jdkfix/target/classes/javafx/collections/transformation $customJavaHome/jre/lib/ext/javafx/collections

sudo zip -ur $customJavaHome/jre/lib/ext/jfxrt.jar javafx/collections/transformation

#sudo unzip $customJavaHome/jre/lib/ext/jfxrt.jar -d $customJavaHome/jre/lib/ext/unzipped
#open $customJavaHome/jre/lib/ext/unzipped/javafx/collections/transformation

sudo rm -r $customJavaHome/jre/lib/ext/javafx

echo "---- Minimize ----"
# save disk space 
sudo rm -r $customJavaHome/javafx-src.zip
sudo rm -r $customJavaHome/src.zip



