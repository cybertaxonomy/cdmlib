package eu.etaxonomy.cdm.remote.service;

import javax.jws.WebService;

@WebService
public interface HelloWorld {
    String sayHi(String text);
}