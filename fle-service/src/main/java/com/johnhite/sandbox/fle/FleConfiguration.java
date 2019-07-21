package com.johnhite.sandbox.fle;


import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rtr.wizard.RequiredConfiguration;

import io.dropwizard.db.DataSourceFactory;

public class FleConfiguration extends RequiredConfiguration {

    @Valid
    @NotNull
    @JsonProperty
    private DataSourceFactory database = new DataSourceFactory();

    public DataSourceFactory getDatabase() {
        return database;
    }

    @JsonProperty("field_level_encryption")
    private FieldLevelEncryptionConf fieldLevelEncryption = new FieldLevelEncryptionConf();

    public FieldLevelEncryptionConf getFieldLevelEncryption() {
        return fieldLevelEncryption;
    }
}