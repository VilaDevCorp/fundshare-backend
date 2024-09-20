package com.viladev.fundshare.service;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.viladev.fundshare.exceptions.FileTooBigException;
import com.viladev.fundshare.exceptions.FileTypeNotSupportedException;
import com.viladev.fundshare.utils.AuthUtils;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.MinioException;
import io.minio.http.Method;

@Service
public class MinioService {

    private final MinioClient minioClient;
    @Value("${minio.bucket}")
    private String minioBucket;

    @Autowired
    public MinioService(@Value("${minio.url}") String minioUrl,
            @Value("${minio.access-key}") String minioAccessKey,
            @Value("${minio.secret-key}") String minioSecretKey) {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(minioAccessKey, minioSecretKey)
                .build();
        this.minioClient = minioClient;
    }

    public void changeProfilePicture(MultipartFile profilePicture)
            throws FileTooBigException, FileTypeNotSupportedException,
            MinioException {
        String loggedUsername = AuthUtils.getUsername();
        try {
            boolean found;
            found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioBucket).build());

            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioBucket).build());
            }
            if (profilePicture == null) {
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(minioBucket)
                                .object(loggedUsername + "_profilepicture.png")
                                .build());
                return;
            }
            if (profilePicture.getSize() > 5000000) {
                throw new FileTooBigException("Profile picture too big");
            }
            if (!profilePicture.getContentType().equals("image/png")
                    && !profilePicture.getContentType().equals("image/jpeg")) {
                throw new FileTypeNotSupportedException("Profile picture type not supported");
            }
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioBucket)
                            .object(loggedUsername + "_profilepicture.png")
                            .stream(profilePicture.getInputStream(), profilePicture.getSize(), -1)
                            .build());
        } catch (FileTooBigException | FileTypeNotSupportedException e) {
            throw e;
        } catch (Exception e) {
            throw new MinioException("Error uploading profile picture");
        }
    }

    public String getProfilePictureUrl(String username) throws MinioException {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioBucket).build());
            if (!found) {
                return null;
            }
            try {
                minioClient.statObject(StatObjectArgs.builder()
                        .bucket(minioBucket)
                        .object(username + "_profilepicture.png")
                        .build());
            } catch (Exception e) {
                return null;
            }
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(minioBucket)
                            .object(username + "_profilepicture.png")
                            .method(Method.GET)
                            .expiry(2, TimeUnit.HOURS)
                            .build());
        } catch (Exception e) {
            throw new MinioException("Error getting profile picture");
        }
    }
}
