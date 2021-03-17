-- sandbox creation
CREATE SCHEMA IF NOT EXISTS {{envExecution}} AUTHORIZATION current_user; 

-- grant full right to application user and read write for arc_restricted
-- TODO : secure arc_restricted to table level ??
REVOKE ALL ON SCHEMA {{envExecution}} FROM public; 
GRANT ALL ON SCHEMA {{envExecution}} TO current_user;

do $$ begin
if ('{{userRestricted}}'!='') then 
	execute 'GRANT USAGE ON SCHEMA {{envExecution}} TO {{userRestricted}}; GRANT SELECT on ALL TABLES IN schema {{envExecution}} TO {{userRestricted}}; ALTER DEFAULT privileges IN SCHEMA {{envExecution}} GRANT SELECT ON TABLES to {{userRestricted}};';
end if;
exception when others then end;
$$;


-- pilotage tables and view
CREATE TABLE IF NOT EXISTS {{envExecution}}.pilotage_fichier_t (date_entree text COLLATE pg_catalog."C") WITH (autovacuum_enabled = false, toast.autovacuum_enabled = false);

CREATE TABLE IF NOT EXISTS {{envExecution}}.pilotage_fichier (id_source text COLLATE pg_catalog."C",  id_norme text COLLATE pg_catalog."C",  validite text COLLATE pg_catalog."C",  periodicite text COLLATE pg_catalog."C",  phase_traitement text COLLATE pg_catalog."C",  etat_traitement text[] COLLATE pg_catalog."C",  date_traitement timestamp without time zone,  rapport text COLLATE pg_catalog."C",  taux_ko numeric,  nb_enr integer,  nb_essais integer,  etape integer,  validite_inf date,  validite_sup date,  version text COLLATE pg_catalog."C",  date_entree text,  container text COLLATE pg_catalog."C",  v_container text COLLATE pg_catalog."C",  o_container text COLLATE pg_catalog."C",  to_delete text COLLATE pg_catalog."C",  client text[],  date_client timestamp without time zone[],  jointure text, generation_composite text COLLATE pg_catalog."C") WITH (autovacuum_enabled = false, toast.autovacuum_enabled = false);

CREATE TABLE IF NOT EXISTS {{envExecution}}.pilotage_archive (  entrepot text COLLATE pg_catalog."C",  nom_archive text COLLATE pg_catalog."C") WITH (autovacuum_enabled = false, toast.autovacuum_enabled = false);


