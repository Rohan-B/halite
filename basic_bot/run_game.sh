#!/bin/sh

rm *.hlt
rm MyBot.class
javac MyBot.java
./halite -d "240 160" "java MyBot" "java MyBot"
