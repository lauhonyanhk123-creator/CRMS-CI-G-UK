package com.crms.security;

import org.casbin.jcasbin.main.Enforcer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CasbinConfig {

    @Bean
    public Enforcer casbinEnforcer() {
        // Create Enforcer with basic RBAC model
        // In production, load model from classpath and policies from database via hibernate-adapter
        Enforcer enforcer = new Enforcer();
        
        // Set basic RBAC model
        enforcer.getModel().addDef("r", "r", "sub, obj, act");
        enforcer.getModel().addDef("p", "p", "sub, obj, act");
        enforcer.getModel().addDef("e", "e", "some(where (p.eft == allow))");
        enforcer.getModel().addDef("m", "m", "r.sub == p.sub && keyMatch2(r.obj, p.obj) && regexMatch(r.act, p.act)");
        
        // Add default policies
        enforcer.addPolicy("admin", "/api/**", "(GET)|(POST)|(PUT)|(DELETE)|(PATCH)");
        enforcer.addPolicy("user", "/api/**", "(GET)");
        
        return enforcer;
    }
}
