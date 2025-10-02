package com.megamaker.study.cache;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.http.MediaType;

@ToString
@Getter
@Builder
@RequiredArgsConstructor
public class Image {
    private final byte[] bytes;
    private final MediaType mediaType;
    private final long size;
}
