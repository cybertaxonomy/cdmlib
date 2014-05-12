The project uses Maven2 and AspectJ.

Please see http://dev.e-taxonomy.eu/trac/wiki/CdmLibrary for details on installation and usage

***Deploying to Application Server***
The POM contains deployment plugins for TOMCAT and also JETTY.

- Production mode: TOMCAT
  Install tomcat,  configure and run it (with bin/startup.sh) 
  Configuration: Deployment information is in web.xml and server info in your personal settings.xml (referenced form pom)
  Deploy: mvn tomcat:deploy  
  Undeploy: mvn tomcat:undeploy  
  Debugging: http://www.mojavelinux.com/blog/archives/2007/03/remote_debugging_with_jetty/
  
- Development mode: JETTY 
  Configuration: in pom.xml the jetty plugin. Configured for port 8080
  Start: In the project folder simply execute "mvn jetty:run"  
  Stop: Ctrl-C  
  Debugging: http://www.mojavelinux.com/blog/archives/2007/03/remote_debugging_with_jetty/