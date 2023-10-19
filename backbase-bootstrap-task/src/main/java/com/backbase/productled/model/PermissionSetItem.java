package com.backbase.productled.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @copyright (C) 2022, Backbase
 * @Version 1.0
 * @Since 19. Oct 2022 11:25 am
 */
@Data
@JsonPropertyOrder({"functionId", "privileges"})
public class PermissionSetItem {

    private String functionId;
    private List<String> privileges = new ArrayList();
}
