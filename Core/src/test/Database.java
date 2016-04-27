package test;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Rogelio Caballero
 * 4/10/11 5:12
 */
public class Database {

    public static void main(String[] args) {
        importDb();
    }

    public static void importDb() {

        try {
            // source database
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
            Connection conn1 = DriverManager.getConnection("jdbc:sqlserver://192.168.1.103;databaseName=store2", "sa", "masterkey");
            DatabaseMetaData metadata = conn1.getMetaData();
            ResultSet rs = metadata.getTables(conn1.getCatalog(), "dbo", null, new String[]{"TABLE"});
            ArrayList<Map<Object, Serializable>> tables = new ArrayList<Map<Object, Serializable>>();
            while (rs.next()) {
                String nombre = rs.getString("TABLE_NAME");
                if (!"t_store_path".equalsIgnoreCase(nombre) && !"t_product_audit_stock".equalsIgnoreCase(nombre) ) {
                    Map<Object, Serializable> map = new HashMap<Object, Serializable>();
                    map.put("TABLE_NAME", nombre);

                    ResultSet rsC = metadata.getColumns(conn1.getCatalog(), null, nombre, null);
                    ArrayList<Map<String, String>> listaCols = new ArrayList<Map<String, String>>();

                    while (rsC.next()) {
                        HashMap<String, String> mapC = new HashMap<String, String>();
                        mapC.put("COLUMN_NAME", rsC.getString("COLUMN_NAME"));
                        mapC.put("COLUMN_SIZE", rsC.getString("COLUMN_SIZE"));
                        mapC.put("TYPE_NAME", rsC.getString("TYPE_NAME"));
                        mapC.put("DECIMAL_DIGITS", rsC.getString("DECIMAL_DIGITS"));
                        mapC.put("NULLABLE", rsC.getString("NULLABLE"));
                        listaCols.add(mapC);
                    }

                    map.put("columnas", listaCols);
                    tables.add(map);
                }
            }

            // target database
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection conn2 = DriverManager.getConnection("jdbc:mysql://localhost/cartronic", "root", "root");
            conn2.setAutoCommit(true);
            // obtener datos

            for (Map m : tables) {
                String tableName = (String) m.get("TABLE_NAME");
                ArrayList<Map<String, String>> columns = (ArrayList<Map<String, String>>) m.get("columnas");

                StringBuilder sbFields = new StringBuilder();
                StringBuilder sbValues = new StringBuilder();
                for (Map map : columns) {
                    if (StringUtils.isNotEmpty(sbFields.toString())) sbFields.append(",");
                    sbFields.append(map.get("COLUMN_NAME"));
                    if (StringUtils.isNotEmpty(sbValues.toString())) sbValues.append(",");
                    sbValues.append("?");
                }

                String insert = "insert into " + tableName + " (" + sbFields.toString() + ") values (" + sbValues + ")";
                PreparedStatement stmtIns = conn2.prepareStatement(insert);

                PreparedStatement stmt = conn1.prepareStatement("select * from " + tableName);
                ResultSet resultSet = stmt.executeQuery();
                int record = 1;
                while (resultSet.next()) {
                    System.out.println("Table " + tableName + " Record: " + record++);
                    int index = 1;
                    for (Map map : columns) {
                        Object value = resultSet.getObject((String) map.get("COLUMN_NAME"));
                        stmtIns.setObject(index++, value);
                    }
                    stmtIns.addBatch();

                    if (record % 1000 == 0) {
                        stmtIns.executeBatch();
                        stmtIns.clearBatch(); //not sure if this is necessary
                    }

                }
                stmtIns.executeBatch();

            }


        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

}
