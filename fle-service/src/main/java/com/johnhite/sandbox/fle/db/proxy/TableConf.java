package com.johnhite.sandbox.fle.db.proxy;

import javax.validation.constraints.Max;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TableConf {
    private String tableName;
    private Map<String, FieldConf> fields = new HashMap<>();
    private String keyIdColumn;
    private String keyIdExtra;
    private Map<String, String> keyIdAttrs = new HashMap<>();

    public TableConf(String name, String keyIdColumn, String keyIdExtra) {
        this.tableName = name;
        this.keyIdColumn = keyIdColumn;
        this.keyIdExtra = keyIdExtra;
        String[] ids = keyIdColumn.split(",");
        String[] idNames = keyIdExtra.split(",");
        for (int i=0; i< ids.length; i++) {
            keyIdAttrs.put(ids[i], idNames[i]);
        }
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

    public Map<String,String> getKeyIdAttrs() {
        return keyIdAttrs;
    }

    public void addField(FieldConf conf) {
        fields.put(conf.getFieldId(), conf);
    }

    public Optional<FieldConf> getField(String fieldId) {
        return Optional.ofNullable(fields.get(fieldId));
    }
}
