package com.crms.domain.user.enums;

public enum Role {
    ROLE_USER,
    ROLE_ADMIN,
    ROLE_OPS_DIRECTOR,
    ROLE_CONTRACTS_MANAGER,
    ROLE_QS,
    ROLE_SITE_AGENT,
    ROLE_ENGINEER,
    ROLE_PLANT_MANAGER,
    ROLE_BUYER,
    ROLE_FINANCE,
    ROLE_ESTIMATOR,
    ROLE_BID_MANAGER,
    ROLE_IT_ADMIN,
    ROLE_SUBCONTRACTOR,
    ROLE_AUDITOR,
    USER;

    public String getName() {
        return name();
    }
}
