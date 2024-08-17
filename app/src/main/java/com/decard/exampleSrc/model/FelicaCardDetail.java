package com.decard.exampleSrc.model;

import lombok.Data;

@Data
public class FelicaCardDetail {
    private final GeneralInfo generalInfo;
    private final IssuerInfo issuerInfo;
    private final PersonalInfo personalInfo;
    private final AttributeInfo attributeInfo;
    private final EPurseInfo ePurseInfo;
    private final OperatorInfo operatorInfo;
    private final HistoryRecord lastTransaction;
    private final GateAccessLog gateAccessLog;
}
