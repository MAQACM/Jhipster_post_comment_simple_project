package com.qacm.test.service.impl;

import com.qacm.test.domain.Comment;
import com.qacm.test.repository.CommentRepository;
import com.qacm.test.service.CommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link Comment}.
 */
@Service
public class CommentServiceImpl implements CommentService {

    private final Logger log = LoggerFactory.getLogger(CommentServiceImpl.class);

    private final CommentRepository commentRepository;

    public CommentServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public Mono<Comment> save(Comment comment) {
        log.debug("Request to save Comment : {}", comment);
        return commentRepository.save(comment);
    }

    @Override
    public Mono<Comment> update(Comment comment) {
        log.debug("Request to save Comment : {}", comment);
        return commentRepository.save(comment);
    }

    @Override
    public Mono<Comment> partialUpdate(Comment comment) {
        log.debug("Request to partially update Comment : {}", comment);

        return commentRepository
            .findById(comment.getId())
            .map(existingComment -> {
                if (comment.getText() != null) {
                    existingComment.setText(comment.getText());
                }
                if (comment.getCreaionDate() != null) {
                    existingComment.setCreaionDate(comment.getCreaionDate());
                }

                return existingComment;
            })
            .flatMap(commentRepository::save);
    }

    @Override
    public Flux<Comment> findAll(Pageable pageable) {
        log.debug("Request to get all Comments");
        return commentRepository.findAllBy(pageable);
    }

    public Flux<Comment> findAllWithEagerRelationships(Pageable pageable) {
        return commentRepository.findAllWithEagerRelationships(pageable);
    }

    public Mono<Long> countAll() {
        return commentRepository.count();
    }

    @Override
    public Mono<Comment> findOne(String id) {
        log.debug("Request to get Comment : {}", id);
        return commentRepository.findOneWithEagerRelationships(id);
    }

    @Override
    public Mono<Void> delete(String id) {
        log.debug("Request to delete Comment : {}", id);
        return commentRepository.deleteById(id);
    }
}
