package com.courtvision.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for SportsBlaze NBA API response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NBAPlayersResponseDTO {

    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private List<NBAPlayerDTO> players;
}
