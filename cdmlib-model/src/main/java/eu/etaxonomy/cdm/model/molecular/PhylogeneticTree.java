/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.molecular;


import eu.etaxonomy.cdm.model.common.ReferencedMedia;
import org.apache.log4j.Logger;

import java.util.*;

import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:43
 */
@Entity
public class PhylogeneticTree extends ReferencedMedia {
	static Logger logger = Logger.getLogger(PhylogeneticTree.class);
	private Set<Sequence> usedSequences = new HashSet();
	
	@OneToMany
	public Set<Sequence> getUsedSequences() {
		return usedSequences;
	}
	protected void setUsedSequences(Set<Sequence> usedSequences) {
		this.usedSequences = usedSequences;
	}
	public void addUsedSequences(Sequence usedSequence) {
		this.usedSequences.add(usedSequence);
	}
	public void removeUsedSequences(Sequence usedSequence) {
		this.usedSequences.remove(usedSequence);
	}

}