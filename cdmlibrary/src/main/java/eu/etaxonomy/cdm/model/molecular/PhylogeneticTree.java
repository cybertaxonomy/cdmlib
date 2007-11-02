/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.molecular;


import etaxonomy.cdm.model.common.ReferencedMedia;
import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:15:08
 */
public class PhylogeneticTree extends ReferencedMedia {
	static Logger logger = Logger.getLogger(PhylogeneticTree.class);

	private ArrayList usedSequences;

	public ArrayList getUsedSequences(){
		return usedSequences;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setUsedSequences(ArrayList newVal){
		usedSequences = newVal;
	}

}