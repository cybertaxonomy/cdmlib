// $Id$
/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.ext.scratchpads;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.etaxonomy.cdm.common.UriUtils;


/**
 * @author l.morris
 * @date Jul 2, 2013
 *
 */
public class ScratchpadsService {

	private static final Logger logger = Logger.getLogger(ScratchpadsService.class);

	public static final String SCRATCHPADS_JSON_ENDPOINT = "http://scratchpads.eu/explore/sites-list/json";

	private static final char[] ILLEGAL_CHARACTERS = { '/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':', '.' };

	private static final String dir = "C:\\Users\\l.morris\\Downloads\\dwca_scratchpads\\";

	public void harvest(){

		InputStream inputStream = null;

		try {
			URL url = new URL(SCRATCHPADS_JSON_ENDPOINT);
			boolean isAvailable = UriUtils.isServiceAvailable(url.toURI());

			if (isAvailable) {
				inputStream = UriUtils.getInputStream(url.toURI());
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
				logger.debug(line);

			}

			ObjectMapper m = new ObjectMapper();

			JsonNode rootNode = m.readTree(sb.toString());
			logger.debug(rootNode.toString());
			logger.debug(rootNode.isArray());

			int num = 0;
			if (rootNode.isArray()) {
				Iterator<JsonNode> arrayElements = rootNode.elements();
				while (arrayElements.hasNext()) {
					JsonNode element = arrayElements.next();
					JsonNode website = element.path("field_website");
					//logger.debug(website.getValueAsText());
					String fieldWebsite = website.asText();

					if (fieldWebsite.startsWith("http")) {

						url = new URL(fieldWebsite + "/dwca.zip");
						URI uri = url.toURI();
						isAvailable = UriUtils.isServiceAvailable(uri);
						logger.debug("Is " + fieldWebsite + " available :" + isAvailable);

						String websiteName = "";
						//websiteName = (fieldWebsite.toString().split("//")[1]).split(".*")[0];
						websiteName = websiteName + fieldWebsite.split("//")[1];
						//if (websiteName.contains(".")){
							//websiteName = websiteName.substring(0, websiteName.indexOf("."));
						websiteName = websiteName.replaceAll("\\.", "_");
							//websiteName = websiteName.substring(0, websiteName.indexOf("."));

						//}

						//logger.debug("the website name " + websiteName);

						for (int j = 0; j < ILLEGAL_CHARACTERS.length; j++) {

							char ch = '_';
							websiteName.replace(ILLEGAL_CHARACTERS[j], ch);
						}

						websiteName = websiteName.substring(0, websiteName.length());

						if (isAvailable) {

							HttpResponse response = UriUtils.getResponse(uri, null);
							if (UriUtils.isOk(response)) {


								logger.debug("There is a dwca " + websiteName);

								try {
									inputStream = UriUtils.getInputStream(url.toURI());

									num++;

									if (inputStream != null) {

										copyDwcaZip(inputStream, websiteName);
										//createDwcaZip(inputStream);
									}

								} catch (HttpException e) {
									// TODO Auto-generated catch block
									logger.error("Failed to get dwca for " + websiteName + " as there was an error " + e);
								}

							}

						}
					}
				}
			}

			inputStream.close();


		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		} catch (IOException ie) {
            throw new RuntimeException(ie);
		} catch (HttpException e) {
            throw new RuntimeException(e);
		}
	}

	/**
	 * FIXME
	 * This is a hack as dwca.zip files from Scratchpads sites have an extra directory when unzipped. i.e. all the text
	 * and meta.xml are in the sub-directory dwca, but the should be directly in the top-level unzipped directory
	 */
	private void createDwcaZip (InputStream inputStream, String websiteName) {

		 ZipInputStream zis = new ZipInputStream(inputStream);

	         byte[] buffer = new byte[4096];
	         ZipEntry ze;
	         try {
				while ((ze = zis.getNextEntry()) != null)
				 {
				    System.out.println("Extracting: " + ze);

				    FileOutputStream fos = new FileOutputStream(ze.getName());
				    {
				       int numBytes;
				       while ((numBytes = zis.read(buffer, 0, buffer.length)) != -1) {
                        fos.write(buffer, 0, numBytes);
                    }
				    }
				    zis.closeEntry();
				 }
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}

	/*
	 * Use this method instead of createDwcaZip, once the dwca.zip structure is fixed in Scratchpads
	 */
	private void copyDwcaZip (InputStream inputStream, String websiteName) {

		FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream("dwca_" + websiteName + ".zip");//dir +

			byte[] b = new byte[1024];
			int count;
			while ((count = inputStream.read(b)) >= 0) {
				outputStream.write(b, 0, count);
			}
			outputStream.flush();
			outputStream.close();
			inputStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		ScratchpadsService spService = new ScratchpadsService();
		spService.harvest();
		// TODO Auto-generated method stub

	}

}
