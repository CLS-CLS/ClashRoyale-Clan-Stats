package org.lytsiware.clash.controller;

import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.converter.InputDtoPlayerWarStatConverter;
import org.lytsiware.clash.domain.war.league.WarLeagueRepository;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.dto.ClansWarGlobalStatsDto;
import org.lytsiware.clash.dto.PlaywerWarStatsWithAvgsDto;
import org.lytsiware.clash.dto.WarLeagueDto;
import org.lytsiware.clash.dto.WarLeagueWithParticipantsDto;
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

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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

    @Autowired
    WarLeagueRepository warLeagueRepository;


    @Autowired
    WarLeagueService warLeagueService;


    @PostMapping(value = "/tagFile")
    public void tagFile(@RequestParam("file") MultipartFile file, Model model, HttpServletResponse response) throws IOException {
        log.info("Controller: tagFile");
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
        log.info("Controller: recalculate");
        if (to == null) {
            to = LocalDate.now();
        }
        playerAggregationWarStatsService.calculateStatsBetweenDates(from, to, span);
        return ResponseEntity.ok().build();

    }

    @GetMapping(value = "/player/{tag}/war")
    public PlaywerWarStatsWithAvgsDto playerWarStats(@PathVariable("tag") String tag) {
        log.info("Controller: playerWarStats");
        return playerWarStatsService.getLatestPlayerWarStatsUntil(tag, Week.now().getEndDate());
    }

    @PostMapping("/uploadwarstats")
    @Transactional(propagation = Propagation.REQUIRED)
    public void uploadWarStats(@RequestParam("file") MultipartFile[] files, Model model) throws IOException {
        log.info("Controller: uploadWarStats");
        warUploadService.upload(files);
    }


    @GetMapping("/warstats/{deltaWar}")
    public ClansWarGlobalStatsDto getAggregatedWarStats(@PathVariable(value = "deltaWar", required = false) Integer deltaWar) {
        log.info("Controller: getAggregatedWarStats deltaWeek = {}", deltaWar);
        return playerAggregationWarStatsService.findLatestWarAggregationStatsForWar(deltaWar == null ? 0 : deltaWar);

    }

    @GetMapping("/warstats/single/{deltaWar}")
    public WarLeagueWithParticipantsDto getSingleWarStats(@PathVariable(value = "deltaWar", required = false) Integer deltaWar) {
        log.info("Controller: getSingleWarStats deltaWeek = {}", deltaWar);
        return warLeagueService.findStatsForWarLeague(deltaWar == null ? 0 : deltaWar);
    }

    @PostMapping("warstats/playersNotParticipated/{date}")
    public List<WarStatsInputDto.PlayerWarStatInputDto> getPlayersNotParticipated(
            @PathVariable("date") @DateTimeFormat(pattern = "yyyy-MM-dd'T'hh:mm") LocalDateTime date,
            @RequestBody List<WarStatsInputDto.PlayerWarStatInputDto> participants) {
        log.info("Controller: playersNotParticipated");
        return playerWarStatsService.findPlayersNotParticipatedInWar(WarStatsInputDto.builder().playerWarStats(participants).build(), date, null).keySet().stream()
                .map(entry -> WarStatsInputDto.PlayerWarStatInputDto.zeroFieldPlayerWarStatInputDto(entry.getTag(), entry.getName()))
                .collect(Collectors.toList());

    }

    @GetMapping("/warstats/inputdata")
    public List<WarStatsInputDto> getWarStatsForInput(@RequestParam(required = false, defaultValue = "true") boolean includeNotParticipating) {
        log.info("Controller: getWarStatsForInput");
        return warInputService.getPlayerWarStatsForInput(includeNotParticipating);
    }


    @PostMapping("/warstats/inputdata")
    public ResponseEntity<String> insertWarStats(@Valid @RequestBody WarStatsInputDto warStatsInputDto, BindingResult bindingResult) {
        log.info("Controller: insertWarStats");
        if (bindingResult.hasErrors()) {
            return ResponseEntity.ok(bindingResult.getAllErrors().stream().map(ObjectError::getCodes)
                    .filter(Objects::nonNull).filter(t -> t.length >= 1).map(t -> t[0])
                    .reduce((l, r) -> l + "\r\n" + r).orElse(""));
        }

        List<PlayerWarStat> statsList = InputDtoPlayerWarStatConverter.toPlayerWarStat(warStatsInputDto);
        CompletableFuture<String> result = playerWarStatsService.saveWarStatsAndUpdateStatistics(statsList);

        try {
            return ResponseEntity.ok(result.get(2, TimeUnit.SECONDS));
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(500).body(e.getMessage() + " cause: " + Optional.ofNullable(e.getCause()).map(Throwable::getMessage).orElse(""));
        } catch (TimeoutException e) {
            return ResponseEntity.ok().build();
        }
    }

    @GetMapping("warstats/warleague")
    public List<WarLeagueDto> getLatestWarLeagues() {
        log.info("Controller: getLatestWarLeagues");
        return warLeagueService.findFirstNthWarLeagueBeforeDate(LocalDate.now().plusDays(1), 100);
    }

}
