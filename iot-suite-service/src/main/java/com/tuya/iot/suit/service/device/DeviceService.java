package com.tuya.iot.suit.service.device;


import com.tuya.iot.suit.ability.device.model.DeviceCommandRequest;
import com.tuya.iot.suit.ability.device.model.DeviceModifyRequest;
import com.tuya.iot.suit.ability.device.model.DeviceSpecification;
import com.tuya.iot.suit.service.dto.DeviceDTO;

import java.util.List;

/**
 * Description  TODO
 *
 * @author Chyern
 * @since 2021/3/27
 */
public interface DeviceService {

    DeviceDTO getDeviceBy(String deviceId);

    List<DeviceDTO> getDevicesBy(List<String> deviceIdList);

    DeviceSpecification selectDeviceSpecification(String deviceId);

    Boolean deleteDeviceBy(String deviceId);

    Boolean modifyDevice(String deviceId, DeviceModifyRequest request);

    Boolean commandDevice(String deviceId, DeviceCommandRequest request);
}
