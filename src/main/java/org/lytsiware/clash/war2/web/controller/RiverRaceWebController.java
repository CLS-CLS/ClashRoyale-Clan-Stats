package org.lytsiware.clash.war2.web.controller;

import org.lytsiware.clash.war2.service.RiverRaceWebService;
import org.lytsiware.clash.war2.web.dto.RiverRaceViewDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest")
public class RiverRaceWebController {

    @Autowired
    RiverRaceWebService riverRaceWebService;

    @RequestMapping("/riverrace/{delta}")
    public RiverRaceViewDto getRiverRace(@PathVariable(value = "delta") int delta) {
        return riverRaceWebService.getRiverRace(delta);
    }
}