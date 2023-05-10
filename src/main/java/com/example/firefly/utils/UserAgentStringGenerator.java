package com.example.firefly.utils;

import java.util.concurrent.ThreadLocalRandom;

public class UserAgentStringGenerator {

    private static final String[] OS = {
            "Windows NT 10.0", "Windows NT 6.1", "Windows NT 6.2", "Windows NT 6.3", "Windows NT 5.1",
            "Macintosh; Intel Mac OS X 10_15_7", "Macintosh; Intel Mac OS X 10_14_6", "Macintosh; Intel Mac OS X 10_13_6",
            "X11; Linux x86_64", "X11; Ubuntu; Linux x86_64"
    };

    private static final String[] BROWSER_ENGINES = {
            "AppleWebKit/537.36 (KHTML, like Gecko)",
            "Gecko/20100101"
    };

    private static final String[] CHROME_VERSIONS = {
            "Chrome/58.0.3029.110", "Chrome/59.0.3071.115", "Chrome/60.0.3112.113",
            "Chrome/61.0.3163.79", "Chrome/62.0.3202.94", "Chrome/63.0.3239.132",
            "Chrome/64.0.3282.140", "Chrome/65.0.3325.181", "Chrome/66.0.3359.139",
            "Chrome/67.0.3396.99", "Chrome/68.0.3440.106", "Chrome/69.0.3497.100",
            "Chrome/70.0.3538.102", "Chrome/71.0.3578.98", "Chrome/72.0.3626.121",
            "Chrome/73.0.3683.103", "Chrome/74.0.3729.169", "Chrome/75.0.3770.142",
            "Chrome/76.0.3809.132", "Chrome/77.0.3865.120", "Chrome/78.0.3904.108",
            "Chrome/79.0.3945.130", "Chrome/80.0.3987.163", "Chrome/81.0.4044.138",
            "Chrome/82.0.4085.6",
            "Firefox/89.0", "Firefox/88.0", "Firefox/87.0", "Firefox/86.0", "Firefox/85.0",
            "Firefox/84.0", "Firefox/83.0", "Firefox/82.0", "Firefox/81.0", "Firefox/80.0",
            "Safari/537.3", "Safari/537.36", "Safari/537.78", "Safari/537.85", "Safari/537.93",
            "Safari/537.106", "Safari/537.108", "Safari/537.110", "Safari/537.113", "Safari/537.115"
    };

    public static String generateUserAgent() {
        String os = randomElement(OS);
        String browserEngine = randomElement(BROWSER_ENGINES);
        String browser = switch (browserEngine) {
            case "AppleWebKit/537.36 (KHTML, like Gecko)" -> randomElement(CHROME_VERSIONS);
            case "Gecko/20100101" -> randomElement(CHROME_VERSIONS);
            default -> randomElement(CHROME_VERSIONS);
        };

        return String.format("Mozilla/5.0 (%s) %s %s", os, browserEngine, browser);
    }

    @SafeVarargs
    private static <T> T randomElement(T... elements) {
        int index = ThreadLocalRandom.current().nextInt(elements.length);
        return elements[index];
    }

}