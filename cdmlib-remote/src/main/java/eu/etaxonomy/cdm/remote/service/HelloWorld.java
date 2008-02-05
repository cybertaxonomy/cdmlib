package eu.etaxonomy.cdm.remote.service;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.WebResult;

import org.codehaus.jra.Delete;
import org.codehaus.jra.Get;
import org.codehaus.jra.HttpResource;
import org.codehaus.jra.Post;
import org.codehaus.jra.Put;

@WebService(targetNamespace = "http://cdm.server.etaxonomy.eu")
public interface HelloWorld {
	@Get
    @HttpResource(location = "/hello")
    @WebResult(name = "Greeting")
    String sayHi(@WebParam(name="text") String text);
}