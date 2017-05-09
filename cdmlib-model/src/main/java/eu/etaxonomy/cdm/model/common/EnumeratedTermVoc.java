/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author a.mueller
 *
 */
public class EnumeratedTermVoc<T extends IEnumTerm<T>> {

	private static Map<Class<? extends IEnumTerm<?>>,EnumeratedTermVoc<?>> vocsMap= new HashMap<>();

	private final Map<T,SingleEnumTerm<T>> lookup = new HashMap<>();

//	public interface EnumTerm<R extends EnumTerm<?>> extends ISimpleTerm<R>{
//		public String getKey();
//	}

	private class SingleEnumTerm<S extends T> implements IEnumTerm<T>{
		private final S term;
		private final String label;
		private final UUID uuid;
		private final String key;
		private final Set<S> children = new HashSet<S>();
		private final S parent;



		private	SingleEnumTerm(S term, UUID uuid, String defaultString, String key, S parent){
			this.term = term;
			this.label = defaultString;
			this.key = key;
			this.uuid = uuid;
			this.parent = parent;
			SingleEnumTerm<T> parentSingleEnum = lookup.get(parent);
			if (parentSingleEnum != null){
				parentSingleEnum.children.add(term);
			}
		}

		@Override
		public UUID getUuid() {	return uuid;}
		@Override
		public T getKindOf() {return parent;}
		public T getTerm() {return term;}
		@Override
		public String getKey() {return key;	}

		@Override
		public String getMessage() {return getMessage(Language.DEFAULT());}

		@Override
		public String getMessage(Language language) {
			//TODO make multi-lingual
			return label;
		}

		@Override
		public Set<T> getGeneralizationOf() {
//			return Collections.unmodifiableSet( children );   //TODO creates stack overflow
			return new HashSet<T>(children);
		}

		@Override
		public boolean isKindOf(T ancestor) {
			if (parent == null || ancestor == null){
				return false;
			}else if (parent.equals(ancestor)){
				return true;
			}else{
				return parent.isKindOf(ancestor);
			}
		}

		@Override
		public Set<T> getGeneralizationOf(boolean recursive) {
			Set<T> result = new HashSet<T>();
			result.addAll(this.children);
			if (recursive){
				for (T child : this.children){
					result.addAll(child.getGeneralizationOf(recursive));
				}
			}
			return result;
		}

	} //end of inner class

//******************* DELEGATE NETHODS ************************

	public String getKey(T term) {return lookup.get(term).getKey();}

	public UUID getUuid(T term) {return lookup.get(term).getUuid();}

	public T getKindOf(T term) {return lookup.get(term).getKindOf();}

	public Set<T> getGeneralizationOf(T term) {return lookup.get(term).getGeneralizationOf();}

//******************* DELEGATE CLASS NETHODS ************************


	public static <S extends IEnumTerm<?>> IEnumTerm addTerm(Class<? extends IEnumTerm<?>> clazz, S term, UUID uuid, String defaultString, String key, S parent){
		if (vocsMap.get(clazz) == null){
			vocsMap.put(clazz, new EnumeratedTermVoc());
		}
		IEnumTerm myTerm = vocsMap.get(clazz).add(term, uuid, defaultString, key, parent);
		return myTerm;
	}

	private  SingleEnumTerm<T> add(ISimpleTerm<?> term, UUID uuid, String defaultString, String key, ISimpleTerm<?> parent) {
		SingleEnumTerm<T> singleTerm = new SingleEnumTerm<T>((T)term, uuid, defaultString, key, (T)parent);
		if (containsKey(lookup, key)){
			throw new RuntimeException(String.format("Key must be unique in enumeration but was not for '%s'", key));
		}
		if (containsUuid(lookup, uuid)){
			throw new RuntimeException(String.format("UUID must be unique in enumeration but was not for '%s'", uuid));
		}
		lookup.put((T)term, singleTerm);
		return singleTerm;
	}

	public boolean containsKey(Map<T, SingleEnumTerm<T>> lookup, String key) {
		for (SingleEnumTerm<T> term : lookup.values()){
			if (term.getKey().equals(key)){
				return true;
			}
		}
		return false;
	}

	public boolean containsUuid(Map<T, SingleEnumTerm<T>> lookup, UUID uuid) {
		for (SingleEnumTerm<T> term : lookup.values()){
			if (term.getUuid().equals(uuid)){
				return true;
			}
		}
		return false;
	}

	public static <R extends IEnumTerm<R>> R byKey(Class<R> clazz, String key) {
		EnumeratedTermVoc<R> voc = getVoc(clazz);
		return voc == null? null:voc.getByKey(key);
	}

	public static <R extends IEnumTerm<R>> R byUuid(Class<R> clazz, UUID uuid) {
		EnumeratedTermVoc<R> voc = getVoc(clazz);
		return voc == null? null:voc.getByUuid(uuid);
	}


	public T getByKey(String key) {
		for (SingleEnumTerm<T> term : lookup.values()){
			if (term.getKey().equals(key)){
				return term.getTerm();
			}
		}
		return null;
	}

	public T getByUuid(UUID uuid) {
		for (SingleEnumTerm<T> term : lookup.values()){
			if (term.getUuid().equals(uuid)){
				return term.getTerm();
			}
		}
		return null;
	}

	public static <R extends IEnumTerm<R>> EnumeratedTermVoc<R> getVoc(Class<R> clazz) {
		return (EnumeratedTermVoc<R>)vocsMap.get(clazz);
	}





}
