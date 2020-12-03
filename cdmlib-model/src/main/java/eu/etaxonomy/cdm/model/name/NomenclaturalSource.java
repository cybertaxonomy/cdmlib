/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.name;

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
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.strategy.merge.Merge;
import eu.etaxonomy.cdm.strategy.merge.MergeMode;

/**
 * This subclass of {@link DescriptionElementSource} exists to
 * support bidirectionality for sources of type
 * {@link OriginalSourceType#NomenclaturalReference}.
 * This is necessary e.g. to support recognition of state changes of the
 * nomenclatural reference which occur in the OriginalSource class
 * but should be handled in the context of the {@link TaxonName} instance
 * this source instance belongs to.
 * Recognition of these state changes are important for triggers as used
 * by the TaxonGraphHibernateListener or for resetting the
 * {@link TaxonName#fullTitleCache fullTitleCache} in {@link TaxonName}.
 *
 * @author a.mueller
 * @since 14.09.2020
 */
@XmlType(name = "NomenclaturalSource", propOrder = {
    })
@Entity
@Audited
public class NomenclaturalSource extends DescriptionElementSource {

    private static final long serialVersionUID = -8891160753285054431L;

    public static NomenclaturalSource NewNomenclaturalInstance(TaxonName taxonName) {
        NomenclaturalSource result = new NomenclaturalSource();
        result.sourcedName = taxonName;
        return result;
    }

// ************************* FIELDS ********************************/

    @XmlElement(name = "sourcedName")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToOne(fetch = FetchType.LAZY /*, mappedBy="nomenclaturalSource"*/)
    @Merge(value=MergeMode.MERGE)  //TODO maybe there is a better solution
    private TaxonName sourcedName;

//*********************** CONSTRUCTOR ******************************/

    @SuppressWarnings("deprecation")
    public NomenclaturalSource(){
        setType(OriginalSourceType.NomenclaturalReference);
        initListener();
    }

    @Override
    public void initListener(){
        PropertyChangeListener sourceListener = event->{
            String propName = event.getPropertyName();
            //full title cache
            if (propName.equals("citation")){
                if (!sourcedName.isProtectedFullTitleCache()){
                    sourcedName.fullTitleCache = null;
                }
                sourcedName.checkNullSource();
            }
            if (propName.equals("citationMicroReference")){
                if (!sourcedName.isProtectedFullTitleCache()){
                    sourcedName.fullTitleCache = null;
                }
                if (event.getNewValue() == null) {
                    sourcedName.checkNullSource();
                }
            }
        };
        addPropertyChangeListener(sourceListener);
    }

//***************** GETTER / SETTER ****************************/

    public TaxonName getSourcedName() {
        return sourcedName;
    }

    public void setSourcedName(TaxonName sourcedName) {
        if (this.sourcedName != sourcedName){
            this.sourcedName = sourcedName;
            if (sourcedName != null){
                sourcedName.setNomenclaturalSource(this);
            }
        }
    }

//************************* CLONE() ************************/

    @Override
    public NomenclaturalSource clone() {
        NomenclaturalSource result;
        try {
            result = (NomenclaturalSource)super.clone();
            //a name may only have 1 single nomenclatural source at time
            result.sourcedName = null;

            //no changes
            return result;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);  //this should not happen
        }
    }
}
