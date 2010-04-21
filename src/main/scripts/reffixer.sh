#!/bin/sh
JK_HOME=../
java -cp $JK_HOME/lib-run/crx-api-1.2.jar:\
$JK_HOME/lib-run/crx-rmi-1.2.jar:\
$JK_HOME/lib/jcr-1.0.jar:\
$JK_HOME/lib-run/crx-commons-1.2.jar:\
$JK_HOME/lib/jackrabbit-jcr-rmi-1.3.jar:\
$JK_HOME/lib/jline-0.9.91.jar:\
$JK_HOME/lib/commons-logging-1.0.jar:\
$JK_HOME/lib-run/crx-core-1.2.jar:\
$JK_HOME/lib-run/jbossall-client.jar:\
$JK_HOME/lib-run/jnp-client.jar:\
$JK_HOME/dist/jcrfixer.jar \
com.ceg.online.jackknife.fixer.BadRefFixer
