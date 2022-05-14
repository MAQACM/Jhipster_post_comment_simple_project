package com.qacm.test.service.impl;

import com.qacm.test.domain.Post;
import com.qacm.test.repository.PostRepository;
import com.qacm.test.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link Post}.
 */
@Service
public class PostServiceImpl implements PostService {

    private final Logger log = LoggerFactory.getLogger(PostServiceImpl.class);

    private final PostRepository postRepository;

    public PostServiceImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public Mono<Post> save(Post post) {
        log.debug("Request to save Post : {}", post);
        return postRepository.save(post);
    }

    @Override
    public Mono<Post> update(Post post) {
        log.debug("Request to save Post : {}", post);
        return postRepository.save(post);
    }

    @Override
    public Mono<Post> partialUpdate(Post post) {
        log.debug("Request to partially update Post : {}", post);

        return postRepository
            .findById(post.getId())
            .map(existingPost -> {
                if (post.getTitle() != null) {
                    existingPost.setTitle(post.getTitle());
                }
                if (post.getRequired() != null) {
                    existingPost.setRequired(post.getRequired());
                }
                if (post.getCreationDate() != null) {
                    existingPost.setCreationDate(post.getCreationDate());
                }

                return existingPost;
            })
            .flatMap(postRepository::save);
    }

    @Override
    public Flux<Post> findAll(Pageable pageable) {
        log.debug("Request to get all Posts");
        return postRepository.findAllBy(pageable);
    }

    public Flux<Post> findAllWithEagerRelationships(Pageable pageable) {
        return postRepository.findAllWithEagerRelationships(pageable);
    }

    public Mono<Long> countAll() {
        return postRepository.count();
    }

    @Override
    public Mono<Post> findOne(String id) {
        log.debug("Request to get Post : {}", id);
        return postRepository.findOneWithEagerRelationships(id);
    }

    @Override
    public Mono<Void> delete(String id) {
        log.debug("Request to delete Post : {}", id);
        return postRepository.deleteById(id);
    }
}
