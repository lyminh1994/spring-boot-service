package io.spring;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtil {

    public boolean isEmpty(String value) {
        return value != null && !value.isEmpty();
    }

    public boolean isNotEmpty(String value) {
        return value == null || value.isEmpty();
    }

}
