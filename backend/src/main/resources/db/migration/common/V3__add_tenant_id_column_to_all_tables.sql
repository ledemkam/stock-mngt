--- categories table
alter table public.categories
    add column  tenant_id varchar(255) not null ;

comment on column public.categories.tenant_id is 'Tenant ID';

--- products table
alter table public.products
    add column  tenant_id varchar(255) not null ;

comment on column public.products.tenant_id is 'Tenant ID';

--- stock_mvts table
alter table public.stock_mvts
    add column tenant_id varchar(255) not null ;

comment on column public.stock_mvts.tenant_id is 'Tenant ID';