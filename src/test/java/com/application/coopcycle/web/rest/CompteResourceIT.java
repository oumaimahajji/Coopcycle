package com.application.coopcycle.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.application.coopcycle.IntegrationTest;
import com.application.coopcycle.domain.Commande;
import com.application.coopcycle.domain.Compte;
import com.application.coopcycle.domain.Cooperative;
import com.application.coopcycle.domain.Panier;
import com.application.coopcycle.domain.enumeration.Role;
import com.application.coopcycle.repository.CompteRepository;
import com.application.coopcycle.service.criteria.CompteCriteria;
import com.application.coopcycle.service.dto.CompteDTO;
import com.application.coopcycle.service.mapper.CompteMapper;
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
 * Integration tests for the {@link CompteResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CompteResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_SURNAME = "AAAAAAAAAA";
    private static final String UPDATED_SURNAME = "BBBBBBBBBB";

    private static final String DEFAULT_EMAIL = "43L@yahooooo.fr";
    private static final String UPDATED_EMAIL = "VeKn7@.fr";

    private static final Role DEFAULT_CATEGORIE = Role.LIVREUR;
    private static final Role UPDATED_CATEGORIE = Role.COMMERCANT;

    private static final String DEFAULT_PHONE_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_PHONE_NUMBER = "BBBBBBBBBB";

    private static final String DEFAULT_ADDRESS = "AAAAAAAAAA";
    private static final String UPDATED_ADDRESS = "BBBBBBBBBB";

    private static final String DEFAULT_POSTAL_CODE = "AAAAA";
    private static final String UPDATED_POSTAL_CODE = "BBBBB";

    private static final String DEFAULT_CITY = "AAAAAAAAAA";
    private static final String UPDATED_CITY = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/comptes";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private CompteMapper compteMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCompteMockMvc;

    private Compte compte;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Compte createEntity(EntityManager em) {
        Compte compte = new Compte()
            .name(DEFAULT_NAME)
            .surname(DEFAULT_SURNAME)
            .email(DEFAULT_EMAIL)
            .categorie(DEFAULT_CATEGORIE)
            .phoneNumber(DEFAULT_PHONE_NUMBER)
            .address(DEFAULT_ADDRESS)
            .postalCode(DEFAULT_POSTAL_CODE)
            .city(DEFAULT_CITY);
        return compte;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Compte createUpdatedEntity(EntityManager em) {
        Compte compte = new Compte()
            .name(UPDATED_NAME)
            .surname(UPDATED_SURNAME)
            .email(UPDATED_EMAIL)
            .categorie(UPDATED_CATEGORIE)
            .phoneNumber(UPDATED_PHONE_NUMBER)
            .address(UPDATED_ADDRESS)
            .postalCode(UPDATED_POSTAL_CODE)
            .city(UPDATED_CITY);
        return compte;
    }

    @BeforeEach
    public void initTest() {
        compte = createEntity(em);
    }

    @Test
    @Transactional
    void createCompte() throws Exception {
        int databaseSizeBeforeCreate = compteRepository.findAll().size();
        // Create the Compte
        CompteDTO compteDTO = compteMapper.toDto(compte);
        restCompteMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(compteDTO)))
            .andExpect(status().isCreated());

        // Validate the Compte in the database
        List<Compte> compteList = compteRepository.findAll();
        assertThat(compteList).hasSize(databaseSizeBeforeCreate + 1);
        Compte testCompte = compteList.get(compteList.size() - 1);
        assertThat(testCompte.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testCompte.getSurname()).isEqualTo(DEFAULT_SURNAME);
        assertThat(testCompte.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testCompte.getCategorie()).isEqualTo(DEFAULT_CATEGORIE);
        assertThat(testCompte.getPhoneNumber()).isEqualTo(DEFAULT_PHONE_NUMBER);
        assertThat(testCompte.getAddress()).isEqualTo(DEFAULT_ADDRESS);
        assertThat(testCompte.getPostalCode()).isEqualTo(DEFAULT_POSTAL_CODE);
        assertThat(testCompte.getCity()).isEqualTo(DEFAULT_CITY);
    }

    @Test
    @Transactional
    void createCompteWithExistingId() throws Exception {
        // Create the Compte with an existing ID
        compte.setId(1L);
        CompteDTO compteDTO = compteMapper.toDto(compte);

        int databaseSizeBeforeCreate = compteRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCompteMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(compteDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Compte in the database
        List<Compte> compteList = compteRepository.findAll();
        assertThat(compteList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = compteRepository.findAll().size();
        // set the field null
        compte.setName(null);

        // Create the Compte, which fails.
        CompteDTO compteDTO = compteMapper.toDto(compte);

        restCompteMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(compteDTO)))
            .andExpect(status().isBadRequest());

        List<Compte> compteList = compteRepository.findAll();
        assertThat(compteList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkSurnameIsRequired() throws Exception {
        int databaseSizeBeforeTest = compteRepository.findAll().size();
        // set the field null
        compte.setSurname(null);

        // Create the Compte, which fails.
        CompteDTO compteDTO = compteMapper.toDto(compte);

        restCompteMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(compteDTO)))
            .andExpect(status().isBadRequest());

        List<Compte> compteList = compteRepository.findAll();
        assertThat(compteList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEmailIsRequired() throws Exception {
        int databaseSizeBeforeTest = compteRepository.findAll().size();
        // set the field null
        compte.setEmail(null);

        // Create the Compte, which fails.
        CompteDTO compteDTO = compteMapper.toDto(compte);

        restCompteMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(compteDTO)))
            .andExpect(status().isBadRequest());

        List<Compte> compteList = compteRepository.findAll();
        assertThat(compteList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllComptes() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList
        restCompteMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(compte.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].surname").value(hasItem(DEFAULT_SURNAME)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].categorie").value(hasItem(DEFAULT_CATEGORIE.toString())))
            .andExpect(jsonPath("$.[*].phoneNumber").value(hasItem(DEFAULT_PHONE_NUMBER)))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS)))
            .andExpect(jsonPath("$.[*].postalCode").value(hasItem(DEFAULT_POSTAL_CODE)))
            .andExpect(jsonPath("$.[*].city").value(hasItem(DEFAULT_CITY)));
    }

    @Test
    @Transactional
    void getCompte() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get the compte
        restCompteMockMvc
            .perform(get(ENTITY_API_URL_ID, compte.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(compte.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.surname").value(DEFAULT_SURNAME))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
            .andExpect(jsonPath("$.categorie").value(DEFAULT_CATEGORIE.toString()))
            .andExpect(jsonPath("$.phoneNumber").value(DEFAULT_PHONE_NUMBER))
            .andExpect(jsonPath("$.address").value(DEFAULT_ADDRESS))
            .andExpect(jsonPath("$.postalCode").value(DEFAULT_POSTAL_CODE))
            .andExpect(jsonPath("$.city").value(DEFAULT_CITY));
    }

    @Test
    @Transactional
    void getComptesByIdFiltering() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        Long id = compte.getId();

        defaultCompteShouldBeFound("id.equals=" + id);
        defaultCompteShouldNotBeFound("id.notEquals=" + id);

        defaultCompteShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultCompteShouldNotBeFound("id.greaterThan=" + id);

        defaultCompteShouldBeFound("id.lessThanOrEqual=" + id);
        defaultCompteShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllComptesByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where name equals to DEFAULT_NAME
        defaultCompteShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the compteList where name equals to UPDATED_NAME
        defaultCompteShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllComptesByNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where name not equals to DEFAULT_NAME
        defaultCompteShouldNotBeFound("name.notEquals=" + DEFAULT_NAME);

        // Get all the compteList where name not equals to UPDATED_NAME
        defaultCompteShouldBeFound("name.notEquals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllComptesByNameIsInShouldWork() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where name in DEFAULT_NAME or UPDATED_NAME
        defaultCompteShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the compteList where name equals to UPDATED_NAME
        defaultCompteShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllComptesByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where name is not null
        defaultCompteShouldBeFound("name.specified=true");

        // Get all the compteList where name is null
        defaultCompteShouldNotBeFound("name.specified=false");
    }

    @Test
    @Transactional
    void getAllComptesByNameContainsSomething() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where name contains DEFAULT_NAME
        defaultCompteShouldBeFound("name.contains=" + DEFAULT_NAME);

        // Get all the compteList where name contains UPDATED_NAME
        defaultCompteShouldNotBeFound("name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllComptesByNameNotContainsSomething() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where name does not contain DEFAULT_NAME
        defaultCompteShouldNotBeFound("name.doesNotContain=" + DEFAULT_NAME);

        // Get all the compteList where name does not contain UPDATED_NAME
        defaultCompteShouldBeFound("name.doesNotContain=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllComptesBySurnameIsEqualToSomething() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where surname equals to DEFAULT_SURNAME
        defaultCompteShouldBeFound("surname.equals=" + DEFAULT_SURNAME);

        // Get all the compteList where surname equals to UPDATED_SURNAME
        defaultCompteShouldNotBeFound("surname.equals=" + UPDATED_SURNAME);
    }

    @Test
    @Transactional
    void getAllComptesBySurnameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where surname not equals to DEFAULT_SURNAME
        defaultCompteShouldNotBeFound("surname.notEquals=" + DEFAULT_SURNAME);

        // Get all the compteList where surname not equals to UPDATED_SURNAME
        defaultCompteShouldBeFound("surname.notEquals=" + UPDATED_SURNAME);
    }

    @Test
    @Transactional
    void getAllComptesBySurnameIsInShouldWork() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where surname in DEFAULT_SURNAME or UPDATED_SURNAME
        defaultCompteShouldBeFound("surname.in=" + DEFAULT_SURNAME + "," + UPDATED_SURNAME);

        // Get all the compteList where surname equals to UPDATED_SURNAME
        defaultCompteShouldNotBeFound("surname.in=" + UPDATED_SURNAME);
    }

    @Test
    @Transactional
    void getAllComptesBySurnameIsNullOrNotNull() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where surname is not null
        defaultCompteShouldBeFound("surname.specified=true");

        // Get all the compteList where surname is null
        defaultCompteShouldNotBeFound("surname.specified=false");
    }

    @Test
    @Transactional
    void getAllComptesBySurnameContainsSomething() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where surname contains DEFAULT_SURNAME
        defaultCompteShouldBeFound("surname.contains=" + DEFAULT_SURNAME);

        // Get all the compteList where surname contains UPDATED_SURNAME
        defaultCompteShouldNotBeFound("surname.contains=" + UPDATED_SURNAME);
    }

    @Test
    @Transactional
    void getAllComptesBySurnameNotContainsSomething() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where surname does not contain DEFAULT_SURNAME
        defaultCompteShouldNotBeFound("surname.doesNotContain=" + DEFAULT_SURNAME);

        // Get all the compteList where surname does not contain UPDATED_SURNAME
        defaultCompteShouldBeFound("surname.doesNotContain=" + UPDATED_SURNAME);
    }

    @Test
    @Transactional
    void getAllComptesByEmailIsEqualToSomething() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where email equals to DEFAULT_EMAIL
        defaultCompteShouldBeFound("email.equals=" + DEFAULT_EMAIL);

        // Get all the compteList where email equals to UPDATED_EMAIL
        defaultCompteShouldNotBeFound("email.equals=" + UPDATED_EMAIL);
    }

    @Test
    @Transactional
    void getAllComptesByEmailIsNotEqualToSomething() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where email not equals to DEFAULT_EMAIL
        defaultCompteShouldNotBeFound("email.notEquals=" + DEFAULT_EMAIL);

        // Get all the compteList where email not equals to UPDATED_EMAIL
        defaultCompteShouldBeFound("email.notEquals=" + UPDATED_EMAIL);
    }

    @Test
    @Transactional
    void getAllComptesByEmailIsInShouldWork() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where email in DEFAULT_EMAIL or UPDATED_EMAIL
        defaultCompteShouldBeFound("email.in=" + DEFAULT_EMAIL + "," + UPDATED_EMAIL);

        // Get all the compteList where email equals to UPDATED_EMAIL
        defaultCompteShouldNotBeFound("email.in=" + UPDATED_EMAIL);
    }

    @Test
    @Transactional
    void getAllComptesByEmailIsNullOrNotNull() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where email is not null
        defaultCompteShouldBeFound("email.specified=true");

        // Get all the compteList where email is null
        defaultCompteShouldNotBeFound("email.specified=false");
    }

    @Test
    @Transactional
    void getAllComptesByEmailContainsSomething() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where email contains DEFAULT_EMAIL
        defaultCompteShouldBeFound("email.contains=" + DEFAULT_EMAIL);

        // Get all the compteList where email contains UPDATED_EMAIL
        defaultCompteShouldNotBeFound("email.contains=" + UPDATED_EMAIL);
    }

    @Test
    @Transactional
    void getAllComptesByEmailNotContainsSomething() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where email does not contain DEFAULT_EMAIL
        defaultCompteShouldNotBeFound("email.doesNotContain=" + DEFAULT_EMAIL);

        // Get all the compteList where email does not contain UPDATED_EMAIL
        defaultCompteShouldBeFound("email.doesNotContain=" + UPDATED_EMAIL);
    }

    @Test
    @Transactional
    void getAllComptesByCategorieIsEqualToSomething() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where categorie equals to DEFAULT_CATEGORIE
        defaultCompteShouldBeFound("categorie.equals=" + DEFAULT_CATEGORIE);

        // Get all the compteList where categorie equals to UPDATED_CATEGORIE
        defaultCompteShouldNotBeFound("categorie.equals=" + UPDATED_CATEGORIE);
    }

    @Test
    @Transactional
    void getAllComptesByCategorieIsNotEqualToSomething() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where categorie not equals to DEFAULT_CATEGORIE
        defaultCompteShouldNotBeFound("categorie.notEquals=" + DEFAULT_CATEGORIE);

        // Get all the compteList where categorie not equals to UPDATED_CATEGORIE
        defaultCompteShouldBeFound("categorie.notEquals=" + UPDATED_CATEGORIE);
    }

    @Test
    @Transactional
    void getAllComptesByCategorieIsInShouldWork() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where categorie in DEFAULT_CATEGORIE or UPDATED_CATEGORIE
        defaultCompteShouldBeFound("categorie.in=" + DEFAULT_CATEGORIE + "," + UPDATED_CATEGORIE);

        // Get all the compteList where categorie equals to UPDATED_CATEGORIE
        defaultCompteShouldNotBeFound("categorie.in=" + UPDATED_CATEGORIE);
    }

    @Test
    @Transactional
    void getAllComptesByCategorieIsNullOrNotNull() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where categorie is not null
        defaultCompteShouldBeFound("categorie.specified=true");

        // Get all the compteList where categorie is null
        defaultCompteShouldNotBeFound("categorie.specified=false");
    }

    @Test
    @Transactional
    void getAllComptesByPhoneNumberIsEqualToSomething() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where phoneNumber equals to DEFAULT_PHONE_NUMBER
        defaultCompteShouldBeFound("phoneNumber.equals=" + DEFAULT_PHONE_NUMBER);

        // Get all the compteList where phoneNumber equals to UPDATED_PHONE_NUMBER
        defaultCompteShouldNotBeFound("phoneNumber.equals=" + UPDATED_PHONE_NUMBER);
    }

    @Test
    @Transactional
    void getAllComptesByPhoneNumberIsNotEqualToSomething() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where phoneNumber not equals to DEFAULT_PHONE_NUMBER
        defaultCompteShouldNotBeFound("phoneNumber.notEquals=" + DEFAULT_PHONE_NUMBER);

        // Get all the compteList where phoneNumber not equals to UPDATED_PHONE_NUMBER
        defaultCompteShouldBeFound("phoneNumber.notEquals=" + UPDATED_PHONE_NUMBER);
    }

    @Test
    @Transactional
    void getAllComptesByPhoneNumberIsInShouldWork() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where phoneNumber in DEFAULT_PHONE_NUMBER or UPDATED_PHONE_NUMBER
        defaultCompteShouldBeFound("phoneNumber.in=" + DEFAULT_PHONE_NUMBER + "," + UPDATED_PHONE_NUMBER);

        // Get all the compteList where phoneNumber equals to UPDATED_PHONE_NUMBER
        defaultCompteShouldNotBeFound("phoneNumber.in=" + UPDATED_PHONE_NUMBER);
    }

    @Test
    @Transactional
    void getAllComptesByPhoneNumberIsNullOrNotNull() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where phoneNumber is not null
        defaultCompteShouldBeFound("phoneNumber.specified=true");

        // Get all the compteList where phoneNumber is null
        defaultCompteShouldNotBeFound("phoneNumber.specified=false");
    }

    @Test
    @Transactional
    void getAllComptesByPhoneNumberContainsSomething() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where phoneNumber contains DEFAULT_PHONE_NUMBER
        defaultCompteShouldBeFound("phoneNumber.contains=" + DEFAULT_PHONE_NUMBER);

        // Get all the compteList where phoneNumber contains UPDATED_PHONE_NUMBER
        defaultCompteShouldNotBeFound("phoneNumber.contains=" + UPDATED_PHONE_NUMBER);
    }

    @Test
    @Transactional
    void getAllComptesByPhoneNumberNotContainsSomething() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where phoneNumber does not contain DEFAULT_PHONE_NUMBER
        defaultCompteShouldNotBeFound("phoneNumber.doesNotContain=" + DEFAULT_PHONE_NUMBER);

        // Get all the compteList where phoneNumber does not contain UPDATED_PHONE_NUMBER
        defaultCompteShouldBeFound("phoneNumber.doesNotContain=" + UPDATED_PHONE_NUMBER);
    }

    @Test
    @Transactional
    void getAllComptesByAddressIsEqualToSomething() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where address equals to DEFAULT_ADDRESS
        defaultCompteShouldBeFound("address.equals=" + DEFAULT_ADDRESS);

        // Get all the compteList where address equals to UPDATED_ADDRESS
        defaultCompteShouldNotBeFound("address.equals=" + UPDATED_ADDRESS);
    }

    @Test
    @Transactional
    void getAllComptesByAddressIsNotEqualToSomething() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where address not equals to DEFAULT_ADDRESS
        defaultCompteShouldNotBeFound("address.notEquals=" + DEFAULT_ADDRESS);

        // Get all the compteList where address not equals to UPDATED_ADDRESS
        defaultCompteShouldBeFound("address.notEquals=" + UPDATED_ADDRESS);
    }

    @Test
    @Transactional
    void getAllComptesByAddressIsInShouldWork() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where address in DEFAULT_ADDRESS or UPDATED_ADDRESS
        defaultCompteShouldBeFound("address.in=" + DEFAULT_ADDRESS + "," + UPDATED_ADDRESS);

        // Get all the compteList where address equals to UPDATED_ADDRESS
        defaultCompteShouldNotBeFound("address.in=" + UPDATED_ADDRESS);
    }

    @Test
    @Transactional
    void getAllComptesByAddressIsNullOrNotNull() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where address is not null
        defaultCompteShouldBeFound("address.specified=true");

        // Get all the compteList where address is null
        defaultCompteShouldNotBeFound("address.specified=false");
    }

    @Test
    @Transactional
    void getAllComptesByAddressContainsSomething() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where address contains DEFAULT_ADDRESS
        defaultCompteShouldBeFound("address.contains=" + DEFAULT_ADDRESS);

        // Get all the compteList where address contains UPDATED_ADDRESS
        defaultCompteShouldNotBeFound("address.contains=" + UPDATED_ADDRESS);
    }

    @Test
    @Transactional
    void getAllComptesByAddressNotContainsSomething() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where address does not contain DEFAULT_ADDRESS
        defaultCompteShouldNotBeFound("address.doesNotContain=" + DEFAULT_ADDRESS);

        // Get all the compteList where address does not contain UPDATED_ADDRESS
        defaultCompteShouldBeFound("address.doesNotContain=" + UPDATED_ADDRESS);
    }

    @Test
    @Transactional
    void getAllComptesByPostalCodeIsEqualToSomething() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where postalCode equals to DEFAULT_POSTAL_CODE
        defaultCompteShouldBeFound("postalCode.equals=" + DEFAULT_POSTAL_CODE);

        // Get all the compteList where postalCode equals to UPDATED_POSTAL_CODE
        defaultCompteShouldNotBeFound("postalCode.equals=" + UPDATED_POSTAL_CODE);
    }

    @Test
    @Transactional
    void getAllComptesByPostalCodeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where postalCode not equals to DEFAULT_POSTAL_CODE
        defaultCompteShouldNotBeFound("postalCode.notEquals=" + DEFAULT_POSTAL_CODE);

        // Get all the compteList where postalCode not equals to UPDATED_POSTAL_CODE
        defaultCompteShouldBeFound("postalCode.notEquals=" + UPDATED_POSTAL_CODE);
    }

    @Test
    @Transactional
    void getAllComptesByPostalCodeIsInShouldWork() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where postalCode in DEFAULT_POSTAL_CODE or UPDATED_POSTAL_CODE
        defaultCompteShouldBeFound("postalCode.in=" + DEFAULT_POSTAL_CODE + "," + UPDATED_POSTAL_CODE);

        // Get all the compteList where postalCode equals to UPDATED_POSTAL_CODE
        defaultCompteShouldNotBeFound("postalCode.in=" + UPDATED_POSTAL_CODE);
    }

    @Test
    @Transactional
    void getAllComptesByPostalCodeIsNullOrNotNull() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where postalCode is not null
        defaultCompteShouldBeFound("postalCode.specified=true");

        // Get all the compteList where postalCode is null
        defaultCompteShouldNotBeFound("postalCode.specified=false");
    }

    @Test
    @Transactional
    void getAllComptesByPostalCodeContainsSomething() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where postalCode contains DEFAULT_POSTAL_CODE
        defaultCompteShouldBeFound("postalCode.contains=" + DEFAULT_POSTAL_CODE);

        // Get all the compteList where postalCode contains UPDATED_POSTAL_CODE
        defaultCompteShouldNotBeFound("postalCode.contains=" + UPDATED_POSTAL_CODE);
    }

    @Test
    @Transactional
    void getAllComptesByPostalCodeNotContainsSomething() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where postalCode does not contain DEFAULT_POSTAL_CODE
        defaultCompteShouldNotBeFound("postalCode.doesNotContain=" + DEFAULT_POSTAL_CODE);

        // Get all the compteList where postalCode does not contain UPDATED_POSTAL_CODE
        defaultCompteShouldBeFound("postalCode.doesNotContain=" + UPDATED_POSTAL_CODE);
    }

    @Test
    @Transactional
    void getAllComptesByCityIsEqualToSomething() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where city equals to DEFAULT_CITY
        defaultCompteShouldBeFound("city.equals=" + DEFAULT_CITY);

        // Get all the compteList where city equals to UPDATED_CITY
        defaultCompteShouldNotBeFound("city.equals=" + UPDATED_CITY);
    }

    @Test
    @Transactional
    void getAllComptesByCityIsNotEqualToSomething() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where city not equals to DEFAULT_CITY
        defaultCompteShouldNotBeFound("city.notEquals=" + DEFAULT_CITY);

        // Get all the compteList where city not equals to UPDATED_CITY
        defaultCompteShouldBeFound("city.notEquals=" + UPDATED_CITY);
    }

    @Test
    @Transactional
    void getAllComptesByCityIsInShouldWork() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where city in DEFAULT_CITY or UPDATED_CITY
        defaultCompteShouldBeFound("city.in=" + DEFAULT_CITY + "," + UPDATED_CITY);

        // Get all the compteList where city equals to UPDATED_CITY
        defaultCompteShouldNotBeFound("city.in=" + UPDATED_CITY);
    }

    @Test
    @Transactional
    void getAllComptesByCityIsNullOrNotNull() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where city is not null
        defaultCompteShouldBeFound("city.specified=true");

        // Get all the compteList where city is null
        defaultCompteShouldNotBeFound("city.specified=false");
    }

    @Test
    @Transactional
    void getAllComptesByCityContainsSomething() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where city contains DEFAULT_CITY
        defaultCompteShouldBeFound("city.contains=" + DEFAULT_CITY);

        // Get all the compteList where city contains UPDATED_CITY
        defaultCompteShouldNotBeFound("city.contains=" + UPDATED_CITY);
    }

    @Test
    @Transactional
    void getAllComptesByCityNotContainsSomething() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        // Get all the compteList where city does not contain DEFAULT_CITY
        defaultCompteShouldNotBeFound("city.doesNotContain=" + DEFAULT_CITY);

        // Get all the compteList where city does not contain UPDATED_CITY
        defaultCompteShouldBeFound("city.doesNotContain=" + UPDATED_CITY);
    }

    @Test
    @Transactional
    void getAllComptesByPanierIsEqualToSomething() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);
        Panier panier = PanierResourceIT.createEntity(em);
        em.persist(panier);
        em.flush();
        compte.addPanier(panier);
        compteRepository.saveAndFlush(compte);
        Long panierId = panier.getId();

        // Get all the compteList where panier equals to panierId
        defaultCompteShouldBeFound("panierId.equals=" + panierId);

        // Get all the compteList where panier equals to (panierId + 1)
        defaultCompteShouldNotBeFound("panierId.equals=" + (panierId + 1));
    }

    @Test
    @Transactional
    void getAllComptesByCommandeIsEqualToSomething() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);
        Commande commande = CommandeResourceIT.createEntity(em);
        em.persist(commande);
        em.flush();
        compte.addCommande(commande);
        compteRepository.saveAndFlush(compte);
        Long commandeId = commande.getId();

        // Get all the compteList where commande equals to commandeId
        defaultCompteShouldBeFound("commandeId.equals=" + commandeId);

        // Get all the compteList where commande equals to (commandeId + 1)
        defaultCompteShouldNotBeFound("commandeId.equals=" + (commandeId + 1));
    }

    @Test
    @Transactional
    void getAllComptesByMemberOfIsEqualToSomething() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);
        Cooperative memberOf = CooperativeResourceIT.createEntity(em);
        em.persist(memberOf);
        em.flush();
        compte.setMemberOf(memberOf);
        compteRepository.saveAndFlush(compte);
        Long memberOfId = memberOf.getId();

        // Get all the compteList where memberOf equals to memberOfId
        defaultCompteShouldBeFound("memberOfId.equals=" + memberOfId);

        // Get all the compteList where memberOf equals to (memberOfId + 1)
        defaultCompteShouldNotBeFound("memberOfId.equals=" + (memberOfId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultCompteShouldBeFound(String filter) throws Exception {
        restCompteMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(compte.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].surname").value(hasItem(DEFAULT_SURNAME)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].categorie").value(hasItem(DEFAULT_CATEGORIE.toString())))
            .andExpect(jsonPath("$.[*].phoneNumber").value(hasItem(DEFAULT_PHONE_NUMBER)))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS)))
            .andExpect(jsonPath("$.[*].postalCode").value(hasItem(DEFAULT_POSTAL_CODE)))
            .andExpect(jsonPath("$.[*].city").value(hasItem(DEFAULT_CITY)));

        // Check, that the count call also returns 1
        restCompteMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultCompteShouldNotBeFound(String filter) throws Exception {
        restCompteMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCompteMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingCompte() throws Exception {
        // Get the compte
        restCompteMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewCompte() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        int databaseSizeBeforeUpdate = compteRepository.findAll().size();

        // Update the compte
        Compte updatedCompte = compteRepository.findById(compte.getId()).get();
        // Disconnect from session so that the updates on updatedCompte are not directly saved in db
        em.detach(updatedCompte);
        updatedCompte
            .name(UPDATED_NAME)
            .surname(UPDATED_SURNAME)
            .email(UPDATED_EMAIL)
            .categorie(UPDATED_CATEGORIE)
            .phoneNumber(UPDATED_PHONE_NUMBER)
            .address(UPDATED_ADDRESS)
            .postalCode(UPDATED_POSTAL_CODE)
            .city(UPDATED_CITY);
        CompteDTO compteDTO = compteMapper.toDto(updatedCompte);

        restCompteMockMvc
            .perform(
                put(ENTITY_API_URL_ID, compteDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(compteDTO))
            )
            .andExpect(status().isOk());

        // Validate the Compte in the database
        List<Compte> compteList = compteRepository.findAll();
        assertThat(compteList).hasSize(databaseSizeBeforeUpdate);
        Compte testCompte = compteList.get(compteList.size() - 1);
        assertThat(testCompte.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testCompte.getSurname()).isEqualTo(UPDATED_SURNAME);
        assertThat(testCompte.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testCompte.getCategorie()).isEqualTo(UPDATED_CATEGORIE);
        assertThat(testCompte.getPhoneNumber()).isEqualTo(UPDATED_PHONE_NUMBER);
        assertThat(testCompte.getAddress()).isEqualTo(UPDATED_ADDRESS);
        assertThat(testCompte.getPostalCode()).isEqualTo(UPDATED_POSTAL_CODE);
        assertThat(testCompte.getCity()).isEqualTo(UPDATED_CITY);
    }

    @Test
    @Transactional
    void putNonExistingCompte() throws Exception {
        int databaseSizeBeforeUpdate = compteRepository.findAll().size();
        compte.setId(count.incrementAndGet());

        // Create the Compte
        CompteDTO compteDTO = compteMapper.toDto(compte);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCompteMockMvc
            .perform(
                put(ENTITY_API_URL_ID, compteDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(compteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Compte in the database
        List<Compte> compteList = compteRepository.findAll();
        assertThat(compteList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCompte() throws Exception {
        int databaseSizeBeforeUpdate = compteRepository.findAll().size();
        compte.setId(count.incrementAndGet());

        // Create the Compte
        CompteDTO compteDTO = compteMapper.toDto(compte);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCompteMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(compteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Compte in the database
        List<Compte> compteList = compteRepository.findAll();
        assertThat(compteList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCompte() throws Exception {
        int databaseSizeBeforeUpdate = compteRepository.findAll().size();
        compte.setId(count.incrementAndGet());

        // Create the Compte
        CompteDTO compteDTO = compteMapper.toDto(compte);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCompteMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(compteDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Compte in the database
        List<Compte> compteList = compteRepository.findAll();
        assertThat(compteList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCompteWithPatch() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        int databaseSizeBeforeUpdate = compteRepository.findAll().size();

        // Update the compte using partial update
        Compte partialUpdatedCompte = new Compte();
        partialUpdatedCompte.setId(compte.getId());

        partialUpdatedCompte
            .name(UPDATED_NAME)
            .categorie(UPDATED_CATEGORIE)
            .phoneNumber(UPDATED_PHONE_NUMBER)
            .postalCode(UPDATED_POSTAL_CODE);

        restCompteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCompte.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCompte))
            )
            .andExpect(status().isOk());

        // Validate the Compte in the database
        List<Compte> compteList = compteRepository.findAll();
        assertThat(compteList).hasSize(databaseSizeBeforeUpdate);
        Compte testCompte = compteList.get(compteList.size() - 1);
        assertThat(testCompte.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testCompte.getSurname()).isEqualTo(DEFAULT_SURNAME);
        assertThat(testCompte.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testCompte.getCategorie()).isEqualTo(UPDATED_CATEGORIE);
        assertThat(testCompte.getPhoneNumber()).isEqualTo(UPDATED_PHONE_NUMBER);
        assertThat(testCompte.getAddress()).isEqualTo(DEFAULT_ADDRESS);
        assertThat(testCompte.getPostalCode()).isEqualTo(UPDATED_POSTAL_CODE);
        assertThat(testCompte.getCity()).isEqualTo(DEFAULT_CITY);
    }

    @Test
    @Transactional
    void fullUpdateCompteWithPatch() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        int databaseSizeBeforeUpdate = compteRepository.findAll().size();

        // Update the compte using partial update
        Compte partialUpdatedCompte = new Compte();
        partialUpdatedCompte.setId(compte.getId());

        partialUpdatedCompte
            .name(UPDATED_NAME)
            .surname(UPDATED_SURNAME)
            .email(UPDATED_EMAIL)
            .categorie(UPDATED_CATEGORIE)
            .phoneNumber(UPDATED_PHONE_NUMBER)
            .address(UPDATED_ADDRESS)
            .postalCode(UPDATED_POSTAL_CODE)
            .city(UPDATED_CITY);

        restCompteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCompte.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCompte))
            )
            .andExpect(status().isOk());

        // Validate the Compte in the database
        List<Compte> compteList = compteRepository.findAll();
        assertThat(compteList).hasSize(databaseSizeBeforeUpdate);
        Compte testCompte = compteList.get(compteList.size() - 1);
        assertThat(testCompte.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testCompte.getSurname()).isEqualTo(UPDATED_SURNAME);
        assertThat(testCompte.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testCompte.getCategorie()).isEqualTo(UPDATED_CATEGORIE);
        assertThat(testCompte.getPhoneNumber()).isEqualTo(UPDATED_PHONE_NUMBER);
        assertThat(testCompte.getAddress()).isEqualTo(UPDATED_ADDRESS);
        assertThat(testCompte.getPostalCode()).isEqualTo(UPDATED_POSTAL_CODE);
        assertThat(testCompte.getCity()).isEqualTo(UPDATED_CITY);
    }

    @Test
    @Transactional
    void patchNonExistingCompte() throws Exception {
        int databaseSizeBeforeUpdate = compteRepository.findAll().size();
        compte.setId(count.incrementAndGet());

        // Create the Compte
        CompteDTO compteDTO = compteMapper.toDto(compte);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCompteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, compteDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(compteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Compte in the database
        List<Compte> compteList = compteRepository.findAll();
        assertThat(compteList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCompte() throws Exception {
        int databaseSizeBeforeUpdate = compteRepository.findAll().size();
        compte.setId(count.incrementAndGet());

        // Create the Compte
        CompteDTO compteDTO = compteMapper.toDto(compte);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCompteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(compteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Compte in the database
        List<Compte> compteList = compteRepository.findAll();
        assertThat(compteList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCompte() throws Exception {
        int databaseSizeBeforeUpdate = compteRepository.findAll().size();
        compte.setId(count.incrementAndGet());

        // Create the Compte
        CompteDTO compteDTO = compteMapper.toDto(compte);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCompteMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(compteDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Compte in the database
        List<Compte> compteList = compteRepository.findAll();
        assertThat(compteList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCompte() throws Exception {
        // Initialize the database
        compteRepository.saveAndFlush(compte);

        int databaseSizeBeforeDelete = compteRepository.findAll().size();

        // Delete the compte
        restCompteMockMvc
            .perform(delete(ENTITY_API_URL_ID, compte.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Compte> compteList = compteRepository.findAll();
        assertThat(compteList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
