/**
 * 
 */
package eu.etaxonomy.cdm.strategy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.CultivarPlantName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.strategy.exceptions.StringNotParsableException;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownRankException;

/**
 * @author a.mueller
 *
 */
public class TaxonNameParserBotanicalNameImpl implements ITaxonNameParser<BotanicalName> {
	private static final Logger logger = Logger.getLogger(TaxonNameParserBotanicalNameImpl.class);
	
	
	public static ITaxonNameParser<BotanicalName> NEW_INSTANCE(){
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
	 * @see eu.etaxonomy.cdm.strategy.ITaxonNameParser#parseSubGenericFullName(java.lang.String)
	 */
	public BotanicalName parseFullName(String fullName){
		return parseFullName(fullName, null);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.ITaxonNameParser#parseFullName(java.lang.String, eu.etaxonomy.cdm.model.name.Rank)
	 */
	public BotanicalName parseFullName(String fullName, Rank rank) {
		
		//TODO prol. etc.
		
		BotanicalName result = new BotanicalName(null);
		String authorString = null;
		
		if (fullName == null){
			return null;
		}
		fullName.replaceAll(oWs , " ");
		//TODO 
		// OLD: fullName = oWsRE.subst(fullName, " "); //substitute multiple whitespaces		   
		fullName = fullName.trim();
		
		String[] epi = pattern.split(fullName);
		try {
	    	//cultivars //TODO 2 implement cultivars
//		    if ( cultivarMarkerRE.match(fullName) ){ funktioniert noch nicht, da es z.B. auch Namen gibt, wie 't Hart
//		    	result = parseCultivar(fullName);
//		    }
		    //hybrids //TODO 2 implement hybrids
		    //else 
		    if (hybridRE.matcher(fullName).matches() ){
		    	result = parseHybrid(fullName);
		    }
		    else if (genusOrSupraGenusRE.matcher(fullName).matches()){
		    	//supraGeneric
				if (rank.isSupraGeneric()){
					result = new BotanicalName(rank);
					result.setGenusOrUninomial(epi[0]);
				} 
				//genus
				else {
					result = new BotanicalName(Rank.GENUS());
					result.setGenusOrUninomial(epi[0]);
				}
				authorString = fullName.substring(epi[0].length());
			}
			//infra genus
			else if (infraGenusRE.matcher(fullName).matches()){
				result = new BotanicalName(Rank.getRankByAbbreviation(epi[1]));
				result.setGenusOrUninomial(epi[0]);
				result.setInfraGenericEpithet(epi[2]);
				authorString = fullName.substring(epi[0].length() + 1 + epi[1].length()+ 1 + epi[2].length());
			}
			//aggr. or group
			else if (aggrOrGroupRE.matcher(fullName).matches()){
				result = new BotanicalName(Rank.getRankByAbbreviation(epi[2]));
				result.setGenusOrUninomial(epi[0]);
				result.setSpecificEpithet(epi[1]);
			}
			//species
			else if (speciesRE.matcher(fullName).matches()){
				result = new BotanicalName(Rank.SPECIES());
				result.setGenusOrUninomial(epi[0]);
				result.setSpecificEpithet(epi[1]);
				authorString = fullName.substring(epi[0].length() + 1 + epi[1].length());
			}
			//autonym
			else if (autonymRE.matcher(fullName).matches()){
				result = new BotanicalName(Rank.getRankByAbbreviation(epi[epi.length - 2]));
				result.setGenusOrUninomial(epi[0]);
				result.setSpecificEpithet(epi[1]);
				result.setInfraSpecificEpithet(epi[epi.length - 1]);
				int lenSpecies = 2 + epi[0].length()+epi[1].length();
				int lenInfraSpecies =  2 + epi[epi.length - 2].length() + epi[epi.length - 1].length();
				authorString = fullName.substring(lenSpecies, fullName.length() - lenInfraSpecies);
			}
			//infraSpecies
			else if (infraSpeciesRE.matcher(fullName).matches()){
				String infraSpecRankEpi = epi[2];
				String infraSpecEpi = epi[3];
				if ("tax.".equals(infraSpecRankEpi)){
					infraSpecRankEpi += " " +  epi[3];
					infraSpecEpi = epi[4];
				}
				result = new BotanicalName(Rank.getRankByAbbreviation(infraSpecRankEpi));
				result.setGenusOrUninomial(epi[0]);
				result.setSpecificEpithet(epi[1]);
				result.setInfraSpecificEpithet(infraSpecEpi);
				authorString = fullName.substring(epi[0].length()+ 1 + epi[1].length() +1 + infraSpecRankEpi.length() + 1 + infraSpecEpi.length());
			}//old infraSpecies
			else if (oldInfraSpeciesRE.matcher(fullName).matches()){
				boolean implemented = false;
				if (implemented){
					result = new BotanicalName(Rank.getRankByNameOrAbbreviation(epi[2]));
					result.setGenusOrUninomial(epi[0]);
					result.setSpecificEpithet(epi[1]);
					//TODO result.setUnnamedNamePhrase(epi[2] + " " + epi[3]);
					authorString = fullName.substring(epi[0].length()+ 1 + epi[1].length() +1 + epi[2].length() + 1 + epi[3].length());
				}else{
					result.setHasProblem(true);
					result.setTitleCache(fullName);
					logger.info("Name string " + fullName + " could not be parsed because UnnnamedNamePhrase is not yet implemented!");
				}
			}
			//none
			else{ 
				result.setHasProblem(true);
				result.setTitleCache(fullName);
				logger.info("no applicable parsing rule could be found for \"" + fullName + "\"");
		    }
			//authors
		    if (result != null && authorString != null && authorString.trim().length() > 0 ){ 
				Team[] authors = null;
				try {
					authors = fullTeams(authorString);
				} catch (StringNotParsableException e) {
					result.setHasProblem(true);
					result.setTitleCache(fullName);
					logger.info("no applicable parsing rule could be found for \"" + fullName + "\"");;
				}
				result.setCombinationAuthorTeam(authors[0]);
				result.setExCombinationAuthorTeam(authors[1]);
				result.setBasionymAuthorTeam(authors[2]);
				result.setExBasionymAuthorTeam(authors[3]);
			}	
			//return
			if (result != null){
		    	return(BotanicalName)result;
			}else{
				result.setHasProblem(true);
				result.setTitleCache(fullName);
				logger.info("Name string " + fullName + " could not be parsed!");
				return result;
			}
		} catch (UnknownRankException e) {
			result.setHasProblem(true);
			result.setTitleCache(fullName);
			logger.info("unknown rank (" + (rank == null? "null":rank) + ") or abbreviation in string " +  fullName);
			return result;
		}
	}
	
	
	/**
	 * Parses the fullAuthorString
	 * @param fullAuthorString
	 * @return array of Teams containing the Team[0], 
	 * ExTeam[1], BasionymTeam[2], ExBasionymTeam[3]
	 */
	public Team[] fullTeams (String fullAuthorString)
			throws StringNotParsableException{
		fullAuthorString = fullAuthorString.trim();
		if (! fullAuthorStringRE.matcher(fullAuthorString).matches())
			throw new StringNotParsableException("fullAuthorString (" +fullAuthorString+") not parsable: ");
		return fullTeamsChecked(fullAuthorString);
	}
	
	
	/*
	 * like fullTeams but without trim and match check
	 */
	private Team[] fullTeamsChecked (String fullAuthorString){
		Team[] result = new Team[4]; 
		int authorTeamStart = 0;
		Matcher basionymMatcher = basionymRE.matcher(fullAuthorString);
		if (basionymMatcher.find(0)){
			
			String basString = basionymMatcher.group();
			basString = basString.replaceFirst(basStart, "");
			basString = basString.replaceAll(basEnd, "").trim();
			authorTeamStart = basionymMatcher.end(1) + 1;
			
			Team[] basTeam = authorTeamAndEx(basString);
			result[2]= basTeam[0];
			result[3]= basTeam[1];
		}
		Team[] aTeam = authorTeamAndEx(fullAuthorString.substring(authorTeamStart));
		result[0]= aTeam[0];
		result[1]= aTeam[1];
		return result;
	}
	
	
	/**
	 * Parses the author and ex-author String
	 * @param authorTeamString String representing the author and the ex-author team
	 * @return array of Teams containing the Team[0] and the ExTeam[1]
	 */
	public Team[] authorTeamAndEx (String authorTeamString){
		Team[] result = new Team[2]; 
		//TODO noch allgemeiner am anfang durch Replace etc. 
		authorTeamString = authorTeamString.trim();
		authorTeamString = authorTeamString.replaceFirst(oWs + "ex" + oWs, " ex. " ); 
		int authorEnd = authorTeamString.length();
		
		Matcher exAuthorMatcher = exAuthorRE.matcher(authorTeamString);
		if (exAuthorMatcher.find(0)){
			int exAuthorBegin = exAuthorMatcher.end(0);
			String exString = authorTeamString.substring(exAuthorBegin).trim();
			authorEnd = exAuthorMatcher.start(0);
			result [1] = authorTeam(exString);
		}
		result [0] = authorTeam(authorTeamString.substring(0, authorEnd));
		return result;
	}
	
	
	/**
	 * Parses an authorTeam String and returns the Team 
	 * !!! TODO (atomization not yet implemented)
	 * @param authorTeamString String representing the author team
	 * @return an Team 
	 */
	public Team authorTeam (String authorTeamString){
		if (authorTeamString == null) 
			return null;
		else if ((authorTeamString = authorTeamString.trim()).length() == 0)
			return null;
		else {
			Team result = new Team ();
			result.setTitleCache(authorTeamString);
			//TODO result = atomizedAuthor(authorTeamString); 
			return result;
		} 
		
	}
	

	//Parsing of the given full name that has been identified as hybrid already somewhere else.
	private BotanicalName parseHybrid(String fullName){
	    logger.warn("parseHybrid --> function not yet implemented");
	    BotanicalName result = new BotanicalName(null);
	    result.setTitleCache(fullName);
	    return result;
    }
	
//	// Parsing of the given full name that has been identified as a cultivar already somwhere else.
//	// The ... cv. ... syntax is not covered here as it is not according the rules for naming cultivars.
	public BotanicalName parseCultivar(String fullName)	throws StringNotParsableException{
		CultivarPlantName result = null;
		    String[] words = oWsRE.split(fullName);
			
		    /* ---------------------------------------------------------------------------------
		     * cultivar
		     * ---------------------------------------------------------------------------------*/
			if (fullName.indexOf(" '") != 0){
				//TODO location of 'xx' is probably not arbitrary
				Matcher cultivarMatcher = cultivarRE.matcher(fullName);
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
    //Words used in an epethiton
    static String nonCapitalEpiWord = "[a-z�\\-]+";
    static String capitalEpiWord = "[A-Z]"+ nonCapitalEpiWord;
    
    
    //marker
    static String InfraGenusMarker = "(subgen.|subg.|sect.|subsect.|ser.|subser.|t.infgen.)";
    static String aggrOrGroupMarker = "(aggr.|agg.|group)";
    static String infraSpeciesMarker = "(subsp.|convar.|var.|subvar.|f.|subf.|f.spec.|tax." + fWs + "infrasp.)";
    static String oldInfraSpeciesMarker = "(prol.|proles|race|taxon|sublusus)";
    
    
    //AuthorString
    static String authorPart = "(" + "(D'|L'|'t\\s)?" + capitalDotWord + "('" + nonCapitalDotWord + ")?" + "|da|de(n|l|\\sla)?)" ;
    static String author = "(" + authorPart + "(" + fWs + "|-)" + ")+" + "(f.|fil.|secundus)?";
    static String teamSplitter = fWs + "(&|,)" + fWs;
    static String authorTeam = fWs + "(" + author + teamSplitter + ")*" + author + "(" + teamSplitter + "al.)?" + fWs;
    static String exString = "(ex.?)";
    static String authorAndExTeam = authorTeam + "(" + oWs + exString + oWs + authorTeam + ")?";
    static String basStart = "\\(";
    static String basEnd = "\\)";
    static String basionymAuthor = basStart + "(" + authorAndExTeam + ")" + basEnd;  // '(' and ')' is for evaluation with RE.paren(x)
    static String fullAuthorString = fWs + "(" + basionymAuthor +")?" + fWs + authorAndExTeam + fWs;
    static String facultFullAuthorString = "(" +  fullAuthorString + ")?" ; 
    
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
    
    //Pattern
    static Pattern oWsRE = Pattern.compile(oWs);
    static Pattern teamSplitterRE = Pattern.compile(teamSplitter);
    static Pattern cultivarRE = Pattern.compile(cultivar);
    static Pattern cultivarMarkerRE = Pattern.compile(cultivarMarker);
    static Pattern hybridRE = Pattern.compile(hybrid); 
    
    static Pattern genusOrSupraGenusRE = Pattern.compile(start + genusOrSupraGenus + facultFullAuthorString + end);
    static Pattern infraGenusRE = Pattern.compile(start + infraGenus + facultFullAuthorString + end);
    static Pattern aggrOrGroupRE = Pattern.compile(start + aggrOrGroup + fWs + end); //aggr. or group has no author string
    static Pattern speciesRE = Pattern.compile(start + species + facultFullAuthorString + end);
    static Pattern infraSpeciesRE = Pattern.compile(start + infraSpecies + facultFullAuthorString + end);
    
    
    
    static Pattern oldInfraSpeciesRE = Pattern.compile(start + oldInfraSpecies + facultFullAuthorString + end);
    static Pattern autonymRE = Pattern.compile(start + autonym + fWs + end);
	
    static Pattern basionymRE = Pattern.compile(basionymAuthor);
    //static Pattern startsWithBasionymRE = Pattern.compile(basionymAuthor + anyEnd);
    
    static Pattern exAuthorRE = Pattern.compile(oWs + exString);
    
    static Pattern fullAuthorStringRE = Pattern.compile(fullAuthorString);

	


}
