/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.model.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import javax.wsdl.xml.WSDLLocator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;

import com.ibm.wsdl.util.StringUtils;

/**
 * WSDLLocator based almost totally upon WSIFWSDLLocatorImpl by Owen Burroughs
 * Created primarily because locating the required wsdl documents in the classpath 
 * seems like a safer thing than using a file path and relying on users remembering to
 * copy the correct wsdl files into the correct places.
 * 
 * @author ben.clark
 * @author Owen Burroughs
 */
public class LSIDWSDLLocator implements WSDLLocator {
	private static Log log = LogFactory.getLog(LSIDWSDLLocator.class);
	
	 private Reader baseReader = null;
	 private Reader importReader = null;
	 private String contextURI = null;
	 private String wsdlLocation = null;
	 private String documentBase = null;
	 private String importBase = null;
	 private ClassLoader loader = null; 

	 public LSIDWSDLLocator(String ctxt, String wsdlURI, ClassLoader cl) {
		contextURI = ctxt;
		wsdlLocation = wsdlURI;
		loader = cl;
	}

	 public LSIDWSDLLocator(String docBase, Reader reader, ClassLoader cl) {
		 documentBase = docBase;
		 baseReader = reader;
		 loader = cl;
	}

	/**
	 * @see javax.wsdl.xml.WSDLLocator#getBaseReader()
	 */
	public Reader getBaseReader() {
		if (baseReader == null) {
			try {
				URL url = null;
				URL contextURL = (contextURI != null) ? StringUtils.getURL(
						null, contextURI) : null;
				if (loader != null) {
					InputStream in = null;
					try {
						if (contextURL != null)
							url = new URL(contextURL, wsdlLocation);
						else {
							if (wsdlLocation.indexOf(":") == -1)
								url = new URL("file", null, wsdlLocation);
							else
								url = new URL(wsdlLocation);
						}
						String wsdlRelativeLocation = url.getPath();
						if (wsdlRelativeLocation.startsWith("/"))
							wsdlRelativeLocation = wsdlRelativeLocation
									.substring(1);
						in = loader
								.getResourceAsStream(wsdlRelativeLocation);
						baseReader = new InputStreamReader(in);
					} catch (Exception exc) {
					}
				}
				if (baseReader == null) {
					url = StringUtils.getURL(contextURL, wsdlLocation);
					baseReader = new InputStreamReader(StringUtils
							.getContentAsInputStream(url));
				}
				if (url != null)
					documentBase = url.toString();
			} catch (Exception e) {
				documentBase = wsdlLocation;
			}
		}

		return baseReader;
	}

	/**
	 * @see javax.wsdl.xml.WSDLLocator#getBaseURI()
	 */
	public String getBaseURI() {
		return documentBase; 
	}

	/**
	 * used to read imports as a document is parsed
	 * 
	 * @see javax.wsdl.xml.WSDLLocator#getImportReader(String, String)
	 */
	public Reader getImportReader(String base, String relativeLocation) {
		// Reset importReader if finding import within import
		importReader = null;
		boolean triedSU = false;
		try {
			// If a ClassLoader was used to load the base
			// document, chances
			// are we need to use it to find the import.
			URL url = null;
			if (loader != null) {
				if (relativeLocation.startsWith("/")
						|| relativeLocation.startsWith("\\")) {
					// Relative location has been specified from a root dir.
					// However,
					// using a ClassLoader, root dirs don't mean anything.
					relativeLocation = relativeLocation.substring(1,relativeLocation.length());
					InputStream in = loader.getResourceAsStream(relativeLocation);
					importReader = new InputStreamReader(in);
				} else if (relativeLocation.indexOf("://") != -1) {
					// This is a fully specified URL of some kind so don't
					// use the
					// ClassLoader to find the import.
					triedSU = true;
					url = StringUtils.getURL(null, relativeLocation);
					importReader = new InputStreamReader(StringUtils
							.getContentAsInputStream(url));
				} else {
					// Import location has been specified relative to the
					// base document
					// and so we can to try to form the complete path to it.
					if (base != null) {
						int i = base.lastIndexOf("/");
						if (i == -1) {
							i = base.lastIndexOf("\\");
						}
						if (i != -1) {
							String path = base.substring(0, i + 1);
							String resolvedPath = path + relativeLocation;
							if (relativeLocation.startsWith("..")) {
								resolvedPath = resolvePath(path,
										relativeLocation);
							}
							if (resolvedPath == null) {
								throw new Exception("Invalid Path");
							}

							// Make sure that resolved path starts with
							// file:
							if (resolvedPath.startsWith("file:")) {
								url = new URL(null, resolvedPath);
							} else {
								url = new URL(null, "file:" + resolvedPath);
							}
						} else {
							url = new URL(null, "file:" + base + File.separator + relativeLocation);
						}
						InputStream in = loader.getResourceAsStream(url.getPath());
						importReader = new InputStreamReader(in);
					} else {
						url = new URL(null, "file:" + relativeLocation);
						InputStream in = loader.getResourceAsStream(url.getPath());
						importReader = new InputStreamReader(in);
					}
				}
			} else {
				triedSU = true;
				URL contextURL = (base != null) ? StringUtils.getURL(null,
						base) : null;
				url = StringUtils.getURL(contextURL, relativeLocation);
				importReader = new InputStreamReader(StringUtils
						.getContentAsInputStream(url));
			}
			importBase = (url == null) ? relativeLocation : url.toString();
		} catch (Exception e) {
            log.error(e.toString());
            log.error(e.getMessage());
			// If we have not tried using a non-ClassLoader route, try it
			// now
			// as a last resort.
			if (!triedSU) {
				try {
					URL contextURL = (base != null) ? StringUtils.getURL(
							null, base) : null;
					URL url = StringUtils.getURL(contextURL,
							relativeLocation);
					importReader = new InputStreamReader(StringUtils
							.getContentAsInputStream(url));
					importBase = (url == null) ? relativeLocation : url
							.toString();
				} catch (Exception e2) {
					 log.error(e2.toString());
					 log.error("Cannot find " + importBase + " so setting importBase to unknownImportURI");
					// we can't find the import so set a temporary value for
					// the import URI. This is
					// necessary to avoid a NullPointerException in
					// WSDLReaderImpl
					importBase = "unknownImportURI";
				}
			} else {
				log.error("Cannot find " + importBase + " so setting importBase to unknownImportURI");
				// we can't find the import so set a temporary value for the
				// import URI. This is
				// necessary to avoid a NullPointerException in
				// WSDLReaderImpl
				importBase = "unknownImportURI";
			}
		}

		return importReader;
	}
	
	/**
	 * Resolve a path when the relative location begins with ..
	 */
	private String resolvePath(String ba, String rel) {
		StringBuffer sb = new StringBuffer(rel);
		int dd = 0;
		while (sb.length() > 0) {
			if (sb.length() > 3 && sb.charAt(0) == '.'
					&& sb.charAt(1) == '.'
					&& (sb.charAt(2) == '/' || sb.charAt(2) == '\\')) {
				dd++;
				sb.delete(0, 3);
			} else {
				break;
			}
		}
		StringBuffer sb2 = new StringBuffer(ba);
		int j = sb2.length() - 1;
		int found = 0;
		for (int k = j; k >= 0; k--) {
			if (k != j && (sb2.charAt(k) == '/' || sb2.charAt(k) == '\\')) {
				found++;
			}
			if (found < dd) {
				sb2.deleteCharAt(k);
			} else {
				break;
			}
		}
		if (found + 1 < dd)
			return null;
		return sb2.toString() + sb.toString();
	}

	/**
	 * @see javax.wsdl.xml.WSDLLocator#getLatestImportURI()
	 */
	public String getLatestImportURI() {
		return importBase; 
	}

	/**
	 * @see javax.wsdl.xml.WSDLLocator#getBaseInputSource()
	 */
	public InputSource getBaseInputSource() {
		return new InputSource(getBaseReader());
	}

	/**
	 * @see javax.wsdl.xml.WSDLLocator#getImportInputSource(String, String)
	 */
	public InputSource getImportInputSource(String arg0, String arg1) {
		return new InputSource(getImportReader(arg0, arg1));
	}

	public void close() {
		if (baseReader != null)
			try {
				baseReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		if (importReader != null)
			try {
				importReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

}
