package com.tuya.iot.suit.service.dto;

import lombok.Data;

import java.util.List;

/**
 * Description  TODO
 *
 * @author Chyern
 * @since 2021/3/9
 */
@Data
public class AssetDTO {

    /**
     * 资产Id
     */
    private String asset_id;

    /**
     * 资产名
     */
    private String asset_name;

    /**
     * 资产全名
     */
    private String asset_full_name;

    /**
     * 父资产Id
     */
    private String parent_asset_id;

    /**
     * 子资产数量
     */
    private Integer child_asset_count;

    /**
     * 子设备数量
     */
    private Integer child_device_count;

    /**
     * 子资产
     */
    private List<AssetDTO> subAssets;
}
