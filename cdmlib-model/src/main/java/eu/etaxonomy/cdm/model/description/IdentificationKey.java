/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import org.apache.log4j.Logger;
import java.util.*;
import javax.persistence.*;

/**
 * The class representing single-access fixed dichotomous or polytomous authored
 * decision keys (as opposed to {@link FeatureTree multiple-access keys}) used to identify
 * {@link SpecimenOrObservationBase specimens or observations} (this means to assign {@link Taxon taxa} to).
 * The determination process is based on the tree structure of the key and on
 * the statements of its leads.
 * 
 * @author m.doering 
 * @version 1.0
 * @created 08-Nov-2007 13:06:28
 */

@Entity
public class IdentificationKey extends Media {
	static Logger logger = Logger.getLogger(IdentificationKey.class);
	
	private Set<Taxon> coveredTaxa = new HashSet();
	
	/** 
	 * Class constructor: creates a new empty identification key instance.
	 */
	protected IdentificationKey() {
		super();
	}
	
	/** 
	 * Creates a new empty identification key instance.
	 */
	public static IdentificationKey NewInstance(){
		return new IdentificationKey();
	}

	
	/** 
	 * Returns the set of possible {@link Taxon taxa} corresponding to
	 * <i>this</i> identification key.
	 */
	@OneToMany
	public Set<Taxon> getCoveredTaxa() {
		return coveredTaxa;
	}
	/**
	 * @see	#getCoveredTaxa() 
	 */
	protected void setCoveredTaxa(Set<Taxon> coveredTaxa) {
		this.coveredTaxa = coveredTaxa;
	}
	/**
	 * Adds a {@link Taxon taxa} to the set of {@link #getCoveredTaxa() covered taxa}
	 * corresponding to <i>this</i> identification key.
	 * 
	 * @param	taxon	the taxon to be added to <i>this</i> identification key
	 * @see    	   		#getCoveredTaxa()
	 */
	public void addCoveredTaxon(Taxon taxon) {
		this.coveredTaxa.add(taxon);
	}
	/** 
	 * Removes one element from the set of {@link #getCoveredTaxa() covered taxa}
	 * corresponding to <i>this</i> identification key.
	 *
	 * @param	taxon	the taxon which should be removed
	 * @see     		#getCoveredTaxa()
	 * @see     		#addCoveredTaxon(Taxon)
	 */
	public void removeCoveredTaxon(Taxon taxon) {
		this.coveredTaxa.remove(taxon);
	}


}