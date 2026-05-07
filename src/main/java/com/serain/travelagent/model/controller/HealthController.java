package com.serain.travelagent.model.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @BelongsProject: travelAgent
 * @BelongsPackage: com.serain.travelagent.model.controller
 * @Author: Serain
 * @CreateTime: 2026-05-07  11:00
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public String healthCheck() {
        return "ok";
    }
}

