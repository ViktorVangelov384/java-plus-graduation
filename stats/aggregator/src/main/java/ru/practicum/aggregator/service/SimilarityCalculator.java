package ru.practicum.aggregator.service;

import org.springframework.stereotype.Component;
import ru.practicum.stats.avro.ActionTypeAvro;
import ru.practicum.stats.avro.EventSimilarityAvro;
import ru.practicum.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SimilarityCalculator {

    // Веса действий (можно вынести в конфиг)
    private static final double VIEW_WEIGHT = 0.4;
    private static final double REGISTER_WEIGHT = 0.8;
    private static final double LIKE_WEIGHT = 1.0;

    private static final Map<ActionTypeAvro, Double> WEIGHTS = Map.of(
            ActionTypeAvro.ACTION_VIEW, VIEW_WEIGHT,
            ActionTypeAvro.ACTION_REGISTER, REGISTER_WEIGHT,
            ActionTypeAvro.ACTION_LIKE, LIKE_WEIGHT
    );

    private final Map<Long, Map<Long, Double>> weightMatrix = new ConcurrentHashMap<>();

    private final Map<Long, Double> weightSumByEvent = new HashMap<>();

    private final Map<Long, Map<Long, Double>> minWeightsSums = new HashMap<>();

    private final Map<Long, Set<Long>> eventsByUser = new HashMap<>();

    public List<EventSimilarityAvro> calculateSimilarity(UserActionAvro action) {
        long eventId = action.getEventId();
        long userId = action.getUserId();
        double newWeight = WEIGHTS.getOrDefault(action.getActionType(), 0.0);

        Map<Long, Double> userWeights = weightMatrix.computeIfAbsent(eventId, k -> new HashMap<>());
        double oldWeight = userWeights.getOrDefault(userId, 0.0);

        if (newWeight > oldWeight) {
            userWeights.put(userId, newWeight);

            double currentSum = weightSumByEvent.getOrDefault(eventId, 0.0);
            weightSumByEvent.put(eventId, currentSum - oldWeight + newWeight);

            Set<Long> userEvents = eventsByUser.computeIfAbsent(userId, k -> new HashSet<>());
            List<EventSimilarityAvro> result = new ArrayList<>();

            for (Long otherEventId : userEvents) {
                if (otherEventId.equals(eventId)) continue;

                double similarity = recalculatePair(eventId, otherEventId, userId, oldWeight, newWeight);
                result.add(mapToEventSimilarityAvro(eventId, otherEventId, similarity));
            }

            userEvents.add(eventId);
            return result;
        }
        return Collections.emptyList();
    }

    private double recalculatePair(long eventA, long eventB, long userId, double oldWeight, double newWeight) {
        double otherEventWeight = weightMatrix.getOrDefault(eventB, Map.of()).getOrDefault(userId, 0.0);

        double diff = Math.min(newWeight, otherEventWeight) - Math.min(oldWeight, otherEventWeight);

        double currentMinSum = getMinSum(eventA, eventB) + diff;
        putMinSum(eventA, eventB, currentMinSum);

        double sumA = weightSumByEvent.getOrDefault(eventA, 0.0);
        double sumB = weightSumByEvent.getOrDefault(eventB, 0.0);

        if (sumA == 0 || sumB == 0) return 0.0;
        return currentMinSum / (Math.sqrt(sumA) * Math.sqrt(sumB));
    }

    private void putMinSum(long a, long b, double sum) {
        long first = Math.min(a, b);
        long second = Math.max(a, b);
        minWeightsSums.computeIfAbsent(first, k -> new HashMap<>()).put(second, sum);
    }

    private double getMinSum(long a, long b) {
        long first = Math.min(a, b);
        long second = Math.max(a, b);
        return minWeightsSums.getOrDefault(first, Map.of()).getOrDefault(second, 0.0);
    }

    private EventSimilarityAvro mapToEventSimilarityAvro(long a, long b, double score) {
        return EventSimilarityAvro.newBuilder()
                .setEventA(Math.min(a, b))
                .setEventB(Math.max(a, b))
                .setScore(score)
                .setTimestamp(Instant.now())
                .build();
    }
}
