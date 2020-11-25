package org.lytsiware.clash.war2.web.controller;

import org.lytsiware.clash.war2.service.PromotionService;
import org.lytsiware.clash.war2.service.RiverRaceWebService;
import org.lytsiware.clash.war2.web.dto.ParticipantDto;
import org.lytsiware.clash.war2.web.dto.RiverRaceViewDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/rest")
public class RiverRaceWebController {

    @Autowired
    RiverRaceWebService riverRaceWebService;

    @Autowired
    PromotionService promotionService;

    @GetMapping("/riverrace/{delta}")
    public RiverRaceViewDto getRiverRace(@PathVariable(value = "delta") int delta) {
        return riverRaceWebService.getRiverRace(delta);
    }

    @GetMapping("/riverrace/player/{tag}")
    public List<ParticipantDto> getRiverRace(@PathVariable(value = "tag") String tag) {
        return riverRaceWebService.getRiverRaceParticipant(tag);
    }

    @GetMapping("/riverrace/promotions/static")
    public List<PromotionService.PromotionDiff> getPromotionStaticData() {
        return Arrays.asList(PromotionService.PromotionDiff.values());
    }

    @GetMapping("/riverrace/promotions")
    public List<PromotionService.PlayerPromotionDto> promotions() {
        return promotionService.calculatePromotions();
    }
}
