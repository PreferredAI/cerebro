package ai.preferred.cerebro.webservice.movieLens;

import ai.preferred.cerebro.feedback.Interaction;

/**
 * @author hpminh@apcs.vn
 */
public class RateInteraction extends Interaction {

    public RateInteraction() {
        this.description = "User directly rate item";
    }

    @Override
    public void inferScore() {

    }
}
