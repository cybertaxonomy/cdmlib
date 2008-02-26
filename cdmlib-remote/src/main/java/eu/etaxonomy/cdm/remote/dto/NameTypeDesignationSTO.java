package eu.etaxonomy.cdm.remote.dto;

import java.util.HashSet;
import java.util.Set;

public class NameTypeDesignationSTO extends ReferencedEntityBaseSTO {
	private NameSTO typeSpecies;
	private IdentifiedString status;
	private boolean isRejectedType;
	private boolean isConservedType;
	private Set<SpecimenTypeDesignationSTO> typeSpecimens = new HashSet();
}
