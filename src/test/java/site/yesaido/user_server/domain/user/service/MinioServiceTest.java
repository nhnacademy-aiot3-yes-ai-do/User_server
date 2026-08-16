package site.yesaido.user_server.domain.user.service;

import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import site.yesaido.user_server.domain.inquiry.exception.InvalidFileException;

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
    @DisplayName("프로필 이미지 업로드 성공")
    void profile_image_upload_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "mushroom.jpg",
                "image/jpeg",
                "image data".getBytes()
        );

        String objectKey = minioService.uploadProfileImage(1L, file);

        assertThat(objectKey)
                .startsWith("profiles/1/")
                .endsWith(".jpg");

        verify(minioClient).putObject(any());
    }

    @Test
    @DisplayName("서버 시작 시 버킷이 없으면 새로 생성한다")
    void ensureBucketExists_createsBucketWhenMissing() throws Exception {
        when(minioClient.bucketExists(any())).thenReturn(false);

        minioService.ensureBucketExists();

        verify(minioClient).makeBucket(any());
    }

    @Test
    @DisplayName("서버 시작 시 버킷이 이미 있으면 생성하지 않는다")
    void ensureBucketExists_doesNotCreateWhenBucketExists() throws Exception {
        when(minioClient.bucketExists(any())).thenReturn(true);

        minioService.ensureBucketExists();

        verify(minioClient, never()).makeBucket(any());
    }

    @Test
    @DisplayName("허용하지 않은 이미지 타입이면 InvalidFileException 예외가 발생한다")
    void uploadProfileImage_throwsExceptionForUnsupportedContentType(){
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "file data".getBytes()
        );
        assertThatThrownBy(() -> minioService.uploadProfileImage(1L, file))
                .isInstanceOf(InvalidFileException.class)
                .hasMessage("JPG, PNG, WEBP 이미지만 업로드할 수 있습니다.");

        verifyNoInteractions(minioClient);
    }

    @Test
    @DisplayName("빈 파일이면 InvalidFileException 예외가 발생한다")
    void uploadProfileImage_throwsExceptionForEmptyFile(){
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        assertThatThrownBy(() -> minioService.uploadProfileImage(1L, file))
                .isInstanceOf(InvalidFileException.class)
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
