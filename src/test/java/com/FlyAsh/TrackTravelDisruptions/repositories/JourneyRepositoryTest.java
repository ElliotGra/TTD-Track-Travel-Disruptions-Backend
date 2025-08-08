package com.FlyAsh.TrackTravelDisruptions.repositories;


import com.FlyAsh.TrackTravelDisruptions.models.Journey;
import com.FlyAsh.TrackTravelDisruptions.models.JourneyLeg;
import com.FlyAsh.TrackTravelDisruptions.models.TransportProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


@Transactional
@SpringBootTest
class JourneyRepositoryTest {

    @Autowired
    JourneyRepository journeyRepository;

    @Test
    void testJourneyRepository() {
        TransportProvider nationalRail = new TransportProvider(1L, "National Rail", "https://www.nationalrail.co.uk/", null);
        TransportProvider transportForLondon = new TransportProvider(2L, "Transport for London", "https://tfl.gov.uk/", null);

        Journey journey = new Journey();
        journey.setId(1L);
        journey.setOriginCRS("Oxford");
        journey.setDestinationCRS("Piccadilly Circus");
        journey.setDepartureTime(LocalTime.parse("08:00"));
        JourneyLeg leg1 = new JourneyLeg(1L, "Oxford", "OXF", "Reading", "RDG", 1, nationalRail, null);
        JourneyLeg leg2 = new JourneyLeg(2L, "Reading", "RDG", "Paddington", "PAD", 2, nationalRail, null);
        nationalRail.setJourneyLegs(Set.of(leg1, leg2));
        JourneyLeg leg3 = new JourneyLeg(3L, "Paddington", "PAD", "Piccadilly Circus", "TES", 3, transportForLondon, null);
        transportForLondon.setJourneyLegs(Set.of(leg3));
        journey.setJourneyLegs(Set.of(leg1, leg2, leg3));
        journey.setNotificationsEnabled(true);
        journey.setDays(Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY));

        journey.getJourneyLegs().forEach(journeyLeg -> journeyLeg.setJourney(journey));

        journeyRepository.save(journey);


        Journey result = journeyRepository.findById(1L).get();

        assertThat(result.getDestinationCRS()).isEqualTo(journey.getDestinationCRS());
        assertThat(result.getJourneyLegs().size()).isEqualTo(3);
        assertThat(result.getJourneyLegs().iterator().next().getTransportProvider().getName()).isEqualTo(nationalRail.getName());
    }

}