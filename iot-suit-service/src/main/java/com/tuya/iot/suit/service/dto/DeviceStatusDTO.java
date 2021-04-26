package com.tuya.iot.suit.service.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Description  TODO
 *
 * @author Chyern
 * @since 2021/3/9
 */
@Data
public class DeviceStatusDTO implements Serializable {

    private static final long serialVersionUID = 61536779439100968L;

    /**
     * 设备状态名
     */
    private String code;

    /**
     * 设备状态值
     */
    private Object value;
}
