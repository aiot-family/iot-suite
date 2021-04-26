package com.tuya.iot.suit.ability.user.connector;

import com.tuya.iot.framework.api.annotations.*;
import com.tuya.iot.openapi.model.PageResult;
import com.tuya.iot.suit.ability.user.ability.UserAbility;
import com.tuya.iot.suit.ability.user.model.*;

/**
 * Description  TODO
 *
 * @author Chyern
 * @date 2021/3/26
 */
public interface UserConnector extends UserAbility {

    @Override
    @POST("/v1.0/iot-03/users/login")
    UserToken loginUser(@Body UserRegisteredRequest request);

    @Override
    @POST("/v1.0/iot-02/users")
    User registeredUser(@Body UserRegisteredRequest request);

    @Override
    @DELETE("/v1.0/iot-02/users/{user_id}")
    Boolean destroyUser(@Path("user_id") String userId);

    @Override
    @PUT("/v1.0/iot-02/users/{user_id}")
    Boolean modifyUserPassword(@Path("user_id") String userId, @Body UserModifyRequest request);

    @Override
    @GET("/v1.0/iot-02/users")
    PageResult<User> selectUsers(@Query("last_row_key") String lastRowKey, @Query("page_size") Integer pageSize);

    @Override
    @GET("/v1.0/iot-02/users/{user_id}")
    User selectUser(@Path("user_id") String userId);

    @Override
    @GET("/v1.0/all-countries")
    MobileCountries selectMobileCountries();
}
