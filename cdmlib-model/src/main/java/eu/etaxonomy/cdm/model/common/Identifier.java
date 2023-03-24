/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;

import eu.etaxonomy.cdm.model.term.IdentifierType;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.validation.annotation.NullOrNotEmpty;

/**
 * @author a.mueller
 * @since 2014-06-30
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Identifier", propOrder = {
    "identifier",
    "type"
})
@Entity
@Audited
@Table(name="Identifier", indexes = { @Index(name = "identifierIndex", columnList = "identifier") })
public class Identifier
            extends AnnotatableEntity {

    private static final long serialVersionUID = 3337567049024506936L;
	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();


	@XmlElement(name ="Identifier" )
	@Column(length=800, name="identifier")
	@Field
    @NullOrNotEmpty
	private String identifier;

    @XmlElement(name = "Type")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	private IdentifierType type;

// **************************** FACTORY ******************************/

    public static Identifier NewInstance(String identifier, IdentifierType type){
    	return new Identifier(identifier, type);
    }

    public static Identifier NewInstance(IdentifiableEntity<?> identifiableEntity,
            String identifier, IdentifierType type){
        Identifier result = new Identifier(identifier, type);
        identifiableEntity.addIdentifier(result);
        return result;
    }

// ************************* CONSTRUCTOR ************************************

    @Deprecated  //for hibernate use only
    protected Identifier(){}

    public Identifier (String identifier, IdentifierType type){
    	this.identifier = identifier;
    	this.type = type;
    }


// ****************** GETTER / SETTER **********************/

	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = isBlank(identifier) ? null : identifier;
	}

	/**
	 * The identifier type. E.g. DOI, LSID, Barcode, Sample Designation, ...
	 * @see TermType#IdentifierType
	 * @return
	 */
	public IdentifierType getType() {
		return type;
	}
	public void setType(IdentifierType identifierType) {
		this.type = identifierType;
	}

	public String getUrl() {
	    try {
            if (type == null || isBlank(type.getUrlPattern()) || isBlank(this.identifier) ) {
                return null;
            }else {
                return type.getUrlPattern().replace("{@ID}", this.identifier);
            }
        } catch (Exception e) {
            return "error creating url pattern";
        }
	}

	//****************** CLONE ************************************************/

	@Override
	public Identifier clone() throws CloneNotSupportedException{
		Identifier result = (Identifier)super.clone();
		//no changes to: type, value
		return result;
	}
}