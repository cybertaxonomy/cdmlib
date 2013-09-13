package eu.etaxonomy.cdm.api.service.search;

import java.util.Set;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author andreas
 * @date Jul 2, 2012
 *
 */
public interface ICdmMassIndexer {


    /**
     * Reindex all cdm entities listed in {@link ICdmMassIndexer#indexedClasses()}.
     * Re-indexing will not purge the index.
     * @param monitor TODO
     */
    public abstract void reindex(IProgressMonitor monitor);

    /**
     * This will wipe out the index.
     */
    public abstract void purge(IProgressMonitor monitor);

    /**
     * Returns a list of (sub-classes of CdmBase) classes to be indexed.
     *
     * @return a List of {@link CdmBase} sub classes
     */
    public Set<Class<? extends CdmBase>> indexedClasses();
    
    /**
     * Adds to the current list of (sub-classes of CdmBase) classes to be indexed.
     *
     * @param cdmBaseClass sub-class of CdmBase class to add
     */
    public void addToIndexedClasses(Class<? extends CdmBase> cdmBaseClass);
    
    /**
     * Clears the current list of (sub-classes of CdmBase) classes to be indexed.     
     */
    public void clearIndexedClasses();
    /**
     * Create (spell-checking) dictionary listed in {@link ICdmMassIndexer#dictionaryClasses()}.
     * This action will not purge the dictionary.
     * @param monitor TODO
     */
    public abstract void createDictionary(IProgressMonitor monitor);
    
    public Class[] dictionaryClasses();

}