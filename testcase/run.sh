#!/bin/bash
e1=bfs
e2=dfs
e3=as
for f in *.txt
do
  echo "Processing $f file..."
  java -jar UnlockMe.jar -s bfs -a ./< $f 
  java -jar UnlockMe.jar -s dfs -a ./< $f
  java -jar UnlockMe.jar -s as - a ./< $f
done
