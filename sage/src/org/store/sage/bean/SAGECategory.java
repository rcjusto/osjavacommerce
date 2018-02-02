package org.store.sage.bean;

import org.store.core.beans.Category;
import org.store.core.beans.CategoryLang;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SAGECategory {

    public static Logger log = Logger.getLogger(SAGECategory.class);
    private List<SAGECategoryItem> categories;

    public List<SAGECategoryItem> getCategories() {
        return categories;
    }

    public SAGECategory(Connection conn, Integer id1, Integer id2, Integer id3, Integer id4) {
        this.categories = new ArrayList<SAGECategoryItem>();
        if (id1 != null && id1>0) loadFromDb(conn, id1);
        if (id2 != null && id2>0) loadFromDb(conn, id2);
        if (id3 != null && id3>0) loadFromDb(conn, id3);
        if (id4 != null && id4>0) loadFromDb(conn, id4);
    }

    private void loadFromDb(Connection connection, Integer id) {
        if (id != null && id>0) {
            try {
                PreparedStatement stmt = connection.prepareStatement("select CL_Intitule from F_CATALOGUE where CL_No=?");
                SAGECategoryItem item = new SAGECategoryItem(id);
                stmt.setInt(1, id);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) item.setName(rs.getString(1));
                if (StringUtils.isNotEmpty(item.getName())) this.categories.add(item);
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }
    }


    public class SAGECategoryItem {
        private Integer id;
        private String name;

        SAGECategoryItem(Integer id) {
            this.id = id;
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

        public void copyToCategory(Category cat) {
            cat.setExternalCode(getId().toString());
        }

        public void copyToCategoryLang(CategoryLang cl) {
            cl.setCategoryName(getName());
        }
    }

}
