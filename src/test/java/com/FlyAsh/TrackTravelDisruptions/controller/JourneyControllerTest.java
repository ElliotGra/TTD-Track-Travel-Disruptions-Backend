package com.FlyAsh.TrackTravelDisruptions.controller;

import com.FlyAsh.TrackTravelDisruptions.config.JacksonConfig;
import com.FlyAsh.TrackTravelDisruptions.dto.JourneyDTO;
import com.FlyAsh.TrackTravelDisruptions.dto.JourneyDTOWithRailDataDTO;
import com.FlyAsh.TrackTravelDisruptions.dto.RailDataDTO;
import com.FlyAsh.TrackTravelDisruptions.models.Journey;
import com.FlyAsh.TrackTravelDisruptions.models.JourneyLeg;
import com.FlyAsh.TrackTravelDisruptions.models.TransportProvider;
import com.FlyAsh.TrackTravelDisruptions.service.JourneyServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@ContextConfiguration(classes = {JacksonConfig.class})
class JourneyControllerTest {

    @Mock
    private JourneyServiceImpl mockJourneyServiceImpl;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvcController;

    @InjectMocks
    private JourneyController journeyController;





    private Journey journey1;
    private JourneyDTO journey1Dto;
    private JourneyDTOWithRailDataDTO journeyDTOWithRailDataDTO;
    private Journey journey2;
    Set<DayOfWeek> days;
    JourneyLeg journeyLeg1;
    JourneyLeg journeyLeg2;
    Set<JourneyLeg> journeyLegs;
    TransportProvider nationalRail;
    TransportProvider transportForLondon;


    @BeforeEach
    public void setup() {
        mockMvcController = MockMvcBuilders.standaloneSetup(journeyController).build();

        journey1Dto = new JourneyDTO(1L,"OXF", "PAD", LocalTime.parse("08:00"), days, true);

        journey1 = new Journey(1L, true, "Origin 1", "Destination 1", 1L, days, LocalTime.parse("08:00"), journeyLegs);
        journey2 = new Journey(2L, false, "Origin 2", "Destination 2", 2L, days, LocalTime.parse("09:30:10"), journeyLegs);
        days = new HashSet<>(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));
        journeyLeg1 = new JourneyLeg(1L, "Oxford", "OXF", "Reading", "RDG", 1, nationalRail, null);
        journeyLeg2 = new JourneyLeg(2L, "Reading", "RDG", "Paddington", "PAD", 2, nationalRail, null);
        journeyLegs = new HashSet<>(Arrays.asList(journeyLeg1, journeyLeg2));
        nationalRail = new TransportProvider(1L, "National Rail", "https://www.nationalrail.co.uk/", null);
        transportForLondon = new TransportProvider(2L, "Transport for London", "https://tfl.gov.uk/", null);
        journeyDTOWithRailDataDTO = new JourneyDTOWithRailDataDTO(journey1Dto, null);
    }

    @Test
    @DisplayName("GetAllJourneyTest")
    void getAllJourney() throws Exception {
        //Arrange
        List<Journey> journeys = List.of(journey1, journey2);
        // Act
        when(mockJourneyServiceImpl.getAllJourneys()).thenReturn(journeys);

        // Assert

        this.mockMvcController.perform(
                        MockMvcRequestBuilders.get("/api/v1/journey"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].originCRS").value("Origin 1"))
                .andExpect(jsonPath("$[1].destinationCRS").value("Destination 2"));

        verify(mockJourneyServiceImpl, times(1)).getAllJourneys();
    }

    @Test
    public void testSerializationOfLocalTime() throws Exception {
        LocalTime time = LocalTime.parse("09:30:10");
        String expected = "\"09:30:10\"";
        String serializedTime = mapper.writeValueAsString(time);
        System.out.println(serializedTime);
        assertEquals(expected, serializedTime, "LocalTime should be serialized to the expected format");
    }

    @Test
    @DisplayName("GetJourneyByIdTest")
    void getJourneysWithRailDataByUserId() throws Exception {
        when(mockJourneyServiceImpl.getJourneysWithRailDataByUserId(1L)).thenReturn(Collections.singletonList(journeyDTOWithRailDataDTO));

        mockMvcController.perform(MockMvcRequestBuilders.get("/api/v1/journey/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].journeyDTO.userId").value(1L))
                .andExpect(jsonPath("$[0].journeyDTO.originCRS").value("OXF"))
                .andExpect(jsonPath("$[0].journeyDTO.destinationCRS").value("PAD"));

    }

    @Test
    @DisplayName("AddNewJourneyTest")
    public void addNewJourneyTest() throws Exception {
        Journey journey3 = new Journey(3L, true, "Origin 3", "Destination 3", 3L, days, LocalTime.parse("11:00"), journeyLegs);

        when(mockJourneyServiceImpl.addNewJourney(journey3)).thenReturn(journey3);

        mockMvcController.perform(MockMvcRequestBuilders.post("/api/v1/journey")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(journey3)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("UpdateJourneyById")
    public void updateJourneyByIdTest() throws Exception {
      Journey updatedJourney = new Journey(1L, false, "Origin 4", "Destination 4", 4L, days, LocalTime.parse("09:00"), journeyLegs);

        when(mockJourneyServiceImpl.updateJourneyById(any(Long.class), any(Journey.class))).thenReturn(updatedJourney);

        mockMvcController.perform(MockMvcRequestBuilders.put("/api/v1/journey/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(journey1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updatedJourney.getId()))
                .andExpect(jsonPath("$.originCRS").value(updatedJourney.getOriginCRS()))
                .andExpect(jsonPath("$.destinationCRS").value(updatedJourney.getDestinationCRS()));
    }

    @Test
    @DisplayName("UpdateJourneyById_Fail")
    public void updateJourneyByIdTest_wrongId() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid ID_UPDATE"))
                .when(mockJourneyServiceImpl).updateJourneyById(eq(99L), any(Journey.class));

        mockMvcController.perform(MockMvcRequestBuilders.put("/api/v1/journey/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(journey1)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(ResponseStatusException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals("404 NOT_FOUND \"Invalid ID_UPDATE\"", result.getResolvedException().getMessage()));
    }

    @Test
    @DisplayName("DeleteJourneyById")
    public void deleteJourneyByIdTest() throws Exception {
        doNothing().when(mockJourneyServiceImpl).deleteJourneyById(1L);

        mockMvcController.perform(MockMvcRequestBuilders.delete("/api/v1/journey/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());
    }
}