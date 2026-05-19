package com.lightbalance.health.service;

import com.lightbalance.health.config.DeepSeekProperties;
import com.lightbalance.health.dto.AppDtos;
import com.lightbalance.health.dto.SeedData;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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
    private final List<AppDtos.AssistantMessage> conversation = new CopyOnWriteArrayList<>();
    private final List<String> quickPrompts;

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
        SeedData.AppSeed appSeed = resourceDataLoader.getAppSeed();
        for (int index = 0; index < appSeed.assistantConversation().size(); index++) {
            SeedData.ConversationSeed item = appSeed.assistantConversation().get(index);
            conversation.add(new AppDtos.AssistantMessage(
                item.speaker(),
                item.tag(),
                item.title(),
                item.content(),
                timeMinutesAgo(18 - (index * 3))
            ));
        }
        this.quickPrompts = appSeed.coachPrompts();
    }

    public AppDtos.AssistantResponse getConversation() {
        return new AppDtos.AssistantResponse(List.copyOf(conversation), quickPrompts, runtime());
    }

    public AppDtos.AssistantResponse reply(String userMessage) {
        conversation.add(new AppDtos.AssistantMessage("user", "你", "综合平衡", userMessage, now()));

        DeepSeekClient.DeepSeekResult result = deepSeekClient.createChatCompletion(buildRequestMessages(userMessage));
        if (result.success()) {
            conversation.add(new AppDtos.AssistantMessage("assistant", "DeepSeek", "智能建议", result.content(), now()));
        } else if (!result.configured()) {
            conversation.add(new AppDtos.AssistantMessage(
                "assistant",
                "DeepSeek",
                "接入提示",
                "DeepSeek 已经集成到系统里了，但当前还没有检测到 API Key。请先配置环境变量 DEEPSEEK_API_KEY，然后重启后端服务。",
                now()
            ));
        } else {
            conversation.add(new AppDtos.AssistantMessage(
                "assistant",
                "DeepSeek",
                "服务异常",
                "这次没有成功从 DeepSeek 拿到回复。请检查 API Key、网络或模型配置后再试一次。错误信息：" + result.errorMessage(),
                now()
            ));
        }

        return getConversation();
    }

    public AppDtos.TrendAdviceResponse trendAdvice() {
        List<DeepSeekClient.DeepSeekMessage> messages = List.of(
            new DeepSeekClient.DeepSeekMessage("system", buildSystemPrompt()),
            new DeepSeekClient.DeepSeekMessage("user", buildTrendAdvicePrompt())
        );
        DeepSeekClient.DeepSeekResult result = deepSeekClient.createChatCompletion(messages);
        String advice;
        if (result.success()) {
            advice = result.content();
        } else if (!result.configured()) {
            advice = "DeepSeek 当前还没有配置 API Key。趋势上看，可以先优先稳定睡眠、控制压力分，并保持最近的步数节奏；配置 DEEPSEEK_API_KEY 后这里会生成更具体的大模型建议。";
        } else {
            advice = "DeepSeek 暂时没有返回成功结果。先按当前趋势做保守调整：保持近 7 天平均睡眠不低于 7 小时，压力分高于 50 的日期减少高强度训练，并把饮水目标拆到白天完成。错误信息：" + result.errorMessage();
        }
        return new AppDtos.TrendAdviceResponse(advice, runtime(), now());
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

    private List<DeepSeekClient.DeepSeekMessage> buildRequestMessages(String userMessage) {
        List<DeepSeekClient.DeepSeekMessage> messages = new ArrayList<>();
        messages.add(new DeepSeekClient.DeepSeekMessage("system", buildSystemPrompt()));

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

    private String buildSystemPrompt() {
        AppDtos.DashboardResponse dashboard = dashboardService.getDashboard();
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
你是 LightBalance 健康生活分析软件中的 DeepSeek 智能助理。

你的职责：
1. 使用简体中文回答。
2. 结合系统提供的健康数据，给出饮食、训练、睡眠、饮水、恢复方面的具体建议。
3. 优先给出可执行、当日可落实的建议，避免空泛说教。
4. 不要捏造不存在的数据；只能基于上下文中的数据和用户消息推断。
5. 不要进行医疗诊断、药物处方或夸大风险；如果问题涉及疾病、药物或紧急症状，提醒用户及时咨询医生。
6. 回复保持亲切、专业、具体，通常控制在 3 到 6 句话。

当前用户概况：
- 当前日期时间：%s
- 姓名：%s
- 当前体重：%.1f kg，目标体重：%.1f kg
- BMI：%.1f，体脂率：%.1f%%
- 今日热量：%d / %d kcal
- 今日饮水：%d / %d ml
- 今日睡眠：%.1f h，睡眠分：%d
- 今日步数：%d / %d
- 今日训练：%d / %d min
- 当前营养结构：%s

模型分析摘要：
- 样本量：%d
- 高风险样本：%d
- 当前表现最好的模型：%s
- 逻辑回归 Accuracy：%.2f%%

请把回答做成一个真正有帮助的健康教练，而不是泛泛的聊天机器人。
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

    private String buildTrendAdvicePrompt() {
        AppDtos.DashboardResponse dashboard = dashboardService.getDashboard();
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
                .append(": 体重 ")
                .append(point.weight())
                .append(" kg，睡眠 ")
                .append(point.sleepHours())
                .append(" h，步数 ")
                .append(point.steps())
                .append("，热量 ")
                .append(point.calories())
                .append(" kcal，压力 ")
                .append(point.stressScore());
        }
        return """
请根据最近 7 条趋势数据，给出一段简短但具体的趋势建议。
要求：
1. 先判断体重、睡眠、压力三个方向的趋势。
2. 给出接下来 24 小时最该做的 3 个动作。
3. 不要夸大风险，不要医疗诊断。
4. 控制在 120 字以内。

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
