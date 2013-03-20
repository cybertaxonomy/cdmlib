/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.jaxb;

import java.io.InputStream;
import java.io.Reader;

import org.w3c.dom.ls.LSInput;

/**
 * Copied from CATE (@author ben.clark). Not used at this point.
 */
public class LsInputImpl implements LSInput {
	
	private String baseURI;
	
	private String encoding;
	
	private String systemId;
	
	private String publicId;
	
	private String stringData;

	private InputStream byteStream;
	
	private Reader characterStream;
	
	private boolean certifiedText;

	public String getBaseURI() {
		return baseURI;
	}

	public void setBaseURI(String baseURI) {
		this.baseURI = baseURI;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public String getPublicId() {
		return publicId;
	}

	public void setPublicId(String publicId) {
		this.publicId = publicId;
	}

	public String getStringData() {
		return stringData;
	}

	public void setStringData(String stringData) {
		this.stringData = stringData;
	}

	public InputStream getByteStream() {
		return byteStream;
	}

	public void setByteStream(InputStream byteStream) {
		this.byteStream = byteStream;
	}

	public Reader getCharacterStream() {
		return characterStream;
	}

	public void setCharacterStream(Reader characterStream) {
		this.characterStream = characterStream;
	}

	public boolean getCertifiedText() {
		return certifiedText;
	}

	public void setCertifiedText(boolean certifiedText) {
		this.certifiedText = certifiedText;
	}
}
