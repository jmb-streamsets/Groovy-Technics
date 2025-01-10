# Groovy-Technics
This repository is dedicated to Groovy (Java) programming Technics that can be used within a StreamSets Pipeline 

## Features

- Feature 1 : Implementing groovy code to create groovy code at runtime then compile and execute it
- Feature 2 : Implementing groovy code to create a branch table technic to organize and optimize the code structure
- Feature 3 : Implementing groovy code to read groovy code from a PGP encrypted text file then compile and execute it

## A StreamSets pipeline will be used to demo all the above features

## Basic groovy scripts will also be provided for IDE basic experiments

## GPG command line utility helper

gpg --full-generate-key

gpg --list-keys --with-colons | grep '^pub' | cut -d':' -f5

gpg --export --armor "xxxxxxx" > pubkey.asc

gpg --export-secret-keys --armor "xxxxxxx" > prvkey.asc

gpg --output pgp/secretGroovyCode.pgp --encrypt --recipient "xxxxxxx" src/secretGroovyCode.groovy

# Pipeline used for the demo

## 3 different groovy evaluators will be used to demo each feature

![Screenshot from 2025-01-10 15-05-20.png](images/Screenshot%20from%202025-01-10%2015-05-20.png)