/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.strategy.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Partial;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IParsable;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.name.BacterialName;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.CultivarPlantName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.IBookSection;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.IVolumeReference;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.strategy.exceptions.StringNotParsableException;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;


/**
 * @author a.mueller
 *
 */
public class NonViralNameParserImpl extends NonViralNameParserImplRegExBase implements INonViralNameParser<NonViralName> {
	private static final Logger logger = Logger.getLogger(NonViralNameParserImpl.class);
	
	// good intro: http://java.sun.com/docs/books/tutorial/essential/regex/index.html
	
	final static boolean MAKE_EMPTY = true;
	final static boolean MAKE_NOT_EMPTY = false;
	
	private boolean authorIsAlwaysTeam = true;
	private ReferenceFactory refFactory = ReferenceFactory.newInstance();
	
	
	public static NonViralNameParserImpl NewInstance(){
		return new NonViralNameParserImpl();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.ITaxonNameParser#parseSubGenericSimpleName(java.lang.String)
	 */
	public NonViralName parseSimpleName(String simpleName){
		return parseSimpleName(simpleName, null, null);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.parser.INonViralNameParser#parseSimpleName(java.lang.String, eu.etaxonomy.cdm.model.name.NomenclaturalCode, eu.etaxonomy.cdm.model.name.Rank)
	 */
	public NonViralName parseSimpleName(String simpleName, NomenclaturalCode code, Rank rank){
		//"parseSimpleName() not yet implemented. Uses parseFullName() instead");
		return parseFullName(simpleName, code, rank);
	}

	public void parseSimpleName(NonViralName nameToBeFilled, String simpleNameString, Rank rank, boolean makeEmpty){
		//"parseSimpleName() not yet implemented. Uses parseFullName() instead");
		parseFullName(nameToBeFilled, simpleNameString, rank, makeEmpty);
	}

	
	public NonViralName getNonViralNameInstance(String fullString, NomenclaturalCode code){
		return getNonViralNameInstance(fullString, code, null);
	}
	
	public NonViralName getNonViralNameInstance(String fullString, NomenclaturalCode code, Rank rank){
		NonViralName result = null;
		if(code ==null) {
			boolean isBotanicalName = anyBotanicFullNamePattern.matcher(fullString).find();
			boolean isZoologicalName = anyZooFullNamePattern.matcher(fullString).find();;
			boolean isBacteriologicalName = false;
			boolean isCultivatedPlantName = false;
			if ( (isBotanicalName || isCultivatedPlantName) && ! isZoologicalName && !isBacteriologicalName){
				if (isBotanicalName){
					result = BotanicalName.NewInstance(rank);
				}else{
					result = CultivarPlantName.NewInstance(rank);
				}
			}else if ( isZoologicalName /*&& ! isBotanicalName*/ && !isBacteriologicalName && !isCultivatedPlantName){
				result = ZoologicalName.NewInstance(rank);
			}else if ( isZoologicalName && ! isBotanicalName && !isBacteriologicalName && !isCultivatedPlantName){
				result = BacterialName.NewInstance(rank);
			}else {
				result =  NonViralName.NewInstance(rank);
			}
		} else {
			switch (code) {
			case ICBN:
				result = BotanicalName.NewInstance(rank);
				break;
			case ICZN:
				result = ZoologicalName.NewInstance(rank);
				break;
			case ICNCP:
				logger.warn("ICNCP parsing not yet implemented");
				result = CultivarPlantName.NewInstance(rank);
				break;
			case ICNB:
				logger.warn("ICNB not yet implemented");
				result = BacterialName.NewInstance(rank);
				break;
			case ICVCN:
				logger.error("Viral name is not a NonViralName !!");
				break;
			default:
				// FIXME Unreachable code
				logger.error("Unknown Nomenclatural Code !!");
			}
		}
		return result;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.parser.INonViralNameParser#parseFullReference(java.lang.String)
	 */
	public NonViralName parseReferencedName(String fullReferenceString) {
		return parseReferencedName(fullReferenceString, null, null);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.ITaxonNameParser#parseFullReference(java.lang.String, eu.etaxonomy.cdm.model.name.Rank)
	 */
	public NonViralName parseReferencedName(String fullReferenceString, NomenclaturalCode nomCode, Rank rank) {
		if (fullReferenceString == null){
			return null;
		}else{
			NonViralName result = getNonViralNameInstance(fullReferenceString, nomCode, rank);
			parseReferencedName(result, fullReferenceString, rank, MAKE_EMPTY);
			return result;
		}
	}
	
	private String standardize(NonViralName nameToBeFilled, String fullReferenceString, boolean makeEmpty){
		//Check null and standardize
		if (fullReferenceString == null){
			//return null;
			return null;
		}
		if (makeEmpty){
			makeEmpty(nameToBeFilled);
		}
		fullReferenceString = fullReferenceString.replaceAll(oWs , " ");
		fullReferenceString = fullReferenceString.trim();
		if ("".equals(fullReferenceString)){
			fullReferenceString = null;
		}
		return fullReferenceString;
	}

	/**
	 * Returns the regEx to be used for the full-name depending on the code
	 * @param nameToBeFilled
	 * @return
	 */
	private String getLocalFullName(NonViralName nameToBeFilled){
		if (nameToBeFilled instanceof ZoologicalName){
			return anyZooFullName;
		}else if (nameToBeFilled instanceof BotanicalName) {
			return anyBotanicFullName;
		}else if (nameToBeFilled instanceof NonViralName) {
			return anyBotanicFullName;  //TODO ?
		}else{
			logger.warn("nameToBeFilled class not supported ("+nameToBeFilled.getClass()+")");
			return null;
		}
	}
	
	/**
	 * Returns the regEx to be used for the fsimple-name depending on the code
	 * @param nameToBeFilled
	 * @return
	 */
	private String getLocalSimpleName(NonViralName nameToBeFilled){
		if (nameToBeFilled instanceof ZoologicalName){
			return anyZooName;
		}else if (nameToBeFilled instanceof NonViralName){
			return anyZooName;  //TODO ?
		}else if (nameToBeFilled instanceof BotanicalName) {
			return anyBotanicName;
		}else{
			logger.warn("nameToBeFilled class not supported ("+nameToBeFilled.getClass()+")");
			return null;
		}
	}
	
	private Matcher getMatcher(String regEx, String matchString){
		Pattern pattern = Pattern.compile(regEx);
		Matcher matcher = pattern.matcher(matchString);
		return matcher;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.ITaxonNameParser#parseFullReference(eu.etaxonomy.cdm.model.name.BotanicalName, java.lang.String, eu.etaxonomy.cdm.model.name.Rank, boolean)
	 */
	public void parseReferencedName(NonViralName nameToBeFilled, String fullReferenceString, Rank rank, boolean makeEmpty) {
		//standardize
		fullReferenceString = standardize(nameToBeFilled, fullReferenceString, makeEmpty);
		if (fullReferenceString == null){
			return;
		}
		// happens already in standardize(...)
//		makeProblemEmpty(nameToBeFilled);
		
		//make nomenclatural status and replace it by empty string 
	    fullReferenceString = parseNomStatus(fullReferenceString, nameToBeFilled);
	    nameToBeFilled.setProblemEnds(fullReferenceString.length());
		
	    //get full name reg
		String localFullName = getLocalFullName(nameToBeFilled);
		//get full name reg
		String localSimpleName = getLocalSimpleName(nameToBeFilled);
		
		//separate name and reference part
		String nameAndRefSeparator = "(^" + localFullName + ")("+ referenceSeperator + ")";
		Matcher nameAndRefSeparatorMatcher = getMatcher (nameAndRefSeparator, fullReferenceString);
		
		Matcher onlyNameMatcher = getMatcher (localFullName, fullReferenceString);
		Matcher onlySimpleNameMatcher = getMatcher (localSimpleName, fullReferenceString);
		
		if (nameAndRefSeparatorMatcher.find()){
			makeNameWithReference(nameToBeFilled, fullReferenceString, nameAndRefSeparatorMatcher, rank, makeEmpty);
		}else if (onlyNameMatcher.matches()){
			makeEmpty = false;
			parseFullName(nameToBeFilled, fullReferenceString, rank, makeEmpty);
		}else if (onlySimpleNameMatcher.matches()){
			makeEmpty = false;
			parseFullName(nameToBeFilled, fullReferenceString, rank, makeEmpty);	//simpleName not yet implemented
		}else{
			makeNoFullRefMatch(nameToBeFilled, fullReferenceString, rank);
		}
		//problem handling. Start and end solved in subroutines
		if (! nameToBeFilled.hasProblem()){
			makeProblemEmpty(nameToBeFilled);
		}
	}
	
	private void makeProblemEmpty(IParsable parsable){
		boolean hasCheckRank = parsable.hasProblem(ParserProblem.CheckRank);
		parsable.setParsingProblem(0);
		if (hasCheckRank){
			parsable.addParsingProblem(ParserProblem.CheckRank);
		}
		parsable.setProblemStarts(-1);
		parsable.setProblemEnds(-1);
	}
	
	private void makeNoFullRefMatch(NonViralName nameToBeFilled, String fullReferenceString, Rank rank){
	    //try to parse first part as name, but keep in mind full string is not parsable
		int start = 0;
		
		String localFullName = getLocalFullName(nameToBeFilled);
		Matcher fullNameMatcher = getMatcher (pStart + localFullName, fullReferenceString);
		if (fullNameMatcher.find()){
			String fullNameString = fullNameMatcher.group(0);
			nameToBeFilled.setProtectedNameCache(false);
			parseFullName(nameToBeFilled, fullNameString, rank, false);
			String sure = nameToBeFilled.getNameCache();
			start = sure.length();
		}
		
//		String localSimpleName = getLocalSimpleName(nameToBeFilled);
//		Matcher simpleNameMatcher = getMatcher (start + localSimpleName, fullReferenceString);
//		if (simpleNameMatcher.find()){
//			String simpleNameString = simpleNameMatcher.group(0);
//			parseFullName(nameToBeFilled, simpleNameString, rank, false);
//			start = simpleNameString.length();
//		}
		
		//don't parse if name can't be separated
		nameToBeFilled.addParsingProblem(ParserProblem.NameReferenceSeparation);
		nameToBeFilled.setTitleCache(fullReferenceString,true);
		nameToBeFilled.setFullTitleCache(fullReferenceString,true);
		// FIXME Quick fix, otherwise search would not deliver results for unparsable names
		nameToBeFilled.setNameCache(fullReferenceString,true);
		// END
		nameToBeFilled.setProblemStarts(start);
		nameToBeFilled.setProblemEnds(fullReferenceString.length());
		logger.info("no applicable parsing rule could be found for \"" + fullReferenceString + "\"");    
	}
	
	private void makeNameWithReference(NonViralName nameToBeFilled, 
			String fullReferenceString, 
			Matcher nameAndRefSeparatorMatcher,
			Rank rank,
			boolean makeEmpty){
		
		String nameAndSeparator = nameAndRefSeparatorMatcher.group(0); 
	    String name = nameAndRefSeparatorMatcher.group(1); 
	    String referenceString = fullReferenceString.substring(nameAndRefSeparatorMatcher.end());
	    
	    // is reference an in ref?
	    String separator = nameAndSeparator.substring(name.length());
		boolean isInReference = separator.matches(inReferenceSeparator);
	    
	    //parse subparts
	    
		int oldProblemEnds = nameToBeFilled.getProblemEnds();
		parseFullName(nameToBeFilled, name, rank, makeEmpty);
	    nameToBeFilled.setProblemEnds(oldProblemEnds);
		
		//zoological new combinations should not have a nom. reference to be parsed
	    if (nameToBeFilled.isInstanceOf(ZoologicalName.class)){
			ZoologicalName zooName = CdmBase.deproxy(nameToBeFilled, ZoologicalName.class);
			//is name new combination?
			if (zooName.getBasionymAuthorTeam() != null || zooName.getOriginalPublicationYear() != null){
				ParserProblem parserProblem = ParserProblem.NewCombinationHasPublication;
				zooName.addParsingProblem(parserProblem);
				nameToBeFilled.setProblemStarts((nameToBeFilled.getProblemStarts()> -1) ? nameToBeFilled.getProblemStarts(): name.length());
				nameToBeFilled.setProblemEnds(Math.max(fullReferenceString.length(), nameToBeFilled.getProblemEnds()));
			}
		}
		
	    parseReference(nameToBeFilled, referenceString, isInReference); 
	    INomenclaturalReference ref = (INomenclaturalReference)nameToBeFilled.getNomenclaturalReference();

	    //problem start
	    int start = nameToBeFilled.getProblemStarts();
	    int nameLength = name.length();
	    int nameAndSeparatorLength = nameAndSeparator.length();
	    int fullRefLength = nameToBeFilled.getFullTitleCache().length();
	    
	    if (nameToBeFilled.isProtectedTitleCache() || nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank)){
	    	start = Math.max(0, start);
		}else{
			if (ref != null && ref.getParsingProblem()!=0){
				start = Math.max(nameAndSeparatorLength, start);
		    	//TODO search within ref
			}	
		}
	    
	    //end
	    int end = nameToBeFilled.getProblemEnds();
	    
	    if (ref != null && ref.getParsingProblem()!=0){
	    	end = Math.min(nameAndSeparatorLength + ref.getProblemEnds(), end);
	    }else{
	    	if (nameToBeFilled.isProtectedTitleCache() ){
	    		end = Math.min(end, nameAndSeparatorLength);
	    		//TODO search within name
			}
	    }
	    nameToBeFilled.setProblemStarts(start);
	    nameToBeFilled.setProblemEnds(end);

	    //delegate has problem to name
	    if (ref != null && ref.getParsingProblem()!=0){
	    	nameToBeFilled.addParsingProblems(ref.getParsingProblem());
	    }
	    
	    ReferenceBase nomRef;
		if ( (nomRef = (ReferenceBase)nameToBeFilled.getNomenclaturalReference()) != null ){
			nomRef.setAuthorTeam((TeamOrPersonBase)nameToBeFilled.getCombinationAuthorTeam());
		}
	}
	
	//TODO make it an Array of status
	/**
	 * Extracts a {@link NomenclaturalStatus} from the reference String and adds it to the @link {@link TaxonNameBase}.
	 * The nomenclatural status part ist deleted from the reference String.
	 * @return  String the new (shortend) reference String 
	 */ 
	private String parseNomStatus(String fullString, NonViralName nameToBeFilled) {
		String statusString;
		Pattern hasStatusPattern = Pattern.compile("(" + pNomStatusPhrase + ")"); 
		Matcher hasStatusMatcher = hasStatusPattern.matcher(fullString);
		
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
			    
				fullString = fullString.replace(statusPhrase, "");
			} catch (UnknownCdmTypeException e) {
				//Do nothing
			}
		}
		return fullString;
	}
	
	
	private void parseReference(NonViralName nameToBeFilled, String strReference, boolean isInReference){
		
		INomenclaturalReference ref;
		String originalStrReference = strReference;
		
		//End (just delete end (e.g. '.', may be ambigous for yearPhrase, but no real information gets lost
		Matcher endMatcher = getMatcher(referenceEnd + end, strReference);
		if (endMatcher.find()){
			String endPart = endMatcher.group(0);
			strReference = strReference.substring(0, strReference.length() - endPart.length());
		}
		
//		String pDetailYear = ".*" + detailSeparator + detail + fWs + yearSeperator + fWs + yearPhrase + fWs + end;
//		Matcher detailYearMatcher = getMatcher(pDetailYear, strReference);
		
		String strReferenceWithYear = strReference;
		//year
		String yearPart = null;
		String pYearPhrase = yearSeperator + fWs + yearPhrase + fWs + end;
		Matcher yearPhraseMatcher = getMatcher(pYearPhrase, strReference);
		if (yearPhraseMatcher.find()){
			yearPart = yearPhraseMatcher.group(0);
			strReference = strReference.substring(0, strReference.length() - yearPart.length());
			yearPart = yearPart.replaceFirst(pStart + yearSeperator, "").trim();
		}else{
			if (nameToBeFilled.isInstanceOf(ZoologicalName.class)){
				ZoologicalName zooName = CdmBase.deproxy(nameToBeFilled, ZoologicalName.class);
				yearPart = String.valueOf(zooName.getPublicationYear());
				//continue
			}else{
				ref = makeDetailYearUnparsable(nameToBeFilled,strReference);
				ref.setDatePublished(TimePeriod.parseString(yearPart));
				return;
			}
		}
		
			
		//detail
		String pDetailPhrase = detailSeparator + fWs + detail + fWs + end;
		Matcher detailPhraseMatcher = getMatcher(pDetailPhrase, strReference);
		if (detailPhraseMatcher.find()){
			String detailPart = detailPhraseMatcher.group(0);
			strReference = strReference.substring(0, strReference.length() - detailPart.length());
			detailPart = detailPart.replaceFirst(pStart + detailSeparator, "").trim();
			nameToBeFilled.setNomenclaturalMicroReference(detailPart);
		}else{
			makeDetailYearUnparsable(nameToBeFilled, strReferenceWithYear);
			return;
		}
		//parse title and author
		ref = parseReferenceTitle(strReference, yearPart, isInReference);
		if (ref.hasProblem()){
			ref.setTitleCache( (isInReference?"in ":"") +  originalStrReference,true);
		}
		nameToBeFilled.setNomenclaturalReference((ReferenceBase)ref);
		int end = Math.min(strReference.length(), ref.getProblemEnds());
		ref.setProblemEnds(end);
	}

	/**
	 * @param nameToBeFilled
	 * @param strReference
	 * @return 
	 */
	private INomenclaturalReference makeDetailYearUnparsable(NonViralName nameToBeFilled, String strReference) {
		INomenclaturalReference ref;
		//ref = Generic.NewInstance();
		
		ref = refFactory.newGeneric();
		ref.setTitleCache(strReference,true);
		ref.setProblemEnds(strReference.length());
		ref.addParsingProblem(ParserProblem.CheckDetailOrYear);
		nameToBeFilled.addParsingProblem(ParserProblem.CheckDetailOrYear);
		nameToBeFilled.setNomenclaturalReference((ReferenceBase)ref);
		return ref;
	}
		
	/**
	 * Parses the referenceTitlePart, including the author volume and edition.
	 * @param reference
	 * @param year
	 * @return
	 */
	private INomenclaturalReference parseReferenceTitle(String strReference, String year, boolean isInReference){
		IBook result = null;
		
		Matcher refSineDetailMatcher = referenceSineDetailPattern.matcher(strReference);
		if (! refSineDetailMatcher.matches()){
			//TODO ?
		}
		
		Matcher articleMatcher = getMatcher(pArticleReference, strReference);
		
		Matcher softArticleMatcher = getMatcher(pSoftArticleReference, strReference);
		Matcher bookMatcher = getMatcher(pBookReference, strReference);
		Matcher bookSectionMatcher = getMatcher(pBookSectionReference, strReference);
		
		
		if(isInReference == false){
			if (bookMatcher.matches() ){
				result = parseBook(strReference);
			}else{
				logger.warn("Non-InRef must be book but does not match book");
				result = refFactory.newBook();
				makeUnparsableRefTitle(result, strReference);
			}
		}else{  //inRef
			if (articleMatcher.matches()){
				//article without separators like ","
				result = parseArticle(strReference);
			}else if (softArticleMatcher.matches()){
				result = parseArticle(strReference);
			}else if (bookSectionMatcher.matches()){
				result = parseBookSection(strReference);
			}else{
				result =  refFactory.newGeneric();
				makeUnparsableRefTitle(result, "in " + strReference);
			}
		}
		//make year
		if (makeYear(result, year) == false){
			//TODO
			logger.warn("Year could not be parsed");
		}
		result.setProblemStarts(0);
		result.setProblemEnds(strReference.length());
		return result;
	}
	
	private void makeUnparsableRefTitle(INomenclaturalReference result, String reference){
		result.setTitleCache(reference,true);
		result.addParsingProblem(ParserProblem.UnparsableReferenceTitle);
	}
	
	/**
	 * Parses a single date string. If the string is not parsable a StringNotParsableException is thrown
	 * @param singleDateString
	 * @return
	 * @throws StringNotParsableException
	 */
	private static Partial parseSingleDate(String singleDateString) 
			throws StringNotParsableException{
		Partial dt = new Partial();
		if (CdmUtils.isNumeric(singleDateString)){
			try {
				Integer year = Integer.valueOf(singleDateString.trim());
				if (year > 1750 && year < 2050){
					dt = dt.with(DateTimeFieldType.year(), year);
				}else{
					dt = null;
				}
			} catch (NumberFormatException e) {
				logger.debug("Not a Integer format in getCalendar()");
				throw new StringNotParsableException(singleDateString + "is not parsable as a single Date");
			}
		}
		return dt;
	}

	
	/**
	 * Parses the publication date part. 
	 * @param nomRef
	 * @param year
	 * @return If the string is not parsable <code>false</code>
	 * is returned. <code>True</code> otherwise
	 */
	private boolean makeYear(INomenclaturalReference nomRef, String year){
		boolean result = true;
		if (year == null){
			return false;
		}
		if ("".equals(year.trim())){
			return true;
		}
		TimePeriod datePublished = TimePeriod.parseString(year);
		
		if (nomRef.getType().equals(ReferenceType.BookSection)){
			handleBookSectionYear((IBookSection)nomRef, datePublished);
		}else if (nomRef instanceof ReferenceBase){
			((ReferenceBase)nomRef).setDatePublished(datePublished);	
		}else{
			throw new ClassCastException("nom Ref is not of type ReferenceBase but " + (nomRef == null? "(null)" : nomRef.getClass()));
		}
		return result;	
	}
	
	private String makeVolume(IVolumeReference nomRef, String strReference){
		//volume
		String volPart = null;
		String pVolPhrase = volumeSeparator +  volume + end;
		Matcher volPhraseMatcher = getMatcher(pVolPhrase, strReference);
		if (volPhraseMatcher.find()){
			volPart = volPhraseMatcher.group(0);
			strReference = strReference.substring(0, strReference.length() - volPart.length());
			volPart = volPart.replaceFirst(pStart + volumeSeparator, "").trim();
			nomRef.setVolume(volPart);
		}
		return strReference;
	}
	
	private String makeEdition(IBook book, String strReference){
		//volume
		String editionPart = null;
		Matcher editionPhraseMatcher = getMatcher(pEditionPart, strReference);
		
		Matcher editionVolumeMatcher = getMatcher(pEditionVolPart, strReference);
		boolean isEditionAndVol = editionVolumeMatcher.find();
		
		if (editionPhraseMatcher.find()){
			editionPart = editionPhraseMatcher.group(0);
			int pos = strReference.indexOf(editionPart);
			int posEnd = pos + editionPart.length();
			if (isEditionAndVol){
				posEnd++;  //delete also comma
			}
			strReference = strReference.substring(0, pos) + strReference.substring(posEnd);
			editionPart = editionPart.replaceFirst(pStart + editionSeparator, "").trim();
			book.setEdition(editionPart);
		}
		return strReference;
	}
	
	private IBook parseBook(String reference){
		IBook result = refFactory.newBook();
		reference = makeEdition(result, reference);
		reference = makeVolume(result, reference);
		result.setTitle(reference);
		return result;
	}
	
	
	private ReferenceBase parseArticle(String reference){
		//if (articlePatter)
		//(type, author, title, volume, editor, series;
		ReferenceBase result = refFactory.newArticle();
		reference = makeVolume(result, reference);
		ReferenceBase inJournal = refFactory.newJournal();
		inJournal.setTitle(reference);
		result.setInReference(inJournal);
		return result;
	}
	
	private ReferenceBase parseBookSection(String reference){
		ReferenceBase result = refFactory.newBookSection();
		String[] parts = reference.split(referenceAuthorSeparator, 2);
		if (parts.length != 2){
			logger.warn("Unexpected number of parts");
			result.setTitleCache(reference,true);
		}else{
			String authorString = parts[0];
			String bookString = parts[1];
			
			TeamOrPersonBase<?> authorTeam = author(authorString);
			IBook inBook = parseBook(bookString);
			inBook.setAuthorTeam(authorTeam);
			result.setInBook(inBook);
		}
		return result;
	}
	
	/**
	 * If the publication date of a book section and it's inBook do differ this is usually 
	 * caused by the fact that a book has been published during a period, because originally 
	 * it consisted of several parts that only later where put together to one book.
	 * If so, the book section's publication date may be a point in time (year or month of year)
	 * whereas the books publication date may be a period of several years.
	 * Therefore a valid nomenclatural reference string should use the book sections 
	 * publication date rather then the book's publication date.<BR>
	 * This method in general adds the publication date to the book section.
	 * An exception exists if the publication date is a period. Then the parser
	 * assumes that the nomenclatural reference string does not follow the above rule but
	 * the books publication date is set.
	 * @param bookSection
	 * @param datePublished
	 */
	private void handleBookSectionYear(IBookSection bookSection, TimePeriod datePublished){
		if (datePublished == null || datePublished.getStart() == null || bookSection == null){
			return;
		}
		if (datePublished.isPeriod() && bookSection.getInBook() != null){
			bookSection.getInBook().setDatePublished(datePublished);
		}else{
			bookSection.setDatePublished(datePublished);	
		}
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.parser.INonViralNameParser#parseFullName(java.lang.String)
	 */
	public NonViralName parseFullName(String fullNameString){
		return parseFullName(fullNameString, null, null);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.ITaxonNameParser#parseFullName(java.lang.String, eu.etaxonomy.cdm.model.name.Rank)
	 */
	public NonViralName parseFullName(String fullNameString, NomenclaturalCode nomCode, Rank rank) {
		
		if (fullNameString == null){
			return null;
		}else{
			NonViralName result = getNonViralNameInstance(fullNameString, nomCode, rank);
			parseFullName(result, fullNameString, rank, false);
			return result;
		}
	}
		
	
	public void parseFullName(NonViralName nameToBeFilled, String fullNameString, Rank rank, boolean makeEmpty) {
		//TODO prol. etc.
		boolean hasCheckRankProblem = false; //was rank guessed in a previous parsing process?
		if (nameToBeFilled == null){
			logger.warn("name is null!");
		}else{
			hasCheckRankProblem = nameToBeFilled.hasProblem(ParserProblem.CheckRank);
			nameToBeFilled.removeParsingProblem(ParserProblem.CheckRank);
		}
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
				if (rank != null && ! hasCheckRankProblem  && (rank.isSupraGeneric()|| rank.isGenus())){
					nameToBeFilled.setRank(rank);
					nameToBeFilled.setGenusOrUninomial(epi[0]);
				} 
				//genus or guess rank
				else {
					rank = guessUninomialRank(nameToBeFilled, epi[0]); 
					nameToBeFilled.setRank(rank);
					nameToBeFilled.setGenusOrUninomial(epi[0]);
					nameToBeFilled.addParsingProblem(ParserProblem.CheckRank);
					nameToBeFilled.setProblemStarts(0);
					nameToBeFilled.setProblemEnds(epi[0].length());
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
					nameToBeFilled.addParsingProblem(ParserProblem.OldInfraSpeciesNotSupported);
					nameToBeFilled.setTitleCache(fullNameString,true);
					// FIXME Quick fix, otherwise search would not deilver results for unparsable names
					nameToBeFilled.setNameCache(fullNameString,true);
					// END
					logger.info("Name string " + fullNameString + " could not be parsed because UnnnamedNamePhrase is not yet implemented!");
				}
			}
			//none
			else{ 
				nameToBeFilled.addParsingProblem(ParserProblem.UnparsableNamePart);
				nameToBeFilled.setTitleCache(fullNameString,true);
				// FIXME Quick fix, otherwise search would not deilver results for unparsable names
				nameToBeFilled.setNameCache(fullNameString,true);
				// END
				logger.info("no applicable parsing rule could be found for \"" + fullNameString + "\"");
		    }
			//authors
		    if (nameToBeFilled != null && authorString != null && authorString.trim().length() > 0 ){ 
				TeamOrPersonBase<?>[] authors = new TeamOrPersonBase[4];
				Integer[] years = new Integer[4];
				try {
					Class<? extends NonViralName> clazz = nameToBeFilled.getClass();
					fullAuthors(authorString, authors, years, clazz);
				} catch (StringNotParsableException e) {
					nameToBeFilled.addParsingProblem(ParserProblem.UnparsableAuthorPart);
					nameToBeFilled.setTitleCache(fullNameString,true);
					// FIXME Quick fix, otherwise search would not deilver results for unparsable names
					nameToBeFilled.setNameCache(fullNameString,true);
					// END
					logger.info("no applicable parsing rule could be found for \"" + fullNameString + "\"");;
				}
				nameToBeFilled.setCombinationAuthorTeam(authors[0]);
				nameToBeFilled.setExCombinationAuthorTeam(authors[1]);
				nameToBeFilled.setBasionymAuthorTeam(authors[2]);
				nameToBeFilled.setExBasionymAuthorTeam(authors[3]);
				if (nameToBeFilled instanceof ZoologicalName){
					ZoologicalName zooName = (ZoologicalName)nameToBeFilled;
					zooName.setPublicationYear(years[0]);
					zooName.setOriginalPublicationYear(years[2]);
				}
			}	
			//return
			if (nameToBeFilled != null){
		    	//return(BotanicalName)result;
				return;
			}
		} catch (UnknownCdmTypeException e) {
			nameToBeFilled.addParsingProblem(ParserProblem.RankNotSupported);
			nameToBeFilled.setTitleCache(fullNameString,true);
			// FIXME Quick fix, otherwise search would not deilver results for unparsable names
			nameToBeFilled.setNameCache(fullNameString,true);
			// END
			logger.info("unknown rank (" + (rank == null? "null":rank) + ") or abbreviation in string " +  fullNameString);
			//return result;
			return;
		}
	}

	
	
	/**
	 * Guesses the rank of uninomial depending on the typical endings for ranks
	 * @param nameToBeFilled
	 * @param string
	 */
	private Rank guessUninomialRank(NonViralName nameToBeFilled, String uninomial) {
		Rank result = Rank.GENUS();
		if (nameToBeFilled.isInstanceOf(BotanicalName.class)){
			if (false){
				//
			}else if (uninomial.endsWith("phyta") || uninomial.endsWith("mycota") ){  //plants, fungi
				result = Rank.SECTION_BOTANY();
			}else if (uninomial.endsWith("bionta")){
				result = Rank.SUBKINGDOM();  //TODO
			}else if (uninomial.endsWith("phytina")|| uninomial.endsWith("mycotina")  ){  //plants, fungi
				result = Rank.SUBSECTION_BOTANY();
			}else if (uninomial.endsWith("opsida") || uninomial.endsWith("phyceae") || uninomial.endsWith("mycetes")){  //plants, algae, fungi
				result = Rank.CLASS();
			}else if (uninomial.endsWith("idae") || uninomial.endsWith("phycidae") || uninomial.endsWith("mycetidae")){ //plants, algae, fungi
				result = Rank.SUBCLASS();
			}else if (uninomial.endsWith("ales")){
				result = Rank.ORDER();
			}else if (uninomial.endsWith("ineae")){
				result = Rank.SUBORDER();
			}else if (uninomial.endsWith("aceae")){
					result = Rank.FAMILY();
			}else if (uninomial.endsWith("oideae")){
				result = Rank.SUBFAMILY();
			}else if (uninomial.endsWith("eae")){
				result = Rank.TRIBE();
			}else if (uninomial.endsWith("inae")){
				result = Rank.SUBTRIBE();
			}else if (uninomial.endsWith("ota")){
				result = Rank.KINGDOM();  //TODO
			}
		}else if (nameToBeFilled.isInstanceOf(ZoologicalName.class)){
			if (false){
				//
			}else if (uninomial.endsWith("oideae")){
				result = Rank.SUPERFAMILY();
			}else if (uninomial.endsWith("idae")){
					result = Rank.FAMILY();
			}else if (uninomial.endsWith("inae")){
				result = Rank.SUBFAMILY();
			}else if (uninomial.endsWith("inae")){
				result = Rank.SUBFAMILY();
			}else if (uninomial.endsWith("ini")){
				result = Rank.TRIBE();
			}else if (uninomial.endsWith("ina")){
				result = Rank.SUBTRIBE();
			}
		}else{
			//
		}
		return result;
	}

	/**
	 * Parses the fullAuthorString
	 * @param fullAuthorString
	 * @return array of Teams containing the Team[0], 
	 * ExTeam[1], BasionymTeam[2], ExBasionymTeam[3]
	 */
	protected void fullAuthors (String fullAuthorString, TeamOrPersonBase<?>[] authors, Integer[] years, Class<? extends NonViralName> clazz)
			throws StringNotParsableException{
		fullAuthorString = fullAuthorString.trim();
		if (fullAuthorString == null || clazz == null){
			return;
		}
		//Botanic
		if ( BotanicalName.class.isAssignableFrom(clazz) ){
			if (! fullBotanicAuthorStringPattern.matcher(fullAuthorString).matches() ){
				throw new StringNotParsableException("fullAuthorString (" +fullAuthorString+") not parsable: ");
			}
		}
		//Zoo
		else if ( ZoologicalName.class.isAssignableFrom(clazz) ){
			if (! fullZooAuthorStringPattern.matcher(fullAuthorString).matches() ){
				throw new StringNotParsableException("fullAuthorString (" +fullAuthorString+") not parsable: ");
			}
		}else {
			//TODO
			logger.warn ("not yet implemented");
			throw new StringNotParsableException("fullAuthorString (" +fullAuthorString+") not parsable: ");
		}
		fullAuthorsChecked(fullAuthorString, authors, years);
	}
	
	/*
	 * like fullTeams but without trim and match check
	 */
	protected void fullAuthorsChecked (String fullAuthorString, TeamOrPersonBase<?>[] authors, Integer[] years){
		int authorTeamStart = 0;
		Matcher basionymMatcher = basionymPattern.matcher(fullAuthorString);
		
		if (basionymMatcher.find(0)){
			
			String basString = basionymMatcher.group();
			basString = basString.replaceFirst(basStart, "");
			basString = basString.replaceAll(basEnd, "").trim();
			authorTeamStart = basionymMatcher.end(1) + 1;
			
			TeamOrPersonBase<?>[] basAuthors = new TeamOrPersonBase[2];
			Integer[] basYears = new Integer[2];
			authorsAndEx(basString, basAuthors, basYears);
			authors[2]= basAuthors[0];
			years[2] = basYears[0];
			authors[3]= basAuthors[1];
			years[3] = basYears[1];
		}
		if (fullAuthorString.length() >= authorTeamStart){
			TeamOrPersonBase<?>[] combinationAuthors = new TeamOrPersonBase[2];;
			Integer[] combinationYears = new Integer[2];
			authorsAndEx(fullAuthorString.substring(authorTeamStart), combinationAuthors, combinationYears);
			authors[0]= combinationAuthors[0] ;
			years[0] = combinationYears[0];
			authors[1]= combinationAuthors[1];
			years[1] = combinationYears[1];
		}
	}
	
	
	/**
	 * Parses the author and ex-author String
	 * @param authorTeamString String representing the author and the ex-author team
	 * @return array of Teams containing the Team[0] and the ExTeam[1]
	 */
	protected void authorsAndEx (String authorTeamString, TeamOrPersonBase<?>[] authors, Integer[] years){
		//TODO noch allgemeiner am anfang durch Replace etc. 
		authorTeamString = authorTeamString.trim();
		authorTeamString = authorTeamString.replaceFirst(oWs + "ex" + oWs, " ex. " ); 
		int authorEnd = authorTeamString.length();
		
		Matcher exAuthorMatcher = exAuthorPattern.matcher(authorTeamString);
		if (exAuthorMatcher.find(0)){
			int exAuthorBegin = exAuthorMatcher.end(0);
			String exString = authorTeamString.substring(exAuthorBegin).trim();
			authorEnd = exAuthorMatcher.start(0);
			authors [1] = author(exString);
		}
		zooOrBotanicAuthor(authorTeamString.substring(0, authorEnd), authors, years );
	}
	
	/**
	 * Parses the authorString and if it matches an botanical or zoological authorTeam it fills
	 * the computes the AuthorTeam and fills it into the first field of the team array. Same applies 
	 * to the year in case of an zoological name. 
	 * @param authorString
	 * @param team
	 * @param year
	 */
	protected void zooOrBotanicAuthor(String authorString, TeamOrPersonBase<?>[] team, Integer[] year){
		if (authorString == null){ 
			return;
		}else if ((authorString = authorString.trim()).length() == 0){
			return;
		}
		Matcher zooAuthorAddidtionMatcher = zooAuthorAddidtionPattern.matcher(authorString);
		if (zooAuthorAddidtionMatcher.find()){
			int index = zooAuthorAddidtionMatcher.start(0); 
			String strYear = authorString.substring(index);
			strYear = strYear.replaceAll(zooAuthorYearSeperator, "").trim();
			year[0] = Integer.valueOf(strYear);
			authorString = authorString.substring(0, index).trim();
		}
		team[0] = author(authorString);
	}
	
	
	/**
	 * Parses an authorTeam String and returns the Team 
	 * !!! TODO (atomization not yet implemented)
	 * @param authorTeamString String representing the author team
	 * @return an Team 
	 */
	protected TeamOrPersonBase<?> author (String authorString){
		if (authorString == null){ 
			return null;
		}else if ((authorString = authorString.trim()).length() == 0){
			return null;
		}else if (! teamSplitterPattern.matcher(authorString).find() && ! authorIsAlwaysTeam){
			//1 Person
			Person result = Person.NewInstance();
			result.setNomenclaturalTitle(authorString);
			return result;
		}else{
			return parsedTeam(authorString);
		} 
		
	}
	
	/**
	 * Parses an authorString (reprsenting a team into the single authors and add
	 * them to the return Team.
	 * @param authorString
	 * @return Team
	 */
	protected Team parsedTeam(String authorString){
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
	    result.setTitleCache(fullName,true);
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

	
	private void makeEmpty(NonViralName nameToBeFilled){
		nameToBeFilled.setRank(null);
		nameToBeFilled.setTitleCache(null, false);
		nameToBeFilled.setFullTitleCache(null, false);
		nameToBeFilled.setNameCache(null, false);
				
		nameToBeFilled.setAppendedPhrase(null);
		nameToBeFilled.setBasionymAuthorTeam(null);
		nameToBeFilled.setCombinationAuthorTeam(null);
		nameToBeFilled.setExBasionymAuthorTeam(null);
		nameToBeFilled.setExCombinationAuthorTeam(null);
		nameToBeFilled.setAuthorshipCache(null, false);
		
		
		//delete problems except check rank
		makeProblemEmpty(nameToBeFilled);
				
		// TODO ?
		//nameToBeFilled.setHomotypicalGroup(newHomotypicalGroup);

		
		nameToBeFilled.setGenusOrUninomial(null);
		nameToBeFilled.setInfraGenericEpithet(null);
		nameToBeFilled.setSpecificEpithet(null);
		nameToBeFilled.setInfraSpecificEpithet(null);
		
		nameToBeFilled.setNomenclaturalMicroReference(null);
		nameToBeFilled.setNomenclaturalReference(null);
		
		nameToBeFilled.setHybridFormula(false);
		nameToBeFilled.setMonomHybrid(false);
		nameToBeFilled.setBinomHybrid(false);
		nameToBeFilled.setTrinomHybrid(false);
		
		if (nameToBeFilled.isInstanceOf(BotanicalName.class)){
			BotanicalName botanicalName = (BotanicalName)nameToBeFilled;
			botanicalName.setAnamorphic(false);
		}
		
		if (nameToBeFilled.isInstanceOf(ZoologicalName.class)){
			ZoologicalName zoologicalName = (ZoologicalName)nameToBeFilled;
			zoologicalName.setBreed(null);
			zoologicalName.setOriginalPublicationYear(null);
			
		}
	}
	
	
    
}
