#!/bin/bash

LANG=en_US.UTF-8; mvn javadoc:javadoc
rm -Rf ~/public_html/javadoc/Utility-sc
mv target/site/apidocs ~/public_html/javadoc/Utility-sc
