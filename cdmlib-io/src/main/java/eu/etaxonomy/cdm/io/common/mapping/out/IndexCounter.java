/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping.out;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 * @created 12.05.2009
 * @version 1.0
 */
public class IndexCounter {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(IndexCounter.class);

	int index = 0;
	
	public IndexCounter(int startValue){
		index = startValue;
	}
	
	/**
	 * Returns the index and increases it by 1
	 * @return
	 */
	public int getIncreasing(){
		return index++;
	}
	
	public String toString(){
		return String.valueOf(index);
	}
	
}
