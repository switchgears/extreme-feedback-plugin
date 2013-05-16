package org.jenkinsci.plugins.extremefeedback.model;

import com.google.common.collect.ImmutableMap;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Map;

public class LampFinderCallableTest {

    @Test
    public void extractMacAddressTest() {
        Map<String, String> macs =ImmutableMap.of(
                "MAC=11:11:11:11:11:11", "11:11:11:11:11:11",
                "MAC=aa:aa:aa:aa:aa:aa", "aa:aa:aa:aa:aa:aa",
                "MAC=11:11:11:11:11:11    ,;+   \n", "11:11:11:11:11:11",
                "MAC=11:11:11:11:11:11    ,=;+   \n", "11:11:11:11:11:11"
        );

        for (String original : macs.keySet()) {
            String actual = LampFinderCallable.extractMacAddress(original);
            String expected = macs.get(original);
            Assert.assertEquals(expected, actual);
        }
    }
}
