package com.application.coopcycle.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.application.coopcycle.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CommandeDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(CommandeDTO.class);
        CommandeDTO commandeDTO1 = new CommandeDTO();
        commandeDTO1.setId(1L);
        CommandeDTO commandeDTO2 = new CommandeDTO();
        assertThat(commandeDTO1).isNotEqualTo(commandeDTO2);
        commandeDTO2.setId(commandeDTO1.getId());
        assertThat(commandeDTO1).isEqualTo(commandeDTO2);
        commandeDTO2.setId(2L);
        assertThat(commandeDTO1).isNotEqualTo(commandeDTO2);
        commandeDTO1.setId(null);
        assertThat(commandeDTO1).isNotEqualTo(commandeDTO2);
    }
}
