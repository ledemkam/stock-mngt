package com.kte.backend.services;

import com.kte.backend.dto.requests.LoginRequest;
import com.kte.backend.dto.responses.LoginReponse;

public interface AuthenticationService {

    LoginReponse login(final LoginRequest request);
}
