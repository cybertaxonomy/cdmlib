/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.media.MediaUtils;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.molecular.SingleRead;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.term.DefinedTerm;

/**
 * Provides summary information on media, DNA, molecular data and scans
 * found in the derivation tree including the current {@link SpecimenOrObservationBase}
 * instance.
 *
 * @author pplitzner
 * @since Mar 26, 2015
 */
public class DerivationTreeSummaryDTO implements Serializable {

    private static final long serialVersionUID = 8329871164348514709L;

    private static final Logger logger = LogManager.getLogger();

    private List<Link> specimenScans = new ArrayList<>();
    private List<MolecularData> molecularDataList = new ArrayList<>();
    private List<Link> detailImages = new ArrayList<>();
    private List<UUID> specimenScanUuids = new ArrayList<>();
    private List<UUID> detailImageUuids = new ArrayList<>();

    /**
     * Factory method to create the summary information on media, DNA, molecular data and scans
     * found in the derivation tree including the passed {@link SpecimenOrObservationBase specimenOrObservation}
     *
     * @param specimenOrObservation
     *      The {@link SpecimenOrObservationBase} to create the summary for.
     * @param specimenIdentifier
     *      In case of a {@link DerivedUnit} being passed to this factory method, the <code>specimenIdentifier</code> should
     *      be set to the result of {@link DerivedUnitDTO#composeSpecimenShortTitle(DerivedUnit)}
     * @return
     *      The new instance.
     */
    public static DerivationTreeSummaryDTO fromEntity(SpecimenOrObservationBase<?> specimenOrObservation, String specimenIdentifier) {

        DerivationTreeSummaryDTO derivateDataDTO = new DerivationTreeSummaryDTO();

        Collection<DerivedUnit> childDerivates = specimenOrObservation.collectDerivedUnits(0);
        for (DerivedUnit childDerivate : childDerivates) {
            // assemble molecular data
            //pattern: DNAMarker [contig1, primer1_1, primer1_2, ...][contig2, primer2_1, ...]...
            if (childDerivate.isInstanceOf(DnaSample.class)) {
                if (childDerivate.getRecordBasis() == SpecimenOrObservationType.TissueSample) {
                    // TODO implement TissueSample assembly for web service
                }
                if (childDerivate.getRecordBasis() == SpecimenOrObservationType.DnaSample) {
                    DnaSample dna = HibernateProxyHelper.deproxy(childDerivate, DnaSample.class);
                    for (Sequence sequence : dna.getSequences()) {
                        URI boldUri = null;
                        try {
                            boldUri = sequence.getBoldUri();
                        } catch (URISyntaxException e1) {
                            // TODO consider better reporting of this incident
                            logger.error("Could not create BOLD URI", e1);
                        }
                        final DefinedTerm dnaMarker = sequence.getDnaMarker();
                        Link providerLink = null;
                        if(boldUri!=null && dnaMarker!=null){
                            providerLink = new DerivationTreeSummaryDTO.Link(boldUri, dnaMarker.getLabel());
                        }
                        MolecularData molecularData = derivateDataDTO.addProviderLink(providerLink);

                        //contig file
                        ContigFile contigFile = null;
                        if (sequence.getContigFile() != null) {
                            MediaRepresentationPart contigMediaRepresentationPart = MediaUtils.getFirstMediaRepresentationPart(sequence.getContigFile());
                            if (contigMediaRepresentationPart != null) {
                                contigFile = molecularData.addContigFile(new Link(contigMediaRepresentationPart.getUri(), "contig"));
                            }
                        }
                        else{
                            contigFile = molecularData.addContigFile(null);
                        }
                        // primer files
                        if (sequence.getSingleReads() != null) {
                            int readCount = 1;
                            for (SingleRead singleRead : sequence.getSingleReads()) {
                                MediaRepresentationPart pherogramMediaRepresentationPart = MediaUtils.getFirstMediaRepresentationPart(singleRead.getPherogram());
                                if (pherogramMediaRepresentationPart != null && contigFile != null) {
                                    contigFile.addPrimerLink(pherogramMediaRepresentationPart.getUri(), "read"+readCount++);
                                }
                            }
                        }
                    }
                }
            }
            // assemble media data
            else if (childDerivate.isInstanceOf(MediaSpecimen.class)) {
                MediaSpecimen mediaSpecimen = HibernateProxyHelper.deproxy(childDerivate, MediaSpecimen.class);

                URI mediaUri = getMediaUri(mediaSpecimen);
                if (mediaSpecimen.getKindOfUnit() != null) {
                    // specimen scan
                    if (mediaSpecimen.getKindOfUnit().getUuid().equals(DefinedTerm.uuidSpecimenScan)) {
                        derivateDataDTO.addSpecimenScanUuid(mediaSpecimen.getMediaSpecimen().getUuid());
                        String imageLinkText = "scan of " + specimenIdentifier;
                        if(CdmUtils.isNotBlank(mediaSpecimen.getMostSignificantIdentifier())) {
                            imageLinkText = mediaSpecimen.getMostSignificantIdentifier();
                        }
                        if(CdmUtils.isNotBlank(mediaSpecimen.getMediaSpecimen().getTitleCache())) {
                            imageLinkText += " (" + mediaSpecimen.getMediaSpecimen().getTitleCache() + ")";
                        }
                        derivateDataDTO.addSpecimenScan(mediaUri, imageLinkText.trim());
                    }
                    // detail image
                    else if (mediaSpecimen.getKindOfUnit().getUuid().equals(DefinedTerm.uuidDetailImage)) {
                        derivateDataDTO.addDetailImageUuid(mediaSpecimen.getMediaSpecimen().getUuid());
                        String motif = "detail image";
                        if (mediaSpecimen.getMediaSpecimen()!=null){
                            if(CdmUtils.isNotBlank(mediaSpecimen.getMediaSpecimen().getTitleCache())) {
                                motif = mediaSpecimen.getMediaSpecimen().getTitleCache();
                            }
                        }
                        derivateDataDTO.addDetailImage(mediaUri, motif);
                    }
                }
            }
        }
        return derivateDataDTO;
    }

//    private void updateDerivateTree(Set<DerivedUnitDTO> derivatives) {
//    	 for (DerivedUnitDTO childDerivate : derivatives) {
//    		 DerivationTreeSummaryDTO childTree = childDerivate.getDerivationTreeSummary();
//    		 for (Link link:childTree.detailImages) {
//    			 this.addDetailImage(null, null);
//    		 }
//    	 }
//
//             // assemble molecular data
//             //pattern: DNAMarker [contig1, primer1_1, primer1_2, ...][contig2, primer2_1, ...]...
//             if (childDerivate instanceof DNASampleDTO) {
//            	 DNASampleDTO dna = (DNASampleDTO)childDerivate;
//                 if (childDerivate.getRecordBase() == SpecimenOrObservationType.TissueSample) {
//                     // TODO implement TissueSample assembly for web service
//                 }
//                 if (childDerivate.getRecordBase() == SpecimenOrObservationType.DnaSample) {
//
//                     for (SequenceDTO sequence : dna.getSequences()) {
//                         URI boldUri = null;
//                         try {
//                             boldUri = sequence.getBoldUri();
//                         } catch (URISyntaxException e1) {
//                             // TODO consider better reporting of this incident
//                             logger.error("Could not create BOLD URI", e1);
//                         }
//                         final String dnaMarker = sequence.getDnaMarker();
//                         Link providerLink = null;
//                         if(boldUri!=null && dnaMarker!=null){
//                             providerLink = new DerivationTreeSummaryDTO.Link(boldUri, dnaMarker);
//                         }
//                         MolecularData molecularData = this.addProviderLink(providerLink);
//
//                         //contig file
//                         ContigFile contigFile = null;
//                         if (sequence.getContigFile() != null) {
//                             MediaRepresentationPart contigMediaRepresentationPart = MediaUtils.getFirstMediaRepresentationPart(sequence.getContigFile());
//                             if (contigMediaRepresentationPart != null) {
//                                 contigFile = molecularData.addContigFile(new Link(contigMediaRepresentationPart.getUri(), "contig"));
//                             }
//                         }
//                         else{
//                             contigFile = molecularData.addContigFile(null);
//                         }
//                         // primer files
//                         if (sequence.getSingleReadAlignments() != null) {
//                             int readCount = 1;
//                             for (SingleReadAlignment singleRead : sequence.getSingleReadAlignments()) {
//                                 MediaRepresentationPart pherogramMediaRepresentationPart = MediaUtils.getFirstMediaRepresentationPart(singleRead.getSingleRead().getPherogram());
//                                 if (pherogramMediaRepresentationPart != null && contigFile != null) {
//                                     contigFile.addPrimerLink(pherogramMediaRepresentationPart.getUri(), "read"+readCount++);
//                                 }
//                             }
//                         }
//                     }
//                 }
//             }
//             // assemble media data
//             else if (childDerivate.hasDetailImage) {
//
//
//                     // specimen scan
//                     if (childDerivate.getKindOfUnit().getUuid().equals(DefinedTerm.uuidSpecimenScan)) {
//                         this.addSpecimenScanUuid(childDerivate.get);
//                         String imageLinkText = "scan of " + specimenIdentifier;
//                         if(CdmUtils.isNotBlank(mediaSpecimen.getMostSignificantIdentifier())) {
//                             imageLinkText = mediaSpecimen.getMostSignificantIdentifier();
//                         }
//                         if(CdmUtils.isNotBlank(mediaSpecimen.getMediaSpecimen().getTitleCache())) {
//                             imageLinkText += " (" + mediaSpecimen.getMediaSpecimen().getTitleCache() + ")";
//                         }
//                         this.addSpecimenScan(mediaUri, imageLinkText.trim());
//                     }
//                     // detail image
//                     else if (mediaSpecimen.getKindOfUnit().getUuid().equals(DefinedTerm.uuidDetailImage)) {
//                         derivateDataDTO.addDetailImageUuid(mediaSpecimen.getMediaSpecimen().getUuid());
//                         String motif = "detail image";
//                         if (mediaSpecimen.getMediaSpecimen()!=null){
//                             if(CdmUtils.isNotBlank(mediaSpecimen.getMediaSpecimen().getTitleCache())) {
//                                 motif = mediaSpecimen.getMediaSpecimen().getTitleCache();
//                             }
//                         }
//                         derivateDataDTO.addDetailImage(mediaUri, motif);
//                     }
//                 }
//             }
//         }
//    }

    @Deprecated
    private static URI getMediaUri(MediaSpecimen mediaSpecimen) {
        URI mediaUri = null;
        Collection<MediaRepresentation> mediaRepresentations = mediaSpecimen.getMediaSpecimen().getRepresentations();
        if (mediaRepresentations != null && !mediaRepresentations.isEmpty()) {
            Collection<MediaRepresentationPart> mediaRepresentationParts = mediaRepresentations.iterator().next().getParts();
            if (mediaRepresentationParts != null && !mediaRepresentationParts.isEmpty()) {
                MediaRepresentationPart part = mediaRepresentationParts.iterator().next();
                if (part.getUri() != null) {
                    mediaUri = part.getUri();
                }
            }
        }
        return mediaUri;
    }

    /**
     * @return the molecularData
     */
    public List<MolecularData> getMolecularDataList() {
        return molecularDataList;
    }

    public MolecularData addProviderLink(Link providerLink) {
        MolecularData molecularData = new MolecularData(providerLink);
        this.molecularDataList.add(molecularData);
        return molecularData;
    }

    /**
     * @return the specimenScans
     */
    public List<Link> getSpecimenScans() {
        return specimenScans;
    }

    public void addSpecimenScan(URI uri, String linkText){
        specimenScans.add(new Link(uri, linkText));
    }

    /**
     * @return the detailImages
     */
    public List<Link> getDetailImages() {
        return detailImages;
    }

    public void addDetailImage(URI uri, String motif){
        detailImages.add(new Link(uri, motif));
    }

    /**
     * @return the specimenScanUuids
     */
    public List<UUID> getSpecimenScanUuids() {
        return specimenScanUuids;
    }

    public void addSpecimenScanUuid(UUID uuid){
        specimenScanUuids.add(uuid);
    }

    /**
     * @return the detailImageUuids
     */
    public List<UUID> getDetailImageUuids() {
        return detailImageUuids;
    }

    public void addDetailImageUuid(UUID uuid){
        detailImageUuids.add(uuid);
    }

    public class MolecularData implements Serializable{

        private static final long serialVersionUID = -3509828381796659200L;

        private final Link providerLink;
        private List<ContigFile> contigFiles;

        public MolecularData(Link providerLink) {
            super();
            this.providerLink = providerLink;
        }

        public ContigFile addContigFile(Link contigLink){
            if(contigFiles==null){
                contigFiles = new ArrayList<ContigFile>();
            }
            ContigFile contigFile = new ContigFile(contigLink);
            contigFiles.add(contigFile);
            return contigFile;
        }

        public synchronized Link getProviderLink() {
            return providerLink;
        }

        public List<ContigFile> getContigFiles() {
            return contigFiles;
        }

    }

    public class ContigFile implements Serializable{

        private final Link contigLink;
        private List<Link> primerLinks;

        public ContigFile(Link contigLink) {
            this.contigLink = contigLink;
        }

        public void addPrimerLink(URI uri, String linkText){
            if(primerLinks==null){
                primerLinks = new ArrayList<Link>();
            }
            primerLinks.add(new Link(uri, linkText));
        }

        public Link getContigLink() {
            return contigLink;
        }

        public List<Link> getPrimerLinks() {
            return primerLinks;
        }

    }

    public static class Link implements Serializable{

        private static final long serialVersionUID = 6635385359662624579L;

        private String linkText;
        private URI uri;

        public Link(URI uri, String linkText) {
            super();
            this.linkText = linkText;
            this.uri = uri;
        }
        /**
         * @return the linkText
         */
        public synchronized String getLinkText() {
            return linkText;
        }
        /**
         * @param linkText the linkText to set
         */
        public synchronized void setLinkText(String linkText) {
            this.linkText = linkText;
        }
        /**
         * @return the uri
         */
        public synchronized URI getUri() {
            return uri;
        }
        /**
         * @param uri the uri to set
         */
        public synchronized void setUri(URI uri) {
            this.uri = uri;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((linkText == null) ? 0 : linkText.hashCode());
            result = prime * result + ((uri == null) ? 0 : uri.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Link other = (Link) obj;
            if (linkText == null) {
                if (other.linkText != null) {
                    return false;
                }
            } else if (!linkText.equals(other.linkText)) {
                return false;
            }
            if (uri == null) {
                if (other.uri != null) {
                    return false;
                }
            } else if (!uri.equals(other.uri)) {
                return false;
            }
            return true;
        }
    }


}
