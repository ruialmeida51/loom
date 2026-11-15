do $$ begin
    assert (select position > 0 from public.shopping_items where id = '00000000-0000-4000-8000-000000000081');
    assert (select version = 1 from public.shopping_items where id = '00000000-0000-4000-8000-000000000081'),
        'The backfill must preserve stored versions, including in archived lists';
    assert (select archived from public.shopping_lists where id = '00000000-0000-4000-8000-000000000080');
end; $$;
