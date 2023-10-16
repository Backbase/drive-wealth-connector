package com.backbase.modelbank.mapper;


import org.mapstruct.Mapping;

@Mapping(target = "confirmedValue.amount", source = "totalOrderAmount")
@Mapping(target = "confirmedValue.currency", constant = "USD")
@Mapping(target = "confirmedQuantity", source = "cumulativeQuantity")
@Mapping(target = "confirmedCommissionFees.amount", source = "fees")
@Mapping(target = "confirmedCommissionFees.currency", constant = "USD")
@Mapping(target = "confirmedInstrumentPrice.currency", constant = "USD")
@Mapping(target = "confirmedFXCurrencyCommission.amount", expression = "java(java.math.BigDecimal.ZERO)")
@Mapping(target = "confirmedFXCurrencyCommission.currency", constant = "USD")
public @interface UpdateOrderMapping {

}
