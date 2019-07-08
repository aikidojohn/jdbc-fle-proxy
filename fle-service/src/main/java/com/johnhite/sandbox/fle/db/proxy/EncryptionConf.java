package com.johnhite.sandbox.fle.db.proxy;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EncryptionConf {
    private Map<String, TableConf> tableConf = new HashMap<>();

    private static EncryptionConf instance = new EncryptionConf();
    public static EncryptionConf getInstance() {
        return instance;
    }

    public Optional<FieldConf> getField(String fieldId) {
        String table = fieldId.split(".")[0];
        TableConf conf = tableConf.get(table);
        if (conf != null) {
            return conf.getField(fieldId);
        }
        return Optional.empty();
    }

    public Optional<FieldConf> getField(String table, String field) {
        TableConf conf = tableConf.get(table);
        if (conf != null) {
            return conf.getField(table + "." + field);
        }
        return Optional.empty();
    }

    public Optional<TableConf> getTable(String table) {
        return Optional.ofNullable(tableConf.get(table));
    }

    public void addTable(TableConf conf) {
        tableConf.put(conf.getTableName(), conf);
    }
}
