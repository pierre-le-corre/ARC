package fr.insee.arc.web.util;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import fr.insee.arc.core.util.LoggerDispatcher;

/** An implementation of LoggerDispatcher that store the log in the session
 * to provide feedback to the user.*/
public class WebLoggerDispatcher implements LoggerDispatcher {
	
	@Autowired
	private Session session;

	/**
	 * Log and store the message in session.
	 *
	 * @param message
	 * @param logger
	 */
	public void error(Object message, Logger logger) {
		try {
			registerMessage(message);
		} catch (Exception e) {
			logger.error("An error occured while storing a message in session", e);
		} finally {
			if (logger.isErrorEnabled()) {
				logger.error(sanitize(message));
			}
		}
	}

	/**
	 * Log and store the message in session.
	 *
	 * @param message
	 * @param ex an exception to log
	 * @param logger
	 */
	public void error(Object message, Exception ex, Logger logger) {
		try {
			registerMessage(message);
		} catch (Exception e) {
			logger.error("An error occured while storing a message in session", e);
		} finally {
			if (logger.isErrorEnabled()) {
				logger.error(sanitize(message), ex);
			}
		}
	}

	/**
	 * Log and store the message in session.
	 *
	 * @param message
	 * @param logger
	 */
	public void warn(Object message, Logger logger) {
		try {
			registerMessage(message);
		} catch (Exception e) {
			logger.error("An error occured while storing a message in session", e);
		} finally {
			if (logger.isWarnEnabled()) {
				logger.warn(sanitize(message));
			}
		}
	}

	/**
	 * Log and store the message in session.
	 *
	 * @param message
	 * @param ex an exception to log
	 * @param logger
	 */
	public void warn(Object message, Exception ex, Logger logger) {
		try {
			registerMessage(message);
		} catch (Exception e) {
			logger.error("An error occured while storing a message in session", e);
		} finally {
			if (logger.isWarnEnabled()) {
				logger.warn(sanitize(message), ex);
			}
		}
	}

	/**
	 * Log and store the message in session.
	 *
	 * @param message
	 * @param logger
	 */
	public void info(Object message, Logger logger) {
		try {
			registerMessage(message);
		} catch (Exception e) {
			logger.error("An error occured while storing a message in session", e);
		} finally {
			if (logger.isInfoEnabled()) {
				logger.info(sanitize(message));
			} 
		}
	}

	/**
	 * Log and store the message in session.
	 *
	 * @param message
	 * @param ex an exception to log
	 * @param logger
	 */
	public void info(Object message, Exception ex, Logger logger) {
		try {
			registerMessage(message);
		} catch (Exception e) {
			logger.error("An error occured while storing a message in session", e);
		} finally {
			if (logger.isInfoEnabled()) {
				logger.info(sanitize(message), ex);
			}
		}
	}

	/**
	 * Log and store the message in session.
	 *
	 * @param message
	 * @param logger
	 */
	public void debug(Object message, Logger logger) {
		try {
			registerMessage(message);
		} catch (Exception e) {
			logger.error("An error occured while storing a message in session", e);
		} finally {
			if (logger.isDebugEnabled()) {
				logger.debug(sanitize(message));
			}
		}
	}

	/**
	 * Log and store the message in session.
	 *
	 * @param message
	 * @param ex an exception to log
	 * @param logger
	 */
	public void debug(Object message, Exception ex, Logger logger) {
		try {
			registerMessage(message);
		} catch (Exception e) {
			logger.error("An error occured while storing a message in session", e);
		} finally {
			if (logger.isDebugEnabled()) {
				logger.debug(sanitize(message), ex);
			}
		}		
	}

	/**
	 * Log and store the message in session.
	 *
	 * @param message
	 * @param logger
	 */
	public void trace(Object message, Logger logger) {
		try {
			registerMessage(message);
		} catch (Exception e) {
			logger.error("An error occured while storing a message in session", e);
		} finally {
			if (logger.isTraceEnabled()) {
				logger.trace(sanitize(message));
			}
		}
	}

	/**
	 * Log and store the message in session.
	 *
	 * @param message
	 * @param ex an exception to log
	 * @param logger
	 */
	public void trace(Object message, Exception ex, Logger logger) {
		try {
			registerMessage(message);
		} catch (Exception e) {
			logger.error("An error occured while storing a message in session", e);
		} finally {
			if (logger.isTraceEnabled()) {
				logger.trace(sanitize(message), ex);
			}
		}
	}

	private void registerMessage(Object message) {
		session.put("console", (String) session.get("console") + new java.util.Date() + " - "
				+ message + "\n");
	}
}
