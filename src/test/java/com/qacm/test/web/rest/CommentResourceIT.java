package com.qacm.test.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.qacm.test.IntegrationTest;
import com.qacm.test.domain.Comment;
import com.qacm.test.domain.Post;
import com.qacm.test.repository.CommentRepository;
import com.qacm.test.service.CommentService;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Integration tests for the {@link CommentResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class CommentResourceIT {

    private static final String DEFAULT_TEXT = "AAAAAAAAAA";
    private static final String UPDATED_TEXT = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_CREAION_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_CREAION_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final String ENTITY_API_URL = "/api/comments";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private CommentRepository commentRepository;

    @Mock
    private CommentRepository commentRepositoryMock;

    @Mock
    private CommentService commentServiceMock;

    @Autowired
    private WebTestClient webTestClient;

    private Comment comment;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Comment createEntity() {
        Comment comment = new Comment().text(DEFAULT_TEXT).creaionDate(DEFAULT_CREAION_DATE);
        // Add required entity
        Post post;
        post = PostResourceIT.createEntity();
        post.setId("fixed-id-for-tests");
        comment.setPost(post);
        return comment;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Comment createUpdatedEntity() {
        Comment comment = new Comment().text(UPDATED_TEXT).creaionDate(UPDATED_CREAION_DATE);
        // Add required entity
        Post post;
        post = PostResourceIT.createUpdatedEntity();
        post.setId("fixed-id-for-tests");
        comment.setPost(post);
        return comment;
    }

    @BeforeEach
    public void setupCsrf() {
        webTestClient = webTestClient.mutateWith(csrf());
    }

    @BeforeEach
    public void initTest() {
        commentRepository.deleteAll().block();
        comment = createEntity();
    }

    @Test
    void createComment() throws Exception {
        int databaseSizeBeforeCreate = commentRepository.findAll().collectList().block().size();
        // Create the Comment
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(comment))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Comment in the database
        List<Comment> commentList = commentRepository.findAll().collectList().block();
        assertThat(commentList).hasSize(databaseSizeBeforeCreate + 1);
        Comment testComment = commentList.get(commentList.size() - 1);
        assertThat(testComment.getText()).isEqualTo(DEFAULT_TEXT);
        assertThat(testComment.getCreaionDate()).isEqualTo(DEFAULT_CREAION_DATE);
    }

    @Test
    void createCommentWithExistingId() throws Exception {
        // Create the Comment with an existing ID
        comment.setId("existing_id");

        int databaseSizeBeforeCreate = commentRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(comment))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Comment in the database
        List<Comment> commentList = commentRepository.findAll().collectList().block();
        assertThat(commentList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void checkCreaionDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = commentRepository.findAll().collectList().block().size();
        // set the field null
        comment.setCreaionDate(null);

        // Create the Comment, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(comment))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Comment> commentList = commentRepository.findAll().collectList().block();
        assertThat(commentList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllComments() {
        // Initialize the database
        commentRepository.save(comment).block();

        // Get all the commentList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(comment.getId()))
            .jsonPath("$.[*].text")
            .value(hasItem(DEFAULT_TEXT))
            .jsonPath("$.[*].creaionDate")
            .value(hasItem(DEFAULT_CREAION_DATE.toString()));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllCommentsWithEagerRelationshipsIsEnabled() {
        when(commentServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=true").exchange().expectStatus().isOk();

        verify(commentServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllCommentsWithEagerRelationshipsIsNotEnabled() {
        when(commentServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=true").exchange().expectStatus().isOk();

        verify(commentServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    void getComment() {
        // Initialize the database
        commentRepository.save(comment).block();

        // Get the comment
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, comment.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(comment.getId()))
            .jsonPath("$.text")
            .value(is(DEFAULT_TEXT))
            .jsonPath("$.creaionDate")
            .value(is(DEFAULT_CREAION_DATE.toString()));
    }

    @Test
    void getNonExistingComment() {
        // Get the comment
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewComment() throws Exception {
        // Initialize the database
        commentRepository.save(comment).block();

        int databaseSizeBeforeUpdate = commentRepository.findAll().collectList().block().size();

        // Update the comment
        Comment updatedComment = commentRepository.findById(comment.getId()).block();
        updatedComment.text(UPDATED_TEXT).creaionDate(UPDATED_CREAION_DATE);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedComment.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedComment))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Comment in the database
        List<Comment> commentList = commentRepository.findAll().collectList().block();
        assertThat(commentList).hasSize(databaseSizeBeforeUpdate);
        Comment testComment = commentList.get(commentList.size() - 1);
        assertThat(testComment.getText()).isEqualTo(UPDATED_TEXT);
        assertThat(testComment.getCreaionDate()).isEqualTo(UPDATED_CREAION_DATE);
    }

    @Test
    void putNonExistingComment() throws Exception {
        int databaseSizeBeforeUpdate = commentRepository.findAll().collectList().block().size();
        comment.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, comment.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(comment))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Comment in the database
        List<Comment> commentList = commentRepository.findAll().collectList().block();
        assertThat(commentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchComment() throws Exception {
        int databaseSizeBeforeUpdate = commentRepository.findAll().collectList().block().size();
        comment.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(comment))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Comment in the database
        List<Comment> commentList = commentRepository.findAll().collectList().block();
        assertThat(commentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamComment() throws Exception {
        int databaseSizeBeforeUpdate = commentRepository.findAll().collectList().block().size();
        comment.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(comment))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Comment in the database
        List<Comment> commentList = commentRepository.findAll().collectList().block();
        assertThat(commentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateCommentWithPatch() throws Exception {
        // Initialize the database
        commentRepository.save(comment).block();

        int databaseSizeBeforeUpdate = commentRepository.findAll().collectList().block().size();

        // Update the comment using partial update
        Comment partialUpdatedComment = new Comment();
        partialUpdatedComment.setId(comment.getId());

        partialUpdatedComment.text(UPDATED_TEXT);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedComment.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedComment))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Comment in the database
        List<Comment> commentList = commentRepository.findAll().collectList().block();
        assertThat(commentList).hasSize(databaseSizeBeforeUpdate);
        Comment testComment = commentList.get(commentList.size() - 1);
        assertThat(testComment.getText()).isEqualTo(UPDATED_TEXT);
        assertThat(testComment.getCreaionDate()).isEqualTo(DEFAULT_CREAION_DATE);
    }

    @Test
    void fullUpdateCommentWithPatch() throws Exception {
        // Initialize the database
        commentRepository.save(comment).block();

        int databaseSizeBeforeUpdate = commentRepository.findAll().collectList().block().size();

        // Update the comment using partial update
        Comment partialUpdatedComment = new Comment();
        partialUpdatedComment.setId(comment.getId());

        partialUpdatedComment.text(UPDATED_TEXT).creaionDate(UPDATED_CREAION_DATE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedComment.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedComment))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Comment in the database
        List<Comment> commentList = commentRepository.findAll().collectList().block();
        assertThat(commentList).hasSize(databaseSizeBeforeUpdate);
        Comment testComment = commentList.get(commentList.size() - 1);
        assertThat(testComment.getText()).isEqualTo(UPDATED_TEXT);
        assertThat(testComment.getCreaionDate()).isEqualTo(UPDATED_CREAION_DATE);
    }

    @Test
    void patchNonExistingComment() throws Exception {
        int databaseSizeBeforeUpdate = commentRepository.findAll().collectList().block().size();
        comment.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, comment.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(comment))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Comment in the database
        List<Comment> commentList = commentRepository.findAll().collectList().block();
        assertThat(commentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchComment() throws Exception {
        int databaseSizeBeforeUpdate = commentRepository.findAll().collectList().block().size();
        comment.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(comment))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Comment in the database
        List<Comment> commentList = commentRepository.findAll().collectList().block();
        assertThat(commentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamComment() throws Exception {
        int databaseSizeBeforeUpdate = commentRepository.findAll().collectList().block().size();
        comment.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(comment))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Comment in the database
        List<Comment> commentList = commentRepository.findAll().collectList().block();
        assertThat(commentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteComment() {
        // Initialize the database
        commentRepository.save(comment).block();

        int databaseSizeBeforeDelete = commentRepository.findAll().collectList().block().size();

        // Delete the comment
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, comment.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Comment> commentList = commentRepository.findAll().collectList().block();
        assertThat(commentList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
