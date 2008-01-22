package eu.etaxonomy.cdm.model.agent;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.common.IMediaDocumented;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifyableMediaEntity;
import eu.etaxonomy.cdm.model.common.Media;

@Entity
public abstract class Agent extends IdentifyableMediaEntity{
	

}
