package org.lytsiware.clash.war2.web.controller;

import org.lytsiware.clash.war2.service.RiverRaceWebService;
import org.lytsiware.clash.war2.web.dto.RiverRaceViewDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest")
public class RiverRaceWebController {

    @Autowired
    RiverRaceWebService riverRaceWebService;

    @RequestMapping("/riverrace")
    public RiverRaceViewDto getRiverRace(@RequestParam(value = "delta", defaultValue = "0") int delta) {
        return riverRaceWebService.getRiverRace(delta);
    }
}
