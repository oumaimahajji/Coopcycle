package com.application.coopcycle.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.application.coopcycle.IntegrationTest;
import com.application.coopcycle.domain.Produit;
import com.application.coopcycle.domain.Restaurant;
import com.application.coopcycle.repository.RestaurantRepository;
import com.application.coopcycle.service.criteria.RestaurantCriteria;
import com.application.coopcycle.service.dto.RestaurantDTO;
import com.application.coopcycle.service.mapper.RestaurantMapper;
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
 * Integration tests for the {@link RestaurantResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class RestaurantResourceIT {

    private static final String DEFAULT_RESTAURANT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_RESTAURANT_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DELIVERY_PRICE = "AAAAAAAAAA";
    private static final String UPDATED_DELIVERY_PRICE = "BBBBBBBBBB";

    private static final String DEFAULT_RESTAURANT_ADDRESS = "AAAAAAAAAA";
    private static final String UPDATED_RESTAURANT_ADDRESS = "BBBBBBBBBB";

    private static final String DEFAULT_RESTAURANT_CITY = "AAAAAAAAAA";
    private static final String UPDATED_RESTAURANT_CITY = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/restaurants";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private RestaurantMapper restaurantMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restRestaurantMockMvc;

    private Restaurant restaurant;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Restaurant createEntity(EntityManager em) {
        Restaurant restaurant = new Restaurant()
            .restaurantName(DEFAULT_RESTAURANT_NAME)
            .deliveryPrice(DEFAULT_DELIVERY_PRICE)
            .restaurantAddress(DEFAULT_RESTAURANT_ADDRESS)
            .restaurantCity(DEFAULT_RESTAURANT_CITY);
        return restaurant;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Restaurant createUpdatedEntity(EntityManager em) {
        Restaurant restaurant = new Restaurant()
            .restaurantName(UPDATED_RESTAURANT_NAME)
            .deliveryPrice(UPDATED_DELIVERY_PRICE)
            .restaurantAddress(UPDATED_RESTAURANT_ADDRESS)
            .restaurantCity(UPDATED_RESTAURANT_CITY);
        return restaurant;
    }

    @BeforeEach
    public void initTest() {
        restaurant = createEntity(em);
    }

    @Test
    @Transactional
    void createRestaurant() throws Exception {
        int databaseSizeBeforeCreate = restaurantRepository.findAll().size();
        // Create the Restaurant
        RestaurantDTO restaurantDTO = restaurantMapper.toDto(restaurant);
        restRestaurantMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(restaurantDTO)))
            .andExpect(status().isCreated());

        // Validate the Restaurant in the database
        List<Restaurant> restaurantList = restaurantRepository.findAll();
        assertThat(restaurantList).hasSize(databaseSizeBeforeCreate + 1);
        Restaurant testRestaurant = restaurantList.get(restaurantList.size() - 1);
        assertThat(testRestaurant.getRestaurantName()).isEqualTo(DEFAULT_RESTAURANT_NAME);
        assertThat(testRestaurant.getDeliveryPrice()).isEqualTo(DEFAULT_DELIVERY_PRICE);
        assertThat(testRestaurant.getRestaurantAddress()).isEqualTo(DEFAULT_RESTAURANT_ADDRESS);
        assertThat(testRestaurant.getRestaurantCity()).isEqualTo(DEFAULT_RESTAURANT_CITY);
    }

    @Test
    @Transactional
    void createRestaurantWithExistingId() throws Exception {
        // Create the Restaurant with an existing ID
        restaurant.setId(1L);
        RestaurantDTO restaurantDTO = restaurantMapper.toDto(restaurant);

        int databaseSizeBeforeCreate = restaurantRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restRestaurantMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(restaurantDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Restaurant in the database
        List<Restaurant> restaurantList = restaurantRepository.findAll();
        assertThat(restaurantList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllRestaurants() throws Exception {
        // Initialize the database
        restaurantRepository.saveAndFlush(restaurant);

        // Get all the restaurantList
        restRestaurantMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(restaurant.getId().intValue())))
            .andExpect(jsonPath("$.[*].restaurantName").value(hasItem(DEFAULT_RESTAURANT_NAME)))
            .andExpect(jsonPath("$.[*].deliveryPrice").value(hasItem(DEFAULT_DELIVERY_PRICE)))
            .andExpect(jsonPath("$.[*].restaurantAddress").value(hasItem(DEFAULT_RESTAURANT_ADDRESS)))
            .andExpect(jsonPath("$.[*].restaurantCity").value(hasItem(DEFAULT_RESTAURANT_CITY)));
    }

    @Test
    @Transactional
    void getRestaurant() throws Exception {
        // Initialize the database
        restaurantRepository.saveAndFlush(restaurant);

        // Get the restaurant
        restRestaurantMockMvc
            .perform(get(ENTITY_API_URL_ID, restaurant.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(restaurant.getId().intValue()))
            .andExpect(jsonPath("$.restaurantName").value(DEFAULT_RESTAURANT_NAME))
            .andExpect(jsonPath("$.deliveryPrice").value(DEFAULT_DELIVERY_PRICE))
            .andExpect(jsonPath("$.restaurantAddress").value(DEFAULT_RESTAURANT_ADDRESS))
            .andExpect(jsonPath("$.restaurantCity").value(DEFAULT_RESTAURANT_CITY));
    }

    @Test
    @Transactional
    void getRestaurantsByIdFiltering() throws Exception {
        // Initialize the database
        restaurantRepository.saveAndFlush(restaurant);

        Long id = restaurant.getId();

        defaultRestaurantShouldBeFound("id.equals=" + id);
        defaultRestaurantShouldNotBeFound("id.notEquals=" + id);

        defaultRestaurantShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultRestaurantShouldNotBeFound("id.greaterThan=" + id);

        defaultRestaurantShouldBeFound("id.lessThanOrEqual=" + id);
        defaultRestaurantShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllRestaurantsByRestaurantNameIsEqualToSomething() throws Exception {
        // Initialize the database
        restaurantRepository.saveAndFlush(restaurant);

        // Get all the restaurantList where restaurantName equals to DEFAULT_RESTAURANT_NAME
        defaultRestaurantShouldBeFound("restaurantName.equals=" + DEFAULT_RESTAURANT_NAME);

        // Get all the restaurantList where restaurantName equals to UPDATED_RESTAURANT_NAME
        defaultRestaurantShouldNotBeFound("restaurantName.equals=" + UPDATED_RESTAURANT_NAME);
    }

    @Test
    @Transactional
    void getAllRestaurantsByRestaurantNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        restaurantRepository.saveAndFlush(restaurant);

        // Get all the restaurantList where restaurantName not equals to DEFAULT_RESTAURANT_NAME
        defaultRestaurantShouldNotBeFound("restaurantName.notEquals=" + DEFAULT_RESTAURANT_NAME);

        // Get all the restaurantList where restaurantName not equals to UPDATED_RESTAURANT_NAME
        defaultRestaurantShouldBeFound("restaurantName.notEquals=" + UPDATED_RESTAURANT_NAME);
    }

    @Test
    @Transactional
    void getAllRestaurantsByRestaurantNameIsInShouldWork() throws Exception {
        // Initialize the database
        restaurantRepository.saveAndFlush(restaurant);

        // Get all the restaurantList where restaurantName in DEFAULT_RESTAURANT_NAME or UPDATED_RESTAURANT_NAME
        defaultRestaurantShouldBeFound("restaurantName.in=" + DEFAULT_RESTAURANT_NAME + "," + UPDATED_RESTAURANT_NAME);

        // Get all the restaurantList where restaurantName equals to UPDATED_RESTAURANT_NAME
        defaultRestaurantShouldNotBeFound("restaurantName.in=" + UPDATED_RESTAURANT_NAME);
    }

    @Test
    @Transactional
    void getAllRestaurantsByRestaurantNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        restaurantRepository.saveAndFlush(restaurant);

        // Get all the restaurantList where restaurantName is not null
        defaultRestaurantShouldBeFound("restaurantName.specified=true");

        // Get all the restaurantList where restaurantName is null
        defaultRestaurantShouldNotBeFound("restaurantName.specified=false");
    }

    @Test
    @Transactional
    void getAllRestaurantsByRestaurantNameContainsSomething() throws Exception {
        // Initialize the database
        restaurantRepository.saveAndFlush(restaurant);

        // Get all the restaurantList where restaurantName contains DEFAULT_RESTAURANT_NAME
        defaultRestaurantShouldBeFound("restaurantName.contains=" + DEFAULT_RESTAURANT_NAME);

        // Get all the restaurantList where restaurantName contains UPDATED_RESTAURANT_NAME
        defaultRestaurantShouldNotBeFound("restaurantName.contains=" + UPDATED_RESTAURANT_NAME);
    }

    @Test
    @Transactional
    void getAllRestaurantsByRestaurantNameNotContainsSomething() throws Exception {
        // Initialize the database
        restaurantRepository.saveAndFlush(restaurant);

        // Get all the restaurantList where restaurantName does not contain DEFAULT_RESTAURANT_NAME
        defaultRestaurantShouldNotBeFound("restaurantName.doesNotContain=" + DEFAULT_RESTAURANT_NAME);

        // Get all the restaurantList where restaurantName does not contain UPDATED_RESTAURANT_NAME
        defaultRestaurantShouldBeFound("restaurantName.doesNotContain=" + UPDATED_RESTAURANT_NAME);
    }

    @Test
    @Transactional
    void getAllRestaurantsByDeliveryPriceIsEqualToSomething() throws Exception {
        // Initialize the database
        restaurantRepository.saveAndFlush(restaurant);

        // Get all the restaurantList where deliveryPrice equals to DEFAULT_DELIVERY_PRICE
        defaultRestaurantShouldBeFound("deliveryPrice.equals=" + DEFAULT_DELIVERY_PRICE);

        // Get all the restaurantList where deliveryPrice equals to UPDATED_DELIVERY_PRICE
        defaultRestaurantShouldNotBeFound("deliveryPrice.equals=" + UPDATED_DELIVERY_PRICE);
    }

    @Test
    @Transactional
    void getAllRestaurantsByDeliveryPriceIsNotEqualToSomething() throws Exception {
        // Initialize the database
        restaurantRepository.saveAndFlush(restaurant);

        // Get all the restaurantList where deliveryPrice not equals to DEFAULT_DELIVERY_PRICE
        defaultRestaurantShouldNotBeFound("deliveryPrice.notEquals=" + DEFAULT_DELIVERY_PRICE);

        // Get all the restaurantList where deliveryPrice not equals to UPDATED_DELIVERY_PRICE
        defaultRestaurantShouldBeFound("deliveryPrice.notEquals=" + UPDATED_DELIVERY_PRICE);
    }

    @Test
    @Transactional
    void getAllRestaurantsByDeliveryPriceIsInShouldWork() throws Exception {
        // Initialize the database
        restaurantRepository.saveAndFlush(restaurant);

        // Get all the restaurantList where deliveryPrice in DEFAULT_DELIVERY_PRICE or UPDATED_DELIVERY_PRICE
        defaultRestaurantShouldBeFound("deliveryPrice.in=" + DEFAULT_DELIVERY_PRICE + "," + UPDATED_DELIVERY_PRICE);

        // Get all the restaurantList where deliveryPrice equals to UPDATED_DELIVERY_PRICE
        defaultRestaurantShouldNotBeFound("deliveryPrice.in=" + UPDATED_DELIVERY_PRICE);
    }

    @Test
    @Transactional
    void getAllRestaurantsByDeliveryPriceIsNullOrNotNull() throws Exception {
        // Initialize the database
        restaurantRepository.saveAndFlush(restaurant);

        // Get all the restaurantList where deliveryPrice is not null
        defaultRestaurantShouldBeFound("deliveryPrice.specified=true");

        // Get all the restaurantList where deliveryPrice is null
        defaultRestaurantShouldNotBeFound("deliveryPrice.specified=false");
    }

    @Test
    @Transactional
    void getAllRestaurantsByDeliveryPriceContainsSomething() throws Exception {
        // Initialize the database
        restaurantRepository.saveAndFlush(restaurant);

        // Get all the restaurantList where deliveryPrice contains DEFAULT_DELIVERY_PRICE
        defaultRestaurantShouldBeFound("deliveryPrice.contains=" + DEFAULT_DELIVERY_PRICE);

        // Get all the restaurantList where deliveryPrice contains UPDATED_DELIVERY_PRICE
        defaultRestaurantShouldNotBeFound("deliveryPrice.contains=" + UPDATED_DELIVERY_PRICE);
    }

    @Test
    @Transactional
    void getAllRestaurantsByDeliveryPriceNotContainsSomething() throws Exception {
        // Initialize the database
        restaurantRepository.saveAndFlush(restaurant);

        // Get all the restaurantList where deliveryPrice does not contain DEFAULT_DELIVERY_PRICE
        defaultRestaurantShouldNotBeFound("deliveryPrice.doesNotContain=" + DEFAULT_DELIVERY_PRICE);

        // Get all the restaurantList where deliveryPrice does not contain UPDATED_DELIVERY_PRICE
        defaultRestaurantShouldBeFound("deliveryPrice.doesNotContain=" + UPDATED_DELIVERY_PRICE);
    }

    @Test
    @Transactional
    void getAllRestaurantsByRestaurantAddressIsEqualToSomething() throws Exception {
        // Initialize the database
        restaurantRepository.saveAndFlush(restaurant);

        // Get all the restaurantList where restaurantAddress equals to DEFAULT_RESTAURANT_ADDRESS
        defaultRestaurantShouldBeFound("restaurantAddress.equals=" + DEFAULT_RESTAURANT_ADDRESS);

        // Get all the restaurantList where restaurantAddress equals to UPDATED_RESTAURANT_ADDRESS
        defaultRestaurantShouldNotBeFound("restaurantAddress.equals=" + UPDATED_RESTAURANT_ADDRESS);
    }

    @Test
    @Transactional
    void getAllRestaurantsByRestaurantAddressIsNotEqualToSomething() throws Exception {
        // Initialize the database
        restaurantRepository.saveAndFlush(restaurant);

        // Get all the restaurantList where restaurantAddress not equals to DEFAULT_RESTAURANT_ADDRESS
        defaultRestaurantShouldNotBeFound("restaurantAddress.notEquals=" + DEFAULT_RESTAURANT_ADDRESS);

        // Get all the restaurantList where restaurantAddress not equals to UPDATED_RESTAURANT_ADDRESS
        defaultRestaurantShouldBeFound("restaurantAddress.notEquals=" + UPDATED_RESTAURANT_ADDRESS);
    }

    @Test
    @Transactional
    void getAllRestaurantsByRestaurantAddressIsInShouldWork() throws Exception {
        // Initialize the database
        restaurantRepository.saveAndFlush(restaurant);

        // Get all the restaurantList where restaurantAddress in DEFAULT_RESTAURANT_ADDRESS or UPDATED_RESTAURANT_ADDRESS
        defaultRestaurantShouldBeFound("restaurantAddress.in=" + DEFAULT_RESTAURANT_ADDRESS + "," + UPDATED_RESTAURANT_ADDRESS);

        // Get all the restaurantList where restaurantAddress equals to UPDATED_RESTAURANT_ADDRESS
        defaultRestaurantShouldNotBeFound("restaurantAddress.in=" + UPDATED_RESTAURANT_ADDRESS);
    }

    @Test
    @Transactional
    void getAllRestaurantsByRestaurantAddressIsNullOrNotNull() throws Exception {
        // Initialize the database
        restaurantRepository.saveAndFlush(restaurant);

        // Get all the restaurantList where restaurantAddress is not null
        defaultRestaurantShouldBeFound("restaurantAddress.specified=true");

        // Get all the restaurantList where restaurantAddress is null
        defaultRestaurantShouldNotBeFound("restaurantAddress.specified=false");
    }

    @Test
    @Transactional
    void getAllRestaurantsByRestaurantAddressContainsSomething() throws Exception {
        // Initialize the database
        restaurantRepository.saveAndFlush(restaurant);

        // Get all the restaurantList where restaurantAddress contains DEFAULT_RESTAURANT_ADDRESS
        defaultRestaurantShouldBeFound("restaurantAddress.contains=" + DEFAULT_RESTAURANT_ADDRESS);

        // Get all the restaurantList where restaurantAddress contains UPDATED_RESTAURANT_ADDRESS
        defaultRestaurantShouldNotBeFound("restaurantAddress.contains=" + UPDATED_RESTAURANT_ADDRESS);
    }

    @Test
    @Transactional
    void getAllRestaurantsByRestaurantAddressNotContainsSomething() throws Exception {
        // Initialize the database
        restaurantRepository.saveAndFlush(restaurant);

        // Get all the restaurantList where restaurantAddress does not contain DEFAULT_RESTAURANT_ADDRESS
        defaultRestaurantShouldNotBeFound("restaurantAddress.doesNotContain=" + DEFAULT_RESTAURANT_ADDRESS);

        // Get all the restaurantList where restaurantAddress does not contain UPDATED_RESTAURANT_ADDRESS
        defaultRestaurantShouldBeFound("restaurantAddress.doesNotContain=" + UPDATED_RESTAURANT_ADDRESS);
    }

    @Test
    @Transactional
    void getAllRestaurantsByRestaurantCityIsEqualToSomething() throws Exception {
        // Initialize the database
        restaurantRepository.saveAndFlush(restaurant);

        // Get all the restaurantList where restaurantCity equals to DEFAULT_RESTAURANT_CITY
        defaultRestaurantShouldBeFound("restaurantCity.equals=" + DEFAULT_RESTAURANT_CITY);

        // Get all the restaurantList where restaurantCity equals to UPDATED_RESTAURANT_CITY
        defaultRestaurantShouldNotBeFound("restaurantCity.equals=" + UPDATED_RESTAURANT_CITY);
    }

    @Test
    @Transactional
    void getAllRestaurantsByRestaurantCityIsNotEqualToSomething() throws Exception {
        // Initialize the database
        restaurantRepository.saveAndFlush(restaurant);

        // Get all the restaurantList where restaurantCity not equals to DEFAULT_RESTAURANT_CITY
        defaultRestaurantShouldNotBeFound("restaurantCity.notEquals=" + DEFAULT_RESTAURANT_CITY);

        // Get all the restaurantList where restaurantCity not equals to UPDATED_RESTAURANT_CITY
        defaultRestaurantShouldBeFound("restaurantCity.notEquals=" + UPDATED_RESTAURANT_CITY);
    }

    @Test
    @Transactional
    void getAllRestaurantsByRestaurantCityIsInShouldWork() throws Exception {
        // Initialize the database
        restaurantRepository.saveAndFlush(restaurant);

        // Get all the restaurantList where restaurantCity in DEFAULT_RESTAURANT_CITY or UPDATED_RESTAURANT_CITY
        defaultRestaurantShouldBeFound("restaurantCity.in=" + DEFAULT_RESTAURANT_CITY + "," + UPDATED_RESTAURANT_CITY);

        // Get all the restaurantList where restaurantCity equals to UPDATED_RESTAURANT_CITY
        defaultRestaurantShouldNotBeFound("restaurantCity.in=" + UPDATED_RESTAURANT_CITY);
    }

    @Test
    @Transactional
    void getAllRestaurantsByRestaurantCityIsNullOrNotNull() throws Exception {
        // Initialize the database
        restaurantRepository.saveAndFlush(restaurant);

        // Get all the restaurantList where restaurantCity is not null
        defaultRestaurantShouldBeFound("restaurantCity.specified=true");

        // Get all the restaurantList where restaurantCity is null
        defaultRestaurantShouldNotBeFound("restaurantCity.specified=false");
    }

    @Test
    @Transactional
    void getAllRestaurantsByRestaurantCityContainsSomething() throws Exception {
        // Initialize the database
        restaurantRepository.saveAndFlush(restaurant);

        // Get all the restaurantList where restaurantCity contains DEFAULT_RESTAURANT_CITY
        defaultRestaurantShouldBeFound("restaurantCity.contains=" + DEFAULT_RESTAURANT_CITY);

        // Get all the restaurantList where restaurantCity contains UPDATED_RESTAURANT_CITY
        defaultRestaurantShouldNotBeFound("restaurantCity.contains=" + UPDATED_RESTAURANT_CITY);
    }

    @Test
    @Transactional
    void getAllRestaurantsByRestaurantCityNotContainsSomething() throws Exception {
        // Initialize the database
        restaurantRepository.saveAndFlush(restaurant);

        // Get all the restaurantList where restaurantCity does not contain DEFAULT_RESTAURANT_CITY
        defaultRestaurantShouldNotBeFound("restaurantCity.doesNotContain=" + DEFAULT_RESTAURANT_CITY);

        // Get all the restaurantList where restaurantCity does not contain UPDATED_RESTAURANT_CITY
        defaultRestaurantShouldBeFound("restaurantCity.doesNotContain=" + UPDATED_RESTAURANT_CITY);
    }

    @Test
    @Transactional
    void getAllRestaurantsByProposesIsEqualToSomething() throws Exception {
        // Initialize the database
        restaurantRepository.saveAndFlush(restaurant);
        Produit proposes = ProduitResourceIT.createEntity(em);
        em.persist(proposes);
        em.flush();
        restaurant.addProposes(proposes);
        restaurantRepository.saveAndFlush(restaurant);
        Long proposesId = proposes.getId();

        // Get all the restaurantList where proposes equals to proposesId
        defaultRestaurantShouldBeFound("proposesId.equals=" + proposesId);

        // Get all the restaurantList where proposes equals to (proposesId + 1)
        defaultRestaurantShouldNotBeFound("proposesId.equals=" + (proposesId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultRestaurantShouldBeFound(String filter) throws Exception {
        restRestaurantMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(restaurant.getId().intValue())))
            .andExpect(jsonPath("$.[*].restaurantName").value(hasItem(DEFAULT_RESTAURANT_NAME)))
            .andExpect(jsonPath("$.[*].deliveryPrice").value(hasItem(DEFAULT_DELIVERY_PRICE)))
            .andExpect(jsonPath("$.[*].restaurantAddress").value(hasItem(DEFAULT_RESTAURANT_ADDRESS)))
            .andExpect(jsonPath("$.[*].restaurantCity").value(hasItem(DEFAULT_RESTAURANT_CITY)));

        // Check, that the count call also returns 1
        restRestaurantMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultRestaurantShouldNotBeFound(String filter) throws Exception {
        restRestaurantMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restRestaurantMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingRestaurant() throws Exception {
        // Get the restaurant
        restRestaurantMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewRestaurant() throws Exception {
        // Initialize the database
        restaurantRepository.saveAndFlush(restaurant);

        int databaseSizeBeforeUpdate = restaurantRepository.findAll().size();

        // Update the restaurant
        Restaurant updatedRestaurant = restaurantRepository.findById(restaurant.getId()).get();
        // Disconnect from session so that the updates on updatedRestaurant are not directly saved in db
        em.detach(updatedRestaurant);
        updatedRestaurant
            .restaurantName(UPDATED_RESTAURANT_NAME)
            .deliveryPrice(UPDATED_DELIVERY_PRICE)
            .restaurantAddress(UPDATED_RESTAURANT_ADDRESS)
            .restaurantCity(UPDATED_RESTAURANT_CITY);
        RestaurantDTO restaurantDTO = restaurantMapper.toDto(updatedRestaurant);

        restRestaurantMockMvc
            .perform(
                put(ENTITY_API_URL_ID, restaurantDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(restaurantDTO))
            )
            .andExpect(status().isOk());

        // Validate the Restaurant in the database
        List<Restaurant> restaurantList = restaurantRepository.findAll();
        assertThat(restaurantList).hasSize(databaseSizeBeforeUpdate);
        Restaurant testRestaurant = restaurantList.get(restaurantList.size() - 1);
        assertThat(testRestaurant.getRestaurantName()).isEqualTo(UPDATED_RESTAURANT_NAME);
        assertThat(testRestaurant.getDeliveryPrice()).isEqualTo(UPDATED_DELIVERY_PRICE);
        assertThat(testRestaurant.getRestaurantAddress()).isEqualTo(UPDATED_RESTAURANT_ADDRESS);
        assertThat(testRestaurant.getRestaurantCity()).isEqualTo(UPDATED_RESTAURANT_CITY);
    }

    @Test
    @Transactional
    void putNonExistingRestaurant() throws Exception {
        int databaseSizeBeforeUpdate = restaurantRepository.findAll().size();
        restaurant.setId(count.incrementAndGet());

        // Create the Restaurant
        RestaurantDTO restaurantDTO = restaurantMapper.toDto(restaurant);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRestaurantMockMvc
            .perform(
                put(ENTITY_API_URL_ID, restaurantDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(restaurantDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Restaurant in the database
        List<Restaurant> restaurantList = restaurantRepository.findAll();
        assertThat(restaurantList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchRestaurant() throws Exception {
        int databaseSizeBeforeUpdate = restaurantRepository.findAll().size();
        restaurant.setId(count.incrementAndGet());

        // Create the Restaurant
        RestaurantDTO restaurantDTO = restaurantMapper.toDto(restaurant);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRestaurantMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(restaurantDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Restaurant in the database
        List<Restaurant> restaurantList = restaurantRepository.findAll();
        assertThat(restaurantList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamRestaurant() throws Exception {
        int databaseSizeBeforeUpdate = restaurantRepository.findAll().size();
        restaurant.setId(count.incrementAndGet());

        // Create the Restaurant
        RestaurantDTO restaurantDTO = restaurantMapper.toDto(restaurant);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRestaurantMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(restaurantDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Restaurant in the database
        List<Restaurant> restaurantList = restaurantRepository.findAll();
        assertThat(restaurantList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateRestaurantWithPatch() throws Exception {
        // Initialize the database
        restaurantRepository.saveAndFlush(restaurant);

        int databaseSizeBeforeUpdate = restaurantRepository.findAll().size();

        // Update the restaurant using partial update
        Restaurant partialUpdatedRestaurant = new Restaurant();
        partialUpdatedRestaurant.setId(restaurant.getId());

        restRestaurantMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRestaurant.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedRestaurant))
            )
            .andExpect(status().isOk());

        // Validate the Restaurant in the database
        List<Restaurant> restaurantList = restaurantRepository.findAll();
        assertThat(restaurantList).hasSize(databaseSizeBeforeUpdate);
        Restaurant testRestaurant = restaurantList.get(restaurantList.size() - 1);
        assertThat(testRestaurant.getRestaurantName()).isEqualTo(DEFAULT_RESTAURANT_NAME);
        assertThat(testRestaurant.getDeliveryPrice()).isEqualTo(DEFAULT_DELIVERY_PRICE);
        assertThat(testRestaurant.getRestaurantAddress()).isEqualTo(DEFAULT_RESTAURANT_ADDRESS);
        assertThat(testRestaurant.getRestaurantCity()).isEqualTo(DEFAULT_RESTAURANT_CITY);
    }

    @Test
    @Transactional
    void fullUpdateRestaurantWithPatch() throws Exception {
        // Initialize the database
        restaurantRepository.saveAndFlush(restaurant);

        int databaseSizeBeforeUpdate = restaurantRepository.findAll().size();

        // Update the restaurant using partial update
        Restaurant partialUpdatedRestaurant = new Restaurant();
        partialUpdatedRestaurant.setId(restaurant.getId());

        partialUpdatedRestaurant
            .restaurantName(UPDATED_RESTAURANT_NAME)
            .deliveryPrice(UPDATED_DELIVERY_PRICE)
            .restaurantAddress(UPDATED_RESTAURANT_ADDRESS)
            .restaurantCity(UPDATED_RESTAURANT_CITY);

        restRestaurantMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRestaurant.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedRestaurant))
            )
            .andExpect(status().isOk());

        // Validate the Restaurant in the database
        List<Restaurant> restaurantList = restaurantRepository.findAll();
        assertThat(restaurantList).hasSize(databaseSizeBeforeUpdate);
        Restaurant testRestaurant = restaurantList.get(restaurantList.size() - 1);
        assertThat(testRestaurant.getRestaurantName()).isEqualTo(UPDATED_RESTAURANT_NAME);
        assertThat(testRestaurant.getDeliveryPrice()).isEqualTo(UPDATED_DELIVERY_PRICE);
        assertThat(testRestaurant.getRestaurantAddress()).isEqualTo(UPDATED_RESTAURANT_ADDRESS);
        assertThat(testRestaurant.getRestaurantCity()).isEqualTo(UPDATED_RESTAURANT_CITY);
    }

    @Test
    @Transactional
    void patchNonExistingRestaurant() throws Exception {
        int databaseSizeBeforeUpdate = restaurantRepository.findAll().size();
        restaurant.setId(count.incrementAndGet());

        // Create the Restaurant
        RestaurantDTO restaurantDTO = restaurantMapper.toDto(restaurant);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRestaurantMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, restaurantDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(restaurantDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Restaurant in the database
        List<Restaurant> restaurantList = restaurantRepository.findAll();
        assertThat(restaurantList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchRestaurant() throws Exception {
        int databaseSizeBeforeUpdate = restaurantRepository.findAll().size();
        restaurant.setId(count.incrementAndGet());

        // Create the Restaurant
        RestaurantDTO restaurantDTO = restaurantMapper.toDto(restaurant);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRestaurantMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(restaurantDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Restaurant in the database
        List<Restaurant> restaurantList = restaurantRepository.findAll();
        assertThat(restaurantList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamRestaurant() throws Exception {
        int databaseSizeBeforeUpdate = restaurantRepository.findAll().size();
        restaurant.setId(count.incrementAndGet());

        // Create the Restaurant
        RestaurantDTO restaurantDTO = restaurantMapper.toDto(restaurant);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRestaurantMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(restaurantDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Restaurant in the database
        List<Restaurant> restaurantList = restaurantRepository.findAll();
        assertThat(restaurantList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteRestaurant() throws Exception {
        // Initialize the database
        restaurantRepository.saveAndFlush(restaurant);

        int databaseSizeBeforeDelete = restaurantRepository.findAll().size();

        // Delete the restaurant
        restRestaurantMockMvc
            .perform(delete(ENTITY_API_URL_ID, restaurant.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Restaurant> restaurantList = restaurantRepository.findAll();
        assertThat(restaurantList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
