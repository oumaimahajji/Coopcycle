package com.application.coopcycle.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommandeMapperTest {

    private CommandeMapper commandeMapper;

    @BeforeEach
    public void setUp() {
        commandeMapper = new CommandeMapperImpl();
    }
}
