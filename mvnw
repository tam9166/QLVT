#!/bin/sh

BASEDIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
WRAPPER_JAR="$BASEDIR/.mvn/wrapper/maven-wrapper.jar"

if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
  JAVACMD="$JAVA_HOME/bin/java"
else
  JAVACMD=java
fi

run_local_maven() {
  if [ -n "$MAVEN_HOME" ] && [ -x "$MAVEN_HOME/bin/mvn" ]; then
    exec "$MAVEN_HOME/bin/mvn" "$@"
  fi
  if [ -n "$M2_HOME" ] && [ -x "$M2_HOME/bin/mvn" ]; then
    exec "$M2_HOME/bin/mvn" "$@"
  fi
  if command -v mvn >/dev/null 2>&1; then
    exec mvn "$@"
  fi
  return 1
}

if run_local_maven "$@"; then
  exit 0
fi

if [ -f "$WRAPPER_JAR" ]; then
  exec "$JAVACMD" -classpath "$WRAPPER_JAR" "-Dmaven.multiModuleProjectDirectory=$BASEDIR" org.apache.maven.wrapper.MavenWrapperMain "$@"
fi

{
  echo "Maven was not found and Maven Wrapper could not run. Check JAVA_HOME or network/certificate access to the configured Maven distribution." >&2
  exit 1
}
