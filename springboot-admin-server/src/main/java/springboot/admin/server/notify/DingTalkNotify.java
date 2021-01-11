package springboot.admin.server.notify;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.notify.AbstractEventNotifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;
import springboot.admin.server.vo.DingTalkTextMessage;
import springboot.admin.server.vo.TextMessageContent;

import java.util.Date;

@Slf4j
@Service
public class DingTalkNotify extends AbstractEventNotifier {

    /**
     * 消息模板
     */
    private static final String template = "monitor:\n时间: %s \n环境: %s \n服务名:%s(%s) \n状态:%s(%s) \n服务ip:%s";

    @Value("${spring.boot.admin.ding-talk-token}")
    private String dingTalkToken;

    @Value("${spring.profiles.active}")
    private String evn;

    private RestTemplate restTemplate;

    public DingTalkNotify(InstanceRepository repository, RestTemplate restTemplate) {
        super(repository);
        this.restTemplate = restTemplate;
    }

    @Override
    protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
        return Mono.fromRunnable(() -> {
            if (event instanceof InstanceStatusChangedEvent) {
                log.info("Instance {} ({}) is {}", instance.getRegistration().getName(), event.getInstance(),
                        ((InstanceStatusChangedEvent) event).getStatusInfo().getStatus());


                String status = ((InstanceStatusChangedEvent) event).getStatusInfo().getStatus();
                String messageText = null;
                String dateTime = DateFormatUtils.ISO_DATETIME_FORMAT.format(new Date());
                switch (status) {
                    // 健康检查没通过
                    case "DOWN":
                        log.info("发送 健康检查没通过 的通知！");
                        messageText = String.format(template,dateTime, evn, instance.getRegistration().getName(), event.getInstance(), ((InstanceStatusChangedEvent) event).getStatusInfo().getStatus(), "健康检查没通过", instance.getRegistration().getServiceUrl());
                        this.sendMessage(messageText);
                        break;
                    // 服务离线
                    case "OFFLINE":
                        log.info("发送 服务离线 的通知！");
                        messageText = String.format(template,dateTime, evn, instance.getRegistration().getName(), event.getInstance(), ((InstanceStatusChangedEvent) event).getStatusInfo().getStatus(), "服务离线", instance.getRegistration().getServiceUrl());
                        this.sendMessage(messageText);
                        break;
                    //服务上线
                    case "UP":
                        log.info("发送 服务上线 的通知！");
                        messageText = String.format(template,dateTime, evn, instance.getRegistration().getName(), event.getInstance(), ((InstanceStatusChangedEvent) event).getStatusInfo().getStatus(), "服务上线", instance.getRegistration().getServiceUrl());
                        this.sendMessage(messageText);
                        break;
                    // 服务未知异常
                    case "UNKNOWN":
                        log.info("发送 服务未知异常 的通知！");
                        messageText = String.format(template,dateTime, evn, instance.getRegistration().getName(), event.getInstance(), ((InstanceStatusChangedEvent) event).getStatusInfo().getStatus(), "服务未知异常", instance.getRegistration().getServiceUrl());
                        this.sendMessage(messageText);
                        break;
                    default:
                        break;
                }
            } else {
                log.info("Instance {} ({}) {}", instance.getRegistration().getName(), event.getInstance(),
                        event.getType());
            }
        });
    }

    /**
     * 发送消息
     *
     * @param messageText
     */
    private void sendMessage(String messageText) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

        DingTalkTextMessage<TextMessageContent> dingTalkTextMessage = new DingTalkTextMessage<>();
        TextMessageContent textMessageContent = new TextMessageContent();
        textMessageContent.setContent(messageText);
        dingTalkTextMessage.setText(textMessageContent);

        HttpEntity<DingTalkTextMessage> request = new HttpEntity<>(dingTalkTextMessage, headers);
        String url = "https://oapi.dingtalk.com/robot/send?access_token=" + dingTalkToken;
        ResponseEntity<String> stringResponseEntity = restTemplate.postForEntity(url, request, String.class);

        log.info("返回:{}", stringResponseEntity.getBody());

    }
}
