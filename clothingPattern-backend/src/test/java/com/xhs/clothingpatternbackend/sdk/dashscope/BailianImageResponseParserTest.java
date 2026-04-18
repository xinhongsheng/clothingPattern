package com.xhs.clothingpatternbackend.sdk.dashscope;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BailianImageResponseParserTest {

    @Test
    void extractImageUrlFromStringImageUrl() {
        String json = """
                {
                  "request_id": "abc",
                  "output": {
                    "choices": [
                      {
                        "message": {
                          "content": [
                            {"image_url": "https://example.com/generated.png"}
                          ]
                        }
                      }
                    ]
                  }
                }
                """;

        assertEquals("https://example.com/generated.png", BailianImageResponseParser.extractImageUrl(json));
        assertEquals("abc", BailianImageResponseParser.extractTraceId(json));
    }

    @Test
    void extractImageUrlFromNestedImageUrl() {
        String json = """
                {
                  "requestId": "nested-id",
                  "output": {
                    "choices": [
                      {
                        "message": {
                          "content": [
                            {"image_url": {"url": "https://example.com/nested.webp"}}
                          ]
                        }
                      }
                    ]
                  }
                }
                """;

        assertEquals("https://example.com/nested.webp", BailianImageResponseParser.extractImageUrl(json));
        assertEquals("nested-id", BailianImageResponseParser.extractTraceId(json));
    }

    @Test
    void extractImageUrlFromImageField() {
        String json = """
                {
                  "output": {
                    "choices": [
                      {
                        "message": {
                          "content": [
                            {"image": "https://example.com/image-field.png"}
                          ]
                        }
                      }
                    ]
                  }
                }
                """;

        assertEquals("https://example.com/image-field.png", BailianImageResponseParser.extractImageUrl(json));
    }
}
