#!/usr/bin/python
import os, sys

modelDir="src/main/java/eu/etaxonomy/cdm/model"
modelPackage="eu.etaxonomy.cdm.model"
sprinfCfg="src/main/resources/sessionfactory.xml"

result = ""
foundClasses=False
for line in file(sprinfCfg):
	if foundClasses:
		if line.find('</list>') >= 0:
			foundClasses=False
		else:
			continue
	elif line.find('<property name="annotatedClasses">') >= 0:
		foundClasses=True
		result+= '    <property name="annotatedClasses">\n        <list>\n'
		for path,dirs,files in os.walk(modelDir):
		    if path <> modelDir:
		        package = "%s.%s"%(modelPackage, path[len(modelDir)+1:])
		    else:
		        package = modelPackage
		    for f in files: 
		        if f.endswith(".java") and f <> "package-info.java":
		            result+="            <value>%s.%s</value>\n" %(package, f[:-5])
		result+="        </list>\n"
		foundClasses=True
	else:
		result+=line

file(sprinfCfg,"w").write(result)
