package org.finsible.backend;

public class AppConstants {
    public static final String USER_NOT_FOUND = "User not found";
    public static final String USER_NOT_FOUND_EXCEPTION = "User is not authorised to perform this action";
    public static final String DATA_FETCH_SUCCESS = "Data fetched successfully";
    public static final String REDIRECT_URI = "http://localhost:5173";
    public static final String LOGIN_SUCCESS_MESSAGE = "You are successfully logged in.";
    public static final String LOGOUT_SUCCESS_MESSAGE = "You have been logged out successfully.";
    public static final String ENTITY_NOT_FOUND = "You have been logged out successfully.";
    public static final String CREDIT_CARD_ACCOUNT_TYPE = "Credit Card";
    public static final String DEBIT_CARD_ACCOUNT_TYPE = "Debit Card";
    public static final String BANK_ACCOUNT_TYPE = "Bank Account";
    public static final String LOAN_ACCOUNT_TYPE = "Loan";
    public static final String CASH_ACCOUNT_TYPE = "Cash";
    public static final String DEVICE_WEB = "web";
    public static final String DEVICE_MOBILE = "mobile";
    public static final String RESPONSE_ERROR_MESSAGE = "An error occurred while processing your request.";
    public static final int UNAUTHORIZED_REQUEST = 401;
    public static final String UNAUTHORIZED_REQUEST_MESSAGE = "Authentication failed. Please try again.";
    public static final int REQUEST_SUCCESS = 200;
    public static final int INTERNAL_SERVER_ERROR = 500;
    public static final String INTERNAL_SERVER_ERROR_MESSAGE = "Internal server error";
    public static final int BAD_REQUEST = 400;
    public static final String BAD_REQUEST_MESSAGE = "The request could not be understood or was missing required parameters.";
    public static final int PAGE_NOT_FOUND = 404;
    public static final int ENTITY_CREATED =201;
    public static final String DEFAULT_LANGUAGE_CODE = "en";
    public static final String DEFAULT_CURRENCY_CODE = "INR";
    public static final Long CASH_ACCOUNT_GROUP_ID = 1L;
    public static final String ADMIN_EMAIL = System.getenv("ADMIN_EMAIL");
}
