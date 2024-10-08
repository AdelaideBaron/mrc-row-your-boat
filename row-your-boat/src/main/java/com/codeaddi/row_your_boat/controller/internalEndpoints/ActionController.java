package com.codeaddi.row_your_boat.controller.internalEndpoints;

import com.codeaddi.row_your_boat.controller.http.AvailabilityClient;
import com.codeaddi.row_your_boat.controller.http.SchedulerClient;
import com.codeaddi.row_your_boat.model.enums.RowerLevel;
import com.codeaddi.row_your_boat.model.enums.SessionType;
import com.codeaddi.row_your_boat.model.enums.Squad;
import com.codeaddi.row_your_boat.model.http.AvailabilityDTO;
import com.codeaddi.row_your_boat.model.http.StandardResponse;
import com.codeaddi.row_your_boat.model.http.enums.Status;
import com.codeaddi.row_your_boat.model.http.inbound.RowingSession;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/internal")
@Slf4j
public class ActionController {

  @Autowired SchedulerClient schedulerClient;
  @Autowired AvailabilityClient availabilityClient;

  @PostMapping("/add-rowing-session")
  public String addRowingSession(
      @RequestParam String day,
      @RequestParam String startTime,
      @RequestParam String endTime,
      @RequestParam String squad,
      @RequestParam String level,
      @RequestParam String sessionType,
      @RequestParam Long maxId,
      RedirectAttributes redirectAttributes) {
    log.info("Request to add new session received");

    RowingSession newSession =
        RowingSession.builder()
            .id(maxId + 1)
            .day(day)
            .startTime(startTime)
            .endTime(endTime)
            .squad(Squad.valueOf(squad))
            .level(RowerLevel.valueOf(level))
            .sessionType(SessionType.valueOf(sessionType))
            .build();
    StandardResponse response = schedulerClient.updateSession(newSession);

    if (response.getStatus().toString().contains("SUCCESS")) {
      redirectAttributes.addFlashAttribute("successMessage", response.getMessage());

    } else {
      redirectAttributes.addFlashAttribute("errorMessage", response.getMessage());
    }

    return "redirect:/standard-sessions";
  }

  @PostMapping("/update-rowing-session")
  public ResponseEntity<String> updateRowingSession(
      @RequestParam String id,
      @RequestParam String day,
      @RequestParam String startTime,
      @RequestParam String endTime,
      @RequestParam String squad,
      @RequestParam String level,
      @RequestParam String sessionType) {
    log.info("Request to update session with id {} received", id);

    RowingSession updatedSession =
        RowingSession.builder()
            .id(Long.valueOf(id))
            .day(day)
            .startTime(startTime)
            .endTime(endTime)
            .squad(Squad.valueOf(squad))
            .level(RowerLevel.valueOf(level))
            .sessionType(SessionType.valueOf(sessionType))
            .build();

    StandardResponse response = schedulerClient.updateSession(updatedSession);

    if (response.getStatus().toString().contains("SUCCESS")) {
      return ResponseEntity.ok(response.getMessage());
    } else {
      return ResponseEntity.badRequest().body(response.getMessage());
    }
  }

  @PostMapping("/delete-session")
  public ResponseEntity<String> updateRowingSession(
      @RequestParam String id, RedirectAttributes redirectAttributes) {
    log.info("Request to delete session with id {} received", id);

    StandardResponse response = schedulerClient.deleteSession(Long.valueOf(id));

    if (response.getStatus().toString().contains("SUCCESS")) {
      return ResponseEntity.ok(response.getMessage());
    } else {
      return ResponseEntity.badRequest().body(response.getMessage());
    }
  }

  @PostMapping("/save-availability")
  public ResponseEntity<String> saveAvailability(
      @RequestBody List<AvailabilityDTO> availabilityData) {
    log.info("Availability save request received");

    StandardResponse response =
        availabilityClient.saveAvailability(addDummyRowerId(availabilityData));

    if (response.getStatus().equals(Status.SUCCESS)) {
      return ResponseEntity.ok(response.getMessage());
    } else {
      return ResponseEntity.badRequest().body(response.getMessage());
    }
  }

  private List<AvailabilityDTO> addDummyRowerId(List<AvailabilityDTO> availabilityData) {
    List<AvailabilityDTO> availabilityDTOSWithDummyId = new ArrayList<>();

    for (AvailabilityDTO availabilityDTO : availabilityData) {
      AvailabilityDTO updated =
          AvailabilityDTO.builder()
              .rowerId(1L)
              .availability(availabilityDTO.isAvailability())
              .sessionId(availabilityDTO.getSessionId())
              .build();
      availabilityDTOSWithDummyId.add(updated);
    }

    return availabilityDTOSWithDummyId;
  }
}
