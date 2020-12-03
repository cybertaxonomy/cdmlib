/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.term;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;


/**
 * @author a.mueller
 * @since 07.03.2019
 *
 * @param <T>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TermTree", propOrder = {

})
@XmlRootElement(name = "TermTree")
@Entity
@Audited
public class TermGraph <T extends DefinedTermBase>
            extends TermGraphBase<T, TermRelation<T>> {

	private static final long serialVersionUID = -6713834139003172735L;
	private static final Logger logger = Logger.getLogger(TermGraph.class);

//******************** FACTORY METHODS ******************************************/

    /**
     * Creates a new term collection instance for the given term type
     * with an empty {@link #getRoot() root node}.
     * @param termType the {@link TermType term type}, must not be null
     */
    public static <T extends DefinedTermBase<T>> TermGraph<T> NewInstance(@NotNull TermType termType){
        return new TermGraph<>(termType);
    }

	/**
	 * Creates a new TermGraph instance with a given uuid.
	 * @param termType
	 * @param uuid
	 * @return
	 */
	public static <T extends DefinedTermBase<T>> TermGraph<T> NewInstance(@NotNull TermType termType, UUID uuid){
		TermGraph<T> result =  new TermGraph<>(termType);
		result.setUuid(uuid);
		return result;
	}

// ******************** CONSTRUCTOR *************************************/

    //TODO needed?
    @Deprecated
    protected TermGraph(){}

	/**
	 * Class constructor: creates a new feature tree instance with an empty
	 * {@link #getRoot() root node}.
	 */
	protected TermGraph(TermType termType) {
        super(termType);
	}

// ****************** GETTER / SETTER **********************************/


//******************** METHODS ***********************************************/

	/**
	 * Computes a set of distinct terms that are present in this term tree
	 *
	 * @return
	 */
	@Override
    @Transient
	public Set<T> getDistinctTerms(){
	    Set<T> result = new HashSet<>();
	    for (TermRelation<T> rel : getTermRelations()){
	        result.add(rel.getTerm());
	        result.add(rel.getToTerm());
	    }
	    result.remove(null);  //just in case
	    return result;
	}

//*********************** CLONE ********************************************************/

	/**
	 * Clones <i>this</i> {@link TermGraph}. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> graph by
	 * modifying only some of the attributes.
	 * {@link TermRelation Term relations} always belong only to one tree, so all
	 * {@link TermRelation Term relations} are cloned to build
	 * the new {@link TermGraph}
	 *
	 * @see java.lang.Object#clone()
	 */
	@Override
	public TermGraph<T> clone() {
		try {
		    TermGraph<T> result = (TermGraph<T>)super.clone();
			return result;
		}catch (CloneNotSupportedException e) {
            String message = "Clone not possible. Object does not implement cloneable";
            logger.warn(message);
            throw new RuntimeException(message);
		}
	}
}