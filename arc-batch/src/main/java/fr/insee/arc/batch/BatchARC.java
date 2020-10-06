package fr.insee.arc.batch;

import java.io.File;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

import fr.insee.arc.batch.unitaryLauncher.ChargerBatch;
import fr.insee.arc.batch.unitaryLauncher.ControlerBatch;
import fr.insee.arc.batch.unitaryLauncher.FiltrerBatch;
import fr.insee.arc.batch.unitaryLauncher.InitialiserBatch;
import fr.insee.arc.batch.unitaryLauncher.MapperBatch;
import fr.insee.arc.batch.unitaryLauncher.NormerBatch;
import fr.insee.arc.batch.unitaryLauncher.RecevoirBatch;
import fr.insee.arc.core.model.ServiceReporting;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.ApiService;
import fr.insee.arc.core.util.BDParameters;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;

/**
 * Classe lanceur de l'application Accueil Reception Contrôle
 * 07/08/2015
 * Version pour les tests de performance et pré-production
 * @author Manu
 * 
 */
public class BatchARC {
	private static final Logger LOGGER = LogManager.getLogger(BatchARC.class);
	static HashMap<String, String> mapParam = new HashMap<>();

	/**
	 * variable dateInitialisation
	 * si vide (ou si date du jour+1 depassé à 20h),  je lance initialisation et j'initialise dateInitialisation à la nouvelle date du jour
	 * puis une fois terminé, je lancent la boucle des batchs
	 * si date du jour+1 depassé a 20h,
	 * - j'indique aux autre batchs de s'arreter
	 * - une fois arretés, je met tempo à la date du jour
	 * - je lance initialisation
	 * etc.
	 */
	private static String version="ARC 11.1.1 10/02/2020";
	
	@Autowired
	PropertiesHandler properties;
	
	// mode production (boucle sur les paquets d'enveloppes)
	private static boolean production=true;
	
	// keepInDatabase = est-ce qu'on garde les données intermédiaire en base ou est-ce qu'on les export sous forme de fichiers dans le repertoire export ?
	// false en production 
	private static boolean keepInDatabase= Boolean.parseBoolean(BDParameters.getString(null, "LanceurARC.keepInDatabase","false"));
	
	// pour le batch en cours, l'ensemble des enveloppes traitées ne peut pas excéder une certaine taille
	protected static int tailleMaxReceptionEnMb=BDParameters.getInt(null, "LanceurARC.tailleMaxReceptionEnMb",10);

	// Maximum number of files to load
	protected static int maxFilesToLoad=BDParameters.getInt(null, "LanceurARC.maxFilesToLoad",101);

	// Maximum number of files processed in each phase iteration
	protected static int maxFilesPerPhase=BDParameters.getInt(null, "LanceurARC.maxFilesPerPhase",1000000);

	// retard permis entre chaque phase : si une phase a deltaStepAllowed coups d'avance
	// elle attend que la suivant est terminée avant de reprendre
	private static int deltaStepAllowed=BDParameters.getInt(null, "LanceurARC.deltaStepAllowed",10000);

	// fréquence à laquelle les phases sont démarrées
	private static int poolingDelay=BDParameters.getInt(null, "LanceurARC.poolingDelay",1000);

	// heure d'initalisation en production
	private static int HEURE_INITIALISATION_PRODUCTION=BDParameters.getInt(null, "ApiService.HEURE_INITIALISATION_PRODUCTION",22);
	
	// interval entre chaque initialisation en nb de jours
	private static Integer INTERVAL_JOUR_INITIALISATION = BDParameters.getInt(null, "LanceurARC.INTERVAL_JOUR_INITIALISATION",7);
	

	/**
	 * dodo
	 * @param duree en ms
	 */
	public static void sleep(int duree)
	{

		 try {
			Thread.sleep(duree);
		} catch (InterruptedException ex) {
			LoggerHelper.errorGenTextAsComment(BatchARC.class, "sleep()", LOGGER, ex);
		}
	}

	public static class DateUtil
	{
	    public static Date addDays(Date date, int days)
	    {
	        Calendar cal = Calendar.getInstance();
	        cal.setTime(date);
	        cal.add(Calendar.DATE, days); //minus number would decrement the days
	        return cal.getTime();
	    }
	}



	public static class InitialiserThread extends Thread {
		public ServiceReporting report=new ServiceReporting();

		  @Override
		  public void run() {
			  InitialiserBatch c=new InitialiserBatch(mapParam.get("env"), mapParam.get("envExecution"), mapParam.get("repertoire") , tailleMaxReceptionEnMb+"", keepInDatabase?null:mapParam.get("numlot"));
			  c.execute();
			  this.report=c.report;
		  }
		}

	public static class RecevoirThread extends Thread {
		public ServiceReporting report=new ServiceReporting();
		  @Override
		  public void run() {
			RecevoirBatch c=new RecevoirBatch(mapParam.get("env"), mapParam.get("envExecution"), mapParam.get("repertoire") , tailleMaxReceptionEnMb+"", keepInDatabase?null:mapParam.get("numlot"));
			c.execute();
			this.report=c.report;
		  }
		}

	public static class ChargerThread extends Thread {
		@Override
		  public void run() {
			ChargerBatch c=new ChargerBatch(mapParam.get("env"), mapParam.get("envExecution"), mapParam.get("repertoire") , maxFilesToLoad+"", keepInDatabase?null:mapParam.get("numlot"));
			c.execute();
		  }
		}

	public static class NormerThread extends Thread {
		  @Override
		  public void run() {
			  NormerBatch c=new NormerBatch(mapParam.get("env"), mapParam.get("envExecution"), mapParam.get("repertoire") , maxFilesPerPhase+"", keepInDatabase?null:mapParam.get("numlot"));
			  c.execute();
		  }
		}

	public static class ControlerThread extends Thread {
		  @Override
		  public void run() {
			  ControlerBatch c=new ControlerBatch(mapParam.get("env"), mapParam.get("envExecution"), mapParam.get("repertoire") , maxFilesPerPhase+"", keepInDatabase?null:mapParam.get("numlot"));
			  c.execute();
		  }
		}

	public static class FiltrerThread extends Thread {
		  @Override
		  public void run() {
			  FiltrerBatch c=new FiltrerBatch(mapParam.get("env"), mapParam.get("envExecution"), mapParam.get("repertoire") , maxFilesPerPhase+"", keepInDatabase?null:mapParam.get("numlot"));
			  c.execute();
		  }
		}


	public static class MapperThread extends Thread {
		  @Override
		  public void run() {
			  MapperBatch c= new MapperBatch(mapParam.get("env"), mapParam.get("envExecution"), mapParam.get("repertoire") , maxFilesPerPhase+"", keepInDatabase?null:mapParam.get("numlot"));
			  c.execute();
		  }
		}
	
	public static void message(String msg)
	{
		System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+"  "+msg);
	}

	/**
	 * Lanceur MAIN arc
	 * @param args
	 */
	public void execute(String[] args) {

		boolean fichierRestant=false;

		message ("Main");
		
		do {
		
		message("Batch ARC "+version);
		
		try{
		
		String env =null;
		String envExecution = null;
		
		// either we take env and envExecution from database or properties
		// default is from properties
		if (Boolean.parseBoolean(BDParameters.getString(null, "LanceurARC.envFromDatabase","false")))
		{
			env=BDParameters.getString(null, "LanceurARC.env","arc.ihm");
			envExecution=BDParameters.getString(null, "LanceurARC.envExecution","arc.prod");
		}	
		else
		{
			env= properties.getBatchArcEnvironment();
			envExecution = properties.getBatchExecutionEnvironment();		
		}
		
		envExecution=envExecution.replace(".", "_");
		
		mapParam.put("env", env);
		mapParam.put("envExecution", envExecution);
		
		String repertoire = properties.getBatchParametersDirectory();
		mapParam.put("repertoire", repertoire);


		creerTablePilotageBatch();

		InitialiserThread initialiser=new InitialiserThread();
		RecevoirThread recevoir=new RecevoirThread();
		ChargerThread charger=new ChargerThread();
		NormerThread normer=new NormerThread();
		ControlerThread controler=new ControlerThread();
		FiltrerThread filtrer=new FiltrerThread();
		MapperThread mapper=new MapperThread();
//		VacuumThread vacuum=new VacuumThread(); 

		
		// opération de maintenance
		message("Maintenance pilotage");
		ApiService.maintenancePilotage(null, envExecution, "freeze");
		message("Fin de Maintenance pilotage");
		
		message("Déplacements de fichiers");

		boolean productionOn=productionOn();

		if (productionOn())
		{
		// on vide les repertoires de chargement OK, KO, ENCOURS
		effacerRepertoireChargement(repertoire, envExecution);
		
		
		// des archives n'ont elles pas été traitées jusqu'au bout ?
		ArrayList<String> aBouger= new GenericBean(UtilitaireDao.get("arc").executeRequest(null,"select distinct container from "+envExecution+".pilotage_fichier where etape=1")).mapContent().get("container");
		
		boolean dejaEnCours=(aBouger!=null);

		// si oui, on essaie de recopier les archives dans chargement OK
		if (aBouger!=null)
		{
			
			for (String container:aBouger)
			{
				String entrepotContainer=ManipString.substringBeforeFirst(container, "_");
				String originalContainer=ManipString.substringAfterFirst(container, "_");
				
				File fIn=new File(repertoire + envExecution.replace(".", "_").toUpperCase()
						+ File.separator + TraitementPhase.RECEPTION+ "_"+ entrepotContainer + "_ARCHIVE"
						+ File.separator + originalContainer);
				File fOut=new File(repertoire + envExecution.replace(".", "_").toUpperCase() 
						+ File.separator + TraitementPhase.RECEPTION+ "_"+TraitementEtat.OK
						+ File.separator + container);

				try{
				Files.copy(fIn.toPath(), fOut.toPath());
				}
				catch(Exception e) 
				{
					message("Fichier "+container+" inexistant dans Archive");
				}
				
				
			}
			
		}
		
		message("Fin des déplacements de fichiers");

		
		do
		{
			
		message("Traitement Début");

		// plage d'initialisation

		 DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd:HH");
		 DateFormat dateFormat2 = new SimpleDateFormat("yyyyMMddHH");
		 DateFormat dateFormatHeure = new SimpleDateFormat("HH");

		 String lastInitialize=null;
         lastInitialize=UtilitaireDao.get("arc").getString(null, "select last_init from arc.pilotage_batch ");


		 Date dNow = new Date();
		 Date dLastInitialize;

		 mapParam.put("numlot", dateFormat2.format(dNow));

	     dLastInitialize = dateFormat.parse(lastInitialize);
	     
	     // la nouvelle initialisation se lance directe : pas de plage horaire. Mais juste en cas de modification de regles.
	     // on lance toujours l'initialisation en mode "non production"
	     // on ne la lance que s'il n'y a rien en cours (pas essentiel mais plus sécurisé)
	     if ((!dejaEnCours && dLastInitialize.compareTo(dNow)<0) || !production)
	     {
	    	 message("Initialisation en cours");
	    	 initialiser.run();
			 message("Initialisation terminée : "+(int)initialiser.report.nbLines+" e : "+initialiser.report.duree+" ms");
			
			UtilitaireDao.get("arc").executeRequest(null, "update arc.pilotage_batch set last_init=to_char(current_date+interval '"+INTERVAL_JOUR_INITIALISATION+" days','yyyy-mm-dd')||':"+HEURE_INITIALISATION_PRODUCTION+"' , operation=case when operation='R' then 'O' else operation end;");
			
			// on met la date d'initialsiation à la date courante
			// si la production a demandée à etre réactivée, on la réactive
//			UtilitaireDao.get("arc").executeRequest(null, "update arc.pilotage_batch set last_init='"+dateFormat.format(dNow)+"', operation=case when operation='R' then 'O' else operation end;");
		}
		 productionOn=productionOn();


		 // Vérifier si la production est activée
		 if (productionOn)
		 {
		
		// Reception : ne faire que si il n'y a rien déjà en cours au début du batch
			 if (!dejaEnCours)
			 {
				 recevoir.run();
				 dejaEnCours=false;
			 }

			 if (production)
			 {
				 message("Reception : "+(int)recevoir.report.nbLines+" e : "+recevoir.report.duree+" ms");
			 }
			 
			 fichierRestant=(int)recevoir.report.nbLines>0;

		// on lance tout systematiquement pour la reprise sur erreur

			int delay=poolingDelay/6;
			boolean exit=false;
		
			message("Début boucle Chargement->Mapping");
			do {
				
			if (!charger.isAlive())
			{
				charger=new ChargerThread();
				charger.start();
			}

			sleep(delay);
			
			if (!normer.isAlive())
			{
				normer=new NormerThread();
				normer.start();
			}
			sleep(delay);

			if (!controler.isAlive())
			{
				controler=new ControlerThread();
				controler.start();
			}

			sleep(delay);
			
			if (!filtrer.isAlive())
			{
				filtrer=new FiltrerThread();
				filtrer.start();
			}

			sleep(delay);
			
			if (!mapper.isAlive())
			{
				mapper=new MapperThread();
				mapper.start();
			}
			
			sleep(delay);
			
			if (
					UtilitaireDao.get("arc").getInt(null,"select count(*) from (select 1 from "+envExecution+".pilotage_fichier where etape=1 limit 1) ww")==0
					)
			{
					exit=true;
			}

			
			productionOn=productionOn();

			if (!productionOn)
			{
				break;
			}
			
			sleep(delay);
		}
			while (!exit);


			if (productionOn)
			{
				// Effacer les fichiers du répertoire OK
				effacerRepertoireChargement(repertoire, envExecution);
			}

//		Files.deleteIfExists(f.toPath());
		}

		 // Maintenance du catalog
		ApiService.maintenancePgCatalog(null, "full");
			

		message("Traitement Fin");

		// si on n'est pas en production, on itere tant qu'il y a des fichiers dans le repertoire.
		}
		while (!production && recevoir.report.nbLines>0 && productionOn);

		
		// paliatif production pour ARC
        UtilitaireDao.get("arc").grantToRole("SELECT","comptelecture");
        // paliatif production pour ARC-DADS
        UtilitaireDao.get("arc").grantToRole("SELECT","lecture");
		
        
        if (args!=null && args.length>0 && args[0].equals("noExit"))
        {
        	message("No Exit");
        }
        else
        {
        	System.exit(0);
        }

	}
		

    } catch (Exception ex) {
         LoggerHelper.errorGenTextAsComment(BatchARC.class, "main()", LOGGER, ex);
		System.exit(202);
    }
	
	} while (fichierRestant);

	message("Fin du batch");

}


	/**
	 * Créer la table de pilotage batch si elle n'existe pas déjà
	 * @throws Exception
	 */
	public static void creerTablePilotageBatch() throws Exception
	{
		StringBuilder requete=new StringBuilder();
		requete.append("\n CREATE TABLE IF NOT EXISTS arc.pilotage_batch (last_init text collate \"C\", operation text collate \"C\"); ");
		requete.append("\n insert into arc.pilotage_batch select '1900-01-01:00','O' where not exists (select 1 from arc.pilotage_batch); ");
        UtilitaireDao.get("arc").executeRequest(null, requete);
	}


	/**
	 * test si la chaine batch est arrétée
	 * @return
	 * @throws Exception
	 */
	public static boolean productionOn() throws Exception {
		if (production) {
			return UtilitaireDao.get("arc").hasResults(null, "select 1 from arc.pilotage_batch where operation='O'");
		}
		else
		{
			return true;
		}
	}
	
	
	/**
	 * Effacer les répertoires de chargement OK KO et ENCOURS
	 * @param repertoire
	 * @param envExecution
	 * @throws Exception 
	 */
	public static void effacerRepertoireChargement(String repertoire, String envExecution) throws Exception
	{
		
		// Effacer les fichiers des répertoires OK et KO
		File f=new File(repertoire + envExecution.replace(".", "_").toUpperCase() + File.separator + TraitementPhase.RECEPTION+"_"+TraitementEtat.OK);
		File[] fs= f.listFiles();
		for (File z:fs)
		{
			if (z.isDirectory())
			{
				FileUtils.deleteDirectory(z);
			}
			else
			{
				// ajout d'un garde fou : si le fichier n'est pas archivé : pas touche
				File fCheck=new File(repertoire + envExecution.replace(".", "_").toUpperCase() + File.separator 
						+ TraitementPhase.RECEPTION + "_"+ ManipString.substringBeforeFirst(z.getName(),"_")
						+ "_ARCHIVE"
						+ File.separator
						+ ManipString.substringAfterFirst(z.getName(),"_")
						);
				if (fCheck.exists())
				{
					z.delete();
				}
				else
				{
					z.renameTo(fCheck);
				}
			}
		}

		// Effacer les fichiers du répertoire KO
		f=new File(repertoire + envExecution.replace(".", "_").toUpperCase() + File.separator + TraitementPhase.RECEPTION+"_"+TraitementEtat.KO);
		fs= f.listFiles();
		for (File z:fs)
		{
			// ajout d'un garde fou : si le fichier n'est pas archivé : pas touche
			File fCheck=new File(repertoire + envExecution.replace(".", "_").toUpperCase() + File.separator 
					+ TraitementPhase.RECEPTION + "_"+ ManipString.substringBeforeFirst(z.getName(),"_")
					+ "_ARCHIVE"
					+ File.separator
					+ ManipString.substringAfterFirst(z.getName(),"_")
					);
			if (fCheck.exists())
			{
				z.delete();
			}
			else
			{
				z.renameTo(fCheck);
			}
		}
		
		f=new File(repertoire + envExecution.replace(".", "_").toUpperCase() + File.separator + TraitementPhase.RECEPTION+"_"+TraitementEtat.ENCOURS);
		fs= f.listFiles();
		for (File z:fs)
		{
			// ajout d'un garde fou : si le fichier n'est pas archivé : pas touche
			File fCheck=new File(repertoire + envExecution.replace(".", "_").toUpperCase() + File.separator 
					+ TraitementPhase.RECEPTION + "_"+ ManipString.substringBeforeFirst(z.getName(),"_")
					+ "_ARCHIVE"
					+ File.separator
					+ ManipString.substringAfterFirst(z.getName(),"_")
					);
			if (fCheck.exists())
			{
				z.delete();
			}
			else
			{
				z.renameTo(fCheck);
			}
		}
		
	}
	

}