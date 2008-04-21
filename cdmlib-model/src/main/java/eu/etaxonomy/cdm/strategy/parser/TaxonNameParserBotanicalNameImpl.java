/**
 * 
 */
package eu.etaxonomy.cdm.strategy.parser;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.CultivarPlantName;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.BookSection;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.strategy.exceptions.StringNotParsableException;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;


/**
 * @author a.mueller
 *
 */
public class TaxonNameParserBotanicalNameImpl implements ITaxonNameParser<BotanicalName> {
	private static final Logger logger = Logger.getLogger(TaxonNameParserBotanicalNameImpl.class);
	
	// good intro: http://java.sun.com/docs/books/tutorial/essential/regex/index.html
	
	public static TaxonNameParserBotanicalNameImpl NewInstance(){
		return new TaxonNameParserBotanicalNameImpl();
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.ITaxonNameParser#parseSimpleName(java.lang.String, eu.etaxonomy.cdm.model.name.Rank)
	 */
	public BotanicalName parseSimpleName(String simpleName, Rank rank){
		//TODO
		logger.warn("parseSimpleName() not yet implemented. Uses parseFullName() instead");
		return parseFullName(simpleName, rank);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.ITaxonNameParser#parseSubGenericSimpleName(java.lang.String)
	 */
	public BotanicalName parseSimpleName(String simpleName){
		return parseSimpleName(simpleName, null);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.ITaxonNameParser#parseFullReference(java.lang.String, eu.etaxonomy.cdm.model.name.Rank)
	 */
	public BotanicalName parseFullReference(String fullReferenceString, Rank rank) {
		if (fullReferenceString == null){
			return null;
		}else{
			BotanicalName result = BotanicalName.NewInstance(null);
			parseFullReference(result, fullReferenceString, rank, false);
			return result;
		}
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.ITaxonNameParser#parseFullReference(eu.etaxonomy.cdm.model.name.BotanicalName, java.lang.String, eu.etaxonomy.cdm.model.name.Rank, boolean)
	 */
	public void parseFullReference(BotanicalName nameToBeFilled, String fullReferenceString, Rank rank, boolean makeEmpty) {
		if (fullReferenceString == null){
			//return null;
			return;
		}
		if (makeEmpty){
			makeEmpty(nameToBeFilled);
		}
		fullReferenceString.replaceAll(oWs , " ");
		fullReferenceString = fullReferenceString.trim();
		
		//seperate name and reference part
		String nameAndRefSeperator = "(^" + anyFullName + ")("+ referenceSeperator + ")";
		Pattern nameAndRefSeperatorPattern = Pattern.compile(nameAndRefSeperator);
		Matcher nameAndRefSeperatorMatcher = nameAndRefSeperatorPattern.matcher(fullReferenceString);
				
		if (nameAndRefSeperatorMatcher.find() ){
			String nameAndSeperator = nameAndRefSeperatorMatcher.group(0); 
		    String name = nameAndRefSeperatorMatcher.group(1); 
		    String reference = fullReferenceString.substring(nameAndRefSeperatorMatcher.end());
		    
		    // inRef?
		    String seperator = nameAndSeperator.substring(name.length());
			boolean isInReference = false;
		    if (seperator.matches(inReferenceSeperator)){
		    	isInReference = true;
		    }
		   	
		    //status
		    reference = parseNomStatus(reference, nameToBeFilled);
		    
		    //parse subparts
		    parseFullName(nameToBeFilled, name, rank, makeEmpty);
		    parseReference(nameToBeFilled, reference, isInReference); 
		
		}else{
			//don't parse if name can't be seperated
			nameToBeFilled.setHasProblem(true);
			nameToBeFilled.setTitleCache(fullReferenceString);
			logger.info("no applicable parsing rule could be found for \"" + fullReferenceString + "\"");    
		}
	}
	
	//TODO make it an Array of status
	/**
	 * Extracts a {@link NomenclaturalStatus} from the reference String and adds it to the @link {@link TaxonNameBase}.
	 * The nomenclatural status part ist deleted from the reference String.
	 * @return  String the new (shortend) reference String 
	 */ 
	String parseNomStatus(String reference, BotanicalName nameToBeFilled) {
		String statusString;
		Pattern hasStatusPattern = Pattern.compile("(" + pNomStatusPhrase + ")"); 
		Matcher hasStatusMatcher = hasStatusPattern.matcher(reference);
		
		if (hasStatusMatcher.find()) {
			String statusPhrase = hasStatusMatcher.group(0);
			
			Pattern statusPattern = Pattern.compile(pNomStatus);
			Matcher statusMatcher = statusPattern.matcher(statusPhrase);
			statusMatcher.find();
			statusString = statusMatcher.group(0);
			try {
				NomenclaturalStatusType nomStatusType = NomenclaturalStatusType.getNomenclaturalStatusTypeByAbbreviation(statusString);
				NomenclaturalStatus nomStatus = NomenclaturalStatus.NewInstance(nomStatusType);
				nameToBeFilled.addStatus(nomStatus);
			    
			    reference = reference.replace(statusPhrase, "");
			} catch (UnknownCdmTypeException e) {
				//Do nothing
			}
		}
		return reference;
	}
	
	
	private void parseReference(BotanicalName nameToBeFilled, String reference, boolean isInReference){
			
		if (referencePattern.matcher(reference).matches() ){
			//End (just delete, may be ambigous for yearPhrase, but no real information gets lost
			Pattern endPattern = Pattern.compile( referenceEnd + end);
			Matcher endMatcher = endPattern.matcher(reference);
			if (endMatcher.find()){
				String endPart = endMatcher.group(0);
				reference = reference.substring(0, reference.length() - endPart.length());
			}
			
			//year
			String yearPart = null;
			String pYearPhrase = yearSeperator + yearPhrase + end;
			Pattern yearPhrasePattern = Pattern.compile(pYearPhrase);
			Matcher yearPhraseMatcher = yearPhrasePattern.matcher(reference);
			if (yearPhraseMatcher.find()){
				yearPart = yearPhraseMatcher.group(0);
				reference = reference.substring(0, reference.length() - yearPart.length());
				yearPart = yearPart.replaceFirst(start + yearSeperator, "").trim();
			}
			
			//detail
			String pDetailPhrase = detailSeperator + detail + end;
			Pattern detailPhrasePattern = Pattern.compile(pDetailPhrase);
			Matcher detailPhraseMatcher = detailPhrasePattern.matcher(reference);
			if (detailPhraseMatcher.find()){
				String detailPart = detailPhraseMatcher.group(0);
				reference = reference.substring(0, reference.length() - detailPart.length());
				detailPart = detailPart.replaceFirst(start + detailSeperator, "").trim();
				nameToBeFilled.setNomenclaturalMicroReference(detailPart);
			}
			//Title (and author)
			parseReferenceTitle(reference, yearPart);
	    }
	    
	}
		
	/**
	 * Parses the referenceTitlePart, including the author volume and edition.
	 * @param reference
	 * @param year
	 * @return
	 */
	private ReferenceBase parseReferenceTitle(String reference, String year){
		ReferenceBase result = null;
		Pattern bookPattern = Pattern.compile(bookReference);
		Pattern articlePattern = Pattern.compile(articleReference);
		Pattern bookSectionPattern = Pattern.compile(bookSectionReference);
		
		
		Matcher articleMatcher = articlePattern.matcher(reference);
		Matcher bookMatcher = bookPattern.matcher(reference);
		Matcher bookSectionMatcher = bookSectionPattern.matcher(reference);
		
		
		if (articleMatcher.matches()){
			//if (articlePatter)
			//(type, author, title, volume, editor, series;
			Article article = new Article();
			article.setTitleCache(reference);
			result = article;
		}else if(bookMatcher.matches()){
			Book book = new Book();
			book .setTitleCache(reference);
			result = book;
		}else if (bookSectionMatcher.matches()){
			BookSection bookSection = new BookSection();
			bookSection.setTitleCache(reference);
			result = bookSection;
		}else{
			logger.warn("unknown reference type not yet implemented");
			//ReferenceBase refBase = 
		}
		return result;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.ITaxonNameParser#parseSubGenericFullName(java.lang.String)
	 */
	public BotanicalName parseFullName(String fullNameString){
		return parseFullName(fullNameString, null);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.ITaxonNameParser#parseFullName(java.lang.String, eu.etaxonomy.cdm.model.name.Rank)
	 */
	public BotanicalName parseFullName(String fullNameString, Rank rank) {
		if (fullNameString == null){
			return null;
		}else{
			BotanicalName result = BotanicalName.NewInstance(null);
			parseFullName(result, fullNameString, rank, false);
			return result;
		}
	}
		
	
	public void parseFullName(BotanicalName nameToBeFilled, String fullNameString, Rank rank, boolean makeEmpty) {
		//TODO prol. etc.
		
		String authorString = null;
		
		if (fullNameString == null){
			return;
		}
		if (makeEmpty){
			makeEmpty(nameToBeFilled);
		}
		fullNameString.replaceAll(oWs , " ");
		//TODO 
		// OLD: fullName = oWsRE.subst(fullName, " "); //substitute multiple whitespaces		   
		fullNameString = fullNameString.trim();
		
		String[] epi = pattern.split(fullNameString);
		try {
	    	//cultivars //TODO 2 implement cultivars
//		    if ( cultivarMarkerRE.match(fullName) ){ funktioniert noch nicht, da es z.B. auch Namen gibt, wie 't Hart
//		    	result = parseCultivar(fullName);
//		    }
		    //hybrids //TODO 2 implement hybrids
		    //else 
		    if (hybridPattern.matcher(fullNameString).matches() ){
		    	nameToBeFilled = parseHybrid(fullNameString);
		    }
		    else if (genusOrSupraGenusPattern.matcher(fullNameString).matches()){
		    	//supraGeneric
				if (rank.isSupraGeneric()){
					nameToBeFilled.setRank(rank);
					nameToBeFilled.setGenusOrUninomial(epi[0]);
				} 
				//genus
				else {
					nameToBeFilled.setRank(Rank.GENUS());
					nameToBeFilled.setGenusOrUninomial(epi[0]);
				}
				authorString = fullNameString.substring(epi[0].length());
			}
			//infra genus
			else if (infraGenusPattern.matcher(fullNameString).matches()){
				nameToBeFilled.setRank(Rank.getRankByAbbreviation(epi[1]));
				nameToBeFilled.setGenusOrUninomial(epi[0]);
				nameToBeFilled.setInfraGenericEpithet(epi[2]);
				authorString = fullNameString.substring(epi[0].length() + 1 + epi[1].length()+ 1 + epi[2].length());
			}
			//aggr. or group
			else if (aggrOrGroupPattern.matcher(fullNameString).matches()){
				nameToBeFilled.setRank(Rank.getRankByAbbreviation(epi[2]));
				nameToBeFilled.setGenusOrUninomial(epi[0]);
				nameToBeFilled.setSpecificEpithet(epi[1]);
			}
			//species
			else if (speciesPattern.matcher(fullNameString).matches()){
				nameToBeFilled.setRank(Rank.SPECIES());
				nameToBeFilled.setGenusOrUninomial(epi[0]);
				nameToBeFilled.setSpecificEpithet(epi[1]);
				authorString = fullNameString.substring(epi[0].length() + 1 + epi[1].length());
			}
			//autonym
			else if (autonymPattern.matcher(fullNameString).matches()){
				nameToBeFilled.setRank(Rank.getRankByAbbreviation(epi[epi.length - 2]));
				nameToBeFilled.setGenusOrUninomial(epi[0]);
				nameToBeFilled.setSpecificEpithet(epi[1]);
				nameToBeFilled.setInfraSpecificEpithet(epi[epi.length - 1]);
				int lenSpecies = 2 + epi[0].length()+epi[1].length();
				int lenInfraSpecies =  2 + epi[epi.length - 2].length() + epi[epi.length - 1].length();
				authorString = fullNameString.substring(lenSpecies, fullNameString.length() - lenInfraSpecies);
			}
			//infraSpecies
			else if (infraSpeciesPattern.matcher(fullNameString).matches()){
				String infraSpecRankEpi = epi[2];
				String infraSpecEpi = epi[3];
				if ("tax.".equals(infraSpecRankEpi)){
					infraSpecRankEpi += " " +  epi[3];
					infraSpecEpi = epi[4];
				}
				nameToBeFilled.setRank(Rank.getRankByAbbreviation(infraSpecRankEpi));
				nameToBeFilled.setGenusOrUninomial(epi[0]);
				nameToBeFilled.setSpecificEpithet(epi[1]);
				nameToBeFilled.setInfraSpecificEpithet(infraSpecEpi);
				authorString = fullNameString.substring(epi[0].length()+ 1 + epi[1].length() +1 + infraSpecRankEpi.length() + 1 + infraSpecEpi.length());
			}//old infraSpecies
			else if (oldInfraSpeciesPattern.matcher(fullNameString).matches()){
				boolean implemented = false;
				if (implemented){
					nameToBeFilled.setRank(Rank.getRankByNameOrAbbreviation(epi[2]));
					nameToBeFilled.setGenusOrUninomial(epi[0]);
					nameToBeFilled.setSpecificEpithet(epi[1]);
					//TODO result.setUnnamedNamePhrase(epi[2] + " " + epi[3]);
					authorString = fullNameString.substring(epi[0].length()+ 1 + epi[1].length() +1 + epi[2].length() + 1 + epi[3].length());
				}else{
					nameToBeFilled.setHasProblem(true);
					nameToBeFilled.setTitleCache(fullNameString);
					logger.info("Name string " + fullNameString + " could not be parsed because UnnnamedNamePhrase is not yet implemented!");
				}
			}
			//none
			else{ 
				nameToBeFilled.setHasProblem(true);
				nameToBeFilled.setTitleCache(fullNameString);
				logger.info("no applicable parsing rule could be found for \"" + fullNameString + "\"");
		    }
			//authors
		    if (nameToBeFilled != null && authorString != null && authorString.trim().length() > 0 ){ 
				TeamOrPersonBase[] authors = null;
				try {
					authors = fullAuthors(authorString);
				} catch (StringNotParsableException e) {
					nameToBeFilled.setHasProblem(true);
					nameToBeFilled.setTitleCache(fullNameString);
					logger.info("no applicable parsing rule could be found for \"" + fullNameString + "\"");;
				}
				nameToBeFilled.setCombinationAuthorTeam(authors[0]);
				nameToBeFilled.setExCombinationAuthorTeam(authors[1]);
				nameToBeFilled.setBasionymAuthorTeam(authors[2]);
				nameToBeFilled.setExBasionymAuthorTeam(authors[3]);
			}	
			//return
			if (nameToBeFilled != null){
		    	//return(BotanicalName)result;
				return;
			}else{
				nameToBeFilled.setHasProblem(true);
				nameToBeFilled.setTitleCache(fullNameString);
				logger.info("Name string " + fullNameString + " could not be parsed!");
				//return result;
				return;
			}
		} catch (UnknownCdmTypeException e) {
			nameToBeFilled.setHasProblem(true);
			nameToBeFilled.setTitleCache(fullNameString);
			logger.info("unknown rank (" + (rank == null? "null":rank) + ") or abbreviation in string " +  fullNameString);
			//return result;
			return;
		}
	}
	
	private void makeEmpty(BotanicalName nameToBeFilled){
		nameToBeFilled.setRank(null);
		nameToBeFilled.setTitleCache(null, false);
		nameToBeFilled.setNameCache(null);
				
		nameToBeFilled.setAnamorphic(false);
		nameToBeFilled.setAppendedPhrase(null);
		//TODO ??
		//nameToBeFilled.setBasionym(basionym);
		nameToBeFilled.setBasionymAuthorTeam(null);
		nameToBeFilled.setCombinationAuthorTeam(null);
		nameToBeFilled.setExBasionymAuthorTeam(null);
		nameToBeFilled.setExCombinationAuthorTeam(null);
		nameToBeFilled.setAuthorshipCache(null);
		
		
		nameToBeFilled.setHasProblem(false);
		// TODO ?
		//nameToBeFilled.setHomotypicalGroup(newHomotypicalGroup);
		
		nameToBeFilled.setHybridFormula(false);
		nameToBeFilled.setMonomHybrid(false);
		nameToBeFilled.setBinomHybrid(false);
		nameToBeFilled.setTrinomHybrid(false);
		
		nameToBeFilled.setGenusOrUninomial(null);
		nameToBeFilled.setInfraGenericEpithet(null);
		nameToBeFilled.setSpecificEpithet(null);
		nameToBeFilled.setInfraSpecificEpithet(null);
		
		nameToBeFilled.setNomenclaturalMicroReference(null);
		nameToBeFilled.setNomenclaturalReference(null);
		
		nameToBeFilled.setUpdated(Calendar.getInstance());
		// TODO nameToBeFilled.setUpdatedBy(updatedBy);
			
	}
	
	
	/**
	 * Parses the fullAuthorString
	 * @param fullAuthorString
	 * @return array of Teams containing the Team[0], 
	 * ExTeam[1], BasionymTeam[2], ExBasionymTeam[3]
	 */
	public TeamOrPersonBase[] fullAuthors (String fullAuthorString)
			throws StringNotParsableException{
		fullAuthorString = fullAuthorString.trim();
		if (! fullAuthorStringPattern.matcher(fullAuthorString).matches())
			throw new StringNotParsableException("fullAuthorString (" +fullAuthorString+") not parsable: ");
		return fullAuthorsChecked(fullAuthorString);
	}
	
	
	/*
	 * like fullTeams but without trim and match check
	 */
	private TeamOrPersonBase[] fullAuthorsChecked (String fullAuthorString){
		TeamOrPersonBase[] result = new TeamOrPersonBase[4]; 
		int authorTeamStart = 0;
		Matcher basionymMatcher = basionymPattern.matcher(fullAuthorString);
		if (basionymMatcher.find(0)){
			
			String basString = basionymMatcher.group();
			basString = basString.replaceFirst(basStart, "");
			basString = basString.replaceAll(basEnd, "").trim();
			authorTeamStart = basionymMatcher.end(1) + 1;
			
			TeamOrPersonBase[] basAuthors = authorsAndEx(basString);
			result[2]= basAuthors[0];
			result[3]= basAuthors[1];
		}
		TeamOrPersonBase[] authors = authorsAndEx(fullAuthorString.substring(authorTeamStart));
		result[0]= authors[0];
		result[1]= authors[1];
		return result;
	}
	
	
	/**
	 * Parses the author and ex-author String
	 * @param authorTeamString String representing the author and the ex-author team
	 * @return array of Teams containing the Team[0] and the ExTeam[1]
	 */
	public TeamOrPersonBase[] authorsAndEx (String authorTeamString){
		TeamOrPersonBase[] result = new TeamOrPersonBase[2]; 
		//TODO noch allgemeiner am anfang durch Replace etc. 
		authorTeamString = authorTeamString.trim();
		authorTeamString = authorTeamString.replaceFirst(oWs + "ex" + oWs, " ex. " ); 
		int authorEnd = authorTeamString.length();
		
		Matcher exAuthorMatcher = exAuthorPattern.matcher(authorTeamString);
		if (exAuthorMatcher.find(0)){
			int exAuthorBegin = exAuthorMatcher.end(0);
			String exString = authorTeamString.substring(exAuthorBegin).trim();
			authorEnd = exAuthorMatcher.start(0);
			result [1] = author(exString);
		}
		result [0] = author(authorTeamString.substring(0, authorEnd));
		return result;
	}
	
	
	/**
	 * Parses an authorTeam String and returns the Team 
	 * !!! TODO (atomization not yet implemented)
	 * @param authorTeamString String representing the author team
	 * @return an Team 
	 */
	public TeamOrPersonBase author (String authorString){
		if (authorString == null){ 
			return null;
		}else if ((authorString = authorString.trim()).length() == 0){
			return null;
		}else if (! teamSplitterPattern.matcher(authorString).find()){
			//1 Person
			Person result = Person.NewInstance();
			result.setNomenclaturalTitle(authorString);
			return result;
		}else{
			return parsedTeam(authorString);
		} 
		
	}
	
	private Team parsedTeam(String authorString){
		Team result = Team.NewInstance();
		String[] authors = authorString.split(teamSplitter);
		for (String author : authors){
			Person person = Person.NewInstance();
			person.setNomenclaturalTitle(author);
			result.addTeamMember(person); 
		}
		return result;
	}
	

	//Parsing of the given full name that has been identified as hybrid already somewhere else.
	private BotanicalName parseHybrid(String fullName){
	    logger.warn("parseHybrid --> function not yet implemented");
	    BotanicalName result = BotanicalName.NewInstance(null);
	    result.setTitleCache(fullName);
	    return result;
    }
	
//	// Parsing of the given full name that has been identified as a cultivar already somwhere else.
//	// The ... cv. ... syntax is not covered here as it is not according the rules for naming cultivars.
	public BotanicalName parseCultivar(String fullName)	throws StringNotParsableException{
		CultivarPlantName result = null;
		    String[] words = oWsPattern.split(fullName);
			
		    /* ---------------------------------------------------------------------------------
		     * cultivar
		     * ---------------------------------------------------------------------------------*/
			if (fullName.indexOf(" '") != 0){
				//TODO location of 'xx' is probably not arbitrary
				Matcher cultivarMatcher = cultivarPattern.matcher(fullName);
				if (cultivarMatcher.find()){
					String namePart = fullName.replaceFirst(cultivar, "");
					
					String cultivarPart = cultivarMatcher.group(0).replace("'","").trim();
					//OLD: String cultivarPart = cultivarRE.getParen(0).replace("'","").trim();
					
					result = (CultivarPlantName)parseFullName(namePart);
					result.setCultivarName(cultivarPart);
				}	
			}else if (fullName.indexOf(" cv.") != 0){
				// cv. is old form (not official) 
				throw new StringNotParsableException("Cultivars with only cv. not yet implemented in name parser!");
			}
				
		    /* ---------------------------------------------------------------------------------
		     * cultivar group
		     * ---------------------------------------------------------------------------------
		     */ 
			// TODO in work 
			//Ann. this is not the official way of noting cultivar groups
		    String group = oWs + "Group" + oWs + capitalEpiWord + end;
			Pattern groupRE = Pattern.compile(group);
			Matcher groupMatcher = groupRE.matcher(fullName);
			if (groupMatcher.find()){
		    	if (! words[words.length - 2].equals("group")){
		            throw new StringNotParsableException ("fct ParseHybrid --> term before cultivar group name in " + fullName + " should be 'group'");
		        }else{
		        	
		        	String namePart = fullName.substring(0, groupMatcher.start(0) - 0);
		        	//OLD: String namePart = fullName.substring(0, groupRE.getParenStart(0) - 0);
		        	
		        	String cultivarPart = words[words.length -1];
		        	result = (CultivarPlantName)parseFullName(namePart);
		        	if (result != null){
		        		result.setCultivarName(cultivarPart);
			        	
		        		//OLD: result.setCultivarGroupName(cultivarPart);
		        	}
		        }

		    }
//		    // ---------------------------------------------------------------------------------
//		    if ( result = "" ){
//		        return "I: fct ParseCultivar: --> could not parse cultivar " + fullName;
//		    }else{
//		        return result;
	//	    }
			return result; //TODO
	}

	
    
    //splitter
    static String epiSplitter = "(\\s+|\\(|\\))"; //( ' '+| '(' | ')' )
    static Pattern pattern = Pattern.compile(epiSplitter); 
    
    //some useful non-terminals
    static String start = "^";
    static String end = "$";
    static String anyEnd = ".*" + end;
    static String oWs = "\\s+"; //obligatory whitespaces
    static String fWs = "\\s*"; //facultative whitespcace
    
    static String capitalWord = "\\p{javaUpperCase}\\p{javaLowerCase}*";
    static String nonCapitalWord = "\\p{javaLowerCase}+";
    
    static String capitalDotWord = capitalWord + "\\.?"; //capitalWord with facultativ '.' at the end
    static String nonCapitalDotWord = nonCapitalWord + "\\.?"; //nonCapitalWord with facultativ '.' at the end
    static String dotWord = "(" + capitalWord + "|" + nonCapitalWord + ")\\.?"; //word (capital or non-capital) with facultativ '.' at the end
    //Words used in an epethiton for a TaxonName
    static String nonCapitalEpiWord = "[a-zï\\-]+";   //TODO solve checkin Problem with Unicode character "[a-zï¿½\\-]+";
    static String capitalEpiWord = "[A-Z]"+ nonCapitalEpiWord;
     
    
   //years
    static String month = "(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)";
    static String singleYear = "\\b" + "(?:17|18|19|20)" + "\\d{2}" + "\\b";                      // word boundary followed by either 17,18,19, or 20 (not captured) followed by 2 digits 	      
    static String yearPhrase = "(" + singleYear + "(-" + singleYear + ")?" + 
    						"(" + month + ")?)" ;                 // optional month
    
    //seperator
    static String yearSeperator = "." + oWs;
    static String detailSeperator = ":" + oWs;
    static String referenceSeperator1 = "," + oWs ;
    static String inReferenceSeperator = oWs + "in" + oWs;
    static String referenceSeperator = "(" + referenceSeperator1 +"|" + inReferenceSeperator + ")" ;
    static String referenceAuthorSeperator = ","+ oWs;
    static String volumeSeperator = "," + fWs ;
    static String referenceEnd = ".";
     
    
    //status
    static String status = "";
    
    //marker
    static String InfraGenusMarker = "(subgen.|subg.|sect.|subsect.|ser.|subser.|t.infgen.)";
    static String aggrOrGroupMarker = "(aggr.|agg.|group)";
    static String infraSpeciesMarker = "(subsp.|convar.|var.|subvar.|f.|subf.|f.spec.|tax." + fWs + "infrasp.)";
    static String oldInfraSpeciesMarker = "(prol.|proles|race|taxon|sublusus)";
    
    
    //AuthorString
    static String authorPart = "(" + "(D'|L'|'t\\s)?" + capitalDotWord + "('" + nonCapitalDotWord + ")?" + "|da|de(n|l|\\sla)?)" ;
    static String author = "(" + authorPart + "(" + fWs + "|-)" + ")+" + "(f.|fil.|secundus)?";
    static String teamSplitter = fWs + "(&)" + fWs;
    static String authorTeam = fWs + "(" + author + teamSplitter + ")*" + author + "(" + teamSplitter + "al.)?" + fWs;
    static String exString = "(ex.?)";
    static String authorAndExTeam = authorTeam + "(" + oWs + exString + oWs + authorTeam + ")?";
    static String basStart = "\\(";
    static String basEnd = "\\)";
    static String basionymAuthor = basStart + "(" + authorAndExTeam + ")" + basEnd;  // '(' and ')' is for evaluation with RE.paren(x)
    static String fullAuthorString = fWs + "(" + basionymAuthor +")?" + fWs + authorAndExTeam + fWs;
    static String facultFullAuthorString = "(" +  fullAuthorString + ")?" ; 

    
    //details
    //TODO still very simple
    static String pageNumber = "\\d{1,5}";
    static String detail = "(" + pageNumber + ")";
    
    //reference
    static String volume = "\\d{4}" + "\\(\\d{4}\\)?"; 	      
    
    static String referenceTitle = "(" + dotWord + fWs + ")" + "{2,}";
    static String bookReference = referenceTitle + volumeSeperator +  volume;
    static String bookSectionReference = authorTeam + referenceAuthorSeperator;
    static String articleReference = inReferenceSeperator + bookReference  ; 
    static String reference = "(" + articleReference + "|" + bookReference +")" + 
    				detailSeperator + detail + yearSeperator + yearPhrase +
    				referenceEnd; 

    static Pattern referencePattern = Pattern.compile(reference);
    
    static String pNomStatusNom = "nom\\." + fWs + "(superfl\\.|nud\\.|illeg\\.|inval\\.|cons\\.|alternativ\\.|subnud.|"+
    					"rej\\.|rej\\."+ fWs + "prop\\.|provis\\.)";
    static String pNomStatusOrthVar = "orth\\." + fWs + "var\\.";
    static String pNomStatus = "(" + pNomStatusNom + "|" + pNomStatusOrthVar +  ")";
    static String pNomStatusPhrase1 = "," + fWs + pNomStatus;
    static String pNomStatusPhrase2 = "\\[" + fWs + pNomStatus + "\\]";
    
    static String pNomStatusPhrase = "(?:" + pNomStatusPhrase1 + "|" + pNomStatusPhrase2 + ")";

// Soraya
//opus utique oppr.
//pro syn.
//provisional synonym
//fossil name

    
    
    //cultivars and hybrids
    static String cultivar = oWs + "'..+'"; //Achtung mit Hochkomma in AuthorNamen
    static String cultivarMarker = oWs + "(cv.|')";
    static String hybrid = oWs + "((x|X)" + oWs + "|notho)";//= ( x )|( X )|( notho)
    
    //  Name String
    static String genusOrSupraGenus = capitalEpiWord;
    static String infraGenus = capitalEpiWord + oWs + InfraGenusMarker + oWs + capitalEpiWord;
    static String aggrOrGroup = capitalEpiWord + oWs + nonCapitalEpiWord + oWs + aggrOrGroupMarker;
    static String species = capitalEpiWord + oWs +  nonCapitalEpiWord;
    static String infraSpecies = capitalEpiWord + oWs +  nonCapitalEpiWord + oWs + infraSpeciesMarker + oWs + nonCapitalEpiWord;
    static String oldInfraSpecies = capitalEpiWord + oWs +  nonCapitalEpiWord + oWs + oldInfraSpeciesMarker + oWs + nonCapitalEpiWord;
    static String autonym = capitalEpiWord + oWs + "(" + nonCapitalEpiWord +")" + oWs + fullAuthorString +  oWs + infraSpeciesMarker + oWs + "\\1";  //2-nd word and last word are the same 
    static String anyName = "(" + genusOrSupraGenus + "|" + infraGenus + "|" + aggrOrGroup + "|" + species + "|" + 
    					infraSpecies + "|" + infraSpecies + "|" + oldInfraSpecies + "|" + autonym   + ")+";
    static String anyFullName = anyName + oWs + fullAuthorString;
    
    //Pattern
    static Pattern oWsPattern = Pattern.compile(oWs);
    static Pattern teamSplitterPattern = Pattern.compile(teamSplitter);
    static Pattern cultivarPattern = Pattern.compile(cultivar);
    static Pattern cultivarMarkerPattern = Pattern.compile(cultivarMarker);
    static Pattern hybridPattern = Pattern.compile(hybrid); 
    
    static Pattern genusOrSupraGenusPattern = Pattern.compile(start + genusOrSupraGenus + facultFullAuthorString + end);
    static Pattern infraGenusPattern = Pattern.compile(start + infraGenus + facultFullAuthorString + end);
    static Pattern aggrOrGroupPattern = Pattern.compile(start + aggrOrGroup + fWs + end); //aggr. or group has no author string
    static Pattern speciesPattern = Pattern.compile(start + species + facultFullAuthorString + end);
    static Pattern infraSpeciesPattern = Pattern.compile(start + infraSpecies + facultFullAuthorString + end);
    static Pattern oldInfraSpeciesPattern = Pattern.compile(start + oldInfraSpecies + facultFullAuthorString + end);
    static Pattern autonymPattern = Pattern.compile(start + autonym + fWs + end);
	
    static Pattern basionymPattern = Pattern.compile(basionymAuthor);
    //static Pattern startsWithBasionymRE = Pattern.compile(basionymAuthor + anyEnd);
        static Pattern exAuthorPattern = Pattern.compile(oWs + exString);
    
    static Pattern fullAuthorStringPattern = Pattern.compile(fullAuthorString);


}
