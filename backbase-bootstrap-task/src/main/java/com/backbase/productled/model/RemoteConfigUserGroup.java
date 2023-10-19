package com.backbase.productled.model;

import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RemoteConfigUserGroup {
    RemoteConfigRoleEnum role;
    Set<String> users;
}
