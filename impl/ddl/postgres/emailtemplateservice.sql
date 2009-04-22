
    create table EMAIL_TEMPLATE_ITEM (
        ID int8 not null,
        LAST_MODIFIED timestamp not null,
        OWNER varchar(255) not null,
        SUBJECT text not null,
        MESSAGE text not null,
        TEMPLATE_KEY varchar(255) not null,
        TEMPLATE_LOCALE varchar(255),
        defaultType varchar(255),
        primary key (ID)
    );

    create index email_templ_owner on EMAIL_TEMPLATE_ITEM (OWNER);

    create index email_templ_key on EMAIL_TEMPLATE_ITEM (TEMPLATE_KEY);

    create sequence emailtemplate_item_seq;
    -- create sequence hibernate_sequence;
