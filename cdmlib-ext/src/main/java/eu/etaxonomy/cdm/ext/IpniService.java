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
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
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
	
	 private enum ServiceType{
		 AUTHOR,
		 NAME,
		 PUBLICATION,
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
	public List<Person> getAuthors(String abbreviation, String surname, String forename, String isoCountry, DelimitedFormat format , ICdmApplicationConfiguration appConfig){
		abbreviation = CdmUtils.Nz(abbreviation);
		surname = CdmUtils.Nz(surname);
		isoCountry = CdmUtils.Nz(isoCountry);
		forename = CdmUtils.Nz(forename);
		
		format = format == null ? getDefaultFormat() : format;
			
		String request = "find_abbreviation=" + abbreviation + 
						"&find_surname=" + surname + 
						"&find_isoCountry=" + isoCountry + 
						"&find_forename=" + forename +
						"&output_format=" + format.parameter;
		
		return (List)queryService(request, appConfig, getServiceUrl(IIpniService.AUTHOR_SERVICE_URL), ServiceType.AUTHOR);
	}
	       

	/**
	 *
	 * @param restRequest
	 * @return
	*/
	private List<? extends IdentifiableEntity> queryService(String request, ICdmApplicationConfiguration appConfig, URL serviceUrl, ServiceType serviceType){
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
            	result = buildAuthorList(content, appConfig);
            }else if (serviceType.equals(ServiceType.NAME)){
            	result = buildNameList(content, appConfig);
            }else{
            	result = buildPublicationList(content, appConfig);
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

	private List<ReferenceBase> buildPublicationList( InputStream content, ICdmApplicationConfiguration appConfig) {
		throw new RuntimeException("Publication service not yet implemented");
	}


	private List<BotanicalName> buildNameList( InputStream content, ICdmApplicationConfiguration appConfig) throws IOException {
		List<BotanicalName> result = new ArrayList<BotanicalName>(); 
		BufferedReader reader = new BufferedReader (new InputStreamReader(content));
		
		String headerLine = reader.readLine();
		Map<Integer, String> parameterMap = getAuthorParameterMap(headerLine);
		
		String line = reader.readLine();
		while (StringUtils.isNotBlank(line)){
			BotanicalName name = getNameFromLine(line,parameterMap, appConfig);
			result.add(name);
			line = reader.readLine();
		}

		return result;
	}


	private BotanicalName getNameFromLine(String line, Map<Integer, String> categoryMap, ICdmApplicationConfiguration appConfig) {
		//Id%Version%Standard form%Default author forename%Default author surname%Taxon groups%Dates%Alternative names
		String[] splits = line.split("%");
		Map<String, String> valueMap = new HashMap<String, String>();
		
		for (int i = 0; i < splits.length; i++){
			valueMap.put(categoryMap.get(i), splits[i]);
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
		
		//basionym
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


	private List<Person> buildAuthorList(InputStream content, ICdmApplicationConfiguration appConfig) throws IOException {
		List<Person> result = new ArrayList<Person>(); 
		BufferedReader reader = new BufferedReader (new InputStreamReader(content));
		
		String headerLine = reader.readLine();
		Map<Integer, String> parameterMap = getAuthorParameterMap(headerLine);
		
		String line = reader.readLine();
		while (StringUtils.isNotBlank(line)){
			Person author = getAuthorFromLine(line,parameterMap, appConfig);
			result.add(author);
			line = reader.readLine();
		}

		return result;
	}



	private Map<Integer, String> getAuthorParameterMap(String headerLine) {
		Map<Integer, String> result = new HashMap<Integer, String>();
		String[] splits = headerLine.split("%");
		for (int i = 0; i < splits.length ; i ++){
			result.put(i, splits[i]);
		}
		return result;
	}


	private Person getAuthorFromLine(String line, Map<Integer, String> categoryMap, ICdmApplicationConfiguration appConfig) {
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

	/**
	 *
	 * @param restRequest
	 * @return
	 * @throws MalformedURLException 
	 */
	public List<BotanicalName> getNamesAdvanced(String family, String genus, String species, String infraFamily, 
			String infraGenus, String infraSpecies, String authorAbbrev, Boolean includePublicationAuthors, 
			Boolean includeBasionymAuthors,
			String publicationTitle,
			Boolean isAPNIRecord, 
			Boolean isGCIRecord, 
			Boolean isIKRecord,
			Rank rankToReturn,
			Boolean sortByFamily,
			DelimitedFormat format , 
			ICdmApplicationConfiguration appConfig) {
		
//		find_rankToReturn=all&output_format=normal&find_sortByFamily=on&find_sortByFamily=off&query_type=by_query&back_page=plantsearch
			
		family = normalizeParameter(family);
		genus = normalizeParameter(genus);
		species = normalizeParameter(species);
		infraFamily = normalizeParameter(infraFamily);
		infraGenus = normalizeParameter(infraGenus);
		infraSpecies = normalizeParameter(infraSpecies);
		authorAbbrev = normalizeParameter(authorAbbrev);

		publicationTitle = normalizeParameter(publicationTitle);
		
		format = format == null ? getDefaultFormat() : format;
		
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
		
		return (List)queryService(request, appConfig, getServiceUrl(IIpniService.ADVANCED_NAME_SERVICE_URL), ServiceType.NAME);

			
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


	/**
	 *
	 * @param restRequest
	 * @return
	 */
	public List<BotanicalName> getNamesSimple(String wholeName, DelimitedFormat format , ICdmApplicationConfiguration appConfig){
		
		
//		query_type=by_query&back_page=query_ipni.html
		
		wholeName = normalizeParameter(wholeName);
		
		format = format == null ? getDefaultFormat() : format;
			
		String request = "find_wholeName=" + wholeName + 
						"&output_format=" + format.parameter;
		
		return (List)queryService(request, appConfig, getServiceUrl(IIpniService.SIMPLE_NAME_SERVICE_URL), ServiceType.NAME);
	}

	/**
	 *
	 * @param restRequest
	 * @return
	 */
	public List<ReferenceBase> getPublications(String wholeName, DelimitedFormat format , ICdmApplicationConfiguration appConfig){
		wholeName = normalizeParameter(wholeName);
		
		format = format == null ? getDefaultFormat() : format;
			
		String request = "find_wholeName=" + wholeName + 
						"&output_format=" + format.parameter;
		
		return (List)queryService(request, appConfig, getServiceUrl(IIpniService.PUBLICATION_SERVICE_URL), ServiceType.PUBLICATION);
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
