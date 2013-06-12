package eu.etaxonomy.cdm.api.service.search;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;

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

    public Class[] indexedClasses();
    
    /**
     * Create (spell-checking) dictionary listed in {@link ICdmMassIndexer#dictionaryClasses()}.
     * This action will not purge the dictionary.
     * @param monitor TODO
     */
    public abstract void createDictionary(IProgressMonitor monitor);
    
    public Class[] dictionaryClasses();

}