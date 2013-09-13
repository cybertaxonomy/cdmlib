package eu.etaxonomy.cdm.hibernate.search;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

/**
 * Base class for class bridges. The {@link AbstractClassBridge} basically provides a set of
 * {@link LuceneOptions} for id and for search fields.
 *
 * @author a.kohlbecker
 * @date Sep 24, 2012
 *
 */
public abstract class AbstractClassBridge implements FieldBridge {

    protected final static NotNullAwareIdBridge idFieldBridge = new NotNullAwareIdBridge();

    public static LuceneOptions idFieldOptions = new IdFieldOptions();

    public static LuceneOptions sortFieldOptions = new SortFieldOptions();


    /* (non-Javadoc)
     * @see org.hibernate.search.bridge.FieldBridge#set(java.lang.String, java.lang.Object, org.apache.lucene.document.Document, org.hibernate.search.bridge.LuceneOptions)
     */
    abstract public void set(String name, Object value, Document document, LuceneOptions luceneOptions);

}