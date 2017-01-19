/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.parser;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.Language;

/**
 * @author a.mueller
 * @created 04.09.2009
 * @version 1.0
 */
public enum ParserProblem {
	CheckRank(WARNING()),
	CheckDetailOrYear(WARNING()),
	NameReferenceSeparation(ERROR()),
	UnparsableReferenceTitle(ERROR()),
	UnparsableNamePart(ERROR()),
	UnparsableAuthorPart(ERROR()),
	OldInfraSpeciesNotSupported(ERROR()),
	RankNotSupported(ERROR()),
	NewCombinationHasPublication(WARNING()),
	;
	
	//logger.warn("ICNCP parsing not yet implemented");
	//logger.warn("ICNB not yet implemented");
	//logger.error("Viral name is not a NonViralName !!");
	//logger.error("Unknown Nomenclatural Code !!");
	//logger.warn("nameToBeFilled class not supported ("+nameToBeFilled.getClass()+")");
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ParserProblem.class);
	
	private final static int WARNING(){return 0;};
	private final static int ERROR() {return 1;};
	
	int type;  
	
	private ParserProblem(int type){
		this.type = type;
	}
	
	public String getMessage(){
		return getMessage(Language.DEFAULT());
	}
	
	public String getMessage(Language language){
		//TODO language not yet supported
		if (this == CheckRank){
			return "check rank";
		}else if (this == CheckDetailOrYear){
			return "detail or year part ambiguous";
		}else if (this == NameReferenceSeparation){
			return "name or authorship not parsable or name-reference separation not possible";
		}else if (this == UnparsableReferenceTitle){
			return "reference title not parsable";
		}else if (this == UnparsableAuthorPart){
			return "author part not parsable";
		}else if (this == UnparsableNamePart){
			return "name part not parsable";
		}else if (this == OldInfraSpeciesNotSupported){
			return "name not parsable - old infraspecific marker not supported by parser";
		}else if (this == RankNotSupported){
			return "rank not supported by parser";
		}else if (this == NewCombinationHasPublication){
			return "zool. new combination should not have a nomencl. reference";
		}else{
			return "unknown parser problem";
		}
	}
	
	
	public boolean isError(){
		return type == ERROR();
	}

	public boolean isWarning(){
		return type == WARNING();
	}

	
	public static List<ParserProblem> warningList(int problem){
		List<ParserProblem> result = new ArrayList<ParserProblem>();
		ParserProblem[] values = ParserProblem.values();
		for (ParserProblem warning: values){
			if (testBit(problem, warning.ordinal())){
				result.add(warning);
			}
		}
		return result;
	}
	
	public static boolean hasError(int problem){
		List<ParserProblem> list = warningList(problem);
		for (ParserProblem warning : list){
			if (warning.isError()){
				return true;
			}
		}
		return false;
	}


	/**
	 * @param number
	 * @param pos
	 * @return
	 */
	private static boolean testBit(int number, int pos) {
		return  (number & 1<<pos)!=0;
	}
	/**
	 * @param hasProblem
	 * @param warning
	 * @return
	 */
	public static int addProblem(int originalProblems, ParserProblem newProblem) {
		if (newProblem == null){
			return originalProblems;
		}else{
			return originalProblems | 1 << newProblem.ordinal();
		}
	}
	
	public static int addProblems(int hasProblem, int newProblems) {
		return hasProblem | newProblems;
	}

	/**
	 * @param parsingProblem
	 * @param problemToRemove
	 * @return
	 */
	public static int removeProblem(int originalProblems, ParserProblem problemToRemove) {
		if (problemToRemove == null){
			return originalProblems;
		}else{
			return originalProblems & ~(1 << problemToRemove.ordinal());
		}
	}	

	
	
}
