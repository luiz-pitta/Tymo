package io.development.tymo.network;

import java.util.ArrayList;

import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.DateTymo;
import io.development.tymo.model_server.FilterServer;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.FriendRequest;
import io.development.tymo.model_server.IconServer;
import io.development.tymo.model_server.AppInfoServer;
import io.development.tymo.model_server.BgFeedServer;
import io.development.tymo.model_server.BgProfileServer;
import io.development.tymo.model_server.InviteRequest;
import io.development.tymo.model_server.Plans;
import io.development.tymo.model_server.Query;
import io.development.tymo.model_server.ReminderServer;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.TagServer;
import io.development.tymo.model_server.User;
import io.development.tymo.model_server.UserPushNotification;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;

public interface RetrofitInterface {

    @POST("users")
    Observable<Response> register(@Body User user);

    @POST("update_interest")
    Observable<Response> updateInterest(@Body User user);

    @POST("users_update")
    Observable<Response> updateUser(@Body User user);

    @POST("update_email")
    Observable<Response> updateEmail(@Body User user);

    @POST("import_from_facebook")
    Observable<Response> registerActivityFacebook(@Body ArrayList<ActivityServer> activitiesServer);

    @POST("import_from_google")
    Observable<Response> registerActivityGoogle(@Body ArrayList<ActivityServer> activitiesServer);

    @POST("activities")
    Observable<Response> registerActivity(@Body ActivityServer activityServer);

    @POST("reminders")
    Observable<Response> registerReminder(@Body ReminderServer reminderServer);

    @POST("flags")
    Observable<Response> registerFlag(@Body FlagServer flagServer);

    @POST("friend_request/{email}")
    Observable<Response> registerFriendRequest(@Path("email") String email, @Body User user);

    @POST("get_friends_friend/{email}")
    Observable<Response> getFriendsFriend(@Path("email") String email, @Body User user);

    @POST("visualize_friend_request/{email}")
    Observable<Response> visualizeFriendRequest(@Path("email") String email);

    @POST("visualize_invite_request/{email}")
    Observable<Response> visualizeInviteRequest(@Path("email") String email);


    @POST("delete_push_notification")
    Observable<Response> deletePushNotification(@Body UserPushNotification pushNotification);

    @POST("set_push_notification")
    Observable<Response> setPushNotification(@Body UserPushNotification pushNotification);

    @POST("favorite_friend/{email}")
    Observable<Response> registerFavoriteRequest(@Path("email") String email, @Body User user);

    @POST("block_friend/{email}")
    Observable<Response> registerBlockRequest(@Path("email") String email, @Body User user);

    @POST("block_friends_list/{email}")
    Observable<Response> registerBlockPeopleRequest(@Path("email") String email, @Body User user);

    @POST("delete_friend/{email}")
    Observable<Response> registerDeleteRequest(@Path("email") String email, @Body User user);

    @POST("update_friend_request")
    Observable<Response> updateFriendRequest(@Body FriendRequest friendRequest);

    @POST("cancel_friend_request")
    Observable<Response> cancelFriendRequest(@Body FriendRequest friendRequest);

    @POST("update_invite_request")
    Observable<Response> updateInviteRequest(@Body InviteRequest inviteRequest);

    @POST("set_adm_activity")
    Observable<Response> setAdm(@Body InviteRequest inviteRequest);

    @POST("add_new_guest")
    Observable<Response> addNewGuest(@Body ActivityServer activityServer);

    @POST("set_privacy_activity")
    Observable<Response> setPrivacyAct(@Body ActivityServer activityServer);

    @POST("delete_activity/{id}")
    Observable<Response> deleteActivity(@Path("id") long id, @Body ActivityServer activityServer);

    @POST("authenticate")
    Observable<Response> login();

    @POST("authenticateFacebook/")
    Observable<Response> loginFacebook(@Body User user);

    @POST("search")
    Observable<Response> getSearchResults(@Body Query query);

    @POST("get_profile_main")
    Observable<Response> getProfileMain(@Body Query query);

    @POST("get_pending_solicitation")
    Observable<Response> getPendingSolicitaion(@Body Query query);

    @POST("plans")
    Observable<Response> getPlans(@Body Plans plans);

    @POST("friends_plans")
    Observable<Response> getFriendsPlans(@Body Plans plans);

    @POST("compare")
    Observable<Response> getCompare(@Body Plans plans);

    @POST("get_activity_day")
    Observable<Response> getActivityDay(@Body Query query);

    @POST("feed/{email}")
    Observable<Response> getFeedActivities(@Path("email") String email, @Body DateTymo date);

    @POST("feed_filter/{email}")
    Observable<Response> getFeedFilter(@Path("email") String email, @Body FilterServer filterServer);

    @POST("search_filter/{email}")
    Observable<Response> getSearchFilter(@Path("email") String email, @Body FilterServer filterServer);

    @POST("set_location/")
    Observable<User> setLocationUser(@Body User user);

    @POST("set_privacy/")
    Observable<User> setPrivacyUser(@Body User user);

    @POST("delete_account/{email}")
    Observable<User> deleteAccount(@Path("email") String email);

    @POST("set_notification/")
    Observable<User> setNotificationUser(@Body User user);

    @GET("tags")
    Observable<ArrayList<TagServer>> getTags();

    @GET("icons")
    Observable<ArrayList<IconServer>> getIcons();

    @GET("appInfo")
    Observable<ArrayList<AppInfoServer>> getAppInfo();

    @GET("bgFeed")
    Observable<ArrayList<BgFeedServer>> getBgFeed();

    @GET("bgProfile")
    Observable<ArrayList<BgProfileServer>> getBgProfile();

    @GET("interest")
    Observable<ArrayList<TagServer>> getInterest();

    @GET("interest/{email}")
    Observable<Response> getInterest(@Path("email") String email);

    @GET("get_flag_reminder/{id}")
    Observable<Response> getFlagReminder(@Path("id") long id);

    @GET("get_act/{id}")
    Observable<Response> getActivity(@Path("id") long id);

    @POST("get_flag2/{id}")
    Observable<Response> getFlag2(@Path("id") long id, @Body FlagServer flagServer);

    @POST("get_act2/{id}")
    Observable<Response> getActivity2(@Path("id") long id, @Body ActivityServer activityServer);

    @POST("get_invite_request/{email}")
    Observable<Response> getInviteRequest(@Path("email") String email, @Body DateTymo date);

    @GET("get_friends/{email}")
    Observable<ArrayList<User>> getUsers(@Path("email") String email);

    @GET("get_contact_us/")
    Observable<Response> getContactUs();

    @GET("get_imported_google/{email}")
    Observable<Response> getImportedGoogle(@Path("email") String email);

    @GET("get_blocked_users/{email}")
    Observable<ArrayList<User>> getBlockedUsers(@Path("email") String email);

    @GET("get_friends_request/{email}")
    Observable<Response> getFriendRequest(@Path("email") String email);

    @POST("get_past_activities/{email}")
    Observable<ArrayList<ActivityServer>> getPastActivities(@Path("email") String email, @Body DateTymo date);

    @GET("users/{email}")
    Observable<User> getProfile(@Path("email") String email);

    @POST("change_password/")
    Observable<Response> changePassword(@Body User user);

    @POST("users/{email}/password")
    Observable<Response> resetPasswordInit(@Path("email") String email);

    @POST("users/{email}/password")
    Observable<Response> resetPasswordFinish(@Path("email") String email, @Body User user);

    @POST("edit_flag/{id}")
    Observable<Response> editFlag(@Path("id") long id, @Body ActivityServer activityServer);

    @POST("edit_reminder/{id}")
    Observable<Response> editReminder(@Path("id") long id, @Body ActivityServer activityServer);

    @POST("edit_activity/{id}")
    Observable<Response> editActivity(@Path("id") long id, @Body ActivityServer activityServer);

    @POST("edit_flag_repeat_single/{id}")
    Observable<Response> editFlagRepeatSingle(@Path("id") long id, @Body ActivityServer activityServer);

    @POST("edit_reminder_repeat_single/{id}")
    Observable<Response> editReminderRepeatSingle(@Path("id") long id, @Body ActivityServer activityServer);

    @POST("edit_activity_repeat_single/{id}")
    Observable<Response> editActivityRepeatSingle(@Path("id") long id, @Body ActivityServer activityServer);

}
