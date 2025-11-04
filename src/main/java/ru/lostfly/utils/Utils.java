package ru.lostfly.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    public static void printUserNotFound() {
        logger.warn("✗ Читатель не найден");
        logger.info("✗ Читатель не найден");
        logger.error("✗ Читатель не найден");
    }
}
