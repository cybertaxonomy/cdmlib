/**
 * 
 */
package eu.etaxonomy.cdm.model.common;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 *
 */
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
	protected Set<T> getNewTermSet(){
		return new TreeSet<T>();
	}
	
	public SortedSet<T> getHigherAndEqualTerms(T otb) {
		SortedSet<T> result = new TreeSet<T>();
		result.addAll( ((SortedSet<T>)terms).tailSet(otb));
		return result;
	}
	public SortedSet<T> getHigherTerms(T otb) {
		SortedSet<T> result = getHigherAndEqualTerms(otb);
		for (T setObject : terms){
			if (setObject.compareTo(otb) == 0){
				result.remove(setObject);
			}
		}
		return result;
	}

	public SortedSet<T> getLowerAndEqualTerms(T otb) {
		SortedSet<T> result = new TreeSet<T>();
		result.addAll( ((SortedSet<T>)terms).headSet(otb));
		return result;
	}
	
	public SortedSet<T> getLowerTerms(T otb) {
		SortedSet<T> result = getLowerAndEqualTerms(otb);
		for (T setObject : terms){
			if (setObject.compareTo(otb) == 0){
				result.remove(setObject);
			}
		}
		return result;
	}
	
	public T getNextHigherTerm(T otb) {
		try {
			return getHigherTerms(otb).last();
		} catch (NoSuchElementException e) {
			return null;
		}
	}
	public T getNextLowerTerm(T otb) {
		try {
			return getLowerTerms(otb).first();
		} catch (NoSuchElementException e) {
			return null;
		}
	}
	
	public T getLowestTerm() {
		try {
			return ((SortedSet<T>)terms).first();
		} catch (NoSuchElementException e) {
			return null;
		}
	}
	
	
	public T getHighestTerm() {
		try {
			return ((SortedSet<T>)terms).last();
		} catch (NoSuchElementException e) {
			return null;
		}
	}
	
	@Override
	public void addTerm(T term) throws WrongTermTypeException {
		SortedSet sortedTerms = ((SortedSet<T>)terms);
		int lowestOrderIndex;
		if (sortedTerms.size() == 0){
			lowestOrderIndex = 1;
		}else{
			Object first = (T)sortedTerms.first();
			lowestOrderIndex = ((T)first).orderIndex;
		}
		term.orderIndex = lowestOrderIndex + 1;
		super.addTerm(term);	
	}

	private T mTermToSucceed;
	
	public void addTermAbove(T termToBeAdded, T termToSucceed) throws WrongTermTypeException {
//		this.mTermToSucceed = termToSucceed;
		int orderInd = termToSucceed.orderIndex;
		termToBeAdded.orderIndex = orderInd;
		
		//increment all orderIndexes that 
		//Successors
		Iterator<T> iterator = terms.iterator();
		while(iterator.hasNext()){
			T term = iterator.next();
			if (term.orderIndex >= orderInd){  //should always be true
				term.orderIndex++;
			}
		}
		super.addTerm(termToBeAdded);
//		this.mTermToSucceed = null;
	}

	public void addTermUnder(T termToBeAdded, T termToPreceed) throws WrongTermTypeException {
		int orderInd = termToPreceed.orderIndex;
		termToBeAdded.orderIndex = orderInd + 1;
		Iterator<T> iterator = getLowerTerms(termToPreceed).iterator();
		while(iterator.hasNext()){
			T term = iterator.next();
			if (term.orderIndex > orderInd){
				term.orderIndex++;
			}
		}
		
		super.addTerm(termToBeAdded);
//		mTermToSucceed = null;
	}
	
	@Override
	public void removeTerm(T term) {
		if (term == null){
			return;
		}
		if (term.compareTo(this.getNextHigherTerm(term)) != 0  &&
						term.compareTo(this.getNextLowerTerm(term)) != 0	){
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
