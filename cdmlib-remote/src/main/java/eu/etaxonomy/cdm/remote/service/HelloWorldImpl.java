package eu.etaxonomy.cdm.remote.service;

import javax.jws.WebService;


@WebService(endpointInterface = "eu.etaxonomy.cdm.remote.service.HelloWorld")
public class HelloWorldImpl implements HelloWorld {
		  
	public String getGreeting(String firstName, String lastName) {
        return "Hello, " + firstName + " the greatest of " + lastName;
	}

	public String sayHi(String nomen) {
        return "Hello " + nomen + "!";
	}
}