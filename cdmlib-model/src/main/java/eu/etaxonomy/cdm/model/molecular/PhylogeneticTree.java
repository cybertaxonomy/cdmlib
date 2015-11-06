/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.molecular;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import eu.etaxonomy.cdm.model.media.Media;

/**
 * "A phylogenetic tree or evolutionary tree is a branching diagram or "tree" showing the
 * inferred evolutionary relationships among various biological species or other entities
 * based upon similarities and differences in their physical and/or genetic characteristics.
 * The taxa joined together in the tree are implied to have descended from a common ancestor."
 * (Wikipedia).
 * <BR> In the CDM we currently store phylogenetic trees only as media. This may change in future.
 *
 * @author m.doering
 * @created 08-Nov-2007
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PhylogeneticTree", propOrder = {
	"usedSequences"
})
@XmlRootElement(name = "PhylogeneticTree")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.media.Media")
@Audited
public class PhylogeneticTree extends Media implements Cloneable{
	private static final long serialVersionUID = -7020182117362324067L;
	private static final  Logger logger = Logger.getLogger(PhylogeneticTree.class);


	@XmlElementWrapper(name = "UsedSequences")
	@XmlElement(name = "UsedSequence")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY)
	//preliminary  #5369
    @JoinTable(
            joinColumns = @JoinColumn( name="Media_id")
    )
    @NotNull
	private Set<Sequence> usedSequences = new HashSet<Sequence>();

//********************** Factory Method **********************************/

    /**
     * Factory method
     * @return
     */
    public static PhylogeneticTree NewInstance(){
        return new PhylogeneticTree();
    }


//***************** Constructor ****************************/

    private PhylogeneticTree(){
    	super();
    }



// ********************** GETTER / SETTER **************************/

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