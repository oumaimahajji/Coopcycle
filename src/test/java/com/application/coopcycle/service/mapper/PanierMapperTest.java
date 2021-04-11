package com.application.coopcycle.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PanierMapperTest {

    private PanierMapper panierMapper;

    @BeforeEach
    public void setUp() {
        panierMapper = new PanierMapperImpl();
    }
}
