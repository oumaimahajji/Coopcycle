package com.application.coopcycle.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.application.coopcycle.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SystemePaiementDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(SystemePaiementDTO.class);
        SystemePaiementDTO systemePaiementDTO1 = new SystemePaiementDTO();
        systemePaiementDTO1.setId(1L);
        SystemePaiementDTO systemePaiementDTO2 = new SystemePaiementDTO();
        assertThat(systemePaiementDTO1).isNotEqualTo(systemePaiementDTO2);
        systemePaiementDTO2.setId(systemePaiementDTO1.getId());
        assertThat(systemePaiementDTO1).isEqualTo(systemePaiementDTO2);
        systemePaiementDTO2.setId(2L);
        assertThat(systemePaiementDTO1).isNotEqualTo(systemePaiementDTO2);
        systemePaiementDTO1.setId(null);
        assertThat(systemePaiementDTO1).isNotEqualTo(systemePaiementDTO2);
    }
}
