package eu.etaxonomy.cdm.api.service.search;

import java.util.Collection;
import java.util.Set;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author andreas
 \* @since Jul 2, 2012
 */
public interface ICdmMassIndexer {


    /**
     * Reindex all cdm entities listed in {@link ICdmMassIndexer#indexedClasses()}.
     * Re-indexing will not purge the index.
     * @param types TODO
     * @param monitor TODO
     */
    public abstract void reindex(Collection<Class<? extends CdmBase>> types, IProgressMonitor monitor);

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
     * Create (spell-checking) dictionary listed in {@link ICdmMassIndexer#dictionaryClasses()}.
     * This action will not purge the dictionary.
     * @param monitor TODO
     */
    public abstract void createDictionary(IProgressMonitor monitor);

    public Class[] dictionaryClasses();
}
