package com.xhs.clothingpatternbackend.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-12-08
 * @Description:基于腾讯云离线词库+DFA算法
 * @Version: 1.0
 */
public class SensitiveWordFilterUtils {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveWordFilterUtils.class);

    // 敏感词替换符
    private static final String REPLACEMENT = "**";

    // 敏感词集合
    private static HashMap<String, Object> sensitiveWordMap;

    private static volatile SensitiveWordFilterUtils instance;

    // 单例模式
    public static SensitiveWordFilterUtils getInstance() {
        if (instance == null) {
            synchronized (SensitiveWordFilterUtils.class) {
                if (instance == null) {
                    instance = new SensitiveWordFilterUtils();
                }
            }
        }
        return instance;
    }

    private SensitiveWordFilterUtils() {
        initSensitiveWordMap();
    }

    // 初始化敏感词库
    private void initSensitiveWordMap() {
        try {
            // 读取敏感词文件
            Set<String> sensitiveWordSet = readSensitiveWordFile();

            // 将敏感词库加入到HashMap中
            addSensitiveWordToHashMap(sensitiveWordSet);

            logger.info("初始化敏感词库完成，共加载 {} 个敏感词", sensitiveWordSet.size());
        } catch (Exception e) {
            logger.error("初始化敏感词库异常", e);
            throw e;
        }
    }

    /**
     * 读取敏感词库文件
     */
    private static Set<String> readSensitiveWordFile() {
        Set<String> set = new HashSet<>();

        try {
            InputStream is = SensitiveWordFilterUtils.class.getClassLoader().getResourceAsStream("sensitive-words.txt");
            if (is != null) {
                InputStreamReader read = new InputStreamReader(is, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(read);
                String word;
                while ((word = bufferedReader.readLine()) != null) {
                    word = word.trim();
                    if (word.length() > 0) {
                        set.add(word);
                    }
                }
                bufferedReader.close();
                read.close();
                is.close();
            } else {
                // 如果文件不存在，添加一些默认敏感词
                set.add("傻瓜");
                set.add("混蛋");
                // 添加更多默认敏感词...
                logger.warn("敏感词文件不存在，使用默认敏感词");
            }
        } catch (Exception e) {
            logger.error("读取敏感词文件失败", e);
        }

        return set;
    }

    /**
     * 将敏感词库加入到HashMap中
     */
    private static void addSensitiveWordToHashMap(Set<String> sensitiveWordSet) {
        // 初始化敏感词容器，减少扩容操作
        sensitiveWordMap = new HashMap<>(sensitiveWordSet.size());

        for (String word : sensitiveWordSet) {
            Map<String, Object> currentMap = sensitiveWordMap;
            for (int i = 0; i < word.length(); i++) {
                // 转换成char型
                char keyChar = word.charAt(i);

                // 获取
                Object tempMap = currentMap.get(String.valueOf(keyChar));

                // 如果存在该key，直接赋值
                if (tempMap != null) {
                    currentMap = (Map<String, Object>) tempMap;
                } else {
                    // 不存在则，则构建一个map，同时将isEnd设置为0
                    Map<String, Object> newMap = new HashMap<>();
                    newMap.put("isEnd", "0");

                    // 将当前节点放入到map
                    currentMap.put(String.valueOf(keyChar), newMap);
                    currentMap = newMap;
                }

                // 最后一个
                if (i == word.length() - 1) {
                    currentMap.put("isEnd", "1");
                }
            }
        }
    }

    /**
     * 检查文本是否包含敏感词
     */
    public boolean containsSensitiveWord(String text) {
        if (text == null || text.length() == 0) {
            return false;
        }

        String txt = text.toLowerCase();
        for (int i = 0; i < txt.length(); i++) {
            int length = checkSensitiveWord(txt, i);
            if (length > 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * 获取文本中的敏感词
     * @param text 待检查的文本
     * @return 敏感词列表
     */
    public static List<String> getSensitiveWords(String text) {
        List<String> words = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            return words;
        }

        // 转换为小写
        String txt = text.toLowerCase();

        for (int i = 0; i < txt.length(); i++) {
            int length = checkSensitiveWord(txt, i);
            if (length > 0) {
                words.add(txt.substring(i, i + length));
                i = i + length - 1; // 跳过已经检测的敏感词
            }
        }

        return words;
    }

    /**
     * 过滤敏感词
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String txt = text.toLowerCase();
        StringBuilder result = new StringBuilder(text);

        // 记录替换的位置和长度，避免重复替换导致的索引偏移
        List<Map<String, Integer>> replacePositions = new ArrayList<>();

        // 找出所有敏感词的位置
        for (int i = 0; i < txt.length(); i++) {
            int length = checkSensitiveWord(txt, i);
            if (length > 0) {
                Map<String, Integer> position = new HashMap<>();
                position.put("start", i);
                position.put("length", length);
                replacePositions.add(position);

                i = i + length - 1; // 跳过已经检测的敏感词
            }
        }

        // 从后往前替换，避免索引变化问题
        for (int i = replacePositions.size() - 1; i >= 0; i--) {
            Map<String, Integer> position = replacePositions.get(i);
            int start = position.get("start");
            int length = position.get("length");
            result.replace(start, start + length, REPLACEMENT);
        }

        return result.toString();
    }

    /**
     * 检查文本中是否包含敏感词
     * @param text 待检查的文本
     * @param beginIndex 起始位置
     * @return 敏感词长度，0表示不是敏感词
     */
    private static int checkSensitiveWord(String text, int beginIndex) {
        // 敏感词库初始化
        if (sensitiveWordMap == null) {
            logger.warn("敏感词库未初始化");
            return 0;
        }

        // 记录敏感词的长度
        int wordLength = 0;

        // 敏感词结束标识位
        boolean flag = false;

        Map<String, Object> currentMap = sensitiveWordMap;
        for (int i = beginIndex; i < text.length(); i++) {
            char word = text.charAt(i);

            // 获取指定key
            Object nextNode = currentMap.get(String.valueOf(word));
            if (nextNode == null) {
                break;
            }

            currentMap = (Map<String, Object>) nextNode;
            wordLength++;

            if ("1".equals(currentMap.get("isEnd"))) {
                flag = true;
            }
        }

        // 长度必须大于0且确实是敏感词（isEnd=1）才算敏感词
        return flag ? wordLength : 0;
    }

    /**
     * 重新加载敏感词库
     */
    public static void reload() {
        logger.info("重新加载敏感词库");
        // 使用单例实例调用非静态方法
        getInstance().initSensitiveWordMap();
    }

}
