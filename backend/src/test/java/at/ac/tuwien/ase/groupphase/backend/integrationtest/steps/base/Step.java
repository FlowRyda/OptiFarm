package at.ac.tuwien.ase.groupphase.backend.integrationtest.steps.base;

@FunctionalInterface
public interface Step<Input, Output> {

    Output run(Input input) throws Exception;

    default <N> Step<Input, N> then(Step<Output, N> next) {
        return in -> {
            Output mid = this.run(in);
            return next.run(mid);
        };
    }
}