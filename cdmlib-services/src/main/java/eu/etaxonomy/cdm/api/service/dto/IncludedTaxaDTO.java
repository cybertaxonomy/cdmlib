/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;

/**
 * @author a.mueller
 * @since 2014-05-20
 *
 */
public class IncludedTaxaDTO {

	public class IncludedTaxon{
		private UUID taxonUuid;
		private List<UUID> pathToTaxon = new ArrayList<UUID>();
		private boolean doubtful = false;
	
		
		public IncludedTaxon(UUID taxonUuid, List<UUID> pathToTaxon, boolean doubtful) {
			this.taxonUuid = taxonUuid;
			this.pathToTaxon = pathToTaxon;
			this.doubtful = doubtful;
		}
		
		
		public UUID getTaxonUuid() {
			return taxonUuid;
		}
		public void setTaxonUuid(UUID taxonUuid) {
			this.taxonUuid = taxonUuid;
		}
		public List<UUID> getPathToTaxon() {
			return pathToTaxon;
		}
		public void setPathToTaxon(List<UUID> pathToTaxon) {
			this.pathToTaxon = pathToTaxon;
		}
		public boolean isDoubtful() {
			return doubtful;
		}
		public void setDoubtful(boolean doubtful) {
			this.doubtful = doubtful;
		}

		
		@Override
		public String toString(){
			return taxonUuid == null? super.toString() : taxonUuid.toString();
		}

	}
	
	private List<IncludedTaxon> includedTaxa = new ArrayList<IncludedTaxaDTO.IncludedTaxon>();

	private DateTime date = DateTime.now();
	
	//** ******************* CONSTRUCTOR **************************/
	
	public IncludedTaxaDTO() {}

	public IncludedTaxaDTO(UUID taxonUuid) {
		IncludedTaxon originalTaxon = new IncludedTaxon(taxonUuid, new ArrayList<UUID>(), false);
		includedTaxa.add(originalTaxon);
	}

 // ************************** GETTER / SETTER  ***********************/
	
	public List<IncludedTaxon> getIncludedTaxa() {
		return includedTaxa;
	}

	public void setIncludedTaxa(List<IncludedTaxon> includedTaxa) {
		this.includedTaxa = includedTaxa;
	}
	
	public void addIncludedTaxon(IncludedTaxon includedTaxon){
		includedTaxa.add(includedTaxon);
	}
	
	public void addIncludedTaxon(UUID taxonUuid, List<UUID> uuidPath, boolean doubtful){
		includedTaxa.add(new IncludedTaxon(taxonUuid, uuidPath, doubtful));
	}
	
	public DateTime getDate() {
		return date;
	}
	public void setDate(DateTime date) {
		this.date = date;
	}
	
	public int getSize(){
		return includedTaxa.size();
	}

	public boolean contains(UUID taxonUuid) {
		for (IncludedTaxon taxon: includedTaxa){
			if (taxon.taxonUuid.equals(taxonUuid)){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString(){
		String result = "";
		for (IncludedTaxon taxon : includedTaxa){
			result += taxon.toString() + ",";
		}
		if (result.length() > 0){
			result = result.substring(0, result.length() - 1);
		}
		
		result = "[" + result + "]";
		return result;
	}
	
	
}
