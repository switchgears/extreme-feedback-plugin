package org.jenkinsci.plugins.extremefeedback.model;

import com.google.common.collect.Maps;
import hudson.model.Result;

import java.util.Collections;
import java.util.Map;

public class States {
    public static Map<Result, Color> resultColorMap;

    public enum Color { GREEN, YELLOW, RED }

    public enum Action { SOLID, FLASHING }
    static {
        Map<Result, States.Color> map = Maps.newHashMap();
        map.put(Result.ABORTED, States.Color.RED);
        map.put(Result.FAILURE, States.Color.RED);
        map.put(Result.NOT_BUILT, States.Color.RED);
        map.put(Result.UNSTABLE, States.Color.YELLOW);
        map.put(Result.SUCCESS, States.Color.GREEN);
        States.resultColorMap = Collections.unmodifiableMap(map);
    }
}
