package eu.etaxonomy.cdm.remote.service;

import javax.jws.WebService;

@WebService(endpointInterface = "demo.spring.HelloWorld")
public class HelloWorldImpl implements HelloWorld {

    public String sayHi(String text) {
        return "Hello " + text;
    }
}