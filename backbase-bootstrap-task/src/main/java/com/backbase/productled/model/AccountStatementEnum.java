package com.backbase.productled.model;

import java.time.LocalDate;
import lombok.Getter;

/**
 * Enum to store account statement type, name and its location
 */
@Getter
public enum AccountStatementEnum {

    MONTHLY_STATEMENT("Monthly Statements", "Monthly Statement from %s to %s", "documents/Monthly Statement.pdf"),
    TAX_STATEMENT("Tax Statements", "Tax statement from %s to %s", "documents/Tax statement.pdf"),
    NOTIFICATION("Notifications & Letter", "Notification %s", "documents/Notification.pdf"),
    OTHERS("Others", "Telephone scam warning %s", "documents/Telephone scam warning.pdf");

    private final String statementType;
    private final String statementName;
    private final String filename;

    AccountStatementEnum(String statementType, String statementName, String filename) {
        this.statementType = statementType;
        this.filename = filename;
        this.statementName = statementName;
    }

    public String getStatementName() {
        return String.format(statementName, LocalDate.now().minusMonths(1), LocalDate.now());
    }

}
