/**
 * DbmsTable.java
 * $ID*
 */

package dalserver.tap;

import ca.nrc.cadc.date.DateUtil;
import ca.nrc.cadc.stc.Position;
import ca.nrc.cadc.stc.Region;
import ca.nrc.cadc.stc.STC;
import ca.nrc.cadc.stc.StcsParsingException;
import ca.nrc.cadc.tap.schema.ColumnDesc;
import ca.nrc.cadc.tap.schema.TableDesc;
import ca.nrc.cadc.tap.upload.DatabaseDataTypeFactory;
import ca.nrc.cadc.tap.upload.datatype.ADQLDataType;
import ca.nrc.cadc.tap.upload.datatype.DatabaseDataType;
import ca.nrc.cadc.uws.Job;
import ca.nrc.cadc.uws.Parameter;
import org.apache.log4j.Logger;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import javax.sql.*;
import java.sql.*;

/**
 * This class provides methods for writing database tables, including creating
 * a table, saving a ResultSet to a table, and related utilities for generat
 * a basic TAP Schema.  This class is based upon the TAP UploadManager class,
 * which performs a similar function for uploaded VOTables.
 *
 * @author DTody (based upon OpenCADC TAP UploadManager by jburke)
 */
public class DbmsTable {
    private static final Logger log = Logger.getLogger(DbmsTable.class);
    
    // Number of rows to insert per commit.
    private static final int NUM_ROWS_PER_COMMIT = 100;
    
    /** DataSource for the DB.  */
    protected DataSource dataSource = null;

    // Local data.
    protected String lastTable = null;
    protected int rowCount = 0;
    
    /** No-arg constructor.  */
    public void DbmsTable() { }

    /**
     * Set the DataSource used for creating and populating tables.
     *
     * @param ds		The DataSource to be used for output
     */
    public void setDataSource(DataSource ds) {
        this.dataSource = ds;
    }

    /**
     * Get the fully qualified name of the most recent output table.
     */
    public String getTableName() {
	return (this.lastTable);
    }

    /**
     * Get the number of rows written or updated in the most recent table
     * update operation.
     */
    public int getRowCount() {
	return (this.rowCount);
    }

    /**
     * Create a new database table.
     *
     * @param	schemaName	The schema to be used
     * @param	tableName	The name of the table to be created
     * @param	tableDesc	Table descriptor for the new table
     */
    public void createTable(String schemaName, String tableName,
	TableDesc tableDesc) {

	// Verify that we have a DataSource to write to.
        if (dataSource == null)
            throw new IllegalStateException("failed to get DataSource");

	log.debug("createTable: schema=" + schemaName + " table=" + tableName);

        // SQL statements
        Statement stmt = null;
        PreparedStatement ps = null;
        Connection conn = null;
	String dbTableName = null;

        try {
            // Get database connection.
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            // Class to map ADQL datatypes to database datatype names.
            DatabaseDataType dbDataType =
		DatabaseDataTypeFactory.getDatabaseDataType(conn);
            
	    // Fully qualified name of the table in the database.
	    dbTableName = getDbTableName(schemaName, tableName);

	    // Build the SQL to create the table.
	    String tableSQL = getCreateTableSQL(tableDesc, dbTableName, dbDataType);
	    log.debug("Create table SQL: " + tableSQL);

	    // Create the table.
	    stmt = conn.createStatement();
	    stmt.executeUpdate(tableSQL);

	    // Grant select access for others to query.
	    String grantSQL = getGrantSelectTableSQL(dbTableName);
	    if (grantSQL != null && !grantSQL.isEmpty()) {
		log.debug("Grant select SQL: " + grantSQL);
		stmt.executeUpdate(grantSQL);
	    }

	    // Commit the create and grant statements.
	    conn.commit();

	    this.lastTable = dbTableName;
	    this.rowCount = 0;

        } catch (SQLException ex) {
            throw new RuntimeException("failed to create table: " + ex.getMessage());

        } finally {
            try {
                if (conn != null)
                    conn.rollback();
            } catch (SQLException ignore) { }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ignore) { }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException ignore) { }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignore) { }
            }
        }
    }

    /**
     * Count the number of rows in a table matching the given WHERE clause.
     *
     * @param	schemaName	The schema containing the table
     * @param	tableName	The table to be queried
     * @param	where		The WHERE clause (default none)
     * @returns			The number of rows in the resultSet
     *
     * This is used to perform a single query of a single table without
     * having to compose the SQL for the query, or form a fully-qualified
     * table name.  It can used for example, to test whether a given
     * record is already present in a table.  If the query results an
     * empty resultSet zero is returned.
     */
    public int queryRows(String schemaName, String tableName,
	String whereClause) throws SQLException {

	// Verify that we have a valid DataSource.
        if (dataSource == null)
            throw new IllegalStateException("failed to get DataSource");

	log.debug("queryRows: schema=" + schemaName + " table=" + tableName +
	    "where=" + whereClause);

	// Fully qualified name of the target table in the database.
	String dbTableName = getDbTableName(schemaName, tableName);

	// Compose the SQL.
	StringBuilder sb = new StringBuilder();
	String sql = null;

	sb.append("SELECT COUNT(*) FROM ");
	sb.append(dbTableName);
	if (whereClause != null) {
	    sb.append(" WHERE ");
	    sb.append(whereClause);
	}
	sql = sb.toString();

	// Perform the query.
        Connection conn = null;
	Statement st;
	ResultSet rs;
	int count = 0;

	try {
	    conn = dataSource.getConnection();
	    st = conn.createStatement();
	    rs = st.executeQuery(sql);
	    if (rs.next())
		count = rs.getInt(1);
	} finally {
	    conn.close();
	}

	this.lastTable = dbTableName;
	this.rowCount = count;

	return (count);
    }

    /**
     * Write a ResultSet to a new database table.
     *
     * @param	rs		A JDBC ResultSet, e.g., from a query
     * @param	schemaName	Schema name within DataSource context
     * @param	tableName	Table name within the schema
     * @param	desc		A brief description of the table, or null
     * @param	utype		The VO Utype for the table, or null
     *
     * The schema for the new table will be automatically determined from the
     * input resultSet.  A DataSource with create table and insert permission
     * must have been set previously.
     */
    public void writeData(ResultSet rs,
	String schemaName, String tableName, String descr, String utype) {

	TableDesc td = getTableDesc(rs, schemaName, tableName, descr, utype);
	writeData(rs, td);
    }

    /**
     * Write a ResultSet to a database table described by a table descriptor.
     *
     * @param	rs		A JDBC ResultSet, e.g., from a query
     * @param	tableDesc	Description of the output table
     *
     * A new table will be created as defined by the provided table descriptor
     * and the contents of the resultSet will be written to the new table.
     * Any resultSet fields not defined in the table descriptor will be 
     * omitted.  Any output table fields not present in the resultSet will be
     * set to null values.  A DataSource with create table and insert permission
     * must have been set previously.
     */
    public void writeData(ResultSet rs, TableDesc tableDesc) {

	// Verify that we have a DataSource to write to.
        if (dataSource == null)
            throw new IllegalStateException("failed to get DataSource");
 
        // SQL statements
        Statement stmt = null;
        PreparedStatement ps = null;
        Connection conn = null;
	String dbTableName;

        try {
            // Get database connection.
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            
            // Class to map ADQL datatypes to database datatype names.
            DatabaseDataType dbDataType =
		DatabaseDataTypeFactory.getDatabaseDataType(conn);

	    // Fully qualified name of the table in the database.
	    String schemaName = tableDesc.getSchemaName();
	    String tableName = tableDesc.getTableName();
	    dbTableName = getDbTableName(schemaName, tableName);

	    // Build the SQL to create the table.
	    String tableSQL = getCreateTableSQL(tableDesc, dbTableName, dbDataType);
	    log.debug("Create table SQL: " + tableSQL);

	    // Create the table.
	    stmt = conn.createStatement();
	    stmt.executeUpdate(tableSQL);

	    // Grant select access for others to query.
	    String grantSQL = getGrantSelectTableSQL(dbTableName);
	    if (grantSQL != null && !grantSQL.isEmpty()) {
		log.debug("Grant select SQL: " + grantSQL);
		stmt.executeUpdate(grantSQL);
	    }

	    // Commit the create and grant statements.
	    conn.commit();

	    // Get a PreparedStatement that populates the table.
	    String insertSQL = getInsertTableSQL(tableDesc, dbTableName); 
	    ps = conn.prepareStatement(insertSQL);
	    log.debug("Insert table SQL: " + insertSQL);
	    int numRows = 0;

	    // Populate the table from the ResultSet tabledata rows.
	    while (rs.next()) {
		ArrayList<Object> row = new ArrayList<Object>();

		// Use the table description which defined the output table
		// to propagate the row data.  Only columns that are present
		// in the tableDesc will be set in the output (if the tableDesc
		// was created from the resultSet then all resultSet data will
		// be propagated).  If the corresponding column is not present
		// in the resultSet, with the same name as in the tableDesc,
		// then the value in the output table will be set to null.
		// The column datatypes in the input ResultSet must match
		// those in the tableSet/output table.

		ArrayList<ColumnDesc> cols = (ArrayList) tableDesc.getColumnDescs();
		for (ColumnDesc col : cols) {
		    String colName = col.getColumnName();
		    row.add((Object)rs.getObject(colName));
		}

		// Update the PreparedStatement with the row data.
		updatePreparedStatement(ps, cols, row);

		// Execute the update.
		ps.executeUpdate();
		
		// Commit every NUM_ROWS_PER_COMMIT rows.
		if (numRows != 0 && (numRows % NUM_ROWS_PER_COMMIT) == 0) {
		    log.debug(NUM_ROWS_PER_COMMIT + " rows committed");
		    conn.commit();
		}

		numRows++;
	    }

	    // Commit any remaining rows.
	    conn.commit();

	    log.debug(numRows + " rows inserted into " + dbTableName);
	    this.lastTable = dbTableName;
	    this.rowCount = numRows;

        } catch (StcsParsingException ex) {
            throw new RuntimeException("failed to create and load table in DB", ex);
        } catch (SQLException ex) {
            throw new RuntimeException("failed to create and load table in DB", ex);

        } finally {
            try {
                if (conn != null)
                    conn.rollback();
            } catch (SQLException ignore) { }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ignore) { }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException ignore) { }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignore) { }
            }
        }
    }

    /**
     * Insert row data into an existing table.
     *
     * @param	schemaName	Schema containing table
     * @param	tableName	Name of table within schema
     * @param	data		Row data
     *
     * Row data is passed as a List of rows, where each row is expressed as
     * List<Object>.  Each Object data instance must match the datatype
     * required by the table column.
     */
    public void insertData(String schemaName, String tableName,
	ArrayList<ArrayList<Object>> data) {

	// Verify that we have a DataSource to write to.
        if (dataSource == null)
            throw new IllegalStateException("failed to get DataSource");

	// Generate a tableDesc instance for the output table.
	TableDesc tableDesc = getTableDesc(schemaName, tableName, null, null);

        // SQL statements
        Statement stmt = null;
        PreparedStatement ps = null;
        Connection conn = null;
	String dbTableName;
	int numRows = 0;

        try {
            // Get database connection.
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            
            // Class to map ADQL datatypes to database datatype names.
            DatabaseDataType dbDataType =
		DatabaseDataTypeFactory.getDatabaseDataType(conn);

	    // Fully qualified name of the table in the database.
	    dbTableName = getDbTableName(schemaName, tableName);

	    // Get a PreparedStatement that populates the table.
	    String insertSQL = getInsertTableSQL(tableDesc, dbTableName); 
	    ps = conn.prepareStatement(insertSQL);
	    log.debug("Insert table SQL: " + insertSQL);

	    // Populate the table from the ResultSet tabledata rows.
	    for (ArrayList<Object> row : data) {

		// Use the table description which defined the output table
		// to propagate the row data.  Only columns that are present
		// in the tableDesc will be set in the output.  The column
		// datatypes in the input data must match those in the
		// tableSet/output table.

		ArrayList<ColumnDesc> cols = (ArrayList) tableDesc.getColumnDescs();
		updatePreparedStatement(ps, cols, row);

		// Execute the update.
		ps.executeUpdate();

		// Commit every NUM_ROWS_PER_COMMIT rows.
		if (numRows != 0 && (numRows % NUM_ROWS_PER_COMMIT) == 0) {
		    log.debug(NUM_ROWS_PER_COMMIT + " rows committed");
		    conn.commit();
		}

		numRows++;
	    }

	    // Commit any remaining rows.
	    conn.commit();

	    log.debug(numRows + " rows inserted into " + dbTableName);
	    this.lastTable = dbTableName;
	    this.rowCount = numRows;

        } catch (StcsParsingException ex) {
            throw new RuntimeException("failed to create and load table in DB", ex);
        } catch (SQLException ex) {
            throw new RuntimeException("failed to create and load table in DB", ex);

        } finally {
            try {
                if (conn != null)
                    conn.rollback();
            } catch (SQLException ignore) { }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ignore) { }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException ignore) { }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignore) { }
            }
        }
    }

    /**
     * Generate a TableDesc instance for the given ResultSet.
     *
     * @param	rs		A JDBC ResultSet, e.g., from a query
     * @param	schemaName	Schema name within DataSource context
     * @param	tableName	Table name within the schema
     * @param	desc		A brief description of the table, or null
     * @param	utype		The VO Utype for the table, or null
     *
     * A basic table descriptor (TableDesc) instance is generated from the
     * given resultSet.  The given schema and table names refer to the
     * tableDesc to be generated (e.g., for a new table), rather than to
     * the input resultSet.
     */
    public TableDesc getTableDesc(ResultSet rs,
	String schemaName, String tableName, String descr, String utype) {

	TableDesc tab = new TableDesc(schemaName, tableName, null, null);
	ArrayList<ColumnDesc> cols = new ArrayList<ColumnDesc>();

	try {
	    // Get the metadata for the input resultSet.
	    ResultSetMetaData meta = rs.getMetaData();
	    int ncols = meta.getColumnCount();

	    // Populate the column list and table descriptor.
	    for (int i=1;  i <= ncols;  i++) {
		ColumnDesc col = new ColumnDesc();

		// Get the column metadata.
		String colName = meta.getColumnName(i).toLowerCase();
		int colType = meta.getColumnType(i);
		int colSize = meta.getPrecision(i);
		String adqlType = getAdqlType(colType);

		// Set the column metadata.
		col.setTableName(tableName);
		col.setColumnName(colName);
		col.setDatatype(adqlType);
		col.setSize(colSize);

		// Add the new column descriptor to the columns list.
		cols.add(col);
	    }
	} catch (SQLException ex) {
            throw new RuntimeException("failed to create table descriptor", ex);
	}

	// Set the column descriptor array.
	tab.setColumnDescs(cols);

	return (tab);
    }

    /**
     * Generate a TableDesc instance for an existing database table.
     *
     * @param	schemaName	Schema name within DataSource context
     * @param	tableName	Table name within the schema
     * @param	desc		A brief description of the table, or null
     * @param	utype		The VO Utype for the table, or null
     *
     * A basic table descriptor (TableDesc) instance is generated from the
     * given database table.
     */
    public TableDesc getTableDesc(String schemaName, String tableName,
	String descr, String utype) {

	TableDesc tab = new TableDesc(schemaName, tableName, null, null);
	ArrayList<ColumnDesc> cols = new ArrayList<ColumnDesc>();
	Connection conn = null;
	DatabaseMetaData dbm;
	ResultSet rs;

	try {
	    // Get the list of table columns as a resultSet.
	    conn = dataSource.getConnection();
	    dbm = conn.getMetaData();
	    rs = dbm.getColumns(null, schemaName, tableName, "%");
	    int nrows = 0;

	    // Populate the column list and table descriptor.
	    while (rs.next()) {
		ColumnDesc col = new ColumnDesc();

		// Get the column metadata.
		String colName = rs.getString("COLUMN_NAME").toLowerCase();
		int colType = rs.getInt("DATA_TYPE");
		int colSize = rs.getInt("COLUMN_SIZE");
		String adqlType = getAdqlType(colType);

		// Set the column metadata.
		col.setTableName(tableName);
		col.setColumnName(colName);
		col.setDatatype(adqlType);
		col.setSize(colSize);

		// Add the new column descriptor to the columns list.
		cols.add(col);
		nrows++;
	    }

	    // If the resultSet was empty, we have no cols hence no table.
	    if (nrows == 0)
		throw new RuntimeException("Empty or nonexistent table (" + tableName + ")");

	} catch (SQLException ex) {
            throw new RuntimeException("failed to create table descriptor", ex);
	} finally {
	    if (conn != null) {
		try {
		    conn.close();
		} catch (SQLException ignore) { }
	    }
	}

	// Set the column descriptor array.
	tab.setColumnDescs(cols);

	return (tab);
    }

    /**
     * Map a JDBC datatype to an ADQL datatype.
     *
     * @param type		The input JDBC type.
     */
    public String getAdqlType(int jdbcType) {
	String adqlType;

	switch (jdbcType) {
	case java.sql.Types.BIT:
	case java.sql.Types.BOOLEAN:
	case java.sql.Types.SMALLINT:
	case java.sql.Types.TINYINT:
	    adqlType = ADQLDataType.ADQL_SMALLINT;
	    break;
	case java.sql.Types.INTEGER:
	    adqlType = ADQLDataType.ADQL_INTEGER;
	    break;
	case java.sql.Types.BIGINT:
	    adqlType = ADQLDataType.ADQL_BIGINT;
	    break;

	case java.sql.Types.FLOAT:
	case java.sql.Types.REAL:
	    adqlType = ADQLDataType.ADQL_REAL;
	    break;
	case java.sql.Types.DECIMAL:
	case java.sql.Types.DOUBLE:
	case java.sql.Types.NUMERIC:
	    adqlType = ADQLDataType.ADQL_DOUBLE;
	    break;

	case java.sql.Types.CHAR:
	    adqlType = ADQLDataType.ADQL_CHAR;
	    break;
	case java.sql.Types.VARCHAR:
	    adqlType = ADQLDataType.ADQL_VARCHAR;
	    break;

	case java.sql.Types.DATE:
	case java.sql.Types.TIMESTAMP:
	    adqlType = ADQLDataType.ADQL_TIMESTAMP;
	    break;

	case java.sql.Types.BLOB:
	case java.sql.Types.CLOB:
	    adqlType = ADQLDataType.ADQL_CLOB;
	    break;

	    // adqlType = ADQLDataType.ADQL_POINT;
	    // adqlType = ADQLDataType.ADQL_REGION;

	default:
	    adqlType = ADQLDataType.ADQL_CHAR;
	    break;
	}

	return (adqlType);
    }

    /**
     * Create the SQL to grant select privileges for the UPLOAD table.
     * 
     * @param dbTableName		Fully qualified table name.
     */
    protected String getGrantSelectTableSQL(String dbTableName) {
        return (null);
    }
 
    /**
     * Construct the fully qualified database table name from the schema and
     * table names.
     * 
     * @param	schemaName		The schema name
     * @param	tableName		The table name within the schema
     * @return				The database table name.
     */
    public String getDbTableName(String schemaName, String tableName) {
        StringBuilder sb = new StringBuilder();

        if (schemaName != null)
            sb.append(schemaName).append(".");
        sb.append(tableName);

	return (sb.toString());
    }

    /**
     * Create the SQL required to create a table described by the TableDesc.
     *
     * @param tableDesc describes the table.
     * @param dbTableName fully qualified table name.
     * @param dbDataType map of SQL types to database specific data types.
     * @return SQL to create the table.
     * @throws SQLException
     */
    protected String getCreateTableSQL(TableDesc tableDesc, String dbTableName,
	DatabaseDataType dbDataType) throws SQLException {

        StringBuilder sb = new StringBuilder();
        sb.append("create table ");
        sb.append(dbTableName);
        sb.append(" ( ");

        for (int i = 0; i < tableDesc.columnDescs.size(); i++) {
            ColumnDesc columnDesc = tableDesc.columnDescs.get(i);
            sb.append(columnDesc.columnName);
            sb.append(" ");
            sb.append(dbDataType.getDataType(columnDesc));
            sb.append(" null ");
            if (i + 1 < tableDesc.columnDescs.size())
                sb.append(", ");
        }

        sb.append(" ) ");
        return (sb.toString());
    }
    
    /**
     * Create the SQL required to create a PreparedStatement
     * to insert into the table described by the TableDesc.
     * 
     * @param tableDesc describes the table.
     * @return SQL to create the table.
     */
    protected String getInsertTableSQL(TableDesc tableDesc, String tableName) {

        StringBuilder sb = new StringBuilder();
        sb.append("insert into ");
        sb.append(tableName);
        sb.append(" ( ");

        for (int i = 0; i < tableDesc.columnDescs.size(); i++) {
            ColumnDesc columnDesc = tableDesc.columnDescs.get(i);
            sb.append(columnDesc.columnName);
            if (i + 1 < tableDesc.columnDescs.size())
                sb.append(", ");
        }

        sb.append(" ) values ( ");
        for (int i = 0; i < tableDesc.columnDescs.size(); i++) {
            sb.append("?");
            if (i + 1 < tableDesc.columnDescs.size())
                sb.append(", ");
        }

        sb.append(" ) ");
        return (sb.toString());
    }

    /**
     * Updated the PreparedStatement with the row data using the ColumnDesc to
     * determine each column data type.
     *
     * @param ps the prepared statement.
     * @param columnDescs List of ColumnDesc for this table.
     * @param row Array containing the data to be inserted into the database.
     * @throws SQLException if the statement is closed or if the parameter index type doesn't match.
     */
    protected void updatePreparedStatement(PreparedStatement ps,
	List<ColumnDesc> columnDescs, List<Object> row)
	throws SQLException, StcsParsingException {

        int i = 1;
        for (Object value : row) {
            ColumnDesc columnDesc = columnDescs.get(i-1);
            log.debug("update ps: " + columnDesc.columnName + "[" +
		columnDesc.datatype + "] = " + value);

            if (value == null)
                ps.setNull(i, ADQLDataType.getSQLType(columnDesc.datatype));
            else if (columnDesc.datatype.equals(ADQLDataType.ADQL_TIMESTAMP)) {
                Date date = (Date) value;
                ps.setTimestamp(i, new Timestamp(date.getTime()));
            } else if (columnDesc.datatype.equals(ADQLDataType.ADQL_POINT)) {
                Region r = (Region) value;
                if (r instanceof Position) {
                    Position pos = (Position) r;
                    Object o = getPointObject(pos);
                    ps.setObject(i, o);
                } else {
                    throw new IllegalArgumentException("failed to parse " +
		    value + " as an " + ADQLDataType.ADQL_POINT);
		}
            } else if (columnDesc.datatype.equals(ADQLDataType.ADQL_REGION)) {
                Region reg = (Region) value;
                Object o = getRegionObject(reg);
                ps.setObject(i, o);
            } else
                ps.setObject(i, value, ADQLDataType.getSQLType(columnDesc.datatype));

            i++;
            
            /*
            else if (columnDesc.datatype.equals(ADQLDataType.ADQL_SMALLINT))
                ps.setShort(i + 1, Short.parseShort(value));
            else if (columnDesc.datatype.equals(ADQLDataType.ADQL_INTEGER))
                ps.setInt(i + 1, Integer.parseInt(value));
            else if (columnDesc.datatype.equals(ADQLDataType.ADQL_BIGINT))
                ps.setLong(i + 1, Long.parseLong(value));
            else if (columnDesc.datatype.equals(ADQLDataType.ADQL_REAL))
                ps.setFloat(i + 1, Float.parseFloat(value));
            else if (columnDesc.datatype.equals(ADQLDataType.ADQL_DOUBLE))
                ps.setDouble(i + 1, Double.parseDouble(value));
            else if (columnDesc.datatype.equals(ADQLDataType.ADQL_CHAR))
                ps.setString(i + 1, value);
            else if (columnDesc.datatype.equals(ADQLDataType.ADQL_VARCHAR))
                ps.setString(i + 1, value);
            else if (columnDesc.datatype.equals(ADQLDataType.ADQL_CLOB))
                ps.setString(i + 1, value);
            else if (columnDesc.datatype.equals(ADQLDataType.ADQL_TIMESTAMP)) {
                try {
                    Date date = dateFormat.parse(value);
                    ps.setTimestamp(i + 1, new Timestamp(date.getTime()));
                } catch (ParseException e) {
                    throw new SQLException("failed to parse timestamp " + value, e);
                }
            } else if (columnDesc.datatype.equals(ADQLDataType.ADQL_POINT)) {
                Region r = STC.parse(value);
                if (r instanceof Position) {
                    Position pos = (Position) r;
                    Object o = getPointObject(pos);
                    ps.setObject(i+1, o);
                } else {
                    throw new IllegalArgumentException("failed to parse " +
		    value + " as an " + ADQLDataType.ADQL_POINT);
		}

            } else if (columnDesc.datatype.equals(ADQLDataType.ADQL_REGION)) {
                Region reg = STC.parse(value);
                Object o = getRegionObject(reg);
                ps.setObject(i+1, o);
            } else
                throw new SQLException("Unsupported ADQL data type " + columnDesc.datatype);
            */
        }
    }

    /**
     * Convert the string representation of the specified ADQL POINT into an object.
     *
     * @param pos
     * @throws SQLException
     * @return an object suitable for use with PreparedStatement.setObject(int,Object)
     */
    protected Object getPointObject(Position pos)
        throws SQLException {

        throw new UnsupportedOperationException(
	"cannot convert ADQL POINT (STC-S Position) -> internal database type");
    }

    /**
     * Convert the string representation of the specified ADQL POINT into an object.
     *
     * @param reg
     * @throws SQLException
     * @return an object suitable for use with PreparedStatement.setObject(int,Object)
     */
    protected Object getRegionObject(Region reg)
        throws SQLException {

        throw new UnsupportedOperationException(
	"cannot convert ADQL REGION (STC-S Region) -> internal database type");
    }
}
