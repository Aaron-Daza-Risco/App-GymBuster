package com.version.gymModuloControl.controller;

import com.version.gymModuloControl.dto.DashboardRecepcionistaDTO;
import com.version.gymModuloControl.service.DashboardRecepcionistaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard-recepcionista")
public class DashboardRecepcionistaController {

    @Autowired
    private DashboardRecepcionistaService dashboardRecepcionistaService;

    @PreAuthorize("hasRole('RECEPCIONISTA')")
    @GetMapping
    public DashboardRecepcionistaDTO getDashboard() {
        return dashboardRecepcionistaService.getDashboardData();
    }
}
