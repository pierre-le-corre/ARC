package fr.insee.arc.core.service.engine.controle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ControleRegleService {

	private ControleRegleService() {
		throw new IllegalStateException("Utility class");
	}

	/** Name of the XSD date format that should be translated in SQL */
	public static final String XSD_DATE_NAME = "xs:date";
	public static final String XSD_DATETIME_NAME = "xs:dateTime";
	public static final String XSD_TIME_NAME = "xs:time";

	private static final Logger logger = LogManager.getLogger(ControleRegleService.class);

}
