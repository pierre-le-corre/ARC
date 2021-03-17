package fr.insee.arc.core.service.thread;

import java.sql.Connection;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.model.JeuDeRegle;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.model.TraitementRapport;
import fr.insee.arc.core.service.ApiControleService;
import fr.insee.arc.core.service.ApiService;
import fr.insee.arc.core.service.engine.controle.ServiceJeuDeRegle;
import fr.insee.arc.core.service.engine.controle.ServiceRequeteSqlRegle;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.Sleep;

/**
 * Comme pour le normage et le filtrage, on parallélise en controlant chaque fichier dans des threads séparés.
 * @author S4LWO8
 *
 */
public class ThreadControleService extends ApiControleService implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(ThreadControleService.class);

    private int indice;

    public String tableControleDataTemp;
    protected String tableControlePilTemp;
    public String tableTempControleFoo;
    protected String tableOutOkTemp="tableOutOkTemp";
    protected String tableOutKoTemp="tableOutKoTemp";
    String tableOutOk;
    String tableOutKo;

    public ServiceJeuDeRegle sjdr;

    public JeuDeRegle jdr;
    
    public String structure;

    public ThreadControleService(Connection connexion, int currentIndice, ApiControleService theApi) {

        this.indice = currentIndice;
        this.setEnvExecution(theApi.getEnvExecution());
        this.idSource = theApi.getTabIdSource().get(ID_SOURCE).get(indice);
        this.connexion = connexion;
        try {
            this.connexion.setClientInfo("ApplicationName", "Controle fichier "+idSource);
        } catch (SQLClientInfoException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        this.setTablePil(theApi.getTablePil());
        this.tablePilTemp = theApi.getTablePilTemp();

        this.setPreviousPhase(theApi.getPreviousPhase());
        this.setCurrentPhase(theApi.getCurrentPhase());

        this.setNbEnr(theApi.getNbEnr());

        this.setTablePrevious(theApi.getTablePrevious());
        this.setTabIdSource(theApi.getTabIdSource());

        this.setTableNorme(theApi.getTableNorme());
        this.setTableNormageRegle(theApi.getTableNormageRegle());

        this.setParamBatch(theApi.getParamBatch());

        this.setTableJeuDeRegle(theApi.getTableJeuDeRegle());
        this.setTableControleRegle(theApi.getTableControleRegle());

        
        this.sjdr = new ServiceJeuDeRegle(theApi.getTableControleRegle());
        this.jdr =new JeuDeRegle();

        
        this.setTableSeuil(theApi.getTableSeuil());

        // Nom des tables temporaires      
        this.tableControleDataTemp = FormatSQL.temporaryTableName("controle_data_temp");
        this.tableControlePilTemp= FormatSQL.temporaryTableName("controle_pil_temp");
        this.tableTempControleFoo = FormatSQL.temporaryTableName("controle_foo_temp");
        
        // tables finales
        this.tableOutOk = dbEnv(this.getEnvExecution()) + this.getCurrentPhase() + "_" + TraitementEtat.OK;
        this.tableOutKo = dbEnv(this.getEnvExecution()) + this.getCurrentPhase() + "_" + TraitementEtat.KO;

    }

    @Override
    public void run() {
        try {
            
            preparation();

            execute();          

            calculSeuilControle();
            
            insertionFinale();
            
        } catch (Exception e) {
			StaticLoggerDispatcher.error(e,LOGGER);
			e.printStackTrace();
		    try {
				this.repriseSurErreur(this.connexion, this.getCurrentPhase(), this.tablePil, this.idSource, e,
					"aucuneTableADroper");
			    } catch (SQLException e2) {
					StaticLoggerDispatcher.error(e2,LOGGER);
			    }
		    Sleep.sleep(PREVENT_ERROR_SPAM_DELAY);
        }
    }

    public void start() {
        StaticLoggerDispatcher.debug("Starting ThreadContrôleService", LOGGER);
        if (t == null) {
            t = new Thread(this, indice + "");
            t.start();
        }
    }

    /**
     * Préparation des données et implémentation des jeux de règles utiles
     *
     * @param this.connexion
     *
     * @param tableIn
     *            la table issue du chargement-normage
     *
     * @param env
     *            l'environnement d'execution
     * @param tableControle
     *            la table temporaire à controler
     * @param tablePilTemp
     *            la table temporaire listant les fichiers en cours de traitement
     * @param tableJeuDeRegle
     *            la table des jeux de règles
     * @param tableRegleC
     *            la table des règles de controles
     * @throws SQLException
     */
    public void preparation() throws SQLException {
        StaticLoggerDispatcher.info("** preparation **", LOGGER);

        StringBuilder blocPrep = new StringBuilder();

        // Marquage du jeux de règles appliqué
        StaticLoggerDispatcher.info("Récupération des rubrique de la table ", LOGGER);

        // fabrication de la table de pilotage controle lié au thread
        blocPrep.append("DISCARD TEMP;");
        blocPrep.append(createTablePilotageIdSource(this.tablePilTemp, this.tableControlePilTemp, this.idSource));

        // Marquage du jeux de règles appliqué
        StaticLoggerDispatcher.info("Marquage du jeux de règles appliqué ", LOGGER);
        blocPrep.append(marqueJeuDeRegleApplique(this.tableControlePilTemp, TraitementEtat.OK.toString()));
        
        // Fabrication de la table de controle temporaire
        StaticLoggerDispatcher.info("Fabrication de la table de controle temporaire ", LOGGER);
        blocPrep.append(createTableTravailIdSource(this.getTablePrevious(),this.tableControleDataTemp, this.idSource, "'"+ServiceRequeteSqlRegle.RECORD_WITH_NOERROR+"'::text collate \"C\" as controle, null::text[] collate \"C\" as brokenrules"));

        UtilitaireDao.get("arc").executeBlock(this.connexion, blocPrep);

        // Récupération des Jeux de règles associés
        this.sjdr.fillRegleControle(this.connexion, jdr, this.getTableControleRegle(), this.tableControleDataTemp);
        this.structure=UtilitaireDao.get("arc").getString(this.connexion, new PreparedStatementBuilder("SELECT jointure FROM "+this.tableControlePilTemp));
    }

    
    /**
     * Méthode pour controler une table
     *
     * @param connexion
     *
     * @param tableControle
     *            la table à controler
     *
     * @throws SQLException
     */
    public void execute() throws Exception {
        StaticLoggerDispatcher.info("** execute CONTROLE sur la table : " + this.tableControleDataTemp + " **", LOGGER);

        this.sjdr.executeJeuDeRegle(this.connexion, jdr, this.tableControleDataTemp, this.structure);

    }

    /**
     * Méthode à passer après les controles
     *
     * @param connexion
     *
     * @param tableIn
     *            la table temporaire avec les marquage du controle
     * @param tableOutOk
     *            la table permanente sur laquelle on ajoute les bons enregistrements de tableIn
     * @param tableOutKo
     *            la table permanente sur laquelle on ajoute les mauvais enregistrements de tableIn
     * @param tablePil
     *            la table de pilotage des fichiers
     * @param tableSeuil
     *            la table des seuils
     * @throws SQLException
     */
    public void calculSeuilControle() throws Exception {
        StaticLoggerDispatcher.info("finControle", LOGGER);


        StringBuilder blocFin = new StringBuilder();
        // Creation des tables temporaires ok et ko
        StaticLoggerDispatcher.info("Creation des tables temporaires ok et ko", LOGGER);
        blocFin.append(FormatSQL.dropTable(tableOutOkTemp));
        blocFin.append(FormatSQL.dropTable(tableOutKoTemp));

        // Execution à mi parcours du bloc de requete afin que les tables tempo soit bien créées
        // ensuite dans le java on s'appuie sur le dessin de ces tables pour ecrire du SQL
        blocFin.append(ApiService.creationTableResultat(this.tableControleDataTemp, tableOutOkTemp));
        blocFin.append(ApiService.creationTableResultat(this.tableControleDataTemp, tableOutKoTemp));

        // Marquage des résultat du control dans la table de pilotage
        StaticLoggerDispatcher.info("Marquage dans la table de pilotage", LOGGER);
        blocFin.append(marquagePilotage());
        
        // insert in OK when
        // etat traitement in OK or OK,KO
        // AND records which have no error or errors that can be kept
        StaticLoggerDispatcher.info("Insertion dans OK", LOGGER);
        blocFin.append(ajoutTableControle(this.tableControleDataTemp, tableOutOkTemp, this.tableControlePilTemp, "etat_traitement in ('{"+TraitementEtat.OK+"}','{"+TraitementEtat.OK+","+TraitementEtat.KO+"}') ",
                "controle in ('"+ServiceRequeteSqlRegle.RECORD_WITH_NOERROR+"','"+ServiceRequeteSqlRegle.RECORD_WITH_ERROR_TO_KEEP+"') AND "));

        // insert in OK when
        // etat traitement in KO
        // OR records which have errors that must be excluded
        StaticLoggerDispatcher.info("Insertion dans KO", LOGGER);
        blocFin.append(ajoutTableControle(this.tableControleDataTemp, tableOutKoTemp, this.tableControlePilTemp, "etat_traitement ='{"+TraitementEtat.KO+"}' "
        		, "controle='"+ServiceRequeteSqlRegle.RECORD_WITH_ERROR_TO_EXCLUDE+"' OR "));

        UtilitaireDao.get("arc").executeBlock(this.connexion, blocFin);

    }

    /**
     * Insertion dans les vraies tables
     * @throws Exception
     */
    private void insertionFinale() throws Exception {

	// promote the application user account to full right
    switchToFullRightRole();
    	
    // Créer les tables héritées
    String tableIdSourceOK=tableOfIdSource(tableOutOk ,this.idSource);
    createTableInherit(connexion, tableOutOkTemp, tableIdSourceOK);
    String tableIdSourceKO=tableOfIdSource(tableOutKo ,this.idSource);
    createTableInherit(connexion, tableOutKoTemp, tableIdSourceKO);
    
    StringBuilder requete = new StringBuilder();
    
    if (paramBatch == null) {
    	requete.append(FormatSQL.tryQuery("alter table "+tableIdSourceOK+" inherit "+ tableOutOk + "_todo;"));
        requete.append(FormatSQL.tryQuery("alter table "+tableIdSourceOK+" inherit "+ tableOutOk +";"));
        requete.append(FormatSQL.tryQuery("alter table "+tableIdSourceKO+" inherit "+ tableOutKo +";"));
    }
    else
    {
        requete.append(FormatSQL.tryQuery("alter table "+tableIdSourceOK+" inherit "+ tableOutOk + "_todo;"));
        requete.append(FormatSQL.tryQuery("DROP TABLE IF EXISTS "+tableIdSourceKO+";"));
    }
    
    requete.append(this.marquageFinal(this.tablePil, this.tableControlePilTemp));
    
    requete.append(FormatSQL.dropTable(tableOutOkTemp).toString());
    requete.append(FormatSQL.dropTable(tableOutKoTemp).toString());
    requete.append(FormatSQL.dropTable(this.tableControleDataTemp).toString());
    requete.append("\n DISCARD SEQUENCES; DISCARD TEMP;");
    
    UtilitaireDao.get("arc").executeBlock(this.connexion, requete);
    }
    
    
    
    /**
     * Marque les résultats des contrôle dans la table de pilotage
     * @return
     */
    public String marquagePilotage() {
        StringBuilder blocFin = new StringBuilder();
        blocFin.append("\n UPDATE "+this.tableControlePilTemp+" ");
        blocFin.append("\n SET etat_traitement= ");
        blocFin.append("\n case ");
        blocFin.append("\n when exists (select from "+ServiceRequeteSqlRegle.TABLE_TEMP_META+" where blocking) then '{"+TraitementEtat.KO+"}'::text[] ");
        blocFin.append("\n when exists (select from "+ServiceRequeteSqlRegle.TABLE_TEMP_META+" where controle='"+ServiceRequeteSqlRegle.RECORD_WITH_ERROR_TO_EXCLUDE+"') then '{"+TraitementEtat.OK+","+TraitementEtat.KO+"}'::text[] "); 
        blocFin.append("\n else '{OK}'::text[] ");
        blocFin.append("\n end ");
        blocFin.append("\n , rapport='Control failed on : '||(select array_agg(brokenrules||case when blocking then ' (blocking rules)' else '' end||case when controle='"+ServiceRequeteSqlRegle.RECORD_WITH_ERROR_TO_EXCLUDE+"' then ' (exclusion rules)' else '' end)::text from "+ServiceRequeteSqlRegle.TABLE_TEMP_META+") ");
        blocFin.append("\n WHERE exists (select from "+ServiceRequeteSqlRegle.TABLE_TEMP_META+") ");
        blocFin.append(";");
        return blocFin.toString();
    }


    /**
     * Insertion des données d'une table dans une autre avec un critère de sélection
     *
     * @param listColTableIn
     *
     * @param phase
     *
     * @param tableIn
     *            la table des données à insérer
     * @param tableOut
     *            la table réceptacle
     * @param tableControlePilTemp
     *            la table de pilotage des fichiers
     * @param etatNull
     *            pour sélectionner certains fichiers
     * @param condEnregistrement
     *            la condition pour filtrer la recopie
     * @return
     */
    public String ajoutTableControle(String tableIn, String tableOut, String tableControlePilTemp, String condFichier,
            String condEnregistrement) {

        StringBuilder requete = new StringBuilder();
        requete.append("\n INSERT INTO " + tableOut + " ");
        requete.append("\n SELECT * ");
        requete.append("\n FROM " + tableIn + " a ");
        requete.append("\n WHERE " + condEnregistrement + " ");
        requete.append("\n EXISTS (select 1 from  " + tableControlePilTemp + " b where "+ condFichier + ") ");
        requete.append(";");
        return requete.toString();
    }

    // Getter et Setter
    public ServiceJeuDeRegle getSjdr() {
        return this.sjdr;
    }

    public void setSjdr(ServiceJeuDeRegle sjdr) {
        this.sjdr = sjdr;
    }

    @Override
    public Thread getT() {
        return t;
    }

    public void setT(Thread t) {
        this.t = t;
    }

    @Override
    public Connection getConnexion() {
        return connexion;
    }

    public void setConnexion(Connection connexion) {
        this.connexion = connexion;
    }

}
