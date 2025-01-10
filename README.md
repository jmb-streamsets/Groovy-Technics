# Groovy-Technics
This repository is dedicated to Groovy (Java) programming Technics that can be used within a StreamSets Pipeline 

## Features

- Feature 1 : Implementing groovy code to create groovy code at runtime then compile and execute it.
- Feature 2 : Implementing groovy code to create a branch table technic to organize and optimize the code structure.
- Feature 3 : Implementing groovy code to read groovy code from a GPG encrypted text file then compile and execute it.

## A StreamSets pipeline will be used to demo all the above features.

## Basic groovy scripts will also be provided for IDE basic experiments

## GPG helper

gpg --export --armor "4C1CB37692A54837" > pubkey.asc

gpg --export-secret-keys --armor "4C1CB37692A54837" > prvkey.asc

gpg --output pgp/secretGroovyCode.pgp --encrypt --recipient "4C1CB37692A54837" src/secretGroovyCode.groovy

