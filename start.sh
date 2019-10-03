#! /bin/bash

java -Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector -jar /opt/irods-clients/nfsrods/nfs4j-irodsvfs-pom-1.0.0-SNAPSHOT.jar
