#!/bin/bash

# Script to set the next snapshot version for KWebParser.
# Should be called right after ./release.sh
#
# Prerequisites
#   - Clean git status (no uncommitted changes in branch 'master')
#
# Parameters
#   1. Next version w/o '-SNAPSHOT'
#
# What it does
#   1. Bump version to '<version>-SNAPSHOT'
#   2. Commit version change



VERSION=$1
SNAPSHOT_VERSION=$1-SNAPSHOT



# Prerequisites
if [[ "$#" -ne 1 ]]; then
    echo "Illegal number of parameters. Please use $0 <version>"
    exit 1
fi

# See https://semver.org/#is-there-a-suggested-regular-expression-regex-to-check-a-semver-string
# and https://gist.github.com/rverst/1f0b97da3cbeb7d93f4986df6e8e5695
if ! [[ "$VERSION" =~ ^(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)$ ]]; then
    echo "Illegal version number. Version must match <major>.<minor>.<patch> and must not end with '-SNAPSHOT'."
    exit 1
fi

printf "\n# Check clean status\n\n"
git checkout master
if ! git diff --no-ext-diff --quiet --exit-code; then
    echo "Unable to release. You have uncommitted changes in the branch 'master'."
    exit 1
fi

printf "\n\n\n# Bump to %s\n\n" "$SNAPSHOT_VERSION"
sed -E -i.versionsBackup "s/\"io.github.grahamdaley:lambda-runtime-graalvm:.*-SNAPSHOT\"/\"kwebparser:$SNAPSHOT_VERSION\"/" README.md
sed -i.versionsBackup "s/^version = \".*\"$/version = \"$SNAPSHOT_VERSION\"/" build.gradle.kts
find . -name "*.versionsBackup" -exec rm {} \;
git commit -am "Next is $SNAPSHOT_VERSION"
git push origin master

printf "\n\n\n<<--==  Bump lambda-runtime-graalvm to %s  ==-->>\n\n" "$SNAPSHOT_VERSION"
