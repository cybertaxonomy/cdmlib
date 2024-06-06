/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.net.URISyntaxException;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.api.dto.DerivationTreeSummaryDTO;
import eu.etaxonomy.cdm.api.dto.DerivationTreeSummaryDTO.ContigFile;
import eu.etaxonomy.cdm.api.dto.DerivationTreeSummaryDTO.Link;
import eu.etaxonomy.cdm.api.dto.DerivationTreeSummaryDTO.MolecularData;
import eu.etaxonomy.cdm.api.dto.DerivedUnitDTO;
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
 * Loader for {@link DerivationTreeSummaryDTO}s. Extracted from DTO class.
 *
 * @author muellera
 * @since 13.02.2024
 */
public class DerivationTreeSummaryDtoLoader {

    private static final Logger logger = LogManager.getLogger();

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
    public static DerivationTreeSummaryDTO fromEntity(SpecimenOrObservationBase<?> specimenOrObservation,
            String specimenIdentifier) {

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

    //TODO AM why deprecated?
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

    //TODO AM can this be deleted?
//  private void updateDerivateTree(Set<DerivedUnitDTO> derivatives) {
//     for (DerivedUnitDTO childDerivate : derivatives) {
//         DerivationTreeSummaryDTO childTree = childDerivate.getDerivationTreeSummary();
//         for (Link link:childTree.detailImages) {
//             this.addDetailImage(null, null);
//         }
//     }
//
//           // assemble molecular data
//           //pattern: DNAMarker [contig1, primer1_1, primer1_2, ...][contig2, primer2_1, ...]...
//           if (childDerivate instanceof DNASampleDTO) {
//             DNASampleDTO dna = (DNASampleDTO)childDerivate;
//               if (childDerivate.getRecordBase() == SpecimenOrObservationType.TissueSample) {
//                   // TODO implement TissueSample assembly for web service
//               }
//               if (childDerivate.getRecordBase() == SpecimenOrObservationType.DnaSample) {
//
//                   for (SequenceDTO sequence : dna.getSequences()) {
//                       URI boldUri = null;
//                       try {
//                           boldUri = sequence.getBoldUri();
//                       } catch (URISyntaxException e1) {
//                           // TODO consider better reporting of this incident
//                           logger.error("Could not create BOLD URI", e1);
//                       }
//                       final String dnaMarker = sequence.getDnaMarker();
//                       Link providerLink = null;
//                       if(boldUri!=null && dnaMarker!=null){
//                           providerLink = new DerivationTreeSummaryDTO.Link(boldUri, dnaMarker);
//                       }
//                       MolecularData molecularData = this.addProviderLink(providerLink);
//
//                       //contig file
//                       ContigFile contigFile = null;
//                       if (sequence.getContigFile() != null) {
//                           MediaRepresentationPart contigMediaRepresentationPart = MediaUtils.getFirstMediaRepresentationPart(sequence.getContigFile());
//                           if (contigMediaRepresentationPart != null) {
//                               contigFile = molecularData.addContigFile(new Link(contigMediaRepresentationPart.getUri(), "contig"));
//                           }
//                       }
//                       else{
//                           contigFile = molecularData.addContigFile(null);
//                       }
//                       // primer files
//                       if (sequence.getSingleReadAlignments() != null) {
//                           int readCount = 1;
//                           for (SingleReadAlignment singleRead : sequence.getSingleReadAlignments()) {
//                               MediaRepresentationPart pherogramMediaRepresentationPart = MediaUtils.getFirstMediaRepresentationPart(singleRead.getSingleRead().getPherogram());
//                               if (pherogramMediaRepresentationPart != null && contigFile != null) {
//                                   contigFile.addPrimerLink(pherogramMediaRepresentationPart.getUri(), "read"+readCount++);
//                               }
//                           }
//                       }
//                   }
//               }
//           }
//           // assemble media data
//           else if (childDerivate.hasDetailImage) {
//
//
//                   // specimen scan
//                   if (childDerivate.getKindOfUnit().getUuid().equals(DefinedTerm.uuidSpecimenScan)) {
//                       this.addSpecimenScanUuid(childDerivate.get);
//                       String imageLinkText = "scan of " + specimenIdentifier;
//                       if(CdmUtils.isNotBlank(mediaSpecimen.getMostSignificantIdentifier())) {
//                           imageLinkText = mediaSpecimen.getMostSignificantIdentifier();
//                       }
//                       if(CdmUtils.isNotBlank(mediaSpecimen.getMediaSpecimen().getTitleCache())) {
//                           imageLinkText += " (" + mediaSpecimen.getMediaSpecimen().getTitleCache() + ")";
//                       }
//                       this.addSpecimenScan(mediaUri, imageLinkText.trim());
//                   }
//                   // detail image
//                   else if (mediaSpecimen.getKindOfUnit().getUuid().equals(DefinedTerm.uuidDetailImage)) {
//                       derivateDataDTO.addDetailImageUuid(mediaSpecimen.getMediaSpecimen().getUuid());
//                       String motif = "detail image";
//                       if (mediaSpecimen.getMediaSpecimen()!=null){
//                           if(CdmUtils.isNotBlank(mediaSpecimen.getMediaSpecimen().getTitleCache())) {
//                               motif = mediaSpecimen.getMediaSpecimen().getTitleCache();
//                           }
//                       }
//                       derivateDataDTO.addDetailImage(mediaUri, motif);
//                   }
//               }
//           }
//       }
//  }
}