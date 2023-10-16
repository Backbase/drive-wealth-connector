package com.backbase.productled.mapper;

import com.backbase.dbs.accesscontrol.api.service.v3.model.PresentationPermissionSet;
import com.backbase.productled.model.AdminApsModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

/**
 * @copyright (C) 2022, Backbase
 * @Version 1.0
 * @Since 19. Oct 2022 4:09 pm
 */
@Mapper(componentModel = "spring")
public interface PermissionSetMapper {
    PresentationPermissionSet map(AdminApsModel adminAps);

}
