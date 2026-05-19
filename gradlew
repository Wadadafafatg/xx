#!/usr/bin/env sh

set -e

if command -v gradle >/dev/null 2>&1; then
  gradle "$@"
else
  echo "Gradle is not installed and wrapper JAR is not present. Install gradle in Termux to run builds." >&2
  exit 1
fi
