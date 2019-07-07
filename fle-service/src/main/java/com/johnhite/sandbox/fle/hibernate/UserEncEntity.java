package com.johnhite.sandbox.fle.hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "users_enc")
public class UserEncEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private byte[] mail;
    @Column(name="first_name")
    private byte[] firstName;
    @Column(name = "last_name")
    private byte[] lastName;
    private byte[] company;
    private byte[] address1;
    private byte[] address2;
    private byte[] address3;
    private String city;
    private String state;
    private String zip;
    private String country;
    @Column(name="phone_number")
    private byte[] phoneNumber;

    public UserEncEntity() {}

    public UserEncEntity(byte[] mail, byte[] firstName, byte[] lastName, byte[] company, byte[] address1, byte[] address2, byte[] address3, String city, String state, String zip, String country,
            byte[] phoneNumber) {
        this.mail = mail;
        this.firstName = firstName;
        this.lastName = lastName;
        this.company = company;
        this.address1 = address1;
        this.address2 = address2;
        this.address3 = address3;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.country = country;
        this.phoneNumber = phoneNumber;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public byte[] getMail() {
        return mail;
    }

    public void setMail(byte[] mail) {
        this.mail = mail;
    }

    public byte[] getFirstName() {
        return firstName;
    }

    public void setFirstName(byte[] firstName) {
        this.firstName = firstName;
    }

    public byte[] getLastName() {
        return lastName;
    }

    public void setLastName(byte[] lastName) {
        this.lastName = lastName;
    }

    public byte[] getCompany() {
        return company;
    }

    public void setCompany(byte[] company) {
        this.company = company;
    }

    public byte[] getAddress1() {
        return address1;
    }

    public void setAddress1(byte[] address1) {
        this.address1 = address1;
    }

    public byte[] getAddress2() {
        return address2;
    }

    public void setAddress2(byte[] address2) {
        this.address2 = address2;
    }

    public byte[] getAddress3() {
        return address3;
    }

    public void setAddress3(byte[] address3) {
        this.address3 = address3;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public byte[] getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(byte[] phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
