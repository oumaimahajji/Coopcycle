package com.application.coopcycle.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.application.coopcycle.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PanierDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(PanierDTO.class);
        PanierDTO panierDTO1 = new PanierDTO();
        panierDTO1.setId(1L);
        PanierDTO panierDTO2 = new PanierDTO();
        assertThat(panierDTO1).isNotEqualTo(panierDTO2);
        panierDTO2.setId(panierDTO1.getId());
        assertThat(panierDTO1).isEqualTo(panierDTO2);
        panierDTO2.setId(2L);
        assertThat(panierDTO1).isNotEqualTo(panierDTO2);
        panierDTO1.setId(null);
        assertThat(panierDTO1).isNotEqualTo(panierDTO2);
    }
}
