/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import java.util.EnumSet;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAttribute;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermType;

/**
 * This
 *
 * @author a.mueller
 * @since 22.04.2021
 */
@Entity
@Audited
public abstract class AvailableForTermBase<T extends DefinedTermBase>
        extends DefinedTermBase<T>{

    private static final long serialVersionUID = 7991846649037898325L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(AvailableForTermBase.class);

    @XmlAttribute(name ="availableFor")
    @NotNull
    @Type(type = "eu.etaxonomy.cdm.hibernate.EnumSetUserType",
        parameters = {@Parameter(name = "enumClass", value = "eu.etaxonomy.cdm.model.common.CdmClass")}
    )
    private EnumSet<CdmClass> availableFor = EnumSet.noneOf(CdmClass.class);

    //for hibernate use only
    @Deprecated
    protected AvailableForTermBase() {
        super();
    }
    @Deprecated
    protected AvailableForTermBase(TermType type) {
        super(type);
    }

    protected AvailableForTermBase(TermType type, String term, String label, String labelAbbrev) {
        super(type, term, label, labelAbbrev);
    }

// ****************************** GETTER_SETTER *******************************/

    protected EnumSet<CdmClass> getAvailableFor() {
        return availableFor;
    }

    /**
     * for know it is private and the boolean getters and setters should be used instead.
     * If you make it public make sure to guarantee that any change to the enum set results
     * in a new enum set (see also {@link #newEnumSet(EnumSet, CdmClass, CdmClass)}
     * and that the client is aware of the enum set being immutable.
     */
    private void setAvailableFor(EnumSet<CdmClass> availableFor){
        this.availableFor = availableFor;
    }

    /**
     * Sets the value for supported classes
     * @param cdmClass the supported class
     * @param value the value if it is supported (<code>true</code>) or not (<code>false</code>)
     */
    protected void setAvailableFor(CdmClass cdmClass, boolean value) {
        if (value && !this.availableFor.contains(cdmClass)){
            setAvailableFor(newEnumSet(this.availableFor, cdmClass, null));
        }else if (!value && this.availableFor.contains(cdmClass)){
            setAvailableFor(newEnumSet(this.availableFor, null, cdmClass));
        }else{
            return;
        }
    }

 // ****************************** CLONE ***********************************

    /**
    * @see java.lang.Object#clone()
    */
   @Override
   public AvailableForTermBase<T> clone()  {
       AvailableForTermBase<T> result;

       result = (AvailableForTermBase<T>)super.clone();

       result.availableFor = this.availableFor.clone();

       return result;
   }
}
