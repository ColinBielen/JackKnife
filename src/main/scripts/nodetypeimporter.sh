#!/bin/sh
JK_HOME=../
java -cp $JK_HOME/lib/jcr-1.0.jar:\
$JK_HOME/lib/commons-logging-1.0.jar:\
$JK_HOME/lib/jline-0.9.91.jar:\
$JK_HOME/lib/jnp-client.jar:\
$JK_HOME/lib/commons-collections-3.1.jar:\
$JK_HOME/lib-run/jbossall-client.jar:\
$JK_HOME/lib-run/jackrabbit-jcr-rmi-1.3.3.jar:\
$JK_HOME/lib-run/jackrabbit-jcr-commons-1.3.3.jar:\
$JK_HOME/lib-run/jbossall-client.jar:\
$JK_HOME/lib-run/slf4j-api-1.4.3.jar:\
$JK_HOME/lib-run/slf4j-jdk14-1.4.3.jar:\
$JK_HOME/lib-run/lucene.jar:\
$JK_HOME/lib-run/jackrabbit-text-extractors-1.3.3.jar:\
$JK_HOME/lib/jackrabbit-core-1.3.3.jar:\
$JK_HOME/lib/jackrabbit-api-1.3.3.jar:\
$JK_HOME/lib-run/derby.jar:\
$JK_HOME/dist/jcrcnfimporter.jar \
com.ceg.online.jackknife.importer.nodetype.JackrabbitCNFImporter $*
