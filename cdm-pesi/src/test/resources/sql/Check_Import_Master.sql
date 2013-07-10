BEGIN

	DECLARE @n INT
	DECLARE @n_cdm INT
	DECLARE @n_bm INT
	DECLARE @str_n NVARCHAR (7)
	DECLARE @str_n_cdm NVARCHAR (7)
	DECLARE @str_n_bm NVARCHAR (7)


------------------------------------------- Taxon -------------------------------

	SELECT @n_bm = COUNT(*) FROM [EM2PESI].[DBO].TAXON
	SELECT @n_cdm = COUNT(*) FROM [CDM_EM2PESI].[DBO].TAXON
	SET @n = @n_bm - @n_cdm

	SET @str_n_bm = Cast(@n_bm AS NVARCHAR)
	SET @str_n_cdm = Cast(@n_cdm AS NVARCHAR)
	SET @str_n = Cast(@n AS NVARCHAR)

	IF @n = 0 BEGIN
		PRINT ('Both databases have the same number of taxa = ' + @str_n_bm)
	END ELSE PRINT ('WARNING: Both databases DO NOT have the same number of taxa, n_bm = ' + @str_n_bm + ' and n_cdm = '+ @str_n_cdm)
	SELECT @n = COUNT(*)
		FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_t
		ON (bm_t.IdInSource = cdm_t.IdInSource OR bm_t.IdInSource IS NULL AND cdm_t.IdInSource IS NULL) 
		    AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
	SET @str_n = Cast(@n AS NVARCHAR)
	IF @n = @n_bm BEGIN
		PRINT ('To each taxon from one database there is an identical taxon in the other one (IdInSource, GUID)')
	END ELSE PRINT ('WARNING: Only ' + @str_n + ' taxa are identical (IdInSource, GUID) in both databases')

		/*
	SELECT cdm_t.Fullname, cdm_t.IdInSource, cdm_t.GUID, 'in CDM_EM2PESI but not in EM2PESI'
	FROM [CDM_EM2PESI].[DBO].TAXON cdm_t
	WHERE NOT EXISTS
		(SELECT * FROM [EM2PESI].[DBO].TAXON WHERE (IdInSource = cdm_t.IdInSource OR IdInSource IS NULL AND cdm_t.IdInSource IS NULL)
		AND ISNULL(GUID, '') = ISNULL(cdm_t.GUID, ''))
	ORDER BY cdm_t.IdInSource

	SELECT bm_t.Fullname, bm_t.IdInSource, bm_t.GUID, 'in EM2PESI but not in CDM_EM2PESI'
	FROM [EM2PESI].[DBO].TAXON bm_t
	WHERE NOT EXISTS
		(SELECT * FROM [CDM_EM2PESI].[DBO].TAXON WHERE (IdInSource = bm_t.IdInSource OR IdInSource IS NULL AND bm_t.IdInSource IS NULL)
		AND ISNULL(GUID, '') = ISNULL(bm_t.GUID, ''))
	ORDER BY bm_t.IdInSource

	*/

-- Source
/* Does not make much sense since the source for taxa is almost always the database itself which must be created as source
	SELECT @n_bm = COUNT(*) FROM [EM2PESI].[DBO].TAXON WHERE SourceFk IS NOT NULL
	SELECT @n_cdm = COUNT(*) FROM [CDM_EM2PESI].[DBO].TAXON WHERE SourceFk IS NOT NULL
	SET @n = @n_bm - @n_cdm

	SET @str_n_bm = Cast(@n_bm AS NVARCHAR)
	SET @str_n_cdm = Cast(@n_cdm AS NVARCHAR)
	SET @str_n = Cast(@n AS NVARCHAR)

	IF @n = 0 BEGIN
		PRINT ('Both databases have the same number of taxa which have a source = ' + @str_n_bm)
	END ELSE PRINT ('WARNING: Both databases DO NOT have the same number of taxa that have a source, n_bm = ' + @str_n_bm + ' and n_cdm = '+ @str_n_cdm)

		SELECT @n = COUNT(*) -- in both databases sources exist but are different
			FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
			[CDM_EM2PESI].[DBO].TAXON cdm_t
			ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '') INNER JOIN
			[EM2PESI].[DBO].SOURCE bm_s ON bm_t.SourceFk = bm_s.SourceId INNER JOIN
			[CDM_EM2PESI].[DBO].TAXON cdm_s ON cdm_t.SourceFk = cdm_s.SourceId
			WHERE bm_s.RefIdInSource <> cdm_s.RefIdInSource OR ISNULL(bm_s.OriginalDB, '') <> ISNULL(cdm_s.OriginalDB, '')
		SET @str_n = Cast(@n AS NVARCHAR)
		IF @n = 0 BEGIN
			PRINT ('All identical taxa that have sources have the same source')
		END ELSE PRINT ('WARNING: ' + @str_n + ' identical taxa have different sources')
	/-*
		SELECT cdm_t.Fullname, cdm_s.RefIdInSource as Source_CDM_EM2PESI, bm_s.RefIdInSource as Source_EM2PESI
		FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_t
		ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '') INNER JOIN
		[EM2PESI].[DBO].SOURCE bm_s ON bm_t.SourceFk = bm_s.SourceId INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_s ON cdm_t.SourceFk = cdm_s.SourceId
		WHERE bm_s.RefIdInSource <> cdm_s.RefIdInSource OR ISNULL(bm_s.OriginalDB, '') <> ISNULL(cdm_s.OriginalDB, '')
	*-/
		SELECT @n_bm = COUNT(*) -- taxa with source only in EM2PESI
			FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
			[CDM_EM2PESI].[DBO].TAXON cdm_t
			ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '') INNER JOIN
			[EM2PESI].[DBO].SOURCE bm_s ON bm_t.SourceFk = bm_s.SourceId
			WHERE NOT EXISTS
				(SELECT * FROM [CDM_EM2PESI].[DBO].TAXON INNER JOIN
				[CDM_EM2PESI].[DBO].TAXON cdm_s ON SourceFk = SourceId
				WHERE cdm_s.RefIdInSource = bm_s.RefIdInSource AND cdm_s.OriginalDB = bm_s.OriginalDB
				AND IdInSource = bm_t.IdInSource AND ISNULL(GUID, '') = ISNULL(bm_t.GUID, ''))
		SET @str_n_bm = Cast(@n_bm AS NVARCHAR)
		IF @n_bm > 0 PRINT ('WARNING: ' + @str_n_bm + ' taxa have sources in EM2PESI but not in CDM_EM2PESI')
	/-*
		SELECT cdm_t.Fullname, bm_s.Fullname as source_EM2PESI, 'in EM2PESI but not in CDM_EM2PESI'
		FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_t
		ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '') INNER JOIN
		[EM2PESI].[DBO].SOURCE bm_s ON bm_t.SourceFk = bm_s.SourceId
		WHERE NOT EXISTS
		(SELECT * FROM [CDM_EM2PESI].[DBO].TAXON INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_s ON SourceFk = SourceId
		WHERE cdm_s.RefIdInSource = bm_s.RefIdInSource AND cdm_s.OriginalDB = bm_s.OriginalDB
		AND IdInSource = bm_t.IdInSource AND ISNULL(GUID, '') = ISNULL(bm_t.GUID, ''))
	*-/
		SELECT @n_cdm = COUNT(*) -- taxa with source only in CDM_EM2PESI
			FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
			[CDM_EM2PESI].[DBO].TAXON cdm_t
			ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '') INNER JOIN
			[CDM_EM2PESI].[DBO].SOURCE cdm_s ON cdm_t.SourceFk = cdm_s.SourceId
			WHERE NOT EXISTS
				(SELECT * FROM [EM2PESI].[DBO].TAXON INNER JOIN
				[EM2PESI].[DBO].TAXON bm_s ON SourceFk = SourceId
				WHERE cdm_s.RefIdInSource = bm_s.RefIdInSource AND cdm_s.OriginalDB = bm_s.OriginalDB
				AND IdInSource = cdm_t.IdInSource AND ISNULL(GUID, '') = ISNULL(cdm_t.GUID, ''))
		SET @str_n_cdm = Cast(@n_cdm AS NVARCHAR)
		IF @n_cdm > 0 PRINT ('WARNING: ' + @str_n_cdm + ' taxa have sources in CDM_EM2PESI but not in EM2PESI')
	/-*
		SELECT cdm_t.Fullname, cdm_s.Fullname as source_CDM_EM2PESI, 'in CDM_EM2PESI but not in EM2PESI'
		FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_t
		ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '') INNER JOIN
		[CDM_EM2PESI].[DBO].SOURCE cdm_s ON bm_t.SourceFk = cdm_s.SourceId
		(SELECT * FROM [EM2PESI].[DBO].TAXON INNER JOIN
		[EM2PESI].[DBO].TAXON bm_s ON SourceFk = SourceId
		WHERE cdm_s.RefIdInSource = bm_s.RefIdInSource AND cdm_s.OriginalDB = bm_s.OriginalDB
		AND IdInSource = cdm_t.IdInSource AND ISNULL(GUID, '') = ISNULL(cdm_t.GUID, ''))
	*-/
		IF @n = 0 AND @n_cdm = 0 AND @n_bm = 0 BEGIN
			PRINT ('All identical taxa have the same source')
		END

*/

-- Authors
	SELECT @n = COUNT(*)
		FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
		CDM_EM2PESI.[DBO].TAXON cdm_t
		ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
		WHERE ISNULL(bm_t.AuthorString, '') <> ISNULL(cdm_t.AuthorString, '')
	SET @str_n = Cast(@n AS NVARCHAR)
	IF @n = 0 BEGIN
		PRINT ('All identical taxa have the same authors')
	END ELSE PRINT ('WARNING: ' + @str_n + ' identical taxa DO NOT have the same authors')

	/*
		SELECT cdm_t.TaxonId as ID_CDM, bm_t.TaxonId as ID_SQL, cdm_t.FullName as FullName_CDM, bm_t.FullName as FullName_SQL,
			cdm_t.AuthorString as AuthorString_CDM, bm_t.AuthorString as AuthorString_SQL
		FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
		CDM_EM2PESI.[DBO].TAXON cdm_t
		ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
		WHERE ISNULL(bm_t.AuthorString, '') <> ISNULL(cdm_t.AuthorString, '')
	*/

-- Epithets
	SELECT @n = COUNT(*)
		FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_t
		ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
		WHERE ISNULL(bm_t.GenusOrUninomial, '') <> ISNULL(cdm_t.GenusOrUninomial, '') OR
		ISNULL(bm_t.InfraGenericEpithet, '') <> ISNULL(cdm_t.InfraGenericEpithet, '') OR
		ISNULL(bm_t.SpecificEpithet, '') <> ISNULL(cdm_t.SpecificEpithet, '') OR
		ISNULL(bm_t.InfraSpecificEpithet, '') <> ISNULL(cdm_t.InfraSpecificEpithet, '')
	SET @str_n = Cast(@n AS NVARCHAR)
	IF @n = 0 BEGIN
		PRINT ('All identical taxa have the same epithets')
	END ELSE PRINT ('WARNING: ' + @str_n + ' identical taxa DO NOT have the same epithets')

	/*
	SELECT  cdm_t.GenusOrUninomial as Genus_CDM, bm_t.GenusOrUninomial as Genus_SQL,
			cdm_t.InfraGenericEpithet as Infragenus_CDM, bm_t.InfraGenericEpithet as Infragenus_SQL,
			cdm_t.SpecificEpithet  as SpecificEpithet_CDM, bm_t.SpecificEpithet as SpecificEpithet_SQL,
			cdm_t.InfraSpecificEpithet as Infraspec_CDM, bm_t.InfraSpecificEpithet as Infraspec_SQL
		FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
		CDM_EM2PESI.[DBO].TAXON cdm_t
		ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
		WHERE ISNULL(bm_t.GenusOrUninomial, '') <> ISNULL(cdm_t.GenusOrUninomial, '') OR
		ISNULL(bm_t.InfraGenericEpithet, '') <> ISNULL(cdm_t.InfraGenericEpithet, '') OR
		ISNULL(bm_t.SpecificEpithet, '') <> ISNULL(cdm_t.SpecificEpithet, '') OR
		ISNULL(bm_t.InfraSpecificEpithet, '') <> ISNULL(cdm_t.InfraSpecificEpithet, '')
	*/

-- Names
	SELECT @n = COUNT(*)
		FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_t
		ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
		WHERE ISNULL(bm_t.WebSearchName, '') <> ISNULL(cdm_t.WebSearchName, '') OR
		ISNULL(bm_t.WebShowName, '') <> ISNULL(cdm_t.WebShowName, '') OR
		ISNULL(bm_t.DisplayName, '') <> ISNULL(cdm_t.DisplayName, '')
	SET @str_n = Cast(@n AS NVARCHAR)
	IF @n = 0 BEGIN
		PRINT ('All identical taxa have the same scientific name')
	END ELSE PRINT ('WARNING: ' + @str_n + ' identical taxa DO NOT have the same scientific name')
	/*
	SELECT cdm_t.WebSearchName as WebSearchName_CDM_EM2PESI, bm_t.WebSearchName as WebSearchName_EM2PESI,
		cdm_t.WebShowName as WebShowName_CDM_EM2PESI, bm_t.WebShowName as WebShowName_EM2PESI,
		cdm_t.FullName as FullName_CDM_EM2PESI, bm_t.FullName as FullName_EM2PESI,
		cdm_t.NomRefString as NomRefString_CDM_EM2PESI, bm_t.NomRefString as NomRefString_EM2PESI,
		cdm_t.DisplayName as DisplayName_CDM_EM2PESI, bm_t.DisplayName as DisplayName_EM2PESI,
		cdm_t.GUID as GUID_CDM, bm_t.GUID as GUID_SQL
	FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
	CDM_EM2PESI.[DBO].TAXON cdm_t
	ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
	WHERE ISNULL(bm_t.WebSearchName, '') <> ISNULL(cdm_t.WebSearchName, '') OR
		ISNULL(bm_t.WebShowName, '') <> ISNULL(cdm_t.WebShowName, '') OR
		ISNULL(bm_t.DisplayName, '') <> ISNULL(cdm_t.DisplayName, '')
	*/

-- Full Name
	SELECT @n = COUNT(*)
		FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
		CDM_EM2PESI.[DBO].TAXON cdm_t
		ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
		WHERE ISNULL(bm_t.FullName, '') <> ISNULL(cdm_t.FullName, '')
	SET @str_n = Cast(@n AS NVARCHAR)
	IF @n = 0 BEGIN
		PRINT ('All identical taxa have the same full name')
	END ELSE PRINT ('WARNING: ' + @str_n + ' identical taxa DO NOT have the same full name')

/*
	SELECT cdm_t.FullName as FullName_CDM_EM2PESI, bm_t.FullName as FullName_EM2PESI,
		cdm_t.WebSearchName as WebSearchName_CDM_EM2PESI, bm_t.WebSearchName as WebSearchName_EM2PESI,
		cdm_t.WebShowName as WebShowName_CDM_EM2PESI, bm_t.WebShowName as WebShowName_EM2PESI,
		cdm_t.FullName as FullName_CDM_EM2PESI, bm_t.FullName as FullName_EM2PESI,
		cdm_t.NomRefString as NomRefString_CDM_EM2PESI, bm_t.NomRefString as NomRefString_EM2PESI,
		cdm_t.DisplayName as DisplayName_CDM_EM2PESI, bm_t.DisplayName as DisplayName_EM2PESI,
		cdm_t.GUID as GUID_CDM, bm_t.GUID as GUID_SQL
	FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
		CDM_EM2PESI.[DBO].TAXON cdm_t
		ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
	WHERE ISNULL(bm_t.FullName, '') <> ISNULL(cdm_t.FullName, '')
	ORDER BY cdm_t.FullName
	*/

-- Nom Ref

	SELECT @n = COUNT(*)
		FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
		CDM_EM2PESI.[DBO].TAXON cdm_t
		ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
		WHERE ISNULL(bm_t.NomRefString, '') <> ISNULL(cdm_t.NomRefString, '')
	SET @str_n = Cast(@n AS NVARCHAR)
	IF @n = 0 BEGIN
		PRINT ('All identical taxa have the same nomenclatural reference')
	END ELSE PRINT ('WARNING: ' + @str_n + ' identical taxa DO NOT have the same nomenclatural reference')
	/*
	SELECT cdm_t.FullName as FullName_CDM_EM2PESI, bm_t.FullName as FullName_EM2PESI,
		cdm_t.NomRefString as NomRefString_CDM_EM2PESI, bm_t.NomRefString as NomRefString_EM2PESI,
		cdm_t.DisplayName as DisplayName_CDM_EM2PESI, bm_t.DisplayName as DisplayName_EM2PESI,
		cdm_t.GUID as GUID_CDM, bm_t.GUID as GUID_SQL
	FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
		CDM_EM2PESI.[DBO].TAXON cdm_t
	ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
	WHERE ISNULL(bm_t.NomRefString, '') <> ISNULL(cdm_t.NomRefString, '')
	ORDER BY cdm_t.NomRefString
	*/

-- Ranks
	SELECT @n = COUNT(*)
		FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_t
		ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
		WHERE ISNULL(bm_t.KingdomFk, 0) <> ISNULL(cdm_t.KingdomFk, 0) OR
		ISNULL(bm_t.RankFk, 0) <> ISNULL(cdm_t.RankFk, 0) OR
		ISNULL(bm_t.RankCache, '') <> ISNULL(cdm_t.RankCache, '')
	SET @str_n = Cast(@n AS NVARCHAR)
	IF @n = 0 BEGIN
		PRINT ('All identical taxa have the same rank (KingdomFk, RankFk, RankCache)')
	END ELSE PRINT ('WARNING: ' + @str_n + ' identical taxa DO NOT have the same rank (KingdomFk, RankFk, RankCache)')
	/*
	SELECT cdm_t.Fullname, cdm_t.KingdomFk as Kingdom_CDM_EM2PESI, bm_t.KingdomFk as Kingdom_EM2PESI,
	cdm_t.RankFk as RankFk_CDM_EM2PESI, bm_t.RankFk as RankFk_EM2PESI,
	cdm_t.RankCache as Rank_CDM_EM2PESI, bm_t.RankCache as Rank_EM2PESI
	FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
	[CDM_EM2PESI].[DBO].TAXON cdm_t
	ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
	WHERE ISNULL(bm_t.KingdomFk, 0) <> ISNULL(cdm_t.KingdomFk, 0) OR
	ISNULL(bm_t.RankFk, 0) <> ISNULL(cdm_t.RankFk, 0) OR
	ISNULL(bm_t.RankCache, '') <> ISNULL(cdm_t.RankCache, '')
	*/

-- Status
	SELECT @n = COUNT(*)
		FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_t
		ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
		WHERE ISNULL(bm_t.NameStatusCache, '') <> ISNULL(cdm_t.NameStatusCache, '') OR
		ISNULL(bm_t.TaxonStatusCache, '') <> ISNULL(cdm_t.TaxonStatusCache, '')
	SET @str_n = Cast(@n AS NVARCHAR)
	IF @n = 0 BEGIN
		PRINT ('All identical taxa have the same status (NameStatusCache, TaxonStatusCache)')
	END ELSE PRINT ('WARNING: ' + @str_n + ' identical taxa DO NOT have the same status (NameStatusCache, TaxonStatusCache)')
	/*
	SELECT cdm_t.Fullname, cdm_t.NameStatusFk as NameStatusFk_CDM_EM2PESI, bm_t.NameStatusFk as NameStatusFk_EM2PESI,
	cdm_t.NameStatusCache as NameStatus_CDM_EM2PESI, bm_t.NameStatusCache as NameStatus_EM2PESI,
	cdm_t.TaxonStatusFk as TaxonStatusFk_CDM_EM2PESI, bm_t.TaxonStatusFk as TaxonStatusFk_EM2PESI,
	cdm_t.TaxonStatusCache as TaxonStatus_CDM_EM2PESI, bm_t.TaxonStatusCache as TaxonStatus_EM2PESI
	FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
	[CDM_EM2PESI].[DBO].TAXON cdm_t
	ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
	WHERE ISNULL(bm_t.NameStatusCache, '') <> ISNULL(cdm_t.NameStatusCache, '') OR
	ISNULL(bm_t.TaxonStatusCache, '') <> ISNULL(cdm_t.TaxonStatusCache, '')
	*/

-- Types
	SELECT @n_bm = COUNT(*) FROM [EM2PESI].[DBO].TAXON WHERE TypeNameFk IS NOT NULL
	SELECT @n_cdm = COUNT(*) FROM [CDM_EM2PESI].[DBO].TAXON WHERE TypeNameFk IS NOT NULL
	SET @n = @n_bm - @n_cdm

	SET @str_n_bm = Cast(@n_bm AS NVARCHAR)
	SET @str_n_cdm = Cast(@n_cdm AS NVARCHAR)
	SET @str_n = Cast(@n AS NVARCHAR)

	IF @n = 0 BEGIN
		PRINT ('Both databases have the same number of taxa which have a type = ' + @str_n_bm)
	END ELSE PRINT ('WARNING: Both databases DO NOT have the same number of taxa that have a type, n_bm = ' + @str_n_bm + ' and n_cdm = '+ @str_n_cdm)

		SELECT @n = COUNT(*) -- in both databases types exist but are different
			FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
			[CDM_EM2PESI].[DBO].TAXON cdm_t
			ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '') INNER JOIN
			[EM2PESI].[DBO].TAXON bm_pt ON bm_t.TypeNameFk = bm_pt.TaxonId INNER JOIN
			[CDM_EM2PESI].[DBO].TAXON cdm_pt ON cdm_t.TypeNameFk = cdm_pt.TaxonId
			WHERE bm_pt.IdInSource <> cdm_pt.IdInSource OR ISNULL(bm_pt.GUID, '') <> ISNULL(cdm_pt.GUID, '')
		SET @str_n = Cast(@n AS NVARCHAR)
		IF @n = 0 BEGIN
			PRINT ('All identical taxa that have types have the same type')
		END ELSE PRINT ('WARNING: ' + @str_n + ' identical taxa have different types')
	/*
		SELECT cdm_t.Fullname, cdm_pt.Fullname as Type_CDM_EM2PESI, bm_pt.Fullname as Type_EM2PESI
		FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_t
		ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '') INNER JOIN
		[EM2PESI].[DBO].TAXON bm_pt ON bm_t.TypeNameFk = bm_pt.TaxonId INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_pt ON cdm_t.TypeNameFk = cdm_pt.TaxonId
		WHERE bm_pt.IdInSource <> cdm_pt.IdInSource OR ISNULL(bm_pt.GUID, '') <> ISNULL(cdm_pt.GUID, '')
		ORDER BY cdm_t.Fullname
	*/
		SELECT @n_bm = COUNT(*) -- taxa with type only in EM2PESI
			FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
			[CDM_EM2PESI].[DBO].TAXON cdm_t
			ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '') INNER JOIN
			[EM2PESI].[DBO].TAXON bm_pt ON bm_t.TypeNameFk = bm_pt.TaxonId
			WHERE NOT EXISTS
				(SELECT * FROM [CDM_EM2PESI].[DBO].TAXON
				WHERE IdInSource = bm_pt.IdInSource AND ISNULL(GUID, '') = ISNULL(bm_pt.GUID, ''))
		SET @str_n_bm = Cast(@n_bm AS NVARCHAR)
		IF @n_bm > 0 PRINT ('WARNING: ' + @str_n_bm + ' identical taxa have types in EM2PESI but not in CDM_EM2PESI')
	/*
		SELECT cdm_t.Fullname, bm_pt.Fullname as Type_EM2PESI, 'in EM2PESI but not in CDM_EM2PESI'
		FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_t
		ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '') INNER JOIN
		[EM2PESI].[DBO].TAXON bm_pt ON bm_t.TypeNameFk = bm_pt.TaxonId
		WHERE NOT EXISTS
		(SELECT * FROM [CDM_EM2PESI].[DBO].TAXON
		WHERE IdInSource = bm_pt.IdInSource AND ISNULL(GUID, '') = ISNULL(bm_pt.GUID, ''))
		ORDER BY cdm_t.Fullname
	*/
		SELECT @n_cdm = COUNT(*) -- taxa with type only in CDM_EM2PESI
			FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
			[CDM_EM2PESI].[DBO].TAXON cdm_t
			ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '') INNER JOIN
			[CDM_EM2PESI].[DBO].TAXON cdm_pt ON cdm_t.TypeNameFk = cdm_pt.TaxonId
			WHERE NOT EXISTS
				(SELECT * FROM [EM2PESI].[DBO].TAXON
				WHERE IdInSource = cdm_pt.IdInSource AND ISNULL(GUID, '') = ISNULL(cdm_pt.GUID, ''))
		SET @str_n_cdm = Cast(@n_cdm AS NVARCHAR)
		IF @n_cdm > 0 PRINT ('WARNING: ' + @str_n_cdm + ' identical taxa have types in CDM_EM2PESI but not in EM2PESI')
	/*
		SELECT cdm_t.Fullname, cdm_pt.Fullname as Type_CDM_EM2PESI, 'in CDM_EM2PESI but not in EM2PESI'
		FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_t
		ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '') INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_pt ON cdm_t.TypeNameFk = cdm_pt.TaxonId
		WHERE NOT EXISTS
		(SELECT * FROM [EM2PESI].[DBO].TAXON
		WHERE IdInSource = cdm_pt.IdInSource AND ISNULL(GUID, '') = ISNULL(cdm_pt.GUID, ''))
	*/
		IF @n = 0 AND @n_cdm = 0 AND @n_bm = 0 BEGIN
			PRINT ('All identical taxa have the same type')
		END

-- QualityStatus
	SELECT @n = COUNT(*)
		FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_t
		ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
		WHERE ISNULL(bm_t.QualityStatusCache, '') <> ISNULL(cdm_t.QualityStatusCache, '')
	SET @str_n = Cast(@n AS NVARCHAR)
	IF @n = 0 BEGIN
		PRINT ('All identical taxa have the same quality status (QualityStatusCache)')
	END ELSE PRINT ('WARNING: ' + @str_n + ' identical taxa DO NOT have the same quality status (QualityStatusCache)')
	/*
	SELECT cdm_t.Fullname, cdm_t.QualityStatusCache as QualityStatus_CDM_EM2PESI, bm_t.QualityStatusCache as QualityStatus_EM2PESI
	FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
	[CDM_EM2PESI].[DBO].TAXON cdm_t
	ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
	WHERE ISNULL(bm_t.QualityStatusCache, '') <> ISNULL(cdm_t.QualityStatusCache, '')
	*/

-- Experts
	SELECT @n = COUNT(*)
		FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_t
		ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
		WHERE ISNULL(bm_t.ExpertName, '') <> ISNULL(cdm_t.ExpertName, '') OR
		ISNULL(bm_t.SpeciesExpertName, '') <> ISNULL(cdm_t.SpeciesExpertName, '')
	SET @str_n = Cast(@n AS NVARCHAR)
	IF @n = 0 BEGIN
		PRINT ('All identical taxa have the same experts (ExpertName, SpeciesExpertName)')
	END ELSE PRINT ('WARNING: ' + @str_n + ' identical taxa DO NOT have the same experts (ExpertName, SpeciesExpertName)')
	/*
	SELECT cdm_t.Fullname, cdm_t.ExpertName as ExpertName_CDM_EM2PESI, bm_t.ExpertName as ExpertName_EM2PESI,
	cdm_t.SpeciesExpertName as SpeciesExpertName_CDM_EM2PESI, bm_t.SpeciesExpertName as SpeciesExpertName_EM2PESI
	FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
	[CDM_EM2PESI].[DBO].TAXON cdm_t
	ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
	WHERE ISNULL(bm_t.ExpertName, '') <> ISNULL(cdm_t.ExpertName, '') OR
	ISNULL(bm_t.SpeciesExpertName, '') <> ISNULL(cdm_t.SpeciesExpertName, '')
	*/

-- Citation
	SELECT @n = COUNT(*)
		FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_t
		ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
		WHERE ISNULL(bm_t.CacheCitation, '') <> ISNULL(cdm_t.CacheCitation, '')
	SET @str_n = Cast(@n AS NVARCHAR)
	IF @n = 0 BEGIN
		PRINT ('All identical taxa have the same CacheCitation')
	END ELSE PRINT ('WARNING: ' + @str_n + ' identical taxa DO NOT have the same CacheCitation')
	/*
	SELECT cdm_t.Fullname, cdm_t.CacheCitation as CacheCitation_CDM_EM2PESI, bm_t.CacheCitation as CacheCitation_EM2PESI
	FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
	[CDM_EM2PESI].[DBO].TAXON cdm_t
	ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
	WHERE ISNULL(bm_t.CacheCitation, '') <> ISNULL(cdm_t.CacheCitation, '')
	*/

-- Original database
	SELECT @n = COUNT(*)
		FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_t
		ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
		WHERE ISNULL(bm_t.OriginalDB, '') <> ISNULL(cdm_t.OriginalDB, '')
	SET @str_n = Cast(@n AS NVARCHAR)
	IF @n = 0 BEGIN
		PRINT ('All identical taxa have the same OriginalDB')
	END ELSE PRINT ('WARNING: ' + @str_n + ' identical taxa DO NOT have the same OriginalDB')
	/*
	SELECT cdm_t.Fullname, cdm_t.OriginalDB as OriginalDB_CDM_EM2PESI, bm_t.OriginalDB as OriginalDB_EM2PESI
	FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
	[CDM_EM2PESI].[DBO].TAXON cdm_t
	ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
	WHERE ISNULL(bm_t.OriginalDB, '') <> ISNULL(cdm_t.OriginalDB, '')
	*/

-- Action
	SELECT @n = COUNT(*)
		FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_t
		ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
		WHERE ISNULL(bm_t.LastAction, '') <> ISNULL(cdm_t.LastAction, '') OR
		convert(smalldatetime, ISNULL(bm_t.LastActionDate, '00:00:00'))  <>
		convert(smalldatetime, ISNULL(cdm_t.LastActionDate, '00:00:00'))
	SET @str_n = Cast(@n AS NVARCHAR)
	IF @n = 0 BEGIN
		PRINT ('All identical taxa have the same last actions (LastAction, LastActionDate)')
	END ELSE PRINT ('WARNING: ' + @str_n + ' identical taxa DO NOT have the last actions (LastAction, LastActionDate)')
	/*
	SELECT cdm_t.Fullname, cdm_t.LastAction as LastAction_CDM_EM2PESI, bm_t.LastAction as LastAction_EM2PESI,
	cdm_t.LastActionDate as LastActionDate_CDM_EM2PESI, bm_t.LastActionDate as LastActionDate_EM2PESI
	FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
	[CDM_EM2PESI].[DBO].TAXON cdm_t
	ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
	WHERE ISNULL(bm_t.LastAction, '') <> ISNULL(cdm_t.LastAction, '') OR
	convert(smalldatetime, ISNULL(bm_t.LastActionDate, '00:00:00')) <>
	convert(smalldatetime, ISNULL(cdm_t.LastActionDate, '00:00:00'))
	*/


	
-- Parents

	PRINT ' '
	PRINT 'PARENTS'
	SELECT @n_bm = COUNT(*) FROM [EM2PESI].[DBO].TAXON WHERE ParentTaxonFk IS NOT NULL
	SELECT @n_cdm = COUNT(*) FROM [CDM_EM2PESI].[DBO].TAXON WHERE ParentTaxonFk IS NOT NULL
	SET @n = @n_bm - @n_cdm

	SET @str_n_bm = Cast(@n_bm AS NVARCHAR)
	SET @str_n_cdm = Cast(@n_cdm AS NVARCHAR)
	SET @str_n = Cast(@n AS NVARCHAR)

	IF @n = 0 BEGIN
		PRINT ('Both databases have the same number of taxa which have a parent = ' + @str_n_bm)
	END ELSE PRINT ('WARNING: Both databases DO NOT have the same number of taxa that have a parent, n_bm = ' + @str_n_bm + ' and n_cdm = '+ @str_n_cdm)

		SELECT @n = COUNT(*) -- in both databases parents exist but are different
			FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
			[CDM_EM2PESI].[DBO].TAXON cdm_t
			ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '') INNER JOIN
			[EM2PESI].[DBO].TAXON bm_pt ON bm_t.ParentTaxonFk = bm_pt.TaxonId INNER JOIN
			[CDM_EM2PESI].[DBO].TAXON cdm_pt ON cdm_t.ParentTaxonFk = cdm_pt.TaxonId
			WHERE bm_pt.IdInSource <> cdm_pt.IdInSource OR ISNULL(bm_pt.GUID, '') <> ISNULL(cdm_pt.GUID, '')
		SET @str_n = Cast(@n AS NVARCHAR)
		IF @n = 0 BEGIN
			PRINT ('All identical taxa that have parents have the same parent')
		END ELSE PRINT ('WARNING: ' + @str_n + ' identical taxa have different parents')
	/*
		SELECT  cdm_t.FullName as Child_CDM, cdm_pt.Fullname as Parent_CDM_EM2PESI, bm_t.FullName as child_sql,  bm_pt.Fullname as Parent_EM2PESI
		FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
			[CDM_EM2PESI].[DBO].TAXON cdm_t ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '') INNER JOIN
			[EM2PESI].[DBO].TAXON bm_pt ON bm_t.ParentTaxonFk = bm_pt.TaxonId INNER JOIN
		[	CDM_EM2PESI].[DBO].TAXON cdm_pt ON cdm_t.ParentTaxonFk = cdm_pt.TaxonId
		WHERE bm_pt.IdInSource <> cdm_pt.IdInSource OR ISNULL(bm_pt.GUID, '') <> ISNULL(cdm_pt.GUID, '')
	*/
		SELECT @n_bm = COUNT(*) -- taxa with parent only in EM2PESI
			FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
			[CDM_EM2PESI].[DBO].TAXON cdm_t
			ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '') INNER JOIN
			[EM2PESI].[DBO].TAXON bm_pt ON bm_t.ParentTaxonFk = bm_pt.TaxonId
			WHERE NOT EXISTS
				(SELECT * FROM [CDM_EM2PESI].[DBO].TAXON
				WHERE IdInSource = bm_pt.IdInSource AND ISNULL(GUID, '') = ISNULL(bm_pt.GUID, ''))
		SET @str_n_bm = Cast(@n_bm AS NVARCHAR)
		IF @n_bm > 0 BEGIN 
			PRINT ('WARNING: ' + @str_n_bm + ' identical taxa have parents in EM2PESI but not in CDM_EM2PESI')
		END ELSE PRINT ('All identical taxa that have parents in EM2PESI do have parents in CDM_EM2PESI')

	/*
		SELECT cdm_t.Fullname ChildName, bm_pt.Fullname as Parent_EM2PESI, 'in EM2PESI but not in CDM_EM2PESI'
		FROM [EM2PESI].[DBO].TAXON bm_t 
			INNER JOIN [EM2PESI].[DBO].TAXON bm_pt ON bm_t.ParentTaxonFk = bm_pt.TaxonId
			INNER JOIN [CDM_EM2PESI].[DBO].TAXON cdm_t ON ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '') 
		
		WHERE cdm_t.ParentTaxonFk  IS NULL AND NOT EXISTS 
		(SELECT * FROM [CDM_EM2PESI].[DBO].TAXON cdm_pt
		WHERE (cdm_pt.IdInSource = bm_pt.IdInSource OR cdm_pt.IdInSource IS NULL AND bm_pt.IdInSource IS NULL) 
				AND ISNULL(cdm_pt.GUID, '') = ISNULL(bm_pt.GUID, '')
				AND cdm_t.ParentTaxonFk = cdm_pt.TaxonId)
		ORDER BY cdm_t.Fullname

	*/
		SELECT @n_cdm = COUNT(*) -- taxa with parent only in CDM_EM2PESI
			FROM [EM2PESI].[DBO].TAXON bm_t INNER JOIN
			[CDM_EM2PESI].[DBO].TAXON cdm_t
			ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '') INNER JOIN
			[CDM_EM2PESI].[DBO].TAXON cdm_pt ON cdm_t.ParentTaxonFk = cdm_pt.TaxonId
			WHERE NOT EXISTS
				(SELECT * FROM [EM2PESI].[DBO].TAXON
				WHERE IdInSource = cdm_pt.IdInSource AND ISNULL(GUID, '') = ISNULL(cdm_pt.GUID, ''))
		SET @str_n_cdm = Cast(@n_cdm AS NVARCHAR)
		IF @n_cdm > 0 BEGIN 
			PRINT ('WARNING: ' + @str_n_cdm + ' identical taxa have parents in CDM_EM2PESI but not in EM2PESI')
		END ELSE PRINT('All identical taxa that have parents in CDM_EM2PESI do have parents in EM2PESI')
		
	/*
		SELECT bm_t.Fullname ChildName, cdm_pt.Fullname as Parent_CDM, 'in CDM_EM2PESI but not in EM2PESI'
		FROM [CDM_EM2PESI].[DBO].TAXON cdm_t 
			INNER JOIN [CDM_EM2PESI].[DBO].TAXON cdm_pt ON cdm_t.ParentTaxonFk = cdm_pt.TaxonId
			INNER JOIN [EM2PESI].[DBO].TAXON bm_t ON ISNULL(cdm_t.GUID, '') = ISNULL(bm_t.GUID, '') 
		
		WHERE bm_t.ParentTaxonFk  IS NULL AND NOT EXISTS 
		(SELECT * FROM [EM2PESI].[DBO].TAXON bm_pt
		WHERE (bm_pt.IdInSource = cdm_pt.IdInSource OR bm_pt.IdInSource IS NULL AND cdm_pt.IdInSource IS NULL) 
				AND ISNULL(bm_pt.GUID, '') = ISNULL(cdm_pt.GUID, '')
				AND bm_t.ParentTaxonFk = bm_pt.TaxonId)
		ORDER BY bm_t.Fullname
	*/
		IF @n = 0 AND @n_cdm = 0 AND @n_bm = 0 BEGIN
			PRINT ('All identical taxa have the same parent')
		END

-- TreeIndex
/* This is not checked. This field should be created by the PESI-Procedure recalculateallstoredpaths.
	Actually checking the parents of the same taxa is enough to ensure the compatibility of the taxonomical tree as a whole.
	See the Parent section
*/
	SELECT @n_bm = COUNT(*) FROM [EM2PESI].[DBO].TAXON WHERE TreeIndex IS NOT NULL
	SELECT @n_cdm = COUNT(*) FROM [CDM_EM2PESI].[DBO].TAXON WHERE TreeIndex IS NOT NULL
	SET @n = @n_bm - @n_cdm

	SET @str_n_bm = Cast(@n_bm AS NVARCHAR)
	SET @str_n_cdm = Cast(@n_cdm AS NVARCHAR)
	SET @str_n = Cast(@n AS NVARCHAR)

	IF @n = 0 BEGIN
		PRINT ('Both databases have the same number of taxa which have a tree index = ' + @str_n_bm)
	END ELSE PRINT ('WARNING: Both databases DO NOT have the same number of taxa that have a tree index, n_bm = ' + @str_n_bm + ' and n_cdm = '+ @str_n_cdm)
	
------------------------------------------- RelTaxon -------------------------------
	PRINT ' '
	PRINT 'RELATIONSHIP'
	
-- taxonomical relationships between taxa and nomenclatural relationships between names
	SELECT @n_bm = COUNT(*) FROM [EM2PESI].[DBO].RELTAXON
	SELECT @n_cdm = COUNT(*) FROM [CDM_EM2PESI].[DBO].RELTAXON
	SET @n = @n_bm - @n_cdm

	SET @str_n_bm = Cast(@n_bm AS NVARCHAR)
	SET @str_n_cdm = Cast(@n_cdm AS NVARCHAR)
	SET @str_n = Cast(@n AS NVARCHAR)

	IF @n = 0 BEGIN
		PRINT ('Both databases have the same number of relationship records = ' + @str_n_bm)
	END ELSE PRINT ('WARNING: Both databases DO NOT have the same number of relationship records, n_bm = ' + @str_n_bm + ' and n_cdm = '+ @str_n_cdm)

		SELECT @n_bm = COUNT(*) -- relationships only in EM2PESI
			FROM [EM2PESI].[DBO].RELTAXON bm_rt INNER JOIN
			[EM2PESI].[DBO].TAXON bm_t1 ON bm_rt.TaxonFk1 = bm_t1.TaxonId INNER JOIN
			[EM2PESI].[DBO].TAXON bm_t2 ON bm_rt.TaxonFk2 = bm_t2.TaxonId INNER JOIN
			[CDM_EM2PESI].[DBO].TAXON cdm_t1
			ON bm_t1.IdInSource = cdm_t1.IdInSource AND ISNULL(bm_t1.GUID, '') = ISNULL(cdm_t1.GUID, '') INNER JOIN
			[CDM_EM2PESI].[DBO].TAXON cdm_t2
			ON bm_t2.IdInSource = cdm_t2.IdInSource AND ISNULL(bm_t2.GUID, '') = ISNULL(cdm_t2.GUID, '')
			WHERE NOT EXISTS
			(SELECT * FROM [CDM_EM2PESI].[DBO].RELTAXON
			WHERE TaxonFk1 = cdm_t1.TaxonId AND TaxonFk2 = cdm_t2.TaxonId AND RelQualifierCache = bm_rt.RelQualifierCache)
		SET @str_n_bm = Cast(@n_bm AS NVARCHAR)
		IF @n_bm = 0 BEGIN
			PRINT ('All relationships in EM2PESI exist also in CDM_EM2PESI')
		END ELSE PRINT ('WARNING: ' + @str_n_bm + ' existing relationships for identical taxa in EM2PESI DO NOT exist in CDM_EM2PESI')
	/*
		SELECT bm_t1.Fullname, bm_rt.RelQualifierCache, bm_t2.Fullname, 'in EM2PESI but not in CDM_EM2PESI'
		FROM [EM2PESI].[DBO].RELTAXON bm_rt INNER JOIN
		[EM2PESI].[DBO].TAXON bm_t1 ON bm_rt.TaxonFk1 = bm_t1.TaxonId INNER JOIN
		[EM2PESI].[DBO].TAXON bm_t2 ON bm_rt.TaxonFk2 = bm_t2.TaxonId INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_t1
		ON bm_t1.IdInSource = cdm_t1.IdInSource AND ISNULL(bm_t1.GUID, '') = ISNULL(cdm_t1.GUID, '') INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_t2
		ON bm_t2.IdInSource = cdm_t2.IdInSource AND ISNULL(bm_t2.GUID, '') = ISNULL(cdm_t2.GUID, '')
		WHERE NOT EXISTS
		(SELECT * FROM [CDM_EM2PESI].[DBO].RELTAXON
		WHERE TaxonFk1 = cdm_t1.TaxonId AND TaxonFk2 = cdm_t2.TaxonId AND RelQualifierCache = bm_rt.RelQualifierCache)

		SELECT [RelTaxonQualifierFk]
		,[RelQualifierCache]
		, COUNT(*) as n
		FROM [CDM_EM2PESI].[dbo].[RelTaxon] rel INNER JOIN [CDM_EM2PESI].[dbo].[Taxon] t1 ON t1.TaxonId = rel.TaxonFk1
		INNER JOIN [CDM_EM2PESI].[dbo].[Taxon] t2 ON t2.TaxonId = rel.TaxonFk2
		GROUP BY RelTaxonQualifierFk, RelQualifierCache
		ORDER BY RelTaxonQualifierFk, COUNT(*) DESC

		SELECT [RelTaxonQualifierFk]
		,[RelQualifierCache]
		, COUNT(*) as n
		FROM [EM2PESI].[dbo].[RelTaxon]
		GROUP BY RelTaxonQualifierFk, RelQualifierCache
		ORDER BY RelTaxonQualifierFk, COUNT(*) DESC
		
		SELECT t1.QualifierId, t1.Qualifier, t1.n as n_cdm, t2.n as n_bm,  t1.n - t2.n as diff
		FROM (

			SELECT rtq.QualifierId , rtq.Qualifier,  COUNT(rel.RelTaxonId) as n
			FROM RelTaxonQualifier rtq LEFT OUTER JOIN [CDM_EM2PESI].[dbo].[RelTaxon] rel ON rel.RelTaxonQualifierFk = rtq.QualifierId 
				LEFT OUTER JOIN [CDM_EM2PESI].[dbo].[Taxon] t1 ON t1.TaxonId = rel.TaxonFk1 
				LEFT OUTER JOIN [CDM_EM2PESI].[dbo].[Taxon] t2 ON t2.TaxonId = rel.TaxonFk2
			GROUP BY QualifierId, Qualifier
		) t1 INNER JOIN  (
			SELECT rtq2.QualifierId , rtq2.Qualifier, COUNT(rel.RelTaxonId) as n
			FROM RelTaxonQualifier rtq2 LEFT OUTER JOIN [EM2PESI].[dbo].[RelTaxon] rel ON rel.RelTaxonQualifierFk = rtq2.QualifierId 
				LEFT OUTER JOIN [EM2PESI].[dbo].[Taxon] t1 ON t1.TaxonId = rel.TaxonFk1 
				LEFT OUTER JOIN [EM2PESI].[dbo].[Taxon] t2 ON t2.TaxonId = rel.TaxonFk2
				GROUP BY QualifierId, Qualifier
		) t2  ON t1.QualifierId  = t2.QualifierId AND t1.n <> t2.n
		ORDER BY t1.QualifierId DESC

	*/
		SELECT @n_cdm = COUNT(*) -- relationships only in CDM_EM2PESI
			FROM [CDM_EM2PESI].[DBO].RELTAXON cdm_rt INNER JOIN
			[CDM_EM2PESI].[DBO].TAXON cdm_t1 ON cdm_rt.TaxonFk1 = cdm_t1.TaxonId INNER JOIN
			[CDM_EM2PESI].[DBO].TAXON cdm_t2 ON cdm_rt.TaxonFk2 = cdm_t2.TaxonId INNER JOIN
			[EM2PESI].[DBO].TAXON bm_t1
			ON bm_t1.IdInSource = cdm_t1.IdInSource AND ISNULL(bm_t1.GUID, '') = ISNULL(cdm_t1.GUID, '') INNER JOIN
			[EM2PESI].[DBO].TAXON bm_t2
			ON bm_t2.IdInSource = cdm_t2.IdInSource AND ISNULL(bm_t2.GUID, '') = ISNULL(cdm_t2.GUID, '')
			WHERE NOT EXISTS
			(SELECT * FROM [EM2PESI].[DBO].RELTAXON
			WHERE TaxonFk1 = bm_t1.TaxonId AND TaxonFk2 = bm_t2.TaxonId AND RelQualifierCache = cdm_rt.RelQualifierCache)
		SET @str_n_cdm = Cast(@n_cdm AS NVARCHAR)
		IF @n_cdm = 0 BEGIN
			PRINT ('All relationships in CDM_EM2PESI exist also in EM2PESI')
		END ELSE PRINT ('WARNING: ' + @str_n_cdm + ' existing relationships for identical taxa in CDM_EM2PESI DO NOT exist in EM2PESI')
	/*
		SELECT cdm_t1.Fullname, cdm_rt.RelQualifierCache, cdm_t2.Fullname, 'in CDM_EM2PESI but not in CDM_EM2PESI'
		FROM [CDM_EM2PESI].[DBO].RELTAXON cdm_rt INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_t1 ON cdm_rt.TaxonFk1 = cdm_t1.TaxonId INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_t2 ON cdm_rt.TaxonFk2 = cdm_t2.TaxonId INNER JOIN
		[EM2PESI].[DBO].TAXON bm_t1
		ON bm_t1.IdInSource = cdm_t1.IdInSource AND ISNULL(bm_t1.GUID, '') = ISNULL(cdm_t1.GUID, '') INNER JOIN
		[EM2PESI].[DBO].TAXON bm_t2
		ON bm_t2.IdInSource = cdm_t2.IdInSource AND ISNULL(bm_t2.GUID, '') = ISNULL(cdm_t2.GUID, '')
		WHERE NOT EXISTS
		(SELECT * FROM [EM2PESI].[DBO].RELTAXON
		WHERE TaxonFk1 = bm_t1.TaxonId AND TaxonFk2 = bm_t2.TaxonId AND RelQualifierCache = cdm_rt.RelQualifierCache)
	*/
		IF @n_cdm = 0 AND @n_bm = 0 BEGIN
			PRINT ('All relationships are identical in both databases')
		END


------------------------------------------- CommonName -------------------------------
	PRINT ' '
	PRINT 'COMMON NAMES'

	SELECT @n_bm = COUNT(*) FROM [EM2PESI].[DBO].CommonName
	SELECT @n_cdm = COUNT(*) FROM [CDM_EM2PESI].[DBO].CommonName
	SET @n = @n_bm - @n_cdm

	SET @str_n_bm = Cast(@n_bm AS NVARCHAR)
	SET @str_n_cdm = Cast(@n_cdm AS NVARCHAR)
	SET @str_n = Cast(@n AS NVARCHAR)

	IF @n = 0 BEGIN
		PRINT ('Both databases have the same number of common names = ' + @str_n_bm)
	END ELSE PRINT ('WARNING: Both databases DO NOT have the same number of common names, n_bm = ' + @str_n_bm + ' and n_cdm = '+ @str_n_cdm)

		SELECT @n_bm = COUNT(*) -- common names only in EM2PESI
			FROM [EM2PESI].[DBO].CommonName bm_cn INNER JOIN
			[EM2PESI].[DBO].TAXON bm_t ON bm_cn.TaxonFk = bm_t.TaxonId INNER JOIN
			[CDM_EM2PESI].[DBO].TAXON cdm_t
			ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
			WHERE NOT EXISTS
			(SELECT * FROM [CDM_EM2PESI].[DBO].CommonName
			WHERE TaxonFk = cdm_t.TaxonId AND ISNULL(CommonName,'') = ISNULL(bm_cn.CommonName,'')
			AND ISNULL(LanguageCache,'') = ISNULL(bm_cn.LanguageCache,'')
			AND ISNULL(SourceNameCache,'') = ISNULL(bm_cn.SourceNameCache,'')
			AND ISNULL(SpeciesExpertName,'') = ISNULL(bm_cn.SpeciesExpertName,'')
			AND ISNULL(LastAction,'') = ISNULL(bm_cn.LastAction,'')
			AND ISNULL(LastActionDate,'00:00:00') = ISNULL(bm_cn.LastActionDate,'00:00:00')
			)
		SET @str_n_bm = Cast(@n_bm AS NVARCHAR)
		IF @n_bm = 0 BEGIN
			PRINT ('All common names in EM2PESI exist also in CDM_EM2PESI')
		END ELSE PRINT ('WARNING: ' + @str_n_bm + ' existing common names for identical taxa in EM2PESI DO NOT exist in CDM_EM2PESI')
	/*
		SELECT bm_t.Fullname, bm_cn.CommonName, bm_cn.LanguageCache,
		bm_cn.SourceNameCache, bm_cn.SpeciesExpertName, bm_cn.LastAction,
		bm_cn.LastActionDate, 'in EM2PESI but not in CDM_EM2PESI'
		FROM [EM2PESI].[DBO].CommonName bm_cn INNER JOIN
		[EM2PESI].[DBO].TAXON bm_t ON bm_cn.TaxonFk = bm_t.TaxonId INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_t
		ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
		WHERE NOT EXISTS
		(SELECT * FROM [CDM_EM2PESI].[DBO].CommonName
		WHERE TaxonFk = cdm_t.TaxonId AND ISNULL(CommonName,'') = ISNULL(bm_cn.CommonName,'')
		AND ISNULL(LanguageCache,'') = ISNULL(bm_cn.LanguageCache,'')
		AND ISNULL(SourceNameCache,'') = ISNULL(bm_cn.SourceNameCache,'')
		AND ISNULL(SpeciesExpertName,'') = ISNULL(bm_cn.SpeciesExpertName,'')
		AND ISNULL(LastAction,'') = ISNULL(bm_cn.LastAction,'')
		AND ISNULL(LastActionDate,'00:00:00') = ISNULL(bm_cn.LastActionDate,'00:00:00')
		)
	*/
		SELECT @n_cdm = COUNT(*) -- common names only in CDM_EM2PESI
			FROM [CDM_EM2PESI].[DBO].CommonName cdm_cn INNER JOIN
			[CDM_EM2PESI].[DBO].TAXON cdm_t ON cdm_cn.TaxonFk = cdm_t.TaxonId INNER JOIN
			[EM2PESI].[DBO].TAXON bm_t
			ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
			WHERE NOT EXISTS
			(SELECT * FROM [EM2PESI].[DBO].CommonName
			WHERE TaxonFk = bm_t.TaxonId AND ISNULL(CommonName,'') = ISNULL(cdm_cn.CommonName,'')
			AND ISNULL(LanguageCache,'') = ISNULL(cdm_cn.LanguageCache,'')
			AND ISNULL(SourceNameCache,'') = ISNULL(cdm_cn.SourceNameCache,'')
			AND ISNULL(SpeciesExpertName,'') = ISNULL(cdm_cn.SpeciesExpertName,'')
			AND ISNULL(LastAction,'') = ISNULL(cdm_cn.LastAction,'')
			AND ISNULL(LastActionDate,'00:00:00') = ISNULL(cdm_cn.LastActionDate,'00:00:00')
			)
		SET @str_n_cdm = Cast(@n_cdm AS NVARCHAR)
		IF @n_cdm = 0 BEGIN
			PRINT ('All common names in CDM_EM2PESI exist also in EM2PESI')
		END ELSE PRINT ('WARNING: ' + @str_n_cdm + ' existing common names for identical taxa in CDM_EM2PESI DO NOT exist in EM2PESI')
	/*
		SELECT bm_t.Fullname, cdm_cn.CommonName, cdm_cn.LanguageCache,
		cdm_cn.SourceNameCache, cdm_cn.SpeciesExpertName, cdm_cn.LastAction,
		cdm_cn.LastActionDate, 'in CDM_EM2PESI but not in EM2PESI'
		FROM [CDM_EM2PESI].[DBO].CommonName cdm_cn INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_t ON cdm_cn.TaxonFk = cdm_t.TaxonId INNER JOIN
		[EM2PESI].[DBO].TAXON bm_t
		ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
		WHERE NOT EXISTS
		(SELECT * FROM [EM2PESI].[DBO].CommonName
		WHERE TaxonFk = bm_t.TaxonId AND ISNULL(CommonName,'') = ISNULL(cdm_cn.CommonName,'')
		AND ISNULL(LanguageCache,'') = ISNULL(cdm_cn.LanguageCache,'')
		AND ISNULL(SourceNameCache,'') = ISNULL(cdm_cn.SourceNameCache,'')
		AND ISNULL(SpeciesExpertName,'') = ISNULL(cdm_cn.SpeciesExpertName,'')
		AND ISNULL(LastAction,'') = ISNULL(cdm_cn.LastAction,'')
		AND ISNULL(LastActionDate,'00:00:00') = ISNULL(cdm_cn.LastActionDate,'00:00:00')
		)
	*/
		IF @n_cdm = 0 AND @n_bm = 0 BEGIN
			PRINT ('All common names are identical in both databases')
		END

------------------------------------------- AdditionalTaxonSource -------------------------------
	PRINT ' '
	PRINT 'ADDITIONAL TAXON SOURCE'
	
	SELECT @n_bm = COUNT(*) FROM [EM2PESI].[DBO].AdditionalTaxonSource
	SELECT @n_cdm = COUNT(*) FROM [CDM_EM2PESI].[DBO].AdditionalTaxonSource
	SET @n = @n_bm - @n_cdm

	SET @str_n_bm = Cast(@n_bm AS NVARCHAR)
	SET @str_n_cdm = Cast(@n_cdm AS NVARCHAR)
	SET @str_n = Cast(@n AS NVARCHAR)

	IF @n = 0 BEGIN
		PRINT ('Both databases have the same number of additional sources = ' + @str_n_bm)
	END ELSE PRINT ('WARNING: Both databases DO NOT have the same number of additional sources, n_bm = ' + @str_n_bm + ' and n_cdm = '+ @str_n_cdm)

		SELECT @n_bm = COUNT(*) -- additional sources only in EM2PESI
			FROM [EM2PESI].[DBO].AdditionalTaxonSource bm_ats INNER JOIN
			[EM2PESI].[DBO].TAXON bm_t ON bm_ats.TaxonFk = bm_t.TaxonId INNER JOIN
			[CDM_EM2PESI].[DBO].TAXON cdm_t
			ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
			WHERE NOT EXISTS
			(SELECT * FROM [CDM_EM2PESI].[DBO].AdditionalTaxonSource
			WHERE TaxonFk = cdm_t.TaxonId 
			AND ISNULL(SourceUseCache,'') = ISNULL(bm_ats.SourceUseCache,'')
			AND ISNULL(SourceNameCache,'') = ISNULL(bm_ats.SourceNameCache,'')
			AND ISNULL(SourceDetail,'') = ISNULL(bm_ats.SourceDetail,'')
			)
		SET @str_n_bm = Cast(@n_bm AS NVARCHAR)
		IF @n_bm = 0 BEGIN
			PRINT ('All additional sources in EM2PESI exist also in CDM_EM2PESI')
		END ELSE PRINT ('WARNING: ' + @str_n_bm + ' existing additional sources for identical taxa in EM2PESI DO NOT exist in CDM_EM2PESI')
	/*
		SELECT bm_t.Fullname, bm_ats.SourceUseCache, bm_ats.SourceNameCache,
		bm_ats.SourceDetail, 'in EM2PESI but not in CDM_EM2PESI'
		FROM [EM2PESI].[DBO].AdditionalTaxonSource bm_ats INNER JOIN
		[EM2PESI].[DBO].TAXON bm_t ON bm_ats.TaxonFk = bm_t.TaxonId INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_t
		ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
		WHERE NOT EXISTS
		(SELECT * FROM [CDM_EM2PESI].[DBO].AdditionalTaxonSource
		WHERE TaxonFk = cdm_t.TaxonId 
		AND ISNULL(SourceUseCache,'') = ISNULL(bm_ats.SourceUseCache,'')
		AND ISNULL(SourceNameCache,'') = ISNULL(bm_ats.SourceNameCache,'')
		AND ISNULL(SourceDetail,'') = ISNULL(bm_ats.SourceDetail,'')
		)
		ORDER BY bm_t.Fullname, bm_ats.SourceUseCache, bm_ats.SourceNameCache
		
	*/
		SELECT @n_cdm = COUNT(*) -- additional sources only in CDM_EM2PESI
			FROM [CDM_EM2PESI].[DBO].AdditionalTaxonSource cdm_ats INNER JOIN
			[CDM_EM2PESI].[DBO].TAXON cdm_t ON cdm_ats.TaxonFk = cdm_t.TaxonId INNER JOIN
			[EM2PESI].[DBO].TAXON bm_t
			ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
			WHERE NOT EXISTS
			(SELECT * FROM [EM2PESI].[DBO].AdditionalTaxonSource
			WHERE TaxonFk = bm_t.TaxonId 
			AND ISNULL(SourceUseCache,'') = ISNULL(cdm_ats.SourceUseCache,'')
			AND ISNULL(SourceNameCache,'') = ISNULL(cdm_ats.SourceNameCache,'')
			AND ISNULL(SourceDetail,'') = ISNULL(cdm_ats.SourceDetail,'')
			)
		SET @str_n_cdm = Cast(@n_cdm AS NVARCHAR)
		IF @n_cdm = 0 BEGIN
			PRINT ('All additional sources in CDM_EM2PESI exist also in EM2PESI')
		END ELSE PRINT ('WARNING: ' + @str_n_cdm + ' existing additional sources for identical taxa in CDM_EM2PESI DO NOT exist in EM2PESI')
	/*
		SELECT bm_t.Fullname, cdm_ats.SourceUseCache, cdm_ats.SourceNameCache,
		cdm_ats.SourceDetail, 'in EM2PESI but not in CDM_EM2PESI'
		FROM [CDM_EM2PESI].[DBO].AdditionalTaxonSource cdm_ats INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_t ON cdm_ats.TaxonFk = cdm_t.TaxonId INNER JOIN
		[EM2PESI].[DBO].TAXON bm_t
		ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
		WHERE NOT EXISTS
		(SELECT * FROM [EM2PESI].[DBO].AdditionalTaxonSource
		WHERE TaxonFk = bm_t.TaxonId 
		AND ISNULL(SourceUseCache,'') = ISNULL(cdm_ats.SourceUseCache,'')
		AND ISNULL(SourceNameCache,'') = ISNULL(cdm_ats.SourceNameCache,'')
		AND ISNULL(SourceDetail,'') = ISNULL(cdm_ats.SourceDetail,'')
		)
		ORDER BY bm_t.Fullname, cdm_ats.SourceUseCache, cdm_ats.SourceNameCache
	*/
		IF @n_cdm = 0 AND @n_bm = 0 BEGIN
			PRINT ('All additional sources are identical in both databases')
		END

------------------------------ Image (currently exist only in ERMS) -------------------------------
	PRINT ' '
	PRINT 'IMAGES'
	
	SELECT @n_bm = COUNT(*) FROM [EM2PESI].[DBO].Image
	SELECT @n_cdm = COUNT(*) FROM [CDM_EM2PESI].[DBO].Image
	SET @n = @n_bm - @n_cdm

	SET @str_n_bm = Cast(@n_bm AS NVARCHAR)
	SET @str_n_cdm = Cast(@n_cdm AS NVARCHAR)
	SET @str_n = Cast(@n AS NVARCHAR)

	IF @n = 0 BEGIN
		PRINT ('Both databases have the same number of images = ' + @str_n_bm)
	END ELSE PRINT ('WARNING: Both databases DO NOT have the same number of images, n_bm = ' + @str_n_bm + ' and n_cdm = '+ @str_n_cdm)

		SELECT @n_bm = COUNT(*) -- images only in EM2PESI
			FROM [EM2PESI].[DBO].Image bm_i INNER JOIN
			[EM2PESI].[DBO].TAXON bm_t ON bm_i.TaxonFk = bm_t.TaxonId INNER JOIN
			[CDM_EM2PESI].[DBO].TAXON cdm_t
			ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
			WHERE NOT EXISTS
			(SELECT * FROM [CDM_EM2PESI].[DBO].Image
			WHERE TaxonFk = cdm_t.TaxonId AND ISNULL(img_thumb,'') = ISNULL(bm_i.img_thumb,'')
			AND ISNULL(img_url,'') = ISNULL(bm_i.img_url,'')
			)
		SET @str_n_bm = Cast(@n_bm AS NVARCHAR)
		IF @n_bm = 0 BEGIN
			PRINT ('All images in EM2PESI exist also in CDM_EM2PESI')
		END ELSE PRINT ('WARNING: ' + @str_n_bm + ' existing images for identical taxa in EM2PESI DO NOT exist in CDM_EM2PESI')
	/*
		SELECT bm_t.Fullname, bm_i.img_thumb, bm_i.img_url, 'in EM2PESI but not in CDM_EM2PESI'
		FROM [EM2PESI].[DBO].Image bm_i INNER JOIN
		[EM2PESI].[DBO].TAXON bm_t ON bm_i.TaxonFk = bm_t.TaxonId INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_t
		ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
		WHERE NOT EXISTS
		(SELECT * FROM [CDM_EM2PESI].[DBO].Image
		WHERE TaxonFk = cdm_t.TaxonId AND ISNULL(img_thumb,'') = ISNULL(bm_i.img_thumb,'')
		AND ISNULL(img_url,'') = ISNULL(bm_i.img_url,'')
		)
	*/
		SELECT @n_cdm = COUNT(*) -- images only in CDM_EM2PESI
			FROM [CDM_EM2PESI].[DBO].Image cdm_i INNER JOIN
			[CDM_EM2PESI].[DBO].TAXON cdm_t ON cdm_i.TaxonFk = cdm_t.TaxonId INNER JOIN
			[EM2PESI].[DBO].TAXON bm_t
			ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
			WHERE NOT EXISTS
			(SELECT * FROM [EM2PESI].[DBO].Image
			WHERE TaxonFk = bm_t.TaxonId AND ISNULL(img_thumb,'') = ISNULL(cdm_i.img_thumb,'')
			AND ISNULL(img_url,'') = ISNULL(cdm_i.img_url,'')
			)
		SET @str_n_cdm = Cast(@n_cdm AS NVARCHAR)
		IF @n_cdm = 0 BEGIN
			PRINT ('All images in CDM_EM2PESI exist also in EM2PESI')
		END ELSE PRINT ('WARNING: ' + @str_n_cdm + ' existing images for identical taxa in CDM_EM2PESI DO NOT exist in EM2PESI')
	/*
		SELECT bm_t.Fullname, cdm_i.img_thumb, cdm_i.img_url, 'in CDM_EM2PESI but not in EM2PESI'
		FROM [CDM_EM2PESI].[DBO].Image cdm_i INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_t ON cdm_i.TaxonFk = cdm_t.TaxonId INNER JOIN
		[EM2PESI].[DBO].TAXON bm_t
		ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
		WHERE NOT EXISTS
		(SELECT * FROM [EM2PESI].[DBO].Image
		WHERE TaxonFk = bm_t.TaxonId AND ISNULL(img_thumb,'') = ISNULL(cdm_i.img_thumb,'')
		AND ISNULL(img_url,'') = ISNULL(cdm_i.img_url,'')
		)
	*/
		IF @n_cdm = 0 AND @n_bm = 0 BEGIN
			PRINT ('All images are identical in both databases')
		END

------------------------------------------- Note -------------------------------
	PRINT ' '
	PRINT 'NOTES'

	SELECT @n_bm = COUNT(*) FROM [EM2PESI].[DBO].Note LEFT OUTER JOIN
		[EM2PESI].[DBO].NoteSource ON NoteFk = NoteId
	SELECT @n_cdm = COUNT(*) FROM [CDM_EM2PESI].[DBO].Note LEFT OUTER JOIN
		[CDM_EM2PESI].[DBO].NoteSource ON NoteFk = NoteId
	SET @n = @n_bm - @n_cdm

	SET @str_n_bm = Cast(@n_bm AS NVARCHAR)
	SET @str_n_cdm = Cast(@n_cdm AS NVARCHAR)
	SET @str_n = Cast(@n AS NVARCHAR)

	IF @n = 0 BEGIN
		PRINT ('Both databases have the same number of note * notesource results = ' + @str_n_bm)
	END ELSE PRINT ('WARNING: Both databases DO NOT have the same number of note * notesource results, n_bm = ' + @str_n_bm + ' and n_cdm = '+ @str_n_cdm)

		SELECT @n_bm = COUNT(*) -- note * notesource results only in EM2PESI
			FROM [EM2PESI].[DBO].Note bm_n INNER JOIN
			[EM2PESI].[DBO].TAXON bm_t ON bm_n.TaxonFk = bm_t.TaxonId LEFT OUTER JOIN
			[EM2PESI].[DBO].NoteSource bm_ns ON bm_ns.NoteFk = bm_n.NoteId INNER JOIN
			[CDM_EM2PESI].[DBO].TAXON cdm_t
			ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
			WHERE NOT EXISTS
			(SELECT * FROM [CDM_EM2PESI].[DBO].Note LEFT OUTER JOIN [CDM_EM2PESI].[DBO].NoteSource
			ON [CDM_EM2PESI].[DBO].Note.NoteId = [CDM_EM2PESI].[DBO].NoteSource.NoteFk
			WHERE TaxonFk = cdm_t.TaxonId 
			AND ISNULL(Note_1,'') = ISNULL(bm_n.Note_1,'')
			AND ISNULL(Note_2,'') = ISNULL(bm_n.Note_2,'')
			AND ISNULL(NoteCategoryCache,'') = ISNULL(bm_n.NoteCategoryCache,'')
			AND ISNULL(LanguageCache,'') = ISNULL(bm_n.LanguageCache,'')
			AND ISNULL(SpeciesExpertName,'') = ISNULL(bm_n.SpeciesExpertName,'')
			AND ISNULL(LastAction,'') = ISNULL(bm_n.LastAction,'')
			AND Left(ISNULL(LastActionDate,'00:00:00'),18) = Left(ISNULL(bm_n.LastActionDate,'00:00:00'),18)
			AND ISNULL(SourceNameCache,'') = ISNULL(bm_ns.SourceNameCache,'')
			AND ISNULL(SourceDetail,'') = ISNULL(bm_ns.SourceDetail,'')
			)
		SET @str_n_bm = Cast(@n_bm AS NVARCHAR)
		IF @n_bm = 0 BEGIN
			PRINT ('All note * notesource results in EM2PESI exist also in CDM_EM2PESI')
		END ELSE PRINT ('WARNING: ' + @str_n_bm + ' existing note * notesource results for identical taxa in EM2PESI DO NOT exist in CDM_EM2PESI')
	/*
		Typical Problems: 
		Duplicate entries during E+M SQL import for taxa using the same name object.
		Missing source info by E+M SQL import
		Last Action date automatically created (but wrong) by CDM import
		
		SELECT bm_t.Fullname, bm_n.Note_1, bm_n.Note_2, bm_n.NoteCategoryCache, bm_n.LanguageCache,
		bm_n.SpeciesExpertName, bm_n.LastAction, bm_n.LastActionDate, bm_ns.SourceNameCache, bm_ns.SourceDetail,
		'in EM2PESI but not in CDM_EM2PESI'
		FROM [EM2PESI].[DBO].Note bm_n INNER JOIN
		[EM2PESI].[DBO].TAXON bm_t ON bm_n.TaxonFk = bm_t.TaxonId LEFT OUTER JOIN
		[EM2PESI].[DBO].NoteSource bm_ns ON bm_ns.NoteFk = bm_n.NoteId INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_t
		ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
		WHERE NOT EXISTS
		(SELECT * FROM [CDM_EM2PESI].[DBO].Note LEFT OUTER JOIN [CDM_EM2PESI].[DBO].NoteSource
		ON [CDM_EM2PESI].[DBO].Note.NoteId = [CDM_EM2PESI].[DBO].NoteSource.NoteFk
		WHERE TaxonFk = cdm_t.TaxonId 
		AND ISNULL(Note_1,'') = ISNULL(bm_n.Note_1,'')
		AND ISNULL(Note_2,'') = ISNULL(bm_n.Note_2,'')
		AND ISNULL(NoteCategoryCache,'') = ISNULL(bm_n.NoteCategoryCache,'')
		AND ISNULL(LanguageCache,'') = ISNULL(bm_n.LanguageCache,'')
		AND ISNULL(SpeciesExpertName,'') = ISNULL(bm_n.SpeciesExpertName,'')
		AND ISNULL(LastAction,'') = ISNULL(bm_n.LastAction,'')
		AND Left(ISNULL(LastActionDate,'00:00:00'),18) = Left(ISNULL(bm_n.LastActionDate,'00:00:00'),18)
		AND ISNULL(SourceNameCache,'') = ISNULL(bm_ns.SourceNameCache,'')
		AND ISNULL(SourceDetail,'') = ISNULL(bm_ns.SourceDetail,'')
		)
		ORDER BY bm_n.NoteCategoryFk, bm_t.Fullname, bm_n.Note_1
	*/
		SELECT @n_cdm = COUNT(*) -- note * notesource results only in CDM_EM2PESI
			FROM [CDM_EM2PESI].[DBO].Note cdm_n INNER JOIN
			[CDM_EM2PESI].[DBO].TAXON cdm_t ON cdm_n.TaxonFk = cdm_t.TaxonId LEFT OUTER JOIN
			[CDM_EM2PESI].[DBO].NoteSource cdm_ns ON cdm_ns.NoteFk = cdm_n.NoteId INNER JOIN
			[EM2PESI].[DBO].TAXON bm_t
			ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
			WHERE NOT EXISTS
			(SELECT * FROM [EM2PESI].[DBO].Note LEFT OUTER JOIN [EM2PESI].[DBO].NoteSource
			ON [EM2PESI].[DBO].Note.NoteId = [EM2PESI].[DBO].NoteSource.NoteFk
			WHERE TaxonFk = bm_t.TaxonId 
			AND ISNULL(Note_1,'') = ISNULL(cdm_n.Note_1,'')
			AND ISNULL(Note_2,'') = ISNULL(cdm_n.Note_2,'')
			AND ISNULL(NoteCategoryCache,'') = ISNULL(cdm_n.NoteCategoryCache,'')
			AND ISNULL(LanguageCache,'') = ISNULL(cdm_n.LanguageCache,'')
			AND ISNULL(SpeciesExpertName,'') = ISNULL(cdm_n.SpeciesExpertName,'')
			AND ISNULL(LastAction,'') = ISNULL(cdm_n.LastAction,'')
			AND LEFT(ISNULL(LastActionDate,'00:00:00'),18) = LEFT(ISNULL(cdm_n.LastActionDate,'00:00:00'),18)
			AND ISNULL(SourceNameCache,'') = ISNULL(cdm_ns.SourceNameCache,'')
			AND ISNULL(SourceDetail,'') = ISNULL(cdm_ns.SourceDetail,'')
			)
		SET @str_n_cdm = Cast(@n_cdm AS NVARCHAR)
		IF @n_cdm = 0 BEGIN
			PRINT ('All note * notesource results in CDM_EM2PESI exist also in EM2PESI')
		END ELSE PRINT ('WARNING: ' + @str_n_cdm + ' existing note * notesource results for identical taxa in CDM_EM2PESI DO NOT exist in EM2PESI')
	/*
		SELECT bm_t.Fullname, cdm_n.Note_1, cdm_n.Note_2, cdm_n.NoteCategoryCache, cdm_n.LanguageCache,
		cdm_n.SpeciesExpertName, cdm_n.LastAction, cdm_n.LastActionDate, cdm_ns.SourceNameCache, cdm_ns.SourceDetail,
		'in CDM_EM2PESI but not in EM2PESI'
		FROM [CDM_EM2PESI].[DBO].Note cdm_n INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_t ON cdm_n.TaxonFk = cdm_t.TaxonId LEFT OUTER JOIN
		[CDM_EM2PESI].[DBO].NoteSource cdm_ns ON cdm_ns.NoteFk = cdm_n.NoteId INNER JOIN
		[EM2PESI].[DBO].TAXON bm_t
		ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
		WHERE NOT EXISTS
		(SELECT * FROM [EM2PESI].[DBO].Note LEFT OUTER JOIN [EM2PESI].[DBO].NoteSource
		ON [EM2PESI].[DBO].Note.NoteId = [EM2PESI].[DBO].NoteSource.NoteFk
		WHERE TaxonFk = bm_t.TaxonId 
		AND ISNULL(Note_1,'') = ISNULL(cdm_n.Note_1,'')
		AND ISNULL(Note_2,'') = ISNULL(cdm_n.Note_2,'')
		AND ISNULL(NoteCategoryCache,'') = ISNULL(cdm_n.NoteCategoryCache,'')
		AND ISNULL(LanguageCache,'') = ISNULL(cdm_n.LanguageCache,'')
		AND ISNULL(SpeciesExpertName,'') = ISNULL(cdm_n.SpeciesExpertName,'')
		AND ISNULL(LastAction,'') = ISNULL(cdm_n.LastAction,'')
		AND LEFT(ISNULL(LastActionDate,'00:00:00'),18) = LEFT(ISNULL(cdm_n.LastActionDate,'00:00:00'),18)
		AND ISNULL(SourceNameCache,'') = ISNULL(cdm_ns.SourceNameCache,'')
		AND ISNULL(SourceDetail,'') = ISNULL(cdm_ns.SourceDetail,'')
		)
		ORDER BY cdm_n.NoteCategoryFk, cdm_t.Fullname,cdm_n.Note_1
		
	*/
		IF @n_cdm = 0 AND @n_bm = 0 BEGIN
			PRINT ('All note * notesource results are identical in both databases')
		END

------------------------------------------- Occurrence -------------------------------
	PRINT ' '
	PRINT 'OCCURRENCES'
	
	SELECT @n_bm = COUNT(*) FROM [EM2PESI].[DBO].Occurrence LEFT OUTER JOIN
		[EM2PESI].[DBO].OccurrenceSource ON OccurrenceFk = OccurrenceId
	SELECT @n_cdm = COUNT(*) FROM [CDM_EM2PESI].[DBO].Occurrence LEFT OUTER JOIN
		[CDM_EM2PESI].[DBO].OccurrenceSource ON OccurrenceFk = OccurrenceId
	SET @n = @n_bm - @n_cdm

	SET @str_n_bm = Cast(@n_bm AS NVARCHAR)
	SET @str_n_cdm = Cast(@n_cdm AS NVARCHAR)
	SET @str_n = Cast(@n AS NVARCHAR)

	IF @n = 0 BEGIN
		PRINT ('Both databases have the same number of occurrence * occurrencesource results = ' + @str_n_bm)
	END ELSE PRINT ('WARNING: Both databases DO NOT have the same number of occurrence * occurrencesource results, n_bm = ' + @str_n_bm + ' and n_cdm = '+ @str_n_cdm)

		SELECT @n_bm = COUNT(*) -- occurrence * occurrencesource results only in EM2PESI
			FROM [EM2PESI].[DBO].Occurrence bm_o INNER JOIN
			[EM2PESI].[DBO].TAXON bm_t ON bm_o.TaxonFk = bm_t.TaxonId LEFT OUTER JOIN
			[EM2PESI].[DBO].OccurrenceSource bm_os ON bm_os.OccurrenceFk = bm_o.OccurrenceId INNER JOIN
			[CDM_EM2PESI].[DBO].TAXON cdm_t
			ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
			WHERE NOT EXISTS
			(SELECT * FROM [CDM_EM2PESI].[DBO].Occurrence LEFT OUTER JOIN [CDM_EM2PESI].[DBO].OccurrenceSource
			ON [CDM_EM2PESI].[DBO].Occurrence.OccurrenceId = [CDM_EM2PESI].[DBO].OccurrenceSource.OccurrenceFk
			WHERE TaxonFk = cdm_t.TaxonId 
				AND ISNULL(TaxonFullNameCache,'') = ISNULL(bm_o.TaxonFullNameCache,'')
				AND ISNULL(AreaNameCache,'') = ISNULL(bm_o.AreaNameCache,'')
				AND ISNULL(OccurrenceStatusCache,'') = ISNULL(bm_o.OccurrenceStatusCache,'')
				AND ISNULL(Notes,'') = ISNULL(bm_o.Notes,'')
				AND ISNULL(SpeciesExpertName,'') = ISNULL(bm_o.SpeciesExpertName,'')
				AND ISNULL(LastAction,'') = ISNULL(bm_o.LastAction,'')
				AND ISNULL(LastActionDate,'00:00:00') = ISNULL(bm_o.LastActionDate,'00:00:00')
				AND ISNULL(SourceNameCache,'') = ISNULL(bm_os.SourceNameCache,'')
				AND ISNULL(OldTaxonName,'') = ISNULL(bm_os.OldTaxonName,'')
			)
		SET @str_n_bm = Cast(@n_bm AS NVARCHAR)
		IF @n_bm = 0 BEGIN
			PRINT ('All occurrence * occurrencesource results in EM2PESI exist also in CDM_EM2PESI')
		END ELSE PRINT ('WARNING: ' + @str_n_bm + ' existing occurrence * occurrencesource results for identical taxa in EM2PESI DO NOT exist in CDM_EM2PESI')
	/*
		SELECT cdm_t.TaxonId as cdmTID, bm_t.TaxonId bmTID, bm_t.Fullname, bm_o.TaxonFullNameCache, bm_o.AreaNameCache, bm_o.OccurrenceStatusCache, bm_o.Notes,
		bm_o.SpeciesExpertName, bm_o.LastAction, bm_o.LastActionDate, bm_os.SourceNameCache, bm_os.OldTaxonName,
		'in EM2PESI but not in CDM_EM2PESI'
		FROM [EM2PESI].[DBO].Occurrence bm_o INNER JOIN
		[EM2PESI].[DBO].TAXON bm_t ON bm_o.TaxonFk = bm_t.TaxonId LEFT OUTER JOIN
		[EM2PESI].[DBO].OccurrenceSource bm_os ON bm_os.OccurrenceFk = bm_o.OccurrenceId INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_t
		ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
		WHERE NOT EXISTS
		(SELECT * FROM [CDM_EM2PESI].[DBO].Occurrence LEFT OUTER JOIN [CDM_EM2PESI].[DBO].OccurrenceSource
		ON [CDM_EM2PESI].[DBO].Occurrence.OccurrenceId = [CDM_EM2PESI].[DBO].OccurrenceSource.OccurrenceFk
		WHERE TaxonFk = cdm_t.TaxonId 
		AND ISNULL(TaxonFullNameCache,'') = ISNULL(bm_o.TaxonFullNameCache,'')
		AND ISNULL(AreaNameCache,'') = ISNULL(bm_o.AreaNameCache,'')
		AND ISNULL(OccurrenceStatusCache,'') = ISNULL(bm_o.OccurrenceStatusCache,'')
		AND ISNULL(Notes,'') = ISNULL(bm_o.Notes,'')
		AND ISNULL(SpeciesExpertName,'') = ISNULL(bm_o.SpeciesExpertName,'')
		AND ISNULL(LastAction,'') = ISNULL(bm_o.LastAction,'')
		AND ISNULL(LastActionDate,'00:00:00') = ISNULL(bm_o.LastActionDate,'00:00:00')
		AND ISNULL(SourceNameCache,'') = ISNULL(bm_os.SourceNameCache,'')
		AND ISNULL(OldTaxonName,'') = ISNULL(bm_os.OldTaxonName,'')
		)
		ORDER BY bm_t.Fullname, AreaNameCache 
	*/
		SELECT @n_cdm = COUNT(*) -- occurrence * occurrencesource results only in CDM_EM2PESI
			FROM [CDM_EM2PESI].[DBO].Occurrence cdm_o INNER JOIN
			[CDM_EM2PESI].[DBO].TAXON cdm_t ON cdm_o.TaxonFk = cdm_t.TaxonId LEFT OUTER JOIN
			[CDM_EM2PESI].[DBO].OccurrenceSource cdm_os ON cdm_os.OccurrenceFk = cdm_o.OccurrenceId INNER JOIN
			[EM2PESI].[DBO].TAXON bm_t
			ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
			WHERE NOT EXISTS
			(SELECT * FROM [EM2PESI].[DBO].Occurrence LEFT OUTER JOIN [EM2PESI].[DBO].OccurrenceSource
			ON [EM2PESI].[DBO].Occurrence.OccurrenceId = [EM2PESI].[DBO].OccurrenceSource.OccurrenceFk
			WHERE TaxonFk = bm_t.TaxonId 
				AND ISNULL(TaxonFullNameCache,'') = ISNULL(cdm_o.TaxonFullNameCache,'')
				AND ISNULL(AreaNameCache,'') = ISNULL(cdm_o.AreaNameCache,'')
				AND ISNULL(OccurrenceStatusCache,'') = ISNULL(cdm_o.OccurrenceStatusCache,'')
				AND ISNULL(Notes,'') = ISNULL(cdm_o.Notes,'')
				AND ISNULL(SpeciesExpertName,'') = ISNULL(cdm_o.SpeciesExpertName,'')
				AND ISNULL(LastAction,'') = ISNULL(cdm_o.LastAction,'')
				AND ISNULL(LastActionDate,'00:00:00') = ISNULL(cdm_o.LastActionDate,'00:00:00')
				AND ISNULL(SourceNameCache,'') = ISNULL(cdm_os.SourceNameCache,'')
				AND ISNULL(OldTaxonName,'') = ISNULL(cdm_os.OldTaxonName,'')
			)
		SET @str_n_cdm = Cast(@n_cdm AS NVARCHAR)
		IF @n_cdm = 0 BEGIN
			PRINT ('All occurrence * occurrencesource results in CDM_EM2PESI exist also in EM2PESI')
		END ELSE PRINT ('WARNING: ' + @str_n_cdm + ' existing occurrence * occurrencesource results for identical taxa in CDM_EM2PESI DO NOT exist in EM2PESI')
	/*
		SELECT cdm_t.TaxonId as cdmTID, bm_t.TaxonId bmTID, cdm_t.Fullname, cdm_o.TaxonFullNameCache, cdm_o.AreaNameCache, cdm_o.OccurrenceStatusCache, cdm_o.Notes,
		cdm_o.SpeciesExpertName, cdm_o.LastAction, cdm_o.LastActionDate, cdm_os.SourceNameCache, cdm_os.OldTaxonName,
		'in CDM_EM2PESI but not in EM2PESI'
		FROM [CDM_EM2PESI].[DBO].Occurrence cdm_o INNER JOIN
		[CDM_EM2PESI].[DBO].TAXON cdm_t ON cdm_o.TaxonFk = cdm_t.TaxonId LEFT OUTER JOIN
		[CDM_EM2PESI].[DBO].OccurrenceSource cdm_os ON cdm_os.OccurrenceFk = cdm_o.OccurrenceId INNER JOIN
		[EM2PESI].[DBO].TAXON bm_t
		ON bm_t.IdInSource = cdm_t.IdInSource AND ISNULL(bm_t.GUID, '') = ISNULL(cdm_t.GUID, '')
		WHERE NOT EXISTS
		(SELECT * FROM [EM2PESI].[DBO].Occurrence LEFT OUTER JOIN [EM2PESI].[DBO].OccurrenceSource
		ON [EM2PESI].[DBO].Occurrence.OccurrenceId = [EM2PESI].[DBO].OccurrenceSource.OccurrenceFk
		WHERE TaxonFk = bm_t.TaxonId 
		AND ISNULL(TaxonFullNameCache,'') = ISNULL(cdm_o.TaxonFullNameCache,'')
		AND ISNULL(AreaNameCache,'') = ISNULL(cdm_o.AreaNameCache,'')
		AND ISNULL(OccurrenceStatusCache,'') = ISNULL(cdm_o.OccurrenceStatusCache,'')
		AND ISNULL(Notes,'') = ISNULL(cdm_o.Notes,'')
		AND ISNULL(SpeciesExpertName,'') = ISNULL(cdm_o.SpeciesExpertName,'')
		AND ISNULL(LastAction,'') = ISNULL(cdm_o.LastAction,'')
		AND ISNULL(LastActionDate,'00:00:00') = ISNULL(cdm_o.LastActionDate,'00:00:00')
		AND ISNULL(SourceNameCache,'') = ISNULL(cdm_os.SourceNameCache,'')
		AND ISNULL(OldTaxonName,'') = ISNULL(cdm_os.OldTaxonName,'')
		)
		ORDER BY bm_t.Fullname, AreaNameCache 
	*/
		IF @n_cdm = 0 AND @n_bm = 0 BEGIN
			PRINT ('All occurrence * occurrencesource results are identical in both databases')
		END


	PRINT ('End of check')


END