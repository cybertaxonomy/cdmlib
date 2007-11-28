package eu.etaxonomy.cdm.model.common;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class NonOrderedTermBase extends DefinedTermBase {



}
