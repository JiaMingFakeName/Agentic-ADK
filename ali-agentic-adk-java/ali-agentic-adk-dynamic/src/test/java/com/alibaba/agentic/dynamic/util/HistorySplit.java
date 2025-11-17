package com.alibaba.agentic.dynamic.util;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HistorySplit {
    @Test
    public void test(){
        String text1 = "<|im_start|>assistant\\n{abb}<|im_end|>   {history}   <|im_start|>\\n{bbs}<|im_end|>";
        String text2 = "<|im_start|>assistant\\n{abb}<|im_end|>{bhistory}<|im_start|>\\n{bbs}<|im_end|>";

// 使用正则提取，保留 <|im_end|> 和 <|im_start|>


        String[] parts1 =  RawTemplateParser.extractParts(text1);


        System.out.println("part1: " + parts1[0]); // <|im_start|>assistant\n{abb}<|im_end|>
        System.out.println("part2: " + parts1[1]); // <|im_start|>\n{bbs}<|im_end|>
        System.out.println("variable: " + parts1[2]);
    }
}
