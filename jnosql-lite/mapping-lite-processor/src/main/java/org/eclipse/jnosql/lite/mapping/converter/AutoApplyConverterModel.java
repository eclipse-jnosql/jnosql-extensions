package org.eclipse.jnosql.lite.mapping.converter;

import java.time.LocalDateTime;
import java.time.Year;

public class AutoApplyConverterModel {

    public LocalDateTime getNow() {
        return LocalDateTime.now();
    }

    public String getCurrentYear(){
        return Year.now().toString();
    }
}
