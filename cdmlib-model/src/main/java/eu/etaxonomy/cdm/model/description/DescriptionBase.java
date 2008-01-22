package eu.etaxonomy.cdm.model.description;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

@MappedSuperclass
public abstract class DescriptionBase extends IdentifiableEntity {
	static Logger logger = Logger.getLogger(DescriptionBase.class);
	private String label;
	private Set<FeatureBase> features = new HashSet();
	private ReferenceBase source;
	@Override
	public String generateTitle() {
		// TODO Auto-generated method stub
		return null;
	}



	@ManyToOne
	@Cascade( { CascadeType.SAVE_UPDATE })
	public ReferenceBase getSource() {
		return this.source;
	}



	public void setSource(ReferenceBase source) {
		this.source= source;
	}



	@OneToMany
	@Cascade( { CascadeType.SAVE_UPDATE })
	public Set<FeatureBase> getFeatures() {
		return this.features;
	}



	protected void setFeatures(Set<FeatureBase> features) {
		this.features = features;
	}



	public void addFeature(FeatureBase feature) {
		this.features.add(feature);
	}



	public void removeFeature(FeatureBase feature) {
		this.features.remove(feature);
	}



	public String getLabel() {
		return this.label;
	}



	public void setLabel(String label) {
		this.label = label;
	}

}
