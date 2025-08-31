package com.example.applib.tenant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a master tenant.
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "master_tenant")
public class MasterTenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, unique = true)
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
    private Boolean isactive = true;

    @Column(name = "connectiontimeout")
    private Long connectionTimeout;

    @Column(name = "idletimeout")
    private Long idleTimeout;

    @Column(name = "maxpoolsize")
    private Integer maxPoolSize;

    @Column(name = "minidle")
    private Integer minIdle;

    /**
     * Checks if the tenant is active.
     *
     * @return True if the tenant is active, false otherwise
     */
    public boolean isActive() {
        return isactive != null && isactive;
    }
}
