/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.taxon;

import java.beans.PropertyChangeListener;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.reference.NamedSourceBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.strategy.merge.Merge;
import eu.etaxonomy.cdm.strategy.merge.MergeMode;

/**
 * This subclass of {@link DescriptionElementSource} exists to
 * support bidirectionality for sources of type
 * {@link OriginalSourceType#SecundumReference}.
 * This is necessary e.g. to support recognition of state changes of the
 * secundum reference which occur in the OriginalSource class
 * but should be handled in the context of the {@link TaxonBase} instance
 * this source instance belongs to.
 *
 * @author a.mueller
 * @since 15.03.2021
 */
@XmlType(name = "SecundumSource", propOrder = {
    })
@Entity
@Audited
public class SecundumSource extends NamedSourceBase {

    private static final long serialVersionUID = 7899107010799860914L;

    public static SecundumSource NewSecundumInstance(TaxonBase<?> taxonBase) {
        SecundumSource result = new SecundumSource();
        result.sourcedTaxon = taxonBase;
        return result;
    }

// ************************* FIELDS ********************************/

    @XmlElement(name = "sourcedTaxon")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToOne(fetch = FetchType.LAZY /*, mappedBy="secSource"*/)
    @Merge(value=MergeMode.MERGE)  //TODO maybe there is a better solution
    private TaxonBase<?> sourcedTaxon;

//*********************** CONSTRUCTOR ******************************/

    @SuppressWarnings("deprecation")
    public SecundumSource(){
        setType(OriginalSourceType.SecundumReference);
        initListener();
    }

    @Override
    public void initListener(){
        PropertyChangeListener sourceListener = event->{
            String propName = event.getPropertyName();
            if (propName.equals("citation")){
                sourcedTaxon.checkNullSource();
            }
            if (propName.equals("citationMicroReference")){
                if (event.getNewValue() == null) {
                    sourcedTaxon.checkNullSource();
                }
            }
            sourcedTaxon.setTitleCache(null);
        };
        addPropertyChangeListener(sourceListener);
    }

//***************** GETTER / SETTER ****************************/

    public TaxonBase<?> getSourcedTaxon() {
        return sourcedTaxon;
    }

    public void setSourcedTaxon(TaxonBase<?> sourcedTaxon) {
        if (this.sourcedTaxon != sourcedTaxon){
            this.sourcedTaxon = sourcedTaxon;
            if (sourcedTaxon != null){
                sourcedTaxon.setSecSource(this);
            }
        }
    }

//************************* CLONE() ************************/

    @Override
    public SecundumSource clone() {
        SecundumSource result;
        try {
            result = (SecundumSource)super.clone();
            //a taxon may only have 1 single sec source at time
            result.sourcedTaxon = null;

            //no changes
            return result;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);  //this should not happen
        }
    }
}