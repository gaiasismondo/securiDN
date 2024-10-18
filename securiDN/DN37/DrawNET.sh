#!/bin/sh

DNCLASSPATH=.
DNCLASSPATH=$DNCLASSPATH:jars/pnml_framework/lib/org.eclipse.core.resources_3.3.0.v20070604.jar
DNCLASSPATH=$DNCLASSPATH:jars/pnml_framework/lib/org.eclipse.core.runtime_3.3.100.v20070530.jar
DNCLASSPATH=$DNCLASSPATH:jars/pnml_framework/lib/org.eclipse.emf.common_2.3.0.v200709252135.jar
DNCLASSPATH=$DNCLASSPATH:jars/pnml_framework/lib/org.eclipse.emf.ecore_2.3.1.v200709252135.jar
DNCLASSPATH=$DNCLASSPATH:jars/pnml_framework/lib/org.eclipse.emf.ecore.xmi_2.3.1.v200709252135.jar
DNCLASSPATH=$DNCLASSPATH:jars/pnml_framework/lib/org.eclipse.emf.mapping.ecore2xml_2.3.0.v200709252135.jar
DNCLASSPATH=$DNCLASSPATH:jars/pnml_framework/lib/org.eclipse.xsd_2.3.1.v200709252135.jar
DNCLASSPATH=$DNCLASSPATH:jars/pnml_framework/fr.lip6.move.pnml.framework_1.2.4.jar
DNCLASSPATH=$DNCLASSPATH:jars/pnml_framework/fr.lip6.move.pnml.framework_1.2.4src.jar
DNCLASSPATH=$DNCLASSPATH:jars/idw-gpl.jar
DNCLASSPATH=$DNCLASSPATH:jars/l2fprod-common-all.jar
DNCLASSPATH=$DNCLASSPATH:jars/skinlf.jar
DNCLASSPATH=$DNCLASSPATH:jars/batik/batik-awt-util.jar
DNCLASSPATH=$DNCLASSPATH:jars/batik/batik-bridge.jar
DNCLASSPATH=$DNCLASSPATH:jars/batik/batik-dom.jar
DNCLASSPATH=$DNCLASSPATH:jars/batik/batik-ext.jar
DNCLASSPATH=$DNCLASSPATH:jars/batik/batik-extension.jar
DNCLASSPATH=$DNCLASSPATH:jars/batik/batik-gui-util.jar
DNCLASSPATH=$DNCLASSPATH:jars/batik/batik-gvt.jar
DNCLASSPATH=$DNCLASSPATH:jars/batik/batik-parser.jar
DNCLASSPATH=$DNCLASSPATH:jars/batik/batik-script.jar
DNCLASSPATH=$DNCLASSPATH:jars/batik/batik-svg-dom.jar
DNCLASSPATH=$DNCLASSPATH:jars/batik/batik-svggen.jar
DNCLASSPATH=$DNCLASSPATH:jars/batik/batik-svgpp.jar
DNCLASSPATH=$DNCLASSPATH:jars/batik/batik-swing.jar
DNCLASSPATH=$DNCLASSPATH:jars/batik/batik-transcoder.jar
DNCLASSPATH=$DNCLASSPATH:jars/batik/batik-util.jar
DNCLASSPATH=$DNCLASSPATH:jars/batik/batik-xml.jar
DNCLASSPATH=$DNCLASSPATH:jars/batik/xerces_2_5_0.jar
DNCLASSPATH=$DNCLASSPATH:jars/batik/xml-apis.jar
DNCLASSPATH=$DNCLASSPATH:jars/gson-2.11.0.jar

java -classpath $DNCLASSPATH RunDrawNET %1 %2 %3
