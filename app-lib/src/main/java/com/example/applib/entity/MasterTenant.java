package com.example.applib.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "master_tenant")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MasterTenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private String tenantId;

    @Column(name = "url")
    private String url;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "dialect")
    private String dialect;

    @Column(name = "version")
    private Integer version;

    @Column(name = "flexdb")
    private String flexDb;

    @Column(name = "procedures_filename")
    private String proceduresFilename;

    @Column(name = "readdb")
    private String readDb;

    @Column(name = "appstoredb")
    private String appstoreDb;

    @Column(name = "db_properties")
    private String dbProperties;

    @Column(name = "isactive")
    private Boolean isActive;

    @Column(name = "connectiontimeout")
    private Long connectionTimeout;

    @Column(name = "idletimeout")
    private Long idleTimeout;

    @Column(name = "maxpoolsize")
    private Integer maxPoolSize;

    @Column(name = "minidle")
    private Integer minIdle;
}
