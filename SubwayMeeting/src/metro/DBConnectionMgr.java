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
        //Connection Pool이 생성되어 있는지 검사
        if (instance == null) { //생성되어 있지 않다면
            synchronized (DBConnectionMgr.class) {//Lock 설정
                if (instance == null) {//수영장이 없으면
                    instance = new DBConnectionMgr();//수영장 생성
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
    //이 메소드는 동시 접속자가 발생할 수 없습니다.
    public synchronized Connection getConnection()
            throws Exception {
        //System.out.println("getConnection():" + instance.hashCode());
        if (!initialized) {
            //JDBC 드라이버를 로딩합니다.
            Class c = Class.forName(_driver);
            DriverManager.registerDriver((Driver) c.newInstance());
            initialized = true;
        }      
        Connection c = null;
        ConnectionObject co = null;
        boolean badConnection = false;
        //Vector 검사
        for (int i = 0; i < connections.size(); i++) {
            co = (ConnectionObject) connections.elementAt(i);
            // If connection is not in use, test to ensure it's still valid!
            if (!co.inUse) {//false
                try {
                    badConnection = co.connection.isClosed();//true
                    if (!badConnection)//false
                        //에러가 발생했다면 true 설정
                        badConnection = (co.connection.getWarnings() != null);
                } catch (Exception e) {
                    badConnection = true;
                    e.printStackTrace();
                }
                // Connection is bad, remove from pool
                if (badConnection) {
                    //Vector에서 Connection 제거
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
        //처음 접속자는 무조건 null이고, Vector로부터 Connection을
        //받지 못한 경우도 null입니다.
        if (c == null) {
            //새로운 Connection 객체를 생성합니다.
            c = createConnection();
            //Connection 객체, 사용여부를 저장하는 flag변수값을
            //Vector에 저장합니다.
            co = new ConnectionObject(c, true);
            //생성된 객체를 Vector에 저장
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
        //Client가 사용한 Connection 객체를 찾아냅니다. 
        for (int i = 0; i < connections.size(); i++) {
            co = (ConnectionObject) connections.elementAt(i);
            //Vector에 등록된 Connection이면
            if (c == co.connection) {
//                System.out.println("c.hashCode():" + c.hashCode());
//                System.out.println("co.connection:" + co.connection);
                //false로해야 다른 사용자가 이 연결객체를 사용할 수 있습니다.
                co.inUse = false;
                break;
            }
        }
        //Connection의 수가 10개를 넘었으면 제거합니다.
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
            //삭제하려는 Connection 객체를 찾습니다.
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
     * 실제의 커넥션을 만들어 돌려줌
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
            //실제의 Connection이 생성됩니다.
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
    public boolean inUse = false; //Connection 의 사용 여부
    //useFlag: Client가 객체를 사용하는지 여부 지정
    //true는 현재 Client가 사용하고 있는 객체라는 뜻
    public ConnectionObject(Connection c, boolean useFlag) {
        connection = c;
        inUse = useFlag;
    }
}