/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.term;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;

/**
 * @author a.mueller
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OrderedTermVocabulary")
@XmlRootElement(name = "OrderedTermVocabulary")
@Entity
@Audited
public class OrderedTermVocabulary<T extends OrderedTermBase>
        extends TermVocabulary<T>
        implements ITermGraph<T, TermNode>    {

	private static final long serialVersionUID = 7871741306306371242L;
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

// ************************* FACTORY METHODS ***********************************************/

	/**
	 * @param type the {@link TermType term type}, must be the same as for all included terms
	 * @return
	 * @throws NullPointerException if type is <code>null</code>
	 */
	public static OrderedTermVocabulary NewInstance(TermType type){
		return new OrderedTermVocabulary(type);
	}

	/**
     * @param type the {@link TermType term type}, must be the same as for all included terms
     * @param class the parameter is only used for correct generics handling
     * @param description the description of this vocabulary
     * @param label
     * @param labelAbbrev
     * @param termSourceUri
     * @return
     * @throws NullPointerException if type is <code>null</code>
     */
    public static <T extends OrderedTermBase<T>> OrderedTermVocabulary<T> NewOrderedInstance(TermType type, Class<T> clazz, String description, String label, String labelAbbrev, URI termSourceUri){
        return new OrderedTermVocabulary<T>(type, description, label, labelAbbrev, termSourceUri, null);
    }

//************************ CONSTRUCTOR *****************************************************/

    //for hibernate use only, *packet* private required by bytebuddy
	@Deprecated
	OrderedTermVocabulary() {}

	protected OrderedTermVocabulary(TermType type) {
		super(type);
	}

	protected OrderedTermVocabulary(TermType type, String term, String label, String labelAbbrev, URI termSourceUri, Language language) {
		super(type, term, label, labelAbbrev, termSourceUri, language);
	}

//************************* METHODS **************************************/

	@Transient
	@Override
	protected Set<T> newTermSet() {
		return new TreeSet<T>();
	}

	@Transient
	public SortedSet<T> getOrderedTerms() {
		SortedSet<T> result = getSortedSetOfTerms();
		return result;
	}

	//FIXME #6794 remove completely
	private SortedSet<T> getHigherAndEqualTerms(T otb) {
		SortedSet<T> result = new TreeSet<>();
		SortedSet<T> sortedSet = getSortedSetOfTerms();
		result.addAll( sortedSet.tailSet(otb));
		return result;
	}

	public SortedSet<T> getHigherTerms(T otb) {
		SortedSet<T> result = getHigherAndEqualTerms(otb);
		for (DefinedTermBase<?> setObjectUnproxied : terms){
		    @SuppressWarnings("unchecked")
            T setObject = (T)CdmBase.deproxy(setObjectUnproxied, OrderedTermBase.class);
            if (setObject.compareTo(otb) == 0){
				result.remove(setObject);
			}
		}
		return result;
	}

	public SortedSet<T> getLowerTerms(T otb) {
		/*SortedSet<T> result = getLowerAndEqualTerms(otb);
		for (T setObject : terms){
			if (setObject.compareTo(otb) == 0){
				result.remove(setObject);
			}
		}*/
	    SortedSet<T> result = new TreeSet<>();
        SortedSet<T> sortedSet = getSortedSetOfTerms();
        //headSet Returns a view of the portion of this set whose elements are STRICTLY less than toElement
        result.addAll( sortedSet.headSet(otb));
		return result;
	}

	public T getNextHigherTerm(T otb) {
		try {
			return getHigherTerms(otb).first();
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	public T getNextLowerTerm(T otb) {
		try {
			return getLowerTerms(otb).last();
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	@Transient
	public T getLowestTerm() {
		try {
			SortedSet<T> sortedSet = getSortedSetOfTerms();
			return sortedSet.first();
			//return ((SortedSet<T>)terms).first();
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	@Transient
	public T getHighestTerm() {
		try {
			SortedSet<T> sortedSet = getSortedSetOfTerms();
			return sortedSet.last();
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	/**
	 * Adds a term to the the end / lowest
	 * @see eu.etaxonomy.cdm.model.term.TermVocabulary#addTerm(eu.etaxonomy.cdm.model.term.DefinedTermBase)
	 */
	@Override
    public void addTerm(T term) {
		SortedSet<T> sortedTerms = getSortedSetOfTerms();
		int lowestOrderIndex;
		if (sortedTerms.isEmpty()){
			lowestOrderIndex = 0;
		}else{
			T first = sortedTerms.first();
			lowestOrderIndex = first.orderIndex;
		}
		term.orderIndex = lowestOrderIndex + 1;
		super.addTerm(term);
	}

	public void addTermAbove(T termToBeAdded, T lowerTerm)  {
		int orderInd = lowerTerm.orderIndex;
		termToBeAdded.orderIndex = orderInd;
		//increment all orderIndexes of terms below
		Set<T> myTerms = getSortedSetOfTerms();
		for(T term : myTerms){
		    if (term.orderIndex >= orderInd){  //should always be true
				term.orderIndex++;
			}
		}
		super.addTerm(termToBeAdded);
	}

	public void addTermBelow(T termToBeAdded, T higherTerm)  {
		int orderInd = higherTerm.orderIndex;
		termToBeAdded.orderIndex = orderInd + 1;
		//increment all orderIndexes of terms below
		Iterator<T> iterator = getLowerTerms(higherTerm).iterator();
		while(iterator.hasNext()){
			T term = iterator.next();
			if (term.orderIndex > orderInd){
				term.orderIndex++;
			}
		}
		super.addTerm(termToBeAdded);
	}

	@Override
	public void removeTerm(T term) {
		if (term == null){
			return;
		}
		int orderIndex = term.orderIndex;
		super.removeTerm(term);
		for (T other:terms) {
            if (term.orderIndex > orderIndex) {
              toBeChangedByObject = other;
              other.decreaseIndex(this);
              toBeChangedByObject = null;
            }
        }
	}

	@Transient
	private T toBeChangedByObject;

	public boolean indexChangeAllowed(OrderedTermBase orderedTermBase){
		return orderedTermBase == toBeChangedByObject ;
	}

	@Transient
	private SortedSet<T> getSortedSetOfTerms(){
		SortedSet<T> sortedSet = new TreeSet<>();
		for (DefinedTermBase<?> termUnproxied : terms){
            @SuppressWarnings("unchecked")
            T term = (T)CdmBase.deproxy(termUnproxied, OrderedTermBase.class);
            sortedSet.add(term);
        }
		return sortedSet;
	}

    @Override
    public Set<TermNode> getTermRelations() {
        return super.termRelations();
    }
    /**
     * For now protected to avoid type checking etc. Might become
     * public in future
     * @param termRelations
     */
//    @Override  //not yet public
    protected void setTermRelations(Set<TermNode> termRelations) {
        super.termRelations(termRelations);
    }

}
