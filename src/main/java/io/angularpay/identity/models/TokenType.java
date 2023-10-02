package io.angularpay.identity.models;

public enum TokenType {
    ACCESS_TOKEN("Bearer"), REFRESH_TOKEN("Refresh");

    private final String type;

    TokenType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
