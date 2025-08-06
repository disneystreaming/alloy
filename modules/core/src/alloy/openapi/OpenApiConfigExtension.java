package alloy.openapi;

public final class OpenApiConfigExtension {


    // OpenAPI 3.1 supports multiple examples, but Swagger UI do not (see https://github.com/swagger-api/swagger-ui/issues/10503), so this feature is hidden by default.
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
