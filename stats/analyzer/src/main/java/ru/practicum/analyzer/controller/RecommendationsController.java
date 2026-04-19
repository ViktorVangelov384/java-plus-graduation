package ru.practicum.analyzer.controller;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.data.domain.PageRequest;
import ru.practicum.analyzer.dao.model.EventSimilarity;
import ru.practicum.analyzer.dao.model.UserAction;
import ru.practicum.analyzer.dao.repository.EventSimilarityRepository;
import ru.practicum.analyzer.dao.repository.UserActionRepository;
import ru.practicum.ewm.stats.proto.dashboard.RecommendationsControllerGrpc;
import ru.practicum.ewm.stats.proto.event.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.event.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.event.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.event.UserPredictionsRequestProto;

import java.util.*;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class RecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final UserActionRepository userActionRepository;
    private final EventSimilarityRepository similarityRepository;

    private static final int DEFAULT_USER_EVENTS_LIMIT = 50;
    private static final int SIMILARITY_MULTIPLIER = 5;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        long userId = request.getUserId();
        int maxResults = request.getMaxResults();

        log.debug("Getting recommendations for user: {}, maxResults: {}", userId, maxResults);

        List<Long> userEventIds = userActionRepository.findEventIdsByUserId(userId, PageRequest.of(0, DEFAULT_USER_EVENTS_LIMIT));

        if (userEventIds.isEmpty()) {
            log.debug("No events found for user: {}", userId);
            responseObserver.onCompleted();
            return;
        }

        Set<Long> userEventIdSet = new HashSet<>(userEventIds);

        List<EventSimilarity> neighbours = similarityRepository.findByEventIdIn(
                userEventIdSet,
                PageRequest.of(0, maxResults * SIMILARITY_MULTIPLIER)
        );

        Set<Long> candidates = collectCandidates(neighbours, userEventIdSet);

        if (candidates.isEmpty()) {
            log.debug("No candidates found for user: {}", userId);
            responseObserver.onCompleted();
            return;
        }

        List<RecommendedEventProto> results = predictScores(userId, new ArrayList<>(candidates), userEventIds)
                .entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(maxResults)
                .map(e -> RecommendedEventProto.newBuilder()
                        .setEventId(e.getKey())
                        .setScore(e.getValue())
                        .build())
                .toList();

        results.forEach(responseObserver::onNext);
        responseObserver.onCompleted();

        log.debug("Returned {} recommendations for user: {}", results.size(), userId);
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        long eventId = request.getEventId();
        long userId = request.getUserId();
        int maxResults = request.getMaxResults();

        log.debug("Getting similar events for eventId: {}, userId: {}, maxResults: {}", eventId, userId, maxResults);

        List<EventSimilarity> allSimilar = similarityRepository.findAllByEventId(eventId);

        if (allSimilar.isEmpty()) {
            log.debug("No similar events found for eventId: {}", eventId);
            responseObserver.onCompleted();
            return;
        }

        Set<Long> userEvents = userActionRepository.findEventIdsByUserIdExcludeEventId(userId, eventId);

        List<RecommendedEventProto> results = allSimilar.stream()
                .filter(similarity -> {
                    long otherEventId = getOtherEventId(similarity, eventId);
                    return !userEvents.contains(otherEventId);
                })
                .sorted(Comparator.comparingDouble(EventSimilarity::getScore).reversed())
                .limit(maxResults)
                .map(similarity -> {
                    long otherEventId = getOtherEventId(similarity, eventId);
                    return RecommendedEventProto.newBuilder()
                            .setEventId(otherEventId)
                            .setScore(similarity.getScore())
                            .build();
                })
                .toList();

        results.forEach(responseObserver::onNext);
        responseObserver.onCompleted();

        log.debug("Returned {} similar events for eventId: {}", results.size(), eventId);
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        Set<Long> eventIds = new HashSet<>(request.getEventIdList());
        Map<Long, Double> countByEvent = getInteractionCounts(eventIds);

        for (Long eventId : eventIds) {
            double score = countByEvent.getOrDefault(eventId, 0.0);
            responseObserver.onNext(RecommendedEventProto.newBuilder()
                    .setEventId(eventId)
                    .setScore(score)
                    .build());
        }
        responseObserver.onCompleted();
    }

    private Set<Long> collectCandidates(List<EventSimilarity> neighbours, Set<Long> userEventIdSet) {
        Set<Long> candidates = new HashSet<>();

        for (EventSimilarity similarity : neighbours) {
            long eventA = similarity.getEventA();
            long eventB = similarity.getEventB();

            if (!userEventIdSet.contains(eventA)) {
                candidates.add(eventA);
            }
            if (!userEventIdSet.contains(eventB)) {
                candidates.add(eventB);
            }
        }

        candidates.removeAll(userEventIdSet);
        return candidates;
    }

    private Map<Long, Double> predictScores(long userId, List<Long> candidates, List<Long> userEventIds) {
        Map<Long, Double> scores = new HashMap<>();

        Map<Long, Double> userActionWeights = getUserActionWeights(userId);

        if (userActionWeights.isEmpty()) {
            return scores;
        }

        for (Long candidate : candidates) {
            List<EventSimilarity> neighbours = similarityRepository.findAllByEventId(candidate);

            if (neighbours.isEmpty()) {
                continue;
            }

            double weightedSum = 0.0;
            double similaritySum = 0.0;

            for (EventSimilarity similarity : neighbours) {
                long neighbourEventId = getOtherEventId(similarity, candidate);
                Double weight = userActionWeights.get(neighbourEventId);

                if (weight != null) {
                    double simScore = similarity.getScore();
                    weightedSum += weight * simScore;
                    similaritySum += simScore;
                }
            }

            if (similaritySum > 0) {
                scores.put(candidate, weightedSum / similaritySum);
            }
        }
        return scores;
    }

    private Map<Long, Double> getUserActionWeights(long userId) {
        List<UserAction> userActions = userActionRepository.findAllByUserId(userId);

        return userActions.stream()
                .collect(Collectors.toMap(
                        UserAction::getEventId,
                        UserAction::getWeight,
                        (existing, replacement) -> existing
                ));
    }

    private Map<Long, Double> getInteractionCounts(Set<Long> eventIds) {
        List<Object[]> sums = userActionRepository.sumWeightsByEventId(eventIds);

        return sums.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Double) row[1]
                ));
    }

    private long getOtherEventId(EventSimilarity similarity, long eventId) {
        return similarity.getEventA() == eventId ? similarity.getEventB() : similarity.getEventA();
    }
}