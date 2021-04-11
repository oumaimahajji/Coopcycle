package com.application.coopcycle.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.application.coopcycle.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SystemePaiementTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(SystemePaiement.class);
        SystemePaiement systemePaiement1 = new SystemePaiement();
        systemePaiement1.setId(1L);
        SystemePaiement systemePaiement2 = new SystemePaiement();
        systemePaiement2.setId(systemePaiement1.getId());
        assertThat(systemePaiement1).isEqualTo(systemePaiement2);
        systemePaiement2.setId(2L);
        assertThat(systemePaiement1).isNotEqualTo(systemePaiement2);
        systemePaiement1.setId(null);
        assertThat(systemePaiement1).isNotEqualTo(systemePaiement2);
    }
}
