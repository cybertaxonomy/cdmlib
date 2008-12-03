/**
 * 
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
import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.BibtexReference;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.BookSection;
import eu.etaxonomy.cdm.model.reference.Generic;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.IVolumeReference;
import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.model.reference.SectionBase;
import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;
import eu.etaxonomy.cdm.strategy.exceptions.StringNotParsableException;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;


/**
 * @author a.mueller
 *
 */
public class NonViralNameParserImpl implements INonViralNameParser<NonViralName<?>> {
	private static final Logger logger = Logger.getLogger(NonViralNameParserImpl.class);
	
	// good intro: http://java.sun.com/docs/books/tutorial/essential/regex/index.html
	
	final static boolean MAKE_EMPTY = true;
	final static boolean MAKE_NOT_EMPTY = false;
	
	
	public static NonViralNameParserImpl NewInstance(){
		return new NonViralNameParserImpl();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.ITaxonNameParser#parseSimpleName(java.lang.String, eu.etaxonomy.cdm.model.name.Rank)
	 */
	public NonViralName<?> parseSimpleName(String simpleName, Rank rank){
		//TODO
		logger.warn("parseSimpleName() not yet implemented. Uses parseFullName() instead");
		return parseFullName(simpleName, null, rank);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.ITaxonNameParser#parseSubGenericSimpleName(java.lang.String)
	 */
	public NonViralName<?> parseSimpleName(String simpleName){
		return parseSimpleName(simpleName, null);
	}
	
	public NonViralName<?> getNonViralNameInstance(String fullString, NomenclaturalCode code){
		return getNonViralNameInstance(fullString, code, null);
	}
	
	public NonViralName<?> getNonViralNameInstance(String fullString, NomenclaturalCode code, Rank rank){
		NonViralName<?> result = null;
		if (code == null){
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
		}else if (code.equals(NomenclaturalCode.ICBN())){
			result = BotanicalName.NewInstance(rank);
		}else if (code.equals(NomenclaturalCode.ICZN())){
			result = ZoologicalName.NewInstance(rank);
		}else if (code.equals(NomenclaturalCode.ICNCP())){
			logger.warn("ICNCP parsing not yet implemented");
			result = CultivarPlantName.NewInstance(rank);
		}else if (code.equals(NomenclaturalCode.ICNB())){
			logger.warn("ICNB not yet implemented");
			result = BacterialName.NewInstance(rank);
		}else if (code.equals(NomenclaturalCode.ICVCN())){
			logger.error("Viral name is not a NonViralName !!");
		}else{
			logger.error("Unknown Nomenclatural Code !!");
		}
		return result;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.parser.INonViralNameParser#parseFullReference(java.lang.String)
	 */
	public NonViralName<?> parseReferencedName(String fullReferenceString) {
		return parseReferencedName(fullReferenceString, null, null);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.ITaxonNameParser#parseFullReference(java.lang.String, eu.etaxonomy.cdm.model.name.Rank)
	 */
	public NonViralName<?> parseReferencedName(String fullReferenceString, NomenclaturalCode nomCode, Rank rank) {
		if (fullReferenceString == null){
			return null;
		}else{
			NonViralName<?> result = getNonViralNameInstance(fullReferenceString, nomCode, rank);
			parseReferencedName(result, fullReferenceString, rank, MAKE_EMPTY);
			return result;
		}
	}
	
	private String standardize(NonViralName<?> nameToBeFilled, String fullReferenceString, boolean makeEmpty){
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
	private String getLocalFullName(NonViralName<?> nameToBeFilled){
		if (nameToBeFilled instanceof ZoologicalName){
			return anyZooFullName;
		}else if (nameToBeFilled instanceof NonViralName) {
			return anyBotanicFullName;  //TODO ?
		}else if (nameToBeFilled instanceof BotanicalName) {
			return anyBotanicFullName;
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
	private String getLocalSimpleName(NonViralName<?> nameToBeFilled){
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
	public void parseReferencedName(NonViralName<?> nameToBeFilled, String fullReferenceString, Rank rank, boolean makeEmpty) {
		//standardize
		fullReferenceString = standardize(nameToBeFilled, fullReferenceString, makeEmpty);
		if (fullReferenceString == null){
			return;
		}
		makeProblemEmpty(nameToBeFilled);
		
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
		parsable.setHasProblem(false);
		parsable.setProblemStarts(-1);
		parsable.setProblemEnds(-1);
	}
	
	private void makeNoFullRefMatch(NonViralName<?> nameToBeFilled, String fullReferenceString, Rank rank){
	    //try to parse first part as name, but keep in mind full string is not parsable
		int start = 0;
		
		String localFullName = getLocalFullName(nameToBeFilled);
		Matcher fullNameMatcher = getMatcher (pStart + localFullName, fullReferenceString);
		if (fullNameMatcher.find()){
			String fullNameString = fullNameMatcher.group(0);
			nameToBeFilled.setProtectedNameCache(false);  //TODO why is is true?
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
		nameToBeFilled.setHasProblem(true);
		nameToBeFilled.setTitleCache(fullReferenceString);
		nameToBeFilled.setFullTitleCache(fullReferenceString);
		nameToBeFilled.setProblemStarts(start);
		nameToBeFilled.setProblemEnds(fullReferenceString.length());
		logger.info("no applicable parsing rule could be found for \"" + fullReferenceString + "\"");    
	}
	
	private void makeNameWithReference(NonViralName<?> nameToBeFilled, 
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
		parseReference(nameToBeFilled, referenceString, isInReference); 
	    INomenclaturalReference<?> ref = nameToBeFilled.getNomenclaturalReference();

	    //problem start
	    int start = nameToBeFilled.getProblemStarts();
	    int nameLength = name.length();
	    int nameAndSeparatorLength = nameAndSeparator.length();
	    int fullRefLength = nameToBeFilled.getFullTitleCache().length();
	    
	    if (nameToBeFilled.isProtectedTitleCache() || nameToBeFilled.getRank() == null ){
	    	start = Math.max(0, start);
		}else{
			if (ref != null && ref.getHasProblem()){
				start = Math.max(nameAndSeparatorLength, start);
		    	//TODO search within ref
			}	
		}
	    
	    //end
	    int end = nameToBeFilled.getProblemEnds();
	    
	    if (ref != null && ref.getHasProblem()){
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
	    if (ref != null && ref.getHasProblem()){
	    	nameToBeFilled.setHasProblem(true);
	    }
	}
	
	//TODO make it an Array of status
	/**
	 * Extracts a {@link NomenclaturalStatus} from the reference String and adds it to the @link {@link TaxonNameBase}.
	 * The nomenclatural status part ist deleted from the reference String.
	 * @return  String the new (shortend) reference String 
	 */ 
	private String parseNomStatus(String fullString, NonViralName<?> nameToBeFilled) {
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
	
	
	private void parseReference(NonViralName<?> nameToBeFilled, String strReference, boolean isInReference){
		INomenclaturalReference<?> ref;
		String originalStrReference = strReference;
		
		//End (just delete end (e.g. '.', may be ambigous for yearPhrase, but no real information gets lost
		Matcher endMatcher = getMatcher(referenceEnd + end, strReference);
		if (endMatcher.find()){
			String endPart = endMatcher.group(0);
			strReference = strReference.substring(0, strReference.length() - endPart.length());
		}
		
		String pDetailYear = ".*" + detailSeparator + detail + fWs + yearSeperator + fWs + yearPhrase + fWs + end;
		Matcher detailYearMatcher = getMatcher(pDetailYear, strReference);
		
		//if (referencePattern.matcher(reference).matches() ){
		if (detailYearMatcher.matches() ){
			
			//year
			String yearPart = null;
			String pYearPhrase = yearSeperator + fWs + yearPhrase + fWs + end;
			Matcher yearPhraseMatcher = getMatcher(pYearPhrase, strReference);
			if (yearPhraseMatcher.find()){
				yearPart = yearPhraseMatcher.group(0);
				strReference = strReference.substring(0, strReference.length() - yearPart.length());
				yearPart = yearPart.replaceFirst(pStart + yearSeperator, "").trim();
			}
			
			//detail
			String pDetailPhrase = detailSeparator + fWs + detail + fWs + end;
			Matcher detailPhraseMatcher = getMatcher(pDetailPhrase, strReference);
			if (detailPhraseMatcher.find()){
				String detailPart = detailPhraseMatcher.group(0);
				strReference = strReference.substring(0, strReference.length() - detailPart.length());
				detailPart = detailPart.replaceFirst(pStart + detailSeparator, "").trim();
				nameToBeFilled.setNomenclaturalMicroReference(detailPart);
			}
			//parse title and author
			ref = parseReferenceTitle(strReference, yearPart, isInReference);
			if (ref.hasProblem()){
				ref.setTitleCache( (isInReference?"in ":"") +  originalStrReference);
			}
			nameToBeFilled.setNomenclaturalReference(ref);
			int end = Math.min(strReference.length(), ref.getProblemEnds());
			ref.setProblemEnds(end);
	    }else{  //detail and year not parsable
	    	ref = Generic.NewInstance();
	    	ref.setTitleCache(strReference);
	    	ref.setProblemEnds(strReference.length());
	    	ref.setHasProblem(true);
	    	nameToBeFilled.setHasProblem(true);
	    	nameToBeFilled.setNomenclaturalReference(ref);
	    }
	}
		
	/**
	 * Parses the referenceTitlePart, including the author volume and edition.
	 * @param reference
	 * @param year
	 * @return
	 */
	private INomenclaturalReference<?> parseReferenceTitle(String strReference, String year, boolean isInReference){
		INomenclaturalReference<?> result = null;
		
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
				result = Book.NewInstance();
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
				result =  Generic.NewInstance();
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
	
	private void makeUnparsableRefTitle(INomenclaturalReference<?> result, String reference){
		result.setTitleCache(reference);
		result.setHasProblem(true);
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
	private boolean makeYear(INomenclaturalReference<?> nomRef, String year){
		boolean result = true;
		if (year == null){
			return false;
		}
		if ("".equals(year.trim())){
			return true;
		}
		String[] years = year.split("-");
		Partial startDate = null;
		Partial endDate = null;
		try{
			if (years.length < 1){
				throw new StringNotParsableException();
			}else {
				startDate = parseSingleDate(years[0]);
				if (years.length > 1){
					endDate = parseSingleDate(years[1]);
					if (years.length > 2){
						throw new StringNotParsableException();
					}
				}
			}
		}catch(StringNotParsableException npe){
			result = false;
		}
		TimePeriod datePublished = TimePeriod.NewInstance(startDate, endDate);
		
		if (nomRef instanceof BookSection){
			((BookSection)nomRef).getInBook().setDatePublished(datePublished);
			((BookSection)nomRef).setDatePublished(datePublished);
		}else if (nomRef instanceof StrictReferenceBase){
			((StrictReferenceBase)nomRef).setDatePublished(datePublished);	
		}else if (nomRef instanceof BibtexReference){
				((BibtexReference)nomRef).setDatePublished(datePublished);
				((BibtexReference)nomRef).setYear(year);
		}else{
			throw new ClassCastException("nom Ref is not of type StrictReferenceBase but " + (nomRef == null? "(null)" : nomRef.getClass()));
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
	
	private String makeEdition(Book book, String strReference){
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
	
	private Book parseBook(String reference){
		Book result = Book.NewInstance();
		reference = makeEdition(result, reference);
		reference = makeVolume(result, reference);
		result.setTitle(reference);
		return result;
	}
	
	
	private Article parseArticle(String reference){
		//if (articlePatter)
		//(type, author, title, volume, editor, series;
		Article result = Article.NewInstance();
		reference = makeVolume(result, reference);
		Journal inJournal = Journal.NewInstance();
		inJournal.setTitle(reference);
		result.setInJournal(inJournal);
		return result;
	}
	
	private BookSection parseBookSection(String reference){
		BookSection result = BookSection.NewInstance();
		String[] parts = reference.split(referenceAuthorSeparator, 2);
		if (parts.length != 2){
			logger.warn("Unexpected number of parts");
			result.setTitleCache(reference);
		}else{
			String authorString = parts[0];
			String bookString = parts[1];
			
			TeamOrPersonBase<?> authorTeam = author(authorString);
			Book inBook = parseBook(bookString);
			inBook.setAuthorTeam(authorTeam);
			result.setInBook(inBook);
		}
		return result;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.ITaxonNameParser#parseSubGenericFullName(java.lang.String)
	 */
	public NonViralName<?> parseFullName(String fullNameString){
		return parseFullName(fullNameString, null, null);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.ITaxonNameParser#parseFullName(java.lang.String, eu.etaxonomy.cdm.model.name.Rank)
	 */
	public NonViralName<?> parseFullName(String fullNameString, NomenclaturalCode nomCode, Rank rank) {
		if (fullNameString == null){
			return null;
		}else{
			NonViralName<?> result = getNonViralNameInstance(fullNameString, nomCode, rank);
			parseFullName(result, fullNameString, rank, false);
			return result;
		}
	}
		
	
	public void parseFullName(NonViralName<?> nameToBeFilled, String fullNameString, Rank rank, boolean makeEmpty) {
		//TODO prol. etc.
		
		if (nameToBeFilled == null){
			logger.warn("name is null!");
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
				if (rank != null && (rank.isSupraGeneric()|| rank.isGenus())){
					nameToBeFilled.setRank(rank);
					nameToBeFilled.setGenusOrUninomial(epi[0]);
				} 
				//genus
				else {
					rank = null;
					nameToBeFilled.setRank(rank);
					nameToBeFilled.setGenusOrUninomial(epi[0]);
					nameToBeFilled.setHasProblem(true);
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
				TeamOrPersonBase<?>[] authors = new TeamOrPersonBase[4];
				Integer[] years = new Integer[4];
				try {
					Class<? extends NonViralName> clazz = nameToBeFilled.getClass();
					fullAuthors(authorString, authors, years, clazz);
				} catch (StringNotParsableException e) {
					nameToBeFilled.setHasProblem(true);
					nameToBeFilled.setTitleCache(fullNameString);
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
			nameToBeFilled.setHasProblem(true);
			nameToBeFilled.setTitleCache(fullNameString);
			logger.info("unknown rank (" + (rank == null? "null":rank) + ") or abbreviation in string " +  fullNameString);
			//return result;
			return;
		}
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
		TeamOrPersonBase<?>[] combinationAuthors = new TeamOrPersonBase[2];;
		Integer[] combinationYears = new Integer[2];
		authorsAndEx(fullAuthorString.substring(authorTeamStart), combinationAuthors, combinationYears);
		authors[0]= combinationAuthors[0] ;
		years[0] = combinationYears[0];
		authors[1]= combinationAuthors[1];
		years[1] = combinationYears[1];
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
		}else if (! teamSplitterPattern.matcher(authorString).find()){
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

	
	private void makeEmpty(NonViralName<?> nameToBeFilled){
		nameToBeFilled.setRank(null);
		nameToBeFilled.setTitleCache(null, false);
		nameToBeFilled.setFullTitleCache(null, false);
		nameToBeFilled.setNameCache(null, false);
				
		nameToBeFilled.setAppendedPhrase(null);
		//TODO ??
		//nameToBeFilled.setBasionym(basionym);
		nameToBeFilled.setBasionymAuthorTeam(null);
		nameToBeFilled.setCombinationAuthorTeam(null);
		nameToBeFilled.setExBasionymAuthorTeam(null);
		nameToBeFilled.setExCombinationAuthorTeam(null);
		nameToBeFilled.setAuthorshipCache(null, false);
		
		
		nameToBeFilled.setHasProblem(false);
		// TODO ?
		//nameToBeFilled.setHomotypicalGroup(newHomotypicalGroup);

		
		nameToBeFilled.setGenusOrUninomial(null);
		nameToBeFilled.setInfraGenericEpithet(null);
		nameToBeFilled.setSpecificEpithet(null);
		nameToBeFilled.setInfraSpecificEpithet(null);
		
		nameToBeFilled.setNomenclaturalMicroReference(null);
		nameToBeFilled.setNomenclaturalReference(null);
		
		if (nameToBeFilled instanceof BotanicalName){
			BotanicalName botanicalName = (BotanicalName)nameToBeFilled;
			botanicalName.setAnamorphic(false);
			botanicalName.setHybridFormula(false);
			botanicalName.setMonomHybrid(false);
			botanicalName.setBinomHybrid(false);
			botanicalName.setTrinomHybrid(false);
		}
		
		if (nameToBeFilled instanceof ZoologicalName){
			ZoologicalName zoologicalName = (ZoologicalName)nameToBeFilled;
			zoologicalName.setBreed(null);
			zoologicalName.setOriginalPublicationYear(null);
		}
		
		//TODO adapt to @Version of versionable entity, throws still optimistic locking error
		//nameToBeFilled.setUpdated(Calendar.getInstance());
		// TODO nameToBeFilled.setUpdatedBy(updatedBy);		
	}
	
	
    
    //splitter
    static String epiSplitter = "(\\s+|\\(|\\))"; //( ' '+| '(' | ')' )
    static Pattern pattern = Pattern.compile(epiSplitter); 
    
    //some useful non-terminals
    static String pStart = "^";
    static String end = "$";
    static String anyEnd = ".*" + end;
    static String oWs = "\\s+"; //obligatory whitespaces
    static String fWs = "\\s*"; //facultative whitespcace
    
    static String capitalWord = "\\p{javaUpperCase}\\p{javaLowerCase}*";
    static String nonCapitalWord = "\\p{javaLowerCase}+";
    static String word = "(" + capitalWord + "|" + nonCapitalWord + ")"; //word (capital or non-capital) with no '.' at the end
    
    
    static String capitalDotWord = capitalWord + "\\.?"; //capitalWord with facultativ '.' at the end
    static String nonCapitalDotWord = nonCapitalWord + "\\.?"; //nonCapitalWord with facultativ '.' at the end
    static String dotWord = "(" + capitalWord + "|" + nonCapitalWord + ")\\.?"; //word (capital or non-capital) with facultativ '.' at the end
    static String obligateDotWord = "(" + capitalWord + "|" + nonCapitalWord + ")\\.+"; //word (capital or non-capital) with obligate '.' at the end
    
    //Words used in an epethiton for a TaxonName
    static String nonCapitalEpiWord = "[a-zï\\-]+";   //TODO solve checkin Problem with Unicode character "[a-zï¿½\\-]+";
    static String capitalEpiWord = "[A-Z]"+ nonCapitalEpiWord;
     
    
   //years
    static String month = "(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)";
    static String singleYear = "\\b" + "(?:17|18|19|20)" + "\\d{2}" + "\\b";                      // word boundary followed by either 17,18,19, or 20 (not captured) followed by 2 digits 	      
    static String yearPhrase = singleYear + "("+ fWs + "-" + fWs + singleYear + ")?" ;
    								//+ "(" + month + ")?)" ;                 // optional month
    
    //seperator
    static String yearSeperator = "\\." + oWs;
    static String detailSeparator = ":" + oWs;
    static String referenceSeparator1 = "," + oWs ;
    static String inReferenceSeparator = oWs + "in" + oWs;
    static String referenceSeperator = "(" + referenceSeparator1 +"|" + inReferenceSeparator + ")" ;
    static String referenceAuthorSeparator = ","+ oWs;
    static String volumeSeparator = oWs ; // changed from "," + fWs
    static String referenceEnd = "\\.";
     
    
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
    static String botanicBasionymAuthor = basStart + "(" + authorAndExTeam + ")" + basEnd;  // '(' and ')' is for evaluation with RE.paren(x)
    static String fullBotanicAuthorString = fWs + "(" + botanicBasionymAuthor +")?" + fWs + authorAndExTeam + fWs;
    static String facultFullBotanicAuthorString = "(" +  fullBotanicAuthorString + ")?" ; 
        
    //Zoo. Author
    //TODO does zoo author have ex-Author?
    static String zooAuthorYearSeperator = ",";
    static String zooAuthorAddidtion = fWs + zooAuthorYearSeperator + fWs + singleYear;
    static String zooAuthorTeam = authorTeam + zooAuthorAddidtion;
    static String zooBasionymAuthor = basStart + "(" + zooAuthorTeam + ")" + basEnd;
    static String fullZooAuthorString = fWs + "(" + zooBasionymAuthor +")?" + fWs + zooAuthorTeam + fWs;
    static String facultFullZooAuthorString = "(" +  fullZooAuthorString + ")?" ; 
 
    static String facultFullAuthorString2 = "(" + facultFullBotanicAuthorString + "|" + facultFullZooAuthorString + ")";
    
    static String basionymAuthor = "(" + botanicBasionymAuthor + "|" + zooBasionymAuthor+ ")";
    static String fullAuthorString = "(" + fullBotanicAuthorString + "|" + fullZooAuthorString+ ")";
    
    //details
    //TODO still very simple
    
    
    static String nr2 = "\\d{1,2}";
    static String nr4 = "\\d{1,4}";
    static String nr5 = "\\d{1,5}";
    
    
    static String pPage = nr5 + "[a-z]?";
    static String pStrNo = "n°" + fWs + "(" + nr4 + ")";
    
    static String pBracketNr = "\\[" + nr4 + "\\]";
    static String pFolBracket = "\\[fol\\." + fWs + "\\d{1,2}(-\\d{1,2})?\\]";
    
    static String pStrTab = "tab\\." + fWs + nr4 + "(" + fWs + "(B|ß|\\(\\d{1,3}\\)))?";
    static String pFig = "fig." + fWs + nr4 + "[a-z]?";
    static String pFigs = pFig + "(-" + nr4 + ")?";
    //static String pTabFig = pStrTab + "(," + fWs + pFigs + ")?";
    static String pTabFig = "(" + pStrTab + "|" + pFigs + ")";
    
    //e.g.: p455; p.455; pp455-456; pp.455-456; pp.455,456; 455, 456; pages 456-457; pages 456,567
    static String pSinglePages = "(p\\.?)?" + fWs + pPage + "(," + pTabFig +")?";
    static String pMultiPages = "(pp\\.?|pages)?" + fWs + pPage + fWs + "(-|,)" +fWs + pPage ;
    //static String pPages = pPage + "(," + fWs + "(" + pPage + "|" + pTabFig + ")" + ")?";
    static String pPages = "(" + pSinglePages +"|" + pMultiPages +")";
    
    
    static String pCouv = "couv\\." + fWs + "\\d{1,3}";
    
    static String pTabSpecial = "tab\\." + fWs + "(ad" + fWs + "\\d{1,3}|alphab)";
    static String pPageSpecial = nr4 + fWs + "(in obs|, Expl\\. Tab)";
    static String pSpecialGardDict = capitalWord + oWs + "n°" + oWs + "\\d{1,2}";
    //TODO
    // static String pSpecialDetail = "(in err|in tab|sine pag|add\\. & emend|Emend|""\\d{3}"" \\[\\d{3}\\])";
 // static String pSpecialDetail = "(in err|in tab|sine pag|add\\. & emend|Emend|""\\d{3}"" \\[\\d{3}\\])";
    static String pSpecialDetail = "(in err|in tab|sine pag|add\\.)";
    
    
//    Const romI = "[Ii]{0,3}"
//    	Const romX = "[Xx]{0,3}"
//    	Const romC = "[Cc]{0,3}"
//    	Const romM = "[Mm]{0,3}"   
//    ' roman numbers
//    ' !! includes empty string: ""
//    romOne = "([Vv]?" & romI & or_ & "(IV|iv)" & or_ & "(IX|ix)" & ")"
//    romTen = "([Ll]?" & romX & or_ & "(XL|xl)" & or_ & "(XC|xc)" & ")"
//    romHun = "([Dd]?" & romC & or_ & "(CD|cd)" & or_ & "(CM|cm)" & ")"
//    romNr = "(?=[MDCLXVImdclxvi])(((" & romM & ")?" & romHun & ")?" & romTen & ")?" & romOne
    static String pRomNr = "ljfweffaflas"; //TODO rom number have to be tested first
    
    static String pDetailAlternatives = "(" + pPages + "|" + pPageSpecial + "|" + pStrNo + "|" + pBracketNr +
    			"|" + pTabFig + "|" + pTabSpecial + "|" + pFolBracket + "|" + pCouv + "|" + pRomNr + "|" + 
    			pSpecialGardDict + "|" + pSpecialDetail + ")";

    static String detail = pDetailAlternatives;
    
    //reference
    static String volume = nr4 + "(\\("+ nr4  + "\\))?"; 	      
    static String anySepChar = "(," + fWs + ")";
    
    static int authorSeparatorMaxPosition = 4;
    static String pTitleWordSeparator = "(\\."+ fWs+"|" + oWs + ")";
    static String referenceTitleFirstPart = "(" + word + pTitleWordSeparator + ")";
    static String referenceTitle = referenceTitleFirstPart + "*" + dotWord;
    static String referenceTitleWithSepCharacters = "(" + referenceTitle  + anySepChar + "?)" + "{1,}";
    static String referenceTitleWithoutAuthor = "(" + referenceTitleFirstPart + ")" + "{"+ (authorSeparatorMaxPosition -1) +",}" + dotWord + 
    			anySepChar + referenceTitleWithSepCharacters;   //separators exist and first separator appears at position authorSeparatorMaxPosition or later
   
    static String editionSeparator = oWs + "ed\\.?" + oWs;
    static String pEdition = nr2;
    
    static String pVolPart = volumeSeparator +  volume;
    static String pEditionPart = editionSeparator +  pEdition;
    static String pEditionVolPart = editionSeparator +  pEdition + fWs + "," + volumeSeparator +  volume;
    static String pEditionVolAlternative = "(" + pEditionPart + "|" + pVolPart + "|" + pEditionVolPart + ")?";
    
    static String pVolRefTitle = referenceTitle + "(" + pVolPart + ")?";
    static String softEditionVolRefTitle = referenceTitleWithSepCharacters + pEditionVolAlternative;
    static String softVolNoAuthorRefTitle = referenceTitleWithoutAuthor + "(" + volumeSeparator +  volume + ")?";
    
    static String pBookReference = softEditionVolRefTitle;
    static String pBookSectionReference = authorTeam + referenceAuthorSeparator + softEditionVolRefTitle;
    static String pArticleReference = pVolRefTitle  ; 
    static String pSoftArticleReference = softVolNoAuthorRefTitle  ; 
    
    
    static String pReferenceSineDetail = "(" + pArticleReference + "|" + pBookSectionReference + "|" + pBookReference + ")"; 
    
    static String pReference = pReferenceSineDetail + detailSeparator + detail + 
					yearSeperator + yearPhrase + "(" + referenceEnd + ")?"; 

    //static String strictBook = referenc 
    
    
    
    static Pattern referencePattern = Pattern.compile(pReference);
    static Pattern referenceSineDetailPattern = Pattern.compile(pReferenceSineDetail);
    
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
    static String autonym = capitalEpiWord + oWs + "(" + nonCapitalEpiWord +")" + oWs + fullBotanicAuthorString +  oWs + infraSpeciesMarker + oWs + "\\1";  //2-nd word and last word are the same 

    static String anyBotanicName = "(" + genusOrSupraGenus + "|" + infraGenus + "|" + aggrOrGroup + "|" + species + "|" + 
					infraSpecies + "|" + infraSpecies + "|" + oldInfraSpecies + "|" + autonym   + ")+";
    static String anyZooName = "(" + genusOrSupraGenus + "|" + infraGenus + "|" + aggrOrGroup + "|" + species + "|" + 
					infraSpecies + "|" + infraSpecies + "|" + oldInfraSpecies + ")+";
    static String anyBotanicFullName = anyBotanicName + oWs + fullBotanicAuthorString ;
    static String anyZooFullName = anyZooName + oWs + fullZooAuthorString ;
    static String anyFullName = "(" + anyBotanicFullName + "|" + anyZooFullName + ")";
    
    //Pattern
    static Pattern oWsPattern = Pattern.compile(oWs);
    static Pattern teamSplitterPattern = Pattern.compile(teamSplitter);
    static Pattern cultivarPattern = Pattern.compile(cultivar);
    static Pattern cultivarMarkerPattern = Pattern.compile(cultivarMarker);
    static Pattern hybridPattern = Pattern.compile(hybrid); 
    
    static Pattern genusOrSupraGenusPattern = Pattern.compile(pStart + genusOrSupraGenus + facultFullAuthorString2 + end);
    static Pattern infraGenusPattern = Pattern.compile(pStart + infraGenus + facultFullAuthorString2 + end);
    static Pattern aggrOrGroupPattern = Pattern.compile(pStart + aggrOrGroup + fWs + end); //aggr. or group has no author string
    static Pattern speciesPattern = Pattern.compile(pStart + species + facultFullAuthorString2 + end);
    static Pattern infraSpeciesPattern = Pattern.compile(pStart + infraSpecies + facultFullAuthorString2 + end);
    static Pattern oldInfraSpeciesPattern = Pattern.compile(pStart + oldInfraSpecies + facultFullAuthorString2 + end);
    static Pattern autonymPattern = Pattern.compile(pStart + autonym + fWs + end);
	
    static Pattern botanicBasionymPattern = Pattern.compile(botanicBasionymAuthor);
    static Pattern zooBasionymPattern = Pattern.compile(zooBasionymAuthor);
    static Pattern basionymPattern = Pattern.compile(basionymAuthor);
    
    static Pattern zooAuthorPattern = Pattern.compile(zooAuthorTeam);
    static Pattern zooAuthorAddidtionPattern = Pattern.compile(zooAuthorAddidtion);
    
    static Pattern exAuthorPattern = Pattern.compile(oWs + exString);
    
    static Pattern fullBotanicAuthorStringPattern = Pattern.compile(fullBotanicAuthorString);
    static Pattern fullZooAuthorStringPattern = Pattern.compile(fullZooAuthorString);
    static Pattern fullAuthorStringPattern = Pattern.compile(fullAuthorString);
    
    static Pattern anyBotanicFullNamePattern = Pattern.compile(anyBotanicFullName);
    static Pattern anyZooFullNamePattern = Pattern.compile(anyZooFullName);
    
    
}
