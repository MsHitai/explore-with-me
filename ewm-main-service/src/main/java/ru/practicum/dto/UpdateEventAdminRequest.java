package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.model.StateAction;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateEventAdminRequest {

    private String annotation;
    private Integer category;
    private String description;
    private String eventDate;
    private LocationDto location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private StateAction stateAction;
    private String title;
}
