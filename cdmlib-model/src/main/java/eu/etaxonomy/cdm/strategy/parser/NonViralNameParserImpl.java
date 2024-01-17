/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.parser;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Partial;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IParsable;
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.IZoologicalName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.IBookSection;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.ISeriesPart;
import eu.etaxonomy.cdm.model.reference.IVolumeReference;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.strategy.exceptions.StringNotParsableException;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;


/**
 * Parser for {@link TaxonName}s of type NonViralName and below.
 *
 * @author a.mueller
 */
public class NonViralNameParserImpl
            extends NonViralNameParserImplRegExBase
            implements INonViralNameParser<INonViralName> {

    private static final Logger logger = LogManager.getLogger();

	// good intro: http://java.sun.com/docs/books/tutorial/essential/regex/index.html

	final static boolean MAKE_EMPTY = true;
	final static boolean MAKE_NOT_EMPTY = false;

	private final boolean authorIsAlwaysTeam = false;
	private boolean removeSpaceAfterDot = false;

    public static NonViralNameParserImpl NewInstance(){
		return new NonViralNameParserImpl();
	}

	@Override
    public INonViralName parseSimpleName(String simpleName){
		return parseSimpleName(simpleName, null, null);
	}

	@Override
    public INonViralName parseSimpleName(String simpleName, NomenclaturalCode code, Rank rank){
		//"parseSimpleName() not yet implemented. Uses parseFullName() instead");
		return parseFullName(simpleName, code, rank);
	}

	public void parseSimpleName(INonViralName nameToBeFilled, String simpleNameString, Rank rank, boolean makeEmpty){
		//"parseSimpleName() not yet implemented. Uses parseFullName() instead");
		parseFullName(nameToBeFilled, simpleNameString, rank, makeEmpty);
	}

	public INonViralName getNonViralNameInstance(String fullString, NomenclaturalCode code){
		return getNonViralNameInstance(fullString, code, null);
	}

	public INonViralName getNonViralNameInstance(String fullString, NomenclaturalCode code, Rank rank){
		INonViralName result = null;
		if(code == null) {
			boolean isBotanicalName = anyBotanicFullNamePattern.matcher(fullString).find();
			boolean isZoologicalName = anyZooFullNamePattern.matcher(fullString).find();
			boolean isBacteriologicalName = false;
			boolean isCultivatedPlantName = anyCultivarNameUnorderedPattern.matcher(fullString).find()
			        && anyCultivarNamePattern.matcher(fullString).find() ;
			if ( (isBotanicalName || isCultivatedPlantName) && ! isZoologicalName && !isBacteriologicalName){
				if (isCultivatedPlantName){
				    result = TaxonNameFactory.NewCultivarInstance(rank);
				}else{
				    result = TaxonNameFactory.NewBotanicalInstance(rank);
				}
			}else if ( isZoologicalName /*&& ! isBotanicalName*/ && !isBacteriologicalName && !isCultivatedPlantName){
				result = TaxonNameFactory.NewZoologicalInstance(rank);
			}else if ( isZoologicalName && ! isBotanicalName && !isBacteriologicalName && !isCultivatedPlantName){
				result = TaxonNameFactory.NewBacterialInstance(rank);
			}else {
				result =  TaxonNameFactory.NewNonViralInstance(rank);
			}
		} else {
			switch (code) {
			case ICNAFP:
				result = TaxonNameFactory.NewBotanicalInstance(rank);
				break;
			case ICZN:
				result = TaxonNameFactory.NewZoologicalInstance(rank);
				break;
			case Fungi:
				result = TaxonNameFactory.NewFungusInstance(rank);
				break;
			case ICNCP:
				logger.warn("ICNCP parsing not yet implemented");
				result = TaxonNameFactory.NewCultivarInstance(rank);
				break;
			case ICNP:
				logger.warn("ICNP not yet implemented");
				result = TaxonNameFactory.NewBacterialInstance(rank);
				break;
			case ICVCN:
				logger.error("Viral name is not a NonViralName !!");
				break;
			case NonViral:
				logger.warn("NonViral parsing not yet implemented");
				result = TaxonNameFactory.NewNonViralInstance(rank);
				break;
			default:
				// FIXME Unreachable code
				logger.error("Unknown Nomenclatural Code !!");
			}
		}
		return result;
	}

	@Override
    public TaxonName parseReferencedName(String fullReferenceString) {
		return parseReferencedName(fullReferenceString, null, null);
	}

	@Override
    public TaxonName parseReferencedName(String fullReferenceString, NomenclaturalCode nomCode, Rank rank) {
		if (fullReferenceString == null){
			return null;
		}else{
		    INonViralName result = getNonViralNameInstance(fullReferenceString, nomCode, rank);
			parseReferencedName(result, fullReferenceString, rank, MAKE_EMPTY);
			return TaxonName.castAndDeproxy(result);
		}
	}

	private String standardize(INonViralName nameToBeFilled, String fullReferenceString, boolean makeEmpty){
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
	 * Returns the regular expression to be used for the full-name depending on the code
	 */
	private String getCodeSpecificFullNameRegEx(INonViralName nameToBeFilledOrig){
	    INonViralName nameToBeFilled = CdmBase.deproxy(nameToBeFilledOrig);
		if (nameToBeFilled.isZoological()){
			return anyZooFullName;
		}else if (nameToBeFilled.isCultivar()) {
            return anyCultivarFullName;
        }else if (nameToBeFilled.isBotanical()) {
			return anyBotanicFullName;
		}else if (nameToBeFilled.isNonViral()) {
			return anyBotanicFullName;  //TODO ?
		}else{
			logger.warn("nameToBeFilled class not supported ("+nameToBeFilled.getClass()+")");
			return null;
		}
	}

	/**
	 * Returns the regular expression to be used for the simple-name depending on the code
	 */
	private String getCodeSpecificSimpleNameRegEx(INonViralName nameToBeFilled){
		nameToBeFilled = CdmBase.deproxy(nameToBeFilled);

		if (nameToBeFilled.isZoological()){
			return anyZooName;
		}else if (nameToBeFilled.isCultivar()) {
            return anyCultivarName;
        }else if (nameToBeFilled.isBotanical()) {
		    return anyBotanicName;
		}else if (nameToBeFilled.isNonViral()){
			return anyZooName;  //TODO ?
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

	@Override
    public void parseReferencedName(INonViralName nameToBeFilled, String fullReferenceStringOrig, Rank rank, boolean makeEmpty) {
		//standardize
		String fullReferenceString = standardize(nameToBeFilled, fullReferenceStringOrig, makeEmpty);
		if (fullReferenceString == null){
			return;
		}
		// happens already in standardize(...)
//		makeProblemEmpty(nameToBeFilled);

		//make nomenclatural status and replace it by empty string
	    fullReferenceString = parseNomStatus(fullReferenceString, nameToBeFilled, makeEmpty);
	    nameToBeFilled.setProblemEnds(fullReferenceString.length());

	    fullReferenceString = parseOriginalSpelling(fullReferenceString, nameToBeFilled, makeEmpty);
        nameToBeFilled.setProblemEnds(fullReferenceString.length());

	    //get full name regEx
		String localFullNameRegEx = getCodeSpecificFullNameRegEx(nameToBeFilled);
		//get simple name regEx
		String localSimpleNameRegEx = getCodeSpecificSimpleNameRegEx(nameToBeFilled);

		//separate name and reference part
		String nameAndRefSeparatorRegEx = "(^" + localFullNameRegEx + ")("+ referenceSeperator + ")";
		Matcher nameAndRefSeparatorMatcher = getMatcher(nameAndRefSeparatorRegEx, fullReferenceString);

		Matcher onlyNameMatcher = getMatcher(localFullNameRegEx, fullReferenceString);
		Matcher hybridMatcher = hybridFormulaPattern.matcher(fullReferenceString);
		Matcher onlySimpleNameMatcher = getMatcher(localSimpleNameRegEx, fullReferenceString);

		if (onlyNameMatcher.matches()){
			makeEmpty = false;
			parseFullName(nameToBeFilled, fullReferenceString, rank, makeEmpty);
		}else if (nameAndRefSeparatorMatcher.find()){
			makeNameWithReference(nameToBeFilled, fullReferenceString, nameAndRefSeparatorMatcher, rank, makeEmpty);
		}else if (hybridMatcher.matches() ){
		    //I do not remember why we need makeEmpty = false for onlyNameMatcher,
		    //but for hybridMatcher we need to remove old Hybrid Relationships if necessary, therefore
		    //I removed it from here
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

	private void makeNoFullRefMatch(INonViralName nameToBeFilled, String fullReferenceString, Rank rank){
	    //try to parse first part as name, but keep in mind full string is not parsable
		int start = 0;

		String localFullName = getCodeSpecificFullNameRegEx(nameToBeFilled);
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
		nameToBeFilled.setTitleCache(fullReferenceString, true);
		nameToBeFilled.setFullTitleCache(fullReferenceString, true);
		// FIXME Quick fix, otherwise search would not deliver results for unparsable names
		nameToBeFilled.setNameCache(fullReferenceString, true);
		// END
		nameToBeFilled.setProblemStarts(start);
		nameToBeFilled.setProblemEnds(fullReferenceString.length());
		logger.info("no applicable parsing rule could be found for \"" + fullReferenceString + "\"");
	}

	private void makeNameWithReference(INonViralName nameToBeFilled,
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

	    //original spelling
        handleOriginalSpelling(nameToBeFilled);

		//zoological new combinations should not have a nom. reference to be parsed
	    if (nameToBeFilled.isZoological()){
			IZoologicalName zooName = (IZoologicalName)CdmBase.deproxy(nameToBeFilled);
			//is name new combination?
			if (zooName.getBasionymAuthorship() != null || zooName.getOriginalPublicationYear() != null){
				ParserProblem parserProblem = ParserProblem.NewCombinationHasPublication;
				zooName.addParsingProblem(parserProblem);
				nameToBeFilled.setProblemStarts((nameToBeFilled.getProblemStarts()> -1) ? nameToBeFilled.getProblemStarts(): name.length());
				nameToBeFilled.setProblemEnds(Math.max(fullReferenceString.length(), nameToBeFilled.getProblemEnds()));
			}
		}

	    parseReference(nameToBeFilled, referenceString, isInReference);
	    INomenclaturalReference ref = nameToBeFilled.getNomenclaturalReference();

	    //problem start
	    int start = nameToBeFilled.getProblemStarts();
	    int nameAndSeparatorLength = nameAndSeparator.length();

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

	    Reference nomRef;
		if ( (nomRef = nameToBeFilled.getNomenclaturalReference()) != null ){
			nomRef.setAuthorship(nameToBeFilled.getCombinationAuthorship());
		}
	}

	/**
     * Tries to create the best name (without authors) that makes an original spelling name for the nameToBeFilled.
     */
    private void handleOriginalSpelling(INonViralName nameToBeFilled) {
        if (nameToBeFilled.getNomenclaturalSource()== null || CdmUtils.isBlank(nameToBeFilled.getNomenclaturalSource().getOriginalInfo())){
            return;
        }
        String originalInfo = nameToBeFilled.getNomenclaturalSource().getOriginalInfo();

        final int n = 5;
        String nameSplit[] = new String[n];
        nameSplit[0] = nameToBeFilled.getGenusOrUninomial();
        nameSplit[1] = nameToBeFilled.getInfraGenericEpithet();
        nameSplit[2] = nameToBeFilled.getSpecificEpithet();
        nameSplit[3] = nameToBeFilled.getRank()== null? null : nameToBeFilled.getRank().getAbbreviation();
        nameSplit[4] = nameToBeFilled.getInfraSpecificEpithet();
        String originalNameSplit[] = originalInfo.split(" ");

        int bestOffset = -1;
        int bestDiff = Integer.MAX_VALUE;
        for (int off=0; off <= n - originalNameSplit.length; off++){
            int diff = 0;
            int off2 = 0;
            for (int i=0+off;i<n && i-off-off2 < originalNameSplit.length; i++){
                String namePart = CdmUtils.Nz(nameSplit[i]);
                String originalPart = originalNameSplit[i-off-off2];
                if (isBlank(namePart)){
                    originalPart = null;
                    off2++;
                }
                if (originalPart != null){
                    int newDiff = StringUtils.getLevenshteinDistance(namePart, originalPart);
                    diff += newDiff;
                }
            }
            if (diff>0 && diff<=bestDiff){
                bestOffset = off;
                bestDiff = diff;
            }
        }
        if (bestDiff == 0){
            return;
        }

        String[] finalSplit = new String[n];
        for (int i = 0; i < n; i++){
            if (isBlank(nameSplit[i])){
                if (bestOffset<i){
                    bestOffset++;
                }
                continue;
            }
            int oi = i-bestOffset;
            finalSplit[i] = (oi)<0 || (oi>= originalNameSplit.length)? nameSplit[i]: originalNameSplit[oi];
        }
        TaxonName originalName = TaxonNameFactory.NewNameInstance(nameToBeFilled.getNameType(), nameToBeFilled.getRank(), nameToBeFilled.getHomotypicalGroup());
        originalName.setGenusOrUninomial(finalSplit[0]);
        originalName.setInfraSpecificEpithet(finalSplit[1]);
        originalName.setSpecificEpithet(finalSplit[2]);
//        originalName.setRank(finalSplit[3]);
        originalName.setInfraSpecificEpithet(finalSplit[4]);

        nameToBeFilled.setOriginalSpelling(originalName);
        nameToBeFilled.setOriginalInfo(null);
        return;
    }

    //TODO make it an Array of status
	/**
	 * Extracts a {@link NomenclaturalStatus} from the reference String and adds it to the @link {@link TaxonName}.
	 * The nomenclatural status part is deleted from the reference String.
	 * @return  String the new (shortend) reference String
	 */
	public String parseNomStatus(String fullString, INonViralName nameToBeFilled, boolean makeEmpty) {
		Set<NomenclaturalStatusType> existingStatusTypeSet = new HashSet<>();
		Set<NomenclaturalStatusType> newStatusTypeSet = new HashSet<>();
		for (NomenclaturalStatus existingStatus : nameToBeFilled.getStatus()){
			existingStatusTypeSet.add(existingStatus.getType());
		}

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
			    TaxonName nameToBeFilledCasted =  TaxonName.castAndDeproxy(nameToBeFilled);
				NomenclaturalStatusType nomStatusType = NomenclaturalStatusType.getNomenclaturalStatusTypeByAbbreviation(statusString, nameToBeFilledCasted);
				if (! existingStatusTypeSet.contains(nomStatusType)){
					NomenclaturalStatus nomStatus = NomenclaturalStatus.NewInstance(nomStatusType);
					nameToBeFilled.addStatus(nomStatus);
				}
				newStatusTypeSet.add(nomStatusType);
				fullString = fullString.replace(statusPhrase, "");
			} catch (UnknownCdmTypeException e) {
				//Do nothing
			}
		}
		//remove not existing nom status
		if (makeEmpty){
			Set<NomenclaturalStatus> tmpStatus = new HashSet<>();
			tmpStatus.addAll(nameToBeFilled.getStatus());
			for (NomenclaturalStatus status : tmpStatus){
				if (! newStatusTypeSet.contains(status.getType())){
					nameToBeFilled.removeStatus(status);
				}
			}
		}

		return fullString;
	}

	/**
     * Extracts the original spelling string and adds it to the original source as <code>originalInfo</code>.
     * The original spelling part is deleted from the reference String.
     * @return  String the new (shortend) reference String
     */
    public String parseOriginalSpelling(String fullString, INonViralName nameToBeFilled, boolean makeEmpty) {

        Pattern hasOrginalSpellingPattern = Pattern.compile("(" + pOriginalSpelling + ")");
        Matcher hasOrginalSpellingMatcher = hasOrginalSpellingPattern.matcher(fullString);

        if (hasOrginalSpellingMatcher.find()) {
            String originalSpellingPhrase = hasOrginalSpellingMatcher.group(0);
            String originalSpellingStr = originalSpellingPhrase.substring(originalSpellingStart.length());
            originalSpellingStr = originalSpellingStr.substring(0, originalSpellingStr.length() - originalSpellingEnd.length()+1);
            nameToBeFilled.setOriginalInfo(originalSpellingStr);
            fullString = fullString.replace(originalSpellingPhrase, "").trim();
        }

        if (makeEmpty){
            nameToBeFilled.setOriginalSpelling(null);
        }

        return fullString;
    }


	private void parseReference(INonViralName nameToBeFilled, String strReference, boolean isInReference){

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
			if (nameToBeFilled.isZoological()){
				IZoologicalName zooName = (IZoologicalName)CdmBase.deproxy(nameToBeFilled);
				yearPart = String.valueOf(zooName.getPublicationYear());
				//continue
			}else{
				ref = makeDetailYearUnparsable(nameToBeFilled,strReference);
				ref.setDatePublished(TimePeriodParser.parseStringVerbatim(yearPart));
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
		    //we need to protect both caches otherwise the titleCache is incorrectly build from atomized parts
			ref.setTitleCache( (isInReference ? "in ":"") +  originalStrReference, true);
			ref.setAbbrevTitleCache( (isInReference ? "in ":"") +  originalStrReference, true);
		}
		nameToBeFilled.setNomenclaturalReference(ref);
		int end = Math.min(strReference.length(), ref.getProblemEnds());
		ref.setProblemEnds(end);
	}

	private Reference makeDetailYearUnparsable(INonViralName nameToBeFilled, String strReference) {
		Reference ref;

		ref = ReferenceFactory.newGeneric();
		ref.setTitleCache(strReference, true);
        ref.setAbbrevTitleCache(strReference, true);
		ref.setProblemEnds(strReference.length());
		ref.addParsingProblem(ParserProblem.CheckDetailOrYear);
		nameToBeFilled.addParsingProblem(ParserProblem.CheckDetailOrYear);
		nameToBeFilled.setNomenclaturalReference(ref);
		return ref;
	}

	/**
	 * Parses the referenceTitlePart, including the author volume and edition.
	 */
	public INomenclaturalReference parseReferenceTitle(String strReference, String year, boolean isInReference){
		IBook result = null;

		Matcher refSineDetailMatcher = referenceSineDetailPattern.matcher(strReference);
		if (! refSineDetailMatcher.matches()){
			//TODO ?
		}

		Matcher articleMatcher = getMatcher(pArticleReference, strReference);
		Matcher bookMatcher = getMatcher(pBookReference, strReference);

		Matcher softArticleMatcher = getMatcher(pSoftArticleReference, strReference);
		Matcher bookSectionMatcher = getMatcher(pBookSectionReference, strReference);


		if(isInReference == false){
			if (bookMatcher.matches() ){
				result = parseBook(strReference);
			}else{
				logger.info("Non-InRef must be book but does not match book: "+ strReference);
				result = ReferenceFactory.newBook();
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
				result =  ReferenceFactory.newGeneric();
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
	    //need to set both to protected otherwise titleCache is created from atomized parts
	    result.setTitleCache(reference, true);
		result.setAbbrevTitleCache(reference, true);
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
		VerbatimTimePeriod datePublished = TimePeriodParser.parseStringVerbatim(year);

		if (nomRef.getType().equals(ReferenceType.BookSection)){
			handleBookSectionYear((IBookSection)nomRef, datePublished);
		}else if (nomRef instanceof Reference){
			((Reference)nomRef).setDatePublished(datePublished);
		}else{
			throw new ClassCastException("nom Ref is not of type Reference but " + (nomRef.getClass()));
		}
		return result;
	}

	private String makeVolumeAndSeries(IVolumeReference nomRef, String strReference){
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
		strReference = parseSeries(strReference, (ISeriesPart)nomRef);
		return strReference;
	}

    private String parseSeries(String strReference, ISeriesPart nomRef) {
        String seriesPart = null;
        String seriesPhrase = pSeriesPart + end;
        Matcher seriesPhraseMatcher = getMatcher(seriesPhrase, strReference);
        if (seriesPhraseMatcher.find()){
            seriesPart = seriesPhraseMatcher.group(0);
            strReference = strReference.substring(0, strReference.length() - seriesPart.length());
            seriesPart = seriesPart.startsWith(",")? seriesPart.substring(1): seriesPart;
            seriesPart = seriesPart.endsWith(",")? seriesPart.substring(0, seriesPart.length()-1): seriesPart;
            nomRef.setSeriesPart(seriesPart.trim());
        }
        return strReference;
    }

    private String makeEdition(IBook book, String strReference){

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
			if (editionPart.startsWith(",")){
			    //, jubilee ed. case
			    editionPart = editionPart.substring(1).trim();
			}else if (!editionPart.matches(pEdition)){
			    editionPart = "ed. " + editionPart;
			}
			book.setEdition(editionPart);
		}
		return strReference;
	}

	private IBook parseBook(String reference){
		IBook result = ReferenceFactory.newBook();
		reference = makeEdition(result, reference);
		reference = makeVolumeAndSeries(result, reference);
		result.setAbbrevTitle(reference);
		return result;
	}

	private Reference parseArticle(String reference){
		//if (articlePattern)
		//(type, author, title, volume, editor, series;
		Reference result = ReferenceFactory.newArticle();
		reference = makeVolumeAndSeries(result, reference);
		Reference inJournal = ReferenceFactory.newJournal();
		if (isNotBlank(result.getSeriesPart()) && result.getSeriesPart().matches(notReallySeriesPart)) {
		    reference += ", " + result.getSeriesPart();
		    result.setSeriesPart(null);
		}
		inJournal.setAbbrevTitle(reference);
		result.setInReference(inJournal);
		return result;
	}

	private Reference parseBookSection(String reference){
		Reference result = ReferenceFactory.newBookSection();

		Pattern authorPattern = Pattern.compile("^" + authorTeam + referenceAuthorSeparator);
		Matcher authorMatcher = authorPattern.matcher(reference);
		boolean find = authorMatcher.find();
		if (find){
			String authorString = authorMatcher.group(0).trim();
			String bookString = reference.substring(authorString.length()).trim();
			authorString = authorString.substring(0, authorString.length() -1);

			TeamOrPersonBase<?> authorTeam = author(authorString);
			IBook inBook = parseBook(bookString);
			inBook.setAuthorship(authorTeam);
			result.setInBook(inBook);
		}else{
			logger.warn("Unexpected non matching book section author part");
			//TODO do we want to record a 'problem' here?
			result.setTitleCache(reference, true);
			result.setAbbrevTitleCache(reference, true);
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
	private void handleBookSectionYear(IBookSection bookSection, VerbatimTimePeriod datePublished){
		if (datePublished == null || datePublished.getStart() == null || bookSection == null){
			return;
		}
		if (datePublished.isPeriod() && bookSection.getInBook() != null){
			bookSection.getInBook().setDatePublished(datePublished);
		}else{
			bookSection.setDatePublished(datePublished);
		}
	}

	@Override
    public INonViralName parseFullName(String fullNameString){
		return parseFullName(fullNameString, null, null);
	}

	@Override
    public INonViralName parseFullName(String fullNameString, NomenclaturalCode nomCode, Rank rank) {

		if (fullNameString == null){
			return null;
		}else{
			INonViralName result = getNonViralNameInstance(fullNameString, nomCode, rank);
			parseFullName(result, fullNameString, rank, false);
			return result;
		}
	}

	@Override
	public void parseFullName(INonViralName nameToBeFilledOrig, String fullNameStringOrig, Rank rank, boolean makeEmpty) {
	    INonViralName nameToBeFilled = nameToBeFilledOrig;

	    //TODO prol. etc.
		boolean hasCheckRankProblem = false; //was rank guessed in a previous parsing process?
		if (nameToBeFilled == null){
			throw new IllegalArgumentException("NameToBeFilled must not be null in name parser");
		}else{
			hasCheckRankProblem = nameToBeFilled.hasProblem(ParserProblem.CheckRank);
			nameToBeFilled.removeParsingProblem(ParserProblem.CheckRank);
		}
		String authorString = null;
		if (fullNameStringOrig == null){
			return;
		}
		if (makeEmpty){
			makeEmpty(nameToBeFilled);
		}

		String fullNameString = fullNameStringOrig.replaceAll(oWs , " ").trim();

		fullNameString = removeHybridBlanks(fullNameString);
		fullNameString = removeSpNovBlanks(fullNameString);
		String[] epi = pattern.split(fullNameString);
		try {
	    	//cultivars //TODO 2 implement cultivars
		    if (nameToBeFilled.isCultivar() && parseCultivar(fullNameString, (TaxonName)nameToBeFilled) ){
		    	//nothing to do, already parsed in method
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
				Rank infraGenericRank;
				if ("[unranked]".equals(epi[1])||"[ranglos]".equals(epi[1])){
					infraGenericRank = Rank.INFRAGENERICTAXON();
				}else{
				    String infraGenericRankMarker = epi[1];
				    if (infraGenericRankMarker.startsWith(notho)){  //#3868
                        nameToBeFilled.setBinomHybrid(true);
                        infraGenericRankMarker = infraGenericRankMarker.substring(notho.length());
                    }else if(infraGenericRankMarker.startsWith("n")){
                        nameToBeFilled.setBinomHybrid(true);
                        infraGenericRankMarker = infraGenericRankMarker.substring(1);
                    }
                    infraGenericRank = Rank.getRankByIdInVoc(infraGenericRankMarker, nameToBeFilledOrig.getNameType());
				}
				nameToBeFilled.setRank(infraGenericRank);
				nameToBeFilled.setGenusOrUninomial(epi[0]);
				nameToBeFilled.setInfraGenericEpithet(epi[2]);
				authorString = fullNameString.substring(epi[0].length() + 1 + epi[1].length()+ 1 + epi[2].length());
			}
			 //aggr. or group
			 else if (aggrOrGroupPattern.matcher(fullNameString).matches()){
				nameToBeFilled.setRank(Rank.getRankByIdInVoc(epi[2]));
				nameToBeFilled.setGenusOrUninomial(epi[0]);
				nameToBeFilled.setSpecificEpithet(epi[1]);
			}
		     //species
			 else if (speciesPattern.matcher(fullNameString).matches()){
				nameToBeFilled.setRank(Rank.SPECIES());
				nameToBeFilled.setGenusOrUninomial(epi[0]);
				nameToBeFilled.setSpecificEpithet(normalizeSpNov(epi[1]));
				authorString = fullNameString.substring(epi[0].length() + 1 + epi[1].length());
			}
		    //species with infra generic epithet
			 else if (speciesWithInfraGenPattern.matcher(fullNameString).matches()){
			     nameToBeFilled.setRank(Rank.SPECIES());
	             nameToBeFilled.setGenusOrUninomial(epi[0]);
                 nameToBeFilled.setInfraGenericEpithet(epi[2]);
	             nameToBeFilled.setSpecificEpithet(epi[4]);
	             authorString = fullNameString.substring(epi[0].length() + 2 + epi[2].length() + 2 + epi[4].length());
			 }
			 //autonym
			 else if (autonymPattern.matcher(fullNameString.replace(UTF8.HYBRID.toString(), "")).matches()){
			    String infraSpecRankMarker = epi[epi.length - 2];
			    boolean isTriHybrid = false;
			    if (infraSpecRankMarker.startsWith(notho)){  //#3868
                    nameToBeFilled.setTrinomHybrid(true);
                    infraSpecRankMarker = infraSpecRankMarker.substring(notho.length());
                    isTriHybrid = true;
                }else if(infraSpecRankMarker.startsWith("n")){
                    nameToBeFilled.setTrinomHybrid(true);
                    infraSpecRankMarker = infraSpecRankMarker.substring(1);
                    isTriHybrid = true;
                }
			    if (epi[1].startsWith(UTF8.HYBRID.toString())) {
			        nameToBeFilled.setBinomHybrid(true);
                }
			    nameToBeFilled.setRank(Rank.getRankByIdInVoc(infraSpecRankMarker));
				nameToBeFilled.setGenusOrUninomial(epi[0]);
				nameToBeFilled.setSpecificEpithet(epi[1]);
				nameToBeFilled.setInfraSpecificEpithet(epi[epi.length - 1]);
				int lenSpecies = 2 + epi[0].length()+epi[1].length();
				int lenInfraSpecies =  2 + epi[epi.length - 2].length() + epi[epi.length - 1].length();
				authorString = fullNameString.substring(lenSpecies, fullNameString.length() - lenInfraSpecies);
			}
	        //genus autonym
            else if (genusAutonymPattern.matcher(fullNameString.replace(UTF8.HYBRID.toString(), "")).matches()){
                String infraGenRankMarker = epi[epi.length - 2];

                nameToBeFilled.setRank(Rank.getRankByIdInVoc(infraGenRankMarker));
                nameToBeFilled.setGenusOrUninomial(epi[0]);
                nameToBeFilled.setInfraGenericEpithet(epi[epi.length - 1]);
                int lenGenus = 1 + epi[0].length();
                int lenInfraGenus =  2 + epi[epi.length - 2].length() + epi[epi.length - 1].length();
                authorString = fullNameString.substring(lenGenus, fullNameString.length() - lenInfraGenus);
            }
			 //infraSpecies
			 else if (infraSpeciesPattern.matcher(fullNameString).matches()){
				String infraSpecRankMarker = epi[2];
				String infraSpecEpi = epi[3];
				if ("tax.".equals(infraSpecRankMarker)){
					infraSpecRankMarker += " " +  epi[3];
					infraSpecEpi = epi[4];
				}
				Rank infraSpecificRank;
				if ("[unranked]".equals(infraSpecRankMarker)||"[ranglos]".equals(infraSpecRankMarker)){
					infraSpecificRank = Rank.INFRASPECIFICTAXON();
				}else{
					String localInfraSpecRankMarker;
					if (infraSpecRankMarker.startsWith(notho)){  //#3868
	                    nameToBeFilled.setTrinomHybrid(true);
	                    localInfraSpecRankMarker = infraSpecRankMarker.substring(notho.length());
					}else if(infraSpecRankMarker.startsWith("n")){
	                    nameToBeFilled.setTrinomHybrid(true);
	                    localInfraSpecRankMarker = infraSpecRankMarker.substring(1);
                    }else{
                        localInfraSpecRankMarker = infraSpecRankMarker;
                    }
				    infraSpecificRank = Rank.getRankByIdInVoc(localInfraSpecRankMarker);
				}
				nameToBeFilled.setRank(infraSpecificRank);
				nameToBeFilled.setGenusOrUninomial(epi[0]);
				nameToBeFilled.setSpecificEpithet(epi[1]);
				nameToBeFilled.setInfraSpecificEpithet(infraSpecEpi);
				authorString = fullNameString.substring(epi[0].length()+ 1 + epi[1].length() +1 + infraSpecRankMarker.length() + 1 + infraSpecEpi.length());

			 }
		      //infraSpecies without marker
			 else if (zooInfraSpeciesPattern.matcher(fullNameString).matches()){
					String infraSpecEpi = epi[2];
					Rank infraSpecificRank = Rank.SUBSPECIES();
					nameToBeFilled.setRank(infraSpecificRank);
					nameToBeFilled.setGenusOrUninomial(epi[0]);
					nameToBeFilled.setSpecificEpithet(epi[1]);
					nameToBeFilled.setInfraSpecificEpithet(infraSpecEpi);
					authorString = fullNameString.substring(epi[0].length()+ 1 + epi[1].length() +1 + infraSpecEpi.length());

			 }//old infraSpecies
			 else if (oldInfraSpeciesPattern.matcher(fullNameString).matches()){
				boolean implemented = true;
				if (implemented){
				    String infraSpecRankMarker = epi[2];
	                String infraSpecEpi = epi[3];

	                Rank infraSpecificRank = Rank.getRankByLatinNameOrIdInVoc(infraSpecRankMarker);
	                nameToBeFilled.setRank(infraSpecificRank);
	                nameToBeFilled.setGenusOrUninomial(epi[0]);
	                nameToBeFilled.setSpecificEpithet(epi[1]);
	                nameToBeFilled.setInfraSpecificEpithet(infraSpecEpi);
	                authorString = fullNameString.substring(epi[0].length()+ 1 + epi[1].length() +1 + infraSpecRankMarker.length() + 1 + infraSpecEpi.length());

				}else{
					nameToBeFilled.addParsingProblem(ParserProblem.OldInfraSpeciesNotSupported);
					nameToBeFilled.setTitleCache(fullNameString, true);
					// FIXME Quick fix, otherwise search would not deliver results for unparsable names
					nameToBeFilled.setNameCache(fullNameString,true);
					// END
					logger.info("Name string " + fullNameString + " could not be parsed because UnnnamedNamePhrase is not yet implemented!");
				}
			 }
		     //hybrid formula
			 else if (hybridFormulaPattern.matcher(fullNameString).matches()){
				 Set<HybridRelationship> existingRelations = new HashSet<>();
				 Set<HybridRelationship> notToBeDeleted = new HashSet<>();

				 for ( HybridRelationship rel : nameToBeFilled.getHybridChildRelations()){
				     existingRelations.add(rel);
				 }

			     String givenNameString = "";
				 String secondNameString = "";
				 boolean isGivenName = true;
				 for (String str : epi){
					 if (str.matches(hybridSign)){
						 isGivenName = false;
					 }else if(isGivenName){
						 givenNameString += " " + str;
					 }else {
						 secondNameString += " " + str;
					 }
				 }
				 givenNameString = givenNameString.trim();
				 secondNameString = secondNameString.trim();
				 nameToBeFilled.setHybridFormula(true);
				 NomenclaturalCode code = nameToBeFilled.getNameType();
				 INonViralName firstName = this.parseFullName(givenNameString, code, rank);
				 if (secondNameString.matches(abbrevHybridSecondPart)){
				     secondNameString = extendSecondHybridPart(firstName, secondNameString);
				 }
				 INonViralName secondName = this.parseFullName(secondNameString, code, rank);
				 HybridRelationship firstRel = nameToBeFilled.addHybridParent(firstName, HybridRelationshipType.FIRST_PARENT(), null);
				 HybridRelationship second = nameToBeFilled.addHybridParent(secondName, HybridRelationshipType.SECOND_PARENT(), null);
				 checkRelationExist(firstRel, existingRelations, notToBeDeleted);
				 checkRelationExist(second, existingRelations, notToBeDeleted);

				 Rank newRank;
				 Rank firstRank = firstName.getRank();
				 Rank secondRank = secondName.getRank();

				 if (firstRank == null || (secondRank != null && firstRank.isHigher(secondRank))){
					 newRank = secondRank;
				 }else{
					 newRank = firstRank;
				 }
				 nameToBeFilled.setRank(newRank);
				 //remove not existing hybrid relation
				 if (makeEmpty){
		            Set<HybridRelationship> tmpChildRels = new HashSet<HybridRelationship>();
		            tmpChildRels.addAll(nameToBeFilled.getHybridChildRelations());
		            for (HybridRelationship rel : tmpChildRels){
		                if (! notToBeDeleted.contains(rel)){
		                    nameToBeFilled.removeHybridRelationship(rel);
		                }
		            }
				 }
			 }
		    //none
			else{
				nameToBeFilled.addParsingProblem(ParserProblem.UnparsableNamePart);
				nameToBeFilled.setTitleCache(fullNameString, true);
				// FIXME Quick fix, otherwise search would not deilver results for unparsable names
				nameToBeFilled.setNameCache(fullNameString, true);
				// END
				logger.info("no applicable parsing rule could be found for \"" + fullNameString + "\"");
		    }
		    //hybrid bits
		    handleHybridBits(nameToBeFilled);
		    if (!nameToBeFilled.isHybridFormula()){
		        Set<HybridRelationship> hybridChildRelations = new HashSet<>();
		        hybridChildRelations.addAll(nameToBeFilled.getHybridChildRelations());

		        for (HybridRelationship hybridRelationship: hybridChildRelations){
		        	nameToBeFilled.removeHybridRelationship(hybridRelationship);
		        }
		    }

			//authors
		    if (isNotBlank(authorString) ){
				handleAuthors(nameToBeFilled, fullNameString, authorString);
			}
		    handleOriginalSpelling(nameToBeFilled);
		    return;
		} catch (UnknownCdmTypeException e) {
			nameToBeFilled.addParsingProblem(ParserProblem.RankNotSupported);
			nameToBeFilled.setTitleCache(fullNameString, true);
			// FIXME Quick fix, otherwise search would not deliver results for unparsable names
			nameToBeFilled.setNameCache(fullNameString,true);
			// END
			logger.info("unknown rank (" + (rank == null? "null":rank) + ") or abbreviation in string " +  fullNameString);
			//return result;
			return;
		}
	}

    private String normalizeSpNov(String epi) {
        if (spNovPattern.matcher(epi).matches()){
            epi = epi.replace(".", ". ").replace("\\s+", " ").trim();
        }
        return epi;
    }

    private String extendSecondHybridPart(INonViralName givenName, String secondNameString) {
        //first letter of genus given
        if (secondNameString.matches("^" + abbrevHybridGenus + ".*")){
            if (isNotBlank(givenName.getGenusOrUninomial())){
                if (secondNameString.substring(0,1).equals(givenName.getGenusOrUninomial().substring(0, 1))){
                    secondNameString = secondNameString.replaceAll("^" + abbrevHybridGenus, givenName.getGenusOrUninomial() + " ");
                }
            }
        }else if (secondNameString.matches(abbrevHybridSecondPartOnlyInfraSpecies)){
            secondNameString = CdmUtils.concat(" " , givenName.getGenusOrUninomial(), givenName.getSpecificEpithet(), secondNameString);
        }else if (true){  //there will be further alternatives in future maybe
            secondNameString = CdmUtils.concat(" " , givenName.getGenusOrUninomial(), secondNameString);
        }
        return secondNameString;
    }

    /**
     * Checks if a hybrid relation exists in the Set of existing relations
     * and <BR>
     *  if it does not adds it to relations not to be deleted <BR>
     *  if it does adds the existing relations to the relations not to be deleted
     *
     * @param firstRel
     * @param existingRelations
     * @param notToBeDeleted
     */
    private void checkRelationExist(
            HybridRelationship newRelation,
            Set<HybridRelationship> existingRelations,
            Set<HybridRelationship> notToBeDeleted) {
        HybridRelationship relToKeep = newRelation;
        for (HybridRelationship existingRelation : existingRelations){
            if (existingRelation.equals(newRelation)){
                relToKeep = existingRelation;
                break;
            }
        }
        notToBeDeleted.add(relToKeep);
    }

    private void handleHybridBits(INonViralName nameToBeFilled) {
		//uninomial
		String uninomial = CdmUtils.Nz(nameToBeFilled.getGenusOrUninomial());
		boolean isUninomialHybrid = uninomial.startsWith(hybridSign);
		if (isUninomialHybrid){
			nameToBeFilled.setMonomHybrid(true);
			nameToBeFilled.setGenusOrUninomial(uninomial.replace(hybridSign, ""));
		}
		//infrageneric
		String infrageneric = CdmUtils.Nz(nameToBeFilled.getInfraGenericEpithet());
		boolean isInfraGenericHybrid = infrageneric.startsWith(hybridSign);
		if (isInfraGenericHybrid){
			nameToBeFilled.setBinomHybrid(true);
			nameToBeFilled.setInfraGenericEpithet(infrageneric.replace(hybridSign, ""));
		}
		//species Epi
		String speciesEpi = CdmUtils.Nz(nameToBeFilled.getSpecificEpithet());
		boolean isSpeciesHybrid = speciesEpi.startsWith(hybridSign);
		if (isSpeciesHybrid){
			if (isBlank(infrageneric)){
				nameToBeFilled.setBinomHybrid(true);
			}else{
				nameToBeFilled.setTrinomHybrid(true);
			}
			nameToBeFilled.setSpecificEpithet(speciesEpi.replace(hybridSign, ""));
		}
		//infra species
		String infraSpeciesEpi = CdmUtils.Nz(nameToBeFilled.getInfraSpecificEpithet());
		boolean isInfraSpeciesHybrid = infraSpeciesEpi.startsWith(hybridSign);
		if (isInfraSpeciesHybrid){
			nameToBeFilled.setTrinomHybrid(true);
			nameToBeFilled.setInfraSpecificEpithet(infraSpeciesEpi.replace(hybridSign, ""));
		}

	}

	private String removeHybridBlanks(String fullNameString) {
		String result = fullNameString
		        .replaceAll(oWs + "[xX]" + oWs + "(?=[A-Z])", " " + hybridSign + " ")
		        .replaceAll(hybridFull, " " + hybridSign).trim();
		if (result.contains(hybridSign + " ") &&
		        result.matches("^" + capitalEpiWord + oWs + hybridSign + oWs + nonCapitalEpiWord + ".*")){
		    result = result.replaceFirst(hybridSign + oWs, hybridSign);
		}
		return result;
	}


    private String removeSpNovBlanks(String fullNameString) {
        Matcher spNovMatcher = spNovPattern.matcher(fullNameString);
        if (spNovMatcher.find()){
            String spNov = spNovMatcher.group(0);
            String spNovShort = spNov.replaceAll("\\s", "");
            if (spNov.length() != spNovShort.length()){
                fullNameString = fullNameString.replace(spNov, spNovShort);
            }
        }
        return fullNameString;
    }


	/**
	 * Author parser for external use
	 * @param nonViralName
	 * @param authorString
	 * @throws StringNotParsableException
	 */
	@Override
	public void parseAuthors(INonViralName nonViralNameOrig, String authorString) throws StringNotParsableException{
	    INonViralName nonViralName = CdmBase.deproxy(nonViralNameOrig);
	    TeamOrPersonBase<?>[] authors = new TeamOrPersonBase[4];
		Integer[] years = new Integer[4];
		NomenclaturalCode code = nonViralName.getNameType();
		fullAuthors(authorString, authors, years, code);
		nonViralName.setCombinationAuthorship(authors[0]);
		nonViralName.setExCombinationAuthorship(authors[1]);
		nonViralName.setBasionymAuthorship(authors[2]);
		nonViralName.setExBasionymAuthorship(authors[3]);
		if (nonViralName.isZoological()){
			IZoologicalName zooName = (IZoologicalName)nonViralName;
			zooName.setPublicationYear(years[0]);
			zooName.setOriginalPublicationYear(years[2]);
		}
	}

	public void handleAuthors(INonViralName nameToBeFilled, String fullNameString, String authorString) {
	    TeamOrPersonBase<?>[] authors = new TeamOrPersonBase[4];
		Integer[] years = new Integer[4];
		try {
			NomenclaturalCode code = nameToBeFilled.getNameType();
			fullAuthors(authorString, authors, years, code);
		} catch (StringNotParsableException e) {
			nameToBeFilled.addParsingProblem(ParserProblem.UnparsableAuthorPart);
			nameToBeFilled.setTitleCache(fullNameString, true);
			// FIXME Quick fix, otherwise search would not deliver results for unparsable names
			nameToBeFilled.setNameCache(fullNameString, true);
			// END
			logger.info("no applicable parsing rule could be found for \"" + fullNameString + "\"");
		}
		nameToBeFilled.setCombinationAuthorship(authors[0]);
		nameToBeFilled.setExCombinationAuthorship(authors[1]);
		nameToBeFilled.setBasionymAuthorship(authors[2]);
		nameToBeFilled.setExBasionymAuthorship(authors[3]);
		if (nameToBeFilled.isZoological()){
			IZoologicalName zooName = (IZoologicalName)nameToBeFilled;
			zooName.setPublicationYear(years[0]);
			zooName.setOriginalPublicationYear(years[2]);
		}
	}

	/**
	 * Guesses the rank of uninomial depending on the typical endings for ranks
	 * @param nameToBeFilled
	 * @param string
	 */
	private Rank guessUninomialRank(INonViralName nameToBeFilled, String uninomial) {
		Rank result = Rank.GENUS();
		if (nameToBeFilled.isBotanical()){
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
		}else if (nameToBeFilled.isZoological()){
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
	protected void fullAuthors (String fullAuthorStringOrig, TeamOrPersonBase<?>[] authors,
	        Integer[] years, NomenclaturalCode code)
			throws StringNotParsableException{

	    if (fullAuthorStringOrig == null || code == null){
			return;
		}
		String fullAuthorString = fullAuthorStringOrig.trim();

		//Botanic
		if ( code.isBotanical() ){
			if (! fullBotanicAuthorStringPattern.matcher(fullAuthorString).matches() ){
				throw new StringNotParsableException("fullAuthorString (" +fullAuthorString+") not parsable: ");
			}
		}
		//Zoo
		else if ( code.isZoological() ){
			if (! fullZooAuthorStringPattern.matcher(fullAuthorString).matches() ){
				throw new StringNotParsableException("fullAuthorString (" +fullAuthorString+") not parsable: ");
			}
		}else {
			//TODO
			logger.warn ("Full author String parsable only for defined BotanicalNames or ZoologicalNames but this is " + code.getLabel());
			throw new StringNotParsableException("fullAuthorString (" +fullAuthorString+") not parsable: ");
		}
		fullAuthorsChecked(fullAuthorString, authors, years);
	}

	/*
	 * like fullTeams but without trim and match check
	 */
	protected void fullAuthorsChecked (String fullAuthorString, TeamOrPersonBase<?>[] authors, Integer[] years){
		int authorShipStart = 0;
		Matcher basionymMatcher = basionymPattern.matcher(fullAuthorString);

		if (basionymMatcher.find(0)){

			String basString = basionymMatcher.group();
			basString = basString.replaceFirst(basStart, "");
			basString = basString.replaceAll(basEnd, "").trim();
			authorShipStart = basionymMatcher.end(1);

			TeamOrPersonBase<?>[] basAuthors = new TeamOrPersonBase[2];
			Integer[] basYears = new Integer[2];
			authorsAndEx(basString, basAuthors, basYears);
			authors[2]= basAuthors[0];
			years[2] = basYears[0];
			authors[3]= basAuthors[1];
			years[3] = basYears[1];
		}
		if (fullAuthorString.length() >= authorShipStart){
			TeamOrPersonBase<?>[] combinationAuthors = new TeamOrPersonBase[2];
			Integer[] combinationYears = new Integer[2];
			authorsAndEx(fullAuthorString.substring(authorShipStart), combinationAuthors, combinationYears);
			authors[0]= combinationAuthors[0] ;
			years[0] = combinationYears[0];
			authors[1]= combinationAuthors[1];
			years[1] = combinationYears[1];
		}
	}


	/**
	 * Parses the author and ex-author String
	 * @param authorShipStringOrig String representing the author and the ex-author team
	 * @return array of Teams containing the Team[0] and the ExTeam[1]
	 */
	protected void authorsAndEx (String authorShipStringOrig, TeamOrPersonBase<?>[] authors, Integer[] years){
		//TODO noch allgemeiner am Anfang durch Replace etc.
		String authorShipString = authorShipStringOrig.trim();
		authorShipString = authorShipString.replaceFirst(oWs + "ex" + oWs, " ex. " );

		//int authorEnd = authorTeamString.length();
		int authorBegin = 0;

		//ex-author
        Matcher exAuthorMatcher = exAuthorPattern.matcher(authorShipString);
		if (exAuthorMatcher.find(0)){
			authorBegin = exAuthorMatcher.end(0);
			int exAuthorEnd = exAuthorMatcher.start(0);
			String exString = authorShipString.substring(0, exAuthorEnd).trim();
			authors [1] = author(exString);
		}
		zooOrBotanicAuthor(authorShipString.substring(authorBegin), authors, years );
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
	 * Parses an author (person or team) string and returns the Person or Team.
	 * @param authorString String representing the author
	 * @return a person or team
	 */
	public TeamOrPersonBase<?> author (String authorString){
		if (authorString == null){
			return null;
		}else if ((authorString = authorString.trim()).length() == 0){
			return null;
		}else if (! finalTeamSplitterPattern.matcher(authorString).find() && ! authorIsAlwaysTeam){
			//1 Person
			Person result = Person.NewInstance();
			authorString = normalizeNomenclaturalPersonString(authorString);
			result.setNomenclaturalTitle(authorString);
			return result;
		}else{
			return parsedTeam(authorString);
		}
	}

	/**
	 * Parses an authorString (representing a team into the single authors and add
	 * them to the return Team.
	 * @param authorString
	 * @return Team
	 */
	protected Team parsedTeam(String authorString){
		Team result = Team.NewInstance();
		String[] authors = authorString.split(notFinalTeamSplitter);
		for (int i = 0; i < authors.length; i++){
		    String author = authors[i];
		    if ("al.".equals(author.trim()) && i == authors.length - 1){  //final al. is handled as hasMoreMembers
			    result.setHasMoreMembers(true);
			}else{
			    Person person = Person.NewInstance();
			    author = normalizeNomenclaturalPersonString(author);
	            person.setNomenclaturalTitle(author);
			    result.addTeamMember(person);
			}
		}
		return result;
	}

    private String normalizeNomenclaturalPersonString(String author) {
        if (removeSpaceAfterDot){
            author = author.replaceAll("\\.\\s", ".");
        }
        return author;
    }

    // Parsing of the given full name that has been identified as a cultivar already somewhere else.
	// The ... cv. ... syntax is not covered here as it is not according the rules for naming cultivars.
	public boolean parseCultivar(String fullNameStr, TaxonName nameToBeFilled){

	    Matcher anyCultivarNameMatcher = anyCultivarNamePattern.matcher(fullNameStr);
	    Matcher anyCultivarFullNameMatcher = anyCultivarFullNamePattern.matcher(fullNameStr);

	    boolean findFull = anyCultivarFullNameMatcher.find();
	    boolean findSimple = anyCultivarNameMatcher.find();
	    boolean simpleOnly = findSimple && !findFull || anyCultivarNameMatcher.matches();
        Matcher matcher = simpleOnly ? anyCultivarNameMatcher : anyCultivarFullNameMatcher;
        boolean matches = matcher.matches();

		if (matches || anyCultivarNameMatcher.matches() ){
		    String scientificName = matcher.group(1);
		    String cultivarEpi = matcher.group("cultivar");
		    String groupEpi = matcher.group("cultivarGroup");
		    String brGroupName = matcher.group("cultivarBrGroup");
		    String grexName = matcher.group("cultivarGrex");
		    String author = simpleOnly ? null : matcher.group("cultivarAuthor");

		    //handle not exact matches
		    if (isBlank(groupEpi) && isBlank(cultivarEpi) && isBlank(brGroupName) && isBlank(grexName)){
		        //name is a botanical name only, should not happen anymore
		        return false;
		    }else if (isNotBlank(groupEpi) && isNotBlank(cultivarEpi)){
                return false;
            }else if (isNotBlank(brGroupName) && isNotBlank(grexName) && isBlank(cultivarEpi)){
                return false;
            }
		    String scientificFullName = CdmUtils.concat(" ", scientificName, author);
		    parseFullName(nameToBeFilled, scientificFullName, null, false);

            if (isNotBlank(grexName)){
                nameToBeFilled.setRank(Rank.GREX_ICNCP());
                nameToBeFilled.setCultivarGroupEpithet(grexName);
            }
            if (isNotBlank(groupEpi) || isNotBlank(brGroupName)){
                nameToBeFilled.setRank(Rank.CULTIVARGROUP());
                if (isBlank(groupEpi)){
                    groupEpi = brGroupName;
                }
                nameToBeFilled.setCultivarGroupEpithet(CdmUtils.concat(" ", grexName, groupEpi));
            }
            if (isNotBlank(cultivarEpi)){
                nameToBeFilled.setRank(Rank.CULTIVAR());
                cultivarEpi = cultivarEpi.substring(1, cultivarEpi.length()-1);
                nameToBeFilled.setCultivarEpithet(cultivarEpi);
            }
            return true;
		}else{
		    return false;
		}
	}

	private void makeEmpty(INonViralName name){
	    TaxonName nameToBeFilled = TaxonName.castAndDeproxy(name);
		nameToBeFilled.setRank(null);
		nameToBeFilled.setTitleCache(null, false);
		nameToBeFilled.setFullTitleCache(null, false);
		nameToBeFilled.setNameCache(null, false);

		nameToBeFilled.setCombinationAuthorship(null);
		nameToBeFilled.setBasionymAuthorship(null);
		nameToBeFilled.setExBasionymAuthorship(null);
		nameToBeFilled.setExCombinationAuthorship(null);
		nameToBeFilled.setAuthorshipCache(null, false);

		//delete problems except check rank
		makeProblemEmpty(nameToBeFilled);

		// TODO ?
		//nameToBeFilled.setHomotypicalGroup(newHomotypicalGroup);

		nameToBeFilled.setGenusOrUninomial(null);
		nameToBeFilled.setInfraGenericEpithet(null);
		nameToBeFilled.setSpecificEpithet(null);
		nameToBeFilled.setInfraSpecificEpithet(null);
		nameToBeFilled.setCultivarEpithet(null);
		nameToBeFilled.setCultivarGroupEpithet(null);
		nameToBeFilled.setAppendedPhrase(null);

		nameToBeFilled.setNomenclaturalMicroReference(null);
		nameToBeFilled.setNomenclaturalReference(null);

		nameToBeFilled.setHybridFormula(false);
		nameToBeFilled.setMonomHybrid(false);
		nameToBeFilled.setBinomHybrid(false);
		nameToBeFilled.setTrinomHybrid(false);

		nameToBeFilled.setAnamorphic(false);

		nameToBeFilled.setBreed(null);
		nameToBeFilled.setOriginalPublicationYear(null);
		nameToBeFilled.setPublicationYear(null);

		//nom status handled in nom status parser, otherwise we loose additional information like reference etc.
		//hybrid relationships handled in hybrid formula and at end of fullNameParser
	}

    /**
     * If <code>true</code> author names are parsed such that spaces after the abbreviated
     * givenname are removed (IPNI style). see #7094
     */
    public boolean isRemoveSpaceAfterDot() {
        return removeSpaceAfterDot;
    }
    /**
     * @see #isRemoveSpaceAfterDot()
     */
    public void setRemoveSpaceAfterDot(boolean removeSpaceAfterDot) {
        this.removeSpaceAfterDot = removeSpaceAfterDot;
    }

    private boolean isNotBlank(String str){
        return StringUtils.isNotBlank(str);
    }

    private boolean isBlank(String str){
        return StringUtils.isBlank(str);
    }
}
