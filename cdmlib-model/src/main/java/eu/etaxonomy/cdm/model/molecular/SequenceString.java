/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.molecular;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.Lob;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author a.mueller
 * @created 2013-07-05
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SequenceString", propOrder = {
	"string",
	"length"
})
@XmlRootElement(name = "SequenceString")
@Embeddable
public class SequenceString implements Cloneable, Serializable{
	private static final long serialVersionUID = 45735207807329055L;
	private static final Logger logger = Logger.getLogger(SequenceString.class);

	/**{@link #getString()}*/
	@XmlElement(name = "String")
    @Lob
	private String string;
	
	@XmlElement(name = "Length")
	private Integer length;

	
// ******************** FACTORY METHOD ******************/	

	public static SequenceString NewInstance(){
		SequenceString result = new SequenceString();
		return result;
	}
	
	public static SequenceString NewInstance(String sequence){
		SequenceString result = new SequenceString();
		result.setString(sequence);
		return result;
	}
	
// ********************* CONSTRUCTOR ********************/
	
// ********************* GETTER / SETTER ********************/

	/**
	 * The sequence as a string of base pairs. 5'->3'
	 */
	public String getString(){
		return this.string;
	}

	/**
	 * Sets the sequence. Also {@link #getLength() length information} will be set automatically.
	 * @see #getString()
	 * @param sequence    sequence
	 */
	public void setString(String sequence){
		this.string = sequence;
		this.setLength(sequence == null ? 0 : sequence.length());
	}
	
	
	/**
	 * The length of the sequence. Will be calculated if the {@link #getString() sequence}  is set.
	 * @return the length of the sequence.
	 */
	public Integer getLength(){
		return this.length;
	}

	/**
	 * Sets the {@link #getLength() length}, if the {@link #getString() sequence} is not set.
	 * If {@link #getString() sequence}  is available, length has no effect. 
	 * @see #getLength()
	 * @param length    length
	 */
	public void setLength(Integer length){
		if (StringUtils.isBlank(string)){
			this.length = length;
		}
	}

		
	// ********************* CLONE ********************/
	
	/** 
	 * Clones <i>this</i> sequence. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> sequencing by
	 * modifying only some of the attributes.<BR><BR>
	 * 
	 *  
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone()  {
		try{
		SequenceString result = (SequenceString)super.clone();

		//don't change sequence, length
		
		return result;
		}catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}
}
