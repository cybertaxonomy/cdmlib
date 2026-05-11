package eu.etaxonomy.cdm.io.cdmprintpub.compare;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubExportState;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;

/**
 * Orders features according to a Feature TermTree (preorder traversal).
 *
 * Option A caching:
 *  - Builds a Map<featureUuid, orderIndex> once per export run
 *  - Stores it in PrintPubExportState (state.setFeatureOrderIndex(...))
 *
 * Tree loading is abstracted behind TermTreeProvider to avoid compile-time coupling
 * to a specific service type in the io module. Provide a Spring bean implementing
 * TermTreeProvider to enable tree-based ordering.
 */
@Component
public class FeatureTreeOrderStrategy implements IPrintPubFeatureOrderStrategy {

    /**
     * Minimal abstraction for loading the Feature TermTree.
     * Implement this interface in a Spring bean within your runtime context.
     *
     * Example implementation could delegate to repository.getTermTreeService().find(uuid).
     */
    public interface TermTreeProvider {
        TermTree<Feature> loadFeatureTree(UUID featureTreeUuid);
    }

    @Autowired(required = false)
    private TermTreeProvider termTreeProvider;

    @Override
    public Comparator<PrintPubFeatureKey> comparator(PrintPubExportState state) {

        ensureIndex(state);

        return new Comparator<PrintPubFeatureKey>() {
            @Override
            public int compare(PrintPubFeatureKey a, PrintPubFeatureKey b) {

                int oa = orderIndex(a != null ? a.getFeatureUuid() : null, state);
                int ob = orderIndex(b != null ? b.getFeatureUuid() : null, state);

                if (oa != ob) {
                    return Integer.compare(oa, ob);
                }

                // deterministic tie-breaker: label then uuid
                String la = (a == null || a.getLabel() == null) ? "" : a.getLabel();
                String lb = (b == null || b.getLabel() == null) ? "" : b.getLabel();
                int c = la.compareToIgnoreCase(lb);
                if (c != 0) {
                    return c;
                }

                String ua = (a == null || a.getFeatureUuid() == null) ? "" : a.getFeatureUuid().toString();
                String ub = (b == null || b.getFeatureUuid() == null) ? "" : b.getFeatureUuid().toString();
                return ua.compareTo(ub);
            }
        };
    }

    // --------------------
    // Internal helpers
    // --------------------

    private int orderIndex(UUID featureUuid, PrintPubExportState state) {
        if (featureUuid == null || state == null) {
            return Integer.MAX_VALUE / 4;
        }
        Map<UUID, Integer> idx = state.getFeatureOrderIndex();
        if (idx == null) {
            return Integer.MAX_VALUE / 4;
        }
        Integer v = idx.get(featureUuid);
        return v == null ? Integer.MAX_VALUE / 4 : v.intValue();
    }

    /**
     * Compute and cache the feature order index in state if missing.
     */
    private void ensureIndex(PrintPubExportState state) {
        if (state == null) {
            return;
        }
        if (state.getFeatureOrderIndex() != null) {
            return; // already cached for this export run
        }

        // cache even if empty to prevent recomputation
        Map<UUID, Integer> idx = new HashMap<UUID, Integer>();
        state.setFeatureOrderIndex(idx);

        // if no provider wired, we cannot load a tree => all features treated as "unknown"
        if (termTreeProvider == null) {
            return;
        }

        // if no tree uuid configured, also nothing to do
        UUID treeUuid = state.getConfig() != null ? state.getConfig().getFeatureTreeUuid() : null;
        if (treeUuid == null) {
            return;
        }

        TermTree<Feature> tree = termTreeProvider.loadFeatureTree(treeUuid);
        if (tree == null || tree.getRoot() == null) {
            return;
        }

        int[] counter = new int[] { 0 };
        for (TermNode<Feature> child : tree.getRoot().getChildNodes()) {
            preorder(child, idx, counter);
        }
    }

    /**
     * Preorder traversal, respecting TermNode child order.
     */
    private void preorder(TermNode<Feature> node, Map<UUID, Integer> idx, int[] counter) {
        if (node == null) {
            return;
        }

        Feature f = node.getTerm();
        if (f != null && f.getUuid() != null && !idx.containsKey(f.getUuid())) {
            idx.put(f.getUuid(), counter[0]++);
        }

        for (TermNode<Feature> child : node.getChildNodes()) {
            preorder(child, idx, counter);
        }
    }
}
