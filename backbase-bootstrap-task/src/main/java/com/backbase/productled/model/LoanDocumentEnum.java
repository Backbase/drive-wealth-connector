package com.backbase.productled.model;

import java.time.LocalDate;
import lombok.Getter;

@Getter
public enum LoanDocumentEnum {
    LOAN_AGREEMENT("Loan Agreement", "SME Loan Agreement %s", "documents/SME Loan Agreement.pdf");

    private final String documentType;
    private final String documentName;
    private final String filename;

    LoanDocumentEnum(String documentType, String documentName, String filename) {
        this.documentType = documentType;
        this.filename = filename;
        this.documentName = documentName;
    }

    public String getDocumentName() {
        return String.format(documentName, LocalDate.now().minusMonths(1), LocalDate.now());
    }

}
