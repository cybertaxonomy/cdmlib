package eu.etaxonomy.cdm.api.service.search;

/**
 * @author andreas
 * @date Jul 2, 2012
 *
 */
public interface ICdmMassIndexer {


    /**
     * Reindex all cdm entities litest in {@link ICdmMassIndexer#indexedClasses()}.
     * Re-indexing will not purge the index.
     */
    public abstract void reindex();

    /**
     * This will wipe out the index.
     */
    public abstract void purge();

    public Class[] indexedClasses();

}