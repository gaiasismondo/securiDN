echo off

set DNCLASSPATH=.
set DNCLASSPATH=%DNCLASSPATH%;jars/pnml_framework/lib/org.eclipse.core.resources_3.3.0.v20070604.jar
set DNCLASSPATH=%DNCLASSPATH%;jars/pnml_framework/lib/org.eclipse.core.runtime_3.3.100.v20070530.jar
set DNCLASSPATH=%DNCLASSPATH%;jars/pnml_framework/lib/org.eclipse.emf.common_2.3.0.v200709252135.jar
set DNCLASSPATH=%DNCLASSPATH%;jars/pnml_framework/lib/org.eclipse.emf.ecore_2.3.1.v200709252135.jar
set DNCLASSPATH=%DNCLASSPATH%;jars/pnml_framework/lib/org.eclipse.emf.ecore.xmi_2.3.1.v200709252135.jar
set DNCLASSPATH=%DNCLASSPATH%;jars/pnml_framework/lib/org.eclipse.emf.mapping.ecore2xml_2.3.0.v200709252135.jar
set DNCLASSPATH=%DNCLASSPATH%;jars/pnml_framework/lib/org.eclipse.xsd_2.3.1.v200709252135.jar
set DNCLASSPATH=%DNCLASSPATH%;jars/pnml_framework/fr.lip6.move.pnml.framework_1.2.4.jar
set DNCLASSPATH=%DNCLASSPATH%;jars/pnml_framework/fr.lip6.move.pnml.framework_1.2.4src.jar
set DNCLASSPATH=%DNCLASSPATH%;jars/idw-gpl.jar
set DNCLASSPATH=%DNCLASSPATH%;jars/l2fprod-common-all.jar
set DNCLASSPATH=%DNCLASSPATH%;jars/skinlf.jar
set DNCLASSPATH=%DNCLASSPATH%;jars/batik/batik-awt-util.jar
set DNCLASSPATH=%DNCLASSPATH%;jars/batik/batik-bridge.jar
set DNCLASSPATH=%DNCLASSPATH%;jars/batik/batik-dom.jar
set DNCLASSPATH=%DNCLASSPATH%;jars/batik/batik-ext.jar
set DNCLASSPATH=%DNCLASSPATH%;jars/batik/batik-extension.jar
set DNCLASSPATH=%DNCLASSPATH%;jars/batik/batik-gui-util.jar
set DNCLASSPATH=%DNCLASSPATH%;jars/batik/batik-gvt.jar
set DNCLASSPATH=%DNCLASSPATH%;jars/batik/batik-parser.jar
set DNCLASSPATH=%DNCLASSPATH%;jars/batik/batik-script.jar
set DNCLASSPATH=%DNCLASSPATH%;jars/batik/batik-svg-dom.jar
set DNCLASSPATH=%DNCLASSPATH%;jars/batik/batik-svggen.jar
set DNCLASSPATH=%DNCLASSPATH%;jars/batik/batik-svgpp.jar
set DNCLASSPATH=%DNCLASSPATH%;jars/batik/batik-swing.jar
set DNCLASSPATH=%DNCLASSPATH%;jars/batik/batik-transcoder.jar
set DNCLASSPATH=%DNCLASSPATH%;jars/batik/batik-util.jar
set DNCLASSPATH=%DNCLASSPATH%;jars/batik/batik-xml.jar
set DNCLASSPATH=%DNCLASSPATH%;jars/batik/xerces_2_5_0.jar
set DNCLASSPATH=%DNCLASSPATH%;jars/batik/xml-apis.jar

echo on

java -classpath %DNCLASSPATH% RunDrawNET %1 %2 %3
