package com.example.boardpjt.model.repository;

import com.example.boardpjt.model.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}
