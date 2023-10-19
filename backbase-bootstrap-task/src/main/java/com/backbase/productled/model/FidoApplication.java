package com.backbase.productled.model;

import java.util.List;
import lombok.Data;

@Data
public class FidoApplication {

    private String appKey;
    private String appId;
    private List<String> trustedFacetIds;
}
