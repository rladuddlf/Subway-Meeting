package metro;

import java.sql.*;
import java.util.Properties;
import java.util.Vector;



public class DBConnectionMgr {
   private Vector connections = new Vector(10);
    private String _driver = "org.mariadb.jdbc.Driver";
    private String _url = "jdbc:mariadb://127.0.0.1:3306/subwaymeeting";    
    private String _user = "root";
    private String _password = "1234";
    private boolean _traceOn = false;
    private boolean initialized = false; 
    private int _openConnections = 10; 
    private static DBConnectionMgr instance = null; 
    public DBConnectionMgr() {
    }
    /** Use this method to set the maximum number of open connections before
     unused connections are closed.
     */
    public static DBConnectionMgr getInstance() {
        //Connection Pool�� �����Ǿ� �ִ��� �˻�
        if (instance == null) { //�����Ǿ� ���� �ʴٸ�
            synchronized (DBConnectionMgr.class) {//Lock ����
                if (instance == null) {//�������� ������
                    instance = new DBConnectionMgr();//������ ����
                }
            }
        }
        //System.out.println("getInstance():" + instance.hashCode());
        return instance;
    }

    public void setOpenConnectionCount(int count) {
        _openConnections = count;
    }

    public void setEnableTrace(boolean enable) {
        _traceOn = enable;
    }

    /** Returns a Vector of java.sql.Connection objects */
    public Vector getConnectionList() {
        return connections;
    }
    /** Opens specified "count" of connections and adds them to the existing pool */
    public synchronized void setInitOpenConnections(int count)
            throws SQLException {
        Connection c = null;
        ConnectionObject co = null;
        for (int i = 0; i < count; i++) {
            c = createConnection();
            co = new ConnectionObject(c, false);
            connections.addElement(co);
            trace("ConnectionPoolManager: Adding new DB connection to pool (" + connections.size() + ")");
        }
    }
    /** Returns a count of open connections */
    public int getConnectionCount() {
        return connections.size();
    }
    /** Returns an unused existing or new connection.  */
    //�� �޼ҵ�� ���� �����ڰ� �߻��� �� �����ϴ�.
    public synchronized Connection getConnection()
            throws Exception {
        //System.out.println("getConnection():" + instance.hashCode());
        if (!initialized) {
            //JDBC ����̹��� �ε��մϴ�.
            Class c = Class.forName(_driver);
            DriverManager.registerDriver((Driver) c.newInstance());
            initialized = true;
        }      
        Connection c = null;
        ConnectionObject co = null;
        boolean badConnection = false;
        //Vector �˻�
        for (int i = 0; i < connections.size(); i++) {
            co = (ConnectionObject) connections.elementAt(i);
            // If connection is not in use, test to ensure it's still valid!
            if (!co.inUse) {//false
                try {
                    badConnection = co.connection.isClosed();//true
                    if (!badConnection)//false
                        //������ �߻��ߴٸ� true ����
                        badConnection = (co.connection.getWarnings() != null);
                } catch (Exception e) {
                    badConnection = true;
                    e.printStackTrace();
                }
                // Connection is bad, remove from pool
                if (badConnection) {
                    //Vector���� Connection ����
                    connections.removeElementAt(i);
                    trace("ConnectionPoolManager: Remove disconnected DB connection #" + i);
                    continue;
                }
                c = co.connection;
                co.inUse = true;
                trace("ConnectionPoolManager: Using existing DB connection #" + (i + 1));
                break;
            }
        }
        //ó�� �����ڴ� ������ null�̰�, Vector�κ��� Connection��
        //���� ���� ��쵵 null�Դϴ�.
        if (c == null) {
            //���ο� Connection ��ü�� �����մϴ�.
            c = createConnection();
            //Connection ��ü, ��뿩�θ� �����ϴ� flag��������
            //Vector�� �����մϴ�.
            co = new ConnectionObject(c, true);
            //������ ��ü�� Vector�� ����
            connections.addElement(co);
            trace("ConnectionPoolManager: Creating new DB connection #" + connections.size());
        }
        return c;
    }
    
    /** Marks a flag in the ConnectionObject to indicate this connection is no longer in use */
    public synchronized void freeConnection(Connection c) {
        if (c == null)
            return;
        ConnectionObject co = null;
        //Client�� ����� Connection ��ü�� ã�Ƴ��ϴ�. 
        for (int i = 0; i < connections.size(); i++) {
            co = (ConnectionObject) connections.elementAt(i);
            //Vector�� ��ϵ� Connection�̸�
            if (c == co.connection) {
//                System.out.println("c.hashCode():" + c.hashCode());
//                System.out.println("co.connection:" + co.connection);
                //false���ؾ� �ٸ� ����ڰ� �� ���ᰴü�� ����� �� �ֽ��ϴ�.
                co.inUse = false;
                break;
            }
        }
        //Connection�� ���� 10���� �Ѿ����� �����մϴ�.
        for (int i = 0; i < connections.size(); i++) {
            co = (ConnectionObject) connections.elementAt(i);
            if ((i + 1) > _openConnections && !co.inUse)
                //10+1=(11 > 10) && !false
                removeConnection(co.connection);
        }
    }
    
    public void freeConnection(Connection c, PreparedStatement p, ResultSet r) {
        try {
            if (r != null) r.close();
            if (p != null) p.close();
            freeConnection(c);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void freeConnection(Connection c, Statement s, ResultSet r) {
        try {
            if (r != null) r.close();
            if (s != null) s.close();
            freeConnection(c);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void freeConnection(Connection c, PreparedStatement p) {
        try {
            if (p != null) p.close();
            freeConnection(c);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void freeConnection(Connection c, Statement s) {
        try {
            if (s != null) s.close();
            freeConnection(c);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /** Marks a flag in the ConnectionObject to indicate this connection is no longer in use */
    public synchronized void removeConnection(Connection c) {
        if (c == null)
            return;
        ConnectionObject co = null;
        for (int i = 0; i < connections.size(); i++) {
            co = (ConnectionObject) connections.elementAt(i);
            //�����Ϸ��� Connection ��ü�� ã���ϴ�.
            if (c == co.connection) {
                try {
                    c.close();
                    connections.removeElementAt(i);
                    trace("Removed " + c.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }
    /**
     * ������ Ŀ�ؼ��� ����� ������
     * @return
     * @throws SQLException
     */
    private Connection createConnection() throws SQLException {
        Connection con = null;
        try {
            if (_user == null) _user = "";
            if (_password == null) _password = "";
            Properties props = new Properties();
            props.put("user", _user);
            props.put("password", _password);
            //������ Connection�� �����˴ϴ�.
            con = DriverManager.getConnection(_url, props);
        } catch (Throwable t) {
            throw new SQLException(t.getMessage());
        }
        return con;
    }
    /** Closes all connections and clears out the connection pool */

    public void releaseFreeConnections() {
        trace("ConnectionPoolManager.releaseFreeConnections()");
        Connection c = null;
        ConnectionObject co = null;
        for (int i = 0; i < connections.size(); i++) {
            co = (ConnectionObject) connections.elementAt(i);
            if (!co.inUse)
                removeConnection(co.connection);
        }
    }
    /** Closes all connections and clears out the connection pool */

    public void finalize() {
        trace("ConnectionPoolManager.finalize()");
        Connection c = null;
        ConnectionObject co = null;
        for (int i = 0; i < connections.size(); i++) {
            co = (ConnectionObject) connections.elementAt(i);
            try {
                co.connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            co = null;
        }
        connections.removeAllElements();
    }

    private void trace(String s) {
        if (_traceOn)
            System.err.println(s);
    }
}

class ConnectionObject {
    public java.sql.Connection connection = null;
    public boolean inUse = false; //Connection �� ��� ����
    //useFlag: Client�� ��ü�� ����ϴ��� ���� ����
    //true�� ���� Client�� ����ϰ� �ִ� ��ü��� ��
    public ConnectionObject(Connection c, boolean useFlag) {
        connection = c;
        inUse = useFlag;
    }
}