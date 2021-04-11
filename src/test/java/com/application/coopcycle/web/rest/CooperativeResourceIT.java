package com.application.coopcycle.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.application.coopcycle.IntegrationTest;
import com.application.coopcycle.domain.Compte;
import com.application.coopcycle.domain.Cooperative;
import com.application.coopcycle.repository.CooperativeRepository;
import com.application.coopcycle.service.criteria.CooperativeCriteria;
import com.application.coopcycle.service.dto.CooperativeDTO;
import com.application.coopcycle.service.mapper.CooperativeMapper;
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
 * Integration tests for the {@link CooperativeResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CooperativeResourceIT {

    private static final String DEFAULT_COOPERATIVE_NAME = "AAAAAAAAAA";
    private static final String UPDATED_COOPERATIVE_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_COOPERATIVE_CITY = "AAAAAAAAAA";
    private static final String UPDATED_COOPERATIVE_CITY = "BBBBBBBBBB";

    private static final String DEFAULT_COOPERATIVE_ADDRESS = "AAAAAAAAAA";
    private static final String UPDATED_COOPERATIVE_ADDRESS = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/cooperatives";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private CooperativeRepository cooperativeRepository;

    @Autowired
    private CooperativeMapper cooperativeMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCooperativeMockMvc;

    private Cooperative cooperative;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Cooperative createEntity(EntityManager em) {
        Cooperative cooperative = new Cooperative()
            .cooperativeName(DEFAULT_COOPERATIVE_NAME)
            .cooperativeCity(DEFAULT_COOPERATIVE_CITY)
            .cooperativeAddress(DEFAULT_COOPERATIVE_ADDRESS);
        return cooperative;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Cooperative createUpdatedEntity(EntityManager em) {
        Cooperative cooperative = new Cooperative()
            .cooperativeName(UPDATED_COOPERATIVE_NAME)
            .cooperativeCity(UPDATED_COOPERATIVE_CITY)
            .cooperativeAddress(UPDATED_COOPERATIVE_ADDRESS);
        return cooperative;
    }

    @BeforeEach
    public void initTest() {
        cooperative = createEntity(em);
    }

    @Test
    @Transactional
    void createCooperative() throws Exception {
        int databaseSizeBeforeCreate = cooperativeRepository.findAll().size();
        // Create the Cooperative
        CooperativeDTO cooperativeDTO = cooperativeMapper.toDto(cooperative);
        restCooperativeMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(cooperativeDTO))
            )
            .andExpect(status().isCreated());

        // Validate the Cooperative in the database
        List<Cooperative> cooperativeList = cooperativeRepository.findAll();
        assertThat(cooperativeList).hasSize(databaseSizeBeforeCreate + 1);
        Cooperative testCooperative = cooperativeList.get(cooperativeList.size() - 1);
        assertThat(testCooperative.getCooperativeName()).isEqualTo(DEFAULT_COOPERATIVE_NAME);
        assertThat(testCooperative.getCooperativeCity()).isEqualTo(DEFAULT_COOPERATIVE_CITY);
        assertThat(testCooperative.getCooperativeAddress()).isEqualTo(DEFAULT_COOPERATIVE_ADDRESS);
    }

    @Test
    @Transactional
    void createCooperativeWithExistingId() throws Exception {
        // Create the Cooperative with an existing ID
        cooperative.setId(1L);
        CooperativeDTO cooperativeDTO = cooperativeMapper.toDto(cooperative);

        int databaseSizeBeforeCreate = cooperativeRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCooperativeMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(cooperativeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Cooperative in the database
        List<Cooperative> cooperativeList = cooperativeRepository.findAll();
        assertThat(cooperativeList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllCooperatives() throws Exception {
        // Initialize the database
        cooperativeRepository.saveAndFlush(cooperative);

        // Get all the cooperativeList
        restCooperativeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(cooperative.getId().intValue())))
            .andExpect(jsonPath("$.[*].cooperativeName").value(hasItem(DEFAULT_COOPERATIVE_NAME)))
            .andExpect(jsonPath("$.[*].cooperativeCity").value(hasItem(DEFAULT_COOPERATIVE_CITY)))
            .andExpect(jsonPath("$.[*].cooperativeAddress").value(hasItem(DEFAULT_COOPERATIVE_ADDRESS)));
    }

    @Test
    @Transactional
    void getCooperative() throws Exception {
        // Initialize the database
        cooperativeRepository.saveAndFlush(cooperative);

        // Get the cooperative
        restCooperativeMockMvc
            .perform(get(ENTITY_API_URL_ID, cooperative.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(cooperative.getId().intValue()))
            .andExpect(jsonPath("$.cooperativeName").value(DEFAULT_COOPERATIVE_NAME))
            .andExpect(jsonPath("$.cooperativeCity").value(DEFAULT_COOPERATIVE_CITY))
            .andExpect(jsonPath("$.cooperativeAddress").value(DEFAULT_COOPERATIVE_ADDRESS));
    }

    @Test
    @Transactional
    void getCooperativesByIdFiltering() throws Exception {
        // Initialize the database
        cooperativeRepository.saveAndFlush(cooperative);

        Long id = cooperative.getId();

        defaultCooperativeShouldBeFound("id.equals=" + id);
        defaultCooperativeShouldNotBeFound("id.notEquals=" + id);

        defaultCooperativeShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultCooperativeShouldNotBeFound("id.greaterThan=" + id);

        defaultCooperativeShouldBeFound("id.lessThanOrEqual=" + id);
        defaultCooperativeShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllCooperativesByCooperativeNameIsEqualToSomething() throws Exception {
        // Initialize the database
        cooperativeRepository.saveAndFlush(cooperative);

        // Get all the cooperativeList where cooperativeName equals to DEFAULT_COOPERATIVE_NAME
        defaultCooperativeShouldBeFound("cooperativeName.equals=" + DEFAULT_COOPERATIVE_NAME);

        // Get all the cooperativeList where cooperativeName equals to UPDATED_COOPERATIVE_NAME
        defaultCooperativeShouldNotBeFound("cooperativeName.equals=" + UPDATED_COOPERATIVE_NAME);
    }

    @Test
    @Transactional
    void getAllCooperativesByCooperativeNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        cooperativeRepository.saveAndFlush(cooperative);

        // Get all the cooperativeList where cooperativeName not equals to DEFAULT_COOPERATIVE_NAME
        defaultCooperativeShouldNotBeFound("cooperativeName.notEquals=" + DEFAULT_COOPERATIVE_NAME);

        // Get all the cooperativeList where cooperativeName not equals to UPDATED_COOPERATIVE_NAME
        defaultCooperativeShouldBeFound("cooperativeName.notEquals=" + UPDATED_COOPERATIVE_NAME);
    }

    @Test
    @Transactional
    void getAllCooperativesByCooperativeNameIsInShouldWork() throws Exception {
        // Initialize the database
        cooperativeRepository.saveAndFlush(cooperative);

        // Get all the cooperativeList where cooperativeName in DEFAULT_COOPERATIVE_NAME or UPDATED_COOPERATIVE_NAME
        defaultCooperativeShouldBeFound("cooperativeName.in=" + DEFAULT_COOPERATIVE_NAME + "," + UPDATED_COOPERATIVE_NAME);

        // Get all the cooperativeList where cooperativeName equals to UPDATED_COOPERATIVE_NAME
        defaultCooperativeShouldNotBeFound("cooperativeName.in=" + UPDATED_COOPERATIVE_NAME);
    }

    @Test
    @Transactional
    void getAllCooperativesByCooperativeNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        cooperativeRepository.saveAndFlush(cooperative);

        // Get all the cooperativeList where cooperativeName is not null
        defaultCooperativeShouldBeFound("cooperativeName.specified=true");

        // Get all the cooperativeList where cooperativeName is null
        defaultCooperativeShouldNotBeFound("cooperativeName.specified=false");
    }

    @Test
    @Transactional
    void getAllCooperativesByCooperativeNameContainsSomething() throws Exception {
        // Initialize the database
        cooperativeRepository.saveAndFlush(cooperative);

        // Get all the cooperativeList where cooperativeName contains DEFAULT_COOPERATIVE_NAME
        defaultCooperativeShouldBeFound("cooperativeName.contains=" + DEFAULT_COOPERATIVE_NAME);

        // Get all the cooperativeList where cooperativeName contains UPDATED_COOPERATIVE_NAME
        defaultCooperativeShouldNotBeFound("cooperativeName.contains=" + UPDATED_COOPERATIVE_NAME);
    }

    @Test
    @Transactional
    void getAllCooperativesByCooperativeNameNotContainsSomething() throws Exception {
        // Initialize the database
        cooperativeRepository.saveAndFlush(cooperative);

        // Get all the cooperativeList where cooperativeName does not contain DEFAULT_COOPERATIVE_NAME
        defaultCooperativeShouldNotBeFound("cooperativeName.doesNotContain=" + DEFAULT_COOPERATIVE_NAME);

        // Get all the cooperativeList where cooperativeName does not contain UPDATED_COOPERATIVE_NAME
        defaultCooperativeShouldBeFound("cooperativeName.doesNotContain=" + UPDATED_COOPERATIVE_NAME);
    }

    @Test
    @Transactional
    void getAllCooperativesByCooperativeCityIsEqualToSomething() throws Exception {
        // Initialize the database
        cooperativeRepository.saveAndFlush(cooperative);

        // Get all the cooperativeList where cooperativeCity equals to DEFAULT_COOPERATIVE_CITY
        defaultCooperativeShouldBeFound("cooperativeCity.equals=" + DEFAULT_COOPERATIVE_CITY);

        // Get all the cooperativeList where cooperativeCity equals to UPDATED_COOPERATIVE_CITY
        defaultCooperativeShouldNotBeFound("cooperativeCity.equals=" + UPDATED_COOPERATIVE_CITY);
    }

    @Test
    @Transactional
    void getAllCooperativesByCooperativeCityIsNotEqualToSomething() throws Exception {
        // Initialize the database
        cooperativeRepository.saveAndFlush(cooperative);

        // Get all the cooperativeList where cooperativeCity not equals to DEFAULT_COOPERATIVE_CITY
        defaultCooperativeShouldNotBeFound("cooperativeCity.notEquals=" + DEFAULT_COOPERATIVE_CITY);

        // Get all the cooperativeList where cooperativeCity not equals to UPDATED_COOPERATIVE_CITY
        defaultCooperativeShouldBeFound("cooperativeCity.notEquals=" + UPDATED_COOPERATIVE_CITY);
    }

    @Test
    @Transactional
    void getAllCooperativesByCooperativeCityIsInShouldWork() throws Exception {
        // Initialize the database
        cooperativeRepository.saveAndFlush(cooperative);

        // Get all the cooperativeList where cooperativeCity in DEFAULT_COOPERATIVE_CITY or UPDATED_COOPERATIVE_CITY
        defaultCooperativeShouldBeFound("cooperativeCity.in=" + DEFAULT_COOPERATIVE_CITY + "," + UPDATED_COOPERATIVE_CITY);

        // Get all the cooperativeList where cooperativeCity equals to UPDATED_COOPERATIVE_CITY
        defaultCooperativeShouldNotBeFound("cooperativeCity.in=" + UPDATED_COOPERATIVE_CITY);
    }

    @Test
    @Transactional
    void getAllCooperativesByCooperativeCityIsNullOrNotNull() throws Exception {
        // Initialize the database
        cooperativeRepository.saveAndFlush(cooperative);

        // Get all the cooperativeList where cooperativeCity is not null
        defaultCooperativeShouldBeFound("cooperativeCity.specified=true");

        // Get all the cooperativeList where cooperativeCity is null
        defaultCooperativeShouldNotBeFound("cooperativeCity.specified=false");
    }

    @Test
    @Transactional
    void getAllCooperativesByCooperativeCityContainsSomething() throws Exception {
        // Initialize the database
        cooperativeRepository.saveAndFlush(cooperative);

        // Get all the cooperativeList where cooperativeCity contains DEFAULT_COOPERATIVE_CITY
        defaultCooperativeShouldBeFound("cooperativeCity.contains=" + DEFAULT_COOPERATIVE_CITY);

        // Get all the cooperativeList where cooperativeCity contains UPDATED_COOPERATIVE_CITY
        defaultCooperativeShouldNotBeFound("cooperativeCity.contains=" + UPDATED_COOPERATIVE_CITY);
    }

    @Test
    @Transactional
    void getAllCooperativesByCooperativeCityNotContainsSomething() throws Exception {
        // Initialize the database
        cooperativeRepository.saveAndFlush(cooperative);

        // Get all the cooperativeList where cooperativeCity does not contain DEFAULT_COOPERATIVE_CITY
        defaultCooperativeShouldNotBeFound("cooperativeCity.doesNotContain=" + DEFAULT_COOPERATIVE_CITY);

        // Get all the cooperativeList where cooperativeCity does not contain UPDATED_COOPERATIVE_CITY
        defaultCooperativeShouldBeFound("cooperativeCity.doesNotContain=" + UPDATED_COOPERATIVE_CITY);
    }

    @Test
    @Transactional
    void getAllCooperativesByCooperativeAddressIsEqualToSomething() throws Exception {
        // Initialize the database
        cooperativeRepository.saveAndFlush(cooperative);

        // Get all the cooperativeList where cooperativeAddress equals to DEFAULT_COOPERATIVE_ADDRESS
        defaultCooperativeShouldBeFound("cooperativeAddress.equals=" + DEFAULT_COOPERATIVE_ADDRESS);

        // Get all the cooperativeList where cooperativeAddress equals to UPDATED_COOPERATIVE_ADDRESS
        defaultCooperativeShouldNotBeFound("cooperativeAddress.equals=" + UPDATED_COOPERATIVE_ADDRESS);
    }

    @Test
    @Transactional
    void getAllCooperativesByCooperativeAddressIsNotEqualToSomething() throws Exception {
        // Initialize the database
        cooperativeRepository.saveAndFlush(cooperative);

        // Get all the cooperativeList where cooperativeAddress not equals to DEFAULT_COOPERATIVE_ADDRESS
        defaultCooperativeShouldNotBeFound("cooperativeAddress.notEquals=" + DEFAULT_COOPERATIVE_ADDRESS);

        // Get all the cooperativeList where cooperativeAddress not equals to UPDATED_COOPERATIVE_ADDRESS
        defaultCooperativeShouldBeFound("cooperativeAddress.notEquals=" + UPDATED_COOPERATIVE_ADDRESS);
    }

    @Test
    @Transactional
    void getAllCooperativesByCooperativeAddressIsInShouldWork() throws Exception {
        // Initialize the database
        cooperativeRepository.saveAndFlush(cooperative);

        // Get all the cooperativeList where cooperativeAddress in DEFAULT_COOPERATIVE_ADDRESS or UPDATED_COOPERATIVE_ADDRESS
        defaultCooperativeShouldBeFound("cooperativeAddress.in=" + DEFAULT_COOPERATIVE_ADDRESS + "," + UPDATED_COOPERATIVE_ADDRESS);

        // Get all the cooperativeList where cooperativeAddress equals to UPDATED_COOPERATIVE_ADDRESS
        defaultCooperativeShouldNotBeFound("cooperativeAddress.in=" + UPDATED_COOPERATIVE_ADDRESS);
    }

    @Test
    @Transactional
    void getAllCooperativesByCooperativeAddressIsNullOrNotNull() throws Exception {
        // Initialize the database
        cooperativeRepository.saveAndFlush(cooperative);

        // Get all the cooperativeList where cooperativeAddress is not null
        defaultCooperativeShouldBeFound("cooperativeAddress.specified=true");

        // Get all the cooperativeList where cooperativeAddress is null
        defaultCooperativeShouldNotBeFound("cooperativeAddress.specified=false");
    }

    @Test
    @Transactional
    void getAllCooperativesByCooperativeAddressContainsSomething() throws Exception {
        // Initialize the database
        cooperativeRepository.saveAndFlush(cooperative);

        // Get all the cooperativeList where cooperativeAddress contains DEFAULT_COOPERATIVE_ADDRESS
        defaultCooperativeShouldBeFound("cooperativeAddress.contains=" + DEFAULT_COOPERATIVE_ADDRESS);

        // Get all the cooperativeList where cooperativeAddress contains UPDATED_COOPERATIVE_ADDRESS
        defaultCooperativeShouldNotBeFound("cooperativeAddress.contains=" + UPDATED_COOPERATIVE_ADDRESS);
    }

    @Test
    @Transactional
    void getAllCooperativesByCooperativeAddressNotContainsSomething() throws Exception {
        // Initialize the database
        cooperativeRepository.saveAndFlush(cooperative);

        // Get all the cooperativeList where cooperativeAddress does not contain DEFAULT_COOPERATIVE_ADDRESS
        defaultCooperativeShouldNotBeFound("cooperativeAddress.doesNotContain=" + DEFAULT_COOPERATIVE_ADDRESS);

        // Get all the cooperativeList where cooperativeAddress does not contain UPDATED_COOPERATIVE_ADDRESS
        defaultCooperativeShouldBeFound("cooperativeAddress.doesNotContain=" + UPDATED_COOPERATIVE_ADDRESS);
    }

    @Test
    @Transactional
    void getAllCooperativesByCompteIsEqualToSomething() throws Exception {
        // Initialize the database
        cooperativeRepository.saveAndFlush(cooperative);
        Compte compte = CompteResourceIT.createEntity(em);
        em.persist(compte);
        em.flush();
        cooperative.addCompte(compte);
        cooperativeRepository.saveAndFlush(cooperative);
        Long compteId = compte.getId();

        // Get all the cooperativeList where compte equals to compteId
        defaultCooperativeShouldBeFound("compteId.equals=" + compteId);

        // Get all the cooperativeList where compte equals to (compteId + 1)
        defaultCooperativeShouldNotBeFound("compteId.equals=" + (compteId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultCooperativeShouldBeFound(String filter) throws Exception {
        restCooperativeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(cooperative.getId().intValue())))
            .andExpect(jsonPath("$.[*].cooperativeName").value(hasItem(DEFAULT_COOPERATIVE_NAME)))
            .andExpect(jsonPath("$.[*].cooperativeCity").value(hasItem(DEFAULT_COOPERATIVE_CITY)))
            .andExpect(jsonPath("$.[*].cooperativeAddress").value(hasItem(DEFAULT_COOPERATIVE_ADDRESS)));

        // Check, that the count call also returns 1
        restCooperativeMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultCooperativeShouldNotBeFound(String filter) throws Exception {
        restCooperativeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCooperativeMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingCooperative() throws Exception {
        // Get the cooperative
        restCooperativeMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewCooperative() throws Exception {
        // Initialize the database
        cooperativeRepository.saveAndFlush(cooperative);

        int databaseSizeBeforeUpdate = cooperativeRepository.findAll().size();

        // Update the cooperative
        Cooperative updatedCooperative = cooperativeRepository.findById(cooperative.getId()).get();
        // Disconnect from session so that the updates on updatedCooperative are not directly saved in db
        em.detach(updatedCooperative);
        updatedCooperative
            .cooperativeName(UPDATED_COOPERATIVE_NAME)
            .cooperativeCity(UPDATED_COOPERATIVE_CITY)
            .cooperativeAddress(UPDATED_COOPERATIVE_ADDRESS);
        CooperativeDTO cooperativeDTO = cooperativeMapper.toDto(updatedCooperative);

        restCooperativeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, cooperativeDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(cooperativeDTO))
            )
            .andExpect(status().isOk());

        // Validate the Cooperative in the database
        List<Cooperative> cooperativeList = cooperativeRepository.findAll();
        assertThat(cooperativeList).hasSize(databaseSizeBeforeUpdate);
        Cooperative testCooperative = cooperativeList.get(cooperativeList.size() - 1);
        assertThat(testCooperative.getCooperativeName()).isEqualTo(UPDATED_COOPERATIVE_NAME);
        assertThat(testCooperative.getCooperativeCity()).isEqualTo(UPDATED_COOPERATIVE_CITY);
        assertThat(testCooperative.getCooperativeAddress()).isEqualTo(UPDATED_COOPERATIVE_ADDRESS);
    }

    @Test
    @Transactional
    void putNonExistingCooperative() throws Exception {
        int databaseSizeBeforeUpdate = cooperativeRepository.findAll().size();
        cooperative.setId(count.incrementAndGet());

        // Create the Cooperative
        CooperativeDTO cooperativeDTO = cooperativeMapper.toDto(cooperative);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCooperativeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, cooperativeDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(cooperativeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Cooperative in the database
        List<Cooperative> cooperativeList = cooperativeRepository.findAll();
        assertThat(cooperativeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCooperative() throws Exception {
        int databaseSizeBeforeUpdate = cooperativeRepository.findAll().size();
        cooperative.setId(count.incrementAndGet());

        // Create the Cooperative
        CooperativeDTO cooperativeDTO = cooperativeMapper.toDto(cooperative);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCooperativeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(cooperativeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Cooperative in the database
        List<Cooperative> cooperativeList = cooperativeRepository.findAll();
        assertThat(cooperativeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCooperative() throws Exception {
        int databaseSizeBeforeUpdate = cooperativeRepository.findAll().size();
        cooperative.setId(count.incrementAndGet());

        // Create the Cooperative
        CooperativeDTO cooperativeDTO = cooperativeMapper.toDto(cooperative);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCooperativeMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(cooperativeDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Cooperative in the database
        List<Cooperative> cooperativeList = cooperativeRepository.findAll();
        assertThat(cooperativeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCooperativeWithPatch() throws Exception {
        // Initialize the database
        cooperativeRepository.saveAndFlush(cooperative);

        int databaseSizeBeforeUpdate = cooperativeRepository.findAll().size();

        // Update the cooperative using partial update
        Cooperative partialUpdatedCooperative = new Cooperative();
        partialUpdatedCooperative.setId(cooperative.getId());

        partialUpdatedCooperative.cooperativeAddress(UPDATED_COOPERATIVE_ADDRESS);

        restCooperativeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCooperative.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCooperative))
            )
            .andExpect(status().isOk());

        // Validate the Cooperative in the database
        List<Cooperative> cooperativeList = cooperativeRepository.findAll();
        assertThat(cooperativeList).hasSize(databaseSizeBeforeUpdate);
        Cooperative testCooperative = cooperativeList.get(cooperativeList.size() - 1);
        assertThat(testCooperative.getCooperativeName()).isEqualTo(DEFAULT_COOPERATIVE_NAME);
        assertThat(testCooperative.getCooperativeCity()).isEqualTo(DEFAULT_COOPERATIVE_CITY);
        assertThat(testCooperative.getCooperativeAddress()).isEqualTo(UPDATED_COOPERATIVE_ADDRESS);
    }

    @Test
    @Transactional
    void fullUpdateCooperativeWithPatch() throws Exception {
        // Initialize the database
        cooperativeRepository.saveAndFlush(cooperative);

        int databaseSizeBeforeUpdate = cooperativeRepository.findAll().size();

        // Update the cooperative using partial update
        Cooperative partialUpdatedCooperative = new Cooperative();
        partialUpdatedCooperative.setId(cooperative.getId());

        partialUpdatedCooperative
            .cooperativeName(UPDATED_COOPERATIVE_NAME)
            .cooperativeCity(UPDATED_COOPERATIVE_CITY)
            .cooperativeAddress(UPDATED_COOPERATIVE_ADDRESS);

        restCooperativeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCooperative.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCooperative))
            )
            .andExpect(status().isOk());

        // Validate the Cooperative in the database
        List<Cooperative> cooperativeList = cooperativeRepository.findAll();
        assertThat(cooperativeList).hasSize(databaseSizeBeforeUpdate);
        Cooperative testCooperative = cooperativeList.get(cooperativeList.size() - 1);
        assertThat(testCooperative.getCooperativeName()).isEqualTo(UPDATED_COOPERATIVE_NAME);
        assertThat(testCooperative.getCooperativeCity()).isEqualTo(UPDATED_COOPERATIVE_CITY);
        assertThat(testCooperative.getCooperativeAddress()).isEqualTo(UPDATED_COOPERATIVE_ADDRESS);
    }

    @Test
    @Transactional
    void patchNonExistingCooperative() throws Exception {
        int databaseSizeBeforeUpdate = cooperativeRepository.findAll().size();
        cooperative.setId(count.incrementAndGet());

        // Create the Cooperative
        CooperativeDTO cooperativeDTO = cooperativeMapper.toDto(cooperative);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCooperativeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, cooperativeDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(cooperativeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Cooperative in the database
        List<Cooperative> cooperativeList = cooperativeRepository.findAll();
        assertThat(cooperativeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCooperative() throws Exception {
        int databaseSizeBeforeUpdate = cooperativeRepository.findAll().size();
        cooperative.setId(count.incrementAndGet());

        // Create the Cooperative
        CooperativeDTO cooperativeDTO = cooperativeMapper.toDto(cooperative);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCooperativeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(cooperativeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Cooperative in the database
        List<Cooperative> cooperativeList = cooperativeRepository.findAll();
        assertThat(cooperativeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCooperative() throws Exception {
        int databaseSizeBeforeUpdate = cooperativeRepository.findAll().size();
        cooperative.setId(count.incrementAndGet());

        // Create the Cooperative
        CooperativeDTO cooperativeDTO = cooperativeMapper.toDto(cooperative);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCooperativeMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(cooperativeDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Cooperative in the database
        List<Cooperative> cooperativeList = cooperativeRepository.findAll();
        assertThat(cooperativeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCooperative() throws Exception {
        // Initialize the database
        cooperativeRepository.saveAndFlush(cooperative);

        int databaseSizeBeforeDelete = cooperativeRepository.findAll().size();

        // Delete the cooperative
        restCooperativeMockMvc
            .perform(delete(ENTITY_API_URL_ID, cooperative.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Cooperative> cooperativeList = cooperativeRepository.findAll();
        assertThat(cooperativeList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
