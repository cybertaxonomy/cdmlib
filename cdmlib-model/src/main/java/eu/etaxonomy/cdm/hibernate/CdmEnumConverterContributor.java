/**
 * Copyright (C) 2026 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.hibernate;

import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.spi.MetadataBuilderContributor;
import org.hibernate.boot.spi.MetadataBuilderInitializer;

import eu.etaxonomy.cdm.model.common.AuthorityType;
import eu.etaxonomy.cdm.model.common.ExternallyManagedImport;
import eu.etaxonomy.cdm.model.common.TriState;
import eu.etaxonomy.cdm.model.description.NoDescriptiveDataStatus;
import eu.etaxonomy.cdm.model.media.ExternalLinkType;
import eu.etaxonomy.cdm.model.metadata.CdmMetaDataPropertyName;
import eu.etaxonomy.cdm.model.molecular.SequenceDirection;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalCodeEdition;
import eu.etaxonomy.cdm.model.name.NomenclaturalStanding;
import eu.etaxonomy.cdm.model.name.RankClass;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameGender;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.permission.PermissionClass;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeStatus;
import eu.etaxonomy.cdm.model.taxon.TaxonStatus;
import eu.etaxonomy.cdm.model.taxon.TaxonomicOperationType;
import eu.etaxonomy.cdm.model.term.TermType;

/**
 * Registers nested {@code @Converter(autoApply = true)} enum converters.
 * <p>
 * CDM configures Hibernate via Spring {@code LocalSessionFactoryBean} /
 * native {@code Configuration} with an explicit class list (no JPA classpath
 * scan). Nested converters inside enums are therefore not discovered.
 * Without registration, Hibernate falls back to ordinal {@code SMALLINT}
 * mapping and fails reading VARCHAR keys such as {@code "LA"}.
 * <p>
 * Discovered via {@link MetadataBuilderInitializer} (Java ServiceLoader),
 * which is applied on both native and JPA bootstrap. Also implements
 * {@link MetadataBuilderContributor} for the JPA
 * {@code hibernate.metadata_builder_contributor} property.
 *
 * @author muellera
 * @since 09.10.2026
 */
public class CdmEnumConverterContributor
        implements MetadataBuilderInitializer, MetadataBuilderContributor {

    @Override
    public void contribute(MetadataBuilder metadataBuilder, StandardServiceRegistry serviceRegistry) {
        contribute(metadataBuilder);
    }

    @Override
    public void contribute(MetadataBuilder metadataBuilder) {
        // force autoApply=true (do not rely on annotation discovery alone)
        metadataBuilder.applyAttributeConverter(AuthorityType.AuthorityTypeConverter.class, true);
        metadataBuilder.applyAttributeConverter(ExternallyManagedImport.ExternallyManagedImportConverter.class, true);
        metadataBuilder.applyAttributeConverter(TriState.TriStateConverter.class, true);
        metadataBuilder.applyAttributeConverter(NoDescriptiveDataStatus.NoDescriptiveDataStatusConverter.class, true);
        metadataBuilder.applyAttributeConverter(ExternalLinkType.ExternalLinkTypeConverter.class, true);
        metadataBuilder.applyAttributeConverter(CdmMetaDataPropertyName.CdmMetaDataPropertyNameConverter.class, true);
        metadataBuilder.applyAttributeConverter(SequenceDirection.SequenceDirectionConverter.class, true);
        metadataBuilder.applyAttributeConverter(NomenclaturalCode.NomenclaturalCodeConverter.class, true);
        metadataBuilder.applyAttributeConverter(NomenclaturalCodeEdition.NomenclaturalCodeEditionConverter.class, true);
        metadataBuilder.applyAttributeConverter(NomenclaturalStanding.NomenclaturalStandingConverter.class, true);
        metadataBuilder.applyAttributeConverter(RankClass.RankClassConverter.class, true);
        metadataBuilder.applyAttributeConverter(RegistrationStatus.RegistrationStatusConverter.class, true);
        metadataBuilder.applyAttributeConverter(TaxonNameGender.TaxonNameGenderConverter.class, true);
        metadataBuilder.applyAttributeConverter(SpecimenOrObservationType.SpecimenOrObservationTypeConverter.class, true);
        metadataBuilder.applyAttributeConverter(PermissionClass.PermissionClassConverter.class, true);
        metadataBuilder.applyAttributeConverter(OriginalSourceType.OriginalSourceTypeConverter.class, true);
        metadataBuilder.applyAttributeConverter(ReferenceType.ReferenceTypeConverter.class, true);
        metadataBuilder.applyAttributeConverter(SynonymType.SynonymTypeConverter.class, true);
        metadataBuilder.applyAttributeConverter(TaxonNodeStatus.TaxonNodeStatusConverter.class, true);
        metadataBuilder.applyAttributeConverter(TaxonStatus.TaxonStatusConverter.class, true);
        metadataBuilder.applyAttributeConverter(TaxonomicOperationType.TaxonomicOperationTypeConverter.class, true);
        metadataBuilder.applyAttributeConverter(TermType.TermTypeConverter.class, true);
    }
}
