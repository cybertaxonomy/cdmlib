#!/usr/bin/python
import os, sys

modelDir="src/main/java/eu/etaxonomy/cdm/model"
modelPackage="eu.etaxonomy.cdm.model"

print"<list>"
for path,dirs,files in os.walk(modelDir):
    if path <> modelDir:
        package = "%s.%s"%(modelPackage, path[len(modelDir)+1:])
    else:
        package = modelPackage
    for f in files: 
        if f.endswith(".java") and f <> "package-info.java":
            print "<value>%s.%s</value>" %(package, f[:-5])
print"</list>"

