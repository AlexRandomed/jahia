
    drop table jahia_contenthistory;

    drop table jahia_db_test;

    drop table jahia_external_mapping;

    create table jahia_contenthistory (
        id varchar(32) not null,
        entry_action varchar(255),
        entry_date int8,
        message varchar(255),
        entry_path text,
        property_name varchar(50),
        user_key varchar(255),
        uuid varchar(36),
        primary key (id)
    );

    create table jahia_db_test (
        testfield varchar(255) not null,
        primary key (testfield)
    );

    create table jahia_external_mapping (
        internalUuid varchar(36) not null,
        externalId text not null,
        providerKey varchar(255) not null,
        primary key (internalUuid)
    );
