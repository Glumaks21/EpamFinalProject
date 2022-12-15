package ua.maksym.hlushchenko.util;

import ua.maksym.hlushchenko.exception.ParamsValidationException;
import jakarta.servlet.http.HttpServletRequest;

public class ParamsValidator {
    public static String getOptionalParam(HttpServletRequest request, String paramName) throws ParamsValidationException {
        return getField(request, paramName, false);
    }

    public static String getRequiredParam(HttpServletRequest request, String paramName) throws ParamsValidationException {
        return getField(request, paramName, true);
    }

    private static String getField(HttpServletRequest request, String paramName, boolean required)
            throws ParamsValidationException {
        String paramValue = request.getParameter(paramName);
        if (paramValue == null || paramValue.trim().isEmpty()) {
            if (required) {
                throw new ParamsValidationException("Param is required");
            }

            paramValue = null;
        }
        return paramValue;
    }
}
