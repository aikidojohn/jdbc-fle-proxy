package com.johnhite.sandbox.fle.db.proxy;

import java.util.HashMap;
import java.util.Map;

public class EncryptionConf {
    private Map<String, FieldConf> fields = new HashMap<>();

    private static EncryptionConf instance = new EncryptionConf();
    public static EncryptionConf getInstance() {
        return instance;
    }
    public void addField(FieldConf conf) {
        fields.put(conf.getFieldId(), conf);
    }
    public FieldConf getField(String fieldId) {
        return fields.get(fieldId);
    }

    public FieldConf getField(String table, String field) {
        return fields.get(table + "." + field);
    }
}
