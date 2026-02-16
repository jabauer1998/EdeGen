#!/bin/bash

location=$(pwd)

echo "Checking if Java Exists..."
javaExists=$(java -version 2>&1 | head -1)

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

if [ -n "$javaExists" ]; then
    echo "Java Found..."
    echo "Searching for Command..."
    if [ $# -eq 0 ]; then
        echo "No command found, expected 1 argument..."
        echo "Example of command can be 'build', 'publish' or 'clean'"
    else
        command=$1
        echo "Command is $command"
        if [ "$command" = "build" ]; then
            srcRoot=$(pwd)
            if [ -f build/BuildList.txt ]; then
                rm -f build/BuildList.txt
            fi
            touch build/BuildList.txt
            find "$srcRoot/src" -type f -name "*.java" ! -name "#*" ! -name "*~" > build/BuildList.txt
            for line in $(cat build/BuildList.txt); do
                if [ -f "$line" ]; then
                    sed -i '1s/^\xEF\xBB\xBF//' "$line"
                fi
            done
            cat "build/BuildList.txt"
            javac "@build/BuildList.txt" -d "./tmp" -sourcepath "./src" -cp "./lib/*" -encoding "UTF-8"
            for jar in lib/*.jar; do
                if [ -f "$jar" ]; then
                    jar xf "$jar" -C "./tmp"
                    if [ -f "./tmp/META-INF/MANIFEST.MF" ]; then
                        rm -f "./tmp/META-INF/MANIFEST.MF"
                    fi
                fi
            done
            jar cfe "./bin/EdeGen.jar" "ede.gen.driver.EdeGenerator" -C "./tmp" "."
            rm -rf ./tmp/*
        elif [ "$command" = "clean" ]; then
            find ./src -name "*.class" -type f -delete
            find ./bin -name "*.class" -type f -delete
            find . -name "*~" -type f -delete
            find . -name "*#" -type f -delete
        else
            echo "Unknown command '$command'"
        fi
    fi
fi

cd "$location"
