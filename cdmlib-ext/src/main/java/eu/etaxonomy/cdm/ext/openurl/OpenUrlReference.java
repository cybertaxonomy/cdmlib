/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.openurl;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.ext.openurl.MobotOpenUrlServiceWrapper.ReferenceType;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.kohlbecker
 \* @since 16.12.2010
 *
 */
public class OpenUrlReference extends Reference {


	private static final String PAGETHUMB_BASE_URI = "http://www.biodiversitylibrary.org/pagethumb/";

	public static final Logger logger = Logger.getLogger(OpenUrlReference.class);

	private static final long serialVersionUID = 1L;

	private URI itemUri;

	private URI titleUri;

	private ReferenceType referenceType = null;

	/**
	 * Links to the specific book or journal, that is to the front page
	 *
	 * @param itemUri the itemUri to set
	 */
	public void setItemUri(URI itemUri) {
		this.itemUri = itemUri;
	}

	/**
	 * Links to the according entry in the bibliography.
	 *
	 * @return the itemUri
	 */
	public URI getItemUri() {
		return itemUri;
	}

	/**
	 * @param titleUri the titleUri to set
	 */
	public void setTitleUri(URI titleUri) {
		this.titleUri = titleUri;
	}


	/**
	 * @return the titleUri
	 */
	public URI getTitleUri() {
		return titleUri;
	}


	/**
	 * Splits the id from the base ulr of the id urls used in bhl. For example the url string http://www.biodiversitylibrary.org/item/16772 will be split into
	 * <ol>
	 * <li>http://www.biodiversitylibrary.org/item</li>
	 * <li>16772</li>
	 * </ol>
	 * @param uri
	 * @return
	 */
	private String[] splitPathAndId(URI uri) {
		String[] tokens = new String[2];
		if(uri != null){
			String titleUriString = uri.toString();
			tokens[1]  = titleUriString.substring(titleUriString.lastIndexOf('/') + 1);
			tokens[0]  = titleUriString.substring(0, titleUriString.lastIndexOf('/'));
			return tokens;
		} else  {
			return null;
		}
	}

	/**
	 * @param referenceType the referenceType to set
	 */
	public void setReferenceType(ReferenceType referenceType) {
		this.referenceType = referenceType;
	}

	/**
	 * @return the referenceType
	 */
	public ReferenceType getReferenceType() {
		return referenceType;
	}

	/**
	 * This method will construct an URI pointing to a service which creates an
	 * jpeg image. This may take a while. For more information please refer to
	 * http://biodivlib.wikispaces.com/Developer+Tools+and+API. If the width or
	 * height of the of the image given as parameter are null or 0 the BHL
	 * service will respond with the default thumbnail which seems to be cached.
	 * This is usually much faster than requesting for a custom imge
	 * size.
	 * <p>
	 *
	 * @param width
	 *            width of the image, may be null or 0
	 * @param height
	 *            height of the image, may be null or 0
	 * @return
	 */
	public URI getJpegImage(Integer width, Integer height){

		URI imageURI = null;
		try {
			String sizeStr = "";
			if(width != null && height != null && width > 0 && height > 0){
				sizeStr = "," + width + "," + height;
			}
			String[] tokens = splitPathAndId(getUri());
			if(tokens.length == 2){
				imageURI = new URI(PAGETHUMB_BASE_URI + tokens[1] + sizeStr);
			}
		} catch (URISyntaxException e) {
			// should never happen, but let's report it anyway
			logger.error(e);
		}
		return imageURI;
	}
}
