/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


/**
 * @author a.mueller
 * @created 21.05.2008
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
	 * @return  the boolean value of the hasProblem flag
	 */
	public int getHasProblem();
	
	/**
	 * @see  #getHasProblem()
	 */
	public void setHasProblem(int hasProblem);
	
	/**
	 * Returns exactly the same int value as the {@link #getHasProblem() getHasProblem} method.  
	 *  
	 * @see  #getHasProblem()
	 */
	public int hasProblem();
	
	/**
	 * Returns the integer value of the position where a parsing problem starts.
	 * If no problem exists -1 is returned.
	 * This method will be removed by a more sophisticated method in future versions, 
	 * therefore it is deprecated.<BR>
	 * Default: -1
	 * @see  #getHasProblem()
	 */
	@Deprecated
	public int getProblemStarts();
	
	/**
	 * Returns the integer value of the position where a parsing problem ends.
	 * If no problem exists -1 is returned.
	 * This method will be removed by a more sophisticated method in future versions, 
	 * therefore it is deprecated. <BR>
	 * Default: -1
	 * @see  #getHasProblem()
	 */
	@Deprecated
	public int getProblemEnds();
	
	/**
	 * @see  #getProblemStarts()
	 */
	@Deprecated
	public void setProblemStarts(int start);
	
	/**
	 * @see  #getProblemEnds()
	 */
	@Deprecated
	public void setProblemEnds(int end);
	
}
