package com.example.applib.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "turbos3config")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurboS3Config {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "awsaccesskeyid")
    private String awsAccessKeyId;

    @Column(name = "awssecretaccesskey")
    private String awsSecretAccessKey;

    @Column(name = "region")
    private String region;

    @Column(name = "bucketname")
    private String bucketName;

    @Column(name = "schemabucketname")
    private String schemaBucketName;

    @Column(name = "imagebucketname")
    private String imageBucketName;

    @Column(name = "flexbucketname")
    private String flexBucketName;

    @Column(name = "companyprofilebucketname")
    private String companyProfileBucketName;

    @Column(name = "datamanagementbucketname")
    private String dataManagementBucketName;
}
