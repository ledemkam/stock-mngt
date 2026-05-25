create table tenants
(
    deleted         boolean      not null,
    created_at      timestamp(6) not null,
    updated_at      timestamp(6),
    admin_email     varchar(255) not null
        unique,
    admin_full_name varchar(255) not null,
    admin_password  varchar(255) not null,
    admin_user_name varchar(255) not null
        unique,
    compagny_code   varchar(255) not null
        unique,
    compagny_name   varchar(255) not null,
    email           varchar(255) not null
        unique,
    id              varchar(255) not null
        primary key,
    status          varchar(255) not null
        constraint tenants_status_check
            check ((status)::text = ANY
                   ((ARRAY ['PENDING'::character varying, 'ACTIVE'::character varying, 'SUSPENDED'::character varying, 'INACTIVE'::character varying])::text[])),
);



create table users
(
    deleted    boolean      not null,
    enable     boolean      not null,
    created_at timestamp(6) not null,
    updated_at timestamp(6),
    created_by varchar(255) not null,
    email      varchar(255) not null
        unique,
    first_name varchar(255) not null,
    id         varchar(255) not null
        primary key,
    last_name  varchar(255) not null,
    password   varchar(255) not null,
    role       varchar(255) not null
        constraint users_role_check
            check ((role)::text = ANY
                   ((ARRAY ['ROLE_PLATFORM_ADMIN'::character varying, 'ROLE_COMPAGNY_ADMIN'::character varying, 'ROLE_USER'::character varying, 'ROLE_SALES_OPERATOR'::character varying])::text[])),
    tenant_id  varchar(255)
        constraint fk_user_tenant_id
            references tenants,
    update_by  varchar(255),
    username   varchar(255) not null
        unique
);



