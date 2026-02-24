package com.example.flinkkafka.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class Transaction implements Serializable {

    @JsonProperty("transaction_id")
    private String transaction_id;

    @JsonProperty("event_time")
    private Long event_time;

    @JsonProperty("user_id")
    private String user_id;

    @JsonProperty("amount")
    private Double amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("category")
    private String category;

    public Transaction() {}

    public Transaction(String transaction_id, Long event_time, String user_id,
                       Double amount, String currency, String category) {
        this.transaction_id = transaction_id;
        this.event_time = event_time;
        this.user_id = user_id;
        this.amount = amount;
        this.currency = currency;
        this.category = category;
    }

    public String getTransaction_id() { return transaction_id; }
    public void setTransaction_id(String transaction_id) { this.transaction_id = transaction_id; }

    public Long getEvent_time() { return event_time; }
    public void setEvent_time(Long event_time) { this.event_time = event_time; }

    public String getUser_id() { return user_id; }
    public void setUser_id(String user_id) { this.user_id = user_id; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    @Override
    public String toString() {
        return "Transaction{" +
                "transaction_id='" + transaction_id + '\'' +
                ", event_time=" + event_time +
                ", user_id='" + user_id + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}
