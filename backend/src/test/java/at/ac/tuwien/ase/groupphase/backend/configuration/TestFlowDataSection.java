package at.ac.tuwien.ase.groupphase.backend.configuration;

public final class TestFlowDataSection implements AutoCloseable {

    private static final ThreadLocal<TestFlowData> CURRENT = new ThreadLocal<>();

    private final TestFlowData previous;

    private TestFlowDataSection(TestFlowData previous) {
        this.previous = previous;
    }

    public static TestFlowDataSection activate() {
        TestFlowData previous = CURRENT.get();
        CURRENT.set(new TestFlowData());
        return new TestFlowDataSection(previous);
    }

    public static TestFlowData current() {
        TestFlowData data = CURRENT.get();
        if (data == null) {
            throw new IllegalStateException(
                "No active TestFlowDataSection."
            );
        }
        return data;
    }

    public static void setToken(String token) {
        current().setToken(token);
    }

    @Override
    public void close() {
        if (previous == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(previous);
        }
    }
}