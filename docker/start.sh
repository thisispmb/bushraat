#!/bin/sh
set -eu

PORT="${PORT:-8080}"
sed -i "s/port=\"8080\"/port=\"${PORT}\"/" "$CATALINA_HOME/conf/server.xml"

exec "$CATALINA_HOME/bin/catalina.sh" run
