package org.lytsiware.clash.war2.service;

import org.junit.Assert;
import org.junit.Test;
import org.lytsiware.clash.core.domain.player.PlayerRepository;
import org.lytsiware.clash.donation.service.PlayerCheckInService;
import org.lytsiware.clash.war2.repository.RiverRaceRepository;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

public class PromotionServiceTest {

    private final PlayerCheckInService checkinService = Mockito.mock(PlayerCheckInService.class);
    private final PlayerRepository playerRepository = Mockito.mock(PlayerRepository.class);
    private final RiverRaceRepository riverRaceRepository = Mockito.mock(RiverRaceRepository.class);

    private final PromotionService promotionService = new PromotionService(checkinService, riverRaceRepository, playerRepository);

    @Test
    public void calculatePromotion() {
        PromotionData promotionData = new PromotionData();
        //given
        Mockito.when(checkinService.findByTag(any())).thenReturn(Optional.of(promotionData.getPlayersInOut().get(0)));
        Mockito.when(playerRepository.findByTag(any())).thenReturn(promotionData.getPlayers().get(0));
        Mockito.when(riverRaceRepository.getRiverRaces(any())).thenReturn(promotionData.getRiverRaces());
        //when
        List<Integer> result = promotionService.calculatePromotion("tag3");

        Assert.assertEquals(-4, (int) result.get(0));
        Assert.assertEquals(-2, (int) result.get(1));
        Assert.assertEquals(0, (int) result.get(2));
        System.out.println(result.size());


    }


    @Test
    public void calculatePromotions() {
        PromotionData promotionData = new PromotionData();
        //given
        Mockito.when(checkinService.findInClan()).thenReturn(promotionData.getPlayersInOut());
        Mockito.when(playerRepository.findInClan()).thenReturn(promotionData.getPlayers());
        Mockito.when(riverRaceRepository.getRiverRaces(any())).thenReturn(promotionData.getRiverRaces());
        //when
        List<PromotionService.PlayerPromotionDto> result = promotionService.calculatePromotions();

        //then
        PromotionService.PlayerPromotionDto tagPromotion = result.stream().filter(p -> p.getTag().equals("tag1")).findFirst().get();
        Assert.assertEquals(10, tagPromotion.getLatestActiveScore());
        Assert.assertEquals(-4, tagPromotion.getTotalPromotionPoints());
        Assert.assertEquals(-2, (int) tagPromotion.getPromotionPoints());

        PromotionService.PlayerPromotionDto tag2Promotion = result.stream().filter(p -> p.getTag().equals("tag2")).findFirst().get();
        Assert.assertEquals(10, tag2Promotion.getLatestActiveScore());
        Assert.assertEquals(-6, tag2Promotion.getTotalPromotionPoints());
        Assert.assertEquals(-4, (int) tag2Promotion.getPromotionPoints());
    }

    @Test
    public void calculatePromotions2() {
        PromotionData promotionData = new PromotionData();
        //given
        Mockito.when(checkinService.findInClan()).thenReturn(promotionData.getPlayersInOut());
        Mockito.when(playerRepository.findInClan()).thenReturn(promotionData.getPlayers());
        Mockito.when(riverRaceRepository.getRiverRaces(any())).thenReturn(promotionData.getRiverRacesAlt());
        //when
        List<PromotionService.PlayerPromotionDto> result = promotionService.calculatePromotions();

        //then
        PromotionService.PlayerPromotionDto tagPromotion = result.stream().filter(p -> p.getTag().equals("tag3")).findFirst().get();
        Assert.assertEquals(0, tagPromotion.getLatestActiveScore());
        Assert.assertEquals(608, (int) tagPromotion.getLatestScore());
        Assert.assertEquals(-1, tagPromotion.getTotalPromotionPoints());
        Assert.assertEquals(1, (int) tagPromotion.getPromotionPoints());

    }
}