/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.ipni;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.ext.ipni.IpniService.IpniRank;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Reference;


/**
* Interface for queying IPNI via webservice ({@link http://www.uk.ipni.org/}).
* Services are available for the plant name index, the autor index and the publication index.
* @author a.mueller
* @created Aug 16, 2010
 */
public interface IIpniService {



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
	public static final String PUBLICATION_SERVICE_URL = "http://www.uk.ipni.org/ipni/advPublicationSearch.do";
	public static final String ID_PUBLICATION_SERVICE_URL = "http://www.uk.ipni.org/ipni/idPublicationSearch.do";
	public static final String ID_NAMESEARCH_SERVICE_URL = "http://www.ipni.org/ipni/idPlantNameSearch.do";

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
	public List<Person> getAuthors(String abbreviation, String surname, String forename, String isoCountry, ICdmRepository appConfig, IpniServiceAuthorConfigurator config);


	/**
	 * Returns a list of names matching the wholeName parameter according to the IPNI Quick search function.
	 * See {@link http://www.uk.ipni.org/sample_searches.html#name_quick} for further explanation about the IPNI Quick search.
	 * <BR/><BR/>
	 * The format parameter defines the depth of the data returned. See {@link http://www.ipni.org/ipni/delimited_help.html} for further
	 * information on the supported data formats.
	 * <BR/>
	 * Please be aware that not all data returned by IPNI are transformed into CDM data as some of the data types are not available in the
	 * CDM and some types are just not yet implemented.
	 *
	 * @param wholeName
	 * @param format
	 * @param appConfig
	 * @return
	 */
	public List<IBotanicalName> getNamesSimple(String wholeName, ICdmRepository services, IpniServiceNamesConfigurator config);

	/**
	 * Returns the name matching the id parameter according to the IPNI Quick search function.
	 * See {@link http://www.uk.ipni.org/sample_searches.html#name_quick} for further explanation about the IPNI Quick search.
	 * <BR/><BR/>
	 *
	 * Please be aware that not all data returned by IPNI are transformed into CDM data as some of the data types are not available in the
	 * CDM and some types are just not yet implemented.
	 *
	 * @param id
	 * @return
	 */
	public InputStream getNamesById(String id);


	/**
	 * Returns the publication matching the id parameter according to the IPNI Quick search function.
	 * See {@link http://www.uk.ipni.org/sample_searches.html#name_quick} for further explanation about the IPNI Quick search.
	 * <BR/><BR/>
	 *
	 * Please be aware that not all data returned by IPNI are transformed into CDM data as some of the data types are not available in the
	 * CDM and some types are just not yet implemented.
	 *
	 * @param id
	 * @return
	 */
	public InputStream getPublicationsById(String id);

	/**
	 * Returns a list of names matching the relevant parameters according to the IPNI full search function.
	 * See {http://www.uk.ipni.org/sample_searches.html#name_full} for further explanation about the IPNI Full search.
	 * <BR/><BR/>
	 * The format parameter defines the depth of the data returned. See {@link http://www.ipni.org/ipni/delimited_help.html} for further
	 * information on the supported data formats.
	 * <BR/>
	 * Please be aware that not all data returned by IPNI are transformed into CDM data as some of the data types are not available in the
	 * CDM and some types are just not yet implemented.
	 *
	 * @param family
	 * @param genus
	 * @param species
	 * @param infraFamily
	 * @param infraGenus
	 * @param infraSpecies
	 * @param authorAbbrev
	 * @param includePublicationAuthors
	 * @param includeBasionymAuthors
	 * @param publicationTitle
	 * @param isAPNIRecord
	 * @param isGCIRecord
	 * @param isIKRecord
	 * @param rankToReturn
	 * @param sortByFamily
	 * @param format
	 * @param appConfig
	 * @return List of botanical names returned by the IPNI web service.
	 */
	public List<BotanicalName> getNamesAdvanced(String family, String genus, String species, String infraFamily,
			String infraGenus, String infraSpecies, String authorAbbrev,
			String publicationTitle,
			IpniRank ipniRankToReturn,
			IpniServiceNamesConfigurator config,
			ICdmRepository repository);

	/**
	 * As {@link #getNamesAdvanced(String, String, String, String, String, String, String, Boolean, Boolean, String, Boolean, Boolean, Boolean, IpniRank, Boolean, IpniServiceNamesConfigurator, ICdmRepository)}
	 * but using CDM Rank instead of IpniRank. The CDM Rank is transformed into an IpniRank so it returns all
	 * names that are in the same IpniRange as the CDM rank. Therefore when using CDM rank 'variety' also a
	 * 'subspecies' may be returned as 'variety' and 'subspecies' are within the same IpniRange 'Infraspecific'.
	 *
	 * @param family
	 * @param genus
	 * @param species
	 * @param infraFamily
	 * @param infraGenus
	 * @param infraSpecies
	 * @param authorAbbrev
	 * @param includePublicationAuthors
	 * @param includeBasionymAuthors
	 * @param publicationTitle
	 * @param isAPNIRecord
	 * @param isGCIRecord
	 * @param isIKRecord
	 * @param rankToReturn
	 * @param sortByFamily
	 * @param format
	 * @param appConfig
	 * @return List of botanical names returned by the IPNI web service.
	 */
	public List<BotanicalName> getNamesAdvanced(String family, String genus, String species, String infraFamily,
			String infraGenus, String infraSpecies, String authorAbbrev,
			String publicationTitle,
			Rank rankRangeToReturn,
			IpniServiceNamesConfigurator config,
			ICdmRepository repository);

	/**
	 * Returns a list of publications matching the title and/or the abbreviation parameter according to the IPNI Publication search function.
	 * See {@link http://www.uk.ipni.org/sample_searches.html#publication_examples} for further explanation about the IPNI Publication search.
	 * <BR/><BR/>
	  * Please be aware that not all data returned by IPNI are transformed into CDM data as some of the data types are not available in the
	 * CDM and some types are just not yet implemented.
	 *
	 * @param title
	 * @param abbreviation
	 * @param services to retrieve data from the database. Needed to attach the same 'IPNI' reference to each of the resulting objects.
	 * Otherwise the resulting IPNI reference will be duplicate for each result and each time the service is called.
	 * @param config for finetuning, maybe null
	 * @return
	 */
	public List<Reference> getPublications(String title, String abbreviation, ICdmRepository services, IpniServicePublicationConfigurator config);

	public URL getServiceUrl(String url);
}

