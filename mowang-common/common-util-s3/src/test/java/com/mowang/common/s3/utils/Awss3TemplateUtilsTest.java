package com.mowang.common.s3.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = Application.class)
public class Awss3TemplateUtilsTest {


    @Value("${app.awss3.bucketName}")
    private String bucketName;

    @Value("${app.awss3.accessKey}")
    private String accessKey;

    @Value("${app.awss3.secretKey}")
    private String secretKey;

    @Value("${app.awss3.region}")
    private String region;

    private static final String FILE_PATH = "src/test/resources/test.jpg";


    @Autowired
    private Awss3TemplateUtils awss3TemplateUtils;


    @BeforeEach
    public void setUp() {

    }

    @Test
    public void testCheckBucketExists() {
        assertTrue(Awss3TemplateUtils.checkBucketExists());
    }

    @Test
    public void testPutObject() {
        File mockFile = new File(FILE_PATH);
        String result = Awss3TemplateUtils.putObject("test.jpg", mockFile);
        assertNotNull(result);
    }

    @Test
    public void testDeleteObject() {
        assertTrue(Awss3TemplateUtils.deleteObject("test.jpg"));
    }

    @Test
    public void testGetUrl() throws Exception {
        String result = Awss3TemplateUtils.getUrl("test.jpg");
        assertNotNull(result);
        assertTrue(result.contains("https://example.cloudfront.net/testFile.txt"));
    }
}
