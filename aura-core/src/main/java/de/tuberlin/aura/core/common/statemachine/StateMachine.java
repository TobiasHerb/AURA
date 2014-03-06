package de.tuberlin.aura.core.common.statemachine;

import de.tuberlin.aura.core.common.utils.Pair;

import java.util.*;

public final class StateMachine {

    // Disallow instantiation.
    private StateMachine() {
    }

    public static final class FiniteStateMachineBuilder<S extends Enum<S>,T extends Enum<T>> {

        public final class TransitionBuilder<S extends Enum<S>, T extends Enum<T>> {

            public TransitionBuilder(final FiniteStateMachineBuilder<S,T> fsmBuilder) {
                // snanity check.
                if(fsmBuilder == null)
                    throw new IllegalArgumentException("fsmBuilder == null");

                this.fsmBuilder = fsmBuilder;
            }

            private final FiniteStateMachineBuilder<S,T> fsmBuilder;

            private S currentState;

            private TransitionBuilder<S,T> currentState(final S state) {
                // Sanity check.
                if(state == null)
                    throw new IllegalArgumentException("state == null");
                this.currentState = state;
                return this;
            }

            public FiniteStateMachineBuilder<S,T> addTransition(final T transition, final S nextState) {
                // Sanity check.
                if(transition == null)
                    throw new IllegalArgumentException("transition == null");
                if(nextState == null)
                    throw new IllegalArgumentException("nextState == null");

                final Map<T, S> transitionMap = fsmBuilder.stateTransitionMtx.get(currentState).getSecond();

                if(transitionMap.get(transition) != errorState) {
                    throw new IllegalStateException("transition already defined");
                }

                transitionMap.put(transition, nextState);

                return fsmBuilder;
            }

            public FiniteStateMachineBuilder<S,T> noTransition() {
                return fsmBuilder;
            }
        }

        private final S errorState;

        private final Class<T> transitionClazz;

        private final Map<S,Pair<StateAction<S,T>,Map<T,S>>> stateTransitionMtx;

        private final TransitionBuilder<S,T> transitionBuilder;


        public FiniteStateMachineBuilder(final Class<S> stateClazz, final Class<T> transitionClazz, final S errorState) {
            // sanity check.
            if(stateClazz == null)
                throw new IllegalArgumentException("stateClazz == null");
            if(transitionClazz == null)
                throw new IllegalArgumentException("transitionClazz == null");

            this.stateTransitionMtx = new HashMap<>();
            // Fill the matrix with all possible states.
            for(final S state : stateClazz.getEnumConstants()) {
                stateTransitionMtx.put(state,null);
            }

            this.errorState = errorState;

            this.transitionClazz = transitionClazz;

            this.transitionBuilder = new TransitionBuilder<>(this);
        }

        public TransitionBuilder<S,T> defineState(final S state, final StateAction<S,T> action) {
            // Sanity check.
            if(state == null)
                throw new IllegalArgumentException("state == null");

            if(stateTransitionMtx.get(state) != null)
                throw new IllegalStateException("state already defined");

            final Map<T, S> transitionMap = new HashMap<>();
            for(final T transition : transitionClazz.getEnumConstants()) {
                transitionMap.put(transition, errorState);
            }

            stateTransitionMtx.put(state, new Pair<>(action, transitionMap));

            return transitionBuilder.currentState(state);
        }

        public TransitionBuilder<S,T> defineState(final S state) {
            return defineState(state, null);
        }

        public TransitionBuilder<S,T> and() {
            return transitionBuilder;
        }

        public FiniteStateMachine<S,T> build(final S initialState) {
            return new FiniteStateMachine<>(stateTransitionMtx, initialState, errorState);
        }
    }

    public static final class FiniteStateMachine<S,T> {

        private final Map<S, Pair<StateAction<S,T>, Map<T, S>>> stateTransitionMtx;

        private final S errorState;

        private S currentState;

        public FiniteStateMachine(final Map<S, Pair<StateAction<S,T>, Map<T, S>>> stateTransitionMtx, final S initialState, final S errorState) {
            // Sanity check.
            if(stateTransitionMtx == null)
                throw new IllegalArgumentException("stateTransitionMtx == null");
            if(initialState == null)
                throw new IllegalStateException("initialState == null");
            if(errorState == null)
                throw new IllegalStateException("errorState == null");

            this.stateTransitionMtx = stateTransitionMtx;

            this.currentState = initialState;

            this.errorState = errorState;
        }

        public void start() {
            stateTransitionMtx.get(currentState).getFirst().stateAction(null, null, currentState);
        }

        public void doTransition(final T transition) {
            // sanity check.
            if(transition == null)
                throw new IllegalArgumentException("transition == null ");

            if(currentState instanceof StateBase) {
                final StateBase state = (StateBase)currentState;
                if(state.isFinalState()) {
                    throw new IllegalStateException(state + " is a final state, no transition allowed");
                }
            }

            final Pair<StateAction<S,T>,Map<T,S>> transitionSpace = stateTransitionMtx.get(currentState);
            final S nextState = transitionSpace.getSecond().get(transition);

            if(nextState == errorState) {
                transitionSpace.getFirst().stateAction(currentState, transition, nextState);
            } else {
                stateTransitionMtx.get(nextState).getFirst().stateAction(currentState, transition, nextState);
            }

            currentState = nextState;
        }
    }

    public static interface StateAction<S,T> {

        public void stateAction(final S previousState, final T transition, final S state);
    }

    public static class StateContext {

    }

    public static interface StateBase {

        public boolean isFinalState();
    }

    /**
     *
     */
    public static enum TopologyState implements StateBase {

        TOPOLOGY_STATE_CREATED(false),

        TOPOLOGY_STATE_PARALLELIZED(false),

        TOPOLOGY_STATE_SCHEDULED(false),

        TOPOLOGY_STATE_DEPLOYED(false),

        TOPOLOGY_STATE_RUNNING(false),

        TOPOLOGY_STATE_FINISHED(true),

        TOPOLOGY_STATE_FAILURE(true),

        ERROR(true);

        TopologyState(final boolean finalState) {

            this.finalState = finalState;

            this.context = new StateContext();
        }

        public final boolean finalState;

        public final StateContext context;

        public boolean isFinalState() {
            return finalState;
        }
    }

    /**
     *
     */
    public enum TopologyTransition {

        TOPOLOGY_TRANSITION_PARALLELIZE,

        TOPOLOGY_TRANSITION_SCHEDULE,

        TOPOLOGY_TRANSITION_DEPLOY,

        TOPOLOGY_TRANSITION_RUN,

        TOPOLOGY_TRANSITION_FINISH,

        TOPOLOGY_TRANSITION_FAIL
    }

    // ---------------------------------------------------
    // Entry Point.
    // ---------------------------------------------------

    public static void main(final String[] args) {

        final StateAction<TopologyState,TopologyTransition> action = new StateAction<TopologyState, TopologyTransition>() {
            @Override
            public void stateAction(TopologyState previousState, TopologyTransition transition, TopologyState state) {
                System.out.println( "previousState = " + previousState
                        + " - transition = " + transition
                        + " - state = " + state);
            }
        };

        final FiniteStateMachineBuilder<TopologyState,TopologyTransition> fsmBuilder
                = new FiniteStateMachineBuilder<>(TopologyState.class, TopologyTransition.class, TopologyState.ERROR);

        final FiniteStateMachine<TopologyState, TopologyTransition> topologyFSM = fsmBuilder
            .defineState(TopologyState.TOPOLOGY_STATE_CREATED, action)
                .addTransition(TopologyTransition.TOPOLOGY_TRANSITION_PARALLELIZE, TopologyState.TOPOLOGY_STATE_PARALLELIZED)
            .defineState(TopologyState.TOPOLOGY_STATE_PARALLELIZED, action)
                .addTransition(TopologyTransition.TOPOLOGY_TRANSITION_SCHEDULE, TopologyState.TOPOLOGY_STATE_SCHEDULED)
            .defineState(TopologyState.TOPOLOGY_STATE_SCHEDULED, action)
                .addTransition(TopologyTransition.TOPOLOGY_TRANSITION_DEPLOY, TopologyState.TOPOLOGY_STATE_DEPLOYED)
            .defineState(TopologyState.TOPOLOGY_STATE_DEPLOYED, action)
                .addTransition(TopologyTransition.TOPOLOGY_TRANSITION_RUN, TopologyState.TOPOLOGY_STATE_RUNNING)
            .defineState(TopologyState.TOPOLOGY_STATE_RUNNING, action)
                .addTransition(TopologyTransition.TOPOLOGY_TRANSITION_FINISH, TopologyState.TOPOLOGY_STATE_FINISHED)
                .and().addTransition(TopologyTransition.TOPOLOGY_TRANSITION_FAIL, TopologyState.TOPOLOGY_STATE_FAILURE)
            .defineState(TopologyState.TOPOLOGY_STATE_FINISHED, action)
                .noTransition()
            .defineState(TopologyState.TOPOLOGY_STATE_FAILURE, action)
                .noTransition()
            .defineState(TopologyState.ERROR)
                .noTransition()
            .build(TopologyState.TOPOLOGY_STATE_CREATED);


        topologyFSM.start();

        topologyFSM.doTransition(TopologyTransition.TOPOLOGY_TRANSITION_PARALLELIZE);

        topologyFSM.doTransition(TopologyTransition.TOPOLOGY_TRANSITION_SCHEDULE);

        topologyFSM.doTransition(TopologyTransition.TOPOLOGY_TRANSITION_DEPLOY);

        topologyFSM.doTransition(TopologyTransition.TOPOLOGY_TRANSITION_RUN);

        topologyFSM.doTransition(TopologyTransition.TOPOLOGY_TRANSITION_FINISH);

        //topologyFSM.doTransition(TopologyTransition.TOPOLOGY_TRANSITION_RUN);

    }
}
