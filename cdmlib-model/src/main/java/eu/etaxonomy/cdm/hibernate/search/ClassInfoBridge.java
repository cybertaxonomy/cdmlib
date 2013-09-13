/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
/**
 * Lucene index class bridge which sets class information for the objects into the index
 *
 * @author c.mathew
 * @version 1.0
 * @created 26 Jul 2013
 */
public class ClassInfoBridge implements FieldBridge {

	@Override
	public void set(String name, Object value, Document document,
			LuceneOptions luceneOptions) {
		Field nameField = new Field(name + ".name",
				value.getClass().getName(),
				luceneOptions.getStore(),
				luceneOptions.getIndex(),
				luceneOptions.getTermVector());
		nameField.setBoost(luceneOptions.getBoost());
		document.add(nameField);
		
		Field canonicalNameField = new Field(name + ".canonicalName",
				value.getClass().getCanonicalName(),
				luceneOptions.getStore(),
				luceneOptions.getIndex(),
				luceneOptions.getTermVector());
		canonicalNameField.setBoost(luceneOptions.getBoost());
		document.add(canonicalNameField);

	}
}
