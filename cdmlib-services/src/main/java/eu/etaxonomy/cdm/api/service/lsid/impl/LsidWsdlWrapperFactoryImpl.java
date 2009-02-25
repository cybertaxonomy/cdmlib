package eu.etaxonomy.cdm.api.service.lsid.impl;

import java.io.InputStream;


import org.springframework.stereotype.Component;

import com.ibm.lsid.LSIDException;

import eu.etaxonomy.cdm.api.service.lsid.LSIDWSDLWrapper;
import eu.etaxonomy.cdm.api.service.lsid.LSIDWSDLWrapperFactory;
import eu.etaxonomy.cdm.model.common.LSID;

@Component("lsidWsdlWrapperFactory")
public class LsidWsdlWrapperFactoryImpl implements LSIDWSDLWrapperFactory {
	
	private String baseURI = "wsdl/";
	
	public void setBaseURI(String baseURI) {
		this.baseURI = baseURI;
	}

	public LSIDWSDLWrapper getLSIDWSDLWrapper(LSID lsid) {
		return new LsidWsdlWrapperImpl(lsid,baseURI);
	}

	public LSIDWSDLWrapper getLSIDWSDLWrapper(String wsdl) throws LSIDException {
		return new LsidWsdlWrapperImpl(wsdl,baseURI);
	}

	public LSIDWSDLWrapper getLSIDWSDLWrapper(InputStream wsdl) throws LSIDException {
		return new LsidWsdlWrapperImpl(wsdl,baseURI);
	}

}
