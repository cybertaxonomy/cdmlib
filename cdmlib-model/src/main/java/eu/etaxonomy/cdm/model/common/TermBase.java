package eu.etaxonomy.cdm.model.common;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@MappedSuperclass
public abstract class TermBase extends VersionableEntity {
	static Logger logger = Logger.getLogger(TermBase.class);
	
	private String uri;
	private Set<Representation> representations = new HashSet<Representation>();
	
	public TermBase() {
		super();
	}
	public TermBase(String term, String label) {
		super();
		this.addRepresentation(new Representation(term, label, Language.DEFAULT()) );
	}

	@OneToMany(fetch=FetchType.EAGER)
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
		return super.toString()+" "+this.getLabel();
	}

}
