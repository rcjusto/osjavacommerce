package org.store.core.globals;

import org.hibernate.dialect.MySQL5Dialect;

import java.sql.Types;

public class MySQL503Dialect extends MySQL5Dialect {

    public MySQL503Dialect() {
        super();
        registerColumnType(Types.BIT, "boolean");
    }

}
