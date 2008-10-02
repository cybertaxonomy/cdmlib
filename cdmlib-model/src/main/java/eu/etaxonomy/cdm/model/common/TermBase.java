package eu.etaxonomy.cdm.model.common;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TermBase", propOrder = {
    "uri",
    "representations"
})
@XmlRootElement(name = "TermBase")
@MappedSuperclass
public abstract class TermBase extends VersionableEntity {
	private static final Logger logger = Logger.getLogger(TermBase.class);
	
	@XmlElement(name = "URI")
	private String uri;
	
	@XmlElementWrapper(name = "Representations")
	@XmlElement(name = "Representation")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private Set<Representation> representations = new HashSet<Representation>();
	
	public TermBase(){
		super();
	}
	public TermBase(String term, String label, String labelAbbrev) {
		super();
		this.addRepresentation(new Representation(term, label, labelAbbrev, Language.DEFAULT()) );
	}

	@OneToMany(fetch=FetchType.LAZY)
	@Cascade( { CascadeType.SAVE_UPDATE, CascadeType.DELETE })
	public Set<Representation> getRepresentations() {
		return this.representations;
	}

	public void setRepresentations(Set<Representation> representations) {
		this.representations = representations;
	}

	public void addRepresentation(Representation representation) {
		this.representations.add(representation);
	}

	public void removeRepresentation(Representation representation) {
		this.representations.remove(representation);
	}

	@Transient
	public Representation getRepresentation(Language lang) {
		for (Representation repr : representations){
			Language reprLanguage = repr.getLanguage();
			if (reprLanguage != null && reprLanguage.equals(lang)){
				return repr;
			}
		}
		return null;
	}
	
	/**
	 * @see #getPreferredRepresentation(Language)
	 * @param language
	 * @return
	 */
	@Transient
	public Representation getPreferredRepresentation(Language language) {
		Representation repr = getRepresentation(language); 
		if(repr == null){
			repr = getRepresentation(Language.DEFAULT());
		}
		if(repr == null){
			repr = getRepresentations().iterator().next();
		}
		return repr;
	}
	
	/**
	 * Returns the Representation in the preferred language. Preferred languages
	 * are specified by the parameter languages, which receives a list of
	 * Language instances in the order of preference. If no representation in
	 * any preferred languages is found the method falls back to return the
	 * Representation in Language.DEFAULT() and if nessecary further falls back
	 * to return the first element found.
	 * 
	 * TODO think about this fall-back strategy!
	 * 
	 * @param languages
	 * @return
	 */
	@Transient
	public Representation getPreferredRepresentation(List<Language> languages) {
		Representation repr = null;
		if(languages != null){
			for(Language language : languages) {
				repr = getRepresentation(language); 
				if(repr != null){
					return repr;
				}
			}
		}
		if(repr == null){
			repr = getRepresentation(Language.DEFAULT());
		}
		if(repr == null){
			repr = getRepresentations().iterator().next();
		}
		return repr;
	}

	public String getUri() {
		return this.uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	@Transient
	public String getLabel() {
		if(getLabel(Language.DEFAULT())!=null){
			Representation repr = getRepresentation(Language.DEFAULT());
			return (repr == null)? null :repr.getLabel();
		}else{
			for (Representation r : representations){
				return r.getLabel();
			}			
		}
		return super.getUuid().toString();
	}
	
	@Transient
	public String getLabel(Language lang) {
		Representation repr = this.getRepresentation(lang);
		return (repr == null) ? null : repr.getLabel();
	}	
	
	@Transient
	public void setLabel(String label){
		Language lang = Language.DEFAULT();
		setLabel(label, lang);
	}

	@Transient
	public void setLabel(String label, Language language){
		if (language != null){
			Representation repr = getRepresentation(language);
			if (repr != null){
				repr.setLabel(label);
			}else{
				repr = Representation.NewInstance(null, label, null, language);
			}
			this.addRepresentation(repr);
		}
	}


	@Transient
	public String getDescription() {
		return this.getDescription(Language.DEFAULT());
	}

	@Transient
	public String getDescription(Language lang) {
		Representation repr = this.getRepresentation(lang);
		return (repr == null) ? null :repr.getDescription();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null){
			return false;
		}
		if (TermBase.class.isAssignableFrom(obj.getClass())){
			TermBase dtb = (TermBase)obj;
			if (dtb.getUuid().equals(this.getUuid())){
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		//FIXME make toString save as explained in CdmBase.toString
		return super.toString()+" "+this.getLabel();
	}

}
