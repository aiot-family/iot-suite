package com.tuya.iot.suit.ability.user.ability;

import com.tuya.iot.openapi.model.PageResult;
import com.tuya.iot.suit.ability.user.model.*;

/**
 * Description  TODO
 *
 * @author Chyern
 * @date 2021/3/26
 */
public interface UserAbility {

    UserToken loginUser(UserRegisteredRequest request);

    /**
     * Registered users
     *
     * @param request
     * @return
     */
    User registeredUser(UserRegisteredRequest request);

    /**
     * Destruction of the user
     *
     * @param userId
     * @return
     */
    Boolean destroyUser(String userId);


    /**
     * Modify Password
     *
     * @param userId
     * @param request
     * @return
     */
    Boolean modifyUserPassword(String userId, UserModifyRequest request);

    /**
     * Find users
     *
     * @param lastRowKey
     * @param pageSize
     * @return
     */
    PageResult<User> selectUsers(String lastRowKey, Integer pageSize);

    /**
     * Find user
     *
     * @param userId
     * @return
     */
    User selectUser(String userId);

    /**
     *  mobile country
     * @return
     */
    MobileCountries selectMobileCountries();


}
