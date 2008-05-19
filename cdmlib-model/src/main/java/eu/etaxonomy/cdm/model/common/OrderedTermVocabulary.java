/**
 * 
 */
package eu.etaxonomy.cdm.model.common;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 *
 */
@Entity
public class OrderedTermVocabulary<T extends OrderedTermBase> extends TermVocabulary<T> {
	private static final Logger logger = Logger.getLogger(OrderedTermVocabulary.class);
	
	/**
	 * 
	 */
	public OrderedTermVocabulary() {
		super();
	}

	/**
	 * @param term
	 * @param label
	 * @param termSourceUri
	 */
	public OrderedTermVocabulary(String term, String label, String termSourceUri) {
		super(term, label, termSourceUri);
	}
	
	@Override
	@Transient
	protected Set<T> getNewTermSet(){
		return new TreeSet<T>();
	}

	@Transient
	public SortedSet<T> getOrderedTerms(T otb) {
		SortedSet<T> result = new TreeSet<T>();
		result.addAll(terms);
		return result;
	}
	
	@Transient
	public SortedSet<T> getHigherAndEqualTerms(T otb) {
		SortedSet<T> result = new TreeSet<T>();
		SortedSet<T> sortedSet = new TreeSet<T>();
		sortedSet.addAll(terms);
		result.addAll( sortedSet.tailSet(otb));
		return result;
	}
	@Transient
	public SortedSet<T> getHigherTerms(T otb) {
		SortedSet<T> result = getHigherAndEqualTerms(otb);
		for (T setObject : terms){
			if (setObject.compareTo(otb) == 0){
				result.remove(setObject);
			}
		}
		return result;
	}

	@Transient
	public SortedSet<T> getLowerAndEqualTerms(T otb) {
		SortedSet<T> result = new TreeSet<T>();
		SortedSet<T> sortedSet = new TreeSet<T>();
		sortedSet.addAll(terms);
		result.addAll( sortedSet.headSet(otb));
		return result;
	}
	
	@Transient
	public SortedSet<T> getLowerTerms(T otb) {
		SortedSet<T> result = getLowerAndEqualTerms(otb);
		for (T setObject : terms){
			if (setObject.compareTo(otb) == 0){
				result.remove(setObject);
			}
		}
		return result;
	}

	@Transient
	public SortedSet<T> getEqualTerms(T otb) {
		SortedSet<T> result = new TreeSet<T>();
		for (T setObject : terms){
			if (setObject.compareTo(otb) == 0){
				result.add(setObject);
			}
		}
		return result;
	}
	
	@Transient
	public T getNextHigherTerm(T otb) {
		try {
			return getHigherTerms(otb).first();
		} catch (NoSuchElementException e) {
			return null;
		}
	}
	
	@Transient
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
			SortedSet<T> sortedSet = new TreeSet<T>();
			sortedSet.addAll(terms);
			return sortedSet.first();
			//return ((SortedSet<T>)terms).first();
		} catch (NoSuchElementException e) {
			return null;
		}
	}
	
	@Transient
	public T getHighestTerm() {
		try {
			SortedSet<T> sortedSet = new TreeSet<T>();
			sortedSet.addAll(terms);
			return sortedSet.last();
		} catch (NoSuchElementException e) {
			return null;
		}
	}
	
	@Override
	public void addTerm(T term) throws WrongTermTypeException {
		SortedSet sortedTerms = ((SortedSet<T>)terms);
		int lowestOrderIndex;
		if (sortedTerms.size() == 0){
			lowestOrderIndex = 0;
		}else{
			Object first = (T)sortedTerms.first();
			lowestOrderIndex = ((T)first).orderIndex;
		}
		term.orderIndex = lowestOrderIndex + 1;
		super.addTerm(term);	
	}

	public void addTermAbove(T termToBeAdded, T lowerTerm) throws WrongTermTypeException {
		int orderInd = lowerTerm.orderIndex;
		termToBeAdded.orderIndex = orderInd;
		//increment all orderIndexes of terms below 
		Iterator<T> iterator = terms.iterator();
		while(iterator.hasNext()){
			T term = iterator.next();
			if (term.orderIndex >= orderInd){  //should always be true
				term.orderIndex++;
			}
		}
		super.addTerm(termToBeAdded);
	}

	public void addTermBelow(T termToBeAdded, T higherTerm) throws WrongTermTypeException {
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
	
//	public void addTermEqualLevel(T termToBeAdded, T equalLevelTerm) throws WrongTermTypeException {
//		int orderInd = equalLevelTerm.orderIndex;
//		termToBeAdded.orderIndex = orderInd;
//		super.addTerm(termToBeAdded);
//	}
	
	@Override
	public void removeTerm(T term) {
		if (term == null){
			return;
		}
		if (this.getEqualTerms(term).size() == 0){
			Iterator<T> iterator = getLowerTerms(term).iterator();
			while (iterator.hasNext()){
				T otb = iterator.next(); 
				toBeChangedByObject = otb;
				otb.decreaseIndex(this);
				toBeChangedByObject = null;
			}
		}
		term.setVocabulary(null);
	}
	
	private T toBeChangedByObject;
	
	public boolean indexChangeAllowed(T otb){
		return otb == toBeChangedByObject ;
	}

}
