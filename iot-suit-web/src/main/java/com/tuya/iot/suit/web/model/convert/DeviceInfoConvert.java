package com.tuya.iot.suit.web.model.convert;

import com.tuya.iot.suit.service.dto.DeviceDTO;
import com.tuya.iot.suit.web.model.DeviceInfoVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * <p> TODO
 *
 * @author 哲也（张梓濠 zheye.zhang@tuya.com）
 * @since 2021/4/23
 */
@Mapper
public interface DeviceInfoConvert {

    DeviceInfoConvert INSTANCE = Mappers.getMapper(DeviceInfoConvert.class);

    DeviceInfoVO deviceDTO2VO(DeviceDTO deviceDTO);

    List<DeviceInfoVO> deviceDTO2VO(List<DeviceDTO> list);
}
