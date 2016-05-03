
-- ##################### Insert admin if not existing ########################################################


INSERT INTO useraccount (id, uuid, accountnonexpired, accountnonlocked, credentialsnonexpired, enabled, password, username) SELECT (Select max(id)+1 from useraccount), '2b78fd58-1179-4e93-a8cb-ff5d2ba50e07', 1, 1, 1, 1, '6d54445d1b1cdc44e668a1e07ee4ab4a', 'admin2' FROM useraccount where (select count(*) from useraccount where username like 'admin')=0 ;


-- ##################### granted authorities ########################################################

Insert into grantedauthorityimpl (id,uuid, authority) VALUES (1,'889f9961-8d0f-41a9-95ec-59905b3941bf', 'USER.Edit');
Insert into grantedauthorityimpl (id,uuid, authority) VALUES (2,'841a1711-20f1-4209-82df-7944ad2050da', 'USER.Create');
Insert into grantedauthorityimpl (id,uuid, authority) VALUES (3,'bb9e2547-1e28-45fd-8c35-d1ceffbfcb36', 'USER.Delete');
Insert into grantedauthorityimpl (id,uuid, authority) VALUES (4,'8a61c102-4643-4e81-a3b6-c40d60d2ba99', 'USER.Admin');

-- ##################### add granted authorities for admin ########################################################

Insert into useraccount_grantedauthorityimpl (User_id, grantedauthorities_id) VALUES ((SELECT id from useraccount where username like 'admin'), 4);
Insert into useraccount_grantedauthorityimpl (User_id, grantedauthorities_id) VALUES ((SELECT id from useraccount where username like 'admin'), 3);
Insert into useraccount_grantedauthorityimpl (User_id, grantedauthorities_id) VALUES ((SELECT id from useraccount where username like 'admin'), 2);
Insert into useraccount_grantedauthorityimpl (User_id, grantedauthorities_id) VALUES ((SELECT id from useraccount where username like 'admin'), 1);


