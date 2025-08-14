package com.example.applib.tenant;

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

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "dialect", nullable = false)
    private String dialect;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "flexdb")
    private String flexDb;

    @Column(name = "readdb")
    private String readDb;

    @Column(name = "appstoredb")
    private String appStoreDb;

    @Column(name = "procedures_filename", columnDefinition = "varchar(255) DEFAULT 'procedures.sql'")
    private String proceduresFilename;

    @Column(name = "db_properties", columnDefinition = "text DEFAULT '{\"minIdle\": 1,\"maxPoolSize\":3,\"connectionTimeout\":1,\"idleTimeout\":1}'")
    private String dbProperties;

    @Column(name = "isactive", columnDefinition = "boolean DEFAULT true")
    private Boolean isActive;

    @Column(name = "minidle")
    private Integer minIdle;

    @Column(name = "maxpoolsize")
    private Integer maxPoolSize;

    @Column(name = "connectiontimeout")
    private Long connectionTimeout;

    @Column(name = "idletimeout")
    private Long idleTimeout;
}

