/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.term;

import java.util.List;

/**
 * @author a.mueller
 * @since 11.03.2019
 */
public interface ITermTree<TERM extends DefinedTermBase, REL extends TermRelationBase>
            extends ITermGraph<TERM, REL> {

    /**
     * Returns the (ordered) list of {@link TermTreeNode feature nodes} which are immediate
     * children of the root node of <i>this</i> term tree.
     */
    public abstract List<TermTreeNode<TERM>> getRootChildren();

    public List<TERM> asTermList();

}
