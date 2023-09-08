package ru.practicum.model;

import lombok.*;
import org.hibernate.annotations.Formula;
import org.hibernate.proxy.HibernateProxy;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String annotation;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    @ToString.Exclude
    private User initiator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @ToString.Exclude
    private Category category;

    @Formula(value = "(select count(r.id) from requests as r where r.event_id = id and r.status like 'CONFIRMED')")
    private int confirmedRequests;

    private String description;

    private LocalDateTime eventDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    @ToString.Exclude
    private Location location;

    private Boolean paid;

    private int participantLimit;

    private Boolean requestModeration;

    private String title;

    @Enumerated(EnumType.STRING)
    private EventState state;

    private LocalDateTime createdOn;

    private LocalDateTime publishedOn;
    @Transient
    private long views;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "compilations_events",
            joinColumns = {@JoinColumn(name = "event_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "compilation_id", referencedColumnName = "id")}
    )
    @ToString.Exclude
    private List<Compilation> compilations;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ?
                ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ?
                ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Event event = (Event) o;
        return getId() != null && Objects.equals(getId(), event.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ?
                ((HibernateProxy) this).getHibernateLazyInitializer()
                        .getPersistentClass().hashCode() : getClass().hashCode();
    }
}
