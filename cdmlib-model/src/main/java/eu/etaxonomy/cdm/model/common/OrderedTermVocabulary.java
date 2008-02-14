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
	
	public SortedSet<T> getHigherTerms(T otb) {
		return ((SortedSet<T>)terms).tailSet(otb);
	}
	public SortedSet<T> getLowerTerms(T otb) {
		return ((SortedSet<T>)terms).headSet(otb);
	}
	public T getNextHigherTerm(T otb) {
		try {
			return ((SortedSet<T>)terms).tailSet(otb).first();
		} catch (NoSuchElementException e) {
			return null;
		}
	}
	public T getNextLowerTerm(T otb) {
		try {
			return ((SortedSet<T>)terms).headSet(otb).last();
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
		int lowestOrderIndex = ((SortedSet<T>)terms).first().orderIndex;
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
		//compute next higher Term
//		int orderInd = termToPreceed.orderIndex;
//		this.mTermToSucceed = getNextTerm(termToPreceed);
//		while (mTermToSucceed != null &&  mTermToSucceed.orderIndex == orderInd ){
//			mTermToSucceed = getNextTerm(mTermToSucceed);
//		}
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
	
//	/** To be used only by OrderedTermBase */
//	@Deprecated
//	private void addTermIntoTermsList(T termToBeAdded){
//		if (mTermToSucceed == null){
//			int highestOrderIndex = ((SortedSet<T>)terms).last().orderIndex;
//			termToBeAdded.orderIndex = highestOrderIndex + 1;
//			terms.add(termToBeAdded);
//		}
//		int termOrderIndex = termToSucceed.orderIndex;
//		termToBeAdded.orderIndex = termToSucceed.orderIndex;
//		
//		//increase index for all succeeding terms ...
//		Iterator<T> iterator = getSucceedingTerms(termToSucceed).iterator();
//		while (iterator.hasNext()){
//			T otb = iterator.next(); 
//			toBeChangedByObject = otb;
//			otb.incrementIndex(this);
//			toBeChangedByObject = null;
//		}
//		// ...  including the succeeding term itself
//		toBeChangedByObject = termToSucceed;
//		termToSucceed.incrementIndex(this);
//		toBeChangedByObject = null;
//	}

	
	@Override
	public void removeTerm(T term) {
		if (this.getNextHigherTerm(term).compareTo(term) != 0  &&
						this.getNextLowerTerm(term).compareTo(term) != 0	){
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
