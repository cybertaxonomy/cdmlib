/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.io.eflora.centralAfrica.ericaceae;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.ext.ipni.IpniService;
import eu.etaxonomy.cdm.io.eflora.EfloraImportState;
import eu.etaxonomy.cdm.io.eflora.EfloraTaxonImport;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;


/**
 * @author a.mueller
 *
 */
@Component
public class CentralAfricaEricaceaeTaxonImport  extends EfloraTaxonImport  {
	private static final Logger logger = Logger.getLogger(CentralAfricaEricaceaeTaxonImport.class);



	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.eflora.EfloraTaxonImport#handleNomenclaturalReference(eu.etaxonomy.cdm.model.name.NonViralName, java.lang.String)
	 */
	@Override
	protected TeamOrPersonBase handleNomenclaturalReference(NonViralName name, String value) {
		Reference nomRef = ReferenceFactory.newGeneric();
		nomRef.setTitleCache(value, true);
		parseNomStatus(nomRef, name);
		name.setNomenclaturalReference(nomRef);
		
		String microReference = parseReferenceYearAndDetail(nomRef);
		microReference = removeTrailing(microReference, ")");
		
		microReference = parseHomonym(microReference, name);
		name.setNomenclaturalMicroReference(microReference);
		
		TeamOrPersonBase  nameTeam = CdmBase.deproxy(name.getCombinationAuthorTeam(), TeamOrPersonBase.class);
		TeamOrPersonBase  refTeam = nomRef.getAuthorTeam();
		if (nameTeam == null ){
			logger.warn("Name has nom. ref. but no author team. Name: " + name.getTitleCache() + ", Nom.Ref.: " + value);
		}else if (refTeam == null ){
			logger.warn("Name has nom. ref. but no nom.ref. author. Name: " + name.getTitleCache() + ", Nom.Ref.: " + value);
		}else if (! authorTeamsMatch(refTeam, nameTeam)){
			logger.warn("Nom.Ref. author and comb. author do not match: " + nomRef.getTitleCache() + " <-> " + nameTeam.getNomenclaturalTitle());
		}else {
			nomRef.setAuthorTeam(nameTeam);
			nomRef.setTitle(CdmUtils.Nz(nomRef.getTitle()) + " - no title given yet -");
			nameTeam.setTitleCache(refTeam.getTitleCache(), true);
		}
		return nameTeam;
	}
	
	/**
	 * Extracts the date published part and returns micro reference
	 * @param ref
	 * @return
	 */
	protected String parseReferenceYearAndDetail(Reference ref){
		String detailResult = null;
		String titleToParse = ref.getTitleCache();
		titleToParse = removeReferenceBracket(titleToParse, ref);
		
		int detailStart = titleToParse.indexOf(":");
		if (detailStart >=  0){
			detailResult = titleToParse.substring(detailStart + 1);
			titleToParse = titleToParse.substring(0, titleToParse.length() -  detailResult.length() - 1).trim();
			detailResult = detailResult.trim();
		}
		
		String reYear = "\\s[1-2]{1}[0-9]{3}";
		String reYearPeriod = reYear;
//		
//		//pattern for the whole string
		Pattern patReference = Pattern.compile( reYearPeriod );
		Matcher matcher = patReference.matcher(titleToParse);
		if (matcher.find()){
			int start = matcher.start();
			int end = matcher.end();
//			
			String strPeriod = titleToParse.substring(start, end);
			TimePeriod datePublished = TimePeriod.parseString(strPeriod);
			ref.setDatePublished(datePublished);
			String author = titleToParse.substring(0, start).trim();
			author = parseInRefrence(ref, author);
			TeamOrPersonBase team = parseSingleTeam(author);
			ref.setAuthorTeam(team);
			ref.setProtectedTitleCache(false);
		}else{
			logger.warn("Could not parse reference: " +  titleToParse);
		}
		return detailResult;
		
	}

	private String parseInRefrence(Reference ref, String author) {
		int pos = author.indexOf(" in ");
		if (pos > -1){
			String inAuthorString = author.substring(pos + 4);
			String myAuthorString = author.substring(0, pos);
			Reference inReference = ReferenceFactory.newGeneric();
			TeamOrPersonBase inAuthor = parseSingleTeam(inAuthorString);
			inReference.setAuthorTeam(inAuthor);
			ref.setInReference(inReference);
			return myAuthorString;
		}else{
			return author;
		}
		
	}

	private String removeReferenceBracket(String refString, Reference ref) {
		String titleToParse = refString;
		String reBracket = "\\(.*\\).?";
		Pattern patBracket = Pattern.compile(reBracket);
		Matcher matcher = patBracket.matcher(titleToParse);
		
		if (matcher.matches()){
			int start = matcher.start() + 1;
			int end = matcher.end() -1 ;
			if (! titleToParse.endsWith("")){
				end = end - 1;
			}
			titleToParse = titleToParse.substring(start, end);
			
			ref.setTitleCache(titleToParse);
		}
		return titleToParse;
	}
	
	/**
	 * @param taxon
	 * @param name
	 * @param value
	 */
	@Override
	protected TeamOrPersonBase handleNameUsage(Taxon taxon, NonViralName name, String referenceTitle, TeamOrPersonBase lastTeam) {
		Reference ref = ReferenceFactory.newGeneric();
		
		ref.setTitleCache(referenceTitle, true);
		
		TeamOrPersonBase team = getReferenceAuthor(ref, name);
		ref.setAuthorTeam(team);
	
		String[] multipleReferences = ref.getTitleCache().split("&");
		
		TaxonDescription description = getDescription(taxon);
		for (String singleReferenceString : multipleReferences){
			Reference singleRef = ReferenceFactory.newGeneric();
			singleRef.setTitleCache(singleReferenceString.trim(), true);
			singleRef.setAuthorTeam(team);
			
			String microReference = parseReferenceYearAndDetailForUsage(singleRef);
			
			singleRef.setTitle( CdmUtils.Nz(singleRef.getTitle()) + " - no title given yet -");
			
	//		parseReferenceType(ref);
			
			TextData textData = TextData.NewInstance(Feature.CITATION());
			textData.addSource(null, null, singleRef, microReference, name, null);
			description.addElement(textData);
		}
		return team;
	}

	private String parseReferenceYearAndDetailForUsage(Reference ref) {
		String detailResult = null;
		String titleToParse = ref.getTitleCache().trim();
		
		int detailStart = titleToParse.indexOf(":");
		if (detailStart >=  0){
			detailResult = titleToParse.substring(detailStart + 1);
			titleToParse = titleToParse.substring(0, titleToParse.length() -  detailResult.length() - 1).trim();
			detailResult = detailResult.trim();
		}
		
		String reYear = "^[1-2]{1}[0-9]{3}[a-e]?$";
		String reYearPeriod = reYear;
//			
//			//pattern for the whole string
		Pattern patReference = Pattern.compile( reYearPeriod );
		Matcher matcher = patReference.matcher(titleToParse);
		if (! matcher.find()){
			logger.warn("Could not parse year: " +  titleToParse);
		}else{
			if (Pattern.matches("^[1-2]{1}[0-9]{3}[a-e]$", titleToParse)){
				String title = titleToParse.substring(4,5);
				ref.setTitle(title);
				titleToParse = titleToParse.substring(0, 4);
			}
			ref.setProtectedTitleCache(false);
		}
		TimePeriod datePublished = TimePeriod.parseString(titleToParse);
		ref.setDatePublished(datePublished);
		return detailResult;
		
	}

	protected TeamOrPersonBase getReferenceAuthor (Reference ref, NonViralName name) {
		String titleString = ref.getTitleCache();
		String re = "\\(.*\\)";
		Pattern pattern = Pattern.compile(re);
		Matcher matcher = pattern.matcher(titleString);
		if (matcher.find()){
			int start = matcher.start();
			String authorString = titleString.substring(0, start).trim();
			String restString = titleString.substring(start + 1 , matcher.end() - 1);
			TeamOrPersonBase team = getAuthorTeam(authorString, name);
			ref.setTitleCache(restString, true);
			return team;
		}else{
			logger.warn("Title does not match: " + titleString);
			return null;
		}
		
	}

	private TeamOrPersonBase getAuthorTeam(String authorString, NonViralName name) {
		//TODO atomize
//		TeamOrPersonBase nameTeam = CdmBase.deproxy(name.getCombinationAuthorTeam(), TeamOrPersonBase.class);
//		String nameTeamTitle = nameTeam == null ? "" : nameTeam.getNomenclaturalTitle();
		
//		if (nameTeam == null || ! authorTeamsMatch(authorString, nameTeamTitle)){
//			logger.warn("Author teams do not match: " + authorString + " <-> " + nameTeamTitle);
			TeamOrPersonBase result = parseSingleTeam(authorString);
			result.setTitleCache(authorString, true);
			return result;
//		}else{
//			nameTeam.setTitleCache(authorString, true);
//			return nameTeam;
//		}
	}

	/**
	 * @param refAuthorTeam
	 * @param nameTeam
	 * @return
	 */
	private boolean authorTeamsMatch(TeamOrPersonBase refAuthorTeam, TeamOrPersonBase nameTeam) {
		String nameTeamString = nameTeam.getNomenclaturalTitle();
		String refAuthorTeamString = refAuthorTeam.getTitleCache();
		if (nameTeamString.equalsIgnoreCase(refAuthorTeamString)){
			return true;
		}
		
		if (nameTeamString.endsWith(".")){
			nameTeamString = nameTeamString.substring(0, nameTeamString.length() - 1 );
			if (refAuthorTeamString.startsWith(nameTeamString)){
				return true;
			}else{
				return checkSingleAndIpniAuthor(nameTeam, refAuthorTeam);
			}
		}else{
			if (nameTeamString.endsWith(refAuthorTeamString) || refAuthorTeamString.endsWith(nameTeamString)){
				return true;
			}else{
				return checkSingleAndIpniAuthor(nameTeam, refAuthorTeam);
			}
		}
	}
	
	private boolean checkSingleAndIpniAuthor(TeamOrPersonBase nameTeam, TeamOrPersonBase refAuthorTeam) {
		if ( nameTeam.isInstanceOf(Team.class) && ((Team)nameTeam).getTeamMembers().size()> 1 ||
				refAuthorTeam.isInstanceOf(Team.class) && ((Team)refAuthorTeam).getTeamMembers().size()> 1){
			//class
			if (! (nameTeam.isInstanceOf(Team.class) && refAuthorTeam.isInstanceOf(Team.class) ) ){
				logger.warn("Only one author is a real team");
				return false;
			}
			Team realNameTeam = (Team)nameTeam;
			Team realRefAuthorTeam = (Team)refAuthorTeam;
			//size
			if (realNameTeam.getTeamMembers().size() != realRefAuthorTeam.getTeamMembers().size()){
				logger.warn("Teams do not have the same size");
				return false;
			}
			//empty teams
			if (realNameTeam.getTeamMembers().size() == 0){
				logger.warn("Teams are empty");
				return false;
			}
			//compare each team member
			for (int i = 0; i < realNameTeam.getTeamMembers().size(); i++){
				Person namePerson = realNameTeam.getTeamMembers().get(i);
				Person refPerson = realRefAuthorTeam.getTeamMembers().get(i);
				if ( authorTeamsMatch(refPerson, namePerson) == false){
					return false;
				}
			}
			return true;
		}
		boolean result = checkIpniAuthor(nameTeam.getNomenclaturalTitle(), refAuthorTeam);
		return result;
	}

	private boolean checkIpniAuthor(String nameTeamString, TeamOrPersonBase refAuthorTeam) {
		IpniService ipniService = new IpniService();
		List<Person> ipniAuthors = ipniService.getAuthors(nameTeamString, null, null, null, null, null);
		if (ipniAuthors != null){
			for (Person ipniAuthor : ipniAuthors){
				if (ipniAuthor.getLastname() != null && ipniAuthor.getLastname().equalsIgnoreCase(refAuthorTeam.getTitleCache())){
					return true;
				}
				logger.warn(ipniAuthor.getTitleCache() + " <-> " + refAuthorTeam.getTitleCache());
			}
		}else{
			logger.warn("IPNI not available");
		}
		return false;
	}

	/**
	 * @param state
	 * @param elNom
	 * @param taxon
	 * @param homotypicalGroup 
	 */
	@Override
	protected void handleTypeRef(EfloraImportState state, Element elNom, Taxon taxon, HomotypicalGroup homotypicalGroup) {
		verifyNoChildren(elNom);
		String typeRef = elNom.getTextNormalize();
		typeRef = removeStartingTypeRefMinus(typeRef);
		typeRef = removeTypePrefix(typeRef);
		TypeDesignationBase typeDesignation = SpecimenTypeDesignation.NewInstance();
		makeSpecimenTypeDesignation(new StringBuffer("Type"), typeRef, typeDesignation);
		for (TaxonNameBase name : homotypicalGroup.getTypifiedNames()){
			name.addTypeDesignation(typeDesignation, true);
		}
	}

	private String removeTypePrefix(String typeRef) {
		typeRef = typeRef.trim().replace("Type: ", "").replace("Types: ", "").trim();
		return typeRef;
	}
	
	protected void handleGenus(String value, TaxonNameBase taxonName) {
		// do nothing
	}

	
	
}
