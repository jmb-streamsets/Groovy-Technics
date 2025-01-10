# Groovy-Technics
This repository is dedicated to Groovy (Java) programming Technics that can be used within a StreamSets Pipeline 

## This initial project will demonstrate few advanced technics that anybody could use within a StreamSets pipeline and in any other environments that are Java & Groovy capable.

## Project Features

- Feature 1 : Implementing groovy code to create groovy code
    - This feature will be used to demonstrate how at run-time to programmatically generate and compile groovy & java
      code
- Feature 2 : How to implement a "branch table"
    - This feature will demonstrate how to use the concept of "branch table" to optimize and organize code structures
- Feature 3 : How to read & execute PGP encrypted groovy code
  - 

## StreamSets pipeline will be used to demo all the above features

## Basic groovy scripts will also be provided for IDE basic experiments

## GPG command line utility helper

````shell
gpg --full-generate-key

gpg --list-keys --with-colons | grep '^pub' | cut -d':' -f5

gpg --export --armor "xxxxxxx" > pubkey.asc

gpg --export-secret-keys --armor "xxxxxxx" > prvkey.asc

gpg --output pgp/secretGroovyCode.pgp --encrypt --recipient "xxxxxxx" src/secretGroovyCode.groovy
````

# Pipeline used for the demo

## 3 different groovy evaluators will be used to demo each feature

![Screenshot from 2025-01-10 15-05-20.png](images/Screenshot%20from%202025-01-10%2015-05-20.png)