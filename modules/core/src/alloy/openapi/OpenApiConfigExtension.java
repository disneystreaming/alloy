/* Copyright 2022 Disney Streaming
 *
 * Licensed under the Tomorrow Open Source Technology License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://disneystreaming.github.io/TOST-1.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package alloy.openapi;

public final class OpenApiConfigExtension {


    // OpenAPI 3.1 supports multiple examples, but Swagger UI do not (see https://github.com/swagger-api/swagger-ui/issues/10503), so this feature is hidden by default.
    private boolean enableMultipleExamples = false;

    private boolean enableOpenEnumsExtension = false;

    public void setEnableMultipleExamples(boolean enableMultipleExamples) {
        this.enableMultipleExamples = enableMultipleExamples;
    }

    public void setEnableOpenEnumsExtension(boolean enableOpenEnumsExtension) {
        this.enableOpenEnumsExtension = enableOpenEnumsExtension;
    }

    public boolean getEnableMultipleExamples() {
        return this.enableMultipleExamples;
    }

    public boolean getEnableOpenEnumExtensions() {
        return this.enableOpenEnumsExtension;
    }

    @Override
    public String toString() {
        return "OpenApiConfigExtension{" +
                "enableMultipleExamples=" + enableMultipleExamples +
                ", enableOpenEnumsExtension=" + enableOpenEnumsExtension +
                '}';
    }
}
