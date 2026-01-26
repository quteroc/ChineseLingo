package com.chineselingo.app;

import com.chineselingo.learning.training.TrainingService;
import com.chineselingo.learning.verifying.VerifyingService;

import java.io.IOException;

public class ApplicationService {
    private final AppContext appContext;
    private final TrainingService trainingService;
    private final VerifyingService verifyingService;

    public ApplicationService(AppContext context) {
        this.appContext = context;

        this.trainingService = new TrainingService(context.getUserManager().getUserState(), context.getRecommendationEngine(),
                context.getCharIdMapper(), context.getDefinitions());

        this.verifyingService = new VerifyingService(context.getUserManager().getUserState(), context.getCharIdMapper(), context.getDefinitions());
    }

    public TrainingService training() {
        return trainingService;
    }

    public VerifyingService verifying() {
        return verifyingService;
    }

    public void saveUserState() {
        try {
            appContext.getUserManager().saveUserState();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
