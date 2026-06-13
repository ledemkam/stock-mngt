package com.kte.backend.services;


import com.kte.backend.common.PageReponse;


public interface CrudServices <I, O>{
  // I for request, O for response
    void create(final I request);

    void update(final String id, final I request);

    PageReponse<O> findAll(final int page, final int size);

    O findById(final String id);

    void delete(final String id);
}
