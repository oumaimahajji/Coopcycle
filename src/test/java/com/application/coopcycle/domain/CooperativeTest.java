package com.application.coopcycle.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.application.coopcycle.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CooperativeTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Cooperative.class);
        Cooperative cooperative1 = new Cooperative();
        cooperative1.setId(1L);
        Cooperative cooperative2 = new Cooperative();
        cooperative2.setId(cooperative1.getId());
        assertThat(cooperative1).isEqualTo(cooperative2);
        cooperative2.setId(2L);
        assertThat(cooperative1).isNotEqualTo(cooperative2);
        cooperative1.setId(null);
        assertThat(cooperative1).isNotEqualTo(cooperative2);
    }
}
