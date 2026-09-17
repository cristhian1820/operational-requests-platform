package co.com.operationalrequests.acceptance;

import com.intuit.karate.junit5.Karate;

class AcceptanceTest {
    @Karate.Test
    Karate apiAcceptance() {
        return Karate.run("requests-api").relativeTo(getClass());
    }
}
