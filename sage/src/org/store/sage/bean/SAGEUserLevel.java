package org.store.sage.bean;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SAGEUserLevel {

    public static Logger log = Logger.getLogger(SAGEUserLevel.class);
    private Integer id;
    private String name;

    public SAGEUserLevel(Connection connection, Integer id) {
        this.id = id;
        loadFromDb(connection);
    }

    private void loadFromDb(Connection connection) {
        try {
            PreparedStatement stmt1 = connection.prepareStatement("SELECT CT_Intitule FROM P_CATTARIF where cbMarq=?");
            stmt1.setInt(1, getId());
            ResultSet rs1 = stmt1.executeQuery();
            if (rs1.next()) setName(rs1.getString(1));
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

    public SAGEUserLevel(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SAGEUserLevel that = (SAGEUserLevel) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
