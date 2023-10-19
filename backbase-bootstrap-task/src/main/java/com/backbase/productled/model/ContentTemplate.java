package com.backbase.productled.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.File;

/**
 * @copyright (C) 2022, Backbase
 * @Version 1.0
 * @Since 01. Nov 2022 5:04 pm
 */
@Data
@Builder
@ToString
public class ContentTemplate {
    private String pathInRepository;
    private File file;
}
