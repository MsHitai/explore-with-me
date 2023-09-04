package ru.practicum.service;

import org.springframework.stereotype.Service;
import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.mapper.HitMapper;
import ru.practicum.model.EndpointHit;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    public StatsServiceImpl(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    @Override
    public void createHit(EndpointHitDto dto) {
        EndpointHit hit = HitMapper.mapToHit(dto);
        statsRepository.save(hit);
    }

    @Override
    public List<ViewStatsDto> findStats(boolean unique, LocalDateTime start, LocalDateTime end, List<String> uris) {
        if (unique) {
            List<EndpointHit> uniqueHits = statsRepository
                    .findFirstDistinctByUriInAndTimestampBetween(uris, start, end);
            return mapStatsByHits(uniqueHits);
        }
        if (!uris.isEmpty()) {
            List<EndpointHit> hits = statsRepository.findAllByUriInAndTimestampBetween(uris, start, end);
            return mapStatsByHits(hits);
        } else {
            List<EndpointHit> hitsWithoutUris = statsRepository.findAllByTimestampBetween(start, end);
            return mapStatsByHitsWithoutUris(hitsWithoutUris);
        }
    }

    private List<ViewStatsDto> mapStatsByHitsWithoutUris(List<EndpointHit> hitsWithoutUris) {
        Map<String, Long> uriCounts = hitsWithoutUris.stream()
                .collect(Collectors.groupingBy(EndpointHit::getUri, Collectors.counting()));

        List<ViewStatsDto> stats = new ArrayList<>();
        for (EndpointHit hit : hitsWithoutUris) {
            ViewStatsDto stat = HitMapper.mapToViewDto(hit);
            long uriCount = uriCounts.get(stat.getUri());
            stat.setHits(uriCount);
            stats.add(stat);
        }

        return stats.stream()
                .sorted(Comparator.comparing(ViewStatsDto::getHits).reversed())
                .collect(Collectors.toList());
    }

    private List<ViewStatsDto> mapStatsByHits(List<EndpointHit> hits) {
        Map<String, Long> uriCounts = hits.stream()
                .collect(Collectors.groupingBy(EndpointHit::getUri, Collectors.counting()));

        Set<ViewStatsDto> stats = new TreeSet<>(Comparator.comparing(ViewStatsDto::getHits).reversed());
        for (EndpointHit hit : hits) {
            ViewStatsDto stat = HitMapper.mapToViewDto(hit);
            long uriCount = uriCounts.get(stat.getUri());
            stat.setHits(uriCount);
            stats.add(stat);
        }

        return new ArrayList<>(stats);
    }


}
