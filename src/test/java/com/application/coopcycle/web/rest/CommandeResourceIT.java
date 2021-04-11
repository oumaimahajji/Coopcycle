package com.application.coopcycle.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.application.coopcycle.IntegrationTest;
import com.application.coopcycle.domain.Commande;
import com.application.coopcycle.domain.Compte;
import com.application.coopcycle.domain.Panier;
import com.application.coopcycle.domain.Produit;
import com.application.coopcycle.domain.enumeration.EtatCommande;
import com.application.coopcycle.repository.CommandeRepository;
import com.application.coopcycle.service.CommandeService;
import com.application.coopcycle.service.criteria.CommandeCriteria;
import com.application.coopcycle.service.dto.CommandeDTO;
import com.application.coopcycle.service.mapper.CommandeMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link CommandeResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class CommandeResourceIT {

    private static final Instant DEFAULT_CREATED = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final EtatCommande DEFAULT_STATE = EtatCommande.STARTED;
    private static final EtatCommande UPDATED_STATE = EtatCommande.BEINGTREATED;

    private static final Instant DEFAULT_TIME_STARTED = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_TIME_STARTED = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_TIME_ENDED = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_TIME_ENDED = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/commandes";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private CommandeRepository commandeRepository;

    @Mock
    private CommandeRepository commandeRepositoryMock;

    @Autowired
    private CommandeMapper commandeMapper;

    @Mock
    private CommandeService commandeServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCommandeMockMvc;

    private Commande commande;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Commande createEntity(EntityManager em) {
        Commande commande = new Commande()
            .created(DEFAULT_CREATED)
            .state(DEFAULT_STATE)
            .timeStarted(DEFAULT_TIME_STARTED)
            .timeEnded(DEFAULT_TIME_ENDED);
        return commande;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Commande createUpdatedEntity(EntityManager em) {
        Commande commande = new Commande()
            .created(UPDATED_CREATED)
            .state(UPDATED_STATE)
            .timeStarted(UPDATED_TIME_STARTED)
            .timeEnded(UPDATED_TIME_ENDED);
        return commande;
    }

    @BeforeEach
    public void initTest() {
        commande = createEntity(em);
    }

    @Test
    @Transactional
    void createCommande() throws Exception {
        int databaseSizeBeforeCreate = commandeRepository.findAll().size();
        // Create the Commande
        CommandeDTO commandeDTO = commandeMapper.toDto(commande);
        restCommandeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(commandeDTO)))
            .andExpect(status().isCreated());

        // Validate the Commande in the database
        List<Commande> commandeList = commandeRepository.findAll();
        assertThat(commandeList).hasSize(databaseSizeBeforeCreate + 1);
        Commande testCommande = commandeList.get(commandeList.size() - 1);
        assertThat(testCommande.getCreated()).isEqualTo(DEFAULT_CREATED);
        assertThat(testCommande.getState()).isEqualTo(DEFAULT_STATE);
        assertThat(testCommande.getTimeStarted()).isEqualTo(DEFAULT_TIME_STARTED);
        assertThat(testCommande.getTimeEnded()).isEqualTo(DEFAULT_TIME_ENDED);
    }

    @Test
    @Transactional
    void createCommandeWithExistingId() throws Exception {
        // Create the Commande with an existing ID
        commande.setId(1L);
        CommandeDTO commandeDTO = commandeMapper.toDto(commande);

        int databaseSizeBeforeCreate = commandeRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCommandeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(commandeDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Commande in the database
        List<Commande> commandeList = commandeRepository.findAll();
        assertThat(commandeList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkCreatedIsRequired() throws Exception {
        int databaseSizeBeforeTest = commandeRepository.findAll().size();
        // set the field null
        commande.setCreated(null);

        // Create the Commande, which fails.
        CommandeDTO commandeDTO = commandeMapper.toDto(commande);

        restCommandeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(commandeDTO)))
            .andExpect(status().isBadRequest());

        List<Commande> commandeList = commandeRepository.findAll();
        assertThat(commandeList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllCommandes() throws Exception {
        // Initialize the database
        commandeRepository.saveAndFlush(commande);

        // Get all the commandeList
        restCommandeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(commande.getId().intValue())))
            .andExpect(jsonPath("$.[*].created").value(hasItem(DEFAULT_CREATED.toString())))
            .andExpect(jsonPath("$.[*].state").value(hasItem(DEFAULT_STATE.toString())))
            .andExpect(jsonPath("$.[*].timeStarted").value(hasItem(DEFAULT_TIME_STARTED.toString())))
            .andExpect(jsonPath("$.[*].timeEnded").value(hasItem(DEFAULT_TIME_ENDED.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllCommandesWithEagerRelationshipsIsEnabled() throws Exception {
        when(commandeServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restCommandeMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(commandeServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllCommandesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(commandeServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restCommandeMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(commandeServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    @Transactional
    void getCommande() throws Exception {
        // Initialize the database
        commandeRepository.saveAndFlush(commande);

        // Get the commande
        restCommandeMockMvc
            .perform(get(ENTITY_API_URL_ID, commande.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(commande.getId().intValue()))
            .andExpect(jsonPath("$.created").value(DEFAULT_CREATED.toString()))
            .andExpect(jsonPath("$.state").value(DEFAULT_STATE.toString()))
            .andExpect(jsonPath("$.timeStarted").value(DEFAULT_TIME_STARTED.toString()))
            .andExpect(jsonPath("$.timeEnded").value(DEFAULT_TIME_ENDED.toString()));
    }

    @Test
    @Transactional
    void getCommandesByIdFiltering() throws Exception {
        // Initialize the database
        commandeRepository.saveAndFlush(commande);

        Long id = commande.getId();

        defaultCommandeShouldBeFound("id.equals=" + id);
        defaultCommandeShouldNotBeFound("id.notEquals=" + id);

        defaultCommandeShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultCommandeShouldNotBeFound("id.greaterThan=" + id);

        defaultCommandeShouldBeFound("id.lessThanOrEqual=" + id);
        defaultCommandeShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllCommandesByCreatedIsEqualToSomething() throws Exception {
        // Initialize the database
        commandeRepository.saveAndFlush(commande);

        // Get all the commandeList where created equals to DEFAULT_CREATED
        defaultCommandeShouldBeFound("created.equals=" + DEFAULT_CREATED);

        // Get all the commandeList where created equals to UPDATED_CREATED
        defaultCommandeShouldNotBeFound("created.equals=" + UPDATED_CREATED);
    }

    @Test
    @Transactional
    void getAllCommandesByCreatedIsNotEqualToSomething() throws Exception {
        // Initialize the database
        commandeRepository.saveAndFlush(commande);

        // Get all the commandeList where created not equals to DEFAULT_CREATED
        defaultCommandeShouldNotBeFound("created.notEquals=" + DEFAULT_CREATED);

        // Get all the commandeList where created not equals to UPDATED_CREATED
        defaultCommandeShouldBeFound("created.notEquals=" + UPDATED_CREATED);
    }

    @Test
    @Transactional
    void getAllCommandesByCreatedIsInShouldWork() throws Exception {
        // Initialize the database
        commandeRepository.saveAndFlush(commande);

        // Get all the commandeList where created in DEFAULT_CREATED or UPDATED_CREATED
        defaultCommandeShouldBeFound("created.in=" + DEFAULT_CREATED + "," + UPDATED_CREATED);

        // Get all the commandeList where created equals to UPDATED_CREATED
        defaultCommandeShouldNotBeFound("created.in=" + UPDATED_CREATED);
    }

    @Test
    @Transactional
    void getAllCommandesByCreatedIsNullOrNotNull() throws Exception {
        // Initialize the database
        commandeRepository.saveAndFlush(commande);

        // Get all the commandeList where created is not null
        defaultCommandeShouldBeFound("created.specified=true");

        // Get all the commandeList where created is null
        defaultCommandeShouldNotBeFound("created.specified=false");
    }

    @Test
    @Transactional
    void getAllCommandesByStateIsEqualToSomething() throws Exception {
        // Initialize the database
        commandeRepository.saveAndFlush(commande);

        // Get all the commandeList where state equals to DEFAULT_STATE
        defaultCommandeShouldBeFound("state.equals=" + DEFAULT_STATE);

        // Get all the commandeList where state equals to UPDATED_STATE
        defaultCommandeShouldNotBeFound("state.equals=" + UPDATED_STATE);
    }

    @Test
    @Transactional
    void getAllCommandesByStateIsNotEqualToSomething() throws Exception {
        // Initialize the database
        commandeRepository.saveAndFlush(commande);

        // Get all the commandeList where state not equals to DEFAULT_STATE
        defaultCommandeShouldNotBeFound("state.notEquals=" + DEFAULT_STATE);

        // Get all the commandeList where state not equals to UPDATED_STATE
        defaultCommandeShouldBeFound("state.notEquals=" + UPDATED_STATE);
    }

    @Test
    @Transactional
    void getAllCommandesByStateIsInShouldWork() throws Exception {
        // Initialize the database
        commandeRepository.saveAndFlush(commande);

        // Get all the commandeList where state in DEFAULT_STATE or UPDATED_STATE
        defaultCommandeShouldBeFound("state.in=" + DEFAULT_STATE + "," + UPDATED_STATE);

        // Get all the commandeList where state equals to UPDATED_STATE
        defaultCommandeShouldNotBeFound("state.in=" + UPDATED_STATE);
    }

    @Test
    @Transactional
    void getAllCommandesByStateIsNullOrNotNull() throws Exception {
        // Initialize the database
        commandeRepository.saveAndFlush(commande);

        // Get all the commandeList where state is not null
        defaultCommandeShouldBeFound("state.specified=true");

        // Get all the commandeList where state is null
        defaultCommandeShouldNotBeFound("state.specified=false");
    }

    @Test
    @Transactional
    void getAllCommandesByTimeStartedIsEqualToSomething() throws Exception {
        // Initialize the database
        commandeRepository.saveAndFlush(commande);

        // Get all the commandeList where timeStarted equals to DEFAULT_TIME_STARTED
        defaultCommandeShouldBeFound("timeStarted.equals=" + DEFAULT_TIME_STARTED);

        // Get all the commandeList where timeStarted equals to UPDATED_TIME_STARTED
        defaultCommandeShouldNotBeFound("timeStarted.equals=" + UPDATED_TIME_STARTED);
    }

    @Test
    @Transactional
    void getAllCommandesByTimeStartedIsNotEqualToSomething() throws Exception {
        // Initialize the database
        commandeRepository.saveAndFlush(commande);

        // Get all the commandeList where timeStarted not equals to DEFAULT_TIME_STARTED
        defaultCommandeShouldNotBeFound("timeStarted.notEquals=" + DEFAULT_TIME_STARTED);

        // Get all the commandeList where timeStarted not equals to UPDATED_TIME_STARTED
        defaultCommandeShouldBeFound("timeStarted.notEquals=" + UPDATED_TIME_STARTED);
    }

    @Test
    @Transactional
    void getAllCommandesByTimeStartedIsInShouldWork() throws Exception {
        // Initialize the database
        commandeRepository.saveAndFlush(commande);

        // Get all the commandeList where timeStarted in DEFAULT_TIME_STARTED or UPDATED_TIME_STARTED
        defaultCommandeShouldBeFound("timeStarted.in=" + DEFAULT_TIME_STARTED + "," + UPDATED_TIME_STARTED);

        // Get all the commandeList where timeStarted equals to UPDATED_TIME_STARTED
        defaultCommandeShouldNotBeFound("timeStarted.in=" + UPDATED_TIME_STARTED);
    }

    @Test
    @Transactional
    void getAllCommandesByTimeStartedIsNullOrNotNull() throws Exception {
        // Initialize the database
        commandeRepository.saveAndFlush(commande);

        // Get all the commandeList where timeStarted is not null
        defaultCommandeShouldBeFound("timeStarted.specified=true");

        // Get all the commandeList where timeStarted is null
        defaultCommandeShouldNotBeFound("timeStarted.specified=false");
    }

    @Test
    @Transactional
    void getAllCommandesByTimeEndedIsEqualToSomething() throws Exception {
        // Initialize the database
        commandeRepository.saveAndFlush(commande);

        // Get all the commandeList where timeEnded equals to DEFAULT_TIME_ENDED
        defaultCommandeShouldBeFound("timeEnded.equals=" + DEFAULT_TIME_ENDED);

        // Get all the commandeList where timeEnded equals to UPDATED_TIME_ENDED
        defaultCommandeShouldNotBeFound("timeEnded.equals=" + UPDATED_TIME_ENDED);
    }

    @Test
    @Transactional
    void getAllCommandesByTimeEndedIsNotEqualToSomething() throws Exception {
        // Initialize the database
        commandeRepository.saveAndFlush(commande);

        // Get all the commandeList where timeEnded not equals to DEFAULT_TIME_ENDED
        defaultCommandeShouldNotBeFound("timeEnded.notEquals=" + DEFAULT_TIME_ENDED);

        // Get all the commandeList where timeEnded not equals to UPDATED_TIME_ENDED
        defaultCommandeShouldBeFound("timeEnded.notEquals=" + UPDATED_TIME_ENDED);
    }

    @Test
    @Transactional
    void getAllCommandesByTimeEndedIsInShouldWork() throws Exception {
        // Initialize the database
        commandeRepository.saveAndFlush(commande);

        // Get all the commandeList where timeEnded in DEFAULT_TIME_ENDED or UPDATED_TIME_ENDED
        defaultCommandeShouldBeFound("timeEnded.in=" + DEFAULT_TIME_ENDED + "," + UPDATED_TIME_ENDED);

        // Get all the commandeList where timeEnded equals to UPDATED_TIME_ENDED
        defaultCommandeShouldNotBeFound("timeEnded.in=" + UPDATED_TIME_ENDED);
    }

    @Test
    @Transactional
    void getAllCommandesByTimeEndedIsNullOrNotNull() throws Exception {
        // Initialize the database
        commandeRepository.saveAndFlush(commande);

        // Get all the commandeList where timeEnded is not null
        defaultCommandeShouldBeFound("timeEnded.specified=true");

        // Get all the commandeList where timeEnded is null
        defaultCommandeShouldNotBeFound("timeEnded.specified=false");
    }

    @Test
    @Transactional
    void getAllCommandesByPriceIsEqualToSomething() throws Exception {
        // Initialize the database
        commandeRepository.saveAndFlush(commande);
        Panier price = PanierResourceIT.createEntity(em);
        em.persist(price);
        em.flush();
        commande.setPrice(price);
        commandeRepository.saveAndFlush(commande);
        Long priceId = price.getId();

        // Get all the commandeList where price equals to priceId
        defaultCommandeShouldBeFound("priceId.equals=" + priceId);

        // Get all the commandeList where price equals to (priceId + 1)
        defaultCommandeShouldNotBeFound("priceId.equals=" + (priceId + 1));
    }

    @Test
    @Transactional
    void getAllCommandesByProduitIsEqualToSomething() throws Exception {
        // Initialize the database
        commandeRepository.saveAndFlush(commande);
        Produit produit = ProduitResourceIT.createEntity(em);
        em.persist(produit);
        em.flush();
        commande.addProduit(produit);
        commandeRepository.saveAndFlush(commande);
        Long produitId = produit.getId();

        // Get all the commandeList where produit equals to produitId
        defaultCommandeShouldBeFound("produitId.equals=" + produitId);

        // Get all the commandeList where produit equals to (produitId + 1)
        defaultCommandeShouldNotBeFound("produitId.equals=" + (produitId + 1));
    }

    @Test
    @Transactional
    void getAllCommandesByDeliveredByIsEqualToSomething() throws Exception {
        // Initialize the database
        commandeRepository.saveAndFlush(commande);
        Compte deliveredBy = CompteResourceIT.createEntity(em);
        em.persist(deliveredBy);
        em.flush();
        commande.setDeliveredBy(deliveredBy);
        commandeRepository.saveAndFlush(commande);
        Long deliveredById = deliveredBy.getId();

        // Get all the commandeList where deliveredBy equals to deliveredById
        defaultCommandeShouldBeFound("deliveredById.equals=" + deliveredById);

        // Get all the commandeList where deliveredBy equals to (deliveredById + 1)
        defaultCommandeShouldNotBeFound("deliveredById.equals=" + (deliveredById + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultCommandeShouldBeFound(String filter) throws Exception {
        restCommandeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(commande.getId().intValue())))
            .andExpect(jsonPath("$.[*].created").value(hasItem(DEFAULT_CREATED.toString())))
            .andExpect(jsonPath("$.[*].state").value(hasItem(DEFAULT_STATE.toString())))
            .andExpect(jsonPath("$.[*].timeStarted").value(hasItem(DEFAULT_TIME_STARTED.toString())))
            .andExpect(jsonPath("$.[*].timeEnded").value(hasItem(DEFAULT_TIME_ENDED.toString())));

        // Check, that the count call also returns 1
        restCommandeMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultCommandeShouldNotBeFound(String filter) throws Exception {
        restCommandeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCommandeMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingCommande() throws Exception {
        // Get the commande
        restCommandeMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewCommande() throws Exception {
        // Initialize the database
        commandeRepository.saveAndFlush(commande);

        int databaseSizeBeforeUpdate = commandeRepository.findAll().size();

        // Update the commande
        Commande updatedCommande = commandeRepository.findById(commande.getId()).get();
        // Disconnect from session so that the updates on updatedCommande are not directly saved in db
        em.detach(updatedCommande);
        updatedCommande.created(UPDATED_CREATED).state(UPDATED_STATE).timeStarted(UPDATED_TIME_STARTED).timeEnded(UPDATED_TIME_ENDED);
        CommandeDTO commandeDTO = commandeMapper.toDto(updatedCommande);

        restCommandeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, commandeDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(commandeDTO))
            )
            .andExpect(status().isOk());

        // Validate the Commande in the database
        List<Commande> commandeList = commandeRepository.findAll();
        assertThat(commandeList).hasSize(databaseSizeBeforeUpdate);
        Commande testCommande = commandeList.get(commandeList.size() - 1);
        assertThat(testCommande.getCreated()).isEqualTo(UPDATED_CREATED);
        assertThat(testCommande.getState()).isEqualTo(UPDATED_STATE);
        assertThat(testCommande.getTimeStarted()).isEqualTo(UPDATED_TIME_STARTED);
        assertThat(testCommande.getTimeEnded()).isEqualTo(UPDATED_TIME_ENDED);
    }

    @Test
    @Transactional
    void putNonExistingCommande() throws Exception {
        int databaseSizeBeforeUpdate = commandeRepository.findAll().size();
        commande.setId(count.incrementAndGet());

        // Create the Commande
        CommandeDTO commandeDTO = commandeMapper.toDto(commande);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCommandeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, commandeDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(commandeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Commande in the database
        List<Commande> commandeList = commandeRepository.findAll();
        assertThat(commandeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCommande() throws Exception {
        int databaseSizeBeforeUpdate = commandeRepository.findAll().size();
        commande.setId(count.incrementAndGet());

        // Create the Commande
        CommandeDTO commandeDTO = commandeMapper.toDto(commande);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCommandeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(commandeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Commande in the database
        List<Commande> commandeList = commandeRepository.findAll();
        assertThat(commandeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCommande() throws Exception {
        int databaseSizeBeforeUpdate = commandeRepository.findAll().size();
        commande.setId(count.incrementAndGet());

        // Create the Commande
        CommandeDTO commandeDTO = commandeMapper.toDto(commande);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCommandeMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(commandeDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Commande in the database
        List<Commande> commandeList = commandeRepository.findAll();
        assertThat(commandeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCommandeWithPatch() throws Exception {
        // Initialize the database
        commandeRepository.saveAndFlush(commande);

        int databaseSizeBeforeUpdate = commandeRepository.findAll().size();

        // Update the commande using partial update
        Commande partialUpdatedCommande = new Commande();
        partialUpdatedCommande.setId(commande.getId());

        partialUpdatedCommande.state(UPDATED_STATE).timeEnded(UPDATED_TIME_ENDED);

        restCommandeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCommande.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCommande))
            )
            .andExpect(status().isOk());

        // Validate the Commande in the database
        List<Commande> commandeList = commandeRepository.findAll();
        assertThat(commandeList).hasSize(databaseSizeBeforeUpdate);
        Commande testCommande = commandeList.get(commandeList.size() - 1);
        assertThat(testCommande.getCreated()).isEqualTo(DEFAULT_CREATED);
        assertThat(testCommande.getState()).isEqualTo(UPDATED_STATE);
        assertThat(testCommande.getTimeStarted()).isEqualTo(DEFAULT_TIME_STARTED);
        assertThat(testCommande.getTimeEnded()).isEqualTo(UPDATED_TIME_ENDED);
    }

    @Test
    @Transactional
    void fullUpdateCommandeWithPatch() throws Exception {
        // Initialize the database
        commandeRepository.saveAndFlush(commande);

        int databaseSizeBeforeUpdate = commandeRepository.findAll().size();

        // Update the commande using partial update
        Commande partialUpdatedCommande = new Commande();
        partialUpdatedCommande.setId(commande.getId());

        partialUpdatedCommande
            .created(UPDATED_CREATED)
            .state(UPDATED_STATE)
            .timeStarted(UPDATED_TIME_STARTED)
            .timeEnded(UPDATED_TIME_ENDED);

        restCommandeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCommande.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCommande))
            )
            .andExpect(status().isOk());

        // Validate the Commande in the database
        List<Commande> commandeList = commandeRepository.findAll();
        assertThat(commandeList).hasSize(databaseSizeBeforeUpdate);
        Commande testCommande = commandeList.get(commandeList.size() - 1);
        assertThat(testCommande.getCreated()).isEqualTo(UPDATED_CREATED);
        assertThat(testCommande.getState()).isEqualTo(UPDATED_STATE);
        assertThat(testCommande.getTimeStarted()).isEqualTo(UPDATED_TIME_STARTED);
        assertThat(testCommande.getTimeEnded()).isEqualTo(UPDATED_TIME_ENDED);
    }

    @Test
    @Transactional
    void patchNonExistingCommande() throws Exception {
        int databaseSizeBeforeUpdate = commandeRepository.findAll().size();
        commande.setId(count.incrementAndGet());

        // Create the Commande
        CommandeDTO commandeDTO = commandeMapper.toDto(commande);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCommandeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, commandeDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(commandeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Commande in the database
        List<Commande> commandeList = commandeRepository.findAll();
        assertThat(commandeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCommande() throws Exception {
        int databaseSizeBeforeUpdate = commandeRepository.findAll().size();
        commande.setId(count.incrementAndGet());

        // Create the Commande
        CommandeDTO commandeDTO = commandeMapper.toDto(commande);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCommandeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(commandeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Commande in the database
        List<Commande> commandeList = commandeRepository.findAll();
        assertThat(commandeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCommande() throws Exception {
        int databaseSizeBeforeUpdate = commandeRepository.findAll().size();
        commande.setId(count.incrementAndGet());

        // Create the Commande
        CommandeDTO commandeDTO = commandeMapper.toDto(commande);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCommandeMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(commandeDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Commande in the database
        List<Commande> commandeList = commandeRepository.findAll();
        assertThat(commandeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCommande() throws Exception {
        // Initialize the database
        commandeRepository.saveAndFlush(commande);

        int databaseSizeBeforeDelete = commandeRepository.findAll().size();

        // Delete the commande
        restCommandeMockMvc
            .perform(delete(ENTITY_API_URL_ID, commande.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Commande> commandeList = commandeRepository.findAll();
        assertThat(commandeList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
