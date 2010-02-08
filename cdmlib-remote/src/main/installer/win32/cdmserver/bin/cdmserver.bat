cd ..
set HOME=%cd%
java -Xmx1024M -jar "%HOME%\libs\cdmserver-standalone.jar" --httpPort=8080 --ajp13Port=-1 --logfile="%HOME%\logs\cdmserverLog.txt"