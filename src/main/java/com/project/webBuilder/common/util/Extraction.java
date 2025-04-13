package com.project.webBuilder.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Extraction {

    // html 추출 정규식
    public static String extractValidHtml(String gptResponse) {
        Pattern pattern = Pattern.compile("<!DOCTYPE html>[\\s\\S]*?<html[^>]*>[\\s\\S]*?</html>", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(gptResponse);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }

    //정규식 template:1 의 형식의 String에서 Long타입의 1을 반환
    public static Long extractTemplateId(String analysis) {
        if (analysis == null || analysis.isEmpty()) {
            throw new IllegalArgumentException("Analysis is null or empty");
        }

        Pattern pattern = Pattern.compile("template:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(analysis.trim());

        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        } else {
            throw new IllegalArgumentException("No suitable template found in analysis string.");
        }
    }
}
