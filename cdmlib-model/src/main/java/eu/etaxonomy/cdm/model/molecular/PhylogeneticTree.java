/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.molecular;


import eu.etaxonomy.cdm.model.media.ReferencedMediaBase;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import java.util.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:43
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PhylogeneticTree", propOrder = {
	"usedSequences"
})
@XmlRootElement(name = "PhylogeneticTree")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.media.Media")
@Audited
public class PhylogeneticTree extends ReferencedMediaBase implements Cloneable{
	private static final long serialVersionUID = -7020182117362324067L;
	private static final  Logger logger = Logger.getLogger(PhylogeneticTree.class);
	
	@XmlElementWrapper(name = "UsedSequences")
	@XmlElement(name = "UsedSequence")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToMany(fetch = FetchType.LAZY)// FIXME surely should be ManyToMany - you can use a sequence to construct several different phylogenetic trees
    @NotNull
	private Set<Sequence> usedSequences = new HashSet<Sequence>();
	
	public Set<Sequence> getUsedSequences() {
		if(usedSequences == null) {
			this.usedSequences = new HashSet<Sequence>();
		}
		return usedSequences;
	}

	public void addUsedSequences(Sequence usedSequence) {
		this.usedSequences.add(usedSequence);
	}
	
	public void removeUsedSequences(Sequence usedSequence) {
		this.usedSequences.remove(usedSequence);
		
	}
	
//*********** CLONE **********************************/	
	
	/** 
	 * Clones <i>this</i> phylogenetic tree. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> phylogenetic tree
	 * by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link Media Media}.
	 * 
	 * @see eu.etaxonomy.cdm.model.media.Media#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	
	public Object clone(){
		PhylogeneticTree result;
		try{
			result= (PhylogeneticTree) super.clone();
			result.usedSequences = new HashSet<Sequence>();
			for (Sequence seq: this.usedSequences){
				result.addUsedSequences((Sequence)seq.clone());
			}
			
			return result;
		}catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}
}