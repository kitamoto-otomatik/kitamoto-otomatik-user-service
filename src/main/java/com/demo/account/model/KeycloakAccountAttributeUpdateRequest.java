package com.demo.account.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class KeycloakAccountAttributeUpdateRequest {
    private Map<String, List<String>> attributes;
}
