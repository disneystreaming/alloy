package alloy.openapi;

public final class OpenApiConfigExtension {

    private boolean enableMultipleExamples = false;

    public void setEnableMultipleExamples(boolean enableMultipleExamples) {
        this.enableMultipleExamples = enableMultipleExamples;
    }

    public boolean getEnableMultipleExamples() {
        return this.enableMultipleExamples;
    }

    @Override
    public String toString() {
        return "OpenApiConfigExtension{" +
                "enableMultipleExamples=" + enableMultipleExamples +
                '}';
    }
}
