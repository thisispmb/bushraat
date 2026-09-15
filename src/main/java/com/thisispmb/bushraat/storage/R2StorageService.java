package com.thisispmb.bushraat.storage;

import com.thisispmb.bushraat.util.Env;
import jakarta.servlet.http.Part;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;

/**
 * Cloudflare R2 storage through its S3-compatible API.
 */
public final class R2StorageService implements StorageService {
    private final S3Client client;
    private final String bucket;

    public R2StorageService() {
        String accountId = required("R2_ACCOUNT_ID");
        String accessKey = required("R2_ACCESS_KEY_ID");
        String secretKey = required("R2_SECRET_ACCESS_KEY");

        this.bucket = required("R2_BUCKET_NAME");

        String endpoint = Env.get(
                "R2_ENDPOINT",
                "https://" + accountId + ".r2.cloudflarestorage.com"
        );

        AwsBasicCredentials credentials =
                AwsBasicCredentials.create(accessKey, secretKey);

        S3Configuration serviceConfiguration =
                S3Configuration.builder().pathStyleAccessEnabled(true)
                        .chunkedEncodingEnabled(false)
                        .build();

        this.client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of("auto"))
                .serviceConfiguration(serviceConfiguration)
                .build();
    }

    private static String requireFilename(Part file) {
        if (file == null || file.getSize() == 0
                || file.getSubmittedFileName() == null) {

            throw new IllegalArgumentException("File is required.");
        }

        return file.getSubmittedFileName();
    }

    private static String required(String key) {
        String value = Env.get(key);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required configuration: " + key);
        }

        return value.trim();
    }

    private static void validateObjectKey(String objectKey) {
        if (objectKey == null || objectKey.isBlank() || objectKey.contains("..")
                || objectKey.contains("\\")
                || !(objectKey.startsWith("books/")
                || objectKey.startsWith("covers/"))) {

            throw new IllegalArgumentException("Invalid storage object key.");
        }
    }

    @Override
    public String storeBook(Part file) throws Exception {
        String name = requireFilename(file).toLowerCase(Locale.ROOT);

        if (!name.endsWith(".pdf")) {
            throw new StorageService.UnsupportedMediaTypeException(
                    "Only PDF books are supported."
            );
        }

        String key = "books/" + UUID.randomUUID() + ".pdf";

        upload(file, key, "application/pdf");

        return key;
    }

    @Override
    public String storeCover(Part file) throws Exception {
        String name = requireFilename(file).toLowerCase(Locale.ROOT);

        String extension;
        String contentType;

        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            extension = ".jpg";
            contentType = "image/jpeg";

        } else if (name.endsWith(".png")) {
            extension = ".png";
            contentType = "image/png";

        } else if (name.endsWith(".webp")) {
            extension = ".webp";
            contentType = "image/webp";

        } else {
            throw new StorageService.UnsupportedMediaTypeException(
                    "Cover must be JPG, PNG or WEBP."
            );
        }

        String key = "covers/" + UUID.randomUUID() + extension;

        upload(file, key, contentType);

        return key;
    }


    /**
     * The temporary file makes the request body repeatable for the
     * AWS SDK without loading the entire PDF into memory.
     */
    private void upload(Part file, String key, String contentType) throws IOException {
        Path temporaryFile = Files.createTempFile("bushraat-upload-", ".tmp");

        try {
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, temporaryFile,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );
            }

            long size = Files.size(temporaryFile);

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .contentLength(size)
                    .build();

            RequestBody requestBody = RequestBody.fromFile(temporaryFile);

            client.putObject(request, requestBody);

        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    @Override
    public StorageObject open(String objectKey) {
        validateObjectKey(objectKey);

        ResponseInputStream<GetObjectResponse> stream = client.getObject(GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build()
        );

        GetObjectResponse response = stream.response();

        return new StorageObject(stream, response.contentLength(),
                response.contentType() == null
                        ? "application/octet-stream"
                        : response.contentType()
        );
    }

    @Override
    public StorageObject openRange(String objectKey, long start, long length) {
        validateObjectKey(objectKey);

        if (start < 0 || length <= 0) {
            throw new IllegalArgumentException("Invalid byte range.");
        }

        long end = start + length - 1;

        if (end < start) {
            throw new IllegalArgumentException("Invalid byte range.");
        }

        String range = "bytes=" + start + "-" + end;

        ResponseInputStream<GetObjectResponse> stream = client.getObject(GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .range(range)
                .build()
        );

        GetObjectResponse response = stream.response();

        return new StorageObject(stream, response.contentLength(),
                response.contentType() == null
                        ? "application/octet-stream"
                        : response.contentType()
        );
    }

    @Override
    public long size(String objectKey) {
        validateObjectKey(objectKey);

        return client.headObject(HeadObjectRequest.builder().bucket(bucket)
                .key(objectKey)
                .build()
        ).contentLength();
    }

    @Override
    public void delete(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return;
        }

        validateObjectKey(objectKey);

        client.deleteObject(DeleteObjectRequest.builder().bucket(bucket)
                .key(objectKey)
                .build()
        );
    }
}