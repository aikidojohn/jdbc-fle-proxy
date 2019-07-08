package com.johnhite.sandbox.fle.db.proxy;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TableConf {
    private String tableName;
    private Map<String, FieldConf> fields = new HashMap<>();
    private String keyIdColumn;
    private String keyIdExtra;

    public TableConf(String name, String keyIdColumn, String keyIdExtra) {
        this.tableName = name;
        this.keyIdColumn = keyIdColumn;
        this.keyIdExtra = keyIdExtra;
    }

    public String getTableName() {
        return tableName;
    }

    public String getKeyIdColumn() {
        return keyIdColumn;
    }

    public String getKeyIdExtra() {
        return keyIdExtra;
    }

    public void addField(FieldConf conf) {
        fields.put(conf.getFieldId(), conf);
    }

    public Optional<FieldConf> getField(String fieldId) {
        return Optional.ofNullable(fields.get(fieldId));
    }
}
