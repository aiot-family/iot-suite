package com.tuya.iot.suit.service.asset.impl;

import com.google.common.collect.Lists;

import com.tuya.iot.openapi.model.PageResult;
import com.tuya.iot.suit.ability.asset.ability.AssetAbility;
import com.tuya.iot.suit.ability.asset.model.*;
import com.tuya.iot.suit.core.constant.Response;
import com.tuya.iot.suit.core.exception.ServiceLogicException;
import com.tuya.iot.suit.core.util.SimpleConvertUtil;
import com.tuya.iot.suit.service.asset.AssetService;
import com.tuya.iot.suit.service.device.DeviceService;
import com.tuya.iot.suit.service.dto.AssetConvertor;
import com.tuya.iot.suit.service.dto.AssetDTO;
import com.tuya.iot.suit.service.dto.DeviceDTO;
import com.tuya.iot.suit.service.model.PageDataVO;
import lombok.extern.slf4j.Slf4j;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static com.tuya.iot.suit.core.constant.ErrorCode.USER_NOT_AUTH;


/**
 * Description  TODO
 *
 * @author Chyern
 * @since 2021/3/10
 */
@Service
@Slf4j
public class AssetServiceImpl implements AssetService {
    private static final int PAGE_NO = 1;
    private static final int PAGE_SIZE = 100;

    private LoadingCache<String, AssetDTO> ASSET_CACHE = Caffeine
            .newBuilder()
            .expireAfterWrite(Duration.ofMillis(10 * 60 * 1000))
            .build(
                    assetId -> {
                        AssetDTO root = new AssetDTO();
                        root.setAsset_id(assetId);
                        root.setSubAssets(getTree(assetId));
                        return root;
                    }
            );

    @Resource
    private AssetAbility assetAbility;

    @Resource
    private DeviceService deviceService;

    @Override
    public Response addAsset(String assetName, String parentAssetId, String userId) {
        //Don't need check asset authorization if the parentAssetId is blank
        if (!StringUtils.isEmpty(parentAssetId)) {
            checkAssetAuthOfUser(parentAssetId, userId);
        }
        AssetAddRequest request = new AssetAddRequest();
        request.setName(assetName);
        request.setParent_asset_id(parentAssetId);
        String assetId = assetAbility.addAsset(request);
        authorizedAssetWithoutToChildren(assetId, userId);
        addAssetToCache(assetId, request);
        return new Response(assetId);
    }


    /**
     * 对资产授权
     *
     * @param assetId
     * @param userId
     */
    private void authorizedAssetWithoutToChildren(String assetId, String userId) {
        log.info("开始资产[{}]授权给用户[{}]", assetId, userId);
        AssetAuthorizationRequest assetAuthorizationRequest = new AssetAuthorizationRequest(userId, assetId, false);
        Boolean result = assetAbility.authorized(assetAuthorizationRequest);
        if (Objects.isNull(result) || !result.booleanValue()) {
            log.info("资产[{}]授权给用户[{}]失败", assetId, userId);
        } else {
            log.info("资产[{}]授权给用户[{}]成功", assetId, userId);
        }
    }

    @Override
    public Response updateAsset(String userId, String assetId, String assetName) {
        checkAssetAuthOfUser(assetId, userId);
        AssetModifyRequest request = new AssetModifyRequest();
        request.setName(assetName);
        Boolean aBoolean = assetAbility.modifyAsset(assetId, request);
        updateAssetToCache(assetId, assetName);
        return new Response(aBoolean);
    }


    private void checkAssetAuthOfUser(String assetId, String userId) {
        List<String> authList = listAuthorizedAssetIds(userId);
        if (!StringUtils.isEmpty(assetId) && !CollectionUtils.contains(authList.listIterator(), assetId)) {
            log.info("资产[{}]未授权给用户[{}]", assetId, userId);
            throw new ServiceLogicException(USER_NOT_AUTH);
        }
    }

    @Override
    public Response deleteAsset(String userId, String assetId) {
        checkAssetAuthOfUser(assetId, userId);
        Boolean aBoolean = assetAbility.deleteAsset(assetId);
        deleteAssetFromCache(assetId);
        ASSET_CACHE.refresh("-1");
        return new Response(aBoolean);
    }

    /**
     * 手动删除cache中的的数据
     *
     * @param assetId
     */
    private void deleteAssetFromCache(String assetId) {
        AssetDTO son = searchFromCache(assetId);
        AssetDTO assetDTO = searchFromCache(checkTopAsset(son.getParent_asset_id()));
        List<AssetDTO> subAssets = assetDTO.getSubAssets();
        subAssets.remove(son);

    }

    private String checkTopAsset(String parent_asset_id) {
        if (StringUtils.isEmpty(parent_asset_id)) {
            return "-1";
        }
        return parent_asset_id;
    }

    /**
     * 手动给缓存添加最新的资产
     *
     * @param assetId
     * @param request
     */
    private void addAssetToCache(String assetId, AssetAddRequest request) {
        AssetDTO assetDTO = searchFromCache(checkTopAsset(request.getParent_asset_id()));
        AssetDTO son = new AssetDTO();
        son.setAsset_id(assetId);
        son.setChild_asset_count(0);
        son.setAsset_name(request.getName());
        son.setParent_asset_id(request.getParent_asset_id());
        son.setChild_device_count(0);
        son.setAsset_full_name(request.getName());
        son.setSubAssets(new ArrayList<>());
        List<AssetDTO> subAssets = assetDTO.getSubAssets();
        if (subAssets == null) {
            subAssets = new ArrayList<>();
        }
        subAssets.add(son);
        assetDTO.setSubAssets(subAssets);
        assetDTO.setChild_asset_count(subAssets.size());
        ASSET_CACHE.refresh("-1");
    }

    /**
     * 手动给缓存更新的资产
     *
     * @param assetId
     * @param assetName
     */
    private void updateAssetToCache(String assetId, String assetName) {
        AssetDTO assetDTO = searchFromCache(assetId);
        assetDTO.setAsset_full_name(assetDTO.getAsset_full_name()
                .substring(0, assetDTO.getAsset_full_name().length() - assetDTO.getAsset_name().length()) + assetName);
        assetDTO.setAsset_name(assetName);
        ASSET_CACHE.refresh("-1");
    }

    /**
     * 从cache中找到该节点
     *
     * @param assetId
     * @return
     */
    private AssetDTO searchFromCache(String assetId) {
        AssetDTO assetDTO = ASSET_CACHE.get("-1");
        AssetDTO temp = searchFromTree(assetId, assetDTO);
        if (temp != null) {
            return temp;
        }
        return new AssetDTO();
    }

    private AssetDTO searchFromTree(String assetId, AssetDTO assetDTO) {
        if (assetDTO == null) {
            return assetDTO;
        }
        if (assetId.equals(assetDTO.getAsset_id())) {
            return assetDTO;
        }
        if (!CollectionUtils.isEmpty(assetDTO.getSubAssets())) {
            for (AssetDTO subAsset : assetDTO.getSubAssets()) {
                AssetDTO temp = searchFromTree(assetId, subAsset);
                if (temp != null) {
                    return temp;
                }
            }
        }
        return null;
    }

    /**
     * 跟进用户ID 获取授权后的资产
     *
     * @param userId
     * @return
     */
    private List<String> listAuthorizedAssetIds(String userId) {
        log.info("查询用户userId[{}]授权的资产IDs", userId);
        List<String> authorizedAssetIds = new ArrayList<>();

        boolean hasMore = true;
        int pageNo = PAGE_NO;
        while (hasMore) {
            PageResultWithTotal<AuthorizedAsset> pageResult = assetAbility.pageListAuthorizedAssets(userId, pageNo, PAGE_SIZE);
            hasMore = pageResult.getHasMore().booleanValue();
            List<AuthorizedAsset> authorizedAsset = pageResult.getList();
            List<String> collect = authorizedAsset.stream().map(AuthorizedAsset::getAssetId).collect(Collectors.toList());
            authorizedAssetIds.addAll(collect);
            pageNo++;
        }
        log.info("返回的authorizedAssetIds是[{}]", authorizedAssetIds);
        return authorizedAssetIds;
    }

    public void getAssetByName(String assetName, AssetDTO assetDTO, List<AssetDTO> list, List<String> authorizedAssetIds) {
        if (Objects.isNull(assetDTO)) {
            return;
        }
        if (!StringUtils.isEmpty(assetDTO.getAsset_name()) && assetDTO.getAsset_name().contains(assetName)) {
            if (authorizedAssetIds.contains(assetDTO.getAsset_id())) {
                list.add(assetDTO);
            }
        }
        if (CollectionUtils.isEmpty(assetDTO.getSubAssets())) {
            return;
        }
        for (int i = 0; i < assetDTO.getSubAssets().size(); i++) {
            getAssetByName(assetName, assetDTO.getSubAssets().get(i), list, authorizedAssetIds);
        }
    }

    @Override
    public List<AssetDTO> getAssetByName(String assetName, String userId) {
        // TODO 云端API暂时未能提供接口，目前从缓存获取
        /*List<Asset> assets = new ArrayList<>();
        final Integer pageSize = 100;
        boolean hasNext = true;
        String lastRowKey = "";
        while (hasNext) {
            PageResult<Asset> childAssetsBy = assetAbility.selectAssets("", assetName, lastRowKey, pageSize);
            hasNext = childAssetsBy.getHas_next();
            lastRowKey = childAssetsBy.getLast_row_key();
            assets.addAll(childAssetsBy.getList());
        }
        List<AssetDTO> result = SimpleConvertUtil.convert(assets, AssetDTO.class);
        setAssetChildInfo(result);*/
        List<String> authorizedAssetIds = listAuthorizedAssetIds(userId);
        List<AssetDTO> result = new ArrayList<>();
        getAssetByName(assetName, ASSET_CACHE.get("-1"), result, authorizedAssetIds);
        return result;
    }

    @Override
    public List<AssetDTO> getChildAssetListBy(String assetId) {
        List<AssetDTO> result = AssetConvertor.$.toAssetDTOList(getChildAssetsBy(assetId));
        setAssetChildInfo(result);
        return result;
    }

    @Override
    public AssetDTO getTreeBy(String assetId, String userId) {
        AssetDTO tree = ASSET_CACHE.get("-1");
        AssetDTO originalTree = findTree(assetId, Lists.newArrayList(tree));
        return filterAuthorizedTree(originalTree, userId);
    }

    /**
     * 根据用户过滤资产树
     *
     * @param originalTree 原资产树对象
     * @param userId       用户ID
     * @return
     */
    private AssetDTO filterAuthorizedTree(AssetDTO originalTree, String userId) {
        List<String> authorizedAssetIds = listAuthorizedAssetIds(userId);
        return removeNonAuthorizeNode(SimpleConvertUtil.convert(originalTree, AssetDTO.class), authorizedAssetIds);
    }


    private AssetDTO findTree(String assetId, List<AssetDTO> list) {
        for (AssetDTO assetDTO : list) {
            if (assetDTO.getAsset_id().equals(assetId)) {
                return assetDTO;
            } else {
                AssetDTO child = findTree(assetId, assetDTO.getSubAssets());
                if (!StringUtils.isEmpty(child.getAsset_id())) {
                    return child;
                }
            }

        }
        return new AssetDTO();
    }

    private List<AssetDTO> getTree(String assetId) {
        List<AssetDTO> result = AssetConvertor.$.toAssetDTOList(getChildAssetsBy(assetId));
        setAssetChildInfo(result);
        for (AssetDTO assetDTO : result) {
            List<AssetDTO> treeBy = getTree(assetDTO.getAsset_id());
            assetDTO.setSubAssets(treeBy);
        }
        return result;
    }

    private void setAssetChildInfo(List<AssetDTO> list) {
        for (AssetDTO assetDTO : list) {
            int childAssetCount = getChildAssetsBy(assetDTO.getAsset_id()).size();
            int childDeviceCount = getChildDeviceIdsBy(assetDTO.getAsset_id()).size();
            assetDTO.setChild_asset_count(childAssetCount);
            assetDTO.setChild_device_count(childDeviceCount);
        }
    }

    private List<Asset> getChildAssetsBy(String assetId) {
        List<Asset> assets = new ArrayList<>();
        final String pageSize = "100";
        boolean hasNext = true;
        String lastRowKey = "";
        while (hasNext) {
            PageResult<Asset> childAssetsBy = assetAbility.selectChildAssets(assetId, lastRowKey, pageSize);
            hasNext = childAssetsBy.getHas_next();
            lastRowKey = childAssetsBy.getLast_row_key();
            assets.addAll(childAssetsBy.getList());
        }
        return assets;
    }

    @Override
    public List<DeviceDTO> getChildDeviceInfoBy(String assetId) {
        List<String> deviceIdList = getChildDeviceIdsBy(assetId);
        return deviceService.getDevicesBy(deviceIdList);
    }

    @Override
    public PageDataVO<DeviceDTO> getChildDeviceInfoPage(String assetId, Integer pageNo, Integer pageSize) {
        PageDataVO<DeviceDTO> vo = new PageDataVO<>();
        vo.setPage_no(pageNo);
        vo.setPage_size(pageSize);
        vo.setData(Lists.newArrayList());
        List<String> deviceIdList = getChildDeviceIdsBy(assetId);
        if (CollectionUtils.isEmpty(deviceIdList)) {
            vo.setTotal(0);
            return vo;
        }
        vo.setTotal(deviceIdList.size());
        int begin = (pageNo - 1) * pageSize < vo.getTotal() ? (pageNo - 1) * pageSize : vo.getTotal();
        int end = pageNo * pageSize < vo.getTotal() ? pageNo * pageSize : vo.getTotal();
        List<String> resDeviceIdList = deviceIdList.subList(begin, end);
        List<DeviceDTO> devicesBy = deviceService.getDevicesBy(resDeviceIdList);
        vo.setData(devicesBy);
        return vo;
    }

    List<String> getChildDeviceIdsBy(String assetId) {
        List<String> deviceIds = new ArrayList<>();
        final String pageSize = "100";
        boolean hasNext = true;
        String lastRowKey = "";
        while (hasNext) {
            PageResult<Asset> childDevicesBy = assetAbility.selectChildDevices(assetId, lastRowKey, pageSize);
            hasNext = childDevicesBy.getHas_next();
            lastRowKey = childDevicesBy.getLast_row_key();
            deviceIds.addAll(childDevicesBy.getList().stream().map(Asset::getDevice_id).collect(Collectors.toList()));
        }
        return deviceIds;
    }


    private AssetDTO removeNonAuthorizeNode(AssetDTO dto, Collection<String> fullCollection) {
        if (CollectionUtils.isEmpty(fullCollection)) {
            return new AssetDTO();
        }
        Collection<String> tempColl = new HashSet<>();
        tempColl.addAll(fullCollection);
        if (checkAssetMarked(dto, tempColl)) {
            if (!CollectionUtils.isEmpty(tempColl)) {
                ASSET_CACHE.refresh("-1");
                return removeNonAuthorizeNode(SimpleConvertUtil.convert(ASSET_CACHE.get("-1"), AssetDTO.class), fullCollection);
            }
            return dto;
        }
        return new AssetDTO();
    }

    /**
     * 判断资产是否在权限集合内
     *
     * @param dto
     * @param fullCollection
     * @return
     */
    private boolean checkAssetMarked(AssetDTO dto, Collection<String> fullCollection) {
        //设立一个标志，用于判断其子节点下是否包含了有权限的节点
        boolean bx = false;
        int len = 0;
        List<AssetDTO> subAssets = dto.getSubAssets();
        if (!CollectionUtils.isEmpty(subAssets)) {
            bx = checkSubAssetMarked(subAssets, fullCollection);
            len = subAssets.size();
        }
        //将最新的数量设置进去
        dto.setChild_asset_count(len);
        boolean ax = fullCollection.contains(dto.getAsset_id());
        if (ax) {
            fullCollection.remove(dto.getAsset_id());
        }
        //子节点不包含且自身也不在权限集合内，则返回false
        return bx || ax;
    }

    /**
     * 判断自己点集合是否在权限集合内
     *
     * @param subAssets
     * @param fullCollection
     * @return
     */
    private boolean checkSubAssetMarked(List<AssetDTO> subAssets, Collection<String> fullCollection) {
        for (int i = subAssets.size() - 1; i >= 0; i--) {
            AssetDTO subAsset = subAssets.get(i);
            //节点不属于权限内，则移除
            if (!checkAssetMarked(subAsset, fullCollection)) {
                subAssets.remove(i);
            }
        }
        return subAssets.size() > 0;
    }


}
