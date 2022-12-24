package ua.maksym.hlushchenko.util;

import ua.maksym.hlushchenko.exception.ParamsValidationException;

public class LoginPasswordValidator {
    public static void validLogin(String login) {
        if (!login.matches("\\w[\\w1-9]{5,9}")) {
            throw new ParamsValidationException("Login must consist of letters and digits and has length 5 - 15");
        }
    }

    public static void validPassword(String password) {
        if (!password.matches("[\\w]{6,20}")) {
            throw new ParamsValidationException("Password must consist of ** and has length 6 - 20");
        }
    }
}
