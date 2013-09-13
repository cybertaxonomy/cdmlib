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
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.usertype.UserCollectionType;

/**
 * TODO move to eu.etaxonomy.cdm.hibernate
 * 
 * @author unknown
 * 
 *
 */
public class PersistentMultiLanguageTextType implements UserCollectionType {
	
	public PersistentMultiLanguageTextType() {
	}

	public boolean contains(Object collection, Object obj) {
		Map<?,?> map = (Map<?,?>) collection;
		return map.containsValue(obj);
	}

	public Iterator getElementsIterator(Object collection) {
		return ( (Map<?,?>) collection ).values().iterator();
	}

	public Object indexOf(Object collection, Object element) {
		Iterator<?> iter = ( (Map<?,?>) collection ).entrySet().iterator();
		while ( iter.hasNext() ) {
			Map.Entry<?,?> me = (Map.Entry<?,?>) iter.next();
			//TODO: proxies!
			if ( me.getValue()==element ) return me.getKey();
		}
		return null;
	}

	public Object instantiate(int anticipatedSize) {
		return anticipatedSize <= 0 
	       ? new MultilanguageText()
	       : new MultilanguageText( anticipatedSize + (int)( anticipatedSize * .75f ), .75f );
	}

	public PersistentCollection instantiate(SessionImplementor session, CollectionPersister persister) throws HibernateException {
		return new PersistentMultiLanguageText();
	}

	public Object replaceElements(Object original, Object target, CollectionPersister collectionPersister,
			Object owner,Map copyCache,	SessionImplementor sessionImplementor) throws HibernateException {
		
		Map<Object,Object> result = (Map<Object,Object>) target;
		result.clear();
		
		Iterator<?> iter = ( (Map<?,?>) original ).entrySet().iterator();
		while ( iter.hasNext() ) {
			Map.Entry<?,?> me = (Map.Entry<?,?>) iter.next();
			Object key = collectionPersister.getIndexType().replace( me.getKey(), null, sessionImplementor, owner, copyCache );
			Object value = collectionPersister.getElementType().replace( me.getValue(), null, sessionImplementor, owner, copyCache );
			result.put(key, value);
		}
		
		return result;
	}

	public PersistentCollection wrap(SessionImplementor sessionImplementor, Object collection) {
		return new PersistentMultiLanguageText( sessionImplementor, (MultilanguageText) collection );
	}

}
