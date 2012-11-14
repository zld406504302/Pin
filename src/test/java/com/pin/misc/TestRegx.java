package com.pin.misc;

import junit.framework.Assert;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: lizhongyuan
 * Date: 12-11-14
 * Time: 上午11:24
 */
public class TestRegx {

    String testStr1 = "aaabbb";
    String testStr2 = "aaa[bbb";
    String testStr3 = "aaa]bbb";
    String testStr4 = "aaa[bbb]aaa";
    String testStr5 = "aaa[bbb]aaa";
    String testStr6 = "aaa[bbb]aaa";
    String testStr7 = "[bbb]aaa";
    String testStr8 = "aaa[bbb]";

    @Test
    public void TestRegx() {

        String hashTag = getHashTag(testStr1);
        Assert.assertEquals(testStr1, hashTag);

        hashTag = getHashTag(testStr2);
        Assert.assertEquals(testStr2, hashTag);

        hashTag = getHashTag(testStr3);
        Assert.assertEquals(testStr3, hashTag);

        hashTag = getHashTag(testStr4);
        Assert.assertEquals("bbb", hashTag);

        hashTag = getHashTag(testStr5);
        Assert.assertEquals("bbb", hashTag);

        hashTag = getHashTag(testStr6);
        Assert.assertEquals("bbb", hashTag);

        hashTag = getHashTag(testStr7);
        Assert.assertEquals("bbb", hashTag);

        hashTag = getHashTag(testStr8);
        Assert.assertEquals("bbb", hashTag);
    }

    private String getHashTag(String key) {
        String regx = "\\[(.*?)\\]";

        Pattern p = Pattern.compile(regx);
        Matcher m = p.matcher(key);
        if (m.find()) {
            return m.group(1);
        } else {
            return key;
        }
    }
}
