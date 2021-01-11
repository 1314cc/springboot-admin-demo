package springboot.admin.server.vo;

import lombok.Data;

@Data
public class DingTalkTextMessage<T>{

    private String msgtype = "text";

    private T text;

    private boolean isAtAll = true;

}
