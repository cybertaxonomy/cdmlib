/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.term;

import java.util.Set;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

/**
 * @author a.mueller
 * @since 07.03.2019
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TermGraph", propOrder = {
})
@Entity
@Audited
public abstract class TermGraphBase<TERM extends DefinedTermBase, REL extends TermRelationBase> //<TERM, REL, TermGraphBase>
            extends TermCollection<TERM, REL>
            implements ITermGraph<TERM, REL>{

    private static final long serialVersionUID = -704169783744494023L;

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(TermGraphBase.class);


 // ******************** CONSTRUCTOR *************************************/

    @Deprecated
    protected TermGraphBase(){}

    protected TermGraphBase(TermType termType) {
        super(termType);
    }

    @Override
    public Set<REL> getTermRelations() {
        return super.termRelations();

    }
    /**
     * For now protected to avoid type checking etc. Might become
     * public in future
     * @param termRelations
     */
//    @Override  //not yet public
    protected void setTermRelations(Set<REL> termRelations) {
        super.termRelations(termRelations);
    }

    public abstract Set<TERM> getDistinctTerms();
}
