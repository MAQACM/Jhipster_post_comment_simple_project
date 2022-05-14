package com.qacm.test.service;

import com.qacm.test.domain.Comment;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link Comment}.
 */
public interface CommentService {
    /**
     * Save a comment.
     *
     * @param comment the entity to save.
     * @return the persisted entity.
     */
    Mono<Comment> save(Comment comment);

    /**
     * Updates a comment.
     *
     * @param comment the entity to update.
     * @return the persisted entity.
     */
    Mono<Comment> update(Comment comment);

    /**
     * Partially updates a comment.
     *
     * @param comment the entity to update partially.
     * @return the persisted entity.
     */
    Mono<Comment> partialUpdate(Comment comment);

    /**
     * Get all the comments.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<Comment> findAll(Pageable pageable);

    /**
     * Get all the comments with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<Comment> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Returns the number of comments available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Get the "id" comment.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<Comment> findOne(String id);

    /**
     * Delete the "id" comment.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(String id);
}
