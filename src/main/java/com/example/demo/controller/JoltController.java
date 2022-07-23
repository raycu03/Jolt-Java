package com.example.demo.controller;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import com.google.gson.Gson;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test")
public class JoltController {

    @GetMapping()
    public ResponseEntity getAccesToken(@RequestBody EntityDTO json) {
        Gson gson = new Gson();
        String JSON = gson.toJson(json);
        List chainrSpecJSON = JsonUtils.classpathToList("/static/spec.json");
        Chainr chainr = Chainr.fromSpec( chainrSpecJSON );
        //Object inputJSON = JsonUtils.classpathToObject("/static/input.json");
        Object inputJSON = JsonUtils.jsonToObject(JSON);
        Object transformedOutput = chainr.transform( inputJSON );
        return ResponseEntity.ok(JsonUtils.toJsonString( transformedOutput ));
    }
}
