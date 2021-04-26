package com.tuya.iot.suit.ability.notification.ability;


import com.tuya.iot.suit.ability.notification.model.BasePushResult;
import com.tuya.iot.suit.ability.notification.model.MailPushRequest;
import com.tuya.iot.suit.ability.notification.model.MailTemplateRequest;
import com.tuya.iot.suit.ability.notification.model.TemplatesResult;

/**
 * <p> TODO
 *
 * @author 哲也（张梓濠 zheye.zhang@tuya.com）
 * @since 2021/4/13
 */
public interface MailAbility {

    BasePushResult push(MailPushRequest request);

    TemplatesResult applyTemplate(MailTemplateRequest mailTemplateRequest);

}
