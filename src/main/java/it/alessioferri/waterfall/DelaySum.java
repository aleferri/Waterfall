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

import java.time.LocalDate;

/**
 * Very easy recursive implementation of Delay sum
 * @param a first Delay
 * @param b second Delay
 */
public record DelaySum(Delay a, Delay b) implements Delay {

    @Override
    public LocalDate addTo(LocalDate date) {
        return b.addTo( a.addTo( date ) );
    }

    @Override
    public Delay add(Delay b) {
        return new DelaySum( this, b );
    }

    @Override
    public boolean isNone() {
        return a.isNone() && b.isNone();
    }

}
