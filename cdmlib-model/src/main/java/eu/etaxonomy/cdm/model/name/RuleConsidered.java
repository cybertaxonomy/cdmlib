/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.name;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

/**
 * @author a.mueller
 * @since 23.07.2019
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RuleConsidered", propOrder = {
    "text",
    "codeEdition",
})
@XmlRootElement(name = "RuleConsidered")
@Embeddable
public class RuleConsidered implements Cloneable, Serializable{

    private static final long serialVersionUID = 531030660792800636L;
    private static final Logger logger = Logger.getLogger(RuleConsidered.class);

    //The nomenclatural code rule considered. The article/note/recommendation in the code in question that is commented on in
    //the note property.
    @XmlElement(name = "text")
    @Column(name="ruleConsidered")
    private String text;

    /**
     * The {@link NomenclaturalCodeEdition code edition} for the rule considered.
     */
    @XmlAttribute(name ="CodeEdition")
    @Column(name="codeEdition", length=20)
    @NotNull
    @Type(type = "eu.etaxonomy.cdm.hibernate.EnumUserType",
        parameters = {@org.hibernate.annotations.Parameter(name = "enumClass", value = "eu.etaxonomy.cdm.model.name.NomenclaturalCodeEdition")}
    )
    @Audited //needed ?
    private NomenclaturalCodeEdition codeEdition;

  //******************** FACTORY METHODS ****************************

    /**
     * Factory method
     * @return
     */
    public static RuleConsidered NewInstance(){
        return new RuleConsidered();
    }

//********** GETTER / SETTER ***********************************/

    /**
     * Returns the nomenclatural code rule considered (that is the
     * article/note/recommendation in the nomenclatural code ruling
     * the  taxon name(s) of this nomenclatural status).
     * The considered rule gives the reason why the
     * {@link NomenclaturalStatusType nomenclatural status type} has been
     * assigned to the {@link TaxonName taxon name(s)}.
     *
     * @see #getCodeEdition()
     */
    public String getText(){
        return this.text;
    }

    /**
     * @see  #getText()
     */
    public void setText(String text){
        this.text = text;
    }

    /**
     * The {@link NomenclaturalCodeEdition code edition} for the {@link #getText() rule considered}.
     */
    public NomenclaturalCodeEdition getCodeEdition() {
        return codeEdition;
    }
    public void setCodeEdition(NomenclaturalCodeEdition codeEdition) {
        this.codeEdition = codeEdition;
    }

  //*********** CLONE **********************************/

    /**
     * Clones <i>this</i> rule considered.
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public RuleConsidered clone(){
        try{
            RuleConsidered result = (RuleConsidered)super.clone();
            //no changes to: ruleConsidered, codeEdition
            return result;
        } catch (CloneNotSupportedException e) {
            logger.warn("Object does not implement cloneable");
            e.printStackTrace();
            return null;
        }
    }
}
