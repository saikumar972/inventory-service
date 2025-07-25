package com.inventory.actuator;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.actuate.endpoint.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Component
@Endpoint(id = "releaseNotes")
public class CustomController {
    Map<String, List<String>> map=new HashMap<>();
    @PostConstruct
    public void postConstruct(){
        map.put("release 1.0", Arrays.asList("home page","auth"));
        map.put("release 2.0", Arrays.asList("middle page","authorization"));
        map.put("release 3.0", Arrays.asList("completed page","jwt"));
    }
    @ReadOperation
    public Map<String,List<String>> getNotes(){
        return map;
    }
    @ReadOperation
    public List<String> getListBasedOnVersion(@Selector String version){
        return map.get(version);
    }
    @WriteOperation
    public void addNotes(@Selector String version,String notes){
        map.put(version,Arrays.stream(notes.split(",")).toList());
    }

    @DeleteOperation
    public void deleteByVersion(@Selector String release){
        map.remove(release);
    }
}
