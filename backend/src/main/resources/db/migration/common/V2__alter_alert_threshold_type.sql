alter table public.products
    alter column alert_threshold type integer using alert_threshold::integer;