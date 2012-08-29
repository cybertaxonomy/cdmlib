// $Id$
/**
 * Copyright (C) 2011 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.hibernate.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * This class bridge is needed to overcome limitations in hibernate search with polymorphism on associations. See:
 * <ol>
 * <li>"Support runtime polymorphism on associations (instead of defining the indexed properties based on the returned type"
 * (https://hibernate.onjira.com/browse/HSEARCH-438)</li>
 * <li>https://forum.hibernate.org/search.php?keywords=indexembedded+subclass&terms=all
 * &author=&sc=1&sf=all&sk=t&sd=d&sr=posts&st=0&ch=300&t=0&submit=Search</li>
 *</ol>
 * DEVELOPER NOTE: the problem is in {@link org.hibernate.search.engine.DocumentBuilderContainedEntity#initializeClass()} which
 * is not taking subclasses into account, so the <code>taxon</code> field defined in {@link TaxonDescription} is not registered in the <code>propertiesMetdata</code>
 *
 * @author Andreas Kohlbecker
 * @date Dec 19, 2011
 *
 */
public class DescriptionBaseClassBridge implements FieldBridge {

    /*
     * (non-Javadoc)
     *
     * @see org.hibernate.search.bridge.FieldBridge#set(java.lang.String,
     * java.lang.Object, org.apache.lucene.document.Document,
     * org.hibernate.search.bridge.LuceneOptions)
     */
    public void set(String name, Object entity, Document document, LuceneOptions luceneOptions) {

            PaddedIntegerBridge idFieldBridge = new PaddedIntegerBridge();

            if (entity instanceof TaxonDescription) {
                Taxon taxon = ((TaxonDescription) entity).getTaxon();
                if (taxon != null) {
                    Field idfield = new Field(name + "taxon.id", idFieldBridge.objectToString(taxon.getId()), Store.YES, Index.ANALYZED,
                            luceneOptions.getTermVector());
                    document.add(idfield);
                    Field titleCachefield = new Field(name + "taxon.titleCache", taxon.getTitleCache(), Store.YES, Index.ANALYZED,
                            luceneOptions.getTermVector());
                    document.add(titleCachefield);
                    Field titleCacheSortfield = new Field(name + "taxon.titleCache__sort", taxon.getTitleCache(), Store.YES, Index.NOT_ANALYZED,
                            luceneOptions.getTermVector());
                    document.add(titleCacheSortfield);
                    Field uuidfield = new Field(name + "taxon.uuid", taxon.getUuid().toString(), Store.YES, Index.ANALYZED,
                            luceneOptions.getTermVector());
                    document.add(uuidfield);
                    for(TaxonNode node : taxon.getTaxonNodes()){
                        if(node.getClassification() != null){
                        Field taxonNodeField = new Field(name + "taxon.taxonNodes.classification.id", idFieldBridge.objectToString(node.getClassification().getId()), Store.YES, Index.ANALYZED,
                                luceneOptions.getTermVector());
                        document.add(taxonNodeField);
                        }
                    }
                }

            }
            if (entity instanceof TaxonNameDescription) {
                TaxonNameBase taxonName = ((TaxonNameDescription) entity).getTaxonName();
                if (taxonName != null) {
                    Field field = new Field(name + "taxonName.id", idFieldBridge.objectToString(taxonName.getId()), luceneOptions.getStore(), luceneOptions.getIndex(),
                            luceneOptions.getTermVector());
                    document.add(field);
                }
            }
    }

}
