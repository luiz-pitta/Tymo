package io.development.tymo.utils;

import android.content.Context;

import io.development.tymo.R;

public class ServerMessage {

    public static String getServerMessage(Context context, String error) {

        switch (error) {
            case "ACTIVITY_DELETED_SUCCESSFULLY":
                return context.getResources().getString(R.string.server_message_activity_deleted_successfully);
            case "ACTIVITY_REGISTERED_SUCCESSFULLY":
                return context.getResources().getString(R.string.server_message_activity_registered_successfully);
            case "ACTIVITY_UPDATED_SUCCESSFULLY":
                return context.getResources().getString(R.string.server_message_activity_updated_successfully);
            case "ALREADY_EXISTS_A_REQUEST_TO_ADD":
                return context.getResources().getString(R.string.server_message_already_exists_a_request_to_add);
            case "CHECK_MAIL_FOR_INSTRUCTIONS":
                return context.getResources().getString(R.string.server_message_check_mail_for_instructions);
            case "CONTACT_DELETED":
                return context.getResources().getString(R.string.server_message_contact_deleted);
            case "DOES_NOT_EXIST_APP_INFO":
                return context.getResources().getString(R.string.server_message_does_not_exist_app_info);
            case "DOES_NOT_EXIST_BACKGROUND":
                return context.getResources().getString(R.string.server_message_does_not_exist_background);
            case "DOES_NOT_EXIST_ICON":
                return context.getResources().getString(R.string.server_message_does_not_exist_icon);
            case "DOES_NOT_EXIST_INTEREST":
                return context.getResources().getString(R.string.server_message_does_not_exist_interest);
            case "DOES_NOT_EXIST_TAG":
                return context.getResources().getString(R.string.server_message_does_not_exist_tag);
            case "FLAG_REGISTERED_SUCCESSFULLY":
                return context.getResources().getString(R.string.server_message_flag_registered_successfully);
            case "FLAG_UPDATED_SUCCESSFULLY":
                return context.getResources().getString(R.string.server_message_flag_updated_successfully);
            case "FROM_FACEBOOK":
                return context.getResources().getString(R.string.server_message_facebook_error);
            case "INTERNAL_SERVER_ERROR":
                return context.getResources().getString(R.string.server_message_internal_server_error);
            case "INVALID_PASSWORD":
                return context.getResources().getString(R.string.server_message_password_updated_error);
            case "INVALID_REQUEST":
                return context.getResources().getString(R.string.server_message_invalid_request);
            case "INVALID_TOKEN":
                return context.getResources().getString(R.string.server_message_invalid_token);
            case "INVITED_SUCCESSFULLY":
                return context.getResources().getString(R.string.server_message_invited_successfully);
            case "NO_GUEST":
                return context.getResources().getString(R.string.server_message_no_guest);
            case "NO_TAG":
                return context.getResources().getString(R.string.server_message_no_tag);
            case "PASSWORD_UPDATED_SUCCESSFULLY":
                return context.getResources().getString(R.string.server_message_password_updated_successfully);
            case "REGISTER":
                return context.getResources().getString(R.string.server_message_register);
            case "RELATIONSHIP_UPDATED_SUCCESSFULLY":
                return context.getResources().getString(R.string.server_message_relationship_updated_successfully);
            case "REMINDER_REGISTERED_SUCCESSFULLY":
                return context.getResources().getString(R.string.server_message_reminder_registered_successfully);
            case "REMINDER_UPDATED_SUCCESSFULLY":
                return context.getResources().getString(R.string.server_message_reminder_updated_successfully);
            case "REQUEST_TO_ADD_ACCEPTED":
                return context.getResources().getString(R.string.server_message_request_to_add_accepted);
            case "SUCCESSFULLY":
                return context.getResources().getString(R.string.server_message_successfully);
            case "TRY_AGAIN":
                return context.getResources().getString(R.string.server_message_try_again);
            case "USER_ALREADY_REGISTERED":
                return context.getResources().getString(R.string.server_message_user_already_registered);
            case "USER_REGISTERED_SUCCESSFULLY":
                return context.getResources().getString(R.string.server_message_user_registered_successfully);
            case "USER_WITHOUT_CONTACTS":
                return context.getResources().getString(R.string.server_message_user_without_contacts);
            case "USER_WITHOUT_PAST_COMMITMENTS":
                return context.getResources().getString(R.string.server_message_user_without_past_commitments);
            case "WITHOUT_NOTIFICATION":
                return context.getResources().getString(R.string.server_message_without_notification);
            case "WORKED":
                return context.getResources().getString(R.string.server_message_worked);
            case "USER_NOT_FOUND":
                return context.getResources().getString(R.string.server_message_user_not_registered);
            default:
                return context.getResources().getString(R.string.server_failure);
        }
    }
}
