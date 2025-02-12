package io.github.simplydemo.utils;

import java.io.IOException;
import java.util.Properties;

public class PropertiesLoader {


    public static Properties load() {
        Properties properties = new Properties();
        properties.putAll(System.getProperties());

        // 환경 변수 PROFILE 값 가져오기
        String profile = System.getenv().getOrDefault("PROFILE", "default");

        // 파일 경로 구성
        String propertiesFileName = "application" + ("default".equals(profile) ? "" : "-" + profile) + ".properties";

        try (var inputStream = PropertiesLoader.class.getClassLoader().getResourceAsStream(propertiesFileName)) {
            if (inputStream != null) {
                properties.load(inputStream);
                System.out.println(propertiesFileName + " 파일이 로드되었습니다.");
            } else {
                System.err.println("파일이 존재하지 않습니다: " + propertiesFileName);
            }
        } catch (IOException e) {
            System.err.println("파일을 읽는 도중 오류가 발생했습니다: " + propertiesFileName);
            e.printStackTrace();
        }

        return properties;
    }

}
