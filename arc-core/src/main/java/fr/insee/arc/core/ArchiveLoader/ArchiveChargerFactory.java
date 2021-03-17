package fr.insee.arc.core.ArchiveLoader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.model.TypeArchive;
import fr.insee.arc.core.util.StaticLoggerDispatcher;


/**
 * Factory to choose the loader
 */
public class ArchiveChargerFactory {
    private Map<TypeArchive, IArchiveFileLoader> map = new HashMap<TypeArchive, IArchiveFileLoader>();
    private static final Logger LOGGER = LogManager.getLogger(ArchiveChargerFactory.class);


    public ArchiveChargerFactory(File fileChargement, String fileName) {
        this.map.put(TypeArchive.ZIP,
               new ZipArchiveLoader(fileChargement, fileName));
        this.map.put(TypeArchive.TARGZ,
        	new TarGzArchiveLoader(fileChargement, fileName));
        this.map.put(TypeArchive.GZ,
        	new GZArchiveLoader(fileChargement, fileName));

    }
    
    public IArchiveFileLoader getChargeur(TypeArchive typeArchive){
        StaticLoggerDispatcher.info("** getLoader from type **", LOGGER);
        return this.map.get(typeArchive);
    }

    
    public IArchiveFileLoader getChargeur(String container){
        StaticLoggerDispatcher.info("** getChargeur from container**", LOGGER);
        IArchiveFileLoader returned = null;
	    if (container.endsWith(".tar.gz") || container.endsWith(".tgz")) {
		returned = getChargeur(TypeArchive.TARGZ);

	    } else if (container.endsWith(".gz")) {
		returned = getChargeur(TypeArchive.GZ);

	    } else if (container.endsWith(".zip")) {
		returned = getChargeur(TypeArchive.ZIP);
	    }
	    return returned;
    }
}