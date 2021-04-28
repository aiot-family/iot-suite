package com.tuya.iot.suit.ability.notification.ability;


import com.tuya.iot.suit.ability.notification.model.BasePushResult;
import com.tuya.iot.suit.ability.notification.model.TemplatesResult;
import com.tuya.iot.suit.ability.notification.model.VoicePushRequest;
import com.tuya.iot.suit.ability.notification.model.VoiceTemplateRequest;

/**
 * <p> TODO
 *
 * @author 哲也（张梓濠 zheye.zhang@tuya.com）
 * @since 2021/4/14
 */
public interface VoiceAbility {

    BasePushResult push(VoicePushRequest request);

    TemplatesResult applyTemplate(VoiceTemplateRequest voiceTemplateRequest);


}
