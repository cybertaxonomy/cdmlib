
-- **** reference system: google earth ************
SELECT ( @refSysVocId := id) as vocId FROM TermVocabulary WHERE uuid = 'ec6376e5-0c9c-4f5c-848b-b288e6c17a86';

INSERT INTO DefinedTermBase (DTYPE, id, uuid, created, protectedtitlecache, titleCache, orderindex, defaultcolor, vocabulary_id) 
SELECT 'ReferenceSystem' ,  (@defTermId := max(id)+1)  as maxId , '1bb67042-2814-4b09-9e76-c8c1e68aa281', '2010-06-01 10:15:00', b'0', 'Google Earth', null, null, @refSysVocId
FROM DefinedTermBase ;

-- language english
SELECT ( @langId := id) as langId FROM DefinedTermBase WHERE uuid = 'e9f8cdb7-6819-44e8-95d3-e2d0690c3523';

-- representation
INSERT INTO Representation (id, created, uuid, text, abbreviatedlabel, label, language_id) 
SELECT  ( @repId := max(id)+1 ) AS maxId ,'2010-06-01 18:49:07','fadb1730-9936-44e7-8911-884a84662b08', 'Google Earth','Google','Google Earth', @langId
FROM Representation;
;

 -- defTerm <-> representation
INSERT INTO DefinedTermBase_Representation (DefinedTermBase_id, representations_id) 
VALUES (@defTermId,@repId);