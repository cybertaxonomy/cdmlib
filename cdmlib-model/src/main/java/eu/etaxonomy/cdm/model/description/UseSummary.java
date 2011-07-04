package eu.etaxonomy.cdm.model.description;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.jboss.envers.Versioned;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.validation.Level2;

/**
 * Creating a class to store the Use summary data for the palms
 * This is a basic exercise to get to grips with the CDM
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UseSummary", propOrder = {
    "useSummary"
})
@XmlRootElement(name = "UseSummary")
@Entity
@Audited
@Indexed(index = "eu.etaxonomy.cdm.model.description.DescriptionElementBase")
public class UseSummary extends DescriptionElementBase implements Cloneable{
	private static final long serialVersionUID = -4728642322958035781L;
	private static final Logger logger = Logger.getLogger(UseSummary.class);
	
	@XmlElement(name = "UseSummary")
	@Field(index = Index.TOKENIZED)
	private String useSummary;
	
	/*
	@XmlElement(name = "NamedArea")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@NotNull(groups = Level2.class)
	private NamedArea ecoRegion;*/
	
	public static UseSummary NewInstance(String useSummary){
		UseSummary result = new UseSummary();
		result.setUseSummary(useSummary);
		return result;
	}
	
	/**
	 * Create a new empty UseSummary object
	 */
	protected UseSummary()	{
		super(Feature.USE_SUMMARY());
		
	}
	
	@Override
	@Deprecated 
	public void setFeature(Feature feature) {
		super.setFeature(feature);
	}
	
	public void setUseSummary(String useSummary) {
		this.useSummary = useSummary;
	}

	public String getUseSummary() {
		return useSummary;
	}

	/*public void setArea(NamedArea area) {
		this.ecoRegion = area;
	}

	public NamedArea getArea() {
		return ecoRegion;
	}*/
	
	@Override
	public String toString(){
		if (StringUtils.isNotBlank(useSummary)){
			return useSummary;
		}else{
			return super.toString();
		}
	}
	
	@Override
	public Object clone() {

		try {
			UseSummary result = (UseSummary)super.clone();
			return result;
			//no changes to name, language, area
		} catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}	
}

