/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import java.util.List;

import eu.etaxonomy.cdm.strategy.parser.ParserProblem;


/**
 * @author a.mueller
 * @since 21.05.2008
 * @version 1.0
 */
public interface IParsable {

	/**
	 * Returns an integer value indicating whether the used parser 
	 * method was able to parse a string successfully into this object (<code>0</code>)
	 * or not (<code>!=0</code>). The the parsing was not successful the value returned 
	 * defines in more detail what the problem was. The definition of these values depends
	 * on the parser that has been used for parsing.
	 *  
	 * @return  the int value parsingProblem
	 */
	public int getParsingProblem();
	
	/**
	 * @see  #getParsingProblem()
	 */
	public void setParsingProblem(int hasProblem);
	
	/**
	 * Returns true if any parsing problem (warning or error) exists.  
	 *  
	 * @see  #getHasProblem()
	 */
	public boolean hasProblem();
	
	/**
	 * Returns true, if the specified problem exists. False otherwise.
	 * @param problem
	 * @return
	 */
	public boolean hasProblem(ParserProblem problem);
	
	
	/**
	 * Returns a list of all warnings and errors that have been recognized during the parsing
	 * and not yet handled.
	 * @return
	 */
	public List<ParserProblem> getParsingProblems();
	
	/**
	 * Adds a parsing problem to the list of parsing problems
	 * @param problem
	 */
	public void addParsingProblem(ParserProblem problem);
	
	/**
	 * Removes a parsing problem from the list of parsing problems.
	 * If the problem is not in the list or is <code>null</code>, nothing happens.
	 * @param warning
	 */public void removeParsingProblem(ParserProblem problem);
	
	
	/**
	 * Returns the integer value of the position where a parsing problem starts.
	 * If no problem exists -1 is returned.
	 * Default: -1
	 * @deprecated This method will be removed by a more sophisticated method in future versions, 
	 * therefore it is deprecated.<BR>
	 * @see  #getHasProblem()
	 */
	@Deprecated
	public int getProblemStarts();
	
	/**
	 * Returns the integer value of the position where a parsing problem ends.
	 * If no problem exists -1 is returned.
	 * Default: -1
	 * @deprecated This method will be removed by a more sophisticated method in future versions, 
	 * therefore it is deprecated. <BR>
	 * @see  #getHasProblem()
	 */
	@Deprecated
	public int getProblemEnds();
	
	/**
	 * @deprecated This method will be removed by a more sophisticated method in future versions, 
	 * therefore it is deprecated. <BR>
	 * @see  #getProblemStarts()
	 */
	@Deprecated
	public void setProblemStarts(int start);
	
	/**
	 * @deprecated This method will be removed by a more sophisticated method in future versions, 
	 * therefore it is deprecated. <BR>
	 * @see  #getProblemEnds()
	 */
	@Deprecated
	public void setProblemEnds(int end);
	
}
