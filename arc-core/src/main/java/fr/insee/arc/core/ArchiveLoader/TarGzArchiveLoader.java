package fr.insee.arc.core.ArchiveLoader;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.util.StaticLoggerDispatcher;


/**
 * The targz archive loader
 */
public class TarGzArchiveLoader extends AbstractArchiveFileLoader {

    private static final Logger LOGGER = LogManager.getLogger(TarGzArchiveLoader.class);

    public TarGzArchiveLoader(File fileChargement, String idSource) {
	super(fileChargement, idSource);
	this.fileDecompresor= new TarGzDecompressor();

    }

    @Override
    public FilesInputStreamLoad readFileWithoutExtracting() throws Exception {
	return null;
    }

    @Override
    public FilesInputStreamLoad loadArchive() throws Exception {
	StaticLoggerDispatcher.info("begin loadArchive() ", LOGGER);

	// Mandatory for multithreading to decompress tar.gz archive
	// as it is not possible to address a specific entry in targz
    extractArchive(fileDecompresor);
	this.filesInputStreamLoad = readFile();

	StaticLoggerDispatcher.info("end loadArchive() ", LOGGER);
	return this.filesInputStreamLoad;

    }


}
