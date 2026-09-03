/**
 * Copyright (C) 2025 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.io.print;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.ITermTreeService;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;

@Service
public class PrintPubFeatureOrderIndexService {

    @Autowired
    private ITermTreeService termTreeService;

    @Transactional(readOnly = true)
    public Map<UUID, Integer> buildFeatureOrderIndex(UUID featureTreeUuid) {

        Map<UUID, Integer> index = new HashMap<>();

        if (featureTreeUuid == null) {
            return index;
        }

        TermTree<Feature> tree = termTreeService.find(featureTreeUuid);
        if (tree == null) {
            return index;
        }

        TermNode<Feature> root = tree.getRoot();
        if (root == null) {
            return index;
        }

        int[] counter = new int[] { 0 };
        traverse(root, index, counter);

        return index;
    }

    private void traverse(
            TermNode<Feature> node,
            Map<UUID, Integer> index,
            int[] counter) {

        if (node == null) {
            return;
        }

        // optional but safe while session is active
        node = HibernateProxyHelper.deproxy(node);

        Feature feature = node.getTerm();
        if (feature != null) {
            feature = HibernateProxyHelper.deproxy(feature);

            if (feature.getUuid() != null && !index.containsKey(feature.getUuid())) {
                index.put(feature.getUuid(), counter[0]++);
            }
        }

        List<TermNode<Feature>> children = node.getChildNodes();
        if (children != null) {
            for (TermNode<Feature> child : children) {
                traverse(child, index, counter);
            }
        }
    }
}