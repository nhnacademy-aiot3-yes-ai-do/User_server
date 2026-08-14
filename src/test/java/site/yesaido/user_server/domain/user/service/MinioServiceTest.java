package site.yesaido.user_server.domain.user.service;

import io.minio.MinioClient;
import io.minio.errors.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioServiceTest {
    @Mock
    private MinioClient minioClient;

    @InjectMocks
    private MinioService minioService;

    @BeforeEach
    void setUp(){
        ReflectionTestUtils.setField(minioService, "bucketName", "testbucket");
    }

    @Test
    void profile_image_upload_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "mushroom.jpg",
                "image/jpeg",
                "image data".getBytes()
        );

        when(minioClient.bucketExists(any())).thenReturn(true);

        String objectKey = minioService.uploadProfileImage(1L, file);

        assertThat(objectKey)
                .startsWith("profiles/1/")
                .endsWith(".jpg");

        verify(minioClient).bucketExists(any());
        verify(minioClient).putObject(any());
        verify(minioClient, never()).makeBucket(any());
    }

    @Test
    @DisplayName("버킷이 없으면 생성한 후 이미지를 업로드한다")
    void uploadProfileImage_createsBucketWhenMissing() throws Exception{
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "mushroom.jpg",
                "image/jpeg",
                "image data".getBytes()
        );
        when(minioClient.bucketExists(any())).thenReturn(false);

        minioService.uploadProfileImage(1L, file);

        verify(minioClient).makeBucket(any());
        verify(minioClient).putObject(any());
    }

    @Test
    @DisplayName("허용하지 않은 이미지 타입이면 업로드에 실패한다")
    void uploadProfileImage_throwsExceptionForUnsupportedContentType(){
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "file data".getBytes()
        );
        assertThatThrownBy(() -> minioService.uploadProfileImage(1L, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("JPG, PNG, WEBP 이미지만 업로드할 수 있습니다.");

        verifyNoInteractions(minioClient);
    }


    @Test
    @DisplayName("빈 파일이면 업로드에 실패한다")
    void uploadProfileImage_throwsExceptionForEmptyFile(){
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        assertThatThrownBy(() -> minioService.uploadProfileImage(1L, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("업로드할 파일이 존재하지 않습니다.");

        verifyNoInteractions(minioClient);
    }

    @Test
    @DisplayName("파일을 삭제한다")
    void deleteFile_success() throws Exception{
        minioService.deleteFile("profiles/1/test.jpg");

        verify(minioClient).removeObject(any());
    }

    @Test
    @DisplayName("빈 objectKey는 삭제하지 않는다")
    void deleteFile_doesNothingForBlankObjectKey() throws Exception{
        minioService.deleteFile(" ");
        verifyNoInteractions(minioClient);
    }


}
