package com.inventory.actuator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
@Component
public class CustomInfo implements InfoContributor {
    @Autowired
    UserRepo userRepo;
    @Override
    public void contribute(Info.Builder builder) {
        Map<String,Long> map=new HashMap<>();
        map.put("active", userRepo.countBasedOnStatus("active"));
        map.put("inactive", userRepo.countBasedOnStatus("inActive"));
        builder.withDetail("userCount",map);
    }
}
