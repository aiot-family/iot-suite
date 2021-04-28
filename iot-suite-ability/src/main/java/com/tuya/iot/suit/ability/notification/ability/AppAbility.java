package com.tuya.iot.suit.ability.notification.ability;

import com.tuya.iot.framework.api.annotations.Body;
import com.tuya.iot.suit.ability.notification.model.AppPushRequest;
import com.tuya.iot.suit.ability.notification.model.AppTemplateRequest;
import com.tuya.iot.suit.ability.notification.model.BasePushResult;
import com.tuya.iot.suit.ability.notification.model.TemplatesResult;

/**
 * @Description:
 * @auther: Medivh.chen@tuya.com
 * @date: 2021/04/14
 **/
public interface AppAbility {
    BasePushResult push(@Body AppPushRequest appPushRequest);

    TemplatesResult applyTemplate(AppTemplateRequest appTemplateRequest);
}
