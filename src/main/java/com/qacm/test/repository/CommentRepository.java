package com.qacm.test.repository;

import com.qacm.test.domain.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data MongoDB reactive repository for the Comment entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CommentRepository extends ReactiveMongoRepository<Comment, String> {
    Flux<Comment> findAllBy(Pageable pageable);

    @Query("{}")
    Flux<Comment> findAllWithEagerRelationships(Pageable pageable);

    @Query("{}")
    Flux<Comment> findAllWithEagerRelationships();

    @Query("{'id': ?0}")
    Mono<Comment> findOneWithEagerRelationships(String id);
}
