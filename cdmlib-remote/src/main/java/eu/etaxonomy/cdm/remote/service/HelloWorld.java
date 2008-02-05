package eu.etaxonomy.cdm.remote.service;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.WebResult;

import org.codehaus.jra.Delete;
import org.codehaus.jra.Get;
import org.codehaus.jra.HttpResource;
import org.codehaus.jra.Post;
import org.codehaus.jra.Put;


@WebService(targetNamespace = "http://cdm.etaxonomy.eu/remote")
public interface HelloWorld {
	@Get
    @HttpResource(location = "/hello/{first}/{last}")
    @WebResult(name = "Greeting")
    String getGreeting(@WebParam(name="first") String firstName, @WebParam(name="last") String lastName);

	@Get
    @HttpResource(location = "/hello/{nomen}")
    @WebResult(name = "Greeting")
    String sayHi(@WebParam(name="nomen") String nomen);
}