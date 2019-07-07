package com.johnhite.sandbox.fle.db.proxy;

public class FieldConf {
    private String fieldId;
    private EncryptionType encryptionType = EncryptionType.BLOCK;
    private EncryptionAlgorithm algorithm = EncryptionAlgorithm.AES;
    private FieldFormat format = FieldFormat.BINARY;

    public FieldConf(String fieldId, EncryptionType encryptionType, EncryptionAlgorithm algorithm, FieldFormat format) {
        this.fieldId = fieldId;
        this.encryptionType = encryptionType;
        this.algorithm = algorithm;
        this.format = format;
    }

    public FieldConf(String fieldId, FieldFormat format) {
        this.fieldId = fieldId;
        this.encryptionType = format == FieldFormat.BINARY ? EncryptionType.BLOCK : EncryptionType.FORMAT_PRESERVING;
        this.algorithm = EncryptionAlgorithm.AES;
        this.format = format;
    }

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public EncryptionType getEncryptionType() {
        return encryptionType;
    }

    public void setEncryptionType(EncryptionType encryptionType) {
        this.encryptionType = encryptionType;
    }

    public EncryptionAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(EncryptionAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public FieldFormat getFormat() {
        return format;
    }

    public void setFormat(FieldFormat format) {
        this.format = format;
    }

    public enum EncryptionType {
        BLOCK,
        FORMAT_PRESERVING;
    }

    public enum FieldFormat {
        BINARY,
        ENGLISH_TEXT,
        ENGLISH_NAME,
        DOMAIN,
        EMAIL,
        CREDIT_CARD,
        NUMBER,
        NUMBER_FIXED_WIDTH;
    }

    public enum EncryptionAlgorithm {
        AES,
        FFX;
    }

}
