package fr.insee.arc.utils.queryhandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import fr.insee.arc.utils.dao.IQueryHandler;
import fr.insee.arc.utils.dao.PreparedStatementBuilder;

public class UtilitaireDAOIhmQueryHandler extends UtilitaireDAOQueryHandler implements IQueryHandler
{

    
    public UtilitaireDAOIhmQueryHandler(String poolName)
    {
        super(poolName);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.insee.siera.sqlengine.IQueryExecutor#executeQuery(java.util.function
     * .Function, java.lang.String, java.util.function.BiConsumer)
     */
    @Override
    public <T> List<T> executeQuery(Function<ResultSet, T> onRecord, PreparedStatementBuilder query,
            BiConsumer<Throwable, String> onException) throws SQLException
    {
        return executeQuery(onRecord, query, onException, getOnClose());
    }

    @Override
    public <T> T execute(Function<ResultSet, T> onResult, PreparedStatementBuilder query, BiConsumer<Throwable, String> onException)
            throws SQLException
    {
        return execute(onResult, query, onException, getOnClose());
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.insee.siera.sqlengine.IQueryExecutor#execute(java.lang.String,
     * java.util.function.BiConsumer)
     */
    @Override
    public void executeUpdate(String query, BiConsumer<Throwable, String> onException) throws Exception
    {
        execute(query, onException, getOnClose());
    }

    @Override
    public Connection getWrapped()
    {
        return null;
    }

    @Override
    public void setWrapped(Connection connection)
    {
        throw new UnsupportedOperationException("Vous devriez instancier le datasource.");
    }

}
