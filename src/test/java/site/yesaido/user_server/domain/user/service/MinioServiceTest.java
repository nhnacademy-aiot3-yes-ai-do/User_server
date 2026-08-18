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
    void deleteFile_doesNothingForBlankObjectKey() {
        minioService.deleteFile(" ");
        verifyNoInteractions(minioClient);
    }

    @Test
    @DisplayName("실패 : 파일이 null이면 NullPointerException 예외가 발생한다")
    void uploadProfileImage_throwsExceptionForNullFile() {
        assertThatThrownBy(() -> minioService.uploadProfileImage(1L, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("실패 : 파일명이 공백이거나 없으면 InvalidFileException 예외가 발생한다")
    void uploadProfileImage_throwsExceptionForBlankFilename() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "   ",
                "image/jpeg",
                "data".getBytes()
        );
        assertThatThrownBy(() -> minioService.uploadProfileImage(1L, file))
                .isInstanceOf(InvalidFileException.class)
                .hasMessage("업로드할 파일이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("실패 : ContentType이 null이면 InvalidFileException 예외가 발생한다")
    void uploadProfileImage_throwsExceptionForNullContentType() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                null,
                "data".getBytes()
        );
        assertThatThrownBy(() -> minioService.uploadProfileImage(1L, file))
                .isInstanceOf(InvalidFileException.class)
                .hasMessage("JPG, PNG, WEBP 이미지만 업로드할 수 있습니다.");
    }

    @Test
    @DisplayName("실패 : 5MB를 초과하는 대용량 파일이면 InvalidFileException 예외가 발생한다")
    void uploadProfileImage_throwsExceptionForExceedingFileSize() {
        byte[] largeBytes = new byte[5 * 1024 * 1024 + 1]; // 5MB + 1바이트
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.jpg",
                "image/jpeg",
                largeBytes
        );
        assertThatThrownBy(() -> minioService.uploadProfileImage(1L, file))
                .isInstanceOf(InvalidFileException.class)
                .hasMessage("프로필 사진은 5MB 이하만 업로드할 수 있습니다.");
    }


    // 확장자 및 문의 사진 업로드 테스트
    @Test
    @DisplayName("성공 : 확장자가 없는 파일명이어도 기본 .jpg 확장자가 부여된다")
    void uploadProfileImage_withoutExtension_defaultsToJpg() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "mushroom_no_ext",
                "image/jpeg",
                "data".getBytes()
        );
        String objectKey = minioService.uploadProfileImage(1L, file);
        assertThat(objectKey).endsWith(".jpg");
    }

    @Test
    @DisplayName("성공 : webp 확장자 파일 업로드 시 .webp 확장자가 유지된다")
    void uploadProfileImage_webp_success() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.webp",
                "image/webp",
                "data".getBytes()
        );
        String objectKey = minioService.uploadProfileImage(1L, file);
        assertThat(objectKey).endsWith(".webp");
    }

    @Test
    @DisplayName("성공 : 문의 사진 업로드 시 inquiries/{answerId}/ 경로로 생성된다")
    void uploadInquiryPhoto_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "inquiry.png",
                "image/png",
                "data".getBytes()
        );
        String objectKey = minioService.uploadInquiryPhoto(100L, file);
        assertThat(objectKey)
                .startsWith("inquiries/100/")
                .endsWith(".png");
        verify(minioClient).putObject(any());
    }

    // 삭제 & MinIO 장애 예외
    @Test
    @DisplayName("파일 업로드 도중 MinIO 예외 발생 시 FileUploadException이 발생한다")
    void uploadProfileImage_minioError_throwsFileUploadException() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "data".getBytes()
        );
        doThrow(new RuntimeException("MinIO 서버 다운")).when(minioClient).putObject(any());

        assertThatThrownBy(() -> minioService.uploadProfileImage(1L, file))
                .isInstanceOf(site.yesaido.user_server.domain.inquiry.exception.FileUploadException.class)
                .hasMessage("사진 업로드에 실패했습니다.");
    }

    @Test
    @DisplayName("null인 objectKey로 파일 삭제 시 아무 작업도 하지 않는다")
    void deleteFile_nullKey_doesNothing() {
        minioService.deleteFile(null);
        verifyNoInteractions(minioClient);
    }

    @Test
    @DisplayName("파일 삭제 실패 시 FileDeleteException 예외가 발생한다")
    void deleteFile_minioError_throwsFileDeleteException() throws Exception {
        doThrow(new RuntimeException("삭제 에러")).when(minioClient).removeObject(any());

        assertThatThrownBy(() -> minioService.deleteFile("profiles/1/test.jpg"))
                .isInstanceOf(site.yesaido.user_server.domain.inquiry.exception.FileDeleteException.class)
                .hasMessage("프로필 사진 삭제에 실패했습니다.");
    }

    @Test
    @DisplayName("deleteQuietly는 파일 삭제 시 예외가 발생해도 예외를 던지지 않고 안전하게 넘어간다")
    void deleteQuietly_swallowsException() throws Exception {
        doThrow(new RuntimeException("네트워크 에러")).when(minioClient).removeObject(any());

        // 소나큐브에게 "예외가 절대 발생하지 않아야 함"을 명시적으로 단언!
        org.assertj.core.api.Assertions.assertThatCode(() -> {
            minioService.deleteQuietly("profiles/1/test.jpg");
            minioService.deleteQuietly(null);
            minioService.deleteQuietly("  ");
        }).doesNotThrowAnyException();
    }


    @Test
    @DisplayName("서버 시작 시 MinIO 통신 장애 발생 시 FileStorageException이 발생한다")
    void ensureBucketExists_minioError_throwsFileStorageException() throws Exception {
        when(minioClient.bucketExists(any())).thenThrow(new RuntimeException("MinIO 연결 실패"));

        assertThatThrownBy(() -> minioService.ensureBucketExists())
                .isInstanceOf(site.yesaido.user_server.domain.inquiry.exception.FileStorageException.class)
                .hasMessage("이미지 저장소를 사용할 수 없습니다.");
    }


}
