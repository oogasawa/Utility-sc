#!/bin/bash


function mvn_install() {
    cd /tmp
    git clone --branch $2 --depth 1 https://github.com/oogasawa/$1 
    cd $1   
    mvn clean compile install
    cd ..
    rm -Rf $1
}

mvn_install POJO-bdd       v1.4.2
mvn_install Utility-cli    v1.0.0

