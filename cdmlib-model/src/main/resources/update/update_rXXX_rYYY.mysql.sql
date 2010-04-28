-- #################### SCHEMA #######################################################


-- change symmetric to symmetrical to be compatible with PostGreSQL
 ALTER TABLE `DefinedTermBase` CHANGE `symmetric` `symmetrical` bit(1) NULL  ;
 
-- new attribute fullReference in ReferenceBase
-- ALTER TABLE `Reference` ADD `fullreference_id` INT(11) NULL COMMENT 'added by CDM schema version 2.4 updater' AFTER `school_id` ;
  -- set foreign key
-- ALTER TABLE `Reference` ADD CONSTRAINT FKA81607BA22D949E FOREIGN KEY (fullreference_id) REFERENCES `Reference` ;
 
-- new attribute abbreviatedReference in ReferenceBase 
-- ALTER TABLE `Reference` ADD `abbreviatedreference_id` INT(11) NULL COMMENT 'added by CDM schema version 2.4 updater' AFTER `fullreference_id` ;
--   -- set foreign key
-- ALTER TABLE `Reference` ADD CONSTRAINT FKA215077A21D842A FOREIGN KEY (abbreviatedreference_id) REFERENCES `Reference` ;




-- ##################### TERMS ########################################################

--absence term max orderindex
SELECT (@maxAbsenceOrderIndex := max(orderindex)) AS b FROM DefinedTermBase WHERE DTYPE = 'AbsenceTerm';

--native reported in error
SELECT (@presenceOrderIndex := orderindex) AS a FROM DefinedTermBase WHERE uuid = '4ba212ef-041e-418d-9d43-2ebb191b61d8';
UPDATE DefinedTermBase SET uuid = '61cee840-801e-41d8-bead-015ad866c2f1', DTYPE = 'AbsenceTerm', vocabulary_id = 18, orderindex = @maxAbsenceOrderIndex + 1 WHERE uuid = '4ba212ef-041e-418d-9d43-2ebb191b61d8';
UPDATE DefinedTermBase SET orderindex = orderindex -1 WHERE DTYPE = 'PresenceTerm' AND orderindex > @presenceOrderIndex ;


--introduced reported in error
SELECT (@presenceOrderIndex := orderindex) AS a FROM DefinedTermBase WHERE uuid = '826239f7-45b7-42b5-857c-c1f852cfad6b';
UPDATE DefinedTermBase SET uuid = 'aeec2947-2700-4623-8e32-9e3a430569d1', DTYPE = 'AbsenceTerm', vocabulary_id = 18, orderindex = @maxAbsenceOrderIndex + 2 WHERE uuid = '826239f7-45b7-42b5-857c-c1f852cfad6b';
UPDATE DefinedTermBase SET orderindex = orderindex -1 WHERE DTYPE = 'PresenceTerm' AND orderindex > @presenceOrderIndex ;


--cultivated reported in error
SELECT (@presenceOrderIndex := orderindex) AS a FROM DefinedTermBase WHERE uuid = 'b47f1679-0d0c-4ea7-a2e4-80709ea791c6';
UPDATE DefinedTermBase SET uuid = '9d4d3431-177a-4abe-8e4b-1558573169d6', DTYPE = 'AbsenceTerm', vocabulary_id = 18, orderindex = @maxAbsenceOrderIndex + 3 WHERE uuid = 'b47f1679-0d0c-4ea7-a2e4-80709ea791c6';
UPDATE DefinedTermBase SET orderindex = orderindex -1 WHERE DTYPE = 'PresenceTerm' AND orderindex > @presenceOrderIndex ;


-- **** doubtfully present ************
UPDATE DefinedTermBase SET orderindex = orderindex + 1 WHERE DTYPE = 'PresenceTerm' AND orderindex > 1 ; 

INSERT INTO DefinedTermBase (DTYPE, id, uuid, created, protectedtitlecache, titleCache, orderindex, defaultcolor, vocabulary_id) 
SELECT 'PresenceTerm' ,  (@defTermId := max(id)+1)  as maxId , '85a60279-a4c2-4f53-bc57-466028a4b3db', '2010-04-01 10:15:00', b'0', 'class eu.etaxonomy.cdm.model.description.PresenceTerm: 75a60279-a4c2-4f53-bc57-466028a4b3db',2, '8dd320', 17
FROM DefinedTermBase ;

  --language
SELECT ( @langId := id) as langId FROM DefinedTermBase WHERE uuid = 'e9f8cdb7-6819-44e8-95d3-e2d0690c3523';

  --representation
INSERT INTO Representation (id, created, uuid, text, abbreviatedlabel, label, language_id) 
SELECT  ( @repId := max(id)+1 ) AS maxId ,'2010-04-01 18:49:07','6453ae2f-5aed-4055-880d-44a86da2bbcc', 'present: doubtfully present','pd','doubtfully present', @langId
FROM Representation;
;
  -- defTerm <-> representation
INSERT INTO DefinedTermBase_Representation (DefinedTermBase_id, representations_id) 
VALUES (@defTermId,@repId);

-- insert new vocabulary NameFeature----
 INSERT INTO TermVocabulary (DTYPE, id, uuid, protectedTitleCache, titleCache, termsourceuri) 
 VALUES ('TermVocabulary',16, 'a7ca3eef-4092-49e1-beec-ed5096193e5e', FALSE, 'class eu.etaxonomy.cdm.model.common.TermVocabulary: a7ca3eef-4092-49e1-beec-ed5096193e5e', 'eu.etaxonomy.cdm.model.description.Feature');

 -- change the vocabulary id for "Protologue" and "Additional Publication"
 UPDATE DefinedTermBase SET vocabulary_id = 16 WHERE uuid = 'cb2eab09-6d9d-4e43-8ad2-873f23400930' or uuid = '7f1fd111-fc52-49f0-9e75-d0097f576b2d'  ;
 
 -- change the text of Protolog to Protologue --
  UPDATE Representation SET text = 'Protologue', label = 'Protologue' WHERE text like 'Protol%';
