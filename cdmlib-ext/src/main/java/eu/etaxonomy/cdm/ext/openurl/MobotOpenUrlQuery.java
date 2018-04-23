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

/**
 * @author a.kohlbecker
 \* @since 17.12.2010
 * 
 */
public class MobotOpenUrlQuery {
	public MobotOpenUrlServiceWrapper.ReferenceType refType;
	public String bookTitle = null;
	public String journalTitle = null;
	public String authorName = null;
	public String authorLastName = null;
	public String authorFirstName = null;
	public String authorNameCorporation = null;
	public String publicationDetails = null;
	public String publisherName = null;
	public String publicationPlace = null;
	public String publicationDate = null;
	public String ISSN = null;
	public String ISBN = null;
	public String CODEN = null;
	public String abbreviation = null;
	public String volume = null;
	public String issue = null;
	public String startPage = null;
	public URI bhlTitleURI = null;
	public URI bhlPageURI = null;
	public String oclcNumber = null;
	public String libofCongressID = null;
	public String schemaShortName = null;
}
