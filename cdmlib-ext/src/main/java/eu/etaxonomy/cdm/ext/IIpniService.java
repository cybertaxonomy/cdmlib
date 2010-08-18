// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext;

import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;


/**
* Interface for queying IPNI via webservice ({@link http://www.uk.ipni.org/}). 
* Services are available for the plant name index, the autor index and the publication index.
* @author a.mueller
* @created Aug 16, 2010
* @version 1.0
 */
public interface IIpniService {

	// GENERAL
	public static String ID = "Id";
	public static String VERSION = "Version";
	
	//NAMES 
	public static final String FULL_NAME_WITHOUT_FAMILY_AND_AUTHORS = "Full name without family and authors";
	public static final String AUTHORS = "Authors";
	public static final String FAMILY = "Family";
	public static final String GENUS = "Genus";
	public static final String INFRA_GENUS = "Infra genus";
	public static final String SPECIES = "Species";
	public static final String INFRA_SPECIFIC = "Infra species";
	public static final String RANK = "Rank";
	public static final String BASIONYM_AUTHOR = "Basionym author";
	public static final String PUBLISHING_AUTHOR = "Publishing author";
	public static final String PUBLICATION = "Publication";
	public static final String PUBLICATION_YEAR_FULL = "Publication year full";
	public static final String NAME_STATUS = "Name status";
	public static final String REMARKS = "Remarks";
	public static final String BASIONYM = "Basionym";
	public static final String REPLACED_SYNONYM = "Replaced synonym";

	
	//AUTHORS
	
	public static final String STANDARD_FORM = "Standard Form";
	
	public static final String DEFAULT_AUTHOR_FORENAME = "Default author forename";
	public static final String DEFAULT_AUTHOR_SURNAME = "Default author surname";
	public static final String TAXON_GROUPS = "Taxon groups";
	public static final String DATES = "Dates";
	public static final String ALTERNATIVE_NAMES = "Alternative names";
	
	public static final String DEFAULT_AUTHOR_NAME = "Default author name";
	
	public static final String NAME_NOTES = "Name notes";
	public static final String NAME_SOURCE = "Name source";
	public static final String DATE_TYPE_CODE = "Date type code";
	public static final String DATE_TYPE_STRING = "Date type string";
	
	public static final String ALTERNATIVE_ABBREVIATIONS = "Alternative abbreviations";
	public static final String EXAMPLE_OF_NAME_PUBLISHED = "Example of name published";
	
	/**
	 * UUID for the reference representing the IPNI database:<BR/>
	 * 8b6d750f-c7e0-4180-afbf-aa4c50148813
	 */
	public static final UUID uuidIpni = UUID.fromString("8b6d750f-c7e0-4180-afbf-aa4c50148813");
	/**
	 * UUID for the extension type 'Alternative name':<BR/>
	 * eee99927-1f9f-4df2-9d8f-11746bf35c0c
	 */
	public static final UUID uuidAlternativeNames = UUID.fromString("eee99927-1f9f-4df2-9d8f-11746bf35c0c");
	

	public static final String AUTHOR_SERVICE_URL = "http://www.ipni.org/ipni/advAuthorSearch.do";
	public static final String SIMPLE_NAME_SERVICE_URL = "http://www.uk.ipni.org/ipni/simplePlantNameSearch.do";
	public static final String ADVANCED_NAME_SERVICE_URL = "http://www.uk.ipni.org/ipni/advPlantNameSearch.do";
	public static final String PUBLICATION_SERVICE_URL = "http://www.uk.ipni.org/ipni/pubXXX.do";
	
	
	 /**
	 * Enumeration of the four return delimited data formats provided by IPNI.<BR/>
	 * @see http://www.ipni.org/ipni/delimited_help.html
	 * @author a.mueller
	 */
	public enum DelimitedFormat{
		/** IPNI classic delimited format */
		 CLASSIC ("delimited-classic"),
		 /** IPNI minimal delimited format */
		 MINIMAL ("delimited-minimal"),
		 /** IPNI short delimited format */
		 SHORT ("delimited-short"),
		 /** IPNI extended delimited format */
		 EXTENDED ("delimited-extended");
		 
		 String parameter;
		 DelimitedFormat(String parameter){
			this.parameter = parameter; 
		 }
	 }
	
	/**
	 * Returns a list of persons (authors) defined by their abbreviation, surname, forename and/or isoCountry.
	 * The amount of data added to each person depends on the format and the database connection (appConfig).
	 * <BR/><BR/>
	 * The {@link DelimitedFormat#MINIMAL minimal}  and {@link DelimitedFormat#SHORT short} format returns the 
	 * <i>standard form</i> as the nomenclatural title, the <i>default author forename</i> as the firstname and
	 * the <i>default author surname</i> as the lastname of the returned {@link Person person} object. 
	 * The <i>id</i> and the <i>version</i> are added as {@link IdentifiableSource source} where the id is the id,
	 * the namespace is "Author" and the microcitation is the <i>version</i>. If an a database connection is passed
	 * (appConig is not <code>null</null>) the database is searched for an existing citation representing the IPNI
	 *  webservice. If not exists a new such reference with the given {@link #uuidIpni ipni uuid} is created and stored
	 *  in the database.<BR/>
	 *  If no database connection is passed a new reference is created each time with a random UUID to avoid duplicate
	 *  key problems when trying to save the returned objects.
	 *  <BR/><BR/>
	 * The {@link DelimitedFormat#EXTENDED minimal} format returns the same object as the {@link DelimitedFormat#SHORT short} format 
	 * but additionally the <i>date</i> is evaluated as lifespan.
	 * Also an extension of type
	 * {@link ExtensionType.INFORMAL_CATEGORY} is added for each semicolon separated part of the 'Alternative names' result.
	 * TODO make alternative name or alternative title an own ExtensionType
	 * <BR/><BR/>
	 * The {@link DelimitedFormat#CLASSIC classic} format at the moment returns the same object as the {@link DelimitedFormat#EXTENDED extended} format 
	 * as the remaining parameters are not yet implemented.
	 *  
	 * @param abbreviation
	 * @param surname
	 * @param forename
	 * @param isoCountry
	 * @param format 
	 * @param appConfig
	 * @return
	 */
	public List<Person> getAuthors(String abbreviation, String surname, String forename, String isoCountry, DelimitedFormat format, ICdmApplicationConfiguration appConfig);


	public List<BotanicalName> getNamesSimple(String wholeName, DelimitedFormat format , ICdmApplicationConfiguration appConfig);

	public List<BotanicalName> getNamesAdvanced(String family, String genus, String species, String infraFamily, 
			String infraGenus, String infraSpecies, String authorAbbrev, Boolean includePublicationAuthors, 
			Boolean includeBasionymAuthors,
			String publicationTitle,
			Boolean isAPNIRecord, 
			Boolean isGCIRecord, 
			Boolean isIKRecord,
			Rank rankToReturn,
			Boolean sortByFamily,
			DelimitedFormat format, 
			ICdmApplicationConfiguration appConfig);
	}
