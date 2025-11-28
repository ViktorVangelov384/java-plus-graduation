package teamfive.service;

import dto.InputHitDto;
import dto.OutHitDto;
import dto.StatDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teamfive.mapper.SimpleHitMapper;
import teamfive.model.Hit;
import teamfive.model.StatHit;
import teamfive.storage.StatRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatServiceImpl implements StatService {

    private final StatRepository statRepository;
    private final SimpleHitMapper mapper;

    @Override
    public List<StatDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        log.info("Запрос статистики: start={}, end={}, uris={}, unique={}",
                start, end, uris, unique);

        List<StatHit> statHitList;
        if (uris == null || uris.isEmpty()) {
            if (unique) {
                statHitList = statRepository.getUniqueStat(start, end, null);
            } else {
                statHitList = statRepository.getNonUniqueStat(start, end, null);
            }
        } else {
            if (unique) {
                statHitList = statRepository.getUniqueStat(start, end, uris);
            } else {
                statHitList = statRepository.getNonUniqueStat(start, end, uris);
            }
        }
        return statHitList.stream().map(mapper::statHitToStatDto)
                .collect(Collectors.toList());

    }

    @Transactional
    @Override
    public OutHitDto createHit(InputHitDto inputHitDto) {
        Hit hit = mapper.dtoToHit(inputHitDto);
        log.info("Запись статистики: app = {}, uri = {}, ip = {}, timestamp = {}",
                inputHitDto.getApp(), inputHitDto.getUri(), inputHitDto.getIp(), inputHitDto.getTimestamp());
        return mapper.hitToDto(statRepository.save(hit));
    }
}
