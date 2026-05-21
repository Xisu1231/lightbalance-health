package com.lightbalance.health.service;

import com.lightbalance.health.config.DeepSeekProperties;
import com.lightbalance.health.domain.UserProfile;
import com.lightbalance.health.dto.AppDtos;
import com.lightbalance.health.dto.SeedData;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;

@Service
public class AssistantService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Shanghai");

    private final DashboardService dashboardService;
    private final AnalyticsService analyticsService;
    private final DeepSeekClient deepSeekClient;
    private final DeepSeekProperties deepSeekProperties;
    private final SeedData.AppSeed appSeed;
    private final List<String> quickPrompts;
    private final Map<Long, CopyOnWriteArrayList<AppDtos.AssistantMessage>> conversations = new ConcurrentHashMap<>();

    public AssistantService(
        DashboardService dashboardService,
        AnalyticsService analyticsService,
        DeepSeekClient deepSeekClient,
        DeepSeekProperties deepSeekProperties,
        ResourceDataLoader resourceDataLoader
    ) {
        this.dashboardService = dashboardService;
        this.analyticsService = analyticsService;
        this.deepSeekClient = deepSeekClient;
        this.deepSeekProperties = deepSeekProperties;
        this.appSeed = resourceDataLoader.getAppSeed();
        this.quickPrompts = appSeed.coachPrompts();
    }

    public AppDtos.AssistantResponse getConversation(UserProfile user) {
        return new AppDtos.AssistantResponse(List.copyOf(conversationFor(user)), quickPrompts, runtime());
    }

    public AppDtos.AssistantResponse reply(UserProfile user, String userMessage) {
        CopyOnWriteArrayList<AppDtos.AssistantMessage> conversation = conversationFor(user);
        conversation.add(new AppDtos.AssistantMessage("user", user.getName(), "Today", userMessage, now()));

        DeepSeekClient.DeepSeekResult result = deepSeekClient.createChatCompletion(buildRequestMessages(user, conversation, userMessage));
        if (result.success()) {
            conversation.add(new AppDtos.AssistantMessage("assistant", "DeepSeek", "Advice", result.content(), now()));
        } else if (!result.configured()) {
            conversation.add(new AppDtos.AssistantMessage(
                "assistant",
                "DeepSeek",
                "Setup",
                "DeepSeek is wired into the app, but no API key is configured yet. Please set DEEPSEEK_API_KEY and restart the backend.",
                now()
            ));
        } else {
            conversation.add(new AppDtos.AssistantMessage(
                "assistant",
                "DeepSeek",
                "Error",
                "DeepSeek did not return a valid response this time. Please check the API key, network, or model settings. Error: " + result.errorMessage(),
                now()
            ));
        }

        return getConversation(user);
    }

    public AppDtos.TrendAdviceResponse trendAdvice(UserProfile user) {
        List<DeepSeekClient.DeepSeekMessage> messages = List.of(
            new DeepSeekClient.DeepSeekMessage("system", buildSystemPrompt(user)),
            new DeepSeekClient.DeepSeekMessage("user", buildTrendAdvicePrompt(user))
        );
        DeepSeekClient.DeepSeekResult result = deepSeekClient.createChatCompletion(messages);
        String advice;
        if (result.success()) {
            advice = result.content();
        } else if (!result.configured()) {
            advice = "DeepSeek API key is not configured. For now, focus on consistent sleep, daytime hydration, and lighter training on high-stress days.";
        } else {
            advice = "DeepSeek could not return a result. For now, keep sleep above 7 hours, reduce high-intensity work on stressful days, and spread hydration across the day. Error: " + result.errorMessage();
        }
        return new AppDtos.TrendAdviceResponse(advice, runtime(), now());
    }

    private CopyOnWriteArrayList<AppDtos.AssistantMessage> conversationFor(UserProfile user) {
        return conversations.computeIfAbsent(user.getId(), ignored -> seedConversation(user));
    }

    private CopyOnWriteArrayList<AppDtos.AssistantMessage> seedConversation(UserProfile user) {
        CopyOnWriteArrayList<AppDtos.AssistantMessage> seeded = new CopyOnWriteArrayList<>();
        for (int index = 0; index < appSeed.assistantConversation().size(); index++) {
            SeedData.ConversationSeed item = appSeed.assistantConversation().get(index);
            String content = item.content()
                .replace("\u5C0F\u660E\u8001\u5E08", user.getName())
                .replace("\u601D\u660E\u8001\u5E08", user.getName());
            seeded.add(new AppDtos.AssistantMessage(
                item.speaker(),
                item.tag(),
                item.title(),
                content,
                timeMinutesAgo(18 - (index * 3))
            ));
        }
        return seeded;
    }

    private AppDtos.AssistantRuntime runtime() {
        DeepSeekClient.DeepSeekRuntime runtime = deepSeekClient.runtime();
        return new AppDtos.AssistantRuntime(
            runtime.provider(),
            runtime.model(),
            runtime.liveMode(),
            runtime.status()
        );
    }

    private List<DeepSeekClient.DeepSeekMessage> buildRequestMessages(
        UserProfile user,
        List<AppDtos.AssistantMessage> conversation,
        String userMessage
    ) {
        List<DeepSeekClient.DeepSeekMessage> messages = new ArrayList<>();
        messages.add(new DeepSeekClient.DeepSeekMessage("system", buildSystemPrompt(user)));

        int historyLimit = Math.min(conversation.size(), deepSeekProperties.getMaxHistoryMessages());
        List<AppDtos.AssistantMessage> recentMessages = conversation.subList(conversation.size() - historyLimit, conversation.size());
        for (AppDtos.AssistantMessage item : recentMessages) {
            String role = "assistant";
            if ("user".equals(item.speaker())) {
                role = "user";
            }
            messages.add(new DeepSeekClient.DeepSeekMessage(role, item.content()));
        }

        if (historyLimit == 0 || !"user".equals(recentMessages.get(recentMessages.size() - 1).speaker())) {
            messages.add(new DeepSeekClient.DeepSeekMessage("user", userMessage));
        }

        return messages;
    }

    private String buildSystemPrompt(UserProfile user) {
        AppDtos.DashboardResponse dashboard = dashboardService.getDashboard(user);
        AppDtos.AnalyticsResponse analytics = analyticsService.getAnalytics();

        StringBuilder macros = new StringBuilder();
        for (AppDtos.MacroProgress macro : dashboard.macros()) {
            if (macros.length() > 0) {
                macros.append("; ");
            }
            macros.append(macro.label())
                .append(": ")
                .append(macro.value())
                .append("/")
                .append(macro.target())
                .append(macro.unit());
        }

        return """
You are the DeepSeek health coach inside the LightBalance app.
Reply in simplified Chinese. Use only the health data provided in the prompt.
Give practical, same-day advice about food, training, hydration, recovery, and sleep.
Do not provide medical diagnosis or medication advice.
If a question mentions illness, medicine, or urgent symptoms, tell the user to contact a doctor.
Keep replies clear, warm, and usually within 3 to 6 sentences.

Current user:
- Current time: %s
- Name: %s
- Weight: %.1f kg
- Target weight: %.1f kg
- BMI: %.1f
- Body fat: %.1f%%
- Calories today: %d / %d kcal
- Water today: %d / %d ml
- Sleep today: %.1f h
- Sleep score: %d
- Steps today: %d / %d
- Workout today: %d / %d min
- Macro balance: %s

Model summary:
- Sample count: %d
- High-risk rows: %d
- Best model: %s
- Logistic regression accuracy: %.2f%%
""".formatted(
            LocalDateTime.now(APP_ZONE).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            dashboard.profile().name(),
            dashboard.profile().weight(),
            dashboard.profile().targetWeight(),
            dashboard.profile().bmi(),
            dashboard.profile().bodyFat(),
            dashboard.summary().calories(),
            dashboard.summary().calorieTarget(),
            dashboard.summary().water(),
            dashboard.summary().waterTarget(),
            dashboard.summary().sleepHours(),
            dashboard.summary().sleepScore(),
            dashboard.summary().steps(),
            dashboard.summary().stepTarget(),
            dashboard.summary().workoutMinutes(),
            dashboard.summary().workoutTarget(),
            macros,
            analytics.datasetSummary().sampleCount(),
            analytics.datasetSummary().highRiskCount(),
            analytics.selectedModel(),
            analytics.models().isEmpty() ? 0 : analytics.models().get(0).accuracy() * 100
        );
    }

    private String buildTrendAdvicePrompt(UserProfile user) {
        AppDtos.DashboardResponse dashboard = dashboardService.getDashboard(user);
        List<AppDtos.TrendPoint> recent = dashboard.trends()
            .stream()
            .skip(Math.max(0, dashboard.trends().size() - 7))
            .toList();
        StringBuilder trendLines = new StringBuilder();
        for (AppDtos.TrendPoint point : recent) {
            if (trendLines.length() > 0) {
                trendLines.append("\n");
            }
            trendLines.append("- ")
                .append(point.date())
                .append(": weight ")
                .append(point.weight())
                .append(" kg, sleep ")
                .append(point.sleepHours())
                .append(" h, steps ")
                .append(point.steps())
                .append(", calories ")
                .append(point.calories())
                .append(" kcal, stress ")
                .append(point.stressScore());
        }
        return """
Based on the last 7 trend entries, produce a concise trend recommendation in simplified Chinese.
Requirements:
1. Judge the trends for weight, sleep, and stress.
2. Give the 3 most important actions for the next 24 hours.
3. Do not make medical claims.
4. Keep it under 120 Chinese characters.

Recent trends:
%s
""".formatted(trendLines);
    }

    private String now() {
        return LocalTime.now(APP_ZONE).format(TIME_FORMATTER);
    }

    private String timeMinutesAgo(int minutes) {
        return LocalDateTime.now(APP_ZONE).minusMinutes(Math.max(1, minutes)).format(TIME_FORMATTER);
    }
}
