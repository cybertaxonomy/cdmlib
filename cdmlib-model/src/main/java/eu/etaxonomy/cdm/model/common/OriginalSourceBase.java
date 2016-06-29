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
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Table;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.springframework.util.Assert;

import eu.etaxonomy.cdm.common.CdmUtils;

/**
 * Abstract base class for classes implementing {@link eu.etaxonomy.cdm.model.common.IOriginalSource IOriginalSource}.
 * @see eu.etaxonomy.cdm.model.common.IOriginalSource
 *
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:22
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OriginalSource", propOrder = {
    "type",
	"idInSource",
    "idNamespace"
})
@XmlRootElement(name = "OriginalSource")
@Entity
@Audited
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@Table(appliesTo="OriginalSourceBase")
public abstract class OriginalSourceBase<T extends ISourceable> extends ReferencedEntityBase implements IOriginalSource<T>, Cloneable {
	private static final long serialVersionUID = -1972959999261181462L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(OriginalSourceBase.class);

	/**
	 * The {@link OriginalSourceType type} of this source. According to PROV the type has to be thought as
	 * an activity that leads from the source entity to the current entity. It is not a property of the
	 * source itself.
	 */
	@XmlAttribute(name ="type")
	@Column(name="sourceType")
	@NotNull
    @Type(type = "eu.etaxonomy.cdm.hibernate.EnumUserType",
    	parameters = {@org.hibernate.annotations.Parameter(name="enumClass", value="eu.etaxonomy.cdm.model.common.OriginalSourceType")}
    )
	@Audited
	private OriginalSourceType type;

	//The object's ID in the source, where the alternative string comes from
	@XmlElement(name = "IdInSource")
	private String idInSource;

	@XmlElement(name = "IdNamespace")
	private String idNamespace;

//***************** CONSTRUCTOR ***********************/

	//for hibernate use only
	protected OriginalSourceBase() {

	}

	/**
	 * Constructor
	 * @param type2
	 */
	protected OriginalSourceBase(OriginalSourceType type){
		if (type == null){
			throw new IllegalArgumentException("OriginalSourceType must not be null");
		}
		this.type = type;
	}

//**************** GETTER / SETTER *******************************/


	@Override
	public String getIdInSource(){
		return this.idInSource;
	}
	@Override
	public void setIdInSource(String idInSource){
		this.idInSource = idInSource;
	}


	@Override
	public String getIdNamespace() {
		return idNamespace;
	}
	@Override
	public void setIdNamespace(String idNamespace) {
		this.idNamespace = idNamespace;
	}


	@Override
	public OriginalSourceType getType() {
		return type;
	}
	@Override
	public void setType(OriginalSourceType type) {
		Assert.notNull(type, "OriginalSourceType must not be null");
		this.type = type;
	}


//********************** CLONE ************************************************/

	@Override
	public Object clone() throws CloneNotSupportedException{
		OriginalSourceBase<?> result = (OriginalSourceBase<?>)super.clone();

		//no changes to: idInSource
		return result;
	}


//************************ toString ***************************************/
	@Override
	public String toString(){
		if (StringUtils.isNotBlank(idInSource) || StringUtils.isNotBlank(idNamespace) ){
			return "OriginalSource:" + CdmUtils.concat(":", idNamespace, idInSource);
		}else{
			return super.toString();
		}
	}

//*********************************** EQUALS *********************************************************/

	/**
     * {@inheritDoc}
     */
    @Override
    public boolean equalsByShallowCompare(ReferencedEntityBase other) {

        if(!super.equalsByShallowCompare(other)) {
            return false;
        }
        OriginalSourceBase<T> theOther = (OriginalSourceBase<T>)other;
        if(!StringUtils.equals(this.getIdInSource(), theOther.getIdInSource())
                || !StringUtils.equals(this.getIdNamespace(), theOther.getIdNamespace())) {
            return false;
        }

        return true;
    }

}