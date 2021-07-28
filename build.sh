#!/bin/sh

sbt assembly
jar_file=$(ls -t target/scala-*/*-assembly-* | head -n1)
cp ${jar_file} checkConf.jar