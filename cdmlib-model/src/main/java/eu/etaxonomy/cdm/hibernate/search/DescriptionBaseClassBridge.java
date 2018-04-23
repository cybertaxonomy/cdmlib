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
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.hibernate.search.bridge.LuceneOptions;

import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.name.TaxonName;
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
 * is not taking subclasses into account, so the <code>taxon</code> field defined in {@link TaxonDescription} is not
 * registered in the <code>propertiesMetdata</code>
 *
 * @author Andreas Kohlbecker
 * @since Dec 19, 2011
 *
 */
public class DescriptionBaseClassBridge extends AbstractClassBridge {


    /*
     * (non-Javadoc)
     *
     * @see org.hibernate.search.bridge.FieldBridge#set(java.lang.String,
     * java.lang.Object, org.apache.lucene.document.Document,
     * org.hibernate.search.bridge.LuceneOptions)
     */
    @Override
    public void set(String name, Object entity, Document document, LuceneOptions luceneOptions) {

            if (entity instanceof TaxonDescription) {

                Taxon taxon = ((TaxonDescription)entity).getTaxon();

                if (taxon != null) {

                    idFieldBridge.set(name + "taxon.id", taxon.getId(), document, idFieldOptions);

                    Field titleCachefield = new TextField(name + "taxon.titleCache", taxon.getTitleCache(), Store.YES);
                    document.add(titleCachefield);

                    // this should not be necessary since the IdentifiableEntity.titleCache already has the according annotation
                    /*
                    Field titleCacheSortfield = new SortedDocValuesField(
                            name + "taxon.titleCache__sort",
                            new BytesRef(taxon.getTitleCache())
                            );
                    LuceneDocumentUtility.setOrReplaceDocValueField(titleCacheSortfield, document);
                    */

                    Field uuidfield = new StringField(name + "taxon.uuid", taxon.getUuid().toString(), Store.YES);
                    document.add(uuidfield);

                    for(TaxonNode node : taxon.getTaxonNodes()){
                        if(node.getClassification() != null){
                            idFieldBridge.set(name + "taxon.taxonNodes.classification.id", node.getClassification().getId(), document, idFieldOptions);
                        }
                    }
                }

            }
            if (entity instanceof TaxonNameDescription) {
                TaxonName taxonName = ((TaxonNameDescription) entity).getTaxonName();
                if (taxonName != null) {
                    idFieldBridge.set(name + "taxonName.id", taxonName.getId(), document, idFieldOptions);
                }
            }
    }


}
