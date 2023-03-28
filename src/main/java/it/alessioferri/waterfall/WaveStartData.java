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

import java.util.ArrayList;
import java.util.Collection;


public record WaveStartData<E extends Enum<E>, S extends FlowStage<E>>(long parentWave, Delay waveDelay, Collection<S> startSet) {
    
    public static <E extends Enum<E>, S extends FlowStage<E>> WaveStartData<E, S> prepare(long parentWave, Delay waveDelay, S ...set) {
        var l = new ArrayList<S>();

        for (var s : set) {
            l.add(s);
        }

        return new WaveStartData<>(parentWave, waveDelay, l);
    }

}
