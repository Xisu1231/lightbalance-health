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
        conversation.add(new AppDtos.AssistantMessage("user", user.getName(), "今日记录", userMessage, now()));

        DeepSeekClient.DeepSeekResult result = deepSeekClient.createChatCompletion(buildRequestMessages(user, conversation, userMessage));
        if (result.success()) {
            conversation.add(new AppDtos.AssistantMessage("assistant", "DeepSeek", "智能建议", result.content(), now()));
        } else if (!result.configured()) {
            conversation.add(new AppDtos.AssistantMessage(
                "assistant",
                "DeepSeek",
                "配置提醒",
                "DeepSeek 已接入系统，但当前没有检测到可用的 API Key。请在部署环境中配置 DEEPSEEK_API_KEY 后重新部署。",
                now()
            ));
        } else {
            conversation.add(new AppDtos.AssistantMessage(
                "assistant",
                "DeepSeek",
                "调用异常",
                "这次 DeepSeek 没有成功返回结果，请检查 API Key、网络或模型配置。错误信息：" + result.errorMessage(),
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
            advice = "当前还没有配置 DeepSeek API Key。先优先保证睡眠规律、白天分次补水，并在高压力日把训练强度降一级。";
        } else {
            advice = "DeepSeek 这次没有成功返回结果。先把睡眠守在 7 小时以上，压力高的日子降低高强度训练，并把补水分散到全天。错误信息：" + result.errorMessage();
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
你是 LightBalance 应用内的 DeepSeek 健康教练。
请只依据提示中提供的数据，用简体中文回答。
请给出当天就能执行的饮食、训练、补水、恢复和睡眠建议。
不要给出医学诊断、用药建议或夸大判断。
如果用户提到疾病、药物或紧急症状，请明确建议联系医生。
语气温和、结论明确，通常控制在 3 到 6 句。

当前用户信息：
- 当前时间：%s
- 姓名：%s
- 当前体重：%.1f kg
- 目标体重：%.1f kg
- BMI：%.1f
- 体脂率：%.1f%%
- 今日热量：%d / %d kcal
- 今日饮水：%d / %d ml
- 今日睡眠：%.1f h
- 睡眠评分：%d
- 今日步数：%d / %d
- 今日训练：%d / %d min
- 营养结构：%s

模型摘要：
- 样本量：%d
- 高风险样本：%d
- 当前最佳模型：%s
- 逻辑回归准确率：%.2f%%
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
请基于最近 7 条趋势记录，生成一段简洁的中文趋势建议。
要求：
1. 判断体重、睡眠和压力的变化趋势。
2. 给出接下来 24 小时最重要的 3 个行动建议。
3. 不要给出医疗结论。
4. 尽量控制在 120 个汉字以内。

最近趋势：
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
