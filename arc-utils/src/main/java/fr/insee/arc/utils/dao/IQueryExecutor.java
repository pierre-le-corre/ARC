package fr.insee.arc.utils.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import fr.insee.arc.utils.queryhandler.UtilitaireDAOQueryHandler.HowToClose;
import fr.insee.arc.utils.queryhandler.UtilitaireDAOQueryHandler.OnException;

public interface IQueryExecutor
{
    /**
     * Ex�cute une requ�te qui renvoie un r�sultat, et transforme chaque
     * enregistrement du r�sultat dans un objet de type {@link T}, en utilisant
     * une factory <i>ad hoc</i>.<br/>
     * En cas d'erreur, le {@link BiConsumer} effectue un traitement d�pendant
     * de l'erreur et de la requ�te.
     *
     * @param onRecord
     *            La fonction qui transforme un enregistrement en un objet de
     *            type {@code T}
     * @param query
     *            la requ�te textuelle en SQL
     * @param onException
     *            le gestionnaire d'exception
     * @return
     * @throws Exception
     * @see {@link OnException#DO_NOTHING}
     * @see {@link OnException#LOG}
     * @see {@link OnException#THROW}
     */
      <T> List<T> executeQuery(Function<ResultSet, T> onRecord, PreparedStatementBuilder query,
            BiConsumer<Throwable, String> onException) throws SQLException;
    
    /**
     * Ex�cute une requ�te qui renvoie un r�sultat, et transforme chaque
     * enregistrement du r�sultat dans un objet de type {@link T}, en utilisant
     * une factory <i>ad hoc</i>.<br/>
     * En cas d'erreur, le {@link BiConsumer} effectue un traitement d�pendant
     * de l'erreur et de la requ�te.
     *
     * @param onResult
     *            La fonction qui transforme un enregistrement en un objet de
     *            type {@code T}
     * @param query
     *            la requ�te textuelle en SQL
     * @param onException
     *            le gestionnaire d'exception
     * @return
     * @throws Exception
     * @see {@link OnException#DO_NOTHING}
     * @see {@link OnException#LOG}
     * @see {@link OnException#THROW}
     */
     <T> T execute(Function<ResultSet, T> onResult, PreparedStatementBuilder query,
            BiConsumer<Throwable, String> onException) throws SQLException;

    /**
     * Ex�cute une requ�te ne renvoyant aucun r�sultat.<br/>
     * En cas d'erreur, le {@link BiConsumer} effectue un traitement d�pendant
     * de l'erreur et de la requ�te.
     *
     * @param query
     *            la requ�te textuelle en SQL
     * @param onException
     *            le gestionnaire d'exception
     * @return
     * @throws Exception
     */
      void executeUpdate(String query, BiConsumer<Throwable, String> onException) throws Exception;

    /**
     * @return the onClose
     */
      Consumer<? super Connection> getOnClose();

    /**
     *
     * @param howToClose
     *            doit g�rer la cl�ture de la connection.
     * @see HowToClose#JUST_CLOSE
     * @see HowToClose#ASSERT_CLOSE
     * @see HowToClose#COMMIT
     * @see HowToClose#COMMIT_CLOSE
     * @see HowToClose#ROLLBACK
     * @see HowToClose#ROLLBACK_CLOSE
     * @see HowToClose#COMMIT_ROLLBACK_CLOSE
     */
      void setOnClose(Consumer<? super Connection> howToClose);
}