package com.lightbalance.health.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lightbalance.health.dto.AppDtos;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NutritionKnowledgeService {

    private static final String SOURCE_SUMMARY = "内置营养库参考 USDA FoodData Central 公开营养标准整理，适合日常食材快速估算。";

    private final ObjectMapper objectMapper;
    private List<FoodReference> foods = List.of();

    public NutritionKnowledgeService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() throws IOException {
        this.foods = objectMapper.readValue(
            new ClassPathResource("seed/food_nutrition_reference.json").getInputStream(),
            new TypeReference<List<FoodReference>>() {
            }
        );
    }

    public AppDtos.NutritionSearchResponse search(String query, int limit) {
        String normalized = normalizeQuery(query);
        if (!StringUtils.hasText(normalized)) {
            return new AppDtos.NutritionSearchResponse(query, 0, SOURCE_SUMMARY, List.of());
        }

        List<ScoredFood> matches = foods.stream()
            .map(food -> new ScoredFood(food, score(food, normalized)))
            .filter(item -> item.score() > 0)
            .sorted(Comparator.comparingInt(ScoredFood::score).reversed().thenComparing(item -> item.food().name()))
            .limit(Math.max(1, limit))
            .toList();

        List<AppDtos.NutritionFoodOption> items = matches.stream()
            .map(item -> toOption(item.food()))
            .toList();

        return new AppDtos.NutritionSearchResponse(query, items.size(), SOURCE_SUMMARY, items);
    }

    public AppDtos.NutritionEstimateResponse estimate(String query, int grams) {
        String normalized = normalizeQuery(query);
        if (!StringUtils.hasText(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请输入食材或菜品名称");
        }

        List<ScoredFood> matches = foods.stream()
            .map(food -> new ScoredFood(food, score(food, normalized)))
            .filter(item -> item.score() > 0)
            .sorted(Comparator.comparingInt(ScoredFood::score).reversed().thenComparing(item -> item.food().name()))
            .limit(4)
            .toList();

        if (matches.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "暂未找到匹配食材，请尝试更具体的名称，例如“西兰花”“菠菜”“番茄”"
            );
        }

        int safeGrams = Math.max(50, grams <= 0 ? 150 : grams);
        List<ScoredFood> blendFoods = matches.stream()
            .filter(item -> item.score() >= 110)
            .limit(2)
            .toList();

        if (blendFoods.size() >= 2) {
            FoodReference primary = blendFoods.get(0).food();
            FoodReference secondary = blendFoods.get(1).food();
            double primaryFactor = safeGrams * 0.6 / 100.0;
            double secondaryFactor = safeGrams * 0.4 / 100.0;

            return new AppDtos.NutritionEstimateResponse(
                query,
                primary.name() + " + " + secondary.name(),
                "组合菜品估算",
                safeGrams,
                safeGrams + " g",
                roundInt(primary.caloriesPer100g() * primaryFactor + secondary.caloriesPer100g() * secondaryFactor),
                round1(primary.proteinPer100g() * primaryFactor + secondary.proteinPer100g() * secondaryFactor),
                round1(primary.carbsPer100g() * primaryFactor + secondary.carbsPer100g() * secondaryFactor),
                round1(primary.fatPer100g() * primaryFactor + secondary.fatPer100g() * secondaryFactor),
                round1(primary.fiberPer100g() * primaryFactor + secondary.fiberPer100g() * secondaryFactor),
                "中高",
                primary.source(),
                "识别到组合菜名，已按主要食材 60% 与次要食材 40% 做快速估算；如烹饪时额外用油较多，建议手动上调脂肪与热量。",
                matches.stream().map(ScoredFood::food).map(this::toOption).toList()
            );
        }

        FoodReference best = matches.get(0).food();
        double factor = safeGrams / 100.0;

        return new AppDtos.NutritionEstimateResponse(
            query,
            best.name(),
            best.category(),
            safeGrams,
            safeGrams + " g",
            roundInt(best.caloriesPer100g() * factor),
            round1(best.proteinPer100g() * factor),
            round1(best.carbsPer100g() * factor),
            round1(best.fatPer100g() * factor),
            round1(best.fiberPer100g() * factor),
            matches.get(0).score() >= 110 ? "高" : "中",
            best.source(),
            "按每 100 g 公开营养数据线性换算，适合基础食材和家常菜的快速记录；复杂烹饪可再手动微调。",
            matches.stream().map(ScoredFood::food).map(this::toOption).toList()
        );
    }

    private AppDtos.NutritionFoodOption toOption(FoodReference food) {
        return new AppDtos.NutritionFoodOption(
            food.id(),
            food.name(),
            food.category(),
            roundInt(food.caloriesPer100g()),
            round1(food.proteinPer100g()),
            round1(food.carbsPer100g()),
            round1(food.fatPer100g()),
            round1(food.fiberPer100g()),
            food.source()
        );
    }

    private int score(FoodReference food, String query) {
        int best = 0;
        for (String candidate : buildAliases(food)) {
            String normalizedCandidate = normalizeAlias(candidate);
            if (!StringUtils.hasText(normalizedCandidate)) {
                continue;
            }
            if (normalizedCandidate.equals(query)) {
                best = Math.max(best, 140);
                continue;
            }
            if (query.contains(normalizedCandidate)) {
                best = Math.max(best, 110 + Math.min(20, normalizedCandidate.length()));
                continue;
            }
            if (normalizedCandidate.contains(query)) {
                best = Math.max(best, 92 + Math.min(16, query.length()));
                continue;
            }
            if (shareToken(query, normalizedCandidate)) {
                best = Math.max(best, 65);
            }
        }
        return best;
    }

    private boolean shareToken(String left, String right) {
        if (left.length() < 2 || right.length() < 2) {
            return false;
        }
        for (int index = 0; index < left.length() - 1; index += 1) {
            String token = left.substring(index, index + 2);
            if (right.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private List<String> buildAliases(FoodReference food) {
        Set<String> aliases = new LinkedHashSet<>();
        aliases.add(food.name());
        aliases.addAll(safeAliases(food.aliases()));
        return new ArrayList<>(aliases);
    }

    private String normalizeQuery(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String normalized = value.toLowerCase(Locale.ROOT)
            .replaceAll("[()（）/、,，·.]", "")
            .replaceAll("\\s+", "")
            .replaceAll("清炒|凉拌|蒜蓉|炖|煮|烤|焯|沙拉|汤|盖饭|炒饭|米饭|套餐|便当", "");
        return normalized.trim();
    }

    private String normalizeAlias(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("\\s+", "").trim();
    }

    private int roundInt(double value) {
        return (int) Math.round(value);
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private record ScoredFood(FoodReference food, int score) {
    }

    private record FoodReference(
        String id,
        String name,
        String category,
        List<String> aliases,
        double caloriesPer100g,
        double proteinPer100g,
        double carbsPer100g,
        double fatPer100g,
        double fiberPer100g,
        String source
    ) {
    }

    private List<String> safeAliases(List<String> aliases) {
        return aliases == null ? List.of() : aliases.stream().filter(StringUtils::hasText).collect(Collectors.toList());
    }
}
