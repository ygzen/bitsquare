package io.bitsquare.util;

import com.google.bitcoin.core.Sha256Hash;

import java.io.UnsupportedEncodingException;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class JsonEntity {

    protected Sha256Hash hash;

    @JsonIgnore
    public abstract JsonMapper getMapper();

    public JsonEntity() {
    }

    @JsonIgnore
    public final Sha256Hash getHash() {
        if (hash == null) {
            try {
                // calculate SHA-256 Hash id for offer json document
                // Change this to "UTF-16" if needed
                hash = Sha256Hash.create(toJson().getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof JsonEntity && ((JsonEntity) obj).getHash().equals(getHash()));
    }

    @Override
    public int hashCode() {
        return getHash().hashCode();
    }


    @Override
    public String toString() {

        return toJson();
    }

    @SuppressWarnings("unchecked")
    public final String toJson() {
        return getMapper().toJson(this);
    }

}
