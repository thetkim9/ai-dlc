package com.tableorder.menu;

import com.tableorder.common.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService(tempDir.toString());
    }

    // TC-BE-023: 유효한 파일 저장
    @Test
    @DisplayName("TC-BE-023: 유효한 jpg 파일 저장 성공")
    void saveFile_validJpg_returnsUrl() {
        // given
        Long storeId = 1L;
        MockMultipartFile file = new MockMultipartFile(
            "image", "menu.jpg", "image/jpeg", "fake-image-content".getBytes()
        );

        // when
        String url = fileStorageService.saveFile(storeId, file);

        // then
        assertThat(url).startsWith("/uploads/1/");
        assertThat(url).endsWith(".jpg");
    }

    @Test
    @DisplayName("TC-BE-023: 유효한 png 파일 저장 성공")
    void saveFile_validPng_returnsUrl() {
        MockMultipartFile file = new MockMultipartFile(
            "image", "menu.png", "image/png", "fake-image-content".getBytes()
        );
        String url = fileStorageService.saveFile(1L, file);
        assertThat(url).endsWith(".png");
    }

    // TC-BE-024: 허용되지 않는 확장자
    @Test
    @DisplayName("TC-BE-024: 허용되지 않는 확장자 - 400")
    void saveFile_invalidExtension_throws400() {
        // given
        MockMultipartFile file = new MockMultipartFile(
            "image", "malicious.exe", "application/octet-stream", "content".getBytes()
        );

        // when & then
        assertThatThrownBy(() -> fileStorageService.saveFile(1L, file))
            .isInstanceOf(ApiException.class)
            .satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("TC-BE-024: gif 확장자 - 400")
    void saveFile_gifExtension_throws400() {
        MockMultipartFile file = new MockMultipartFile(
            "image", "anim.gif", "image/gif", "content".getBytes()
        );
        assertThatThrownBy(() -> fileStorageService.saveFile(1L, file))
            .isInstanceOf(ApiException.class);
    }

    // TC-BE-025: 크기 초과
    @Test
    @DisplayName("TC-BE-025: 5MB 초과 파일 - 400")
    void saveFile_oversized_throws400() {
        // given - 6MB 파일
        byte[] largeContent = new byte[6 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
            "image", "large.jpg", "image/jpeg", largeContent
        );

        // when & then
        assertThatThrownBy(() -> fileStorageService.saveFile(1L, file))
            .isInstanceOf(ApiException.class)
            .satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }
}
