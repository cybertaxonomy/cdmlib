/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.model.common;

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

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

/**
 * @author a.mueller
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OrderedTermVocabulary")
@XmlRootElement(name = "OrderedTermVocabulary")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.common.TermVocabulary")
@Audited
public class OrderedTermVocabulary<T extends OrderedTermBase> extends TermVocabulary<T> {
	private static final long serialVersionUID = 7871741306306371242L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(OrderedTermVocabulary.class);

	/**
	 * @param term
	 * @param label
	 * @param termSourceUri
	 */
	public OrderedTermVocabulary(String term, String label, String labelAbbrev, String termSourceUri) {
		super(term, label, labelAbbrev, termSourceUri);
	}

	public OrderedTermVocabulary() {
		super();
	}
	
	@Transient
	@Override
	public Set<T> getNewTermSet() {
		return new TreeSet<T>();
	}
	
	@Transient
	public SortedSet<T> getOrderedTerms() {
		SortedSet<T> result = getSortedSetOfTerms();
		return result;
	}

	public SortedSet<T> getHigherAndEqualTerms(T otb) {
		SortedSet<T> result = new TreeSet<T>();
		SortedSet<T> sortedSet = getSortedSetOfTerms();
		result.addAll( sortedSet.tailSet(otb));
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
		SortedSet<T> sortedSet = getSortedSetOfTerms();
	
		result.addAll( sortedSet.headSet(otb));
		//headSet Returns a view of the portion of this set whose elements are STRICTLY less than toElement
		for (T setObject : terms){
			if (setObject.compareTo(otb) == 0){
				result.add(setObject);
			}
		}
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

	public SortedSet<T> getEqualTerms(T otb) {
		SortedSet<T> result = new TreeSet<T>();
		for (T setObject : terms){
			if (setObject.compareTo(otb) == 0){
				result.add(setObject);
			}
		}
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
	
	public void addTerm(T term) {
		SortedSet<T> sortedTerms = getSortedSetOfTerms();
		int lowestOrderIndex;
		if (sortedTerms.size() == 0){
			lowestOrderIndex = 0;
		}else{
			T first = sortedTerms.first();
			lowestOrderIndex = first.orderIndex;
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
	
	public void addTermEqualLevel(T termToBeAdded, T equalLevelTerm) throws WrongTermTypeException {
		int orderInd = equalLevelTerm.orderIndex;
		termToBeAdded.orderIndex = orderInd;
		super.addTerm(termToBeAdded);
	}
	
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
		super.removeTerm(term);
	}
	
	@Transient
	private T toBeChangedByObject;
	
	public boolean indexChangeAllowed(OrderedTermBase<T> orderedTermBase){
		return orderedTermBase == toBeChangedByObject ;
	}
	
	
	@Transient
	private SortedSet<T> getSortedSetOfTerms(){
		SortedSet<T> sortedSet = new TreeSet<T>();
		sortedSet.addAll(terms);
		return sortedSet;
	}

}
