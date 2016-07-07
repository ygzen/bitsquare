#!/bin/bash

set -e

version="0.4.9"

cd ../../

projectDir=$(pwd)

deployDir="$projectDir/gui/deploy"
customJDK="$deployDir/jdk1.8.92_minimized.jdk"
customJavaHome="$customJDK/Contents/Home"

mkdir -p gui/deploy

mvn clean package -DskipTests -Dmaven.javadoc.skip=true

cp gui/target/shaded.jar "gui/deploy/Bitsquare-$version.jar"
cp seednode/target/SeedNode.jar "gui/deploy/SeedNode-$version.jar"

cp gui/target/shaded.jar "/Users/mk/vm_shared_ubuntu/Bitsquare-$version.jar"
cp gui/target/shaded.jar "/Users/mk/vm_shared_windows/Bitsquare-$version.jar"
cp gui/target/shaded.jar "/Users/mk/vm_shared_ubuntu14_32bit/Bitsquare-$version.jar"
cp gui/target/shaded.jar "/Users/mk/vm_shared_windows_32bit/Bitsquare-$version.jar"

echo "JAVA_HOME: $JAVA_HOME"
echo "CustomJavaHome $customJavaHome"

$JAVA_HOME/bin/javapackager \
    -deploy \
    -BappVersion=$version \
    -Bmac.CFBundleIdentifier=io.bitsquare \
    -Bmac.CFBundleName=Bitsquare \
    -Bicon=package/mac/Bitsquare.icns \
    -Bruntime="$customJavaHome/jre" \
    -native dmg \
    -name Bitsquare \
    -title Bitsquare \
    -vendor Bitsquare \
    -outdir gui/deploy \
    -srcfiles "gui/deploy/Bitsquare-$version.jar" \
    -appclass io.bitsquare.app.BitsquareAppMain \
    -outfile Bitsquare \
    -BjvmProperties=-Djava.net.preferIPv4Stack=true

mv "gui/deploy/bundles/Bitsquare-$version.dmg" "gui/deploy/Bitsquare-$version.dmg"
rm "gui/deploy/Bitsquare.html"
rm "gui/deploy/Bitsquare.jnlp"
rm -r "gui/deploy/bundles"

cd package/mac