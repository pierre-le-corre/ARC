package fr.insee.arc.core.service.thread;

import java.sql.Connection;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.service.ApiNormageService;
import fr.insee.arc.core.service.engine.normage.NormageEngine;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.Sleep;
import fr.insee.arc.core.util.StaticLoggerDispatcher;


/**
 * ThreadNormageService
 *
 *      1- créer la table des données à traiter dans le module</br>
 *      2- calcul de la norme, validité, periodicité sur chaque ligne de la table de donnée</br>
 *      3- déterminer pour chaque fichier si le normage s'est bien déroulé et marquer sa norme, sa validité et sa périodicité</br>
 *      4- créer les tables OK et KO; marquer les info de normage(norme, validité, périodicité) sur chaque ligne de donnée</br>
 *      5- transformation de table de donnée; mise à plat du fichier; suppression et relation</br>
 *      6- mettre à jour le nombre d'enregistrement par fichier après sa transformation</br>
 *      7- sortir les données du module vers l'application</br>
 *
 * @author Manuel SOULIER
 *
 */
public class ThreadNormageService extends ApiNormageService implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(ThreadNormageService.class);

    public int indice ;
    public Thread t = null;
    
    protected String tableNormageDataTemp;
    protected String tableNormagePilTemp;

    protected String tableNormageOKTemp;
    protected String tableNormageKOTemp;
    
    protected String tableNormageOK;
    protected String tableNormageKO;

    protected String structure;

    public ThreadNormageService(Connection connexion, int currentIndice, ApiNormageService theApi) {
        
        this.indice = currentIndice;
        this.idSource = theApi.getTabIdSource().get(ID_SOURCE).get(indice);
        this.connexion = connexion;
        
        try {
            this.connexion.setClientInfo("ApplicationName", "Normage fichier "+idSource);
        } catch (SQLClientInfoException e) {
        	LOGGER.error(e);
        }

        // tables du thread
        
        this.tableNormageDataTemp = FormatSQL.temporaryTableName("normage_data_temp");
        this.tableNormagePilTemp = FormatSQL.temporaryTableName("normage_pil_Temp");
        
        this.tableNormageOKTemp = FormatSQL.temporaryTableName("ok_Temp");
        this.tableNormageKOTemp = FormatSQL.temporaryTableName("ko_Temp");       
        
        this.tableNormageOK = globalTableName(theApi.getEnvExecution(), theApi.getCurrentPhase(), TraitementEtat.OK.toString());
        this.tableNormageKO = globalTableName(theApi.getEnvExecution(), theApi.getCurrentPhase(), TraitementEtat.KO.toString());

        // tables héritées
        this.setTableNormageRegle(theApi.getTableNormageRegle());
        this.setTableControleRegle(theApi.getTableControleRegle());
        this.setTableFiltrageRegle(theApi.getTableFiltrageRegle());
        this.setTableMappingRegle(theApi.getTableMappingRegle());
        
        this.setTablePil(theApi.getTablePil());
        this.setTablePilTemp(theApi.getTablePilTemp());
        this.setPreviousPhase(theApi.getPreviousPhase());
        this.setCurrentPhase(theApi.getCurrentPhase());
        this.setNbEnr(theApi.getNbEnr());
        this.setTablePrevious(theApi.getTablePrevious());
        this.setTabIdSource(theApi.getTabIdSource());
        this.setTableNorme(theApi.getTableNorme());
        this.setTableNormageRegle(theApi.getTableNormageRegle());
        this.setEnvExecution(theApi.getEnvExecution());
        this.setParamBatch(theApi.getParamBatch());

    }

    public void start() {
        StaticLoggerDispatcher.debug("Starting ThreadNormageService", LOGGER);
        if (t == null) {
            t = new Thread(this, indice + "");
            t.start();
        }
    }

    @Override
    public void run() {
        try {

            // créer la table des données à traiter dans le module
            creerTableTravail();

            // transformation de table de donnée; mise à plat du fichier; suppression et relation
            jointureBlocXML();
            
            // sortir les données du module vers l'application
            insertionFinale();

        } catch (Exception e) {
            StaticLoggerDispatcher.error(e, LOGGER);
	    try {
		this.repriseSurErreur(this.connexion, this.getCurrentPhase(), this.tablePil, this.idSource, e,
			"aucuneTableADroper");
	    } catch (SQLException e2) {
            StaticLoggerDispatcher.error(e2, LOGGER);
	    }
            Sleep.sleep(PREVENT_ERROR_SPAM_DELAY);
        }
    }


    /**
     * Créer la table de travail du normage Contient les donnée d'un seul id source. Cela est du au fait que le type composite varie d'un id
     * source à l'autre,
     * 
     * @throws SQLException
     */
    private void creerTableTravail() throws SQLException {
        StaticLoggerDispatcher.info("Créer les tables images", LOGGER);
        // Créer la table image de la phase précédente (ajouter les colonnes qu'il faut)
        StringBuilder bloc1 = new StringBuilder("DISCARD TEMP;");
        
        // créer la table de pilotage relative au thread
        bloc1.append(createTablePilotageIdSource(this.tablePilTemp, this.tableNormagePilTemp, this.idSource));
        bloc1.append(createTableTravailIdSource(this.getTablePrevious(),this.tableNormageDataTemp, this.idSource));

        StaticLoggerDispatcher.debug("requete créer table travail" + bloc1, LOGGER);

       
        //On met que le normage s'est bien passé
        bloc1.append("\n UPDATE "+this.tableNormagePilTemp);
        bloc1.append("\n\t SET etat_traitement = '{"+TraitementEtat.OK+"}'");
        bloc1.append("\n\t , phase_traitement = '"+this.currentPhase+"'");
        bloc1.append("\n\t WHERE id_source='"+this.idSource+"';");
        
        bloc1.append(this.createTableTravail("", this.tableNormageDataTemp, this.tableNormageKOTemp, this.tableNormagePilTemp, TraitementEtat.KO.toString()));
                
        UtilitaireDao.get(poolName).executeBlock(this.getConnexion(), bloc1);

    }

    /**
     * Réaliser la jointure entre les blocs XML pour mettre les fichier à plat
     * Pour chaque fichier, on retravaille la requete de jointure obtenue en phase de chargement :
     * 1- en supprimant les blocs définis "à supprimer" par les regles utilisateurs du normage
     * 2- en ajoutant des conditions de jointures relationnelles entre 2 rubriques défini par les règle utilisateur de type "relation" dans les regles de normage
     *
     * Fait sur une maintenance urgente après réception des fichiers lot2 en moins de 2j ...
     * La méthode devrait etre refactor (pour séparer "deletion" et "relation")
     * La réécriture de la requete selon les règles utilisateurs devrait être moins adhérente à la structure de la requete issu du chargement
     * (trop dépendant des mot clés ou saut de ligne pour l'instant)
     * @throws Exception
     *
     */
    private void jointureBlocXML() throws Exception {

        StaticLoggerDispatcher.info("jointureBlocXML()", LOGGER);

        // on parcours les fichiers pour effectuer la requete de jointure avec les regles de normage
        // c'est une des requete la plus couteuse de l'application (reconstitution de la structure plate à partir du modele xml)
        // pour chaque fichier, on va executer sa requete


        		// récupérer les caractéristiques du fichier
			    HashMap<String, ArrayList<String>> pil = getBean(this.connexion,getNormeAttributes(this.idSource, tableNormagePilTemp));

			    // récupéreration des règles relative au fichier pour la phase courante
			    HashMap<String,ArrayList<String>> regle = getBean(this.connexion,getRegles(this.tableNormageRegle, this.tableNormagePilTemp));
			    
			    
		        // récupéreration des rubriques utilisées dans règles relative au fichier pour l'ensemble des phases
		        
			    HashMap<String,ArrayList<String>> rubriqueUtiliseeDansRegles=null;
			    
			    if (paramBatch!=null)
			    {
				    String tableTmpRubriqueDansregles="TMP_RUBRIQUE_DANS_REGLES";
			        UtilitaireDao.get("arc").executeImmediate(
			        		this.connexion,
			        		"\n DROP TABLE IF EXISTS "+tableTmpRubriqueDansregles+"; "
			        		+ "\n CREATE TEMPORARY TABLE "+tableTmpRubriqueDansregles+" AS "
			        		+ getAllRubriquesInRegles(this.tableNormagePilTemp, this.tableNormageRegle, this.tableControleRegle, this.tableFiltrageRegle, this.tableMappingRegle)
			        		);
				    
				    rubriqueUtiliseeDansRegles = getBean(this.connexion,getRegles(tableTmpRubriqueDansregles, this.tableNormagePilTemp));
			    }
		    
			    NormageEngine n=new NormageEngine(this.connexion , pil, regle, rubriqueUtiliseeDansRegles, this.tableNormageDataTemp, this.tableNormageOKTemp, this.paramBatch);
			    n.execute();
			    
			    this.structure=n.structure;

    }
    
    
 

	/**
	 * Remplace les UNION ALL par des inserts
	 * @param jointure
	 * @return
	 */

    /**
     * On sort les données des tables temporaires du module vers : - les tables définitives du normage (normage_ok et normage_ko) de
     * l'application - la vraie table de pilotage - la table buffer
     *
     * IMPORTANT : les ajouts ou mise à jours de données sur les tables de l'application doivent avoir lieu dans un même bloc de transaction
     * (ACID)
     * 
     * @throws Exception
     *
     */
    private void insertionFinale() throws Exception {
    	
    	updateNbEnr(this.tableNormagePilTemp, this.tableNormageOKTemp, this.structure);
    	
    	// promote the application user account to full right
    	switchToFullRightRole();
    	
    	String tableIdSourceOK=tableOfIdSource(this.tableNormageOK ,this.idSource);
        createTableInherit(connexion, this.tableNormageOKTemp, tableIdSourceOK);
        String tableIdSourceKO=tableOfIdSource(this.tableNormageKO ,this.idSource);
        createTableInherit(connexion, this.tableNormageKOTemp, tableIdSourceKO);
        
        StringBuilder requete = new StringBuilder();
        
        if (paramBatch == null) {
        	requete.append(FormatSQL.tryQuery("alter table "+tableIdSourceOK+" inherit "+ this.tableNormageOK + "_todo;"));
            requete.append(FormatSQL.tryQuery("alter table "+tableIdSourceOK+" inherit "+ this.tableNormageOK +";"));
            requete.append(FormatSQL.tryQuery("alter table "+tableIdSourceKO+" inherit "+ this.tableNormageKO +";"));
        }
        else
        {
            requete.append(FormatSQL.tryQuery("alter table "+tableIdSourceOK+" inherit "+ this.tableNormageOK + "_todo;"));
            requete.append(FormatSQL.tryQuery("DROP TABLE IF EXISTS "+tableIdSourceKO+";"));
//            requete.append(FormatSQL.tryQuery("alter table "+tableIdSourceKO+" inherit "+ this.tableNormageKO +";"));
        }
        
        requete.append(this.marquageFinal(this.tablePil, this.tableNormagePilTemp));
        UtilitaireDao.get("arc").executeBlock(connexion, requete);

        
    }

    public Connection getConnexion() {
        return connexion;
    }

    public void setConnexion(Connection connexion) {
        this.connexion = connexion;
    }

    public Thread getT() {
        return t;
    }

    public void setT(Thread t) {
        this.t = t;
    }
    

}


