package com.backbase.productled.model;

import com.backbase.dbs.accesscontrol.api.service.v3.model.PresentationPermissionSetItem;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.List;

/**
 * @copyright (C) 2022, Backbase
 * @Version 1.0
 * @Since 19. Oct 2022 10:33 am
 */
@Data
@JsonPropertyOrder({"name", "description", "permissions"})
public class AdminApsModel {

    private String internalId;
    private String name;
    private String description;
    private List<PermissionSetItem> permissions = null;
}
