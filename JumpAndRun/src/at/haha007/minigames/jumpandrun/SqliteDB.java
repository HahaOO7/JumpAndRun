package at.haha007.minigames.jumpandrun;

import java.sql.*;

public class SqliteDB {
	public String sDriver = "";
	public String sUrl = null;
	public int iTimeout = 30;
	public Connection conn = null;
	public Statement statement = null;

	public SqliteDB(String sUrlKey) throws SQLException {
		init("org.sqlite.JDBC", sUrlKey);
	}

	public void init(String sDriverVar, String sUrlVar) throws SQLException {
		setDriver(sDriverVar);
		setUrl(sUrlVar);
		setConnection();
		setStatement();
	}

	private void setDriver(String sDriverVar) {
		this.sDriver = sDriverVar;
	}

	private void setUrl(String sUrlVar) {
		this.sUrl = sUrlVar;
	}

	public void setConnection() throws SQLException {
//		Class.forName(this.sDriver);
		this.conn = DriverManager.getConnection(this.sUrl);
	}

	public Connection getConnection() {
		return this.conn;
	}

	public void setStatement() throws SQLException {
		if (this.conn == null) {
			setConnection();
		}
		this.statement = this.conn.createStatement();
		this.statement.setQueryTimeout(this.iTimeout);
	}

	public Statement getStatement() {
		return this.statement;
	}

	public void executeStmt(String instruction) throws SQLException {
		this.statement.executeUpdate(instruction);
	}

	public void executeStmt(String[] instructionSet)
		throws SQLException {
		for (int i = 0; i < instructionSet.length; i++)
			executeStmt(instructionSet[i]);
	}

	public ResultSet executeQry(String instruction) throws SQLException {
		return this.statement.executeQuery(instruction);
	}

	public PreparedStatement prepareStatement(String sql) {
		try {
			return conn.prepareStatement(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void CloseConnection() {
		try {
			this.conn.close();
		} catch (Exception localException) {
		}
	}
}
