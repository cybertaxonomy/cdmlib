// $Id$
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
	public static int addWarning(int originalWarnings, ParserProblem newWarning) {
		return originalWarnings | 1 << newWarning.ordinal();
	}
	
	public static int addWarnings(int hasProblem, int newWarnings) {
		return hasProblem | newWarnings;
	}
	
//	
//	private static BitSet bitSetFromInt(int number){
//		BitSet result = new BitSet(32);
//		for (int i = 0; i < 32; i++){
//			if (testBit(number, i)){
//				result.set(i);
//			}
//		}
//		return result;
//	}
	
}
