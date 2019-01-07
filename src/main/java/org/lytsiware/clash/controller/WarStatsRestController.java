package org.lytsiware.clash.controller;

import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.converter.InputDtoPlayerWarStatConverter;
import org.lytsiware.clash.domain.war.aggregation.PlayerAggregationWarStats;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.dto.ClansWarGlobalStatsDto;
import org.lytsiware.clash.dto.PlaywerWarStatsWithAvgsDto;
import org.lytsiware.clash.dto.war.input.WarStatsInputDto;
import org.lytsiware.clash.service.war.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityExistsException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/rest/")
public class WarStatsRestController {


    @Autowired
    WarUploadService warUploadService;

    @Autowired
    PlayerAggregationWarStatsService playerAggregationWarStatsService;

    @Autowired
    PlayerWarStatsService playerWarStatsService;

    @Autowired
    WarInputServiceImpl warInputService;


    @PostMapping(value = "/tagFile")
    public void tagFile(@RequestParam("file") MultipartFile file, Model model, HttpServletResponse response) throws IOException {
        log.info("START tagFile");
        String replaced = warUploadService.replaceNameWithTag(file.getInputStream(), file.getOriginalFilename());
        response.getOutputStream().write(replaced.getBytes(StandardCharsets.UTF_8));
        response.setContentType("application/text");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getOriginalFilename() + "\"");
        response.flushBuffer();

    }

    @GetMapping(value = "/warstats/recalculate")
    public ResponseEntity recalculate(@RequestParam(required = false, defaultValue = "" + WarConstants.leagueSpan) int span,
                                      @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate from,
                                      @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate to) {

        if (to == null) {
            to = LocalDate.now();
        }
        playerAggregationWarStatsService.calculateStatsBetweenDates(from, to, span);
        return ResponseEntity.ok().build();

    }

    @GetMapping(value = "/player/{tag}/war")
    public PlaywerWarStatsWithAvgsDto playerWarStats(@PathVariable("tag") String tag) {
        return playerWarStatsService.getPlayerWarStatsForWeek(tag, Week.now().getEndDate());
    }

    @PostMapping("/uploadwarstats")
    @Transactional(propagation = Propagation.REQUIRED)
    public void uploadWarStats(@RequestParam("file") MultipartFile[] files, Model model) throws IOException {
        log.info("START uploadWarStats");
        warUploadService.upload(files);
    }

    @GetMapping("/calculatewarstats")
    public void calculateWarStats() {
        log.info("START calculating missing war stats");
        playerAggregationWarStatsService.calculateMissingStats(null, null);
    }


    @GetMapping("/warstats/{deltaWeek}")
    public ClansWarGlobalStatsDto getWarStatsForWeeks(@PathVariable(value = "deltaWeek", required = false) Integer deltaWeek) {
        log.info("START warStats deltaWeek = {}", deltaWeek);

        List<PlayerAggregationWarStats> playerAggregationWarStats = playerAggregationWarStatsService.findLatestWarAggregationStatsForWeek(Week.now().minusWeeks(deltaWeek));

        return new ClansWarGlobalStatsDto(playerAggregationWarStats);

    }

    @PostMapping("warstats/playersNotParticipated/{date}")
    public List<WarStatsInputDto.PlayerWarStatInputDto> getPlayersNotParticipated(@PathVariable("date") @DateTimeFormat(pattern = "yyyy-MM-dd'T'hh:mm") LocalDateTime date,
                                                                                  @RequestBody List<WarStatsInputDto.PlayerWarStatInputDto> participants) {

        return playerWarStatsService.findPlayersNotParticipatedInWar(WarStatsInputDto.builder().playerWarStats(participants).build(), date, null).keySet().stream()
                .map(entry -> WarStatsInputDto.PlayerWarStatInputDto.zeroFieldPlayerWarStatInputDto(entry.getTag(), entry.getName()))
                .collect(Collectors.toList());

    }

    @GetMapping("/warstats/inputdata")
    public List<WarStatsInputDto> getWarStatsForInput(@RequestParam(required = false, defaultValue = "true") boolean includeNotParticipating) {
        return warInputService.getPlayerWarStatsForInput(includeNotParticipating);
    }


    @PostMapping("/warstats/inputdata")
    public ResponseEntity<Optional<String>> insertWarStats(@Valid @RequestBody WarStatsInputDto warStatsInputDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.ok(bindingResult.getAllErrors().stream().map(ObjectError::getCodes)
                    .filter(Objects::nonNull).filter(t -> t.length >= 1).map(t -> t[0])
                    .reduce((l, r) -> l + "\r\n" + r));
        }

        List<PlayerWarStat> statsList = InputDtoPlayerWarStatConverter.toPlayerWarStat(warStatsInputDto);
        try {
            playerWarStatsService.savePlayerWarStats(statsList);
        } catch (EntityExistsException ex) {
            return ResponseEntity.ok(Optional.of("League Already exists"));
        }


        return ResponseEntity.ok().build();
    }

}
