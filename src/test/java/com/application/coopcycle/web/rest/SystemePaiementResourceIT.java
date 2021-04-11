package com.application.coopcycle.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.application.coopcycle.IntegrationTest;
import com.application.coopcycle.domain.Panier;
import com.application.coopcycle.domain.SystemePaiement;
import com.application.coopcycle.repository.SystemePaiementRepository;
import com.application.coopcycle.service.criteria.SystemePaiementCriteria;
import com.application.coopcycle.service.dto.SystemePaiementDTO;
import com.application.coopcycle.service.mapper.SystemePaiementMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link SystemePaiementResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class SystemePaiementResourceIT {

    private static final String DEFAULT_CREDIT_CARD = "AAAAAAAAAAAAAAAA";
    private static final String UPDATED_CREDIT_CARD = "BBBBBBBBBBBBBBBB";

    private static final Instant DEFAULT_EXPERATION_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_EXPERATION_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_TYPE_CARD = "AAAAAAAAAA";
    private static final String UPDATED_TYPE_CARD = "BBBBBBBBBB";

    private static final String DEFAULT_AMOUNT = "AAAAAAAAAA";
    private static final String UPDATED_AMOUNT = "BBBBBBBBBB";

    private static final Instant DEFAULT_BILL_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_BILL_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/systeme-paiements";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private SystemePaiementRepository systemePaiementRepository;

    @Autowired
    private SystemePaiementMapper systemePaiementMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restSystemePaiementMockMvc;

    private SystemePaiement systemePaiement;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SystemePaiement createEntity(EntityManager em) {
        SystemePaiement systemePaiement = new SystemePaiement()
            .creditCard(DEFAULT_CREDIT_CARD)
            .experationDate(DEFAULT_EXPERATION_DATE)
            .typeCard(DEFAULT_TYPE_CARD)
            .amount(DEFAULT_AMOUNT)
            .billDate(DEFAULT_BILL_DATE);
        return systemePaiement;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SystemePaiement createUpdatedEntity(EntityManager em) {
        SystemePaiement systemePaiement = new SystemePaiement()
            .creditCard(UPDATED_CREDIT_CARD)
            .experationDate(UPDATED_EXPERATION_DATE)
            .typeCard(UPDATED_TYPE_CARD)
            .amount(UPDATED_AMOUNT)
            .billDate(UPDATED_BILL_DATE);
        return systemePaiement;
    }

    @BeforeEach
    public void initTest() {
        systemePaiement = createEntity(em);
    }

    @Test
    @Transactional
    void createSystemePaiement() throws Exception {
        int databaseSizeBeforeCreate = systemePaiementRepository.findAll().size();
        // Create the SystemePaiement
        SystemePaiementDTO systemePaiementDTO = systemePaiementMapper.toDto(systemePaiement);
        restSystemePaiementMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(systemePaiementDTO))
            )
            .andExpect(status().isCreated());

        // Validate the SystemePaiement in the database
        List<SystemePaiement> systemePaiementList = systemePaiementRepository.findAll();
        assertThat(systemePaiementList).hasSize(databaseSizeBeforeCreate + 1);
        SystemePaiement testSystemePaiement = systemePaiementList.get(systemePaiementList.size() - 1);
        assertThat(testSystemePaiement.getCreditCard()).isEqualTo(DEFAULT_CREDIT_CARD);
        assertThat(testSystemePaiement.getExperationDate()).isEqualTo(DEFAULT_EXPERATION_DATE);
        assertThat(testSystemePaiement.getTypeCard()).isEqualTo(DEFAULT_TYPE_CARD);
        assertThat(testSystemePaiement.getAmount()).isEqualTo(DEFAULT_AMOUNT);
        assertThat(testSystemePaiement.getBillDate()).isEqualTo(DEFAULT_BILL_DATE);
    }

    @Test
    @Transactional
    void createSystemePaiementWithExistingId() throws Exception {
        // Create the SystemePaiement with an existing ID
        systemePaiement.setId(1L);
        SystemePaiementDTO systemePaiementDTO = systemePaiementMapper.toDto(systemePaiement);

        int databaseSizeBeforeCreate = systemePaiementRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restSystemePaiementMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(systemePaiementDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SystemePaiement in the database
        List<SystemePaiement> systemePaiementList = systemePaiementRepository.findAll();
        assertThat(systemePaiementList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkExperationDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = systemePaiementRepository.findAll().size();
        // set the field null
        systemePaiement.setExperationDate(null);

        // Create the SystemePaiement, which fails.
        SystemePaiementDTO systemePaiementDTO = systemePaiementMapper.toDto(systemePaiement);

        restSystemePaiementMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(systemePaiementDTO))
            )
            .andExpect(status().isBadRequest());

        List<SystemePaiement> systemePaiementList = systemePaiementRepository.findAll();
        assertThat(systemePaiementList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkBillDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = systemePaiementRepository.findAll().size();
        // set the field null
        systemePaiement.setBillDate(null);

        // Create the SystemePaiement, which fails.
        SystemePaiementDTO systemePaiementDTO = systemePaiementMapper.toDto(systemePaiement);

        restSystemePaiementMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(systemePaiementDTO))
            )
            .andExpect(status().isBadRequest());

        List<SystemePaiement> systemePaiementList = systemePaiementRepository.findAll();
        assertThat(systemePaiementList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllSystemePaiements() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);

        // Get all the systemePaiementList
        restSystemePaiementMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(systemePaiement.getId().intValue())))
            .andExpect(jsonPath("$.[*].creditCard").value(hasItem(DEFAULT_CREDIT_CARD)))
            .andExpect(jsonPath("$.[*].experationDate").value(hasItem(DEFAULT_EXPERATION_DATE.toString())))
            .andExpect(jsonPath("$.[*].typeCard").value(hasItem(DEFAULT_TYPE_CARD)))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT)))
            .andExpect(jsonPath("$.[*].billDate").value(hasItem(DEFAULT_BILL_DATE.toString())));
    }

    @Test
    @Transactional
    void getSystemePaiement() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);

        // Get the systemePaiement
        restSystemePaiementMockMvc
            .perform(get(ENTITY_API_URL_ID, systemePaiement.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(systemePaiement.getId().intValue()))
            .andExpect(jsonPath("$.creditCard").value(DEFAULT_CREDIT_CARD))
            .andExpect(jsonPath("$.experationDate").value(DEFAULT_EXPERATION_DATE.toString()))
            .andExpect(jsonPath("$.typeCard").value(DEFAULT_TYPE_CARD))
            .andExpect(jsonPath("$.amount").value(DEFAULT_AMOUNT))
            .andExpect(jsonPath("$.billDate").value(DEFAULT_BILL_DATE.toString()));
    }

    @Test
    @Transactional
    void getSystemePaiementsByIdFiltering() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);

        Long id = systemePaiement.getId();

        defaultSystemePaiementShouldBeFound("id.equals=" + id);
        defaultSystemePaiementShouldNotBeFound("id.notEquals=" + id);

        defaultSystemePaiementShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultSystemePaiementShouldNotBeFound("id.greaterThan=" + id);

        defaultSystemePaiementShouldBeFound("id.lessThanOrEqual=" + id);
        defaultSystemePaiementShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllSystemePaiementsByCreditCardIsEqualToSomething() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);

        // Get all the systemePaiementList where creditCard equals to DEFAULT_CREDIT_CARD
        defaultSystemePaiementShouldBeFound("creditCard.equals=" + DEFAULT_CREDIT_CARD);

        // Get all the systemePaiementList where creditCard equals to UPDATED_CREDIT_CARD
        defaultSystemePaiementShouldNotBeFound("creditCard.equals=" + UPDATED_CREDIT_CARD);
    }

    @Test
    @Transactional
    void getAllSystemePaiementsByCreditCardIsNotEqualToSomething() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);

        // Get all the systemePaiementList where creditCard not equals to DEFAULT_CREDIT_CARD
        defaultSystemePaiementShouldNotBeFound("creditCard.notEquals=" + DEFAULT_CREDIT_CARD);

        // Get all the systemePaiementList where creditCard not equals to UPDATED_CREDIT_CARD
        defaultSystemePaiementShouldBeFound("creditCard.notEquals=" + UPDATED_CREDIT_CARD);
    }

    @Test
    @Transactional
    void getAllSystemePaiementsByCreditCardIsInShouldWork() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);

        // Get all the systemePaiementList where creditCard in DEFAULT_CREDIT_CARD or UPDATED_CREDIT_CARD
        defaultSystemePaiementShouldBeFound("creditCard.in=" + DEFAULT_CREDIT_CARD + "," + UPDATED_CREDIT_CARD);

        // Get all the systemePaiementList where creditCard equals to UPDATED_CREDIT_CARD
        defaultSystemePaiementShouldNotBeFound("creditCard.in=" + UPDATED_CREDIT_CARD);
    }

    @Test
    @Transactional
    void getAllSystemePaiementsByCreditCardIsNullOrNotNull() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);

        // Get all the systemePaiementList where creditCard is not null
        defaultSystemePaiementShouldBeFound("creditCard.specified=true");

        // Get all the systemePaiementList where creditCard is null
        defaultSystemePaiementShouldNotBeFound("creditCard.specified=false");
    }

    @Test
    @Transactional
    void getAllSystemePaiementsByCreditCardContainsSomething() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);

        // Get all the systemePaiementList where creditCard contains DEFAULT_CREDIT_CARD
        defaultSystemePaiementShouldBeFound("creditCard.contains=" + DEFAULT_CREDIT_CARD);

        // Get all the systemePaiementList where creditCard contains UPDATED_CREDIT_CARD
        defaultSystemePaiementShouldNotBeFound("creditCard.contains=" + UPDATED_CREDIT_CARD);
    }

    @Test
    @Transactional
    void getAllSystemePaiementsByCreditCardNotContainsSomething() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);

        // Get all the systemePaiementList where creditCard does not contain DEFAULT_CREDIT_CARD
        defaultSystemePaiementShouldNotBeFound("creditCard.doesNotContain=" + DEFAULT_CREDIT_CARD);

        // Get all the systemePaiementList where creditCard does not contain UPDATED_CREDIT_CARD
        defaultSystemePaiementShouldBeFound("creditCard.doesNotContain=" + UPDATED_CREDIT_CARD);
    }

    @Test
    @Transactional
    void getAllSystemePaiementsByExperationDateIsEqualToSomething() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);

        // Get all the systemePaiementList where experationDate equals to DEFAULT_EXPERATION_DATE
        defaultSystemePaiementShouldBeFound("experationDate.equals=" + DEFAULT_EXPERATION_DATE);

        // Get all the systemePaiementList where experationDate equals to UPDATED_EXPERATION_DATE
        defaultSystemePaiementShouldNotBeFound("experationDate.equals=" + UPDATED_EXPERATION_DATE);
    }

    @Test
    @Transactional
    void getAllSystemePaiementsByExperationDateIsNotEqualToSomething() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);

        // Get all the systemePaiementList where experationDate not equals to DEFAULT_EXPERATION_DATE
        defaultSystemePaiementShouldNotBeFound("experationDate.notEquals=" + DEFAULT_EXPERATION_DATE);

        // Get all the systemePaiementList where experationDate not equals to UPDATED_EXPERATION_DATE
        defaultSystemePaiementShouldBeFound("experationDate.notEquals=" + UPDATED_EXPERATION_DATE);
    }

    @Test
    @Transactional
    void getAllSystemePaiementsByExperationDateIsInShouldWork() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);

        // Get all the systemePaiementList where experationDate in DEFAULT_EXPERATION_DATE or UPDATED_EXPERATION_DATE
        defaultSystemePaiementShouldBeFound("experationDate.in=" + DEFAULT_EXPERATION_DATE + "," + UPDATED_EXPERATION_DATE);

        // Get all the systemePaiementList where experationDate equals to UPDATED_EXPERATION_DATE
        defaultSystemePaiementShouldNotBeFound("experationDate.in=" + UPDATED_EXPERATION_DATE);
    }

    @Test
    @Transactional
    void getAllSystemePaiementsByExperationDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);

        // Get all the systemePaiementList where experationDate is not null
        defaultSystemePaiementShouldBeFound("experationDate.specified=true");

        // Get all the systemePaiementList where experationDate is null
        defaultSystemePaiementShouldNotBeFound("experationDate.specified=false");
    }

    @Test
    @Transactional
    void getAllSystemePaiementsByTypeCardIsEqualToSomething() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);

        // Get all the systemePaiementList where typeCard equals to DEFAULT_TYPE_CARD
        defaultSystemePaiementShouldBeFound("typeCard.equals=" + DEFAULT_TYPE_CARD);

        // Get all the systemePaiementList where typeCard equals to UPDATED_TYPE_CARD
        defaultSystemePaiementShouldNotBeFound("typeCard.equals=" + UPDATED_TYPE_CARD);
    }

    @Test
    @Transactional
    void getAllSystemePaiementsByTypeCardIsNotEqualToSomething() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);

        // Get all the systemePaiementList where typeCard not equals to DEFAULT_TYPE_CARD
        defaultSystemePaiementShouldNotBeFound("typeCard.notEquals=" + DEFAULT_TYPE_CARD);

        // Get all the systemePaiementList where typeCard not equals to UPDATED_TYPE_CARD
        defaultSystemePaiementShouldBeFound("typeCard.notEquals=" + UPDATED_TYPE_CARD);
    }

    @Test
    @Transactional
    void getAllSystemePaiementsByTypeCardIsInShouldWork() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);

        // Get all the systemePaiementList where typeCard in DEFAULT_TYPE_CARD or UPDATED_TYPE_CARD
        defaultSystemePaiementShouldBeFound("typeCard.in=" + DEFAULT_TYPE_CARD + "," + UPDATED_TYPE_CARD);

        // Get all the systemePaiementList where typeCard equals to UPDATED_TYPE_CARD
        defaultSystemePaiementShouldNotBeFound("typeCard.in=" + UPDATED_TYPE_CARD);
    }

    @Test
    @Transactional
    void getAllSystemePaiementsByTypeCardIsNullOrNotNull() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);

        // Get all the systemePaiementList where typeCard is not null
        defaultSystemePaiementShouldBeFound("typeCard.specified=true");

        // Get all the systemePaiementList where typeCard is null
        defaultSystemePaiementShouldNotBeFound("typeCard.specified=false");
    }

    @Test
    @Transactional
    void getAllSystemePaiementsByTypeCardContainsSomething() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);

        // Get all the systemePaiementList where typeCard contains DEFAULT_TYPE_CARD
        defaultSystemePaiementShouldBeFound("typeCard.contains=" + DEFAULT_TYPE_CARD);

        // Get all the systemePaiementList where typeCard contains UPDATED_TYPE_CARD
        defaultSystemePaiementShouldNotBeFound("typeCard.contains=" + UPDATED_TYPE_CARD);
    }

    @Test
    @Transactional
    void getAllSystemePaiementsByTypeCardNotContainsSomething() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);

        // Get all the systemePaiementList where typeCard does not contain DEFAULT_TYPE_CARD
        defaultSystemePaiementShouldNotBeFound("typeCard.doesNotContain=" + DEFAULT_TYPE_CARD);

        // Get all the systemePaiementList where typeCard does not contain UPDATED_TYPE_CARD
        defaultSystemePaiementShouldBeFound("typeCard.doesNotContain=" + UPDATED_TYPE_CARD);
    }

    @Test
    @Transactional
    void getAllSystemePaiementsByAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);

        // Get all the systemePaiementList where amount equals to DEFAULT_AMOUNT
        defaultSystemePaiementShouldBeFound("amount.equals=" + DEFAULT_AMOUNT);

        // Get all the systemePaiementList where amount equals to UPDATED_AMOUNT
        defaultSystemePaiementShouldNotBeFound("amount.equals=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllSystemePaiementsByAmountIsNotEqualToSomething() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);

        // Get all the systemePaiementList where amount not equals to DEFAULT_AMOUNT
        defaultSystemePaiementShouldNotBeFound("amount.notEquals=" + DEFAULT_AMOUNT);

        // Get all the systemePaiementList where amount not equals to UPDATED_AMOUNT
        defaultSystemePaiementShouldBeFound("amount.notEquals=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllSystemePaiementsByAmountIsInShouldWork() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);

        // Get all the systemePaiementList where amount in DEFAULT_AMOUNT or UPDATED_AMOUNT
        defaultSystemePaiementShouldBeFound("amount.in=" + DEFAULT_AMOUNT + "," + UPDATED_AMOUNT);

        // Get all the systemePaiementList where amount equals to UPDATED_AMOUNT
        defaultSystemePaiementShouldNotBeFound("amount.in=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllSystemePaiementsByAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);

        // Get all the systemePaiementList where amount is not null
        defaultSystemePaiementShouldBeFound("amount.specified=true");

        // Get all the systemePaiementList where amount is null
        defaultSystemePaiementShouldNotBeFound("amount.specified=false");
    }

    @Test
    @Transactional
    void getAllSystemePaiementsByAmountContainsSomething() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);

        // Get all the systemePaiementList where amount contains DEFAULT_AMOUNT
        defaultSystemePaiementShouldBeFound("amount.contains=" + DEFAULT_AMOUNT);

        // Get all the systemePaiementList where amount contains UPDATED_AMOUNT
        defaultSystemePaiementShouldNotBeFound("amount.contains=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllSystemePaiementsByAmountNotContainsSomething() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);

        // Get all the systemePaiementList where amount does not contain DEFAULT_AMOUNT
        defaultSystemePaiementShouldNotBeFound("amount.doesNotContain=" + DEFAULT_AMOUNT);

        // Get all the systemePaiementList where amount does not contain UPDATED_AMOUNT
        defaultSystemePaiementShouldBeFound("amount.doesNotContain=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllSystemePaiementsByBillDateIsEqualToSomething() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);

        // Get all the systemePaiementList where billDate equals to DEFAULT_BILL_DATE
        defaultSystemePaiementShouldBeFound("billDate.equals=" + DEFAULT_BILL_DATE);

        // Get all the systemePaiementList where billDate equals to UPDATED_BILL_DATE
        defaultSystemePaiementShouldNotBeFound("billDate.equals=" + UPDATED_BILL_DATE);
    }

    @Test
    @Transactional
    void getAllSystemePaiementsByBillDateIsNotEqualToSomething() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);

        // Get all the systemePaiementList where billDate not equals to DEFAULT_BILL_DATE
        defaultSystemePaiementShouldNotBeFound("billDate.notEquals=" + DEFAULT_BILL_DATE);

        // Get all the systemePaiementList where billDate not equals to UPDATED_BILL_DATE
        defaultSystemePaiementShouldBeFound("billDate.notEquals=" + UPDATED_BILL_DATE);
    }

    @Test
    @Transactional
    void getAllSystemePaiementsByBillDateIsInShouldWork() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);

        // Get all the systemePaiementList where billDate in DEFAULT_BILL_DATE or UPDATED_BILL_DATE
        defaultSystemePaiementShouldBeFound("billDate.in=" + DEFAULT_BILL_DATE + "," + UPDATED_BILL_DATE);

        // Get all the systemePaiementList where billDate equals to UPDATED_BILL_DATE
        defaultSystemePaiementShouldNotBeFound("billDate.in=" + UPDATED_BILL_DATE);
    }

    @Test
    @Transactional
    void getAllSystemePaiementsByBillDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);

        // Get all the systemePaiementList where billDate is not null
        defaultSystemePaiementShouldBeFound("billDate.specified=true");

        // Get all the systemePaiementList where billDate is null
        defaultSystemePaiementShouldNotBeFound("billDate.specified=false");
    }

    @Test
    @Transactional
    void getAllSystemePaiementsByPanierIsEqualToSomething() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);
        Panier panier = PanierResourceIT.createEntity(em);
        em.persist(panier);
        em.flush();
        systemePaiement.addPanier(panier);
        systemePaiementRepository.saveAndFlush(systemePaiement);
        Long panierId = panier.getId();

        // Get all the systemePaiementList where panier equals to panierId
        defaultSystemePaiementShouldBeFound("panierId.equals=" + panierId);

        // Get all the systemePaiementList where panier equals to (panierId + 1)
        defaultSystemePaiementShouldNotBeFound("panierId.equals=" + (panierId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultSystemePaiementShouldBeFound(String filter) throws Exception {
        restSystemePaiementMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(systemePaiement.getId().intValue())))
            .andExpect(jsonPath("$.[*].creditCard").value(hasItem(DEFAULT_CREDIT_CARD)))
            .andExpect(jsonPath("$.[*].experationDate").value(hasItem(DEFAULT_EXPERATION_DATE.toString())))
            .andExpect(jsonPath("$.[*].typeCard").value(hasItem(DEFAULT_TYPE_CARD)))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT)))
            .andExpect(jsonPath("$.[*].billDate").value(hasItem(DEFAULT_BILL_DATE.toString())));

        // Check, that the count call also returns 1
        restSystemePaiementMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultSystemePaiementShouldNotBeFound(String filter) throws Exception {
        restSystemePaiementMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restSystemePaiementMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingSystemePaiement() throws Exception {
        // Get the systemePaiement
        restSystemePaiementMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewSystemePaiement() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);

        int databaseSizeBeforeUpdate = systemePaiementRepository.findAll().size();

        // Update the systemePaiement
        SystemePaiement updatedSystemePaiement = systemePaiementRepository.findById(systemePaiement.getId()).get();
        // Disconnect from session so that the updates on updatedSystemePaiement are not directly saved in db
        em.detach(updatedSystemePaiement);
        updatedSystemePaiement
            .creditCard(UPDATED_CREDIT_CARD)
            .experationDate(UPDATED_EXPERATION_DATE)
            .typeCard(UPDATED_TYPE_CARD)
            .amount(UPDATED_AMOUNT)
            .billDate(UPDATED_BILL_DATE);
        SystemePaiementDTO systemePaiementDTO = systemePaiementMapper.toDto(updatedSystemePaiement);

        restSystemePaiementMockMvc
            .perform(
                put(ENTITY_API_URL_ID, systemePaiementDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(systemePaiementDTO))
            )
            .andExpect(status().isOk());

        // Validate the SystemePaiement in the database
        List<SystemePaiement> systemePaiementList = systemePaiementRepository.findAll();
        assertThat(systemePaiementList).hasSize(databaseSizeBeforeUpdate);
        SystemePaiement testSystemePaiement = systemePaiementList.get(systemePaiementList.size() - 1);
        assertThat(testSystemePaiement.getCreditCard()).isEqualTo(UPDATED_CREDIT_CARD);
        assertThat(testSystemePaiement.getExperationDate()).isEqualTo(UPDATED_EXPERATION_DATE);
        assertThat(testSystemePaiement.getTypeCard()).isEqualTo(UPDATED_TYPE_CARD);
        assertThat(testSystemePaiement.getAmount()).isEqualTo(UPDATED_AMOUNT);
        assertThat(testSystemePaiement.getBillDate()).isEqualTo(UPDATED_BILL_DATE);
    }

    @Test
    @Transactional
    void putNonExistingSystemePaiement() throws Exception {
        int databaseSizeBeforeUpdate = systemePaiementRepository.findAll().size();
        systemePaiement.setId(count.incrementAndGet());

        // Create the SystemePaiement
        SystemePaiementDTO systemePaiementDTO = systemePaiementMapper.toDto(systemePaiement);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSystemePaiementMockMvc
            .perform(
                put(ENTITY_API_URL_ID, systemePaiementDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(systemePaiementDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SystemePaiement in the database
        List<SystemePaiement> systemePaiementList = systemePaiementRepository.findAll();
        assertThat(systemePaiementList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchSystemePaiement() throws Exception {
        int databaseSizeBeforeUpdate = systemePaiementRepository.findAll().size();
        systemePaiement.setId(count.incrementAndGet());

        // Create the SystemePaiement
        SystemePaiementDTO systemePaiementDTO = systemePaiementMapper.toDto(systemePaiement);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSystemePaiementMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(systemePaiementDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SystemePaiement in the database
        List<SystemePaiement> systemePaiementList = systemePaiementRepository.findAll();
        assertThat(systemePaiementList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamSystemePaiement() throws Exception {
        int databaseSizeBeforeUpdate = systemePaiementRepository.findAll().size();
        systemePaiement.setId(count.incrementAndGet());

        // Create the SystemePaiement
        SystemePaiementDTO systemePaiementDTO = systemePaiementMapper.toDto(systemePaiement);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSystemePaiementMockMvc
            .perform(
                put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(systemePaiementDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the SystemePaiement in the database
        List<SystemePaiement> systemePaiementList = systemePaiementRepository.findAll();
        assertThat(systemePaiementList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateSystemePaiementWithPatch() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);

        int databaseSizeBeforeUpdate = systemePaiementRepository.findAll().size();

        // Update the systemePaiement using partial update
        SystemePaiement partialUpdatedSystemePaiement = new SystemePaiement();
        partialUpdatedSystemePaiement.setId(systemePaiement.getId());

        partialUpdatedSystemePaiement
            .creditCard(UPDATED_CREDIT_CARD)
            .experationDate(UPDATED_EXPERATION_DATE)
            .typeCard(UPDATED_TYPE_CARD)
            .amount(UPDATED_AMOUNT)
            .billDate(UPDATED_BILL_DATE);

        restSystemePaiementMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSystemePaiement.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedSystemePaiement))
            )
            .andExpect(status().isOk());

        // Validate the SystemePaiement in the database
        List<SystemePaiement> systemePaiementList = systemePaiementRepository.findAll();
        assertThat(systemePaiementList).hasSize(databaseSizeBeforeUpdate);
        SystemePaiement testSystemePaiement = systemePaiementList.get(systemePaiementList.size() - 1);
        assertThat(testSystemePaiement.getCreditCard()).isEqualTo(UPDATED_CREDIT_CARD);
        assertThat(testSystemePaiement.getExperationDate()).isEqualTo(UPDATED_EXPERATION_DATE);
        assertThat(testSystemePaiement.getTypeCard()).isEqualTo(UPDATED_TYPE_CARD);
        assertThat(testSystemePaiement.getAmount()).isEqualTo(UPDATED_AMOUNT);
        assertThat(testSystemePaiement.getBillDate()).isEqualTo(UPDATED_BILL_DATE);
    }

    @Test
    @Transactional
    void fullUpdateSystemePaiementWithPatch() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);

        int databaseSizeBeforeUpdate = systemePaiementRepository.findAll().size();

        // Update the systemePaiement using partial update
        SystemePaiement partialUpdatedSystemePaiement = new SystemePaiement();
        partialUpdatedSystemePaiement.setId(systemePaiement.getId());

        partialUpdatedSystemePaiement
            .creditCard(UPDATED_CREDIT_CARD)
            .experationDate(UPDATED_EXPERATION_DATE)
            .typeCard(UPDATED_TYPE_CARD)
            .amount(UPDATED_AMOUNT)
            .billDate(UPDATED_BILL_DATE);

        restSystemePaiementMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSystemePaiement.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedSystemePaiement))
            )
            .andExpect(status().isOk());

        // Validate the SystemePaiement in the database
        List<SystemePaiement> systemePaiementList = systemePaiementRepository.findAll();
        assertThat(systemePaiementList).hasSize(databaseSizeBeforeUpdate);
        SystemePaiement testSystemePaiement = systemePaiementList.get(systemePaiementList.size() - 1);
        assertThat(testSystemePaiement.getCreditCard()).isEqualTo(UPDATED_CREDIT_CARD);
        assertThat(testSystemePaiement.getExperationDate()).isEqualTo(UPDATED_EXPERATION_DATE);
        assertThat(testSystemePaiement.getTypeCard()).isEqualTo(UPDATED_TYPE_CARD);
        assertThat(testSystemePaiement.getAmount()).isEqualTo(UPDATED_AMOUNT);
        assertThat(testSystemePaiement.getBillDate()).isEqualTo(UPDATED_BILL_DATE);
    }

    @Test
    @Transactional
    void patchNonExistingSystemePaiement() throws Exception {
        int databaseSizeBeforeUpdate = systemePaiementRepository.findAll().size();
        systemePaiement.setId(count.incrementAndGet());

        // Create the SystemePaiement
        SystemePaiementDTO systemePaiementDTO = systemePaiementMapper.toDto(systemePaiement);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSystemePaiementMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, systemePaiementDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(systemePaiementDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SystemePaiement in the database
        List<SystemePaiement> systemePaiementList = systemePaiementRepository.findAll();
        assertThat(systemePaiementList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchSystemePaiement() throws Exception {
        int databaseSizeBeforeUpdate = systemePaiementRepository.findAll().size();
        systemePaiement.setId(count.incrementAndGet());

        // Create the SystemePaiement
        SystemePaiementDTO systemePaiementDTO = systemePaiementMapper.toDto(systemePaiement);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSystemePaiementMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(systemePaiementDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SystemePaiement in the database
        List<SystemePaiement> systemePaiementList = systemePaiementRepository.findAll();
        assertThat(systemePaiementList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamSystemePaiement() throws Exception {
        int databaseSizeBeforeUpdate = systemePaiementRepository.findAll().size();
        systemePaiement.setId(count.incrementAndGet());

        // Create the SystemePaiement
        SystemePaiementDTO systemePaiementDTO = systemePaiementMapper.toDto(systemePaiement);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSystemePaiementMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(systemePaiementDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the SystemePaiement in the database
        List<SystemePaiement> systemePaiementList = systemePaiementRepository.findAll();
        assertThat(systemePaiementList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteSystemePaiement() throws Exception {
        // Initialize the database
        systemePaiementRepository.saveAndFlush(systemePaiement);

        int databaseSizeBeforeDelete = systemePaiementRepository.findAll().size();

        // Delete the systemePaiement
        restSystemePaiementMockMvc
            .perform(delete(ENTITY_API_URL_ID, systemePaiement.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<SystemePaiement> systemePaiementList = systemePaiementRepository.findAll();
        assertThat(systemePaiementList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
