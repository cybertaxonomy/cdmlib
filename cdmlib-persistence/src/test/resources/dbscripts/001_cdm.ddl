	create table Address (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        locality varchar(255),
        location_errorradius integer,
        location_latitude double,
        location_longitude double,
        pobox varchar(255),
        postcode varchar(255),
        region varchar(255),
        street varchar(255),
        createdby_id integer,
        updatedby_id integer,
        country_id integer,
        location_referencesystem_id integer,
        primary key (id),
        unique (uuid)
    );

    create table Address_AUD (
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        locality varchar(255),
        location_errorradius integer,
        location_latitude double,
        location_longitude double,
        pobox varchar(255),
        postcode varchar(255),
        region varchar(255),
        street varchar(255),
        createdby_id integer,
        updatedby_id integer,
        country_id integer,
        location_referencesystem_id integer,
        primary key (id, REV)
    );

    create table AgentBase (
        DTYPE varchar(31) not null,
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        lsid_authority varchar(255),
        lsid_lsid varchar(255),
        lsid_namespace varchar(255),
        lsid_object varchar(255),
        lsid_revision varchar(255),
        protectedtitlecache bit not null,
        titleCache varchar(255),
        code varchar(255),
        name varchar(255),
        nomenclaturaltitle varchar(255),
        firstname varchar(255),
        lastname varchar(255),
        lifespan_end varchar(255),
        lifespan_freetext varchar(255),
        lifespan_start varchar(255),
        prefix varchar(255),
        suffix varchar(255),
        protectednomenclaturaltitlecache bit,
        createdby_id integer,
        updatedby_id integer,
        ispartof_id integer,
        primary key (id),
        unique (uuid)
    );

    create table AgentBase_AUD (
        DTYPE varchar(31) not null,
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        lsid_authority varchar(255),
        lsid_lsid varchar(255),
        lsid_namespace varchar(255),
        lsid_object varchar(255),
        lsid_revision varchar(255),
        protectedtitlecache bit,
        titleCache varchar(255),
        createdby_id integer,
        updatedby_id integer,
        code varchar(255),
        name varchar(255),
        ispartof_id integer,
        nomenclaturaltitle varchar(255),
        firstname varchar(255),
        lastname varchar(255),
        lifespan_end varchar(255),
        lifespan_freetext varchar(255),
        lifespan_start varchar(255),
        prefix varchar(255),
        suffix varchar(255),
        protectednomenclaturaltitlecache bit,
        primary key (id, REV)
    );

    create table AgentBase_Address (
        AgentBase_id integer not null,
        contact_addresses_id integer not null,
        primary key (AgentBase_id, contact_addresses_id),
        unique (contact_addresses_id)
    );

    create table AgentBase_Address_AUD (
        REV integer not null,
        AgentBase_id integer not null,
        contact_addresses_id integer not null,
        revtype tinyint,
        primary key (REV, AgentBase_id, contact_addresses_id)
    );

    create table AgentBase_AgentBase (
        AgentBase_id integer not null,
        teammembers_id integer not null,
        sortIndex integer not null,
        primary key (AgentBase_id, sortIndex)
    );

    create table AgentBase_AgentBase_AUD (
        REV integer not null,
        AgentBase_id integer not null,
        teammembers_id integer not null,
        sortIndex integer not null,
        revtype tinyint,
        primary key (REV, AgentBase_id, teammembers_id, sortIndex)
    );

    create table AgentBase_Annotation (
        AgentBase_id integer not null,
        annotations_id integer not null,
        primary key (AgentBase_id, annotations_id),
        unique (annotations_id)
    );

    create table AgentBase_Annotation_AUD (
        REV integer not null,
        AgentBase_id integer not null,
        annotations_id integer not null,
        revtype tinyint,
        primary key (REV, AgentBase_id, annotations_id)
    );

    create table AgentBase_Credit (
        AgentBase_id integer not null,
        credits_id integer not null,
        sortIndex integer not null,
        primary key (AgentBase_id, sortIndex),
        unique (credits_id)
    );

    create table AgentBase_Credit_AUD (
        REV integer not null,
        AgentBase_id integer not null,
        credits_id integer not null,
        sortIndex integer not null,
        revtype tinyint,
        primary key (REV, AgentBase_id, credits_id, sortIndex)
    );

    create table AgentBase_DefinedTermBase (
        AgentBase_id integer not null,
        types_id integer not null,
        primary key (AgentBase_id, types_id)
    );

    create table AgentBase_DefinedTermBase_AUD (
        REV integer not null,
        AgentBase_id integer not null,
        types_id integer not null,
        revtype tinyint,
        primary key (REV, AgentBase_id, types_id)
    );

    create table AgentBase_Extension (
        AgentBase_id integer not null,
        extensions_id integer not null,
        primary key (AgentBase_id, extensions_id),
        unique (extensions_id)
    );

    create table AgentBase_Extension_AUD (
        REV integer not null,
        AgentBase_id integer not null,
        extensions_id integer not null,
        revtype tinyint,
        primary key (REV, AgentBase_id, extensions_id)
    );

    create table AgentBase_Marker (
        AgentBase_id integer not null,
        markers_id integer not null,
        primary key (AgentBase_id, markers_id),
        unique (markers_id)
    );

    create table AgentBase_Marker_AUD (
        REV integer not null,
        AgentBase_id integer not null,
        markers_id integer not null,
        revtype tinyint,
        primary key (REV, AgentBase_id, markers_id)
    );

    create table AgentBase_Media (
        AgentBase_id integer not null,
        media_id integer not null,
        primary key (AgentBase_id, media_id)
    );

    create table AgentBase_Media_AUD (
        REV integer not null,
        AgentBase_id integer not null,
        media_id integer not null,
        revtype tinyint,
        primary key (REV, AgentBase_id, media_id)
    );

    create table AgentBase_OriginalSourceBase (
        AgentBase_id integer not null,
        sources_id integer not null,
        primary key (AgentBase_id, sources_id),
        unique (sources_id)
    );

    create table AgentBase_OriginalSourceBase_AUD (
        REV integer not null,
        AgentBase_id integer not null,
        sources_id integer not null,
        revtype tinyint,
        primary key (REV, AgentBase_id, sources_id)
    );

    create table AgentBase_Rights (
        AgentBase_id integer not null,
        rights_id integer not null,
        primary key (AgentBase_id, rights_id),
        unique (rights_id)
    );

    create table AgentBase_Rights_AUD (
        REV integer not null,
        AgentBase_id integer not null,
        rights_id integer not null,
        revtype tinyint,
        primary key (REV, AgentBase_id, rights_id)
    );

    create table AgentBase_contact_emailaddresses (
        AgentBase_id integer not null,
        contact_emailaddresses_element varchar(255)
    );

    create table AgentBase_contact_emailaddresses_AUD (
        REV integer not null,
        AgentBase_id integer not null,
        contact_emailaddresses_element varchar(255) not null,
        revtype tinyint,
        primary key (REV, AgentBase_id, contact_emailaddresses_element)
    );

    create table AgentBase_contact_faxnumbers (
        AgentBase_id integer not null,
        contact_faxnumbers_element varchar(255)
    );

    create table AgentBase_contact_faxnumbers_AUD (
        REV integer not null,
        AgentBase_id integer not null,
        contact_faxnumbers_element varchar(255) not null,
        revtype tinyint,
        primary key (REV, AgentBase_id, contact_faxnumbers_element)
    );

    create table AgentBase_contact_phonenumbers (
        AgentBase_id integer not null,
        contact_phonenumbers_element varchar(255)
    );

    create table AgentBase_contact_phonenumbers_AUD (
        REV integer not null,
        AgentBase_id integer not null,
        contact_phonenumbers_element varchar(255) not null,
        revtype tinyint,
        primary key (REV, AgentBase_id, contact_phonenumbers_element)
    );

    create table AgentBase_contact_urls (
        AgentBase_id integer not null,
        contact_urls_element varchar(255)
    );

    create table AgentBase_contact_urls_AUD (
        REV integer not null,
        AgentBase_id integer not null,
        contact_urls_element varchar(255) not null,
        revtype tinyint,
        primary key (REV, AgentBase_id, contact_urls_element)
    );

    create table Annotation (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        text longvarchar,
        annotatedObj_type varchar(255),
        annotatedObj_id integer not null,
        linkbackurl varbinary(255),
        createdby_id integer,
        updatedby_id integer,
        language_id integer,
        annotationtype_id integer,
        commentator_id integer,
        primary key (id),
        unique (uuid)
    );

    create table Annotation_AUD (
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        text longvarchar,
        linkbackurl varbinary(255),
        createdby_id integer,
        updatedby_id integer,
        language_id integer,
        annotationtype_id integer,
        commentator_id integer,
        primary key (id, REV)
    );

    create table Annotation_Annotation (
        Annotation_id integer not null,
        annotations_id integer not null,
        primary key (Annotation_id, annotations_id),
        unique (annotations_id)
    );

    create table Annotation_Annotation_AUD (
        REV integer not null,
        Annotation_id integer not null,
        annotations_id integer not null,
        revtype tinyint,
        primary key (REV, Annotation_id, annotations_id)
    );

    create table Annotation_Marker (
        Annotation_id integer not null,
        markers_id integer not null,
        primary key (Annotation_id, markers_id),
        unique (markers_id)
    );

    create table Annotation_Marker_AUD (
        REV integer not null,
        Annotation_id integer not null,
        markers_id integer not null,
        revtype tinyint,
        primary key (REV, Annotation_id, markers_id)
    );

    create table AuditEvent (
        revisionnumber integer generated by default as identity (start with 1),
        date timestamp,
        timestamp bigint,
        uuid varchar(255),
        primary key (revisionnumber)
    );

    create table CDM_VIEW (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        description varchar(255),
        name varchar(255),
        createdby_id integer,
        reference_id integer,
        primary key (id),
        unique (uuid)
    );

    create table CDM_VIEW_CDM_VIEW (
        CDM_VIEW_id integer not null,
        superviews_id integer not null,
        primary key (CDM_VIEW_id, superviews_id),
        unique (superviews_id)
    );
    
    
    create table Classification (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        lsid_authority varchar(255),
        lsid_lsid varchar(255),
        lsid_namespace varchar(255),
        lsid_object varchar(255),
        lsid_revision varchar(255),
        protectedtitlecache bit not null,
        titleCache varchar(255),
        microreference varchar(255),
        createdby_id integer,
        updatedby_id integer,
        name_id integer,
        reference_id integer,
        primary key (id),
        unique (uuid)
    );

    create table Classification_AUD (
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        lsid_authority varchar(255),
        lsid_lsid varchar(255),
        lsid_namespace varchar(255),
        lsid_object varchar(255),
        lsid_revision varchar(255),
        protectedtitlecache bit,
        titleCache varchar(255),
        microreference varchar(255),
        createdby_id integer,
        updatedby_id integer,
        name_id integer,
        reference_id integer,
        primary key (id, REV)
    );

    create table Classification_Annotation (
        Classification_id integer not null,
        annotations_id integer not null,
        primary key (Classification_id, annotations_id),
        unique (annotations_id)
    );

    create table Classification_Annotation_AUD (
        REV integer not null,
        Classification_id integer not null,
        annotations_id integer not null,
        revtype tinyint,
        primary key (REV, Classification_id, annotations_id)
    );

    create table Classification_Credit (
        Classification_id integer not null,
        credits_id integer not null,
        sortIndex integer not null,
        primary key (Classification_id, sortIndex),
        unique (credits_id)
    );

    create table Classification_Credit_AUD (
        REV integer not null,
        Classification_id integer not null,
        credits_id integer not null,
        sortIndex integer not null,
        revtype tinyint,
        primary key (REV, Classification_id, credits_id, sortIndex)
    );

    create table Classification_Extension (
        Classification_id integer not null,
        extensions_id integer not null,
        primary key (Classification_id, extensions_id),
        unique (extensions_id)
    );

    create table Classification_Extension_AUD (
        REV integer not null,
        Classification_id integer not null,
        extensions_id integer not null,
        revtype tinyint,
        primary key (REV, Classification_id, extensions_id)
    );

    create table Classification_Marker (
        Classification_id integer not null,
        markers_id integer not null,
        primary key (Classification_id, markers_id),
        unique (markers_id)
    );

    create table Classification_Marker_AUD (
        REV integer not null,
        Classification_id integer not null,
        markers_id integer not null,
        revtype tinyint,
        primary key (REV, Classification_id, markers_id)
    );

    create table Classification_OriginalSourceBase (
        Classification_id integer not null,
        sources_id integer not null,
        primary key (Classification_id, sources_id),
        unique (sources_id)
    );

    create table Classification_OriginalSourceBase_AUD (
        REV integer not null,
        Classification_id integer not null,
        sources_id integer not null,
        revtype tinyint,
        primary key (REV, Classification_id, sources_id)
    );

    create table Classification_Rights (
        Classification_id integer not null,
        rights_id integer not null,
        primary key (Classification_id, rights_id),
        unique (rights_id)
    );

    create table Classification_Rights_AUD (
        REV integer not null,
        Classification_id integer not null,
        rights_id integer not null,
        revtype tinyint,
        primary key (REV, Classification_id, rights_id)
    );

    create table Classification_TaxonNode (
        Classification_id integer not null,
        rootnodes_id integer not null,
        primary key (Classification_id, rootnodes_id),
        unique (rootnodes_id)
    );

    create table Classification_TaxonNode_AUD (
        REV integer not null,
        Classification_id integer not null,
        rootnodes_id integer not null,
        revtype tinyint,
        primary key (REV, Classification_id, rootnodes_id)
    );


    create table Collection (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        lsid_authority varchar(255),
        lsid_lsid varchar(255),
        lsid_namespace varchar(255),
        lsid_object varchar(255),
        lsid_revision varchar(255),
        protectedtitlecache bit not null,
        titleCache varchar(255),
        code varchar(255),
        codestandard varchar(255),
        name varchar(255),
        townorlocation varchar(255),
        createdby_id integer,
        updatedby_id integer,
        institute_id integer,
        supercollection_id integer,
        primary key (id),
        unique (uuid)
    );

    create table Collection_AUD (
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        lsid_authority varchar(255),
        lsid_lsid varchar(255),
        lsid_namespace varchar(255),
        lsid_object varchar(255),
        lsid_revision varchar(255),
        protectedtitlecache bit,
        titleCache varchar(255),
        code varchar(255),
        codestandard varchar(255),
        name varchar(255),
        townorlocation varchar(255),
        createdby_id integer,
        updatedby_id integer,
        institute_id integer,
        supercollection_id integer,
        primary key (id, REV)
    );

    create table Collection_Annotation (
        Collection_id integer not null,
        annotations_id integer not null,
        primary key (Collection_id, annotations_id),
        unique (annotations_id)
    );

    create table Collection_Annotation_AUD (
        REV integer not null,
        Collection_id integer not null,
        annotations_id integer not null,
        revtype tinyint,
        primary key (REV, Collection_id, annotations_id)
    );

    create table Collection_Credit (
        Collection_id integer not null,
        credits_id integer not null,
        sortIndex integer not null,
        primary key (Collection_id, sortIndex),
        unique (credits_id)
    );

    create table Collection_Credit_AUD (
        REV integer not null,
        Collection_id integer not null,
        credits_id integer not null,
        sortIndex integer not null,
        revtype tinyint,
        primary key (REV, Collection_id, credits_id, sortIndex)
    );

    create table Collection_Extension (
        Collection_id integer not null,
        extensions_id integer not null,
        primary key (Collection_id, extensions_id),
        unique (extensions_id)
    );

    create table Collection_Extension_AUD (
        REV integer not null,
        Collection_id integer not null,
        extensions_id integer not null,
        revtype tinyint,
        primary key (REV, Collection_id, extensions_id)
    );

    create table Collection_Marker (
        Collection_id integer not null,
        markers_id integer not null,
        primary key (Collection_id, markers_id),
        unique (markers_id)
    );

    create table Collection_Marker_AUD (
        REV integer not null,
        Collection_id integer not null,
        markers_id integer not null,
        revtype tinyint,
        primary key (REV, Collection_id, markers_id)
    );

    create table Collection_Media (
        Collection_id integer not null,
        media_id integer not null,
        primary key (Collection_id, media_id)
    );

    create table Collection_Media_AUD (
        REV integer not null,
        Collection_id integer not null,
        media_id integer not null,
        revtype tinyint,
        primary key (REV, Collection_id, media_id)
    );

    create table Collection_OriginalSourceBase (
        Collection_id integer not null,
        sources_id integer not null,
        primary key (Collection_id, sources_id),
        unique (sources_id)
    );

    create table Collection_OriginalSourceBase_AUD (
        REV integer not null,
        Collection_id integer not null,
        sources_id integer not null,
        revtype tinyint,
        primary key (REV, Collection_id, sources_id)
    );

    create table Collection_Rights (
        Collection_id integer not null,
        rights_id integer not null,
        primary key (Collection_id, rights_id),
        unique (rights_id)
    );

    create table Collection_Rights_AUD (
        REV integer not null,
        Collection_id integer not null,
        rights_id integer not null,
        revtype tinyint,
        primary key (REV, Collection_id, rights_id)
    );

    create table Credit (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        text longvarchar,
        abbreviatedtext varchar(255),
        createdby_id integer,
        updatedby_id integer,
        language_id integer,
        agent_id integer,
        primary key (id),
        unique (uuid)
    );

    create table Credit_AUD (
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        text longvarchar,
        abbreviatedtext varchar(255),
        createdby_id integer,
        updatedby_id integer,
        language_id integer,
        agent_id integer,
        primary key (id, REV)
    );

    create table Credit_Annotation (
        Credit_id integer not null,
        annotations_id integer not null,
        primary key (Credit_id, annotations_id),
        unique (annotations_id)
    );

    create table Credit_Annotation_AUD (
        REV integer not null,
        Credit_id integer not null,
        annotations_id integer not null,
        revtype tinyint,
        primary key (REV, Credit_id, annotations_id)
    );

    create table Credit_Marker (
        Credit_id integer not null,
        markers_id integer not null,
        primary key (Credit_id, markers_id),
        unique (markers_id)
    );

    create table Credit_Marker_AUD (
        REV integer not null,
        Credit_id integer not null,
        markers_id integer not null,
        revtype tinyint,
        primary key (REV, Credit_id, markers_id)
    );

    create table DefinedTermBase (
        DTYPE varchar(31) not null,
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        lsid_authority varchar(255),
        lsid_lsid varchar(255),
        lsid_namespace varchar(255),
        lsid_object varchar(255),
        lsid_revision varchar(255),
        protectedtitlecache bit not null,
        titleCache varchar(255),
        uri varchar(255),
        iso639_1 varchar(2),
        iso639_2 varchar(3),
        istechnical bit,
        orderindex integer,
        symmetrical bit,
        transitive bit,
        defaultcolor varchar(255),
        supportscategoricaldata bit,
        supportscommontaxonname bit,
        supportsdistribution bit,
        supportsindividualassociation bit,
        supportsquantitativedata bit,
        supportstaxoninteraction bit,
        supportstextdata bit,
        pointapproximation_errorradius integer,
        pointapproximation_latitude double,
        pointapproximation_longitude double,
        validperiod_end varchar(255),
        validperiod_freetext varchar(255),
        validperiod_start varchar(255),
        iso3166_a2 varchar(2),
        createdby_id integer,
        updatedby_id integer,
        kindof_id integer,
        partof_id integer,
        vocabulary_id integer,
        level_id integer,
        pointapproximation_referencesystem_id integer,
        shape_id integer,
        type_id integer,
        primary key (id),
        unique (uuid)
    );

    create table DefinedTermBase_AUD (
        DTYPE varchar(31) not null,
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        lsid_authority varchar(255),
        lsid_lsid varchar(255),
        lsid_namespace varchar(255),
        lsid_object varchar(255),
        lsid_revision varchar(255),
        protectedtitlecache bit,
        titleCache varchar(255),
        uri varchar(255),
        createdby_id integer,
        updatedby_id integer,
        kindof_id integer,
        partof_id integer,
        vocabulary_id integer,
        istechnical bit,
        orderindex integer,
        iso639_1 varchar(2),
        iso639_2 varchar(3),
        symmetrical bit,
        transitive bit,
        pointapproximation_errorradius integer,
        pointapproximation_latitude double,
        pointapproximation_longitude double,
        validperiod_end varchar(255),
        validperiod_freetext varchar(255),
        validperiod_start varchar(255),
        level_id integer,
        pointapproximation_referencesystem_id integer,
        shape_id integer,
        type_id integer,
        iso3166_a2 varchar(2),
        defaultcolor varchar(255),
        supportscategoricaldata bit,
        supportscommontaxonname bit,
        supportsdistribution bit,
        supportsindividualassociation bit,
        supportsquantitativedata bit,
        supportstaxoninteraction bit,
        supportstextdata bit,
        primary key (id, REV)
    );

    create table DefinedTermBase_Annotation (
        DefinedTermBase_id integer not null,
        annotations_id integer not null,
        primary key (DefinedTermBase_id, annotations_id),
        unique (annotations_id)
    );

    create table DefinedTermBase_Annotation_AUD (
        REV integer not null,
        DefinedTermBase_id integer not null,
        annotations_id integer not null,
        revtype tinyint,
        primary key (REV, DefinedTermBase_id, annotations_id)
    );

    create table DefinedTermBase_Continent (
        DefinedTermBase_id integer not null,
        continents_id integer not null,
        primary key (DefinedTermBase_id, continents_id)
    );

    create table DefinedTermBase_Continent_AUD (
        REV integer not null,
        DefinedTermBase_id integer not null,
        continents_id integer not null,
        revtype tinyint,
        primary key (REV, DefinedTermBase_id, continents_id)
    );

    create table DefinedTermBase_Credit (
        DefinedTermBase_id integer not null,
        credits_id integer not null,
        sortIndex integer not null,
        primary key (DefinedTermBase_id, sortIndex),
        unique (credits_id)
    );

    create table DefinedTermBase_Credit_AUD (
        REV integer not null,
        DefinedTermBase_id integer not null,
        credits_id integer not null,
        sortIndex integer not null,
        revtype tinyint,
        primary key (REV, DefinedTermBase_id, credits_id, sortIndex)
    );

    create table DefinedTermBase_Extension (
        DefinedTermBase_id integer not null,
        extensions_id integer not null,
        primary key (DefinedTermBase_id, extensions_id),
        unique (extensions_id)
    );

    create table DefinedTermBase_Extension_AUD (
        REV integer not null,
        DefinedTermBase_id integer not null,
        extensions_id integer not null,
        revtype tinyint,
        primary key (REV, DefinedTermBase_id, extensions_id)
    );

    create table DefinedTermBase_Marker (
        DefinedTermBase_id integer not null,
        markers_id integer not null,
        primary key (DefinedTermBase_id, markers_id),
        unique (markers_id)
    );

    create table DefinedTermBase_Marker_AUD (
        REV integer not null,
        DefinedTermBase_id integer not null,
        markers_id integer not null,
        revtype tinyint,
        primary key (REV, DefinedTermBase_id, markers_id)
    );

    create table DefinedTermBase_MeasurementUnit (
        DefinedTermBase_id integer not null,
        recommendedmeasurementunits_id integer not null,
        primary key (DefinedTermBase_id, recommendedmeasurementunits_id)
    );

    create table DefinedTermBase_MeasurementUnit_AUD (
        REV integer not null,
        DefinedTermBase_id integer not null,
        recommendedmeasurementunits_id integer not null,
        revtype tinyint,
        primary key (REV, DefinedTermBase_id, recommendedmeasurementunits_id)
    );

    create table DefinedTermBase_Media (
        DefinedTermBase_id integer not null,
        media_id integer not null,
        primary key (DefinedTermBase_id, media_id),
        unique (media_id)
    );

    create table DefinedTermBase_Media_AUD (
        REV integer not null,
        DefinedTermBase_id integer not null,
        media_id integer not null,
        revtype tinyint,
        primary key (REV, DefinedTermBase_id, media_id)
    );

    create table DefinedTermBase_OriginalSourceBase (
        DefinedTermBase_id integer not null,
        sources_id integer not null,
        primary key (DefinedTermBase_id, sources_id),
        unique (sources_id)
    );

    create table DefinedTermBase_OriginalSourceBase_AUD (
        REV integer not null,
        DefinedTermBase_id integer not null,
        sources_id integer not null,
        revtype tinyint,
        primary key (REV, DefinedTermBase_id, sources_id)
    );

    create table DefinedTermBase_RecommendedModifierEnumeration (
        DefinedTermBase_id integer not null,
        recommendedmodifierenumeration_id integer not null,
        primary key (DefinedTermBase_id, recommendedmodifierenumeration_id),
        unique (recommendedmodifierenumeration_id)
    );

    create table DefinedTermBase_RecommendedModifierEnumeration_AUD (
        REV integer not null,
        DefinedTermBase_id integer not null,
        recommendedmodifierenumeration_id integer not null,
        revtype tinyint,
        primary key (REV, DefinedTermBase_id, recommendedmodifierenumeration_id)
    );

    create table DefinedTermBase_Representation (
        DefinedTermBase_id integer not null,
        representations_id integer not null,
        primary key (DefinedTermBase_id, representations_id),
        unique (representations_id)
    );

    create table DefinedTermBase_Representation_AUD (
        REV integer not null,
        DefinedTermBase_id integer not null,
        representations_id integer not null,
        revtype tinyint,
        primary key (REV, DefinedTermBase_id, representations_id)
    );

    create table DefinedTermBase_Rights (
        DefinedTermBase_id integer not null,
        rights_id integer not null,
        primary key (DefinedTermBase_id, rights_id),
        unique (rights_id)
    );

    create table DefinedTermBase_Rights_AUD (
        REV integer not null,
        DefinedTermBase_id integer not null,
        rights_id integer not null,
        revtype tinyint,
        primary key (REV, DefinedTermBase_id, rights_id)
    );

    create table DefinedTermBase_StatisticalMeasure (
        DefinedTermBase_id integer not null,
        recommendedstatisticalmeasures_id integer not null,
        primary key (DefinedTermBase_id, recommendedstatisticalmeasures_id)
    );

    create table DefinedTermBase_StatisticalMeasure_AUD (
        REV integer not null,
        DefinedTermBase_id integer not null,
        recommendedstatisticalmeasures_id integer not null,
        revtype tinyint,
        primary key (REV, DefinedTermBase_id, recommendedstatisticalmeasures_id)
    );

    create table DefinedTermBase_SupportedCategoricalEnumeration (
        DefinedTermBase_id integer not null,
        supportedcategoricalenumerations_id integer not null,
        primary key (DefinedTermBase_id, supportedcategoricalenumerations_id),
        unique (supportedcategoricalenumerations_id)
    );

    create table DefinedTermBase_SupportedCategoricalEnumeration_AUD (
        REV integer not null,
        DefinedTermBase_id integer not null,
        supportedcategoricalenumerations_id integer not null,
        revtype tinyint,
        primary key (REV, DefinedTermBase_id, supportedcategoricalenumerations_id)
    );

    create table DefinedTermBase_WaterbodyOrCountry (
        DefinedTermBase_id integer not null,
        waterbodiesorcountries_id integer not null,
        primary key (DefinedTermBase_id, waterbodiesorcountries_id)
    );

    create table DefinedTermBase_WaterbodyOrCountry_AUD (
        REV integer not null,
        DefinedTermBase_id integer not null,
        waterbodiesorcountries_id integer not null,
        revtype tinyint,
        primary key (REV, DefinedTermBase_id, waterbodiesorcountries_id)
    );

    create table DerivationEvent (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        description varchar(255),
        timeperiod_end varchar(255),
        timeperiod_freetext varchar(255),
        timeperiod_start varchar(255),
        createdby_id integer,
        updatedby_id integer,
        actor_id integer,
        type_id integer,
        primary key (id),
        unique (uuid)
    );

    create table DerivationEvent_AUD (
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        description varchar(255),
        timeperiod_end varchar(255),
        timeperiod_freetext varchar(255),
        timeperiod_start varchar(255),
        createdby_id integer,
        updatedby_id integer,
        actor_id integer,
        type_id integer,
        primary key (id, REV)
    );

    create table DerivationEvent_Annotation (
        DerivationEvent_id integer not null,
        annotations_id integer not null,
        primary key (DerivationEvent_id, annotations_id),
        unique (annotations_id)
    );

    create table DerivationEvent_Annotation_AUD (
        REV integer not null,
        DerivationEvent_id integer not null,
        annotations_id integer not null,
        revtype tinyint,
        primary key (REV, DerivationEvent_id, annotations_id)
    );

    create table DerivationEvent_Marker (
        DerivationEvent_id integer not null,
        markers_id integer not null,
        primary key (DerivationEvent_id, markers_id),
        unique (markers_id)
    );

    create table DerivationEvent_Marker_AUD (
        REV integer not null,
        DerivationEvent_id integer not null,
        markers_id integer not null,
        revtype tinyint,
        primary key (REV, DerivationEvent_id, markers_id)
    );

    create table DescriptionBase (
        DTYPE varchar(31) not null,
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        lsid_authority varchar(255),
        lsid_lsid varchar(255),
        lsid_namespace varchar(255),
        lsid_object varchar(255),
        lsid_revision varchar(255),
        protectedtitlecache bit not null,
        titleCache varchar(255),
        imagegallery bit not null,
        createdby_id integer,
        updatedby_id integer,
        taxon_id integer,
        taxonname_id integer,
        primary key (id),
        unique (uuid)
    );

    create table DescriptionBase_AUD (
        DTYPE varchar(31) not null,
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        lsid_authority varchar(255),
        lsid_lsid varchar(255),
        lsid_namespace varchar(255),
        lsid_object varchar(255),
        lsid_revision varchar(255),
        protectedtitlecache bit,
        titleCache varchar(255),
        imagegallery bit,
        createdby_id integer,
        updatedby_id integer,
        taxon_id integer,
        taxonname_id integer,
        primary key (id, REV)
    );

    create table DescriptionBase_Annotation (
        DescriptionBase_id integer not null,
        annotations_id integer not null,
        primary key (DescriptionBase_id, annotations_id),
        unique (annotations_id)
    );

    create table DescriptionBase_Annotation_AUD (
        REV integer not null,
        DescriptionBase_id integer not null,
        annotations_id integer not null,
        revtype tinyint,
        primary key (REV, DescriptionBase_id, annotations_id)
    );

    create table DescriptionBase_Credit (
        DescriptionBase_id integer not null,
        credits_id integer not null,
        sortIndex integer not null,
        primary key (DescriptionBase_id, sortIndex),
        unique (credits_id)
    );

    create table DescriptionBase_Credit_AUD (
        REV integer not null,
        DescriptionBase_id integer not null,
        credits_id integer not null,
        sortIndex integer not null,
        revtype tinyint,
        primary key (REV, DescriptionBase_id, credits_id, sortIndex)
    );

    create table DescriptionBase_Extension (
        DescriptionBase_id integer not null,
        extensions_id integer not null,
        primary key (DescriptionBase_id, extensions_id),
        unique (extensions_id)
    );

    create table DescriptionBase_Extension_AUD (
        REV integer not null,
        DescriptionBase_id integer not null,
        extensions_id integer not null,
        revtype tinyint,
        primary key (REV, DescriptionBase_id, extensions_id)
    );

    create table DescriptionBase_Feature (
        DescriptionBase_id integer not null,
        descriptivesystem_id integer not null,
        primary key (DescriptionBase_id, descriptivesystem_id)
    );

    create table DescriptionBase_Feature_AUD (
        REV integer not null,
        DescriptionBase_id integer not null,
        descriptivesystem_id integer not null,
        revtype tinyint,
        primary key (REV, DescriptionBase_id, descriptivesystem_id)
    );

    create table DescriptionBase_GeoScope (
        DescriptionBase_id integer not null,
        geoscopes_id integer not null,
        primary key (DescriptionBase_id, geoscopes_id)
    );

    create table DescriptionBase_GeoScope_AUD (
        REV integer not null,
        DescriptionBase_id integer not null,
        geoscopes_id integer not null,
        revtype tinyint,
        primary key (REV, DescriptionBase_id, geoscopes_id)
    );

    create table DescriptionBase_Marker (
        DescriptionBase_id integer not null,
        markers_id integer not null,
        primary key (DescriptionBase_id, markers_id),
        unique (markers_id)
    );

    create table DescriptionBase_Marker_AUD (
        REV integer not null,
        DescriptionBase_id integer not null,
        markers_id integer not null,
        revtype tinyint,
        primary key (REV, DescriptionBase_id, markers_id)
    );

    create table DescriptionBase_OriginalSourceBase (
        DescriptionBase_id integer not null,
        sources_id integer not null,
        primary key (DescriptionBase_id, sources_id),
        unique (sources_id)
    );

    create table DescriptionBase_OriginalSourceBase_AUD (
        REV integer not null,
        DescriptionBase_id integer not null,
        sources_id integer not null,
        revtype tinyint,
        primary key (REV, DescriptionBase_id, sources_id)
    );

    create table DescriptionBase_Reference (
        DescriptionBase_id integer not null,
        descriptionsources_id integer not null,
        primary key (DescriptionBase_id, descriptionsources_id)
    );

    create table DescriptionBase_Reference_AUD (
        REV integer not null,
        DescriptionBase_id integer not null,
        descriptionsources_id integer not null,
        revtype tinyint,
        primary key (REV, DescriptionBase_id, descriptionsources_id)
    );

    create table DescriptionBase_Rights (
        DescriptionBase_id integer not null,
        rights_id integer not null,
        primary key (DescriptionBase_id, rights_id),
        unique (rights_id)
    );

    create table DescriptionBase_Rights_AUD (
        REV integer not null,
        DescriptionBase_id integer not null,
        rights_id integer not null,
        revtype tinyint,
        primary key (REV, DescriptionBase_id, rights_id)
    );

    create table DescriptionBase_Scope (
        DescriptionBase_id integer not null,
        scopes_id integer not null,
        primary key (DescriptionBase_id, scopes_id)
    );

    create table DescriptionBase_Scope_AUD (
        REV integer not null,
        DescriptionBase_id integer not null,
        scopes_id integer not null,
        revtype tinyint,
        primary key (REV, DescriptionBase_id, scopes_id)
    );

    create table DescriptionBase_SpecimenOrObservationBase (
        descriptions_id integer not null,
        describedspecimenorobservations_id integer not null,
        primary key (descriptions_id, describedspecimenorobservations_id)
    );

    create table DescriptionBase_SpecimenOrObservationBase_AUD (
        REV integer not null,
        descriptions_id integer not null,
        describedspecimenorobservations_id integer not null,
        revtype tinyint,
        primary key (REV, descriptions_id, describedspecimenorobservations_id)
    );

    create table DescriptionElementBase (
        DTYPE varchar(31) not null,
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        orderrelevant bit,
        name varchar(255),
        createdby_id integer,
        updatedby_id integer,
        feature_id integer,
        indescription_id integer,
        language_id integer,
        area_id integer,
        status_id integer,
        associatedspecimenorobservation_id integer,
        unit_id integer,
        taxon2_id integer,
        format_id integer,
        primary key (id),
        unique (uuid)
    );

    create table DescriptionElementBase_AUD (
        DTYPE varchar(31) not null,
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        createdby_id integer,
        updatedby_id integer,
        feature_id integer,
        indescription_id integer,
        orderrelevant bit,
        associatedspecimenorobservation_id integer,
        name varchar(255),
        language_id integer,
        taxon2_id integer,
        area_id integer,
        status_id integer,
        unit_id integer,
        format_id integer,
        primary key (id, REV)
    );

    create table DescriptionElementBase_Annotation (
        DescriptionElementBase_id integer not null,
        annotations_id integer not null,
        primary key (DescriptionElementBase_id, annotations_id),
        unique (annotations_id)
    );

    create table DescriptionElementBase_Annotation_AUD (
        REV integer not null,
        DescriptionElementBase_id integer not null,
        annotations_id integer not null,
        revtype tinyint,
        primary key (REV, DescriptionElementBase_id, annotations_id)
    );

    create table DescriptionElementBase_LanguageString (
        DescriptionElementBase_id integer not null,
        multilanguagetext_id integer not null,
        multilanguagetext_mapkey_id integer not null,
        primary key (DescriptionElementBase_id, multilanguagetext_mapkey_id),
        unique (multilanguagetext_id)
    );

    create table DescriptionElementBase_LanguageString_AUD (
        REV integer not null,
        DescriptionElementBase_id integer not null,
        multilanguagetext_id integer not null,
        multilanguagetext_mapkey_id integer not null,
        revtype tinyint,
        primary key (REV, DescriptionElementBase_id, multilanguagetext_id, multilanguagetext_mapkey_id)
    );

    create table DescriptionElementBase_Marker (
        DescriptionElementBase_id integer not null,
        markers_id integer not null,
        primary key (DescriptionElementBase_id, markers_id),
        unique (markers_id)
    );

    create table DescriptionElementBase_Marker_AUD (
        REV integer not null,
        DescriptionElementBase_id integer not null,
        markers_id integer not null,
        revtype tinyint,
        primary key (REV, DescriptionElementBase_id, markers_id)
    );

    create table DescriptionElementBase_Media (
        DescriptionElementBase_id integer not null,
        media_id integer not null,
        sortIndex integer not null,
        primary key (DescriptionElementBase_id, sortIndex)
    );

    create table DescriptionElementBase_Media_AUD (
        REV integer not null,
        DescriptionElementBase_id integer not null,
        media_id integer not null,
        sortIndex integer not null,
        revtype tinyint,
        primary key (REV, DescriptionElementBase_id, media_id, sortIndex)
    );

    create table DescriptionElementBase_Modifier (
        DescriptionElementBase_id integer not null,
        modifiers_id integer not null,
        primary key (DescriptionElementBase_id, modifiers_id)
    );

    create table DescriptionElementBase_Modifier_AUD (
        REV integer not null,
        DescriptionElementBase_id integer not null,
        modifiers_id integer not null,
        revtype tinyint,
        primary key (REV, DescriptionElementBase_id, modifiers_id)
    );

    create table DescriptionElementBase_ModifyingText (
        DescriptionElementBase_id integer not null,
        modifyingtext_id integer not null,
        modifyingtext_mapkey_id integer not null,
        primary key (DescriptionElementBase_id, modifyingtext_mapkey_id),
        unique (modifyingtext_id)
    );

    create table DescriptionElementBase_ModifyingText_AUD (
        REV integer not null,
        DescriptionElementBase_id integer not null,
        modifyingtext_id integer not null,
        modifyingtext_mapkey_id integer not null,
        revtype tinyint,
        primary key (REV, DescriptionElementBase_id, modifyingtext_id, modifyingtext_mapkey_id)
    );

    create table DescriptionElementBase_OriginalSourceBase (
        DescriptionElementBase_id integer not null,
        sources_id integer not null,
        primary key (DescriptionElementBase_id, sources_id),
        unique (sources_id)
    );

    create table DescriptionElementBase_OriginalSourceBase_AUD (
        REV integer not null,
        DescriptionElementBase_id integer not null,
        sources_id integer not null,
        revtype tinyint,
        primary key (REV, DescriptionElementBase_id, sources_id)
    );

    create table DescriptionElementBase_StateData (
        DescriptionElementBase_id integer not null,
        states_id integer not null
    );

    create table DescriptionElementBase_StateData_AUD (
        REV integer not null,
        DescriptionElementBase_id integer not null,
        states_id integer not null,
        revtype tinyint,
        primary key (REV, DescriptionElementBase_id, states_id)
    );

    create table DescriptionElementBase_StatisticalMeasurementValue (
        DescriptionElementBase_id integer not null,
        statisticalvalues_id integer not null,
        primary key (DescriptionElementBase_id, statisticalvalues_id),
        unique (statisticalvalues_id)
    );

    create table DescriptionElementBase_StatisticalMeasurementValue_AUD (
        REV integer not null,
        DescriptionElementBase_id integer not null,
        statisticalvalues_id integer not null,
        revtype tinyint,
        primary key (REV, DescriptionElementBase_id, statisticalvalues_id)
    );

    create table DeterminationEvent (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        description varchar(255),
        timeperiod_end varchar(255),
        timeperiod_freetext varchar(255),
        timeperiod_start varchar(255),
        preferredflag bit not null,
        createdby_id integer,
        updatedby_id integer,
        actor_id integer,
        identifiedunit_id integer,
        modifier_id integer,
        taxon_id integer,
        primary key (id),
        unique (uuid)
    );

    create table DeterminationEvent_AUD (
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        description varchar(255),
        timeperiod_end varchar(255),
        timeperiod_freetext varchar(255),
        timeperiod_start varchar(255),
        preferredflag bit,
        createdby_id integer,
        updatedby_id integer,
        actor_id integer,
        identifiedunit_id integer,
        modifier_id integer,
        taxon_id integer,
        primary key (id, REV)
    );

    create table DeterminationEvent_Annotation (
        DeterminationEvent_id integer not null,
        annotations_id integer not null,
        primary key (DeterminationEvent_id, annotations_id),
        unique (annotations_id)
    );

    create table DeterminationEvent_Annotation_AUD (
        REV integer not null,
        DeterminationEvent_id integer not null,
        annotations_id integer not null,
        revtype tinyint,
        primary key (REV, DeterminationEvent_id, annotations_id)
    );

    create table DeterminationEvent_Marker (
        DeterminationEvent_id integer not null,
        markers_id integer not null,
        primary key (DeterminationEvent_id, markers_id),
        unique (markers_id)
    );

    create table DeterminationEvent_Marker_AUD (
        REV integer not null,
        DeterminationEvent_id integer not null,
        markers_id integer not null,
        revtype tinyint,
        primary key (REV, DeterminationEvent_id, markers_id)
    );

    create table DeterminationEvent_Reference (
        DeterminationEvent_id integer not null,
        setofreferences_id integer not null,
        primary key (DeterminationEvent_id, setofreferences_id)
    );

    create table DeterminationEvent_Reference_AUD (
        REV integer not null,
        DeterminationEvent_id integer not null,
        setofreferences_id integer not null,
        revtype tinyint,
        primary key (REV, DeterminationEvent_id, setofreferences_id)
    );

    create table Extension (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        extendedObj_type varchar(255),
        extendedObj_id integer not null,
        value longvarchar,
        createdby_id integer,
        updatedby_id integer,
        type_id integer,
        primary key (id),
        unique (uuid)
    );

    create table Extension_AUD (
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        value longvarchar,
        createdby_id integer,
        updatedby_id integer,
        type_id integer,
        primary key (id, REV)
    );

    create table FeatureNode (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        createdby_id integer,
        updatedby_id integer,
        featureTree_id integer not null,
        feature_id integer,
        parent_id integer,
        primary key (id),
        unique (uuid)
    );

    create table FeatureNode_AUD (
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        createdby_id integer,
        updatedby_id integer,
       	featureTree_id integer not null,
        feature_id integer,
        parent_id integer,
        primary key (id, REV)
    );

    create table FeatureNode_DefinedTermBase_InapplicableIf (
        FeatureNode_id integer not null,
        inapplicableif_id integer not null,
        primary key (FeatureNode_id, inapplicableif_id)
    );

    create table FeatureNode_DefinedTermBase_InapplicableIf_AUD (
        REV integer not null,
        FeatureNode_id integer not null,
        inapplicableif_id integer not null,
        revtype tinyint,
        primary key (REV, FeatureNode_id, inapplicableif_id)
    );

    create table FeatureNode_DefinedTermBase_OnlyApplicable (
        FeatureNode_id integer not null,
        onlyapplicableif_id integer not null,
        primary key (FeatureNode_id, onlyapplicableif_id)
    );

    create table FeatureNode_DefinedTermBase_OnlyApplicable_AUD (
        REV integer not null,
        FeatureNode_id integer not null,
        onlyapplicableif_id integer not null,
        revtype tinyint,
        primary key (REV, FeatureNode_id, onlyapplicableif_id)
    );

    create table FeatureTree (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        lsid_authority varchar(255),
        lsid_lsid varchar(255),
        lsid_namespace varchar(255),
        lsid_object varchar(255),
        lsid_revision varchar(255),
        protectedtitlecache bit not null,
        titleCache varchar(255),
        uri varchar(255),
        descriptionseparated bit not null,
        createdby_id integer,
        updatedby_id integer,
        root_id integer,
        primary key (id),
        unique (uuid)
    );

    create table FeatureTree_AUD (
        DTYPE varchar(31) not null,
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        lsid_authority varchar(255),
        lsid_lsid varchar(255),
        lsid_namespace varchar(255),
        lsid_object varchar(255),
        lsid_revision varchar(255),
        protectedtitlecache bit,
        titleCache varchar(255),
        uri varchar(255),
        descriptionseparated bit,
        createdby_id integer,
        updatedby_id integer,
        root_id integer,
        primary key (id, REV)
    );

    create table FeatureTree_Annotation (
        FeatureTree_id integer not null,
        annotations_id integer not null,
        primary key (FeatureTree_id, annotations_id),
        unique (annotations_id)
    );

    create table FeatureTree_Annotation_AUD (
        REV integer not null,
        FeatureTree_id integer not null,
        annotations_id integer not null,
        revtype tinyint,
        primary key (REV, FeatureTree_id, annotations_id)
    );

    create table FeatureTree_Credit (
        FeatureTree_id integer not null,
        credits_id integer not null,
        sortIndex integer not null,
        primary key (FeatureTree_id, sortIndex),
        unique (credits_id)
    );

    create table FeatureTree_Credit_AUD (
        REV integer not null,
        FeatureTree_id integer not null,
        credits_id integer not null,
        sortIndex integer not null,
        revtype tinyint,
        primary key (REV, FeatureTree_id, credits_id, sortIndex)
    );

    create table FeatureTree_Extension (
        FeatureTree_id integer not null,
        extensions_id integer not null,
        primary key (FeatureTree_id, extensions_id),
        unique (extensions_id)
    );

    create table FeatureTree_Extension_AUD (
        REV integer not null,
        FeatureTree_id integer not null,
        extensions_id integer not null,
        revtype tinyint,
        primary key (REV, FeatureTree_id, extensions_id)
    );

    create table FeatureTree_Marker (
        FeatureTree_id integer not null,
        markers_id integer not null,
        primary key (FeatureTree_id, markers_id),
        unique (markers_id)
    );

    create table FeatureTree_Marker_AUD (
        REV integer not null,
        FeatureTree_id integer not null,
        markers_id integer not null,
        revtype tinyint,
        primary key (REV, FeatureTree_id, markers_id)
    );

    create table FeatureTree_OriginalSourceBase (
        FeatureTree_id integer not null,
        sources_id integer not null,
        primary key (FeatureTree_id, sources_id),
        unique (sources_id)
    );

    create table FeatureTree_OriginalSourceBase_AUD (
        REV integer not null,
        FeatureTree_id integer not null,
        sources_id integer not null,
        revtype tinyint,
        primary key (REV, FeatureTree_id, sources_id)
    );

    create table FeatureTree_Representation (
        FeatureTree_id integer not null,
        representations_id integer not null,
        primary key (FeatureTree_id, representations_id),
        unique (representations_id)
    );

    create table FeatureTree_Representation_AUD (
        REV integer not null,
        FeatureTree_id integer not null,
        representations_id integer not null,
        revtype tinyint,
        primary key (REV, FeatureTree_id, representations_id)
    );

    create table FeatureTree_Rights (
        FeatureTree_id integer not null,
        rights_id integer not null,
        primary key (FeatureTree_id, rights_id),
        unique (rights_id)
    );

    create table FeatureTree_Rights_AUD (
        REV integer not null,
        FeatureTree_id integer not null,
        rights_id integer not null,
        revtype tinyint,
        primary key (REV, FeatureTree_id, rights_id)
    );

    create table FeatureTree_TaxonBase (
        FeatureTree_id integer not null,
        coveredtaxa_id integer not null,
        primary key (FeatureTree_id, coveredtaxa_id)
    );

    create table FeatureTree_TaxonBase_AUD (
        REV integer not null,
        FeatureTree_id integer not null,
        coveredtaxa_id integer not null,
        revtype tinyint,
        primary key (REV, FeatureTree_id, coveredtaxa_id)
    );

    create table GatheringEvent (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        description varchar(255),
        timeperiod_end varchar(255),
        timeperiod_freetext varchar(255),
        timeperiod_start varchar(255),
        absoluteelevation integer,
        absoluteelevationerror integer,
        collectingmethod varchar(255),
        distancetoground integer,
        distancetowatersurface integer,
        exactlocation_errorradius integer,
        exactlocation_latitude double,
        exactlocation_longitude double,
        createdby_id integer,
        updatedby_id integer,
        country_id integer,
        actor_id integer,
        exactlocation_referencesystem_id integer,
        locality_id integer,
        primary key (id),
        unique (uuid)
    );

    create table GatheringEvent_AUD (
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        description varchar(255),
        timeperiod_end varchar(255),
        timeperiod_freetext varchar(255),
        timeperiod_start varchar(255),
        absoluteelevation integer,
        absoluteelevationerror integer,
        collectingmethod varchar(255),
        distancetoground integer,
        distancetowatersurface integer,
        exactlocation_errorradius integer,
        exactlocation_latitude double,
        exactlocation_longitude double,
        createdby_id integer,
        updatedby_id integer,
        country_id integer,
        actor_id integer,
        exactlocation_referencesystem_id integer,
        locality_id integer,
        primary key (id, REV)
    );

    create table GatheringEvent_Annotation (
        GatheringEvent_id integer not null,
        annotations_id integer not null,
        primary key (GatheringEvent_id, annotations_id),
        unique (annotations_id)
    );

    create table GatheringEvent_Annotation_AUD (
        REV integer not null,
        GatheringEvent_id integer not null,
        annotations_id integer not null,
        revtype tinyint,
        primary key (REV, GatheringEvent_id, annotations_id)
    );

    create table GatheringEvent_DefinedTermBase (
        GatheringEvent_id integer not null,
        collectingareas_id integer not null,
        primary key (GatheringEvent_id, collectingareas_id)
    );

    create table GatheringEvent_DefinedTermBase_AUD (
        REV integer not null,
        GatheringEvent_id integer not null,
        collectingareas_id integer not null,
        revtype tinyint,
        primary key (REV, GatheringEvent_id, collectingareas_id)
    );

    create table GatheringEvent_Marker (
        GatheringEvent_id integer not null,
        markers_id integer not null,
        primary key (GatheringEvent_id, markers_id),
        unique (markers_id)
    );

    create table GatheringEvent_Marker_AUD (
        REV integer not null,
        GatheringEvent_id integer not null,
        markers_id integer not null,
        revtype tinyint,
        primary key (REV, GatheringEvent_id, markers_id)
    );

    create table GenBankAccession (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        accessionnumber varchar(255),
        uri varchar(255),
        createdby_id integer,
        updatedby_id integer,
        primary key (id),
        unique (uuid)
    );

    create table GenBankAccession_AUD (
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        accessionnumber varchar(255),
        uri varchar(255),
        createdby_id integer,
        updatedby_id integer,
        primary key (id, REV)
    );

    create table GrantedAuthorityImpl (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        authority varchar(255),
        createdby_id integer,
        primary key (id),
        unique (uuid, authority)
    );
	
	create table hibernate_sequences (
		sequence_name varchar(255),
		next_val integer,
		primary key (sequence_name)
	);

    create table HomotypicalGroup (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        createdby_id integer,
        updatedby_id integer,
        primary key (id),
        unique (uuid)
    );

    create table HomotypicalGroup_AUD (
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        createdby_id integer,
        updatedby_id integer,
        primary key (id, REV)
    );

    create table HomotypicalGroup_Annotation (
        HomotypicalGroup_id integer not null,
        annotations_id integer not null,
        primary key (HomotypicalGroup_id, annotations_id),
        unique (annotations_id)
    );

    create table HomotypicalGroup_Annotation_AUD (
        REV integer not null,
        HomotypicalGroup_id integer not null,
        annotations_id integer not null,
        revtype tinyint,
        primary key (REV, HomotypicalGroup_id, annotations_id)
    );

    create table HomotypicalGroup_Marker (
        HomotypicalGroup_id integer not null,
        markers_id integer not null,
        primary key (HomotypicalGroup_id, markers_id),
        unique (markers_id)
    );

    create table HomotypicalGroup_Marker_AUD (
        REV integer not null,
        HomotypicalGroup_id integer not null,
        markers_id integer not null,
        revtype tinyint,
        primary key (REV, HomotypicalGroup_id, markers_id)
    );

    create table HybridRelationship (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        citationmicroreference varchar(255),
        originalnamestring varchar(255),
        doubtful bit not null,
        ruleconsidered varchar(255),
        createdby_id integer,
        updatedby_id integer,
        citation_id integer,
        relatedfrom_id integer,
        relatedto_id integer,
        type_id integer,
        primary key (id),
        unique (uuid)
    );

    create table HybridRelationship_AUD (
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        citationmicroreference varchar(255),
        originalnamestring varchar(255),
        doubtful bit,
        ruleconsidered varchar(255),
        createdby_id integer,
        updatedby_id integer,
        citation_id integer,
        relatedfrom_id integer,
        relatedto_id integer,
        type_id integer,
        primary key (id, REV)
    );

    create table HybridRelationship_Annotation (
        HybridRelationship_id integer not null,
        annotations_id integer not null,
        primary key (HybridRelationship_id, annotations_id),
        unique (annotations_id)
    );

    create table HybridRelationship_Annotation_AUD (
        REV integer not null,
        HybridRelationship_id integer not null,
        annotations_id integer not null,
        revtype tinyint,
        primary key (REV, HybridRelationship_id, annotations_id)
    );

    create table HybridRelationship_Marker (
        HybridRelationship_id integer not null,
        markers_id integer not null,
        primary key (HybridRelationship_id, markers_id),
        unique (markers_id)
    );

    create table HybridRelationship_Marker_AUD (
        REV integer not null,
        HybridRelationship_id integer not null,
        markers_id integer not null,
        revtype tinyint,
        primary key (REV, HybridRelationship_id, markers_id)
    );

    create table IndividualAssociation_LanguageString (
        DescriptionElementBase_id integer not null,
        description_id integer not null,
        description_mapkey_id integer not null,
        primary key (DescriptionElementBase_id, description_mapkey_id),
        unique (description_id)
    );

    create table IndividualAssociation_LanguageString_AUD (
        REV integer not null,
        DescriptionElementBase_id integer not null,
        description_id integer not null,
        description_mapkey_id integer not null,
        revtype tinyint,
        primary key (REV, DescriptionElementBase_id, description_id, description_mapkey_id)
    );

    create table InstitutionalMembership (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        department varchar(255),
        period_end varchar(255),
        period_freetext varchar(255),
        period_start varchar(255),
        role varchar(255),
        createdby_id integer,
        updatedby_id integer,
        institute_id integer,
        person_id integer,
        primary key (id),
        unique (uuid)
    );

    create table InstitutionalMembership_AUD (
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        department varchar(255),
        period_end varchar(255),
        period_freetext varchar(255),
        period_start varchar(255),
        role varchar(255),
        createdby_id integer,
        updatedby_id integer,
        institute_id integer,
        person_id integer,
        primary key (id, REV)
    );
           
    create table KeyStatement (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        createdby_id integer,
        updatedby_id integer,
        primary key (id),
        unique (uuid)
    );
    
    create table KeyStatement_AUD (
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        createdby_id integer,
        updatedby_id integer,
        primary key (id, REV)
      );
      
     create table KeyStatement_LanguageString (
        keyStatement_id integer not null,
        label_id integer not null,
        label_mapkey_id integer not null,
        primary key (keyStatement_id, label_mapkey_id),
        unique (label_id)
    );

    create table KeyStatement_LanguageString_AUD (
        REV integer not null,
        keyStatement_id integer not null,
        label_id integer not null,
        label_mapkey_id integer not null,
        revtype tinyint,
        primary key (REV, keyStatement_id, label_id, label_mapkey_id)
    );

    create table LSIDAuthority (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        authority varchar(255),
        authoritywsdl longvarchar,
        port integer not null,
        server varchar(255),
        url varchar(255),
        createdby_id integer,
        primary key (id),
        unique (uuid, authority)
    );

    create table LSIDAuthority_namespaces (
        LSIDAuthority_id integer not null,
        namespaces_element varchar(255),
        namespaces_mapkey varchar(255),
        primary key (LSIDAuthority_id, namespaces_mapkey)
    );

    create table LanguageString (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        text longvarchar,
        createdby_id integer,
        updatedby_id integer,
        language_id integer,
        primary key (id),
        unique (uuid)
    );

    create table LanguageString_AUD (
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        text longvarchar,
        createdby_id integer,
        updatedby_id integer,
        language_id integer,
        primary key (id, REV)
    );

    create table LanguageString_Annotation (
        LanguageString_id integer not null,
        annotations_id integer not null,
        primary key (LanguageString_id, annotations_id),
        unique (annotations_id)
    );

    create table LanguageString_Annotation_AUD (
        REV integer not null,
        LanguageString_id integer not null,
        annotations_id integer not null,
        revtype tinyint,
        primary key (REV, LanguageString_id, annotations_id)
    );

    create table LanguageString_Marker (
        LanguageString_id integer not null,
        markers_id integer not null,
        primary key (LanguageString_id, markers_id),
        unique (markers_id)
    );

    create table LanguageString_Marker_AUD (
        REV integer not null,
        LanguageString_id integer not null,
        markers_id integer not null,
        revtype tinyint,
        primary key (REV, LanguageString_id, markers_id)
    );

    create table Locus (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        description varchar(255),
        name varchar(255),
        createdby_id integer,
        updatedby_id integer,
        primary key (id),
        unique (uuid)
    );

    create table Locus_AUD (
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        description varchar(255),
        name varchar(255),
        createdby_id integer,
        updatedby_id integer,
        primary key (id, REV)
    );

    create table Marker (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        flag bit not null,
        markedObj_type varchar(255),
        markedObj_id integer not null,
        createdby_id integer,
        updatedby_id integer,
        markertype_id integer,
        primary key (id),
        unique (uuid)
    );

    create table Marker_AUD (
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        flag bit,
        createdby_id integer,
        updatedby_id integer,
        markertype_id integer,
        primary key (id, REV)
    );

    create table Media (
        DTYPE varchar(31) not null,
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        lsid_authority varchar(255),
        lsid_lsid varchar(255),
        lsid_namespace varchar(255),
        lsid_object varchar(255),
        lsid_revision varchar(255),
        protectedtitlecache bit not null,
        titleCache varchar(255),
        mediacreated timestamp,
        citationmicroreference varchar(255),
        createdby_id integer,
        updatedby_id integer,
        artist_id integer,
        citation_id integer,
        primary key (id),
        unique (uuid)
    );

    create table MediaKey_NamedArea (
        Media_id integer not null,
        geographicalscope_id integer not null,
        primary key (Media_id, geographicalscope_id)
    );

    create table MediaKey_NamedArea_AUD (
        REV integer not null,
        Media_id integer not null,
        geographicalscope_id integer not null,
        revtype tinyint,
        primary key (REV, Media_id, geographicalscope_id)
    );

    create table MediaKey_Scope (
        Media_id integer not null,
        scoperestrictions_id integer not null,
        primary key (Media_id, scoperestrictions_id)
    );

    create table MediaKey_Scope_AUD (
        REV integer not null,
        Media_id integer not null,
        scoperestrictions_id integer not null,
        revtype tinyint,
        primary key (REV, Media_id, scoperestrictions_id)
    );

    create table MediaKey_Taxon (
        mediaKey_id integer not null,
        taxon_id integer not null,
        primary key (mediaKey_id, taxon_id)
    );

    create table MediaKey_Taxon_AUD (
        REV integer not null,
        mediaKey_id integer not null,
        taxon_id integer not null,
        revtype tinyint,
        primary key (REV, mediaKey_id, taxon_id)
    );

    create table MediaRepresentation (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        mimetype varchar(255),
        suffix varchar(255),
        createdby_id integer,
        updatedby_id integer,
        media_id integer,
        primary key (id),
        unique (uuid)
    );

    create table MediaRepresentationPart (
        DTYPE varchar(31) not null,
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        size integer,
        uri varchar(255),
        duration integer,
        height integer,
        width integer,
        createdby_id integer,
        updatedby_id integer,
        representation_id integer not null,
        sortIndex integer,
        primary key (id),
        unique (uuid)
    );

    create table MediaRepresentationPart_AUD (
        DTYPE varchar(31) not null,
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        size integer,
        uri varchar(255),
        createdby_id integer,
        updatedby_id integer,
        representation_id integer,
        duration integer,
        height integer,
        width integer,
        primary key (id, REV)
    );

    create table MediaRepresentation_AUD (
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        mimetype varchar(255),
        suffix varchar(255),
        createdby_id integer,
        updatedby_id integer,
        media_id integer,
        primary key (id, REV)
    );

    create table MediaRepresentation_MediaRepresentationPart_AUD (
        REV integer not null,
        representation_id integer not null,
        id integer not null,
        sortIndex integer not null,
        revtype tinyint,
        primary key (REV, representation_id, id, sortIndex)
    );

    create table Media_AUD (
        DTYPE varchar(31) not null,
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        lsid_authority varchar(255),
        lsid_lsid varchar(255),
        lsid_namespace varchar(255),
        lsid_object varchar(255),
        lsid_revision varchar(255),
        protectedtitlecache bit,
        titleCache varchar(255),
        mediacreated timestamp,
        createdby_id integer,
        updatedby_id integer,
        artist_id integer,
        citationmicroreference varchar(255),
        citation_id integer,
        primary key (id, REV)
    );

    create table Media_Annotation (
        Media_id integer not null,
        annotations_id integer not null,
        primary key (Media_id, annotations_id),
        unique (annotations_id)
    );

    create table Media_Annotation_AUD (
        REV integer not null,
        Media_id integer not null,
        annotations_id integer not null,
        revtype tinyint,
        primary key (REV, Media_id, annotations_id)
    );

    create table Media_Credit (
        Media_id integer not null,
        credits_id integer not null,
        sortIndex integer not null,
        primary key (Media_id, sortIndex),
        unique (credits_id)
    );

    create table Media_Credit_AUD (
        REV integer not null,
        Media_id integer not null,
        credits_id integer not null,
        sortIndex integer not null,
        revtype tinyint,
        primary key (REV, Media_id, credits_id, sortIndex)
    );

    create table Media_Description (
        Media_id integer not null,
        description_id integer not null,
        description_mapkey_id integer not null,
        primary key (Media_id, description_mapkey_id),
        unique (description_id)
    );

    create table Media_Description_AUD (
        REV integer not null,
        Media_id integer not null,
        description_id integer not null,
        description_mapkey_id integer not null,
        revtype tinyint,
        primary key (REV, Media_id, description_id, description_mapkey_id)
    );

    create table Media_Extension (
        Media_id integer not null,
        extensions_id integer not null,
        primary key (Media_id, extensions_id),
        unique (extensions_id)
    );

    create table Media_Extension_AUD (
        REV integer not null,
        Media_id integer not null,
        extensions_id integer not null,
        revtype tinyint,
        primary key (REV, Media_id, extensions_id)
    );

    create table Media_LanguageString (
        Media_id integer not null,
        title_id integer not null,
        title_mapkey_id integer not null,
        primary key (Media_id, title_mapkey_id),
        unique (title_id)
    );

    create table Media_LanguageString_AUD (
        REV integer not null,
        Media_id integer not null,
        title_id integer not null,
        title_mapkey_id integer not null,
        revtype tinyint,
        primary key (REV, Media_id, title_id, title_mapkey_id)
    );

    create table Media_Marker (
        Media_id integer not null,
        markers_id integer not null,
        primary key (Media_id, markers_id),
        unique (markers_id)
    );

    create table Media_Marker_AUD (
        REV integer not null,
        Media_id integer not null,
        markers_id integer not null,
        revtype tinyint,
        primary key (REV, Media_id, markers_id)
    );

    create table Media_OriginalSourceBase (
        Media_id integer not null,
        sources_id integer not null,
        primary key (Media_id, sources_id),
        unique (sources_id)
    );

    create table Media_OriginalSourceBase_AUD (
        REV integer not null,
        Media_id integer not null,
        sources_id integer not null,
        revtype tinyint,
        primary key (REV, Media_id, sources_id)
    );

    create table Media_Representation (
        Media_id integer not null,
        keyrepresentations_id integer not null,
        primary key (Media_id, keyrepresentations_id)
    );

    create table Media_Representation_AUD (
        REV integer not null,
        Media_id integer not null,
        keyrepresentations_id integer not null,
        revtype tinyint,
        primary key (REV, Media_id, keyrepresentations_id)
    );

    create table Media_Rights (
        Media_id integer not null,
        rights_id integer not null,
        primary key (Media_id, rights_id),
        unique (rights_id)
    );

    create table Media_Rights_AUD (
        REV integer not null,
        Media_id integer not null,
        rights_id integer not null,
        revtype tinyint,
        primary key (REV, Media_id, rights_id)
    );

    create table Media_Sequence (
        Media_id integer not null,
        usedsequences_id integer not null,
        primary key (Media_id, usedsequences_id),
        unique (usedsequences_id)
    );

    create table Media_Sequence_AUD (
        REV integer not null,
        Media_id integer not null,
        usedsequences_id integer not null,
        revtype tinyint,
        primary key (REV, Media_id, usedsequences_id)
    );

    create table Media_TaxonBase (
        Media_id integer not null,
        coveredtaxa_id integer not null,
        primary key (Media_id, coveredtaxa_id)
    );

    create table Media_TaxonBase_AUD (
        REV integer not null,
        Media_id integer not null,
        coveredtaxa_id integer not null,
        revtype tinyint,
        primary key (REV, Media_id, coveredtaxa_id)
    );

    create table MultiAccessKey_NamedArea (
        WorkingSet_id integer not null,
        geographicalscope_id integer not null,
        primary key (WorkingSet_id, geographicalscope_id)
    );

    create table MultiAccessKey_NamedArea_AUD (
        REV integer not null,
        WorkingSet_id integer not null,
        geographicalscope_id integer not null,
        revtype tinyint,
        primary key (REV, WorkingSet_id, geographicalscope_id)
    );

    create table MultiAccessKey_Scope (
        WorkingSet_id integer not null,
        scoperestrictions_id integer not null,
        primary key (WorkingSet_id, scoperestrictions_id)
    );

    create table MultiAccessKey_Scope_AUD (
        REV integer not null,
        WorkingSet_id integer not null,
        scoperestrictions_id integer not null,
        revtype tinyint,
        primary key (REV, WorkingSet_id, scoperestrictions_id)
    );

    create table MultiAccessKey_Taxon (
        multiAccessKey_id integer not null,
        taxon_id integer not null,
        primary key (multiAccessKey_id, taxon_id)
    );

    create table MultiAccessKey_Taxon_AUD (
        REV integer not null,
        multiAccessKey_id integer not null,
        taxon_id integer not null,
        revtype tinyint,
        primary key (REV, multiAccessKey_id, taxon_id)
    );

    create table NameRelationship (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        citationmicroreference varchar(255),
        originalnamestring varchar(255),
        doubtful bit not null,
        ruleconsidered varchar(255),
        createdby_id integer,
        updatedby_id integer,
        citation_id integer,
        relatedfrom_id integer,
        relatedto_id integer,
        type_id integer,
        primary key (id),
        unique (uuid)
    );

    create table NameRelationship_AUD (
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        citationmicroreference varchar(255),
        originalnamestring varchar(255),
        doubtful bit,
        ruleconsidered varchar(255),
        createdby_id integer,
        updatedby_id integer,
        citation_id integer,
        relatedfrom_id integer,
        relatedto_id integer,
        type_id integer,
        primary key (id, REV)
    );

    create table NameRelationship_Annotation (
        NameRelationship_id integer not null,
        annotations_id integer not null,
        primary key (NameRelationship_id, annotations_id),
        unique (annotations_id)
    );

    create table NameRelationship_Annotation_AUD (
        REV integer not null,
        NameRelationship_id integer not null,
        annotations_id integer not null,
        revtype tinyint,
        primary key (REV, NameRelationship_id, annotations_id)
    );

    create table NameRelationship_Marker (
        NameRelationship_id integer not null,
        markers_id integer not null,
        primary key (NameRelationship_id, markers_id),
        unique (markers_id)
    );

    create table NameRelationship_Marker_AUD (
        REV integer not null,
        NameRelationship_id integer not null,
        markers_id integer not null,
        revtype tinyint,
        primary key (REV, NameRelationship_id, markers_id)
    );

    create table NomenclaturalStatus (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        citationmicroreference varchar(255),
        originalnamestring varchar(255),
        ruleconsidered varchar(255),
        createdby_id integer,
        updatedby_id integer,
        citation_id integer,
        type_id integer,
        primary key (id),
        unique (uuid)
    );

    create table NomenclaturalStatus_AUD (
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        citationmicroreference varchar(255),
        originalnamestring varchar(255),
        ruleconsidered varchar(255),
        createdby_id integer,
        updatedby_id integer,
        citation_id integer,
        type_id integer,
        primary key (id, REV)
    );

    create table NomenclaturalStatus_Annotation (
        NomenclaturalStatus_id integer not null,
        annotations_id integer not null,
        primary key (NomenclaturalStatus_id, annotations_id),
        unique (annotations_id)
    );

    create table NomenclaturalStatus_Annotation_AUD (
        REV integer not null,
        NomenclaturalStatus_id integer not null,
        annotations_id integer not null,
        revtype tinyint,
        primary key (REV, NomenclaturalStatus_id, annotations_id)
    );

    create table NomenclaturalStatus_Marker (
        NomenclaturalStatus_id integer not null,
        markers_id integer not null,
        primary key (NomenclaturalStatus_id, markers_id),
        unique (markers_id)
    );

    create table NomenclaturalStatus_Marker_AUD (
        REV integer not null,
        NomenclaturalStatus_id integer not null,
        markers_id integer not null,
        revtype tinyint,
        primary key (REV, NomenclaturalStatus_id, markers_id)
    );

    create table OriginalSourceBase (
        DTYPE varchar(31) not null,
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        citationmicroreference varchar(255),
        originalnamestring varchar(255),
        idinsource varchar(255),
        idnamespace varchar(255),
        sourcedObj_type varchar(255),
        sourcedObj_id integer not null,
        createdby_id integer,
        updatedby_id integer,
        citation_id integer,
        nameusedinsource_id integer,
        primary key (id),
        unique (uuid)
    );

    create table OriginalSourceBase_AUD (
        DTYPE varchar(31) not null,
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        citationmicroreference varchar(255),
        originalnamestring varchar(255),
        idinsource varchar(255),
        idnamespace varchar(255),
        createdby_id integer,
        updatedby_id integer,
        citation_id integer,
        nameusedinsource_id integer,
        primary key (id, REV)
    );

    create table OriginalSourceBase_Annotation (
        OriginalSourceBase_id integer not null,
        annotations_id integer not null,
        primary key (OriginalSourceBase_id, annotations_id),
        unique (annotations_id)
    );

    create table OriginalSourceBase_Annotation_AUD (
        REV integer not null,
        OriginalSourceBase_id integer not null,
        annotations_id integer not null,
        revtype tinyint,
        primary key (REV, OriginalSourceBase_id, annotations_id)
    );

    create table OriginalSourceBase_Marker (
        OriginalSourceBase_id integer not null,
        markers_id integer not null,
        primary key (OriginalSourceBase_id, markers_id),
        unique (markers_id)
    );

    create table OriginalSourceBase_Marker_AUD (
        REV integer not null,
        OriginalSourceBase_id integer not null,
        markers_id integer not null,
        revtype tinyint,
        primary key (REV, OriginalSourceBase_id, markers_id)
    );

    create table PermissionGroup (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        name varchar(255),
        createdby_id integer,
        primary key (id),
        unique (uuid, name)
    );

    create table PermissionGroup_GrantedAuthorityImpl (
        PermissionGroup_id integer not null,
        grantedauthorities_id integer not null,
        primary key (PermissionGroup_id, grantedauthorities_id)
    );

   create table PolytomousKey (
       	id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        lsid_authority varchar(255),
        lsid_lsid varchar(255),
        lsid_namespace varchar(255),
        lsid_object varchar(255),
        lsid_revision varchar(255),
        protectedtitlecache bit not null,
        titleCache varchar(255),
        uri varchar(255),
        createdby_id integer,
        updatedby_id integer,
        root_id integer,
        primary key (id),
        unique (uuid)
    );

   create table PolytomousKey_AUD (
       	id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        lsid_authority varchar(255),
        lsid_lsid varchar(255),
        lsid_namespace varchar(255),
        lsid_object varchar(255),
        lsid_revision varchar(255),
        protectedtitlecache bit not null,
        titleCache varchar(255),
        uri varchar(255),
        createdby_id integer,
        updatedby_id integer,
        root_id integer,
        primary key (id, REV)
    );

    create table PolytomousKey_Annotation (
        PolytomousKey_id integer not null,
        annotations_id integer not null,
        primary key (PolytomousKey_id, annotations_id),
        unique (annotations_id)
    );

    create table PolytomousKey_Annotation_AUD (
        REV integer not null,
        PolytomousKey_id integer not null,
        annotations_id integer not null,
        revtype tinyint,
        primary key (REV, PolytomousKey_id, annotations_id)
    );

    create table PolytomousKey_Credit (
        PolytomousKey_id integer not null,
        credits_id integer not null,
        sortIndex integer not null,
        primary key (PolytomousKey_id, sortIndex),
        unique (credits_id)
    );

    create table PolytomousKey_Credit_AUD (
        REV integer not null,
        PolytomousKey_id integer not null,
        credits_id integer not null,
        sortIndex integer not null,
        revtype tinyint,
        primary key (REV, PolytomousKey_id, credits_id, sortIndex)
    );

    create table PolytomousKey_Extension (
        PolytomousKey_id integer not null,
        extensions_id integer not null,
        primary key (PolytomousKey_id, extensions_id),
        unique (extensions_id)
    );

    create table PolytomousKey_Extension_AUD (
        REV integer not null,
        PolytomousKey_id integer not null,
        extensions_id integer not null,
        revtype tinyint,
        primary key (REV, PolytomousKey_id, extensions_id)
    );

    create table PolytomousKey_Marker (
        PolytomousKey_id integer not null,
        markers_id integer not null,
        primary key (PolytomousKey_id, markers_id),
        unique (markers_id)
    );

    create table PolytomousKey_Marker_AUD (
        REV integer not null,
        PolytomousKey_id integer not null,
        markers_id integer not null,
        revtype tinyint,
        primary key (REV, PolytomousKey_id, markers_id)
    );

    create table PolytomousKey_NamedArea (
        PolytomousKey_id integer not null,
        geographicalscope_id integer not null,
        primary key (PolytomousKey_id, geographicalscope_id)
    );

    create table PolytomousKey_NamedArea_AUD (
        REV integer not null,
        PolytomousKey_id integer not null,
        geographicalscope_id integer not null,
        revtype tinyint,
        primary key (REV, PolytomousKey_id, geographicalscope_id)
    );
    
    create table PolytomousKey_OriginalSourceBase (
        PolytomousKey_id integer not null,
        sources_id integer not null,
        primary key (PolytomousKey_id, sources_id),
        unique (sources_id)
    );

    create table PolytomousKey_OriginalSourceBase_AUD (
        REV integer not null,
        PolytomousKey_id integer not null,
        sources_id integer not null,
        revtype tinyint,
        primary key (REV, PolytomousKey_id, sources_id)
    );


    create table PolytomousKey_Rights (
        PolytomousKey_id integer not null,
        rights_id integer not null,
        primary key (PolytomousKey_id, rights_id),
        unique (rights_id)
    );

    create table PolytomousKey_Rights_AUD (
        REV integer not null,
        PolytomousKey_id integer not null,
        rights_id integer not null,
        revtype tinyint,
        primary key (REV, PolytomousKey_id, rights_id)
    );

    create table PolytomousKey_Scope (
        PolytomousKey_id integer not null,
        scoperestrictions_id integer not null,
        primary key (PolytomousKey_id, scoperestrictions_id)
    );

    create table PolytomousKey_Scope_AUD (
        REV integer not null,
        PolytomousKey_id integer not null,
        scoperestrictions_id integer not null,
        revtype tinyint,
        primary key (REV, PolytomousKey_id, scoperestrictions_id)
    );

    create table PolytomousKey_Taxon (
        polytomousKey_id integer not null,
        taxon_id integer not null,
        primary key (polytomousKey_id, taxon_id)
    );

    create table PolytomousKey_Taxon_AUD (
        REV integer not null,
        polytomousKey_id integer not null,
        taxon_id integer not null,
        revtype tinyint,
        primary key (REV, polytomousKey_id, taxon_id)
    );
    
    create table PolytomousKey_TaxonBase (
        polytomousKey_id integer not null,
        coveredtaxa_id integer not null,
        primary key (polytomousKey_id, coveredtaxa_id)
    );

    create table PolytomousKey_TaxonBase_AUD (
        REV integer not null,
        polytomousKey_id integer not null,
        coveredtaxa_id integer not null,
        revtype tinyint,
        primary key (REV, polytomousKey_id, coveredtaxa_id)
    );
    
        
    create table PolytomousKeyNode (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        nodenumber integer,
        sortindex integer,
        createdby_id integer,
        updatedby_id integer,
        key_id integer,
        parent_id integer,
        question_id integer,
        statement_id integer,
        feature_id integer,
        taxon_id integer,
        subkey_id integer,
        othernode_id integer,
        primary key (id),
        unique (uuid)
    );

    create table PolytomousKeyNode_AUD (
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        nodenumber integer,
        sortindex integer,
        createdby_id integer,
        updatedby_id integer,
        key_id integer,
        parent_id integer,
        question_id integer,
        statement_id integer,
        feature_id integer,
        taxon_id integer,
        subkey_id integer,
        othernode_id integer,
        primary key (id, REV)
    );
    
    create table PolytomousKeyNode_LanguageString (
		PolytomousKeyNode_id integer not null,
		modifyingtext_id integer not null,
		modifyingtext_mapkey_id integer not null,
		primary key (PolytomousKeyNode_id, modifyingtext_mapkey_id),
		unique (modifyingtext_id)
	)
	
	create table PolytomousKeyNode_LanguageString_AUD (
		REV integer not null,
		revtype tinyint,
		PolytomousKeyNode_id integer not null,
		modifyingtext_id integer not null,
		modifyingtext_mapkey_id integer not null,
		primary key (REV, PolytomousKeyNode_id, modifyingtext_id, modifyingtext_mapkey_id)
	)
    
    create table PolytomousKeyNode_PolytomousKeyNode_AUD(
    	id integer not null,
        REV integer not null,
        revtype tinyint,
        parent_id integer, 
        sortIndex integer
    )

    create table Reference (
        DTYPE varchar(31) not null,
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        lsid_authority varchar(255),
        lsid_lsid varchar(255),
        lsid_namespace varchar(255),
        lsid_object varchar(255),
        lsid_revision varchar(255),
        protectedtitlecache bit not null,
        titleCache varchar(255),
        datepublished_end varchar(255),
        datepublished_freetext varchar(255),
        datepublished_start varchar(255),
        edition varchar(255),
        editor varchar(255),
        isbn varchar(255),
        issn varchar(255),
        nomenclaturallyrelevant bit not null,
        organization varchar(255),
        pages varchar(255),
        parsingproblem integer not null,
        placepublished varchar(255),
        problemends integer not null,
        problemstarts integer not null,
        publisher varchar(255),
        referenceAbstract longvarchar,
        series varchar(255),
        seriespart varchar(255),
        title longvarchar,
        refType integer,
        uri varchar(255),
        volume varchar(255),
        createdby_id integer,
        updatedby_id integer,
        authorteam_id integer,
        inreference_id integer,
        institution_id integer,
        school_id integer,
        abbreviatedReference_id integer,
        fullReference_id integer,
        primary key (id),
        unique (uuid)
    );

    create table Reference_AUD (
        DTYPE varchar(31) not null,
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        lsid_authority varchar(255),
        lsid_lsid varchar(255),
        lsid_namespace varchar(255),
        lsid_object varchar(255),
        lsid_revision varchar(255),
        protectedtitlecache bit,
        titleCache varchar(255),
        datepublished_end varchar(255),
        datepublished_freetext varchar(255),
        datepublished_start varchar(255),
        edition varchar(255),
        editor varchar(255),
        isbn varchar(255),
        issn varchar(255),
        nomenclaturallyrelevant bit,
        organization varchar(255),
        pages varchar(255),
        parsingproblem integer,
        placepublished varchar(255),
        problemends integer,
        problemstarts integer,
        publisher varchar(255),
        referenceAbstract longvarchar,
        series varchar(255),
        seriespart varchar(255),
        title longvarchar,
        refType integer,
        uri varchar(255),
        volume varchar(255),
        createdby_id integer,
        updatedby_id integer,
        authorteam_id integer,
        inreference_id integer,
        institution_id integer,
        school_id integer,
        abbreviatedReference_id integer,
        fullReference_id integer,
        primary key (id, REV)
    );

    create table Reference_Annotation (
        Reference_id integer not null,
        annotations_id integer not null,
        primary key (Reference_id, annotations_id),
        unique (annotations_id)
    );

    create table Reference_Annotation_AUD (
        REV integer not null,
        Reference_id integer not null,
        annotations_id integer not null,
        revtype tinyint,
        primary key (REV, Reference_id, annotations_id)
    );

    create table Reference_Credit (
        Reference_id integer not null,
        credits_id integer not null,
        sortIndex integer not null,
        primary key (Reference_id, sortIndex),
        unique (credits_id)
    );

    create table Reference_Credit_AUD (
        REV integer not null,
        Reference_id integer not null,
        credits_id integer not null,
        sortIndex integer not null,
        revtype tinyint,
        primary key (REV, Reference_id, credits_id, sortIndex)
    );

    create table Reference_Extension (
        Reference_id integer not null,
        extensions_id integer not null,
        primary key (Reference_id, extensions_id),
        unique (extensions_id)
    );

    create table Reference_Extension_AUD (
        REV integer not null,
        Reference_id integer not null,
        extensions_id integer not null,
        revtype tinyint,
        primary key (REV, Reference_id, extensions_id)
    );

    create table Reference_Marker (
        Reference_id integer not null,
        markers_id integer not null,
        primary key (Reference_id, markers_id),
        unique (markers_id)
    );

    create table Reference_Marker_AUD (
        REV integer not null,
        Reference_id integer not null,
        markers_id integer not null,
        revtype tinyint,
        primary key (REV, Reference_id, markers_id)
    );

    create table Reference_Media (
        Reference_id integer not null,
        media_id integer not null,
        primary key (Reference_id, media_id)
    );

    create table Reference_Media_AUD (
        REV integer not null,
        Reference_id integer not null,
        media_id integer not null,
        revtype tinyint,
        primary key (REV, Reference_id, media_id)
    );

    create table Reference_OriginalSourceBase (
        Reference_id integer not null,
        sources_id integer not null,
        primary key (Reference_id, sources_id),
        unique (sources_id)
    );

    create table Reference_OriginalSourceBase_AUD (
        REV integer not null,
        Reference_id integer not null,
        sources_id integer not null,
        revtype tinyint,
        primary key (REV, Reference_id, sources_id)
    );

    create table Reference_Rights (
        Reference_id integer not null,
        rights_id integer not null,
        primary key (Reference_id, rights_id),
        unique (rights_id)
    );

    create table Reference_Rights_AUD (
        REV integer not null,
        Reference_id integer not null,
        rights_id integer not null,
        revtype tinyint,
        primary key (REV, Reference_id, rights_id)
    );

    create table RelationshipTermBase_inverseRepresentation (
        DefinedTermBase_id integer not null,
        inverserepresentations_id integer not null,
        primary key (DefinedTermBase_id, inverserepresentations_id),
        unique (inverserepresentations_id)
    );

    create table RelationshipTermBase_inverseRepresentation_AUD (
        REV integer not null,
        DefinedTermBase_id integer not null,
        inverserepresentations_id integer not null,
        revtype tinyint,
        primary key (REV, DefinedTermBase_id, inverserepresentations_id)
    );

    create table Representation (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        text longvarchar,
        abbreviatedlabel varchar(255),
        label varchar(255),
        createdby_id integer,
        updatedby_id integer,
        language_id integer,
        primary key (id),
        unique (uuid)
    );

    create table Representation_AUD (
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        text longvarchar,
        abbreviatedlabel varchar(255),
        label varchar(255),
        createdby_id integer,
        updatedby_id integer,
        language_id integer,
        primary key (id, REV)
    );

    create table Representation_Annotation (
        Representation_id integer not null,
        annotations_id integer not null,
        primary key (Representation_id, annotations_id),
        unique (annotations_id)
    );

    create table Representation_Annotation_AUD (
        REV integer not null,
        Representation_id integer not null,
        annotations_id integer not null,
        revtype tinyint,
        primary key (REV, Representation_id, annotations_id)
    );

    create table Representation_Marker (
        Representation_id integer not null,
        markers_id integer not null,
        primary key (Representation_id, markers_id),
        unique (markers_id)
    );

    create table Representation_Marker_AUD (
        REV integer not null,
        Representation_id integer not null,
        markers_id integer not null,
        revtype tinyint,
        primary key (REV, Representation_id, markers_id)
    );

    create table Rights (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        text longvarchar,
        abbreviatedtext varchar(255),
        uri varchar(255),
        createdby_id integer,
        updatedby_id integer,
        language_id integer,
        agent_id integer,
        type_id integer,
        primary key (id),
        unique (uuid)
    );

    create table Rights_AUD (
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        text longvarchar,
        abbreviatedtext varchar(255),
        uri varchar(255),
        createdby_id integer,
        updatedby_id integer,
        language_id integer,
        agent_id integer,
        type_id integer,
        primary key (id, REV)
    );

    create table Rights_Annotation (
        Rights_id integer not null,
        annotations_id integer not null,
        primary key (Rights_id, annotations_id),
        unique (annotations_id)
    );

    create table Rights_Annotation_AUD (
        REV integer not null,
        Rights_id integer not null,
        annotations_id integer not null,
        revtype tinyint,
        primary key (REV, Rights_id, annotations_id)
    );

    create table Rights_Marker (
        Rights_id integer not null,
        markers_id integer not null,
        primary key (Rights_id, markers_id),
        unique (markers_id)
    );

    create table Rights_Marker_AUD (
        REV integer not null,
        Rights_id integer not null,
        markers_id integer not null,
        revtype tinyint,
        primary key (REV, Rights_id, markers_id)
    );

    create table Sequence (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        lsid_authority varchar(255),
        lsid_lsid varchar(255),
        lsid_namespace varchar(255),
        lsid_object varchar(255),
        lsid_revision varchar(255),
        protectedtitlecache bit not null,
        titleCache varchar(255),
        barcode bit not null,
        citationmicroreference varchar(255),
        datesequenced timestamp,
        length integer,
        sequence varchar(255),
        createdby_id integer,
        updatedby_id integer,
        locus_id integer,
        publishedin_id integer,
        primary key (id),
        unique (uuid)
    );

    create table Sequence_AUD (
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        lsid_authority varchar(255),
        lsid_lsid varchar(255),
        lsid_namespace varchar(255),
        lsid_object varchar(255),
        lsid_revision varchar(255),
        protectedtitlecache bit,
        titleCache varchar(255),
        barcode bit,
        citationmicroreference varchar(255),
        datesequenced timestamp,
        length integer,
        sequence varchar(255),
        createdby_id integer,
        updatedby_id integer,
        locus_id integer,
        publishedin_id integer,
        primary key (id, REV)
    );

    create table Sequence_Annotation (
        Sequence_id integer not null,
        annotations_id integer not null,
        primary key (Sequence_id, annotations_id),
        unique (annotations_id)
    );

    create table Sequence_Annotation_AUD (
        REV integer not null,
        Sequence_id integer not null,
        annotations_id integer not null,
        revtype tinyint,
        primary key (REV, Sequence_id, annotations_id)
    );

    create table Sequence_Credit (
        Sequence_id integer not null,
        credits_id integer not null,
        sortIndex integer not null,
        primary key (Sequence_id, sortIndex),
        unique (credits_id)
    );

    create table Sequence_Credit_AUD (
        REV integer not null,
        Sequence_id integer not null,
        credits_id integer not null,
        sortIndex integer not null,
        revtype tinyint,
        primary key (REV, Sequence_id, credits_id, sortIndex)
    );

    create table Sequence_Extension (
        Sequence_id integer not null,
        extensions_id integer not null,
        primary key (Sequence_id, extensions_id),
        unique (extensions_id)
    );

    create table Sequence_Extension_AUD (
        REV integer not null,
        Sequence_id integer not null,
        extensions_id integer not null,
        revtype tinyint,
        primary key (REV, Sequence_id, extensions_id)
    );

    create table Sequence_GenBankAccession (
        Sequence_id integer not null,
        genbankaccession_id integer not null,
        primary key (Sequence_id, genbankaccession_id),
        unique (genbankaccession_id)
    );

    create table Sequence_GenBankAccession_AUD (
        REV integer not null,
        Sequence_id integer not null,
        genbankaccession_id integer not null,
        revtype tinyint,
        primary key (REV, Sequence_id, genbankaccession_id)
    );

    create table Sequence_Marker (
        Sequence_id integer not null,
        markers_id integer not null,
        primary key (Sequence_id, markers_id),
        unique (markers_id)
    );

    create table Sequence_Marker_AUD (
        REV integer not null,
        Sequence_id integer not null,
        markers_id integer not null,
        revtype tinyint,
        primary key (REV, Sequence_id, markers_id)
    );

    create table Sequence_Media (
        Sequence_id integer not null,
        chromatograms_id integer not null,
        primary key (Sequence_id, chromatograms_id),
        unique (chromatograms_id)
    );

    create table Sequence_Media_AUD (
        REV integer not null,
        Sequence_id integer not null,
        chromatograms_id integer not null,
        revtype tinyint,
        primary key (REV, Sequence_id, chromatograms_id)
    );

    create table Sequence_OriginalSourceBase (
        Sequence_id integer not null,
        sources_id integer not null,
        primary key (Sequence_id, sources_id),
        unique (sources_id)
    );

    create table Sequence_OriginalSourceBase_AUD (
        REV integer not null,
        Sequence_id integer not null,
        sources_id integer not null,
        revtype tinyint,
        primary key (REV, Sequence_id, sources_id)
    );

    create table Sequence_Reference (
        Sequence_id integer not null,
        citations_id integer not null,
        primary key (Sequence_id, citations_id),
        unique (citations_id)
    );

    create table Sequence_Reference_AUD (
        REV integer not null,
        Sequence_id integer not null,
        citations_id integer not null,
        revtype tinyint,
        primary key (REV, Sequence_id, citations_id)
    );

    create table Sequence_Rights (
        Sequence_id integer not null,
        rights_id integer not null,
        primary key (Sequence_id, rights_id),
        unique (rights_id)
    );

    create table Sequence_Rights_AUD (
        REV integer not null,
        Sequence_id integer not null,
        rights_id integer not null,
        revtype tinyint,
        primary key (REV, Sequence_id, rights_id)
    );

    create table SpecimenOrObservationBase (
        DTYPE varchar(31) not null,
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        lsid_authority varchar(255),
        lsid_lsid varchar(255),
        lsid_namespace varchar(255),
        lsid_object varchar(255),
        lsid_revision varchar(255),
        protectedtitlecache bit not null,
        titleCache varchar(255),
        individualcount integer,
        accessionnumber varchar(255),
        catalognumber varchar(255),
        collectorsnumber varchar(255),
        barcode varchar(255),
        fieldnotes varchar(255),
        fieldnumber varchar(255),
        createdby_id integer,
        updatedby_id integer,
        lifestage_id integer,
        sex_id integer,    
        exsiccatum varchar(255),
    	primarycollector_id integer,
        collection_id integer,
        derivedfrom_id integer,
        storedunder_id integer,
        preservation_id integer,
        gatheringevent_id integer,
        primary key (id),
        unique (uuid)
    );
    

    create table SpecimenOrObservationBase_AUD (
        DTYPE varchar(31) not null,
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        lsid_authority varchar(255),
        lsid_lsid varchar(255),
        lsid_namespace varchar(255),
        lsid_object varchar(255),
        lsid_revision varchar(255),
        protectedtitlecache bit,
        titleCache varchar(255),
        individualcount integer,
        createdby_id integer,
        updatedby_id integer,
        lifestage_id integer,
        sex_id integer,
        fieldnotes varchar(255),
        fieldnumber varchar(255),
        barcode varchar(255),
        exsiccatum varchar(255),
        gatheringevent_id integer,
        accessionnumber varchar(255),
        catalognumber varchar(255),
        collectorsnumber varchar(255),
        primarycollector_id integer,
        collection_id integer,
        derivedfrom_id integer,
        storedunder_id integer,
        preservation_id integer,
        primary key (id, REV)
    );

    create table SpecimenOrObservationBase_Annotation (
        SpecimenOrObservationBase_id integer not null,
        annotations_id integer not null,
        primary key (SpecimenOrObservationBase_id, annotations_id),
        unique (annotations_id)
    );

    create table SpecimenOrObservationBase_Annotation_AUD (
        REV integer not null,
        SpecimenOrObservationBase_id integer not null,
        annotations_id integer not null,
        revtype tinyint,
        primary key (REV, SpecimenOrObservationBase_id, annotations_id)
    );

    create table SpecimenOrObservationBase_Credit (
        SpecimenOrObservationBase_id integer not null,
        credits_id integer not null,
        sortIndex integer not null,
        primary key (SpecimenOrObservationBase_id, sortIndex),
        unique (credits_id)
    );

    create table SpecimenOrObservationBase_Credit_AUD (
        REV integer not null,
        SpecimenOrObservationBase_id integer not null,
        credits_id integer not null,
        sortIndex integer not null,
        revtype tinyint,
        primary key (REV, SpecimenOrObservationBase_id, credits_id, sortIndex)
    );

    create table SpecimenOrObservationBase_DerivationEvent (
        originals_id integer not null,
        derivationevents_id integer not null,
        primary key (originals_id, derivationevents_id)
    );

    create table SpecimenOrObservationBase_DerivationEvent_AUD (
        REV integer not null,
        originals_id integer not null,
        derivationevents_id integer not null,
        revtype tinyint,
        primary key (REV, originals_id, derivationevents_id)
    );

    create table SpecimenOrObservationBase_Extension (
        SpecimenOrObservationBase_id integer not null,
        extensions_id integer not null,
        primary key (SpecimenOrObservationBase_id, extensions_id),
        unique (extensions_id)
    );

    create table SpecimenOrObservationBase_Extension_AUD (
        REV integer not null,
        SpecimenOrObservationBase_id integer not null,
        extensions_id integer not null,
        revtype tinyint,
        primary key (REV, SpecimenOrObservationBase_id, extensions_id)
    );

    create table SpecimenOrObservationBase_LanguageString (
        SpecimenOrObservationBase_id integer not null,
        definition_id integer not null,
        definition_mapkey_id integer not null,
        primary key (SpecimenOrObservationBase_id, definition_mapkey_id),
        unique (definition_id)
    );

    create table SpecimenOrObservationBase_LanguageString_AUD (
        REV integer not null,
        SpecimenOrObservationBase_id integer not null,
        definition_id integer not null,
        definition_mapkey_id integer not null,
        revtype tinyint,
        primary key (REV, SpecimenOrObservationBase_id, definition_id, definition_mapkey_id)
    );

    create table SpecimenOrObservationBase_Marker (
        SpecimenOrObservationBase_id integer not null,
        markers_id integer not null,
        primary key (SpecimenOrObservationBase_id, markers_id),
        unique (markers_id)
    );

    create table SpecimenOrObservationBase_Marker_AUD (
        REV integer not null,
        SpecimenOrObservationBase_id integer not null,
        markers_id integer not null,
        revtype tinyint,
        primary key (REV, SpecimenOrObservationBase_id, markers_id)
    );

    create table SpecimenOrObservationBase_Media (
        SpecimenOrObservationBase_id integer not null,
        media_id integer not null,
        primary key (SpecimenOrObservationBase_id, media_id)
    );

    create table SpecimenOrObservationBase_Media_AUD (
        REV integer not null,
        SpecimenOrObservationBase_id integer not null,
        media_id integer not null,
        revtype tinyint,
        primary key (REV, SpecimenOrObservationBase_id, media_id)
    );

    create table SpecimenOrObservationBase_OriginalSourceBase (
        SpecimenOrObservationBase_id integer not null,
        sources_id integer not null,
        primary key (SpecimenOrObservationBase_id, sources_id),
        unique (sources_id)
    );

    create table SpecimenOrObservationBase_OriginalSourceBase_AUD (
        REV integer not null,
        SpecimenOrObservationBase_id integer not null,
        sources_id integer not null,
        revtype tinyint,
        primary key (REV, SpecimenOrObservationBase_id, sources_id)
    );

    create table SpecimenOrObservationBase_Rights (
        SpecimenOrObservationBase_id integer not null,
        rights_id integer not null,
        primary key (SpecimenOrObservationBase_id, rights_id),
        unique (rights_id)
    );

    create table SpecimenOrObservationBase_Rights_AUD (
        REV integer not null,
        SpecimenOrObservationBase_id integer not null,
        rights_id integer not null,
        revtype tinyint,
        primary key (REV, SpecimenOrObservationBase_id, rights_id)
    );

    create table SpecimenOrObservationBase_Sequence (
        SpecimenOrObservationBase_id integer not null,
        sequences_id integer not null,
        primary key (SpecimenOrObservationBase_id, sequences_id),
        unique (sequences_id)
    );

    create table SpecimenOrObservationBase_Sequence_AUD (
        REV integer not null,
        SpecimenOrObservationBase_id integer not null,
        sequences_id integer not null,
        revtype tinyint,
        primary key (REV, SpecimenOrObservationBase_id, sequences_id)
    );

    create table StateData (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        createdby_id integer,
        updatedby_id integer,
        state_id integer,
        primary key (id),
        unique (uuid)
    );

    create table StateData_AUD (
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        createdby_id integer,
        updatedby_id integer,
        state_id integer,
        primary key (id, REV)
    );

    create table StateData_DefinedTermBase (
        StateData_id integer not null,
        modifiers_id integer not null,
        primary key (StateData_id, modifiers_id),
        unique (modifiers_id)
    );

    create table StateData_DefinedTermBase_AUD (
        REV integer not null,
        StateData_id integer not null,
        modifiers_id integer not null,
        revtype tinyint,
        primary key (REV, StateData_id, modifiers_id)
    );

    create table StateData_LanguageString (
        StateData_id integer not null,
        modifyingtext_id integer not null,
        modifyingtext_mapkey_id integer not null,
        primary key (StateData_id, modifyingtext_mapkey_id),
        unique (modifyingtext_id)
    );

    create table StateData_LanguageString_AUD (
        REV integer not null,
        StateData_id integer not null,
        modifyingtext_id integer not null,
        modifyingtext_mapkey_id integer not null,
        revtype tinyint,
        primary key (REV, StateData_id, modifyingtext_id, modifyingtext_mapkey_id)
    );

    create table StatisticalMeasurementValue (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        value float not null,
        createdby_id integer,
        updatedby_id integer,
        type_id integer,
        primary key (id),
        unique (uuid)
    );

    create table StatisticalMeasurementValue_AUD (
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        value float,
        createdby_id integer,
        updatedby_id integer,
        type_id integer,
        primary key (id, REV)
    );

    create table StatisticalMeasurementValue_DefinedTermBase (
        StatisticalMeasurementValue_id integer not null,
        modifiers_id integer not null,
        primary key (StatisticalMeasurementValue_id, modifiers_id),
        unique (modifiers_id)
    );

    create table StatisticalMeasurementValue_DefinedTermBase_AUD (
        REV integer not null,
        StatisticalMeasurementValue_id integer not null,
        modifiers_id integer not null,
        revtype tinyint,
        primary key (REV, StatisticalMeasurementValue_id, modifiers_id)
    );

    create table SynonymRelationship (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        citationmicroreference varchar(255),
        originalnamestring varchar(255),
        doubtful bit not null,
        partial bit not null,
        proparte bit not null,
        createdby_id integer,
        updatedby_id integer,
        citation_id integer,
        relatedfrom_id integer,
        relatedto_id integer,
        type_id integer,
        primary key (id),
        unique (uuid)
    );

    create table SynonymRelationship_AUD (
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        citationmicroreference varchar(255),
        originalnamestring varchar(255),
        doubtful bit,
        partial bit,
        proparte bit,
        createdby_id integer,
        updatedby_id integer,
        citation_id integer,
        relatedfrom_id integer,
        relatedto_id integer,
        type_id integer,
        primary key (id, REV)
    );

    create table SynonymRelationship_Annotation (
        SynonymRelationship_id integer not null,
        annotations_id integer not null,
        primary key (SynonymRelationship_id, annotations_id),
        unique (annotations_id)
    );

    create table SynonymRelationship_Annotation_AUD (
        REV integer not null,
        SynonymRelationship_id integer not null,
        annotations_id integer not null,
        revtype tinyint,
        primary key (REV, SynonymRelationship_id, annotations_id)
    );

    create table SynonymRelationship_Marker (
        SynonymRelationship_id integer not null,
        markers_id integer not null,
        primary key (SynonymRelationship_id, markers_id),
        unique (markers_id)
    );

    create table SynonymRelationship_Marker_AUD (
        REV integer not null,
        SynonymRelationship_id integer not null,
        markers_id integer not null,
        revtype tinyint,
        primary key (REV, SynonymRelationship_id, markers_id)
    );

    create table TaxonBase (
        DTYPE varchar(31) not null,
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        lsid_authority varchar(255),
        lsid_lsid varchar(255),
        lsid_namespace varchar(255),
        lsid_object varchar(255),
        lsid_revision varchar(255),
        protectedtitlecache bit not null,
        titleCache varchar(255),
        appendedphrase varchar(255),
        doubtful bit not null,
        usenamecache bit not null,
        taxonstatusunknown bit,
        unplaced bit,
        excluded bit,
        taxonomicchildrencount integer,
        createdby_id integer,
        updatedby_id integer,
        name_id integer,
        sec_id integer,
        taxonomicparentcache_id integer,
        primary key (id),
        unique (uuid)
    );

    create table TaxonBase_AUD (
        DTYPE varchar(31) not null,
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        lsid_authority varchar(255),
        lsid_lsid varchar(255),
        lsid_namespace varchar(255),
        lsid_object varchar(255),
        lsid_revision varchar(255),
        protectedtitlecache bit,
        titleCache varchar(255),
        appendedphrase varchar(255),
        doubtful bit,
        usenamecache bit,
        createdby_id integer,
        updatedby_id integer,
        name_id integer,
        sec_id integer,
        taxonstatusunknown bit,
        unplaced bit,
        excluded bit,
        taxonomicchildrencount integer,
        taxonomicparentcache_id integer,
        primary key (id, REV)
    );

    create table TaxonBase_Annotation (
        TaxonBase_id integer not null,
        annotations_id integer not null,
        primary key (TaxonBase_id, annotations_id),
        unique (annotations_id)
    );

    create table TaxonBase_Annotation_AUD (
        REV integer not null,
        TaxonBase_id integer not null,
        annotations_id integer not null,
        revtype tinyint,
        primary key (REV, TaxonBase_id, annotations_id)
    );

    create table TaxonBase_Credit (
        TaxonBase_id integer not null,
        credits_id integer not null,
        sortIndex integer not null,
        primary key (TaxonBase_id, sortIndex),
        unique (credits_id)
    );

    create table TaxonBase_Credit_AUD (
        REV integer not null,
        TaxonBase_id integer not null,
        credits_id integer not null,
        sortIndex integer not null,
        revtype tinyint,
        primary key (REV, TaxonBase_id, credits_id, sortIndex)
    );

    create table TaxonBase_Extension (
        TaxonBase_id integer not null,
        extensions_id integer not null,
        primary key (TaxonBase_id, extensions_id),
        unique (extensions_id)
    );

    create table TaxonBase_Extension_AUD (
        REV integer not null,
        TaxonBase_id integer not null,
        extensions_id integer not null,
        revtype tinyint,
        primary key (REV, TaxonBase_id, extensions_id)
    );

    create table TaxonBase_Marker (
        TaxonBase_id integer not null,
        markers_id integer not null,
        primary key (TaxonBase_id, markers_id),
        unique (markers_id)
    );

    create table TaxonBase_Marker_AUD (
        REV integer not null,
        TaxonBase_id integer not null,
        markers_id integer not null,
        revtype tinyint,
        primary key (REV, TaxonBase_id, markers_id)
    );

    create table TaxonBase_OriginalSourceBase (
        TaxonBase_id integer not null,
        sources_id integer not null,
        primary key (TaxonBase_id, sources_id),
        unique (sources_id)
    );

    create table TaxonBase_OriginalSourceBase_AUD (
        REV integer not null,
        TaxonBase_id integer not null,
        sources_id integer not null,
        revtype tinyint,
        primary key (REV, TaxonBase_id, sources_id)
    );

    create table TaxonBase_Rights (
        TaxonBase_id integer not null,
        rights_id integer not null,
        primary key (TaxonBase_id, rights_id),
        unique (rights_id)
    );

    create table TaxonBase_Rights_AUD (
        REV integer not null,
        TaxonBase_id integer not null,
        rights_id integer not null,
        revtype tinyint,
        primary key (REV, TaxonBase_id, rights_id)
    );

    create table TaxonInteraction_LanguageString (
        DescriptionElementBase_id integer not null,
        description_id integer not null,
        description_mapkey_id integer not null,
        primary key (DescriptionElementBase_id, description_mapkey_id),
        unique (description_id)
    );

    create table TaxonInteraction_LanguageString_AUD (
        REV integer not null,
        DescriptionElementBase_id integer not null,
        description_id integer not null,
        description_mapkey_id integer not null,
        revtype tinyint,
        primary key (REV, DescriptionElementBase_id, description_id, description_mapkey_id)
    );

    create table TaxonNameBase (
        DTYPE varchar(31) not null,
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        lsid_authority varchar(255),
        lsid_lsid varchar(255),
        lsid_namespace varchar(255),
        lsid_object varchar(255),
        lsid_revision varchar(255),
        protectedtitlecache bit not null,
        titleCache varchar(255),
        appendedphrase varchar(255),
        fullTitleCache varchar(330),
        nomenclaturalmicroreference varchar(255),
        parsingproblem integer not null,
        problemends integer not null,
        problemstarts integer not null,
        protectedfulltitlecache bit not null,
        authorshipcache varchar(255),
        binomhybrid bit,
        genusoruninomial varchar(255),
        hybridformula bit,
        infragenericepithet varchar(255),
        infraspecificepithet varchar(255),
        monomhybrid bit,
        namecache varchar(255),
        protectedauthorshipcache bit,
        protectednamecache bit,
        specificepithet varchar(255),
        trinomhybrid bit,
        nameapprobation varchar(255),
        subgenusauthorship varchar(255),
        anamorphic bit,
        cultivarname varchar(255),
        acronym varchar(255),
        breed varchar(255),
        originalpublicationyear integer,
        publicationyear integer,
        createdby_id integer,
        updatedby_id integer,
        homotypicalgroup_id integer,
        nomenclaturalreference_id integer,
        rank_id integer,
        basionymauthorteam_id integer,
        combinationauthorteam_id integer,
        exbasionymauthorteam_id integer,
        excombinationauthorteam_id integer,
        primary key (id),
        unique (uuid)
    );

    create table TaxonNameBase_AUD (
        DTYPE varchar(31) not null,
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        lsid_authority varchar(255),
        lsid_lsid varchar(255),
        lsid_namespace varchar(255),
        lsid_object varchar(255),
        lsid_revision varchar(255),
        protectedtitlecache bit,
        titleCache varchar(255),
        appendedphrase varchar(255),
        fullTitleCache varchar(330),
        nomenclaturalmicroreference varchar(255),
        parsingproblem integer,
        problemends integer,
        problemstarts integer,
        protectedfulltitlecache bit,
        createdby_id integer,
        updatedby_id integer,
        homotypicalgroup_id integer,
        nomenclaturalreference_id integer,
        rank_id integer,
        acronym varchar(255),
        authorshipcache varchar(255),
        binomhybrid bit,
        genusoruninomial varchar(255),
        hybridformula bit,
        infragenericepithet varchar(255),
        infraspecificepithet varchar(255),
        monomhybrid bit,
        namecache varchar(255),
        protectedauthorshipcache bit,
        protectednamecache bit,
        specificepithet varchar(255),
        trinomhybrid bit,
        basionymauthorteam_id integer,
        combinationauthorteam_id integer,
        exbasionymauthorteam_id integer,
        excombinationauthorteam_id integer,
        anamorphic bit,
        breed varchar(255),
        originalpublicationyear integer,
        publicationyear integer,
        cultivarname varchar(255),
        nameapprobation varchar(255),
        subgenusauthorship varchar(255),
        primary key (id, REV)
    );

    create table TaxonNameBase_Annotation (
        TaxonNameBase_id integer not null,
        annotations_id integer not null,
        primary key (TaxonNameBase_id, annotations_id),
        unique (annotations_id)
    );

    create table TaxonNameBase_Annotation_AUD (
        REV integer not null,
        TaxonNameBase_id integer not null,
        annotations_id integer not null,
        revtype tinyint,
        primary key (REV, TaxonNameBase_id, annotations_id)
    );

    create table TaxonNameBase_Credit (
        TaxonNameBase_id integer not null,
        credits_id integer not null,
        sortIndex integer not null,
        primary key (TaxonNameBase_id, sortIndex),
        unique (credits_id)
    );

    create table TaxonNameBase_Credit_AUD (
        REV integer not null,
        TaxonNameBase_id integer not null,
        credits_id integer not null,
        sortIndex integer not null,
        revtype tinyint,
        primary key (REV, TaxonNameBase_id, credits_id, sortIndex)
    );

    create table TaxonNameBase_Extension (
        TaxonNameBase_id integer not null,
        extensions_id integer not null,
        primary key (TaxonNameBase_id, extensions_id),
        unique (extensions_id)
    );

    create table TaxonNameBase_Extension_AUD (
        REV integer not null,
        TaxonNameBase_id integer not null,
        extensions_id integer not null,
        revtype tinyint,
        primary key (REV, TaxonNameBase_id, extensions_id)
    );

    create table TaxonNameBase_Marker (
        TaxonNameBase_id integer not null,
        markers_id integer not null,
        primary key (TaxonNameBase_id, markers_id),
        unique (markers_id)
    );

    create table TaxonNameBase_Marker_AUD (
        REV integer not null,
        TaxonNameBase_id integer not null,
        markers_id integer not null,
        revtype tinyint,
        primary key (REV, TaxonNameBase_id, markers_id)
    );

    create table TaxonNameBase_NomenclaturalStatus (
        TaxonNameBase_id integer not null,
        status_id integer not null,
        primary key (TaxonNameBase_id, status_id),
        unique (status_id)
    );

    create table TaxonNameBase_NomenclaturalStatus_AUD (
        REV integer not null,
        TaxonNameBase_id integer not null,
        status_id integer not null,
        revtype tinyint,
        primary key (REV, TaxonNameBase_id, status_id)
    );

    create table TaxonNameBase_OriginalSourceBase (
        TaxonNameBase_id integer not null,
        sources_id integer not null,
        primary key (TaxonNameBase_id, sources_id),
        unique (sources_id)
    );

    create table TaxonNameBase_OriginalSourceBase_AUD (
        REV integer not null,
        TaxonNameBase_id integer not null,
        sources_id integer not null,
        revtype tinyint,
        primary key (REV, TaxonNameBase_id, sources_id)
    );

    create table TaxonNameBase_Rights (
        TaxonNameBase_id integer not null,
        rights_id integer not null,
        primary key (TaxonNameBase_id, rights_id),
        unique (rights_id)
    );

    create table TaxonNameBase_Rights_AUD (
        REV integer not null,
        TaxonNameBase_id integer not null,
        rights_id integer not null,
        revtype tinyint,
        primary key (REV, TaxonNameBase_id, rights_id)
    );

    create table TaxonNameBase_TypeDesignationBase (
        TaxonNameBase_id integer not null,
        typedesignations_id integer not null,
        primary key (TaxonNameBase_id, typedesignations_id)
    );

    create table TaxonNameBase_TypeDesignationBase_AUD (
        REV integer not null,
        TaxonNameBase_id integer not null,
        typedesignations_id integer not null,
        revtype tinyint,
        primary key (REV, TaxonNameBase_id, typedesignations_id)
    );

    create table TaxonNode (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        countchildren integer not null,
        microreferenceforparentchildrelation varchar(255),
        createdby_id integer,
        updatedby_id integer,
        parent_id integer,
        referenceforparentchildrelation_id integer,
        synonymtobeused_id integer,
        taxon_id integer,
        classification_id integer,
        primary key (id),
        unique (uuid)
    );

    create table TaxonNode_AUD (
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        countchildren integer,
        microreferenceforparentchildrelation varchar(255),
        createdby_id integer,
        updatedby_id integer,
        parent_id integer,
        referenceforparentchildrelation_id integer,
        synonymtobeused_id integer,
        taxon_id integer,
        classification_id integer,
        primary key (id, REV)
    );

    create table TaxonNode_Annotation (
        TaxonNode_id integer not null,
        annotations_id integer not null,
        primary key (TaxonNode_id, annotations_id),
        unique (annotations_id)
    );

    create table TaxonNode_Annotation_AUD (
        REV integer not null,
        TaxonNode_id integer not null,
        annotations_id integer not null,
        revtype tinyint,
        primary key (REV, TaxonNode_id, annotations_id)
    );

    create table TaxonNode_Marker (
        TaxonNode_id integer not null,
        markers_id integer not null,
        primary key (TaxonNode_id, markers_id),
        unique (markers_id)
    );

    create table TaxonNode_Marker_AUD (
        REV integer not null,
        TaxonNode_id integer not null,
        markers_id integer not null,
        revtype tinyint,
        primary key (REV, TaxonNode_id, markers_id)
    );

    create table TaxonRelationship (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        citationmicroreference varchar(255),
        originalnamestring varchar(255),
        doubtful bit not null,
        createdby_id integer,
        updatedby_id integer,
        citation_id integer,
        relatedfrom_id integer,
        relatedto_id integer,
        type_id integer,
        primary key (id),
        unique (uuid)
    );

    create table TaxonRelationship_AUD (
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        citationmicroreference varchar(255),
        originalnamestring varchar(255),
        doubtful bit,
        createdby_id integer,
        updatedby_id integer,
        citation_id integer,
        relatedfrom_id integer,
        relatedto_id integer,
        type_id integer,
        primary key (id, REV)
    );

    create table TaxonRelationship_Annotation (
        TaxonRelationship_id integer not null,
        annotations_id integer not null,
        primary key (TaxonRelationship_id, annotations_id),
        unique (annotations_id)
    );

    create table TaxonRelationship_Annotation_AUD (
        REV integer not null,
        TaxonRelationship_id integer not null,
        annotations_id integer not null,
        revtype tinyint,
        primary key (REV, TaxonRelationship_id, annotations_id)
    );

    create table TaxonRelationship_Marker (
        TaxonRelationship_id integer not null,
        markers_id integer not null,
        primary key (TaxonRelationship_id, markers_id),
        unique (markers_id)
    );

    create table TaxonRelationship_Marker_AUD (
        REV integer not null,
        TaxonRelationship_id integer not null,
        markers_id integer not null,
        revtype tinyint,
        primary key (REV, TaxonRelationship_id, markers_id)
    );

    create table TermVocabulary (
        DTYPE varchar(31) not null,
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        lsid_authority varchar(255),
        lsid_lsid varchar(255),
        lsid_namespace varchar(255),
        lsid_object varchar(255),
        lsid_revision varchar(255),
        protectedtitlecache bit not null,
        titleCache varchar(255),
        uri varchar(255),
        termsourceuri varchar(255),
        createdby_id integer,
        updatedby_id integer,
        primary key (id),
        unique (uuid)
    );

    create table TermVocabulary_AUD (
        DTYPE varchar(31) not null,
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        lsid_authority varchar(255),
        lsid_lsid varchar(255),
        lsid_namespace varchar(255),
        lsid_object varchar(255),
        lsid_revision varchar(255),
        protectedtitlecache bit,
        titleCache varchar(255),
        uri varchar(255),
        termsourceuri varchar(255),
        createdby_id integer,
        updatedby_id integer,
        primary key (id, REV)
    );

    create table TermVocabulary_Annotation (
        TermVocabulary_id integer not null,
        annotations_id integer not null,
        primary key (TermVocabulary_id, annotations_id),
        unique (annotations_id)
    );

    create table TermVocabulary_Annotation_AUD (
        REV integer not null,
        TermVocabulary_id integer not null,
        annotations_id integer not null,
        revtype tinyint,
        primary key (REV, TermVocabulary_id, annotations_id)
    );

    create table TermVocabulary_Credit (
        TermVocabulary_id integer not null,
        credits_id integer not null,
        sortIndex integer not null,
        primary key (TermVocabulary_id, sortIndex),
        unique (credits_id)
    );

    create table TermVocabulary_Credit_AUD (
        REV integer not null,
        TermVocabulary_id integer not null,
        credits_id integer not null,
        sortIndex integer not null,
        revtype tinyint,
        primary key (REV, TermVocabulary_id, credits_id, sortIndex)
    );

    create table TermVocabulary_Extension (
        TermVocabulary_id integer not null,
        extensions_id integer not null,
        primary key (TermVocabulary_id, extensions_id),
        unique (extensions_id)
    );

    create table TermVocabulary_Extension_AUD (
        REV integer not null,
        TermVocabulary_id integer not null,
        extensions_id integer not null,
        revtype tinyint,
        primary key (REV, TermVocabulary_id, extensions_id)
    );

    create table TermVocabulary_Marker (
        TermVocabulary_id integer not null,
        markers_id integer not null,
        primary key (TermVocabulary_id, markers_id),
        unique (markers_id)
    );

    create table TermVocabulary_Marker_AUD (
        REV integer not null,
        TermVocabulary_id integer not null,
        markers_id integer not null,
        revtype tinyint,
        primary key (REV, TermVocabulary_id, markers_id)
    );

    create table TermVocabulary_OriginalSourceBase (
        TermVocabulary_id integer not null,
        sources_id integer not null,
        primary key (TermVocabulary_id, sources_id),
        unique (sources_id)
    );

    create table TermVocabulary_OriginalSourceBase_AUD (
        REV integer not null,
        TermVocabulary_id integer not null,
        sources_id integer not null,
        revtype tinyint,
        primary key (REV, TermVocabulary_id, sources_id)
    );

    create table TermVocabulary_Representation (
        TermVocabulary_id integer not null,
        representations_id integer not null,
        primary key (TermVocabulary_id, representations_id),
        unique (representations_id)
    );

    create table TermVocabulary_Representation_AUD (
        REV integer not null,
        TermVocabulary_id integer not null,
        representations_id integer not null,
        revtype tinyint,
        primary key (REV, TermVocabulary_id, representations_id)
    );

    create table TermVocabulary_Rights (
        TermVocabulary_id integer not null,
        rights_id integer not null,
        primary key (TermVocabulary_id, rights_id),
        unique (rights_id)
    );

    create table TermVocabulary_Rights_AUD (
        REV integer not null,
        TermVocabulary_id integer not null,
        rights_id integer not null,
        revtype tinyint,
        primary key (REV, TermVocabulary_id, rights_id)
    );

    create table TypeDesignationBase (
        DTYPE varchar(31) not null,
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        citationmicroreference varchar(255),
        originalnamestring varchar(255),
        notdesignated bit not null,
        conservedtype bit,
        rejectedtype bit,
        createdby_id integer,
        updatedby_id integer,
        citation_id integer,
        homotypicalgroup_id integer,
        typestatus_id integer,
        typename_id integer,
        typespecimen_id integer,
        primary key (id),
        unique (uuid)
    );

    create table TypeDesignationBase_AUD (
        DTYPE varchar(31) not null,
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        citationmicroreference varchar(255),
        originalnamestring varchar(255),
        notdesignated bit,
        createdby_id integer,
        updatedby_id integer,
        citation_id integer,
        homotypicalgroup_id integer,
        typestatus_id integer,
        typespecimen_id integer,
        conservedtype bit,
        rejectedtype bit,
        typename_id integer,
        primary key (id, REV)
    );

    create table TypeDesignationBase_Annotation (
        TypeDesignationBase_id integer not null,
        annotations_id integer not null,
        primary key (TypeDesignationBase_id, annotations_id),
        unique (annotations_id)
    );

    create table TypeDesignationBase_Annotation_AUD (
        REV integer not null,
        TypeDesignationBase_id integer not null,
        annotations_id integer not null,
        revtype tinyint,
        primary key (REV, TypeDesignationBase_id, annotations_id)
    );

    create table TypeDesignationBase_Marker (
        TypeDesignationBase_id integer not null,
        markers_id integer not null,
        primary key (TypeDesignationBase_id, markers_id),
        unique (markers_id)
    );

    create table TypeDesignationBase_Marker_AUD (
        REV integer not null,
        TypeDesignationBase_id integer not null,
        markers_id integer not null,
        revtype tinyint,
        primary key (REV, TypeDesignationBase_id, markers_id)
    );

    create table TypeDesignationBase_TaxonNameBase (
        TypeDesignationBase_id integer not null,
        typifiednames_id integer not null,
        primary key (TypeDesignationBase_id, typifiednames_id)
    );

    create table TypeDesignationBase_TaxonNameBase_AUD (
        REV integer not null,
        TypeDesignationBase_id integer not null,
        typifiednames_id integer not null,
        revtype tinyint,
        primary key (REV, TypeDesignationBase_id, typifiednames_id)
    );

    create table UserAccount (
        id integer not null,
        created timestamp,
        uuid varchar(36),
        accountnonexpired bit not null,
        accountnonlocked bit not null,
        credentialsnonexpired bit not null,
        emailaddress varchar(255),
        enabled bit not null,
        password varchar(255),
        username varchar(255),
        createdby_id integer,
        person_id integer,
        primary key (id),
        unique (uuid, username)
    );

    create table UserAccount_AUD (
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        accountnonexpired bit,
        accountnonlocked bit,
        credentialsnonexpired bit,
        emailaddress varchar(255),
        enabled bit,
        username varchar(255),
        createdby_id integer,
        person_id integer,
        primary key (id, REV)
    );

    create table UserAccount_GrantedAuthorityImpl (
        UserAccount_id integer not null,
        grantedauthorities_id integer not null,
        primary key (UserAccount_id, grantedauthorities_id)
    );

    create table UserAccount_PermissionGroup (
        members_id integer not null,
        groups_id integer not null,
        primary key (members_id, groups_id)
    );

    create table WorkingSet (
        DTYPE varchar(31) not null,
        id integer not null,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        createdby_id integer,
        updatedby_id integer,
        descriptivesystem_id integer,
        primary key (id),
        unique (uuid)
    );

    create table WorkingSet_AUD (
        DTYPE varchar(31) not null,
        id integer not null,
        REV integer not null,
        revtype tinyint,
        created timestamp,
        uuid varchar(36),
        updated timestamp,
        createdby_id integer,
        updatedby_id integer,
        descriptivesystem_id integer,
        primary key (id, REV)
    );

    create table WorkingSet_Annotation (
        WorkingSet_id integer not null,
        annotations_id integer not null,
        primary key (WorkingSet_id, annotations_id),
        unique (annotations_id)
    );

    create table WorkingSet_Annotation_AUD (
        REV integer not null,
        WorkingSet_id integer not null,
        annotations_id integer not null,
        revtype tinyint,
        primary key (REV, WorkingSet_id, annotations_id)
    );

    create table WorkingSet_DescriptionBase (
        WorkingSet_id integer not null,
        descriptions_id integer not null,
        primary key (WorkingSet_id, descriptions_id)
    );

    create table WorkingSet_DescriptionBase_AUD (
        REV integer not null,
        WorkingSet_id integer not null,
        descriptions_id integer not null,
        revtype tinyint,
        primary key (REV, WorkingSet_id, descriptions_id)
    );

    create table WorkingSet_Marker (
        WorkingSet_id integer not null,
        markers_id integer not null,
        primary key (WorkingSet_id, markers_id),
        unique (markers_id)
    );

    create table WorkingSet_Marker_AUD (
        REV integer not null,
        WorkingSet_id integer not null,
        markers_id integer not null,
        revtype tinyint,
        primary key (REV, WorkingSet_id, markers_id)
    );

    create table WorkingSet_Representation (
        WorkingSet_id integer not null,
        representations_id integer not null,
        primary key (WorkingSet_id, representations_id),
        unique (representations_id)
    );

    create table WorkingSet_Representation_AUD (
        REV integer not null,
        WorkingSet_id integer not null,
        representations_id integer not null,
        revtype tinyint,
        primary key (REV, WorkingSet_id, representations_id)
    );

    create table WorkingSet_TaxonBase (
        WorkingSet_id integer not null,
        coveredtaxa_id integer not null,
        primary key (WorkingSet_id, coveredtaxa_id)
    );

    create table WorkingSet_TaxonBase_AUD (
        REV integer not null,
        WorkingSet_id integer not null,
        coveredtaxa_id integer not null,
        revtype tinyint,
        primary key (REV, WorkingSet_id, coveredtaxa_id)
    );

    alter table Address 
        add constraint FK1ED033D44FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table Address 
        add constraint FK1ED033D4132A2FE8 
        foreign key (location_referencesystem_id) 
        references DefinedTermBase;

    alter table Address 
        add constraint FK1ED033D42687715A 
        foreign key (country_id) 
        references DefinedTermBase;

    alter table Address 
        add constraint FK1ED033D4BC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table Address_AUD 
        add constraint FK115657A534869AAE 
        foreign key (REV) 
        references AuditEvent;

    create index agentTitleCacheIndex on AgentBase (titleCache);

    alter table AgentBase 
        add constraint FK1205D3564FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table AgentBase 
        add constraint FK1205D356A830578 
        foreign key (ispartof_id) 
        references AgentBase;

    alter table AgentBase 
        add constraint FK1205D356BC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table AgentBase_AUD 
        add constraint FK29CC662734869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table AgentBase_Address 
        add constraint FK1EDFF7EB86EFC5D4 
        foreign key (AgentBase_id) 
        references AgentBase;

    alter table AgentBase_Address 
        add constraint FK1EDFF7EB50751EC5 
        foreign key (contact_addresses_id) 
        references Address;

    alter table AgentBase_Address_AUD 
        add constraint FK3D28383C34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table AgentBase_AgentBase 
        add constraint FK4D34EDAD1C0E9907 
        foreign key (AgentBase_id) 
        references AgentBase;

    alter table AgentBase_AgentBase 
        add constraint FK4D34EDADE9E535F9 
        foreign key (teammembers_id) 
        references AgentBase;

    alter table AgentBase_AgentBase_AUD 
        add constraint FKA8A87CFE34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table AgentBase_Annotation 
        add constraint FK44D5F7D886EFC5D4 
        foreign key (AgentBase_id) 
        references AgentBase;

    alter table AgentBase_Annotation 
        add constraint FK44D5F7D81E403E0B 
        foreign key (annotations_id) 
        references Annotation;

    alter table AgentBase_Annotation_AUD 
        add constraint FK771279A934869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table AgentBase_Credit 
        add constraint FK2636742286EFC5D4 
        foreign key (AgentBase_id) 
        references AgentBase;

    alter table AgentBase_Credit 
        add constraint FK2636742232D1B9F 
        foreign key (credits_id) 
        references Credit;

    alter table AgentBase_Credit_AUD 
        add constraint FK7FE7C0F334869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table AgentBase_DefinedTermBase 
        add constraint FK6665C77D8D9AB196 
        foreign key (AgentBase_id) 
        references AgentBase;

    alter table AgentBase_DefinedTermBase 
        add constraint FK6665C77D9A161BED 
        foreign key (types_id) 
        references DefinedTermBase;

    alter table AgentBase_DefinedTermBase_AUD 
        add constraint FKA737EECE34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table AgentBase_Extension 
        add constraint FK8E1E567686EFC5D4 
        foreign key (AgentBase_id) 
        references AgentBase;

    alter table AgentBase_Extension 
        add constraint FK8E1E5676927DE9DF 
        foreign key (extensions_id) 
        references Extension;

    alter table AgentBase_Extension_AUD 
        add constraint FK11AE594734869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table AgentBase_Marker 
        add constraint FK365D5D63777265A1 
        foreign key (markers_id) 
        references Marker;

    alter table AgentBase_Marker 
        add constraint FK365D5D6386EFC5D4 
        foreign key (AgentBase_id) 
        references AgentBase;

    alter table AgentBase_Marker_AUD 
        add constraint FKE40621B434869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table AgentBase_Media 
        add constraint FKE8FC5D9B86EFC5D4 
        foreign key (AgentBase_id) 
        references AgentBase;

    alter table AgentBase_Media 
        add constraint FKE8FC5D9BC2C29593 
        foreign key (media_id) 
        references Media;

    alter table AgentBase_Media_AUD 
        add constraint FK323A45EC34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table AgentBase_OriginalSourceBase 
        add constraint FKB482C5E686EFC5D4 
        foreign key (AgentBase_id) 
        references AgentBase;

    alter table AgentBase_OriginalSourceBase 
        add constraint FKB482C5E63A6735D9 
        foreign key (sources_id) 
        references OriginalSourceBase;

    alter table AgentBase_OriginalSourceBase_AUD 
        add constraint FK886D90B734869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table AgentBase_Rights 
        add constraint FK3F514B0086EFC5D4 
        foreign key (AgentBase_id) 
        references AgentBase;

    alter table AgentBase_Rights 
        add constraint FK3F514B00C13F7B21 
        foreign key (rights_id) 
        references Rights;

    alter table AgentBase_Rights_AUD 
        add constraint FK4FDFF8D134869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table AgentBase_contact_emailaddresses 
        add constraint FK4BD2B08E86EFC5D4 
        foreign key (AgentBase_id) 
        references AgentBase;

    alter table AgentBase_contact_emailaddresses_AUD 
        add constraint FKCAF7E75F34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table AgentBase_contact_faxnumbers 
        add constraint FK52E1AD9586EFC5D4 
        foreign key (AgentBase_id) 
        references AgentBase;

    alter table AgentBase_contact_faxnumbers_AUD 
        add constraint FK88A308E634869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table AgentBase_contact_phonenumbers 
        add constraint FKC171CC2486EFC5D4 
        foreign key (AgentBase_id) 
        references AgentBase;

    alter table AgentBase_contact_phonenumbers_AUD 
        add constraint FKDDD347F534869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table AgentBase_contact_urls 
        add constraint FK9A9643EC86EFC5D4 
        foreign key (AgentBase_id) 
        references AgentBase;

    alter table AgentBase_contact_urls_AUD 
        add constraint FK1CE69BBD34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Annotation 
        add constraint FK1A21C74F4FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table Annotation 
        add constraint FK1A21C74FE7692740 
        foreign key (commentator_id) 
        references AgentBase;

    alter table Annotation 
        add constraint FK1A21C74FDF299D00 
        foreign key (annotationtype_id) 
        references DefinedTermBase;

    alter table Annotation 
        add constraint FK1A21C74FE8D36B00 
        foreign key (language_id) 
        references DefinedTermBase;

    alter table Annotation 
        add constraint FK1A21C74FBC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table Annotation_AUD 
        add constraint FK1A6BB5A034869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Annotation_Annotation 
        add constraint FKC99DFE3F994CCE20 
        foreign key (Annotation_id) 
        references Annotation;

    alter table Annotation_Annotation 
        add constraint FKC99DFE3F1E403E0B 
        foreign key (annotations_id) 
        references Annotation;

    alter table Annotation_Annotation_AUD 
        add constraint FKB212F49034869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Annotation_Marker 
        add constraint FKB17EAF4A777265A1 
        foreign key (markers_id) 
        references Marker;

    alter table Annotation_Marker 
        add constraint FKB17EAF4A994CCE20 
        foreign key (Annotation_id) 
        references Annotation;

    alter table Annotation_Marker_AUD 
        add constraint FK68CE281B34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table CDM_VIEW 
        add constraint FKC5DE8EF84FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table CDM_VIEW 
        add constraint FKC5DE8EF8765B124B 
        foreign key (reference_id) 
        references Reference;

    alter table CDM_VIEW_CDM_VIEW 
        add constraint FK230A885F7208BB38 
        foreign key (superviews_id) 
        references CDM_VIEW;

    alter table CDM_VIEW_CDM_VIEW 
        add constraint FK230A885FC00D1213 
        foreign key (CDM_VIEW_id) 
        references CDM_VIEW;

    create index collectionTitleCacheIndex on Collection (titleCache);

    alter table Collection 
        add constraint FKF078ABE4FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table Collection 
        add constraint FKF078ABE16B9CA77 
        foreign key (institute_id) 
        references AgentBase;

    alter table Collection 
        add constraint FKF078ABECEB38EFF 
        foreign key (supercollection_id) 
        references Collection;

    alter table Collection 
        add constraint FKF078ABEBC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table Collection_AUD 
        add constraint FKD6D4298F34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Collection_Annotation 
        add constraint FKEA970F70EB62BE9A 
        foreign key (Collection_id) 
        references Collection;

    alter table Collection_Annotation 
        add constraint FKEA970F701E403E0B 
        foreign key (annotations_id) 
        references Annotation;

    alter table Collection_Annotation_AUD 
        add constraint FKA0CE054134869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Collection_Credit 
        add constraint FKE0A317BAEB62BE9A 
        foreign key (Collection_id) 
        references Collection;

    alter table Collection_Credit 
        add constraint FKE0A317BA32D1B9F 
        foreign key (credits_id) 
        references Credit;

    alter table Collection_Credit_AUD 
        add constraint FK25A8D88B34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Collection_Extension 
        add constraint FKF68FEBDEEB62BE9A 
        foreign key (Collection_id) 
        references Collection;

    alter table Collection_Extension 
        add constraint FKF68FEBDE927DE9DF 
        foreign key (extensions_id) 
        references Extension;

    alter table Collection_Extension_AUD 
        add constraint FK1306FAAF34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Collection_Marker 
        add constraint FKF0CA00FBEB62BE9A 
        foreign key (Collection_id) 
        references Collection;

    alter table Collection_Marker 
        add constraint FKF0CA00FB777265A1 
        foreign key (markers_id) 
        references Marker;

    alter table Collection_Marker_AUD 
        add constraint FK89C7394C34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Collection_Media 
        add constraint FK7320E703EB62BE9A 
        foreign key (Collection_id) 
        references Collection;

    alter table Collection_Media 
        add constraint FK7320E703C2C29593 
        foreign key (media_id) 
        references Media;

    alter table Collection_Media_AUD 
        add constraint FK9AABDB5434869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Collection_OriginalSourceBase 
        add constraint FK37DEC57EEB62BE9A 
        foreign key (Collection_id) 
        references Collection;

    alter table Collection_OriginalSourceBase 
        add constraint FK37DEC57E3A6735D9 
        foreign key (sources_id) 
        references OriginalSourceBase;

    alter table Collection_OriginalSourceBase_AUD 
        add constraint FKF810044F34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Collection_Rights 
        add constraint FKF9BDEE98EB62BE9A 
        foreign key (Collection_id) 
        references Collection;

    alter table Collection_Rights 
        add constraint FKF9BDEE98C13F7B21 
        foreign key (rights_id) 
        references Rights;

    alter table Collection_Rights_AUD 
        add constraint FKF5A1106934869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Credit 
        add constraint FK78CA97194FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table Credit 
        add constraint FK78CA9719F7976FC5 
        foreign key (agent_id) 
        references AgentBase;

    alter table Credit 
        add constraint FK78CA9719E8D36B00 
        foreign key (language_id) 
        references DefinedTermBase;

    alter table Credit 
        add constraint FK78CA9719BC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table Credit_AUD 
        add constraint FK5533906A34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Credit_Annotation 
        add constraint FKE8DA4C354CF694E0 
        foreign key (Credit_id) 
        references Credit;

    alter table Credit_Annotation 
        add constraint FKE8DA4C351E403E0B 
        foreign key (annotations_id) 
        references Annotation;

    alter table Credit_Annotation_AUD 
        add constraint FK1DEB578634869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Credit_Marker 
        add constraint FK10CC6840777265A1 
        foreign key (markers_id) 
        references Marker;

    alter table Credit_Marker 
        add constraint FK10CC68404CF694E0 
        foreign key (Credit_id) 
        references Credit;

    alter table Credit_Marker_AUD 
        add constraint FK880A761134869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DefinedTermBase 
        add constraint FK2E340A664FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table DefinedTermBase 
        add constraint FK2E340A66D040DBF0 
        foreign key (partof_id) 
        references DefinedTermBase;

    alter table DefinedTermBase 
        add constraint FK2E340A66CC0240B6 
        foreign key (shape_id) 
        references Media;

    alter table DefinedTermBase 
        add constraint FK2E340A6647AF954C 
        foreign key (vocabulary_id) 
        references TermVocabulary;

    alter table DefinedTermBase 
        add constraint FK2E340A6624AF3F70 
        foreign key (level_id) 
        references DefinedTermBase;

    alter table DefinedTermBase 
        add constraint FK2E340A6688206484 
        foreign key (type_id) 
        references DefinedTermBase;

    alter table DefinedTermBase 
        add constraint FK2E340A6636C6F6F6 
        foreign key (pointapproximation_referencesystem_id) 
        references DefinedTermBase;

    alter table DefinedTermBase 
        add constraint FK2E340A663B0DA0EF 
        foreign key (kindof_id) 
        references DefinedTermBase;

    alter table DefinedTermBase 
        add constraint FK2E340A66BC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table DefinedTermBase_AUD 
        add constraint FK86E8953734869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DefinedTermBase_Annotation 
        add constraint FK589B6C8C0DB4934 
        foreign key (DefinedTermBase_id) 
        references DefinedTermBase;

    alter table DefinedTermBase_Annotation 
        add constraint FK589B6C81E403E0B 
        foreign key (annotations_id) 
        references Annotation;

    alter table DefinedTermBase_Annotation_AUD 
        add constraint FK28ED409934869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DefinedTermBase_Continent 
        add constraint FK45F60AFB3927C853 
        foreign key (continents_id) 
        references DefinedTermBase;

    alter table DefinedTermBase_Continent 
        add constraint FK45F60AFBE8CE10AA 
        foreign key (DefinedTermBase_id) 
        references DefinedTermBase;

    alter table DefinedTermBase_Continent_AUD 
        add constraint FKF5DE434C34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DefinedTermBase_Credit 
        add constraint FK78FF2B12C0DB4934 
        foreign key (DefinedTermBase_id) 
        references DefinedTermBase;

    alter table DefinedTermBase_Credit 
        add constraint FK78FF2B1232D1B9F 
        foreign key (credits_id) 
        references Credit;

    alter table DefinedTermBase_Credit_AUD 
        add constraint FK409B7FE334869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DefinedTermBase_Extension 
        add constraint FK397EF986927DE9DF 
        foreign key (extensions_id) 
        references Extension;

    alter table DefinedTermBase_Extension 
        add constraint FK397EF986C0DB4934 
        foreign key (DefinedTermBase_id) 
        references DefinedTermBase;

    alter table DefinedTermBase_Extension_AUD 
        add constraint FK6E6F45734869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DefinedTermBase_Marker 
        add constraint FK89261453777265A1 
        foreign key (markers_id) 
        references Marker;

    alter table DefinedTermBase_Marker 
        add constraint FK89261453C0DB4934 
        foreign key (DefinedTermBase_id) 
        references DefinedTermBase;

    alter table DefinedTermBase_Marker_AUD 
        add constraint FKA4B9E0A434869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DefinedTermBase_MeasurementUnit 
        add constraint FKE9D17767D0BDAE9B 
        foreign key (DefinedTermBase_id) 
        references DefinedTermBase;

    alter table DefinedTermBase_MeasurementUnit 
        add constraint FKE9D17767F3BB39BD 
        foreign key (recommendedmeasurementunits_id) 
        references DefinedTermBase;

    alter table DefinedTermBase_MeasurementUnit_AUD 
        add constraint FK2C1599B834869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DefinedTermBase_Media 
        add constraint FK6FC908ABC0DB4934 
        foreign key (DefinedTermBase_id) 
        references DefinedTermBase;

    alter table DefinedTermBase_Media 
        add constraint FK6FC908ABC2C29593 
        foreign key (media_id) 
        references Media;

    alter table DefinedTermBase_Media_AUD 
        add constraint FKDD9AE8FC34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DefinedTermBase_OriginalSourceBase 
        add constraint FKDCC094D6C0DB4934 
        foreign key (DefinedTermBase_id) 
        references DefinedTermBase;

    alter table DefinedTermBase_OriginalSourceBase 
        add constraint FKDCC094D63A6735D9 
        foreign key (sources_id) 
        references OriginalSourceBase;

    alter table DefinedTermBase_OriginalSourceBase_AUD 
        add constraint FKAE4A67A734869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DefinedTermBase_RecommendedModifierEnumeration 
        add constraint FKA72FB5AED0BDAE9B 
        foreign key (DefinedTermBase_id) 
        references DefinedTermBase;

    alter table DefinedTermBase_RecommendedModifierEnumeration 
        add constraint FKA72FB5AE5255EAFD 
        foreign key (recommendedmodifierenumeration_id) 
        references TermVocabulary;

    alter table DefinedTermBase_RecommendedModifierEnumeration_AUD 
        add constraint FK780D5C7F34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DefinedTermBase_Representation 
        add constraint FKAAC8AFE6B31C4747 
        foreign key (representations_id) 
        references Representation;

    alter table DefinedTermBase_Representation 
        add constraint FKAAC8AFE6C0DB4934 
        foreign key (DefinedTermBase_id) 
        references DefinedTermBase;

    alter table DefinedTermBase_Representation_AUD 
        add constraint FKB5AE7AB734869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DefinedTermBase_Rights 
        add constraint FK921A01F0C0DB4934 
        foreign key (DefinedTermBase_id) 
        references DefinedTermBase;

    alter table DefinedTermBase_Rights 
        add constraint FK921A01F0C13F7B21 
        foreign key (rights_id) 
        references Rights;

    alter table DefinedTermBase_Rights_AUD 
        add constraint FK1093B7C134869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DefinedTermBase_StatisticalMeasure 
        add constraint FK6FF15DFCD0BDAE9B 
        foreign key (DefinedTermBase_id) 
        references DefinedTermBase;

    alter table DefinedTermBase_StatisticalMeasure 
        add constraint FK6FF15DFCC9CD5B57 
        foreign key (recommendedstatisticalmeasures_id) 
        references DefinedTermBase;

    alter table DefinedTermBase_StatisticalMeasure_AUD 
        add constraint FK3C062DCD34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DefinedTermBase_SupportedCategoricalEnumeration 
        add constraint FK2170B25CD0BDAE9B 
        foreign key (DefinedTermBase_id) 
        references DefinedTermBase;

    alter table DefinedTermBase_SupportedCategoricalEnumeration 
        add constraint FK2170B25C5AF2C74 
        foreign key (supportedcategoricalenumerations_id) 
        references TermVocabulary;

    alter table DefinedTermBase_SupportedCategoricalEnumeration_AUD 
        add constraint FKBB04522D34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DefinedTermBase_WaterbodyOrCountry 
        add constraint FKCAF43931603B036 
        foreign key (waterbodiesorcountries_id) 
        references DefinedTermBase;

    alter table DefinedTermBase_WaterbodyOrCountry 
        add constraint FKCAF4393CE5C0F9E 
        foreign key (DefinedTermBase_id) 
        references DefinedTermBase;

    alter table DefinedTermBase_WaterbodyOrCountry_AUD 
        add constraint FKD5996FE434869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DerivationEvent 
        add constraint FK426BC034FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table DerivationEvent 
        add constraint FK426BC033DA462D5 
        foreign key (actor_id) 
        references AgentBase;

    alter table DerivationEvent 
        add constraint FK426BC038524B89D 
        foreign key (type_id) 
        references DefinedTermBase;

    alter table DerivationEvent 
        add constraint FK426BC03BC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table DerivationEvent_AUD 
        add constraint FKDABF305434869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DerivationEvent_Annotation 
        add constraint FKEFA0D10B4AAB411A 
        foreign key (DerivationEvent_id) 
        references DerivationEvent;

    alter table DerivationEvent_Annotation 
        add constraint FKEFA0D10B1E403E0B 
        foreign key (annotations_id) 
        references Annotation;

    alter table DerivationEvent_Annotation_AUD 
        add constraint FKA197815C34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DerivationEvent_Marker 
        add constraint FKE412C816777265A1 
        foreign key (markers_id) 
        references Marker;

    alter table DerivationEvent_Marker 
        add constraint FKE412C8164AAB411A 
        foreign key (DerivationEvent_id) 
        references DerivationEvent;

    alter table DerivationEvent_Marker_AUD 
        add constraint FK8ED0FAE734869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DescriptionBase 
        add constraint FKFF4D58CD4FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table DescriptionBase 
        add constraint FKFF4D58CDDE9A3DE3 
        foreign key (taxon_id) 
        references TaxonBase;

    alter table DescriptionBase 
        add constraint FKFF4D58CDDA93512F 
        foreign key (taxonname_id) 
        references TaxonNameBase;

    alter table DescriptionBase 
        add constraint FKFF4D58CDBC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table DescriptionBase_AUD 
        add constraint FK7456581E34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DescriptionBase_Annotation 
        add constraint FKF3AD3201F1DDBFAB 
        foreign key (DescriptionBase_id) 
        references DescriptionBase;

    alter table DescriptionBase_Annotation 
        add constraint FKF3AD32011E403E0B 
        foreign key (annotations_id) 
        references Annotation;

    alter table DescriptionBase_Annotation_AUD 
        add constraint FK15FE775234869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DescriptionBase_Credit 
        add constraint FK510B2ACBF1DDBFAB 
        foreign key (DescriptionBase_id) 
        references DescriptionBase;

    alter table DescriptionBase_Credit 
        add constraint FK510B2ACB32D1B9F 
        foreign key (credits_id) 
        references Credit;

    alter table DescriptionBase_Credit_AUD 
        add constraint FK2EBEFB1C34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DescriptionBase_Extension 
        add constraint FKD5D2B32DF1DDBFAB 
        foreign key (DescriptionBase_id) 
        references DescriptionBase;

    alter table DescriptionBase_Extension 
        add constraint FKD5D2B32D927DE9DF 
        foreign key (extensions_id) 
        references Extension;

    alter table DescriptionBase_Extension_AUD 
        add constraint FK79E7827E34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DescriptionBase_Feature 
        add constraint FK58ACF564F1DDBFAB 
        foreign key (DescriptionBase_id) 
        references DescriptionBase;

    alter table DescriptionBase_Feature 
        add constraint FK58ACF5649AE62C6 
        foreign key (descriptivesystem_id) 
        references DefinedTermBase;

    alter table DescriptionBase_Feature_AUD 
        add constraint FKA4D7D13534869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DescriptionBase_GeoScope 
        add constraint FK3ADD7CD586D04E74 
        foreign key (geoscopes_id) 
        references DefinedTermBase;

    alter table DescriptionBase_GeoScope 
        add constraint FK3ADD7CD5D86445CE 
        foreign key (DescriptionBase_id) 
        references DescriptionBase;

    alter table DescriptionBase_GeoScope_AUD 
        add constraint FK63A5382634869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DescriptionBase_Marker 
        add constraint FK6132140C777265A1 
        foreign key (markers_id) 
        references Marker;

    alter table DescriptionBase_Marker 
        add constraint FK6132140CF1DDBFAB 
        foreign key (DescriptionBase_id) 
        references DescriptionBase;

    alter table DescriptionBase_Marker_AUD 
        add constraint FK92DD5BDD34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DescriptionBase_OriginalSourceBase 
        add constraint FKDC75C70FF1DDBFAB 
        foreign key (DescriptionBase_id) 
        references DescriptionBase;

    alter table DescriptionBase_OriginalSourceBase 
        add constraint FKDC75C70F3A6735D9 
        foreign key (sources_id) 
        references OriginalSourceBase;

    alter table DescriptionBase_OriginalSourceBase_AUD 
        add constraint FK8F39D56034869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DescriptionBase_Reference 
        add constraint FKC330D639F1DDBFAB 
        foreign key (DescriptionBase_id) 
        references DescriptionBase;

    alter table DescriptionBase_Reference 
        add constraint FKC330D63945AB7BBA 
        foreign key (descriptionsources_id) 
        references Reference;

    alter table DescriptionBase_Reference_AUD 
        add constraint FK76253F8A34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DescriptionBase_Rights 
        add constraint FK6A2601A9F1DDBFAB 
        foreign key (DescriptionBase_id) 
        references DescriptionBase;

    alter table DescriptionBase_Rights 
        add constraint FK6A2601A9C13F7B21 
        foreign key (rights_id) 
        references Rights;

    alter table DescriptionBase_Rights_AUD 
        add constraint FKFEB732FA34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DescriptionBase_Scope 
        add constraint FKB9257C42951A5D40 
        foreign key (scopes_id) 
        references DefinedTermBase;

    alter table DescriptionBase_Scope 
        add constraint FKB9257C42D86445CE 
        foreign key (DescriptionBase_id) 
        references DescriptionBase;

    alter table DescriptionBase_Scope_AUD 
        add constraint FK75D5B91334869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DescriptionBase_SpecimenOrObservationBase 
        add constraint FKDE29CD8033B8A841 
        foreign key (descriptions_id) 
        references DescriptionBase;

    alter table DescriptionBase_SpecimenOrObservationBase 
        add constraint FKDE29CD805C9E3461 
        foreign key (describedspecimenorobservations_id) 
        references SpecimenOrObservationBase;

    alter table DescriptionBase_SpecimenOrObservationBase_AUD 
        add constraint FKF1B33B5134869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DescriptionElementBase 
        add constraint FK38FE76711C3C3FF7 
        foreign key (area_id) 
        references DefinedTermBase;

    alter table DescriptionElementBase 
        add constraint FK38FE76714FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table DescriptionElementBase 
        add constraint FK38FE76716D0D7A56 
        foreign key (format_id) 
        references DefinedTermBase;

    alter table DescriptionElementBase 
        add constraint FK38FE76714220AFEB 
        foreign key (feature_id) 
        references DefinedTermBase;

    alter table DescriptionElementBase 
        add constraint FK38FE76719108D9B 
        foreign key (taxon2_id) 
        references TaxonBase;

    alter table DescriptionElementBase 
        add constraint FK38FE76715E9914B8 
        foreign key (status_id) 
        references DefinedTermBase;

    alter table DescriptionElementBase 
        add constraint FK38FE767110A80E07 
        foreign key (unit_id) 
        references DefinedTermBase;

    alter table DescriptionElementBase 
        add constraint FK38FE76716561D9B1 
        foreign key (associatedspecimenorobservation_id) 
        references SpecimenOrObservationBase;

    alter table DescriptionElementBase 
        add constraint FK38FE767134AF0E81 
        foreign key (indescription_id) 
        references DescriptionBase;

    alter table DescriptionElementBase 
        add constraint FK38FE7671E8D36B00 
        foreign key (language_id) 
        references DefinedTermBase;

    alter table DescriptionElementBase 
        add constraint FK38FE7671BC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table DescriptionElementBase_AUD 
        add constraint FKF3803C234869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DescriptionElementBase_Annotation 
        add constraint FK7EE5E5DD3B8BB609 
        foreign key (DescriptionElementBase_id) 
        references DescriptionElementBase;

    alter table DescriptionElementBase_Annotation 
        add constraint FK7EE5E5DD1E403E0B 
        foreign key (annotations_id) 
        references Annotation;

    alter table DescriptionElementBase_Annotation_AUD 
        add constraint FK2BC1DD2E34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DescriptionElementBase_LanguageString 
        add constraint FKC753F137C086B46F 
        foreign key (DescriptionElementBase_id) 
        references DescriptionElementBase;

    alter table DescriptionElementBase_LanguageString 
        add constraint FKC753F137ACF5F60B 
        foreign key (multilanguagetext_id) 
        references LanguageString;

    alter table DescriptionElementBase_LanguageString 
        add constraint FKC753F137C6D55834 
        foreign key (multilanguagetext_mapkey_id) 
        references DefinedTermBase;

    alter table DescriptionElementBase_LanguageString_AUD 
        add constraint FK2D26AB8834869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DescriptionElementBase_Marker 
        add constraint FK1CB715E8777265A1 
        foreign key (markers_id) 
        references Marker;

    alter table DescriptionElementBase_Marker 
        add constraint FK1CB715E83B8BB609 
        foreign key (DescriptionElementBase_id) 
        references DescriptionElementBase;

    alter table DescriptionElementBase_Marker_AUD 
        add constraint FK1E160FB934869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DescriptionElementBase_Media 
        add constraint FK21F700763B8BB609 
        foreign key (DescriptionElementBase_id) 
        references DescriptionElementBase;

    alter table DescriptionElementBase_Media 
        add constraint FK21F70076C2C29593 
        foreign key (media_id) 
        references Media;

    alter table DescriptionElementBase_Media_AUD 
        add constraint FK5522034734869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DescriptionElementBase_Modifier 
        add constraint FK97E0D1053B8BB609 
        foreign key (DescriptionElementBase_id) 
        references DescriptionElementBase;

    alter table DescriptionElementBase_Modifier 
        add constraint FK97E0D105E0960EC4 
        foreign key (modifiers_id) 
        references DefinedTermBase;

    alter table DescriptionElementBase_Modifier_AUD 
        add constraint FK2982F45634869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DescriptionElementBase_ModifyingText 
        add constraint FK522D90C73B8BB609 
        foreign key (DescriptionElementBase_id) 
        references DescriptionElementBase;

    alter table DescriptionElementBase_ModifyingText 
        add constraint FK522D90C7F05D08D4 
        foreign key (modifyingtext_id) 
        references LanguageString;

    alter table DescriptionElementBase_ModifyingText 
        add constraint FK522D90C79682414B 
        foreign key (modifyingtext_mapkey_id) 
        references DefinedTermBase;

    alter table DescriptionElementBase_ModifyingText_AUD 
        add constraint FK6C06031834869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DescriptionElementBase_OriginalSourceBase 
        add constraint FKF41ADEEB3B8BB609 
        foreign key (DescriptionElementBase_id) 
        references DescriptionElementBase;

    alter table DescriptionElementBase_OriginalSourceBase 
        add constraint FKF41ADEEB53DD72E3 
        foreign key (sources_id) 
        references OriginalSourceBase;

    alter table DescriptionElementBase_OriginalSourceBase_AUD 
        add constraint FK9C979F3C34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DescriptionElementBase_StateData 
        add constraint FK592D6F6D15153604 
        foreign key (states_id) 
        references StateData;

    alter table DescriptionElementBase_StateData 
        add constraint FK592D6F6D987CC6A4 
        foreign key (DescriptionElementBase_id) 
        references DescriptionElementBase;

    alter table DescriptionElementBase_StateData_AUD 
        add constraint FK1D0A1EBE34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DescriptionElementBase_StatisticalMeasurementValue 
        add constraint FK8AF511C28F213219 
        foreign key (DescriptionElementBase_id) 
        references DescriptionElementBase;

    alter table DescriptionElementBase_StatisticalMeasurementValue 
        add constraint FK8AF511C2D883945E 
        foreign key (statisticalvalues_id) 
        references StatisticalMeasurementValue;

    alter table DescriptionElementBase_StatisticalMeasurementValue_AUD 
        add constraint FK2DE8E9334869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DeterminationEvent 
        add constraint FK1DB24974FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table DeterminationEvent 
        add constraint FK1DB24973DA462D5 
        foreign key (actor_id) 
        references AgentBase;

    alter table DeterminationEvent 
        add constraint FK1DB2497DE9A3E39 
        foreign key (taxon_id) 
        references TaxonBase;

    alter table DeterminationEvent 
        add constraint FK1DB24974B251DAD 
        foreign key (identifiedunit_id) 
        references SpecimenOrObservationBase;

    alter table DeterminationEvent 
        add constraint FK1DB2497378D1BD 
        foreign key (modifier_id) 
        references DefinedTermBase;

    alter table DeterminationEvent 
        add constraint FK1DB2497BC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table DeterminationEvent_AUD 
        add constraint FKA0252EE834869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DeterminationEvent_Annotation 
        add constraint FKB74F03F76BE0BFDA 
        foreign key (DeterminationEvent_id) 
        references DeterminationEvent;

    alter table DeterminationEvent_Annotation 
        add constraint FKB74F03F71E403E0B 
        foreign key (annotations_id) 
        references Annotation;

    alter table DeterminationEvent_Annotation_AUD 
        add constraint FKAFDA5E4834869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DeterminationEvent_Marker 
        add constraint FK5C475102777265A1 
        foreign key (markers_id) 
        references Marker;

    alter table DeterminationEvent_Marker 
        add constraint FK5C4751026BE0BFDA 
        foreign key (DeterminationEvent_id) 
        references DeterminationEvent;

    alter table DeterminationEvent_Marker_AUD 
        add constraint FK567F2DD334869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table DeterminationEvent_Reference 
        add constraint FK8FB1ED833EF09CD5 
        foreign key (setofreferences_id) 
        references Reference;

    alter table DeterminationEvent_Reference 
        add constraint FK8FB1ED836BE0BFDA 
        foreign key (DeterminationEvent_id) 
        references DeterminationEvent;

    alter table DeterminationEvent_Reference_AUD 
        add constraint FK6255A1D434869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Extension 
        add constraint FK52EF3C1F4FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table Extension 
        add constraint FK52EF3C1FAD392BD3 
        foreign key (type_id) 
        references DefinedTermBase;

    alter table Extension 
        add constraint FK52EF3C1FBC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table Extension_AUD 
        add constraint FK92D2427034869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table FeatureNode 
        add constraint FK4CEED9F84FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table FeatureNode 
        add constraint FK4CEED9F8E0AD2C03 
        foreign key (parent_id) 
        references FeatureNode;

    alter table FeatureNode 
        add constraint FK4CEED9F8DE9A3E39 
        foreign key (featureTree_id) 
        references FeatureTree;

    alter table FeatureNode 
        add constraint FK4CEED9F84220AFEB 
        foreign key (feature_id) 
        references DefinedTermBase;

    alter table FeatureNode 
        add constraint FK4CEED9F8BC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table FeatureNode_AUD 
        add constraint FK25AD4BC934869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table FeatureNode_DefinedTermBase_InapplicableIf 
        add constraint FK56833D011128E63B 
        foreign key (inapplicableif_id) 
        references DefinedTermBase;

    alter table FeatureNode_DefinedTermBase_InapplicableIf 
        add constraint FK56833D0152FCC4B 
        foreign key (FeatureNode_id) 
        references FeatureNode;

    alter table FeatureNode_DefinedTermBase_InapplicableIf_AUD 
        add constraint FKB8D7025234869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table FeatureNode_DefinedTermBase_OnlyApplicable 
        add constraint FK6AE876AB57FA94D4 
        foreign key (onlyapplicableif_id) 
        references DefinedTermBase;

    alter table FeatureNode_DefinedTermBase_OnlyApplicable 
        add constraint FK6AE876AB52FCC4B 
        foreign key (FeatureNode_id) 
        references FeatureNode;

    alter table FeatureNode_DefinedTermBase_OnlyApplicable_AUD 
        add constraint FK3F5356FC34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table FeatureTree 
        add constraint FK4CF19F944FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table FeatureTree 
        add constraint FK4CF19F94B7892921 
        foreign key (root_id) 
        references FeatureNode;

    alter table FeatureTree 
        add constraint FK4CF19F94BC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table FeatureTree_AUD 
        add constraint FK355BE36534869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table FeatureTree_Annotation 
        add constraint FK5D8B8DA47C496CB 
        foreign key (FeatureTree_id) 
        references FeatureTree;

    alter table FeatureTree_Annotation 
        add constraint FK5D8B8DA1E403E0B 
        foreign key (annotations_id) 
        references Annotation;

    alter table FeatureTree_Annotation_AUD 
        add constraint FK86E8E9AB34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table FeatureTree_Credit 
        add constraint FK7536062432D1B9F 
        foreign key (credits_id) 
        references Credit;

    alter table FeatureTree_Credit 
        add constraint FK7536062447C496CB 
        foreign key (FeatureTree_id) 
        references FeatureTree;

    alter table FeatureTree_Credit_AUD 
        add constraint FK40EA81F534869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table FeatureTree_Extension 
        add constraint FKAD1E6D34927DE9DF 
        foreign key (extensions_id) 
        references Extension;

    alter table FeatureTree_Extension 
        add constraint FKAD1E6D3447C496CB 
        foreign key (FeatureTree_id) 
        references FeatureTree;

    alter table FeatureTree_Extension_AUD 
        add constraint FKF128E10534869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table FeatureTree_Marker 
        add constraint FK855CEF65777265A1 
        foreign key (markers_id) 
        references Marker;

    alter table FeatureTree_Marker 
        add constraint FK855CEF6547C496CB 
        foreign key (FeatureTree_id) 
        references FeatureTree;

    alter table FeatureTree_Marker_AUD 
        add constraint FKA508E2B634869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table FeatureTree_OriginalSourceBase 
        add constraint FK13BD64E847C496CB 
        foreign key (FeatureTree_id) 
        references FeatureTree;

    alter table FeatureTree_OriginalSourceBase 
        add constraint FK13BD64E83A6735D9 
        foreign key (sources_id) 
        references OriginalSourceBase;

    alter table FeatureTree_OriginalSourceBase_AUD 
        add constraint FK7B5CDEB934869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table FeatureTree_Representation 
        add constraint FK8C458F8B31C4747 
        foreign key (representations_id) 
        references Representation;

    alter table FeatureTree_Representation 
        add constraint FK8C458F847C496CB 
        foreign key (FeatureTree_id) 
        references FeatureTree;

    alter table FeatureTree_Representation_AUD 
        add constraint FKECAB4AC934869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table FeatureTree_Rights 
        add constraint FK8E50DD0247C496CB 
        foreign key (FeatureTree_id) 
        references FeatureTree;

    alter table FeatureTree_Rights 
        add constraint FK8E50DD02C13F7B21 
        foreign key (rights_id) 
        references Rights;

    alter table FeatureTree_Rights_AUD 
        add constraint FK10E2B9D334869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table FeatureTree_TaxonBase 
        add constraint FKEC78E5B0ED57882F 
        foreign key (FeatureTree_id) 
        references FeatureTree;

    alter table FeatureTree_TaxonBase 
        add constraint FKEC78E5B07C3D0017 
        foreign key (coveredtaxa_id) 
        references TaxonBase;

    alter table FeatureTree_TaxonBase_AUD 
        add constraint FK955ABB8134869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table GatheringEvent 
        add constraint FK6F1286F38B455EC6 
        foreign key (locality_id) 
        references LanguageString;

    alter table GatheringEvent 
        add constraint FK6F1286F34FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table GatheringEvent 
        add constraint FK6F1286F33DA462D5 
        foreign key (actor_id) 
        references AgentBase;

    alter table GatheringEvent 
        add constraint FK6F1286F3F55AFD89 
        foreign key (exactlocation_referencesystem_id) 
        references DefinedTermBase;

    alter table GatheringEvent 
        add constraint FK6F1286F3BC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table GatheringEvent_AUD 
        add constraint FK3EC034434869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table GatheringEvent_Annotation 
        add constraint FK76DDD01BF95F225A 
        foreign key (GatheringEvent_id) 
        references GatheringEvent;

    alter table GatheringEvent_Annotation 
        add constraint FK76DDD01B1E403E0B 
        foreign key (annotations_id) 
        references Annotation;

    alter table GatheringEvent_Annotation_AUD 
        add constraint FK351E786C34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table GatheringEvent_DefinedTermBase 
        add constraint FK69D9A11A7C34B6D6 
        foreign key (collectingareas_id) 
        references DefinedTermBase;

    alter table GatheringEvent_DefinedTermBase 
        add constraint FK69D9A11AF95F225A 
        foreign key (GatheringEvent_id) 
        references GatheringEvent;

    alter table GatheringEvent_DefinedTermBase_AUD 
        add constraint FKB3BBB1EB34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table GatheringEvent_Marker 
        add constraint FK7B49CF26777265A1 
        foreign key (markers_id) 
        references Marker;

    alter table GatheringEvent_Marker 
        add constraint FK7B49CF26F95F225A 
        foreign key (GatheringEvent_id) 
        references GatheringEvent;

    alter table GatheringEvent_Marker_AUD 
        add constraint FK160DF9F734869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table GenBankAccession 
        add constraint FK86C1DBF84FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table GenBankAccession 
        add constraint FK86C1DBF8BC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table GenBankAccession_AUD 
        add constraint FK5A2F4DC934869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table GrantedAuthorityImpl 
        add constraint FKB05CF9284FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table HomotypicalGroup 
        add constraint FK7DECCC184FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table HomotypicalGroup 
        add constraint FK7DECCC18BC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table HomotypicalGroup_AUD 
        add constraint FKE4252DE934869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table HomotypicalGroup_Annotation 
        add constraint FK7A0351D6BFEAE500 
        foreign key (HomotypicalGroup_id) 
        references HomotypicalGroup;

    alter table HomotypicalGroup_Annotation 
        add constraint FK7A0351D61E403E0B 
        foreign key (annotations_id) 
        references Annotation;

    alter table HomotypicalGroup_Annotation_AUD 
        add constraint FK41E6A4A734869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table HomotypicalGroup_Marker 
        add constraint FK97D36661777265A1 
        foreign key (markers_id) 
        references Marker;

    alter table HomotypicalGroup_Marker 
        add constraint FK97D36661BFEAE500 
        foreign key (HomotypicalGroup_id) 
        references HomotypicalGroup;

    alter table HomotypicalGroup_Marker_AUD 
        add constraint FK19337BB234869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table HybridRelationship 
        add constraint FK9033CE744FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table HybridRelationship 
        add constraint FK9033CE749803512F 
        foreign key (citation_id) 
        references Reference;

    alter table HybridRelationship 
        add constraint FK9033CE749DD57A93 
        foreign key (relatedfrom_id) 
        references TaxonNameBase;

    alter table HybridRelationship 
        add constraint FK9033CE7455F241D4 
        foreign key (type_id) 
        references DefinedTermBase;

    alter table HybridRelationship 
        add constraint FK9033CE74AF4F9F62 
        foreign key (relatedto_id) 
        references TaxonNameBase;

    alter table HybridRelationship 
        add constraint FK9033CE74BC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table HybridRelationship_AUD 
        add constraint FK9C2BA24534869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table HybridRelationship_Annotation 
        add constraint FK2C7E7DFA59832240 
        foreign key (HybridRelationship_id) 
        references HybridRelationship;

    alter table HybridRelationship_Annotation 
        add constraint FK2C7E7DFA1E403E0B 
        foreign key (annotations_id) 
        references Annotation;

    alter table HybridRelationship_Annotation_AUD 
        add constraint FKACE71ECB34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table HybridRelationship_Marker 
        add constraint FKCEF24485777265A1 
        foreign key (markers_id) 
        references Marker;

    alter table HybridRelationship_Marker 
        add constraint FKCEF2448559832240 
        foreign key (HybridRelationship_id) 
        references HybridRelationship;

    alter table HybridRelationship_Marker_AUD 
        add constraint FKCBAEA7D634869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table IndividualAssociation_LanguageString 
        add constraint FKB5C75EC028459272 
        foreign key (description_mapkey_id) 
        references DefinedTermBase;

    alter table IndividualAssociation_LanguageString 
        add constraint FKB5C75EC084FF3EDF 
        foreign key (DescriptionElementBase_id) 
        references DescriptionElementBase;

    alter table IndividualAssociation_LanguageString 
        add constraint FKB5C75EC02BEBA58D 
        foreign key (description_id) 
        references LanguageString;

    alter table IndividualAssociation_LanguageString_AUD 
        add constraint FKB1A62C9134869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table InstitutionalMembership 
        add constraint FK3C8E1FF94FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table InstitutionalMembership 
        add constraint FK3C8E1FF9AAC1B820 
        foreign key (person_id) 
        references AgentBase;

    alter table InstitutionalMembership 
        add constraint FK3C8E1FF916B9CA77 
        foreign key (institute_id) 
        references AgentBase;

    alter table InstitutionalMembership 
        add constraint FK3C8E1FF9BC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table InstitutionalMembership_AUD 
        add constraint FK847A94A34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table LSIDAuthority 
        add constraint FK759DB8814FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table LSIDAuthority_namespaces 
        add constraint FKB04948F64FFCFD94 
        foreign key (LSIDAuthority_id) 
        references LSIDAuthority;

    alter table LanguageString 
        add constraint FKB5FDC9A94FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table LanguageString 
        add constraint FKB5FDC9A9E8D36B00 
        foreign key (language_id) 
        references DefinedTermBase;

    alter table LanguageString 
        add constraint FKB5FDC9A9BC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table LanguageString_AUD 
        add constraint FK896AFAFA34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table LanguageString_Annotation 
        add constraint FK8400DFA537998500 
        foreign key (LanguageString_id) 
        references LanguageString;

    alter table LanguageString_Annotation 
        add constraint FK8400DFA51E403E0B 
        foreign key (annotations_id) 
        references Annotation;

    alter table LanguageString_Annotation_AUD 
        add constraint FKD3BAB2F634869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table LanguageString_Marker 
        add constraint FK8DA633B0777265A1 
        foreign key (markers_id) 
        references Marker;

    alter table LanguageString_Marker 
        add constraint FK8DA633B037998500 
        foreign key (LanguageString_id) 
        references LanguageString;

    alter table LanguageString_Marker_AUD 
        add constraint FK2331098134869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Locus 
        add constraint FK462F1BE4FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table Locus 
        add constraint FK462F1BEBC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table Locus_AUD 
        add constraint FK5224108F34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Marker 
        add constraint FK88F1805A4FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table Marker 
        add constraint FK88F1805AD64DC020 
        foreign key (markertype_id) 
        references DefinedTermBase;

    alter table Marker 
        add constraint FK88F1805ABC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table Marker_AUD 
        add constraint FKB951F12B34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Media 
        add constraint FK46C7FC44FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table Media 
        add constraint FK46C7FC49803512F 
        foreign key (citation_id) 
        references Reference;

    alter table Media 
        add constraint FK46C7FC4C2445443 
        foreign key (artist_id) 
        references AgentBase;

    alter table Media 
        add constraint FK46C7FC4BC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table MediaKey_NamedArea 
        add constraint FK31E7D4023FF8E7B2 
        foreign key (geographicalscope_id) 
        references DefinedTermBase;

    alter table MediaKey_NamedArea 
        add constraint FK31E7D402BE59D760 
        foreign key (Media_id) 
        references Media;

    alter table MediaKey_NamedArea_AUD 
        add constraint FK922630D334869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table MediaKey_Scope 
        add constraint FKBFFEE8F0BE59D760 
        foreign key (Media_id) 
        references Media;

    alter table MediaKey_Scope 
        add constraint FKBFFEE8F0546985E4 
        foreign key (scoperestrictions_id) 
        references DefinedTermBase;

    alter table MediaKey_Scope_AUD 
        add constraint FK63AD1EC134869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table MediaKey_Taxon 
        add constraint FKC00C3966815C793 
        foreign key (mediaKey_id) 
        references Media;

    alter table MediaKey_Taxon 
        add constraint FKC00C3966DE9A3DE3 
        foreign key (taxon_id) 
        references TaxonBase;

    alter table MediaKey_Taxon_AUD 
        add constraint FK311443734869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table MediaRepresentation 
        add constraint FK1966BDB14FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table MediaRepresentation 
        add constraint FK1966BDB1C2C29593 
        foreign key (media_id) 
        references Media;

    alter table MediaRepresentation 
        add constraint FK1966BDB1BC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table MediaRepresentationPart 
        add constraint FK67A455444FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table MediaRepresentationPart 
        add constraint FK67A45544E3818E37 
        foreign key (representation_id) 
        references MediaRepresentation;

    alter table MediaRepresentationPart 
        add constraint FK67A45544BC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table MediaRepresentationPart_AUD 
        add constraint FKA75C411534869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table MediaRepresentation_AUD 
        add constraint FK67AAAB0234869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table MediaRepresentation_MediaRepresentationPart_AUD 
        add constraint FK3544378734869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Media_AUD 
        add constraint FKF70B2B9534869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Media_Annotation 
        add constraint FKA020DAAAC2C29593 
        foreign key (Media_id) 
        references Media;

    alter table Media_Annotation 
        add constraint FKA020DAAA1E403E0B 
        foreign key (annotations_id) 
        references Annotation;

    alter table Media_Annotation_AUD 
        add constraint FK99ABA37B34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Media_Credit 
        add constraint FKC1F78FF432D1B9F 
        foreign key (credits_id) 
        references Credit;

    alter table Media_Credit 
        add constraint FKC1F78FF4C2C29593 
        foreign key (Media_id) 
        references Media;

    alter table Media_Credit_AUD 
        add constraint FKDB32A3C534869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Media_Description 
        add constraint FK368283E128459272 
        foreign key (description_mapkey_id) 
        references DefinedTermBase;

    alter table Media_Description 
        add constraint FK368283E12BEBA58D 
        foreign key (description_id) 
        references LanguageString;

    alter table Media_Description 
        add constraint FK368283E1C2C29593 
        foreign key (Media_id) 
        references Media;

    alter table Media_Description_AUD 
        add constraint FK6817D93234869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Media_Extension 
        add constraint FKDB62D164927DE9DF 
        foreign key (extensions_id) 
        references Extension;

    alter table Media_Extension 
        add constraint FKDB62D164C2C29593 
        foreign key (Media_id) 
        references Media;

    alter table Media_Extension_AUD 
        add constraint FKE13FAD3534869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Media_LanguageString 
        add constraint FK353DB784A0A6EDCE 
        foreign key (title_mapkey_id) 
        references DefinedTermBase;

    alter table Media_LanguageString 
        add constraint FK353DB784C2C29593 
        foreign key (Media_id) 
        references Media;

    alter table Media_LanguageString 
        add constraint FK353DB784A1CA19B1 
        foreign key (title_id) 
        references LanguageString;

    alter table Media_LanguageString_AUD 
        add constraint FK68FA835534869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Media_Marker 
        add constraint FKD21E7935777265A1 
        foreign key (markers_id) 
        references Marker;

    alter table Media_Marker 
        add constraint FKD21E7935C2C29593 
        foreign key (Media_id) 
        references Media;

    alter table Media_Marker_AUD 
        add constraint FK3F51048634869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Media_OriginalSourceBase 
        add constraint FK2FEEB6B8C2C29593 
        foreign key (Media_id) 
        references Media;

    alter table Media_OriginalSourceBase 
        add constraint FK2FEEB6B83A6735D9 
        foreign key (sources_id) 
        references OriginalSourceBase;

    alter table Media_OriginalSourceBase_AUD 
        add constraint FK97F0C88934869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Media_Representation 
        add constraint FK1B8712C8BE59D760 
        foreign key (Media_id) 
        references Media;

    alter table Media_Representation 
        add constraint FK1B8712C88F6CABE6 
        foreign key (keyrepresentations_id) 
        references Representation;

    alter table Media_Representation_AUD 
        add constraint FK8DC9C9934869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Media_Rights 
        add constraint FKDB1266D2C13F7B21 
        foreign key (rights_id) 
        references Rights;

    alter table Media_Rights 
        add constraint FKDB1266D2C2C29593 
        foreign key (Media_id) 
        references Media;

    alter table Media_Rights_AUD 
        add constraint FKAB2ADBA334869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Media_Sequence 
        add constraint FK61D09FCF29B4761 
        foreign key (usedsequences_id) 
        references Sequence;

    alter table Media_Sequence 
        add constraint FK61D09FC3282B64 
        foreign key (Media_id) 
        references Media;

    alter table Media_Sequence_AUD 
        add constraint FK3C7BD9CD34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Media_TaxonBase 
        add constraint FK1ABD49E0BE59D760 
        foreign key (Media_id) 
        references Media;

    alter table Media_TaxonBase 
        add constraint FK1ABD49E07C3D0017 
        foreign key (coveredtaxa_id) 
        references TaxonBase;

    alter table Media_TaxonBase_AUD 
        add constraint FK857187B134869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table MultiAccessKey_NamedArea 
        add constraint FK1F5A74893FF8E7B2 
        foreign key (geographicalscope_id) 
        references DefinedTermBase;

    alter table MultiAccessKey_NamedArea 
        add constraint FK1F5A7489B4555A9A 
        foreign key (WorkingSet_id) 
        references WorkingSet;

    alter table MultiAccessKey_NamedArea_AUD 
        add constraint FK4CB735DA34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table MultiAccessKey_Scope 
        add constraint FKCC6CE4F7546985E4 
        foreign key (scoperestrictions_id) 
        references DefinedTermBase;

    alter table MultiAccessKey_Scope 
        add constraint FKCC6CE4F7B4555A9A 
        foreign key (WorkingSet_id) 
        references WorkingSet;

    alter table MultiAccessKey_Scope_AUD 
        add constraint FK511FBF4834869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table MultiAccessKey_Taxon 
        add constraint FKCC7A356DB64A7AD3 
        foreign key (multiAccessKey_id) 
        references WorkingSet;

    alter table MultiAccessKey_Taxon 
        add constraint FKCC7A356DDE9A3DE3 
        foreign key (taxon_id) 
        references TaxonBase;

    alter table MultiAccessKey_Taxon_AUD 
        add constraint FKF083E4BE34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table NameRelationship 
        add constraint FK5E510834FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table NameRelationship 
        add constraint FK5E510839803512F 
        foreign key (citation_id) 
        references Reference;

    alter table NameRelationship 
        add constraint FK5E5108316CDFF85 
        foreign key (relatedfrom_id) 
        references TaxonNameBase;

    alter table NameRelationship 
        add constraint FK5E51083AF619DE3 
        foreign key (type_id) 
        references DefinedTermBase;

    alter table NameRelationship 
        add constraint FK5E5108328482454 
        foreign key (relatedto_id) 
        references TaxonNameBase;

    alter table NameRelationship 
        add constraint FK5E51083BC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table NameRelationship_AUD 
        add constraint FK743F44D434869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table NameRelationship_Annotation 
        add constraint FK2E38AC8B7B4CB560 
        foreign key (NameRelationship_id) 
        references NameRelationship;

    alter table NameRelationship_Annotation 
        add constraint FK2E38AC8B1E403E0B 
        foreign key (annotations_id) 
        references Annotation;

    alter table NameRelationship_Annotation_AUD 
        add constraint FKD1D59CDC34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table NameRelationship_Marker 
        add constraint FKE3E46396777265A1 
        foreign key (markers_id) 
        references Marker;

    alter table NameRelationship_Marker 
        add constraint FKE3E463967B4CB560 
        foreign key (NameRelationship_id) 
        references NameRelationship;

    alter table NameRelationship_Marker_AUD 
        add constraint FKCD68D66734869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table NomenclaturalStatus 
        add constraint FK1FFEC88B4FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table NomenclaturalStatus 
        add constraint FK1FFEC88B9803512F 
        foreign key (citation_id) 
        references Reference;

    alter table NomenclaturalStatus 
        add constraint FK1FFEC88B7029BD9F 
        foreign key (type_id) 
        references DefinedTermBase;

    alter table NomenclaturalStatus 
        add constraint FK1FFEC88BBC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table NomenclaturalStatus_AUD 
        add constraint FKFB2DB8DC34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table NomenclaturalStatus_Annotation 
        add constraint FKE6E91F838D2CB1D4 
        foreign key (NomenclaturalStatus_id) 
        references NomenclaturalStatus;

    alter table NomenclaturalStatus_Annotation 
        add constraint FKE6E91F831E403E0B 
        foreign key (annotations_id) 
        references Annotation;

    alter table NomenclaturalStatus_Annotation_AUD 
        add constraint FK6A3D3D434869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table NomenclaturalStatus_Marker 
        add constraint FK2F5128E777265A1 
        foreign key (markers_id) 
        references Marker;

    alter table NomenclaturalStatus_Marker 
        add constraint FK2F5128E8D2CB1D4 
        foreign key (NomenclaturalStatus_id) 
        references NomenclaturalStatus;

    alter table NomenclaturalStatus_Marker_AUD 
        add constraint FK8619495F34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table OriginalSourceBase 
        add constraint FK505F2E5D4FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table OriginalSourceBase 
        add constraint FK505F2E5D966B96B2 
        foreign key (nameusedinsource_id) 
        references TaxonNameBase;

    alter table OriginalSourceBase 
        add constraint FK505F2E5D9803512F 
        foreign key (citation_id) 
        references Reference;

    alter table OriginalSourceBase 
        add constraint FK505F2E5DBC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table OriginalSourceBase_AUD 
        add constraint FK9662E5AE34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table OriginalSourceBase_Annotation 
        add constraint FK20814271B029DDA0 
        foreign key (OriginalSourceBase_id) 
        references OriginalSourceBase;

    alter table OriginalSourceBase_Annotation 
        add constraint FK208142711E403E0B 
        foreign key (annotations_id) 
        references Annotation;

    alter table OriginalSourceBase_Annotation_AUD 
        add constraint FKA074CFC234869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table OriginalSourceBase_Marker 
        add constraint FKB3FFDC7C777265A1 
        foreign key (markers_id) 
        references Marker;

    alter table OriginalSourceBase_Marker 
        add constraint FKB3FFDC7CB029DDA0 
        foreign key (OriginalSourceBase_id) 
        references OriginalSourceBase;

    alter table OriginalSourceBase_Marker_AUD 
        add constraint FKBFB16C4D34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table PermissionGroup 
        add constraint FK629941D04FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table PermissionGroup_GrantedAuthorityImpl 
        add constraint FK5311437CA0971A3 
        foreign key (PermissionGroup_id) 
        references PermissionGroup;

    alter table PermissionGroup_GrantedAuthorityImpl 
        add constraint FK53114371857F6C2 
        foreign key (grantedauthorities_id) 
        references GrantedAuthorityImpl;

    alter table PolytomousKeyNode_LanguageString 
        add constraint FK5574E12EF05D08D4 
        foreign key (modifyingtext_id) 
        references LanguageString;

    alter table PolytomousKeyNode_LanguageString 
        add constraint FK5574E12EF135C42B 
        foreign key (PolytomousKeyNode_id) 
        references PolytomousKeyNode;

    alter table PolytomousKeyNode_LanguageString 
        add constraint FK5574E12E9682414B 
        foreign key (modifyingtext_mapkey_id) 
        references DefinedTermBase;


    alter table PolytomousKey_NamedArea 
        add constraint FK1C727CFF3FF8E7B2 
        foreign key (geographicalscope_id) 
        references DefinedTermBase;

    alter table PolytomousKey_NamedArea 
        add constraint FK1C727CFFED57882F 
        foreign key (PolytomousKey_id) 
        references PolytomousKey;

    alter table PolytomousKey_NamedArea_AUD 
        add constraint FK750A135034869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table PolytomousKey_Scope 
        add constraint FK8D97986DED57882F 
        foreign key (PolytomousKey_id) 
        references PolytomousKey;

    alter table PolytomousKey_Scope 
        add constraint FK8D97986D546985E4 
        foreign key (scoperestrictions_id) 
        references DefinedTermBase;

    alter table PolytomousKey_Scope_AUD 
        add constraint FK4E37C7BE34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table PolytomousKey_Taxon 
        add constraint FK8DA4E8E389D9775 
        foreign key (polytomousKey_id) 
        references PolytomousKey;

    alter table PolytomousKey_Taxon 
        add constraint FK8DA4E8E3DE9A3DE3 
        foreign key (taxon_id) 
        references TaxonBase;

    alter table PolytomousKey_Taxon_AUD 
        add constraint FKED9BED3434869AAE 
        foreign key (REV) 
        references AuditEvent;

    create index ReferenceTitleCacheIndex on Reference (titleCache);

    alter table Reference 
        add constraint FK404D5F2B4FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table Reference 
        add constraint FK404D5F2B403E17F4 
        foreign key (institution_id) 
        references AgentBase;

    alter table Reference 
        add constraint FK404D5F2B969F8FF0 
        foreign key (inreference_id) 
        references Reference;

    alter table Reference 
        add constraint FK404D5F2BAEC3B8B8 
        foreign key (school_id) 
        references AgentBase;

    alter table Reference 
        add constraint FK404D5F2B697665E 
        foreign key (authorteam_id) 
        references AgentBase;

    alter table Reference 
        add constraint FK404D5F2BBC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table Reference_AUD 
        add constraint FK8F0FFF7C34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Reference_Annotation 
        add constraint FKFC824E3765B124B 
        foreign key (Reference_id) 
        references Reference;

    alter table Reference_Annotation 
        add constraint FKFC824E31E403E0B 
        foreign key (annotations_id) 
        references Annotation;

    alter table Reference_Annotation_AUD 
        add constraint FKF3C1293434869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Reference_Credit 
        add constraint FK5BC6DEAD32D1B9F 
        foreign key (credits_id) 
        references Credit;

    alter table Reference_Credit 
        add constraint FK5BC6DEAD765B124B 
        foreign key (Reference_id) 
        references Reference;

    alter table Reference_Credit_AUD 
        add constraint FK4AD9EDFE34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Reference_Extension 
        add constraint FKDEFCDC0B927DE9DF 
        foreign key (extensions_id) 
        references Extension;

    alter table Reference_Extension 
        add constraint FKDEFCDC0B765B124B 
        foreign key (Reference_id) 
        references Reference;

    alter table Reference_Extension_AUD 
        add constraint FK1DF60C5C34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Reference_Marker 
        add constraint FK6BEDC7EE777265A1 
        foreign key (markers_id) 
        references Marker;

    alter table Reference_Marker 
        add constraint FK6BEDC7EE765B124B 
        foreign key (Reference_id) 
        references Reference;

    alter table Reference_Marker_AUD 
        add constraint FKAEF84EBF34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Reference_Media 
        add constraint FKBBEF5B0765B124B 
        foreign key (Reference_id) 
        references Reference;

    alter table Reference_Media 
        add constraint FKBBEF5B0C2C29593 
        foreign key (media_id) 
        references Media;

    alter table Reference_Media_AUD 
        add constraint FK8318CB8134869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Reference_OriginalSourceBase 
        add constraint FKD3E8B7F1765B124B 
        foreign key (Reference_id) 
        references Reference;

    alter table Reference_OriginalSourceBase 
        add constraint FKD3E8B7F13A6735D9 
        foreign key (sources_id) 
        references OriginalSourceBase;

    alter table Reference_OriginalSourceBase_AUD 
        add constraint FKC025854234869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Reference_Rights 
        add constraint FK74E1B58BC13F7B21 
        foreign key (rights_id) 
        references Rights;

    alter table Reference_Rights 
        add constraint FK74E1B58B765B124B 
        foreign key (Reference_id) 
        references Reference;

    alter table Reference_Rights_AUD 
        add constraint FK1AD225DC34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table RelationshipTermBase_inverseRepresentation 
        add constraint FK98592F33ECEEF4AF 
        foreign key (DefinedTermBase_id) 
        references DefinedTermBase;

    alter table RelationshipTermBase_inverseRepresentation 
        add constraint FK98592F33473FB677 
        foreign key (inverserepresentations_id) 
        references Representation;

    alter table RelationshipTermBase_inverseRepresentation_AUD 
        add constraint FK5D248B8434869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Representation 
        add constraint FK9C4724ED4FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table Representation 
        add constraint FK9C4724EDE8D36B00 
        foreign key (language_id) 
        references DefinedTermBase;

    alter table Representation 
        add constraint FK9C4724EDBC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table Representation_AUD 
        add constraint FK294D143E34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Representation_Annotation 
        add constraint FK371091E147E8AE60 
        foreign key (Representation_id) 
        references Representation;

    alter table Representation_Annotation 
        add constraint FK371091E11E403E0B 
        foreign key (annotations_id) 
        references Annotation;

    alter table Representation_Annotation_AUD 
        add constraint FK36EEE73234869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Representation_Marker 
        add constraint FK560063EC777265A1 
        foreign key (markers_id) 
        references Marker;

    alter table Representation_Marker 
        add constraint FK560063EC47E8AE60 
        foreign key (Representation_id) 
        references Representation;

    alter table Representation_Marker_AUD 
        add constraint FKD640BBBD34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Rights 
        add constraint FK91E56DF74FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table Rights 
        add constraint FK91E56DF7F7976FC5 
        foreign key (agent_id) 
        references AgentBase;

    alter table Rights 
        add constraint FK91E56DF7E6D2886A 
        foreign key (type_id) 
        references DefinedTermBase;

    alter table Rights 
        add constraint FK91E56DF7E8D36B00 
        foreign key (language_id) 
        references DefinedTermBase;

    alter table Rights 
        add constraint FK91E56DF7BC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table Rights_AUD 
        add constraint FK252BC84834869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Rights_Annotation 
        add constraint FK27CB1E97C13F7B21 
        foreign key (Rights_id) 
        references Rights;

    alter table Rights_Annotation 
        add constraint FK27CB1E971E403E0B 
        foreign key (annotations_id) 
        references Annotation;

    alter table Rights_Annotation_AUD 
        add constraint FKF98828E834869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Rights_Marker 
        add constraint FKB739BBA2777265A1 
        foreign key (markers_id) 
        references Marker;

    alter table Rights_Marker 
        add constraint FKB739BBA2C13F7B21 
        foreign key (Rights_id) 
        references Rights;

    alter table Rights_Marker_AUD 
        add constraint FKC6FB487334869AAE 
        foreign key (REV) 
        references AuditEvent;

    create index sequenceTitleCacheIndex on Sequence (titleCache);

    alter table Sequence 
        add constraint FK544ADBE14FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table Sequence 
        add constraint FK544ADBE1B982A103 
        foreign key (publishedin_id) 
        references Reference;

    alter table Sequence 
        add constraint FK544ADBE12DBE1F1F 
        foreign key (locus_id) 
        references Locus;

    alter table Sequence 
        add constraint FK544ADBE1BC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table Sequence_AUD 
        add constraint FK39F4313234869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Sequence_Annotation 
        add constraint FK1010BA6DD57FFDD5 
        foreign key (Sequence_id) 
        references Sequence;

    alter table Sequence_Annotation 
        add constraint FK1010BA6D1E403E0B 
        foreign key (annotations_id) 
        references Annotation;

    alter table Sequence_Annotation_AUD 
        add constraint FKCB4FE9BE34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Sequence_Credit 
        add constraint FK2CFBC93732D1B9F 
        foreign key (credits_id) 
        references Credit;

    alter table Sequence_Credit 
        add constraint FK2CFBC937D57FFDD5 
        foreign key (Sequence_id) 
        references Sequence;

    alter table Sequence_Credit_AUD 
        add constraint FK4B22838834869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Sequence_Extension 
        add constraint FK7BE66D41927DE9DF 
        foreign key (extensions_id) 
        references Extension;

    alter table Sequence_Extension 
        add constraint FK7BE66D41D57FFDD5 
        foreign key (Sequence_id) 
        references Sequence;

    alter table Sequence_Extension_AUD 
        add constraint FK1CA8129234869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Sequence_GenBankAccession 
        add constraint FK8F698096D57FFDD5 
        foreign key (Sequence_id) 
        references Sequence;

    alter table Sequence_GenBankAccession 
        add constraint FK8F69809615C4EF35 
        foreign key (genbankaccession_id) 
        references GenBankAccession;

    alter table Sequence_GenBankAccession_AUD 
        add constraint FKC717736734869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Sequence_Marker 
        add constraint FK3D22B278777265A1 
        foreign key (markers_id) 
        references Marker;

    alter table Sequence_Marker 
        add constraint FK3D22B278D57FFDD5 
        foreign key (Sequence_id) 
        references Sequence;

    alter table Sequence_Marker_AUD 
        add constraint FKAF40E44934869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Sequence_Media 
        add constraint FK8E5D91E666ACA7EC 
        foreign key (chromatograms_id) 
        references Media;

    alter table Sequence_Media 
        add constraint FK8E5D91E6D57FFDD5 
        foreign key (Sequence_id) 
        references Sequence;

    alter table Sequence_Media_AUD 
        add constraint FK20025CB734869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Sequence_OriginalSourceBase 
        add constraint FKCDB0237BD57FFDD5 
        foreign key (Sequence_id) 
        references Sequence;

    alter table Sequence_OriginalSourceBase 
        add constraint FKCDB0237B3A6735D9 
        foreign key (sources_id) 
        references OriginalSourceBase;

    alter table Sequence_OriginalSourceBase_AUD 
        add constraint FK69D81BCC34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Sequence_Reference 
        add constraint FK6944904D7291F8A 
        foreign key (citations_id) 
        references Reference;

    alter table Sequence_Reference 
        add constraint FK6944904DD57FFDD5 
        foreign key (Sequence_id) 
        references Sequence;

    alter table Sequence_Reference_AUD 
        add constraint FK18E5CF9E34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Sequence_Rights 
        add constraint FK4616A015C13F7B21 
        foreign key (rights_id) 
        references Rights;

    alter table Sequence_Rights 
        add constraint FK4616A015D57FFDD5 
        foreign key (Sequence_id) 
        references Sequence;

    alter table Sequence_Rights_AUD 
        add constraint FK1B1ABB6634869AAE 
        foreign key (REV) 
        references AuditEvent;

    create index specimenOrObservationBaseTitleCacheIndex on SpecimenOrObservationBase (titleCache);

    
    alter table SpecimenOrObservationBase 
        add constraint FK11CB3232F75F225E 
        foreign key (primarycollector_id) 
        references AgentBase;
    
    alter table SpecimenOrObservationBase 
        add constraint FK21CA32727CC340C5 
        foreign key (storedunder_id) 
        references TaxonNameBase;

    alter table SpecimenOrObservationBase 
        add constraint FK21CA32728C750E27 
        foreign key (lifestage_id) 
        references DefinedTermBase;

    alter table SpecimenOrObservationBase 
        add constraint FK21CA32724FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table SpecimenOrObservationBase 
        add constraint FK21CA3272EB62BE9A 
        foreign key (collection_id) 
        references Collection;

    alter table SpecimenOrObservationBase 
        add constraint FK21CA3272E17C9A6B 
        foreign key (sex_id) 
        references DefinedTermBase;

    alter table SpecimenOrObservationBase 
        add constraint FK21CA3272C8505DB 
        foreign key (preservation_id) 
        references DefinedTermBase;

    alter table SpecimenOrObservationBase 
        add constraint FK21CA32724AAB411A 
        foreign key (derivedfrom_id) 
        references DerivationEvent;

    alter table SpecimenOrObservationBase 
        add constraint FK21CA3272F95F225A 
        foreign key (gatheringevent_id) 
        references GatheringEvent;

    alter table SpecimenOrObservationBase 
        add constraint FK21CA3272BC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table SpecimenOrObservationBase_AUD 
        add constraint FKF3D3D74334869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table SpecimenOrObservationBase_Annotation 
        add constraint FK365E4F3C3B8A5ABA 
        foreign key (SpecimenOrObservationBase_id) 
        references SpecimenOrObservationBase;

    alter table SpecimenOrObservationBase_Annotation 
        add constraint FK365E4F3C1E403E0B 
        foreign key (annotations_id) 
        references Annotation;

    alter table SpecimenOrObservationBase_Annotation_AUD 
        add constraint FK34187F0D34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table SpecimenOrObservationBase_Credit 
        add constraint FK7E3A1D8632D1B9F 
        foreign key (credits_id) 
        references Credit;

    alter table SpecimenOrObservationBase_Credit 
        add constraint FK7E3A1D863B8A5ABA 
        foreign key (SpecimenOrObservationBase_id) 
        references SpecimenOrObservationBase;

    alter table SpecimenOrObservationBase_Credit_AUD 
        add constraint FK7170185734869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table SpecimenOrObservationBase_DerivationEvent 
        add constraint FK20132036BD59A1AD 
        foreign key (derivationevents_id) 
        references DerivationEvent;

    alter table SpecimenOrObservationBase_DerivationEvent 
        add constraint FK2013203654C216AA 
        foreign key (originals_id) 
        references SpecimenOrObservationBase;

    alter table SpecimenOrObservationBase_DerivationEvent_AUD 
        add constraint FKA4A8430734869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table SpecimenOrObservationBase_Extension 
        add constraint FKE03B8292927DE9DF 
        foreign key (extensions_id) 
        references Extension;

    alter table SpecimenOrObservationBase_Extension 
        add constraint FKE03B82923B8A5ABA 
        foreign key (SpecimenOrObservationBase_id) 
        references SpecimenOrObservationBase;

    alter table SpecimenOrObservationBase_Extension_AUD 
        add constraint FK7AE0176334869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table SpecimenOrObservationBase_LanguageString 
        add constraint FKCFAA931628459272 
        foreign key (definition_mapkey_id) 
        references DefinedTermBase;

    alter table SpecimenOrObservationBase_LanguageString 
        add constraint FKCFAA93162BEBA58D 
        foreign key (definition_id) 
        references LanguageString;

    alter table SpecimenOrObservationBase_LanguageString 
        add constraint FKCFAA93163B8A5ABA 
        foreign key (SpecimenOrObservationBase_id) 
        references SpecimenOrObservationBase;

    alter table SpecimenOrObservationBase_LanguageString_AUD 
        add constraint FK38B45E734869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table SpecimenOrObservationBase_Marker 
        add constraint FK8E6106C7777265A1 
        foreign key (markers_id) 
        references Marker;

    alter table SpecimenOrObservationBase_Marker 
        add constraint FK8E6106C73B8A5ABA 
        foreign key (SpecimenOrObservationBase_id) 
        references SpecimenOrObservationBase;

    alter table SpecimenOrObservationBase_Marker_AUD 
        add constraint FKD58E791834869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table SpecimenOrObservationBase_Media 
        add constraint FK4EEBF7B7C2C29593 
        foreign key (media_id) 
        references Media;

    alter table SpecimenOrObservationBase_Media 
        add constraint FK4EEBF7B73B8A5ABA 
        foreign key (SpecimenOrObservationBase_id) 
        references SpecimenOrObservationBase;

    alter table SpecimenOrObservationBase_Media_AUD 
        add constraint FK8457720834869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table SpecimenOrObservationBase_OriginalSourceBase 
        add constraint FKCA7F794A3B8A5ABA 
        foreign key (SpecimenOrObservationBase_id) 
        references SpecimenOrObservationBase;

    alter table SpecimenOrObservationBase_OriginalSourceBase 
        add constraint FKCA7F794A3A6735D9 
        foreign key (sources_id) 
        references OriginalSourceBase;

    alter table SpecimenOrObservationBase_OriginalSourceBase_AUD 
        add constraint FK2059F21B34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table SpecimenOrObservationBase_Rights 
        add constraint FK9754F464C13F7B21 
        foreign key (rights_id) 
        references Rights;

    alter table SpecimenOrObservationBase_Rights 
        add constraint FK9754F4643B8A5ABA 
        foreign key (SpecimenOrObservationBase_id) 
        references SpecimenOrObservationBase;

    alter table SpecimenOrObservationBase_Rights_AUD 
        add constraint FK4168503534869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table SpecimenOrObservationBase_Sequence 
        add constraint FKBBF27B0E35B10F24 
        foreign key (sequences_id) 
        references Sequence;

    alter table SpecimenOrObservationBase_Sequence 
        add constraint FKBBF27B0E7EE2770E 
        foreign key (SpecimenOrObservationBase_id) 
        references SpecimenOrObservationBase;

    alter table SpecimenOrObservationBase_Sequence_AUD 
        add constraint FK392E71DF34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table StateData 
        add constraint FKFB1697BB4FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table StateData 
        add constraint FKFB1697BB682A4E4B 
        foreign key (state_id) 
        references DefinedTermBase;

    alter table StateData 
        add constraint FKFB1697BBBC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table StateData_AUD 
        add constraint FKDA6A700C34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table StateData_DefinedTermBase 
        add constraint FK107321E28E7BF9AB 
        foreign key (StateData_id) 
        references StateData;

    alter table StateData_DefinedTermBase 
        add constraint FK107321E2E0960EC4 
        foreign key (modifiers_id) 
        references DefinedTermBase;

    alter table StateData_DefinedTermBase_AUD 
        add constraint FK7C978EB334869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table StateData_LanguageString 
        add constraint FK93FFD2AD8E7BF9AB 
        foreign key (StateData_id) 
        references StateData;

    alter table StateData_LanguageString 
        add constraint FK93FFD2ADF05D08D4 
        foreign key (modifyingtext_id) 
        references LanguageString;

    alter table StateData_LanguageString 
        add constraint FK93FFD2AD9682414B 
        foreign key (modifyingtext_mapkey_id) 
        references DefinedTermBase;

    alter table StateData_LanguageString_AUD 
        add constraint FK1578E1FE34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table StatisticalMeasurementValue 
        add constraint FK2DCE02904FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table StatisticalMeasurementValue 
        add constraint FK2DCE02904C428112 
        foreign key (type_id) 
        references DefinedTermBase;

    alter table StatisticalMeasurementValue 
        add constraint FK2DCE0290BC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table StatisticalMeasurementValue_AUD 
        add constraint FKBB16686134869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table StatisticalMeasurementValue_DefinedTermBase 
        add constraint FK686C42B7E0960EC4 
        foreign key (modifiers_id) 
        references DefinedTermBase;

    alter table StatisticalMeasurementValue_DefinedTermBase 
        add constraint FK686C42B75C9F4F2B 
        foreign key (StatisticalMeasurementValue_id) 
        references StatisticalMeasurementValue;

    alter table StatisticalMeasurementValue_DefinedTermBase_AUD 
        add constraint FKFEBA3D0834869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table SynonymRelationship 
        add constraint FKF483ADB34FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table SynonymRelationship 
        add constraint FKF483ADB39803512F 
        foreign key (citation_id) 
        references Reference;

    alter table SynonymRelationship 
        add constraint FKF483ADB34BAC703F 
        foreign key (relatedfrom_id) 
        references TaxonBase;

    alter table SynonymRelationship 
        add constraint FKF483ADB380924EEC 
        foreign key (type_id) 
        references DefinedTermBase;

    alter table SynonymRelationship 
        add constraint FKF483ADB3F8991B9D 
        foreign key (relatedto_id) 
        references TaxonBase;

    alter table SynonymRelationship 
        add constraint FKF483ADB3BC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table SynonymRelationship_AUD 
        add constraint FK8AEBCA0434869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table SynonymRelationship_Annotation 
        add constraint FKF494F15B260A8379 
        foreign key (SynonymRelationship_id) 
        references SynonymRelationship;

    alter table SynonymRelationship_Annotation 
        add constraint FKF494F15B1E403E0B 
        foreign key (annotations_id) 
        references Annotation;

    alter table SynonymRelationship_Annotation_AUD 
        add constraint FKD3E2F9AC34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table SynonymRelationship_Marker 
        add constraint FK7A439066777265A1 
        foreign key (markers_id) 
        references Marker;

    alter table SynonymRelationship_Marker 
        add constraint FK7A439066260A8379 
        foreign key (SynonymRelationship_id) 
        references SynonymRelationship;

    alter table SynonymRelationship_Marker_AUD 
        add constraint FK93C51B3734869AAE 
        foreign key (REV) 
        references AuditEvent;

    create index taxonBaseTitleCacheIndex on TaxonBase (titleCache);

    alter table TaxonBase 
        add constraint FK9249B49B4FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table TaxonBase 
        add constraint FK9249B49B5E4A2F85 
        foreign key (sec_id) 
        references Reference;

    alter table TaxonBase 
        add constraint FK9249B49B7C7B5AED 
        foreign key (taxonomicparentcache_id) 
        references TaxonBase;

    alter table TaxonBase 
        add constraint FK9249B49BDA93512F 
        foreign key (name_id) 
        references TaxonNameBase;

    alter table TaxonBase 
        add constraint FK9249B49BBC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table TaxonBase_AUD 
        add constraint FK37041CEC34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table TaxonBase_Annotation 
        add constraint FK41ED09739C9D39 
        foreign key (TaxonBase_id) 
        references TaxonBase;

    alter table TaxonBase_Annotation 
        add constraint FK41ED09731E403E0B 
        foreign key (annotations_id) 
        references Annotation;

    alter table TaxonBase_Annotation_AUD 
        add constraint FK8C145C434869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table TaxonBase_Credit 
        add constraint FK4CB48B3D9C9D39 
        foreign key (TaxonBase_id) 
        references TaxonBase;

    alter table TaxonBase_Credit 
        add constraint FK4CB48B3D32D1B9F 
        foreign key (credits_id) 
        references Credit;

    alter table TaxonBase_Credit_AUD 
        add constraint FK7CFED28E34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table TaxonBase_Extension 
        add constraint FKF961257B927DE9DF 
        foreign key (extensions_id) 
        references Extension;

    alter table TaxonBase_Extension 
        add constraint FKF961257B9C9D39 
        foreign key (TaxonBase_id) 
        references TaxonBase;

    alter table TaxonBase_Extension_AUD 
        add constraint FK71381DCC34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table TaxonBase_Marker 
        add constraint FK5CDB747E777265A1 
        foreign key (markers_id) 
        references Marker;

    alter table TaxonBase_Marker 
        add constraint FK5CDB747E9C9D39 
        foreign key (TaxonBase_id) 
        references TaxonBase;

    alter table TaxonBase_Marker_AUD 
        add constraint FKE11D334F34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table TaxonBase_OriginalSourceBase 
        add constraint FKFB680C819C9D39 
        foreign key (TaxonBase_id) 
        references TaxonBase;

    alter table TaxonBase_OriginalSourceBase 
        add constraint FKFB680C813A6735D9 
        foreign key (sources_id) 
        references OriginalSourceBase;

    alter table TaxonBase_OriginalSourceBase_AUD 
        add constraint FKB7C811D234869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table TaxonBase_Rights 
        add constraint FK65CF621B9C9D39 
        foreign key (TaxonBase_id) 
        references TaxonBase;

    alter table TaxonBase_Rights 
        add constraint FK65CF621BC13F7B21 
        foreign key (rights_id) 
        references Rights;

    alter table TaxonBase_Rights_AUD 
        add constraint FK4CF70A6C34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table TaxonInteraction_LanguageString 
        add constraint FK579A1DC028459272 
        foreign key (description_mapkey_id) 
        references DefinedTermBase;

    alter table TaxonInteraction_LanguageString 
        add constraint FK579A1DC086C86FE0 
        foreign key (DescriptionElementBase_id) 
        references DescriptionElementBase;

    alter table TaxonInteraction_LanguageString 
        add constraint FK579A1DC02BEBA58D 
        foreign key (description_id) 
        references LanguageString;

    alter table TaxonInteraction_LanguageString_AUD 
        add constraint FK9E016B9134869AAE 
        foreign key (REV) 
        references AuditEvent;

    create index taxonNameBaseTitleCacheIndex on TaxonNameBase (titleCache);

    alter table TaxonNameBase 
        add constraint FKB4870C64FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table TaxonNameBase 
        add constraint FKB4870C662AD57A2 
        foreign key (excombinationauthorteam_id) 
        references AgentBase;

    alter table TaxonNameBase 
        add constraint FKB4870C6BFEAE500 
        foreign key (homotypicalgroup_id) 
        references HomotypicalGroup;

    alter table TaxonNameBase 
        add constraint FKB4870C6D7BE55A0 
        foreign key (rank_id) 
        references DefinedTermBase;

    alter table TaxonNameBase 
        add constraint FKB4870C67F90DF03 
        foreign key (exbasionymauthorteam_id) 
        references AgentBase;

    alter table TaxonNameBase 
        add constraint FKB4870C62B4FEDD6 
        foreign key (basionymauthorteam_id) 
        references AgentBase;

    alter table TaxonNameBase 
        add constraint FKB4870C64AC9C024 
        foreign key (nomenclaturalreference_id) 
        references Reference;

    alter table TaxonNameBase 
        add constraint FKB4870C6B14B73EF 
        foreign key (combinationauthorteam_id) 
        references AgentBase;

    alter table TaxonNameBase 
        add constraint FKB4870C6BC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table TaxonNameBase_AUD 
        add constraint FK5CA2CB9734869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table TaxonNameBase_Annotation 
        add constraint FK9E7794688C85CF94 
        foreign key (TaxonNameBase_id) 
        references TaxonNameBase;

    alter table TaxonNameBase_Annotation 
        add constraint FK9E7794681E403E0B 
        foreign key (annotations_id) 
        references Annotation;

    alter table TaxonNameBase_Annotation_AUD 
        add constraint FKB6734E3934869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table TaxonNameBase_Credit 
        add constraint FK29BCD8B28C85CF94 
        foreign key (TaxonNameBase_id) 
        references TaxonNameBase;

    alter table TaxonNameBase_Credit 
        add constraint FK29BCD8B232D1B9F 
        foreign key (credits_id) 
        references Credit;

    alter table TaxonNameBase_Credit_AUD 
        add constraint FKD9895D8334869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table TaxonNameBase_Extension 
        add constraint FKC28EE7E68C85CF94 
        foreign key (TaxonNameBase_id) 
        references TaxonNameBase;

    alter table TaxonNameBase_Extension 
        add constraint FKC28EE7E6927DE9DF 
        foreign key (extensions_id) 
        references Extension;

    alter table TaxonNameBase_Extension_AUD 
        add constraint FK8F98B2B734869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table TaxonNameBase_Marker 
        add constraint FK39E3C1F3777265A1 
        foreign key (markers_id) 
        references Marker;

    alter table TaxonNameBase_Marker 
        add constraint FK39E3C1F38C85CF94 
        foreign key (TaxonNameBase_id) 
        references TaxonNameBase;

    alter table TaxonNameBase_Marker_AUD 
        add constraint FK3DA7BE4434869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table TaxonNameBase_NomenclaturalStatus 
        add constraint FK560BA7928C85CF94 
        foreign key (TaxonNameBase_id) 
        references TaxonNameBase;

    alter table TaxonNameBase_NomenclaturalStatus 
        add constraint FK560BA7926615E90D 
        foreign key (status_id) 
        references NomenclaturalStatus;

    alter table TaxonNameBase_NomenclaturalStatus_AUD 
        add constraint FK9215BC6334869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table TaxonNameBase_OriginalSourceBase 
        add constraint FKF746D2768C85CF94 
        foreign key (TaxonNameBase_id) 
        references TaxonNameBase;

    alter table TaxonNameBase_OriginalSourceBase 
        add constraint FKF746D2763A6735D9 
        foreign key (sources_id) 
        references OriginalSourceBase;

    alter table TaxonNameBase_OriginalSourceBase_AUD 
        add constraint FK7A38D54734869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table TaxonNameBase_Rights 
        add constraint FK42D7AF908C85CF94 
        foreign key (TaxonNameBase_id) 
        references TaxonNameBase;

    alter table TaxonNameBase_Rights 
        add constraint FK42D7AF90C13F7B21 
        foreign key (rights_id) 
        references Rights;

    alter table TaxonNameBase_Rights_AUD 
        add constraint FKA981956134869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table TaxonNameBase_TypeDesignationBase 
        add constraint FKC0D6BBB58C85CF94 
        foreign key (TaxonNameBase_id) 
        references TaxonNameBase;

    alter table TaxonNameBase_TypeDesignationBase 
        add constraint FKC0D6BBB5C7DF530C 
        foreign key (typedesignations_id) 
        references TypeDesignationBase;

    alter table TaxonNameBase_TypeDesignationBase_AUD 
        add constraint FKBB24070634869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table TaxonNode 
        add constraint FK924F5BCC4FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table TaxonNode 
        add constraint FK924F5BCC215EDF26 
        foreign key (referenceforparentchildrelation_id) 
        references Reference;

    alter table TaxonNode 
        add constraint FK924F5BCC759FE399 
        foreign key (classification_id) 
        references Classification;

    alter table TaxonNode 
        add constraint FK924F5BCCDE9A3E39 
        foreign key (taxon_id) 
        references TaxonBase;

    alter table TaxonNode 
        add constraint FK924F5BCCCC05993E 
        foreign key (synonymtobeused_id) 
        references TaxonBase;

    alter table TaxonNode 
        add constraint FK924F5BCC39DB2DFB 
        foreign key (parent_id) 
        references TaxonNode;

    alter table TaxonNode 
        add constraint FK924F5BCCBC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table TaxonNode_AUD 
        add constraint FKE090C39D34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table TaxonNode_Annotation 
        add constraint FKD8A9A9A2927D8399 
        foreign key (TaxonNode_id) 
        references TaxonNode;

    alter table TaxonNode_Annotation 
        add constraint FKD8A9A9A21E403E0B 
        foreign key (annotations_id) 
        references Annotation;

    alter table TaxonNode_Annotation_AUD 
        add constraint FKB2C4367334869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table TaxonNode_Marker 
        add constraint FK395842D777265A1 
        foreign key (markers_id) 
        references Marker;

    alter table TaxonNode_Marker 
        add constraint FK395842D927D8399 
        foreign key (TaxonNode_id) 
        references TaxonNode;

    alter table TaxonNode_Marker_AUD 
        add constraint FK77D9D37E34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table TaxonRelationship 
        add constraint FK7482BA024FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table TaxonRelationship 
        add constraint FK7482BA029803512F 
        foreign key (citation_id) 
        references Reference;

    alter table TaxonRelationship 
        add constraint FK7482BA02E71EF6CE 
        foreign key (relatedfrom_id) 
        references TaxonBase;

    alter table TaxonRelationship 
        add constraint FK7482BA02F11BD77B 
        foreign key (type_id) 
        references DefinedTermBase;

    alter table TaxonRelationship 
        add constraint FK7482BA02F8991B9D 
        foreign key (relatedto_id) 
        references TaxonBase;

    alter table TaxonRelationship 
        add constraint FK7482BA02BC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table TaxonRelationship_AUD 
        add constraint FKA0DE16D334869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table TaxonRelationship_Annotation 
        add constraint FK82C86DAC2BD180D9 
        foreign key (TaxonRelationship_id) 
        references TaxonRelationship;

    alter table TaxonRelationship_Annotation 
        add constraint FK82C86DAC1E403E0B 
        foreign key (annotations_id) 
        references Annotation;

    alter table TaxonRelationship_Annotation_AUD 
        add constraint FKE86DE57D34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table TaxonRelationship_Marker 
        add constraint FK69FBDD37777265A1 
        foreign key (markers_id) 
        references Marker;

    alter table TaxonRelationship_Marker 
        add constraint FK69FBDD372BD180D9 
        foreign key (TaxonRelationship_id) 
        references TaxonRelationship;

    alter table TaxonRelationship_Marker_AUD 
        add constraint FK21F8978834869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Classification 
        add constraint FKE332DBE04FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table Classification 
        add constraint FKE332DBE0765B124B 
        foreign key (reference_id) 
        references Reference;

    alter table Classification 
        add constraint FKE332DBE077E2F09E 
        foreign key (name_id) 
        references LanguageString;

    alter table Classification 
        add constraint FKE332DBE0BC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table Classification_AUD 
        add constraint FK14CE19B134869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Classification_Annotation 
        add constraint FK9877150E759FE399 
        foreign key (Classification_id) 
        references Classification;

    alter table Classification_Annotation 
        add constraint FK9877150E1E403E0B 
        foreign key (annotations_id) 
        references Annotation;

    alter table Classification_Annotation_AUD 
        add constraint FKADD60BDF34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Classification_Credit 
        add constraint FK21329C58759FE399 
        foreign key (Classification_id) 
        references Classification;

    alter table Classification_Credit 
        add constraint FK21329C5832D1B9F 
        foreign key (credits_id) 
        references Credit;

    alter table Classification_Credit_AUD 
        add constraint FKD388DE2934869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Classification_Extension 
        add constraint FKF3E9BA80759FE399 
        foreign key (Classification_id) 
        references Classification;

    alter table Classification_Extension 
        add constraint FKF3E9BA80927DE9DF 
        foreign key (extensions_id) 
        references Extension;

    alter table Classification_Extension_AUD 
        add constraint FK1BB4A85134869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Classification_Marker 
        add constraint FK31598599777265A1 
        foreign key (markers_id) 
        references Marker;

    alter table Classification_Marker 
        add constraint FK31598599759FE399 
        foreign key (Classification_id) 
        references Classification;

    alter table Classification_Marker_AUD 
        add constraint FK37A73EEA34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Classification_OriginalSourceBase 
        add constraint FKDE264D1C759FE399 
        foreign key (Classification_id) 
        references Classification;

    alter table Classification_OriginalSourceBase 
        add constraint FKDE264D1C3A6735D9 
        foreign key (sources_id) 
        references OriginalSourceBase;

    alter table Classification_OriginalSourceBase_AUD 
        add constraint FK99EE8CED34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Classification_Rights 
        add constraint FK3A4D7336759FE399 
        foreign key (Classification_id) 
        references Classification;

    alter table Classification_Rights 
        add constraint FK3A4D7336C13F7B21 
        foreign key (rights_id) 
        references Rights;

    alter table Classification_Rights_AUD 
        add constraint FKA381160734869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table Classification_TaxonNode 
        add constraint FK3349DA2D759FE399 
        foreign key (Classification_id) 
        references Classification;

    alter table Classification_TaxonNode 
        add constraint FK3349DA2D18929176 
        foreign key (rootnodes_id) 
        references TaxonNode;

    alter table Classification_TaxonNode_AUD 
        add constraint FK6973297E34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table TermVocabulary 
        add constraint FK487AA6924FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table TermVocabulary 
        add constraint FK487AA692BC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table TermVocabulary_AUD 
        add constraint FKA6ED3B6334869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table TermVocabulary_Annotation 
        add constraint FK76D2071C258E060 
        foreign key (TermVocabulary_id) 
        references TermVocabulary;

    alter table TermVocabulary_Annotation 
        add constraint FK76D2071C1E403E0B 
        foreign key (annotations_id) 
        references Annotation;

    alter table TermVocabulary_Annotation_AUD 
        add constraint FK222D46ED34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table TermVocabulary_Credit 
        add constraint FK7604C566258E060 
        foreign key (TermVocabulary_id) 
        references TermVocabulary;

    alter table TermVocabulary_Credit 
        add constraint FK7604C56632D1B9F 
        foreign key (credits_id) 
        references Credit;

    alter table TermVocabulary_Credit_AUD 
        add constraint FKB1E3D03734869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table TermVocabulary_Extension 
        add constraint FKA8814EB2258E060 
        foreign key (TermVocabulary_id) 
        references TermVocabulary;

    alter table TermVocabulary_Extension 
        add constraint FKA8814EB2927DE9DF 
        foreign key (extensions_id) 
        references Extension;

    alter table TermVocabulary_Extension_AUD 
        add constraint FKD522D38334869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table TermVocabulary_Marker 
        add constraint FK862BAEA7777265A1 
        foreign key (markers_id) 
        references Marker;

    alter table TermVocabulary_Marker 
        add constraint FK862BAEA7258E060 
        foreign key (TermVocabulary_id) 
        references TermVocabulary;

    alter table TermVocabulary_Marker_AUD 
        add constraint FK160230F834869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table TermVocabulary_OriginalSourceBase 
        add constraint FK8F2D512A258E060 
        foreign key (TermVocabulary_id) 
        references TermVocabulary;

    alter table TermVocabulary_OriginalSourceBase 
        add constraint FK8F2D512A3A6735D9 
        foreign key (sources_id) 
        references OriginalSourceBase;

    alter table TermVocabulary_OriginalSourceBase_AUD 
        add constraint FKA898D9FB34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table TermVocabulary_Representation 
        add constraint FKA408B63A258E060 
        foreign key (TermVocabulary_id) 
        references TermVocabulary;

    alter table TermVocabulary_Representation 
        add constraint FKA408B63AB31C4747 
        foreign key (representations_id) 
        references Representation;

    alter table TermVocabulary_Representation_AUD 
        add constraint FK681B370B34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table TermVocabulary_Rights 
        add constraint FK8F1F9C44258E060 
        foreign key (TermVocabulary_id) 
        references TermVocabulary;

    alter table TermVocabulary_Rights 
        add constraint FK8F1F9C44C13F7B21 
        foreign key (rights_id) 
        references Rights;

    alter table TermVocabulary_Rights_AUD 
        add constraint FK81DC081534869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table TypeDesignationBase 
        add constraint FK8AC9DCAE4FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table TypeDesignationBase 
        add constraint FK8AC9DCAE9803512F 
        foreign key (citation_id) 
        references Reference;

    alter table TypeDesignationBase 
        add constraint FK8AC9DCAEBFEAE500 
        foreign key (homotypicalgroup_id) 
        references HomotypicalGroup;

    alter table TypeDesignationBase 
        add constraint FK8AC9DCAE94DB044A 
        foreign key (typespecimen_id) 
        references SpecimenOrObservationBase;

    alter table TypeDesignationBase 
        add constraint FK8AC9DCAE4CB0F315 
        foreign key (typename_id) 
        references TaxonNameBase;

    alter table TypeDesignationBase 
        add constraint FK8AC9DCAE9E3ED08 
        foreign key (typestatus_id) 
        references DefinedTermBase;

    alter table TypeDesignationBase 
        add constraint FK8AC9DCAEBC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table TypeDesignationBase_AUD 
        add constraint FK243C037F34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table TypeDesignationBase_Annotation 
        add constraint FK4D73278044E9E6D4 
        foreign key (TypeDesignationBase_id) 
        references TypeDesignationBase;

    alter table TypeDesignationBase_Annotation 
        add constraint FK4D7327801E403E0B 
        foreign key (annotations_id) 
        references Annotation;

    alter table TypeDesignationBase_Annotation_AUD 
        add constraint FK88BF955134869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table TypeDesignationBase_Marker 
        add constraint FKB914A10B777265A1 
        foreign key (markers_id) 
        references Marker;

    alter table TypeDesignationBase_Marker 
        add constraint FKB914A10B44E9E6D4 
        foreign key (TypeDesignationBase_id) 
        references TypeDesignationBase;

    alter table TypeDesignationBase_Marker_AUD 
        add constraint FKECA3515C34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table TypeDesignationBase_TaxonNameBase 
        add constraint FKF61156F54D901A92 
        foreign key (typifiednames_id) 
        references TaxonNameBase;

    alter table TypeDesignationBase_TaxonNameBase 
        add constraint FKF61156F544E9E6D4 
        foreign key (TypeDesignationBase_id) 
        references TypeDesignationBase;

    alter table TypeDesignationBase_TaxonNameBase_AUD 
        add constraint FK4F1F024634869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table UserAccount 
        add constraint FKB3F13C24FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table UserAccount 
        add constraint FKB3F13C2AAC1B820 
        foreign key (person_id) 
        references AgentBase;

    alter table UserAccount_AUD 
        add constraint FK6A57909334869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table UserAccount_GrantedAuthorityImpl 
        add constraint FKFD724D855EA5DD89 
        foreign key (UserAccount_id) 
        references UserAccount;

    alter table UserAccount_GrantedAuthorityImpl 
        add constraint FKFD724D851857F6C2 
        foreign key (grantedauthorities_id) 
        references GrantedAuthorityImpl;

    alter table UserAccount_PermissionGroup 
        add constraint FK812DE753887E3D12 
        foreign key (members_id) 
        references UserAccount;

    alter table UserAccount_PermissionGroup 
        add constraint FK812DE753DA9DCB5F 
        foreign key (groups_id) 
        references PermissionGroup;

    alter table WorkingSet 
        add constraint FK668D5B914FF2DB2C 
        foreign key (createdby_id) 
        references UserAccount;

    alter table WorkingSet 
        add constraint FK668D5B9123DB7F04 
        foreign key (descriptivesystem_id) 
        references FeatureTree;

    alter table WorkingSet 
        add constraint FK668D5B91BC5DA539 
        foreign key (updatedby_id) 
        references UserAccount;

    alter table WorkingSet_AUD 
        add constraint FK628F58E234869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table WorkingSet_Annotation 
        add constraint FKCBBA8CBDBBD2C869 
        foreign key (WorkingSet_id) 
        references WorkingSet;

    alter table WorkingSet_Annotation 
        add constraint FKCBBA8CBD1E403E0B 
        foreign key (annotations_id) 
        references Annotation;

    alter table WorkingSet_Annotation_AUD 
        add constraint FK1E28140E34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table WorkingSet_DescriptionBase 
        add constraint FK731CC81F33B8A841 
        foreign key (descriptions_id) 
        references DescriptionBase;

    alter table WorkingSet_DescriptionBase 
        add constraint FK731CC81FBBD2C869 
        foreign key (WorkingSet_id) 
        references WorkingSet;

    alter table WorkingSet_DescriptionBase_AUD 
        add constraint FK8959CE7034869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table WorkingSet_Marker 
        add constraint FK9CB22CC8777265A1 
        foreign key (markers_id) 
        references Marker;

    alter table WorkingSet_Marker 
        add constraint FK9CB22CC8BBD2C869 
        foreign key (WorkingSet_id) 
        references WorkingSet;

    alter table WorkingSet_Marker_AUD 
        add constraint FK6AEAB69934869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table WorkingSet_Representation 
        add constraint FKA003835BB31C4747 
        foreign key (representations_id) 
        references Representation;

    alter table WorkingSet_Representation 
        add constraint FKA003835BBBD2C869 
        foreign key (WorkingSet_id) 
        references WorkingSet;

    alter table WorkingSet_Representation_AUD 
        add constraint FK21B88BAC34869AAE 
        foreign key (REV) 
        references AuditEvent;

    alter table WorkingSet_TaxonBase 
        add constraint FK34EB896DB4555A9A 
        foreign key (WorkingSet_id) 
        references WorkingSet;

    alter table WorkingSet_TaxonBase 
        add constraint FK34EB896D7C3D0017 
        foreign key (coveredtaxa_id) 
        references TaxonBase;

    alter table WorkingSet_TaxonBase_AUD 
        add constraint FK582B38BE34869AAE 
        foreign key (REV) 
        references AuditEvent;
