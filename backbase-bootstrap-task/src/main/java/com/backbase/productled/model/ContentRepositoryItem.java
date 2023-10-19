package com.backbase.productled.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @copyright (C) 2022, Backbase
 * @Version 1.0
 * @Since 01. Nov 2022 4:17 pm
 */
@Data
@Builder
@ToString
public class ContentRepositoryItem {

    private String repositoryId;
    private List<ContentTemplate> templates;

}
