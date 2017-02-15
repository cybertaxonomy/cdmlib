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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.UriUtils;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * @author a.mueller
 * @created Aug 16, 2010
 *
 * TODO the whole ipni service should be refactored to use UriUtils. I did this for the queryService method only because we ran into timeout
 * problems in tests. UriUtils handles these problems already.
 *
 */
@Component
public class IpniService  implements IIpniService{
	private static final String EAST_OR_WEST = "East or west";

	private static final String NORTH_OR_SOUTH = "North or south";

	private static final String LATITUDE_SECONDS = "Latitude seconds";

	private static final String LATITUDE_MINUTES = "Latitude minutes";

	private static final String LATITUDE_DEGREES = "Latitude degrees";

	private static final String COLLECTION_DATE_AS_TEXT = "Collection date as text";

	private static final String COLLECTION_DAY1 = "Collection day1";

	private static final String COLLECTION_MONTH1 = "Collection month1";

	private static final String COLLECTION_YEAR1 = "Collection year1";

	private static final String COLLECTION_DAY2 = "Collection day2";

	private static final String COLLECTION_MONTH2 = "Collection month2";

	private static final String COLLECTION_YEAR2 = "Collection year2";

	private static final String COLLECTION_NUMBER = "Collection number";

	private static final String COLLECTOR_TEAM_AS_TEXT = "Collector team as text";

	private static final String LOCALITY = "Locality";

	private static final String TYPE_REMARKS = "Type remarks";

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
		 ID
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

// ****************************** METHODS ****************************************************/


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.ext.ipni.IIpniService#getAuthors(java.lang.String, java.lang.String, java.lang.String, java.lang.String, eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration, eu.etaxonomy.cdm.ext.ipni.IpniServiceAuthorConfigurator)
	 */
	@Override
    public List<Person> getAuthors(String abbreviation, String surname, String forename, String isoCountry, ICdmRepository services, IpniServiceAuthorConfigurator config){
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
	 *	FIXME rewrote this method to rely on {@link UriUtils}. The whole class should be adjusted to reflect this change.
	 *	Also see comments in the class' documentation block.
	 *
	 * @param restRequest
	 * @return
	*/
	private List<? extends IdentifiableEntity> queryService(String request, ICdmRepository services, URL serviceUrl, IIpniServiceConfigurator config, ServiceType serviceType){
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

            URI newUri = newUrl.toURI();
            logger.info("Firing request for URI: " + newUri);
            HttpResponse response = UriUtils.getResponse(newUri, null);

            int responseCode = response.getStatusLine().getStatusCode();

            // get the content at the resource
            InputStream content = response.getEntity().getContent();
            // build the result
            List<? extends IdentifiableEntity> result;
            if (serviceType.equals(ServiceType.AUTHOR)){
            	result = buildAuthorList(content, services, config);
            }else if (serviceType.equals(ServiceType.NAME)){
            	result = buildNameList(content, services, config);
            }else {
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
        } catch (URISyntaxException e) {
			logger.error("Given URL could not be transformed into URI", e);
		}

        // error
        return null;
    }

	public InputStream queryServiceForID (String request, URL serviceUrl){

		try {

            // create the request url
            URL newUrl = new URL(serviceUrl.getProtocol(),
                                                     serviceUrl.getHost(),
                                                     serviceUrl.getPort(),
                                                     serviceUrl.getPath()
                                                     + "?" + request);


            URI newUri = newUrl.toURI();

            logger.info("Firing request for URI: " + newUri);

            HttpResponse response = UriUtils.getResponse(newUri, null);

            int responseCode = response.getStatusLine().getStatusCode();

            // get the content at the resource
            InputStream content = response.getEntity().getContent();
            return content;

		   } catch (IOException e) {
	            logger.error("No content for request: " + request);
	        } catch (URISyntaxException e) {
				logger.error("Given URL could not be transformed into URI", e);
			}

		return null;

	}




	private List<Reference> buildPublicationList( InputStream content, ICdmRepository services, IIpniServiceConfigurator iConfig) throws IOException {
		IpniServicePublicationConfigurator config = (IpniServicePublicationConfigurator)iConfig;

		List<Reference> result = new ArrayList<Reference>();
		BufferedReader reader = new BufferedReader (new InputStreamReader(content));

		String headerLine = reader.readLine();
		Map<Integer, String> parameterMap = getParameterMap(headerLine);

		String line = reader.readLine();
		while (StringUtils.isNotBlank(line)){
			Reference reference = getPublicationFromLine(line, parameterMap, services, config);
			result.add(reference);
			line = reader.readLine();
		}

		return result;
	}


	/**
	 * @param line
	 * @param parameterMap
	 * @param appConfig
	 * @param config
	 * @return
	 */
	private Reference getPublicationFromLine(String line, Map<Integer, String> parameterMap, ICdmRepository appConfig, IpniServicePublicationConfigurator config) {
		//fill value map
		String[] splits = line.split("%");

		Map<String, String> valueMap = new HashMap<String, String>();
		for (int i = 0; i < splits.length; i++){
			valueMap.put(parameterMap.get(i), splits[i]);
		}

		//create reference object
		Reference ref = ReferenceFactory.newGeneric();

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
		if (StringUtils.isNotBlank(author)){
			Team team = Team.NewTitledInstance(author, author);
			ref.setAuthorship(team);
		}

		//remarks
		String remarks = valueMap.get(REMARKS);
		Annotation annotation = Annotation.NewInstance(remarks, AnnotationType.EDITORIAL(), Language.ENGLISH());
		ref.addAnnotation(annotation);


		String tl2AuthorString = valueMap.get(TL2_AUTHOR);
		if (ref.getAuthorship() == null){
			Team tl2Author = Team.NewTitledInstance(tl2AuthorString, null);
			ref.setAuthorship(tl2Author);
		}else{
			//TODO parse name,
			ref.getAuthorship().setTitleCache(tl2AuthorString, true);
			ref.addAnnotation(Annotation.NewInstance(tl2AuthorString, AnnotationType.EDITORIAL(), Language.ENGLISH()));
		}


		//dates
		TimePeriod date = TimePeriodParser.parseString(valueMap.get(DATE));
		ref.setDatePublished(date);


		//source
		Reference citation = getIpniCitation(appConfig);
		ref.addSource(OriginalSourceType.Lineage, valueMap.get(ID), "Publication", citation, valueMap.get(VERSION));



/*		TODO
		BPH number
		Authors role
		In publication facade
		LC number
		Preceded by
		TL2 number
		TDWG abbreviation
	*/

		return ref;
	}


	private List<TaxonNameBase<?,?>> buildNameList( InputStream content, ICdmRepository appConfig, IIpniServiceConfigurator iConfig) throws IOException {
		IpniServiceNamesConfigurator config = (IpniServiceNamesConfigurator)iConfig;
		List<TaxonNameBase<?,?>> result = new ArrayList<>();
		BufferedReader reader = new BufferedReader (new InputStreamReader(content));

		String headerLine = reader.readLine();
		Map<Integer, String> parameterMap = getParameterMap(headerLine);

		String line = reader.readLine();
		while (StringUtils.isNotBlank(line)){

		    TaxonNameBase<?,?> name = (TaxonNameBase<?,?>)getNameFromLine(line,parameterMap, appConfig);
			result.add(name);
			line = reader.readLine();

		}


		return result;
	}


	private IBotanicalName getNameFromLine(String line, Map<Integer, String> parameterMap, ICdmRepository appConfig) {
		//Id%Version%Standard form%Default author forename%Default author surname%Taxon groups%Dates%Alternative names
		String[] splits = line.split("%");
		Map<String, String> valueMap = new HashMap<String, String>();

		for (int i = 0; i < splits.length; i++){
			valueMap.put(parameterMap.get(i), splits[i]);
		}

		IBotanicalName name = TaxonNameFactory.NewBotanicalInstance(null);

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
			name.setRank(Rank.getRankByNameOrIdInVoc(rankStr, NomenclaturalCode.ICNAFP, true));
		} catch (UnknownCdmTypeException e) {
			logger.warn("Rank was unknown");
		}

		//authors
		name.setBasionymAuthorship(Team.NewTitledInstance(valueMap.get(BASIONYM_AUTHOR), valueMap.get(BASIONYM_AUTHOR)));
		name.setCombinationAuthorship(Team.NewTitledInstance(valueMap.get(PUBLISHING_AUTHOR), valueMap.get(PUBLISHING_AUTHOR)));

		//publication
		Reference ref = ReferenceFactory.newGeneric();
		ref.setTitleCache(valueMap.get(PUBLICATION), true);
		TimePeriod datePublished = TimePeriodParser.parseString(valueMap.get(PUBLICATION_YEAR_FULL));
		name.setNomenclaturalReference(ref);

		//name status
		NomenclaturalStatusType statusType = null;
		String statusString = valueMap.get(NAME_STATUS);
		if (StringUtils.isNotBlank(statusString)){
			try {
				statusType = NomenclaturalStatusType.getNomenclaturalStatusTypeByAbbreviation(statusString, name);
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
		TaxonNameBase<?,?> basionym = TaxonNameFactory.NewBotanicalInstance(null);
		basionym.setTitleCache(valueMap.get(BASIONYM), true);
		name.addBasionym(basionym);

		//replaced synonym
		TaxonNameBase<?,?> replacedSynoynm = TaxonNameFactory.NewBotanicalInstance(null);
		replacedSynoynm.setTitleCache(valueMap.get(REPLACED_SYNONYM), true);
		name.addReplacedSynonym(replacedSynoynm, null, null, null);

		//type information
		DerivedUnitFacade specimen = DerivedUnitFacade.NewInstance(SpecimenOrObservationType.PreservedSpecimen);


		//gathering period
		String collectionDateAsText = valueMap.get(COLLECTION_DATE_AS_TEXT);
		TimePeriod gatheringPeriod = TimePeriodParser.parseString(collectionDateAsText);

		try {
			gatheringPeriod.setStartDay(getIntegerDateValueOrNull(valueMap, COLLECTION_DAY1));
			gatheringPeriod.setStartMonth(getIntegerDateValueOrNull(valueMap, COLLECTION_MONTH1));
			gatheringPeriod.setStartYear(getIntegerDateValueOrNull(valueMap, COLLECTION_YEAR1));
			gatheringPeriod.setEndDay(getIntegerDateValueOrNull(valueMap, COLLECTION_DAY2));
			gatheringPeriod.setEndMonth(getIntegerDateValueOrNull(valueMap, COLLECTION_MONTH2));
			gatheringPeriod.setEndYear(getIntegerDateValueOrNull(valueMap, COLLECTION_YEAR2));
		} catch (IndexOutOfBoundsException e) {
			logger.info("Exception occurred when trying to fill gathering period");
		}
		specimen.setGatheringPeriod(gatheringPeriod);

		specimen.setFieldNumber(valueMap.get(COLLECTION_NUMBER));

		//collector team
		String team = valueMap.get(COLLECTOR_TEAM_AS_TEXT);
		Team collectorTeam = Team.NewTitledInstance(team, team);
		specimen.setCollector(collectorTeam);

		specimen.setLocality(valueMap.get(LOCALITY));

		try {
			String latDegrees = CdmUtils.Nz(valueMap.get(LATITUDE_DEGREES));
			String latMinutes = CdmUtils.Nz(valueMap.get(LATITUDE_MINUTES));
			String latSeconds = CdmUtils.Nz(valueMap.get(LATITUDE_SECONDS));
			String direction = CdmUtils.Nz(valueMap.get(NORTH_OR_SOUTH));
			String latitude = latDegrees + "°" + latMinutes + "'" + latSeconds + "\"" + direction;

			String lonDegrees = CdmUtils.Nz(valueMap.get(LATITUDE_DEGREES));
			String lonMinutes = CdmUtils.Nz(valueMap.get(LATITUDE_MINUTES));
			String lonSeconds = CdmUtils.Nz(valueMap.get(LATITUDE_SECONDS));
			direction = CdmUtils.Nz(valueMap.get(EAST_OR_WEST));
			String longitude = lonDegrees + "°" + lonMinutes + "'" + lonSeconds + "\"" + direction;


			specimen.setExactLocationByParsing(longitude, latitude, null, null);
		} catch (ParseException e) {
			logger.info("Parsing exception occurred when trying to parse type exact location."  + e.getMessage());
		} catch (Exception e) {
			logger.info("Exception occurred when trying to read type exact location."  + e.getMessage());
		}


		//type annotation
		Annotation typeAnnotation = Annotation.NewInstance(TYPE_REMARKS, AnnotationType.EDITORIAL(), Language.DEFAULT());
		specimen.addAnnotation(typeAnnotation);


		//TODO  Type name
		//TODO "Type locations"  , eg. holotype   CAT  ,isotype   CAT  ,isotype   FI

		//TODO Geographic unit as text





		//source
		Reference citation = getIpniCitation(appConfig);
		name.addSource(OriginalSourceType.Lineage, valueMap.get(ID), "Name", citation, valueMap.get(VERSION));


//		//TODO
		//SHORT Family, Infra family, Hybrid genus, Hybrid, Collation, Nomenclatural synonym, Distribution, Citation type
/*		EXTENDED
 *      Species author,
 *       Standardised basionym author flag,
 *       Standardised publishing author flag
	      Full name
	      Full name without family
	      Full name without authors

	      Reference
	      Standardised publication flag
	      Publication year
	      publication year note
	      Publication year text
	      Volume
	      Start page
	      End page
	      Primary pagination
	      Secondary pagination
	      Reference remarks
	      Hybrid parents
	      Replaced synonym Author team
	      Other links
	      Same citation as
	      Bibliographic reference
	      Bibliographic type info

	      Original taxon name
	      Original taxon name author team
	      Original replaced synonym
	      Original replaced synonym author team
	      Original basionym
	      Original basionym author team
	      Original parent citation taxon name author team
	      Original taxon distribution
	      Original hybrid parentage
	      Original cited type
	      Original remarks

		*/
		return name;
	}

	/**
	 * @param valueMap
	 * @return
	 */
	private Integer getIntegerDateValueOrNull(Map<String, String> valueMap, String key) {
		try {
			Integer result = Integer.valueOf(valueMap.get(key));
			if (result == 0){
				result = null;
			}
			return result;
		} catch (NumberFormatException e) {
			if (logger.isDebugEnabled()){
				logger.debug("Number Format exception for " + valueMap.get(key));
			}
			return null;
		}
	}


	private String nomalizeRank(String string) {
		String result = string.replace("spec.", "sp.");
		return result;
	}


	private List<Person> buildAuthorList(InputStream content, ICdmRepository services, IIpniServiceConfigurator iConfig) throws IOException {
		IpniServiceAuthorConfigurator config = (IpniServiceAuthorConfigurator)iConfig;
		List<Person> result = new ArrayList<Person>();
		BufferedReader reader = new BufferedReader (new InputStreamReader(content));

		String headerLine = reader.readLine();
		if (headerLine != null){
			Map<Integer, String> parameterMap = getParameterMap(headerLine);

			String line = reader.readLine();
			while (StringUtils.isNotBlank(line)){
				Person author = getAuthorFromLine(line,parameterMap, services, config);
				result.add(author);
				line = reader.readLine();
			}
		}

		return result;
	}



	private Map<Integer, String> getParameterMap(String headerLine) {
		Map<Integer, String> result = new HashMap<Integer, String>();
		if ( headerLine != null ){
			String[] splits = headerLine.split("%");
			for (int i = 0; i < splits.length ; i ++){
				result.put(i, splits[i]);
			}
		}
		return result;
	}


	private Person getAuthorFromLine(String line, Map<Integer, String> categoryMap, ICdmRepository appConfig, IpniServiceAuthorConfigurator config) {
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

		Reference citation = getIpniCitation(appConfig);

		//id, version
		person.addSource(OriginalSourceType.Lineage, valueMap.get(ID), "Author", citation, valueMap.get(VERSION));

		//dates
		TimePeriod lifespan = TimePeriodParser.parseString(valueMap.get(DATES));
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


	private Reference getIpniCitation(ICdmRepository appConfig) {
		Reference ipniReference;
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
	private Reference getNewIpniReference() {
		Reference ipniReference;
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

	@Override
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
			ICdmRepository services){
		IpniRank ipniRank = IpniRank.valueOf(rankInRangeToReturn);
		return getNamesAdvanced(family, genus, species, infraFamily, infraGenus, infraSpecies, authorAbbrev, includePublicationAuthors, includeBasionymAuthors, publicationTitle, isAPNIRecord, isGCIRecord, isIKRecord, ipniRank, sortByFamily, config, services);
	}

	@Override
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
			ICdmRepository services) {

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
	@Override
    public List<BotanicalName> getNamesSimple(String wholeName, ICdmRepository services, IpniServiceNamesConfigurator config){
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
	@Override
    public List<Reference> getPublications(String title, String abbreviation, ICdmRepository services, IpniServicePublicationConfigurator config){
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

		List<Reference> result = (List)queryService(request, services, getServiceUrl(IIpniService.PUBLICATION_SERVICE_URL), config, ServiceType.PUBLICATION);
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
	@Override
    public URL getServiceUrl(String url) {
		URL serviceUrl;
		try {
			serviceUrl = new URL(url);
		} catch (MalformedURLException e) {
			throw new RuntimeException("This should not happen", e);
		}
		return serviceUrl;
	}


	@Override
	public InputStream getNamesById(String id) {


		String request = "id="+id + "&output_format=lsid-metadata";
		return queryServiceForID(request, getServiceUrl(IIpniService.ID_NAMESEARCH_SERVICE_URL));

	}

	@Override
	public InputStream getPublicationsById(String id) {


		String request = "id="+id ;
		return queryServiceForID(request, getServiceUrl(IIpniService.ID_PUBLICATION_SERVICE_URL));

	}



}
