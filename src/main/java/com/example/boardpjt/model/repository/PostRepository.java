package com.example.boardpjt.model.repository;

import com.example.boardpjt.model.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    // 오름차순
    Page<Post> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);

    // 최신순으로
    Page<Post> findByTitleContainingOrContentContainingOrderByIdDesc(
            String title, String content, Pageable pageable);
    // Desc -> PK (Long id)
}
