package eu.etaxonomy.cdm.remote.dto.occurrencecatalogue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.remote.controller.BaseListController;
import eu.etaxonomy.cdm.remote.dto.common.RemoteResponse;

/**
 * The class representing the response from the CDM Remote Web Service API to a single UUID search query.
 * All information contained in this class originates from a call to {@link SpecimenOrObservationBase}
 * <P>
 *
 * @author p.kelbert
 * @version 1.0
 * @created march 10 2014
 */


public class OccurrenceSearch implements RemoteResponse {

    private OccurrenceSearchRequest request;
    private final List<OccurrenceSearchResponse> response;

    public static final Logger logger = Logger.getLogger(BaseListController.class);

    public OccurrenceSearch() {
    	this.response = new ArrayList<OccurrenceSearchResponse>();
    }

    public void setRequest(String q) {
        request = new OccurrenceSearchRequest();
        request.setQuery(q);
    }

    public OccurrenceSearchRequest getRequest() {
        return this.request;
    }
    
    public class OccurrenceSearchRequest {
        private String taxonUuid;
        public OccurrenceSearchRequest() {
            this.taxonUuid = "";
        }

        public void setQuery(String q) {
            this.taxonUuid = q;
        }

        public String getQuery() {
            return this.taxonUuid;
        }
    }
    
    public void addToResponse(String acceptedTaxon,
    		String acceptedTaxonUuid,
    		DerivedUnitFacade duf) {
    	
    	OccurrenceSearch.OccurrenceSearchResponse osr = 
    			new OccurrenceSearch.OccurrenceSearchResponse();

    	osr.setAcceptedTaxon(acceptedTaxon);
    	osr.setAcceptedTaxonUuid(acceptedTaxonUuid);
    	
    	if(duf.getCollector() != null) {
    		osr.setCollector(duf.getCollector().getTitleCache());
    	}
    	if(duf.getCollection() != null) {
    		osr.setCollection(duf.getCollection().getName());
    		if(duf.getCollection().getInstitute() != null) {
    			osr.setInstitution(duf.getCollection().getInstitute().getName());
    		}
    	}
    	
    	osr.setFieldNotes(duf.getFieldNotes());
    	if(duf.getType() != null) {
    		osr.setType(duf.getType().name());
    	}
    	osr.setUnitCount(duf.getIndividualCount());
    	
    	if(duf.getKindOfUnit() != null) {
    		osr.setKindOfUnit(duf.getKindOfUnit().getLabel());
    	}
    	
    	osr.setElevation(duf.getAbsoluteElevation());
    	osr.setMaxElevation(duf.getAbsoluteElevationMaximum());
    	
    	osr.setDepth(duf.getDistanceToGround());
    	osr.setMaxDepth(duf.getDistanceToGroundMax());
    	
    	if(duf.getGatheringPeriod() != null) {
    		TimePeriod tp = duf.getGatheringPeriod();
    		if(tp.getStart() != null) {
    			osr.setStartGatheringDate(tp.getStart().toString());
    		}
    		if(tp.getEnd() != null) {
    			osr.setEndGatheringDate(tp.getEnd().toString());
    		}
    	}
    	
    	
    	OccurrenceSearch.OccurrenceSearchResponse.Location loc = 
    			osr.getLocation();
    	
    	Point exactLocation = duf.getExactLocation();
    	if(exactLocation != null) {
    		loc.setDecimalLatitude(exactLocation.getLatitude());
    		loc.setDecimalLongitude(exactLocation.getLongitude());
    		loc.setErrorRadius(exactLocation.getErrorRadius());    		
    		if(exactLocation.getReferenceSystem() != null) {
    			loc.setReferenceSystem(exactLocation.getReferenceSystem().getTitleCache());
    		}

    	}
    	if(duf.getCountry() != null) {
    		loc.setCountry(duf.getCountry().getTitleCache());
    	}
    	
    	if(duf.getLocality() != null) {
    		loc.setLocality(duf.getLocality().getText());
    	}
    	
    	osr.setFieldNumber(duf.getFieldNumber());
    	osr.setAccessionNumber(duf.getAccessionNumber());
    	osr.setCatalogNumber(duf.getCatalogNumber());
    	
    	Set<IdentifiableSource> sources = duf.getSources();
    	boolean dateFound = false;
    	String datePublishedString = null;
    	List<String> sourceTitleList = new ArrayList<String>();
    	for (IdentifiableSource source:sources) {
    		String citation = source.getCitation().getTitleCache();    

    		datePublishedString = source.getCitation().getDatePublishedString();
    		if (!dateFound && !StringUtils.isEmpty(datePublishedString)){
    			osr.setPublicationDate(datePublishedString);
    			dateFound=true;
    		}
    		String  micro = source.getCitationMicroReference();
    		
    		if(citation == null) {
    			citation = "";
    		}    		
    		if(micro == null) {
    			micro = "";
    		}    		
    		sourceTitleList.add(citation + " " + micro);
    	}
    	osr.setSources(sourceTitleList);
    	
    	osr.setPublicationDate(datePublishedString);
    	
    	List<String> rightsTextList = new ArrayList<String>();
    	Set<Rights> rightsSet = duf.innerDerivedUnit().getRights();
    	for(Rights rights : rightsSet) {
    		if(rights.getAbbreviatedText() != null) {
    			rightsTextList.add(rights.getAbbreviatedText());
    		}
    	}
    	osr.setRights(rightsTextList);
    	response.add(osr);
    }

    public List<OccurrenceSearchResponse> getResponse() {
        return this.response;
    }


    public OccurrenceSearchResponse createResponse(DerivedUnitFacade duFacade) {
    	OccurrenceSearch.OccurrenceSearchResponse osResponse = 
    			new OccurrenceSearch.OccurrenceSearchResponse();
    	
    	return osResponse;
    }

    public class OccurrenceSearchResponse {
   
        private String acceptedTaxon;
        private String acceptedTaxonUuid;
        
        private String collector;
        private String collection;
        private String institution;
              
        private String fieldNotes;
        
        //
        private String type;
        private Object unitCount;
        private String kindOfUnit;

        //ELEVATION
        private Object elevation;
        private Object maxElevation;
        
        //DEPTH
        private Object depth;
        private Object maxDepth;

        private String startGatherinDate;
        private String endGatheringDate;
              
        private Location location;        	

        private String fieldNumber;        
        private String accessionNumber;
        private String catalogNumber;
        private String barcode;
        private String publicationDate;


		private List<String> rights;
        private List<String> sources;
        
        //FIXME: Ignoring the fields below for the moment
        //       Will come back to them when requested and
        //       when the model allows it properly
        
//      private String citation;

        public OccurrenceSearchResponse() {             	        	
        	location = new Location();
        	rights = new ArrayList<String>();
        	sources = new ArrayList<String>();

        }
        
        /**
         * @return the specimenOrObservationType
         */
        public String getType() {
            return type;
        }

        /**
         * @param specimenOrObservationType the specimenOrObservationType to set
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         * @return the acceptedTaxon
         */
        public String getAcceptedTaxon() {
            return acceptedTaxon;
        }

        /**
         * @param acceptedTaxon the acceptedTaxon to set
         */
        public void setAcceptedTaxon(String acceptedTaxon) {
            if (acceptedTaxon !=null) {
                this.acceptedTaxon = acceptedTaxon.split(" sec.")[0];
            } else {
                this.acceptedTaxon = acceptedTaxon;
            }
        }


        /**
         * @return the collector
         */
        public String getCollector() {
            return collector;
        }

        /**
         * @param collector the collector to set
         */
        public void setCollector(String collector) {
            this.collector = collector;
        }


        /**
         * @param acceptedTaxonUuid the acceptedTaxonUuid to set
         */
        public void setAcceptedTaxonUuid(String acceptedTaxonUuid) {
            this.acceptedTaxonUuid = acceptedTaxonUuid;
        }


        /**
         * @return the nameUuid
         */
        public String getAcceptedTaxonUuid() {
            return acceptedTaxonUuid;
        }


        /**
         * @return the accessionNumber
         */

        public String getAccessionNumber() {
            return accessionNumber;
        }

        /**
         * @param accessionNumber the accessionNumber to set
         */
   
        public void setAccessionNumber(String accessionNumber) {
            this.accessionNumber = accessionNumber;
        }

        /**
         * @return the catalogNumber
         */
        public String getCatalogNumber() {
            return catalogNumber;
        }

        /**
         * @param catalogNumber the catalogNumber to set
         */
        public void setCatalogNumber(String catalogNumber) {
            this.catalogNumber = catalogNumber;
        }

        /**
         * @return the fieldNumber
         */
        public String getFieldNumber() {
            return fieldNumber;
        }

        /**
         * @param fieldNumber the fieldNumber to set
         */
        public void setFieldNumber(String fieldNumber) {
            this.fieldNumber = fieldNumber;
        }

        /**
         * @return the minElevation
         */
        public Object getElevation() {
            return elevation;
        }

        /**
         * @param minElevation the minElevation to set
         */
        public void setElevation(Integer elevation) {
            this.elevation = elevation;
        }

        /**
         * @return the maxElevation
         */
        public Object getMaxElevation() {
            return maxElevation;
        }

        /**
         * @param maxElevation the maxElevation to set
         */
        public void setMaxElevation(Integer maxElevation) {
            this.maxElevation = maxElevation;
        }

        /**
         * @return the dateBegin
         */
        public String getStartGatheringDate() {
            return startGatherinDate;
        }

        /**
         * @param dateBegin the dateBegin to set
         */
        public void setStartGatheringDate(String dateBegin) {
            this.startGatherinDate = dateBegin;
        }

        /**
         * @return the dateEnd
         */
        public String getEndGatheringDate() {
            return endGatheringDate;
        }

        /**
         * @param dateEnd the dateEnd to set
         */
        public void setEndGatheringDate(String dateEnd) {
            this.endGatheringDate = dateEnd;
        }

        /**
         * @return the unitCount
         */
        public Object getUnitCount() {
            return unitCount;
        }

        /**
         * @param unitCount the unitCount to set
         */
        public void setUnitCount(Integer unitCount) {
            this.unitCount = unitCount;
        }

        /**
         * @return the barcode
         */
        public String getBarcode() {
            return barcode;
        }

        /**
         * @param barcode the barcode to set
         */
        public void setBarcode(String barcode) {
            this.barcode = barcode;
        }

        
		public String getPublicationDate() {
			return publicationDate;
		}
		
		public void setPublicationDate(String publicationDate) {
			this.publicationDate = publicationDate;
		}

        /**
         * @return the kindOfUnit
         */
        public String getKindOfUnit() {
            return kindOfUnit;
        }

        /**
         * @param kindOfUnit the kindOfUnit to set
         */
        public void setKindOfUnit(String kindOfUnit) {
            this.kindOfUnit = kindOfUnit;
        }

        /**
         * @return the sources
         */
        public List<String> getSources() {
            return sources;
        }

        /**
         * @param sources the sources to set
         */
        public void setSources(List<String> sources) {
            this.sources = sources;
        }

        /**
         * @return the collection
         */
        public String getCollection() {
            return collection;
        }

        /**
         * @param collection the collection to set
         */
        public void setCollection(String collection) {
            this.collection = collection;
        }

        /**
         * @return the institution
         */
        public String getInstitution() {
            return institution;
        }

        /**
         * @param institution the institution to set
         */
        public void setInstitution(String institution) {
            this.institution = institution;
        }

        /**
         * @return the depth
         */
        public Object getDepth() {
            return depth;
        }

        /**
         * @param depth the depth to set
         */
        public void setDepth(Double depth) {
            this.depth = depth;
        }

        /**
         * @return the maxDepth
         */
        public Object getMaxDepth() {
            return maxDepth;
        }

        /**
         * @param maxDepth the maxDepth to set
         */
        public void setMaxDepth(Double maxDepth) {
            this.maxDepth = maxDepth;
        }

        /**
         * @return the fieldNotes
         */
        public String getFieldNotes() {
            return fieldNotes;
        }

        /**
         * @param fieldNotes the fieldNotes to set
         */
        public void setFieldNotes(String fieldNotes) {
            this.fieldNotes = fieldNotes;
        }

//        /**
//         * @return the dataResourceCitation
//         */
//        public String getCitation() {
//            return citation;
//        }
//
//        /**
//         * @param dataResourceCitation the dataResourceCitation to set
//         */
//        public void setCitation(String citation) {
//            this.citation = citation;
//        }

        /**
         * @return the dataResourceRights
         */
        public List<String> getRights() {
            return rights;
        }

        /**
         * @param dataResourceRights the dataResourceRights to set
         */
        public void setRights(List<String> rights) {
            this.rights = rights;
        }

        public Location getLocation() {
			return location;
		}

		public void setLocation(Location location) {
			this.location = location;
		}

        public class Location {
        	private Object decimalLatitude;
			private Object decimalLongitude;
        	private Object errorRadius;
        	private String country;
        	private String locality;        	
        	private String referenceSystem;
        	
        	public Object getDecimalLatitude() {
				return decimalLatitude;
			}
			public void setDecimalLatitude(Double decimalLatitude) {
				this.decimalLatitude = decimalLatitude;
			}
			public Object getDecimalLongitude() {
				return decimalLongitude;
			}
			public void setDecimalLongitude(Double decimalLongitude) {
				this.decimalLongitude = decimalLongitude;
			}
			public Object getErrorRadius() {
				return errorRadius;
			}
			public void setErrorRadius(Integer errorRadius) {
				this.errorRadius = errorRadius;
			}
			public String getCountry() {
				return country;
			}
			public void setCountry(String country) {
				this.country = country;
			}
			public String getLocality() {
				return locality;
			}
			public void setLocality(String locality) {
				this.locality = locality;
			}
			public String getReferenceSystem() {
				return referenceSystem;
			}
			public void setReferenceSystem(String referenceSystem) {
				this.referenceSystem = referenceSystem;
			}

        }

    }
    
//    OccurrenceSearchResponse createOccurrence(OccurrenceSearchResponse res, String nameUuid,String titleCache, NamedArea countryNamedArea,
//            AgentBase<?> agent, LanguageString localityObject,Point coords, String fieldNumber, String accessionNumber,
//            String catalogNumber, String barcode, String specimenOrObservationType, Integer absoluteElevation, Integer absoluteElevationMax,
//            Double depthMax, Double depth,  String dateBegin, boolean dateFromPublication, String kindOfUnit, String unitCount,
//            Set<IdentifiableSource> sources,  String taxonName, Collection collection, String fieldNotes){
//        String country=null;
//        String collector= null;
//        String locality=null;
//        String longitude;
//        String latitude;
//        String errorRadius;
//        String referenceSystem;
//
//        res = new OccurrenceSearchResponse();
//        res.setTitle(titleCache);
//        res.setNameUUID(nameUuid);
//        res.setAcceptedTaxon(taxonName);
//
//        try {
//            country=countryNamedArea.getTitleCache();
//        } catch (Exception e) {
//            country=null;
//        }
//        //        if (country == null && countryarea !=null) {
//        //          res.setCountry(countryarea.getTitleCache());
//        //      } else {
//        res.setCountry(country);
//        //      }
//
//        try {
//            collector=agent.getTitleCache();
//        } catch (Exception e) {
//            collector=null;
//        }
//        res.setCollector(collector);
//
//        String collectionStr;
//        try {
//            collectionStr = collection.getTitleCache();
//        } catch (Exception e) {
//            collectionStr=null;
//        }
//        String institutionStr;
//        try {
//            Institution institution = collection.getInstitute();
//            institutionStr = institution.getTitleCache();
//        } catch (Exception e) {
//            institutionStr=null;
//        }
//
//        res.setCollection(collectionStr);
//        res.setInstitution(institutionStr);
//
//        try {
//            locality = localityObject.getText();
//        } catch (Exception e) {
//            locality=null;
//        }
//        res.setLocality(locality);
//
//        //COORDS
//        try {
//            longitude = coords.getLatitude().toString();
//        } catch (Exception e) {
//            longitude=null;
//        }
//        try {    latitude = coords.getLongitude().toString();
//        } catch (Exception e) {
//            latitude = null;
//        }
//
//        res.setLongitude(longitude);
//        res.setLatitude(latitude);
//
//        try {
//            errorRadius = coords.getErrorRadius().toString();
//        } catch (Exception e) {
//            errorRadius = null;
//        }
//        res.setErrorRadius(errorRadius);
//
//        try {
//            referenceSystem = coords.getReferenceSystem().getTitleCache();
//        } catch (Exception e) {
//            referenceSystem = null;
//        }
//        res.setReferenceSystem(referenceSystem);
//
//        //NUMBERS and IDS
//
//        res.setCatalogNumber(catalogNumber);
//        res.setFieldNumber(fieldNumber);
//        res.setAccessionNumber(accessionNumber);
//        res.setBarcode(barcode);
//
//        res.setFieldNotes(fieldNotes);
//
//        //SPECIMEN DATA
//        res.setSpecimenOrObservationType(specimenOrObservationType);
//        res.setUnitCount(unitCount);
//        res.setKindOfUnit(kindOfUnit);
//
//      //ELEVATION
//        String maxElevation=null;
//        try {
//            maxElevation=absoluteElevationMax.toString();
//        } catch (Exception e) {
//            maxElevation=null;
//        }
//        res.setMaxElevation(maxElevation);
//
//        String elevation;
//        try {
//            elevation=absoluteElevation.toString();
//        } catch (Exception e) {
//            elevation=null;
//        }
//        res.setElevation(elevation);
//
//      //DEPTH
//        String maxDepth;
//        String depthStr;
//        try {
//            maxDepth = depthMax.toString();
//        } catch (Exception e) {
//           maxDepth=null;
//        }
//        try {
//            depthStr = depth.toString();
//        } catch (Exception e) {
//            depthStr=null;
//        }
//
//        res.setMaxDepth(maxDepth);
//        res.setDepth(depthStr);
//
//        //DATE
//        res.setDateBegin(dateBegin);
//        res.setDateFromPublication(dateFromPublication);
//
//        //SOURCES
//        List<String> sourceTitleList = new ArrayList<String>();
//
//        String citation;
//        String micro;
//
//        boolean dateFound=false;
//        for (IdentifiableSource source:sources){
//
//            try {
//                citation = source.getCitation().getTitleCache();
//                if (dateFromPublication && !dateFound){
//                    String datePublishedString = source.getCitation().getDatePublishedString();
//                    if (!StringUtils.isEmpty(datePublishedString)){
//                        res.setDateBegin(datePublishedString);
//                        dateFound=true;
//                    }
//                }
//            } catch (Exception e) {
//                // TODO Auto-generated catch block
//                citation=null;
//            }
//            try {
//                micro = source.getCitationMicroReference();
//            } catch (Exception e) {
//                micro=null;
//            }
//
//            if(citation !=null && micro !=null) {
//                sourceTitleList.add(citation+" "+micro);
//            } else
//                if(citation !=null ) {
//                    sourceTitleList.add(citation);
//                }
//            if (micro !=null) {
//                sourceTitleList.add(micro);
//            }
//
//        }
//        res.setSources(sourceTitleList);
//
//
//        return res;
//    }
//
//    /**
//     * @param nameUuid
//     * @param facade
//     * @return
//     */
//    public OccurrenceSearchResponse createOccurrence(String nameUuid, DerivedUnit derivedUnit, DerivedUnitFacade facade,
//            SpecimenOrObservationBase<?> specimen, String taxonName) {
//
//        OccurrenceSearchResponse res = responseWithUUID(nameUuid);
//        boolean dateFromPublication=false;
//
//        if (res == null) {
//
//            //NUMBERS
//
//            String accessionNumber;
//            try {
//                accessionNumber = derivedUnit.getAccessionNumber();
//            } catch (Exception e) {
//                accessionNumber=null;
//            }
//            String catalogNumber;
//            try {
//                catalogNumber = facade.getCatalogNumber();
//            } catch (Exception e) {
//                catalogNumber=null;
//            }
//
//            Collection collection = facade.getCollection();
//
//            //FIELD UNIT RELATED DATA
//            String fieldNumber=null;
//            FieldUnit fu;
//            try{
//                fu=facade.getFieldUnit(false);
//            }catch(Exception e){
//                fu=null;
//            }
//
//            try{
//                fieldNumber=fu.getFieldNumber();
//            }catch(Exception e){
//                fieldNumber=null;
//            }
//
//            String barcode;
//            try {
//                barcode=derivedUnit.getBarcode();
//            } catch (Exception e1) {
//                barcode=null;
//            }
//
//
//            String kindOfUnit;
//            try {
//                kindOfUnit = fu.getKindOfUnit().toString();
//            } catch (Exception e2) {
//                kindOfUnit=null;
//            }
//
//            String unitCount;
//            try {
//                unitCount = fu.getIndividualCount().toString();
//            } catch (Exception e2) {
//                unitCount=null;
//            }
//
//
//            String specimenOrObservationType=null;
//            try{
//                specimenOrObservationType = facade.getType().toString();
//            }catch(Exception e){
//                specimenOrObservationType=null;
//            }
//
//            //FIELD NOTES
//            String fieldNotes = facade.getFieldNotes();
//
//            //ELEVATION
//            Integer maxElevation=null;
//            Integer elevation=null;
//
//            GatheringEvent ge;
//            try {
//                ge=facade.getGatheringEvent(false);
//            } catch (Exception e1) {
//                ge=null;
//            }
//            try {
//                maxElevation=ge.getAbsoluteElevationMax();
//            } catch (Exception e) {
//                maxElevation=null;
//            }
//            try {
//                elevation=ge.getAbsoluteElevation();
//            } catch (Exception e) {
//                elevation=null;
//            }
//
//            Double depthMax;
//            Double depth;
//            try {
//                depthMax = ge.getDistanceToGroundMax();
//            } catch (Exception e1) {
//                depthMax=null;
//            }
//            try {
//                depth = ge.getDistanceToGround();
//            } catch (Exception e1) {
//                depth=null;
//            }
//
//            //DATE
//            String dateBegin;
//            try {
//                dateBegin=ge.getGatheringDate().toString();
//            } catch (Exception e) {
//                dateBegin=null;
//            }
//
//            if (dateBegin == null){
//                //TODO GET DATE FROM PUBLICATION
//                dateFromPublication = true;
//            }
//
//            //SOURCE
//            Set<IdentifiableSource> sources = derivedUnit.getSources();
//
//            sources.addAll(specimen.getSources());
//            sources.addAll(facade.getSources());
//            try {
//                sources.addAll(fu.getSources());
//            } catch (Exception e) {
//                //DO NOTHING
//            }
//
//
//
//
//            return createOccurrence(res,  nameUuid,facade.getTitleCache(),facade.getCountry(),facade.getCollector(),
//                    facade.getLocality(),facade.getExactLocation(), fieldNumber, accessionNumber, catalogNumber, barcode,
//                    specimenOrObservationType, elevation, maxElevation, depthMax, depth, dateBegin,dateFromPublication, kindOfUnit,
//                    unitCount, sources,taxonName, collection,fieldNotes);
//        }
//        return res;
//    }
//
//    public OccurrenceSearchResponse createOccurrence(String nameUuid, SpecimenOrObservationBase<?> specimen, GatheringEvent gatheringEvent,
//            String taxonName) {
//
//        OccurrenceSearchResponse res = responseWithUUID(nameUuid);
//        boolean dateFromPublication=false;
//
//        if (res == null) {
//
//            //NUMBERS
//
//            String accessionNumber;
//            DerivedUnit du;
//            try {
//                du = (DerivedUnit) specimen;
//            } catch (Exception e1) {
//                du=null;
//            }
//
//            try {
//                accessionNumber = du.getAccessionNumber();
//            } catch (Exception e) {
//                accessionNumber=null;
//            }
//            String catalogNumber;
//            try {
//                catalogNumber = du.getCatalogNumber();
//            } catch (Exception e) {
//                catalogNumber=null;
//            }
//
//            String barcode;
//            try {
//                barcode=du.getBarcode();
//            } catch (Exception e1) {
//                barcode=null;
//            }
//
//            String fieldNumber=null;
//            FieldUnit fu;
//            try{
//                fu=(FieldUnit)specimen;
//            }catch(Exception e){fu=null;}
//
//            try{
//                fieldNumber=fu.getFieldNumber();
//            }catch(Exception e){
//                fieldNumber=null;
//            }
//
//            String kindOfUnit;
//            try {
//                kindOfUnit = fu.getKindOfUnit().toString();
//            } catch (Exception e2) {
//                kindOfUnit=null;
//            }
//
//            String fieldNotes;
//            try {
//                fieldNotes=fu.getFieldNotes();
//            } catch (Exception e) {
//                fieldNotes=null;
//            }
//
//            String unitCount;
//            try {
//                unitCount = fu.getIndividualCount().toString();
//            } catch (Exception e2) {
//                unitCount=null;
//            }
//
//
//            String specimenOrObservationType=null;
//            try{
//                specimenOrObservationType = specimen.getRecordBasis().toString();
//            }catch(Exception e){
//                specimenOrObservationType=null;
//            }
//
//
//            Integer absoluteElevationMax = gatheringEvent.getAbsoluteElevationMax();
//            Integer elevation = gatheringEvent.getAbsoluteElevation();
//
//            Double depthMax = gatheringEvent.getDistanceToGroundMax();
//            Double depth = gatheringEvent.getDistanceToGround();
//
//
//            //DATE
//            String dateBegin;
//            try {
//                dateBegin=gatheringEvent.getGatheringDate().toString();
//            } catch (Exception e) {
//                dateBegin=null;
//            }
//            String dateEnd;
//            if (dateBegin == null){
//                //TODO GET DATE FROM PUBLICATION
//                dateFromPublication = true;
//            }
//
//            Collection collection;
//            try {
//                collection = du.getCollection();
//            } catch (Exception e) {
//               collection=null;
//            }
//
//            Set<IdentifiableSource> sources = specimen.getSources();
//            try {
//                sources.addAll(du.getSources());
//            } catch (Exception e) {
//                //DO NOTHING
//            }
//            try {
//                sources.addAll(fu.getSources());
//            } catch (Exception e) {
//                //DO NOTHING
//            }
//
//            return createOccurrence(res,  nameUuid,specimen.getTitleCache(),gatheringEvent.getCountry(),gatheringEvent.getCollector(),
//                    gatheringEvent.getLocality(),gatheringEvent.getExactLocation(), fieldNumber, accessionNumber, catalogNumber,barcode,
//                    specimenOrObservationType, elevation, absoluteElevationMax,depthMax, depth, dateBegin,dateFromPublication, kindOfUnit, unitCount, sources,
//                    taxonName, collection, fieldNotes);
//        }
//        return res;
//    }


//    private OccurrenceSearchResponse responseWithtitle(String title) {
//        for(OccurrenceSearch.OccurrenceSearchResponse nsres : response) {
//            if(nsres.getTitle().trim().equals(title.trim())) {
//                return nsres;
//            }
//        }
//        return null;
//    }
//
//    private OccurrenceSearchResponse responseWithUUID(String nameUUID) {
//        for(OccurrenceSearch.OccurrenceSearchResponse nsres : response) {
//            if(nsres.getAcceptedTaxon().trim().equals(nameUUID.trim())) {
//                return nsres;
//            }
//        }
//        return null;
//    }





}



