/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.term;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.envers.Audited;

/**
 * @author a.mueller
 * @since 06.03.2019
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TermCollection", propOrder = {
//    "termSourceUri",
//    "terms"
})
@Entity
@Audited
@Table(name="TermCollection")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class TermCollection<T extends DefinedTermBase>
            extends TermBase{

    private static final long serialVersionUID = 6102175902060054329L;

    @SuppressWarnings("deprecation")
    protected TermCollection(){}

    protected TermCollection(TermType type){
        super(type);
    }

    protected TermCollection(TermType type, String term, String label, String labelAbbrev) {
        super(type, term, label, labelAbbrev);
    }
}
