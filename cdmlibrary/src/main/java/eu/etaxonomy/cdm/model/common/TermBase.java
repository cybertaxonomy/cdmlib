package eu.etaxonomy.cdm.model.common;

import java.util.HashSet;
import java.util.Set;

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
	private Set<Representation> representations = new HashSet();


	public TermBase() {
		super();
	}
	public TermBase(String term, String label) {
		super();
		this.addRepresentation(new Representation(term, label, Language.DEFAULT()) );
	}


	@OneToMany
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
			if (repr.getLanguage() == lang){
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

	public String toString() {
		String result="DT<"+uri+">:";
		for (Representation r : representations){
			result += r.getLabel()+"("+r.getLanguage().getTermLabel()+")";
		}
		return result;
	}

	@Transient
	public String getTermLabel() {
		return this.getRepresentation(Language.DEFAULT()).getLabel();
	}

	@Transient
	public String getTermLabel(Language lang) {
		return this.getRepresentation(lang).getLabel();
	}

	@Transient
	public String getTermText() {
		return this.getRepresentation(Language.DEFAULT()).getLabel();
	}

	@Transient
	public String getTermText(Language lang) {
		return this.getRepresentation(lang).getLabel();
	}

	@Override
	public boolean equals(Object obj) {
		if (DefinedTermBase.class.isAssignableFrom(obj.getClass())){
			DefinedTermBase dtb = (DefinedTermBase)obj;
			if (dtb.getUuid().equals(this.getUuid())){
				return true;
			}
		}
		return false;
	}

}
