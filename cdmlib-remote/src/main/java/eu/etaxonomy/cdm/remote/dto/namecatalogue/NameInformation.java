package eu.etaxonomy.cdm.remote.dto.namecatalogue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.remote.dto.common.RemoteResponse;

public class NameInformation implements RemoteResponse {

	private NameInformation.NameInformationRequest request;
	private NameInformation.NameInformationResponse response;

	public NameInformation() {

	}

	public void setRequest(String nameUuid) {
		request = new NameInformation.NameInformationRequest();
		request.setNameUuid(nameUuid);
	}

	public void setResponse(String title,
			String name,
			String rank,
			Set<NomenclaturalStatus> nomenclatureStatus,
			String citation,
			Set<NameRelationship> relationsFromThisName,
			Set<NameRelationship> relationsToThisName,
			Set<TaxonBase> taxonBases) {
		response = new NameInformation.NameInformationResponse();
		response.setTitle(title);
		response.setName(name);
		response.setRank(rank);

		// set status list
		Iterator it = nomenclatureStatus.iterator();
		System.out.println("NStatus Size : " + nomenclatureStatus.size());
		while(it.hasNext()) {
			NomenclaturalStatus ns = (NomenclaturalStatus)it.next();
			response.addToNomenclatureStaus(ns.toString());
		}
		// set citation
		response.setCitation(citation);

		addToResponseNameRelationships(relationsFromThisName, relationsToThisName);
		addToResponseTaxonInfo(taxonBases);

	}


	private void addToResponseNameRelationships(Set<NameRelationship> relationsFromThisName,
				Set<NameRelationship> relationsToThisName) {
		// set name from relationship
		String typeString = "";
		String relatednameCache = "";
		String relatedCitation = "";

		Iterator it = relationsFromThisName.iterator();

		while(it.hasNext()) {
			typeString = "";
			relatednameCache = "";
			relatedCitation = "";

			NameRelationship nr = (NameRelationship)it.next();
			if(nr.getType() != null) {
				typeString = nr.getType()
				        .getRepresentation(Language.DEFAULT())
                        .getLabel();
			}
			TaxonName tnbRelatedTo = nr.getToName();
			if(tnbRelatedTo != null) {
			    System.out.println("tnbRelatedTo not null");
				relatednameCache = tnbRelatedTo.getTitleCache();
				Reference ref = (Reference) tnbRelatedTo.getNomenclaturalReference();
				if(ref !=null) {
				    relatedCitation = ref.getTitleCache();
				}
			}
			response.addNameRelationships(typeString,
					relatednameCache,
					relatedCitation);
		}
		// set name to relationship
		it = relationsToThisName.iterator();

		while(it.hasNext()) {
			typeString = "";
			relatednameCache = "";
			relatedCitation = "";

			NameRelationship nr = (NameRelationship)it.next();
			if(nr.getType() != null) {
				typeString = nr.getType()
				        .getInverseRepresentation(Language.DEFAULT())
                        .getLabel();
			}

			TaxonName tnbRelatedFrom = nr.getFromName();
			if(tnbRelatedFrom != null) {
			    System.out.println("tnbRelatedTo not null");
				relatednameCache = tnbRelatedFrom.getTitleCache();
				Reference ref = (Reference) tnbRelatedFrom.getNomenclaturalReference();
				if(ref !=null) {
				    relatedCitation = ref.getTitleCache();
				}
			}
			response.addNameRelationships(typeString,
					relatednameCache,
					relatedCitation);
		}

	}

	public void addToResponseTaxonInfo(Set<TaxonBase> taxonBases) {
		Iterator it = taxonBases.iterator();
		while(it.hasNext()) {
			TaxonBase tb = (TaxonBase)it.next();
			response.addToTaxonUuids(tb.getUuid().toString());
			if(tb.isInstanceOf(Taxon.class) && tb.getLsid() != null) {
				response.addToTaxonLsids(tb.getLsid().toString());
			}

		}
	}


	public NameInformation.NameInformationResponse getResponse() {
		return response;
	}

	public NameInformation.NameInformationRequest getRequest() {
		return this.request;
	}

	public class NameInformationRequest {
		private String nameUuid;

		public NameInformationRequest() {
			this.nameUuid = "";
		}

		public void setNameUuid(String nameUuid) {
			this.nameUuid = nameUuid;
		}

		public String getNameUuid() {
			return this.nameUuid;
		}


	}

	public class NameInformationResponse {
		private String title;
		private String name;
		private String rank;
		private List<String> nomenclatureStatus;
		private String citation;
		private List<NameInformation.NameInformationResponse.NameRelationshipInfo> nameRelationships;
		private Set<String> taxonUuids;
		private Set<String> taxonLsids;


		public NameInformationResponse() {
			title = "";
			name = "";
			rank = "";
			nomenclatureStatus = new ArrayList<String>();
			citation = "";
			nameRelationships = new ArrayList<NameInformation.NameInformationResponse.NameRelationshipInfo>();
			taxonUuids = new HashSet<String>();
			taxonLsids = new HashSet<String>();

		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getTitle() {
			return this.title;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}

		public void setRank(String rank) {
			this.rank = rank;
		}

		public String getRank() {
			return this.rank;
		}

		public void addToNomenclatureStaus(String nomenclatureStatus) {
			this.nomenclatureStatus.add(nomenclatureStatus);
		}

		public List<String> getNomenclatureStatus() {
			return this.nomenclatureStatus;
		}

		public void setCitation(String citation) {
			this.citation = citation;
		}

		public String getCitation() {
			return this.citation;
		}

		public void addNameRelationships(String type, String relatedName, String citation) {
			NameInformation.NameInformationResponse.NameRelationshipInfo nr =
					new NameInformation.NameInformationResponse.NameRelationshipInfo();
			nr.setRelationInfo(type, relatedName, citation);
			nameRelationships.add(nr);
		}

		public List<NameInformation.NameInformationResponse.NameRelationshipInfo> getNameRelationships() {
			return nameRelationships;
		}

		public void addToTaxonUuids(String taxonUuid) {
			taxonUuids.add(taxonUuid);
		}

		public Set<String> getTaxonUuids() {
			return this.taxonUuids;
		}

		public void addToTaxonLsids(String lsid) {
			taxonLsids.add(lsid);
		}

		public Set<String> getTaxonLsids() {
			return this.taxonLsids;
		}

		public class NameRelationshipInfo {
			private String type;
			private String relatedName;
			private String citation;



			public NameRelationshipInfo() {

			}

			public void setRelationInfo(String type, String relatedName, String citation) {
				this.type = type;
				this.relatedName = relatedName;
				this.citation = citation;
			}

			public String getType() {
				return this.type;
			}

			public String getRelatedName() {
				return this.relatedName;
			}

			public String getCitation() {
				return this.citation;
			}


		}
	}
}
