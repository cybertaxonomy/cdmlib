// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.ipni;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.ext.ipni.IIpniService.DelimitedFormat;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;


/**
* @author a.mueller
* @created Aug 16, 2010
* @version 1.0
 *
 */
@Component
public class IpniService implements IIpniService{
	private static final Logger logger = Logger.getLogger(IpniService.class);
	
	// GENERAL
	public static String ID = "Id";
	public static String VERSION = "Version";
	public static final String REMARKS = "Remarks";
	
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
	
	
	//PUBLICATIONS
	
	public static final String ABBREVIATION = "Abbreviation";
	public static final String TITLE = "Title";
	public static final String BPH_NUMBER = "BPH number";
	public static final String ISBN = "ISBN";
	public static final String ISSN = "ISSN";
	public static final String AUTHORS_ROLE = "Authors role";
	public static final String EDITION = "Edition";
	public static final String DATE = "Date";
	public static final String IN_PUBLICATION_FACADE = "In publication facade";
	public static final String LC_NUMBER = "LC number";
	public static final String PLACE = "Place";
	public static final String PUBLICATION_AUTHOR_TEAM = "Publication author team";
	public static final String PRECEDED_BY = "Preceded";
	public static final String TL2_AUTHOR = "TL2 author";
	public static final String TL2_NUMBER = "TL2 number";
	public static final String TDWG_ABBREVIATION = "TDWG abbreviation";
	
	private enum ServiceType{
		 AUTHOR,
		 NAME,
		 PUBLICATION,
	}
	
	public enum IpniRank{
		ALL ("All"),
		FAMILIAL ("Familial"),
		INFRA_FAMILIAL ("Infrafamilial"),
		GENERIC("Generic"),
		INFRA_GENERIC("Infrageneric"),
		SPECIFIC ("Specific"),
		INFRA_SPECIFIC("InfraSpecific");
		
		String strRank;
		IpniRank(String strRank){
			this.strRank = strRank;
		}
		
		public static IpniRank valueOf(Rank rank){
			if (rank == null){
				return ALL;
			}else if (rank.isInfraSpecific()){
				return INFRA_SPECIFIC;
			}else if (rank.isSpecies()){
				return SPECIFIC;
			}else if (rank.isInfraGeneric()){
				return INFRA_GENERIC;
			}else if (rank.isGenus()){
				return GENERIC;
			}else if (rank.isLower(Rank.FAMILY())){
				return INFRA_FAMILIAL;
			}else if (rank.isHigher(Rank.SUBFAMILY())){
				return FAMILIAL;
			}else{
				logger.warn("Rank could not be transformed to ipni rank. Use ALL instead");
				return ALL;
			}
		}
	}
		
	 
//	private URL serviceUrl;
	 
// ******************************** CONSTRUCTOR **************************************
	   
	   
//	/**
//	 * Creates new instance of this factory and connects it to the given
//	 * CDM Community Stores access point.
//	 *
//	 * Typically, there is no need to instantiate this class.
//	 */
//	protected IpniService(URL webserviceUrl){
//		this.serviceUrl = webserviceUrl;
//	}

// ****************************** METHODS ****************************************************/	
	
	/**
	 *
	 * @param restRequest
	 * @return
	 */
	public List<Person> getAuthors(String abbreviation, String surname, String forename, String isoCountry, ICdmApplicationConfiguration services, IpniServiceAuthorConfigurator config){
		//config
		if (config == null){
			config = new IpniServiceAuthorConfigurator();
		}
		
		
		abbreviation = normalizeParameter(abbreviation);
		surname = normalizeParameter(surname);
		isoCountry = normalizeParameter(isoCountry);
		forename = normalizeParameter(forename);
		
		DelimitedFormat format = config.getFormat();
			
		String request = "find_abbreviation=" + abbreviation + 
						"&find_surname=" + surname + 
						"&find_isoCountry=" + isoCountry + 
						"&find_forename=" + forename +
						"&output_format=" + format.parameter;
		
		return (List)queryService(request, services, getServiceUrl(IIpniService.AUTHOR_SERVICE_URL), config, ServiceType.AUTHOR);
	}
	       

	/**
	 *
	 * @param restRequest
	 * @return
	*/
	private List<? extends IdentifiableEntity> queryService(String request, ICdmApplicationConfiguration services, URL serviceUrl, IIpniServiceConfigurator config, ServiceType serviceType){
		if (config == null){
			throw new NullPointerException("Ipni service configurator should not be null");
		}
		try {
            // create the request url
            URL newUrl = new URL(serviceUrl.getProtocol(),
                                                     serviceUrl.getHost(),
                                                     serviceUrl.getPort(),
                                                     serviceUrl.getPath() 
                                                     + "?" + request);
            // open a connection
            HttpURLConnection connection = (HttpURLConnection) newUrl.openConnection();
            // set the accept property to XML so we can use jdom to handle the content
            //connection.setRequestProperty("Accept", "text/xml");
   
           
            logger.info("Firing request for URL: " + newUrl);
                   
            int responseCode = connection.getResponseCode();
           
            // get the content at the resource
            InputStream content = (InputStream) connection.getContent();
           
            // build the result
            List<? extends IdentifiableEntity> result;
            if (serviceType.equals(ServiceType.AUTHOR)){
            	result = buildAuthorList(content, services, config);
            }else if (serviceType.equals(ServiceType.NAME)){
            	result = buildNameList(content, services, config);
            }else{
            	result = buildPublicationList(content, services, config);
            }
            if(responseCode == HttpURLConnection.HTTP_OK){
                    return result;
            }else{
                //TODO error handling    
            	logger.error("No Http_OK");
            }
               
        } catch (IOException e) {
                logger.error("No content for request: " + request);
        }
       
        // error
        return null;
    }

	private List<ReferenceBase> buildPublicationList( InputStream content, ICdmApplicationConfiguration services, IIpniServiceConfigurator iConfig) throws IOException {
		IpniServicePublicationConfigurator config = (IpniServicePublicationConfigurator)iConfig;
		
		List<ReferenceBase> result = new ArrayList<ReferenceBase>(); 
		BufferedReader reader = new BufferedReader (new InputStreamReader(content));
		
		String headerLine = reader.readLine();
		Map<Integer, String> parameterMap = getParameterMap(headerLine);
		
		String line = reader.readLine();
		while (StringUtils.isNotBlank(line)){
			ReferenceBase reference = getPublicationFromLine(line, parameterMap, services, config);
			result.add(reference);
			line = reader.readLine();
		}

		return result;
	}


	private ReferenceBase getPublicationFromLine(String line, Map<Integer, String> parameterMap, ICdmApplicationConfiguration appConfig, IpniServicePublicationConfigurator config) {
		//fill value map
		String[] splits = line.split("%");
		
		Map<String, String> valueMap = new HashMap<String, String>();
		for (int i = 0; i < splits.length; i++){
			valueMap.put(parameterMap.get(i), splits[i]);
		}
		
		//create reference object
		ReferenceBase ref = ReferenceFactory.newGeneric();
		
		//reference
		if (config.isUseAbbreviationAsTitle() == true){
			ref.setTitle(valueMap.get(ABBREVIATION));
			//TODO handle title as extension
		}else{
			ref.setTitle(valueMap.get(TITLE));
			//TODO handle abbreviation as extension
		}
		ref.setIsbn(valueMap.get(ISBN));
		ref.setIssn(valueMap.get(ISSN));
		ref.setEdition(valueMap.get(EDITION));
		ref.setPlacePublished(valueMap.get(PLACE));

		String author = valueMap.get(PUBLICATION_AUTHOR_TEAM);
		Team team = Team.NewTitledInstance(author, author);
		ref.setAuthorTeam(team);
		
		//remarks
		String remarks = valueMap.get(REMARKS);
		Annotation annotation = Annotation.NewInstance(remarks, AnnotationType.EDITORIAL(), Language.ENGLISH());
		ref.addAnnotation(annotation);

		//dates
		TimePeriod date = TimePeriod.parseString(valueMap.get(DATE));
		ref.setDatePublished(date);

		//source
		ReferenceBase citation = getIpniCitation(appConfig);
		ref.addSource(valueMap.get(ID), "Publication", citation, valueMap.get(VERSION));

		
/*		TODO
		BPH number
		Authors role
		In publication facade
		LC number
		Preceded by
		TL2 author
		TL2 number
		TDWG abbreviation
	*/
		
		return ref;
	}


	private List<BotanicalName> buildNameList( InputStream content, ICdmApplicationConfiguration appConfig, IIpniServiceConfigurator iConfig) throws IOException {
		IpniServiceNamesConfigurator config = (IpniServiceNamesConfigurator)iConfig;
		List<BotanicalName> result = new ArrayList<BotanicalName>(); 
		BufferedReader reader = new BufferedReader (new InputStreamReader(content));
		
		String headerLine = reader.readLine();
		Map<Integer, String> parameterMap = getParameterMap(headerLine);
		
		String line = reader.readLine();
		while (StringUtils.isNotBlank(line)){
			BotanicalName name = getNameFromLine(line,parameterMap, appConfig);
			result.add(name);
			line = reader.readLine();
		}

		
		return result;
	}


	private BotanicalName getNameFromLine(String line, Map<Integer, String> parameterMap, ICdmApplicationConfiguration appConfig) {
		//Id%Version%Standard form%Default author forename%Default author surname%Taxon groups%Dates%Alternative names
		String[] splits = line.split("%");
		Map<String, String> valueMap = new HashMap<String, String>();
		
		for (int i = 0; i < splits.length; i++){
			valueMap.put(parameterMap.get(i), splits[i]);
		}
		
		BotanicalName name = BotanicalName.NewInstance(null);
		
		//caches
		name.setNameCache(valueMap.get(FULL_NAME_WITHOUT_FAMILY_AND_AUTHORS), true);
		name.setAuthorshipCache(valueMap.get(AUTHORS), true);
		
		//epithets
		name.setGenusOrUninomial(valueMap.get(GENUS));
		name.setInfraGenericEpithet(valueMap.get(INFRA_GENUS));
		name.setSpecificEpithet(valueMap.get(SPECIES));
		name.setInfraSpecificEpithet(valueMap.get(INFRA_SPECIFIC));
		
		//rank
		try {
			String rankStr = nomalizeRank(valueMap.get(RANK));
			name.setRank(Rank.getRankByNameOrAbbreviation(rankStr, NomenclaturalCode.ICBN, true));
		} catch (UnknownCdmTypeException e) {
			logger.warn("Rank was unknown");
		}
		
		//authors
		name.setBasionymAuthorTeam(Team.NewTitledInstance(valueMap.get(BASIONYM_AUTHOR), valueMap.get(BASIONYM_AUTHOR)));
		name.setCombinationAuthorTeam(Team.NewTitledInstance(valueMap.get(PUBLISHING_AUTHOR), valueMap.get(PUBLISHING_AUTHOR)));
		
		//publication
		ReferenceBase ref = ReferenceFactory.newGeneric();
		ref.setTitleCache(valueMap.get(PUBLICATION));
		TimePeriod datePublished = TimePeriod.parseString(valueMap.get(PUBLICATION_YEAR_FULL));
		name.setNomenclaturalReference(ref);
		
		//name status
		NomenclaturalStatusType statusType = null;
		String statusString = valueMap.get(NAME_STATUS);
		if (StringUtils.isNotBlank(statusString)){
			try {
				statusType = NomenclaturalStatusType.getNomenclaturalStatusTypeByAbbreviation(statusString);
				NomenclaturalStatus nomStatus = NomenclaturalStatus.NewInstance(statusType);
				name.addStatus(nomStatus);
			} catch (UnknownCdmTypeException e) {
				logger.warn("Name status not recognized: " + statusString);
			}
		}
		
		//remarks
		String remarks = valueMap.get(REMARKS);
		Annotation annotation = Annotation.NewInstance(remarks, AnnotationType.EDITORIAL(), Language.ENGLISH());
		name.addAnnotation(annotation);
		
		//basionym
		BotanicalName basionym = BotanicalName.NewInstance(null);
		basionym.setTitleCache(valueMap.get(BASIONYM), true);
		name.addBasionym(basionym);
		
		//replaced synonym
		BotanicalName replacedSynoynm = BotanicalName.NewInstance(null);
		replacedSynoynm.setTitleCache(valueMap.get(REPLACED_SYNONYM), true);
		name.addReplacedSynonym(replacedSynoynm, null, null, null);

		
		//source
		ReferenceBase citation = getIpniCitation(appConfig);
		name.addSource(valueMap.get(ID), "Name", citation, valueMap.get(VERSION));
		
		
//		//TODO Family, Infra family, Hybrid genus, Hybrid, Collation, Nomenclatural synonym, Distribution, Citation type

		
		return name;
	}


	private String nomalizeRank(String string) {
		String result = string.replace("spec.", "sp.");
		return result;
	}


	private List<Person> buildAuthorList(InputStream content, ICdmApplicationConfiguration services, IIpniServiceConfigurator iConfig) throws IOException {
		IpniServiceAuthorConfigurator config = (IpniServiceAuthorConfigurator)iConfig;
		List<Person> result = new ArrayList<Person>(); 
		BufferedReader reader = new BufferedReader (new InputStreamReader(content));
		
		String headerLine = reader.readLine();
		Map<Integer, String> parameterMap = getParameterMap(headerLine);
		
		String line = reader.readLine();
		while (StringUtils.isNotBlank(line)){
			Person author = getAuthorFromLine(line,parameterMap, services, config);
			result.add(author);
			line = reader.readLine();
		}

		return result;
	}



	private Map<Integer, String> getParameterMap(String headerLine) {
		Map<Integer, String> result = new HashMap<Integer, String>();
		String[] splits = headerLine.split("%");
		for (int i = 0; i < splits.length ; i ++){
			result.put(i, splits[i]);
		}
		return result;
	}


	private Person getAuthorFromLine(String line, Map<Integer, String> categoryMap, ICdmApplicationConfiguration appConfig, IpniServiceAuthorConfigurator config) {
		//Id%Version%Standard form%Default author forename%Default author surname%Taxon groups%Dates%Alternative names
		String[] splits = line.split("%");
		Map<String, String> valueMap = new HashMap<String, String>();
		
		for (int i = 0; i < splits.length; i++){
			valueMap.put(categoryMap.get(i), splits[i]);
		}
		
		Person person = Person.NewInstance();
		
		person.setNomenclaturalTitle(valueMap.get(STANDARD_FORM));
		person.setFirstname(valueMap.get(DEFAULT_AUTHOR_FORENAME));
		person.setLastname(valueMap.get(DEFAULT_AUTHOR_SURNAME));
		
		ReferenceBase citation = getIpniCitation(appConfig);
		
		//id, version
		person.addSource(valueMap.get(ID), "Author", citation, valueMap.get(VERSION));
		
		//dates
		TimePeriod lifespan = TimePeriod.parseString(valueMap.get(DATES));
		person.setLifespan(lifespan);
		
		//alternative_names
		String alternativeNames = valueMap.get(ALTERNATIVE_NAMES);
		if (StringUtils.isNotBlank(alternativeNames)){
			String[] alternativeNameSplits = alternativeNames.split("%");
			for (String alternativeName : alternativeNameSplits){
				if (alternativeName.startsWith(">")){
					alternativeName = alternativeName.substring(1);
				}
				Extension.NewInstance(person, alternativeName, ExtensionType.INFORMAL_CATEGORY());
			}
		}
		
		//TODO taxonGroups
		
		return person;
	}

	
	private ReferenceBase getIpniCitation(ICdmApplicationConfiguration appConfig) {
		ReferenceBase ipniReference;
		if (appConfig != null){
			ipniReference = appConfig.getReferenceService().find(uuidIpni);
			if (ipniReference == null){
				ipniReference = getNewIpniReference();
				ipniReference.setUuid(uuidIpni);
				appConfig.getReferenceService().save(ipniReference);
			}
		}else{
			ipniReference = getNewIpniReference();
		}
		return ipniReference;
	}

	/**
	 * @return
	 */
	private ReferenceBase getNewIpniReference() {
		ReferenceBase ipniReference;
		ipniReference = ReferenceFactory.newDatabase();
		ipniReference.setTitleCache("The International Plant Names Index (IPNI)");
		return ipniReference;
	}


	/**
	 * @param parameter
	 */
	private String normalizeParameter(String parameter) {
		String result = CdmUtils.Nz(parameter).replace(" ", "+");
		return result;
	}

	public List<BotanicalName> getNamesAdvanced(String family, String genus, String species, String infraFamily, 
			String infraGenus, String infraSpecies, String authorAbbrev, Boolean includePublicationAuthors, 
			Boolean includeBasionymAuthors,
			String publicationTitle,
			Boolean isAPNIRecord, 
			Boolean isGCIRecord, 
			Boolean isIKRecord,
			Rank rankInRangeToReturn,
			Boolean sortByFamily,
			IpniServiceNamesConfigurator config,
			ICdmApplicationConfiguration services){
		IpniRank ipniRank = IpniRank.valueOf(rankInRangeToReturn);
		return getNamesAdvanced(family, genus, species, infraFamily, infraGenus, infraSpecies, authorAbbrev, includePublicationAuthors, includeBasionymAuthors, publicationTitle, isAPNIRecord, isGCIRecord, isIKRecord, ipniRank, sortByFamily, config, services);
	}
	
	public List<BotanicalName> getNamesAdvanced(String family, String genus, String species, String infraFamily, 
			String infraGenus, String infraSpecies, String authorAbbrev, Boolean includePublicationAuthors, 
			Boolean includeBasionymAuthors,
			String publicationTitle,
			Boolean isAPNIRecord, 
			Boolean isGCIRecord, 
			Boolean isIKRecord,
			IpniRank rankToReturn,
			Boolean sortByFamily,
			IpniServiceNamesConfigurator config,
			ICdmApplicationConfiguration services) {
		
//		find_rankToReturn=all&output_format=normal&find_sortByFamily=on&find_sortByFamily=off&query_type=by_query&back_page=plantsearch

		//config
		if (config == null){
			config = new IpniServiceNamesConfigurator();
		}

		
		family = normalizeParameter(family);
		genus = normalizeParameter(genus);
		species = normalizeParameter(species);
		infraFamily = normalizeParameter(infraFamily);
		infraGenus = normalizeParameter(infraGenus);
		infraSpecies = normalizeParameter(infraSpecies);
		authorAbbrev = normalizeParameter(authorAbbrev);

		publicationTitle = normalizeParameter(publicationTitle);
		
		DelimitedFormat format = config.getFormat();
		
		String request = 
				"find_family=" + family + 
				"&find_genus=" + genus + 
				"&find_species=" + species + 
				"&find_infrafamily=" + infraFamily +
				"&find_infragenus=" + infraGenus +
				"&find_infraspecies=" + infraSpecies +
				"&find_authorAbbrev=" + authorAbbrev +
				getBooleanParameter("&find_includePublicationAuthors=", includePublicationAuthors, "on", "off") +
				getBooleanParameter("&find_includeBasionymAuthors=", includePublicationAuthors, "on", "off") +
				getBooleanParameter("&find_find_isAPNIRecord=", includePublicationAuthors, "on", "false") +
				getBooleanParameter("&find_isGCIRecord=", includePublicationAuthors, "on", "false") +
				getBooleanParameter("&find_isIKRecord=", includePublicationAuthors, "on", "false") +
				
				
				"&find_publicationTitle=" + publicationTitle +
				"&output_format=" + format.parameter;
		
		return (List)queryService(request, services, getServiceUrl(IIpniService.ADVANCED_NAME_SERVICE_URL), config, ServiceType.NAME);
	}

	
	private String getBooleanParameter(String urlParamString, Boolean booleanParameter, String trueString, String falseString) {
		String result;
		if (booleanParameter == null){
			result = getBooleanParameter(urlParamString, true, trueString, falseString) + getBooleanParameter(urlParamString, false, trueString, falseString); 
		}else if (booleanParameter == true){
			result = urlParamString + trueString;
		}else {
			result = urlParamString + falseString;
		}
		return result;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.ext.IIpniService#getNamesSimple(java.lang.String, eu.etaxonomy.cdm.ext.IIpniService.DelimitedFormat, eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration)
	 */
	public List<BotanicalName> getNamesSimple(String wholeName, ICdmApplicationConfiguration services, IpniServiceNamesConfigurator config){
		if (config == null){
			config = new IpniServiceNamesConfigurator();
		}
		
		
//		query_type=by_query&back_page=query_ipni.html
		
		wholeName = normalizeParameter(wholeName);
		
		DelimitedFormat format = config.getFormat();
			
		String request = "find_wholeName=" + wholeName + 
						"&output_format=" + format.parameter;
		
		return (List)queryService(request, services, getServiceUrl(IIpniService.SIMPLE_NAME_SERVICE_URL), config, ServiceType.NAME);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.ext.IIpniService#getPublications(java.lang.String, java.lang.String, boolean, eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration)
	 */
	public List<ReferenceBase> getPublications(String title, String abbreviation, ICdmApplicationConfiguration services, IpniServicePublicationConfigurator config){
//		http://www.uk.ipni.org/ipni/advPublicationSearch.do?find_title=Spe*plant*&find_abbreviation=&output_format=normal&query_type=by_query&back_page=publicationsearch
//		http://www.uk.ipni.org/ipni/advPublicationSearch.do?find_title=*Hortus+Britannicus*&find_abbreviation=&output_format=delimited-classic&output_format=delimited
		
		if (config == null){
			config = new IpniServicePublicationConfigurator();
		}
		
		title = normalizeParameter(title);
		abbreviation = normalizeParameter(abbreviation);
	
		String request = "find_title=" + title +
						"&find_abbreviation=" + abbreviation +
						"&output_format=" + DelimitedFormat.CLASSIC.parameter;
		
		List<ReferenceBase> result = (List)queryService(request, services, getServiceUrl(IIpniService.PUBLICATION_SERVICE_URL), config, ServiceType.PUBLICATION);
		return result;
	}

	
	
	/**
	 * @return
	 */
	private DelimitedFormat getDefaultFormat() {
		return DelimitedFormat.SHORT;
	}

	
	/**
	 * The service url
	 *
	 * @return the serviceUrl
	 */
	public URL getServiceUrl(String url) {
		URL serviceUrl;
		try {
			serviceUrl = new URL(url);
		} catch (MalformedURLException e) {
			throw new RuntimeException("This should not happen", e);
		}
		return serviceUrl;
	}

		
	
}
