package com.tuplugin.multas.model;

import java.time.Instant;
import java.util.UUID;

public class Multa {
    private final int id;
    private final UUID target;
    private final String issuer;
    private final double amount;
    private final String reason;
    private final Instant date;
    private String type;
    private String importance;
    private String state;

    public Multa(int id, UUID target, String issuer, double amount, String reason, Instant date, String type, String importance, String state){
        this.id = id;
        this.target = target;
        this.issuer = issuer;
        this.amount = amount;
        this.reason = reason;
        this.date = date;
        this.type = type;
        this.importance = importance;
        this.state = state;
    }

    public int getId(){ return id; }
    public UUID getTarget(){ return target; }
    public String getIssuer(){ return issuer; }
    public double getAmount(){ return amount; }
    public String getReason(){ return reason; }
    public Instant getDate(){ return date; }
    public String getType(){ return type; }
    public String getImportance(){ return importance; }
    public String getState(){ return state; }
    public void setImportance(String importance){ this.importance = importance; }
    public void setState(String state){ this.state = state; }
}
