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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

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
import eu.etaxonomy.cdm.strategy.exceptions.StringNotParsableException;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;
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
    private static final Logger logger = Logger.getLogger(IpniService.class);


    //TYPE
    private static final String EAST_OR_WEST = "East or west";

	private static final String NORTH_OR_SOUTH = "North or south";

	private static final String LATITUDE_SECONDS = "Latitude seconds";

	private static final String LATITUDE_MINUTES = "Latitude minutes";

	private static final String LATITUDE_DEGREES = "Latitude degrees";

	private static final String LONGITUDE_SECONDS = "Longitude seconds";

    private static final String LONGITUDE_MINUTES = "Longitude minutes";

    private static final String LONGITUDE_DEGREES = "Longitude degrees";



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
	public static final String HYBRID = "Hybrid";
	public static final String RANK = "Rank";
	public static final String BASIONYM_AUTHOR = "Basionym author";
	public static final String PUBLISHING_AUTHOR = "Publishing author";
	public static final String PUBLICATION = "Publication";
	public static final String COLLATION = "Collation";
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
	 *	FIXME rewrote this method to rely on {@link UriUtils}. The whole class should be
	 *  adjusted to reflect this change.
	 *	Also see comments in the class' documentation block.
	 *
	 * @param restRequest
	 * @return
	*/
	private List<? extends IdentifiableEntity> queryService(String request, ICdmRepository repository, URL serviceUrl,
	            IIpniServiceConfigurator config, ServiceType serviceType){
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
            List<? extends IdentifiableEntity<?>> result;
            if (serviceType.equals(ServiceType.AUTHOR)){
            	result = buildAuthorList(content, repository, config);
            }else if (serviceType.equals(ServiceType.NAME)){
            	result = buildNameList(content, repository, config);
            }else {
            	result = buildPublicationList(content, repository, config);
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
	           throw new RuntimeException(e);
	       } catch (URISyntaxException e) {
				logger.error("Given URL could not be transformed into URI", e);
				   throw new RuntimeException(e);
		   }

	}




	private List<Reference> buildPublicationList( InputStream content, ICdmRepository services, IIpniServiceConfigurator iConfig) throws IOException {
		IpniServicePublicationConfigurator config = (IpniServicePublicationConfigurator)iConfig;

		List<Reference> result = new ArrayList<>();
		BufferedReader reader = new BufferedReader (new InputStreamReader(content));

		String headerLine = reader.readLine();
		Map<Integer, String> parameterMap = getParameterMap(headerLine);

		String line = reader.readLine();
		while (isNotBlank(line)){
			Reference reference = getPublicationFromLine(line, parameterMap, services, config);
			result.add(reference);
			line = reader.readLine();
		}

		return result;
	}


	/**
	 * @param line
	 * @param parameterMap
	 * @param repository
	 * @param config
	 * @return
	 */
	private Reference getPublicationFromLine(String line, Map<Integer, String> parameterMap,
	        ICdmRepository repository, IpniServicePublicationConfigurator config) {
		//fill value map
		String[] splits = line.split("%");

		Map<String, String> valueMap = fillValueMap(parameterMap, splits);

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
		if (isNotBlank(author)){
			Team team = Team.NewTitledInstance(author, author);
			ref.setAuthorship(team);
		}

		//remarks
		String remarks = valueMap.get(REMARKS);
		if (remarks != null){
		    Annotation annotation = Annotation.NewInstance(remarks, AnnotationType.EDITORIAL(), Language.ENGLISH());
		    ref.addAnnotation(annotation);
		}


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
		Reference citation = getIpniCitation(repository);
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


	private List<TaxonNameBase<?,?>> buildNameList( InputStream content, ICdmRepository repository, IIpniServiceConfigurator iConfig) throws IOException {
		IpniServiceNamesConfigurator config = (IpniServiceNamesConfigurator)iConfig;
		List<TaxonNameBase<?,?>> result = new ArrayList<>();
		BufferedReader reader = new BufferedReader (new InputStreamReader(content));

		String headerLine = reader.readLine();
//		System.out.println(headerLine);
		Map<Integer, String> parameterMap = getParameterMap(headerLine);

		String line = reader.readLine();
		while (isNotBlank(line)){
//		    System.out.println(line);
		    TaxonNameBase<?,?> name = (TaxonNameBase<?,?>)getNameFromLine(line,parameterMap, repository, config);
			result.add(name);
			line = reader.readLine();
		}

		return result;
	}


	private static final NonViralNameParserImpl nvnParser = NonViralNameParserImpl.NewInstance();

	private IBotanicalName getNameFromLine(String line, Map<Integer, String> parameterMap, ICdmRepository repository, IpniServiceNamesConfigurator config) {
		//Id%Version%Standard form%Default author forename%Default author surname%Taxon groups%Dates%Alternative names
		String[] splits = line.split("%");

		Map<String, String> valueMap = fillValueMap(parameterMap, splits);

		IBotanicalName name = TaxonNameFactory.NewBotanicalInstance(null);

		//epithets
		name.setGenusOrUninomial(valueMap.get(GENUS));
		name.setInfraGenericEpithet(valueMap.get(INFRA_GENUS));
		name.setSpecificEpithet(valueMap.get(SPECIES));
		name.setInfraSpecificEpithet(valueMap.get(INFRA_SPECIFIC));

		//rank
		try {
			String rankStr = nomalizeRank(valueMap.get(RANK));
			Rank rank = Rank.getRankByNameOrIdInVoc(rankStr, NomenclaturalCode.ICNAFP, true);
			name.setRank(rank);
		} catch (UnknownCdmTypeException e) {
			logger.warn("Rank was unknown");
		}
        //caches
		String pureName = valueMap.get(FULL_NAME_WITHOUT_FAMILY_AND_AUTHORS);
        String nameCache = name.getNameCache();
		if (!Nz(pureName).equals(nameCache)) {
            nvnParser.parseSimpleName(name, valueMap.get(FULL_NAME_WITHOUT_FAMILY_AND_AUTHORS), name.getRank(), true);
//            name.setNameCache(valueMap.get(FULL_NAME_WITHOUT_FAMILY_AND_AUTHORS), true);
        }



        String authors = "";
		//authors
		if (valueMap.get(BASIONYM_AUTHOR)!= null){
		    authors = valueMap.get(BASIONYM_AUTHOR);
//		    name.setBasionymAuthorship(Team.NewTitledInstance(valueMap.get(BASIONYM_AUTHOR), valueMap.get(BASIONYM_AUTHOR)));
		}
        if (valueMap.get(PUBLISHING_AUTHOR)!= null){
            authors += valueMap.get(PUBLISHING_AUTHOR);
//            name.setCombinationAuthorship(Team.NewTitledInstance(valueMap.get(PUBLISHING_AUTHOR), valueMap.get(PUBLISHING_AUTHOR)));
        }
        try {
            nvnParser.parseAuthors(name, authors);
        } catch (StringNotParsableException e1) {
            //
        }
        if (!Nz(valueMap.get(AUTHORS)).equals(name.getAuthorshipCache())) {
            name.setAuthorshipCache(valueMap.get(AUTHORS), true);
        }
        if ("Y".equals(valueMap.get(HYBRID))){
            if (!name.isHybrid()){
                //Is there a concrete way to include the hybrid flag info? As it does not say which type of hybrid it seems
                //to be best to handle hybrids via parsing. But there might be a better errror handling possible.
                logger.warn("Name is flagged as hybrid at IPNI but CDM name has no hybrid flag set: " + name.getTitleCache());
            }
        }

		//publication
		if (valueMap.get(PUBLICATION)!= null || valueMap.get(COLLATION)!= null || valueMap.get(PUBLICATION_YEAR_FULL) != null){
		    Reference ref = ReferenceFactory.newGeneric();

		    //TODO probably we can do better parsing here
		    String pub = CdmUtils.concat(" ", valueMap.get(PUBLICATION), valueMap.get(COLLATION));
		    if (isNotBlank(pub)){
    		    String nomRefTitle = pub;
    		    String[] split = nomRefTitle.split(":");
    		    if (split.length > 1){
    		        String detail = split[split.length-1];
    		        name.setNomenclaturalMicroReference(detail.trim());
    		        nomRefTitle = nomRefTitle.substring(0, nomRefTitle.length() - detail.length() - 1).trim();
    		    }

    		    ref.setAbbrevTitle(nomRefTitle);
		    }

    		TimePeriod datePublished = parsePublicationFullYear(valueMap.get(PUBLICATION_YEAR_FULL));
		    ref.setDatePublished(datePublished);

		    name.setNomenclaturalReference(ref);
		}

		//name status
		NomenclaturalStatusType statusType = null;
		String statusString = valueMap.get(NAME_STATUS);
		if (isNotBlank(statusString)){
			try {
				statusType = NomenclaturalStatusType.getNomenclaturalStatusTypeByAbbreviation(statusString, name);
				NomenclaturalStatus nomStatus = NomenclaturalStatus.NewInstance(statusType);
				name.addStatus(nomStatus);
			} catch (UnknownCdmTypeException e) {
				logger.warn("Name status not recognized: " + statusString);
	            Annotation annotation = Annotation.NewInstance("Name status: " + statusString, AnnotationType.EDITORIAL(), Language.ENGLISH());
	            name.addAnnotation(annotation);
			}
		}

		//remarks
		String remarks = valueMap.get(REMARKS);
		if (remarks != null){
		    Annotation annotation = Annotation.NewInstance(remarks, AnnotationType.EDITORIAL(), Language.ENGLISH());
		    name.addAnnotation(annotation);
		}

		//basionym
		if (config.isDoBasionyms() && valueMap.get(BASIONYM)!= null){
		    TaxonNameBase<?,?> basionym = TaxonNameFactory.NewBotanicalInstance(null);
		    basionym.setTitleCache(valueMap.get(BASIONYM), true);
		    name.addBasionym(basionym);
		}

		//replaced synonym
		if (config.isDoBasionyms() && valueMap.get(REPLACED_SYNONYM)!= null){
		    TaxonNameBase<?,?> replacedSynoynm = TaxonNameFactory.NewBotanicalInstance(null);
		    replacedSynoynm.setTitleCache(valueMap.get(REPLACED_SYNONYM), true);
		    name.addReplacedSynonym(replacedSynoynm, null, null, null);
		}

		//type information
		if (config.isDoType() && valueMap.get(COLLECTION_DATE_AS_TEXT)!= null || valueMap.get(COLLECTION_NUMBER) != null
		        || valueMap.get(COLLECTION_DAY1) != null || valueMap.get(COLLECTION_DAY2) != null
		        || valueMap.get(COLLECTION_MONTH1) != null || valueMap.get(COLLECTION_MONTH2) != null
		        || valueMap.get(COLLECTION_YEAR1) != null || valueMap.get(COLLECTION_YEAR2) != null
		        || valueMap.get(COLLECTOR_TEAM_AS_TEXT) != null || valueMap.get(LOCALITY)!= null
		        || valueMap.get(LATITUDE_DEGREES) != null || valueMap.get(LATITUDE_MINUTES) != null
                || valueMap.get(LATITUDE_SECONDS) != null || valueMap.get(NORTH_OR_SOUTH) != null
                || valueMap.get(COLLECTION_YEAR1) != null || valueMap.get(COLLECTION_YEAR2) != null
                //TODO TBC
		        ){
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
    		if (team != null){
    		    Team collectorTeam = Team.NewTitledInstance(team, team);
    		    specimen.setCollector(collectorTeam);
    		}

    		specimen.setLocality(valueMap.get(LOCALITY));

    		try {
    			String latDegrees = CdmUtils.Nz(valueMap.get(LATITUDE_DEGREES));
    			String latMinutes = CdmUtils.Nz(valueMap.get(LATITUDE_MINUTES));
    			String latSeconds = CdmUtils.Nz(valueMap.get(LATITUDE_SECONDS));
    			String direction = CdmUtils.Nz(valueMap.get(NORTH_OR_SOUTH));
    			String latitude = latDegrees + "°" + latMinutes + "'" + latSeconds + "\"" + direction;

    			String lonDegrees = CdmUtils.Nz(valueMap.get(LONGITUDE_DEGREES));
    			String lonMinutes = CdmUtils.Nz(valueMap.get(LONGITUDE_MINUTES));
    			String lonSeconds = CdmUtils.Nz(valueMap.get(LONGITUDE_SECONDS));
    			direction = CdmUtils.Nz(valueMap.get(EAST_OR_WEST));
    			String longitude = lonDegrees + "°" + lonMinutes + "'" + lonSeconds + "\"" + direction;

    			specimen.setExactLocationByParsing(longitude, latitude, null, null);
    		} catch (ParseException e) {
    			logger.info("Parsing exception occurred when trying to parse type exact location."  + e.getMessage());
    		} catch (Exception e) {
    			logger.info("Exception occurred when trying to read type exact location."  + e.getMessage());
    		}


    		//type annotation
    		if (valueMap.get(TYPE_REMARKS)!= null){
    		    Annotation typeAnnotation = Annotation.NewInstance(valueMap.get(TYPE_REMARKS), AnnotationType.EDITORIAL(), Language.DEFAULT());
    		    specimen.addAnnotation(typeAnnotation);
    		}
		}

		//TODO  Type name
		//TODO "Type locations"  , eg. holotype   CAT  ,isotype   CAT  ,isotype   FI

		//TODO Geographic unit as text


		//source
		Reference citation = getIpniCitation(repository);
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

    private String datePatternStr = "([12][0789]\\d{2})\\s\\[([123]?\\d\\s[A-Z][a-z][a-z]\\s[1-2][0789]\\d{2})\\]";
    private Pattern datePattern = Pattern.compile(datePatternStr);

	/**
     * Parses the full year string as a {@link TimePeriod}
     * @param string
     * @return
     */
    private TimePeriod parsePublicationFullYear(String fullYearStr) {
        TimePeriod result = null;

        if (fullYearStr != null){
            Matcher matcher = datePattern.matcher(fullYearStr);
            if (matcher.matches()){
                String yearStr = matcher.group(1);
                Integer year = Integer.valueOf(yearStr);
                String exactDate = matcher.group(2);
                result = TimePeriodParser.parseString(exactDate);
                if (!year.equals(result.getStartYear())){
                    logger.warn("Year and exact date year do not match");
                    result = TimePeriod.NewInstance(year);
                    result.setFreeText(fullYearStr);
                }
            }else{
                result = TimePeriodParser.parseString(fullYearStr);
            }
        }
        return result;
    }


    /**
     * Fills the map where the key is the parameter name from the parameterMap and the value is the value from the split.
     * @param parameterMap
     * @param splits
     * @param valueMap
     */
    private Map<String, String> fillValueMap(Map<Integer, String> parameterMap, String[] splits) {
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < splits.length; i++){
		    String key = parameterMap.get(i);
		    String value = splits[i];
		    if (isNotBlank(value)){
		        result.put(key, value);
		    }
		}
        return result;
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


	private List<Person> buildAuthorList(InputStream content, ICdmRepository repository, IIpniServiceConfigurator iConfig) throws IOException {
		IpniServiceAuthorConfigurator config = (IpniServiceAuthorConfigurator)iConfig;
		List<Person> result = new ArrayList<>();
		BufferedReader reader = new BufferedReader (new InputStreamReader(content));

		String headerLine = reader.readLine();
		if (headerLine != null){
			Map<Integer, String> parameterMap = getParameterMap(headerLine);

			String line = reader.readLine();
			while (isNotBlank(line)){
				Person author = getAuthorFromLine(line,parameterMap, repository, config);
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


	private Person getAuthorFromLine(String line, Map<Integer, String> categoryMap, ICdmRepository repository, IpniServiceAuthorConfigurator config) {
		//Id%Version%Standard form%Default author forename%Default author surname%Taxon groups%Dates%Alternative names
		String[] splits = line.split("%");
		Map<String, String> valueMap = fillValueMap(categoryMap, splits);

		Person person = Person.NewInstance();

		person.setNomenclaturalTitle(valueMap.get(STANDARD_FORM));
		person.setFirstname(valueMap.get(DEFAULT_AUTHOR_FORENAME));
		person.setLastname(valueMap.get(DEFAULT_AUTHOR_SURNAME));

		Reference citation = getIpniCitation(repository);

		//id, version
		person.addSource(OriginalSourceType.Lineage, valueMap.get(ID), "Author", citation, valueMap.get(VERSION));

		//dates
		TimePeriod lifespan = TimePeriodParser.parseString(valueMap.get(DATES));
		person.setLifespan(lifespan);

		//alternative_names
		String alternativeNames = valueMap.get(ALTERNATIVE_NAMES);
		if (isNotBlank(alternativeNames)){
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
		ipniReference.setTitle("The International Plant Names Index (IPNI)");
		return ipniReference;
	}



	@Override
    public List<BotanicalName> getNamesAdvanced(String family, String genus, String species, String infraFamily,
			String infraGenus, String infraSpecies, String authorAbbrev,
			String publicationTitle,
			Rank rankInRangeToReturn,
			IpniServiceNamesConfigurator config,
			ICdmRepository services){
		IpniRank ipniRank = IpniRank.valueOf(rankInRangeToReturn);
		return getNamesAdvanced(family, genus, species, infraFamily, infraGenus, infraSpecies, authorAbbrev, publicationTitle, ipniRank, config, services);
	}

	@Override
    public List<BotanicalName> getNamesAdvanced(String family, String genus, String species, String infraFamily,
			String infraGenus, String infraSpecies, String authorAbbrev,
			String publicationTitle,
			IpniRank rankToReturn,
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
				getBooleanParameter("&find_includePublicationAuthors=", config.isIncludePublicationAuthors(), "on", "off") +
				getBooleanParameter("&find_includeBasionymAuthors=", config.isIncludeBasionymAuthors(), "on", "off") +
				getBooleanParameter("&find_isAPNIRecord=", config.isDoApni(), "on", "false") +
				getBooleanParameter("&find_isGCIRecord=", config.isDoGci(), "on", "false") +
				getBooleanParameter("&find_isIKRecord=", config.isDoIk(), "on", "false") +
				getBooleanParameter("&find_sortByFamily=", config.isSortByFamily(), "on", "off") +
				(rankToReturn == null? "all" : rankToReturn.strRank)+
				"&find_publicationTitle=" + publicationTitle +
				"&output_format=" + format.parameter;

		System.out.println(request);
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


	@Override
    public List<IBotanicalName> getNamesSimple(String wholeName, ICdmRepository repository,
            IpniServiceNamesConfigurator config){
		if (config == null){
			config = new IpniServiceNamesConfigurator();
		}

//		query_type=by_query&back_page=query_ipni.html

		wholeName = normalizeParameter(wholeName);

		DelimitedFormat format = config.getFormat();

		String request = "find_wholeName=" + wholeName +
						"&output_format=" + format.parameter;

		return (List)queryService(request, repository, getServiceUrl(IIpniService.SIMPLE_NAME_SERVICE_URL), config, ServiceType.NAME);
	}

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


    /**
     * @param parameter
     */
    private String normalizeParameter(String parameter) {
        String result = CdmUtils.Nz(parameter).replace(" ", "+");
        return result;
    }

    private String nomalizeRank(String string) {
        if (string == null){
            return null;
        }
        String result = string.replace("spec.", "sp.");
        return result;
    }

    /**
     * @return
     */
    private DelimitedFormat getDefaultFormat() {
        return DelimitedFormat.SHORT;
    }

    private boolean isNotBlank(String line) {
        return StringUtils.isNotBlank(line);
    }

    @NotNull
    private String Nz(String string) {
        return CdmUtils.Nz(string);
    }


}
