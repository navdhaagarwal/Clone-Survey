--First drop the view.As a side effect this would drop the user proviledges too, if any.
IF EXISTS (SELECT TABLE_NAME FROM INFORMATION_SCHEMA.VIEWS
        WHERE TABLE_NAME = 'user_org_view')
    DROP VIEW user_org_view;

CREATE  VIEW user_org_view AS SELECT usr.id as user_id , usr.password_expiration_date as passwordExpiryDate, usr.user_status as userstatus, usr.username as username,usr.uuid as useruuid, usr.created_by_uri as created_by_uri,org.id as org_id ,org.name as orgname ,usrProfile.full_name as full_name, uobm.is_primary_branch from users usr LEFT OUTER JOIN user_org_branch_mapping uobm ON usr.id=uobm.associated_user LEFT OUTER JOIN  organization org ON org.id = uobm.organization_branch LEFT OUTER JOIN user_profile usrProfile ON usr.id=usrProfile.associated_user;