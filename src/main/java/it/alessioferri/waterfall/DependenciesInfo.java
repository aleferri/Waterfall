package it.alessioferri.waterfall;

/*-
 * #%L
 * Waterfall
 * %%
 * Copyright (C) 2023 Alessio Ferri
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Collection;

public record DependenciesInfo(Collection<TaskSnapshot> deps) {

    public boolean allDidResolveAs(TaskResult target) {
        boolean ok = true;

        for (var t : deps) {
            ok = ok && t.status().isFinished() && t.result() == target;
        }

        return ok;
    }

    public boolean allDidResolve() {
        boolean ok = true;

        for (var t : deps) {
            ok = ok && t.status().isFinished();
        }

        return ok;
    }

    public boolean anyDidResolveAs(TaskResult target) {
        boolean ok = false;

        for (var t : deps) {
            ok = ok || t.status().isFinished() && t.result() == target;
        }

        return ok;
    }

}
