package com.example.applib.tenant;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "master_tenant")
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
    private int version;
    
    @Column(name = "flexdb")
    private String flexdb;
    
    @Column(name = "procedures_filename")
    private String proceduresFilename;
    
    @Column(name = "readdb")
    private String readdb;
    
    @Column(name = "appstoredb")
    private String appstoredb;
    
    @Column(name = "db_properties")
    private String dbProperties;
    
    @Column(name = "isactive")
    private Boolean isActive = true;
    
    @Column(name = "connectiontimeout")
    private Long connectionTimeout;
    
    @Column(name = "idletimeout")
    private Long idleTimeout;
    
    @Column(name = "maxpoolsize")
    private Integer maxPoolSize;
    
    @Column(name = "minidle")
    private Integer minIdle;
}
