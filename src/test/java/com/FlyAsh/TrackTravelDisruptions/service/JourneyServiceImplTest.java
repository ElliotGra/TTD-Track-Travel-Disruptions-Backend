package com.FlyAsh.TrackTravelDisruptions.service;

import com.FlyAsh.TrackTravelDisruptions.models.Journey;
import com.FlyAsh.TrackTravelDisruptions.models.JourneyLeg;
import com.FlyAsh.TrackTravelDisruptions.models.TransportProvider;
import com.FlyAsh.TrackTravelDisruptions.repositories.JourneyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class JourneyServiceImplTest {

    private Journey journey1;
    private Journey journey2;
    Set<DayOfWeek> days;
    JourneyLeg journeyLeg1;
    JourneyLeg journeyLeg2;
    Set<JourneyLeg> journeyLegs;
    TransportProvider nationalRail;
    TransportProvider transportForLondon;

    @Mock
    private JourneyRepository mockJourneyRepository;

    @InjectMocks
    private JourneyServiceImpl journeyService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        journey1 = new Journey(1L, true, "Origin 1", "Destination 1", 1L, days, LocalTime.parse("08:00"), journeyLegs);
        journey2 = new Journey(2L, false, "Origin 2", "Destination 2", 2L, days, LocalTime.parse("09:00"), journeyLegs);
        days = new HashSet<>(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));
        journeyLeg1 = new JourneyLeg(1L, "Oxford", "OXF",  "Reading", "RDG", 1, nationalRail, null);
        journeyLeg2 = new JourneyLeg(2L, "Reading", "RDG", "Paddington", "PAD", 2, nationalRail, null);
        journeyLegs = new HashSet<>(Arrays.asList(journeyLeg1, journeyLeg2));
        nationalRail = new TransportProvider(1L, "National Rail", "https://www.nationalrail.co.uk/", null);
        transportForLondon = new TransportProvider(2L, "Transport for London", "https://tfl.gov.uk/", null);
    }

    @Test
    void getAllJourney() {
        List<Journey> allJourney = List.of(journey1,journey2);
        when(mockJourneyRepository.findAll()).thenReturn(allJourney);

        List<Journey> mockResult = journeyService.getAllJourneys();

        assertEquals(2, mockResult.size());
        verify(mockJourneyRepository, times(1)).findAll();
    }

    @Test
    void getJourneyById_Success() {
        // Arrange
        when(mockJourneyRepository.findById(1L)).thenReturn(Optional.of(journey1));

        // Act
        Journey result = journeyService.getJourneyById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(journey1.getId(), result.getId());
        verify(mockJourneyRepository, times(1)).findById(1L);
    }

    @Test
    void getJouneryById_NotFound() {
        // Arrange
        when(mockJourneyRepository.findById(9L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            journeyService.getJourneyById(9L);
        });
        assertEquals("Journey not found with ID: 9", exception.getMessage());
        verify(mockJourneyRepository, times(1)).findById(9L);
    }




    @Test
    void addNewJounery() {

        Journey journey3 = new Journey(3L, true, "Origin 3", "Destination 3", 3L, days, LocalTime.parse("21:00"), journeyLegs);
        when(mockJourneyRepository.save(journey3)).thenReturn(journey3);
        // Act

        Journey result = journeyService.addNewJourney(journey3);

        // Assert
        assertNotNull(result);
        assertEquals(result.getId(), journey3.getId());
        verify(mockJourneyRepository, times(1)).save(journey3);

    }

    @Test
    void testUpdateJourneyById_Success() {
        // Arrange
        Long journeyId = 1L;
        Journey updateJourney = new Journey(1L, true, "NewOrigin 1", "NewDestination 1", 1L, days, LocalTime.parse("21:00"), journeyLegs);
        when(mockJourneyRepository.findById(journeyId)).thenReturn(Optional.of(journey1));
        when(mockJourneyRepository.save(any(Journey.class))).thenReturn(updateJourney);

        // Act
        Journey updatedJourney = journeyService.updateJourneyById(journeyId, updateJourney);

        // Assert
        assertEquals("NewOrigin 1", updatedJourney.getOriginCRS());
        assertEquals("NewDestination 1", updatedJourney.getDestinationCRS());
        assertEquals("21:00", updatedJourney.getDepartureTime().toString());
        assertTrue(updatedJourney.getNotificationsEnabled());
        verify(mockJourneyRepository, times(1)).findById(journeyId);

        //Use argumentCaptor to capture what send to the repository
        ArgumentCaptor<Journey> journeyArgumentCaptor = ArgumentCaptor.forClass(Journey.class);
        verify(mockJourneyRepository, times(1)).save(journeyArgumentCaptor.capture());
        Journey capturedJourney = journeyArgumentCaptor.getValue();
        //to verify what send to the repository
        assertEquals("NewOrigin 1", capturedJourney.getOriginCRS());
        assertEquals("NewDestination 1", capturedJourney.getDestinationCRS());
        assertEquals("21:00", capturedJourney.getDepartureTime().toString());
        assertTrue(capturedJourney.getNotificationsEnabled());
    }

    @Test
    void testUpdateJourneyById_NotFound() {
        // Arrange
        Long journeyId = 9L;
        when(mockJourneyRepository.findById(journeyId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            journeyService.updateJourneyById(journeyId, any(Journey.class));
        });
        assertEquals("Journey not found with ID: 9", exception.getMessage());
        verify(mockJourneyRepository, times(1)).findById(9L);
    }

    @Test
    void deleteJourneyById_Success() {
        // Arrange
        Long journeyId = 1L;
        Journey journey = new Journey();
        journey.setId(journeyId);
        when(mockJourneyRepository.findById(journeyId)).thenReturn(Optional.of(journey));

        // Act
        journeyService.deleteJourneyById(journeyId);

        // Assert
        verify(mockJourneyRepository, times(1)).findById(journeyId);
        ArgumentCaptor<Journey> journeyArgumentCaptor = ArgumentCaptor.forClass(Journey.class);
        verify(mockJourneyRepository, times(1)).delete(journeyArgumentCaptor.capture());
        Journey capturedJourney = journeyArgumentCaptor.getValue();
        assertEquals(journeyId, capturedJourney.getId());
    }

    @Test
    void deleteJourneyById_NotFound() {
        // Arrange
        Long journeyId = 1L;
        when(mockJourneyRepository.findById(journeyId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> journeyService.deleteJourneyById(journeyId));
        verify(mockJourneyRepository, times(1)).findById(journeyId);
        verify(mockJourneyRepository, never()).delete(any(Journey.class));
    }
}