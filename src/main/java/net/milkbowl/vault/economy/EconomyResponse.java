package net.milkbowl.vault.economy;

public class EconomyResponse {
    public static final int SUCCESS = 1;
    public static final int FAILURE = 2;
    public static final int NOT_IMPLEMENTED = 3;

    public final double amount;
    public final double balance;
    public final ResponseType type;
    public final String errorMessage;

    public enum ResponseType {
        SUCCESS,
        FAILURE,
        NOT_IMPLEMENTED
    }

    public EconomyResponse(double amount, double balance, ResponseType type, String errorMessage) {
        this.amount = amount;
        this.balance = balance;
        this.type = type;
        this.errorMessage = errorMessage;
    }

    public boolean transactionSuccess() {
        return type == ResponseType.SUCCESS;
    }
}
