package com.johnhite.sandbox.fle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.johnhite.sandbox.fle.crypto.Encryption;
import com.johnhite.sandbox.fle.db.proxy.EncryptionConf;
import com.johnhite.sandbox.fle.db.proxy.FieldConf;
import com.johnhite.sandbox.fle.db.proxy.TableConf;

import java.util.HashMap;
import java.util.Map;

public class FieldLevelEncryptionConf {
    private Map<String, FLETableConf> tables = new HashMap<>();

    @JsonIgnore
    public void createEncryptionConf() {
        for (Map.Entry<String, FLETableConf> entry : tables.entrySet()) {
            FLETableConf tc = entry.getValue();
            TableConf table = new TableConf(entry.getKey(), tc.getKeyIdColumn(), tc.getKeyIdName());
            for (Map.Entry<String, FieldConf.FieldFormat> fe : tc.getFields().entrySet()) {
                table.addField(new FieldConf(table.getTableName() + "." + fe.getKey(), fe.getValue()));
            }
            EncryptionConf.getInstance().addTable(table);
        }
    }

    public Map<String, FLETableConf> getTables() {
        return tables;
    }

    public void setTables(Map<String, FLETableConf> tables) {
        this.tables = tables;
    }

    private static class FLETableConf {
        private String keyIdColumn;
        private String keyIdName;
        private Map<String, FieldConf.FieldFormat> fields = new HashMap<>();

        public String getKeyIdColumn() {
            return keyIdColumn;
        }

        public void setKeyIdColumn(String keyIdColumn) {
            this.keyIdColumn = keyIdColumn;
        }

        public String getKeyIdName() {
            return keyIdName;
        }

        public void setKeyIdName(String keyIdName) {
            this.keyIdName = keyIdName;
        }

        public Map<String, FieldConf.FieldFormat> getFields() {
            return fields;
        }

        public void setFields(Map<String, FieldConf.FieldFormat> fields) {
            this.fields = fields;
        }
    }

}
