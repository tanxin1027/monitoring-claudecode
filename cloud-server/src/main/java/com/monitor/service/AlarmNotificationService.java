package com.monitor.service;

import com.monitor.entity.AlarmRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 告警通知服务 - 发送短信或微信通知
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmNotificationService {

    /**
     * 发送短信通知
     */
    public void sendSmsNotification(AlarmRecord alarmRecord, String recipientPhone) {
        try {
            // TODO: 集成实际短信服务（如阿里云短信、腾讯云短信等）
            // 这里仅做日志记录，实际项目中需要调用短信 API
            log.info("【短信告警】发送到：{}, 内容：{}", recipientPhone, alarmRecord.getAlarmContent());

            // 示例：阿里云短信发送代码（需要根据实际配置）
            // SendSmsRequest request = new SendSmsRequest();
            // request.setPhoneNumbers(recipientPhone);
            // request.setSignName("您的签名名称");
            // request.setTemplateCode("您的模板 CODE");
            // request.setTemplateParameters("{\"alarmContent\":\"" + alarmRecord.getAlarmContent() + "\"}");
            // client.sendSms(request);

        } catch (Exception e) {
            log.error("Send SMS notification failed, alarmId={}", alarmRecord.getId(), e);
        }
    }

    /**
     * 发送微信通知
     */
    public void sendWechatNotification(AlarmRecord alarmRecord, String recipientOpenId) {
        try {
            // TODO: 集成实际微信服务（如企业微信、微信公众号模板消息等）
            // 这里仅做日志记录，实际项目中需要调用微信 API
            log.info("【微信告警】发送到：{}, 内容：{}", recipientOpenId, alarmRecord.getAlarmContent());

            // 示例：企业微信机器人 Webhook 通知
            // String webhookUrl = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxx";
            // JSONObject content = new JSONObject();
            // content.put("msgtype", "markdown");
            // JSONObject markdown = new JSONObject();
            // markdown.put("content", buildWechatMessage(alarmRecord));
            // content.put("markdown", markdown);
            // restTemplate.postForObject(webhookUrl, content, String.class);

            // 示例：微信公众号模板消息
            // WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
            //     .toUser(recipientOpenId)
            //     .templateId("templateId")
            //     .data(buildTemplateData(alarmRecord))
            //     .build();
            // wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);

        } catch (Exception e) {
            log.error("Send Wechat notification failed, alarmId={}", alarmRecord.getId(), e);
        }
    }

    /**
     * 构建微信消息内容（企业微信 Markdown 格式）
     */
    private String buildWechatMessage(AlarmRecord alarmRecord) {
        StringBuilder sb = new StringBuilder();
        sb.append("### 监控告警通知\n");
        sb.append("> 实例名称：").append(alarmRecord.getInstanceName()).append("\n");
        sb.append("> 告警内容：").append(alarmRecord.getAlarmContent()).append("\n");
        sb.append("> 当前值：").append(alarmRecord.getCurrentValue()).append("\n");
        sb.append("> 连续次数：").append(alarmRecord.getContinuousCount()).append("\n");

        String severityText;
        switch (alarmRecord.getSeverity()) {
            case 3: severityText = "严重"; break;
            case 2: severityText = "警告"; break;
            default: severityText = "提示";
        }
        sb.append("> 严重程度：").append(severityText).append("\n");
        sb.append("> 告警时间：").append(alarmRecord.getAlarmTime()).append("\n");

        return sb.toString();
    }
}
